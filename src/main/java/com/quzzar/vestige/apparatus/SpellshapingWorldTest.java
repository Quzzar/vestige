package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.definition.*;
import com.quzzar.vestige.magic.effect.SpellEffects;
import com.quzzar.vestige.magic.expression.SpellValue;
import com.quzzar.vestige.magic.runtime.*;
import com.quzzar.vestige.magic.world.NativeMagic;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.*;
import java.util.*;

@GameTestHolder(VestigeMainMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SpellshapingWorldTest {
    private static final BlockPos CENTER=new BlockPos(4,1,4);
    private static final LeylineShaping.Geometry GEOMETRY=new LeylineShaping.Geometry(8,LeylineShaping.Shape.CROSS,2,0,LeylineShaping.Shape.DIAGONAL,3,0);
    private static net.minecraft.resources.ResourceLocation id(String n){return VestigeMainMod.location(n);}
    private static Spellshaping.Selection augment(String n){return new Spellshaping.Selection(id(n),1);}
    private static OfferingBlockEntity structure(GameTestHelper h,boolean outer){
        h.setBlock(CENTER,ApparatusBlocks.SPELLSTONE.get());for(int i=0;i<8;i++)if(outer || i%2==0)h.setBlock(CENTER.offset(GEOMETRY.offset(i)),ApparatusBlocks.PLINTH.get());return (OfferingBlockEntity)h.getBlockEntity(CENTER);
    }
    private static void fill(RitualCrafting.Layout l,String name){var r=RitualCrafting.catalog().recipes().get(id(name));for(var p:r.parts())l.stands().get(r.circle()==4 ? p.seat()*2 : p.seat()).insert(p.ingredient().hint());}
    private static void fillShard(RitualCrafting.Layout l){
        var seats=List.of(0,1,2,4,5,7);var ingredients=AttunementShardItem.ingredients();
        for(int i=0;i<ingredients.size();i++)l.stands().get(seats.get(i)).insert(new ItemStack(BuiltInRegistries.ITEM.get(ingredients.get(i))));
    }
    private static void coloredMaterialCraft(GameTestHelper h,String spell,Item material,String expectedAugment){
        var recipe=RitualCrafting.catalog().recipes().get(id(spell));
        var center=structure(h,recipe.circle()==8);var layout=RitualCrafting.layout(center);fill(layout,spell);
        var paper=layout.stands().stream().filter(Objects::nonNull).filter(n->n.displayedItem().is(Items.PAPER)).findFirst().orElseThrow();
        h.assertTrue(paper.installMaterial(new ItemStack(material)),"Colored imbuement did not install");
        h.assertTrue(RitualCrafting.activate(h.makeMockPlayer(GameType.SURVIVAL),center)==RitualCrafting.Outcome.CRAFTING,"Colored imbuement rejected ordinary recipe");
        h.runAfterDelay(65,()->{
            var result=ScrollItems.scroll(RitualTestOutput.stack(center)).orElseThrow();
            h.assertTrue(result.augments().equals(List.of(augment(expectedAugment))),"Color changed the selected augment: "+result.augments());
            h.assertTrue(paper.materialItem().is(material) && paper.displayedItem().isEmpty(),"Craft changed or consumed the colored socket material");
            h.succeed();
        });
    }
    @GameTest(template="empty_9x3x9",batch="spellshaping_colored_wool",timeoutTicks=100)
    public static void redWoolCraftsQuietingFireballAndRetainsItsActualColor(GameTestHelper h){
        coloredMaterialCraft(h,"fireball",Items.RED_WOOL,"quieting");
    }
    @GameTest(template="empty_9x3x9",batch="spellshaping_colored_concrete",timeoutTicks=100)
    public static void blueConcreteCraftsVeiledDetectMagicAndRetainsItsActualColor(GameTestHelper h){
        coloredMaterialCraft(h,"pf2_detect_magic",Items.BLUE_CONCRETE,"veiled");
    }
    @GameTest(template="empty_9x3x9",batch="attunement_imbuement_colors",timeoutTicks=100)
    public static void coloredSocketsCraftTheSameShardKeyAndPersistTheirActualMaterials(GameTestHelper h){
        var center=structure(h,true);var layout=RitualCrafting.layout(center);fillShard(layout);
        var wool=layout.stands().get(0);var concrete=layout.stands().get(3);
        wool.installMaterial(new ItemStack(Items.WHITE_WOOL));concrete.installMaterial(new ItemStack(Items.WHITE_CONCRETE));
        var white=AttunementShardItem.signature(AttunementShardItem.create(RitualInputs.capture(layout))).orElseThrow();
        wool.removeMaterial();concrete.removeMaterial();
        h.assertTrue(wool.installMaterial(new ItemStack(Items.RED_WOOL)) && concrete.installMaterial(new ItemStack(Items.BLUE_CONCRETE)),"Color variants did not install");
        h.assertTrue(AttunementShardItem.signature(AttunementShardItem.create(RitualInputs.capture(layout))).orElseThrow().key().equals(white.key()),"Imbuement color entered the key");
        h.assertTrue(RitualCrafting.activate(h.makeMockPlayer(GameType.SURVIVAL),center)==RitualCrafting.Outcome.ATTUNING,"Colored shard recipe rejected");
        h.runAfterDelay(65,()->{
            var output=RitualTestOutput.stack(center);var signature=AttunementShardItem.signature(output).orElseThrow();
            h.assertTrue(signature.key().equals(white.key()),"Crafted key differs from white materials");
            boolean red=signature.nodes().stream().anyMatch(n->n.material().equals(Optional.of(net.minecraft.resources.ResourceLocation.withDefaultNamespace("red_wool"))));
            boolean blue=signature.nodes().stream().anyMatch(n->n.count()==0 && n.material().equals(Optional.of(net.minecraft.resources.ResourceLocation.withDefaultNamespace("blue_concrete"))));
            h.assertTrue(red && blue,"Shard lost actual-color blueprint materials");
            var saved=ItemStack.parse(h.getLevel().registryAccess(),output.save(h.getLevel().registryAccess())).orElseThrow();
            h.assertTrue(AttunementShardItem.signature(saved).orElseThrow().equals(signature),"Colored blueprint/key did not round-trip");
            h.assertTrue(wool.materialItem().is(Items.RED_WOOL) && concrete.materialItem().is(Items.BLUE_CONCRETE) && layout.items().stream().allMatch(ItemStack::isEmpty),"Craft consumed or recolored the sockets");
            h.succeed();
        });
    }
    @GameTest(template="empty_9x3x9",batch="spellshaping_craft",timeoutTicks=100)
    public static void localShapingCraftsPersistsPreservesSocketsAndIgnoresOuterAndReferenceAugments(GameTestHelper h){
        var center=structure(h,true);var full=RitualCrafting.layout(center);fill(full,"fireball");
        full.stands().get(2).installMaterial(new ItemStack(Items.END_STONE));full.stands().get(4).installMaterial(new ItemStack(Items.BONE_BLOCK));full.stands().get(6).installMaterial(new ItemStack(Items.COPPER_BLOCK));
        full.stands().get(1).insert(new ItemStack(Items.PAPER));full.stands().get(1).installMaterial(new ItemStack(Items.GOLD_BLOCK));
        var reference=ScrollItems.shapedScroll(id("fireball"),new LeylineShaping.Modifiers(1.5,1.5,1.5,2),List.of(augment("greedy")));center.insert(reference);
        h.assertTrue(RitualCrafting.activate(h.makeMockPlayer(GameType.SURVIVAL),center)==RitualCrafting.Outcome.CRAFTING,"Mixed shaping failed");
        h.runAfterDelay(65,()->{
            var output=ScrollItems.scroll(RitualTestOutput.stack(center)).orElseThrow();h.assertTrue(output.augments().equals(List.of(augment("bleeding"),augment("reaching"),augment("shocking"))),"Local augments copied/stacked or ignored: "+output.augments());
            h.assertTrue(ItemStack.matches(center.displayedItem(),reference),"Reference was changed");h.assertTrue(full.stands().get(1).displayedItem().is(Items.PAPER),"Inactive outer slot consumed");
            h.assertTrue(full.stands().get(4).materialItem().is(Items.BONE_BLOCK) && full.stands().get(4).displayedItem().isEmpty(),"Socket was consumed with offering");
            var restored=ItemStack.parse(h.getLevel().registryAccess(),RitualTestOutput.stack(center).save(h.getLevel().registryAccess())).orElseThrow();h.assertTrue(ScrollItems.scroll(restored).orElseThrow().equals(output),"Augments did not round-trip");
            h.assertTrue(!RitualTestOutput.stack(center).getHoverName().getString().contains("Bleeding"),"Server item name exposed an undiscovered augment");h.succeed();
        });
    }
    @GameTest(template="empty_9x3x9",batch="spellshaping_reject",timeoutTicks=80)
    public static void incompatiblePairingRejectsBeforePaymentOrIngredients(GameTestHelper h){
        var center=structure(h,false);var l=RitualCrafting.layout(center);fill(l,"teleport");l.stands().get(4).installMaterial(new ItemStack(Items.COPPER_BLOCK));
        // Compass/Copper is currently neutral. Feather/Copper is Hurried, then Paper/Gold requires a live Amplify consumer.
        l.stands().get(0).installMaterial(new ItemStack(Items.GOLD_BLOCK));
        h.assertTrue(RitualCrafting.activate(h.makeMockPlayer(GameType.SURVIVAL),center)==RitualCrafting.Outcome.INVALID,"No-op output was accepted");
        h.runAfterDelay(65,()->{h.assertTrue(l.items().stream().filter(s->!s.isEmpty()).count()==4 && RitualTestOutput.stack(center).isEmpty() && !center.busy(),"Rejected shaping consumed or locked inputs");h.succeed();});
    }
    @GameTest(template="empty_9x3x9",batch="attunement_craft",timeoutTicks=170)
    public static void sixOfferingsOnEightNodesCraftReproducibleShardAndWholeRotationKeepsKey(GameTestHelper h){
        var center=structure(h,true);var l=RitualCrafting.layout(center);var p=h.makeMockPlayer(GameType.SURVIVAL);fillShard(l);
        for(int i=0;i<8;i++)l.stands().get(i).installMaterial(new ItemStack(i%2==0 ? Items.COPPER_BLOCK : Items.IRON_BLOCK));
        var signature=AttunementShardItem.signature(AttunementShardItem.create(RitualInputs.capture(l))).orElseThrow();String expected=signature.key();
        h.assertTrue(signature.nodes().stream().filter(n->n.count()==0).count()==2,"Empty seats missing from blueprint");
        h.assertTrue(RitualCrafting.activate(p,center)==RitualCrafting.Outcome.ATTUNING,"Eight-slot shard recipe failed");
        h.runAfterDelay(65,()->{
            var first=RitualTestOutput.take(center);h.assertTrue(AttunementShardItem.signature(first).orElseThrow().key().equals(expected) && l.items().stream().allMatch(ItemStack::isEmpty),"Shard changed key or failed to consume all six offerings");
            var saved=ItemStack.parse(h.getLevel().registryAccess(),first.save(h.getLevel().registryAccess())).orElseThrow();h.assertTrue(AttunementShardItem.signature(saved).orElseThrow().key().equals(expected),"Shard blueprint did not persist");
            for(var node:signature.nodes()){
                var rotated=node.rotate();int seat=java.util.stream.IntStream.range(0,8).filter(i->GEOMETRY.offset(i).equals(rotated.offset())).findFirst().orElseThrow();
                if(rotated.count()!=0)l.stands().get(seat).insert(new ItemStack(BuiltInRegistries.ITEM.get(rotated.ingredient())));
                h.assertTrue(rotated.material().orElseThrow().equals(BuiltInRegistries.ITEM.getKey(l.stands().get(seat).materialItem().getItem())),"Material socket consumed or pairing changed");
            }
            h.assertTrue(RitualCrafting.activate(p,center)==RitualCrafting.Outcome.ATTUNING,"Rotated shard recipe failed");
            h.runAfterDelay(65,()->{h.assertTrue(AttunementShardItem.signature(RitualTestOutput.stack(center)).orElseThrow().key().equals(expected),"Complete quarter turn changed key");h.succeed();});
        });
    }
    @GameTest(template="empty_9x3x9",batch="attunement_cancel",timeoutTicks=100)
    public static void shardNeedsEightSlotsAndChangesCancelAtomically(GameTestHelper h){
        var center=structure(h,false);var l=RitualCrafting.layout(center);var p=h.makeMockPlayer(GameType.SURVIVAL);
        for(int i=0;i<4;i++)l.stands().get(i*2).insert(new ItemStack(BuiltInRegistries.ITEM.get(AttunementShardItem.ingredients().get(i))));
        h.assertTrue(RitualCrafting.activate(p,center)==RitualCrafting.Outcome.INVALID,"Four-node shard crafted");
        h.runAfterDelay(11,()->{
            for(var n:l.stands())if(n!=null)n.remove();for(int i=1;i<8;i+=2)h.setBlock(CENTER.offset(GEOMETRY.offset(i)),ApparatusBlocks.PLINTH.get());var full=RitualCrafting.layout(center);
            fillShard(full);
            h.assertTrue(RitualCrafting.activate(p,center)==RitualCrafting.Outcome.ATTUNING,"Shard fixture failed");h.setBlock(CENTER.offset(GEOMETRY.offset(6)),Blocks.DIAMOND_BLOCK);
            h.runAfterDelay(65,()->{h.assertTrue(RitualTestOutput.stack(center).isEmpty() && full.items().stream().filter(s->!s.isEmpty()).count()==6 && !center.busy(),"Changed empty node did not cancel without consuming retained inputs");h.succeed();});
        });
    }
    @GameTest(template="empty_9x3x9",batch="attunement_counts",timeoutTicks=100)
    public static void shardRequiresExactDuplicateCountsAndRejectsExtraOfferings(GameTestHelper h){
        var center=structure(h,true);var l=RitualCrafting.layout(center);var p=h.makeMockPlayer(GameType.SURVIVAL);fillShard(l);
        l.stands().get(1).remove();
        h.assertTrue(RitualCrafting.activate(p,center)==RitualCrafting.Outcome.INVALID,"Missing second Amethyst accepted");
        h.runAfterDelay(11,()->{
            l.stands().get(1).insert(new ItemStack(Items.ECHO_SHARD));
            h.assertTrue(RitualCrafting.activate(p,center)==RitualCrafting.Outcome.INVALID,"Extra Echo replaced required Amethyst");
            l.stands().get(1).remove();l.stands().get(1).insert(new ItemStack(Items.AMETHYST_SHARD));l.stands().get(3).insert(new ItemStack(Items.AMETHYST_SHARD));
        });
        h.runAfterDelay(22,()->{
            h.assertTrue(RitualCrafting.activate(p,center)==RitualCrafting.Outcome.INVALID,"Seventh offering accepted");
            l.stands().get(3).remove();l.stands().get(2).remove();l.stands().get(2).insert(new ItemStack(Items.QUARTZ));
        });
        h.runAfterDelay(33,()->{
            h.assertTrue(RitualCrafting.activate(p,center)==RitualCrafting.Outcome.INVALID,"Wrong ingredient accepted");
            l.stands().get(2).remove();l.stands().get(2).insert(new ItemStack(Items.ECHO_SHARD));
        });
        h.runAfterDelay(44,()->h.assertTrue(RitualCrafting.activate(p,center)==RitualCrafting.Outcome.ATTUNING,"Correct duplicate recipe rejected"));
        h.runAfterDelay(90,()->{h.assertTrue(AttunementShardItem.signature(RitualTestOutput.stack(center)).isPresent() && l.items().stream().allMatch(ItemStack::isEmpty),"Valid recipe did not commit once after rejected attempts");h.succeed();});
    }
    @GameTest(template="empty_9x3x9",batch="shaping_decode")
    public static void malformedAugmentsAndShardBlueprintsRejectSafely(GameTestHelper h){
        var scroll=ScrollItems.shapedScroll(id("fireball"),new LeylineShaping.Modifiers(1,1,1,1),List.of(augment("bleeding")));
        CustomData.update(DataComponents.CUSTOM_DATA,scroll,t->t.getList("vestige_augments",net.minecraft.nbt.Tag.TAG_COMPOUND).getCompound(0).putString("id","INVALID ID"));h.assertTrue(ScrollItems.scroll(scroll).isEmpty(),"Malformed augment crashed or decoded");
        var center=structure(h,true);var l=RitualCrafting.layout(center);fillShard(l);
        var shard=AttunementShardItem.create(RitualInputs.capture(l));CustomData.update(DataComponents.CUSTOM_DATA,shard,t->t.getList("nodes",net.minecraft.nbt.Tag.TAG_COMPOUND).getCompound(0).putString("ingredient","INVALID ID"));h.assertTrue(AttunementShardItem.signature(shard).isEmpty(),"Malformed shard crashed or decoded");h.succeed();
    }
    @GameTest(template="empty_9x3x9",batch="shaping_healing",timeoutTicks=120)
    public static void mendingScrollActuallyHealsPaysOnceAndIdentifiesBaseSpell(GameTestHelper h){
        var p=h.makeMockPlayer(GameType.SURVIVAL);p.setHealth(8);p.getPersistentData().putDouble("vestige:mana",100);SpellKnowledge.identify(p,id("heal"));
        var scroll=ScrollItems.shapedScroll(id("heal"),new LeylineShaping.Modifiers(1,1,1,1),List.of(augment("mending")));p.setItemInHand(InteractionHand.MAIN_HAND,scroll);
        h.assertTrue(ScrollCasting.cast(p,scroll),"Mending scroll failed");h.runAfterDelay(110,()->{h.assertTrue(p.getHealth()==16 && p.getPersistentData().getDouble("vestige:mana")==83 && p.getMainHandItem().isEmpty(),"Mending restoration/payment differed: "+p.getHealth()+" / "+p.getPersistentData().getDouble("vestige:mana"));h.assertTrue(SpellKnowledge.identified(p,id("heal")),"Base spell identity lost");h.succeed();});
    }
    @GameTest(template="empty_9x3x9",batch="shaping_contacts",timeoutTicks=110)
    public static void committedDamageGetsElectricalRiderAndFiniteWound(GameTestHelper h){
        var caster=h.spawnWithNoFreeWill(EntityType.VILLAGER,new BlockPos(1,1,1));var victim=h.spawnWithNoFreeWill(EntityType.VILLAGER,new BlockPos(4,1,4));
        caster.setNoGravity(true);victim.setNoGravity(true);
        var world=NativeMagic.session(h.getLevel().getServer());world.world().registerActor(caster);world.world().registerActor(victim);
        var primary=new SpellEffects.Action(id("damage"),Map.of("amount",new SpellValue.Constant(2)),Map.of());
        var base=new SpellDefinition(id("shaping_contact_test"),Set.of(Tradition.ARCANE),new TraitProfile(Map.of(id("amplify"),1d)),List.of(new SpellCost.Mana(10)),List.of(new SpellTrigger(id("cast"),SpellTriggerTypes.INTERACT,List.of())),List.of(new SpellEffects.ForEach(new TargetSpec(TargetSpec.Selection.EVENT_TARGET,new SpellValue.Constant(0)),List.of(primary))));
        var compiled=Spellshaping.compile(base,List.of(augment("shocking"),augment("bleeding")),List.of(),new CastShaping(1,true));var cast=world.runtime().cast(compiled.spell(),SpellEvent.of(SpellTriggerTypes.INTERACT,caster.getUUID(),new SpellSubject.Entity(victim.getUUID())),compiled.modifiers(),true,Optional.empty(),true,compiled.shaping());
        h.assertTrue(victim.getHealth()==16,"Electrical rider did not accompany primary contact: "+victim.getHealth());h.runAfterDelay(90,()->{h.assertTrue(victim.getHealth()==13 && cast.status()==SpellRuntime.Status.COMPLETED,"Wound did not terminate at three actual ticks: "+victim.getHealth()+" "+cast.status());h.succeed();});
    }
    @GameTest(template="empty_9x3x9",batch="shaping_protection",timeoutTicks=120)
    public static void ownedWardAddsFiniteActualMitigationThenExpires(GameTestHelper h){
        var caster=h.spawnWithNoFreeWill(EntityType.VILLAGER,new BlockPos(1,1,1));var attacker=h.spawnWithNoFreeWill(EntityType.VILLAGER,new BlockPos(4,1,4));var session=NativeMagic.session(h.getLevel().getServer());session.world().registerActor(caster);
        caster.setNoGravity(true);attacker.setNoGravity(true);
        var base=NativeMagic.spells().spells().get(id("pf2_air_bubble"));var compiled=Spellshaping.compile(base,List.of(augment("warded")),List.of(),new CastShaping(1,true));session.runtime().cast(compiled.spell(),SpellEvent.of(SpellTriggerTypes.INTERACT,caster.getUUID(),null),compiled.modifiers(),true,Optional.empty(),true,compiled.shaping());
        h.runAfterDelay(25,()->{h.assertTrue(caster.getHealth()==20,"Ward fixture took unrelated damage: "+caster.getHealth());caster.invulnerableTime=0;caster.hurt(caster.damageSources().mobAttack(attacker),4);h.assertTrue(caster.getHealth()==17,"Added guard did not mitigate actual harm: "+caster.getHealth());});
        h.runAfterDelay(110,()->{caster.invulnerableTime=0;caster.hurt(caster.damageSources().mobAttack(attacker),4);h.assertTrue(caster.getHealth()==13,"Expired shaping guard still mitigated: "+caster.getHealth());h.succeed();});
    }
    @GameTest(template="empty_9x3x9",batch="shaping_payment")
    public static void exhaustingChargesRealManaAndAmplifiesAnActualHitWithoutCooldown(GameTestHelper h){
        var player=h.makeMockPlayer(GameType.SURVIVAL);player.getPersistentData().putDouble("vestige:mana",26);
        var victim=h.spawnWithNoFreeWill(EntityType.VILLAGER,new BlockPos(4,1,4));victim.setNoGravity(true);
        var session=NativeMagic.session(h.getLevel().getServer());session.world().registerActor(player);session.world().registerActor(victim);
        var base=new SpellDefinition(id("exhausting_payment_test"),Set.of(Tradition.ARCANE),new TraitProfile(Map.of(id("amplify"),1d)),List.of(new SpellCost.Mana(20)),List.of(new SpellTrigger(id("cast"),SpellTriggerTypes.INTERACT,List.of())),List.of(new SpellEffects.ForEach(new TargetSpec(TargetSpec.Selection.EVENT_TARGET,new SpellValue.Constant(0)),List.of(new SpellEffects.Action(id("damage"),Map.of("amount",new SpellValue.Product(List.of(new SpellValue.Constant(2),new SpellValue.Trait(id("amplify"))))),Map.of())))));
        var compiled=Spellshaping.compile(base,List.of(augment("exhausting")),List.of(),new CastShaping(1,true));
        var event=SpellEvent.of(SpellTriggerTypes.INTERACT,player.getUUID(),new SpellSubject.Entity(victim.getUUID()));
        var rejected=session.runtime().cast(compiled.spell(),event,compiled.modifiers(),true,Optional.empty(),false,compiled.shaping());
        h.assertTrue(rejected.status()==SpellRuntime.Status.COST_FAILED && player.getPersistentData().getDouble("vestige:mana")==26 && victim.getHealth()==20,"Unaffordable Exhausting partially committed");
        player.getPersistentData().putDouble("vestige:mana",100);
        var cast=session.runtime().cast(compiled.spell(),event,compiled.modifiers(),true,Optional.empty(),false,compiled.shaping());
        h.assertTrue(cast.paymentCommitted() && cast.status()==SpellRuntime.Status.COMPLETED && player.getPersistentData().getDouble("vestige:mana")==73 && victim.getHealth()==17,"Exhausting did not buy actual power with actual mana");h.succeed();
    }
    @GameTest(template="empty_9x3x9",batch="shaping_payment")
    public static void exchangedAndMaterialPaymentsCommitTogetherOrLeaveEverythingUntouched(GameTestHelper h){
        var player=h.makeMockPlayer(GameType.SURVIVAL);player.setHealth(20);player.getFoodData().setFoodLevel(20);player.getPersistentData().putDouble("vestige:mana",100);
        var victim=h.spawnWithNoFreeWill(EntityType.VILLAGER,new BlockPos(4,1,4));victim.setNoGravity(true);
        var session=NativeMagic.session(h.getLevel().getServer());session.world().registerActor(player);session.world().registerActor(victim);
        var base=new SpellDefinition(id("shaping_payment_test"),Set.of(Tradition.ARCANE),new TraitProfile(Map.of(id("amplify"),1d)),List.of(new SpellCost.Mana(20)),List.of(new SpellTrigger(id("cast"),SpellTriggerTypes.INTERACT,List.of())),List.of(new SpellEffects.ForEach(new TargetSpec(TargetSpec.Selection.EVENT_TARGET,new SpellValue.Constant(0)),List.of(new SpellEffects.Action(id("damage"),Map.of("amount",new SpellValue.Product(List.of(new SpellValue.Constant(2),new SpellValue.Trait(id("amplify"))))),Map.of())))));
        var compiled=Spellshaping.compile(base,List.of(augment("bloodbound"),augment("fasting"),augment("votive"),augment("sacrificial")),List.of(),new CastShaping(1,true));
        player.getInventory().setItem(1,new ItemStack(Items.COAL,2));
        var event=SpellEvent.of(SpellTriggerTypes.INTERACT,player.getUUID(),new SpellSubject.Entity(victim.getUUID()));
        var failed=session.runtime().cast(compiled.spell(),event,compiled.modifiers(),true,Optional.empty(),false,compiled.shaping());
        h.assertTrue(failed.status()==SpellRuntime.Status.COST_FAILED && !failed.paymentCommitted() && player.getHealth()==20 && player.getFoodData().getFoodLevel()==20 && player.getPersistentData().getDouble("vestige:mana")==100 && player.getInventory().getItem(1).getCount()==2 && victim.getHealth()==20,"Failed typed payment partially committed");
        player.getInventory().setItem(2,new ItemStack(Items.IRON_PICKAXE));
        var paid=session.runtime().cast(compiled.spell(),event,compiled.modifiers(),true,Optional.empty(),false,compiled.shaping());
        h.assertTrue(paid.paymentCommitted() && paid.status()==SpellRuntime.Status.COMPLETED && player.getHealth()==18 && player.getFoodData().getFoodLevel()==19 && player.getPersistentData().getDouble("vestige:mana")==89 && player.getInventory().getItem(1).getCount()==1 && player.getInventory().getItem(2).getDamageValue()==4 && victim.getHealth()==17,"Typed payment did not commit once: "+paid.status()+" / "+compiled.shaping().costs(base.costs()));h.succeed();
    }
    @GameTest(template="empty_9x3x9",batch="shaping_revealing",timeoutTicks=130)
    public static void revealingOutlinesEightInvisibleCreaturesAndExpiresWithoutDispelling(GameTestHelper h){
        var caster=h.spawnWithNoFreeWill(EntityType.VILLAGER,new BlockPos(4,1,4));caster.setNoGravity(true);
        var hidden=new ArrayList<net.minecraft.world.entity.LivingEntity>();
        for(int i=0;i<10;i++){
            var victim=h.spawnWithNoFreeWill(EntityType.VILLAGER,new BlockPos(3+i%3,1,3+i/3));victim.setNoGravity(true);victim.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.INVISIBILITY,300,0));hidden.add(victim);
        }
        var visible=h.spawnWithNoFreeWill(EntityType.VILLAGER,new BlockPos(4,1,3));visible.setNoGravity(true);
        var outside=h.spawnWithNoFreeWill(EntityType.VILLAGER,new BlockPos(4,1,11));outside.setNoGravity(true);outside.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.INVISIBILITY,300,0));
        var session=NativeMagic.session(h.getLevel().getServer());session.world().registerActor(caster);
        var base=NativeMagic.spells().spells().get(id("pf2_detect_magic"));var compiled=Spellshaping.compile(base,List.of(augment("revealing")),List.of(),new CastShaping(1,true));
        session.runtime().cast(compiled.spell(),SpellEvent.of(SpellTriggerTypes.INTERACT,caster.getUUID(),null),compiled.modifiers(),true,Optional.empty(),true,compiled.shaping());
        h.runAfterDelay(12,()->{
            h.assertTrue(hidden.stream().filter(e->e.hasEffect(net.minecraft.world.effect.MobEffects.GLOWING)).count()==8,"Revealing recipients: "+hidden.stream().filter(e->e.hasEffect(net.minecraft.world.effect.MobEffects.GLOWING)).count());
            h.assertTrue(hidden.stream().allMatch(net.minecraft.world.entity.LivingEntity::isInvisible) && !visible.hasEffect(net.minecraft.world.effect.MobEffects.GLOWING) && !outside.hasEffect(net.minecraft.world.effect.MobEffects.GLOWING),"Revealing dispelled invisibility or leaked outside its eligible radius");
        });
        h.runAfterDelay(115,()->{h.assertTrue(hidden.stream().noneMatch(e->e.hasEffect(net.minecraft.world.effect.MobEffects.GLOWING)),"Revealing outline did not expire");h.succeed();});
    }
    @GameTest(template="empty_9x3x9",batch="shaping_anchor",timeoutTicks=135)
    public static void anchoredGravityFieldKeepsItsOriginAndActualDamageThenCleansUp(GameTestHelper h){
        var caster=h.spawnWithNoFreeWill(EntityType.VILLAGER,new BlockPos(1,1,1));caster.setNoGravity(true);caster.setYRot(-90);caster.setXRot(0);
        var inside=h.spawnWithNoFreeWill(EntityType.VILLAGER,new BlockPos(1,1,3));inside.setNoGravity(true);
        var outside=h.spawnWithNoFreeWill(EntityType.VILLAGER,new BlockPos(7,1,1));outside.setNoGravity(true);
        var session=NativeMagic.session(h.getLevel().getServer());session.world().registerActor(caster);
        var base=NativeMagic.spells().spells().get(id("gravity_fissure"));var compiled=Spellshaping.compile(base,List.of(augment("anchored")),List.of(),new CastShaping(1,true));
        var cast=session.runtime().cast(compiled.spell(),SpellEvent.of(SpellTriggerTypes.INTERACT,caster.getUUID(),null),compiled.modifiers(),true,Optional.empty(),true,compiled.shaping());
        h.runAfterDelay(40,()->{
            var anchors=h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.decoration.ArmorStand.class,caster.getBoundingBox().inflate(12),e->session.world().isOwnedBy(e,caster.getUUID()));
            h.assertTrue(anchors.size()==1 && anchors.getFirst().position().distanceToSqr(caster.position())<.01,"Anchored field origin still moved");
            h.assertTrue(inside.getHealth()<20 && outside.getHealth()==20,"Anchored field damage did not use the stationary spatial origin");
        });
        h.runAfterDelay(125,()->{
            h.assertTrue(cast.status()==SpellRuntime.Status.COMPLETED && h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.decoration.ArmorStand.class,caster.getBoundingBox().inflate(12),e->session.world().isOwnedBy(e,caster.getUUID())).isEmpty(),"Anchored field outlived its backing or failed to clean up");h.succeed();
        });
    }
    private static net.minecraft.world.entity.projectile.Arrow incoming(GameTestHelper h,net.minecraft.world.entity.LivingEntity target,net.minecraft.world.entity.LivingEntity shooter){
        var arrow=EntityType.ARROW.create(h.getLevel());arrow.setOwner(shooter);arrow.setNoGravity(true);arrow.setBaseDamage(2);
        arrow.setPos(target.getBoundingBox().getCenter().add(1.5,0,0));arrow.setDeltaMovement(-.25,0,0);h.getLevel().addFreshEntity(arrow);return arrow;
    }
    @GameTest(template="empty_9x3x9",batch="shaping_reflect",timeoutTicks=100)
    public static void reflectingReturnsActualArrowsWithSharedBudgetAndExpires(GameTestHelper h){
        for(int x=2;x<=8;x++)for(int y=1;y<=3;y++)for(int z=3;z<=5;z++)h.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        var caster=h.spawnWithNoFreeWill(EntityType.VILLAGER,new BlockPos(3,1,4));caster.setNoGravity(true);
        var shooter=h.spawnWithNoFreeWill(EntityType.VILLAGER,new BlockPos(7,1,4));shooter.setNoGravity(true);
        var session=NativeMagic.session(h.getLevel().getServer());session.world().registerActor(caster);
        var base=NativeMagic.spells().spells().get(id("pf2_glass_shield"));var compiled=Spellshaping.compile(base,List.of(augment("reflecting")),List.of(),new CastShaping(1,true));
        session.runtime().cast(compiled.spell(),SpellEvent.of(SpellTriggerTypes.INTERACT,caster.getUUID(),null),compiled.modifiers(),true,Optional.empty(),true,compiled.shaping());
        var first=incoming(h,caster,shooter);var friendly=incoming(h,caster,caster);
        var outgoing=incoming(h,caster,shooter);outgoing.setPos(caster.getBoundingBox().getCenter().add(1.2,0,1));outgoing.setDeltaMovement(.25,0,0);
        var nativeDelivery=com.quzzar.vestige.magic.world.SpellEntities.PROJECTILE.get().create(h.getLevel());nativeDelivery.setOwner(shooter);nativeDelivery.setPos(caster.getBoundingBox().getCenter().add(1.5,0,-1));nativeDelivery.setDeltaMovement(-.25,0,0);h.getLevel().addFreshEntity(nativeDelivery);
        net.minecraft.world.entity.projectile.Arrow[] second={null},third={null};
        h.runAfterDelay(3,()->{h.assertTrue(first.getOwner()==caster && first.getDeltaMovement().x>0 && session.world().cause(first).secondary(),"Actual arrow was not returned with secondary lineage");h.assertTrue(friendly.getDeltaMovement().x<0,"Friendly arrow was returned");h.assertTrue(outgoing.getOwner()==shooter && outgoing.getDeltaMovement().x>0 && nativeDelivery.getOwner()==shooter,"Outgoing or native delivery was returned");friendly.discard();outgoing.discard();nativeDelivery.discard();});
        h.runAfterDelay(22,()->{h.assertTrue(shooter.getHealth()<20,"Returned arrow never damaged shooter: "+first.position()+" -> "+shooter.position()+" / "+first.getDeltaMovement()+" / removed="+first.isRemoved()+" health="+shooter.getHealth()+" ticks="+first.tickCount+" block="+h.getLevel().getBlockState(first.blockPosition()));second[0]=incoming(h,caster,shooter);});
        h.runAfterDelay(25,()->{h.assertTrue(second[0].getOwner()==caster,"Second reflection did not use shared cast budget");second[0].discard();third[0]=incoming(h,caster,shooter);});
        h.runAfterDelay(28,()->{h.assertTrue(third[0].getOwner()==shooter,"Reflection budget allowed a third projectile");h.assertTrue(!session.world().cause(second[0]).secondary(),"Removed returned-projectile lineage leaked");third[0].discard();});
        h.runAfterDelay(70,()->{third[0]=incoming(h,caster,shooter);});
        h.runAfterDelay(73,()->{h.assertTrue(third[0].getOwner()==shooter,"Expired reflection still redirected arrows");third[0].discard();});
        h.runAfterDelay(76,()->{h.assertTrue(!session.world().cause(third[0]).secondary(),"Removed returned-projectile lineage leaked");h.succeed();});
    }

    @GameTest(template="empty_9x3x9",batch="shaping_reflect_lineage",timeoutTicks=30)
    public static void returnedTridentKeepsItsRootAcrossTwoProtectiveCasts(GameTestHelper h){
        for(int x=2;x<=8;x++)for(int y=1;y<=3;y++)for(int z=3;z<=8;z++)h.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        var first=h.spawnWithNoFreeWill(EntityType.VILLAGER,new BlockPos(3,1,4));var second=h.spawnWithNoFreeWill(EntityType.VILLAGER,new BlockPos(3,1,7));var shooter=h.spawnWithNoFreeWill(EntityType.VILLAGER,new BlockPos(7,1,4));
        first.setNoGravity(true);second.setNoGravity(true);shooter.setNoGravity(true);
        var session=NativeMagic.session(h.getLevel().getServer());session.world().registerActor(first);session.world().registerActor(second);
        var base=NativeMagic.spells().spells().get(id("pf2_glass_shield"));var compiled=Spellshaping.compile(base,List.of(augment("reflecting")),List.of(),new CastShaping(1,true));
        for(var actor:List.of(first,second))session.runtime().cast(compiled.spell(),SpellEvent.of(SpellTriggerTypes.INTERACT,actor.getUUID(),null),compiled.modifiers(),true,Optional.empty(),true,compiled.shaping());
        var trident=EntityType.TRIDENT.create(h.getLevel());trident.setOwner(shooter);trident.setNoGravity(true);trident.setPos(first.getBoundingBox().getCenter().add(1.5,0,0));trident.setDeltaMovement(-.25,0,0);h.getLevel().addFreshEntity(trident);
        UUID[] root={null};
        h.runAfterDelay(3,()->{h.assertTrue(trident.getOwner()==first,"Incoming trident did not reflect");root[0]=session.world().cause(trident).rootId();trident.setPos(second.getBoundingBox().getCenter().add(1.5,0,0));trident.setDeltaMovement(-.25,0,0);});
        h.runAfterDelay(6,()->{h.assertTrue(trident.getOwner()==second && session.world().cause(trident).secondary() && session.world().cause(trident).rootId().equals(root[0]),"Second reflection replaced the original causal root");trident.setPos(first.getBoundingBox().getCenter().add(1.5,0,0));trident.setDeltaMovement(-.25,0,0);});
        h.runAfterDelay(9,()->{h.assertTrue(trident.getOwner()==second,"The same cast reflected one projectile twice");trident.discard();});
        h.runAfterDelay(12,()->{h.assertTrue(!session.world().cause(trident).secondary(),"Removed trident lineage leaked");h.succeed();});
    }

}
