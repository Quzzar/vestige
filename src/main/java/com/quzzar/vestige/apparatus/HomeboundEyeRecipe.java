package com.quzzar.vestige.apparatus;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import java.util.*;

/** A four-offering device ritual. Only the Spider Eye's own socket selects payment. */
public final class HomeboundEyeRecipe {
    private HomeboundEyeRecipe() { }
    public static List<Item> ingredients() { return List.of(ScrollItems.ATTUNEMENT_SHARD.get(), Items.SPIDER_EYE, Items.ENDER_PEARL, Items.FLINT); }
    public static boolean matches(List<ItemStack> items) {
        var remaining = new ArrayList<>(ingredients());
        for (var item : items) if (!item.isEmpty() && !remaining.remove(item.getItem())) return false;
        return remaining.isEmpty() && items.stream().filter(i -> i.is(ScrollItems.ATTUNEMENT_SHARD.get())).allMatch(i -> AttunementShardItem.signature(i).isPresent());
    }
    public static Optional<HomeboundEyeItem.Payment> payment(ItemStack material) {
        if (material.isEmpty()) return Optional.of(HomeboundEyeItem.Payment.DURABILITY);
        return switch (BuiltInRegistries.ITEM.getKey(material.getItem()).toString()) {
            case "minecraft:soul_sand" -> Optional.of(HomeboundEyeItem.Payment.HEALTH);
            case "minecraft:moss_block" -> Optional.of(HomeboundEyeItem.Payment.HUNGER);
            case "minecraft:lapis_block" -> Optional.of(HomeboundEyeItem.Payment.EXPERIENCE);
            case "minecraft:amethyst_block" -> Optional.of(HomeboundEyeItem.Payment.MANA);
            default -> Optional.empty();
        };
    }
    public static boolean isPaymentMaterial(ResourceLocation material) {
        return Set.of("minecraft:soul_sand", "minecraft:moss_block", "minecraft:lapis_block", "minecraft:amethyst_block").contains(material.toString());
    }
    public static Optional<ItemStack> result(RitualCrafting.Layout layout) {
        if (layout.geometry().slots() != 4 || !matches(layout.items())) return Optional.empty();
        var inputs = RitualInputs.capture(layout);
        var shard = inputs.nodes().stream().filter(n -> n.offering().is(ScrollItems.ATTUNEMENT_SHARD.get())).findFirst().orElseThrow().offering();
        var eye = inputs.nodes().stream().filter(n -> n.offering().is(Items.SPIDER_EYE)).findFirst().orElseThrow();
        return payment(eye.material()).map(route -> HomeboundEyeItem.bound(AttunementShardItem.signature(shard).orElseThrow().key(),
                layout.level().dimension(), layout.center().getBlockPos(), route));
    }
}
