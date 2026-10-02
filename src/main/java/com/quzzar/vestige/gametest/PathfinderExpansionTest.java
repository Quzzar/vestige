package com.quzzar.vestige.gametest;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.condition.ConditionValue;
import com.quzzar.vestige.magic.definition.*;
import com.quzzar.vestige.magic.effect.SpellEffects;
import com.quzzar.vestige.magic.runtime.*;
import com.quzzar.vestige.magic.world.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.*;
import java.util.*;

@GameTestHolder(VestigeMainMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PathfinderExpansionTest {
    private PathfinderExpansionTest() { }

    @GameTest(template="empty_3x3x3",batch="expansion_glass")
    public static void glassShieldHasOneMitigationAndNearbyAttackerRetaliation(GameTestHelper h) {
        var caster=caster(h); var attacker=villager(h,2); var cast=cast(h,caster,"glass_shield",null);
        caster.hurt(attacker.damageSources().mobAttack(attacker),5);
        require(h,caster.getHealth()==18 && attacker.getHealth()==17,"Glass shield mitigation/retaliation incorrect");
        caster.invulnerableTime=0; attacker.invulnerableTime=0; caster.hurt(attacker.damageSources().mobAttack(attacker),5);
        require(h,caster.getHealth()==13 && attacker.getHealth()==17,"Glass shield reacted more than once");
        require(h,cast.failure().isEmpty(),"Glass reaction failed"); finish(h,caster);
    }

    @GameTest(template="empty_3x3x3",batch="expansion_translocate")
    public static void translocateRequiresSupportedLandingAndShowsArrivalAfterSuccess(GameTestHelper h) {
        var caster=caster(h); var origin=caster.position();
        var unsupported=new SpellSubject.Position(h.getLevel().dimension(),origin.add(1,8,0));
        var failed=cast(h,caster,"translocate",unsupported);
        require(h,failed.failure().isPresent() && caster.position().equals(origin),"Unsupported teleport moved caster");
        var world=NativeMagic.session(h.getLevel().getServer()).world();
        require(h,world.visualCues().stream().noneMatch(v->v.points().getFirst().entityUuid().equals(caster.getUUID())),"Failed teleport showed arrival");
        var landing=new SpellSubject.Position(h.getLevel().dimension(),origin.add(0,0,1));
        success(h,cast(h,caster,"translocate",landing));
        require(h,caster.position().distanceToSqr(origin)>.5 && caster.fallDistance==0,"Safe teleport did not land");
        require(h,world.visualCues().stream().anyMatch(v->v.points().getFirst().entityUuid().equals(caster.getUUID())),"Successful teleport omitted arrival");
        finish(h,caster);
    }

    @GameTest(template="empty_3x3x3",batch="expansion_figment",timeoutTicks=30)
    public static void figmentLuresWithoutDamageAndEndsWhenItsMarkerIsStruck(GameTestHelper h) {
        var caster=caster(h); var hostile=h.spawnWithNoFreeWill(EntityType.ZOMBIE,new BlockPos(2,1,2)); hostile.setNoGravity(true); hostile.setNoAi(true);
        hostile.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.LEATHER_HELMET));
        var cast=cast(h,caster,"figment",new SpellSubject.Position(h.getLevel().dimension(),caster.position().add(0,0,1)));
        var marker=owned(h,caster,SpellAnchor.class).getFirst();
        h.runAfterDelay(8,()-> {
            success(h,cast); require(h,hostile.getTarget()==marker && hostile.getHealth()==20,"Figment did not lure harmlessly; health="+hostile.getHealth()+", target="+hostile.getTarget()+", marker="+marker+", cast="+cast.failure());
            marker.hurt(marker.damageSources().generic(),1);
            h.runAfterDelay(2,()-> { require(h,marker.isRemoved() && runtime(h).manifestations(cast.id()).isEmpty(),"Broken figment retained backing"); finish(h,caster); });
        });
    }

    @GameTest(template="empty_3x3x3",batch="expansion_illusion",timeoutTicks=30)
    public static void illusoryCreatureIsSilentInertAndOneHit(GameTestHelper h) {
        var caster=caster(h); var cast=cast(h,caster,"illusory_creature",new SpellSubject.Position(h.getLevel().dimension(),caster.position().add(0,0,1)));
        var wolf=owned(h,caster,net.minecraft.world.entity.animal.Wolf.class).getFirst();
        require(h,wolf.isNoAi() && wolf.isSilent() && wolf.getHealth()==1 && wolf.getAttributeValue(Attributes.ATTACK_DAMAGE)==0,"Illusion proxy could fight");
        wolf.hurt(wolf.damageSources().generic(),1);
        h.runAfterDelay(2,()-> { require(h,runtime(h).manifestations(cast.id()).isEmpty(),"One-hit illusion retained its lease"); finish(h,caster); });
    }

    @GameTest(template="empty_3x3x3",batch="expansion_invisibility",timeoutTicks=30)
    public static void invisibilityEndsAfterCommittedDamageAndDispelStopsRefresh(GameTestHelper h) {
        var caster=caster(h); var victim=villager(h,2); var cast=cast(h,caster,"invisibility",null);
        h.runAfterDelay(3,()-> {
            require(h,caster.hasEffect(MobEffects.INVISIBILITY),"Invisibility never applied");
            victim.hurt(caster.damageSources().mobAttack(caster),1);
            require(h,runtime(h).manifestations(cast.id()).isEmpty(),"Committed attack did not end invisibility lease");
            h.runAfterDelay(4,()-> {
                require(h,!caster.hasEffect(MobEffects.INVISIBILITY),"Attack retained invisibility refresh");
                cast(h,caster,"invisibility",null);
                h.runAfterDelay(3,()-> {
                    runtime(h).dispelActor(caster.getUUID());
                    h.runAfterDelay(4,()-> { require(h,!caster.hasEffect(MobEffects.INVISIBILITY),"Dispel retained invisibility refresh"); finish(h,caster); });
                });
            });
        });
    }

    @GameTest(template="empty_3x3x3",batch="expansion_external_invisibility",timeoutTicks=20)
    public static void invisibilityCleanupPreservesAnExternalLongerEffect(GameTestHelper h) {
        var caster=caster(h);
        caster.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.INVISIBILITY,100));
        cast(h,caster,"invisibility",null);
        h.runAfterDelay(3,()-> {
            runtime(h).dispelActor(caster.getUUID());
            h.runAfterDelay(4,()-> { require(h,caster.hasEffect(MobEffects.INVISIBILITY) && caster.getEffect(MobEffects.INVISIBILITY).getDuration()>80,"Native cleanup removed external invisibility"); finish(h,caster); });
        });
    }

    @GameTest(template="empty_3x3x3",batch="expansion_ground",timeoutTicks=155)
    public static void greaseAndMudHaveDifferentStrengthAndReleaseAfterTheirFields(GameTestHelper h) {
        var caster=caster(h); var victim=villager(h,2); var position=victim.position();
        h.onEachTick(()-> { victim.setPos(position); victim.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO); });
        cast(h,caster,"grease",new SpellSubject.Position(h.getLevel().dimension(),position));
        h.runAfterDelay(12,()-> {
            require(h,victim.hasEffect(MobEffects.MOVEMENT_SLOWDOWN) && victim.getEffect(MobEffects.MOVEMENT_SLOWDOWN).getAmplifier()==1,"Grease strength wrong");
            runtime(h).dispelActor(caster.getUUID());
            cast(h,caster,"mud_pit",new SpellSubject.Position(h.getLevel().dimension(),position));
        });
        h.runAfterDelay(26,()-> { require(h,victim.getEffect(MobEffects.MOVEMENT_SLOWDOWN).getAmplifier()==2,"Mud did not apply stronger slowing"); });
        h.runAfterDelay(150,()-> { require(h,!victim.hasEffect(MobEffects.MOVEMENT_SLOWDOWN) && victim.getHealth()==20,"Ground fields left damage or permanent slow"); finish(h,caster); });
    }

    @GameTest(template="empty_3x3x3",batch="expansion_vitality")
    public static void healAndHarmReverseRecipientEligibilityAndHonorAmplification(GameTestHelper h) {
        var caster=caster(h); var living=villager(h,2); living.setHealth(1);
        success(h,cast(h,caster,"heal",null)); require(h,living.getHealth()==9,"Heal did not restore living HP");
        success(h,cast(h,caster,"harm",null)); require(h,living.getHealth()==1,"Harm did not damage living HP"); living.discard();
        var undead=h.spawnWithNoFreeWill(EntityType.SKELETON,new BlockPos(1,1,2)); undead.setNoGravity(true); undead.setHealth(1);
        undead.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.LEATHER_HELMET));
        success(h,cast(h,caster,"harm",null)); require(h,undead.getHealth()==9,"Harm did not restore undead HP");
        success(h,cast(h,caster,"heal",null)); require(h,undead.getHealth()==1,"Heal did not damage undead HP");
        undead.setHealth(1);
        var base=definition("harm"); var immediate=copy(base,base.effects());
        var boosted=runtime(h).cast(immediate,SpellEvent.of(SpellTriggerTypes.INTERACT,caster.getUUID(),null),
                List.of(new TraitModifier(VestigeMainMod.location("amplify"),TraitModifier.Operation.MULTIPLY,2)),true);
        success(h,boosted); require(h,undead.getHealth()==17,"Harm amplification did not double healing"); finish(h,caster);
    }

    @GameTest(template="empty_3x3x3",batch="expansion_sensing")
    public static void magicDetectionTracksActiveGameplayAndHeldItemInspection(GameTestHelper h) {
        var caster=caster(h);
        success(h,cast(h,caster,"glass_shield",null));
        var found=cast(h,caster,"detect_magic",null); require(h,flag(found)==1,"Detector missed native ward");
        runtime(h).dispelActor(caster.getUUID());
        var absent=cast(h,caster,"detect_magic",null); require(h,flag(absent)==0,"Detector found ended ward or cosmetic ring");
        caster.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.STICK));
        require(h,flag(cast(h,caster,"read_aura",null))==0,"Plain held item reported enchanted");
        var enchanted=new ItemStack(Items.IRON_SWORD);
        enchanted.enchant(h.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.UNBREAKING),1);
        caster.setItemSlot(EquipmentSlot.MAINHAND,enchanted);
        require(h,flag(cast(h,caster,"read_aura",null))==1 && flag(cast(h,caster,"detect_magic",null))==1,"Equipped enchantment was not recognized");
        finish(h,caster);
    }

    @GameTest(template="empty_3x3x3",batch="expansion_rust",timeoutTicks=135)
    public static void rustCloudHasFiveCappedPulsesAndCleansItsBacking(GameTestHelper h) {
        var caster=caster(h); var victim=villager(h,2); var position=victim.position();
        h.onEachTick(()->victim.setPos(position));
        var cast=cast(h,caster,"rust_cloud",new SpellSubject.Position(h.getLevel().dimension(),position));
        h.runAfterDelay(105,()-> { require(h,victim.getHealth()==10 && runtime(h).manifestations(cast.id()).isEmpty(),"Rust cloud damage/pulses/expiry wrong: "+victim.getHealth()); });
        h.runAfterDelay(130,()-> { require(h,!victim.hasEffect(MobEffects.BLINDNESS),"Rust cloud left permanent blindness"); finish(h,caster); });
    }

    @GameTest(template="empty_3x3x3",batch="expansion_swarm",timeoutTicks=125)
    public static void cinderSwarmKeepsItsCapturedTargetWithoutSplashingBystanders(GameTestHelper h) {
        var caster=caster(h); var victim=villager(h,2); var bystander=h.spawnWithNoFreeWill(EntityType.VILLAGER,new BlockPos(2,1,2)); bystander.setNoGravity(true);
        var cast=cast(h,caster,"cinder_swarm",null);
        var moved=victim.position().add(3,0,0); victim.setPos(moved); h.onEachTick(()->victim.setPos(moved));
        h.runAfterDelay(105,()-> {
            require(h,victim.getHealth()==10 && bystander.getHealth()==20,"Swarm lost target or splashed bystander");
            require(h,runtime(h).manifestations(cast.id()).isEmpty(),"Swarm retained backing"); finish(h,caster);
        });
    }

    @GameTest(template="empty_3x3x3",batch="expansion_animal",timeoutTicks=30)
    public static void summonedAnimalHasNativeStatsOwnershipAndRecastDismissal(GameTestHelper h) {
        var caster=caster(h); var spell=copy(definition("summon_animal"),definition("summon_animal").effects());
        var cast=runtime(h).cast(spell,SpellEvent.of(SpellTriggerTypes.INTERACT,caster.getUUID(),null),List.of(),true);
        var wolf=owned(h,caster,net.minecraft.world.entity.animal.Wolf.class).getFirst();
        require(h,wolf.getHealth()==16 && wolf.getAttributeValue(Attributes.ATTACK_DAMAGE)==3 && wolf.isTame(),"Summoned animal stats/ownership wrong");
        require(h,cast.status()==SpellRuntime.Status.AWAITING_RECAST,"Animal did not expose dismissal continuation");
        var recast=runtime(h).cast(spell,SpellEvent.of(SpellTriggerTypes.INTERACT,caster.getUUID(),null),List.of(),true);
        success(h,recast); require(h,wolf.isRemoved() && runtime(h).manifestations(cast.id()).isEmpty(),"Recast did not dismiss animal"); finish(h,caster);
    }

    @GameTest(template="empty_3x3x3",batch="expansion_flicker",timeoutTicks=50)
    public static void flickerUsesSafeSupportedMovementAndFiniteNonmagicalMitigation(GameTestHelper h) {
        var caster=caster(h); caster.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100); caster.setHealth(100);
        for(int x=-3;x<7;x++)for(int z=-3;z<7;z++)h.setBlock(new BlockPos(x,0,z),Blocks.STONE);
        var origin=caster.position(); cast(h,caster,"flicker",null);
        h.runAfterDelay(42,()-> {
            require(h,caster.position().distanceToSqr(origin)>.5 && caster.position().distanceToSqr(origin)<=9.1,"Flicker did not move within its safe radius");
            var support=caster.blockPosition().below();
            require(h,h.getLevel().getBlockState(support).isFaceSturdy(h.getLevel(),support,net.minecraft.core.Direction.UP),"Flicker landed without support");
            for(int i=0;i<4;i++){ caster.invulnerableTime=0; caster.hurt(caster.damageSources().generic(),10); }
            require(h,caster.getHealth()==70,"Flicker four quarter-hit reductions wrong");
            caster.invulnerableTime=0; caster.hurt(caster.damageSources().generic(),10); require(h,caster.getHealth()==60,"Flicker exceeded four reductions");
            caster.invulnerableTime=0; caster.hurt(caster.damageSources().magic(),10); require(h,caster.getHealth()==50,"Flicker reduced magical damage"); finish(h,caster);
        });
    }

    @GameTest(template="empty_3x3x3",batch="expansion_breeze",timeoutTicks=130)
    public static void gentleBreezeRequiresContinuousDwellAndExcludesUndead(GameTestHelper h) {
        var caster=caster(h); caster.setHealth(5); var visitor=villager(h,2); visitor.setHealth(10);
        var undead=h.spawnWithNoFreeWill(EntityType.SKELETON,new BlockPos(2,1,1)); undead.setNoGravity(true); undead.setHealth(5);
        undead.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.LEATHER_HELMET));
        var origin=visitor.position(); var center=caster.position().add(0,0,1);
        cast(h,caster,"gentle_breeze",new SpellSubject.Position(h.getLevel().dimension(),center));
        h.runAfterDelay(25,()->visitor.setPos(origin.add(10,0,0)));
        h.runAfterDelay(50,()->visitor.setPos(origin));
        h.runAfterDelay(80,()-> {
            require(h,caster.getHealth()==11,"Continuous occupant missed the once-only six-HP heal");
            require(h,visitor.getHealth()==10,"Leaving field did not reset dwell");
            require(h,undead.getHealth()==5,"Breeze healed or harmed undead");
        });
        h.runAfterDelay(125,()-> { require(h,visitor.getHealth()==16 && caster.getHealth()==11,"Breeze final healing exceeded once per creature"); finish(h,caster); });
    }

    private static SpellDefinition definition(String name) { return NativeMagic.spells().spells().get(VestigeMainMod.location("pf2_"+name)); }
    private static SpellDefinition copy(SpellDefinition s,List<SpellEffect> effects) { return new SpellDefinition(s.id(),s.rarity(),s.traditions(),s.traits(),List.of(),s.triggers(),effects,s.modes(),s.source()); }
    private static SpellRuntime.Cast cast(GameTestHelper h,LivingEntity caster,String name,SpellSubject point) {
        var s=definition(name); var effects=s.effects();
        if(point!=null) {
            var selected=(SpellEffects.ForEach)effects.getFirst();
            effects=List.of(new SpellEffects.ForEach(new TargetSpec(TargetSpec.Selection.EVENT_TARGET,new com.quzzar.vestige.magic.expression.SpellValue.Constant(0)),selected.effects(),selected.visual()));
        }
        return runtime(h).cast(copy(s,effects),SpellEvent.of(SpellTriggerTypes.INTERACT,caster.getUUID(),point),List.of(),true);
    }
    private static double flag(SpellRuntime.Cast cast) { return ((ConditionValue.Decimal)cast.state().get(VestigeMainMod.location("magic_found"))).value(); }
    private static SpellRuntime runtime(GameTestHelper h) { return NativeMagic.session(h.getLevel().getServer()).runtime(); }
    private static Villager caster(GameTestHelper h) {
        for(int x=0;x<3;x++)for(int z=0;z<3;z++)h.setBlock(new BlockPos(x,0,z),Blocks.STONE);
        var caster=villager(h,0); caster.setYRot(0); caster.setXRot(0);
        NativeMagic.session(h.getLevel().getServer()).world().registerActor(caster); return caster;
    }
    private static Villager villager(GameTestHelper h,int z) { var entity=h.spawnWithNoFreeWill(EntityType.VILLAGER,new BlockPos(1,1,z)); entity.setNoAi(true); entity.setNoGravity(true); return entity; }
    private static <T extends Entity> List<T> owned(GameTestHelper h,LivingEntity caster,Class<T> type) { return h.getLevel().getEntitiesOfClass(type,caster.getBoundingBox().inflate(8),e->NativeMagic.session(h.getLevel().getServer()).world().isOwnedBy(e,caster.getUUID())); }
    private static void success(GameTestHelper h,SpellRuntime.Cast cast) { require(h,cast.status()==SpellRuntime.Status.COMPLETED && cast.failure().isEmpty(),"Cast failed: "+cast.status()+cast.failure()); }
    private static void require(GameTestHelper h,boolean condition,String message) { if(!condition)h.fail(message); }
    private static void finish(GameTestHelper h,LivingEntity caster) { runtime(h).interruptActor(caster.getUUID()); runtime(h).dispelActor(caster.getUUID()); caster.discard(); h.succeed(); }
}
