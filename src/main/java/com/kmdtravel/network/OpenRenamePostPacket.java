package com.kmdtravel.network;

import com.kmdtravel.KMDTravel;
import com.kmdtravel.client.ClientTravelScreens;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenRenamePostPacket(BlockPos pos, String currentName) implements CustomPacketPayload {
    public static final Type<OpenRenamePostPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(KMDTravel.MOD_ID, "open_rename_post"));
    public static final StreamCodec<FriendlyByteBuf, OpenRenamePostPacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> encode(packet, buffer),
            OpenRenamePostPacket::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void encode(OpenRenamePostPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos());
        buffer.writeUtf(packet.currentName());
    }

    public static OpenRenamePostPacket decode(FriendlyByteBuf buffer) {
        return new OpenRenamePostPacket(buffer.readBlockPos(), buffer.readUtf(64));
    }

    public static void handle(OpenRenamePostPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientTravelScreens.openRenamePost(packet));
    }
}
