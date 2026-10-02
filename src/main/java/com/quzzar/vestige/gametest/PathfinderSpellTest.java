package com.quzzar.vestige.gametest;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.definition.SpellTriggerTypes;
import com.quzzar.vestige.magic.runtime.*;
import com.quzzar.vestige.magic.world.NativeMagic;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.*;
import java.util.*;

@GameTestHolder(VestigeMainMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PathfinderSpellTest {
    private PathfinderSpellTest() { }

    @GameTest(template="empty_3x3x3", batch="pathfinder_armament", timeoutTicks=140)
    public static void spiritualArmamentStrikesItsStoredTargetRatherThanItsBacking(GameTestHelper helper) {
        var caster=caster(helper,0); var victim=helper.spawnWithNoFreeWill(EntityType.VILLAGER,new BlockPos(1,1,2));
        victim.setNoAi(true); victim.setNoGravity(true);
        victim.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(100); victim.setHealth(100);
        var cast=cast(helper,caster,"spiritual_armament");
        helper.runAfterDelay(125,()-> {
            successful(helper,cast); require(helper,victim.getHealth()==85,"Spirit strikes hit backing or miscounted pulses: "+victim.getHealth());
            require(helper,NativeMagic.session(helper.getLevel().getServer()).runtime().manifestations(cast.id()).isEmpty(),"Spirit backing survived expiry");
            cleanup(helper,caster); helper.succeed();
        });
    }

    @GameTest(template="empty_3x3x3", batch="pathfinder_typed_magic")
    public static void typedIgnitionIsFireAndDoesNotTriggerOrdinaryAttackEcho(GameTestHelper helper) {
        var caster=caster(helper,0); var victim=helper.spawnWithNoFreeWill(EntityType.VILLAGER,new BlockPos(1,1,2));
        successful(helper,cast(helper,caster,"echoing_strikes",false));
        successful(helper,cast(helper,victim,"resist_energy"));
        successful(helper,cast(helper,caster,"ignition"));
        require(helper,victim.getHealth()==17,"Typed spell did not halve as fire or triggered an ordinary-attack echo: "+victim.getHealth());
        victim.clearFire(); cleanup(helper,caster); cleanup(helper,victim); helper.succeed();
    }

    @GameTest(template="empty_3x3x3", batch="pathfinder_retaliation", timeoutTicks=40)
    public static void fireShieldRetaliatesAgainstAttackerRatherThanDefender(GameTestHelper helper) {
        var caster=caster(helper,0); var attacker=helper.spawnWithNoFreeWill(EntityType.VILLAGER,new BlockPos(1,1,2));
        var cast=cast(helper,caster,"fire_shield");
        helper.runAfterDelay(15,()-> {
            successful(helper,cast); caster.hurt(attacker.damageSources().mobAttack(attacker),4);
            require(helper,caster.getHealth()==19,"Fire shield did not block three HP or damaged its defender: "+caster.getHealth());
            require(helper,attacker.getHealth()==18,"Fire shield did not deal two HP to attacker: "+attacker.getHealth());
            cleanup(helper,caster); helper.succeed();
        });
    }

    @GameTest(template="empty_3x3x3", batch="pathfinder_arc")
    public static void electricArcHitsTwoDistinctTargetsAtMost(GameTestHelper helper) {
        var caster=caster(helper,0); var first=helper.spawnWithNoFreeWill(EntityType.VILLAGER,new BlockPos(1,1,2));
        var second=helper.spawnWithNoFreeWill(EntityType.VILLAGER,new BlockPos(2,1,2));
        var third=helper.spawnWithNoFreeWill(EntityType.VILLAGER,new BlockPos(0,1,2));
        var cast=cast(helper,caster,"electric_arc"); successful(helper,cast);
        require(helper,first.getHealth()==16,"Initial arc target did not take four HP");
        require(helper,List.of(first,second,third).stream().filter(e->e.getHealth()==16).count()==2,"Arc did not strike exactly two distinct targets");
        require(helper,List.of(first,second,third).stream().noneMatch(e->e.getHealth()<16),"Arc struck one target twice");
        var cue=NativeMagic.session(helper.getLevel().getServer()).world().visualCues().stream()
                .filter(v->v.points().getFirst().entityUuid().equals(caster.getUUID())).findFirst().orElseThrow();
        require(helper,cue.points().size()==3 && !cue.follow(),"Arc visual did not preserve the two resolved jumps");
        require(helper,cue.points().get(1).entityUuid().equals(first.getUUID()),"Arc visual selected a different first target");
        require(helper,List.of(first,second,third).stream().filter(e->e.getHealth()==16).allMatch(e->cue.points().stream().anyMatch(p->p.entityUuid().equals(e.getUUID()))),"Arc visual differs from damaged targets");
        cleanup(helper,caster); helper.succeed();
    }

    @GameTest(template="empty_3x3x3", batch="visual_ward")
    public static void wardPresentationEndsOnConsumptionReplacementAndDispel(GameTestHelper helper) {
        var caster=caster(helper,0); var world=NativeMagic.session(helper.getLevel().getServer()).world();
        successful(helper,cast(helper,caster,"shield"));
        var first=world.visualCues().stream().filter(v->v.points().getFirst().entityUuid().equals(caster.getUUID())).toList();
        require(helper,first.size()==1 && first.getFirst().follow(),"Shield did not create one attached cue");
        caster.hurt(caster.damageSources().generic(),4);
        require(helper,caster.getHealth()==19,"Presentation changed shield mitigation");
        require(helper,world.visualCues().stream().noneMatch(v->v.id().equals(first.getFirst().id())),"Consumed shield retained its visual lease");
        successful(helper,cast(helper,caster,"shield")); successful(helper,cast(helper,caster,"shield"));
        require(helper,world.visualCues().stream().filter(v->v.points().getFirst().entityUuid().equals(caster.getUUID())).count()==1,"Replacement leaked a shield cue");
        NativeMagic.session(helper.getLevel().getServer()).runtime().dispelActor(caster.getUUID());
        require(helper,world.visualCues().stream().noneMatch(v->v.points().getFirst().entityUuid().equals(caster.getUUID())),"Dispel retained shield cue");
        cleanup(helper,caster); helper.succeed();
    }

    @GameTest(template="empty_3x3x3", batch="visual_projectile", timeoutTicks=35)
    public static void fireballPresentationUsesResolvedAreaAndClosesDeliveryAfterImpact(GameTestHelper helper) {
        var caster=caster(helper,0); var victim=helper.spawnWithNoFreeWill(EntityType.VILLAGER,new BlockPos(1,1,2));
        victim.setNoAi(true); victim.setNoGravity(true);
        victim.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(100); victim.setHealth(100);
        var base=NativeMagic.spells().spells().get(VestigeMainMod.location("pf2_fireball"));
        var immediate=new com.quzzar.vestige.magic.definition.SpellDefinition(base.id(),base.rarity(),base.traditions(),base.traits(),List.of(),base.triggers(),base.effects(),base.modes(),base.source());
        var session=NativeMagic.session(helper.getLevel().getServer());
        var cast=session.runtime().cast(immediate,SpellEvent.of(SpellTriggerTypes.INTERACT,caster.getUUID(),null),
                List.of(new com.quzzar.vestige.magic.definition.TraitModifier(VestigeMainMod.location("area"),com.quzzar.vestige.magic.definition.TraitModifier.Operation.MULTIPLY,1.5)),true);
        successful(helper,cast);
        var projectile=helper.getLevel().getEntitiesOfClass(com.quzzar.vestige.magic.world.SpellProjectile.class,caster.getBoundingBox().inflate(3),e->e.getOwner()==caster).getFirst();
        var cue=session.world().visualCues().stream().filter(v->v.points().getFirst().entityUuid().equals(projectile.getUUID())).findFirst().orElseThrow();
        require(helper,projectile.hasVisual() && cue.follow(),"Fireball did not acquire a procedural body");
        helper.runAfterDelay(5,()-> {
            require(helper,projectile.isRemoved(),"Fireball did not terminate after contact");
            require(helper,session.world().visualCues().stream().noneMatch(v->v.id().equals(cue.id())),"Projectile cue survived contact");
            require(helper,session.world().visualCues().stream().anyMatch(v->!v.follow() && Math.abs(v.visual().radius()-4.5)<.001 && v.points().getFirst().entityUuid().equals(victim.getUUID())),"Blast visual did not use resolved area radius");
            require(helper,victim.getHealth()<=86 && victim.getHealth()>80,"Presentation changed the authored fireball outcome");
            cleanup(helper,caster); helper.succeed();
        });
    }

    @GameTest(template="empty_3x3x3", batch="pathfinder_vitality")
    public static void vitalityAndVoidRespectUndeadEligibility(GameTestHelper helper) {
        var caster=caster(helper,0); var undead=helper.spawnWithNoFreeWill(EntityType.SKELETON,new BlockPos(1,1,2));
        successful(helper,cast(helper,caster,"vitality_lash"));
        require(helper,undead.getHealth()==13,"Vitality did not damage undead for seven HP");
        successful(helper,cast(helper,caster,"void_warp")); require(helper,undead.getHealth()==13,"Void harmed undead"); undead.discard();
        var living=helper.spawnWithNoFreeWill(EntityType.VILLAGER,new BlockPos(1,1,2));
        successful(helper,cast(helper,caster,"vitality_lash")); require(helper,living.getHealth()==20,"Vitality harmed living creature");
        successful(helper,cast(helper,caster,"void_warp")); require(helper,living.getHealth()==15,"Void did not damage living creature for five HP");
        cleanup(helper,caster); helper.succeed();
    }

    @GameTest(template="empty_3x3x3", batch="pathfinder_shield")
    public static void shieldBlocksExactlyOneLimitedHit(GameTestHelper helper) {
        var caster=caster(helper,1); successful(helper,cast(helper,caster,"shield"));
        caster.hurt(caster.damageSources().generic(),6); require(helper,caster.getHealth()==17,"Shield did not reduce first hit by three HP");
        caster.invulnerableTime=0; caster.hurt(caster.damageSources().generic(),6);
        require(helper,caster.getHealth()==11,"Shield prevented more than one hit"); cleanup(helper,caster); helper.succeed();
    }

    @GameTest(template="empty_3x3x3", batch="pathfinder_resistance")
    public static void fireResistanceIgnoresOrdinaryHitsAndHalvesTaggedFire(GameTestHelper helper) {
        var caster=caster(helper,1); successful(helper,cast(helper,caster,"resist_energy"));
        caster.hurt(caster.damageSources().generic(),6); require(helper,caster.getHealth()==14,"Fire ward reduced ordinary damage");
        caster.invulnerableTime=0; caster.hurt(caster.damageSources().inFire(),6);
        require(helper,caster.getHealth()==11,"Fire ward did not halve fire damage"); cleanup(helper,caster); helper.succeed();
    }

    @GameTest(template="empty_3x3x3", batch="pathfinder_regeneration", timeoutTicks=210)
    public static void regenerationHasDelayedPulsesAndEndsOnFire(GameTestHelper helper) {
        var caster=caster(helper,1); caster.setHealth(5); var cast=cast(helper,caster,"regenerate");
        helper.runAfterDelay(40,()-> {
            successful(helper,cast); require(helper,caster.getHealth()==6.5f,"Expected one delayed healing pulse");
            caster.invulnerableTime=0; caster.hurt(caster.damageSources().inFire(),2);
            helper.runAfterDelay(150,()-> {
                require(helper,caster.getHealth()==4.5f,"Regeneration continued after fire damage");
                require(helper,NativeMagic.session(helper.getLevel().getServer()).runtime().manifestations(cast.id()).isEmpty(),"Fire retained regeneration backing");
                cleanup(helper,caster); helper.succeed();
            });
        });
    }

    @GameTest(template="empty_3x3x3", batch="pathfinder_control", timeoutTicks=200)
    public static void bindUndeadRestoresPriorTargetAndDoesNotDespawnVictim(GameTestHelper helper) {
        var caster=caster(helper,0); var victim=helper.spawnWithNoFreeWill(EntityType.SKELETON,new BlockPos(1,1,2)); victim.setTarget(caster);
        var cast=cast(helper,caster,"bind_undead"); var world=NativeMagic.session(helper.getLevel().getServer()).world();
        helper.runAfterDelay(20,()-> { successful(helper,cast); require(helper,world.isOwnedBy(victim,caster.getUUID()),"Bind did not claim existing undead"); });
        helper.runAfterDelay(185,()-> {
            require(helper,!world.isOwnedBy(victim,caster.getUUID()),"Control survived expiry");
            require(helper,!victim.isRemoved() && victim.getTarget()==caster,"Expiry did not restore living body and previous target");
            cleanup(helper,caster); helper.succeed();
        });
    }

    @GameTest(template="empty_3x3x3", batch="pathfinder_dispel", timeoutTicks=55)
    public static void dispellingControlledUndeadReleasesRatherThanBanishes(GameTestHelper helper) {
        var caster=caster(helper,0); var victim=helper.spawnWithNoFreeWill(EntityType.SKELETON,new BlockPos(1,1,2));
        var cast=cast(helper,caster,"bind_undead");
        helper.runAfterDelay(20,()-> {
            successful(helper,cast); successful(helper,cast(helper,caster,"counterspell",false));
            var world=NativeMagic.session(helper.getLevel().getServer()).world();
            require(helper,!victim.isRemoved() && !world.isOwnedBy(victim,caster.getUUID()),"Dispel banished a controlled existing mob");
            cleanup(helper,caster); helper.succeed();
        });
    }

    @GameTest(template="empty_3x3x3", batch="pathfinder_control_overlap", timeoutTicks=230)
    public static void overlappingControlLeasesPreserveLatestCasterUntilItsExpiry(GameTestHelper helper) {
        var first=caster(helper,0); var victim=helper.spawnWithNoFreeWill(EntityType.SKELETON,new BlockPos(1,1,2));
        var firstCast=cast(helper,first,"bind_undead"); var world=NativeMagic.session(helper.getLevel().getServer()).world();
        helper.runAfterDelay(20,()-> {
            successful(helper,firstCast); var second=caster(helper,1); var secondCast=cast(helper,second,"bind_undead");
            helper.runAfterDelay(20,()-> { successful(helper,secondCast); require(helper,world.isOwnedBy(victim,second.getUUID()),"Latest caster did not claim control"); });
            helper.runAfterDelay(160,()-> { require(helper,world.isOwnedBy(victim,second.getUUID()),"Older expiry erased newer control"); });
            helper.runAfterDelay(185,()-> {
                require(helper,!world.isOwnedBy(victim,first.getUUID()) && !world.isOwnedBy(victim,second.getUUID()) && !victim.isRemoved(),"Final release retained ownership or discarded body");
                cleanup(helper,first); cleanup(helper,second); helper.succeed();
            });
        });
    }

    @GameTest(template="empty_3x3x3", batch="pathfinder_life", timeoutTicks=165)
    public static void lifeFieldHealsAllLivingIncludingCasterAndHurtsUndead(GameTestHelper helper) {
        var caster=caster(helper,1); caster.setHealth(4); caster.setXRot(90);
        var living=helper.spawnWithNoFreeWill(EntityType.VILLAGER,new BlockPos(0,1,1)); living.setHealth(10);
        var undead=helper.spawnWithNoFreeWill(EntityType.SKELETON,new BlockPos(2,1,1));
        undead.setItemSlot(EquipmentSlot.HEAD,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.LEATHER_HELMET));
        living.setNoAi(true); living.setNoGravity(true); undead.setNoAi(true); undead.setNoGravity(true); caster.setNoGravity(true);
        var casterPoint=caster.position(); var livingPoint=living.position(); var undeadPoint=undead.position();
        helper.onEachTick(()-> { caster.setPos(casterPoint); living.setPos(livingPoint); undead.setPos(undeadPoint); });
        var cast=cast(helper,caster,"field_of_life");
        helper.runAfterDelay(145,()-> {
            successful(helper,cast); require(helper,caster.getHealth()==10,"Life field omitted caster or miscounted pulses: "+caster.getHealth()+", last damage "+caster.getLastDamageSource());
            require(helper,living.getHealth()==16,"Life field did not heal other living creature six HP: "+living.getHealth());
            require(helper,undead.getHealth()==8,"Life field did not damage undead twelve HP: "+undead.getHealth());
            cleanup(helper,caster); helper.succeed();
        });
    }

    @GameTest(template="empty_3x3x3", batch="pathfinder_push", timeoutTicks=35)
    public static void hydraulicPushDamagesAndDisplacesItsTarget(GameTestHelper helper) {
        var caster=caster(helper,0); var victim=helper.spawnWithNoFreeWill(EntityType.VILLAGER,new BlockPos(1,1,2)); var origin=victim.position();
        var cast=cast(helper,caster,"hydraulic_push");
        helper.runAfterDelay(15,()-> {
            successful(helper,cast); require(helper,victim.getHealth()==13,"Hydraulic push did not deal seven HP");
            require(helper,victim.position().distanceToSqr(origin)>.25,"Hydraulic push did not move target");
            cleanup(helper,caster); helper.succeed();
        });
    }

    private static Villager caster(GameTestHelper helper,int z) {
        for(int x=0;x<3;x++)for(int dz=0;dz<3;dz++)helper.setBlock(new BlockPos(x,0,dz),Blocks.STONE);
        var caster=helper.spawnWithNoFreeWill(EntityType.VILLAGER,new BlockPos(1,1,z)); caster.setNoAi(true); caster.setYRot(0); caster.setXRot(0);
        NativeMagic.session(helper.getLevel().getServer()).world().registerActor(caster); return caster;
    }
    private static SpellRuntime.Cast cast(GameTestHelper helper,LivingEntity caster,String name) { return cast(helper,caster,name,true); }
    private static SpellRuntime.Cast cast(GameTestHelper helper,LivingEntity caster,String name,boolean prefix) {
        var spell=NativeMagic.spells().spells().get(VestigeMainMod.location((prefix?"pf2_":"")+name)); require(helper,spell!=null,"Missing spell "+name);
        return NativeMagic.session(helper.getLevel().getServer()).runtime().cast(spell,SpellEvent.of(SpellTriggerTypes.INTERACT,caster.getUUID(),null),List.of(),true,Optional.empty(),true);
    }
    private static void successful(GameTestHelper helper,SpellRuntime.Cast cast) { require(helper,cast.failure().isEmpty() && cast.status()==SpellRuntime.Status.COMPLETED,"Cast failed: "+cast.status()+cast.failure()); }
    private static void require(GameTestHelper helper,boolean condition,String message) { if(!condition)helper.fail(message); }
    private static void cleanup(GameTestHelper helper,LivingEntity caster) {
        var runtime=NativeMagic.session(helper.getLevel().getServer()).runtime(); runtime.interruptActor(caster.getUUID()); runtime.dispelActor(caster.getUUID()); caster.discard();
    }
}
