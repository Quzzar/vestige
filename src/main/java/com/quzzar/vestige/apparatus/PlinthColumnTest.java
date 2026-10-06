package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.VestigeMainMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(VestigeMainMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PlinthColumnTest {
    private PlinthColumnTest() { }
    @GameTest(template="empty_9x3x9",batch="plinth_column_placement")
    public static void survivalStacksMixedFinishesAndRemovalRestoresEachPart(GameTestHelper h) {
        var player=h.makeMockPlayer(GameType.SURVIVAL);player.setShiftKeyDown(true);
        var bottom=new BlockPos(4,0,4);h.setBlock(bottom.below(),Blocks.STONE);
        var materials=new ApparatusMaterials[]{ApparatusMaterials.TUFF,ApparatusMaterials.QUARTZ,ApparatusMaterials.COBBLESTONE};
        for(int i=0;i<3;i++) {
            var p=bottom.above(i);h.setBlock(p,Blocks.AIR);
            var stack=new ItemStack(ApparatusBlocks.PLINTHS.get(materials[i]).get());player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            var support=h.absolutePos(p.below());
            stack.useOn(new UseOnContext(player,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(support),Direction.UP,support,false)));
            h.assertTrue(h.getBlockState(p).is(ApparatusBlocks.PLINTHS.get(materials[i]).get()) && stack.isEmpty(),"Survival column placement failed");
        }
        h.assertTrue(h.getBlockState(bottom).getValue(ApparatusBlock.PART)==ApparatusBlock.Part.BASE,"Missing column foot");
        h.assertTrue(h.getBlockState(bottom.above()).getValue(ApparatusBlock.PART)==ApparatusBlock.Part.SHAFT,"Middle has repeated cap/foot");
        h.assertTrue(h.getBlockState(bottom.above(2)).getValue(ApparatusBlock.PART)==ApparatusBlock.Part.CAP,"Missing column crown");
        for(int i=0;i<2;i++)h.assertTrue(h.getBlockState(bottom.above(i)).getCollisionShape(h.getLevel(),h.absolutePos(bottom.above(i))).max(Direction.Axis.Y)==1,"Column segment does not reach next block");
        var original=(OfferingBlockEntity)h.getBlockEntity(bottom);original.installMaterial(new ItemStack(Items.GOLD_BLOCK));
        h.setBlock(bottom.above(2),Blocks.AIR);
        h.assertTrue(h.getBlockState(bottom.above()).getValue(ApparatusBlock.PART)==ApparatusBlock.Part.CAP,"Removing top did not cap newly exposed shaft");
        h.setBlock(bottom.above(),Blocks.AIR);
        h.assertTrue(h.getBlockState(bottom).getValue(ApparatusBlock.PART)==ApparatusBlock.Part.SINGLE,"Removing stack did not restore standalone Plinth");
        h.assertTrue(h.getBlockEntity(bottom)==original && original.materialItem().is(Items.GOLD_BLOCK),"Model state change lost socket/entity");
        h.succeed();
    }
    @GameTest(template="empty_9x3x9",batch="plinth_column_state")
    public static void allFinishesConnectByRoleAndCoveredOfferingsDropOnce(GameTestHelper h) {
        var bottom=new BlockPos(4,0,4);var player=h.makeMockPlayer(GameType.SURVIVAL);
        for(var material:ApparatusMaterials.values()) {
            h.setBlock(bottom.above(),Blocks.AIR);h.setBlock(bottom,ApparatusBlocks.PLINTHS.get(material).get());
            var node=(OfferingBlockEntity)h.getBlockEntity(bottom);node.insert(new ItemStack(Items.DIAMOND));
            h.setBlock(bottom.above(),ApparatusBlocks.PLINTH.get());
            h.assertTrue(!ApparatusBlock.exposed(h.getBlockState(bottom)) && ApparatusBlock.exposed(h.getBlockState(bottom.above())),"Finishes do not share connections");
            var hit=new BlockHitResult(Vec3.atCenterOf(h.absolutePos(bottom)),Direction.NORTH,h.absolutePos(bottom),false);
            var drops=h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(h.absolutePos(bottom)).inflate(2),e->e.getItem().is(Items.DIAMOND));
            h.assertTrue(node.displayedItem().isEmpty() && drops.size()==1 && drops.getFirst().getItem().getCount()==1,"Covered offering was lost or duplicated");
            drops.forEach(net.minecraft.world.entity.Entity::discard);
            player.getInventory().clearContent();
            var ingredient=new ItemStack(Items.PAPER);player.setItemInHand(InteractionHand.MAIN_HAND,ingredient);
            h.assertTrue(h.getBlockState(bottom).useItemOn(ingredient,h.getLevel(),player,InteractionHand.MAIN_HAND,hit)==ItemInteractionResult.CONSUME_PARTIAL,"Covered shaft allowed item-use fallback");
            h.assertTrue(ingredient.getCount()==1 && node.displayedItem().isEmpty(),"Covered shaft consumed an offering");
            h.assertTrue(!node.insert(new ItemStack(Items.PAPER)),"Covered shaft accepted a direct offering");
            h.setBlock(bottom.above(),Blocks.STONE);
            h.assertTrue(h.getBlockState(bottom).getValue(ApparatusBlock.PART)==ApparatusBlock.Part.SINGLE,"Ordinary stone incorrectly extended a Plinth column");
        }
        h.succeed();
    }
    @GameTest(template="empty_9x3x9",batch="plinth_column_ritual",timeoutTicks=100)
    public static void onlyColumnCapsParticipateAndCoveringAnActiveCapCancelsSafely(GameTestHelper h) {
        var pos=new BlockPos(4,1,4);h.setBlock(pos,ApparatusBlocks.SPELLSTONE.get());
        for(var offset:LeylineStructure.offsets(LeylineShaping.Shape.CROSS,2,0)) {
            h.setBlock(pos.offset(offset).below(),ApparatusBlocks.PLINTH.get());h.setBlock(pos.offset(offset),ApparatusBlocks.PLINTHS.get(ApparatusMaterials.TUFF).get());
        }
        var center=(OfferingBlockEntity)h.getBlockEntity(pos);var layouts=LeylineStructure.find(center,4);
        h.assertTrue(layouts.size()==1 && layouts.getFirst().geometry().innerHeight()==0,"Covered lower nodes added a competing ring");
        var layout=layouts.getFirst();var recipe=RitualCrafting.catalog().recipes().get(VestigeMainMod.location("fireball"));
        recipe.parts().forEach(p->layout.stands().get(p.seat()*2).insert(p.ingredient().hint()));
        center.insert(ScrollItems.scroll(recipe.spell()));
        h.assertTrue(RitualCrafting.activate(h.makeMockPlayer(GameType.SURVIVAL),center)==RitualCrafting.Outcome.CRAFTING,"Column caps reject normal recipe");
        var covered=layout.stands().get(0);var original=covered.displayedItem().copy();
        h.setBlock(layout.stands().get(0).getBlockPos().above().subtract(h.absolutePos(BlockPos.ZERO)),ApparatusBlocks.PLINTH.get());
        h.runAfterDelay(65,()->{
            h.assertTrue(RitualTestOutput.stack(center).isEmpty() && covered.displayedItem().isEmpty() && layout.items().stream().filter(i->!i.isEmpty()).count()==3 && h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(covered.getBlockPos()).inflate(2),e->ItemStack.matches(e.getItem(),original)).size()==1 && layout.blocks().stream().noneMatch(OfferingBlockEntity::busy),"Covering active surface consumed ingredients or retained lock");h.succeed();
        });
    }
}
