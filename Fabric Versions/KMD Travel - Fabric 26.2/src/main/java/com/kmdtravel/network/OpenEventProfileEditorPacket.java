package com.kmdtravel.network;

import com.kmdtravel.KMDTravel;
import com.kmdtravel.client.ClientTravelScreens;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import java.util.ArrayList;
import java.util.List;

public record OpenEventProfileEditorPacket(CompoundTag profiles, List<String> mobs, List<String> dimensions, List<String> biomes) implements CustomPacketPayload {
    public static final Type<OpenEventProfileEditorPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(KMDTravel.MOD_ID, "open_event_profile_editor"));
    public static final StreamCodec<FriendlyByteBuf, OpenEventProfileEditorPacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> encode(packet, buffer),
            OpenEventProfileEditorPacket::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void encode(OpenEventProfileEditorPacket packet, FriendlyByteBuf buffer) {
        buffer.writeNbt(packet.profiles());
        writeStrings(buffer, packet.mobs());
        writeStrings(buffer, packet.dimensions());
        writeStrings(buffer, packet.biomes());
    }

    public static OpenEventProfileEditorPacket decode(FriendlyByteBuf buffer) {
        CompoundTag profiles = buffer.readNbt();
        return new OpenEventProfileEditorPacket(
                profiles == null ? new CompoundTag() : profiles,
                readStrings(buffer),
                readStrings(buffer),
                readStrings(buffer));
    }

    public static void handle(OpenEventProfileEditorPacket packet, ClientPlayNetworking.Context context) {
        context.client().execute(() -> ClientTravelScreens.openEventProfileEditor(packet));
    }

    private static void writeStrings(FriendlyByteBuf buffer, List<String> values) {
        buffer.writeVarInt(values.size());
        for (String value : values) {
            buffer.writeUtf(value, 128);
        }
    }

    private static List<String> readStrings(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        List<String> values = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            values.add(buffer.readUtf(128));
        }
        return List.copyOf(values);
    }
}
