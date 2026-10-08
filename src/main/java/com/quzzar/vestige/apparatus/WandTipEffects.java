package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.condition.*;
import com.quzzar.vestige.magic.definition.*;
import com.quzzar.vestige.magic.effect.SpellEffects;
import com.quzzar.vestige.magic.expression.SpellValue;
import com.quzzar.vestige.magic.runtime.*;
import com.quzzar.vestige.magic.world.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.*;
import java.util.*;

/** Cast-owned component behavior; all extra spell outcomes execute in the shared runtime as secondary plans. */
final class WandTipEffects implements CastObserver {
    private final WandTips.Tip tip;
    private final Player caster;
    private final TraitProfile source;
    private final Set<UUID> recipients=new HashSet<>();
    private boolean spent;
    private double lightning;
    private AttributeInstance stance;
    private ResourceLocation stanceId;
    WandTipEffects(WandTips.Tip tip,Player caster,TraitProfile source){this.tip=tip;this.caster=caster;this.source=source;}
    private double trait(SpellRuntime.Context context,String name){
        var id=VestigeMainMod.location(name);
        return Math.clamp(context.traits().rating(id)/Math.max(1,source.rating(id)),.1,4);
    }
    @Override public void preparing(SpellRuntime.Context context){
        if(tip!=WandTips.Tip.NETHERITE)return;
        stance=caster.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if(stance==null)return;
        stanceId=VestigeMainMod.location("preparation/"+context.castId());
        double resistance=Math.clamp(stance.getValue(),0,1);
        double portion=Math.min(.75,.5*trait(context,"metal"));
        stance.addTransientModifier(new AttributeModifier(stanceId,(1-resistance)*portion,AttributeModifier.Operation.ADD_VALUE));
    }
    private void clearStance(){if(stance!=null && stanceId!=null)stance.removeModifier(stanceId);stance=null;stanceId=null;}
    @Override public void activated(SpellRuntime.Context context){
        clearStance();
        if(tip==WandTips.Tip.ENDER_PEARL)context.emitSecondary(List.of(action("optional_backstep",Map.of("distance",Math.min(2,1.5*trait(context,"space"))),Map.of())),actor());
    }
    @Override public void ended(SpellRuntime.Status status){clearStance();}
    @Override public void resolved(Kind kind,double amount,SpellRuntime.Context context){
        if(context.cause().secondary())return;
        LivingEntity recipient=entity(context.target());
        switch(tip){
            case AMETHYST -> {
                if(spent || !hp(kind) || recipient==null || !valid(kind,recipient))return;
                spent=true;double pool=Math.min(2,amount*.2*trait(context,"sonic"));
                int delay=(int)Math.clamp(Math.round(15*trait(context,"time")),1,60);
                var effects=List.<SpellEffect>of(new SpellEffects.Delay(delay),validRecipient(kind,List.of(outcome(kind,pool))));
                context.emitSecondary(effects,context.target());
            }
            case DIAMOND -> {
                if(spent || !hp(kind) || recipient==null || (kind==Kind.HEAL ? !valid(kind,recipient) : ally(recipient)))return;
                spent=true;double radius=Math.min(4,2*trait(context,"area"));
                var nearby=nearby(recipient,radius,kind==Kind.HEAL).stream().filter(e -> e!=recipient).limit(3).toList();
                if(nearby.isEmpty())return;
                double pool=Math.min(4,amount*.2*trait(context,kind==Kind.HEAL ? "life" : "force"));
                for(var next:nearby)context.emitSecondary(List.of(outcome(kind,pool/nearby.size())),new SpellSubject.Entity(next.getUUID()));
            }
            case EMERALD -> {
                if(spent || context.paidMana()<=0)return;
                spent=true;NativeMana.restore(caster,Math.min(5,context.paidMana()*.1));
            }
            case COPPER -> {
                if(kind!=Kind.DAMAGE || recipient==null || !valid(kind,recipient) || lightning>=3 || recipients.size()>=3 || !recipients.add(recipient.getUUID()))return;
                double extra=Math.min(3-lightning,trait(context,"lightning"));lightning+=extra;
                context.emitSecondary(List.of(action("damage",Map.of("amount",extra,"ignore_invulnerability",1d,"quantize_amount",0d,"budget_maximum",35d),Map.of("damage_type",ResourceLocation.parse("minecraft:lightning_bolt"),"amount_budget",VestigeMainMod.location("equipment/electrical")))),context.target());
            }
            case IRON -> {
                if(recipient==null)return;
                List<LivingEntity> targets;
                if(kind==Kind.DAMAGE && valid(kind,recipient))targets=List.of(recipient);
                else if((kind==Kind.HEAL || kind==Kind.PROTECTION) && ally(recipient))targets=nearby(recipient,2,false);
                else return;
                var origin=kind==Kind.DAMAGE ? actor() : context.target();
                for(var enemy:targets){
                    if(recipients.size()>=3)break;
                    if(!recipients.add(enemy.getUUID()))continue;
                    var key=VestigeMainMod.location("equipment/repelling_origin");
                    context.execute(List.of(new SpellEffects.StoreTarget(key)),origin);
                    context.emitSecondary(List.of(action("knockback",Map.of("strength",Math.min(.7,.35*trait(context,"motion")),"up",0d,"respect_resistance",1d,"maximum_speed",1.5),Map.of("origin",key))),new SpellSubject.Entity(enemy.getUUID()));
                }
            }
            case GHAST_TEAR -> {
                if(spent)return;spent=true;
                var beneficiary=(kind==Kind.HEAL || kind==Kind.PROTECTION) && recipient!=null && ally(recipient) ? context.target() : actor();
                int interval=(int)Math.clamp(Math.round(40*trait(context,"time")),5,50);
                double perPulse=Math.min(1,trait(context,"life"));
                context.emitSecondary(List.of(new SpellEffects.Delay(interval),alive(List.of(outcome(Kind.HEAL,perPulse))),
                        new SpellEffects.Delay(interval),alive(List.of(outcome(Kind.HEAL,perPulse)))),beneficiary);
            }
            default -> { }
        }
    }
    private static boolean hp(Kind kind){return kind==Kind.DAMAGE || kind==Kind.HEAL;}
    private boolean valid(Kind kind,LivingEntity target){return target.isAlive() && (kind==Kind.HEAL ? ally(target) : !ally(target));}
    private boolean ally(Entity target){return target==caster || caster.isAlliedTo(target)
            || NativeMagic.session(caster.getServer()).world().isOwnedBy(target,caster.getUUID())
            || target instanceof OwnableEntity own && caster.getUUID().equals(own.getOwnerUUID());}
    private SpellSubject actor(){return new SpellSubject.Entity(caster.getUUID());}
    private LivingEntity entity(SpellSubject subject){
        if(!(subject instanceof SpellSubject.Entity e))return null;
        if(e.id().equals(caster.getUUID()))return caster;
        for(ServerLevel level:caster.getServer().getAllLevels())if(level.getEntity(e.id()) instanceof LivingEntity living)return living;
        return null;
    }
    private List<LivingEntity> nearby(LivingEntity center,double radius,boolean friendly){
        return center.level().getEntitiesOfClass(LivingEntity.class,center.getBoundingBox().inflate(radius),
                e -> e.isAlive() && e.distanceToSqr(center)<=radius*radius && ally(e)==friendly && center.hasLineOfSight(e)).stream()
                .sorted(Comparator.<LivingEntity>comparingDouble(e -> e.distanceToSqr(center)).thenComparing(Entity::getUUID)).toList();
    }
    private static SpellEffects.Branch validRecipient(Kind kind,List<SpellEffect> effects){
        return alive(List.of(new SpellEffects.Branch(
                BuiltInCondition.Compare.to(ConditionPaths.TARGET_ALLIED,BuiltInCondition.Comparison.EQUAL,new ConditionValue.Flag(kind==Kind.HEAL)),effects,List.of())));
    }
    private static SpellEffects.Branch alive(List<SpellEffect> effects){return new SpellEffects.Branch(
            BuiltInCondition.Compare.to(ConditionPaths.TARGET_ALIVE,BuiltInCondition.Comparison.EQUAL,new ConditionValue.Flag(true)),effects,List.of());}
    private static SpellEffects.Action outcome(Kind kind,double amount){return action(kind==Kind.HEAL ? "heal" : "damage",
            Map.of("amount",amount,"quantize_amount",0d,"ignore_invulnerability",1d),Map.of());}
    private static SpellEffects.Action action(String type,Map<String,Double> values,Map<String,ResourceLocation> identifiers){
        var expressions=new HashMap<String,SpellValue>();values.forEach((key,value)->expressions.put(key,new SpellValue.Constant(value)));
        return new SpellEffects.Action(VestigeMainMod.location(type),expressions,identifiers);
    }
}
