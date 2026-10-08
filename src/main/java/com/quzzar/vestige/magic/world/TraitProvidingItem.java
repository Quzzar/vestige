package com.quzzar.vestige.magic.world;

import com.quzzar.vestige.magic.definition.MagicDefinition;
import com.quzzar.vestige.magic.definition.TraitModifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Trusted item implementations explicitly select their supported slots and eligible base definitions. */
public interface TraitProvidingItem {
    List<TraitModifier> traitModifiers(ItemStack stack, EquipmentSlot slot, MagicDefinition recipient);
}
