package com.quzzar.vestige.equipment;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.equipment.mixin.LivingDamageAccess;
import com.quzzar.vestige.magic.condition.ConditionValue;
import com.quzzar.vestige.magic.definition.SpellTriggerTypes;
import com.quzzar.vestige.magic.runtime.*;
import com.quzzar.vestige.magic.world.*;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.joml.Vector3f;
import java.util.*;

/** Trusted worn-item adapter. Definitions, trait resolution, ward lifetime and recovery belong to the shared runtime. */
@EventBusSubscriber(modid = VestigeMainMod.MOD_ID)
public final class EquipmentMagic {
    private static final Map<DamageContainer, Hit> HITS = new WeakHashMap<>();
    private static final Map<UUID, WornSource> WARDS = new HashMap<>();
    private EquipmentMagic() { }
    private static ResourceLocation id(String path) { return VestigeMainMod.location(path); }
    private static DamageContainer current(LivingEntity actor) {
        var stack = ((LivingDamageAccess) actor).vestige$damageContainers();
        return stack.isEmpty() ? null : stack.peek();
    }
    public static double manaBonus(LivingEntity actor) {
        ItemStack stack = actor.getItemBySlot(EquipmentSlot.CHEST);
        if (!(stack.getItem() instanceof MagicArmorItem item)) return 0;
        var ability = NativeMagic.abilities().abilities().get(item.ability());
        return ability == null ? 0 : Math.max(0, MagicResolution.resolve(ability, List.of()).variable(id("mana_bonus")));
    }
    /** Called after shields and hurt immunity have accepted a hit, immediately before vanilla armor mitigation. */
    public static float protect(LivingEntity actor, DamageSource damage, float amount) {
        if (actor.level().isClientSide() || amount <= 0 || actor.isInvulnerableTo(damage) || damage.is(DamageTypeTags.BYPASSES_EFFECTS)) return amount;
        ItemStack armor = actor.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack boots = actor.getItemBySlot(EquipmentSlot.FEET);
        if (!(armor.getItem() instanceof MagicArmorItem) && !(boots.getItem() instanceof WayfarerBootsItem)) return amount;
        var ability = armor.getItem() instanceof MagicArmorItem item ? NativeMagic.abilities().abilities().get(item.ability()) : null;
        DamageContainer container = current(actor);
        if (container == null) return amount;
        var session = NativeMagic.session(actor.getServer()); session.world().registerActor(actor);
        var hit = new Hit(armor, boots, new SpellEvent.PendingOutcome(amount));
        HITS.put(container, hit);
        var event = new SpellEvent(SpellTriggerTypes.ARMOR_DAMAGE_CALCULATING, actor.getUUID(),
                Optional.of(new SpellSubject.Entity(actor.getUUID())), Optional.of(hit.pending), session.world().cause(damage.getDirectEntity()),
                Map.of(id("event/wayfarer_landing"), new ConditionValue.Decimal(damage.is(DamageTypeTags.IS_FALL) && WayfarerMagic.landingEligible(actor) ? 1 : 0)));
        session.runtime().emit(event);
        if (ability != null && armor.is(MagicEquipment.CINDERWEAVE.get()) && damage.is(DamageTypeTags.IS_FIRE)) {
            var resolved = session.runtime().resolve(ability, actor.getUUID(), List.of());
            double reduction = Math.min(hit.pending.amount(), Math.min(hit.pending.amount() * resolved.variable(id("fire_reduction")), resolved.variable(id("fire_cap"))));
            if (reduction > 0) { hit.pending.reduce(reduction); hit.protectedHit = true; shimmer(actor, true); }
        }
        float remaining = (float) hit.pending.commit();
        if (hit.protectedHit) {
            // Same ordinary per-hit wear formula, using the eligible incoming hit before its own protection.
            armor.hurtAndBreak(Math.min(armor.getMaxDamage(), Math.max(1, (int) (amount / 4))), actor, EquipmentSlot.CHEST);
            NativeMana.reconcile(actor);
            if (armor.isEmpty()) removed(actor);
        }
        if (hit.bootsProtected) {
            boots.hurtAndBreak(Math.min(boots.getMaxDamage(), Math.max(1, (int) (amount / 4))), actor, EquipmentSlot.FEET);
            if (boots.isEmpty()) WayfarerMagic.remove(actor);
        }
        return remaining;
    }
    @SubscribeEvent public static void armorWear(ArmorHurtEvent event) {
        Hit hit = HITS.get(current(event.getEntity()));
        if (hit != null && hit.protectedHit && event.getArmorItemStack(EquipmentSlot.CHEST) == hit.armor)
            event.setNewDamage(EquipmentSlot.CHEST, 0);
        if (hit != null && hit.bootsProtected && event.getArmorItemStack(EquipmentSlot.FEET) == hit.boots)
            event.setNewDamage(EquipmentSlot.FEET, 0);
    }
    static boolean preventedFeet(LivingEntity actor, SpellRuntime.Context context) {
        Hit hit = HITS.get(current(actor));
        if (hit == null || context.event().pending().orElse(null) != hit.pending) return false;
        hit.bootsProtected = true; return true;
    }
    @SubscribeEvent public static void damaged(LivingDamageEvent.Post event) {
        var actor = event.getEntity();
        if (actor.level().isClientSide()) return;
        Hit hit = HITS.remove(current(actor));
        if (event.getNewDamage() <= 0 || !actor.isAlive() || event.getSource().is(DamageTypeTags.BYPASSES_EFFECTS)
                || hit != null && hit.wardSpent) return;
        ItemStack armor = actor.getItemBySlot(EquipmentSlot.CHEST);
        if (!armor.is(MagicEquipment.WARDWEAVE.get())) return;
        var old = WARDS.get(actor.getUUID());
        if (old != null && old.continues()) return;
        var ability = NativeMagic.abilities().abilities().get(((MagicArmorItem) armor.getItem()).ability());
        if (ability == null) return;
        var session = NativeMagic.session(actor.getServer()); session.world().registerActor(actor);
        var source = new WornSource(actor, armor);
        session.runtime().activate(ability, new SpellEvent(SpellTriggerTypes.DAMAGE_TAKEN, actor.getUUID(),
                Optional.of(new SpellSubject.Entity(actor.getUUID())), Optional.empty(), session.world().cause(event.getSource().getDirectEntity()),
                Map.of(id("event/damage_amount"), new ConditionValue.Decimal(event.getNewDamage()))), List.of(), source);
    }
    /** Slot mutation revokes a ward immediately. Its actor/ability recovery remains in the runtime. */
    public static void removed(LivingEntity actor) {
        var source = WARDS.remove(actor.getUUID());
        if (source != null) source.armed = false;
    }
    @SubscribeEvent public static void equipment(LivingEquipmentChangeEvent event) {
        if (event.getEntity().level().isClientSide() || event.getSlot() != EquipmentSlot.CHEST) return;
        var source = WARDS.get(event.getEntity().getUUID());
        if (source != null && !source.valid()) removed(event.getEntity());
        NativeMana.reconcile(event.getEntity());
        NativeMana.sync(event.getEntity(), true);
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) { removed(event.getEntity()); }
    @SubscribeEvent public static void death(LivingDeathEvent event) { removed(event.getEntity()); }
    @SubscribeEvent public static void stop(net.neoforged.neoforge.event.server.ServerStoppedEvent event) { clear(); }
    public static void clear() { WARDS.values().forEach(s -> s.armed = false); WARDS.clear(); HITS.clear(); }
    public static void tick() { WARDS.entrySet().removeIf(e -> !e.getValue().continues()); }
    private static void wardShimmer(LivingEntity wearer, SpellRuntime.Context context) {
        var visual = new com.quzzar.vestige.magic.presentation.SpellVisual(10,
                new com.quzzar.vestige.magic.expression.SpellValue.Constant(.35), 1,
                List.of(new com.quzzar.vestige.magic.presentation.SpellVisual.Layer(
                        com.quzzar.vestige.magic.presentation.SpellVisual.Shape.SIGIL, 0xc0d1de, .55f, .015f, 1, 0, 0, 4)), false, Optional.empty());
        // The same bounded procedural rune renderer used by spells, with a brief cast-owned cue.
        NativeMagic.session(wearer.getServer()).world().present(visual, List.of(new SpellSubject.Entity(wearer.getUUID())), context);
    }
    private static void shimmer(LivingEntity actor, boolean cinder) {
        if (actor.level() instanceof ServerLevel level) {
            var color = cinder ? new Vector3f(1f, .32f, .06f) : new Vector3f(.75f, .82f, .87f);
            level.sendParticles(new DustParticleOptions(color, .55f), actor.getX(), actor.getY() + actor.getBbHeight() * .6,
                    actor.getZ(), 6, .18, .2, .18, 0);
        }
    }
    private static final class Hit {
        final ItemStack armor, boots;
        final SpellEvent.PendingOutcome pending;
        boolean protectedHit, wardSpent, bootsProtected;
        Hit(ItemStack armor, ItemStack boots, SpellEvent.PendingOutcome pending) { this.armor = armor; this.boots = boots; this.pending = pending; }
    }
    private static final class WornSource implements CastReservation, CastObserver {
        final LivingEntity actor;
        final ItemStack armor;
        boolean armed, started;
        long expires;
        WornSource(LivingEntity actor, ItemStack armor) { this.actor = actor; this.armor = armor; }
        @Override public boolean valid() { return actor.isAlive() && !armor.isEmpty() && actor.getItemBySlot(EquipmentSlot.CHEST) == armor; }
        @Override public boolean continues() { return !started || armed && valid() && actor.level().getGameTime() < expires; }
        @Override public void commit() { }
        @Override public CastObserver observer() { return this; }
        @Override public void activated(SpellRuntime.Context context) {
            started = true; armed = true;
            expires = actor.level().getGameTime() + Math.clamp((long) Math.ceil(context.number(new com.quzzar.vestige.magic.expression.SpellValue.Variable(id("ward_ticks")))), 1, 240000);
            WARDS.put(actor.getUUID(), this);
            wardShimmer(actor, context);
        }
        @Override public void mitigated(double amount, SpellRuntime.Context context) {
            Hit hit = HITS.get(current(actor));
            if (hit == null || context.event().pending().orElse(null) != hit.pending) return;
            hit.protectedHit = true; hit.wardSpent = true; armed = false;
            wardShimmer(actor, context);
        }
    }
}
