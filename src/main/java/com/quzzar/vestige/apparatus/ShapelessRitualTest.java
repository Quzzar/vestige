package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.VestigeMainMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.*;
import java.util.*;

@GameTestHolder(VestigeMainMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ShapelessRitualTest {
    private static final BlockPos CENTER=new BlockPos(4,1,4);
    private static final LeylineShaping.Geometry GEOMETRY=new LeylineShaping.Geometry(8,
            LeylineShaping.Shape.CROSS,2,0,LeylineShaping.Shape.DIAGONAL,3,0);
    private static RitualCrafting.Layout table(GameTestHelper h,boolean eight) {
        h.setBlock(CENTER,ApparatusBlocks.SPELLSTONE.get());
        for(int i=0;i<8;i++) if(eight || (i&1)==0) h.setBlock(CENTER.offset(GEOMETRY.offset(i)),ApparatusBlocks.PLINTH.get());
        return RitualCrafting.layout((OfferingBlockEntity)h.getBlockEntity(CENTER));
    }
    private static ItemStack target() {
        var target=StaffData.create(VestigeMainMod.location("fire"));target.setDamageValue(23);return target;
    }
    private static ItemStack shard() {
        var ingredients=AttunementShardItem.ingredients();var nodes=new ArrayList<RitualInputs.Node>();
        for(int i=0;i<8;i++) nodes.add(new RitualInputs.Node(i,GEOMETRY.offset(i),i<ingredients.size()
                ? new ItemStack(BuiltInRegistries.ITEM.get(ingredients.get(i))) : ItemStack.EMPTY,ItemStack.EMPTY));
        return AttunementShardItem.create(new RitualInputs(GEOMETRY,nodes));
    }
    private static void activate(GameTestHelper h,RitualCrafting.Layout table) {
        h.assertTrue(RitualCrafting.activate(h.makeMockPlayer(GameType.SURVIVAL),table.center(),()->1)
                ==RitualCrafting.Outcome.CRAFTING,"Shapeless arrangement rejected");
    }
    private static void repair(GameTestHelper h,boolean eight,int catalystSeat,int targetSeat,boolean explode) {
        var table=table(h,eight);var target=target();var catalyst=new ItemStack(ScrollItems.FLUXED_FLINT.get());
        table.stands().get(catalystSeat).insert(catalyst);table.stands().get(targetSeat).insert(target);
        h.assertTrue(RitualCrafting.activate(h.makeMockPlayer(GameType.SURVIVAL),table.center(),()->explode ? 0 : 1)
                ==(explode ? RitualCrafting.Outcome.EXPLOSION_PENDING : RitualCrafting.Outcome.CRAFTING),"Repair rejected");
        h.runAfterDelay(45,()->{
            var worn=catalyst.copy();if(!explode) worn.setDamageValue(10);
            h.assertTrue(ItemStack.matches(worn,table.stands().get(catalystSeat).displayedItem()),"Catalyst changed beyond the expected wear");
            h.assertTrue(table.stands().get(targetSeat).displayedItem().isEmpty(),"Target was duplicated or retained");
            if(explode) h.assertTrue(RitualTestOutput.stack(table.center()).isEmpty(),"Backfire produced an output");
            else {var expected=target.copy();expected.setDamageValue(13);h.assertTrue(ItemStack.matches(expected,RitualTestOutput.stack(table.center())),"Repair changed output beyond damage");}
            h.succeed();
        });
    }
    @GameTest(template="empty_9x3x9",batch="shapeless_ritual",timeoutTicks=90)
    public static void repairAcceptsAdjacentMixedRingSlots(GameTestHelper h) { repair(h,true,0,1,false); }
    @GameTest(template="empty_9x3x9",batch="shapeless_ritual",timeoutTicks=90)
    public static void repairAcceptsOppositeOuterSlotsWithEveryInnerSeatEmpty(GameTestHelper h) { repair(h,true,1,5,false); }
    @GameTest(template="empty_9x3x9",batch="shapeless_ritual",timeoutTicks=90)
    public static void repairStillWorksOnTheFourSlotTable(GameTestHelper h) { repair(h,false,2,4,false); }
    @GameTest(template="empty_9x3x9",batch="shapeless_ritual",timeoutTicks=90)
    public static void outerCatalystBackfireProtectsTheExactTrigger(GameTestHelper h) { repair(h,true,7,3,true); }
    @GameTest(template="empty_9x3x9",batch="shapeless_ritual",timeoutTicks=90)
    public static void constructionRejectsFourOuterOfferingsWithoutConsumption(GameTestHelper h) {
        var table=table(h,true);
        for(int i=0;i<4;i++) table.stands().get(i*2+1).insert(new ItemStack(FluxedFlintRecipe.ingredients().get(i)));
        var before=RitualInputs.capture(table);
        h.assertTrue(RitualCrafting.activate(h.makeMockPlayer(GameType.SURVIVAL),table.center(),()->{throw new AssertionError("Invalid pattern rolled risk");})
                ==RitualCrafting.Outcome.INVALID && before.matches(table) && table.blocks().stream().noneMatch(OfferingBlockEntity::busy),"Outer construction changed inputs");
        h.succeed();
    }
    @GameTest(template="empty_9x3x9",batch="shapeless_ritual")
    public static void everyRepairPairAndConstructionArrangementMatches(GameTestHelper h) {
        int repairs=0,constructions=0;
        for(int a=0;a<8;a++) for(int b=0;b<8;b++) if(a!=b) {
            var seats=new ArrayList<>(Collections.nCopies(8,ItemStack.EMPTY));
            seats.set(a,new ItemStack(ScrollItems.FLUXED_FLINT.get()));seats.set(b,target());
            var repair=FluxedFlintRecipe.repair(seats).orElseThrow();
            h.assertTrue(repair.catalystSeat()==a && repair.targetSeat()==b,"Seat roles changed");repairs++;
        }
        for(int a=0;a<8;a++) for(int b=0;b<8;b++) for(int c=0;c<8;c++) for(int d=0;d<8;d++) {
            if(a==b || a==c || b==c || a==d || b==d || c==d) continue;
            var seats=new ArrayList<>(Collections.nCopies(8,ItemStack.EMPTY));var positions=List.of(a,b,c,d);
            for(int i=0;i<4;i++) seats.set(positions.get(i),new ItemStack(FluxedFlintRecipe.ingredients().get(i)));
            boolean valid=(a&1)==0 && b==(a+2)%8 && c==(a+4)%8 && d==(a+6)%8;
            h.assertTrue(FluxedFlintRecipe.create(seats).isPresent()==valid,"Construction ignored relative order");if(valid)constructions++;
        }
        h.assertTrue(repairs==56 && constructions==4,"Arrangement audit was incomplete");h.succeed();
    }
    @GameTest(template="empty_9x3x9",batch="shapeless_ritual")
    public static void extraOuterOfferingRejectsInsteadOfFallingBackToFourSlots(GameTestHelper h) {
        var table=table(h,true);table.stands().get(0).insert(new ItemStack(ScrollItems.FLUXED_FLINT.get()));
        table.stands().get(4).insert(target());table.stands().get(1).insert(new ItemStack(Items.DIAMOND));
        var before=RitualInputs.capture(table);
        h.assertTrue(RitualCrafting.activate(h.makeMockPlayer(GameType.SURVIVAL),table.center(),()->{throw new AssertionError("Invalid recipe rolled risk");})
                ==RitualCrafting.Outcome.INVALID,"Extra offering was ignored");
        h.assertTrue(before.matches(table) && table.blocks().stream().noneMatch(OfferingBlockEntity::busy),"Rejected arrangement changed inputs");h.succeed();
    }
    @GameTest(template="empty_9x3x9",batch="shapeless_ritual",timeoutTicks=90)
    public static void editingAnUnusedOuterSlotCancelsTheSelectedEightSlotRecipe(GameTestHelper h) {
        var table=table(h,true);var catalyst=new ItemStack(ScrollItems.FLUXED_FLINT.get());var target=target();
        table.stands().get(0).insert(catalyst);table.stands().get(4).insert(target);activate(h,table);
        h.assertTrue(table.stands().get(1).busy(),"Empty outer slot was not reserved");
        h.runAfterDelay(5,()->table.stands().get(1).insert(new ItemStack(Items.PAPER)));
        h.runAfterDelay(45,()->{h.assertTrue(ItemStack.matches(catalyst,table.stands().get(0).displayedItem())
                && ItemStack.matches(target,table.stands().get(4).displayedItem()) && RitualTestOutput.stack(table.center()).isEmpty()
                && table.blocks().stream().noneMatch(OfferingBlockEntity::busy),"Edited table partially committed or leaked locks");h.succeed();});
    }
    @GameTest(template="empty_9x3x9",batch="ordered_ritual",timeoutTicks=90)
    public static void rotatedThreadKeepsStringSocketAndIgnoresOuterEdits(GameTestHelper h) {
        var table=table(h,true);table.stands().get(2).insert(new ItemStack(Items.STRING));
        table.stands().get(4).insert(new ItemStack(Items.AMETHYST_SHARD));table.stands().get(6).insert(new ItemStack(Items.HONEYCOMB));
        table.stands().get(2).installMaterial(new ItemStack(Items.GOLD_BLOCK));activate(h,table);
        h.assertTrue(!table.stands().get(1).busy() && table.stands().get(0).busy(),"Wrong active layer reserved");
        h.runAfterDelay(5,()->table.stands().get(1).insert(new ItemStack(Items.DIAMOND)));
        h.runAfterDelay(45,()->{h.assertTrue(RitualTestOutput.stack(table.center()).is(ScrollItems.SMOLDERING_THREAD.get())
                && table.stands().get(2).materialItem().is(Items.GOLD_BLOCK) && table.stands().get(1).displayedItem().is(Items.DIAMOND),"Outer edit canceled or socket changed");h.succeed();});
    }
    @GameTest(template="empty_9x3x9",batch="ordered_ritual",timeoutTicks=90)
    public static void rotatedEyeKeepsOwnSocketAndShardKey(GameTestHelper h) {
        var table=table(h,true);var shard=shard();table.stands().get(2).insert(shard);
        table.stands().get(4).insert(new ItemStack(Items.FLINT));table.stands().get(6).insert(new ItemStack(Items.SPIDER_EYE));
        table.stands().get(0).insert(new ItemStack(Items.ENDER_PEARL));table.stands().get(6).installMaterial(new ItemStack(Items.AMETHYST_BLOCK));activate(h,table);
        h.runAfterDelay(45,()->{var binding=HomeboundEyeItem.binding(RitualTestOutput.stack(table.center())).orElseThrow();
            h.assertTrue(binding.payment()==HomeboundEyeItem.Payment.MANA && binding.key().equals(AttunementShardItem.signature(shard).orElseThrow().key())
                    && table.items().stream().allMatch(ItemStack::isEmpty) && table.stands().get(6).materialItem().is(Items.AMETHYST_BLOCK),"Rotated Eye binding/selector changed");h.succeed();});
    }
    @GameTest(template="empty_9x3x9",batch="ordered_ritual",timeoutTicks=90)
    public static void rotatedShellRetainsChannel(GameTestHelper h) {
        var table=table(h,true);var shard=shard();table.stands().get(4).insert(shard);
        table.stands().get(6).insert(new ItemStack(Items.SCULK_SENSOR));table.stands().get(0).insert(new ItemStack(Items.NAUTILUS_SHELL));activate(h,table);
        h.runAfterDelay(45,()->{h.assertTrue(WhisperingShellItem.key(RitualTestOutput.stack(table.center())).orElseThrow()
                .equals(AttunementShardItem.signature(shard).orElseThrow().key()) && table.items().stream().allMatch(ItemStack::isEmpty),"Shell channel changed");h.succeed();});
    }
    @GameTest(template="empty_9x3x9",batch="ordered_ritual",timeoutTicks=90)
    public static void rotatedStoneRetainsChannel(GameTestHelper h) {
        var table=table(h,true);var shard=shard();table.stands().get(6).insert(shard);
        table.stands().get(0).insert(new ItemStack(Items.STONE_BRICKS));table.stands().get(2).insert(new ItemStack(Items.ENDER_PEARL));
        table.stands().get(4).insert(new ItemStack(Items.STONE_BRICKS));activate(h,table);
        h.runAfterDelay(45,()->{h.assertTrue(com.quzzar.vestige.travel.StandingStones.key(RitualTestOutput.stack(table.center())).orElseThrow()
                .equals(AttunementShardItem.signature(shard).orElseThrow().key()) && table.items().stream().allMatch(ItemStack::isEmpty),"Stone key changed or inputs survived");h.succeed();});
    }
}
