package com.quzzar.vestige.magic.data;

import com.google.gson.*;
import com.quzzar.vestige.magic.definition.MagicDefinition;
import com.quzzar.vestige.magic.world.NativeMagic;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;

/** Shared atomic catalog boundary for immutable spells and trusted item abilities. */
public abstract class RuntimeMagicLoader<T extends MagicDefinition> extends SimpleJsonResourceReloadListener {
    private Map<ResourceLocation, T> definitions = Map.of();
    private final BiFunction<ResourceLocation, JsonObject, T> reader;

    protected RuntimeMagicLoader(String directory, BiFunction<ResourceLocation, JsonObject, T> reader) {
        super(new Gson(), directory);
        this.reader = reader;
    }
    protected final Map<ResourceLocation, T> definitions() { return definitions; }

    @Override protected final void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceLocation, T> parsed = new LinkedHashMap<>();
        resources.forEach((id, json) -> {
            try { parsed.put(id, reader.apply(id, json.getAsJsonObject())); }
            catch (RuntimeException exception) { throw new JsonParseException("Invalid native magic " + id + ": " + exception.getMessage(), exception); }
        });
        definitions = Map.copyOf(parsed);
        NativeMagic.reload();
    }
}
