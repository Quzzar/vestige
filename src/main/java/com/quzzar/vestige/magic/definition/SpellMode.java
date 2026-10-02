package com.quzzar.vestige.magic.definition;

import net.minecraft.resources.ResourceLocation;
import java.util.List;
import java.util.Objects;

/** An alternate cost and effect plan retaining the parent spell's identity and traits. */
public record SpellMode(ResourceLocation id, List<SpellCost> costs, List<SpellEffect> effects) {
    public SpellMode {
        Objects.requireNonNull(id);
        costs = List.copyOf(costs);
        effects = List.copyOf(effects);
        if (effects.isEmpty()) throw new IllegalArgumentException("A cast mode needs effects");
    }
}
