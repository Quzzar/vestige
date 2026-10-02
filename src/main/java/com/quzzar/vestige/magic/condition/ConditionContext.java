package com.quzzar.vestige.magic.condition;

import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

/**
 * Supplies named event and cast facts to the condition language.
 * Runtime adapters retain ownership of Minecraft-specific objects and predicate evaluation.
 */
public interface ConditionContext {
    /**
     * Resolves a scalar condition value.
     *
     * @param path the namespaced value path
     */
    Optional<ConditionValue> value(ResourceLocation path);

    /**
     * Tests whether the object referenced by a path belongs to a registry tag.
     *
     * @param path the namespaced value or subject path
     * @param tag the tag identifier
     */
    boolean isTagged(ResourceLocation path, ResourceLocation tag);

    /**
     * Tests an object referenced by a path against a named structured predicate.
     *
     * @param path the namespaced value or subject path
     * @param predicate the predicate identifier
     */
    boolean matches(ResourceLocation path, ResourceLocation predicate);

    /**
     * Returns a random sample in the range from zero inclusive to one exclusive.
     */
    double random();
}
