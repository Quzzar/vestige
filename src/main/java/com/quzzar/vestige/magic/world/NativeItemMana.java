package com.quzzar.vestige.magic.world;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.ManaItemCosts;
import com.quzzar.vestige.magic.definition.SpellDefinition;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import java.util.*;

/** Server-approved final mana costs, refreshed for inventory/menu changes and catalog reloads. */
@EventBusSubscriber(modid = VestigeMainMod.MOD_ID)
public final class NativeItemMana {
    private record Cached(Object definition, OptionalDouble cost) { }
    private static final Map<Player, Map<UUID, Cached>> CACHE = new IdentityHashMap<>();
    private static final Map<Player, Map<UUID, Double>> SENT = new IdentityHashMap<>();
    private NativeItemMana() { }
    public static Map<UUID, Double> snapshot(Player player) {
        var sources = new LinkedHashMap<UUID, ManaItemCosts.Source>();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) include(sources, player.getInventory().getItem(i));
        include(sources, player.containerMenu.getCarried());
        for (var slot : player.containerMenu.slots) include(sources, slot.getItem());
        var previous = CACHE.getOrDefault(player, Map.of());
        var cached = new HashMap<UUID, Cached>(); var costs = new HashMap<UUID, Double>();
        sources.forEach((key, source) -> {
            var definition = source instanceof ManaItemCosts.SpellSource spell ? NativeMagic.spells().spells().get(spell.scroll().spell()) : null;
            Object identity = source instanceof ManaItemCosts.AbilitySource ? NativeMagic.abilities().abilities().get(com.quzzar.vestige.equipment.WayfarerImbuements.ABILITY) : definition;
            var value = previous.get(key);
            if (value == null || value.definition() != identity) {
                OptionalDouble cost;
                try { cost = OptionalDouble.of(source.mana(definition)); }
                catch (IllegalArgumentException unsupported) { cost = OptionalDouble.empty(); }
                value = new Cached(identity, cost);
            }
            cached.put(key, value); value.cost().ifPresent(cost -> costs.put(key, cost));
        });
        CACHE.put(player, Map.copyOf(cached));
        return Map.copyOf(costs);
    }
    private static void include(Map<UUID, ManaItemCosts.Source> sources, ItemStack item) {
        if (item.isEmpty() || sources.size() >= ItemManaPayload.MAX_ITEMS) return;
        ManaItemCosts.source(item).ifPresent(source -> sources.putIfAbsent(source.key(), source));
    }
    @SubscribeEvent public static void tick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.connection == null
                || !NetworkRegistry.hasChannel(player.connection, ItemManaPayload.TYPE.id())) return;
        var costs = snapshot(player);
        if (!costs.equals(SENT.get(player))) {
            PacketDistributor.sendToPlayer(player, new ItemManaPayload(costs)); SENT.put(player, costs);
        }
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) { remove(event.getEntity()); }
    @SubscribeEvent public static void clonePlayer(PlayerEvent.Clone event) { remove(event.getOriginal()); remove(event.getEntity()); }
    @SubscribeEvent public static void stop(ServerStoppedEvent event) {
        CACHE.keySet().removeIf(p -> p.getServer() == event.getServer());
        SENT.keySet().removeIf(p -> p.getServer() == event.getServer());
    }
    private static void remove(Player player) { CACHE.remove(player); SENT.remove(player); }
}
