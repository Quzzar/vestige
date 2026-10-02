package com.quzzar.vestige.magic.data;

import com.google.gson.*;
import com.quzzar.vestige.magic.definition.SpellDefinition;
import com.quzzar.vestige.magic.world.NativeMagic;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import java.util.LinkedHashMap;
import java.util.Map;

/** Atomically reloads the standalone native spell catalog. */
public final class RuntimeSpellLoader extends SimpleJsonResourceReloadListener {
    private Map<ResourceLocation, SpellDefinition> spells = Map.of();
    public RuntimeSpellLoader() { super(new Gson(), "runtime_spells"); }
    public Map<ResourceLocation, SpellDefinition> spells() { return spells; }
    @Override protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceLocation, SpellDefinition> parsed = new LinkedHashMap<>();
        resources.forEach((id, json) -> {
            try { parsed.put(id, SpellJson.read(id, json.getAsJsonObject())); }
            catch (RuntimeException exception) { throw new JsonParseException("Invalid native spell " + id + ": " + exception.getMessage(), exception); }
        });
        spells = Map.copyOf(parsed);
        NativeMagic.reload();
    }
}
