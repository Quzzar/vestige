package com.quzzar.vestige.gametest;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.definition.SpellTriggerTypes;
import com.quzzar.vestige.magic.runtime.*;
import com.quzzar.vestige.magic.world.NativeMagic;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.neoforged.neoforge.gametest.*;
import java.util.*;

@GameTestHolder(VestigeMainMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class NativeMagicTest {
    private NativeMagicTest() { }

    @GameTest(template="empty_3x3x3")
    public static void catalogLoadsWithoutLegacyRegistries(GameTestHelper helper) {
        var spells = NativeMagic.spells().spells();
        require(helper, spells.size() == 214, "Expected 110 Iron, 100 Pathfinder and four native examples, got " + spells.size());
        require(helper, spells.values().stream().filter(s -> s.source().isPresent()).count() == 210, "Source provenance missing");
        helper.succeed();
    }

    @GameTest(template="empty_3x3x3")
    public static void projectileDamage(GameTestHelper helper) {
        var caster = caster(helper, 0); var victim = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(1,1,2));
        caster.setXRot(10); var cast = cast(helper,caster,"force_arrow");
        successful(helper,cast);
        var world=NativeMagic.session(helper.getLevel().getServer()).world();
        helper.runAfterDelay(8, () -> {
            require(helper, Math.abs(victim.getHealth()-3)<.01,"Expected seven projectile damage, got " + victim.getHealth());
            var impact=world.visualCues().stream().filter(v->!v.follow() && v.points().getFirst().position().distanceTo(victim.position())<2).findFirst();
            require(helper,impact.isPresent(),"Default projectile damage omitted its contact burst");
            require(helper,impact.get().visual().duration()==24 && impact.get().visual().radius()>=.9f,"Default impact was too brief or small");
        });
        helper.runAfterDelay(40, () -> {
            require(helper,world.visualCues().stream().noneMatch(v->!v.follow() && v.points().getFirst().position().distanceTo(victim.position())<2),"Default projectile contact burst leaked after expiry");
            cleanup(helper,caster); helper.succeed();
        });
    }

    @GameTest(template="empty_3x3x3", timeoutTicks=90)
    public static void nativeHealing(GameTestHelper helper) {
        var caster = caster(helper,1); caster.setHealth(10); var cast = cast(helper,caster,"heal");
        helper.runAfterDelay(70, () -> { successful(helper,cast); require(helper, Math.abs(caster.getHealth()-15)<.01,"Native healing was not applied, health " + caster.getHealth()); cleanup(helper,caster); helper.succeed(); });
    }

    @GameTest(template="empty_3x3x3", timeoutTicks=230)
    public static void heartstopDefersAndRepaysDamage(GameTestHelper helper) {
        var caster = caster(helper,1); var cast = cast(helper,caster,"heartstop"); successful(helper,cast);
        caster.hurt(caster.damageSources().generic(),6); require(helper,caster.getHealth()==20,"Heartstop did not prevent incoming damage");
        caster.invulnerableTime=0; caster.hurt(caster.damageSources().generic(),4);
        helper.runAfterDelay(210, () -> { require(helper,Math.abs(caster.getHealth()-15)<.01,"Expected repayment of five damage, got " + caster.getHealth()); cleanup(helper,caster); helper.succeed(); });
    }

    @GameTest(template="empty_3x3x3")
    public static void echoUsesAttackerSubjectAndCannotLoop(GameTestHelper helper) {
        var caster = caster(helper,0); var victim = helper.spawnWithNoFreeWill(EntityType.VILLAGER,new BlockPos(1,1,2));
        successful(helper,cast(helper,caster,"echoing_strikes"));
        victim.hurt(caster.damageSources().mobAttack(caster),4);
        require(helper,Math.abs(victim.getHealth()-14)<.01,"Expected ordinary hit plus one 50% echo, got " + victim.getHealth());
        cleanup(helper,caster); helper.succeed();
    }

    @GameTest(template="empty_3x3x3", timeoutTicks=90)
    public static void blightChangesHealingBeforeCommit(GameTestHelper helper) {
        var caster = caster(helper,0); var victim = helper.spawnWithNoFreeWill(EntityType.VILLAGER,new BlockPos(1,1,2));
        caster.setXRot(0); var cast=cast(helper,caster,"blight");
        helper.runAfterDelay(50, () -> { successful(helper,cast); victim.setHealth(8); victim.heal(4); require(helper,Math.abs(victim.getHealth()-10)<.01,"Blight did not halve healing"); cleanup(helper,caster); helper.succeed(); });
    }

    @GameTest(template="empty_3x3x3", timeoutTicks=80)
    public static void summonCohortAndRecastDismissal(GameTestHelper helper) {
        var caster = caster(helper,1); var cast = cast(helper,caster,"raise_dead");
        helper.runAfterDelay(45, () -> {
            require(helper,cast.status()==SpellRuntime.Status.AWAITING_RECAST,"Summons did not wait for dismissal: " + cast.status()+cast.failure());
            var world=NativeMagic.session(helper.getLevel().getServer()).world();
            var mobs=helper.getLevel().getEntitiesOfClass(Mob.class,caster.getBoundingBox().inflate(5),e->world.isOwnedBy(e,caster.getUUID()));
            require(helper,mobs.size()==3,"Expected all three undead, got " + mobs.size());
            require(helper,cast==cast(helper,caster,"raise_dead"),"Recast started a new session");
            require(helper,mobs.stream().allMatch(Entity::isRemoved),"Dismissal retained summon bodies"); cleanup(helper,caster); helper.succeed();
        });
    }

    @GameTest(template="empty_3x3x3")
    public static void reactiveTombBreaksOnFirstHit(GameTestHelper helper) {
        var caster = caster(helper,1); var cast=cast(helper,caster,"ice_tomb"); successful(helper,cast);
        caster.hurt(caster.damageSources().generic(),6); require(helper,caster.getHealth()==20,"Tomb did not intercept damage");
        caster.invulnerableTime=0; caster.hurt(caster.damageSources().generic(),6);
        require(helper,caster.getHealth()==14,"Tomb protected more than one hit"); cleanup(helper,caster); helper.succeed();
    }

    @GameTest(template="empty_3x3x3")
    public static void counterspellInterruptsChargingTarget(GameTestHelper helper) {
        var caster=caster(helper,0); var victim=caster(helper,2); var charging=cast(helper,victim,"greater_heal");
        require(helper,charging.status()==SpellRuntime.Status.CHARGING,"Target was not charging");
        successful(helper,cast(helper,caster,"counterspell"));
        require(helper,charging.status()==SpellRuntime.Status.INTERRUPTED,"Counterspell did not interrupt the aimed caster"); cleanup(helper,caster); cleanup(helper,victim); helper.succeed();
    }

    @GameTest(template="empty_3x3x3")
    public static void arcaneLockPersistsAndOwnerCanUnlock(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1,1,1),Blocks.CHEST); var caster=caster(helper,2); caster.setYRot(180); caster.setXRot(45);
        successful(helper,cast(helper,caster,"arcane_lock")); var block=(BaseContainerBlockEntity)helper.getBlockEntity(new BlockPos(1,1,1));
        require(helper,caster.getUUID().toString().equals(block.getPersistentData().getString("vestige:lock_owner")),"Lock owner was not persisted");
        successful(helper,cast(helper,caster,"arcane_lock")); require(helper,!block.getPersistentData().contains("vestige:lock_owner"),"Recast did not unlock"); cleanup(helper,caster); helper.succeed();
    }

    @GameTest(template="empty_3x3x3")
    public static void spectralHammerMinesAndPreservesDrops(GameTestHelper helper) {
        var caster=caster(helper,2); caster.setYRot(180); caster.setXRot(45); caster.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.IRON_PICKAXE));
        helper.setBlock(new BlockPos(1,1,1),Blocks.STONE); var cast=cast(helper,caster,"spectral_hammer");
        helper.runAfterDelay(15,()-> { successful(helper,cast); require(helper,helper.getBlockState(new BlockPos(1,1,1)).isAir(),"Hammer did not mine the target face"); cleanup(helper,caster); helper.succeed(); });
    }

    @GameTest(template="empty_3x3x3", timeoutTicks=90)
    public static void wololoTargetsSheep(GameTestHelper helper) {
        var caster=caster(helper,0); var sheep=helper.spawnWithNoFreeWill(EntityType.SHEEP,new BlockPos(1,1,2));
        caster.setXRot(10);
        var cast=cast(helper,caster,"wololo"); helper.runAfterDelay(60,()-> { successful(helper,cast); require(helper,sheep.isAlive(),"Wololo harmed its subject"); cleanup(helper,caster);helper.succeed(); });
    }

    @GameTest(template="empty_3x3x3", timeoutTicks=90)
    public static void throwRecoversHeldItem(GameTestHelper helper) {
        var caster=caster(helper,0); caster.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.IRON_SWORD)); caster.setXRot(0);
        var cast=cast(helper,caster,"throw");
        helper.runAfterDelay(60,()-> { successful(helper,cast); require(helper,caster.getMainHandItem().isEmpty(),"Thrown stack was not removed"); var items=helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,caster.getBoundingBox().inflate(35),e->e.getItem().is(Items.IRON_SWORD)); require(helper,!items.isEmpty(),"Thrown item was lost"); cleanup(helper,caster);helper.succeed(); });
    }

    private static Villager caster(GameTestHelper helper,int z) {
        for (int x=0;x<3;x++) for (int dz=0;dz<3;dz++) helper.setBlock(new BlockPos(x,0,dz),Blocks.STONE);
        var caster=helper.spawnWithNoFreeWill(EntityType.VILLAGER,new BlockPos(1,1,z));
        caster.setNoAi(true);caster.setYRot(0);caster.setXRot(0); NativeMagic.session(helper.getLevel().getServer()).world().registerActor(caster); return caster;
    }

    @GameTest(template="empty_3x3x3", batch="spatial", timeoutTicks=90)
    public static void recastPortalTransfersBetweenBothEndpoints(GameTestHelper helper) {
        var caster=caster(helper,1); caster.setXRot(15);
        var cast=cast(helper,caster,"portal"); require(helper,cast.status()==SpellRuntime.Status.AWAITING_RECAST,"Portal did not await its second endpoint");
        caster.setYRot(180); require(helper,cast==cast(helper,caster,"portal"),"Portal recast changed identity"); successful(helper,cast);
        var world=NativeMagic.session(helper.getLevel().getServer()).world();
        var ends=helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.decoration.ArmorStand.class,caster.getBoundingBox().inflate(15),e->world.isOwnedBy(e,caster.getUUID()));
        require(helper,ends.size()==2,"Portal did not create both endpoints");
        var pig=helper.spawnWithNoFreeWill(EntityType.PIG,new BlockPos(1,1,1)); pig.setNoAi(true); pig.setNoGravity(true); pig.setPos(ends.getFirst().position());
        helper.runAfterDelay(3,()-> { require(helper,pig.position().distanceTo(ends.getLast().position())<2,"Portal did not transfer its visitor"); cleanup(helper,caster); pig.discard(); helper.succeed(); });
    }

    @GameTest(template="empty_3x3x3", batch="fields", timeoutTicks=160)
    public static void blackHolePulsesWithoutDestroyingItsBacking(GameTestHelper helper) {
        var caster=caster(helper,1); caster.setXRot(45); var victim=helper.spawnWithNoFreeWill(EntityType.VILLAGER,new BlockPos(1,1,2)); victim.setNoAi(true); victim.setNoGravity(true);
        var cast=cast(helper,caster,"black_hole");
        helper.runAfterDelay(125,()-> { successful(helper,cast); require(helper,victim.getHealth()<20,"Field did not pulse damage"); require(helper,!NativeMagic.session(helper.getLevel().getServer()).runtime().manifestations(cast.id()).isEmpty(),"Field damaged its own backing marker"); cleanup(helper,caster);victim.discard();helper.succeed(); });
    }
    private static SpellRuntime.Cast cast(GameTestHelper helper, LivingEntity caster,String name) {
        var spell=NativeMagic.spells().spells().get(VestigeMainMod.location(name)); require(helper,spell!=null,"Missing definition " + name);
        return NativeMagic.session(helper.getLevel().getServer()).runtime().cast(spell,SpellEvent.of(SpellTriggerTypes.INTERACT,caster.getUUID(),null),List.of(),true,Optional.empty(),true);
    }
    private static void successful(GameTestHelper helper,SpellRuntime.Cast cast) {
        require(helper,cast.failure().isEmpty() && (cast.status()==SpellRuntime.Status.COMPLETED || cast.status()==SpellRuntime.Status.AWAITING_RECAST),"Cast failed: " + cast.status()+cast.failure());
    }

    @GameTest(template="empty_3x3x3", batch="backing", timeoutTicks=130)
    public static void rootBackingUsesAuthoredHealthAndReleasesSlow(GameTestHelper helper) {
        var caster=caster(helper,0); var victim=helper.spawnWithNoFreeWill(EntityType.VILLAGER,new BlockPos(1,1,2));victim.setNoAi(true);
        var cast=cast(helper,caster,"root");
        helper.runAfterDelay(70,()-> {
            successful(helper,cast);
            var anchors=helper.getLevel().getEntitiesOfClass(com.quzzar.vestige.magic.world.SpellAnchor.class,victim.getBoundingBox().inflate(2));
            require(helper,anchors.size()==1,"Root did not create a native backing body"); var anchor=anchors.getFirst();
            require(helper,anchor.getHealth()==10,"Root lost its authored hit points");
            anchor.hurt(caster.damageSources().magic(),4); require(helper,anchor.getHealth()==6,"Backing ignored spell damage or used armor-stand break rules");
            anchor.hurt(caster.damageSources().magic(),6); require(helper,anchor.isRemoved(),"Destroyed root retained its backing");
            helper.runAfterDelay(30,()-> { require(helper,!victim.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN),"Destroyed root kept refreshing its slow");cleanup(helper,caster);victim.discard();helper.succeed(); });
        });
    }
    @GameTest(template="empty_3x3x3", batch="balance", timeoutTicks=65)
    public static void balancedCastingPaysForEveryImmediateRepeat(GameTestHelper helper) {
        var caster=caster(helper,0); var session=NativeMagic.session(helper.getLevel().getServer());
        caster.getPersistentData().putDouble("vestige:mana",200);
        var spell=NativeMagic.spells().spells().get(VestigeMainMod.location("force_arrow"));
        var event=SpellEvent.of(SpellTriggerTypes.INTERACT,caster.getUUID(),null);
        successful(helper,session.runtime().cast(spell,event,List.of(),true));
        require(helper,caster.getPersistentData().getDouble("vestige:mana")==186,"Force Arrow did not spend its fourteen mana");
        successful(helper,session.runtime().cast(spell,event,List.of(),true));
        require(helper,caster.getPersistentData().getDouble("vestige:mana")==172,"Immediate repeat retained recovery or skipped payment");
        successful(helper,session.runtime().cast(spell,event,List.of(),true));
        require(helper,caster.getPersistentData().getDouble("vestige:mana")==158,"Repeat did not pay exactly once");
        cleanup(helper,caster); helper.succeed();
    }

    @GameTest(template="empty_3x3x3", batch="balance", timeoutTicks=170)
    public static void sunbeamHasTunedThroughputAndAmplifyDoublesActualDamage(GameTestHelper helper) {
        var caster=caster(helper,0); var victim=durableVictim(helper);
        var point=victim.position(); helper.onEachTick(()-> { victim.setPos(point); victim.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO); });
        var spell=NativeMagic.spells().spells().get(VestigeMainMod.location("sunbeam"));
        var session=NativeMagic.session(helper.getLevel().getServer());
        var first=cast(helper,caster,"sunbeam");
        helper.runAfterDelay(70,()-> {
            successful(helper,first); require(helper,Math.abs(victim.getHealth()-76)<.01,"Expected sixteen 1.5-HP pulses, got " + victim.getHealth());
            var boosted=session.runtime().cast(spell,SpellEvent.of(SpellTriggerTypes.INTERACT,caster.getUUID(),null),
                    List.of(new com.quzzar.vestige.magic.definition.TraitModifier(VestigeMainMod.location("amplify"),com.quzzar.vestige.magic.definition.TraitModifier.Operation.MULTIPLY,2)),true,Optional.empty(),true);
            helper.runAfterDelay(70,()-> {
                successful(helper,boosted); require(helper,Math.abs(victim.getHealth()-28)<.01,"Amplify did not produce forty-eight actual HP damage: " + victim.getHealth());
                require(helper,spell.traits().rating(VestigeMainMod.location("amplify"))==1,"Boost mutated the base definition");
                cleanup(helper,caster); victim.discard(); helper.succeed();
            });
        });
    }

    @GameTest(template="empty_3x3x3", batch="balance", timeoutTicks=40)
    public static void dashCannotMultiplyDamageOnRepeatedContact(GameTestHelper helper) {
        var caster=caster(helper,0); var victim=durableVictim(helper); var origin=caster.position(); var point=victim.position();
        helper.onEachTick(()-> { caster.setPos(origin); caster.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO); victim.setPos(point); victim.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO); });
        var cast=cast(helper,caster,"volt_strike");
        helper.runAfterDelay(12,()-> {
            successful(helper,cast); require(helper,Math.abs(victim.getHealth()-88)<.01,"Repeated contact multiplied Volt Strike's twelve HP: " + victim.getHealth());
            cleanup(helper,caster); victim.discard(); helper.succeed();
        });
    }

    @GameTest(template="empty_3x3x3", batch="balance", timeoutTicks=90)
    public static void summonReplacementCannotAccumulateCohorts(GameTestHelper helper) {
        var caster=caster(helper,1); var session=NativeMagic.session(helper.getLevel().getServer());
        cast(helper,caster,"summon_zombie");
        helper.runAfterDelay(25,()-> {
            var first=helper.getLevel().getEntitiesOfClass(Mob.class,caster.getBoundingBox().inflate(8),e->session.world().isOwnedBy(e,caster.getUUID()));
            require(helper,first.size()==1,"First cast did not create one minion");
            require(helper,first.getFirst().getMaxHealth()==16,"Minion did not use its tuned sixteen HP");
            cast(helper,caster,"summon_zombie");
            helper.runAfterDelay(25,()-> {
                require(helper,first.getFirst().isRemoved(),"Replacement retained the first cohort");
                var second=helper.getLevel().getEntitiesOfClass(Mob.class,caster.getBoundingBox().inflate(8),e->session.world().isOwnedBy(e,caster.getUUID()));
                require(helper,second.size()==1,"Replacement accumulated minions"); cleanup(helper,caster); helper.succeed();
            });
        });
    }

    @GameTest(template="empty_3x3x3", batch="balance", timeoutTicks=45)
    public static void fireballRespectsItsCrowdTargetLimit(GameTestHelper helper) {
        var caster=caster(helper,0); var victims=new ArrayList<Villager>();
        for(int i=0;i<8;i++) victims.add(durableVictim(helper));
        var point=victims.getFirst().position();
        helper.onEachTick(()->victims.forEach(victim->{victim.setPos(point); victim.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);}));
        var cast=cast(helper,caster,"fireball");
        helper.runAfterDelay(35,()-> {
            successful(helper,cast); long damaged=victims.stream().filter(v->v.getHealth()<100).count();
            require(helper,damaged==6,"Expected six eligible explosion victims, got " + damaged);
            cleanup(helper,caster); victims.forEach(Entity::discard); helper.succeed();
        });
    }

    @GameTest(template="empty_3x3x3", batch="balance_caps", timeoutTicks=65, skyAccess=true)
    public static void volleySharesItsHitCapAcrossIndependentProjectiles(GameTestHelper helper) {
        var caster=caster(helper,0); var victim=durableVictim(helper); var point=victim.position();
        helper.onEachTick(()-> { victim.setPos(point); victim.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO); });
        var cast=cast(helper,caster,"arrow_volley");
        helper.runAfterDelay(45,()-> {
            successful(helper,cast); require(helper,Math.abs(victim.getHealth()-88)<.01,"Volley did not cap twelve independent arrows at four hits: " + victim.getHealth());
            cleanup(helper,caster); victim.discard(); helper.succeed();
        });
    }

    @GameTest(template="empty_9x3x9", batch="balance_caps", timeoutTicks=120, skyAccess=true)
    public static void starfallSharesItsCapAcrossSeparateMeteorPulses(GameTestHelper helper) {
        var caster=caster(helper,0); var victim=durableVictim(helper); var point=victim.position();
        // Rain starts up to three blocks from its target. Keep the complete flight envelope
        // inside the cleared fixture, away from adjacent test structures and subjects.
        caster.setPos(caster.position().add(3,0,3));victim.setPos(point.add(3,0,3));point=victim.position();
        for(int x=3;x<6;x++)for(int z=3;z<6;z++)helper.setBlock(new BlockPos(x,0,z),Blocks.STONE);
        var stationaryPoint=point;
        helper.onEachTick(()-> { victim.setPos(stationaryPoint); victim.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO); });
        var cast=cast(helper,caster,"starfall");
        helper.runAfterDelay(100,()-> {
            successful(helper,cast); require(helper,Math.abs(victim.getHealth()-60)<.01,"Twenty meteors did not share their ten-hit limit: " + victim.getHealth());
            cleanup(helper,caster); victim.discard(); helper.succeed();
        });
    }

    @GameTest(template="empty_3x3x3", batch="balance_healing", timeoutTicks=175)
    public static void circlePulsesExactlySixTimesBeforeExpiry(GameTestHelper helper) {
        var caster=caster(helper,1); caster.setXRot(90); caster.setHealth(1);
        var cast=cast(helper,caster,"healing_circle");
        helper.runAfterDelay(150,()-> {
            successful(helper,cast); require(helper,Math.abs(caster.getHealth()-13)<.01,"Expected six two-HP healing pulses, got " + caster.getHealth());
            require(helper,NativeMagic.session(helper.getLevel().getServer()).runtime().manifestations(cast.id()).isEmpty(),"Expired circle retained its backing");
            cleanup(helper,caster); helper.succeed();
        });
    }

    @GameTest(template="empty_3x3x3", batch="balance_healing", timeoutTicks=95)
    public static void siphoningCannotHealFromOverkillOrACorpse(GameTestHelper helper) {
        var caster=caster(helper,0); caster.setHealth(5); var victim=durableVictim(helper); victim.setHealth(1);
        var cast=cast(helper,caster,"ray_of_siphoning");
        helper.runAfterDelay(70,()-> {
            successful(helper,cast); require(helper,!victim.isAlive(),"Siphoning did not defeat its one-HP target");
            require(helper,Math.abs(caster.getHealth()-5.5)<.01,"Siphoning manufactured healing beyond half one actual HP: " + caster.getHealth());
            cleanup(helper,caster); victim.discard(); helper.succeed();
        });
    }

    private static Villager durableVictim(GameTestHelper helper) {
        var victim=helper.spawnWithNoFreeWill(EntityType.VILLAGER,new BlockPos(1,1,2));
        victim.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(100);
        victim.setHealth(100); victim.setNoAi(true); victim.setNoGravity(true); return victim;
    }

    private static void cleanup(GameTestHelper helper,LivingEntity caster) {
        var runtime=NativeMagic.session(helper.getLevel().getServer()).runtime(); runtime.interruptActor(caster.getUUID()); runtime.dispelActor(caster.getUUID());caster.discard();
    }
    private static void require(GameTestHelper helper,boolean condition,String message) { if (!condition) helper.fail(message); }
}
