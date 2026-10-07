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
public final class BlockFormationSpellTest {
    private static final net.minecraft.server.level.TicketType<UUID> TICKET=net.minecraft.server.level.TicketType.create("vestige_formation_test",Comparator.<UUID>naturalOrder());
    private BlockFormationSpellTest() { }
    @GameTest(template="empty_3x3x3",batch="formation_tree",timeoutTicks=260)
    public static void treeGrowsRealLogsAndLeavesThenPreservesMissingAndReplacedCells(GameTestHelper h) {
        var caster=fixture(h);success(h,cast(h,caster,"pf2_protector_tree"));
        h.runAfterDelay(45,()->{
            check(h,count(h,SpellBlocks.TEMPORARY_LOG.get())==4,"Tree did not form four logs");
            check(h,count(h,SpellBlocks.TEMPORARY_LEAVES.get())==17,"Tree canopy did not form");
            h.getLevel().destroyBlock(base(h).above(),true);
            h.getLevel().setBlock(base(h).offset(1,3,0),Blocks.DIAMOND_BLOCK.defaultBlockState(),3);
            h.getLevel().setBlock(base(h).offset(-1,3,0),Blocks.AIR.defaultBlockState(),3);
            h.getLevel().setBlock(base(h).offset(-1,3,0),Blocks.OAK_LEAVES.defaultBlockState(),3);
            check(h,h.getLevel().getEntitiesOfClass(ItemEntity.class,area(h)).isEmpty(),"Conjured tree dropped loot");
        });
        h.runAfterDelay(230,()->{
            check(h,count(h,SpellBlocks.TEMPORARY_LOG.get())==0 && count(h,SpellBlocks.TEMPORARY_LEAVES.get())==0 && displays(h)==0,"Tree cleanup leaked terrain or displays");
            check(h,h.getLevel().getBlockState(base(h).offset(1,3,0)).is(Blocks.DIAMOND_BLOCK),"Tree removed a diamond replacement");
            check(h,h.getLevel().getBlockState(base(h).offset(-1,3,0)).is(Blocks.OAK_LEAVES),"Tree removed replacement leaves");finish(h,caster);
        });
    }
    @GameTest(template="empty_3x3x3",batch="formation_water",timeoutTicks=260)
    public static void waterImmersesWithoutSpreadingAndDrainsOnlyItsOwnedCells(GameTestHelper h) {
        var caster=fixture(h);success(h,cast(h,caster,"pf2_wall_of_water"));
        var swimmer=h.spawnWithNoFreeWill(EntityType.VILLAGER,new BlockPos(1,1,5));swimmer.setNoGravity(true);swimmer.igniteForSeconds(20);
        h.runAfterDelay(35,()->{swimmer.setPos(base(h).getBottomCenter());swimmer.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);});
        h.runAfterDelay(40,()->{
            check(h,count(h,SpellBlocks.TEMPORARY_WATER.get())==15,"Water wall did not form fifteen cells");
            check(h,swimmer.isInWater() && !swimmer.isOnFire(),"Real water did not immerse and extinguish a creature");
            check(h,h.getLevel().getBlockState(base(h).south()).isAir(),"Bound water spread outside its ownership");
            h.getLevel().setBlock(base(h).above(),Blocks.DIAMOND_BLOCK.defaultBlockState(),3);
            h.getLevel().setBlock(base(h).east().above(),Blocks.AIR.defaultBlockState(),3);
            h.getLevel().setBlock(base(h).east().above(),Blocks.WATER.defaultBlockState(),3);
        });
        h.runAfterDelay(230,()->{
            check(h,count(h,SpellBlocks.TEMPORARY_WATER.get())==0,"Water survived expiry");
            check(h,h.getLevel().getBlockState(base(h).above()).is(Blocks.DIAMOND_BLOCK),"Water removed a player block");
            check(h,h.getLevel().getBlockState(base(h).east().above()).is(Blocks.WATER),"Water removed a player source");swimmer.discard();finish(h,caster);
        });
    }
    @GameTest(template="empty_3x3x3",batch="formation_obstruction")
    public static void treeAndWaterRejectObstructionsWithoutPartialPlacement(GameTestHelper h) {
        var caster=fixture(h);h.getLevel().setBlock(base(h).above(3),Blocks.DIAMOND_BLOCK.defaultBlockState(),3);
        check(h,cast(h,caster,"pf2_protector_tree").failure().isPresent(),"Occupied canopy was accepted");
        h.getLevel().setBlock(base(h).above(),Blocks.DIAMOND_BLOCK.defaultBlockState(),3);
        check(h,cast(h,caster,"pf2_wall_of_water").failure().isPresent(),"Occupied water cell was accepted");
        check(h,count(h,SpellBlocks.TEMPORARY_LOG.get())==0 && count(h,SpellBlocks.TEMPORARY_WATER.get())==0,"Rejected formation partially placed blocks");
        h.getLevel().setBlock(base(h).above(3),Blocks.AIR.defaultBlockState(),3);h.getLevel().setBlock(base(h).above(),Blocks.AIR.defaultBlockState(),3);
        caster.setPos(base(h).west().getBottomCenter());success(h,cast(h,caster,"pf2_protector_tree"));
        finish(h,caster);
    }
    @GameTest(template="empty_3x3x3",batch="formation_orphans",timeoutTicks=60)
    public static void orphanLogsLeavesAndWaterClearWithoutLootOrSpread(GameTestHelper h) {
        var caster=fixture(h);
        // Other GameTests can leave ordinary loot inside this deliberately wide search area.
        var unrelated=new ItemEntity(h.getLevel(),base(h).getX()-2+.5,base(h).getY()+.5,base(h).getZ()+.5,
                new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STONE_BUTTON));
        unrelated.setNoGravity(true);unrelated.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        h.getLevel().addFreshEntity(unrelated);
        var initialItems=h.getLevel().getEntitiesOfClass(ItemEntity.class,area(h)).stream().collect(java.util.stream.Collectors.toMap(Entity::getUUID,e->e.getItem().copy()));
        h.getLevel().setBlock(base(h),SpellBlocks.TEMPORARY_LOG.get().defaultBlockState(),3);
        h.getLevel().setBlock(base(h).above(),SpellBlocks.TEMPORARY_LEAVES.get().defaultBlockState(),3);
        h.getLevel().setBlock(base(h).east(),SpellBlocks.TEMPORARY_WATER.get().defaultBlockState(),3);
        h.runAfterDelay(25,()->{
            check(h,h.getLevel().getBlockState(base(h)).isAir() && h.getLevel().getBlockState(base(h).above()).isAir() && h.getLevel().getBlockState(base(h).east()).isAir(),"Orphan cells remained");
            check(h,h.getLevel().getBlockState(base(h).east().south()).isAir(),"Orphan water spread");
            check(h,h.getLevel().getEntitiesOfClass(ItemEntity.class,area(h)).stream().allMatch(item->{
                var initial=initialItems.get(item.getUUID());
                return initial!=null && net.minecraft.world.item.ItemStack.isSameItemSameComponents(initial,item.getItem())
                        && item.getItem().getCount()<=initial.getCount();
            }),"Orphans created new loot");
            check(h,!unrelated.isRemoved(),"Cleanup test removed unrelated existing loot");
            unrelated.discard();finish(h,caster);
        });
    }
    @GameTest(template="empty_3x3x3",batch="formation_unload",timeoutTicks=100)
    public static void unloadingCleansTreeAndWaterAndPreservesEdits(GameTestHelper h) {
        var caster=fixture(h);success(h,cast(h,caster,"pf2_protector_tree"));
        h.runAfterDelay(45,()->{
            h.getLevel().setBlock(base(h).above(2),Blocks.DIAMOND_BLOCK.defaultBlockState(),3);
            world(h).features.unloading(h.getLevel().getChunkAt(base(h)));
            check(h,count(h,SpellBlocks.TEMPORARY_LOG.get())==0 && count(h,SpellBlocks.TEMPORARY_LEAVES.get())==0 && displays(h)==0,"Unload leaked tree");
            check(h,h.getLevel().getBlockState(base(h).above(2)).is(Blocks.DIAMOND_BLOCK),"Unload deleted replacement");
            h.getLevel().setBlock(base(h).above(2),Blocks.AIR.defaultBlockState(),3);
            success(h,cast(h,caster,"pf2_wall_of_water"));
        });
        h.runAfterDelay(80,()->{world(h).features.unloading(h.getLevel().getChunkAt(base(h)));check(h,count(h,SpellBlocks.TEMPORARY_WATER.get())==0,"Unload leaked water");finish(h,caster);});
    }
    @GameTest(template="empty_3x3x3",batch="formation_images",timeoutTicks=70)
    public static void entityImagesFollowConsumeAndCleanupWithoutDuplicatingActors(GameTestHelper h) {
        var caster=fixture(h);success(h,cast(h,caster,"pf2_mirror_image"));
        var echoes=h.getLevel().getEntitiesOfClass(SpellEcho.class,area(h));
        check(h,echoes.size()==3 && echoes.stream().allMatch(e->e.source()==caster && !e.isPickable() && !e.shouldBeSaved()),"Copies lack live identity or behave as actors");
        var positions=echoes.stream().map(Entity::position).toList();caster.setPos(caster.position().add(1,0,0));
        h.runAfterDelay(2,()->{
            for(int i=0;i<echoes.size();i++)check(h,echoes.get(i).position().distanceToSqr(positions.get(i).add(1,0,0))<.01,"Image did not follow caster");
            var attacker=h.spawnWithNoFreeWill(EntityType.VILLAGER,new BlockPos(2,1,0));
            caster.hurt(caster.damageSources().mobAttack(attacker),4);
            check(h,caster.getHealth()==20 && echoes.stream().filter(e->!e.isRemoved()).count()==2,"Hit did not consume exactly one copy");
            runtime(h).dispelActor(caster.getUUID());check(h,echoes.stream().allMatch(Entity::isRemoved),"Dispel leaked image entities");attacker.discard();finish(h,caster);
        });
    }
    @GameTest(template="empty_3x3x3",batch="formation_companion",timeoutTicks=180)
    public static void summonedWolfAcquiresAndDamagesAHostileForNpcOwner(GameTestHelper h) {
        for(int x=-5;x<=7;x++) for(int z=-1;z<=7;z++) for(int y=1;y<=6;y++) h.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        var caster=fixture(h);var enemy=h.spawnWithNoFreeWill(EntityType.ZOMBIE,new BlockPos(1,1,3));enemy.setNoAi(true);var helmet=new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.LEATHER_HELMET);helmet.set(net.minecraft.core.component.DataComponents.UNBREAKABLE,new net.minecraft.world.item.component.Unbreakable(true));enemy.setItemSlot(EquipmentSlot.HEAD,helmet);
        boolean[] wolfDamage={false};
        // Vanilla forgets its last attacker after 100 ticks; observe attribution when the hit happens.
        h.onEachTick(()->{if(enemy.getHealth()<20 && enemy.getLastHurtByMob() instanceof net.minecraft.world.entity.animal.Wolf && !enemy.isOnFire())wolfDamage[0]=true;});
        var source=NativeMagic.spells().spells().get(VestigeMainMod.location("pf2_summon_animal"));
        var spell=new SpellDefinition(source.id(),source.rarity(),source.traditions(),source.traits(),List.of(),source.triggers(),source.effects(),Map.of(),source.source());
        var cast=runtime(h).cast(spell,SpellEvent.of(SpellTriggerTypes.INTERACT,caster.getUUID(),new SpellSubject.Entity(caster.getUUID())),List.of(),true);
        check(h,cast.failure().isEmpty(),"Summon failed");
        h.runAfterDelay(10,()->{
            var wolves=h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.animal.Wolf.class,area(h));
            check(h,wolves.size()==1 && wolves.getFirst().getTarget()==enemy,"Owned wolf did not acquire hostile; wolves="+wolves.size()+", target="+(wolves.isEmpty()?"none":wolves.getFirst().getTarget()));
        });
        h.runAfterDelay(160,()->{
            check(h,wolfDamage[0] && !enemy.isOnFire(),"Owned wolf did not deal actual combat damage; health="+enemy.getHealth()+", last="+enemy.getLastHurtByMob()+", burning="+enemy.isOnFire()+", wolves="+h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.animal.Wolf.class,area(h)).stream().map(w->w.position()+" target="+w.getTarget()+" sitting="+w.isInSittingPose()).toList());
            runtime(h).dispelActor(caster.getUUID());check(h,h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.animal.Wolf.class,area(h)).isEmpty(),"Companion leaked after dispel");enemy.discard();finish(h,caster);
        });
    }
    private static Villager fixture(GameTestHelper h) {
        for(int x=-5;x<=7;x++) for(int z=-1;z<=7;z++) h.setBlock(new BlockPos(x,0,z),Blocks.STONE);
        var caster=h.spawnWithNoFreeWill(EntityType.VILLAGER,new BlockPos(1,1,0));caster.setNoGravity(true);caster.setYRot(0);
        h.getLevel().getChunkSource().addRegionTicket(TICKET,new net.minecraft.world.level.ChunkPos(base(h)),2,caster.getUUID());
        world(h).registerActor(caster);return caster;
    }
    private static SpellRuntime.Cast cast(GameTestHelper h,Villager caster,String id) {
        var source=NativeMagic.spells().spells().get(VestigeMainMod.location(id));
        var first=(SpellEffects.ForEach)source.effects().getFirst();
        var effects=List.<SpellEffect>of(new SpellEffects.ForEach(new TargetSpec(id.equals("pf2_mirror_image")?TargetSpec.Selection.SELF:TargetSpec.Selection.EVENT_TARGET,new SpellValue.Constant(0)),first.effects(),first.visual()));
        var spell=new SpellDefinition(source.id(),source.rarity(),source.traditions(),source.traits(),List.of(),source.triggers(),effects,Map.of(),source.source());
        return runtime(h).cast(spell,SpellEvent.of(SpellTriggerTypes.INTERACT,caster.getUUID(),new SpellSubject.Position(h.getLevel().dimension(),base(h).getBottomCenter())),List.of(),true);
    }
    private static BlockPos base(GameTestHelper h) { return h.absolutePos(new BlockPos(1,1,5)); }
    private static AABB area(GameTestHelper h) { return new AABB(base(h)).inflate(8); }
    private static int count(GameTestHelper h,net.minecraft.world.level.block.Block block) { int n=0;for(int x=-3;x<=3;x++) for(int z=-3;z<=3;z++) for(int y=0;y<6;y++) if(h.getLevel().getBlockState(base(h).offset(x,y,z)).is(block)) n++;return n; }
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
