package com.quzzar.vestige.apparatus;

import net.minecraft.world.item.*;
import java.util.*;

/** Diamond and skull opposite, with a separate Gunpowder offering on either side. */
public final class DissentientDiamondRecipe {
    private DissentientDiamondRecipe() { }
    public static List<Item> ingredients() { return List.of(Items.DIAMOND, Items.GUNPOWDER, Items.WITHER_SKELETON_SKULL, Items.GUNPOWDER); }
    public static Optional<ItemStack> create(List<ItemStack> seats) {
        return FourSlotPattern.matches(seats, ingredients())
                ? Optional.of(new ItemStack(ScrollItems.DISSENTIENT_DIAMOND.get())) : Optional.empty();
    }
}
