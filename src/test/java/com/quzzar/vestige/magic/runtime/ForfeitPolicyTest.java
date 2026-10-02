package com.quzzar.vestige.magic.runtime;

import com.quzzar.vestige.magic.definition.SpecialTraits;
import com.quzzar.vestige.magic.definition.TraitProfile;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ForfeitPolicyTest {
    private static final ResourceLocation FIRE = ResourceLocation.fromNamespaceAndPath("vestige", "fire");

    @Test
    void preservesTheLegacyUnknownSpellBaseline() {
        assertEquals(0.20, ForfeitPolicy.DEFAULT.chance(TraitProfile.empty(), false));
    }

    @Test
    void discoveredNonvolatileSpellsDoNotForfeit() {
        TraitProfile traits = new TraitProfile(Map.of(FIRE, 100.0));

        assertEquals(0.0, ForfeitPolicy.DEFAULT.chance(traits, true));
    }

    @Test
    void volatilityAppliesEvenAfterDiscovery() {
        TraitProfile traits = new TraitProfile(Map.of(SpecialTraits.VOLATILE, 6.0));

        assertEquals(0.30, ForfeitPolicy.DEFAULT.chance(traits, true), 0.000001);
    }

    @Test
    void unknownRiskUsesTheHigherOfBaselineAndInherentVolatility() {
        TraitProfile mild = new TraitProfile(Map.of(SpecialTraits.VOLATILE, 2.0));
        TraitProfile severe = new TraitProfile(Map.of(SpecialTraits.VOLATILE, 8.0));

        assertEquals(0.20, ForfeitPolicy.DEFAULT.chance(mild, false), 0.000001);
        assertEquals(0.40, ForfeitPolicy.DEFAULT.chance(severe, false), 0.000001);
    }

    @Test
    void forfeitChanceCannotExceedCertainty() {
        TraitProfile traits = new TraitProfile(Map.of(SpecialTraits.VOLATILE, 100.0));

        assertEquals(1.0, ForfeitPolicy.DEFAULT.chance(traits, true));
    }
}
