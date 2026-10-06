package com.quzzar.vestige.apparatus;

import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import javax.imageio.ImageIO;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class FragmentSymbolsTest {
    @Test void everyBuiltInSymbolHasItsOwnPackagedVisibleGlyph() throws Exception {
        try (var mapping = getClass().getResourceAsStream("/assets/vestige/fragment_symbols.json");
             var font = getClass().getResourceAsStream("/assets/vestige/font/fragment_symbols.json");
             var texture = getClass().getResourceAsStream("/assets/vestige/textures/font/fragment_symbols.png")) {
            var symbols = JsonParser.parseReader(new InputStreamReader(mapping, StandardCharsets.UTF_8)).getAsJsonObject();
            var provider = JsonParser.parseReader(new InputStreamReader(font, StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonArray("providers").get(0).getAsJsonObject();
            var chars = provider.getAsJsonArray("chars");
            var atlas = ImageIO.read(texture);
            assertEquals(52, symbols.size());
            var glyphs = new HashSet<String>();
            var silhouettes = new HashSet<String>();
            for (var entry : symbols.entrySet()) {
                var label = FragmentSymbols.label(ResourceLocation.parse(entry.getKey()));
                assertEquals(entry.getValue().getAsString(), label.getString());
                assertEquals("vestige:fragment_symbols", label.getStyle().getFont().toString());
                assertEquals(0xd6b46a, label.getStyle().getColor().getValue());
                assertTrue(glyphs.add(label.getString()));
                StringBuilder mask = new StringBuilder();
                boolean found = false;
                for (int row = 0; row < chars.size(); row++) {
                    int column = chars.get(row).getAsString().indexOf(label.getString());
                    if (column < 0) continue;
                    assertFalse(found, "Duplicate font entry");
                    found = true;
                    for (int y = 0; y < 8; y++) for (int x = 0; x < 8; x++)
                        mask.append(atlas.getRGB(column * 8 + x, row * 8 + y) >>> 24 == 0 ? '.' : '#');
                }
                assertTrue(found, "Symbol missing from font atlas: " + entry.getKey());
                assertTrue(mask.indexOf("#") >= 0, "Blank glyph");
                assertTrue(silhouettes.add(mask.toString()), "Duplicate glyph shape");
            }
        }
    }

    @Test void unknownTraitsRetainNamesAndTheirNamespaceWithoutBorrowingBuiltInSymbols() {
        var future = FragmentSymbols.label(ResourceLocation.parse("vestige:future_trait"));
        var foreign = FragmentSymbols.label(ResourceLocation.parse("addon:fire"));
        assertEquals("future_trait", future.getString());
        assertEquals("addon:fire", foreign.getString());
        assertEquals("minecraft:default", future.getStyle().getFont().toString());
        assertEquals("minecraft:default", foreign.getStyle().getFont().toString());
        assertNull(future.getStyle().getColor());
        assertNull(foreign.getStyle().getColor());
        assertNotEquals(foreign.getString(), FragmentSymbols.label(ResourceLocation.parse("vestige:fire")).getString());
    }
}
