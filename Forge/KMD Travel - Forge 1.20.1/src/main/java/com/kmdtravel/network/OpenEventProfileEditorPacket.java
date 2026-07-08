package com.kmdtravel.network;

import com.kmdtravel.client.ClientTravelScreens;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record OpenEventProfileEditorPacket(CompoundTag profiles, List<String> mobs, List<String> dimensions, List<String> biomes) {
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

    public static void handle(OpenEventProfileEditorPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientTravelScreens.openEventProfileEditor(packet)));
        context.get().setPacketHandled(true);
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
