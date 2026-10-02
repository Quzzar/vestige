package com.quzzar.vestige.magic.runtime;

import com.quzzar.vestige.magic.definition.SpecialTraits;
import com.quzzar.vestige.magic.definition.TraitProfile;

import java.util.Objects;

/**
 * Calculates the chance that a cast is replaced by a random forfeit.
 *
 * <p>The policy preserves Electroblob's 20% baseline for undiscovered spells. Volatility is inherent to the spell, so
 * its chance applies whether or not the caster has discovered the spell. The higher of the discovery risk and the
 * spell's inherent volatile risk is used, capped at certainty.</p>
 *
 * <p>{@code vestige:volatile} is the spell engine's sole semantic trait. Do not teach this policy or another core
 * system to recognize a second trait name without an explicit design conversation with the project owner.</p>
 *
 * @param undiscoveredChance the minimum forfeit chance for an undiscovered spell
 * @param chancePerVolatilePoint the inherent forfeit chance contributed by each volatile point
 */
public record ForfeitPolicy(double undiscoveredChance, double chancePerVolatilePoint) {
    public static final double LEGACY_UNDISCOVERED_CHANCE = 0.20;
    public static final double DEFAULT_CHANCE_PER_VOLATILE_POINT = 0.05;
    public static final ForfeitPolicy DEFAULT = new ForfeitPolicy(
            LEGACY_UNDISCOVERED_CHANCE,
            DEFAULT_CHANCE_PER_VOLATILE_POINT
    );

    public ForfeitPolicy {
        requireProbability(undiscoveredChance, "undiscoveredChance");
        requireProbability(chancePerVolatilePoint, "chancePerVolatilePoint");
    }

    /**
     * Returns the forfeit probability for a resolved trait profile and the caster's discovery state.
     *
     * <p>At the default tuning, each volatile point contributes five percentage points. An undiscovered spell has at
     * least the legacy 20% risk, while a discovered nonvolatile spell has no forfeit risk.</p>
     */
    public double chance(TraitProfile traits, boolean discovered) {
        Objects.requireNonNull(traits, "traits");

        double discoveryRisk = discovered ? 0.0 : undiscoveredChance;
        double inherentRisk = traits.rating(SpecialTraits.VOLATILE) * chancePerVolatilePoint;
        return Math.min(1.0, Math.max(discoveryRisk, inherentRisk));
    }

    private static void requireProbability(double value, String name) {
        if (!Double.isFinite(value) || value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException(name + " must be a finite probability from 0 to 1");
        }
    }
}
