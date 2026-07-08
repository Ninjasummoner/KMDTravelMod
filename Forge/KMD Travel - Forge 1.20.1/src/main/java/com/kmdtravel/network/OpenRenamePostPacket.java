package com.kmdtravel.network;

import com.kmdtravel.client.ClientTravelScreens;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record OpenRenamePostPacket(BlockPos pos, String currentName) {
    public static void encode(OpenRenamePostPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos());
        buffer.writeUtf(packet.currentName());
    }

    public static OpenRenamePostPacket decode(FriendlyByteBuf buffer) {
        return new OpenRenamePostPacket(buffer.readBlockPos(), buffer.readUtf(64));
    }

    public static void handle(OpenRenamePostPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientTravelScreens.openRenamePost(packet)));
        context.get().setPacketHandled(true);
    }
}

