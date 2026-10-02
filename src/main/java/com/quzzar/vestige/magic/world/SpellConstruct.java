package com.quzzar.vestige.magic.world;

import net.minecraft.network.syncher.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/** Authored, finite collision geometry. It creates no blocks, loot or persistent inventory. */
public final class SpellConstruct extends SpellAnchor {
    private static final EntityDataAccessor<Float> WIDTH = SynchedEntityData.defineId(SpellConstruct.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> HEIGHT = SynchedEntityData.defineId(SpellConstruct.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DEPTH = SynchedEntityData.defineId(SpellConstruct.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> SOLID = SynchedEntityData.defineId(SpellConstruct.class, EntityDataSerializers.BOOLEAN);
    public SpellConstruct(EntityType<? extends ArmorStand> type, Level level) { super(type, level); noPhysics = true; setNoGravity(true); setInvisible(true); }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(WIDTH, 1f); builder.define(HEIGHT, 1f); builder.define(DEPTH, 1f); builder.define(SOLID, false);
    }
    public void geometry(double width, double height, double depth, boolean solid) {
        entityData.set(WIDTH, (float) width); entityData.set(HEIGHT, (float) height); entityData.set(DEPTH, (float) depth);
        entityData.set(SOLID, solid); refreshDimensions();
    }
    @Override public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (key == WIDTH || key == HEIGHT || key == DEPTH) refreshDimensions();
    }
    @Override public EntityDimensions getDefaultDimensions(Pose pose) { return EntityDimensions.scalable(entityData.get(WIDTH), entityData.get(HEIGHT)); }
    @Override protected AABB makeBoundingBox() {
        return new AABB(getX() - entityData.get(WIDTH) / 2, getY(), getZ() - entityData.get(DEPTH) / 2,
                getX() + entityData.get(WIDTH) / 2, getY() + entityData.get(HEIGHT), getZ() + entityData.get(DEPTH) / 2);
    }
    @Override public boolean canBeCollidedWith() { return entityData.get(SOLID) && !isRemoved(); }
    @Override public boolean isPushable() { return false; }
    @Override public void push(Entity entity) { }
    @Override public void push(double x, double y, double z) { }
}
