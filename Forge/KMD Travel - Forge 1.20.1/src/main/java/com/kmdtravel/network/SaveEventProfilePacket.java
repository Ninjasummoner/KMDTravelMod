package com.kmdtravel.network;

import com.kmdtravel.eventconfig.EventProfile;
import com.kmdtravel.eventconfig.EventProfileSavedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SaveEventProfilePacket(CompoundTag profile, boolean remove) {
    public static void encode(SaveEventProfilePacket packet, FriendlyByteBuf buffer) {
        buffer.writeNbt(packet.profile());
        buffer.writeBoolean(packet.remove());
    }

    public static SaveEventProfilePacket decode(FriendlyByteBuf buffer) {
        CompoundTag profile = buffer.readNbt();
        return new SaveEventProfilePacket(profile == null ? new CompoundTag() : profile, buffer.readBoolean());
    }

    public static void handle(SaveEventProfilePacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null || !player.hasPermissions(2)) {
                return;
            }
            EventProfileSavedData data = EventProfileSavedData.get(player.serverLevel());
            EventProfile profile = EventProfile.load(packet.profile());
            if (packet.remove()) {
                data.removeProfile(profile.id());
                player.displayClientMessage(Component.literal("Removed KMD event profile: " + profile.id()), false);
            } else {
                data.upsertProfile(profile);
                player.displayClientMessage(Component.literal("Saved KMD event profile: " + profile.id()), false);
            }
        });
        context.get().setPacketHandled(true);
    }
}
