package com.quzzar.vestige.magic.definition;

import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.Set;

/**
 * The built-in trigger identifiers understood by Vestige's spell adapters.
 * Other namespaces can still provide additional trigger identifiers.
 */
public final class SpellTriggerTypes {
    public static final ResourceLocation INTERACT = id("interact");
    public static final ResourceLocation ATTACK = id("attack");
    public static final ResourceLocation JUMP = id("jump");
    public static final ResourceLocation CROUCH = id("crouch");
    public static final ResourceLocation LAND = id("land");
    public static final ResourceLocation EQUIP = id("equip");
    public static final ResourceLocation UNEQUIP = id("unequip");
    public static final ResourceLocation ITEM_USE_FINISHED = id("item_use_finished");

    public static final ResourceLocation BREAK_BLOCK = id("break_block");
    public static final ResourceLocation PLACE_BLOCK = id("place_block");
    public static final ResourceLocation PICKUP_ITEM = id("pickup_item");
    public static final ResourceLocation DROP_ITEM = id("drop_item");
    public static final ResourceLocation CRAFT_ITEM = id("craft_item");

    public static final ResourceLocation DAMAGE_DEALT = id("damage_dealt");
    public static final ResourceLocation DAMAGE_TAKEN = id("damage_taken");
    public static final ResourceLocation DAMAGE_CALCULATING = id("damage_calculating");
    public static final ResourceLocation ATTACK_TARGETED = id("attack_targeted");
    public static final ResourceLocation SAVE_REQUESTED = id("save_requested");
    public static final ResourceLocation HEAL_DEALT = id("heal_dealt");
    public static final ResourceLocation HEAL_RECEIVED = id("heal_received");
    public static final ResourceLocation HEAL_CALCULATING = id("heal_calculating");
    public static final ResourceLocation KILL = id("kill");
    public static final ResourceLocation DIE = id("die");
    public static final ResourceLocation PROJECTILE_HIT = id("projectile_hit");
    public static final ResourceLocation EFFECT_APPLIED = id("effect_applied");
    public static final ResourceLocation EFFECT_REMOVED = id("effect_removed");

    public static final ResourceLocation SUMMON = id("summon");
    public static final ResourceLocation CAST_STARTED = id("cast_started");
    public static final ResourceLocation CAST_COMPLETED = id("cast_completed");
    public static final ResourceLocation CAST_FAILED = id("cast_failed");
    public static final ResourceLocation CAST_INTERRUPTED = id("cast_interrupted");
    public static final ResourceLocation MANA_CHANGED = id("mana_changed");
    public static final ResourceLocation SURGE = id("surge");

    public static final ResourceLocation ENTER_AREA = id("enter_area");
    public static final ResourceLocation LEAVE_AREA = id("leave_area");
    public static final ResourceLocation TICK = id("tick");

    private static final Set<ResourceLocation> ALL = Set.of(
            INTERACT,
            ATTACK,
            JUMP,
            CROUCH,
            LAND,
            EQUIP,
            UNEQUIP,
            ITEM_USE_FINISHED,
            BREAK_BLOCK,
            PLACE_BLOCK,
            PICKUP_ITEM,
            DROP_ITEM,
            CRAFT_ITEM,
            DAMAGE_DEALT,
            DAMAGE_TAKEN,
            DAMAGE_CALCULATING,
            ATTACK_TARGETED,
            SAVE_REQUESTED,
            HEAL_DEALT,
            HEAL_RECEIVED,
            HEAL_CALCULATING,
            KILL,
            DIE,
            PROJECTILE_HIT,
            EFFECT_APPLIED,
            EFFECT_REMOVED,
            SUMMON,
            CAST_STARTED,
            CAST_COMPLETED,
            CAST_FAILED,
            CAST_INTERRUPTED,
            MANA_CHANGED,
            SURGE,
            ENTER_AREA,
            LEAVE_AREA,
            TICK
    );

    private SpellTriggerTypes() {
    }

    /**
     * Returns every built-in trigger identifier.
     */
    public static Set<ResourceLocation> all() {
        return ALL;
    }

    /**
     * Returns whether the identifier belongs to the built-in catalog.
     *
     * @param trigger the trigger identifier to inspect
     */
    public static boolean isBuiltIn(ResourceLocation trigger) {
        return ALL.contains(Objects.requireNonNull(trigger, "trigger"));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("vestige", path);
    }
}
