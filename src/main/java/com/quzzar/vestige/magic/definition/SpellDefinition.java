package com.quzzar.vestige.magic.definition;

import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * An immutable spell program composed of rarity, traditions, traits, costs, triggers, and effects.
 *
 * @param id the spell's stable identifier
 * @param rarity the spell's authored rarity classification
 * @param traditions the traditions through which the spell can be cast
 * @param traits the spell's base trait profile
 * @param costs the costs required to cast the spell
 * @param triggers the event patterns that can activate the spell
 * @param effects the outcomes produced by the spell
 */
public record SpellDefinition(
        ResourceLocation id,
        SpellRarity rarity,
        Set<Tradition> traditions,
        TraitProfile traits,
        List<SpellCost> costs,
        List<SpellTrigger> triggers,
        List<SpellEffect> effects,
        Map<ResourceLocation, SpellMode> modes,
        java.util.Optional<SpellSource> source
) {
    public SpellDefinition(ResourceLocation id, Set<Tradition> traditions, TraitProfile traits,
                           List<SpellCost> costs, List<SpellTrigger> triggers, List<SpellEffect> effects) {
        this(id, SpellRarity.COMMON, traditions, traits, costs, triggers, effects, Map.of(), java.util.Optional.empty());
    }

    public SpellDefinition(ResourceLocation id, Set<Tradition> traditions, TraitProfile traits,
                           List<SpellCost> costs, List<SpellTrigger> triggers, List<SpellEffect> effects,
                           Map<ResourceLocation, SpellMode> modes) {
        this(id, SpellRarity.COMMON, traditions, traits, costs, triggers, effects, modes, java.util.Optional.empty());
    }

    public SpellDefinition(ResourceLocation id, Set<Tradition> traditions, TraitProfile traits,
                           List<SpellCost> costs, List<SpellTrigger> triggers, List<SpellEffect> effects,
                           Map<ResourceLocation, SpellMode> modes, java.util.Optional<SpellSource> source) {
        this(id, SpellRarity.COMMON, traditions, traits, costs, triggers, effects, modes, source);
    }

    public SpellDefinition(ResourceLocation id, SpellRarity rarity, Set<Tradition> traditions, TraitProfile traits,
                           List<SpellCost> costs, List<SpellTrigger> triggers, List<SpellEffect> effects) {
        this(id, rarity, traditions, traits, costs, triggers, effects, Map.of(), java.util.Optional.empty());
    }

    public SpellDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(rarity, "rarity");
        Objects.requireNonNull(traditions, "traditions");
        Objects.requireNonNull(traits, "traits");
        Objects.requireNonNull(costs, "costs");
        Objects.requireNonNull(triggers, "triggers");
        Objects.requireNonNull(effects, "effects");
        Objects.requireNonNull(source, "source");
        if (traditions.isEmpty()) {
            throw new IllegalArgumentException("A spell needs at least one compatible tradition");
        }
        if (triggers.isEmpty()) {
            throw new IllegalArgumentException("A spell needs at least one trigger");
        }
        if (effects.isEmpty()) {
            throw new IllegalArgumentException("A spell needs at least one effect");
        }
        traditions = Set.copyOf(traditions);
        costs = List.copyOf(costs);
        triggers = List.copyOf(triggers);
        effects = List.copyOf(effects);
        modes = Map.copyOf(modes);
        modes.forEach((key, mode) -> {
            if (!key.equals(mode.id())) throw new IllegalArgumentException("Mode key differs from mode identity");
        });
        if (triggers.stream().map(SpellTrigger::id).distinct().count() != triggers.size()) {
            throw new IllegalArgumentException("Spell trigger IDs must be unique within a spell");
        }
    }
}
