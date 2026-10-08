package com.quzzar.vestige.apparatus.recipeviewer;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.*;
import com.quzzar.vestige.magic.definition.SpellRarity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import java.util.*;

/** The live recipe palette also supplies both optional viewers; no independently maintained variant recipes. */
public final class HourglassDisplays {
    private static final String PREFIX="ritual/kairotic_hourglass/";
    private HourglassDisplays() { }
    public static Optional<ItemStack> output(ResourceLocation id) {
        if(!id.getNamespace().equals("vestige") || !id.getPath().startsWith(PREFIX))return Optional.empty();
        try {
            int index=Integer.parseInt(id.getPath().substring(PREFIX.length()));
            return index<0 || index>=HourglassData.variants().size() ? Optional.empty() : Optional.of(HourglassData.create(HourglassData.variants().get(index)));
        } catch(IllegalArgumentException invalid) { return Optional.empty(); }
    }
    public static List<RitualDisplays.Entry> entries() {
        var offerings=java.util.stream.IntStream.range(0,4).mapToObj(i -> new RitualDisplays.Offering(i*2,
                new RitualRecipe.Ingredient(List.of(BuiltInRegistries.ITEM.getKey(HourglassRecipe.ingredients().get(i))),List.of()))).toList();
        var variants=HourglassData.variants();var entries=new ArrayList<RitualDisplays.Entry>();
        for(int i=0;i<variants.size();i++)entries.add(new RitualDisplays.Entry(VestigeMainMod.location(PREFIX+i),Optional.empty(),false,SpellRarity.COMMON,4,
                offerings,variants.get(i).choices().stream().sorted().map(c -> new RitualDisplays.Imbuement(c.seat(),c.material)).toList()));
        return List.copyOf(entries);
    }
}
