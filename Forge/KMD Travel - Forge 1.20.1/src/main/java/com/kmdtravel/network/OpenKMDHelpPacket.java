package com.kmdtravel.network;

import com.kmdtravel.client.ClientTravelScreens;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record OpenKMDHelpPacket() {
    public static final OpenKMDHelpPacket INSTANCE = new OpenKMDHelpPacket();

    public static void encode(OpenKMDHelpPacket packet, FriendlyByteBuf buffer) {
    }

    public static OpenKMDHelpPacket decode(FriendlyByteBuf buffer) {
        return INSTANCE;
    }

    public static void handle(OpenKMDHelpPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientTravelScreens::openHelp));
        context.get().setPacketHandled(true);
    }
}
