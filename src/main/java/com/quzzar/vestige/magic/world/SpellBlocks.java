package com.quzzar.vestige.magic.world;

import com.quzzar.vestige.VestigeMainMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;

/** Real, breakable ice with no harvested item, melting water or movable ownership. */
public final class SpellBlocks {
    public static final DeferredRegister<Block> BLOCKS=DeferredRegister.create(BuiltInRegistries.BLOCK,VestigeMainMod.MOD_ID);
    public static final Supplier<Block> TEMPORARY_ICE=BLOCKS.register("temporary_ice",()->new TemporaryIce());
    public static final Supplier<Block> TEMPORARY_LOG=BLOCKS.register("temporary_log",()->new OwnedBlock(Blocks.OAK_LOG));
    public static final Supplier<Block> TEMPORARY_LEAVES=BLOCKS.register("temporary_leaves",()->new OwnedBlock(Blocks.OAK_LEAVES));
    public static final Supplier<Block> TEMPORARY_WATER=BLOCKS.register("temporary_water",TemporaryWater::new);
    private static void placed(Level level,BlockPos pos,Block block) { if(level instanceof ServerLevel server) server.scheduleTick(pos,block,20); }
    private static void changed(BlockState state,Level level,BlockPos pos,BlockState next) { if(!state.is(next.getBlock()) && level instanceof ServerLevel server) NativeMagic.blockChanged(server,pos); }
    private static void orphan(ServerLevel level,BlockPos pos,Block block) {
        if(NativeMagic.ownsTemporaryBlock(level,pos)) level.scheduleTick(pos,block,20);
        else level.setBlock(pos,Blocks.AIR.defaultBlockState(),3);
    }
    private static BlockBehaviour.Properties ownedProperties(Block model) {
        var properties=BlockBehaviour.Properties.of().strength(model==Blocks.OAK_LOG?2:.2f).sound(model==Blocks.OAK_LOG?SoundType.WOOD:SoundType.GRASS).noLootTable();
        return model==Blocks.OAK_LOG?properties:properties.noOcclusion().isSuffocating((s,l,p)->false).isViewBlocking((s,l,p)->false);
    }
    private static final class OwnedBlock extends Block {
        OwnedBlock(Block model) { super(ownedProperties(model)); }
        @Override protected void onPlace(BlockState state,Level level,BlockPos pos,BlockState previous,boolean piston) { placed(level,pos,this); }
        @Override protected void onRemove(BlockState state,Level level,BlockPos pos,BlockState next,boolean piston) { changed(state,level,pos,next);super.onRemove(state,level,pos,next,piston); }
        @Override protected void tick(BlockState state,ServerLevel level,BlockPos pos,RandomSource random) { orphan(level,pos,this); }
        @Override public net.minecraft.world.level.material.PushReaction getPistonPushReaction(BlockState state) { return net.minecraft.world.level.material.PushReaction.DESTROY; }
    }
    private static final class TemporaryWater extends LiquidBlock {
        TemporaryWater() { super(SpellFluids.WATER.get(),BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()); }
        @Override protected void onPlace(BlockState state,Level level,BlockPos pos,BlockState previous,boolean piston) { super.onPlace(state,level,pos,previous,piston);placed(level,pos,this); }
        @Override protected void onRemove(BlockState state,Level level,BlockPos pos,BlockState next,boolean piston) { changed(state,level,pos,next);super.onRemove(state,level,pos,next,piston); }
        @Override protected void tick(BlockState state,ServerLevel level,BlockPos pos,RandomSource random) { orphan(level,pos,this); }
        @Override public net.minecraft.world.item.ItemStack pickupBlock(net.minecraft.world.entity.player.Player player,net.minecraft.world.level.LevelAccessor level,BlockPos pos,BlockState state) { return net.minecraft.world.item.ItemStack.EMPTY; }
    }
    private SpellBlocks() { }
    private static final class TemporaryIce extends HalfTransparentBlock {
        TemporaryIce() { super(BlockBehaviour.Properties.ofFullCopy(Blocks.ICE).noLootTable()); }
        @Override protected void onPlace(BlockState state,Level level,BlockPos pos,BlockState previous,boolean piston) {
            if (level instanceof ServerLevel server) server.scheduleTick(pos,this,20);
        }
        @Override protected void onRemove(BlockState state,Level level,BlockPos pos,BlockState next,boolean piston) {
            if (!state.is(next.getBlock()) && level instanceof ServerLevel server) NativeMagic.blockChanged(server,pos);
            super.onRemove(state,level,pos,next,piston);
        }
        @Override protected void tick(BlockState state,ServerLevel level,BlockPos pos,RandomSource random) {
            // Scheduled ticks survive chunk saves; an orphan never becomes permanent terrain.
            if (NativeMagic.ownsTemporaryBlock(level,pos)) level.scheduleTick(pos,this,20);
            else level.setBlock(pos,Blocks.AIR.defaultBlockState(),3);
        }
        @Override public net.minecraft.world.level.material.PushReaction getPistonPushReaction(BlockState state) {
            return net.minecraft.world.level.material.PushReaction.DESTROY;
        }
    }
}
