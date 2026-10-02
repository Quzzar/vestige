package com.quzzar.vestige.magic.definition;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpecialTraitsTest {
    @Test
    void volatileIsTheOnlyTraitWithEngineSemantics() {
        assertEquals(Set.of(SpecialTraits.VOLATILE), SpecialTraits.all());
        assertTrue(SpecialTraits.isSpecial(SpecialTraits.VOLATILE));
        assertFalse(SpecialTraits.isSpecial(ResourceLocation.fromNamespaceAndPath("vestige", "fire")));
    }
}
