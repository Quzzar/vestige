package com.quzzar.vestige.magic.definition;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

/**
 * A typed cost that must be satisfied by a spell cast.
 */
public sealed interface SpellCost permits SpellCost.Time, SpellCost.Cooldown, SpellCost.Material, SpellCost.Mana, SpellCost.Health, SpellCost.Hunger, SpellCost.Experience {
    /** Per-actor, per-spell recovery, starting when the initial cast pays. Recasts share it. */
    record Cooldown(int ticks) implements SpellCost {
        public Cooldown { requirePositive(ticks, "Cooldown costs"); }
    }

    /**
     * A charge-up duration measured in Minecraft ticks.
     *
     * @param ticks the positive charge-up duration
     */
    record Time(int ticks) implements SpellCost {
        public Time {
            requirePositive(ticks, "Time costs");
        }
    }

    /**
     * An item consumption or durability cost.
     *
     * @param item the required item
     * @param operation how the item is spent
     * @param amount the number of items or durability points spent
     */
    record Material(ResourceLocation item, Operation operation, int amount) implements SpellCost {
        public Material {
            Objects.requireNonNull(item, "item");
            Objects.requireNonNull(operation, "operation");
            requirePositive(amount, "Material costs");
        }

        /**
         * How the matching material is spent.
         */
        public enum Operation {
            CONSUME,
            DAMAGE
        }
    }

    /**
     * Mana removed from the responsible caster or source.
     *
     * @param amount the positive mana amount
     */
    record Mana(double amount) implements SpellCost {
        public Mana {
            requirePositive(amount, "Mana costs");
        }
    }

    /**
     * Health removed from the responsible caster or source.
     *
     * @param amount the positive health amount
     */
    record Health(double amount) implements SpellCost {
        public Health {
            requirePositive(amount, "Health costs");
        }
    }

    /**
     * Hunger removed from the responsible caster or source.
     *
     * @param amount the positive hunger amount
     */
    record Hunger(int amount) implements SpellCost {
        public Hunger {
            requirePositive(amount, "Hunger costs");
        }
    }

    /** Current experience points, never levels or the historical total counter. */
    record Experience(int amount) implements SpellCost {
        public Experience { requirePositive(amount, "Experience costs"); }
    }

    private static void requirePositive(int amount, String label) {
        if (amount <= 0) {
            throw new IllegalArgumentException(label + " must be positive");
        }
    }

    private static void requirePositive(double amount, String label) {
        if (!Double.isFinite(amount) || amount <= 0) {
            throw new IllegalArgumentException(label + " must be finite and positive");
        }
    }
}
