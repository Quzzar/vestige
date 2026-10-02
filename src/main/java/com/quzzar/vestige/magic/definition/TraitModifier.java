package com.quzzar.vestige.magic.definition;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

/**
 * One adjustment applied while resolving a spell's trait profile for a cast.
 *
 * @param trait the trait to adjust
 * @param operation the operation used to adjust the trait
 * @param amount the amount supplied to the operation
 */
public record TraitModifier(ResourceLocation trait, Operation operation, double amount) {
    public TraitModifier {
        Objects.requireNonNull(trait, "trait");
        Objects.requireNonNull(operation, "operation");
        if (!Double.isFinite(amount)) {
            throw new IllegalArgumentException("Trait modifier amounts must be finite");
        }
        if (operation == Operation.MULTIPLY && amount < 0) {
            throw new IllegalArgumentException("Trait multipliers cannot be negative");
        }
    }

    /**
     * The supported operations for resolving trait ratings.
     */
    public enum Operation {
        ADD,
        MULTIPLY
    }
}
