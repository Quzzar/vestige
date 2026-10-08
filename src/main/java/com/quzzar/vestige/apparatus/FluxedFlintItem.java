package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.magic.definition.SpecialTraits;
import com.quzzar.vestige.magic.definition.TraitProfile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.util.Map;

/** Starting playtest budget: one point spent per point of durability restored. */
public final class FluxedFlintItem extends Item implements RitualTraitSource {
    public static final int DURABILITY = 128;
    public static final TraitProfile TRAITS = new TraitProfile(Map.of(SpecialTraits.VOLATILE, 2.0));
    public FluxedFlintItem(Properties properties) { super(properties); }
    @Override public TraitProfile ritualTraits(ItemStack stack) { return TRAITS; }
    @Override public boolean isRepairable(ItemStack stack) { return false; }
    @Override public boolean canGrindstoneRepair(ItemStack stack) { return false; }
    @Override public boolean supportsEnchantment(ItemStack stack, net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment> enchantment) { return false; }
}
