package com.kmdtravel.network;

import com.kmdtravel.KMDTravel;
import com.kmdtravel.client.ClientTravelScreens;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record BeginTravelPacket(UUID sourceId, UUID destinationId, long worldSeed, Identifier dimension, int startX, int startZ, int endX, int endZ, int durationTicks, List<OpenTravelScreenPacket.Entry> locations, List<OpenTravelScreenPacket.MapSample> samples) implements CustomPacketPayload {
    public static final Type<BeginTravelPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(KMDTravel.MOD_ID, "begin_travel"));
    public static final StreamCodec<FriendlyByteBuf, BeginTravelPacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> encode(packet, buffer),
            BeginTravelPacket::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void encode(BeginTravelPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.sourceId());
        buffer.writeUUID(packet.destinationId());
        buffer.writeLong(packet.worldSeed());
        buffer.writeIdentifier(packet.dimension());
        buffer.writeInt(packet.startX());
        buffer.writeInt(packet.startZ());
        buffer.writeInt(packet.endX());
        buffer.writeInt(packet.endZ());
        buffer.writeVarInt(packet.durationTicks());
        buffer.writeVarInt(packet.locations().size());
        for (OpenTravelScreenPacket.Entry entry : packet.locations()) {
            buffer.writeUUID(entry.id());
            buffer.writeUtf(entry.name());
            buffer.writeIdentifier(entry.dimension());
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
        Identifier dimension = buffer.readIdentifier();
        int startX = buffer.readInt();
        int startZ = buffer.readInt();
        int endX = buffer.readInt();
        int endZ = buffer.readInt();
        int durationTicks = buffer.readVarInt();
        int size = buffer.readVarInt();
        List<OpenTravelScreenPacket.Entry> locations = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            locations.add(new OpenTravelScreenPacket.Entry(buffer.readUUID(), buffer.readUtf(64), buffer.readIdentifier(), buffer.readInt(), buffer.readInt(), buffer.readVarInt(), buffer.readBoolean(), buffer.readInt(), buffer.readInt()));
        }
        int sampleSize = buffer.readVarInt();
        List<OpenTravelScreenPacket.MapSample> samples = new ArrayList<>();
        for (int i = 0; i < sampleSize; i++) {
            samples.add(new OpenTravelScreenPacket.MapSample(buffer.readInt(), buffer.readInt(), buffer.readInt()));
        }
        return new BeginTravelPacket(sourceId, destinationId, worldSeed, dimension, startX, startZ, endX, endZ, durationTicks, locations, samples);
    }

    public static void handle(BeginTravelPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientTravelScreens.openTravelProgress(packet));
    }
}


