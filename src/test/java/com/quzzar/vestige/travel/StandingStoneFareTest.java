package com.quzzar.vestige.travel;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StandingStoneFareTest {
    @Test void acceptedDistanceExamplesUseWholeXpPoints() {
        int[][] examples = {{0,12},{64,15},{256,18},{1024,24},{4096,36},{16384,60}};
        for (var example : examples) assertEquals(example[1], StandingStoneFare.xp(BlockPos.ZERO, new BlockPos(example[0],0,0)));
    }
    @Test void heightAndDiagonalDistanceMatterAndTranslationDoesNot() {
        assertEquals(24, StandingStoneFare.xp(BlockPos.ZERO, new BlockPos(0,1024,0)));
        assertEquals(17, StandingStoneFare.xp(BlockPos.ZERO, new BlockPos(128,128,64)));
        assertEquals(17, StandingStoneFare.xp(new BlockPos(-1000,30,900),new BlockPos(-872,158,964)));
        assertEquals(17, StandingStoneFare.xp(new BlockPos(128,128,64),BlockPos.ZERO));
        assertTrue(StandingStoneFare.xp(new BlockPos(Integer.MIN_VALUE,0,0),new BlockPos(Integer.MAX_VALUE,0,0))>0);
    }
}
