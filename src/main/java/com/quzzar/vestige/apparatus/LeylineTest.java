package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.definition.*;
import com.quzzar.vestige.magic.effect.SpellEffects;
import com.quzzar.vestige.magic.expression.SpellValue;
import com.quzzar.vestige.magic.runtime.*;
import com.quzzar.vestige.magic.world.NativeMagic;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;
import java.util.*;

@GameTestHolder(VestigeMainMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LeylineTest {
    private static final BlockPos CENTER=new BlockPos(17,13,17);
    private static net.minecraft.resources.ResourceLocation id(String name) { return VestigeMainMod.location(name); }
    private static OfferingBlockEntity structure(GameTestHelper h,LeylineShaping.Geometry geometry) {
        h.setBlock(CENTER,ApparatusBlocks.SPELLSTONE.get());
        for (var p:LeylineStructure.offsets(geometry.innerShape(),geometry.inner(),geometry.innerHeight())) h.setBlock(CENTER.offset(p),ApparatusBlocks.PLINTH.get());
        if (geometry.slots()==8) for (var p:LeylineStructure.offsets(geometry.outerShape(),geometry.outer(),geometry.innerHeight()+geometry.outerStep())) h.setBlock(CENTER.offset(p),ApparatusBlocks.PLINTH.get());
        return (OfferingBlockEntity)h.getBlockEntity(CENTER);
    }
    private static void fill(RitualCrafting.Layout layout,RitualRecipe recipe) {
        recipe.parts().forEach(p -> layout.stands().get(recipe.circle()==4 ? p.seat()*2 : p.seat()).insert(p.ingredient().hint()));
    }
    private static OfferingBlockEntity mixedStructure(GameTestHelper h,LeylineShaping.Geometry geometry) {
        h.setBlock(CENTER,ApparatusBlocks.SPELLSTONES.get(ApparatusMaterials.TUFF).get());
        for(int i=0;i<8;i++) if(geometry.slots()==8 || i%2==0)
            h.setBlock(CENTER.offset(geometry.offset(i)),ApparatusBlocks.PLINTHS.get(ApparatusMaterials.values()[i*4]).get());
        return (OfferingBlockEntity)h.getBlockEntity(CENTER);
    }
    /** Float probes at burst height so ordinary gravity does not move the radius fixtures. */
    private static net.minecraft.world.entity.npc.Villager blastProbe(GameTestHelper h,BlockPos node,Vec3 offset) {
        var creature=EntityType.VILLAGER.create(h.getLevel());
        creature.setNoAi(true);creature.setNoGravity(true);
        creature.setPos(Vec3.atBottomCenterOf(h.absolutePos(node)).add(offset));
        h.getLevel().addFreshEntity(creature);return creature;
    }

    @GameTest(template="empty_35x15x35",batch="leyline_blast_four",timeoutTicks=90)
    public static void fourSlotBackfireReachesDistantInnerNodesAndIgnoresEveryOuterNode(GameTestHelper h) {
        var geometry=new LeylineShaping.Geometry(8,LeylineShaping.Shape.CROSS,8,-6,LeylineShaping.Shape.CROSS,16,-6);
        var center=structure(h,geometry);var layout=RitualCrafting.layout(center);
        var reference=ScrollItems.scroll(id("fireball"));center.insert(reference);
        List<net.minecraft.world.entity.npc.Villager> inner=new ArrayList<>(),outer=new ArrayList<>();
        for(int i=0;i<8;i++) {
            var offset=geometry.offset(i);layout.stands().get(i).insert(new ItemStack(Items.DIRT));
            Vec3 side=new Vec3(-offset.getZ(),0,offset.getX()).normalize().scale(1.25).add(0,.9,0);
            (i%2==0 ? inner : outer).add(blastProbe(h,CENTER.offset(offset),side));
        }
        var gap=blastProbe(h,CENTER,new Vec3(4.5,.9,0));
        h.assertTrue(RitualCrafting.activate(h.makeMockPlayer(GameType.SURVIVAL),center,()->0)==RitualCrafting.Outcome.EXPLOSION_PENDING,"Distant four-slot failure did not queue");
        h.runAfterDelay(35,()->{
            h.assertTrue(inner.stream().allMatch(c->c.getHealth()<20) && outer.stream().allMatch(c->c.getHealth()==20) && gap.getHealth()==20,"Four-slot bursts missed inner nodes or leaked into inactive outer/gap areas");
            for(int i=0;i<8;i++)h.assertTrue(i%2==0 ? layout.stands().get(i).displayedItem().isEmpty() : layout.stands().get(i).displayedItem().is(Items.DIRT),"Failure consumed the wrong layer");
            h.assertTrue(ItemStack.matches(reference,center.displayedItem()) && RitualTestOutput.stack(center).isEmpty(),"Distant failure changed reference/result");h.succeed();
        });
    }

    @GameTest(template="empty_35x15x35",batch="leyline_blast_eight",timeoutTicks=90)
    public static void eightSlotBackfireReachesAllRaisedAndDistantNodesWithLocalRadiusAndOcclusion(GameTestHelper h) {
        var geometry=new LeylineShaping.Geometry(8,LeylineShaping.Shape.CROSS,8,-6,LeylineShaping.Shape.CROSS,16,-6);
        var center=structure(h,geometry);var layout=RitualCrafting.layout(center);
        var recipe=RitualCrafting.catalog().recipes().get(id("pf2_flicker"));var reference=ScrollItems.scroll(recipe.spell());center.insert(reference);
        for(var part:recipe.parts())layout.stands().get(part.seat()).insert(new ItemStack(Items.DIRT));
        layout.stands().get(0).installMaterial(new ItemStack(Items.GOLD_BLOCK));
        List<net.minecraft.world.entity.npc.Villager> probes=new ArrayList<>();
        for(int i=0;i<8;i++) {
            var offset=geometry.offset(i);
            Vec3 side=new Vec3(-offset.getZ(),0,offset.getX()).normalize().scale(1.25).add(0,.9,0);
            probes.add(blastProbe(h,CENTER.offset(offset),side));
        }
        var central=blastProbe(h,CENTER,new Vec3(.75,.9,0));
        var gap=blastProbe(h,CENTER,new Vec3(4.5,.9,0));
        BlockPos outer=CENTER.offset(geometry.offset(1));
        var above=blastProbe(h,outer,new Vec3(0,3.15,0));
        var shielded=blastProbe(h,outer,new Vec3(0,.9,1.8));
        for(int x=-1;x<=1;x++)for(int y=0;y<5;y++)h.setBlock(outer.offset(x,y,1),Blocks.OBSIDIAN);
        var drop=new ItemEntity(h.getLevel(),center.getBlockPos().getX()+.5,center.getBlockPos().getY()+1,center.getBlockPos().getZ()+.5,new ItemStack(Items.DIAMOND));
        drop.setNoGravity(true);h.getLevel().addFreshEntity(drop);
        h.assertTrue(RitualCrafting.activate(h.makeMockPlayer(GameType.SURVIVAL),center,()->0)==RitualCrafting.Outcome.EXPLOSION_PENDING,"Eight-slot failure did not queue");
        h.runAfterDelay(35,()->{
            h.assertTrue(probes.stream().allMatch(c->c.getHealth()<20) && central.getHealth()<20,"Eight-slot failure missed an active node, including an empty recipe position");
            h.assertTrue(gap.getHealth()==20 && above.getHealth()==20 && shielded.getHealth()==20,"Bursts escaped local spheres or ignored wall occlusion");
            h.assertTrue(layout.items().stream().allMatch(ItemStack::isEmpty) && ItemStack.matches(reference,center.displayedItem()) && center.resultItem().isEmpty(),"Eight-slot commitment consumed reference or left an offering");
            h.assertTrue(layout.stands().get(0).materialItem().is(Items.GOLD_BLOCK) && drop.isAlive() && drop.getItem().is(Items.DIAMOND),"Failure consumed a socket or dropped item");
            h.assertTrue(h.getBlockState(outer.offset(0,1,1)).is(Blocks.OBSIDIAN) && layout.blocks().stream().allMatch(b->h.getLevel().getBlockEntity(b.getBlockPos())==b),"Failure damaged terrain or apparatus");h.succeed();
        });
    }

    @GameTest(template="empty_35x15x35",batch="leyline_cosmetic_recipe",timeoutTicks=100)
    public static void mixedFinishesCraftTheUnchangedSpellRecipeWithIdenticalShaping(GameTestHelper h) {
        var geometry=new LeylineShaping.Geometry(4,LeylineShaping.Shape.CROSS,2,0,LeylineShaping.Shape.CROSS,0,0);
        var center=mixedStructure(h,geometry);var layout=RitualCrafting.layout(center);
        var recipe=RitualCrafting.catalog().recipes().get(id("fireball"));fill(layout,recipe);
        var reference=ScrollItems.scroll(recipe.spell());center.insert(reference);
        h.assertTrue(RitualCrafting.activate(h.makeMockPlayer(GameType.SURVIVAL),center)==RitualCrafting.Outcome.CRAFTING,"Mixed Plinths or Tuff Spellstone rejected normal recipe");
        h.runAfterDelay(65,()->{
            var output=ScrollItems.scroll(RitualTestOutput.stack(center)).orElseThrow();
            var expected=LeylineShaping.resolve(geometry,NativeMagic.spells().spells().get(recipe.spell()).traits().ratings().keySet());
            h.assertTrue(output.spell().equals(recipe.spell()) && output.modifiers().equals(expected.traits()) && Math.abs(output.shaping().castingCost()-expected.cost())<1e-12,"Cosmetic carriers changed crafted shaping");
            h.assertTrue(ItemStack.matches(reference,center.displayedItem()) && layout.items().stream().allMatch(ItemStack::isEmpty),"Mixed finish craft lost reference or retained ingredients");
            var player=h.makeMockPlayer(GameType.SURVIVAL);var absolute=center.getBlockPos();
            var dropped=RitualTestOutput.entity(center);dropped.setPickUpDelay(0);dropped.playerTouch(player);
            h.assertTrue(RitualTestOutput.stack(center).isEmpty() && ItemStack.matches(reference,center.displayedItem()),"Tuff result retrieval also removed reference");h.succeed();
        });
    }
    @GameTest(template="empty_35x15x35",batch="leyline_cosmetic_attunement",timeoutTicks=100)
    public static void mixedFinishesPreserveAttunementIdentityAndEightNodeCrafting(GameTestHelper h) {
        var geometry=new LeylineShaping.Geometry(8,LeylineShaping.Shape.DIAGONAL,2,0,LeylineShaping.Shape.CROSS,4,-1);
        var center=structure(h,geometry);var baseline=RitualCrafting.layout(center);
        var ingredients=AttunementShardItem.ingredients();
        for(int i=0;i<ingredients.size();i++) baseline.stands().get(i).insert(new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(ingredients.get(i))));
        baseline.stands().get(0).installMaterial(new ItemStack(Items.GOLD_BLOCK));
        var expected=AttunementShardItem.signature(AttunementShardItem.create(RitualInputs.capture(baseline))).orElseThrow().key();
        for(var node:baseline.stands()) { node.remove();node.removeMaterial(); }
        center=mixedStructure(h,geometry);var mixed=RitualCrafting.layout(center);
        for(int i=0;i<ingredients.size();i++) mixed.stands().get(i).insert(new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(ingredients.get(i))));
        mixed.stands().get(0).installMaterial(new ItemStack(Items.GOLD_BLOCK));
        var actual=AttunementShardItem.signature(AttunementShardItem.create(RitualInputs.capture(mixed))).orElseThrow().key();
        h.assertTrue(actual.equals(expected),"Cosmetic finishes entered the attunement identity");
        h.assertTrue(RitualCrafting.activate(h.makeMockPlayer(GameType.SURVIVAL),center)==RitualCrafting.Outcome.ATTUNING,"Mixed eight-node attunement rejected");
        var resultCenter=center;
        h.runAfterDelay(65,()->{h.assertTrue(AttunementShardItem.signature(RitualTestOutput.stack(resultCenter)).orElseThrow().key().equals(expected) && mixed.items().stream().allMatch(ItemStack::isEmpty) && mixed.stands().get(0).materialItem().is(Items.GOLD_BLOCK),"Attunement craft lost identity or socket");h.succeed();});
    }
    @GameTest(template="empty_35x15x35",batch="leyline_pyramid",timeoutTicks=100)
    public static void fullSizedPyramidCraftStoresExactModifiersAndDoesNotStackTheReference(GameTestHelper h) {
        var geometry=new LeylineShaping.Geometry(8,LeylineShaping.Shape.CROSS,8,-6,LeylineShaping.Shape.CROSS,16,-6);
        var center=structure(h,geometry);var layout=RitualCrafting.layout(center);
        h.assertTrue(layout!=null && layout.geometry().equals(geometry),"Large pyramid did not resolve independent distances/heights");
        var recipe=RitualCrafting.catalog().recipes().get(id("pf2_clairvoyance"));fill(layout,recipe);
        var reference=ScrollItems.shapedScroll(recipe.spell(),new LeylineShaping.Modifiers(1.5,1.5,1.5,2));center.insert(reference);
        var p=h.makeMockPlayer(GameType.SURVIVAL);
        h.assertTrue(RitualCrafting.activate(p,center)==RitualCrafting.Outcome.CRAFTING,"Pyramid recipe rejected");
        h.runAfterDelay(65,()->{
            var output=ScrollItems.scroll(RitualTestOutput.stack(center)).orElseThrow();var definition=NativeMagic.spells().spells().get(recipe.spell());
            var expected=LeylineShaping.resolve(geometry,definition.traits().ratings().keySet());
            h.assertTrue(output.modifiers().equals(expected.traits()) && Math.abs(output.shaping().castingCost()-expected.cost())<1e-12,"Stored modifiers differ or stacked a reference boost");
            h.assertTrue(ItemStack.matches(reference,center.displayedItem()),"Reference changed");
            var restored=new OfferingBlockEntity(center.getBlockPos(),center.getBlockState());restored.loadWithComponents(center.saveWithFullMetadata(h.getLevel().registryAccess()),h.getLevel().registryAccess());
            h.assertTrue(ItemStack.matches(RitualTestOutput.stack(center),ItemStack.parse(h.getLevel().registryAccess(),RitualTestOutput.stack(center).save(h.getLevel().registryAccess())).orElseThrow()) && restored.resultItem().isEmpty(),"Shaped output did not persist");h.succeed();
        });
    }
    @GameTest(template="empty_35x15x35",batch="leyline_outer",timeoutTicks=100)
    public static void fourSlotCraftIgnoresChangingPartialOuterRingAndSupportingTerrain(GameTestHelper h) {
        var geometry=new LeylineShaping.Geometry(4,LeylineShaping.Shape.DIAGONAL,2,-1,LeylineShaping.Shape.CROSS,0,0);
        var c=structure(h,geometry);var l=RitualCrafting.layout(c);var r=RitualCrafting.catalog().recipes().get(id("fireball"));fill(l,r);c.insert(ScrollItems.scroll(r.spell()));
        BlockPos extra=CENTER.offset(0,-2,-7);h.setBlock(extra,ApparatusBlocks.PLINTH.get());var unused=(OfferingBlockEntity)h.getBlockEntity(extra);unused.insert(new ItemStack(Items.DIRT));unused.installMaterial(new ItemStack(Items.GOLD_BLOCK));
        h.assertTrue(RitualCrafting.activate(h.makeMockPlayer(GameType.SURVIVAL),c)==RitualCrafting.Outcome.CRAFTING,"Partial outer affected basic craft");
        h.assertTrue(!unused.busy(),"Unused outer was locked");h.setBlock(extra,Blocks.DIAMOND_BLOCK);h.setBlock(CENTER.offset(2,-2,2),Blocks.OBSIDIAN);
        h.runAfterDelay(65,()->{h.assertTrue(!RitualTestOutput.stack(c).isEmpty() && l.items().stream().allMatch(ItemStack::isEmpty),"Inactive outer or foundation cancelled craft");h.succeed();});
    }
    @GameTest(template="empty_35x15x35",batch="leyline_shapes")
    public static void allFourEightSlotShapesAndBothInnerShapesResolveAndAsymmetryRejects(GameTestHelper h) {
        for (var inner:LeylineShaping.Shape.values()) for (var outer:LeylineShaping.Shape.values()) {
            var g=new LeylineShaping.Geometry(8,inner,2,-2,outer,5,1);var c=structure(h,g);
            var candidates=LeylineStructure.find(c,8);h.assertTrue(candidates.stream().anyMatch(l->l.geometry().equals(g)),"Missing shape "+inner+"/"+outer);
            for (var p:LeylineStructure.offsets(inner,2,-2)) h.setBlock(CENTER.offset(p),Blocks.AIR);
            for (var p:LeylineStructure.offsets(outer,5,-1)) h.setBlock(CENTER.offset(p),Blocks.AIR);
            var basic=g.innerOnly();c=structure(h,basic);h.assertTrue(LeylineStructure.find(c,4).size()==1,"Missing inner shape");
            h.setBlock(CENTER.offset(LeylineStructure.offsets(inner,2,-2).getFirst()),Blocks.AIR);h.assertTrue(LeylineStructure.find(c,4).isEmpty(),"Asymmetric ring accepted");
            for (var p:LeylineStructure.offsets(inner,2,-2)) h.setBlock(CENTER.offset(p),Blocks.AIR);
        }
        h.succeed();
    }
    @GameTest(template="empty_35x15x35",batch="leyline_ambiguous")
    public static void matchingMultipleRingsRejectsSafelyWithoutChoosingAnOptimum(GameTestHelper h) {
        var c=structure(h,new LeylineShaping.Geometry(4,LeylineShaping.Shape.CROSS,2,0,LeylineShaping.Shape.CROSS,0,0));
        var r=RitualCrafting.catalog().recipes().get(id("fireball"));fill(LeylineStructure.find(c,4).getFirst(),r);
        structure(h,new LeylineShaping.Geometry(4,LeylineShaping.Shape.CROSS,2,-2,LeylineShaping.Shape.CROSS,0,0));
        var second=LeylineStructure.find(c,4).stream().filter(l->l.geometry().innerHeight()==-2).findFirst().orElseThrow();fill(second,r);c.insert(ScrollItems.scroll(r.spell()));
        h.assertTrue(RitualCrafting.activate(h.makeMockPlayer(GameType.SURVIVAL),c,()->{throw new AssertionError("Ambiguity rolled risk");})==RitualCrafting.Outcome.INVALID && !c.busy(),"Ambiguous recipe was consumed or arbitrarily selected");h.succeed();
    }
    @GameTest(template="empty_35x15x35",batch="leyline_layer_limits")
    public static void incompatibleOuterHeightDoesNotSkipTheActualInnerLayer(GameTestHelper h) {
        var c=structure(h,new LeylineShaping.Geometry(4,LeylineShaping.Shape.CROSS,2,1,LeylineShaping.Shape.CROSS,0,0));
        for (int radius:new int[]{4,6}) for (var p:LeylineStructure.offsets(LeylineShaping.Shape.CROSS,radius,-6)) h.setBlock(CENTER.offset(p),ApparatusBlocks.PLINTH.get());
        h.assertTrue(LeylineStructure.find(c,4).getFirst().geometry().inner()==2 && LeylineStructure.find(c,8).isEmpty(),"Advanced scan skipped the nearest inner to evade the height bound");h.succeed();
    }
    @GameTest(template="empty_9x3x9",batch="leyline_scroll_data")
    public static void shapedScrollDecodesDiscountsAndRejectsInvalidVersionsAndBounds(GameTestHelper h) {
        var shaped=ScrollItems.shapedScroll(id("heal"),new LeylineShaping.Modifiers(.95,.9,.9,.8));
        h.assertTrue(ScrollItems.scroll(shaped).orElseThrow().shaping().castingCost()==.8,"Stored discount did not decode");
        net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA,shaped,t->t.putDouble("vestige_casting_cost",Double.NaN));
        h.assertTrue(ScrollItems.scroll(shaped).isEmpty(),"Nonfinite cost decoded");
        var version=ScrollItems.shapedScroll(id("heal"),new LeylineShaping.Modifiers(1,1,1,1));
        net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA,version,t->t.putString("vestige_shaping","unknown"));
        h.assertTrue(ScrollItems.scroll(version).isEmpty(),"Unknown shaping version decoded");
        var unversioned=ScrollItems.shapedScroll(id("heal"),new LeylineShaping.Modifiers(1.1,1,1,1));
        net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA,unversioned,t->t.remove("vestige_shaping"));
        h.assertTrue(ScrollItems.scroll(unversioned).isEmpty(),"Obsolete unversioned augmentation format decoded");
        h.assertTrue(!ScrollItems.scroll(ScrollItems.scroll(id("heal"))).orElseThrow().shaping().roundAmounts(),"Base discovery scroll must remain unshaped");h.succeed();
    }
    @GameTest(template="empty_9x3x9",batch="leyline_materials",timeoutTicks=50)
    public static void materialSocketIsIndependentPersistsAndBreakReturnsBothItems(GameTestHelper h) {
        BlockPos pos=new BlockPos(4,1,4);h.setBlock(pos,ApparatusBlocks.PLINTH.get());var n=(OfferingBlockEntity)h.getBlockEntity(pos);
        var p=h.makeMockPlayer(GameType.SURVIVAL);var material=new ItemStack(Items.GOLD_BLOCK,2);p.setItemInHand(InteractionHand.MAIN_HAND,material);
        var absolute=h.absolutePos(pos);var hit=new BlockHitResult(Vec3.atCenterOf(absolute),Direction.NORTH,absolute,false);
        h.getBlockState(pos).useItemOn(material,h.getLevel(),p,InteractionHand.MAIN_HAND,hit);
        h.assertTrue(n.materialItem().is(Items.GOLD_BLOCK) && material.getCount()==1 && n.displayedItem().isEmpty(),"Side click did not install exactly one material");
        n.insert(new ItemStack(Items.DIAMOND));var copy=new OfferingBlockEntity(absolute,n.getBlockState());copy.loadWithComponents(n.getUpdateTag(h.getLevel().registryAccess()),h.getLevel().registryAccess());
        h.assertTrue(copy.materialItem().is(Items.GOLD_BLOCK) && copy.displayedItem().is(Items.DIAMOND),"Socket/offering did not synchronize independently");
        p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);p.setShiftKeyDown(true);h.getBlockState(pos).useWithoutItem(h.getLevel(),p,hit);
        h.assertTrue(n.materialItem().isEmpty() && n.displayedItem().is(Items.DIAMOND),"Material retrieval removed offering");
        n.installMaterial(new ItemStack(Items.IRON_BLOCK));n.lock(50);h.setBlock(pos,Blocks.AIR);
        var drops=h.getLevel().getEntitiesOfClass(ItemEntity.class,h.getBounds());
        h.assertTrue(drops.stream().filter(e->e.getItem().is(Items.IRON_BLOCK)).mapToInt(e->e.getItem().getCount()).sum()==1 && drops.stream().filter(e->e.getItem().is(Items.DIAMOND)).mapToInt(e->e.getItem().getCount()).sum()==1,"Locked break lost/duplicated socket/offering");h.succeed();
    }
    @GameTest(template="empty_9x3x9",batch="leyline_cast",timeoutTicks=100)
    public static void shapedScrollActuallyPaysRoundedCostAndAmplifiesHealing(GameTestHelper h) {
        var p=h.makeMockPlayer(GameType.SURVIVAL);p.teleportTo(h.absolutePos(new BlockPos(4,1,4)).getBottomCenter().x,h.absolutePos(new BlockPos(4,1,4)).getY(),h.absolutePos(new BlockPos(4,1,4)).getBottomCenter().z);
        p.setHealth(10);p.getPersistentData().putDouble("vestige:mana",100);SpellKnowledge.identify(p,id("heal"));
        var scroll=ScrollItems.shapedScroll(id("heal"),new LeylineShaping.Modifiers(1.2,1,1,1.1));p.setItemInHand(InteractionHand.MAIN_HAND,scroll);
        h.assertTrue(ScrollCasting.cast(p,scroll),"Shaped scroll rejected");
        h.runAfterDelay(45,()->{h.assertTrue(Math.abs(p.getHealth()-16)<.01 && p.getPersistentData().getDouble("vestige:mana")==83 && p.getMainHandItem().isEmpty(),"Actual shaped heal/cost/consumption differs: "+p.getHealth()+" / "+p.getPersistentData().getDouble("vestige:mana"));h.succeed();});
    }
    @GameTest(template="empty_9x3x9",batch="leyline_spatial")
    public static void shapedAreaActuallyReachesAnExpandedBoundaryAndRoundsDamage(GameTestHelper h) {
        var caster=h.spawnWithNoFreeWill(EntityType.VILLAGER,new BlockPos(1,1,4));var near=h.spawnWithNoFreeWill(EntityType.VILLAGER,new BlockPos(3,1,4));var outside=h.spawnWithNoFreeWill(EntityType.VILLAGER,new BlockPos(6,1,4));
        var session=NativeMagic.session(h.getLevel().getServer());session.world().registerActor(caster);
        var area=new SpellValue.Product(List.of(new SpellValue.Constant(1.6),new SpellValue.Trait(id("area"))));
        var damage=new SpellEffects.Action(id("damage"),Map.of("amount",new SpellValue.Product(List.of(new SpellValue.Constant(2.25),new SpellValue.Trait(id("amplify"))))),Map.of());
        var each=new SpellEffects.ForEach(new TargetSpec(TargetSpec.Selection.NEARBY_ENTITIES,area,true,Map.of("through_blocks",new SpellValue.Constant(1)),TargetSpec.Relationship.ANY),List.of(damage));
        var spell=new SpellDefinition(id("leyline_boundary_test"),Set.of(Tradition.ARCANE),new TraitProfile(Map.of(id("area"),1.0,id("amplify"),1.0)),List.of(),List.of(new SpellTrigger(id("primary"),SpellTriggerTypes.INTERACT,List.of())),List.of(each));
        var mods=new LeylineShaping.Modifiers(1.2,1,1.5,1);
        var cast=session.runtime().cast(spell,SpellEvent.of(SpellTriggerTypes.INTERACT,caster.getUUID(),null),mods.traits(),true,Optional.empty(),false,new CastShaping(1,true));
        h.assertTrue(cast.status()==SpellRuntime.Status.COMPLETED && near.getHealth()==17 && outside.getHealth()==20,"Rounded area or damage did not apply in Minecraft");h.succeed();
    }
    @GameTest(template="empty_9x3x9",batch="leyline_range")
    public static void shapedRangeActuallyExtendsTheEntityRay(GameTestHelper h) {
        var caster=h.spawnWithNoFreeWill(EntityType.VILLAGER,new BlockPos(1,1,4));
        var target=h.spawnWithNoFreeWill(EntityType.VILLAGER,new BlockPos(4,1,4));
        caster.setYRot(-90);caster.setXRot(0);
        var session=NativeMagic.session(h.getLevel().getServer());session.world().registerActor(caster);
        var range=new SpellValue.Product(List.of(new SpellValue.Constant(2.25),new SpellValue.Trait(id("range"))));
        var damage=new SpellEffects.Action(id("damage"),Map.of("amount",new SpellValue.Constant(1)),Map.of());
        var each=new SpellEffects.ForEach(new TargetSpec(TargetSpec.Selection.ENTITY_RAY,range,false),List.of(damage));
        var spell=new SpellDefinition(id("leyline_range_test"),Set.of(Tradition.ARCANE),new TraitProfile(Map.of(id("range"),1.0)),List.of(),List.of(new SpellTrigger(id("primary"),SpellTriggerTypes.INTERACT,List.of())),List.of(each));
        session.runtime().cast(spell,SpellEvent.of(SpellTriggerTypes.INTERACT,caster.getUUID(),null),List.of(),true,Optional.empty(),false,new CastShaping(1,true));
        h.assertTrue(target.getHealth()==20,"Base two-block ray unexpectedly reached a three-block target");
        var cast=session.runtime().cast(spell,SpellEvent.of(SpellTriggerTypes.INTERACT,caster.getUUID(),null),new LeylineShaping.Modifiers(1,1.5,1,1).traits(),true,Optional.empty(),false,new CastShaping(1,true));
        h.assertTrue(cast.status()==SpellRuntime.Status.COMPLETED && target.getHealth()==19,"Shaped three-block ray did not reach the target");h.succeed();
    }
}
