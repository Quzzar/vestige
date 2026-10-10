package com.quzzar.vestige.equipment;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.*;
import com.quzzar.vestige.apparatus.recipeviewer.RitualDisplays;
import com.quzzar.vestige.magic.definition.SpellRarity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.DyedItemColor;
import java.util.*;

/** Concrete color/selection entries preserve old plain recipe IDs and use ordinary dynamic viewers. */
public final class WardweaveDisplays {
    private static final String PREFIX = "ritual/wardweave_robes/";
    private WardweaveDisplays() { }
    public static Set<WardweaveImbuements.Choice> choices(int mask) {
        if (mask < 0 || mask > 15) throw new IllegalArgumentException("Invalid Wardweave display");
        var result = EnumSet.noneOf(WardweaveImbuements.Choice.class);
        for (var choice : WardweaveImbuements.Choice.values()) if ((mask & (1 << choice.ordinal())) != 0) result.add(choice);
        return Set.copyOf(result);
    }
    public static ItemStack create(DyeColor color, int mask) {
        var result = WardweaveImbuements.create(choices(mask));
        result.set(DataComponents.DYED_COLOR,new DyedItemColor(color.getTextureDiffuseColor(),false));
        return result;
    }
    public static Optional<ItemStack> output(ResourceLocation id) {
        if (!id.getNamespace().equals("vestige") || !id.getPath().startsWith(PREFIX)) return Optional.empty();
        var parts = id.getPath().substring(PREFIX.length()).split("/", -1);
        if (parts.length != 1 && parts.length != 2) return Optional.empty();
        var color = DyeColor.byName(parts[0],null); if (color == null) return Optional.empty();
        try { return Optional.of(create(color, parts.length == 1 ? 0 : Integer.parseInt(parts[1]))); }
        catch (IllegalArgumentException invalid) { return Optional.empty(); }
    }
    public static List<RitualDisplays.Entry> entries() {
        var result = new ArrayList<RitualDisplays.Entry>();
        for (var color : DyeColor.values()) for (int mask = 0; mask < 16; mask++) {
            var offerings = new ArrayList<RitualDisplays.Offering>();
            for (int i = 0; i < 8; i++) {
                Item item = i % 2 == 0 ? MagicArmorRecipe.wool(color) : i == 1 || i == 5 ? ScrollItems.CALLOUS_THREAD.get()
                        : i == 3 ? Items.IRON_INGOT : Items.PUFFERFISH;
                offerings.add(new RitualDisplays.Offering(i,new RitualRecipe.Ingredient(List.of(BuiltInRegistries.ITEM.getKey(item)),List.of())));
            }
            var materials = new ArrayList<RitualDisplays.Imbuement>();
            for (var choice : choices(mask)) {
                int seat = switch (choice) { case WARDED -> 7; case ENDURING -> 0; case QUICKENED -> 1; case REINFORCED -> 5; };
                Item material = switch (choice) { case WARDED, REINFORCED -> Items.IRON_BLOCK; case ENDURING -> Items.AMETHYST_BLOCK; case QUICKENED -> Items.COPPER_BLOCK; };
                materials.add(new RitualDisplays.Imbuement(seat,BuiltInRegistries.ITEM.getKey(material)));
            }
            result.add(new RitualDisplays.Entry(VestigeMainMod.location(PREFIX+color.getName()+(mask==0?"":"/"+mask)),
                    Optional.empty(),false,SpellRarity.COMMON,8,offerings,materials));
        }
        return List.copyOf(result);
    }
}
