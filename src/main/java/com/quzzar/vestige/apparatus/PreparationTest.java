package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.world.NativeMagic;
import com.quzzar.vestige.magic.world.NativeMana;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(VestigeMainMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PreparationTest {
    @GameTest(template="empty_9x3x9",batch="cast_preparation",timeoutTicks=110)
    public static void chargedScrollProgressPaysAtReleaseAndRepeatsWithoutRecovery(GameTestHelper h) {
        var player=h.makeMockPlayer(GameType.SURVIVAL);
        var id=VestigeMainMod.location("greater_heal");SpellKnowledge.identify(player,id);
        NativeMana.set(player,100);player.setHealth(4);
        player.setItemInHand(InteractionHand.MAIN_HAND,ScrollItems.scroll(id).copyWithCount(3));
        var runtime=NativeMagic.session(h.getLevel().getServer()).runtime();
        h.assertTrue(ScrollCasting.cast(player,player.getMainHandItem()),"Charge rejected");
        h.assertTrue(runtime.preparation(player.getUUID()).orElseThrow().totalTicks()==40,"Progress omitted authored time");
        h.runAfterDelay(20,()->{
            var progress=runtime.preparation(player.getUUID()).orElseThrow();
            h.assertTrue(progress.elapsedTicks()>=19 && progress.elapsedTicks()<=21
                    && NativeMana.amount(player)==100 && player.getMainHandItem().getCount()==3,"Preparation advanced incorrectly or paid early");
        });
        h.runAfterDelay(43,()->{
            h.assertTrue(runtime.preparation(player.getUUID()).isEmpty() && NativeMana.amount(player)==58
                    && player.getMainHandItem().getCount()==2,"Release did not clear progress and pay once");
            h.assertTrue(ScrollCasting.cast(player,player.getMainHandItem()) && runtime.preparation(player.getUUID()).isPresent(),"Ordinary recovery blocked the next charge");
        });
        h.runAfterDelay(87,()->{
            h.assertTrue(runtime.preparation(player.getUUID()).isEmpty() && NativeMana.amount(player)==16
                    && player.getMainHandItem().getCount()==1,"Repeat did not pay exactly once");h.succeed();
        });
    }
    @GameTest(template="empty_9x3x9",batch="cast_preparation",timeoutTicks=110)
    public static void canceledAndUnaffordableChargesClearProgressWithoutSpending(GameTestHelper h) {
        var player=h.makeMockPlayer(GameType.SURVIVAL);
        var id=VestigeMainMod.location("greater_heal");SpellKnowledge.identify(player,id);
        NativeMana.set(player,100);var scroll=ScrollItems.scroll(id);
        player.setItemInHand(InteractionHand.MAIN_HAND,scroll);
        var runtime=NativeMagic.session(h.getLevel().getServer()).runtime();
        h.assertTrue(ScrollCasting.cast(player,scroll),"Charge rejected");
        h.runAfterDelay(20,()->player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY));
        h.runAfterDelay(24,()->{
            h.assertTrue(runtime.preparation(player.getUUID()).isEmpty() && NativeMana.amount(player)==100
                    && scroll.getCount()==1,"Cancellation retained preparation or spent resources");
            player.setItemInHand(InteractionHand.MAIN_HAND,scroll);NativeMana.set(player,0);
            h.assertTrue(ScrollCasting.cast(player,scroll),"Failed-payment preparation was rejected early");
        });
        h.runAfterDelay(68,()->{
            h.assertTrue(runtime.preparation(player.getUUID()).isEmpty() && NativeMana.amount(player)==0
                    && scroll.getCount()==1,"Failed payment retained preparation or consumed scroll");h.succeed();
        });
    }
}
