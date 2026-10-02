package com.quzzar.vestige.magic.world;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.definition.*;
import com.quzzar.vestige.magic.effect.SpellEffects;
import com.quzzar.vestige.magic.expression.SpellValue;
import com.quzzar.vestige.magic.runtime.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.*;
import java.util.*;

@GameTestHolder(VestigeMainMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BlockWallSpellTest {
    private static final net.minecraft.server.level.TicketType<UUID> TICKET=net.minecraft.server.level.TicketType.create("vestige_ice_test",Comparator.<UUID>naturalOrder());
    private BlockWallSpellTest() { }
    @GameTest(template="empty_3x3x3",batch="ice_rise",timeoutTicks=260)
    public static void realIceRisesAndSafelyLiftsOccupant(GameTestHelper h) {
        var caster=fixture(h);BlockPos base=base(h);
        var occupant=h.spawnWithNoFreeWill(EntityType.VILLAGER,new BlockPos(1,1,5));occupant.setNoAi(false);
        occupant.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED).setBaseValue(0);
        double before=occupant.getY();success(h,cast(h,caster));
        h.runAfterDelay(5,()->check(h,occupant.getY()>before+.3,"Wall did not progressively lift its occupant"));
        h.runAfterDelay(35,()->{
            check(h,count(h)==15,"Wall did not form fifteen real ice blocks");
            check(h,occupant.getY()>=base.getY()+3,"Occupant was left inside the wall");
            check(h,!h.getLevel().noCollision(caster,new AABB(base)),"Wall has no block collision");
            check(h,displays(h)==0,"Rising displays leaked after block placement");
        });
        h.runAfterDelay(230,()->{
            check(h,occupant.getY()<base.getY()+.1 && occupant.getY()>=base.getY(),"Occupant did not return to the ground after collapse");
            check(h,occupant.isAlive(),"The wall killed its occupant");finish(h,caster);
        });
    }
    @GameTest(template="empty_3x3x3",batch="ice_expiry",timeoutTicks=260)
    public static void collapsePreservesHolesAndDifferentAndSameIceReplacements(GameTestHelper h) {
        var caster=fixture(h);BlockPos base=base(h);success(h,cast(h,caster));
        h.runAfterDelay(35,()->{
            h.getLevel().destroyBlock(base,true);
            h.getLevel().setBlock(base.west().above(),Blocks.GOLD_BLOCK.defaultBlockState(),3);
            h.getLevel().setBlock(base.east().above(),Blocks.AIR.defaultBlockState(),3);
            h.getLevel().setBlock(base.east().above(),Blocks.ICE.defaultBlockState(),3);
            check(h,h.getLevel().getEntitiesOfClass(ItemEntity.class,area(h)).isEmpty(),"Conjured ice dropped a harvestable item");
        });
        h.runAfterDelay(205,()->check(h,displays(h)>0,"Expiry did not animate the surviving blocks; blocks="+count(h)+", own="+world(h).features.ownsTemporaryBlock(h.getLevel(),base.above(2))));
        h.runAfterDelay(230,()->{
            check(h,count(h)==0 && displays(h)==0,"Expired ice or collapse displays leaked");
            check(h,h.getLevel().getBlockState(base).isAir(),"Broken cell was refilled");
            check(h,h.getLevel().getBlockState(base.west().above()).is(Blocks.GOLD_BLOCK),"Player replacement was removed");
            check(h,h.getLevel().getBlockState(base.east().above()).is(Blocks.ICE),"Replacement ice was mistaken for spell-owned ice");finish(h,caster);
        });
    }
    @GameTest(template="empty_3x3x3",batch="ice_dispose",timeoutTicks=100)
    public static void dispelDuringFormationAndAfterEditingLeavesNoBlocksOrDisplays(GameTestHelper h) {
        var caster=fixture(h);success(h,cast(h,caster));
        h.runAfterDelay(5,()->{
            check(h,displays(h)>0,"Formation animation never started");runtime(h).dispelActor(caster.getUUID());
            check(h,count(h)==0 && displays(h)==0,"Dispel leaked a partial wall");success(h,cast(h,caster));
        });
        h.runAfterDelay(45,()->{
            check(h,count(h)==15,"Second formation failed");
            h.getLevel().setBlock(base(h),Blocks.GOLD_BLOCK.defaultBlockState(),3);
            runtime(h).dispelActor(caster.getUUID());
            check(h,count(h)==0 && displays(h)==0,"Cleanup leaked blocks or displays");
            check(h,h.getLevel().getBlockState(base(h)).is(Blocks.GOLD_BLOCK),"Cleanup overwrote a replacement");finish(h,caster);
        });
    }
    @GameTest(template="empty_3x3x3",batch="ice_obstacle")
    public static void obstructedPlacementFailsWithoutReplacingTerrain(GameTestHelper h) {
        var caster=fixture(h);h.getLevel().setBlock(base(h).above(),Blocks.GOLD_BLOCK.defaultBlockState(),3);
        var result=cast(h,caster);check(h,result.failure().isPresent(),"Occupied wall volume was accepted");
        check(h,count(h)==0 && displays(h)==0,"Rejected wall partially formed");
        check(h,h.getLevel().getBlockState(base(h).above()).is(Blocks.GOLD_BLOCK),"Rejected wall replaced terrain");finish(h,caster);
    }
    @GameTest(template="empty_3x3x3",batch="ice_ceiling")
    public static void insufficientLiftingHeadroomRejectsWall(GameTestHelper h) {
        var caster=fixture(h);h.spawnWithNoFreeWill(EntityType.VILLAGER,new BlockPos(1,1,5));
        h.getLevel().setBlock(base(h).above(4),Blocks.STONE.defaultBlockState(),3);
        check(h,cast(h,caster).failure().isPresent(),"Wall accepted a lift into a low ceiling");
        check(h,count(h)==0,"Unsafe lift partially placed blocks");finish(h,caster);
    }
    @GameTest(template="empty_3x3x3",batch="ice_unload",timeoutTicks=100)
    public static void unloadRemovesOwnedCellsAndPreservesEdits(GameTestHelper h) {
        var caster=fixture(h);success(h,cast(h,caster));
        h.runAfterDelay(35,()->{
            h.getLevel().setBlock(base(h),Blocks.GOLD_BLOCK.defaultBlockState(),3);
            world(h).features.unloading(h.getLevel().getChunkAt(base(h)));
            check(h,count(h)==0 && displays(h)==0,"Chunk unload leaked transient terrain");
            check(h,h.getLevel().getBlockState(base(h)).is(Blocks.GOLD_BLOCK),"Unload removed a replacement");finish(h,caster);
        });
    }
    @GameTest(template="empty_3x3x3",batch="ice_orphan",timeoutTicks=60)
    public static void orphanConjuredIceClearsWithoutWaterOrLoot(GameTestHelper h) {
        var caster=fixture(h);BlockPos pos=base(h);h.getLevel().setBlock(pos,SpellBlocks.TEMPORARY_ICE.get().defaultBlockState(),3);
        h.runAfterDelay(25,()->{
            check(h,h.getLevel().getBlockState(pos).isAir(),"Orphan ice became permanent terrain");
            check(h,h.getLevel().getEntitiesOfClass(ItemEntity.class,area(h)).isEmpty(),"Orphan cleanup dropped items");finish(h,caster);
        });
    }
    @GameTest(template="empty_3x3x3",batch="ice_shutdown",timeoutTicks=240)
    public static void shutdownDuringCollapseClearsAnimationsAndPreservesEdits(GameTestHelper h) {
        var caster=fixture(h);success(h,cast(h,caster));
        h.runAfterDelay(35,()->h.getLevel().setBlock(base(h),Blocks.GOLD_BLOCK.defaultBlockState(),3));
        h.runAfterDelay(205,()->{
            check(h,displays(h)>0,"Collapse was not active before shutdown");
            world(h).features.close();
            check(h,count(h)==0 && displays(h)==0,"Shutdown leaked a queued collapse");
            check(h,h.getLevel().getBlockState(base(h)).is(Blocks.GOLD_BLOCK),"Shutdown removed a replacement");finish(h,caster);
        });
    }
    private static Villager fixture(GameTestHelper h) {
        for(int x=-5;x<=7;x++) for(int z=-1;z<=7;z++) h.setBlock(new BlockPos(x,0,z),Blocks.STONE);
        var caster=h.spawnWithNoFreeWill(EntityType.VILLAGER,new BlockPos(1,1,0));caster.setNoGravity(true);caster.setYRot(0);
        h.getLevel().getChunkSource().addRegionTicket(TICKET,new net.minecraft.world.level.ChunkPos(base(h)),2,caster.getUUID());
        world(h).registerActor(caster);return caster;
    }
    private static SpellRuntime.Cast cast(GameTestHelper h,Villager caster) {
        var source=NativeMagic.spells().spells().get(VestigeMainMod.location("pf2_wall_of_ice"));
        var first=(SpellEffects.ForEach)source.effects().getFirst();
        var effects=List.<SpellEffect>of(new SpellEffects.ForEach(new TargetSpec(TargetSpec.Selection.EVENT_TARGET,new SpellValue.Constant(0)),first.effects(),first.visual()));
        var spell=new SpellDefinition(source.id(),source.rarity(),source.traditions(),source.traits(),List.of(),source.triggers(),effects,Map.of(),source.source());
        return runtime(h).cast(spell,SpellEvent.of(SpellTriggerTypes.INTERACT,caster.getUUID(),new SpellSubject.Position(h.getLevel().dimension(),base(h).getBottomCenter())),List.of(),true);
    }
    private static BlockPos base(GameTestHelper h) { return h.absolutePos(new BlockPos(1,1,5)); }
    private static AABB area(GameTestHelper h) { return new AABB(base(h)).inflate(8); }
    private static int count(GameTestHelper h) { int n=0;for(int x=-2;x<=2;x++) for(int y=0;y<3;y++) if(h.getLevel().getBlockState(base(h).offset(x,y,0)).is(SpellBlocks.TEMPORARY_ICE.get())) n++;return n; }
    private static int displays(GameTestHelper h) { return h.getLevel().getEntitiesOfClass(SpellBlockDisplay.class,area(h)).size(); }
    private static MinecraftSpellWorld world(GameTestHelper h) { return NativeMagic.session(h.getLevel().getServer()).world(); }
    private static SpellRuntime runtime(GameTestHelper h) { return NativeMagic.session(h.getLevel().getServer()).runtime(); }
    private static void success(GameTestHelper h,SpellRuntime.Cast cast) { check(h,cast.failure().isEmpty() && cast.status()==SpellRuntime.Status.COMPLETED,"Cast failed: "+cast.failure()); }
    private static void check(GameTestHelper h,boolean condition,String message) { if(!condition) h.fail(message); }
    private static void finish(GameTestHelper h,Villager caster) {
        runtime(h).dispelActor(caster.getUUID());
        h.getLevel().getChunkSource().removeRegionTicket(TICKET,new net.minecraft.world.level.ChunkPos(base(h)),2,caster.getUUID());
        caster.discard();h.succeed();
    }
}
