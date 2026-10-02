package com.quzzar.vestige.magic.definition;

import com.quzzar.vestige.magic.expression.SpellValue;
import java.util.Objects;

/** Selects subjects independently of the outcome applied to them. */
public record TargetSpec(Selection selection, SpellValue distance, boolean required,
                         java.util.Map<String, SpellValue> options, Relationship relationship) {
    public TargetSpec(Selection selection, SpellValue distance) { this(selection, distance, true); }
    public TargetSpec(Selection selection, SpellValue distance, boolean required) {
        this(selection, distance, required, java.util.Map.of(), Relationship.ANY);
    }

    public TargetSpec {
        Objects.requireNonNull(selection, "selection");
        Objects.requireNonNull(distance, "distance");
        options = java.util.Map.copyOf(options);
        Objects.requireNonNull(relationship);
    }

    public enum Selection { SELF, CURRENT, EVENT_TARGET, EVENT_ATTACKER, ENTITY_RAY, BLOCK_RAY, NEARBY_ENTITIES,
        AIMED_POSITION, NEAR_TARGET, BEAM, CONE, CHAIN, MELEE, ANY_ENTITY_RAY, STORED_TARGET }
    public enum Relationship { ANY, ALLY, HOSTILE, OWNED }

    public static TargetSpec self() {
        return new TargetSpec(Selection.SELF, new SpellValue.Constant(0));
    }
}
