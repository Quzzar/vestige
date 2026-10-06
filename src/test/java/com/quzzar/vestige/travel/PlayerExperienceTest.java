package com.quzzar.vestige.travel;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlayerExperienceTest {
    @Test void cumulativePointBoundariesMatchVanillaLevelCosts() {
        for (int level = 0; level < 100; level++) {
            int next = level < 15 ? 7 + level * 2 : level < 30 ? 37 + (level - 15) * 5 : 112 + (level - 30) * 9;
            assertEquals(next, PlayerExperience.atLevel(level + 1) - PlayerExperience.atLevel(level));
        }
        assertEquals(315, PlayerExperience.atLevel(15));
        assertEquals(1395, PlayerExperience.atLevel(30));
    }
}
