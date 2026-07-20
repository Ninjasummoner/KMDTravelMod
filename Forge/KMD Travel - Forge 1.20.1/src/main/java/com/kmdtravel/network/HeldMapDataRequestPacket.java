package com.kmdtravel.network;

import com.kmdtravel.block.FastTravelPostBlock;
import com.kmdtravel.data.PlayerTravelData;
import com.kmdtravel.data.TravelSavedData;
import com.kmdtravel.travel.TravelLocation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

public final class HeldMapDataRequestPacket {
    public static final HeldMapDataRequestPacket INSTANCE = new HeldMapDataRequestPacket();
    public static void encode(HeldMapDataRequestPacket packet, FriendlyByteBuf buffer) {}
    public static HeldMapDataRequestPacket decode(FriendlyByteBuf buffer) { return INSTANCE; }

    public static void handle(HeldMapDataRequestPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            ResourceLocation dimension = player.serverLevel().dimension().location();
            TravelSavedData savedData = TravelSavedData.get(player.serverLevel());
            Set<UUID> discovered = PlayerTravelData.discovered(player);
            List<HeldMapDataPacket.Marker> markers = new ArrayList<>();
            List<UUID> stale = new ArrayList<>();
            for (TravelLocation location : savedData.all()) {
                if (!dimension.equals(location.dimension())) continue;
                if (isLoadedMissingPost(player, location)) { stale.add(location.id()); continue; }
                if (discovered.contains(location.id()) || location.shared()) {
                    markers.add(new HeldMapDataPacket.Marker(location.id(), location.name(), location.pos().getX(),
                            location.pos().getZ(), location.shared(), location.markerColor(), location.markerPattern()));
                }
            }
            stale.forEach(savedData::remove);
            KMDNetwork.sendToPlayer(player, new HeldMapDataPacket(player.serverLevel().getSeed(), dimension, List.copyOf(markers)));
        });
        context.setPacketHandled(true);
    }

    private static boolean isLoadedMissingPost(ServerPlayer player, TravelLocation location) {
        if (!player.serverLevel().hasChunkAt(location.pos())) return false;
        BlockState state = player.serverLevel().getBlockState(location.pos());
        return !(state.getBlock() instanceof FastTravelPostBlock);
    }
}
