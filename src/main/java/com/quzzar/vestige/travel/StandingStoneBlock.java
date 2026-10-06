package com.quzzar.vestige.travel;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.quzzar.vestige.apparatus.ApparatusMaterials;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

/** A two-block native monolith. Only its lower half owns an endpoint and a bound item drop. */
public final class StandingStoneBlock extends BaseEntityBlock {
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<StandingStoneShape> PROFILE = EnumProperty.create("profile", StandingStoneShape.class);
    public static final MapCodec<StandingStoneBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.xmap(value -> ApparatusMaterials.valueOf(value.toUpperCase(java.util.Locale.ROOT)), ApparatusMaterials::id)
                    .fieldOf("material").forGetter(StandingStoneBlock::material), propertiesCodec()
    ).apply(instance, StandingStoneBlock::new));
    private final ApparatusMaterials material;
    public StandingStoneBlock(ApparatusMaterials material, Properties properties) {
        super(properties); this.material = material;
        registerDefaultState(stateDefinition.any().setValue(HALF, DoubleBlockHalf.LOWER)
                .setValue(FACING, Direction.NORTH).setValue(PROFILE, StandingStoneShape.BLADE));
    }
    public ApparatusMaterials material() { return material; }
    @Override public MapCodec<StandingStoneBlock> codec() { return CODEC; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(HALF, FACING, PROFILE); }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? new StandingStoneEntity(pos, state) : null;
    }
    @Override public PushReaction getPistonPushReaction(BlockState state) { return PushReaction.BLOCK; }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return StandingStoneCollision.shape(state.getValue(PROFILE), state.getValue(HALF), state.getValue(FACING));
    }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        if (pos.getY() >= context.getLevel().getMaxBuildHeight() - 1 || !context.getLevel().getBlockState(pos.above()).canBeReplaced(context)) return null;
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(PROFILE, StandingStoneShape.fromKey(StandingStones.key(context.getItemInHand()).orElse("")));
    }
    @Override public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), 3);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof StandingStoneEntity stone)
            stone.configure(StandingStones.key(stack).orElse(""), stack.getHoverName().getString());
    }
    @Override protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        var half = state.getValue(HALF);
        if (direction == (half == DoubleBlockHalf.LOWER ? Direction.UP : Direction.DOWN)
                && (!neighbor.is(this) || neighbor.getValue(HALF) == half)) return Blocks.AIR.defaultBlockState();
        return super.updateShape(state, direction, neighbor, level, pos, neighborPos);
    }
    @Override public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && player.isCreative() && state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            var lower = level.getBlockState(pos.below());
            if (lower.is(this) && lower.getValue(HALF) == DoubleBlockHalf.LOWER) {
                level.setBlock(pos.below(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
                level.levelEvent(player, 2001, pos.below(), Block.getId(lower));
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
    @Override protected BlockState rotate(BlockState state, Rotation rotation) { return state.setValue(FACING, rotation.rotate(state.getValue(FACING))); }
    @Override protected BlockState mirror(BlockState state, Mirror mirror) { return state.rotate(mirror.getRotation(state.getValue(FACING))); }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        BlockPos base = state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
        if (player instanceof ServerPlayer serverPlayer && level.getBlockEntity(base) instanceof StandingStoneEntity stone)
            StoneTravel.open(serverPlayer, stone, 0);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moved) {
        if (state.getBlock() != replacement.getBlock() && level.getBlockEntity(pos) instanceof StandingStoneEntity stone) stone.unregister();
        super.onRemove(state, level, pos, replacement, moved);
    }
}
