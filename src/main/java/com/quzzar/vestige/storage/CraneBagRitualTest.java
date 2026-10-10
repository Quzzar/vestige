package com.quzzar.vestige.storage;

import com.mojang.authlib.GameProfile;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.BundleContents;
import net.neoforged.neoforge.gametest.*;
import java.util.*;

@GameTestHolder(VestigeMainMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CraneBagRitualTest {
    private static Player player(GameTestHelper h) {
        return new Player(h.getLevel(),h.absolutePos(new BlockPos(1,1,1)),0,new GameProfile(UUID.randomUUID(),"quiet-crane-crafter")) {
            public boolean isSpectator() { return false; }
            public boolean isCreative() { return false; }
            public void displayClientMessage(Component text,boolean overlay) { throw new AssertionError("Crane ritual emitted text"); }
            public void sendSystemMessage(Component text) { throw new AssertionError("Crane ritual emitted text"); }
        };
    }
    private static ItemStack shard() {
        var g=new LeylineShaping.Geometry(8,LeylineShaping.Shape.CROSS,2,0,LeylineShaping.Shape.DIAGONAL,3,0);
        var ingredients=AttunementShardItem.ingredients();var nodes=new ArrayList<RitualInputs.Node>();
        for(int i=0;i<8;i++)nodes.add(new RitualInputs.Node(i,g.offset(i),i<ingredients.size()?new ItemStack(BuiltInRegistries.ITEM.get(ingredients.get(i))):ItemStack.EMPTY,ItemStack.EMPTY));
        return AttunementShardItem.create(new RitualInputs(g,nodes));
    }
    private static RitualCrafting.Layout ritual(GameTestHelper h,ItemStack shard,int rotation) {
        var center=new BlockPos(4,1,4);var g=new LeylineShaping.Geometry(4,LeylineShaping.Shape.CROSS,2,0,LeylineShaping.Shape.DIAGONAL,3,0);
        h.setBlock(center,ApparatusBlocks.SPELLSTONE.get());for(int i=0;i<8;i+=2)h.setBlock(center.offset(g.offset(i)),ApparatusBlocks.PLINTH.get());
        var layout=RitualCrafting.layout((OfferingBlockEntity)h.getBlockEntity(center));
        for(int i=0;i<4;i++)layout.stands().get((i*2+rotation)%8).insert(i==0?shard.copy():new ItemStack(CraneBagRecipe.ingredients().get(i)));return layout;
    }
    @GameTest(template="empty_9x3x9",batch="crane_bag_ritual",timeoutTicks=160)
    public static void twoRotatedCraftsInheritOneExistingPoolWithoutChangingItsContents(GameTestHelper h) {
        var shard=shard();var key=AttunementShardItem.signature(shard).orElseThrow().key();var storage=CraneBagStorage.get(h.getLevel().getServer());
        storage.edit(key,h.getLevel().registryAccess(),before->new BundleContents(List.of(new ItemStack(Items.DIAMOND,7))));
        var layout=ritual(h,shard,0);var p=player(h);
        layout.stands().get(0).installMaterial(new ItemStack(Items.COPPER_BLOCK));
        h.assertTrue(RitualCrafting.activate(p,layout.center())==RitualCrafting.Outcome.CRAFTING,"Crane construction rejected");
        h.assertTrue(RitualCrafting.activate(p,layout.center())==RitualCrafting.Outcome.BUSY,"Duplicate construction accepted");
        h.runAfterDelay(65,()->{
            var first=RitualTestOutput.take(layout.center());h.assertTrue(CraneBagItem.key(first).orElseThrow().equals(key)
                    && layout.items().stream().allMatch(ItemStack::isEmpty) && layout.stands().get(0).materialItem().is(Items.COPPER_BLOCK),"First output re-attuned key or consumed socket");
            var second=ritual(h,shard,2);h.assertTrue(RitualCrafting.activate(p,second.center())==RitualCrafting.Outcome.CRAFTING,"Rotated second construction rejected");
        });
        h.runAfterDelay(130,()->{var second=RitualTestOutput.take(layout.center());h.assertTrue(CraneBagItem.key(second).orElseThrow().equals(key)
                && storage.contents(key).itemCopyStream().mapToInt(ItemStack::getCount).sum()==7 && layout.blocks().stream().noneMatch(OfferingBlockEntity::busy),"Second access point altered shared pool or retained locks");h.succeed();});
    }
    @GameTest(template="empty_9x3x9",batch="crane_bag_ritual",timeoutTicks=90)
    public static void changedShardCancelsConstructionWithoutConsumingRemainingOfferings(GameTestHelper h) {
        var layout=ritual(h,shard(),0);h.assertTrue(RitualCrafting.activate(player(h),layout.center())==RitualCrafting.Outcome.CRAFTING,"Initial crane construction rejected");
        h.runAfterDelay(10,()->{var stand=layout.stands().get(0);stand.unlock();stand.remove();stand.insert(new ItemStack(ScrollItems.ATTUNEMENT_SHARD.get()));});
        h.runAfterDelay(65,()->{h.assertTrue(RitualTestOutput.stack(layout.center()).isEmpty() && layout.items().stream().filter(i->!i.isEmpty()).count()==4
                && layout.blocks().stream().noneMatch(OfferingBlockEntity::busy),"Canceled crane construction consumed inputs or retained locks");h.succeed();});
    }
    @GameTest(template="empty_9x3x9",batch="crane_bag_ritual")
    public static void mirroredPatternRejectsBeforeConsumption(GameTestHelper h) {
        var layout=ritual(h,shard(),0);var eye=layout.stands().get(2).remove();var leather=layout.stands().get(6).remove();layout.stands().get(2).insert(leather);layout.stands().get(6).insert(eye);
        h.assertTrue(RitualCrafting.activate(player(h),layout.center())==RitualCrafting.Outcome.INVALID && layout.items().stream().filter(i->!i.isEmpty()).count()==4,"Mirrored recipe crafted or consumed inputs");
        h.succeed();
    }
    @GameTest(template="empty_9x3x9",batch="crane_bag_ritual")
    public static void unboundShardRejectsBeforeLockingOrConsumption(GameTestHelper h) {
        var layout=ritual(h,shard(),0);
        layout.stands().get(0).remove();layout.stands().get(0).insert(new ItemStack(ScrollItems.ATTUNEMENT_SHARD.get()));
        h.assertTrue(RitualCrafting.activate(player(h),layout.center())==RitualCrafting.Outcome.INVALID && layout.blocks().stream().noneMatch(OfferingBlockEntity::busy),"Unbound source crafted or locked apparatus");h.succeed();
    }
}
