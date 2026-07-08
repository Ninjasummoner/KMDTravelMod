package com.kmdtravel.network;

import com.kmdtravel.KMDTravel;
import com.kmdtravel.eventconfig.EventProfile;
import com.kmdtravel.eventconfig.EventProfileSavedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SaveEventProfilePacket(CompoundTag profile, boolean remove) implements CustomPacketPayload {
    public static final Type<SaveEventProfilePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(KMDTravel.MOD_ID, "save_event_profile"));
    public static final StreamCodec<FriendlyByteBuf, SaveEventProfilePacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> encode(packet, buffer),
            SaveEventProfilePacket::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void encode(SaveEventProfilePacket packet, FriendlyByteBuf buffer) {
        buffer.writeNbt(packet.profile());
        buffer.writeBoolean(packet.remove());
    }

    public static SaveEventProfilePacket decode(FriendlyByteBuf buffer) {
        CompoundTag profile = buffer.readNbt();
        return new SaveEventProfilePacket(profile == null ? new CompoundTag() : profile, buffer.readBoolean());
    }

    public static void handle(SaveEventProfilePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || !player.hasPermissions(2)) {
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
    }
}
