package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.world.NativeMagic;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import java.util.ArrayList;
import java.util.List;

@GameTestHolder(VestigeMainMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class RitualTest {
    private static final BlockPos CENTER=new BlockPos(4,1,4);
    private static final LeylineShaping.Geometry FIXTURE=new LeylineShaping.Geometry(8,LeylineShaping.Shape.CROSS,2,0,LeylineShaping.Shape.DIAGONAL,3,0);
    private static net.minecraft.resources.ResourceLocation id(String name) { return VestigeMainMod.location(name); }
    private static OfferingBlockEntity structure(GameTestHelper h,boolean advanced) {
        h.setBlock(CENTER,ApparatusBlocks.SPELLSTONE.get());
        for (int i=0;i<8;i++) h.setBlock(CENTER.offset(FIXTURE.offset(i)),(i&1)==0 ? ApparatusBlocks.PLINTH.get() : advanced ? ApparatusBlocks.PLINTH.get() : Blocks.AIR);
        return (OfferingBlockEntity)h.getBlockEntity(CENTER);
    }
    private static RitualRecipe recipe(String name) { return RitualCrafting.catalog().recipes().get(id(name)); }
    /** Ordinary interactions must communicate through the apparatus/spell presentation, never text. */
    private static net.minecraft.world.entity.player.Player quietPlayer(GameTestHelper h) {
        return new net.minecraft.world.entity.player.Player(h.getLevel(),h.absolutePos(CENTER),0,
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"quiet-ritual-player")) {
            @Override public boolean isSpectator() { return false; }
            @Override public boolean isCreative() { return false; }
            @Override public void displayClientMessage(net.minecraft.network.chat.Component message,boolean overlay) {
                throw new AssertionError("Gameplay emitted "+(overlay ? "actionbar" : "chat")+": "+message.getString());
            }
            @Override public void sendSystemMessage(net.minecraft.network.chat.Component message) {
                throw new AssertionError("Gameplay emitted system text: "+message.getString());
            }
        };
    }
    private static void fill(RitualCrafting.Layout layout,RitualRecipe recipe,int turn) {
        for (var part:recipe.parts()) layout.stands().get(recipe.circle()==4 ? ((part.seat()+turn)%4)*2 : (part.seat()+turn*2)%8).insert(part.ingredient().hint());
    }
    @GameTest(template="empty_9x3x9",batch="ritual_catalog")
    public static void everyRecipeUsesRealItemsMatchesAllRotationsAndHasNoAmbiguousBaseline(GameTestHelper h) {
        var recipes=RitualCrafting.catalog().recipes(); h.assertTrue(recipes.size()==214 && recipes.keySet().equals(NativeMagic.spells().spells().keySet()),"Recipe/catalog coverage differs");
        var sizes=new java.util.HashSet<Integer>();
        for (var r:recipes.values()) {
            sizes.add(r.parts().size());
            for (var part:r.parts()) h.assertTrue(!part.ingredient().hint().isEmpty(),"Missing baseline registry item: "+r.spell()+" / "+part.role());
            for (int turn=0;turn<4;turn++) {
                List<ItemStack> items=new ArrayList<>(java.util.Collections.nCopies(8,ItemStack.EMPTY));
                for (var part:r.parts()) items.set(r.circle()==4 ? ((part.seat()+turn)%4)*2 : (part.seat()+turn*2)%8,part.ingredient().hint());
                h.assertTrue(r.evaluate(items).correct(),"Rotated recipe does not match "+r.spell()+" turn "+turn);
                h.assertTrue(recipes.values().stream().filter(other -> other.circle()==r.circle() && other.evaluate(items).correct()).count()==1,"Ambiguous unreferenced recipe "+r.spell());
            }
        }
        h.assertTrue(sizes.equals(java.util.Set.of(4,5,6,7,8)),"Missing supported recipe size"); h.succeed();
    }
    @GameTest(template="empty_9x3x9",batch="ritual_basic",timeoutTicks=100)
    public static void correctReferenceCraftPreservesScrollConsumesOnceAndDoesNotIdentify(GameTestHelper h) {
        var c=structure(h,false); var l=RitualCrafting.layout(c); var r=recipe("fireball"); fill(l,r,1); ItemStack reference=ScrollItems.scroll(r.spell()); c.insert(reference);
        var player=quietPlayer(h);
        h.assertTrue(RitualCrafting.activate(player,c,()->1)==RitualCrafting.Outcome.CRAFTING,"Correct reference rejected");
        h.assertTrue(!SpellKnowledge.crafted(player,r.spell()),"Recipe unlocked before commitment");
        var other=quietPlayer(h);
        h.assertTrue(RitualCrafting.activate(player,c)==RitualCrafting.Outcome.BUSY,"Overlapping activation accepted");
        h.runAfterDelay(45,()->{
            h.assertTrue(ItemStack.matches(reference,c.displayedItem()),"Reference was consumed");
            h.assertTrue(ScrollItems.scroll(RitualTestOutput.stack(c)).orElseThrow().spell().equals(r.spell()),"Wrong crafted output");
            h.assertTrue(l.items().stream().allMatch(ItemStack::isEmpty),"Ingredients were not consumed");
            h.assertTrue(!SpellKnowledge.identified(player,r.spell()),"Crafting identified a spell");
            h.assertTrue(SpellKnowledge.crafted(player,r.spell()) && !SpellKnowledge.crafted(other,r.spell()),"Recipe knowledge was missing or shared with another player");
            var drop=RitualTestOutput.entity(c); drop.setPickUpDelay(0); drop.playerTouch(player);
            h.assertTrue(RitualTestOutput.stack(c).isEmpty() && !c.displayedItem().isEmpty(),"Output retrieval removed reference");
            h.assertTrue(c.busy(),"Fixture missed the result pickup during the settling animation");
            h.runAfterDelay(20,()->{
                player.getInventory().selected=8; player.setShiftKeyDown(true); h.useBlock(CENTER,player);
                h.assertTrue(c.displayedItem().isEmpty(),"Sneaking did not retrieve reference"); h.succeed();
            });
        });
    }
    @GameTest(template="empty_9x3x9",batch="ritual_output",timeoutTicks=100)
    public static void craftSpawnsOneCenteredOrdinaryDropWithoutRandomMotion(GameTestHelper h) {
        var center=structure(h,false);var layout=RitualCrafting.layout(center);var recipe=recipe("fireball");fill(layout,recipe,0);
        var reference=ScrollItems.scroll(recipe.spell());center.insert(reference);
        var observed=new java.util.ArrayList<ItemEntity>();
        java.util.function.Consumer<net.neoforged.neoforge.event.entity.EntityJoinLevelEvent> listener=event->{
            if(event.getLevel()==h.getLevel() && event.getEntity() instanceof ItemEntity item
                    && item.position().distanceTo(net.minecraft.world.phys.Vec3.atCenterOf(center.getBlockPos()))<1
                    && ScrollItems.scroll(item.getItem()).isPresent()) {
                var pos=center.getBlockPos();double surface=((ApparatusBlock)center.getBlockState().getBlock()).offeringHeight();
                h.assertTrue(item.getX()==pos.getX()+.5 && item.getZ()==pos.getZ()+.5
                        && Math.abs(item.getY()-(pos.getY()+surface+.05))<1e-10,"Result spawned away from center or top");
                h.assertTrue(item.getDeltaMovement().equals(net.minecraft.world.phys.Vec3.ZERO) && !item.isNoGravity(),"Result was tossed or artificially suspended");
                observed.add(item);
            }
        };
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(listener);
        h.assertTrue(RitualCrafting.activate(quietPlayer(h),center)==RitualCrafting.Outcome.CRAFTING,"Drop fixture did not start");
        h.runAfterDelay(65,()->{try {
            h.assertTrue(observed.size()==1 && observed.getFirst().isAlive() && center.resultItem().isEmpty(),"Result duplicated or remained in apparatus storage");
            h.assertTrue(ItemStack.matches(center.displayedItem(),reference) && layout.items().stream().allMatch(ItemStack::isEmpty),"Drop changed reference or consumption");
            var player=quietPlayer(h);var result=observed.getFirst();result.playerTouch(player);
            h.assertTrue(!result.isAlive() && player.getInventory().items.stream().anyMatch(i->ScrollItems.scroll(i).isPresent()),"Ordinary collision pickup failed");h.succeed();
        } finally {net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(listener);}});
    }
    @GameTest(template="empty_9x3x9",batch="ritual_output_cancel",timeoutTicks=100)
    public static void rejectedOutputSpawnPreservesAllIngredientsAndReference(GameTestHelper h) {
        var center=structure(h,false);var layout=RitualCrafting.layout(center);var recipe=recipe("fireball");fill(layout,recipe,0);
        var reference=ScrollItems.scroll(recipe.spell());center.insert(reference);
        java.util.function.Consumer<net.neoforged.neoforge.event.entity.EntityJoinLevelEvent> listener=event->{
            if(event.getLevel()==h.getLevel() && event.getEntity() instanceof ItemEntity item
                    && item.position().distanceTo(net.minecraft.world.phys.Vec3.atCenterOf(center.getBlockPos()))<1)event.setCanceled(true);
        };
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(listener);
        var player=quietPlayer(h);
        h.assertTrue(RitualCrafting.activate(player,center)==RitualCrafting.Outcome.CRAFTING,"Cancelled drop fixture did not start");
        h.runAfterDelay(65,()->{try {
            h.assertTrue(RitualTestOutput.stack(center).isEmpty() && layout.items().stream().filter(i->!i.isEmpty()).count()==4
                    && ItemStack.matches(reference,center.displayedItem()) && layout.blocks().stream().noneMatch(OfferingBlockEntity::busy),"Rejected spawn charged ingredients or left locks");
            h.assertTrue(!SpellKnowledge.crafted(player,recipe.spell()),"Cancelled spawn taught the recipe");h.succeed();
        } finally {net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(listener);}});
    }
    @GameTest(template="empty_9x3x9",batch="ritual_output_edit",timeoutTicks=100)
    public static void inputEditedDuringOutputJoiningCancelsBeforeConsumptionOrRecipeMemory(GameTestHelper h) {
        var center=structure(h,false);var layout=RitualCrafting.layout(center);var recipe=recipe("fireball");fill(layout,recipe,0);
        var reference=ScrollItems.scroll(recipe.spell());center.insert(reference);var player=quietPlayer(h);
        var removed=new java.util.ArrayList<ItemStack>();
        java.util.function.Consumer<net.neoforged.neoforge.event.entity.EntityJoinLevelEvent> listener=event->{
            if(event.getLevel()==h.getLevel() && event.getEntity() instanceof ItemEntity item
                    && item.position().distanceTo(net.minecraft.world.phys.Vec3.atCenterOf(center.getBlockPos()))<1)removed.add(layout.stands().get(0).remove());
        };
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(listener);
        h.assertTrue(RitualCrafting.activate(player,center)==RitualCrafting.Outcome.CRAFTING,"Edited drop fixture did not start");
        h.runAfterDelay(65,()->{try {
            h.assertTrue(removed.size()==1 && !removed.getFirst().isEmpty() && RitualTestOutput.stack(center).isEmpty(),"Input edit duplicated or committed an output");
            h.assertTrue(layout.items().stream().filter(i->!i.isEmpty()).count()==3 && ItemStack.matches(reference,center.displayedItem())
                    && !SpellKnowledge.crafted(player,recipe.spell()) && layout.blocks().stream().noneMatch(OfferingBlockEntity::busy),"Input edit consumed other offerings, taught recipe or retained locks");h.succeed();
        } finally {net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(listener);}});
    }
    @GameTest(template="empty_9x3x9",batch="ritual_feedback")
    public static void wrongPedestalTypesUseTheSameSidewaysShakeAsMisplacedIngredients(GameTestHelper h) {
        var c=structure(h,true); var l=RitualCrafting.layout(c); var r=recipe("pf2_flicker");
        c.insert(ScrollItems.scroll(r.spell()));
        l.stands().get(0).insert(new ItemStack(Items.PAPER));
        l.stands().get(2).insert(new ItemStack(Items.ENDER_PEARL));
        l.stands().get(7).insert(new ItemStack(Items.CLOCK));
        l.stands().get(4).insert(new ItemStack(Items.PHANTOM_MEMBRANE));
        h.assertTrue(RitualCrafting.activate(quietPlayer(h),c,
                ()->{throw new AssertionError("Incomplete hint inspection rolled risk");})==RitualCrafting.Outcome.HINTS,"Incomplete arrangement rejected");
        for (int i:new int[]{2,4,7}) h.assertTrue(l.stands().get(i).feedback()==RitualRecipe.Feedback.SHAKE,
                "Misplaced ingredient did not use sideways shake: "+i);
        h.assertTrue(l.stands().get(0).feedback()==RitualRecipe.Feedback.CORRECT,"Correct Paper lost steady glow");
        h.succeed();
    }
    @GameTest(template="empty_9x3x9",batch="ritual_advanced",timeoutTicks=180)
    public static void fiveAndSevenPartRecipesCraftWithoutReferences(GameTestHelper h) {
        var c=structure(h,true); var l=RitualCrafting.layout(c); var player=quietPlayer(h);
        var five=RitualCrafting.catalog().recipes().values().stream().filter(r -> r.parts().size()==5).findFirst().orElseThrow(); fill(l,five,2);
        h.assertTrue(RitualCrafting.activate(player,c)==RitualCrafting.Outcome.CRAFTING,"Five-part unreferenced recipe rejected");
        h.runAfterDelay(65,()->{
            h.assertTrue(ScrollItems.scroll(RitualTestOutput.take(c)).orElseThrow().spell().equals(five.spell()),"Five-part output differs"); l.stands().forEach(s -> s.remove());
            var seven=RitualCrafting.catalog().recipes().values().stream().filter(r -> r.parts().size()==7).findFirst().orElseThrow(); fill(l,seven,3);
            h.assertTrue(RitualCrafting.activate(player,c)==RitualCrafting.Outcome.CRAFTING,"Seven-part unreferenced recipe rejected");
            h.runAfterDelay(65,()->{h.assertTrue(ScrollItems.scroll(RitualTestOutput.stack(c)).orElseThrow().spell().equals(seven.spell()),"Seven-part output differs"); h.succeed();});
        });
    }
    @GameTest(template="empty_9x3x9",batch="ritual_safe")
    public static void unknownUnreferencedAndIncompleteReferencedAttemptsNeverRollOrConsume(GameTestHelper h) {
        var c=structure(h,false); var l=RitualCrafting.layout(c); var p=quietPlayer(h);
        l.stands().get(0).insert(new ItemStack(Items.DIRT));
        h.assertTrue(RitualCrafting.activate(p,c,()->{throw new AssertionError("Unreferenced invalid attempt rolled risk");})==RitualCrafting.Outcome.INVALID,"Unsafe rejection");
        h.runAfterDelay(11,()->{
            c.insert(ScrollItems.scroll(id("fireball")));
            h.assertTrue(RitualCrafting.activate(p,c,()->{throw new AssertionError("Incomplete attempt rolled risk");})==RitualCrafting.Outcome.HINTS,"Incomplete recipe did not show hints");
            h.assertTrue(l.stands().get(0).displayedItem().is(Items.DIRT) && RitualTestOutput.stack(c).isEmpty(),"Safe attempt consumed ingredients");
            h.assertTrue(l.stands().stream().filter(java.util.Objects::nonNull).anyMatch(s -> s.feedback()==RitualRecipe.Feedback.HINT && !s.hintItem().isEmpty()),"Missing item shadows"); h.succeed();
        });
    }
    @GameTest(template="empty_9x3x9",batch="ritual_cancel",timeoutTicks=100)
    public static void replacingAnActivePlinthCancelsBeforeAnyConsumption(GameTestHelper h) {
        var c=structure(h,false); var l=RitualCrafting.layout(c); var r=recipe("fireball"); fill(l,r,0);
        var player=quietPlayer(h);
        h.assertTrue(RitualCrafting.activate(player,c)==RitualCrafting.Outcome.CRAFTING,"Fixture did not start");
        h.setBlock(CENTER.offset(FIXTURE.offset(0)),Blocks.DIAMOND_BLOCK);
        h.runAfterDelay(65,()->{
            var output=RitualTestOutput.stack(c);var remaining=l.items().stream().filter(s -> !s.isEmpty()).count();
            // A broken Plinth's ordinary Paper offering can drift into the output observation box.
            h.assertTrue((output.isEmpty() || output.is(Items.PAPER)) && remaining==3 && !c.busy(),"Cancellation output="+output+", remaining="+remaining+", busy="+c.busy());
            h.assertTrue(!SpellKnowledge.crafted(player,r.spell()),"Replacing a Plinth taught the cancelled recipe");h.succeed();
        });
    }
    @GameTest(template="empty_9x3x9",batch="ritual_blast",timeoutTicks=90)
    public static void completedWrongRecipeDestroysOfferingsHurtsCreaturesPreservesTerrainReferenceAndDrops(GameTestHelper h) {
        var c=structure(h,true);var l=RitualCrafting.layout(c); var ref=ScrollItems.scroll(id("fireball"));c.insert(ref);
        l.stands().forEach(s -> s.insert(new ItemStack(Items.DIRT))); var p=quietPlayer(h);
        var villager=h.spawn(EntityType.VILLAGER,4,1,5); var outside=h.spawn(EntityType.VILLAGER,8,1,8);
        villager.setNoAi(true); outside.setNoAi(true);
        float before=villager.getHealth(),far=outside.getHealth(); var ground=h.getBlockState(CENTER.below());
        var drop=new ItemEntity(h.getLevel(),h.absolutePos(CENTER).getX()+.5,h.absolutePos(CENTER).getY()+1,h.absolutePos(CENTER).getZ()+.5,new ItemStack(Items.DIAMOND));h.getLevel().addFreshEntity(drop);
        h.assertTrue(RitualCrafting.activate(p,c,()->0)==RitualCrafting.Outcome.EXPLOSION_PENDING,"Unknown wrong recipe did not schedule blast");
        h.runAfterDelay(40,()->{
            h.assertTrue(villager.getHealth()<before && outside.getHealth()==far,"Creature blast radius is wrong: near="+before+" -> "+villager.getHealth()+", far="+far+" -> "+outside.getHealth());
            h.assertTrue(java.util.stream.IntStream.of(0,2,4,6).allMatch(i->l.items().get(i).isEmpty()) && java.util.stream.IntStream.of(1,3,5,7).allMatch(i->!l.items().get(i).isEmpty()),"Explosion did not limit consumption to the active inner recipe");
            h.assertTrue(ItemStack.matches(ref,c.displayedItem()) && c.resultItem().isEmpty(),"Explosion altered center scroll");
            h.assertTrue(drop.isAlive() && drop.getItem().is(Items.DIAMOND),"Explosion destroyed a loose item");
            h.assertTrue(h.getBlockState(CENTER.below()).equals(ground) && l.blocks().stream().allMatch(b -> h.getLevel().getBlockEntity(b.getBlockPos())==b),"Explosion damaged terrain/apparatus"); h.succeed();
        });
    }
    @GameTest(template="empty_9x3x9",batch="ritual_blast",timeoutTicks=90)
    public static void overlappingNodeBurstsDealOnlyTheStrongestSingleHit(GameTestHelper h) {
        var center=structure(h,false);var layout=RitualCrafting.layout(center);
        center.insert(ScrollItems.scroll(id("fireball")));
        for(var node:layout.stands())if(node!=null)node.insert(new ItemStack(Items.DIRT));
        int[] hits=new int[2];float[] damage=new float[2],expected=new float[2];boolean[] strongest=new boolean[2];
        var central=net.minecraft.world.phys.Vec3.atBottomCenterOf(center.getBlockPos()).add(0,.9,0);
        var eastern=central.add(2,0,0);
        for(int i=0;i<2;i++) {
            int index=i;
            var creature=new net.minecraft.world.entity.npc.Villager(EntityType.VILLAGER,h.getLevel()) {
                @Override public boolean hurt(net.minecraft.world.damagesource.DamageSource source,float amount) {
                    // The enclosure can hide part of a tall creature. Compare the two known
                    // competing sources using its actual pre-hit position and vanilla visibility.
                    double centerExposure=(1-position().distanceTo(central)/4)*net.minecraft.world.level.Explosion.getSeenPercent(central,this);
                    double eastExposure=Math.max(0,1-position().distanceTo(eastern)/2)*net.minecraft.world.level.Explosion.getSeenPercent(eastern,this);
                    double exposure=index==0 ? centerExposure : eastExposure;
                    strongest[index]=index==0 ? centerExposure>eastExposure : eastExposure>centerExposure;
                    expected[index]=(float)((exposure*exposure+exposure)*14+1);
                    hits[index]++;damage[index]=amount;return super.hurt(source,amount);
                }
            };
            creature.setNoAi(true);creature.setNoGravity(true);
            creature.setPos(net.minecraft.world.phys.Vec3.atBottomCenterOf(center.getBlockPos()).add(i==0 ? .5 : 2.5,.9,0));
            h.getLevel().addFreshEntity(creature);
        }
        h.assertTrue(RitualCrafting.activate(quietPlayer(h),center,()->0)==RitualCrafting.Outcome.EXPLOSION_PENDING,"Overlap fixture did not queue failure");
        h.runAfterDelay(30,()->{
            h.assertTrue(hits[0]==1 && hits[1]==1,"Overlapping nodes dealt multiple damage calls: "+java.util.Arrays.toString(hits));
            h.assertTrue(strongest[0] && strongest[1] && Math.abs(damage[0]-expected[0])<.001f && Math.abs(damage[1]-expected[1])<.001f,"Strongest center/Plinth hit was not selected: "+java.util.Arrays.toString(damage)+", expected "+java.util.Arrays.toString(expected));
            h.succeed();
        });
    }

    @GameTest(template="empty_9x3x9",batch="ritual_blast_cancel",timeoutTicks=90)
    public static void changedActiveNodeCancelsFailureBeforeAnyBurstOrConsumption(GameTestHelper h) {
        var center=structure(h,true);var layout=RitualCrafting.layout(center);var recipe=recipe("pf2_flicker");
        var reference=ScrollItems.scroll(recipe.spell());center.insert(reference);
        for(var part:recipe.parts())layout.stands().get(part.seat()).insert(new ItemStack(Items.DIRT));
        var creature=h.spawn(EntityType.VILLAGER,4,2,5);creature.setNoAi(true);creature.setNoGravity(true);
        h.assertTrue(RitualCrafting.activate(quietPlayer(h),center,()->0)==RitualCrafting.Outcome.EXPLOSION_PENDING,"Cancellation fixture did not queue failure");
        h.setBlock(CENTER.offset(FIXTURE.offset(1)),Blocks.AIR);
        h.runAfterDelay(35,()->{
            h.assertTrue(creature.getHealth()==20 && ItemStack.matches(reference,center.displayedItem()) && RitualTestOutput.stack(center).isEmpty(),"Cancelled failure harmed a creature or changed the reference/result");
            for(int i=0;i<8;i++)if(i!=1) {
                var node=layout.stands().get(i);
                h.assertTrue(!node.busy(),"Cancelled node retains its reservation");
                int seat=i;
                if(recipe.parts().stream().anyMatch(part->part.seat()==seat))h.assertTrue(node.displayedItem().is(Items.DIRT),"Cancelled failure consumed an unchanged offering");
            }
            h.succeed();
        });
    }

    @GameTest(template="empty_9x3x9",batch="ritual_known")
    public static void identifiedNonvolatileReferenceHasNoFailureRoll(GameTestHelper h) {
        var c=structure(h,false);var l=RitualCrafting.layout(c);c.insert(ScrollItems.scroll(id("fireball"))); for (int i:new int[]{0,2,4,6}) l.stands().get(i).insert(new ItemStack(Items.DIRT));
        var p=quietPlayer(h);SpellKnowledge.identify(p,id("fireball"));
        h.assertTrue(RitualCrafting.activate(p,c,()->{throw new AssertionError("Known stable spell rolled failure");})==RitualCrafting.Outcome.WRONG,"Stable recipe failure differs"); h.succeed();
    }
    @GameTest(template="empty_9x3x9",batch="ritual_discovery",timeoutTicks=100)
    public static void fragmentsDiscoverOnlyIntersectionAndAreConsumedExactlyOnce(GameTestHelper h) {
        var c=structure(h,false);var l=RitualCrafting.layout(c); for (int i:new int[]{0,2,4,6}) l.stands().get(i).insert(ScrollItems.fragment(id(i==0 ? "evocation" : "fire")));
        var p=quietPlayer(h);h.assertTrue(RitualCrafting.activate(p,c,()->.4)==RitualCrafting.Outcome.DISCOVERING,"Mixed fragment discovery rejected");
        h.runAfterDelay(65,()->{var output=ScrollItems.scroll(RitualTestOutput.stack(c)).orElseThrow(); var spell=NativeMagic.spells().spells().get(output.spell());
            h.assertTrue(spell.traits().rating(id("fire"))>0 && spell.traits().rating(id("evocation"))>0 && l.items().stream().allMatch(ItemStack::isEmpty),"Discovery lost its trait intersection or consumed twice");
            h.assertTrue(!SpellKnowledge.crafted(p,output.spell()) && !SpellKnowledge.identified(p,output.spell()),"Fragment discovery revealed spell or recipe"); h.succeed();});
    }
    @GameTest(template="empty_9x3x9",batch="ritual_containers",timeoutTicks=100)
    public static void milkIngredientReturnsBucketAndReferenceResultPersistIndependently(GameTestHelper h) {
        var r=RitualCrafting.catalog().recipes().values().stream().filter(recipe -> recipe.parts().stream().anyMatch(part -> part.ingredient().hint().is(Items.MILK_BUCKET))).findFirst().orElseThrow();
        var c=structure(h,r.circle()==8);var l=RitualCrafting.layout(c);fill(l,r,0);c.insert(ScrollItems.scroll(r.spell()));
        h.assertTrue(RitualCrafting.activate(quietPlayer(h),c)==RitualCrafting.Outcome.CRAFTING,"Milk recipe did not start");
        h.runAfterDelay(65,()->{
            h.assertTrue(l.items().stream().filter(s -> s.is(Items.BUCKET)).count()==1,"Milk bucket remainder lost/duplicated");
            var restored=new OfferingBlockEntity(c.getBlockPos(),c.getBlockState());restored.loadWithComponents(c.saveWithFullMetadata(h.getLevel().registryAccess()),h.getLevel().registryAccess());
            h.assertTrue(ItemStack.matches(restored.displayedItem(),c.displayedItem()) && restored.resultItem().isEmpty() && ItemStack.matches(RitualTestOutput.stack(c),ItemStack.parse(h.getLevel().registryAccess(),RitualTestOutput.stack(c).save(h.getLevel().registryAccess())).orElseThrow()) && !restored.busy(),"Reference/result did not persist independently");h.succeed();
        });
    }
    @GameTest(template="empty_9x3x9",batch="scroll_dismantling")
    public static void craftingPreviewHasNoTraitThenExactlyThreeOwnedFragmentsResolve(GameTestHelper h) {
        var input=CraftingInput.of(1,1,List.of(ScrollItems.scroll(id("fireball")))); var recipe=h.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,input,h.getLevel()).orElseThrow();
        var first=recipe.value().assemble(input,h.getLevel().registryAccess()); var second=recipe.value().assemble(input,h.getLevel().registryAccess());
        h.assertTrue(first.getCount()==3 && ItemStack.matches(first,second) && ScrollItems.fragment(first).isEmpty(),"Preview rolled fragments");
        var p=quietPlayer(h);p.getInventory().setItem(0,first);ScrollFragmentItem.resolve(p,first);
        var fragments=p.getInventory().items.stream().filter(s -> !s.isEmpty()).toList();
        h.assertTrue(fragments.stream().mapToInt(ItemStack::getCount).sum()==3 && fragments.stream().allMatch(s -> ScrollItems.fragment(s).filter(t -> NativeMagic.spells().spells().get(id("fireball")).traits().rating(t)>0).isPresent()),"Dismantling count/trait ratios wrong");
        ScrollFragmentItem.resolve(p,first);h.assertTrue(p.getInventory().items.stream().mapToInt(ItemStack::getCount).sum()==3,"Already resolved fragments rolled again");h.succeed();
    }
    @GameTest(template="empty_9x3x9",batch="scroll_cast",timeoutTicks=120)
    public static void successfulScrollCastConsumesOnceAndIdentifies(GameTestHelper h) {
        var p=quietPlayer(h); p.teleportTo(h.absolutePos(CENTER).getX()+.5,h.absolutePos(CENTER).getY(),h.absolutePos(CENTER).getZ()+.5);p.getPersistentData().putDouble("vestige:mana",200);
        var r=id("pf2_shield");p.setItemInHand(InteractionHand.MAIN_HAND,ScrollItems.scroll(r));p.getRandom().setSeed(1); h.assertTrue(!SpellKnowledge.identified(p,r),"Fixture already identified");
        h.assertTrue(ScrollCasting.cast(p,p.getMainHandItem()),"Affordable scroll cast rejected");
        h.runAfterDelay(80,()->{h.assertTrue(p.getMainHandItem().isEmpty() && SpellKnowledge.identified(p,r),"Successful cast failed consumption/identification");h.succeed();});
    }
    @GameTest(template="empty_9x3x9",batch="scroll_knowledge")
    public static void identificationAndCraftHistoryPersistSeparatelyAcrossPlayerClones(GameTestHelper h) {
        var player=quietPlayer(h);var spell=id("pf2_shield");var crafted=id("fireball");
        SpellKnowledge.identify(player,spell);SpellKnowledge.recordCraft(player,crafted);
        h.assertTrue(!SpellKnowledge.crafted(player,spell) && !SpellKnowledge.identified(player,crafted),"Knowledge states conflated");
        var clone=quietPlayer(h);
        SpellKnowledge.clonePlayer(new net.neoforged.neoforge.event.entity.player.PlayerEvent.Clone(clone,player,true));
        h.assertTrue(SpellKnowledge.identified(clone,spell) && SpellKnowledge.crafted(clone,crafted),"Death clone lost knowledge");
        var saved=player.getPersistentData().getCompound(net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG).copy();
        var restored=quietPlayer(h);
        restored.getPersistentData().put(net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG,saved);
        h.assertTrue(SpellKnowledge.identified(restored,spell) && SpellKnowledge.crafted(restored,crafted),"Saved knowledge did not restore");
        h.succeed();
    }
    @GameTest(template="empty_9x3x9",batch="scroll_cost",timeoutTicks=90)
    public static void failedPaymentKeepsScrollAndUnknownState(GameTestHelper h) {
        var p=quietPlayer(h);p.getPersistentData().putDouble("vestige:mana",0);var r=id("fireball");p.setItemInHand(InteractionHand.MAIN_HAND,ScrollItems.scroll(r));
        ScrollCasting.cast(p,p.getMainHandItem());
        h.runAfterDelay(65,()->{h.assertTrue(ScrollItems.scroll(p.getMainHandItem()).isPresent() && !SpellKnowledge.identified(p,r),"Failed payment consumed or identified scroll");h.succeed();});
    }
    @GameTest(template="empty_9x3x9",batch="scroll_silent",timeoutTicks=100)
    public static void invalidScrollAndUnaffordableRepeatRejectWithoutTextOrConsumption(GameTestHelper h) {
        var p=quietPlayer(h);var malformed=ScrollItems.scroll(id("missing_spell"));
        p.setItemInHand(InteractionHand.MAIN_HAND,malformed);
        h.assertTrue(!ScrollCasting.cast(p,malformed) && p.getMainHandItem()==malformed,"Invalid scroll was consumed");
        var spell=id("pf2_shield");SpellKnowledge.identify(p,spell);
        p.getPersistentData().putDouble("vestige:mana",100);
        p.setItemInHand(InteractionHand.MAIN_HAND,ScrollItems.scroll(spell));
        h.assertTrue(ScrollCasting.cast(p,p.getMainHandItem()),"First shield cast was rejected");
        h.runAfterDelay(1,()->{
            h.assertTrue(p.getMainHandItem().isEmpty(),"First shield scroll was not consumed");
            p.setItemInHand(InteractionHand.MAIN_HAND,ScrollItems.scroll(spell));
            com.quzzar.vestige.magic.world.NativeMana.set(p,0);
            double mana=p.getPersistentData().getDouble("vestige:mana");
            h.assertTrue(!ScrollCasting.cast(p,p.getMainHandItem()) && !p.getMainHandItem().isEmpty()
                    && p.getPersistentData().getDouble("vestige:mana")==mana,"Unaffordable repeat consumed a scroll or payment");
            NativeMagic.session(h.getLevel().getServer()).runtime().dispelActor(p.getUUID());
            h.succeed();
        });
    }
    @GameTest(template="empty_9x3x9",batch="ritual_foundations",timeoutTicks=100)
    public static void differentFoundationMaterialsDoNotAlterBaseRecipeOrBoostScroll(GameTestHelper h) {
        var c=structure(h,false);var layout=RitualCrafting.layout(c);var r=recipe("fireball");fill(layout,r,0);
        var materials=List.of(Blocks.DIAMOND_BLOCK,Blocks.AMETHYST_BLOCK,Blocks.IRON_BLOCK,Blocks.OBSIDIAN);
        for (int i=0;i<4;i++) h.setBlock(CENTER.offset(FIXTURE.offset(i*2)).below(),materials.get(i));
        h.assertTrue(RitualCrafting.activate(quietPlayer(h),c)==RitualCrafting.Outcome.CRAFTING,"Foundation imposed a recipe requirement");
        h.runAfterDelay(65,()->{h.assertTrue(ScrollItems.scroll(RitualTestOutput.stack(c)).orElseThrow().modifiers().stream().allMatch(m -> Math.abs(m.amount()-1)<1e-12),"Unapproved augmentation enabled");h.succeed();});
    }
    @GameTest(template="empty_9x3x9",batch="scroll_forfeit")
    public static void paidChaoticCastConsumesScrollWithoutIdentification(GameTestHelper h) {
        var p=quietPlayer(h);p.getPersistentData().putDouble("vestige:mana",200);p.getRandom().setSeed(4096);
        var spell=id("pf2_shield");p.setItemInHand(InteractionHand.MAIN_HAND,ScrollItems.scroll(spell));ScrollCasting.cast(p,p.getMainHandItem());
        h.assertTrue(p.getMainHandItem().isEmpty() && !SpellKnowledge.identified(p,spell) && p.getPersistentData().getDouble("vestige:mana")<200,"Paid forfeit did not consume once or identified the spell");h.succeed();
    }
    @GameTest(template="empty_9x3x9",batch="scroll_reservation",timeoutTicks=100)
    public static void movingAnUnpaidScrollInterruptsWithoutConsumption(GameTestHelper h) {
        var p=quietPlayer(h);p.getPersistentData().putDouble("vestige:mana",200);var spell=id("fireball");p.setItemInHand(InteractionHand.MAIN_HAND,ScrollItems.scroll(spell));
        ScrollCasting.cast(p,p.getMainHandItem());var scroll=p.getMainHandItem();p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);p.getInventory().setItem(8,scroll);
        h.runAfterDelay(65,()->{h.assertTrue(ScrollItems.scroll(p.getInventory().getItem(8)).isPresent() && p.getPersistentData().getDouble("vestige:mana")==200 && !SpellKnowledge.identified(p,spell),"Unpaid reserved scroll was consumed or cast after moving");h.succeed();});
    }
    @GameTest(template="empty_9x3x9",batch="scroll_recast",timeoutTicks=140)
    public static void summoningRecastKeepsOriginalPaymentAndUsesOnlyOneScroll(GameTestHelper h) {
        var p=quietPlayer(h);p.teleportTo(h.absolutePos(CENTER).getX()+.5,h.absolutePos(CENTER).getY(),h.absolutePos(CENTER).getZ()+.5);p.getPersistentData().putDouble("vestige:mana",200);
        var spell=id("pf2_summon_animal");SpellKnowledge.identify(p,spell);p.setItemInHand(InteractionHand.MAIN_HAND,ScrollItems.scroll(spell));ScrollCasting.cast(p,p.getMainHandItem());
        h.runAfterDelay(60,()->{
            double mana=p.getPersistentData().getDouble("vestige:mana");h.assertTrue(p.getMainHandItem().isEmpty() && mana<200,"First summon did not pay/consume");
            h.assertTrue(ScrollCasting.recast(p),"Awaiting scroll recast was lost after consuming its item");
            h.assertTrue(p.getPersistentData().getDouble("vestige:mana")==mana && !ScrollCasting.recast(p),"Recast paid twice or continued after completion");h.succeed();
        });
    }

}
