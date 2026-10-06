package com.quzzar.vestige.apparatus;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Shared apparatus behavior; material appearances never change ritual capabilities. */
public final class ApparatusBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public enum Part implements StringRepresentable {
        SINGLE, BASE, SHAFT, CAP;
        @Override public String getSerializedName() { return name().toLowerCase(java.util.Locale.ROOT); }
    }
    public static final EnumProperty<Part> PART=EnumProperty.create("part",Part.class);
    public static final BooleanProperty WATERLOGGED=BlockStateProperties.WATERLOGGED;
    public enum Role {
        PLINTH("plinth", 14), SPELLSTONE("spellstone", ApparatusShapes.SPELLSTONE_SURFACE * 16);
        private final String path;
        private final double surface;
        Role(String path, double surface) {
            this.path=path; this.surface=surface;
        }
        public String path() { return path; }
    }
    public static final MapCodec<ApparatusBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.xmap(s -> Role.valueOf(s.toUpperCase(java.util.Locale.ROOT)), Role::path)
                    .fieldOf("role").forGetter(ApparatusBlock::role), propertiesCodec()
    ).apply(instance, ApparatusBlock::new));
    private final Role role;
    private final VoxelShape shape;

    public ApparatusBlock(Role role, Properties properties) {
        super(properties);
        this.role = role;
        shape = role == Role.SPELLSTONE ? ApparatusShapes.SPELLSTONE : ApparatusShapes.PLINTH;
        registerDefaultState(stateDefinition.any().setValue(PART,Part.SINGLE).setValue(WATERLOGGED,false));
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder) { builder.add(PART,WATERLOGGED); }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        var state=defaultBlockState().setValue(WATERLOGGED,context.getLevel().getFluidState(context.getClickedPos()).is(FluidTags.WATER));
        return connectedState(state,context.getLevel(),context.getClickedPos());
    }
    private BlockState connectedState(BlockState state,BlockGetter level,BlockPos pos) {
        if(role!=Role.PLINTH)return state.setValue(PART,Part.SINGLE);
        boolean above=ApparatusBlocks.isPlinth(level.getBlockState(pos.above()));
        boolean below=ApparatusBlocks.isPlinth(level.getBlockState(pos.below()));
        return state.setValue(PART,above ? below ? Part.SHAFT : Part.BASE : below ? Part.CAP : Part.SINGLE);
    }
    @Override protected BlockState updateShape(BlockState state,Direction direction,BlockState neighbor,LevelAccessor level,BlockPos pos,BlockPos neighborPos) {
        if(state.getValue(WATERLOGGED))level.scheduleTick(pos,Fluids.WATER,Fluids.WATER.getTickDelay(level));
        if(role==Role.PLINTH && direction==Direction.UP && ApparatusBlocks.isPlinth(neighbor)
                && level instanceof Level world && !world.isClientSide
                && world.getBlockEntity(pos) instanceof OfferingBlockEntity offering) {
            // Clear storage before spawning: repeated column updates cannot duplicate an offering.
            // Eject beside the column rather than inside the newly covered shaft.
            Block.popResourceFromFace(world,pos,Direction.NORTH,offering.remove());
        }
        return direction.getAxis()==Direction.Axis.Y ? connectedState(state,level,pos) : state;
    }
    @Override public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }
    @Override protected void onPlace(BlockState state,Level level,BlockPos pos,BlockState previous,boolean moved) {
        super.onPlace(state,level,pos,previous,moved);
        var connected=connectedState(state,level,pos);
        if(!connected.equals(state))level.setBlock(pos,connected,Block.UPDATE_ALL);
    }
    /** Covered column segments keep their stored materials but have no available offering surface. */
    public static boolean exposed(BlockState state) {
        return !(state.getBlock() instanceof ApparatusBlock block) || block.role()!=Role.PLINTH
                || state.getValue(PART)==Part.SINGLE || state.getValue(PART)==Part.CAP;
    }
    /** A side socket remains accessible even when the offering surface is covered. */
    public static boolean hasOfferingSpace(BlockState state,BlockGetter level,BlockPos pos) {
        if(!exposed(state))return false;
        if(!ApparatusBlocks.isPlinth(state))return true;
        var above=level.getBlockState(pos.above());
        return above.isAir() || (!above.getFluidState().isEmpty() && above.getCollisionShape(level,pos.above()).isEmpty());
    }

    public Role role() { return role; }
    public double offeringHeight() { return role.surface / 16.0; }
    public double physicalHeight() { return shape.bounds().maxY; }
    public double offeringWidth() { return shape.bounds().getXsize(); }
    @Override public MapCodec<ApparatusBlock> codec() { return CODEC; }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.ENTITYBLOCK_ANIMATED; }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if(role!=Role.PLINTH)return shape;
        return switch(state.getValue(PART)) {
            case SINGLE -> shape;
            case BASE -> ApparatusShapes.PLINTH_BASE;
            case SHAFT -> ApparatusShapes.PLINTH_SHAFT;
            case CAP -> ApparatusShapes.PLINTH_CAP;
        };
    }
    @Override public PushReaction getPistonPushReaction(BlockState state) { return PushReaction.BLOCK; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new OfferingBlockEntity(pos, state); }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.isEmpty()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!(level.getBlockEntity(pos) instanceof OfferingBlockEntity offering)) return ItemInteractionResult.FAIL;
        // Top face remains an offering slot, including block ingredients. Side faces install a separate socket.
        if (role == Role.PLINTH && hit.getDirection()!=net.minecraft.core.Direction.UP && stack.getItem() instanceof net.minecraft.world.item.BlockItem) {
            // Handle rejected socket clicks too, preventing vanilla placement against the Plinth.
            if (!offering.canInstallMaterial(stack)) return ItemInteractionResult.CONSUME_PARTIAL;
            if (!level.isClientSide) {
                if (!offering.installMaterial(stack)) return ItemInteractionResult.CONSUME_PARTIAL;
                if (!player.hasInfiniteMaterials()) stack.shrink(1);
                level.playSound(null,pos,SoundEvents.AMETHYST_BLOCK_PLACE,SoundSource.BLOCKS,.5f,1.1f);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (offering.busy()) return ItemInteractionResult.FAIL;
        if (!hasOfferingSpace(state,level,pos)) return ItemInteractionResult.CONSUME_PARTIAL;
        if (!offering.displayedItem().isEmpty()) return ItemInteractionResult.FAIL;
        if (!level.isClientSide) ScrollFragmentItem.resolve(player, stack);
        if (!level.isClientSide && offering.insert(stack)) {
            if (!player.hasInfiniteMaterials()) stack.shrink(1);
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, .35f, .85f);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof OfferingBlockEntity offering)) return InteractionResult.PASS;
        if (!offering.resultItem().isEmpty()) {
            if (!level.isClientSide) { ItemStack output=offering.removeResult(); if (!player.getInventory().add(output)) player.drop(output,false); }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (offering.busy()) return InteractionResult.FAIL;
        if (role == Role.PLINTH && player.isShiftKeyDown() && hit.getDirection()!=net.minecraft.core.Direction.UP && !offering.materialItem().isEmpty()) {
            if (!level.isClientSide) {
                ItemStack material=offering.removeMaterial();
                if (!player.getInventory().add(material)) player.drop(material,false);
                level.playSound(null,pos,SoundEvents.AMETHYST_BLOCK_HIT,SoundSource.BLOCKS,.5f,1.2f);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (role == Role.SPELLSTONE && !player.isShiftKeyDown()
                && (offering.displayedItem().isEmpty() || ScrollItems.scroll(offering.displayedItem()).isPresent())) {
            if (!level.isClientSide) RitualCrafting.activate(player,offering);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (offering.displayedItem().isEmpty()) return InteractionResult.PASS;
        if (!level.isClientSide) {
            ItemStack item = offering.remove();
            if (!player.getInventory().add(item)) player.drop(item, false);
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, .35f, .85f);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean piston) {
        if (!state.is(next.getBlock()) && !level.isClientSide && level.getBlockEntity(pos) instanceof OfferingBlockEntity offering) {
            offering.unlock();
            Block.popResource(level, pos, offering.removeMaterial());
            Block.popResource(level, pos, offering.remove());
            Block.popResource(level, pos, offering.removeResult());
        }
        super.onRemove(state, level, pos, next, piston);
    }

}
