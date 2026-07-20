package com.kmdtravel.network;

import com.kmdtravel.KMDTravel;
import com.kmdtravel.block.FastTravelPostBlock;
import com.kmdtravel.data.PlayerTravelData;
import com.kmdtravel.data.TravelSavedData;
import com.kmdtravel.travel.TravelLocation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record HeldMapDataRequestPacket() implements CustomPacketPayload {
    public static final HeldMapDataRequestPacket INSTANCE = new HeldMapDataRequestPacket();
    public static final Type<HeldMapDataRequestPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(KMDTravel.MOD_ID, "held_map_data_request"));
    public static final StreamCodec<FriendlyByteBuf, HeldMapDataRequestPacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> {
            },
            buffer -> INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(HeldMapDataRequestPacket packet, ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            ServerPlayer player = context.player();
                ResourceLocation dimension = player.serverLevel().dimension().location();
                TravelSavedData savedData = TravelSavedData.get(player.serverLevel());
                Set<UUID> discovered = PlayerTravelData.discovered(player);
                List<HeldMapDataPacket.Marker> markers = new ArrayList<>();
                List<UUID> staleLoadedPosts = new ArrayList<>();
                for (TravelLocation location : savedData.all()) {
                    if (!dimension.equals(location.dimension())) {
                        continue;
                    }
                    if (isLoadedMissingPost(player, location)) {
                        staleLoadedPosts.add(location.id());
                        continue;
                    }
                    if (discovered.contains(location.id()) || location.shared()) {
                        markers.add(new HeldMapDataPacket.Marker(
                                location.id(),
                                location.name(),
                                location.pos().getX(),
                                location.pos().getZ(),
                                location.shared(),
                                location.markerColor(),
                                location.markerPattern()));
                    }
                }
                staleLoadedPosts.forEach(savedData::remove);
                KMDNetwork.sendToPlayer(player, new HeldMapDataPacket(
                        player.serverLevel().getSeed(),
                        dimension,
                        List.copyOf(markers)));
        });
    }

    private static boolean isLoadedMissingPost(ServerPlayer player, TravelLocation location) {
        if (!player.serverLevel().hasChunkAt(location.pos())) {
            return false;
        }
        BlockState state = player.serverLevel().getBlockState(location.pos());
        return !(state.getBlock() instanceof FastTravelPostBlock);
    }
}
