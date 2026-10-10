package com.quzzar.vestige.apparatus.recipeviewer;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.RitualRecipe;
import com.quzzar.vestige.magic.definition.SpellRarity;
import com.quzzar.vestige.travel.StandingStoneRecipe;
import com.quzzar.vestige.travel.StandingStones;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import java.util.*;

/** The live construction catalog supplies public displays without exposing attunement data. */
public final class StandingStoneDisplays {
    private static final String PREFIX = "ritual/standing_stone/";
    private StandingStoneDisplays() { }
    public static Optional<ItemStack> output(ResourceLocation id) {
        if (!id.getNamespace().equals("vestige") || !id.getPath().startsWith(PREFIX)) return Optional.empty();
        String variant = id.getPath().substring(PREFIX.length());
        return StandingStoneRecipe.variants().stream().filter(value -> value.id().equals(variant)).findFirst().map(StandingStoneRecipe.Variant::preview);
    }
    public static String subtype(ItemStack stack) {
        return StandingStones.material(stack).flatMap(material -> StandingStones.payment(stack).map(payment -> material.id() + "/" + payment.id())).orElse("");
    }
    public static List<RitualDisplays.Entry> entries() {
        return StandingStoneRecipe.variants().stream().map(variant -> new RitualDisplays.Entry(
                VestigeMainMod.location(PREFIX + variant.id()), Optional.empty(), false, SpellRarity.COMMON, 4,
                java.util.stream.IntStream.range(0, 4).mapToObj(i -> new RitualDisplays.Offering(i * 2,
                        new RitualRecipe.Ingredient(List.of(BuiltInRegistries.ITEM.getKey(variant.ingredients().get(i))), List.of()))).toList(),
                variant.payment().material().map(material -> List.of(new RitualDisplays.Imbuement(4, material))).orElse(List.of()))).toList();
    }
}
