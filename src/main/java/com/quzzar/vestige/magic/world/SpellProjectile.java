package com.quzzar.vestige.magic.world;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;

/** A single reusable delivery entity. Spell definitions supply its impact plan. */
public final class SpellProjectile extends ThrowableItemProjectile {
    private static final EntityDataAccessor<Boolean> VISUAL = SynchedEntityData.defineId(SpellProjectile.class, EntityDataSerializers.BOOLEAN);
    private final Set<UUID> hit = new HashSet<>();
    private Consumer<HitResult> impact = ignored -> { };
    private int lifetime = 100;
    private int pierces;
    private double gravity;
    private double homing;
    private UUID seek;
    private java.util.function.Predicate<Entity> targets = ignored -> true;
    private net.minecraft.world.item.ItemStack recovery = net.minecraft.world.item.ItemStack.EMPTY;

    public SpellProjectile(EntityType<? extends ThrowableItemProjectile> type, Level level) { super(type, level); }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder); builder.define(VISUAL, false);
    }
    public void visual(boolean enabled) { entityData.set(VISUAL, enabled); }
    public boolean hasVisual() { return entityData.get(VISUAL); }
    public void configure(int lifetime, int pierces, double gravity, double homing, Entity seek, java.util.function.Predicate<Entity> targets, Consumer<HitResult> impact) {
        this.lifetime = lifetime; this.pierces = pierces; this.gravity = gravity; this.homing = homing;
        this.seek = seek == null ? null : seek.getUUID(); this.impact = Objects.requireNonNull(impact);
        this.targets = Objects.requireNonNull(targets);
    }
    @Override protected Item getDefaultItem() { return Items.AMETHYST_SHARD; }
    public void recover(net.minecraft.world.item.ItemStack stack) { recovery = stack.copy(); }
    @Override public void remove(RemovalReason reason) {
        if (!level().isClientSide && !recovery.isEmpty()) { spawnAtLocation(recovery); recovery = net.minecraft.world.item.ItemStack.EMPTY; }
        super.remove(reason);
    }
    @Override protected double getDefaultGravity() { return gravity; }
    @Override protected boolean canHitEntity(Entity entity) { return super.canHitEntity(entity) && targets.test(entity) && !hit.contains(entity.getUUID()); }
    @Override protected void onHit(HitResult result) {
        if (level().isClientSide) return;
        if (result instanceof EntityHitResult entity) hit.add(entity.getEntity().getUUID());
        impact.accept(result);
        if (!(result instanceof EntityHitResult) || pierces-- <= 0) discard();
    }
    @Override public void tick() {
        if (!level().isClientSide && tickCount >= lifetime) { discard(); return; }
        if (homing > 0 && seek != null && level() instanceof ServerLevel level && level.getEntity(seek) instanceof LivingEntity target && target.isAlive()) {
            Vec3 desired = target.getEyePosition().subtract(position()).normalize().scale(getDeltaMovement().length());
            setDeltaMovement(getDeltaMovement().lerp(desired, Math.min(1, homing)));
        }
        super.tick();
        if (level().isClientSide && !hasVisual()) level().addParticle(ParticleTypes.END_ROD, getX(), getY(), getZ(), 0, 0, 0);
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        // Callbacks belong to the active runtime and cannot survive unloading/reloading.
        discard();
    }
}
