package com.quzzar.vestige.equipment;

import com.quzzar.vestige.apparatus.ScrollItems;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.core.component.DataComponents;
import java.util.*;

/** Eight offerings, four identical vanilla wool colors; rotations preserve inner/outer placement. */
public final class MagicArmorRecipe {
    private MagicArmorRecipe() { }
    public static Optional<ItemStack> result(List<ItemStack> seats) {
        if (seats.size() != 8 || seats.stream().anyMatch(s -> s.isEmpty() || s.getCount() != 1)) return Optional.empty();
        DyeColor wool = null;
        for (DyeColor color : DyeColor.values()) {
            if (seats.get(0).is(wool(color))) { wool = color; break; }
        }
        if (wool == null) return Optional.empty();
        for (int i = 0; i < 8; i += 2) if (!seats.get(i).is(wool(wool))) return Optional.empty();
        for (int rotation = 0; rotation < 8; rotation += 2) {
            Item thread = seats.get((1 + rotation) % 8).getItem();
            if (!seats.get((5 + rotation) % 8).is(thread)) continue;
            Item first = seats.get((3 + rotation) % 8).getItem(), last = seats.get((7 + rotation) % 8).getItem();
            Item result = thread == ScrollItems.CALLOUS_THREAD.get() && first == Items.IRON_INGOT && last == Items.PUFFERFISH
                    ? MagicEquipment.WARDWEAVE.get()
                    : thread == ScrollItems.SMOLDERING_THREAD.get() && first == Items.BLAZE_POWDER && last == Items.MAGMA_CREAM
                    ? MagicEquipment.CINDERWEAVE.get() : null;
            if (result != null) {
                var output = new ItemStack(result);
                output.set(DataComponents.DYED_COLOR, new DyedItemColor(wool.getTextureDiffuseColor(), false));
                return Optional.of(output);
            }
        }
        return Optional.empty();
    }
    public static Item wool(DyeColor color) {
        return net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.withDefaultNamespace(color.getName() + "_wool"));
    }
}
