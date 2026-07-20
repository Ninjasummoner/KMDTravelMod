package com.kmdtravel.network;

import com.kmdtravel.client.ClientMapCache;
import com.kmdtravel.client.render.HeldTravelMapRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public record HeldMapDataPacket(long worldSeed, ResourceLocation dimension, List<Marker> markers) {
    public record Marker(UUID id, String name, int x, int z, boolean shared, int color, int pattern) {}

    public static void encode(HeldMapDataPacket packet, FriendlyByteBuf buffer) {
        buffer.writeLong(packet.worldSeed);
        buffer.writeResourceLocation(packet.dimension);
        buffer.writeVarInt(packet.markers.size());
        for (Marker marker : packet.markers) {
            buffer.writeUUID(marker.id); buffer.writeUtf(marker.name); buffer.writeInt(marker.x); buffer.writeInt(marker.z);
            buffer.writeBoolean(marker.shared); buffer.writeInt(marker.color); buffer.writeInt(marker.pattern);
        }
    }

    public static HeldMapDataPacket decode(FriendlyByteBuf buffer) {
        long seed = buffer.readLong();
        ResourceLocation dimension = buffer.readResourceLocation();
        int count = buffer.readVarInt();
        List<Marker> markers = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            markers.add(new Marker(buffer.readUUID(), buffer.readUtf(64), buffer.readInt(), buffer.readInt(),
                    buffer.readBoolean(), buffer.readInt(), buffer.readInt()));
        }
        return new HeldMapDataPacket(seed, dimension, List.copyOf(markers));
    }

    public static void handle(HeldMapDataPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ClientMapCache.useMap(packet.worldSeed, packet.dimension);
            HeldTravelMapRenderer.setMarkers(packet.dimension, packet.markers);
        });
        context.setPacketHandled(true);
    }
}
