package com.quzzar.vestige.magic.definition;

import java.util.Set;

/** Executable primitives shared by authored native spells. */
public final class SpellActionTypes {
    public static final Set<String> ACTIONS = Set.of("damage", "heal", "reduce_pending_damage", "defer_pending_damage",
            "equip", "unlock", "status", "remove_status", "cleanse", "ignite", "freeze", "knockback", "pull", "launch",
            "dash", "teleport", "recall", "explode", "dispel", "interrupt", "break_block", "replace_block",
            "aggro_clear", "aggro_decoy", "aggro_convert", "ender_inventory", "pocket_dimension", "grip", "leech",
            "weapon_damage", "grant_max_health", "food_mana", "redirect_projectiles", "fangs", "attribute", "remove_attribute",
            "despawn", "dismiss_manifestations", "break_blocks", "flight", "end_flight", "reduce_pending_heal", "control",
            "optional_backstep", "random_teleport", "dwell_heal", "detect_magic", "inspect_item", "reveal_hidden", "reflect_projectiles", "transpose", "create_water", "shape_stone", "gather_items", "utterance");
    public static final Set<String> MANIFESTATIONS = Set.of("projectile", "summon", "block_lock", "barrier", "area", "wall", "portal", "decoy", "status", "tether",
            "construct", "block_wall", "zone", "mobility", "guard", "sensor", "pet_cache", "passage");
    private SpellActionTypes() { }
}
