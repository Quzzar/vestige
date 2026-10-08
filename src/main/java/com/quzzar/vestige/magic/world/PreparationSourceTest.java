package com.quzzar.vestige.magic.world;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.*;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import java.util.UUID;

/** Source routing exercises real reservations, shaped timing and cancellation without client-only classes. */
@GameTestHolder(VestigeMainMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PreparationSourceTest {
    @GameTest(template="empty_9x3x9",batch="cast_preparation",timeoutTicks=65)
    public static void scrollWandAndStaffProgressSelectOnlyTheReservedHand(GameTestHelper h) {
        var server=h.getLevel().getServer();var runtime=NativeMagic.session(server).runtime();
        var spell=VestigeMainMod.location("greater_heal");var definition=NativeMagic.spells().spells().get(spell);
        var scroll=ScrollItems.scroll(spell);
        var sources=java.util.List.of(scroll.copyWithCount(3),
                WandData.create(WandComponents.Base.STICK,MagicalThreadRecipe.Type.ENSORCELLED,scroll),
                StaffData.bind(StaffData.create(VestigeMainMod.location("life")),scroll,definition));
        var players=new java.util.ArrayList<net.minecraft.world.entity.player.Player>();
        for(int i=0;i<sources.size();i++) {
            var player=h.makeMockPlayer(GameType.SURVIVAL);players.add(player);
            var hand=i==1 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            player.setItemInHand(hand,sources.get(i));player.setItemInHand(hand==InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND,scroll.copy());
            NativeMana.set(player,100);SpellKnowledge.identify(player,spell);
            boolean accepted=switch(i) {
                case 0 -> ScrollCasting.cast(player,player.getItemInHand(hand));
                case 1 -> WandCasting.cast(player,hand);
                default -> StaffCasting.cast(player,hand);
            };
            h.assertTrue(accepted,"Source charge rejected: "+i);
            var preparation=runtime.preparation(player.getUUID()).orElseThrow();
            var source=NativePreparation.heldSource(player,preparation).orElseThrow();
            h.assertTrue(source.hand()==hand && source.matches(player.getItemInHand(hand)),"Preparation borrowed the wrong hand/source");
            h.assertTrue(NativePreparation.heldSource(player,new com.quzzar.vestige.magic.runtime.SpellRuntime.Preparation(UUID.randomUUID(),0,40)).isEmpty(),"Unrelated cast borrowed held animation");
            if(i==2) h.assertTrue(!source.matches(scroll),"Staff identity collided with its source scroll");
        }
        h.runAfterDelay(15,()->{
            for(var player:players) {
                var preparation=runtime.preparation(player.getUUID()).orElseThrow();
                var source=NativePreparation.heldSource(player,preparation).orElseThrow();
                player.setItemInHand(source.hand(),ItemStack.EMPTY);
            }
        });
        h.runAfterDelay(19,()->{
            for(var player:players) {
                h.assertTrue(runtime.preparation(player.getUUID()).isEmpty() && NativeMana.amount(player)==100,"Canceled source retained progress or spent mana");
            }
            h.succeed();
        });
    }
}
