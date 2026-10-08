package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.magic.definition.TraitProfile;
import net.minecraft.world.item.ItemStack;

/** Intrinsic item traits when offered to a ritual; contained spells are separate objects. */
public interface RitualTraitSource {
    TraitProfile ritualTraits(ItemStack stack);
}
