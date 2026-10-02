package com.quzzar.vestige.magic.world;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.definition.*;
import com.quzzar.vestige.magic.effect.SpellEffects;
import com.quzzar.vestige.magic.expression.SpellValue;
import com.quzzar.vestige.magic.presentation.SpellVisual;
import com.quzzar.vestige.magic.presentation.SpellVisualPayload;
import com.quzzar.vestige.magic.runtime.*;
import net.minecraft.core.*;
import net.minecraft.core.particles.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.phys.*;
import java.util.*;

/** Reusable backing outcomes and their lifecycle; authored plans provide tick/impact/end behavior. */
final class SpellManifestations {
    private final MinecraftSpellWorld world;
    SpellManifestations(MinecraftSpellWorld world) { this.world = world; }

    Optional<SpellWorld.ManifestationHandle> create(SpellEffects.Manifestation definition, Map<String, Double> values, SpellRuntime.Context context) {
        LivingEntity caster = world.actor(context); if (caster == null) return Optional.empty();
        ServerLevel level = world.level(context.target(), caster); if (level == null) return Optional.empty();
        Vec3 point = world.position(context.target(), caster);
        return switch (definition.kind().getPath()) {
            case "construct", "block_wall", "zone", "mobility", "guard", "sensor", "pet_cache", "passage" -> world.features.create(definition, values, context);
            case "projectile" -> projectile(definition, values, context, caster, level);
            case "summon", "decoy" -> summon(definition, values, context, caster, level, point);
            case "block_lock" -> lock(context, caster);
            case "portal" -> portal(context, caster, level, point);
            case "wall" -> wall(values, context, caster, level, point);
            case "area", "barrier", "status", "tether" -> field(definition, values, context, caster, level, point);
            default -> Optional.empty();
        };
    }
    private Optional<SpellWorld.ManifestationHandle> projectile(SpellEffects.Manifestation definition, Map<String, Double> values,
                                                               SpellRuntime.Context context, LivingEntity caster, ServerLevel level) {
        int count = integer(values, "count", 1, 64);
        double speed = bounded(values, "speed", 1.5, 0.05, 32), distance = bounded(values, "distance", 32, 0.1, 128);
        List<Entity> entities = new ArrayList<>();
        var sought = world.select(new TargetSpec(TargetSpec.Selection.ENTITY_RAY, new SpellValue.Constant(distance), false, Map.of(), TargetSpec.Relationship.HOSTILE), context);
        Entity seek = sought.isEmpty() ? null : world.entity(((SpellSubject.Entity) sought.getFirst()).id());
        Item item = BuiltInRegistries.ITEM.getOptional(definition.identifiers().getOrDefault("item", ResourceLocation.parse("minecraft:amethyst_shard"))).orElse(Items.AMETHYST_SHARD);
        double spread = bounded(values, "spread", 0, 0, 180);
        for (int i = 0; i < count; i++) {
            SpellProjectile projectile = SpellEntities.PROJECTILE.get().create(level);
            if (projectile == null) break;
            projectile.setOwner(caster); projectile.setItem(new ItemStack(item));
            if (values.getOrDefault("held_item", 0.0) > 0 && !caster.getMainHandItem().isEmpty()) {
                var thrown = caster.getMainHandItem().copyWithCount(1); projectile.setItem(thrown);
                if (!(caster instanceof net.minecraft.world.entity.player.Player player) || !player.isCreative()) {
                    caster.getMainHandItem().shrink(1); projectile.recover(thrown);
                }
            }
            Vec3 origin = caster.getEyePosition(); Vec3 direction = caster.getLookAngle();
            if (values.getOrDefault("origin_target", 0.0) > 0) origin = world.position(context.target(), caster).add(0, 1, 0);
            if (values.getOrDefault("targeted", 0.0) > 0 && seek != null) origin = seek.position().add(Math.cos(i * Math.PI * 2 / count) * 3, 4, Math.sin(i * Math.PI * 2 / count) * 3);
            if (values.getOrDefault("rain", 0.0) > 0) {
                Vec3 aim = seek == null ? world.position(world.select(new TargetSpec(TargetSpec.Selection.AIMED_POSITION, new SpellValue.Constant(distance)), context).getFirst(), caster) : seek.position();
                origin = aim.add((caster.getRandom().nextDouble() - 0.5) * 6, 8 + caster.getRandom().nextDouble() * 4, (caster.getRandom().nextDouble() - 0.5) * 6);
                direction = aim.add(0, 0.5, 0).subtract(origin).normalize();
            } else if (values.getOrDefault("targeted", 0.0) > 0 && seek != null) direction = seek.getEyePosition().subtract(origin).normalize();
            else direction = direction.yRot((float) Math.toRadians(count == 1 ? 0 : spread * (i / (double) (count - 1) - 0.5)));
            projectile.setPos(origin);
            projectile.configure(Math.min(definition.durationTicks() < 0 ? 200 : definition.durationTicks(), Math.max(1, (int) Math.ceil(distance / speed))),
                    integer(values, "pierce", 0, 64), bounded(values, "gravity", 0, 0, 1), bounded(values, "homing", 0, 0, 1), seek,
                    entity -> entity instanceof LivingEntity && !world.ally(caster, entity), hit -> {
                        SpellSubject subject = hit instanceof EntityHitResult entity ? new SpellSubject.Entity(entity.getEntity().getUUID()) : new SpellSubject.Position(level.dimension(), hit.getLocation());
                        if (!definition.onHit().isEmpty()) context.execute(definition.onHit(), subject);
                        else {
                            if (hit instanceof EntityHitResult) context.execute(List.of(new SpellEffects.Action(VestigeMainMod.location("damage"), Map.of("amount", new SpellValue.Constant(values.getOrDefault("damage", 0.0))), Map.of())), subject);
                            // Default damage has no callback graph to own a hit cue. Reuse the
                            // authored projectile material for a finite burst at the real contact.
                            definition.visual().ifPresent(visual -> {
                                double radius=Math.max(.9,Math.min(3,context.number(visual.radius())*2));
                                var impact=new SpellVisual(24,new SpellValue.Constant(radius),0,visual.layers(),false,Optional.empty());
                                var anchor=new SpellVisualPayload.Point(hit.getLocation(),-1,new UUID(0,0),0);
                                world.visuals.start(level,impact,radius,false,()->List.of(anchor),()->true);
                            });
                        }
                    });
            projectile.shoot(direction.x, direction.y, direction.z, (float) speed, 0);
            if (level.addFreshEntity(projectile)) {
                entities.add(projectile); world.causedEntities.put(projectile.getUUID(), context.cause());
                definition.visual().ifPresent(visual -> {
                    UUID cue = world.visuals.start(level, visual, context.number(visual.radius()), true,
                            () -> List.of(MinecraftSpellWorld.visualPoint(projectile, visual.height())),
                            () -> projectile.isAlive() && !projectile.isRemoved() && projectile.level() == level);
                    projectile.visual(cue != null);
                });
            }
        }
        return entities.isEmpty() ? Optional.empty() : Optional.of(entities(entities));
    }
    private Optional<SpellWorld.ManifestationHandle> summon(SpellEffects.Manifestation definition, Map<String, Double> values,
                                                           SpellRuntime.Context context, LivingEntity caster, ServerLevel level, Vec3 point) {
        ResourceLocation id = definition.identifiers().getOrDefault("entity", ResourceLocation.parse("minecraft:zombie"));
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElse(null);
        if (type == null) return Optional.empty();
        int count = integer(values, "count", 1, 16); List<Entity> entities = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Entity entity = type.create(level); if (!(entity instanceof Mob mob)) continue;
            BlockPos position = findSpawn(level, caster, mob, point, i);
            if (position == null) continue;
            mob.setPos(position.getX() + 0.5, position.getY(), position.getZ() + 0.5);
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(position), MobSpawnType.MOB_SUMMONED, null);
            if (mob instanceof TamableAnimal tameable) {
                tameable.setTame(true, true);tameable.setOwnerUUID(caster.getUUID());
                // Vanilla's sit goal treats a non-player owner as missing. Native ownership supports any living caster.
                if (!(caster instanceof net.minecraft.world.entity.player.Player)) tameable.goalSelector.removeAllGoals(goal->goal instanceof net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal);
            }
            if (mob instanceof net.minecraft.world.entity.animal.horse.Horse horse) { horse.setTamed(true); horse.setOwnerUUID(caster.getUUID()); horse.equipSaddle(new ItemStack(Items.SADDLE), net.minecraft.sounds.SoundSource.PLAYERS); }
            if (values.containsKey("health")) {
                mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH)
                        .setBaseValue(Math.max(1, Math.min(100, values.get("health"))));
                mob.setHealth(mob.getMaxHealth());
            }
            var attack = mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
            if (attack != null && values.containsKey("attack_damage")) attack.setBaseValue(Math.max(0, Math.min(20, values.get("attack_damage"))));
            mob.setPersistenceRequired();
            if (values.getOrDefault("inert", 0.0) > 0) { mob.setNoAi(true); mob.setSilent(true); }
            if (definition.identifiers().containsKey("head")) mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(BuiltInRegistries.ITEM.get(definition.identifiers().get("head"))));
            if (definition.identifiers().containsKey("weapon")) mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(BuiltInRegistries.ITEM.get(definition.identifiers().get("weapon"))));
            if (level.addFreshEntity(mob)) { entities.add(mob); world.owners.put(mob.getUUID(), caster.getUUID()); world.causedEntities.put(mob.getUUID(), context.cause()); }
        }
        if (entities.isEmpty()) return Optional.empty();
        SpellWorld.ManifestationHandle handle = entities(entities);
        if (!definition.kind().getPath().equals("decoy")) return Optional.of(handle);
        return Optional.of(new SpellWorld.ManifestationHandle() {
            public SpellSubject subject() { return handle.subject(); }
            public boolean alive() { return handle.alive(); }
            public void tick() {
                for (Mob mob : level.getEntitiesOfClass(Mob.class, entities.getFirst().getBoundingBox().inflate(12)))
                    if (!world.ally(caster, mob)) mob.setTarget((LivingEntity) entities.getFirst());
            }
            public void close(SpellRuntime.EndReason reason) { handle.close(reason); }
        });
    }
    private Optional<SpellWorld.ManifestationHandle> lock(SpellRuntime.Context context, LivingEntity caster) {
        if (!(world.block(context.target(), caster) instanceof BaseContainerBlockEntity block)) return Optional.empty();
        if (block.getPersistentData().contains("vestige:lock_owner")) return Optional.empty();
        block.getPersistentData().putString("vestige:lock_owner", caster.getUUID().toString()); block.setChanged();
        return Optional.of(new SpellWorld.ManifestationHandle() {
            public SpellSubject subject() { return context.target(); }
            public boolean alive() { return world.block(context.target(), caster) == block && caster.getUUID().toString().equals(block.getPersistentData().getString("vestige:lock_owner")); }
            public void close(SpellRuntime.EndReason reason) {
                if (reason == SpellRuntime.EndReason.SERVER_STOP || reason == SpellRuntime.EndReason.OWNER_UNAVAILABLE || reason == SpellRuntime.EndReason.BACKING_REMOVED) return;
                if (caster.getUUID().toString().equals(block.getPersistentData().getString("vestige:lock_owner"))) { block.getPersistentData().remove("vestige:lock_owner"); block.setChanged(); }
            }
        });
    }
    private Optional<SpellWorld.ManifestationHandle> field(SpellEffects.Manifestation definition, Map<String, Double> values,
                                                          SpellRuntime.Context context, LivingEntity caster, ServerLevel level, Vec3 point) {
        String kind = definition.kind().getPath(); Entity target = world.target(context);
        boolean attached = kind.equals("status") || kind.equals("barrier") && values.getOrDefault("stationary", 0.0) == 0;
        if (attached && !(target instanceof LivingEntity)) return Optional.empty();
        Entity subject = attached ? target : marker(level, point, values.getOrDefault("health", 20.0));
        if (subject == null) return Optional.empty();
        if (!attached) { world.owners.put(subject.getUUID(), caster.getUUID()); world.causedEntities.put(subject.getUUID(), context.cause()); }
        SpellWorld.ManifestationHandle backing = attached ? null : entities(List.of(subject));
        double radius = bounded(values, "radius", 1.5, 0, 64);
        Vec3 anchor = point;
        Vec3 motion = caster.getLookAngle().multiply(1, 0, 1).normalize().scale(values.getOrDefault("motion", 0.0));
        return Optional.of(new SpellWorld.ManifestationHandle() {
            public SpellSubject subject() { return new SpellSubject.Entity(kind.equals("tether") && target != null ? target.getUUID() : subject.getUUID()); }
            public boolean alive() { return subject.isAlive() && !subject.isRemoved() && (target == null || target.isAlive())
                    && (values.getOrDefault("follow_target", 0.0) == 0 || target == null || target.level() == level); }
            public void tick() {
                if (!attached && values.getOrDefault("follow_target", 0.0) > 0 && target != null) subject.setPos(target.position());
                if (!attached && motion.lengthSqr() > 0) subject.setPos(subject.position().add(motion));
                if (values.getOrDefault("particles", 1.0) > 0)
                    particles(level, subject.position(), definition.identifiers().getOrDefault("particle", ResourceLocation.parse("minecraft:enchant")), radius);
                if (kind.equals("barrier") && !attached) {
                    for (Projectile projectile : level.getEntitiesOfClass(Projectile.class, subject.getBoundingBox().inflate(radius))) {
                        if (projectile.getOwner() != caster) projectile.discard();
                    }
                }
                if (kind.equals("tether") && target instanceof LivingEntity living && living.position().distanceToSqr(anchor) > radius * radius) {
                    living.setDeltaMovement(living.getDeltaMovement().add(anchor.subtract(living.position()).normalize().scale(0.35))); living.hurtMarked = true;
                }
            }
            public void close(SpellRuntime.EndReason reason) { if (backing != null) backing.close(reason); if (attached) world.actions.cleanup(context.castId()); }
        });
    }
    private Optional<SpellWorld.ManifestationHandle> wall(Map<String, Double> values, SpellRuntime.Context context, LivingEntity caster, ServerLevel level, Vec3 end) {
        Optional<SpellSubject> stored = context.anchor(VestigeMainMod.location("first_anchor")); if (stored.isEmpty()) return Optional.empty();
        if (world.level(stored.get(), caster) != level) return Optional.empty();
        Vec3 start = world.position(stored.get(), caster); double length = start.distanceTo(end);
        if (length < 0.5 || length > values.getOrDefault("max_length", 64.0)) return Optional.empty();
        Entity marker = marker(level, start.lerp(end, 0.5), 20); if (marker == null) return Optional.empty();
        world.owners.put(marker.getUUID(), caster.getUUID());
        var backing = entities(List.of(marker));
        return Optional.of(new SpellWorld.ManifestationHandle() {
            int tick;
            public SpellSubject subject() { return backing.subject(); }
            public boolean alive() { return backing.alive(); }
            public void tick() {
                tick++;
                for (int i = 0; i <= (int) length * 2; i++) level.sendParticles(ParticleTypes.FLAME, start.lerp(end, i / Math.max(1.0, (int) length * 2)).x, start.y + 1, start.lerp(end, i / Math.max(1.0, (int) length * 2)).z, 1, 0, 0.7, 0, 0);
                AABB box = new AABB(start, end).inflate(1, 3, 1);
                int damaged = 0;
                for (Entity entity : level.getEntities(caster, box)) {
                    double t = Math.max(0, Math.min(1, entity.position().subtract(start).dot(end.subtract(start)) / (length * length)));
                    Vec3 closest = start.lerp(end, t);
                    if (entity.position().multiply(1, 0, 1).distanceToSqr(closest.multiply(1, 0, 1)) > 1.5) continue;
                    if (entity instanceof Projectile) entity.discard();
                    if (tick % 10 == 0 && entity instanceof LivingEntity living && !world.ally(caster, living)
                            && damaged++ < values.getOrDefault("max_targets", 128.0)) {
                        context.execute(List.of(new SpellEffects.Action(VestigeMainMod.location("damage"), Map.of("amount", new SpellValue.Constant(values.getOrDefault("damage", 4.0))), Map.of()),
                                new SpellEffects.Action(VestigeMainMod.location("ignite"), Map.of("seconds", new SpellValue.Constant(3)), Map.of())), new SpellSubject.Entity(living.getUUID()));
                    }
                }
            }
            public void close(SpellRuntime.EndReason reason) { backing.close(reason); }
        });
    }
    private Optional<SpellWorld.ManifestationHandle> portal(SpellRuntime.Context context, LivingEntity caster, ServerLevel level, Vec3 end) {
        Optional<SpellSubject> stored = context.anchor(VestigeMainMod.location("first_anchor")); if (stored.isEmpty()) return Optional.empty();
        ServerLevel firstLevel = world.level(stored.get(), caster); Vec3 start = world.position(stored.get(), caster);
        if (firstLevel == null) return Optional.empty();
        if (start.distanceToSqr(end) < 4 && firstLevel == level) throw new IllegalStateException("Portal endpoints must be at least two blocks apart: " + start + " -> " + end);
        Entity first = marker(firstLevel, start, 20), second = marker(level, end, 20);
        if (first == null || second == null) { if (first != null) first.discard(); if (second != null) second.discard(); return Optional.empty(); }
        world.owners.put(first.getUUID(), caster.getUUID()); world.owners.put(second.getUUID(), caster.getUUID());
        var backing = entities(List.of(first, second)); Map<UUID, Long> cooldowns = new HashMap<>();
        return Optional.of(new SpellWorld.ManifestationHandle() {
            public SpellSubject subject() { return backing.subject(); }
            public boolean alive() { return first.isAlive() && second.isAlive(); }
            public void tick() { transfer(firstLevel, start, level, end); transfer(level, end, firstLevel, start); }
            private void transfer(ServerLevel from, Vec3 point, ServerLevel to, Vec3 exit) {
                from.sendParticles(ParticleTypes.PORTAL, point.x, point.y + 1, point.z, 8, 0.5, 1, 0.5, 0.1);
                long now = world.server.getTickCount();
                for (Entity entity : from.getEntities((Entity) null, new AABB(point, point).inflate(0.6, 1.5, 0.6), e -> e != first && e != second && !e.isPassenger())) {
                    if (cooldowns.getOrDefault(entity.getUUID(), 0L) > now) continue;
                    cooldowns.put(entity.getUUID(), now + 30);
                    if (entity instanceof ServerPlayer player) player.teleportTo(to, exit.x, exit.y + 0.1, exit.z, Set.of(), player.getYRot(), player.getXRot());
                    else if (from == to) entity.teleportTo(exit.x, exit.y + 0.1, exit.z);
                    else entity.changeDimension(new net.minecraft.world.level.portal.DimensionTransition(to, exit.add(0, 0.1, 0), entity.getDeltaMovement(), entity.getYRot(), entity.getXRot(), net.minecraft.world.level.portal.DimensionTransition.DO_NOTHING));
                }
                cooldowns.entrySet().removeIf(e -> e.getValue() < now - 100);
            }
            public void close(SpellRuntime.EndReason reason) { backing.close(reason); }
        });
    }
    private ArmorStand marker(ServerLevel level, Vec3 position, double health) {
        ArmorStand stand = SpellEntities.ANCHOR.get().create(level); if (stand == null) return null;
        stand.setPos(position); stand.setInvisible(true); stand.setNoGravity(true);
        stand.setHealth((float) Math.min(20, Math.max(1, health)));
        return level.addFreshEntity(stand) ? stand : null;
    }
    private SpellWorld.ManifestationHandle entities(List<Entity> entities) {
        return new SpellWorld.ManifestationHandle() {
            public SpellSubject subject() { return new SpellSubject.Entity(entities.getFirst().getUUID()); }
            public boolean alive() { return entities.stream().anyMatch(e -> e.isAlive() && !e.isRemoved()); }
            public void close(SpellRuntime.EndReason reason) {
                for (Entity entity : entities) { world.causedEntities.remove(entity.getUUID()); world.owners.remove(entity.getUUID()); entity.discard(); }
            }
        };
    }
    private static BlockPos findSpawn(ServerLevel level, LivingEntity caster, Mob mob, Vec3 point, int index) {
        BlockPos origin = BlockPos.containing(point);
        for (int radius = 1; radius <= 4; radius++) for (int step = 0; step < 8 * radius; step++) {
            double angle = (step + index) * Math.PI * 2 / (8 * radius);
            int x = origin.getX() + (int) Math.round(Math.cos(angle) * radius), z = origin.getZ() + (int) Math.round(Math.sin(angle) * radius);
            for (int dy = 2; dy >= -3; dy--) {
                BlockPos pos = new BlockPos(x, origin.getY() + dy, z);
                if (!level.hasChunkAt(pos) || !level.getBlockState(pos.below()).isCollisionShapeFullBlock(level, pos.below())) continue;
                mob.setPos(x + 0.5, pos.getY(), z + 0.5);
                if (mob.getBoundingBox().intersects(caster.getBoundingBox()) || !level.noCollision(mob)) continue;
                var sight = level.clip(new net.minecraft.world.level.ClipContext(caster.getEyePosition(), mob.getEyePosition(), net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, caster));
                if (sight.getType() == HitResult.Type.MISS) return pos;
            }
        }
        return null;
    }
    private static void particles(ServerLevel level, Vec3 position, ResourceLocation id, double radius) {
        var type = BuiltInRegistries.PARTICLE_TYPE.getOptional(id).orElse(ParticleTypes.ENCHANT);
        if (type instanceof SimpleParticleType particle) level.sendParticles(particle, position.x, position.y + 1, position.z, 5, radius * 0.3, 0.5, radius * 0.3, 0.01);
    }
    private static double bounded(Map<String, Double> values, String key, double fallback, double min, double max) {
        double value = values.getOrDefault(key, fallback);
        if (!Double.isFinite(value) || value < min || value > max) throw new IllegalArgumentException("Invalid manifestation " + key);
        return value;
    }
    private static int integer(Map<String, Double> values, String key, int fallback, int max) { return (int) Math.max(0, Math.min(max, Math.floor(values.getOrDefault(key, (double) fallback)))); }
}
