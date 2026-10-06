package com.quzzar.vestige.apparatus;

import com.google.gson.JsonParser;
import com.quzzar.vestige.VestigeMainMod;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Built-in visual vocabulary only; fragment identity and discovery use the full trait ID. */
public final class FragmentSymbols {
    private static final ResourceLocation FONT = VestigeMainMod.location("fragment_symbols");
    private static final Map<ResourceLocation, String> SYMBOLS = load();
    private FragmentSymbols() { }

    public static Component label(ResourceLocation trait) {
        String glyph = SYMBOLS.get(trait);
        if (glyph == null) return Component.literal(trait.getNamespace().equals(VestigeMainMod.MOD_ID) ? trait.getPath() : trait.toString());
        return Component.literal(glyph).setStyle(Style.EMPTY.withFont(FONT).withColor(0xd6b46a));
    }

    private static Map<ResourceLocation, String> load() {
        try (var input = FragmentSymbols.class.getResourceAsStream("/assets/vestige/fragment_symbols.json")) {
            if (input == null) throw new IllegalStateException("Missing fragment symbols");
            var json = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
            var result = new LinkedHashMap<ResourceLocation, String>();
            for (var entry : json.entrySet()) result.put(ResourceLocation.parse(entry.getKey()), entry.getValue().getAsString());
            return Collections.unmodifiableMap(result);
        } catch (IOException exception) { throw new UncheckedIOException(exception); }
    }
}
