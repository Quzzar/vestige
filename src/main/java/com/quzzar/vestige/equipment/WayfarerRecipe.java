package com.quzzar.vestige.equipment;

import com.quzzar.vestige.apparatus.ScrollItems;
import com.quzzar.vestige.apparatus.Spellshaping;
import net.minecraft.world.item.*;
import java.util.*;

/** Ordered eight-seat construction; every socket stays paired with its own offering under rotation. */
public final class WayfarerRecipe {
    private WayfarerRecipe() { }
    public static boolean matches(List<ItemStack> offerings) {
        if (offerings.size() != 8 || offerings.stream().anyMatch(s -> s.isEmpty() || s.getCount() != 1)) return false;
        for (int r = 0; r < 8; r += 2) {
            boolean match = true;
            for (int i = 0; i < 8; i++) {
                Item required = i % 2 == 0 ? Items.LEATHER : i == 1 || i == 5 ? ScrollItems.LACED_THREAD.get() : i == 3 ? Items.FEATHER : Items.RABBIT_FOOT;
                if (!offerings.get((i + r) % 8).is(required)) { match = false; break; }
            }
            if (match) return true;
        }
        return false;
    }
    public static Optional<ItemStack> result(List<ItemStack> offerings, List<ItemStack> materials) {
        if (!matches(offerings) || materials.size() != 8) return Optional.empty();
        var selected = EnumSet.noneOf(WayfarerImbuements.Choice.class);
        for (int i = 0; i < 8; i++) {
            ItemStack offering = offerings.get(i), material = materials.get(i);
            WayfarerImbuements.Choice choice = offering.is(Items.FEATHER) && material.is(Items.EMERALD_BLOCK) ? WayfarerImbuements.Choice.SWIFT
                    : offering.is(Items.RABBIT_FOOT) && material.is(Items.AMETHYST_BLOCK) ? WayfarerImbuements.Choice.ENDURING
                    : offering.is(Items.LEATHER) && material.is(Items.IRON_BLOCK) ? WayfarerImbuements.Choice.REINFORCED
                    : offering.is(ScrollItems.LACED_THREAD.get()) && material.is(Items.COPPER_BLOCK) ? WayfarerImbuements.Choice.QUICKENED : null;
            if (choice != null) { if (!selected.add(choice)) return Optional.empty(); }
            else if (!material.isEmpty() && Spellshaping.rules().values().stream().anyMatch(rule -> rule.pairs().stream().anyMatch(pair ->
                    pair.offering().equals(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(offering.getItem()))
                            && pair.material().equals(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(material.getItem()))))) return Optional.empty();
        }
        return Optional.of(WayfarerImbuements.create(selected));
    }
}
