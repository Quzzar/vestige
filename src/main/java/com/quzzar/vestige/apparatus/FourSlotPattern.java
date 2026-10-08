package com.quzzar.vestige.apparatus;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.util.List;

/** Clockwise inner-layer patterns, equivalent under whole quarter-turns. Outer nodes are inactive. */
public final class FourSlotPattern {
    private FourSlotPattern() { }
    public static boolean matches(List<ItemStack> seats, List<Item> ingredients) {
        if (seats.size() != 8 || ingredients.isEmpty() || ingredients.size() > 4) return false;
        for (int rotation = 0; rotation < 4; rotation++) {
            boolean match = true;
            for (int i = 0; i < 4; i++) {
                var offering = seats.get(((i + rotation) % 4) * 2);
                if (i < ingredients.size() ? offering.getCount() != 1 || !offering.is(ingredients.get(i)) : !offering.isEmpty()) {
                    match = false;
                    break;
                }
            }
            if (match) return true;
        }
        return false;
    }
}
