package com.quzzar.vestige.magic.definition;

import java.util.Arrays;
import java.util.Optional;

/**
 * A recognized way of accessing magic.
 */
public enum Tradition {
    ARCANE("arcane"),
    PRIMAL("primal"),
    DIVINE("divine"),
    OCCULT("occult");

    private final String id;

    Tradition(String id) {
        this.id = id;
    }

    /**
     * Returns the stable serialized identifier for this tradition.
     */
    public String id() {
        return id;
    }

    /**
     * Resolves a tradition by its serialized identifier.
     */
    public static Optional<Tradition> fromId(String id) {
        return Arrays.stream(values()).filter(tradition -> tradition.id.equals(id)).findFirst();
    }
}
