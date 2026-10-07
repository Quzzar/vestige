package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.definition.SpellTriggerTypes;
import com.quzzar.vestige.magic.runtime.SpellEvent;
import com.quzzar.vestige.magic.runtime.SpellRuntime;
import com.quzzar.vestige.magic.world.NativeMagic;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/** Scroll reservation, single consumption and persistent identification around the shared native runtime. */
@EventBusSubscriber(modid = VestigeMainMod.MOD_ID)
public final class ScrollCasting {
    private static final Map<Player, Pending> PENDING = new IdentityHashMap<>();
    private ScrollCasting() { }
    public static boolean cast(Player player, ItemStack stack) {
        if (WandCasting.awaiting(player)) return false;
        Pending existing = PENDING.get(player);
        if (existing != null) return existing.cast.status() == SpellRuntime.Status.AWAITING_RECAST && recast(player);
        var data = ScrollItems.scroll(stack).orElse(null);
        var spell = data == null ? null : NativeMagic.spells().spells().get(data.spell());
        if (spell == null) return false;
        Spellshaping.Compiled compiled;
        try { compiled=Spellshaping.compile(spell,data.augments(),data.modifiers(),data.shaping()); }
        catch(IllegalArgumentException invalid){return false;}
        var session = NativeMagic.session(player.getServer()); session.world().registerActor(player);
        ItemStack reserved = stack.copyWithCount(1);
        InteractionHand hand = player.getMainHandItem() == stack ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        var cast = session.runtime().cast(compiled.spell(), SpellEvent.of(SpellTriggerTypes.INTERACT, player.getUUID(), null), compiled.modifiers(), SpellKnowledge.identified(player, data.spell()), java.util.Optional.empty(), false, compiled.shaping());
        if (List.of(SpellRuntime.Status.BUSY, SpellRuntime.Status.COOLDOWN, SpellRuntime.Status.CONDITIONS_FAILED, SpellRuntime.Status.LOOP_REJECTED, SpellRuntime.Status.COST_FAILED).contains(cast.status())) {
            return false;
        }
        Pending pending = new Pending(player, hand, reserved, data, cast); PENDING.put(player,pending); settle(pending);
        return true;
    }
    /** Empty-hand air use continues this player's own awaiting scroll, with the original paid cast state. */
    public static boolean recast(Player player) {
        Pending pending = PENDING.get(player);
        if (pending == null || pending.cast.status() != SpellRuntime.Status.AWAITING_RECAST) return false;
        var spell = NativeMagic.spells().spells().get(pending.data.spell());
        if (spell == null) return false;
        NativeMagic.session(player.getServer()).runtime().cast(spell, SpellEvent.of(SpellTriggerTypes.INTERACT, player.getUUID(), null),
                pending.data.modifiers(), SpellKnowledge.identified(player, pending.data.spell()));
        settle(pending); return true;
    }
    @SubscribeEvent public static void beforeTick(ServerTickEvent.Pre event) {
        for (Pending pending : List.copyOf(PENDING.values())) {
            if (pending.player.getServer() != event.getServer()) continue;
            if (!pending.consumed && !ItemStack.isSameItemSameComponents(pending.reserved, pending.player.getItemInHand(pending.hand))) {
                NativeMagic.session(event.getServer()).runtime().interruptActor(pending.player.getUUID());
            }
        }
    }
    @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOW) public static void tick(ServerTickEvent.Post event) {
        for (Pending pending : List.copyOf(PENDING.values())) if (pending.player.getServer() == event.getServer()) settle(pending);
        for (var player : event.getServer().getPlayerList().getPlayers()) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) ScrollFragmentItem.resolve(player, player.getInventory().getItem(i));
            ScrollFragmentItem.resolve(player, player.containerMenu.getCarried());
        }
    }
    private static void settle(Pending pending) {
        if (pending.cast.paymentCommitted() && !pending.consumed) {
            ItemStack held = pending.player.getItemInHand(pending.hand);
            if (ItemStack.isSameItemSameComponents(pending.reserved, held) && !pending.player.hasInfiniteMaterials()) held.shrink(1);
            pending.consumed = true;
        }
        if (pending.cast.status() == SpellRuntime.Status.COMPLETED) {
            SpellKnowledge.identify(pending.player, pending.data.spell());
            PENDING.remove(pending.player);
        } else if (pending.cast.status() != SpellRuntime.Status.AWAITING_RECAST
                && pending.cast.status() != SpellRuntime.Status.RUNNING && pending.cast.status() != SpellRuntime.Status.CHARGING) {
            PENDING.remove(pending.player);
        }
    }
    public static void cancelAll() { PENDING.clear(); }
    static boolean awaiting(Player player) { return PENDING.containsKey(player); }
    @SubscribeEvent public static void stop(ServerStoppedEvent event) { PENDING.keySet().removeIf(p -> p.getServer() == event.getServer()); }
    private static final class Pending {
        final Player player; final InteractionHand hand; final ItemStack reserved; final ScrollItems.Scroll data; final SpellRuntime.Cast cast;
        boolean consumed;
        Pending(Player player, InteractionHand hand, ItemStack reserved, ScrollItems.Scroll data, SpellRuntime.Cast cast) {
            this.player=player; this.hand=hand; this.reserved=reserved; this.data=data; this.cast=cast;
        }
    }
}
