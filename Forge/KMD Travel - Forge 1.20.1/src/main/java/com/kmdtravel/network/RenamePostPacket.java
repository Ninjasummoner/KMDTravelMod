package com.kmdtravel.network;

import com.kmdtravel.block.FastTravelPostBlockEntity;
import com.kmdtravel.data.TravelSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record RenamePostPacket(BlockPos pos, String name) {
    public static void encode(RenamePostPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos());
        buffer.writeUtf(packet.name());
    }

    public static RenamePostPacket decode(FriendlyByteBuf buffer) {
        return new RenamePostPacket(buffer.readBlockPos(), buffer.readUtf(64));
    }

    public static void handle(RenamePostPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null) {
                return;
            }

            ServerLevel level = player.serverLevel();
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
        context.get().setPacketHandled(true);
    }
}

