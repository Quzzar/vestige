package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.definition.SpellRarity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import java.util.Map;

/** Exactly two knowledge states; only successful native scroll casts identify a spell. */
@EventBusSubscriber(modid = VestigeMainMod.MOD_ID)
public final class SpellKnowledge {
    private static final String KEY = "vestige:identified_spells";
    private static final String CRAFTED = "vestige:crafted_spells";
    private static volatile Map<ResourceLocation,SpellRarity> visibleSpells = Map.of();
    /** Item names have no player argument; the client receives only its own identification snapshot. */
    public static boolean visible(ResourceLocation spell) {
        return net.neoforged.fml.util.thread.EffectiveSide.get().isClient() && visibleSpells.containsKey(spell);
    }
    public static void updateVisible(Map<ResourceLocation,SpellRarity> spells) { visibleSpells = Map.copyOf(spells); }
    /** Rarity is revealed only by the viewing player's identified server snapshot. */
    public static SpellRarity visibleRarity(ResourceLocation spell) {
        return visible(spell) ? visibleSpells.getOrDefault(spell,SpellRarity.COMMON) : SpellRarity.COMMON;
    }
    public static boolean crafted(Player player, ResourceLocation spell) {
        return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound(CRAFTED).getBoolean(spell.toString());
    }
    /** Successful ingredient commitment teaches the recipe without identifying its spell. */
    public static void recordCraft(Player player, ResourceLocation spell) { remember(player, spell, CRAFTED); }
    private SpellKnowledge() { }
    public static boolean identified(Player player, ResourceLocation spell) {
        return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound(KEY).getBoolean(spell.toString());
    }
    public static void identify(Player player, ResourceLocation spell) {
        remember(player, spell, KEY);
    }
    private static void remember(Player player, ResourceLocation spell, String key) {
        CompoundTag retained = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        CompoundTag known = retained.getCompound(key);
        if (known.getBoolean(spell.toString())) return;
        known.putBoolean(spell.toString(), true);
        retained.put(key, known); player.getPersistentData().put(Player.PERSISTED_NBT_TAG, retained);
        com.quzzar.vestige.apparatus.recipeviewer.RitualDisplayPayload.Sync.send(player, true);
    }
    @SubscribeEvent public static void clonePlayer(PlayerEvent.Clone event) {
        CompoundTag source = event.getOriginal().getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        CompoundTag retained = event.getEntity().getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        for (String key : java.util.List.of(KEY, CRAFTED))
            if (source.contains(key)) retained.put(key, source.getCompound(key).copy());
        event.getEntity().getPersistentData().put(Player.PERSISTED_NBT_TAG, retained);
    }
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent event) {
        com.quzzar.vestige.apparatus.recipeviewer.RitualDisplayPayload.Sync.send(event.getEntity(), true);
    }
    @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        com.quzzar.vestige.apparatus.recipeviewer.RitualDisplayPayload.Sync.send(event.getEntity(), true);
    }
}
