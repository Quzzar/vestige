package com.quzzar.vestige.travel;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class StandingStoneShapeTest {
    @Test void everySignatureBucketHasEqualOpportunityAcrossSixteenDistinctForms() {
        var counts = new EnumMap<StandingStoneShape, Integer>(StandingStoneShape.class);
        for (int suffix = 0; suffix < 256; suffix++) {
            var shape = StandingStoneShape.fromKey("a".repeat(62) + String.format(Locale.ROOT, "%02x", suffix));
            counts.merge(shape, 1, Integer::sum);
        }
        assertEquals(16, counts.size());
        assertTrue(counts.values().stream().allMatch(count -> count == 16));
        assertEquals(16, Arrays.stream(StandingStoneShape.values()).map(StandingStoneShape::getSerializedName).distinct().count());
    }

    @Test void originalReviewKeysKeepTheirApprovedFormsAndInvalidKeysUseTheDefault() {
        assertEquals(StandingStoneShape.BLADE, StandingStoneShape.fromKey("76f32f1c38d3f867b7314795817de918434f62f7742e565283a18dff843ed500"));
        assertEquals(StandingStoneShape.SHOULDER, StandingStoneShape.fromKey("d9943647cbf7789d06ff728ebe1ecd8973b9fef68ddb481a7f68a213c91dcfc4"));
        assertEquals(StandingStoneShape.LEANING, StandingStoneShape.fromKey("fd6beea2466e6b35d7d933b94217d9152e658d0b2a3fd7d409c3378c04c38811"));
        for (String invalid : List.of("", "0".repeat(63), "G".repeat(64)))
            assertEquals(StandingStoneShape.BLADE, StandingStoneShape.fromKey(invalid));
    }
}
