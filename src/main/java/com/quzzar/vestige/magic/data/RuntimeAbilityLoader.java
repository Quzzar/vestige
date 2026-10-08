package com.quzzar.vestige.magic.data;

import com.quzzar.vestige.magic.definition.ItemAbilityDefinition;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

/** Server-authored abilities are separate from the discoverable spell catalog. */
public final class RuntimeAbilityLoader extends RuntimeMagicLoader<ItemAbilityDefinition> {
    public RuntimeAbilityLoader() { super("item_abilities", SpellJson::readAbility); }
    public Map<ResourceLocation, ItemAbilityDefinition> abilities() { return definitions(); }
}
