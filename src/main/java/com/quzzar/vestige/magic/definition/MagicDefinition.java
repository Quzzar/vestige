package com.quzzar.vestige.magic.definition;

import com.quzzar.vestige.magic.expression.SpellValue;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;

/** Immutable executable magic shared by spells and trusted item abilities. */
public interface MagicDefinition {
    ResourceLocation id();
    TraitProfile traits();
    Map<ResourceLocation, SpellValue> variables();
    List<SpellCost> costs();
    List<SpellTrigger> triggers();
    List<SpellEffect> effects();
    /** Reactive equipment may run during a player's prepared/channelled spell. */
    default boolean occupiesCasting() { return true; }
    default Map<ResourceLocation, SpellMode> modes() { return Map.of(); }
}
