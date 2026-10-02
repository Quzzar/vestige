package com.quzzar.vestige.magic.world;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import java.util.*;

/** A tracked visual copy, with no AI, inventory, collision, loot or independently executable actor. */
public final class SpellEcho extends Entity {
    private static final EntityDataAccessor<Integer> SOURCE=SynchedEntityData.defineId(SpellEcho.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<UUID>> UUID_SOURCE=SynchedEntityData.defineId(SpellEcho.class,EntityDataSerializers.OPTIONAL_UUID);
    public SpellEcho(EntityType<?> type,Level level) { super(type,level);noPhysics=true;setNoGravity(true); }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) { builder.define(SOURCE,-1);builder.define(UUID_SOURCE,Optional.empty()); }
    public void source(LivingEntity source) { entityData.set(SOURCE,source.getId());entityData.set(UUID_SOURCE,Optional.of(source.getUUID())); }
    public LivingEntity source() {
        Entity entity=level().getEntity(entityData.get(SOURCE));
        return entity instanceof LivingEntity living && living.isAlive() && entityData.get(UUID_SOURCE).filter(living.getUUID()::equals).isPresent()?living:null;
    }
    @Override public boolean isPickable() { return false; }
    @Override public boolean isPushable() { return false; }
    @Override public boolean shouldBeSaved() { return false; }
    @Override protected void readAdditionalSaveData(CompoundTag tag) { discard(); }
    @Override protected void addAdditionalSaveData(CompoundTag tag) { }
}
