package com.quzzar.vestige.magic.expression;

import com.quzzar.vestige.magic.definition.TraitProfile;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;

/**
 * A numerical value that an effect can resolve from a cast's trait profile.
 */
public sealed interface SpellValue permits SpellValue.Constant, SpellValue.Trait, SpellValue.Sum, SpellValue.Product, SpellValue.Fact {
    /**
     * Resolves this value against the supplied trait profile.
     */
    double resolve(TraitProfile traits);

    /** Numerical world or stored state, resolved by a cast context. */
    record Fact(ResourceLocation path) implements SpellValue {
        public Fact { Objects.requireNonNull(path); }
        @Override public double resolve(TraitProfile traits) { throw new IllegalStateException("A fact requires a cast context"); }
    }

    /**
     * A literal numerical value.
     *
     * @param value the literal value
     */
    record Constant(double value) implements SpellValue {
        public Constant {
            if (!Double.isFinite(value)) {
                throw new IllegalArgumentException("Spell constants must be finite");
            }
        }

        @Override
        public double resolve(TraitProfile traits) {
            Objects.requireNonNull(traits, "traits");
            return value;
        }
    }

    /**
     * A reference to one entry in the resolved trait profile.
     *
     * @param trait the referenced trait
     */
    record Trait(ResourceLocation trait) implements SpellValue {
        public Trait {
            Objects.requireNonNull(trait, "trait");
        }

        @Override
        public double resolve(TraitProfile traits) {
            return Objects.requireNonNull(traits, "traits").rating(trait);
        }
    }

    /**
     * The sum of one or more spell values.
     *
     * @param terms the values to add
     */
    record Sum(List<SpellValue> terms) implements SpellValue {
        public Sum {
            Objects.requireNonNull(terms, "terms");
            if (terms.isEmpty()) {
                throw new IllegalArgumentException("A spell-value sum needs at least one term");
            }
            terms = List.copyOf(terms);
        }

        @Override
        public double resolve(TraitProfile traits) {
            Objects.requireNonNull(traits, "traits");
            return terms.stream().mapToDouble(term -> term.resolve(traits)).sum();
        }
    }

    /**
     * The product of one or more spell values.
     *
     * @param factors the values to multiply
     */
    record Product(List<SpellValue> factors) implements SpellValue {
        public Product {
            Objects.requireNonNull(factors, "factors");
            if (factors.isEmpty()) {
                throw new IllegalArgumentException("A spell-value product needs at least one factor");
            }
            factors = List.copyOf(factors);
        }

        @Override
        public double resolve(TraitProfile traits) {
            Objects.requireNonNull(traits, "traits");
            return factors.stream().mapToDouble(factor -> factor.resolve(traits)).reduce(1.0, (left, right) -> left * right);
        }
    }
}
