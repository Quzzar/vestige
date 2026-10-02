package com.quzzar.vestige.magic.definition;

import java.util.List;

/**
 * A typed magical outcome in a spell definition.
 * Concrete effects expose any conditions that guard their execution.
 */
public interface SpellEffect {
    /**
     * Returns the conditions that must match before this effect executes.
     */
    List<SpellCondition> conditions();
}
