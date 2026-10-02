package com.quzzar.vestige.magic.definition;

import java.util.Arrays;
import java.util.Optional;

/** A spell's authored rarity; classification does not implicitly multiply its effects. */
public enum SpellRarity {
    COMMON("common"),
    UNCOMMON("uncommon"),
    RARE("rare"),
    MYTHIC("mythic");

    private final String id;

    SpellRarity(String id) {
        this.id = id;
    }

    /** Returns the stable serialized rarity name. */
    public String id() {
        return id;
    }

    /** Resolves one of the four supported serialized names. */
    public static Optional<SpellRarity> fromId(String id) {
        return Arrays.stream(values()).filter(rarity -> rarity.id.equals(id)).findFirst();
    }
}
