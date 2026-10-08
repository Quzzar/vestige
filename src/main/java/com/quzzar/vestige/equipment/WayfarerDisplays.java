package com.quzzar.vestige.equipment;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.*;
import com.quzzar.vestige.apparatus.recipeviewer.RitualDisplays;
import com.quzzar.vestige.magic.definition.SpellRarity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import java.util.*;

/** All sixteen exact outputs and their retained material frames use the same public recipe viewer. */
public final class WayfarerDisplays {
    private static final String PREFIX = "ritual/wayfarer_boots/";
    private WayfarerDisplays() { }
    public static Set<WayfarerImbuements.Choice> choices(int mask) {
        if (mask < 0 || mask > 15) throw new IllegalArgumentException("Invalid Wayfarer display");
        var result = EnumSet.noneOf(WayfarerImbuements.Choice.class);
        for (var choice : WayfarerImbuements.Choice.values()) if ((mask & (1 << choice.ordinal())) != 0) result.add(choice);
        return Set.copyOf(result);
    }
    public static Optional<ItemStack> output(ResourceLocation id) {
        if (!id.getNamespace().equals("vestige") || !id.getPath().startsWith(PREFIX)) return Optional.empty();
        try { return Optional.of(WayfarerImbuements.create(choices(Integer.parseInt(id.getPath().substring(PREFIX.length()))))); }
        catch (IllegalArgumentException invalid) { return Optional.empty(); }
    }
    public static List<RitualDisplays.Entry> entries() {
        var result = new ArrayList<RitualDisplays.Entry>();
        for (int mask = 0; mask < 16; mask++) {
            var offerings = new ArrayList<RitualDisplays.Offering>();
            for (int i = 0; i < 8; i++) {
                Item item = i % 2 == 0 ? Items.LEATHER : i == 1 || i == 5 ? ScrollItems.LACED_THREAD.get() : i == 3 ? Items.FEATHER : Items.RABBIT_FOOT;
                offerings.add(new RitualDisplays.Offering(i, new RitualRecipe.Ingredient(List.of(BuiltInRegistries.ITEM.getKey(item)), List.of())));
            }
            var materials = new ArrayList<RitualDisplays.Imbuement>();
            for (var choice : choices(mask)) {
                int seat = switch (choice) { case SWIFT -> 3; case ENDURING -> 7; case REINFORCED -> 0; case QUICKENED -> 1; };
                Item material = switch (choice) { case SWIFT -> Items.EMERALD_BLOCK; case ENDURING -> Items.AMETHYST_BLOCK; case REINFORCED -> Items.IRON_BLOCK; case QUICKENED -> Items.COPPER_BLOCK; };
                materials.add(new RitualDisplays.Imbuement(seat, BuiltInRegistries.ITEM.getKey(material)));
            }
            result.add(new RitualDisplays.Entry(VestigeMainMod.location(PREFIX + mask), Optional.empty(), false, SpellRarity.COMMON, 8, offerings, materials));
        }
        return List.copyOf(result);
    }
}
