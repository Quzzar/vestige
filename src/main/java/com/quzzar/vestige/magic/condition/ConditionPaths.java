package com.quzzar.vestige.magic.condition;

import net.minecraft.resources.ResourceLocation;

/**
 * Built-in paths exposed to conditions by spell event adapters.
 * Additional namespaces can expose their own paths without changing the condition language.
 */
public final class ConditionPaths {
    public static final ResourceLocation ACTOR = id("actor");
    public static final ResourceLocation ACTOR_ENTITY_TYPE = id("actor/entity_type");
    public static final ResourceLocation ACTOR_HEALTH = id("actor/health");
    public static final ResourceLocation ACTOR_MAX_HEALTH = id("actor/max_health");
    public static final ResourceLocation ACTOR_HEALTH_PERCENT = id("actor/health_percent");
    public static final ResourceLocation ACTOR_HUNGER = id("actor/hunger");
    public static final ResourceLocation ACTOR_SATURATION = id("actor/saturation");
    public static final ResourceLocation ACTOR_MANA = id("actor/mana");
    public static final ResourceLocation ACTOR_MAX_MANA = id("actor/max_mana");
    public static final ResourceLocation ACTOR_MANA_PERCENT = id("actor/mana_percent");
    public static final ResourceLocation ACTOR_BURNING = id("actor/burning");
    public static final ResourceLocation ACTOR_WET = id("actor/wet");
    public static final ResourceLocation ACTOR_CROUCHING = id("actor/crouching");
    public static final ResourceLocation ACTOR_AIRBORNE = id("actor/airborne");
    public static final ResourceLocation ACTOR_INVISIBLE = id("actor/invisible");
    public static final ResourceLocation ACTOR_ALIVE = id("actor/alive");

    public static final ResourceLocation SOURCE = id("source");
    public static final ResourceLocation SOURCE_ITEM = id("source/item");
    public static final ResourceLocation SOURCE_CUSTOM_NAME = id("source/custom_name");
    public static final ResourceLocation SOURCE_COUNT = id("source/count");
    public static final ResourceLocation SOURCE_DURABILITY = id("source/durability");
    public static final ResourceLocation SOURCE_MAX_DURABILITY = id("source/max_durability");

    public static final ResourceLocation TARGET = id("target");
    public static final ResourceLocation TARGET_ENTITY_TYPE = id("target/entity_type");
    public static final ResourceLocation TARGET_HEALTH = id("target/health");
    public static final ResourceLocation TARGET_MAX_HEALTH = id("target/max_health");
    public static final ResourceLocation TARGET_HEALTH_PERCENT = id("target/health_percent");
    public static final ResourceLocation TARGET_BURNING = id("target/burning");
    public static final ResourceLocation TARGET_WET = id("target/wet");
    public static final ResourceLocation TARGET_ALIVE = id("target/alive");

    public static final ResourceLocation CREATED = id("created");
    public static final ResourceLocation CREATED_ENTITY_TYPE = id("created/entity_type");

    public static final ResourceLocation BLOCK = id("block");
    public static final ResourceLocation BLOCK_ID = id("block/id");
    public static final ResourceLocation BLOCK_ENTITY_TYPE = id("block/entity_type");

    public static final ResourceLocation WORLD = id("world");
    public static final ResourceLocation DIMENSION = id("world/dimension");
    public static final ResourceLocation BIOME = id("world/biome");
    public static final ResourceLocation WEATHER = id("world/weather");
    public static final ResourceLocation GAME_TIME = id("world/game_time");
    public static final ResourceLocation LIGHT_LEVEL = id("world/light_level");
    public static final ResourceLocation MOON_PHASE = id("world/moon_phase");
    public static final ResourceLocation HEIGHT = id("world/height");
    public static final ResourceLocation SKY_VISIBLE = id("world/sky_visible");

    public static final ResourceLocation EVENT = id("event");
    public static final ResourceLocation EVENT_PROVIDER = id("event/provider");
    public static final ResourceLocation EVENT_ID = id("event/id");
    public static final ResourceLocation INTERACTION_HAND = id("event/interaction_hand");
    public static final ResourceLocation INTERACTION_KIND = id("event/interaction_kind");
    public static final ResourceLocation CROUCH_STATE = id("event/crouch_state");
    public static final ResourceLocation DAMAGE_TYPE = id("event/damage_type");
    public static final ResourceLocation DAMAGE_AMOUNT = id("event/damage_amount");
    public static final ResourceLocation DAMAGE_CRITICAL = id("event/damage_critical");
    public static final ResourceLocation DAMAGE_BLOCKED = id("event/damage_blocked");
    public static final ResourceLocation HEALING_AMOUNT = id("event/healing_amount");
    public static final ResourceLocation PROJECTILE_TYPE = id("event/projectile_type");
    public static final ResourceLocation MANA_DELTA = id("event/mana_delta");

    public static final ResourceLocation SPELL = id("spell");
    public static final ResourceLocation SPELL_PROVIDER = id("spell/provider");
    public static final ResourceLocation SPELL_ID = id("spell/id");
    public static final ResourceLocation SPELL_RARITY = id("spell/rarity");
    public static final ResourceLocation SPELL_SCHOOL = id("spell/school");
    public static final ResourceLocation SPELL_LEVEL = id("spell/level");
    public static final ResourceLocation SPELL_CAST_TYPE = id("spell/cast_type");
    public static final ResourceLocation SPELL_CAST_SOURCE = id("spell/cast_source");
    public static final ResourceLocation ACTIVE_TRADITION = id("spell/active_tradition");

    public static final ResourceLocation CAUSE = id("cause");
    public static final ResourceLocation CAUSING_SPELL = id("cause/spell");
    public static final ResourceLocation CAUSING_TRIGGER = id("cause/trigger");
    public static final ResourceLocation ACTIVATION_COUNT = id("cause/activation_count");

    private ConditionPaths() {
    }

    /**
     * Returns the condition path for a spell trait without imposing a trait catalog.
     *
     * @param trait the flat trait identifier
     */
    public static ResourceLocation trait(ResourceLocation trait) {
        return id("spell/trait/" + trait.getNamespace() + "/" + trait.getPath());
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("vestige", path);
    }
}
