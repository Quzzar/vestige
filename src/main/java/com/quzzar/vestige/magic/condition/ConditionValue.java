package com.quzzar.vestige.magic.condition;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

/**
 * A scalar value that can be read or compared by spell conditions.
 */
public sealed interface ConditionValue permits ConditionValue.Decimal, ConditionValue.Text, ConditionValue.Flag, ConditionValue.Identifier {
    /**
     * A finite numerical value.
     *
     * @param value the numerical value
     */
    record Decimal(double value) implements ConditionValue {
        public Decimal {
            if (!Double.isFinite(value)) {
                throw new IllegalArgumentException("Condition numbers must be finite");
            }
        }
    }

    /**
     * A textual value, including a custom item name.
     *
     * @param value the text
     */
    record Text(String value) implements ConditionValue {
        public Text {
            Objects.requireNonNull(value, "value");
        }
    }

    /**
     * A boolean state.
     *
     * @param value the state
     */
    record Flag(boolean value) implements ConditionValue {
    }

    /**
     * A namespaced registry or domain identifier.
     *
     * @param value the identifier
     */
    record Identifier(ResourceLocation value) implements ConditionValue {
        public Identifier {
            Objects.requireNonNull(value, "value");
        }
    }
}
