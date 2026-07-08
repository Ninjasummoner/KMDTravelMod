package com.kmdtravel.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;

public final class WorldMapSampler {
    private WorldMapSampler() {
    }

    public static int sample(Minecraft minecraft, long seed, int worldX, int worldZ) {
        Level level = minecraft.level;
        if (level != null) {
            return sampleFromLevel(level, seed, worldX, worldZ);
        }
        return placeholder(seed, ResourceLocation.parse("overworld"), worldX, worldZ);
    }

    public static boolean hasLoadedColumn(Minecraft minecraft, int worldX, int worldZ) {
        Level level = minecraft.level;
        return level != null && level.hasChunkAt(new BlockPos(worldX, 0, worldZ));
    }

    public static int placeholder(long seed, ResourceLocation dimension, int worldX, int worldZ) {
        return 0xFF9C9C9C;
    }

    private static int sampleFromLevel(Level level, long seed, int worldX, int worldZ) {
        BlockPos column = new BlockPos(worldX, 0, worldZ);
        if (!level.hasChunkAt(column)) {
            return placeholder(seed, level.dimension().location(), worldX, worldZ);
        }
        if (level.dimension() == Level.NETHER) {
            return sampleNetherColumn(level, seed, worldX, worldZ);
        }

        BlockPos surface = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, new BlockPos(worldX, 0, worldZ));
        BlockPos.MutableBlockPos cursor = surface.mutable();

        for (int y = surface.getY(); y >= level.getMinBuildHeight(); y--) {
            cursor.setY(y);
            if (!level.hasChunkAt(cursor)) {
                break;
            }

            BlockState state = level.getBlockState(cursor);
            if (state.isAir() || state.is(Blocks.BEDROCK)) {
                continue;
            }

            FluidState fluid = state.getFluidState();
            if (!fluid.isEmpty()) {
                if (fluid.is(FluidTags.LAVA) || state.is(Blocks.LAVA)) {
                    return shadeWithRelief(level, worldX, worldZ, y, 0xFFFF4A12, 0.82D);
                }
                Holder<Biome> biome = level.getBiome(cursor);
                return shadeWithRelief(level, worldX, worldZ, y, enrichWaterColor(0xFF000000 | biome.value().getWaterColor(), y), 0.70D);
            }

            MapColor mapColor = state.getMapColor(level, cursor);
            if (mapColor == MapColor.NONE) {
                continue;
            }

            int color = resolveMapColor(mapColor, level.getBiome(cursor), worldX, worldZ);
            color = enrichSurfaceColor(color, state, worldX, worldZ);
            return shadeWithRelief(level, worldX, worldZ, y, color, state.is(BlockTags.LEAVES) ? 1.15D : 1.0D);
        }

        return placeholder(seed, level.dimension().location(), worldX, worldZ);
    }

    private static int sampleNetherColumn(Level level, long seed, int worldX, int worldZ) {
        Minecraft minecraft = Minecraft.getInstance();
        int playerY = minecraft.player == null ? 64 : minecraft.player.blockPosition().getY();
        int top = Math.max(level.getMinBuildHeight() + 2, Math.min(level.getMaxBuildHeight() - 8, playerY + 8));
        int bottom = Math.max(level.getMinBuildHeight() + 2, playerY - 48);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(worldX, 0, worldZ);
        int blended = 0xFF9C9C9C;
        boolean foundAny = false;

        for (int y = bottom; y <= top; y++) {
            cursor.setY(y);
            BlockState state = level.getBlockState(cursor);
            if (state.isAir() || state.is(Blocks.BEDROCK)) {
                continue;
            }
            FluidState fluid = state.getFluidState();
            if (!fluid.isEmpty()) {
                int lavaColor = netherShade(0xFFFF4A12, y, playerY);
                blended = foundAny ? blend(blended, lavaColor, 0.72D) : lavaColor;
                foundAny = true;
                continue;
            }
            MapColor mapColor = state.getMapColor(level, cursor);
            if (mapColor != MapColor.NONE) {
                int surfaceColor = netherShade(resolveMapColor(mapColor, level.getBiome(cursor), worldX, worldZ), y, playerY);
                double amount = y >= playerY - 4 ? 0.80D : y >= playerY - 16 ? 0.55D : 0.32D;
                blended = foundAny ? blend(blended, surfaceColor, amount) : surfaceColor;
                foundAny = true;
            }
        }

        if (foundAny) {
            return blended;
        }
        return placeholder(seed, level.dimension().location(), worldX, worldZ);
    }

    private static int blend(int base, int overlay, double amount) {
        int red = blendChannel((base >> 16) & 0xFF, (overlay >> 16) & 0xFF, amount);
        int green = blendChannel((base >> 8) & 0xFF, (overlay >> 8) & 0xFF, amount);
        int blue = blendChannel(base & 0xFF, overlay & 0xFF, amount);
        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }

    private static int blendChannel(int base, int overlay, double amount) {
        return Math.max(0, Math.min(255, (int) Math.round(base * (1.0D - amount) + overlay * amount)));
    }

    private static int netherShade(int color, int y, int playerY) {
        double amount = Math.max(-0.20D, Math.min(0.18D, (y - playerY) / 64.0D));
        int red = shadeChannel((color >> 16) & 0xFF, amount);
        int green = shadeChannel((color >> 8) & 0xFF, amount);
        int blue = shadeChannel(color & 0xFF, amount);
        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }

    private static int shade(int color, int y, Level level) {
        double midpoint = (level.getMinBuildHeight() + level.getMaxBuildHeight()) / 2.0D;
        double amount = Math.max(-0.18D, Math.min(0.18D, (y - midpoint) / 420.0D));
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;
        red = shadeChannel(red, amount);
        green = shadeChannel(green, amount);
        blue = shadeChannel(blue, amount);
        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }

    private static int shadeWithRelief(Level level, int worldX, int worldZ, int y, int color, double reliefStrength) {
        double heightAmount = heightShadeAmount(y, level);
        double slopeAmount = slopeShadeAmount(level, worldX, worldZ, y) * reliefStrength;
        double textureAmount = textureShadeAmount(worldX, worldZ) * 0.42D;
        return shadeByAmount(color, Math.max(-0.30D, Math.min(0.30D, heightAmount + slopeAmount + textureAmount)));
    }

    private static double heightShadeAmount(int y, Level level) {
        double midpoint = (level.getMinBuildHeight() + level.getMaxBuildHeight()) / 2.0D;
        return Math.max(-0.14D, Math.min(0.14D, (y - midpoint) / 500.0D));
    }

    private static double slopeShadeAmount(Level level, int worldX, int worldZ, int y) {
        int east = nearbySurfaceY(level, worldX + 1, worldZ, y);
        int south = nearbySurfaceY(level, worldX, worldZ + 1, y);
        int delta = (y - east) + (y - south);
        return Math.max(-0.16D, Math.min(0.16D, delta / 24.0D));
    }

    private static int nearbySurfaceY(Level level, int worldX, int worldZ, int fallbackY) {
        BlockPos pos = new BlockPos(worldX, 0, worldZ);
        if (!level.hasChunkAt(pos)) {
            return fallbackY;
        }
        return level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, pos).getY();
    }

    private static double textureShadeAmount(int worldX, int worldZ) {
        int hash = worldX * 73428767 ^ worldZ * 91227153;
        hash ^= hash >>> 13;
        return (Math.floorMod(hash, 9) - 4) / 255.0D;
    }

    private static int shadeByAmount(int color, double amount) {
        int red = shadeChannel((color >> 16) & 0xFF, amount);
        int green = shadeChannel((color >> 8) & 0xFF, amount);
        int blue = shadeChannel(color & 0xFF, amount);
        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }

    private static int enrichSurfaceColor(int color, BlockState state, int worldX, int worldZ) {
        if (state.is(BlockTags.LEAVES)) {
            return blend(color, 0xFF153F18, 0.18D + Math.floorMod(worldX * 13L + worldZ * 7L, 4) * 0.025D);
        }
        if (state.is(BlockTags.LOGS)) {
            return blend(color, 0xFF4A2D14, 0.24D);
        }
        return color;
    }

    private static int enrichWaterColor(int color, int y) {
        double amount = 0.08D + Math.floorMod(y, 5) * 0.01D;
        return blend(color, 0xFF123C8F, amount);
    }

    private static int shadeChannel(int channel, double amount) {
        int adjusted = amount >= 0.0D
                ? (int) (channel + (255 - channel) * amount)
                : (int) (channel * (1.0D + amount));
        return Math.max(0, Math.min(255, adjusted));
    }

    private static int resolveMapColor(MapColor mapColor, Holder<Biome> biome, int worldX, int worldZ) {
        if (mapColor == MapColor.GRASS) {
            return 0xFF000000 | biome.value().getGrassColor(worldX, worldZ);
        }
        if (mapColor == MapColor.PLANT || mapColor == MapColor.COLOR_GREEN) {
            return 0xFF000000 | biome.value().getFoliageColor();
        }
        if (mapColor == MapColor.WATER) {
            return 0xFF000000 | biome.value().getWaterColor();
        }
        return 0xFF000000 | mapColor.col;
    }

    private static int fallbackColor(long seed, ResourceLocation dimension, int worldX, int worldZ) {
        int regionX = Math.floorDiv(worldX, 224);
        int regionZ = Math.floorDiv(worldZ, 224);
        long hash = seed ^ (regionX * 341873128712L) ^ (regionZ * 132897987541L);
        hash ^= hash >>> 33;
        int pick = (int) Math.floorMod(hash, 10);
        int variation = (int) Math.floorMod((Math.floorDiv(worldX, 32) * 31L + Math.floorDiv(worldZ, 32) * 17L + seed) >> 2, 12) - 6;
        int base = dimension.getPath().equals("the_nether") ? switch (pick) {
            case 0, 1, 2, 3 -> 0xFF7B2720;
            case 4, 5 -> 0xFF4D1A22;
            case 6 -> 0xFFBB5A2B;
            case 7 -> 0xFF2F1B25;
            case 8 -> 0xFF8B5F35;
            default -> 0xFF5E241E;
        } : dimension.getPath().equals("the_end") ? switch (pick) {
            case 0, 1, 2, 3 -> 0xFFD8D0A0;
            case 4, 5 -> 0xFF7D7194;
            case 6 -> 0xFF24222E;
            default -> 0xFFBFB68A;
        } : switch (pick) {
            case 0, 1, 2 -> 0xFF6E9932;
            case 3 -> 0xFF8F7748;
            case 4 -> 0xFF3F76E4;
            case 5 -> 0xFFCFBFA3;
            case 6 -> 0xFF287082;
            case 7 -> 0xFF4F7D3A;
            case 8 -> 0xFFB7A06A;
            default -> 0xFF3A8E8C;
        };
        return vary(base, variation);
    }

    private static int vary(int color, int amount) {
        int red = Math.max(0, Math.min(255, ((color >> 16) & 0xFF) + amount));
        int green = Math.max(0, Math.min(255, ((color >> 8) & 0xFF) + amount));
        int blue = Math.max(0, Math.min(255, (color & 0xFF) + amount));
        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }
}
