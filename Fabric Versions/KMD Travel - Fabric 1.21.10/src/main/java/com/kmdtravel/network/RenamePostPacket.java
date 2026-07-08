package com.kmdtravel.network;

import com.kmdtravel.KMDTravel;
import com.kmdtravel.block.FastTravelPostBlockEntity;
import com.kmdtravel.data.TravelSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public record RenamePostPacket(BlockPos pos, String name) implements CustomPacketPayload {
    public static final Type<RenamePostPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(KMDTravel.MOD_ID, "rename_post"));
    public static final StreamCodec<FriendlyByteBuf, RenamePostPacket> STREAM_CODEC = StreamCodec.of(
            (buffer, packet) -> encode(packet, buffer),
            RenamePostPacket::decode);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void encode(RenamePostPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos());
        buffer.writeUtf(packet.name());
    }

    public static RenamePostPacket decode(FriendlyByteBuf buffer) {
        return new RenamePostPacket(buffer.readBlockPos(), buffer.readUtf(64));
    }

    public static void handle(RenamePostPacket packet, ServerPlayNetworking.Context context) {
        context.server().execute(() -> {
            ServerPlayer player = context.player();

            ServerLevel level = ((ServerLevel) player.level());
            BlockPos lowerPos = level.getBlockState(packet.pos()).getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER
                    ? packet.pos()
                    : packet.pos().below();
            if (!(level.getBlockEntity(lowerPos) instanceof FastTravelPostBlockEntity post)) {
                return;
            }
            if (player.distanceToSqr(lowerPos.getX() + 0.5D, lowerPos.getY() + 0.5D, lowerPos.getZ() + 0.5D) > 64.0D) {
                return;
            }

            String trimmed = packet.name().trim();
            if (trimmed.isEmpty()) {
                trimmed = "Travel Post";
            }
            post.setPostName(Component.literal(trimmed));
            TravelSavedData.get(level).upsertFromPost(post);
            player.displayClientMessage(Component.translatable("message.kmdtravel.renamed", post.getPostName()), true);
        });
    }
}
