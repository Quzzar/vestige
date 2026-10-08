package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.definition.*;
import com.quzzar.vestige.magic.runtime.*;
import com.quzzar.vestige.magic.world.NativeMagic;
import net.minecraft.sounds.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import java.util.*;

/** Staff casts preserve a selected immutable scroll view; normal payment and rarity-based wear commit together. */
@EventBusSubscriber(modid=VestigeMainMod.MOD_ID)
public final class StaffCasting {
    private record Pending(SpellRuntime.Cast cast,Spellshaping.Compiled compiled,InteractionHand hand) { }
    private static final Map<Player,Pending> PENDING=new IdentityHashMap<>();
    private StaffCasting() { }
    public static boolean cast(Player player,InteractionHand hand) {
        if(PENDING.containsKey(player)) return recast(player);
        if(!player.isAlive() || player.isSpectator() || ScrollCasting.awaiting(player) || WandCasting.awaiting(player)) return false;
        var stack=player.getItemInHand(hand);var binding=StaffData.binding(stack).orElse(null);
        var scroll=binding==null ? null : binding.active().orElse(null);
        var spell=scroll==null ? null : NativeMagic.spells().spells().get(scroll.spell());
        if(spell==null || !StaffData.accepts(binding.affinity(),spell)) return false;
        Spellshaping.Compiled compiled;
        try {
            compiled=Spellshaping.compile(spell,scroll.augments(),scroll.modifiers(),scroll.shaping());
            // A reserved casting source cannot pay its own material consume/damage cost.
            if(compiled.shaping().costs(compiled.spell().costs()).stream().anyMatch(c -> c instanceof SpellCost.Material m && m.item().equals(VestigeMainMod.location("staff")))) return false;
        } catch(IllegalArgumentException incompatible) { return false; }
        var expected=stack.copy();int wear=StaffData.wear(compiled.spell().rarity());
        var reservation=new CastReservation() {
            public boolean valid() { return ItemStack.matches(expected,player.getItemInHand(hand)); }
            public void commit() {
                if(player.hasInfiniteMaterials()) return;
                var held=player.getItemInHand(hand);int damage=held.getDamageValue()+wear;
                if(damage>=held.getMaxDamage()) {
                    held.shrink(1);player.level().playSound(null,player.blockPosition(),SoundEvents.ITEM_BREAK,SoundSource.PLAYERS,.8f,1);
                } else held.setDamageValue(damage);
            }
        };
        var session=NativeMagic.session(player.getServer());session.world().registerActor(player);
        var cast=session.runtime().cast(compiled.spell(),SpellEvent.of(SpellTriggerTypes.INTERACT,player.getUUID(),null),
                compiled.modifiers(),SpellKnowledge.identified(player,spell.id()),Optional.empty(),false,compiled.shaping(),reservation);
        if(List.of(SpellRuntime.Status.BUSY,SpellRuntime.Status.COOLDOWN,SpellRuntime.Status.CONDITIONS_FAILED,
                SpellRuntime.Status.LOOP_REJECTED,SpellRuntime.Status.COST_FAILED,SpellRuntime.Status.INTERRUPTED).contains(cast.status())) return false;
        var active=new Pending(cast,compiled,hand);PENDING.put(player,active);settle(player,active);return true;
    }
    /** A continuation keeps its original selected spell, even after selection changes or the last use breaks. */
    public static boolean recast(Player player) {
        var pending=PENDING.get(player);if(pending==null || pending.cast().status()!=SpellRuntime.Status.AWAITING_RECAST) return false;
        var continued=NativeMagic.session(player.getServer()).runtime().cast(pending.compiled().spell(),
                SpellEvent.of(SpellTriggerTypes.INTERACT,player.getUUID(),null),pending.compiled().modifiers(),true,
                Optional.empty(),false,pending.compiled().shaping());
        settle(player,pending);return continued==pending.cast();
    }
    public static boolean awaiting(Player player) { return PENDING.containsKey(player); }
    /** Presentation follows only the hand reserved by this exact initial cast. */
    public static Optional<InteractionHand> preparingHand(Player player,UUID castId) {
        var pending=PENDING.get(player);
        return pending!=null && pending.cast().id().equals(castId) && pending.cast().status()==SpellRuntime.Status.CHARGING
                ? Optional.of(pending.hand()) : Optional.empty();
    }
    private static void settle(Player player,Pending pending) {
        if(!List.of(SpellRuntime.Status.CHARGING,SpellRuntime.Status.RUNNING,SpellRuntime.Status.AWAITING_RECAST).contains(pending.cast().status())) PENDING.remove(player);
    }
    @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOW)
    public static void tick(ServerTickEvent.Post event) {
        for(var entry:List.copyOf(PENDING.entrySet())) if(entry.getKey().getServer()==event.getServer()) settle(entry.getKey(),entry.getValue());
    }
    public static void cancelAll() { PENDING.clear(); }
    @SubscribeEvent public static void stop(ServerStoppedEvent event) { PENDING.keySet().removeIf(p -> p.getServer()==event.getServer()); }
}
