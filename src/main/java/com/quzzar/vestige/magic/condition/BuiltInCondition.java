package com.quzzar.vestige.magic.condition;

import com.quzzar.vestige.magic.definition.SpellCondition;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The eight built-in forms in Vestige's reusable condition language.
 */
public sealed interface BuiltInCondition extends SpellCondition permits BuiltInCondition.All, BuiltInCondition.Any,
        BuiltInCondition.Not, BuiltInCondition.Compare, BuiltInCondition.Exists, BuiltInCondition.Tagged,
        BuiltInCondition.Matches, BuiltInCondition.Chance {
    /**
     * Requires every nested condition to match.
     *
     * @param conditions the conditions to evaluate
     */
    record All(List<SpellCondition> conditions) implements BuiltInCondition {
        public All {
            conditions = copyNonEmpty(conditions, "all");
        }

        @Override
        public ResourceLocation type() {
            return SpellConditionTypes.ALL;
        }

        @Override
        public boolean matches(ConditionContext context) {
            Objects.requireNonNull(context, "context");
            return conditions.stream().allMatch(condition -> condition.matches(context));
        }
    }

    /**
     * Requires at least one nested condition to match.
     *
     * @param conditions the conditions to evaluate
     */
    record Any(List<SpellCondition> conditions) implements BuiltInCondition {
        public Any {
            conditions = copyNonEmpty(conditions, "any");
        }

        @Override
        public ResourceLocation type() {
            return SpellConditionTypes.ANY;
        }

        @Override
        public boolean matches(ConditionContext context) {
            Objects.requireNonNull(context, "context");
            return conditions.stream().anyMatch(condition -> condition.matches(context));
        }
    }

    /**
     * Inverts one nested condition.
     *
     * @param condition the condition to invert
     */
    record Not(SpellCondition condition) implements BuiltInCondition {
        public Not {
            Objects.requireNonNull(condition, "condition");
        }

        @Override
        public ResourceLocation type() {
            return SpellConditionTypes.NOT;
        }

        @Override
        public boolean matches(ConditionContext context) {
            return !condition.matches(Objects.requireNonNull(context, "context"));
        }
    }

    /**
     * Compares a scalar context value with one or two expected values.
     *
     * @param path the context value path
     * @param operation the comparison operation
     * @param expected the expected value or inclusive lower bound
     * @param upperBound the inclusive upper bound used only by {@link Comparison#BETWEEN}
     */
    record Compare(
            ResourceLocation path,
            Comparison operation,
            ConditionValue expected,
            Optional<ConditionValue> upperBound
    ) implements BuiltInCondition {
        public Compare {
            Objects.requireNonNull(path, "path");
            Objects.requireNonNull(operation, "operation");
            Objects.requireNonNull(expected, "expected");
            Objects.requireNonNull(upperBound, "upperBound");
            if (operation == Comparison.BETWEEN && upperBound.isEmpty()) {
                throw new IllegalArgumentException("Between comparisons need an upper bound");
            }
            if (operation != Comparison.BETWEEN && upperBound.isPresent()) {
                throw new IllegalArgumentException("Only between comparisons accept an upper bound");
            }
        }

        /**
         * Creates a comparison with one expected value.
         */
        public static Compare to(ResourceLocation path, Comparison operation, ConditionValue expected) {
            return new Compare(path, operation, expected, Optional.empty());
        }

        /**
         * Creates an inclusive numerical range comparison.
         */
        public static Compare between(ResourceLocation path, double lowerBound, double upperBound) {
            if (lowerBound > upperBound) {
                throw new IllegalArgumentException("A lower bound cannot exceed its upper bound");
            }
            return new Compare(
                    path,
                    Comparison.BETWEEN,
                    new ConditionValue.Decimal(lowerBound),
                    Optional.of(new ConditionValue.Decimal(upperBound))
            );
        }

        @Override
        public ResourceLocation type() {
            return SpellConditionTypes.COMPARE;
        }

        @Override
        public boolean matches(ConditionContext context) {
            Optional<ConditionValue> actual = Objects.requireNonNull(context, "context").value(path);
            return actual.filter(value -> compare(value, operation, expected, upperBound)).isPresent();
        }
    }

    /**
     * Requires a context path to contain a value or subject.
     *
     * @param path the context path
     */
    record Exists(ResourceLocation path) implements BuiltInCondition {
        public Exists {
            Objects.requireNonNull(path, "path");
        }

        @Override
        public ResourceLocation type() {
            return SpellConditionTypes.EXISTS;
        }

        @Override
        public boolean matches(ConditionContext context) {
            return Objects.requireNonNull(context, "context").value(path).isPresent();
        }
    }

    /**
     * Requires a context value or subject to belong to a registry tag.
     *
     * @param path the context path
     * @param tag the registry tag identifier
     */
    record Tagged(ResourceLocation path, ResourceLocation tag) implements BuiltInCondition {
        public Tagged {
            Objects.requireNonNull(path, "path");
            Objects.requireNonNull(tag, "tag");
        }

        @Override
        public ResourceLocation type() {
            return SpellConditionTypes.TAGGED;
        }

        @Override
        public boolean matches(ConditionContext context) {
            return Objects.requireNonNull(context, "context").isTagged(path, tag);
        }
    }

    /**
     * Requires a context subject to satisfy a named structured predicate.
     *
     * @param path the subject path
     * @param predicate the predicate identifier
     */
    record Matches(ResourceLocation path, ResourceLocation predicate) implements BuiltInCondition {
        public Matches {
            Objects.requireNonNull(path, "path");
            Objects.requireNonNull(predicate, "predicate");
        }

        @Override
        public ResourceLocation type() {
            return SpellConditionTypes.MATCHES;
        }

        @Override
        public boolean matches(ConditionContext context) {
            return Objects.requireNonNull(context, "context").matches(path, predicate);
        }
    }

    /**
     * Passes according to a probability from zero to one.
     *
     * @param probability the inclusive probability range from zero to one
     */
    record Chance(double probability) implements BuiltInCondition {
        public Chance {
            if (!Double.isFinite(probability) || probability < 0 || probability > 1) {
                throw new IllegalArgumentException("Condition probabilities must be between zero and one");
            }
        }

        @Override
        public ResourceLocation type() {
            return SpellConditionTypes.CHANCE;
        }

        @Override
        public boolean matches(ConditionContext context) {
            Objects.requireNonNull(context, "context");
            return probability == 1 || probability > 0 && context.random() < probability;
        }
    }

    /**
     * Supported scalar comparison operations.
     */
    enum Comparison {
        EQUAL,
        NOT_EQUAL,
        LESS_THAN,
        LESS_THAN_OR_EQUAL,
        GREATER_THAN,
        GREATER_THAN_OR_EQUAL,
        BETWEEN
    }

    private static List<SpellCondition> copyNonEmpty(List<SpellCondition> conditions, String type) {
        Objects.requireNonNull(conditions, "conditions");
        if (conditions.isEmpty()) {
            throw new IllegalArgumentException("The " + type + " condition needs at least one nested condition");
        }
        conditions.forEach(condition -> Objects.requireNonNull(condition, "condition"));
        return List.copyOf(conditions);
    }

    private static boolean compare(
            ConditionValue actual,
            Comparison operation,
            ConditionValue expected,
            Optional<ConditionValue> upperBound
    ) {
        return switch (operation) {
            case EQUAL -> actual.equals(expected);
            case NOT_EQUAL -> !actual.equals(expected);
            case LESS_THAN -> compareNumbers(actual, expected, (left, right) -> left < right);
            case LESS_THAN_OR_EQUAL -> compareNumbers(actual, expected, (left, right) -> left <= right);
            case GREATER_THAN -> compareNumbers(actual, expected, (left, right) -> left > right);
            case GREATER_THAN_OR_EQUAL -> compareNumbers(actual, expected, (left, right) -> left >= right);
            case BETWEEN -> upperBound.filter(upper -> compareNumbers(actual, expected, (left, lower) -> left >= lower)
                    && compareNumbers(actual, upper, (left, right) -> left <= right)).isPresent();
        };
    }

    private static boolean compareNumbers(ConditionValue left, ConditionValue right, NumberComparison comparison) {
        if (left instanceof ConditionValue.Decimal leftNumber && right instanceof ConditionValue.Decimal rightNumber) {
            return comparison.test(leftNumber.value(), rightNumber.value());
        }
        return false;
    }

    @FunctionalInterface
    interface NumberComparison {
        boolean test(double left, double right);
    }
}
