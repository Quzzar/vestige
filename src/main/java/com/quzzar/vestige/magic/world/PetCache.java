package com.quzzar.vestige.magic.world;

import com.quzzar.vestige.magic.effect.SpellEffects;
import com.quzzar.vestige.magic.runtime.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.nbt.*;
import net.minecraft.resources.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.*;
import java.util.*;

/** Emergency return journal, not saved active spells. UUID, inventory and ownership remain the pet's. */
final class PetCache extends SavedData {
    private final Map<UUID,CompoundTag> returns=new LinkedHashMap<>();
    private static final Set<UUID> ACTIVE=new HashSet<>();
    private static final Factory<PetCache> FACTORY=new Factory<>(PetCache::new,PetCache::load);
    private static PetCache data(MinecraftServer server) { return server.overworld().getDataStorage().computeIfAbsent(FACTORY,"vestige_pet_returns"); }
    private static PetCache load(CompoundTag tag,HolderLookup.Provider provider) {
        PetCache data=new PetCache();
        for (Tag entry : tag.getList("returns",Tag.TAG_COMPOUND)) { CompoundTag record=(CompoundTag)entry; data.returns.put(record.getUUID("uuid"),record); }
        return data;
    }
    @Override public CompoundTag save(CompoundTag tag,HolderLookup.Provider provider) {
        ListTag list=new ListTag(); returns.values().forEach(record->list.add(record.copy())); tag.put("returns",list); return tag;
    }
    static NativeSpellFeatures.Lease create(MinecraftSpellWorld world,SpellEffects.Manifestation d,Map<String,Double> values,SpellRuntime.Context c) {
        LivingEntity caster=world.actor(c);
        if (!(world.target(c) instanceof Mob pet) || !(pet instanceof OwnableEntity own) || !caster.getUUID().equals(own.getOwnerUUID())
                || pet.isPassenger() || pet.isVehicle() || pet.isLeashed() || ACTIVE.contains(pet.getUUID())) return null;
        ServerLevel origin=(ServerLevel)pet.level(); MinecraftServer server=origin.getServer();
        ResourceKey<Level> dimension=d.identifiers().containsKey("dimension") ? ResourceKey.create(Registries.DIMENSION,d.identifiers().get("dimension")) : PrivateSpaces.DIMENSION;
        ServerLevel cache=server.getLevel(dimension); if (cache==null) return null;
        PetCache journal=data(server); if (journal.returns.containsKey(pet.getUUID())) return null;
        int slot=0; Set<Integer> used=new HashSet<>(); journal.returns.values().forEach(r->used.add(r.getInt("slot"))); while (used.contains(slot)) slot++;
        BlockPos cell=new BlockPos(2000000+(slot%10000)*16,8,(slot/10000)*16);
        cache.getChunkAt(cell);
        for (int x=-2;x<=2;x++) for (int z=-2;z<=2;z++) cache.setBlock(cell.offset(x,-1,z),Blocks.BARRIER.defaultBlockState(),3);
        CompoundTag record=new CompoundTag(); record.putUUID("uuid",pet.getUUID()); record.putUUID("owner",caster.getUUID());
        record.putString("origin",origin.dimension().location().toString()); record.putString("cache",dimension.location().toString()); record.putInt("slot",slot);
        record.putDouble("x",pet.getX()); record.putDouble("y",pet.getY()); record.putDouble("z",pet.getZ());
        CompoundTag backup=pet.saveWithoutId(new CompoundTag()); backup.putString("id",BuiltInRegistries.ENTITY_TYPE.getKey(pet.getType()).toString()); record.put("backup",backup);
        record.putBoolean("no_ai",pet.isNoAi()); record.putBoolean("invulnerable",pet.isInvulnerable()); record.putBoolean("no_gravity",pet.isNoGravity());
        journal.returns.put(pet.getUUID(),record); journal.setDirty(); server.overworld().getDataStorage().save();
        ACTIVE.add(pet.getUUID());
        Entity moved=pet.changeDimension(new DimensionTransition(cache,cell.getBottomCenter(),Vec3.ZERO,pet.getYRot(),pet.getXRot(),DimensionTransition.DO_NOTHING));
        if (!(moved instanceof Mob cached) || cache.getEntity(pet.getUUID())!=cached) {
            ACTIVE.remove(pet.getUUID()); recover(server,pet.getUUID()); return null;
        }
        cached.setNoAi(true); cached.setInvulnerable(true); cached.setNoGravity(true); cached.setDeltaMovement(Vec3.ZERO);
        SpellAnchor marker=SpellEntities.ANCHOR.get().create(origin);
        if (marker==null) { ACTIVE.remove(pet.getUUID()); recover(server,pet.getUUID()); return null; }
        marker.setPos(record.getDouble("x"),record.getDouble("y"),record.getDouble("z")); marker.setInvisible(true); marker.setNoGravity(true);
        if (!origin.addFreshEntity(marker)) { ACTIVE.remove(pet.getUUID()); recover(server,pet.getUUID()); return null; }
        world.owners.put(marker.getUUID(),caster.getUUID());
        return world.features.new Lease(d,values,c,caster,origin,marker) {
            @Override public boolean alive() { return super.alive() && cached.isAlive() && cache.getEntity(cached.getUUID())==cached; }
            @Override public void tick() { super.tick(); cached.setPos(cell.getBottomCenter()); cached.setDeltaMovement(Vec3.ZERO); cached.setAirSupply(cached.getMaxAirSupply()); }
            @Override void release() { ACTIVE.remove(cached.getUUID()); recover(server,cached.getUUID()); }
        };
    }
    static void retry(MinecraftServer server) { for (UUID id : List.copyOf(data(server).returns.keySet())) if (!ACTIVE.contains(id)) recover(server,id); }
    private static void recover(MinecraftServer server,UUID id) {
        PetCache journal=data(server); CompoundTag record=journal.returns.get(id); if (record==null) return;
        ServerLevel origin=server.getLevel(ResourceKey.create(Registries.DIMENSION,ResourceLocation.parse(record.getString("origin"))));
        ServerLevel cache=server.getLevel(ResourceKey.create(Registries.DIMENSION,ResourceLocation.parse(record.getString("cache"))));
        if (origin==null || cache==null) return;
        int slot=record.getInt("slot"); BlockPos cell=new BlockPos(2000000+(slot%10000)*16,8,(slot/10000)*16);
        Vec3 destination=new Vec3(record.getDouble("x"),record.getDouble("y"),record.getDouble("z"));
        cache.getChunkAt(cell); origin.getChunkAt(BlockPos.containing(destination));
        Entity existing=cache.getEntity(id);
        if (existing==null) for (ServerLevel level : server.getAllLevels()) if (level.getEntity(id)!=null) { existing=level.getEntity(id); break; }
        // A crash between the two dimension saves can leave both snapshots. Only this journal's
        // same-type, same-owner copies are eligible for reconciliation; prefer the sheltered body.
        if (existing!=null) {
            if (!(existing instanceof OwnableEntity own) || !record.getUUID("owner").equals(own.getOwnerUUID())) return;
            List<Entity> duplicates=new ArrayList<>();
            for (ServerLevel level : server.getAllLevels()) {
                Entity duplicate=level.getEntity(id);
                if (duplicate!=null && duplicate!=existing) {
                    if (duplicate.getType()!=existing.getType() || !(duplicate instanceof OwnableEntity owned) || !record.getUUID("owner").equals(owned.getOwnerUUID())) return;
                    duplicates.add(duplicate);
                }
            }
            duplicates.forEach(Entity::discard);
        }
        Entity pet=existing;
        if (pet==null) pet=EntityType.loadEntityRecursive(record.getCompound("backup"),origin,e->e);
        if (!(pet instanceof Mob mob)) return;
        for (int y=0;y<=128;y++) {
            Vec3 attempt=destination.add(0,y,0);
            if (origin.noCollision(mob,mob.getBoundingBox().move(attempt.subtract(mob.position())))) { destination=attempt; break; }
            if (y==128) return;
        }
        if (existing==null) { mob.setPos(destination); if (!origin.addFreshEntity(mob)) return; }
        else if (mob.level()!=origin) {
            Entity returned=mob.changeDimension(new DimensionTransition(origin,destination,Vec3.ZERO,mob.getYRot(),mob.getXRot(),DimensionTransition.DO_NOTHING));
            if (!(returned instanceof Mob returnedMob) || origin.getEntity(id)!=returnedMob) return;
            mob=returnedMob;
        } else mob.teleportTo(destination.x,destination.y,destination.z);
        mob.setNoAi(record.getBoolean("no_ai")); mob.setInvulnerable(record.getBoolean("invulnerable")); mob.setNoGravity(record.getBoolean("no_gravity"));
        journal.returns.remove(id); journal.setDirty(); server.overworld().getDataStorage().save();
    }
}
