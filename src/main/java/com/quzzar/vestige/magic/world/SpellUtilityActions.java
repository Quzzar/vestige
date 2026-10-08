package com.quzzar.vestige.magic.world;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.condition.ConditionValue;
import com.quzzar.vestige.magic.effect.SpellEffects;
import com.quzzar.vestige.magic.runtime.SpellRuntime;
import com.quzzar.vestige.magic.runtime.CastObserver;
import com.quzzar.vestige.magic.presentation.SpellVisual;
import com.quzzar.vestige.magic.presentation.SpellVisualPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import net.neoforged.neoforge.network.PacketDistributor;

/** Finite movement, occupancy and inspection operations; no discovery or progression state. */
final class SpellUtilityActions {
    private SpellUtilityActions() { }
    static boolean execute(MinecraftSpellWorld world, SpellEffects.Action action, SpellRuntime.Context context,
                           LivingEntity caster, LivingEntity target, ServerLevel level) {
        return switch (action.type().getPath()) {
            case "random_teleport" -> {
                double radius = SpellActions.value(action, context, "radius", 4);
                if (radius < 1 || radius > 16) yield false;
                boolean moved = false;
                for (int attempt = 0; attempt < 16 && !moved; attempt++) {
                    double angle = caster.getRandom().nextDouble() * Math.PI * 2;
                    double distance = 1 + caster.getRandom().nextDouble() * (radius - 1);
                    Vec3 destination = caster.position().add(Math.cos(angle) * distance, -1, Math.sin(angle) * distance);
                    moved = SpellActions.teleport(caster, (ServerLevel) caster.level(), destination, true);
                }
                // Obstructed terrain skips this pulse without ending the surrounding buff.
                context.setNumber(VestigeMainMod.location("last_teleport"), moved ? 1 : 0);
                if (moved) context.resolved(CastObserver.Kind.UTILITY,1);
                yield true;
            }
            case "dwell_heal" -> {
                if (target == null) yield false;
                double amount = SpellActions.value(action, context, "amount", 0);
                int required = (int) SpellActions.value(action, context, "required_ticks", 60);
                int interval = (int) SpellActions.value(action, context, "interval", 5);
                if (amount < 0 || amount > 1000 || required < 1 || required > 2400 || interval < 1 || interval > 100) yield false;
                var startedKey = VestigeMainMod.location("dwell/" + target.getUUID() + "/started");
                var lastKey = VestigeMainMod.location("dwell/" + target.getUUID() + "/last");
                long now = level.getGameTime();
                double last = context.value(lastKey).filter(v -> v instanceof ConditionValue.Decimal)
                        .map(v -> ((ConditionValue.Decimal) v).value()).orElse(Double.NEGATIVE_INFINITY);
                if (now - last > interval) context.setNumber(startedKey, now);
                context.setNumber(lastKey, now);
                double started = ((ConditionValue.Decimal) context.value(startedKey).orElseThrow()).value();
                if (now - started >= required && context.claimHit(target.getUUID(), VestigeMainMod.location("dwell_heal"), 1)) {
                    float before=target.getHealth();target.heal((float)amount);
                    context.resolved(CastObserver.Kind.HEAL,Math.max(0,target.getHealth()-before));
                }
                yield true;
            }
            case "detect_magic" -> {
                double radius = SpellActions.value(action, context, "radius", 16);
                if (radius < 0 || radius > 64) yield false;
                boolean found = world.hasNativeMagic(caster, radius);
                context.setNumber(VestigeMainMod.location("magic_found"), found ? 1 : 0);
                aura(caster, caster.position().add(0,.1,0), found);
                yield true;
            }
            case "inspect_item" -> {
                boolean found = !caster.getMainHandItem().isEmpty() && caster.getMainHandItem().isEnchanted();
                context.setNumber(VestigeMainMod.location("magic_found"), found ? 1 : 0);
                aura(caster, caster.getEyePosition().add(caster.getLookAngle().scale(.75)), found);
                yield true;
            }
            case "reveal_hidden" -> {
                double radius=SpellActions.value(action,context,"radius",4);
                int duration=(int)SpellActions.value(action,context,"duration",80);
                int count=(int)SpellActions.value(action,context,"count",8);
                if(radius<1 || radius>16 || duration<20 || duration>200 || count<1 || count>8)yield false;
                Vec3 point=world.position(context.target(),caster);
                var nearby=level.getEntitiesOfClass(LivingEntity.class,new AABB(point,point).inflate(radius),
                        e->e.isAlive() && e.isInvisible() && e.position().distanceToSqr(point)<=radius*radius);
                nearby.sort(Comparator.<LivingEntity>comparingDouble(e->e.position().distanceToSqr(point)).thenComparing(LivingEntity::getUUID));
                for(var hidden:nearby)if(context.claimContact(hidden.getUUID(),VestigeMainMod.location("reveal_hidden"),1,count))
                    hidden.addEffect(new MobEffectInstance(MobEffects.GLOWING,duration,0),caster);
                yield true;
            }
            case "reflect_projectiles" -> {
                if(target==null)yield false;
                double radius=SpellActions.value(action,context,"radius",2);
                int count=(int)SpellActions.value(action,context,"count",2);
                if(radius<.5 || radius>4 || count<1 || count>4)yield false;
                Vec3 point=target.getBoundingBox().getCenter();
                var nearby=level.getEntitiesOfClass(AbstractArrow.class,target.getBoundingBox().inflate(radius),arrow->
                        (arrow.getType()==EntityType.ARROW || arrow.getType()==EntityType.SPECTRAL_ARROW || arrow.getType()==EntityType.TRIDENT)
                        && arrow.isAlive() && level.noCollision(arrow,arrow.getBoundingBox()) && arrow.position().distanceToSqr(point)<=radius*radius
                        && arrow.getDeltaMovement().lengthSqr()>.0001
                        && arrow.getDeltaMovement().dot(point.subtract(arrow.position()))>0
                        && (arrow.getOwner()==null || !world.ally(target,arrow.getOwner())));
                nearby.sort(Comparator.<AbstractArrow>comparingDouble(e->e.position().distanceToSqr(point)).thenComparing(AbstractArrow::getUUID));
                for(var arrow:nearby) {
                    var previous=world.causedEntities.getOrDefault(arrow.getUUID(),context.cause());
                    var returnedCause=previous.asSecondary().enter(new com.quzzar.vestige.magic.runtime.CausalChain.ActivationKey(context.spell().id(),VestigeMainMod.location("reflect_projectiles")));
                    if(returnedCause.isEmpty())continue;
                    if(!context.claimContact(arrow.getUUID(),VestigeMainMod.location("reflect_projectiles"),1,count))continue;
                    var shooter=arrow.getOwner();
                    Vec3 direction=shooter instanceof LivingEntity living && living.isAlive() && living.level()==level
                            ? living.getEyePosition().subtract(arrow.position()) : arrow.getDeltaMovement().reverse();
                    if(direction.lengthSqr()<.0001)direction=arrow.getDeltaMovement().reverse();
                    double speed=arrow.getDeltaMovement().length();
                    arrow.setOwner(target);arrow.setDeltaMovement(direction.normalize().scale(speed));arrow.hasImpulse=true;
                    world.returnedProjectile(arrow,returnedCause.get());
                }
                yield true;
            }
            default -> false;
        };
    }
    private static void aura(LivingEntity caster, Vec3 point, boolean found) {
        if (!(caster instanceof ServerPlayer player)) return;
        var visual=new SpellVisual.Resolved(20,.3f,List.of(new SpellVisual.Layer(
                SpellVisual.Shape.RING,found ? 0xf4e5ff : 0x60606c,found ? .8f : .3f,.02f,1)));
        PacketDistributor.sendToPlayer(player,new SpellVisualPayload(UUID.randomUUID(),player.level().dimension().location(),visual,
                List.of(new SpellVisualPayload.Point(point,-1,new UUID(0,0),0)),0,0,true,false,false));
    }
}
