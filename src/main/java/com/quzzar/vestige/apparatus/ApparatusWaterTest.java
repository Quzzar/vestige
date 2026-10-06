package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.VestigeMainMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import java.util.List;

@GameTestHolder(VestigeMainMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ApparatusWaterTest {
    private ApparatusWaterTest() { }

    @GameTest(template="empty_3x3x3",batch="apparatus_clearance")
    public static void plinthOfferingsRequireAirOrUnobstructedFluidAbove(GameTestHelper h) {
        var player=h.makeMockPlayer(GameType.SURVIVAL);var pos=new BlockPos(1,0,1);
        var absolute=h.absolutePos(pos);var top=new BlockHitResult(Vec3.atCenterOf(absolute),Direction.UP,absolute,false);
        var allowed=List.of(Blocks.AIR.defaultBlockState(),Blocks.CAVE_AIR.defaultBlockState(),Blocks.WATER.defaultBlockState(),
                Fluids.FLOWING_WATER.getFlowing(4,false).createLegacyBlock(),Blocks.LAVA.defaultBlockState(),Blocks.BUBBLE_COLUMN.defaultBlockState());
        var blocked=List.of(Blocks.STONE.defaultBlockState(),Blocks.GLASS.defaultBlockState(),Blocks.DANDELION.defaultBlockState(),
                Blocks.OAK_SLAB.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED,true));
        for(var finish:ApparatusBlocks.PLINTHS.values()) {
            h.setBlock(pos,finish.get());var node=(OfferingBlockEntity)h.getBlockEntity(pos);
            for(var above:allowed) {
                h.setBlock(pos.above(),above);var source=new ItemStack(Items.DIAMOND,2);player.setItemInHand(InteractionHand.MAIN_HAND,source);
                h.getBlockState(pos).useItemOn(source,h.getLevel(),player,InteractionHand.MAIN_HAND,top);
                h.assertTrue(source.getCount()==1 && node.displayedItem().is(Items.DIAMOND),"Open air/fluid rejected offering: "+above);
                node.remove();
            }
            for(var above:blocked) {
                h.setBlock(pos.above(),above);var source=new ItemStack(Items.DIRT,2);player.setItemInHand(InteractionHand.MAIN_HAND,source);
                var result=h.getBlockState(pos).useItemOn(source,h.getLevel(),player,InteractionHand.MAIN_HAND,top);
                h.assertTrue(result==ItemInteractionResult.CONSUME_PARTIAL && source.getCount()==2 && node.displayedItem().isEmpty(),"Covered offering consumed an ingredient or permitted placement fallback: "+above);
                h.assertTrue(!node.insert(new ItemStack(Items.DIAMOND)),"Direct insertion bypassed covered surface");
            }
            h.setBlock(pos.above(),Blocks.AIR);node.insert(new ItemStack(Items.DIAMOND));h.setBlock(pos.above(),Blocks.STONE);
            h.assertTrue(!node.hasOfferingSpace() && node.displayedItem().is(Items.DIAMOND),"Covering deleted a stored offering");
            player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
            h.getBlockState(pos).useWithoutItem(h.getLevel(),player,new BlockHitResult(Vec3.atCenterOf(absolute),Direction.NORTH,absolute,false));
            h.assertTrue(node.displayedItem().isEmpty() && player.getInventory().items.stream().anyMatch(s->s.is(Items.DIAMOND)),"Covered offering was not recoverable");
            player.getInventory().clearContent();
        }
        h.succeed();
    }

    @GameTest(template="empty_3x3x3",batch="apparatus_waterlogging")
    public static void everyFinishPlacesInWaterAndBucketsPreserveItsContents(GameTestHelper h) {
        var player=h.makeMockPlayer(GameType.SURVIVAL);var pos=new BlockPos(1,1,1);var absolute=h.absolutePos(pos);
        for(var block:ApparatusBlocks.all())for(var water:List.of(Fluids.WATER.getSource(false),Fluids.FLOWING_WATER.getFlowing(4,false))) {
            h.setBlock(pos.below(),Blocks.STONE);h.setBlock(pos.above(),Blocks.AIR);h.setBlock(pos,water.createLegacyBlock());
            var source=new ItemStack(block);player.setItemInHand(InteractionHand.MAIN_HAND,source);
            source.useOn(new UseOnContext(player,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(absolute.below()),Direction.UP,absolute.below(),false)));
            var state=h.getBlockState(pos);var node=(OfferingBlockEntity)h.getBlockEntity(pos);
            h.assertTrue(state.is(block) && source.isEmpty() && state.getValue(ApparatusBlock.WATERLOGGED) && state.getFluidState().is(FluidTags.WATER),"Underwater survival placement lost water: "+block);
            var offered=new ItemStack(Items.DIAMOND);offered.set(DataComponents.CUSTOM_NAME,Component.literal("Submerged offering"));node.insert(offered);
            if(block.role()==ApparatusBlock.Role.PLINTH)node.installMaterial(new ItemStack(Items.GOLD_BLOCK));
            var bucket=((SimpleWaterloggedBlock)block).pickupBlock(player,h.getLevel(),absolute,state);
            h.assertTrue(bucket.is(Items.WATER_BUCKET) && !h.getBlockState(pos).getValue(ApparatusBlock.WATERLOGGED) && h.getBlockState(pos).getFluidState().isEmpty(),"Bucket did not drain apparatus");
            h.assertTrue(h.getBlockEntity(pos)==node && ItemStack.matches(node.displayedItem(),offered),"Draining replaced state or lost offering");
            h.assertTrue(((SimpleWaterloggedBlock)block).placeLiquid(h.getLevel(),absolute,h.getBlockState(pos),Fluids.WATER.getSource(false)),"Water bucket refill failed");
            h.assertTrue(!((SimpleWaterloggedBlock)block).placeLiquid(h.getLevel(),absolute,h.getBlockState(pos),Fluids.WATER.getSource(false)),"Refilling an already wet block accepted a duplicate source");
            h.assertTrue(h.getBlockEntity(pos)==node && ItemStack.matches(node.displayedItem(),offered) && h.getBlockState(pos).getValue(ApparatusBlock.WATERLOGGED),"Refilling lost contents or water");
            if(block.role()==ApparatusBlock.Role.PLINTH)h.assertTrue(node.materialItem().is(Items.GOLD_BLOCK),"Bucket interaction lost imbuement");
            h.setBlock(pos,Blocks.AIR);
        }
        h.succeed();
    }

    @GameTest(template="empty_9x3x9",batch="apparatus_column_sockets")
    public static void everyColumnSegmentCanBeImbuedWhileWetOrCovered(GameTestHelper h) {
        var player=h.makeMockPlayer(GameType.SURVIVAL);var bottom=new BlockPos(4,0,4);
        var materials=List.of(Items.AMETHYST_BLOCK,Items.IRON_BLOCK,Items.GOLD_BLOCK);
        for(var finish:ApparatusBlocks.PLINTHS.values()) {
            for(int i=0;i<3;i++)h.setBlock(bottom.above(i),finish.get().defaultBlockState().setValue(ApparatusBlock.WATERLOGGED,true));
            for(int i=0;i<3;i++) {
                var pos=bottom.above(i);var absolute=h.absolutePos(pos);var node=(OfferingBlockEntity)h.getBlockEntity(pos);
                var source=new ItemStack(materials.get(i),2);player.setItemInHand(InteractionHand.MAIN_HAND,source);player.setShiftKeyDown(false);
                h.getBlockState(pos).useItemOn(source,h.getLevel(),player,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(absolute),Direction.NORTH,absolute,false));
                h.assertTrue(source.getCount()==1 && node.materialItem().is(materials.get(i)),"Covered/wet column socket rejected side installation");
                h.assertTrue(h.getBlockState(pos).getValue(ApparatusBlock.WATERLOGGED),"Column connection lost waterlogging");
            }
            h.setBlock(bottom.above(2),Blocks.AIR);
            h.assertTrue(h.getBlockState(bottom.above()).getValue(ApparatusBlock.PART)==ApparatusBlock.Part.CAP && h.getBlockState(bottom.above()).getValue(ApparatusBlock.WATERLOGGED),"Removing wet cap lost connection/water");
            for(int i=0;i<2;i++) {
                var pos=bottom.above(i);var absolute=h.absolutePos(pos);var node=(OfferingBlockEntity)h.getBlockEntity(pos);
                var expected=materials.get(i);
                player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);player.setShiftKeyDown(true);
                h.getBlockState(pos).useWithoutItem(h.getLevel(),player,new BlockHitResult(Vec3.atCenterOf(absolute),Direction.NORTH,absolute,false));
                h.assertTrue(node.materialItem().isEmpty() && player.getInventory().items.stream().anyMatch(s->s.is(expected)),"Covered/wet socket was not individually recoverable");
            }
            player.getInventory().clearContent();for(int i=0;i<3;i++)h.setBlock(bottom.above(i),Blocks.AIR);
        }
        h.succeed();
    }

    @GameTest(template="empty_9x3x9",batch="apparatus_underwater_ritual",timeoutTicks=100)
    public static void submergedSpellstoneAndPlinthsCraftNormally(GameTestHelper h) {
        var layout=readyRitual(h,true);
        h.assertTrue(RitualCrafting.activate(h.makeMockPlayer(GameType.SURVIVAL),layout.center())==RitualCrafting.Outcome.CRAFTING,"Submerged recipe did not start");
        h.runAfterDelay(65,()->{
            h.assertTrue(!RitualTestOutput.stack(layout.center()).isEmpty() && layout.stands().get(0).displayedItem().isEmpty(),"Submerged recipe did not commit");
            h.assertTrue(layout.blocks().stream().allMatch(b->b.getBlockState().getValue(ApparatusBlock.WATERLOGGED) && !b.busy()),"Ritual removed water or retained locks");
            h.succeed();
        });
    }

    @GameTest(template="empty_9x3x9",batch="apparatus_cover_cancellation",timeoutTicks=100)
    public static void ordinarySolidCoverCancelsReservedRecipeBeforeConsumption(GameTestHelper h) {
        var layout=readyRitual(h,false);
        h.assertTrue(RitualCrafting.activate(h.makeMockPlayer(GameType.SURVIVAL),layout.center())==RitualCrafting.Outcome.CRAFTING,"Recipe did not start");
        var node=layout.stands().get(0);h.getLevel().setBlock(node.getBlockPos().above(),Blocks.GLASS.defaultBlockState(),3);
        h.assertTrue(LeylineStructure.find(layout.center(),4).isEmpty(),"Solid-covered surface still supplied a ritual node");
        h.runAfterDelay(65,()->{
            h.assertTrue(RitualTestOutput.stack(layout.center()).isEmpty() && !node.displayedItem().isEmpty() && layout.blocks().stream().noneMatch(OfferingBlockEntity::busy),"Solid cover consumed ingredients or retained locks");
            h.succeed();
        });
    }

    private static RitualCrafting.Layout readyRitual(GameTestHelper h,boolean wet) {
        var pos=new BlockPos(4,0,4);h.setBlock(pos,ApparatusBlocks.SPELLSTONE.get().defaultBlockState().setValue(ApparatusBlock.WATERLOGGED,wet));
        for(var offset:LeylineStructure.offsets(LeylineShaping.Shape.CROSS,2,0)) {
            var p=pos.offset(offset);h.setBlock(p,ApparatusBlocks.PLINTH.get().defaultBlockState().setValue(ApparatusBlock.WATERLOGGED,wet));
            h.setBlock(p.above(),wet ? Blocks.WATER : Blocks.AIR);
        }
        h.setBlock(pos.above(),wet ? Blocks.WATER : Blocks.AIR);
        var center=(OfferingBlockEntity)h.getBlockEntity(pos);var layout=LeylineStructure.find(center,4).getFirst();
        var recipe=RitualCrafting.catalog().recipes().get(VestigeMainMod.location("fireball"));
        recipe.parts().forEach(p->layout.stands().get(p.seat()*2).insert(p.ingredient().hint()));
        center.insert(ScrollItems.scroll(recipe.spell()));return layout;
    }
}
