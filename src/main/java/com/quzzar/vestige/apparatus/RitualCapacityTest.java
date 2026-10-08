package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.VestigeMainMod;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(VestigeMainMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class RitualCapacityTest {
    private static final BlockPos CENTER=new BlockPos(4,1,4);
    private static OfferingBlockEntity structure(GameTestHelper h) {
        h.setBlock(CENTER,ApparatusBlocks.SPELLSTONE.get());
        for(var offset:LeylineStructure.offsets(LeylineShaping.Shape.CROSS,2,0))
            h.setBlock(CENTER.offset(offset),ApparatusBlocks.PLINTH.get());
        return (OfferingBlockEntity)h.getBlockEntity(CENTER);
    }
    private static void reference(OfferingBlockEntity center,String spell) {
        center.insert(ScrollItems.scroll(VestigeMainMod.location(spell)));
    }
    @GameTest(template="empty_9x3x9",batch="ritual_capacity",timeoutTicks=100)
    public static void eightSlotReferenceOnFourPlinthsSignalsCapacityWithoutChargingOrConsuming(GameTestHelper h) {
        var center=structure(h);reference(center,"pf2_flicker");
        var inner=RitualCrafting.layout(center);
        inner.stands().get(0).insert(new ItemStack(Items.PAPER));
        inner.stands().get(2).insert(new ItemStack(Items.CLOCK));
        inner.stands().get(0).installMaterial(new ItemStack(Items.GOLD_BLOCK));
        var before=RitualInputs.capture(inner);
        var reference=center.displayedItem();
        var player=h.makeMockPlayer(GameType.CREATIVE);
        h.assertTrue(RitualCrafting.activate(player,center,()->{throw new AssertionError("Capacity shortage rolled chaos");})
                ==RitualCrafting.Outcome.NEEDS_PLINTHS,"Missing outer ring was silent");
        h.assertTrue(RitualCrafting.activate(player,center)==RitualCrafting.Outcome.BUSY,"Repeated clicks bypassed throttle");
        h.runAfterDelay(45,()->{
            h.assertTrue(before.matches(inner) && ItemStack.matches(reference,center.displayedItem()),
                    "Capacity cue changed ingredients, material or reference");
            h.assertTrue(inner.blocks().stream().noneMatch(OfferingBlockEntity::busy),"Capacity cue locked nodes");
            h.assertTrue(inner.blocks().stream().allMatch(b->b.feedback()==RitualRecipe.Feedback.NONE),"Capacity cue gave misleading ingredient hints");
            h.assertTrue(RitualTestOutput.stack(center).isEmpty(),"Capacity cue produced output");h.succeed();
        });
    }
    @GameTest(template="empty_9x3x9",batch="ritual_capacity")
    public static void partialOuterRingStillSignalsCapacity(GameTestHelper h) {
        var center=structure(h);reference(center,"pf2_flicker");
        h.setBlock(CENTER.offset(3,0,-3),ApparatusBlocks.PLINTH.get());
        h.setBlock(CENTER.offset(3,0,3),ApparatusBlocks.PLINTH.get());
        h.setBlock(CENTER.offset(-3,0,3),ApparatusBlocks.PLINTH.get());
        h.assertTrue(RitualCrafting.activate(h.makeMockPlayer(GameType.SURVIVAL),center)==RitualCrafting.Outcome.NEEDS_PLINTHS,
                "Three outer Plinths incorrectly supplied eight slots");h.succeed();
    }
    @GameTest(template="empty_9x3x9",batch="ritual_capacity")
    public static void completeOuterRingReturnsOrdinaryIngredientHints(GameTestHelper h) {
        var center=structure(h);reference(center,"pf2_flicker");
        for(var offset:LeylineStructure.offsets(LeylineShaping.Shape.DIAGONAL,3,0))
            h.setBlock(CENTER.offset(offset),ApparatusBlocks.PLINTH.get());
        h.assertTrue(RitualCrafting.activate(h.makeMockPlayer(GameType.SURVIVAL),center)==RitualCrafting.Outcome.HINTS,
                "Complete layout incorrectly signaled capacity");h.succeed();
    }
    @GameTest(template="empty_9x3x9",batch="ritual_capacity")
    public static void fourSlotBloodNeedlesKeepsItsRecipeColoredInspectionWithoutCapacityCue(GameTestHelper h) {
        var center=structure(h);reference(center,"blood_needles");
        h.assertTrue(RitualCrafting.activate(h.makeMockPlayer(GameType.CREATIVE),center)==RitualCrafting.Outcome.HINTS,
                "Four-slot recipe incorrectly required outer Plinths");
        var recipe=RitualCrafting.catalog().recipes().get(VestigeMainMod.location("blood_needles"));
        h.assertTrue(center.feedback()==RitualRecipe.Feedback.CORRECT && center.feedbackColor()==recipe.color(),
                "Recipe preview rim no longer follows the spell color");h.succeed();
    }
    @GameTest(template="empty_35x15x35",batch="ritual_capacity")
    public static void ambiguousInnerRingsDoNotPretendToKnowTheMissingLayer(GameTestHelper h) {
        var center=structure(h);reference(center,"pf2_flicker");
        for(var offset:LeylineStructure.offsets(LeylineShaping.Shape.CROSS,2,2))
            h.setBlock(CENTER.offset(offset),ApparatusBlocks.PLINTH.get());
        h.assertTrue(LeylineStructure.find(center,4).size()==2,"Fixture did not create two exposed equal-radius rings");
        h.assertTrue(RitualCrafting.activate(h.makeMockPlayer(GameType.SURVIVAL),center)==RitualCrafting.Outcome.INVALID,
                "Ambiguous layout reported a specific capacity shortage");h.succeed();
    }
}
