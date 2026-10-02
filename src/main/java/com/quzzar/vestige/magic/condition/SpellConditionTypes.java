package com.quzzar.vestige.magic.condition;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.Set;

/**
 * The built-in condition identifiers supported by the spell condition language.
 */
public final class SpellConditionTypes {
    public static final ResourceLocation ALL = id("all");
    public static final ResourceLocation ANY = id("any");
    public static final ResourceLocation NOT = id("not");
    public static final ResourceLocation COMPARE = id("compare");
    public static final ResourceLocation EXISTS = id("exists");
    public static final ResourceLocation TAGGED = id("tagged");
    public static final ResourceLocation MATCHES = id("matches");
    public static final ResourceLocation CHANCE = id("chance");

    private static final Set<ResourceLocation> BUILT_INS = Set.of(
            ALL,
            ANY,
            NOT,
            COMPARE,
            EXISTS,
            TAGGED,
            MATCHES,
            CHANCE
    );

    private SpellConditionTypes() {
    }

    /**
     * Returns every built-in condition identifier.
     */
    public static Set<ResourceLocation> all() {
        return BUILT_INS;
    }

    /**
     * Returns whether the identifier belongs to the built-in catalog.
     *
     * @param condition the condition identifier to inspect
     */
    public static boolean isBuiltIn(ResourceLocation condition) {
        return BUILT_INS.contains(Objects.requireNonNull(condition, "condition"));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("vestige", path);
    }
}
