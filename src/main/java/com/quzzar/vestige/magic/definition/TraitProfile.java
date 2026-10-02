package com.quzzar.vestige.magic.definition;

import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * An immutable, flat mapping from trait identifiers to their numerical ratings.
 */
public final class TraitProfile {
    private final Map<ResourceLocation, Double> ratings;

    public TraitProfile(Map<ResourceLocation, Double> ratings) {
        Objects.requireNonNull(ratings, "ratings");

        Map<ResourceLocation, Double> validatedRatings = new LinkedHashMap<>();
        ratings.forEach((trait, rating) -> {
            Objects.requireNonNull(trait, "trait");
            Objects.requireNonNull(rating, "rating");
            if (!Double.isFinite(rating) || rating < 0) {
                throw new IllegalArgumentException("Trait ratings must be finite and non-negative");
            }
            if (rating > 0) {
                validatedRatings.put(trait, rating);
            }
        });
        this.ratings = Map.copyOf(validatedRatings);
    }

    /**
     * Creates an empty trait profile.
     */
    public static TraitProfile empty() {
        return new TraitProfile(Map.of());
    }

    /**
     * Returns a trait's rating, or zero when the trait is absent.
     */
    public double rating(ResourceLocation trait) {
        return ratings.getOrDefault(Objects.requireNonNull(trait, "trait"), 0.0);
    }

    /**
     * Returns the immutable trait-to-rating mapping.
     */
    public Map<ResourceLocation, Double> ratings() {
        return ratings;
    }

    /**
     * Produces a resolved profile without mutating this base profile.
     * Additions are combined before multipliers so resolution is independent of modifier order.
     */
    public TraitProfile resolve(Collection<TraitModifier> modifiers) {
        Objects.requireNonNull(modifiers, "modifiers");

        Map<ResourceLocation, Double> additions = new LinkedHashMap<>();
        Map<ResourceLocation, Double> multipliers = new LinkedHashMap<>();
        for (TraitModifier modifier : modifiers) {
            Objects.requireNonNull(modifier, "modifier");
            switch (modifier.operation()) {
                case ADD -> additions.merge(modifier.trait(), modifier.amount(), Double::sum);
                case MULTIPLY -> multipliers.merge(modifier.trait(), modifier.amount(), (left, right) -> left * right);
            }
        }

        Map<ResourceLocation, Double> resolved = new LinkedHashMap<>(ratings);
        additions.keySet().forEach(trait -> resolved.putIfAbsent(trait, 0.0));
        multipliers.keySet().forEach(trait -> resolved.putIfAbsent(trait, 0.0));
        resolved.replaceAll((trait, base) -> Math.max(0, (base + additions.getOrDefault(trait, 0.0))
                * multipliers.getOrDefault(trait, 1.0)));
        return new TraitProfile(resolved);
    }
}
