package com.quzzar.vestige.apparatus;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Data-driven native rituals with frozen material rationale in their authoring ledger. */
public final class RitualCatalog extends SimpleJsonResourceReloadListener {
    private Map<ResourceLocation, RitualRecipe> recipes = Map.of();
    public RitualCatalog() { super(new Gson(), "ritual_recipes"); }
    public Map<ResourceLocation, RitualRecipe> recipes() { return recipes; }
    @Override protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceLocation, RitualRecipe> parsed = new LinkedHashMap<>();
        resources.forEach((id, json) -> {
            try { parsed.put(id, RitualRecipe.read(id, json.getAsJsonObject())); }
            catch (RuntimeException e) { throw new JsonParseException("Invalid ritual recipe " + id, e); }
        });
        RitualCrafting.cancelAll(); recipes = Map.copyOf(parsed);
    }
    private static final class Builtin {
        static final JsonObject INDEX = read();
        private static JsonObject read() {
            try (var stream = RitualCatalog.class.getResourceAsStream("/data/vestige/ritual_catalog.json")) {
                if (stream == null) throw new IllegalStateException("Missing ritual item catalog");
                return new Gson().fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), JsonObject.class);
            } catch (java.io.IOException e) { throw new IllegalStateException("Cannot read ritual item catalog", e); }
        }
    }
    public static List<ResourceLocation> builtinIds() { return Builtin.INDEX.getAsJsonArray("spells").asList().stream().map(e -> ResourceLocation.parse(e.getAsString())).toList(); }
    public static List<ResourceLocation> builtinTraits() { return Builtin.INDEX.getAsJsonArray("traits").asList().stream().map(e -> ResourceLocation.parse(e.getAsString())).toList(); }
}
