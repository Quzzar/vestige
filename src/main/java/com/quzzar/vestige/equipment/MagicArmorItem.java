package com.quzzar.vestige.equipment;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.Level;

/** Native cloth armor; vanilla-compatible enchants except the two that bypass its wear economy. */
public class MagicArmorItem extends ArmorItem {
    private final ResourceLocation ability;
    public MagicArmorItem(Holder<ArmorMaterial> material, ResourceLocation ability) {
        this(material, ability, Type.CHESTPLATE, new Item.Properties().durability(80).rarity(Rarity.UNCOMMON).component(net.minecraft.core.component.DataComponents.DYED_COLOR, new net.minecraft.world.item.component.DyedItemColor(DyeColor.WHITE.getTextureDiffuseColor(), false)));
    }
    protected MagicArmorItem(Holder<ArmorMaterial> material, ResourceLocation ability, Type type, Item.Properties properties) {
        super(material, type, properties);
        this.ability = ability;
    }
    public ResourceLocation ability() { return ability; }
    @Override public net.minecraft.network.chat.Component getName(ItemStack stack) {
        if (ability.equals(WardweaveImbuements.ABILITY))
            return WardweaveImbuements.read(stack).map(v -> com.quzzar.vestige.magic.presentation.MagicAdjectives.prefix(WardweaveImbuements.FAMILY,v.selections()).append(super.getName(stack)))
                    .orElseGet(() -> super.getName(stack).copy());
        return super.getName(stack);
    }
    private static boolean forbidden(Holder<Enchantment> enchantment) {
        return enchantment.is(Enchantments.UNBREAKING) || enchantment.is(Enchantments.MENDING);
    }
    @Override public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return !forbidden(enchantment) && enchantment.value().isSupportedItem(stack);
    }
    @Override public boolean isPrimaryItemFor(ItemStack stack, Holder<Enchantment> enchantment) {
        return supportsEnchantment(stack, enchantment)
                && enchantment.value().definition().primaryItems().map(stack::is).orElse(true);
    }
    @Override public ItemEnchantments getAllEnchantments(ItemStack stack, HolderLookup.RegistryLookup<Enchantment> lookup) {
        var result = new ItemEnchantments.Mutable(stack.getTagEnchantments());
        result.removeIf(MagicArmorItem::forbidden);
        return result.toImmutable();
    }
    @Override public int getEnchantmentLevel(ItemStack stack, Holder<Enchantment> enchantment) {
        return forbidden(enchantment) ? 0 : stack.getTagEnchantments().getLevel(enchantment);
    }
    @Override public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide()) {
            var original = stack.getTagEnchantments();
            if (original.keySet().stream().anyMatch(MagicArmorItem::forbidden)) {
                var filtered = new ItemEnchantments.Mutable(original);
                filtered.removeIf(MagicArmorItem::forbidden);
                stack.set(net.minecraft.core.component.DataComponents.ENCHANTMENTS, filtered.toImmutable());
            }
        }
        super.inventoryTick(stack, level, entity, slot, selected);
    }
}
