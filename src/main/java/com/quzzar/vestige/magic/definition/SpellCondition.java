package com.quzzar.vestige.magic.definition;

import com.quzzar.vestige.magic.condition.ConditionContext;
import net.minecraft.resources.ResourceLocation;

/**
 * A typed predicate used by triggers and effects.
 * Concrete conditions own their data and evaluate through the shared condition context.
 */
public interface SpellCondition {
    /**
     * Returns the registered condition type identifier.
     */
    ResourceLocation type();

    /**
     * Evaluates this condition.
     *
     * @param context the available event and cast facts
     */
    boolean matches(ConditionContext context);
}
