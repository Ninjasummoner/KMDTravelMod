package com.kmdtravel.network;

import com.kmdtravel.KMDTravel;
import com.kmdtravel.block.FastTravelPostBlock;
import com.kmdtravel.client.ClientTravelScreens;
import com.kmdtravel.data.PlayerTravelData;
import com.kmdtravel.data.TravelSavedData;
import com.kmdtravel.travel.FastTravelManager;
import com.kmdtravel.travel.TravelLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record OpenTravelScreenPacket(UUID sourceId, long worldSeed, ResourceLocation dimension, List<Entry> locations, List<MapSample> samples) implements CustomPacketPayload {
    public static final Type<OpenTravelScreenPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(KMDTravel.MOD_ID, "open_travel_screen"));
    public static final StreamCodec<FriendlyByteBuf, OpenTravelScreenPacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> encode(packet, buffer),
            OpenTravelScreenPacket::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public record Entry(UUID id, String name, ResourceLocation dimension, int x, int z, int ambushChance, boolean shared, int markerColor, int markerPattern) {
        public static Entry from(ServerPlayer player, TravelLocation source, TravelLocation location) {
            return new Entry(
                    location.id(),
                    location.name(),
                    location.dimension(),
                    location.pos().getX(),
                    location.pos().getZ(),
                    FastTravelManager.previewAmbushChance(player, source, location),
                    location.shared(),
                    location.markerColor(),
                    location.markerPattern());
        }
    }

    public record MapSample(int x, int z, int color) {
    }

    public static OpenTravelScreenPacket from(ServerPlayer player, UUID sourceId) {
        Set<UUID> discovered = PlayerTravelData.discovered(player);
        TravelSavedData savedData = TravelSavedData.get(player.serverLevel());
        TravelLocation source = savedData.get(sourceId).orElse(null);
        List<Entry> entries = new ArrayList<>();
        List<UUID> staleLoadedPosts = new ArrayList<>();
        for (TravelLocation location : savedData.all()) {
            if (isLoadedMissingPost(player.serverLevel(), location)) {
                staleLoadedPosts.add(location.id());
                continue;
            }
            if ((discovered.contains(location.id()) || location.shared()) && source != null && location.dimension().equals(source.dimension())) {
                entries.add(Entry.from(player, source, location));
            }
        }
        for (UUID id : staleLoadedPosts) {
            savedData.remove(id);
        }
        ResourceLocation dimension = source == null ? player.serverLevel().dimension().location() : source.dimension();
        return new OpenTravelScreenPacket(sourceId, player.serverLevel().getSeed(), dimension, entries, samplesFor(player.serverLevel(), entries));
    }

    private static boolean isLoadedMissingPost(ServerLevel level, TravelLocation location) {
        if (!location.dimension().equals(level.dimension().location()) || !level.hasChunkAt(location.pos())) {
            return false;
        }
        BlockState state = level.getBlockState(location.pos());
        return !(state.getBlock() instanceof FastTravelPostBlock);
    }

    public static void encode(OpenTravelScreenPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.sourceId());
        buffer.writeLong(packet.worldSeed());
        buffer.writeResourceLocation(packet.dimension());
        buffer.writeVarInt(packet.locations().size());
        for (Entry entry : packet.locations()) {
            buffer.writeUUID(entry.id());
            buffer.writeUtf(entry.name());
            buffer.writeResourceLocation(entry.dimension());
            buffer.writeInt(entry.x());
            buffer.writeInt(entry.z());
            buffer.writeVarInt(entry.ambushChance());
            buffer.writeBoolean(entry.shared());
            buffer.writeInt(entry.markerColor());
            buffer.writeInt(entry.markerPattern());
        }
        buffer.writeVarInt(packet.samples().size());
        for (MapSample sample : packet.samples()) {
            buffer.writeInt(sample.x());
            buffer.writeInt(sample.z());
            buffer.writeInt(sample.color());
        }
    }

    public static OpenTravelScreenPacket decode(FriendlyByteBuf buffer) {
        UUID sourceId = buffer.readUUID();
        long worldSeed = buffer.readLong();
        ResourceLocation dimension = buffer.readResourceLocation();
        int size = buffer.readVarInt();
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            entries.add(new Entry(buffer.readUUID(), buffer.readUtf(64), buffer.readResourceLocation(), buffer.readInt(), buffer.readInt(), buffer.readVarInt(), buffer.readBoolean(), buffer.readInt(), buffer.readInt()));
        }
        int sampleSize = buffer.readVarInt();
        List<MapSample> samples = new ArrayList<>();
        for (int i = 0; i < sampleSize; i++) {
            samples.add(new MapSample(buffer.readInt(), buffer.readInt(), buffer.readInt()));
        }
        return new OpenTravelScreenPacket(sourceId, worldSeed, dimension, entries, samples);
    }

    public static void handle(OpenTravelScreenPacket packet, ClientPlayNetworking.Context context) {
        context.client().execute(() -> ClientTravelScreens.open(packet));
    }

    private static List<MapSample> samplesFor(ServerLevel level, List<Entry> entries) {
        return List.of();
    }

    private static int sampleColor(ServerLevel level, int worldX, int worldZ) {
        BlockPos surface = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, new BlockPos(worldX, 0, worldZ));
        BlockPos.MutableBlockPos cursor = surface.mutable();
        for (int y = surface.getY(); y >= level.getMinBuildHeight(); y--) {
            cursor.setY(y);
            BlockState state = level.getBlockState(cursor);
            if (state.isAir()) {
                continue;
            }
            FluidState fluid = state.getFluidState();
            if (!fluid.isEmpty()) {
                return shade(0xFF000000 | level.getBiome(cursor).value().getWaterColor(), y, level);
            }
            MapColor mapColor = state.getMapColor(level, cursor);
            if (mapColor == MapColor.NONE) {
                continue;
            }
            Biome biome = level.getBiome(cursor).value();
            int color = mapColor == MapColor.GRASS ? biome.getGrassColor(worldX, worldZ)
                    : mapColor == MapColor.PLANT || mapColor == MapColor.COLOR_GREEN ? biome.getFoliageColor()
                    : mapColor == MapColor.WATER ? biome.getWaterColor()
                    : mapColor.col;
            return shade(0xFF000000 | color, y, level);
        }
        return 0xFF8F7748;
    }

    private static int shade(int color, int y, ServerLevel level) {
        double midpoint = (level.getMinBuildHeight() + level.getMaxBuildHeight()) / 2.0D;
        double amount = Math.max(-0.22D, Math.min(0.22D, (y - midpoint) / 380.0D));
        int red = adjust((color >> 16) & 0xFF, amount);
        int green = adjust((color >> 8) & 0xFF, amount);
        int blue = adjust(color & 0xFF, amount);
        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }

    private static int adjust(int channel, double amount) {
        int value = amount >= 0.0D ? (int) (channel + (255 - channel) * amount) : (int) (channel * (1.0D + amount));
        return Math.max(0, Math.min(255, value));
    }
}
