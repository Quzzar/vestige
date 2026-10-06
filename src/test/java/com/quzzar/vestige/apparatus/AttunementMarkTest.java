package com.quzzar.vestige.apparatus;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AttunementMarkTest {
    @Test void sameFullKeyHasStableReadableGlyphAndColorSequence() {
        var key="00112233445566778899aabbccddeeff".repeat(2);
        var first=AttunementMark.fromKey(key);
        assertEquals(first,AttunementMark.fromKey(key));
        assertEquals(4,first.runes().size());
        for(var rune:first.runes()) {
            assertTrue(rune.letter()>='a' && rune.letter()<='z');
            assertNotNull(rune.component().getStyle().getColor());
            assertEquals("minecraft:alt",rune.component().getStyle().getFont().toString());
        }
        assertNotEquals(first,AttunementMark.fromKey("ff".repeat(32)));
        assertThrows(UnsupportedOperationException.class,()->first.runes().clear());
    }
    @Test void malformedKeysCannotProduceMisleadingMarks() {
        for(var key:new String[]{"", "f".repeat(63), "g".repeat(64), "0".repeat(65)})
            assertThrows(IllegalArgumentException.class,()->AttunementMark.fromKey(key));
        assertThrows(IllegalArgumentException.class,()->AttunementMark.fromKey(null));
    }
}
