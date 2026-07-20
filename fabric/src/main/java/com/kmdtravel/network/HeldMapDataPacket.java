package com.kmdtravel.network;

import com.kmdtravel.KMDTravel;
import com.kmdtravel.client.ClientMapCache;
import com.kmdtravel.client.render.HeldTravelMapRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record HeldMapDataPacket(long worldSeed, ResourceLocation dimension, List<Marker> markers) implements CustomPacketPayload {
    public static final Type<HeldMapDataPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(KMDTravel.MOD_ID, "held_map_data"));
    public static final StreamCodec<FriendlyByteBuf, HeldMapDataPacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> {
                buffer.writeLong(packet.worldSeed());
                buffer.writeResourceLocation(packet.dimension());
                buffer.writeVarInt(packet.markers().size());
                for (Marker marker : packet.markers()) {
                    buffer.writeUUID(marker.id());
                    buffer.writeUtf(marker.name());
                    buffer.writeInt(marker.x());
                    buffer.writeInt(marker.z());
                    buffer.writeBoolean(marker.shared());
                    buffer.writeInt(marker.color());
                    buffer.writeInt(marker.pattern());
                }
            },
            buffer -> {
                long worldSeed = buffer.readLong();
                ResourceLocation dimension = buffer.readResourceLocation();
                int markerCount = buffer.readVarInt();
                List<Marker> markers = new ArrayList<>(markerCount);
                for (int index = 0; index < markerCount; index++) {
                    markers.add(new Marker(
                            buffer.readUUID(),
                            buffer.readUtf(64),
                            buffer.readInt(),
                            buffer.readInt(),
                            buffer.readBoolean(),
                            buffer.readInt(),
                            buffer.readInt()));
                }
                return new HeldMapDataPacket(worldSeed, dimension, List.copyOf(markers));
            });

    public record Marker(UUID id, String name, int x, int z, boolean shared, int color, int pattern) {
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(HeldMapDataPacket packet, ClientPlayNetworking.Context context) {
        context.client().execute(() -> {
            ClientMapCache.useMap(packet.worldSeed(), packet.dimension());
            HeldTravelMapRenderer.setMarkers(packet.dimension(), packet.markers());
        });
    }
}
