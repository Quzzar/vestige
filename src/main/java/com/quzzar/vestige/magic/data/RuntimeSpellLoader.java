package com.quzzar.vestige.magic.data;

import com.quzzar.vestige.magic.definition.SpellDefinition;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

/** The native spell catalog retains its discovery and provenance identities. */
public final class RuntimeSpellLoader extends RuntimeMagicLoader<SpellDefinition> {
    public RuntimeSpellLoader() { super("runtime_spells", SpellJson::read); }
    public Map<ResourceLocation, SpellDefinition> spells() { return definitions(); }
}
