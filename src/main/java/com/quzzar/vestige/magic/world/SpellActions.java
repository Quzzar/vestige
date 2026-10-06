package com.quzzar.vestige.magic.world;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.effect.SpellEffects;
import com.quzzar.vestige.magic.runtime.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import java.util.*;

/** Small world operations. Spells choose their order, targets, values, and lifecycle in data. */
final class SpellActions {
    private final MinecraftSpellWorld world;
    private final Map<UUID, List<Runnable>> undo = new HashMap<>();
    private final Map<UUID, Control> controls = new HashMap<>();
    private static final class Control {
        final Mob mob;
        final LivingEntity previousTarget;
        final LinkedHashMap<UUID, UUID> leases = new LinkedHashMap<>();
        Control(Mob mob) { this.mob = mob; previousTarget = mob.getTarget(); }
    }
    SpellActions(MinecraftSpellWorld world) { this.world = world; }
    void cleanup(UUID cast) { var actions = undo.remove(cast); if (actions != null) actions.forEach(Runnable::run); }
    void close() { List.copyOf(undo.keySet()).forEach(this::cleanup); }
    private void own(SpellRuntime.Context context, Runnable action) { undo.computeIfAbsent(context.castId(), ignored -> new ArrayList<>()).add(action); }

    boolean execute(SpellEffects.Action action, SpellRuntime.Context context) {
        LivingEntity caster = world.actor(context); if (caster == null) return false;
        Entity target = world.target(context); LivingEntity living = target instanceof LivingEntity e ? e : null;
        ServerLevel level = world.level(context.target(), caster); if (level == null) return false;
        Vec3 point = world.position(context.target(), caster);
        switch (action.type().getPath()) {
            case "transpose", "create_water", "shape_stone", "gather_items", "utterance" -> {
                return world.features.execute(action, context);
            }
            case "random_teleport", "dwell_heal", "detect_magic", "inspect_item", "reveal_hidden", "reflect_projectiles" -> {
                return SpellUtilityActions.execute(world, action, context, caster, living, level);
            }
            case "damage", "weapon_damage" -> {
                if (living == null) { context.setNumber(VestigeMainMod.location("last_damage"), 0); return true; }
                double amount = action.values().containsKey("amount") ? context.number(action.values().get("amount")) : 0;
                if (action.type().getPath().equals("weapon_damage") && caster.getAttribute(Attributes.ATTACK_DAMAGE) != null) amount += caster.getAttributeValue(Attributes.ATTACK_DAMAGE) * value(action, context, "weapon_fraction", 1);
                amount=context.amount(amount);
                if (!finiteAmount(amount)) return false;
                int maxHits = (int) value(action, context, "max_hits_per_target", 0);
                if (maxHits > 0 && !context.claimHit(living.getUUID(), identifier(action, "hit_group", "vestige:damage"), maxHits)) {
                    context.setNumber(VestigeMainMod.location("last_damage"), 0);
                    return true;
                }
                float before = living.getHealth();
                if (value(action, context, "ignore_invulnerability", 0) > 0) living.invulnerableTime = 0;
                DamageSource source = caster.damageSources().indirectMagic(caster, caster);
                if (action.identifiers().containsKey("damage_type")) {
                    var type = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                            .getHolder(net.minecraft.resources.ResourceKey.create(Registries.DAMAGE_TYPE, action.identifiers().get("damage_type")));
                    if (type.isEmpty()) return false;
                    source = new DamageSource(type.get(), caster, caster);
                }
                living.hurt(source, (float) amount);
                context.setNumber(VestigeMainMod.location("last_damage"), Math.max(0, before - living.getHealth()));
                level.sendParticles(net.minecraft.core.particles.ParticleTypes.ENCHANTED_HIT, point.x, point.y + 1, point.z, 8, .3, .4, .3, .1);
            }
            case "heal" -> {
                context.setNumber(VestigeMainMod.location("last_heal"),0);
                if (living == null) return false; double amount = value(action, context, "amount", 0); if (!finiteAmount(amount)) return false;
                float before=living.getHealth(); living.heal((float) amount);
                context.setNumber(VestigeMainMod.location("last_heal"),Math.max(0,living.getHealth()-before));
            }
            case "leech" -> {
                double dealt = context.value(VestigeMainMod.location("last_damage")).filter(v -> v instanceof com.quzzar.vestige.magic.condition.ConditionValue.Decimal)
                        .map(v -> ((com.quzzar.vestige.magic.condition.ConditionValue.Decimal) v).value()).orElse(0.0);
                caster.heal((float) context.amount(Math.min(value(action,context,"maximum",1000), dealt * value(action, context, "fraction", 0.25))));
            }
            case "status" -> {
                if (living == null) return true;
                var effect = BuiltInRegistries.MOB_EFFECT.getHolder(identifier(action, "effect", "minecraft:slowness")); if (effect.isEmpty()) return false;
                living.addEffect(new MobEffectInstance(effect.get(), ticks(action, context, "duration", 100), (int) Math.max(0, Math.min(10, value(action, context, "amplifier", 0)))));
            }
            case "remove_status" -> {
                if (living == null) return false;
                BuiltInRegistries.MOB_EFFECT.getHolder(identifier(action, "effect", "minecraft:slowness")).ifPresent(living::removeEffect);
            }
            case "cleanse" -> {
                if (living == null) return false;
                List.copyOf(living.getActiveEffects()).stream().filter(e -> !e.getEffect().value().isBeneficial()).forEach(e -> living.removeEffect(e.getEffect()));
            }
            case "control" -> {
                if (!(target instanceof Mob mob) || world.owners.containsKey(mob.getUUID()) && !controls.containsKey(mob.getUUID())) return false;
                Control control = controls.computeIfAbsent(mob.getUUID(), ignored -> new Control(mob));
                control.leases.remove(context.castId()); control.leases.put(context.castId(), caster.getUUID());
                world.owners.put(mob.getUUID(), caster.getUUID()); mob.setTarget(null);
                own(context, () -> releaseControl(mob.getUUID(), context.castId()));
            }
            case "ignite" -> { if (target != null) target.igniteForSeconds((float) value(action, context, "seconds", 3)); }
            case "freeze" -> { if (target != null) target.setTicksFrozen((int) Math.min(400, target.getTicksFrozen() + value(action, context, "ticks", 140))); }
            case "knockback", "launch", "pull", "dash" -> {
                Entity moved = action.type().getPath().equals("dash") ? caster : target; if (moved == null) return false;
                Vec3 direction = switch (action.type().getPath()) {
                    case "pull" -> context.origin().map(s -> world.position(s, caster)).orElse(caster.position()).subtract(moved.position()).normalize();
                    case "dash" -> caster.getLookAngle().multiply(1, 0, 1).normalize();
                    default -> moved.position().subtract(caster.position()).multiply(1, 0, 1).normalize();
                };
                moved.setDeltaMovement(moved.getDeltaMovement().add(direction.scale(value(action, context, "strength", 0.5))).add(0, value(action, context, "up", action.type().getPath().equals("launch") ? 1 : 0.15), 0));
                moved.hurtMarked = true;
            }
            case "teleport" -> {
                Vec3 destination = living != null && living != caster ? living.position().subtract(living.getLookAngle().multiply(1, 0, 1).normalize().scale(2)) : point;
                if (!teleport(caster, level, destination, value(action, context, "grounded", 0) > 0)) return false;
            }
            case "recall" -> {
                ServerLevel destination = world.server.overworld(); BlockPos pos = destination.getSharedSpawnPos();
                if (caster instanceof ServerPlayer player && player.getRespawnPosition() != null && world.server.getLevel(player.getRespawnDimension()) != null) {
                    destination = world.server.getLevel(player.getRespawnDimension()); pos = player.getRespawnPosition();
                }
                if (!teleport(caster, destination, pos.getBottomCenter().add(0, 1, 0))) return false;
            }
            case "explode" -> {
                double radius = value(action, context, "radius", 3), amount = value(action, context, "amount", 8);
                if (radius < 0 || radius > 64 || !finiteAmount(amount)) return false;
                for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, new AABB(point, point).inflate(radius), e -> e.isAlive() && !world.ally(caster, e)).stream()
                        .filter(e -> e.position().distanceToSqr(point) <= radius * radius)
                        .sorted(Comparator.comparingDouble(e -> e.position().distanceToSqr(point)))
                        .limit((long) Math.max(0, value(action, context, "count", 128))).toList()) {
                    victim.hurt(caster.damageSources().indirectMagic(caster, caster), (float) amount);
                }
                level.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION, point.x, point.y + 0.5, point.z, 2, 0.3, 0.3, 0.3, 0);
            }
            case "equip" -> {
                if (living == null) return false;
                var item = BuiltInRegistries.ITEM.getOptional(identifier(action, "item", "minecraft:stone_sword")); if (item.isEmpty()) return false;
                living.setItemSlot(EquipmentSlot.valueOf(identifier(action, "slot", "vestige:mainhand").getPath().toUpperCase(Locale.ROOT)), new ItemStack(item.get()));
            }
            case "unlock" -> {
                var block = world.block(context.target(), caster); if (block == null || !caster.getUUID().toString().equals(block.getPersistentData().getString("vestige:lock_owner"))) return false;
                block.getPersistentData().remove("vestige:lock_owner"); block.setChanged();
            }
            case "dispel", "interrupt" -> {
                if (target == null) return false;
                var runtime = NativeMagic.session(world.server).runtime(); runtime.interruptActor(target.getUUID());
                if (action.type().getPath().equals("dispel")) {
                    runtime.dispelSubject(new SpellSubject.Entity(target.getUUID()));
                    if (living != null) List.copyOf(living.getActiveEffects()).forEach(e -> living.removeEffect(e.getEffect()));
                    if (controls.containsKey(target.getUUID())) releaseAllControl(target.getUUID());
                    else if (world.owners.containsKey(target.getUUID()) || world.causedEntities.containsKey(target.getUUID()) || target instanceof Projectile) {
                        world.owners.remove(target.getUUID()); world.causedEntities.remove(target.getUUID()); target.discard();
                    }
                }
            }
            case "break_block", "break_blocks" -> {
                int radius = action.type().getPath().equals("break_blocks") ? (int) Math.max(0, Math.min(3, value(action, context, "radius", 1))) : 0;
                int depth = (int) Math.max(1, Math.min(8, value(action, context, "depth", 1)));
                var facing = Direction.getNearest(caster.getLookAngle().x, caster.getLookAngle().y, caster.getLookAngle().z);
                BlockPos origin = BlockPos.containing(point); boolean broken = false;
                for (int d = 0; d < depth; d++) for (int a = -radius; a <= radius; a++) for (int b = -radius; b <= radius; b++) {
                    BlockPos offset = switch (facing.getAxis()) { case X -> new BlockPos(0, a, b); case Y -> new BlockPos(a, 0, b); case Z -> new BlockPos(a, b, 0); };
                    broken |= breakBlock(level, origin.relative(facing, d).offset(offset), caster, value(action, context, "hardness", 5));
                }
                if (!broken) return false;
            }
            case "replace_block" -> {
                BlockPos pos = BlockPos.containing(point); var old = level.getBlockState(pos);
                var replacement = BuiltInRegistries.BLOCK.getOptional(identifier(action, "block", "minecraft:ice"));
                if (replacement.isEmpty() || old.getDestroySpeed(level, pos) < 0 || !old.canBeReplaced()) return false;
                level.setBlock(pos, replacement.get().defaultBlockState(), 3);
                own(context, () -> { if (level.getBlockState(pos).is(replacement.get())) level.setBlock(pos, old, 3); });
            }
            case "aggro_clear" -> {
                for (Mob mob : level.getEntitiesOfClass(Mob.class, caster.getBoundingBox().inflate(value(action, context, "radius", 24)))) if (mob.getTarget() == caster) mob.setTarget(null);
            }
            case "aggro_decoy" -> {
                if (living == null) return false;
                for (Mob mob : level.getEntitiesOfClass(Mob.class, living.getBoundingBox().inflate(value(action, context, "radius", 12)))) if (!world.ally(caster, mob)) mob.setTarget(living);
            }
            case "aggro_convert" -> {
                if (!(living instanceof Sheep sheep)) return false;
                var colors = net.minecraft.world.item.DyeColor.values(); sheep.setColor(colors[caster.getRandom().nextInt(colors.length)]);
            }
            case "despawn" -> {
                if (target == null || !caster.getUUID().equals(world.owners.get(target.getUUID()))) return false;
                target.discard();
            }
            case "dismiss_manifestations" -> {
                var runtime = NativeMagic.session(world.server).runtime();
                runtime.manifestations(context.castId()).forEach(runtime::dispel);
            }
            case "ender_inventory" -> {
                if (!(caster instanceof ServerPlayer player)) return false;
                player.openMenu(new SimpleMenuProvider((id, inventory, p) -> ChestMenu.threeRows(id, inventory, player.getEnderChestInventory()), net.minecraft.network.chat.Component.translatable("container.enderchest")));
            }
            case "pocket_dimension" -> { if (!(caster instanceof ServerPlayer player) || !PrivateSpaces.visit(player)) return false; }
            case "grip" -> {
                if (target == null || target == caster) return false;
                Vec3 destination = caster.getEyePosition().add(caster.getLookAngle().scale(value(action, context, "distance", 6)));
                Vec3 force = destination.subtract(target.position()).scale(0.2); if (force.length() > 2) force = force.normalize().scale(2);
                target.setDeltaMovement(force); target.hurtMarked = true;
                if (target.horizontalCollision && living != null && context.claimHit(living.getUUID(), VestigeMainMod.location("grip"), (int) value(action, context, "max_hits_per_target", 3))) {
                    living.invulnerableTime = 0;
                    living.hurt(caster.damageSources().indirectMagic(caster, caster), (float) value(action, context, "impact_damage", 4));
                }
            }
            case "attribute", "grant_max_health" -> {
                if (living == null) return false;
                ResourceLocation name = identifier(action, "attribute", "minecraft:generic.max_health");
                var attribute = BuiltInRegistries.ATTRIBUTE.getHolder(name); if (attribute.isEmpty()) return false;
                var instance = living.getAttribute(attribute.get()); if (instance == null) return false;
                ResourceLocation modifier = VestigeMainMod.location("cast/" + context.castId() + "/" + name.getPath().replace('.', '/'));
                instance.removeModifier(modifier); instance.addTransientModifier(new AttributeModifier(modifier, value(action, context, "amount", 2), AttributeModifier.Operation.ADD_VALUE));
                own(context, () -> { instance.removeModifier(modifier); living.setHealth(Math.min(living.getHealth(), living.getMaxHealth())); });
            }
            case "remove_attribute" -> cleanup(context.castId());
            case "food_mana" -> {
                var recipient=living==null?caster:living;
                NativeMana.restore(recipient,value(action,context,"amount",20));
            }
            case "redirect_projectiles" -> {
                if (living == null) return false;
                for (Projectile projectile : level.getEntitiesOfClass(Projectile.class, living.getBoundingBox().inflate(value(action, context, "radius", 6)))) {
                    if (projectile.getOwner() != living) projectile.setDeltaMovement(living.getEyePosition().subtract(projectile.position()).normalize().scale(Math.max(0.3, projectile.getDeltaMovement().length())));
                }
            }
            case "fangs" -> {
                double damage = value(action, context, "amount", 6);
                Set<UUID> struck = new HashSet<>();
                for (int i = 0; i < (int) Math.min(32, value(action, context, "count", 1)); i++) {
                    double angle = i * Math.PI * 2 / Math.max(1, value(action, context, "count", 1));
                    Vec3 pos = value(action, context, "line", 0) > 0 ? caster.position().add(caster.getLookAngle().multiply(1, 0, 1).normalize().scale(1 + i * 1.25)) : point.add(Math.cos(angle) * value(action, context, "radius", 0), 0, Math.sin(angle) * value(action, context, "radius", 0));
                    level.sendParticles(net.minecraft.core.particles.ParticleTypes.CRIT, pos.x, pos.y + 0.5, pos.z, 15, 0.2, 0.6, 0.2, 0.05);
                    for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, new AABB(pos, pos).inflate(0.75, 1, 0.75))) {
                        if (!world.ally(caster, victim) && struck.size() < (int) value(action, context, "max_targets", 128)
                                && struck.add(victim.getUUID())) {
                            victim.invulnerableTime = 0;
                            victim.hurt(caster.damageSources().indirectMagic(caster, caster), (float) damage);
                        }
                    }
                }
            }
            case "flight" -> {
                if (!(caster instanceof ServerPlayer player)) return false;
                boolean mayfly = player.getAbilities().mayfly;
                player.getAbilities().mayfly = true; player.onUpdateAbilities();
                own(context, () -> { if (!player.isCreative() && !player.isSpectator()) { player.getAbilities().mayfly = mayfly; if (!mayfly) player.getAbilities().flying = false; player.onUpdateAbilities(); } });
            }
            case "end_flight" -> cleanup(context.castId());
            default -> { return false; }
        }
        return true;
    }
    private void releaseControl(UUID target, UUID cast) {
        Control control = controls.get(target); if (control == null) return;
        control.leases.remove(cast);
        if (control.leases.isEmpty()) releaseAllControl(target);
        else { world.owners.put(target, control.leases.lastEntry().getValue()); control.mob.setTarget(null); }
    }
    private void releaseAllControl(UUID target) {
        Control control = controls.remove(target); if (control == null) return;
        world.owners.remove(target);
        control.mob.getNavigation().stop();
        control.mob.setTarget(control.previousTarget != null && control.previousTarget.isAlive() ? control.previousTarget : null);
    }
    static double value(SpellEffects.Action action, SpellRuntime.Context context, String key, double fallback) { return action.values().containsKey(key) ? context.gameplayValue(key,action.values().get(key)) : fallback; }
    private static int ticks(SpellEffects.Action action, SpellRuntime.Context context, String key, int fallback) { return (int) Math.max(1, Math.min(240000, value(action, context, key, fallback))); }
    private static ResourceLocation identifier(SpellEffects.Action action, String key, String fallback) { return action.identifiers().getOrDefault(key, ResourceLocation.parse(fallback)); }
    private static boolean finiteAmount(double value) { return Double.isFinite(value) && value >= 0 && value <= Float.MAX_VALUE; }
    private static boolean breakBlock(ServerLevel level, BlockPos pos, LivingEntity caster, double maximumHardness) {
        if (!level.hasChunkAt(pos)) return false;
        var state = level.getBlockState(pos); double hardness = state.getDestroySpeed(level, pos);
        if (state.isAir() || hardness < 0 || hardness > maximumHardness) return false;
        if (caster instanceof Player player) {
            var event = new net.neoforged.neoforge.event.level.BlockEvent.BreakEvent(level, pos, state, player);
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(event); if (event.isCanceled() || !player.mayBuild()) return false;
        }
        var drops = Block.getDrops(state, level, pos, level.getBlockEntity(pos), caster, caster.getMainHandItem());
        level.destroyBlock(pos, false, caster); drops.forEach(stack -> Block.popResource(level, pos, stack)); return true;
    }
    private static boolean teleport(LivingEntity caster, ServerLevel level, Vec3 destination) {
        return teleport(caster, level, destination, false);
    }
    static boolean teleport(LivingEntity caster, ServerLevel level, Vec3 destination, boolean grounded) {
        if (Math.abs(destination.x) > 29999900 || Math.abs(destination.z) > 29999900) return false;
        for (int dy = 0; dy <= 3; dy++) {
            Vec3 point = destination.add(0, dy, 0); AABB box = caster.getBoundingBox().move(point.subtract(caster.position()));
            if (grounded) {
                BlockPos feet = BlockPos.containing(point), head = BlockPos.containing(point.add(0, caster.getBbHeight(), 0));
                if (!level.hasChunkAt(feet) || !level.hasChunkAt(head) || !level.getWorldBorder().isWithinBounds(feet)
                        || !level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), Direction.UP)
                        || !level.getFluidState(feet).isEmpty() || !level.getFluidState(head).isEmpty()) continue;
            }
            if (point.y < level.getMinBuildHeight() || point.y + caster.getBbHeight() >= level.getMaxBuildHeight() || !level.noCollision(caster, box)) continue;
            if (caster instanceof ServerPlayer player) player.teleportTo(level, point.x, point.y, point.z, Set.of(), player.getYRot(), player.getXRot());
            else if (caster.level() == level) caster.teleportTo(point.x, point.y, point.z);
            else return false;
            caster.fallDistance = 0; return true;
        }
        return false;
    }
}
