package com.quzzar.vestige.magic.presentation;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ManaReadinessTest {
    @Test void eachItemUsesItsNextPaymentInsteadOfTotalCapacityOrCastCount() {
        assertEquals(0, ManaReadiness.shortage(15, 10));
        assertEquals(.5f, ManaReadiness.shortage(15, 30));
        assertEquals(.75f, ManaReadiness.shortage(15, 60));
        for (int cast = 0; cast < 10; cast++) assertEquals(0, ManaReadiness.shortage(100 - cast * 10, 10));
        assertEquals(1, ManaReadiness.shortage(0, 10));
        assertEquals(0, ManaReadiness.shortage(0, 0));
        assertEquals(0, ManaReadiness.shortage(100, 10));
    }
    @Test void fractionalRecoveryClearsOnlyAtTheExactAffordableBoundary() {
        assertTrue(ManaReadiness.shortage(Math.nextDown(30d), 30) > 0);
        assertEquals(0, ManaReadiness.shortage(30, 30));
        assertEquals(.125f, ManaReadiness.shortage(26.25, 30));
        assertThrows(IllegalArgumentException.class, () -> ManaReadiness.shortage(Double.NaN, 30));
        assertThrows(IllegalArgumentException.class, () -> ManaReadiness.shortage(10, Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> ManaReadiness.shortage(-1, 30));
    }
}
