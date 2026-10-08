package com.quzzar.vestige.magic.definition;

import com.quzzar.vestige.magic.expression.MagicVariables;
import com.quzzar.vestige.magic.expression.SpellValue;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Item-owned magic has no scroll discovery, spell traditions, rarity or source provenance. */
public record ItemAbilityDefinition(ResourceLocation id, TraitProfile traits,
                                    Map<ResourceLocation, SpellValue> variables, List<SpellCost> costs,
                                    List<SpellTrigger> triggers, List<SpellEffect> effects, Activation activation) implements MagicDefinition {
    public enum Activation { ACTIVE, REACTIVE, PASSIVE }
    public ItemAbilityDefinition(ResourceLocation id, TraitProfile traits, Map<ResourceLocation, SpellValue> variables,
                                 List<SpellCost> costs, List<SpellTrigger> triggers, List<SpellEffect> effects) {
        this(id, traits, variables, costs, triggers, effects, triggers.isEmpty() ? Activation.PASSIVE : Activation.ACTIVE);
    }
    public ItemAbilityDefinition {
        Objects.requireNonNull(id);
        Objects.requireNonNull(traits);
        variables = MagicVariables.validate(variables);
        costs = List.copyOf(costs);
        triggers = List.copyOf(triggers);
        effects = List.copyOf(effects);
        Objects.requireNonNull(activation);
        if (triggers.stream().map(SpellTrigger::id).distinct().count() != triggers.size())
            throw new IllegalArgumentException("Item ability trigger IDs must be unique");
        if (triggers.isEmpty() && (!costs.isEmpty() || !effects.isEmpty()))
            throw new IllegalArgumentException("An active item ability needs a trigger");
        if (!triggers.isEmpty() && effects.isEmpty())
            throw new IllegalArgumentException("An active item ability needs an effect");
        if ((activation == Activation.PASSIVE) != triggers.isEmpty())
            throw new IllegalArgumentException("Only passive abilities have no activation trigger");
        if (activation == Activation.REACTIVE && costs.stream().anyMatch(SpellCost.Time.class::isInstance))
            throw new IllegalArgumentException("Reactive item abilities cannot reserve preparation time");
    }
    @Override public boolean occupiesCasting() { return activation == Activation.ACTIVE; }
}
