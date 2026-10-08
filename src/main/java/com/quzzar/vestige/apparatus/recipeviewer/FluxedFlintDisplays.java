package com.quzzar.vestige.apparatus.recipeviewer;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.*;
import com.quzzar.vestige.magic.definition.SpellRarity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import java.util.*;

/** Public construction variants share the live palette; retained materials follow their offerings. */
public final class FluxedFlintDisplays {
    private static final String PREFIX = "ritual/fluxed_flint/";
    private FluxedFlintDisplays() { }
    public static Optional<ItemStack> output(ResourceLocation id) {
        if (!id.getNamespace().equals("vestige") || !id.getPath().startsWith(PREFIX)) return Optional.empty();
        try {
            int index = Integer.parseInt(id.getPath().substring(PREFIX.length()));
            return index < 1 || index >= FluxedFlintImbuements.variants().size() ? Optional.empty()
                    : Optional.of(FluxedFlintImbuements.create(FluxedFlintImbuements.variants().get(index)));
        } catch (NumberFormatException invalid) { return Optional.empty(); }
    }
    public static List<RitualDisplays.Entry> entries() {
        var base = RitualDisplays.fluxedFlint();
        var result = new ArrayList<RitualDisplays.Entry>();
        var variants = FluxedFlintImbuements.variants();
        for (int i = 1; i < variants.size(); i++) result.add(new RitualDisplays.Entry(VestigeMainMod.location(PREFIX + i), Optional.empty(),
                false, SpellRarity.COMMON, 4, base.offerings(), variants.get(i).choices().stream().sorted()
                .map(c -> new RitualDisplays.Imbuement(c.displaySeat, c.material)).toList()));
        return List.copyOf(result);
    }
}
