package com.quzzar.vestige.apparatus;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MundaneStaffProfileTest {
    @Test void mundaneBodiesMakeSmallPhysicalTradeoffsAroundTheWoodenPickaxeReference() {
        var wooden = MundaneStaffs.Shaft.STICK;
        assertEquals(2.0D, wooden.attackDamage());
        assertEquals(1.2D, wooden.attackSpeed());
        assertEquals(.40F, wooden.guardRatio());

        for (var shaft : MundaneStaffs.Shaft.values()) {
            assertTrue(shaft.attackDamage() >= 1.0D && shaft.attackDamage() <= 3.0D, shaft.id());
            assertTrue(shaft.attackSpeed() >= .9D && shaft.attackSpeed() <= 1.7D, shaft.id());
            assertTrue(shaft.guardRatio() >= .30F && shaft.guardRatio() <= .45F, shaft.id());
        }
        assertTrue(MundaneStaffs.Shaft.BAMBOO.attackSpeed() > wooden.attackSpeed());
        assertTrue(MundaneStaffs.Shaft.LIGHTNING_ROD.attackDamage() > wooden.attackDamage());
        assertTrue(MundaneStaffs.Shaft.END_ROD.guardRatio() > wooden.guardRatio());
    }
}
