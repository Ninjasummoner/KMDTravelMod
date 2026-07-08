package com.kmdtravel.network;

import com.kmdtravel.KMDTravel;
import com.kmdtravel.client.ClientTravelScreens;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenKMDHelpPacket() implements CustomPacketPayload {
    public static final OpenKMDHelpPacket INSTANCE = new OpenKMDHelpPacket();
    public static final Type<OpenKMDHelpPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(KMDTravel.MOD_ID, "open_help"));
    public static final StreamCodec<FriendlyByteBuf, OpenKMDHelpPacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> {
            },
            buffer -> INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(OpenKMDHelpPacket packet, IPayloadContext context) {
        context.enqueueWork(ClientTravelScreens::openHelp);
    }
}


