package com.quzzar.vestige.magic.expression;

import com.quzzar.vestige.magic.definition.TraitProfile;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Pure named formulas, validated before use and resolved once per activation. */
public final class MagicVariables {
    private static final int MAX_VARIABLES = 64, MAX_DEPTH = 32;
    private MagicVariables() { }

    public static Map<ResourceLocation, SpellValue> validate(Map<ResourceLocation, SpellValue> variables) {
        Map<ResourceLocation, SpellValue> copy = Map.copyOf(variables);
        if (copy.size() > MAX_VARIABLES) throw new IllegalArgumentException("Magic supports at most 64 named variables");
        Set<ResourceLocation> complete = new HashSet<>();
        for (ResourceLocation key : copy.keySet()) check(key, copy, new HashSet<>(), complete, 0);
        return copy;
    }

    private static void check(ResourceLocation key, Map<ResourceLocation, SpellValue> variables,
                              Set<ResourceLocation> visiting, Set<ResourceLocation> complete, int depth) {
        if (depth > MAX_DEPTH) throw new IllegalArgumentException("Magic variable nesting exceeds 32");
        if (complete.contains(key)) return;
        if (!variables.containsKey(key)) throw new IllegalArgumentException("Unknown magic variable: " + key);
        if (!visiting.add(key)) throw new IllegalArgumentException("Cyclic magic variable: " + key);
        checkValue(variables.get(key), variables, visiting, complete, depth + 1);
        visiting.remove(key);
        complete.add(key);
    }

    private static void checkValue(SpellValue value, Map<ResourceLocation, SpellValue> variables,
                                   Set<ResourceLocation> visiting, Set<ResourceLocation> complete, int depth) {
        if (depth > MAX_DEPTH) throw new IllegalArgumentException("Magic variable nesting exceeds 32");
        switch (value) {
            case SpellValue.Variable variable -> check(variable.key(), variables, visiting, complete, depth + 1);
            case SpellValue.Sum sum -> sum.terms().forEach(v -> checkValue(v, variables, visiting, complete, depth + 1));
            case SpellValue.Product product -> product.factors().forEach(v -> checkValue(v, variables, visiting, complete, depth + 1));
            case SpellValue.Clamp clamp -> checkValue(clamp.value(), variables, visiting, complete, depth + 1);
            case SpellValue.Fact ignored -> throw new IllegalArgumentException("Magic variables use traits; capture world facts in an effect");
            default -> { }
        }
    }

    public static Map<ResourceLocation, Double> resolve(Map<ResourceLocation, SpellValue> variables, TraitProfile traits) {
        Map<ResourceLocation, Double> resolved = new HashMap<>();
        for (ResourceLocation key : variables.keySet()) resolveKey(key, variables, traits, resolved, 0);
        return Map.copyOf(resolved);
    }

    private static double resolveKey(ResourceLocation key, Map<ResourceLocation, SpellValue> variables,
                                     TraitProfile traits, Map<ResourceLocation, Double> resolved, int depth) {
        if (depth > MAX_DEPTH) throw new IllegalArgumentException("Magic variable nesting exceeds 32");
        if (resolved.containsKey(key)) return resolved.get(key);
        SpellValue value = variables.get(key);
        if (value == null) throw new IllegalArgumentException("Unknown magic variable: " + key);
        double result = resolveValue(value, variables, traits, resolved, depth + 1);
        if (!Double.isFinite(result)) throw new IllegalArgumentException("Magic variables must resolve to finite values");
        resolved.put(key, result);
        return result;
    }

    private static double resolveValue(SpellValue value, Map<ResourceLocation, SpellValue> variables,
                                       TraitProfile traits, Map<ResourceLocation, Double> resolved, int depth) {
        if (depth > MAX_DEPTH) throw new IllegalArgumentException("Magic variable nesting exceeds 32");
        return switch (value) {
            case SpellValue.Variable variable -> resolveKey(variable.key(), variables, traits, resolved, depth + 1);
            case SpellValue.Sum sum -> sum.terms().stream().mapToDouble(v -> resolveValue(v, variables, traits, resolved, depth + 1)).sum();
            case SpellValue.Product product -> product.factors().stream().mapToDouble(v -> resolveValue(v, variables, traits, resolved, depth + 1)).reduce(1, (a, b) -> a * b);
            case SpellValue.Clamp clamp -> Math.clamp(resolveValue(clamp.value(), variables, traits, resolved, depth + 1), clamp.minimum(), clamp.maximum());
            default -> value.resolve(traits);
        };
    }
}
