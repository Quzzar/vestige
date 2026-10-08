package com.quzzar.vestige.equipment;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.definition.SpellTriggerTypes;
import com.quzzar.vestige.magic.expression.SpellValue;
import com.quzzar.vestige.magic.runtime.*;
import com.quzzar.vestige.magic.world.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import java.util.*;

/** Native worn-source movement adapter; traits, payments, recovery and landing bindings use the common runtime. */
@EventBusSubscriber(modid = VestigeMainMod.MOD_ID)
public final class WayfarerMagic {
    private static final ResourceLocation SPEED = VestigeMainMod.location("wayfarer/burst");
    private static final Map<UUID, Source> BURSTS = new HashMap<>();
    private WayfarerMagic() { }
    public static boolean eligible(Player actor) {
        return actor.isAlive() && actor.isSprinting() && !actor.isSpectator() && !actor.isPassenger()
                && !actor.isInWaterOrBubble() && !actor.isInLava() && !actor.getAbilities().flying && !actor.isFallFlying();
    }
    /** Called only after Minecraft commits a grounded jump; canceled/prevented jumps never reach this event. */
    @SubscribeEvent public static void jump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide()) activate(player);
    }
    public static SpellRuntime.Cast activate(Player actor) {
        if (!eligible(actor) || actor.level().isClientSide()) return null;
        ItemStack boots = actor.getItemBySlot(EquipmentSlot.FEET);
        var variant = WayfarerImbuements.read(boots);
        var ability = NativeMagic.abilities().abilities().get(WayfarerImbuements.ABILITY);
        if (variant.isEmpty() || ability == null) return null;
        var session = NativeMagic.session(actor.getServer()); session.world().registerActor(actor);
        var source = new Source(actor, boots);
        return session.runtime().cast(ability, SpellEvent.of(SpellTriggerTypes.JUMP, actor.getUUID(), null), variant.get().modifiers(),
                true, Optional.empty(), false, variant.get().shaping(), source);
    }
    @SubscribeEvent public static void landing(LivingFallEvent event) {
        var source = BURSTS.get(event.getEntity().getUUID());
        if (source != null && source.continues()) {
            source.landingTick = source.landing ? source.actor.level().getGameTime() : -1;
            source.landing = false;
        }
    }
    public static boolean landingEligible(LivingEntity actor) {
        var source = BURSTS.get(actor.getUUID());
        return source != null && source.continues() && source.landingTick == actor.level().getGameTime();
    }
    @SubscribeEvent public static void equipment(LivingEquipmentChangeEvent event) {
        if (event.getSlot() == EquipmentSlot.FEET && !event.getEntity().level().isClientSide()) {
            var source = BURSTS.get(event.getEntity().getUUID());
            if (source != null && !source.valid()) remove(event.getEntity());
        }
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post event) {
        for (var source : List.copyOf(BURSTS.values())) if (!source.continues()) remove(source.actor);
    }
    @SubscribeEvent public static void death(LivingDeathEvent event) { remove(event.getEntity()); }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) { remove(event.getEntity()); }
    @SubscribeEvent public static void stop(ServerStoppedEvent event) { clear(); }
    public static void remove(LivingEntity actor) {
        Source source = BURSTS.remove(actor.getUUID());
        if (source != null) source.close();
    }
    public static void clear() { for (var source : List.copyOf(BURSTS.values())) source.close(); BURSTS.clear(); }
    public static boolean active(LivingEntity actor) { return Optional.ofNullable(BURSTS.get(actor.getUUID())).map(Source::continues).orElse(false); }
    private static final class Source implements CastReservation, CastObserver {
        final Player actor;
        final ItemStack boots;
        boolean started, revoked, landing = true;
        long expires, landingTick = -1;
        Source(Player actor, ItemStack boots) { this.actor = actor; this.boots = boots; }
        @Override public boolean valid() { return !revoked && actor.isAlive() && !actor.isPassenger() && !actor.isInWaterOrBubble()
                && !actor.isInLava() && !actor.getAbilities().flying && !actor.isFallFlying()
                && actor.getItemBySlot(EquipmentSlot.FEET) == boots && WayfarerImbuements.read(boots).isPresent(); }
        @Override public boolean continues() { return valid() && (!started || actor.level().getGameTime() < expires); }
        @Override public void commit() { }
        @Override public CastObserver observer() { return this; }
        @Override public void activated(SpellRuntime.Context context) {
            var previous = BURSTS.remove(actor.getUUID()); if (previous != null) previous.close();
            started = true;
            expires = actor.level().getGameTime() + Math.clamp((long) Math.ceil(context.number(new SpellValue.Variable(VestigeMainMod.location("burst_ticks")))), 1, 240000);
            double bonus = context.number(new SpellValue.Variable(VestigeMainMod.location("movement_bonus")));
            var attribute = actor.getAttribute(Attributes.MOVEMENT_SPEED);
            if (attribute == null || !Double.isFinite(bonus) || bonus < 0) throw new IllegalStateException("Invalid movement ability");
            attribute.removeModifier(SPEED);
            attribute.addTransientModifier(new AttributeModifier(SPEED, bonus, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            BURSTS.put(actor.getUUID(), this);
            if (actor.level() instanceof ServerLevel level) level.sendParticles(ParticleTypes.CLOUD, actor.getX(), actor.getY() + .1, actor.getZ(), 5, .15, .05, .15, .01);
        }
        @Override public void mitigated(double amount, SpellRuntime.Context context) {
            if (amount > 0 && EquipmentMagic.preventedFeet(actor, context)) landingTick = -1;
        }
        @Override public void ended(SpellRuntime.Status status) {
            if (status != SpellRuntime.Status.COMPLETED && BURSTS.get(actor.getUUID()) == this) remove(actor);
        }
        void close() {
            revoked = true;
            var attribute = actor.getAttribute(Attributes.MOVEMENT_SPEED); if (attribute != null) attribute.removeModifier(SPEED);
        }
    }
}
