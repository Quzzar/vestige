package com.quzzar.vestige.magic.definition;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpellTriggerTypesTest {
    @Test
    void containsTheAcceptedBuiltInTriggerCatalog() {
        assertEquals(37, SpellTriggerTypes.all().size());
        assertTrue(SpellTriggerTypes.isBuiltIn(SpellTriggerTypes.INTERACT));
        assertTrue(SpellTriggerTypes.isBuiltIn(SpellTriggerTypes.SUMMON));
        assertTrue(SpellTriggerTypes.isBuiltIn(SpellTriggerTypes.TICK));
    }
}
