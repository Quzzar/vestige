package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.world.NativeMana;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(VestigeMainMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ScrollCreativeTest {
    @GameTest(template="empty_9x3x9",batch="scroll_creative",timeoutTicks=80)
    public static void creativeScrollsBypassManaAndRepeatWithoutOrdinaryCooldowns(GameTestHelper h) {
        var player=h.makeMockPlayer(GameType.CREATIVE);
        // The mock overrides isCreative but does not install Creative inventory abilities.
        GameType.CREATIVE.updatePlayerAbilities(player.getAbilities());
        NativeMana.set(player,0);
        var firebolt=VestigeMainMod.location("firebolt");
        var needles=VestigeMainMod.location("blood_needles");
        SpellKnowledge.identify(player,firebolt);SpellKnowledge.identify(player,needles);
        player.setItemInHand(InteractionHand.MAIN_HAND,ScrollItems.scroll(firebolt));
        h.assertTrue(ScrollCasting.cast(player,player.getMainHandItem()),"Creative cast was blocked at zero mana");
        h.assertTrue(NativeMana.amount(player)==0 && player.getMainHandItem().getCount()==1,"Creative paid mana or consumed its scroll");
        h.assertTrue(ScrollCasting.cast(player,player.getMainHandItem()),"Creative retained ordinary spell cooldown");
        player.setItemInHand(InteractionHand.MAIN_HAND,ScrollItems.scroll(needles));
        h.assertTrue(ScrollCasting.cast(player,player.getMainHandItem()),"First spell cooldown blocked a different spell");
        h.assertTrue(NativeMana.amount(player)==0,"Other Creative spell paid mana");
        player.setItemInHand(InteractionHand.MAIN_HAND,ScrollItems.scroll(firebolt));
        h.assertTrue(ScrollCasting.cast(player,player.getMainHandItem()),"Switching spells acquired ordinary cooldown");h.succeed();
    }
}
