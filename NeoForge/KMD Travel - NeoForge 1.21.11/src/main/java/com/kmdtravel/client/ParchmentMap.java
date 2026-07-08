package com.kmdtravel.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.kmdtravel.network.OpenTravelScreenPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class ParchmentMap {
    private static final int TILE_SIZE = 1;
    private static final int TILE_TEXTURE_SIZE = 384;
    private static final int CACHE_LIMIT = 1000000;
    private static final int TEXTURE_CACHE_LIMIT = 512;
    private static final int TILE_BUILDS_PER_FRAME = 2;
    private static final int LOCAL_VIEW_SPAN = 2048;
    private static final double MIN_ZOOM = 1.0D;
    private static final double MAX_ZOOM = 16.0D;
    private static final Identifier PARCHMENT_FRAME = Identifier.fromNamespaceAndPath("kmdtravel", "textures/gui/parchment_frame.png");
    private static final Identifier PARCHMENT_PANEL = Identifier.fromNamespaceAndPath("kmdtravel", "textures/gui/parchment_panel.png");
    private static final int PARCHMENT_TEXTURE_SIZE = 256;
    private static final int FRAME_SLICE = 36;
    private static final int PANEL_SLICE = 28;
    private static int textureSerial;
    private static final Map<Long, CacheEntry> TERRAIN_CACHE = new LinkedHashMap<>(CACHE_LIMIT, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, CacheEntry> eldest) {
            return size() > CACHE_LIMIT;
        }
    };
    private static final Map<TerrainTileKey, TerrainTile> TERRAIN_TILES = new LinkedHashMap<>(TEXTURE_CACHE_LIMIT, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<TerrainTileKey, TerrainTile> eldest) {
            if (size() <= TEXTURE_CACHE_LIMIT) {
                return false;
            }
            Minecraft.getInstance().getTextureManager().release(eldest.getValue().location);
            return true;
        }
    };

    private ParchmentMap() {
    }

    public record Bounds(int x, int y, int w, int h) {
        public boolean contains(double px, double py) {
            return px >= x && px <= x + w && py >= y && py <= y + h;
        }
    }

    public static Bounds bounds(int width, int height) {
        return bounds(width, height, true);
    }

    public static Bounds bounds(int width, int height, boolean listOpen) {
        int side = listOpen ? 166 : 44;
        int top = 48;
        int bottom = 54;
        int right = 42;
        return new Bounds(side, top, width - side - right, height - top - bottom);
    }

    public static Bounds listBounds(int width, int height) {
        Bounds map = bounds(width, height);
        return new Bounds(20, map.y(), 128, map.h());
    }

    public static Bounds listToggleBounds(int width, int height, boolean listOpen) {
        Bounds map = bounds(width, height, listOpen);
        return new Bounds(map.x() - 30, map.y() + 8, 22, 22);
    }

    public static void drawFrame(GuiGraphics graphics, Font font, Component title, int width, int height) {
        drawFrame(graphics, font, title, width, height, true);
    }

    public static void drawFrame(GuiGraphics graphics, Font font, Component title, int width, int height, boolean listOpen) {
        graphics.fill(0, 0, width, height, 0xD8000000);
        Bounds bounds = bounds(width, height, listOpen);
        Bounds listBounds = listBounds(width, height);
        drawBookFrame(graphics, bounds);
        if (listOpen) {
            drawBookPanel(graphics, listBounds);
        }
        int titleX = bounds.x() + bounds.w() / 2 - font.width(title) / 2;
        graphics.fill(titleX - 8, 12, titleX + font.width(title) + 8, 26, 0xAA3A2111);
        graphics.drawString(font, title, titleX, 15, 0xF5D99C, false);
    }

    public static void drawMap(GuiGraphics graphics, Font font, Bounds bounds, long seed, Identifier dimension, List<OpenTravelScreenPacket.Entry> locations,
                               UUID sourceId, Progress progress, double panX, double panY, double zoom, double mouseX, double mouseY) {
        drawMap(graphics, font, bounds, seed, dimension, locations, sourceId, progress, panX, panY, zoom, mouseX, mouseY, true, true, false);
    }

    public static void drawMapWithoutLabels(GuiGraphics graphics, Font font, Bounds bounds, long seed, Identifier dimension, List<OpenTravelScreenPacket.Entry> locations,
                                            UUID sourceId, Progress progress, double panX, double panY, double zoom, double mouseX, double mouseY) {
        drawMap(graphics, font, bounds, seed, dimension, locations, sourceId, progress, panX, panY, zoom, mouseX, mouseY, false, false, false);
    }

    private static void drawMap(GuiGraphics graphics, Font font, Bounds bounds, long seed, Identifier dimension, List<OpenTravelScreenPacket.Entry> locations,
                                UUID sourceId, Progress progress, double panX, double panY, double zoom, double mouseX, double mouseY, boolean allowTerrainRebuild, boolean drawLabels, boolean centerProgressMarker) {
        Identifier mapDimension = ClientMapCache.mapDimension(dimension);
        graphics.fill(bounds.x(), bounds.y(), bounds.x() + bounds.w(), bounds.y() + bounds.h(), 0xFFE9CB88);

        if (locations.isEmpty()) {
            drawParchmentDetails(graphics, bounds);
            graphics.drawCenteredString(font, Component.translatable("screen.kmdtravel.no_locations"), bounds.x() + bounds.w() / 2, bounds.y() + bounds.h() / 2, 0x5A321A);
            return;
        }

        OpenTravelScreenPacket.Entry anchor = anchorLocation(locations, sourceId);
        Scale scale = Scale.of(locations, bounds, panX, panY, zoom, anchor);
        graphics.enableScissor(bounds.x() + 1, bounds.y() + 1, bounds.x() + bounds.w() - 1, bounds.y() + bounds.h() - 1);
        drawTerrainMap(graphics, bounds, scale, seed, mapDimension, allowTerrainRebuild);
        drawParchmentDetails(graphics, bounds);

        if (progress != null && centerProgressMarker) {
            drawPlayerHead(graphics, bounds.x() + bounds.w() / 2 - 6, bounds.y() + bounds.h() / 2 - 6);
        } else if (progress != null) {
            double sx = scale.xDouble(progress.startX());
            double sy = scale.yDouble(progress.startZ());
            double ex = scale.xDouble(progress.endX());
            double ey = scale.yDouble(progress.endZ());
            int lineStartX = (int) Math.round(Math.min(sx, ex));
            int lineEndX = (int) Math.round(Math.max(sx, ex));
            int lineY = (int) Math.round(sy);
            int lineX = (int) Math.round(ex);
            int lineStartY = (int) Math.round(Math.min(sy, ey));
            int lineEndY = (int) Math.round(Math.max(sy, ey));
            graphics.hLine(lineStartX, lineEndX, lineY, 0xAA7D1B1B);
            graphics.vLine(lineX, lineStartY, lineEndY, 0xAA7D1B1B);
            int px = (int) Math.round(sx + (ex - sx) * progress.amount());
            int py = (int) Math.round(sy + (ey - sy) * progress.amount());
            drawPlayerHead(graphics, px - 6, py - 6);
        }

        for (OpenTravelScreenPacket.Entry location : locations) {
            int px = scale.x(location.x());
            int py = scale.y(location.z());
            boolean current = location.id().equals(sourceId);
            boolean hot = Math.abs(mouseX - px) <= 7 && Math.abs(mouseY - py) <= 7;
            int color = current ? 0xFF8B1E1E : 0xFF000000 | (location.markerColor() & 0xFFFFFF);
            drawShieldPin(graphics, px, py, color, location.markerPattern(), hot || location.shared());
            if (!drawLabels) {
                continue;
            }
            drawReadableString(graphics, font, location.name(), px + 8, py - 5, 0x2D2012);
            if (current) {
                graphics.drawCenteredString(font, Component.translatable("screen.kmdtravel.you_are_here"), px, py + 10, 0x6D2716);
            } else if (hot) {
                Component ambush = Component.translatable("screen.kmdtravel.ambush_chance", location.ambushChance());
                drawReadableString(graphics, font, ambush.getString(), px - font.width(ambush) / 2, py + 10, 0x2D2012);
            }
        }
        graphics.disableScissor();
    }

    private static void drawShieldPin(GuiGraphics graphics, int x, int y, int color, int pattern, boolean highlighted) {
        int border = highlighted ? 0xFFFFD36A : 0xFF1F160D;
        graphics.fill(x - 5, y - 8, x + 6, y + 1, border);
        graphics.fill(x - 4, y - 7, x + 5, y + 1, color);
        graphics.fill(x - 3, y + 1, x + 4, y + 4, border);
        graphics.fill(x - 2, y + 1, x + 3, y + 4, color);
        graphics.fill(x - 1, y + 4, x + 2, y + 7, border);
        graphics.fill(x, y + 4, x + 1, y + 7, color);
        int accent = readableAccent(color);
        if ((pattern & 1) != 0) {
            graphics.vLine(x, y - 6, y + 3, accent);
        }
        if ((pattern & 2) != 0) {
            graphics.hLine(x - 3, x + 3, y - 3, accent);
        }
        if ((pattern & 4) != 0) {
            graphics.fill(x - 3, y - 6, x - 1, y - 1, accent);
        }
        if ((pattern & 8) != 0) {
            graphics.fill(x + 1, y - 6, x + 3, y - 1, accent);
        }
    }

    private static int readableAccent(int color) {
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;
        int brightness = red + green + blue;
        return brightness > 420 ? 0xFF2D2012 : 0xFFFFE8A8;
    }

    public static void rememberSamples(long seed, Identifier dimension, List<OpenTravelScreenPacket.MapSample> samples) {
        Identifier mapDimension = ClientMapCache.mapDimension(dimension);
        for (OpenTravelScreenPacket.MapSample sample : samples) {
            rememberSample(seed, mapDimension, sample.x(), sample.z(), sample.color());
        }
    }

    public static void rememberSample(long seed, Identifier dimension, int worldX, int worldZ, int color) {
        int tileX = Math.floorDiv(worldX, TILE_SIZE);
        int tileZ = Math.floorDiv(worldZ, TILE_SIZE);
        long key = cacheKey(seed, dimension, tileX, tileZ);
        CacheEntry previous = TERRAIN_CACHE.put(key, new CacheEntry(color, Long.MIN_VALUE));
        if (previous == null || previous.color() != color) {
            int textureX = Math.floorDiv(tileX, TILE_TEXTURE_SIZE);
            int textureZ = Math.floorDiv(tileZ, TILE_TEXTURE_SIZE);
            TerrainTile texture = TERRAIN_TILES.get(new TerrainTileKey(seed, dimension, textureX, textureZ));
            if (texture != null) {
                texture.dirty = true;
            }
        }
    }

    public static void clearTerrainCache() {
        TERRAIN_CACHE.clear();
        for (TerrainTile tile : TERRAIN_TILES.values()) {
            Minecraft.getInstance().getTextureManager().release(tile.location);
        }
        TERRAIN_TILES.clear();
    }

    public static int tileSize() {
        return TILE_SIZE;
    }

    public static long tileKey(int tileX, int tileZ) {
        return ((long) tileX << 32) ^ (tileZ & 0xFFFFFFFFL);
    }

    public static void drawLocationList(GuiGraphics graphics, Font font, Bounds bounds, List<OpenTravelScreenPacket.Entry> locations,
                                        UUID sourceId, int scroll, double mouseX, double mouseY) {
        graphics.fill(bounds.x() + 8, bounds.y() + 7, bounds.x() + bounds.w() - 8, bounds.y() + 22, 0x66412616);
        Component title = Component.translatable("screen.kmdtravel.discovered_areas");
        drawCenteredPlain(graphics, font, title, bounds.x() + bounds.w() / 2, bounds.y() + 10, 0xFFF1D889);
        if (locations.isEmpty()) {
            drawCenteredPlain(graphics, font, Component.translatable("screen.kmdtravel.no_locations"), bounds.x() + bounds.w() / 2, bounds.y() + 32, 0x5A321A);
            return;
        }

        int first = Math.max(0, scroll);
        int visible = visibleLocationRows(bounds);
        for (int index = first; index < Math.min(locations.size(), first + visible); index++) {
            OpenTravelScreenPacket.Entry location = locations.get(index);
            int y = bounds.y() + 24 + (index - first) * 16;
            boolean hot = locationListIndex(bounds, locations, scroll, mouseX, mouseY) == index;
            int rowColor = location.id().equals(sourceId) ? 0x77994B24 : hot ? 0x55F3D88E : 0x00000000;
            if (rowColor != 0) {
                graphics.fill(bounds.x() + 10, y - 2, bounds.x() + bounds.w() - 10, y + 13, rowColor);
            }
            String name = font.plainSubstrByWidth(location.name(), bounds.w() - 24);
            drawCenteredPlain(graphics, font, name, bounds.x() + bounds.w() / 2, y, 0x2D2012);
        }
    }

    public static void drawLocationToggle(GuiGraphics graphics, Font font, Bounds bounds, boolean open) {
        graphics.fill(bounds.x() + 2, bounds.y() + 3, bounds.x() + bounds.w() + 3, bounds.y() + bounds.h() + 4, 0x66000000);
        graphics.fill(bounds.x(), bounds.y(), bounds.x() + bounds.w(), bounds.y() + bounds.h(), 0xFF5A321A);
        graphics.fill(bounds.x() + 2, bounds.y() + 2, bounds.x() + bounds.w() - 2, bounds.y() + bounds.h() - 2, 0xFFE8C982);
        graphics.fill(bounds.x() + 4, bounds.y() + 4, bounds.x() + bounds.w() - 4, bounds.y() + bounds.h() - 4, 0xFFF5DE9E);
        graphics.drawCenteredString(font, open ? "<" : ">", bounds.x() + bounds.w() / 2, bounds.y() + 7, 0x3A2111);
    }

    public static int locationListIndex(Bounds bounds, List<OpenTravelScreenPacket.Entry> locations, int scroll, double mouseX, double mouseY) {
        if (!bounds.contains(mouseX, mouseY) || mouseY < bounds.y() + 22) {
            return -1;
        }
        int row = (int) ((mouseY - bounds.y() - 24) / 16);
        if (row < 0 || row >= visibleLocationRows(bounds)) {
            return -1;
        }
        int index = scroll + row;
        return index >= 0 && index < locations.size() ? index : -1;
    }

    public static double panXFor(Bounds bounds, List<OpenTravelScreenPacket.Entry> locations, double zoom, OpenTravelScreenPacket.Entry location) {
        Scale scale = Scale.of(locations, bounds, 0, 0, zoom, anchorLocation(locations, null));
        return bounds.x() + bounds.w() / 2.0D - scale.x(location.x());
    }

    public static double panYFor(Bounds bounds, List<OpenTravelScreenPacket.Entry> locations, double zoom, OpenTravelScreenPacket.Entry location) {
        Scale scale = Scale.of(locations, bounds, 0, 0, zoom, anchorLocation(locations, null));
        return bounds.y() + bounds.h() / 2.0D - scale.y(location.z());
    }

    public static double panXFor(Bounds bounds, List<OpenTravelScreenPacket.Entry> locations, UUID sourceId, double zoom, OpenTravelScreenPacket.Entry location) {
        Scale scale = Scale.of(locations, bounds, 0, 0, zoom, anchorLocation(locations, sourceId));
        return bounds.x() + bounds.w() / 2.0D - scale.x(location.x());
    }

    public static double panYFor(Bounds bounds, List<OpenTravelScreenPacket.Entry> locations, UUID sourceId, double zoom, OpenTravelScreenPacket.Entry location) {
        Scale scale = Scale.of(locations, bounds, 0, 0, zoom, anchorLocation(locations, sourceId));
        return bounds.y() + bounds.h() / 2.0D - scale.y(location.z());
    }

    public static double panXFor(Bounds bounds, List<OpenTravelScreenPacket.Entry> locations, UUID sourceId, double zoom, int worldX) {
        return panXFor(bounds, locations, sourceId, zoom, (double) worldX);
    }

    public static double panYFor(Bounds bounds, List<OpenTravelScreenPacket.Entry> locations, UUID sourceId, double zoom, int worldZ) {
        return panYFor(bounds, locations, sourceId, zoom, (double) worldZ);
    }

    public static double panXFor(Bounds bounds, List<OpenTravelScreenPacket.Entry> locations, UUID sourceId, double zoom, double worldX) {
        Scale scale = Scale.of(locations, bounds, 0, 0, zoom, anchorLocation(locations, sourceId));
        return bounds.x() + bounds.w() / 2.0D - scale.xDouble(worldX);
    }

    public static double panYFor(Bounds bounds, List<OpenTravelScreenPacket.Entry> locations, UUID sourceId, double zoom, double worldZ) {
        Scale scale = Scale.of(locations, bounds, 0, 0, zoom, anchorLocation(locations, sourceId));
        return bounds.y() + bounds.h() / 2.0D - scale.yDouble(worldZ);
    }

    public static int visibleLocationRows(Bounds bounds) {
        return Math.max(1, (bounds.h() - 28) / 16);
    }

    public static double clampZoom(double zoom) {
        return Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoom));
    }

    public static OpenTravelScreenPacket.Entry hovered(Bounds bounds, List<OpenTravelScreenPacket.Entry> locations, double panX, double panY, double zoom, double mouseX, double mouseY) {
        return hovered(bounds, locations, null, panX, panY, zoom, mouseX, mouseY);
    }

    public static OpenTravelScreenPacket.Entry hovered(Bounds bounds, List<OpenTravelScreenPacket.Entry> locations, UUID sourceId, double panX, double panY, double zoom, double mouseX, double mouseY) {
        if (!bounds.contains(mouseX, mouseY) || locations.isEmpty()) {
            return null;
        }
        Scale scale = Scale.of(locations, bounds, panX, panY, zoom, anchorLocation(locations, sourceId));
        for (OpenTravelScreenPacket.Entry location : locations) {
            int px = scale.x(location.x());
            int py = scale.y(location.z());
            if (Math.abs(mouseX - px) <= 8 && Math.abs(mouseY - py) <= 8) {
                return location;
            }
        }
        return null;
    }

    private static OpenTravelScreenPacket.Entry anchorLocation(List<OpenTravelScreenPacket.Entry> locations, UUID sourceId) {
        if (locations.isEmpty()) {
            return null;
        }
        if (sourceId != null) {
            for (OpenTravelScreenPacket.Entry location : locations) {
                if (location.id().equals(sourceId)) {
                    return location;
                }
            }
        }
        return locations.getFirst();
    }

    public static void drawMapFast(GuiGraphics graphics, Font font, Bounds bounds, long seed, Identifier dimension, List<OpenTravelScreenPacket.Entry> locations,
                                   UUID sourceId, Progress progress, double panX, double panY, double zoom, double mouseX, double mouseY) {
        drawMap(graphics, font, bounds, seed, dimension, locations, sourceId, progress, panX, panY, zoom, mouseX, mouseY, false, true, true);
    }

    public static void drawMapFastWithoutLabels(GuiGraphics graphics, Font font, Bounds bounds, long seed, Identifier dimension, List<OpenTravelScreenPacket.Entry> locations,
                                                UUID sourceId, Progress progress, double panX, double panY, double zoom, double mouseX, double mouseY) {
        drawMap(graphics, font, bounds, seed, dimension, locations, sourceId, progress, panX, panY, zoom, mouseX, mouseY, false, false, true);
    }

    private static void drawTerrainMap(GuiGraphics graphics, Bounds bounds, Scale scale, long seed, Identifier dimension, boolean allowRebuild) {
        int minWorldX = Math.min(scale.worldX(bounds.x()), scale.worldX(bounds.x() + bounds.w()));
        int maxWorldX = Math.max(scale.worldX(bounds.x()), scale.worldX(bounds.x() + bounds.w()));
        int minWorldZ = Math.min(scale.worldZ(bounds.y()), scale.worldZ(bounds.y() + bounds.h()));
        int maxWorldZ = Math.max(scale.worldZ(bounds.y()), scale.worldZ(bounds.y() + bounds.h()));
        int minTextureX = Math.floorDiv(Math.floorDiv(minWorldX, TILE_SIZE), TILE_TEXTURE_SIZE) - 1;
        int maxTextureX = Math.floorDiv(Math.floorDiv(maxWorldX, TILE_SIZE), TILE_TEXTURE_SIZE) + 1;
        int minTextureZ = Math.floorDiv(Math.floorDiv(minWorldZ, TILE_SIZE), TILE_TEXTURE_SIZE) - 1;
        int maxTextureZ = Math.floorDiv(Math.floorDiv(maxWorldZ, TILE_SIZE), TILE_TEXTURE_SIZE) + 1;
        int builtThisFrame = 0;

        for (int textureZ = minTextureZ; textureZ <= maxTextureZ; textureZ++) {
            for (int textureX = minTextureX; textureX <= maxTextureX; textureX++) {
                TerrainTileKey key = new TerrainTileKey(seed, dimension, textureX, textureZ);
                TerrainTile texture = TERRAIN_TILES.get(key);
                if (allowRebuild && (texture == null || texture.dirty) && builtThisFrame < TILE_BUILDS_PER_FRAME) {
                    if (texture != null) {
                        Minecraft.getInstance().getTextureManager().release(texture.location);
                    }
                    texture = buildTerrainTile(key);
                    TERRAIN_TILES.put(key, texture);
                    builtThisFrame++;
                }
                int worldLeft = textureX * TILE_TEXTURE_SIZE * TILE_SIZE;
                int worldTop = textureZ * TILE_TEXTURE_SIZE * TILE_SIZE;
                int worldRight = worldLeft + TILE_TEXTURE_SIZE * TILE_SIZE;
                int worldBottom = worldTop + TILE_TEXTURE_SIZE * TILE_SIZE;
                int left = scale.x(worldLeft);
                int top = scale.y(worldTop);
                int right = scale.x(worldRight);
                int bottom = scale.y(worldBottom);
                int drawX = Math.min(left, right);
                int drawY = Math.min(top, bottom);
                int drawW = Math.max(1, Math.abs(right - left));
                int drawH = Math.max(1, Math.abs(bottom - top));
                if (texture == null) {
                    graphics.fill(drawX, drawY, drawX + drawW, drawY + drawH, WorldMapSampler.placeholder(seed, dimension, worldLeft, worldTop));
                } else {
                    graphics.blit(RenderPipelines.GUI_TEXTURED, texture.location, drawX, drawY, 0.0F, 0.0F, drawW, drawH, TILE_TEXTURE_SIZE, TILE_TEXTURE_SIZE);
                }
            }
        }
    }

    private static TerrainTile buildTerrainTile(TerrainTileKey key) {
        NativeImage image = new NativeImage(TILE_TEXTURE_SIZE, TILE_TEXTURE_SIZE, false);
        int baseTileX = key.textureX() * TILE_TEXTURE_SIZE;
        int baseTileZ = key.textureZ() * TILE_TEXTURE_SIZE;
        for (int y = 0; y < TILE_TEXTURE_SIZE; y++) {
            for (int x = 0; x < TILE_TEXTURE_SIZE; x++) {
                int worldX = (baseTileX + x) * TILE_SIZE + TILE_SIZE / 2;
                int worldZ = (baseTileZ + y) * TILE_SIZE + TILE_SIZE / 2;
                image.setPixel(x, y, toNativeRgba(cachedTerrainColor(key.seed(), key.dimension(), worldX, worldZ)));
            }
        }
        DynamicTexture dynamicTexture = new DynamicTexture(() -> "kmdtravel_map_" + textureSerial, image);
        Identifier location = Identifier.fromNamespaceAndPath("kmdtravel", "map/" + textureSerial++);
        Minecraft.getInstance().getTextureManager().register(location, dynamicTexture);
        return new TerrainTile(location);
    }

    private static int cachedTerrainColor(long seed, Identifier dimension, int worldX, int worldZ) {
        int tileX = Math.floorDiv(worldX, TILE_SIZE);
        int tileZ = Math.floorDiv(worldZ, TILE_SIZE);
        long key = cacheKey(seed, dimension, tileX, tileZ);
        CacheEntry cached = TERRAIN_CACHE.get(key);
        if (cached != null) {
            return cached.color();
        }
        return WorldMapSampler.placeholder(seed, dimension, tileX * TILE_SIZE + TILE_SIZE / 2, tileZ * TILE_SIZE + TILE_SIZE / 2);
    }

    private static int toNativeRgba(int argb) {
        int alpha = (argb >> 24) & 0xFF;
        int red = (argb >> 16) & 0xFF;
        int green = (argb >> 8) & 0xFF;
        int blue = argb & 0xFF;
        return alpha << 24 | blue << 16 | green << 8 | red;
    }

    private static long cacheKey(long seed, Identifier dimension, int tileX, int tileZ) {
        return tileKey(tileX, tileZ) ^ seed ^ ((long) dimension.hashCode() << 32);
    }

    private static void drawParchmentDetails(GuiGraphics graphics, Bounds bounds) {
    }

    private static void drawBookFrame(GuiGraphics graphics, Bounds bounds) {
        int outerLeft = bounds.x() - 18;
        int outerRight = bounds.x() + bounds.w() + 18;
        int outerTop = bounds.y() - 24;
        int outerBottom = bounds.y() + bounds.h() + 22;
        graphics.fill(outerLeft + 4, outerTop + 5, outerRight + 4, outerBottom + 5, 0x88000000);
        drawNineSlice(graphics, PARCHMENT_FRAME, outerLeft, outerTop, outerRight - outerLeft, outerBottom - outerTop, FRAME_SLICE);
        graphics.fill(bounds.x() - 3, bounds.y() - 3, bounds.x() + bounds.w() + 3, bounds.y() + bounds.h() + 3, 0xAA4A2914);
        graphics.fill(bounds.x() - 2, bounds.y() - 2, bounds.x() + bounds.w() + 2, bounds.y() + bounds.h() + 2, 0xFFE2C37B);
        graphics.fill(bounds.x() - 1, bounds.y() - 1, bounds.x() + bounds.w() + 1, bounds.y() + bounds.h() + 1, 0xFF5B391D);
        graphics.fill(bounds.x(), bounds.y(), bounds.x() + bounds.w(), bounds.y() + bounds.h(), 0xFFE8CD8D);
    }

    private static void drawBookPanel(GuiGraphics graphics, Bounds bounds) {
        graphics.fill(bounds.x() - 3, bounds.y() - 3, bounds.x() + bounds.w() + 8, bounds.y() + bounds.h() + 9, 0x77000000);
        drawNineSlice(graphics, PARCHMENT_PANEL, bounds.x() - 10, bounds.y() - 12, bounds.w() + 20, bounds.h() + 24, PANEL_SLICE);
    }

    private static void drawNineSlice(GuiGraphics graphics, Identifier texture, int x, int y, int width, int height, int slice) {
        int centerSource = PARCHMENT_TEXTURE_SIZE - slice * 2;
        int centerWidth = Math.max(0, width - slice * 2);
        int centerHeight = Math.max(0, height - slice * 2);
        int right = x + width - slice;
        int bottom = y + height - slice;
        blitPart(graphics, texture, x, y, slice, slice, 0, 0, slice, slice);
        blitPart(graphics, texture, right, y, slice, slice, PARCHMENT_TEXTURE_SIZE - slice, 0, slice, slice);
        blitPart(graphics, texture, x, bottom, slice, slice, 0, PARCHMENT_TEXTURE_SIZE - slice, slice, slice);
        blitPart(graphics, texture, right, bottom, slice, slice, PARCHMENT_TEXTURE_SIZE - slice, PARCHMENT_TEXTURE_SIZE - slice, slice, slice);
        if (centerWidth > 0) {
            blitPart(graphics, texture, x + slice, y, centerWidth, slice, slice, 0, centerSource, slice);
            blitPart(graphics, texture, x + slice, bottom, centerWidth, slice, slice, PARCHMENT_TEXTURE_SIZE - slice, centerSource, slice);
        }
        if (centerHeight > 0) {
            blitPart(graphics, texture, x, y + slice, slice, centerHeight, 0, slice, slice, centerSource);
            blitPart(graphics, texture, right, y + slice, slice, centerHeight, PARCHMENT_TEXTURE_SIZE - slice, slice, slice, centerSource);
        }
        if (centerWidth > 0 && centerHeight > 0) {
            blitPart(graphics, texture, x + slice, y + slice, centerWidth, centerHeight, slice, slice, centerSource, centerSource);
        }
    }

    private static void blitPart(GuiGraphics graphics, Identifier texture, int x, int y, int width, int height, int sourceX, int sourceY, int sourceWidth, int sourceHeight) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, (float) sourceX, (float) sourceY, width, height, sourceWidth, sourceHeight, PARCHMENT_TEXTURE_SIZE, PARCHMENT_TEXTURE_SIZE);
    }

    private static void drawPlayerHead(GuiGraphics graphics, int x, int y) {
        Identifier skin = Minecraft.getInstance().player == null
                ? Identifier.parse("textures/entity/player/wide/steve.png")
                : Minecraft.getInstance().getSkinManager().createLookup(Minecraft.getInstance().player.getGameProfile(), false).get().body().texturePath();
        graphics.fill(x - 1, y - 1, x + 13, y + 13, 0xFF1B120A);
        graphics.blit(RenderPipelines.GUI_TEXTURED, skin, x, y, 8.0F, 8.0F, 12, 12, 8, 8, 64, 64);
        graphics.blit(RenderPipelines.GUI_TEXTURED, skin, x, y, 40.0F, 8.0F, 12, 12, 8, 8, 64, 64);
    }

    private static void drawCenteredPlain(GuiGraphics graphics, Font font, Component text, int centerX, int y, int color) {
        graphics.drawString(font, text, centerX - font.width(text) / 2, y, color, false);
    }

    private static void drawCenteredPlain(GuiGraphics graphics, Font font, String text, int centerX, int y, int color) {
        graphics.drawString(font, text, centerX - font.width(text) / 2, y, color, false);
    }

    private static void drawReadableString(GuiGraphics graphics, Font font, String text, int x, int y, int color) {
        int width = font.width(text);
        graphics.fill(x - 2, y - 1, x + width + 2, y + 10, 0xAAEFDCA7);
        graphics.drawString(font, text, x, y, color, false);
    }

    public record Progress(int startX, int startZ, int endX, int endZ, double amount) {
    }

    private record CacheEntry(int color, long tickBucket) {
    }

    private static final class TerrainTile {
        private final Identifier location;
        private boolean dirty;

        private TerrainTile(Identifier location) {
            this.location = location;
        }
    }

    private record TerrainTileKey(long seed, Identifier dimension, int textureX, int textureZ) {
    }

    private record Scale(int minX, int minZ, int spanX, int spanZ, Bounds bounds, double panX, double panY, double zoom, int padX, int padY) {
        static Scale of(List<OpenTravelScreenPacket.Entry> locations, Bounds bounds, double panX, double panY, double zoom, OpenTravelScreenPacket.Entry anchor) {
            int padX = Math.min(24, bounds.w() / 12);
            int padY = Math.min(24, bounds.h() / 12);
            int drawableW = Math.max(1, bounds.w() - padX * 2);
            int drawableH = Math.max(1, bounds.h() - padY * 2);
            int spanX;
            int spanZ;
            int minX;
            int minZ;
            if (anchor != null) {
                spanX = LOCAL_VIEW_SPAN;
                spanZ = LOCAL_VIEW_SPAN;
                minX = anchor.x() - spanX / 2;
                minZ = anchor.z() - spanZ / 2;
            } else {
                int locationMinX = locations.stream().mapToInt(OpenTravelScreenPacket.Entry::x).min().orElse(0);
                int maxX = locations.stream().mapToInt(OpenTravelScreenPacket.Entry::x).max().orElse(0);
                int locationMinZ = locations.stream().mapToInt(OpenTravelScreenPacket.Entry::z).min().orElse(0);
                int maxZ = locations.stream().mapToInt(OpenTravelScreenPacket.Entry::z).max().orElse(0);
                spanX = Math.min(LOCAL_VIEW_SPAN, Math.max(64, maxX - locationMinX));
                spanZ = Math.min(LOCAL_VIEW_SPAN, Math.max(64, maxZ - locationMinZ));
                minX = locationMinX;
                minZ = locationMinZ;
            }
            double mapAspect = drawableW / (double) drawableH;
            double worldAspect = spanX / (double) spanZ;
            if (worldAspect > mapAspect) {
                int targetSpanZ = Math.max(spanZ, (int) Math.ceil(spanX / mapAspect));
                minZ -= (targetSpanZ - spanZ) / 2;
                spanZ = targetSpanZ;
            } else {
                int targetSpanX = Math.max(spanX, (int) Math.ceil(spanZ * mapAspect));
                minX -= (targetSpanX - spanX) / 2;
                spanX = targetSpanX;
            }
            return new Scale(minX, minZ, spanX, spanZ, bounds, panX, panY, clampZoom(zoom), padX, padY);
        }

        int x(int worldX) {
            return (int) Math.round(xDouble(worldX));
        }

        double xDouble(double worldX) {
            double center = bounds.x() + bounds.w() / 2.0D;
            double base = bounds.x() + padX + (worldX - minX) / (double) spanX * (bounds.w() - padX * 2);
            return center + (base - center) * zoom + panX;
        }

        int y(int worldZ) {
            return (int) Math.round(yDouble(worldZ));
        }

        double yDouble(double worldZ) {
            double center = bounds.y() + bounds.h() / 2.0D;
            double base = bounds.y() + padY + (worldZ - minZ) / (double) spanZ * (bounds.h() - padY * 2);
            return center + (base - center) * zoom + panY;
        }

        int worldX(int screenX) {
            double center = bounds.x() + bounds.w() / 2.0D;
            double baseScreenX = center + (screenX - panX - center) / zoom;
            return minX + (int) ((baseScreenX - bounds.x() - padX) / (double) Math.max(1, bounds.w() - padX * 2) * spanX);
        }

        int worldZ(int screenY) {
            double center = bounds.y() + bounds.h() / 2.0D;
            double baseScreenY = center + (screenY - panY - center) / zoom;
            return minZ + (int) ((baseScreenY - bounds.y() - padY) / (double) Math.max(1, bounds.h() - padY * 2) * spanZ);
        }

        double blocksPerPixel() {
            return Math.max(spanX / (double) Math.max(1, bounds.w()), spanZ / (double) Math.max(1, bounds.h())) / zoom;
        }
    }
}




