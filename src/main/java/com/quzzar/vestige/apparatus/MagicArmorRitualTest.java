package com.quzzar.vestige.apparatus;

import com.mojang.authlib.GameProfile;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.equipment.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.gametest.*;
import java.util.*;

@GameTestHolder(VestigeMainMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MagicArmorRitualTest {
    private static RitualCrafting.Layout setup(GameTestHelper h, boolean cinder, boolean mixed) {
        var center=new BlockPos(4,1,4);var geometry=new LeylineShaping.Geometry(8,LeylineShaping.Shape.CROSS,2,0,LeylineShaping.Shape.DIAGONAL,3,0);
        h.setBlock(center,ApparatusBlocks.SPELLSTONE.get());
        for(int i=0;i<8;i++)h.setBlock(center.offset(geometry.offset(i)),ApparatusBlocks.PLINTH.get());
        var layout=LeylineStructure.find((OfferingBlockEntity)h.getBlockEntity(center),8).getFirst();
        for(int i=0;i<8;i++) {
            Item item=i%2==0 ? mixed && i==4 ? Items.WHITE_WOOL : Items.BLUE_WOOL
                    : i==1 || i==5 ? cinder ? ScrollItems.SMOLDERING_THREAD.get() : ScrollItems.CALLOUS_THREAD.get()
                    : i==3 ? cinder ? Items.BLAZE_POWDER : Items.IRON_INGOT : cinder ? Items.MAGMA_CREAM : Items.PUFFERFISH;
            layout.stands().get(i).insert(new ItemStack(item));layout.stands().get(i).installMaterial(new ItemStack(Items.DIAMOND_BLOCK));
        }
        return layout;
    }
    private static Player player(GameTestHelper h) {
        return new Player(h.getLevel(),h.absolutePos(new BlockPos(4,1,4)),0,new GameProfile(UUID.randomUUID(),"quiet-robe-crafter")) {
            public boolean isSpectator(){return false;}public boolean isCreative(){return false;}
            public void displayClientMessage(net.minecraft.network.chat.Component text,boolean overlay){throw new AssertionError("Robe ritual emitted text");}
            public void sendSystemMessage(net.minecraft.network.chat.Component text){throw new AssertionError("Robe ritual emitted text");}
        };
    }
    private static void craft(GameTestHelper h, boolean cinder) {
        var layout=setup(h,cinder,false);h.assertTrue(RitualCrafting.activate(player(h),layout.center())==RitualCrafting.Outcome.CRAFTING,"Robe recipe rejected");
        h.runAfterDelay(65,()->{
            var result=RitualTestOutput.stack(layout.center());
            h.assertTrue(result.is(cinder?MagicEquipment.CINDERWEAVE.get():MagicEquipment.WARDWEAVE.get()) && result.getCount()==1
                    && result.get(DataComponents.DYED_COLOR).rgb()==DyeColor.BLUE.getTextureDiffuseColor(),"Incorrect robe/drop color");
            h.assertTrue(layout.items().stream().allMatch(ItemStack::isEmpty) && layout.center().displayedItem().isEmpty(),"Robe ingredients not consumed");
            h.assertTrue(layout.stands().stream().allMatch(s->s.materialItem().is(Items.DIAMOND_BLOCK)) && !result.has(DataComponents.CUSTOM_DATA),"Socket contributed incidental spell shaping or was consumed");h.succeed();
        });
    }
    @GameTest(template="empty_9x3x9",batch="robe_rituals",timeoutTicks=90)
    public static void wardweaveConsumesEightOffersAndRetainsSockets(GameTestHelper h){craft(h,false);}
    @GameTest(template="empty_9x3x9",batch="robe_rituals",timeoutTicks=90)
    public static void cinderweaveConsumesEightOffersAndRetainsSockets(GameTestHelper h){craft(h,true);}
    @GameTest(template="empty_9x3x9",batch="robe_rituals")
    public static void mixedWoolRejectsWithoutConsumption(GameTestHelper h) {
        var layout=setup(h,false,true);var before=layout.items().stream().map(ItemStack::copy).toList();
        h.assertTrue(RitualCrafting.activate(player(h),layout.center())!=RitualCrafting.Outcome.CRAFTING,"Mixed colors accepted");
        for(int i=0;i<8;i++)h.assertTrue(ItemStack.matches(before.get(i),layout.items().get(i)),"Rejected recipe consumed offering");h.succeed();
    }
    @GameTest(template="empty_9x3x9",batch="robe_ritual_cancel",timeoutTicks=90)
    public static void changingAnOfferingDuringAnimationCancelsAtomically(GameTestHelper h) {
        var layout=setup(h,false,false);h.assertTrue(RitualCrafting.activate(player(h),layout.center())==RitualCrafting.Outcome.CRAFTING,"Robe recipe rejected");
        layout.stands().get(4).remove();layout.stands().get(4).insert(new ItemStack(Items.RED_WOOL));
        h.runAfterDelay(65,()->{h.assertTrue(RitualTestOutput.stack(layout.center()).isEmpty() && layout.items().stream().noneMatch(ItemStack::isEmpty),"Canceled craft emitted output or consumed other ingredients");h.succeed();});
    }
}
