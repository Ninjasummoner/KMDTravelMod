package com.kmdtravel.network;

import com.kmdtravel.client.ClientTravelScreens;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public record BeginTravelPacket(UUID sourceId, UUID destinationId, long worldSeed, ResourceLocation dimension, int startX, int startZ, int endX, int endZ, int durationTicks, List<OpenTravelScreenPacket.Entry> locations, List<OpenTravelScreenPacket.MapSample> samples) {
    public static void encode(BeginTravelPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.sourceId());
        buffer.writeUUID(packet.destinationId());
        buffer.writeLong(packet.worldSeed());
        buffer.writeResourceLocation(packet.dimension());
        buffer.writeInt(packet.startX());
        buffer.writeInt(packet.startZ());
        buffer.writeInt(packet.endX());
        buffer.writeInt(packet.endZ());
        buffer.writeVarInt(packet.durationTicks());
        buffer.writeVarInt(packet.locations().size());
        for (OpenTravelScreenPacket.Entry entry : packet.locations()) {
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
        for (OpenTravelScreenPacket.MapSample sample : packet.samples()) {
            buffer.writeInt(sample.x());
            buffer.writeInt(sample.z());
            buffer.writeInt(sample.color());
        }
    }

    public static BeginTravelPacket decode(FriendlyByteBuf buffer) {
        UUID sourceId = buffer.readUUID();
        UUID destinationId = buffer.readUUID();
        long worldSeed = buffer.readLong();
        ResourceLocation dimension = buffer.readResourceLocation();
        int startX = buffer.readInt();
        int startZ = buffer.readInt();
        int endX = buffer.readInt();
        int endZ = buffer.readInt();
        int durationTicks = buffer.readVarInt();
        int size = buffer.readVarInt();
        List<OpenTravelScreenPacket.Entry> locations = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            locations.add(new OpenTravelScreenPacket.Entry(buffer.readUUID(), buffer.readUtf(64), buffer.readResourceLocation(), buffer.readInt(), buffer.readInt(), buffer.readVarInt(), buffer.readBoolean(), buffer.readInt(), buffer.readInt()));
        }
        int sampleSize = buffer.readVarInt();
        List<OpenTravelScreenPacket.MapSample> samples = new ArrayList<>();
        for (int i = 0; i < sampleSize; i++) {
            samples.add(new OpenTravelScreenPacket.MapSample(buffer.readInt(), buffer.readInt(), buffer.readInt()));
        }
        return new BeginTravelPacket(sourceId, destinationId, worldSeed, dimension, startX, startZ, endX, endZ, durationTicks, locations, samples);
    }

    public static void handle(BeginTravelPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientTravelScreens.openTravelProgress(packet)));
        context.get().setPacketHandled(true);
    }
}

