package com.quzzar.vestige.equipment;

import com.quzzar.vestige.magic.presentation.MagicAdjectives;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;

/** Ordinary armor equipment/repair rules, with trusted crafted naming and source identity. */
public final class WayfarerBootsItem extends MagicArmorItem {
    public WayfarerBootsItem(Holder<ArmorMaterial> material) {
        super(material, WayfarerImbuements.ABILITY, Type.BOOTS, new Item.Properties().durability(65).rarity(Rarity.UNCOMMON));
    }
    @Override public Component getName(ItemStack stack) {
        return WayfarerImbuements.read(stack).map(v -> MagicAdjectives.prefix(WayfarerImbuements.FAMILY, v.selections()).append(super.getName(stack)))
                .orElseGet(() -> super.getName(stack).copy());
    }
}
