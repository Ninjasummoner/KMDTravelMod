package com.kmdtravel.network;

import com.kmdtravel.KMDTravel;
import com.kmdtravel.eventconfig.EventProfileSavedData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public record SetDefaultEventProfilePacket(String profileId) implements CustomPacketPayload {
    public static final Type<SetDefaultEventProfilePacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(KMDTravel.MOD_ID, "set_default_event_profile"));
    public static final StreamCodec<FriendlyByteBuf, SetDefaultEventProfilePacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> buffer.writeUtf(packet.profileId(), 64),
            buffer -> new SetDefaultEventProfilePacket(buffer.readUtf(64)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SetDefaultEventProfilePacket packet, ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            ServerPlayer player = context.player();
            if (!player.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)) {
                return;
            }
            EventProfileSavedData data = EventProfileSavedData.get(((ServerLevel) player.level()));
            if (data.profile(packet.profileId()).isPresent()) {
                data.setGlobalProfile(packet.profileId());
                player.sendSystemMessage(Component.literal("Default KMD event profile set to: " + packet.profileId()));
            }
        });
    }
}
