package com.quzzar.vestige.magic.definition;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.Set;

/**
 * Trait identifiers whose names have behavior built into the spell engine.
 *
 * <p>{@link #VOLATILE} is intentionally the only special trait. Every other trait is inert data until an effect,
 * condition, item, or adapter explicitly reads it. Adding another identifier to this class is a design decision that
 * requires an explicit conversation with the project owner, not an ordinary implementation shortcut.</p>
 */
public final class SpecialTraits {
    public static final ResourceLocation VOLATILE = id("volatile");

    private static final Set<ResourceLocation> ALL = Set.of(VOLATILE);

    private SpecialTraits() {
    }

    /**
     * Returns whether the trait has behavior built directly into the spell engine.
     */
    public static boolean isSpecial(ResourceLocation trait) {
        return ALL.contains(Objects.requireNonNull(trait, "trait"));
    }

    /**
     * Returns the complete set of traits with engine-level semantics.
     *
     * <p>This set must contain only {@link #VOLATILE} unless the project owner explicitly approves another exception.</p>
     */
    public static Set<ResourceLocation> all() {
        return ALL;
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("vestige", path);
    }
}
