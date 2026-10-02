package com.quzzar.vestige.magic.world;

import com.quzzar.vestige.VestigeMainMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.material.*;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;

/** Water physics and vanilla water appearance, confined to the formation's owned cells. */
public final class SpellFluids {
    public static final DeferredRegister<Fluid> FLUIDS=DeferredRegister.create(BuiltInRegistries.FLUID,VestigeMainMod.MOD_ID);
    public static final Supplier<FlowingFluid> WATER=FLUIDS.register("bound_water",Source::new);
    public static final Supplier<FlowingFluid> FLOWING_WATER=FLUIDS.register("flowing_bound_water",Flowing::new);
    private SpellFluids() { }
    private abstract static class BoundWater extends WaterFluid {
        @Override public Fluid getSource() { return WATER.get(); }
        @Override public Fluid getFlowing() { return FLOWING_WATER.get(); }
        @Override public FluidType getFluidType() { return NeoForgeMod.WATER_TYPE.value(); }
        @Override public boolean isSame(Fluid fluid) { return fluid==WATER.get() || fluid==FLOWING_WATER.get(); }
        @Override public Item getBucket() { return Items.AIR; }
        @Override public BlockState createLegacyBlock(FluidState state) { return SpellBlocks.TEMPORARY_WATER.get().defaultBlockState().setValue(LiquidBlock.LEVEL,getLegacyLevel(state)); }
        @Override public void tick(Level level,BlockPos pos,FluidState state) { /* The owning lease grows and drains cells; no unowned flowing descendants. */ }
    }
    private static final class Source extends BoundWater {
        @Override public int getAmount(FluidState state) { return 8; }
        @Override public boolean isSource(FluidState state) { return true; }
    }
    private static final class Flowing extends BoundWater {
        @Override protected void createFluidStateDefinition(StateDefinition.Builder<Fluid,FluidState> builder) { super.createFluidStateDefinition(builder);builder.add(LEVEL); }
        @Override public int getAmount(FluidState state) { return state.getValue(LEVEL); }
        @Override public boolean isSource(FluidState state) { return false; }
    }
}
