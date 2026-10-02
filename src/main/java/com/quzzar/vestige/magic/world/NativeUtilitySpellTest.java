package com.quzzar.vestige.magic.world;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.definition.*;
import com.quzzar.vestige.magic.effect.SpellEffects;
import com.quzzar.vestige.magic.expression.SpellValue;
import com.quzzar.vestige.magic.runtime.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;
import java.util.*;

@GameTestHolder(VestigeMainMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class NativeUtilitySpellTest {
    private NativeUtilitySpellTest() { }
    @GameTest(template="empty_3x3x3",batch="utility_food",timeoutTicks=180)
    public static void gluttonyReceivesRealFoodCompletionAndStopsAfterThreeMeals(GameTestHelper h) {
        var caster=caster(h);
        caster.getPersistentData().putDouble("vestige:mana",0);
        var source=NativeMagic.spells().spells().get(VestigeMainMod.location("gluttony"));
        success(h,runtime(h).cast(copy(source,source.effects()),SpellEvent.of(SpellTriggerTypes.INTERACT,caster.getUUID(),null),List.of(),true));
        caster.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.BREAD,4));
        caster.startUsingItem(net.minecraft.world.InteractionHand.MAIN_HAND);
        for(int meal=1;meal<=4;meal++) {
            int expected=meal;
            h.runAfterDelay(meal*38,()->{
                check(h,caster.getPersistentData().getDouble("vestige:mana")==Math.min(expected,3)*30,"Real food completion did not grant the bounded nutrition-based mana");
                check(h,caster.getMainHandItem().getCount()==4-expected,"Food was not actually consumed");
                if(expected<4)caster.startUsingItem(net.minecraft.world.InteractionHand.MAIN_HAND);
                else finish(h,caster);
            });
        }
    }
    @GameTest(template="empty_3x3x3",batch="utility_stone")
    public static void solidWallBlocksCollisionAndNativeSightThenCleansUp(GameTestHelper h) {
        var caster=caster(h); var point=position(h,new BlockPos(1,1,5)); success(h,cast(h,caster,"wall_of_stone",point));
        var walls=owned(h,caster,SpellConstruct.class); check(h,walls.size()==1,"Missing stone construct"); var wall=walls.getFirst();
        check(h,wall.canBeCollidedWith() && wall.getBoundingBox().getXsize()==7 && wall.getBoundingBox().getYsize()==3,"Solid geometry dimensions wrong");
        check(h,!h.getLevel().noCollision(caster,caster.getBoundingBox().move(wall.position().subtract(caster.position()))),"Construct did not block creature collision");
        check(h,world(h).features.obscured(caster.position(),wall.position().add(0,0,2),h.getLevel()),"Construct did not block native sight");
        runtime(h).dispelActor(caster.getUUID()); check(h,wall.isRemoved(),"Wall backing leaked after dispel");finish(h,caster);
    }
    @GameTest(template="empty_3x3x3",batch="utility_ice")
    public static void illusionsNeverCollide(GameTestHelper h) {
        var caster=caster(h);success(h,cast(h,caster,"illusory_object",position(h,new BlockPos(1,1,5))));
        var image=owned(h,caster,SpellConstruct.class).getFirst();
        check(h,!image.canBeCollidedWith(),"Illusion acquired physical collision");finish(h,caster);
    }
    @GameTest(template="empty_3x3x3",batch="utility_tree")
    public static void protectorTreeSpendsOnePoolAcrossRecipients(GameTestHelper h) {
        var caster=caster(h); caster.setPos(caster.position().add(0,0,3)); var ally=creature(h,2,3); var team=h.getLevel().getScoreboard().addPlayerTeam("tree_"+caster.getId());
        h.getLevel().getScoreboard().addPlayerToTeam(caster.getScoreboardName(),team);h.getLevel().getScoreboard().addPlayerToTeam(ally.getScoreboardName(),team);
        success(h,cast(h,caster,"protector_tree",position(h,new BlockPos(1,1,5))));
        caster.hurt(caster.damageSources().generic(),10); ally.hurt(ally.damageSources().generic(),10);
        check(h,caster.getHealth()==20 && ally.getHealth()==16,"Tree gave separate pools or lost prevention");
        caster.invulnerableTime=0;caster.hurt(caster.damageSources().generic(),2);check(h,caster.getHealth()==18,"Exhausted tree protected again");finish(h,caster);
    }
    @GameTest(template="empty_3x3x3",batch="utility_guards")
    public static void heavyGuardAndImagesRespectEligibilityOverflowAndCharges(GameTestHelper h) {
        var caster=caster(h);success(h,cast(h,caster,"wooden_double",null));
        caster.hurt(caster.damageSources().generic(),4);check(h,caster.getHealth()==16,"Small hit triggered double");
        caster.invulnerableTime=0;caster.hurt(caster.damageSources().generic(),10);check(h,caster.getHealth()==12,"Heavy-hit overflow incorrect");
        caster.invulnerableTime=0;caster.hurt(caster.damageSources().generic(),2);check(h,caster.getHealth()==10,"Double intercepted twice");
        runtime(h).dispelActor(caster.getUUID());caster.setHealth(20);success(h,cast(h,caster,"mirror_image",null));var enemy=creature(h,2,1);
        caster.invulnerableTime=0;caster.hurt(caster.damageSources().generic(),2);check(h,caster.getHealth()==18,"Environment consumed images");
        for(int i=0;i<4;i++){caster.invulnerableTime=0;caster.hurt(enemy.damageSources().mobAttack(enemy),4);}
        check(h,caster.getHealth()==14,"Image count/per-hit protection incorrect");finish(h,caster);
    }
    @GameTest(template="empty_3x3x3",batch="utility_share")
    public static void lifeShareMovesActualDamageAndHonorsOneHpReserve(GameTestHelper h) {
        var caster=caster(h); var ally=creature(h,2,0);var team=h.getLevel().getScoreboard().addPlayerTeam("share_"+caster.getId());
        h.getLevel().getScoreboard().addPlayerToTeam(caster.getScoreboardName(),team);h.getLevel().getScoreboard().addPlayerToTeam(ally.getScoreboardName(),team);
        success(h,cast(h,caster,"share_life",new SpellSubject.Entity(ally.getUUID())));
        ally.hurt(ally.damageSources().generic(),10);check(h,caster.getHealth()==15 && ally.getHealth()==15,"Half damage not transferred");
        caster.setHealth(2);ally.invulnerableTime=0;ally.hurt(ally.damageSources().generic(),4);
        check(h,caster.getHealth()==1 && ally.getHealth()==12,"Life reserve or actual loss accounting broken");finish(h,caster);
    }
    @GameTest(template="empty_3x3x3",batch="utility_water")
    public static void waterIsFiniteAndLooseMetalKeepsStackIdentity(GameTestHelper h) {
        var caster=caster(h);BlockPos pos=h.absolutePos(new BlockPos(1,1,2));h.getLevel().setBlock(pos,Blocks.CAULDRON.defaultBlockState(),3);
        for(int i=0;i<3;i++)success(h,cast(h,caster,"create_water",new SpellSubject.Block(h.getLevel().dimension(),pos)));
        check(h,h.getLevel().getBlockState(pos).getValue(LayeredCauldronBlock.LEVEL)==3,"Water amount incorrect");
        var failed=cast(h,caster,"create_water",new SpellSubject.Block(h.getLevel().dimension(),pos));check(h,failed.failure().isPresent(),"Full cauldron accepted water");
        ItemEntity metal=new ItemEntity(h.getLevel(),caster.getX()+3,caster.getY(),caster.getZ(),new ItemStack(Items.IRON_INGOT,7));
        ItemEntity other=new ItemEntity(h.getLevel(),caster.getX()-3,caster.getY(),caster.getZ(),new ItemStack(Items.DIAMOND,2));h.getLevel().addFreshEntity(metal);h.getLevel().addFreshEntity(other);
        var id=metal.getUUID();Vec3 otherBefore=other.getDeltaMovement();success(h,cast(h,caster,"magnetic_attraction",null));
        check(h,metal.getUUID().equals(id) && metal.getItem().getCount()==7 && metal.getDeltaMovement().length()>.5,"Magnet copied or failed metal stack");
        check(h,other.getDeltaMovement().equals(otherBefore),"Nonmetal attracted");metal.discard();other.discard();finish(h,caster);
    }
    @GameTest(template="empty_3x3x3",batch="utility_size")
    public static void physicalSizeAndReachLeasesRestoreOnlyOwnedModifiers(GameTestHelper h) {
        var caster=caster(h);caster.setPos(caster.position().add(0,0,5));var original=caster.getBbWidth();success(h,cast(h,caster,"enlarge",null));
        check(h,Math.abs(caster.getScale()-1.5)<.001 && caster.getBbWidth()>original*1.4,"Enlarge did not change physical dimensions");
        runtime(h).dispelActor(caster.getUUID());check(h,caster.getScale()==1 && caster.getBbWidth()==original,"Enlarge scale leaked");
        success(h,cast(h,caster,"shrink",null));check(h,caster.getScale()==.5 && caster.getBbWidth()<original*.6,"Shrink did not change clearance");
        runtime(h).dispelActor(caster.getUUID());check(h,caster.getScale()==1,"Shrink scale leaked");finish(h,caster);
    }
    @GameTest(template="empty_3x3x3",batch="utility_movement")
    public static void waterContactAndWallContactMovementAreBounded(GameTestHelper h) {
        var caster=caster(h);BlockPos below=caster.blockPosition().below();h.getLevel().setBlock(below,Blocks.WATER.defaultBlockState(),3);
        caster.setPos(caster.getX(),below.getY()+1,caster.getZ());caster.setDeltaMovement(0,-.1,0);SpellMobility.apply(caster,"water_walk");
        check(h,caster.getDeltaMovement().y==0 && caster.onGround(),"Water surface failed to support");
        caster.setPos(caster.getX(),below.getY()+.2,caster.getZ());caster.setDeltaMovement(0,-.1,0);SpellMobility.apply(caster,"water_walk");
        check(h,caster.getDeltaMovement().y<0,"Submerged body was lifted");caster.horizontalCollision=true;SpellMobility.apply(caster,"climb");check(h,caster.getDeltaMovement().y==.16,"Wall contact did not climb");
        caster.horizontalCollision=false;caster.setDeltaMovement(Vec3.ZERO);SpellMobility.apply(caster,"climb");check(h,caster.getDeltaMovement().equals(Vec3.ZERO),"Grip gave unsupported flight");finish(h,caster);
    }
    @GameTest(template="empty_3x3x3",batch="utility_sensing")
    public static void privacyAndAcousticZonesHaveScopedBehaviorAndCleanup(GameTestHelper h) {
        var caster=caster(h);success(h,cast(h,caster,"peaceful_bubble",null));
        check(h,world(h).features.obscured(caster.position(),caster.position().add(6,0,0),h.getLevel()),"Privacy did not obscure across boundary");
        check(h,!world(h).features.obscured(caster.position(),caster.position().add(1,0,0),h.getLevel()),"Privacy hid same-side subjects");
        success(h,cast(h,caster,"silence",new SpellSubject.Position(h.getLevel().dimension(),caster.position())));
        check(h,world(h).features.silent(caster.position(),h.getLevel()) && !world(h).features.silent(caster.position().add(8,0,0),h.getLevel()),"Silence boundary scope wrong");
        var sonic=NativeMagic.spells().spells().get(VestigeMainMod.location("sonic_boom"));check(h,!world(h).canActivate(caster.getUUID(),sonic),"Vocal sonic delivery not suppressed");
        var spell=NativeMagic.spells().spells().get(VestigeMainMod.location("pf2_ignition"));check(h,world(h).canActivate(caster.getUUID(),spell),"Silence blocked nonvocal spell");
        runtime(h).dispelActor(caster.getUUID());check(h,!world(h).features.silent(caster.position(),h.getLevel()),"Silence leaked");finish(h,caster);
    }
    @GameTest(template="empty_3x3x3",batch="utility_sensor")
    public static void sensorKeepsBodyVulnerableAndTimeAbsenceBlocksNewCasts(GameTestHelper h) {
        var caster=caster(h);caster.setYRot(-90);caster.setYHeadRot(-90);caster.setXRot(12);
        Vec3 start=caster.position();success(h,cast(h,caster,"clairvoyance",position(h,new BlockPos(1,1,5))));
        check(h,caster.position().equals(start) && world(h).features.remote(caster.getUUID()),"Sensor moved body or did not activate");
        var camera=h.getLevel().getEntitiesOfClass(SpellAnchor.class,caster.getBoundingBox().inflate(8),e->world(h).isOwnedBy(e,caster.getUUID())).getFirst();
        check(h,camera.getViewYRot(1)==-90 && camera.getViewXRot(1)==12,"Sensor view did not retain the cast direction");
        caster.setYHeadRot(30);check(h,camera.getViewYRot(1)==-90,"Fixed sensor view followed later caster head movement");
        caster.hurt(caster.damageSources().generic(),3);check(h,caster.getHealth()==17 && !world(h).features.remote(caster.getUUID()),"Sensor protected body or failed hurt cancellation");
        success(h,cast(h,caster,"time_jump",null));caster.invulnerableTime=0;caster.hurt(caster.damageSources().generic(),3);check(h,caster.getHealth()==17,"Absence took damage");
        check(h,cast(h,caster,"enlarge",null).status()==SpellRuntime.Status.CONDITIONS_FAILED,"Absence allowed a new cast");
        h.runAfterDelay(22,()->{check(h,!world(h).features.absent(caster.getUUID()),"Absence leaked beyond expiry");finish(h,caster);});
    }
    @GameTest(template="empty_3x3x3",batch="utility_waterwall")
    public static void waterSheetDragsPhysicalProjectilesWithoutDestroyingThem(GameTestHelper h) {
        var caster=caster(h);var point=position(h,new BlockPos(1,1,5));success(h,cast(h,caster,"wall_of_water",point));
        h.runAfterDelay(35,()->{
            Arrow arrow=new Arrow(EntityType.ARROW,h.getLevel());arrow.setNoGravity(true);arrow.setPos(h.absolutePos(new BlockPos(1,2,5)).getCenter());arrow.setDeltaMovement(.5,0,0);h.getLevel().addFreshEntity(arrow);
            h.runAfterDelay(1,()->{check(h,!arrow.isRemoved() && arrow.getDeltaMovement().length()<.3,"Physical projectile was destroyed or not slowed");arrow.discard();finish(h,caster);});
        });
    }
    @GameTest(template="empty_3x3x3",batch="utility_boundaries")
    public static void repulsionRejectsEntryButContainmentHasSharedAttackDurability(GameTestHelper h) {
        var caster=caster(h);var enemy=creature(h,1,5);success(h,cast(h,caster,"repulsion",null));
        h.runAfterDelay(1,()->enemy.setPos(caster.position().add(0,0,2)));
        h.runAfterDelay(3,()->{check(h,enemy.distanceToSqr(caster)>9,"Repulsion allowed inward crossing");runtime(h).dispelActor(caster.getUUID());
            enemy.setPos(caster.position().add(0,0,5));success(h,cast(h,caster,"containment",new SpellSubject.Entity(enemy.getUUID())));
            enemy.hurt(caster.damageSources().mobAttack(caster),10);check(h,enemy.getHealth()==20,"Containment failed first outside protection");
            enemy.invulnerableTime=0;enemy.hurt(caster.damageSources().mobAttack(caster),10);check(h,enemy.getHealth()==16,"Containment pool multiplied");finish(h,caster);});
    }
    @GameTest(template="empty_3x3x3",batch="utility_support",timeoutTicks=90)
    public static void supportSummonsSpendSharedFiniteHealingAndTrickBudgets(GameTestHelper h) {
        var caster=caster(h);caster.setHealth(5);success(h,cast(h,caster,"summon_plant_or_fungus",position(h,new BlockPos(1,1,2))));
        h.runAfterDelay(85,()->{check(h,caster.getHealth()==9,"Healing support pulse or shared budget incorrect");finish(h,caster);});
    }
    @GameTest(template="empty_3x3x3",batch="utility_pet")
    public static void petShelterPreservesUuidHealthEquipmentAndReturnsOnDispel(GameTestHelper h) {
        var caster=caster(h);Wolf pet=h.spawnWithNoFreeWill(EntityType.WOLF,new BlockPos(1,1,2));pet.setTame(true,true);pet.setOwnerUUID(caster.getUUID());pet.setNoAi(true);pet.setHealth(9);pet.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.DIAMOND,3));UUID id=pet.getUUID();
        var s=definition("pet_cache");var selected=(SpellEffects.ForEach)s.effects().getFirst();var create=(SpellEffects.CreateManifestation)selected.effects().getFirst();var m=create.manifestation();
        var test=new SpellEffects.Manifestation(m.kind(),m.durationTicks(),m.values(),Map.of("dimension",net.minecraft.world.level.Level.END.location()),m.bindings(),m.onHit(),m.onTick(),m.onEnd(),m.interval(),m.visual());
        var effects=List.<SpellEffect>of(new SpellEffects.CreateManifestation(test,new TargetSpec(TargetSpec.Selection.EVENT_TARGET,new SpellValue.Constant(0))));
        success(h,runtime(h).cast(copy(s,effects),SpellEvent.of(SpellTriggerTypes.INTERACT,caster.getUUID(),new SpellSubject.Entity(id)),List.of(),true));
        check(h,h.getLevel().getServer().getLevel(net.minecraft.world.level.Level.END).getEntity(id) instanceof Wolf cached && cached.getX()>1000000,"Pet was not sheltered");
        runtime(h).dispelActor(caster.getUUID());
        check(h,h.getLevel().getEntity(id) instanceof Wolf returned && returned.getOwnerUUID().equals(caster.getUUID()) && returned.getHealth()==9 && returned.getMainHandItem().getCount()==3 && returned.isNoAi() && returned.distanceToSqr(caster)<16,"Pet return lost UUID, owner, health, equipment or flags");
        h.getLevel().getEntity(id).discard();finish(h,caster);
    }
    @GameTest(template="empty_3x3x3",batch="utility_pet_recovery")
    public static void refusedPetInsertionRestoresOriginalIdentityAndInventory(GameTestHelper h) {
        var caster=caster(h);Wolf pet=h.spawnWithNoFreeWill(EntityType.WOLF,new BlockPos(1,1,2));
        pet.setTame(true,true);pet.setOwnerUUID(caster.getUUID());pet.setHealth(9);pet.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.DIAMOND,3));UUID id=pet.getUUID();
        var s=definition("pet_cache");var selected=(SpellEffects.ForEach)s.effects().getFirst();var create=(SpellEffects.CreateManifestation)selected.effects().getFirst();var m=create.manifestation();
        var test=new SpellEffects.Manifestation(m.kind(),m.durationTicks(),m.values(),Map.of("dimension",net.minecraft.world.level.Level.END.location()),m.bindings(),m.onHit(),m.onTick(),m.onEnd(),m.interval(),m.visual());
        var effects=List.<SpellEffect>of(new SpellEffects.CreateManifestation(test,new TargetSpec(TargetSpec.Selection.EVENT_TARGET,new SpellValue.Constant(0))));
        java.util.function.Consumer<net.neoforged.neoforge.event.entity.EntityJoinLevelEvent> reject=event->{
            if(event.getEntity().getUUID().equals(id) && event.getLevel().dimension().equals(net.minecraft.world.level.Level.END))event.setCanceled(true);
        };
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(reject);
        try {
            var result=runtime(h).cast(copy(s,effects),SpellEvent.of(SpellTriggerTypes.INTERACT,caster.getUUID(),new SpellSubject.Entity(id)),List.of(),true);
            check(h,result.failure().isPresent(),"Refused pet insertion reported success");
            check(h,h.getLevel().getEntity(id) instanceof Wolf restored && restored.getOwnerUUID().equals(caster.getUUID()) && restored.getHealth()==9 && restored.getMainHandItem().getCount()==3,"Failed travel lost the original pet identity or inventory");
            check(h,h.getLevel().getServer().getLevel(net.minecraft.world.level.Level.END).getEntity(id)==null,"Failed travel left a duplicate in shelter");
        } finally { net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(reject); }
        h.getLevel().getEntity(id).discard();finish(h,caster);
    }
    @GameTest(template="empty_3x3x3",batch="utility_passage")
    public static void passageRestoresStoneAndEvacuatesItsOccupant(GameTestHelper h) {
        var caster=caster(h);for(int z=0;z<=6;z++) h.setBlock(new BlockPos(1,0,z),Blocks.STONE);
        for(int z=2;z<=4;z++)for(int y=1;y<=2;y++)h.setBlock(new BlockPos(1,y,z),Blocks.STONE);
        BlockPos entry=h.absolutePos(new BlockPos(1,1,2));
        success(h,cast(h,caster,"magic_passage",new SpellSubject.Block(h.getLevel().dimension(),entry)));
        check(h,h.getLevel().getBlockState(entry).isAir() && h.getLevel().getBlockState(entry.relative(Direction.SOUTH,2).above()).isAir(),"Passage did not open complete volume");
        var occupant=creature(h,1,3);runtime(h).dispelActor(caster.getUUID());
        check(h,h.getLevel().getBlockState(entry).is(Blocks.STONE) && h.getLevel().getBlockState(entry.relative(Direction.SOUTH,2).above()).is(Blocks.STONE),"Passage did not restore owned stone");
        check(h,occupant.getZ()>=h.absolutePos(new BlockPos(1,1,5)).getZ() && h.getLevel().noCollision(occupant,occupant.getBoundingBox()),"Passage entombed its occupant");occupant.discard();finish(h,caster);
    }
    @GameTest(template="empty_3x3x3",batch="utility_shape")
    public static void stoneRecastConservesVolumeAndRejectsOre(GameTestHelper h) {
        var caster=caster(h);BlockPos from=h.absolutePos(new BlockPos(1,1,2)),to=h.absolutePos(new BlockPos(1,1,5));h.getLevel().setBlock(from,Blocks.STONE.defaultBlockState(),3);
        var s=definition("shape_stone");var effects=new ArrayList<SpellEffect>();
        for(SpellEffect effect:s.effects())if(effect instanceof SpellEffects.ForEach each)effects.add(new SpellEffects.ForEach(new TargetSpec(TargetSpec.Selection.EVENT_TARGET,new SpellValue.Constant(0)),each.effects(),each.visual()));else effects.add(effect);
        var spell=copy(s,effects);var cast=runtime(h).cast(spell,SpellEvent.of(SpellTriggerTypes.INTERACT,caster.getUUID(),new SpellSubject.Block(h.getLevel().dimension(),from)),List.of(),true);
        check(h,cast.status()==SpellRuntime.Status.AWAITING_RECAST,"Shape stone did not await destination");
        success(h,runtime(h).cast(spell,SpellEvent.of(SpellTriggerTypes.INTERACT,caster.getUUID(),new SpellSubject.Block(h.getLevel().dimension(),to)),List.of(),true));
        check(h,h.getLevel().getBlockState(from).isAir() && h.getLevel().getBlockState(to).is(Blocks.STONE),"Stone volume not conserved");
        h.getLevel().setBlock(from,Blocks.DIAMOND_ORE.defaultBlockState(),3);
        runtime(h).cast(spell,SpellEvent.of(SpellTriggerTypes.INTERACT,caster.getUUID(),new SpellSubject.Block(h.getLevel().dimension(),from)),List.of(),true);
        var failed=runtime(h).cast(spell,SpellEvent.of(SpellTriggerTypes.INTERACT,caster.getUUID(),position(h,new BlockPos(2,1,5))),List.of(),true);
        check(h,failed.failure().isPresent() && h.getLevel().getBlockState(from).is(Blocks.DIAMOND_ORE),"Shape stone mined ore");finish(h,caster);
    }
    @GameTest(template="empty_3x3x3",batch="utility_transpose")
    public static void groupPlacementValidatesBeforeAnyRecipientMoves(GameTestHelper h) {
        var caster=caster(h);var start=caster.position();
        var failed=cast(h,caster,"collective_transposition",new SpellSubject.Position(h.getLevel().dimension(),start.add(0,8,0)));
        check(h,failed.failure().isPresent() && caster.position().equals(start),"Unsupported group destination partially moved caster");
        for(int x=0;x<3;x++)for(int z=4;z<7;z++)h.setBlock(new BlockPos(x,0,z),Blocks.STONE);
        success(h,cast(h,caster,"collective_transposition",position(h,new BlockPos(2,1,5))));
        check(h,caster.position().distanceToSqr(start)>4,"Valid group placement did not move");finish(h,caster);
    }
    @GameTest(template="empty_3x3x3",batch="utility_air")
    public static void airRescueOnlyPreventsBreathingDamageAndEndsInSafeAir(GameTestHelper h) {
        var caster=caster(h);caster.setPos(caster.position().add(0,0,5));success(h,cast(h,caster,"air_bubble",null));
        caster.hurt(caster.damageSources().drown(),4);check(h,caster.getHealth()==20,"Air rescue did not prevent drowning");
        caster.invulnerableTime=0;caster.hurt(caster.damageSources().generic(),3);check(h,caster.getHealth()==17,"Air rescue became a general ward");
        h.runAfterDelay(4,()->{caster.invulnerableTime=0;caster.hurt(caster.damageSources().drown(),4);check(h,caster.getHealth()==13,"Rescue did not close after breathing resumed");finish(h,caster);});
    }
    private static SpellDefinition definition(String name) { return NativeMagic.spells().spells().get(VestigeMainMod.location("pf2_"+name)); }
    private static SpellDefinition copy(SpellDefinition s,List<SpellEffect> effects) { return new SpellDefinition(s.id(),s.rarity(),s.traditions(),s.traits(),List.of(),s.triggers(),effects,Map.of(),s.source()); }
    private static SpellRuntime.Cast cast(GameTestHelper h,LivingEntity caster,String name,SpellSubject point) {
        var s=definition(name);List<SpellEffect> effects=s.effects();
        if(point!=null){var first=(SpellEffects.ForEach)effects.getFirst();var changed=new ArrayList<>(effects);changed.set(0,new SpellEffects.ForEach(new TargetSpec(TargetSpec.Selection.EVENT_TARGET,new SpellValue.Constant(0)),first.effects(),first.visual()));effects=changed;}
        return runtime(h).cast(copy(s,effects),SpellEvent.of(SpellTriggerTypes.INTERACT,caster.getUUID(),point),List.of(),true);
    }
    private static MinecraftSpellWorld world(GameTestHelper h) { return NativeMagic.session(h.getLevel().getServer()).world(); }
    private static SpellRuntime runtime(GameTestHelper h) { return NativeMagic.session(h.getLevel().getServer()).runtime(); }
    private static Villager caster(GameTestHelper h) {
        for(int x=-3;x<=5;x++)for(int z=0;z<=7;z++)h.setBlock(new BlockPos(x,0,z),Blocks.STONE);
        var caster=creature(h,1,0);caster.setYRot(0);caster.setXRot(0);world(h).registerActor(caster);return caster;
    }
    private static Villager creature(GameTestHelper h,int x,int z) { var entity=h.spawnWithNoFreeWill(EntityType.VILLAGER,new BlockPos(x,1,z));entity.setNoAi(true);entity.setNoGravity(true);return entity; }
    private static SpellSubject.Position position(GameTestHelper h,BlockPos pos) { return new SpellSubject.Position(h.getLevel().dimension(),h.absolutePos(pos).getBottomCenter()); }
    private static <T extends Entity> List<T> owned(GameTestHelper h,LivingEntity caster,Class<T> type) { return h.getLevel().getEntitiesOfClass(type,caster.getBoundingBox().inflate(12),e->world(h).isOwnedBy(e,caster.getUUID())); }
    private static void success(GameTestHelper h,SpellRuntime.Cast cast) { check(h,cast.status()==SpellRuntime.Status.COMPLETED && cast.failure().isEmpty(),"Cast failed: "+cast.status()+cast.failure()); }
    private static void check(GameTestHelper h,boolean result,String message) { if(!result)h.fail(message); }
    private static void finish(GameTestHelper h,LivingEntity caster) { runtime(h).interruptActor(caster.getUUID());runtime(h).dispelActor(caster.getUUID());caster.discard();h.succeed(); }
}
