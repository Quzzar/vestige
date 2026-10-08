package com.quzzar.vestige.magic.runtime;

import com.quzzar.vestige.magic.definition.MagicDefinition;
import com.quzzar.vestige.magic.definition.TraitModifier;
import com.quzzar.vestige.magic.definition.TraitProfile;
import com.quzzar.vestige.magic.expression.MagicVariables;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/** A stable trait and variable snapshot shared by active and passive magical abilities. */
public record MagicResolution(TraitProfile traits, Map<ResourceLocation, Double> variables) {
    public MagicResolution {
        Objects.requireNonNull(traits);
        variables = Map.copyOf(variables);
        if (variables.values().stream().anyMatch(value -> !Double.isFinite(value)))
            throw new IllegalArgumentException("Resolved magic variables must be finite");
    }
    public static MagicResolution resolve(MagicDefinition definition, Collection<TraitModifier> modifiers) {
        TraitProfile traits = definition.traits().resolve(modifiers);
        return new MagicResolution(traits, MagicVariables.resolve(definition.variables(), traits));
    }
    public double variable(ResourceLocation key) {
        Double value = variables.get(key);
        if (value == null) throw new IllegalArgumentException("Unknown magic variable: " + key);
        return value;
    }
}
