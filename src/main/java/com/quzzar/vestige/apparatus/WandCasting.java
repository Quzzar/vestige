package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.definition.SpellTriggerTypes;
import com.quzzar.vestige.magic.runtime.*;
import com.quzzar.vestige.magic.world.NativeMagic;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import java.util.*;

/** Reservations use complete held components, and deterministic wear commits before any spell outcomes. */
@EventBusSubscriber(modid=VestigeMainMod.MOD_ID)
public final class WandCasting {
    private record Pending(SpellRuntime.Cast cast,Spellshaping.Compiled compiled,WandData.Binding binding) { }
    private static final Map<Player,Pending> PENDING=new IdentityHashMap<>();
    private WandCasting() { }
    public static boolean cast(Player player,InteractionHand hand) {
        var pending=PENDING.get(player);
        if (pending!=null) {
            if (!WandData.binding(player.getItemInHand(hand)).filter(pending.binding()::equals).isPresent()) return false;
            return recast(player);
        }
        var stack=player.getItemInHand(hand);var binding=WandData.binding(stack).orElse(null);
        var spell=binding==null ? null : NativeMagic.spells().spells().get(binding.scroll().spell());
        if (spell==null || !player.isAlive() || player.isSpectator() || ScrollCasting.awaiting(player)) return false;
        WandComponents.Compiled component;
        try { component=WandComponents.compile(spell,binding.scroll(),binding.base(),binding.thread(),binding.tip()); }
        catch (IllegalArgumentException incompatible) { return false; }
        var session=NativeMagic.session(player.getServer());session.world().registerActor(player);
        var expected=stack.copy();
        var observer=binding.tip().<CastObserver>map(t -> new WandTipEffects(t,player,spell.traits())).orElse(CastObserver.NONE);
        var reservation=new CastReservation() {
            public CastObserver observer() { return observer; }
            public boolean valid() { return ItemStack.matches(expected,player.getItemInHand(hand)); }
            public Optional<Recovery> recovery() {
                return Optional.of(new Recovery(VestigeMainMod.location("wand"),WandComponents.COOLDOWN_TICKS));
            }
            public void commit() {
                if (player.hasInfiniteMaterials()) return;
                var held=player.getItemInHand(hand);int damage=held.getDamageValue()+component.wear();
                if (damage>=held.getMaxDamage()) {
                    held.shrink(1);
                    player.level().playSound(null,player.blockPosition(),SoundEvents.ITEM_BREAK,SoundSource.PLAYERS,.8f,1);
                } else held.setDamageValue(damage);
            }
        };
        var compiled=component.cast();
        var cast=session.runtime().cast(compiled.spell(),SpellEvent.of(SpellTriggerTypes.INTERACT,player.getUUID(),null),
                compiled.modifiers(),SpellKnowledge.identified(player,spell.id()),Optional.empty(),false,compiled.shaping(),reservation);
        if (List.of(SpellRuntime.Status.BUSY,SpellRuntime.Status.COOLDOWN,SpellRuntime.Status.CONDITIONS_FAILED,
                SpellRuntime.Status.LOOP_REJECTED,SpellRuntime.Status.COST_FAILED,SpellRuntime.Status.INTERRUPTED).contains(cast.status())) return false;
        var active=new Pending(cast,compiled,binding);PENDING.put(player,active);settle(player,active);
        return true;
    }
    /** Continues the same already-paid cast, including empty-hand input after its final durability use. */
    public static boolean recast(Player player) {
        var pending=PENDING.get(player);
        if (pending==null || pending.cast().status()!=SpellRuntime.Status.AWAITING_RECAST) return false;
        var continued=NativeMagic.session(player.getServer()).runtime().cast(pending.compiled().spell(),
                SpellEvent.of(SpellTriggerTypes.INTERACT,player.getUUID(),null),pending.compiled().modifiers(),true,
                Optional.empty(),false,pending.compiled().shaping());
        settle(player,pending);
        return continued==pending.cast();
    }
    public static boolean awaiting(Player player) { return PENDING.containsKey(player); }
    private static void settle(Player player,Pending pending) {
        if (!List.of(SpellRuntime.Status.CHARGING,SpellRuntime.Status.RUNNING,SpellRuntime.Status.AWAITING_RECAST).contains(pending.cast().status()))
            PENDING.remove(player);
        // A wand can never identify its bound spell, including a successful continuation.
    }
    @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOW)
    public static void tick(ServerTickEvent.Post event) {
        for (var entry:List.copyOf(PENDING.entrySet())) if (entry.getKey().getServer()==event.getServer()) settle(entry.getKey(),entry.getValue());
    }
    public static void cancelAll() { PENDING.clear(); }
    @SubscribeEvent public static void stop(ServerStoppedEvent event) { PENDING.keySet().removeIf(p -> p.getServer()==event.getServer()); }
}
