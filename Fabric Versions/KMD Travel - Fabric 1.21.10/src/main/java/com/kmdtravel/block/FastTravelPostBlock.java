package com.kmdtravel.block;

import com.kmdtravel.data.PlayerTravelData;
import com.kmdtravel.data.TravelSavedData;
import com.kmdtravel.network.KMDNetwork;
import com.kmdtravel.network.OpenRenamePostPacket;
import com.kmdtravel.network.OpenTravelScreenPacket;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

public class FastTravelPostBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final MapCodec<FastTravelPostBlock> CODEC = simpleCodec(FastTravelPostBlock::new);
    public static final net.minecraft.world.level.block.state.properties.EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    private static final VoxelShape LOWER_NORTH_SHAPE = Block.box(9.5D, 0.0D, 4.5D, 16.0D, 16.0D, 11.5D);
    private static final VoxelShape LOWER_SOUTH_SHAPE = Block.box(0.0D, 0.0D, 4.5D, 6.5D, 16.0D, 11.5D);
    private static final VoxelShape LOWER_EAST_SHAPE = Block.box(4.5D, 0.0D, 9.5D, 11.5D, 16.0D, 16.0D);
    private static final VoxelShape LOWER_WEST_SHAPE = Block.box(4.5D, 0.0D, 0.0D, 11.5D, 16.0D, 6.5D);
    private static final VoxelShape UPPER_NORTH_SOUTH_SHAPE = Block.box(0.0D, 0.0D, 5.0D, 16.0D, 16.0D, 11.0D);
    private static final VoxelShape UPPER_EAST_WEST_SHAPE = Block.box(5.0D, 0.0D, 0.0D, 11.0D, 16.0D, 16.0D);
    private static final VoxelShape FANCY_SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);
    private final boolean shared;

    public FastTravelPostBlock(Properties properties) {
        this(properties, false);
    }

    public FastTravelPostBlock(Properties properties, boolean shared) {
        super(properties);
        this.shared = shared;
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HALF, DoubleBlockHalf.LOWER));
    }

    public boolean isShared() {
        return shared;
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos above = context.getClickedPos().above();
        if (above.getY() >= context.getLevel().getMaxY() || !context.getLevel().getBlockState(above).canBeReplaced(context)) {
            return null;
        }
        return defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(HALF, DoubleBlockHalf.LOWER);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), 3);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof FastTravelPostBlockEntity post) {
            post.setShared(shared);
            if (stack.has(DataComponents.CUSTOM_NAME)) {
                post.setPostName(stack.getHoverName());
            }
            TravelSavedData.get((ServerLevel) level).upsertFromPost(post);
            if (placer instanceof ServerPlayer serverPlayer) {
                discoverPost(serverPlayer, post);
            }
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return interact(state, level, pos, player, InteractionHand.MAIN_HAND, ItemStack.EMPTY);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return interact(state, level, pos, player, hand, held);
    }

    private InteractionResult interact(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack held) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockPos lowerPos = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
        if (!(level.getBlockEntity(lowerPos) instanceof FastTravelPostBlockEntity post) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.CONSUME;
        }

        if (player.isShiftKeyDown()) {
            KMDNetwork.sendToPlayer(serverPlayer, new OpenRenamePostPacket(lowerPos, post.getPostName().getString()));
            return InteractionResult.CONSUME;
        }

        if (held.getItem() instanceof BannerItem bannerItem) {
            int pattern = held.getComponents().hashCode();
            post.setMarker(bannerItem.getColor().getTextColor(), pattern);
            TravelSavedData.get((ServerLevel) level).upsertFromPost(post);
            player.displayClientMessage(Component.translatable("message.kmdtravel.marker_changed", post.getPostName()), true);
            return InteractionResult.CONSUME;
        }

        if (held.getItem() instanceof DyeItem dyeItem) {
            post.setTextColor(dyeItem.getDyeColor().getTextColor());
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
            player.displayClientMessage(Component.translatable("message.kmdtravel.dyed", post.getPostName()), true);
            return InteractionResult.CONSUME;
        }

        if (held.is(Items.NAME_TAG) && held.has(DataComponents.CUSTOM_NAME)) {
            post.setPostName(held.getHoverName());
            TravelSavedData.get((ServerLevel) level).upsertFromPost(post);
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }
            player.displayClientMessage(Component.translatable("message.kmdtravel.renamed", post.getPostName()), true);
            return InteractionResult.CONSUME;
        }

        TravelSavedData savedData = TravelSavedData.get((ServerLevel) level);
        savedData.upsertFromPost(post);
        discoverPost(serverPlayer, post);
        KMDNetwork.sendToPlayer(serverPlayer, OpenTravelScreenPacket.from(serverPlayer, post.getLocationId()));
        return InteractionResult.CONSUME;
    }

    private static void discoverPost(ServerPlayer player, FastTravelPostBlockEntity post) {
        if (PlayerTravelData.discover(player, post.getLocationId())) {
            player.displayClientMessage(Component.translatable("message.kmdtravel.discovered", post.getPostName()), false);
            player.level().playSound(null, post.getBlockPos(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.9F, 1.25F);
        }
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess tickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighbor, RandomSource random) {
        DoubleBlockHalf half = state.getValue(HALF);
        if ((direction == Direction.UP && half == DoubleBlockHalf.LOWER && !neighbor.is(this))
                || (direction == Direction.DOWN && half == DoubleBlockHalf.UPPER && !neighbor.is(this))) {
            return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, tickAccess, pos, direction, neighborPos, neighbor, random);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockPos otherPos = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
        BlockPos lowerPos = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
        if (!level.isClientSide() && level.getBlockEntity(lowerPos) instanceof FastTravelPostBlockEntity post) {
            TravelSavedData.get((ServerLevel) level).remove(post.getLocationId());
        }
        BlockState otherState = level.getBlockState(otherPos);
        if (otherState.is(this)) {
            level.setBlock(otherPos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 35);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (shared) {
            return FANCY_SHAPE;
        }
        Direction facing = state.getValue(FACING);
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            return facing.getAxis() == Direction.Axis.X ? UPPER_EAST_WEST_SHAPE : UPPER_NORTH_SOUTH_SHAPE;
        }
        return switch (facing) {
            case SOUTH -> LOWER_SOUTH_SHAPE;
            case EAST -> LOWER_EAST_SHAPE;
            case WEST -> LOWER_WEST_SHAPE;
            default -> LOWER_NORTH_SHAPE;
        };
    }

    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? new FastTravelPostBlockEntity(pos, state) : null;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF);
    }
}
