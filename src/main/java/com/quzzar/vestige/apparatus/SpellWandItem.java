package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.presentation.MagicAdjectives;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Native reusable casting source; the vanilla ingredient never retains block-placement behavior. */
public final class SpellWandItem extends Item {
    public SpellWandItem(Properties properties) { super(properties); }
    /** Vanilla combining rebuilds a fresh item and discards stored magic; use exact-stack ritual repair. */
    @Override public boolean isRepairable(ItemStack stack) { return false; }
    @Override public boolean canGrindstoneRepair(ItemStack stack) { return false; }
    @Override public Component getName(ItemStack stack) {
        var binding=WandData.binding(stack).orElse(null);
        if (binding==null || !SpellKnowledge.visible(binding.scroll().spell())) return super.getName(stack).copy().withStyle(ChatFormatting.WHITE);
        var adjectives=new java.util.ArrayList<>(binding.scroll().augments().stream()
                .map(a -> MagicAdjectives.Adjustment.spellshaping(a.id(), a.degree())).toList());
        binding.tip().ifPresent(t -> adjectives.add(MagicAdjectives.Adjustment.wandTip(t.id())));
        var name=MagicAdjectives.prefix(VestigeMainMod.location("wand"), adjectives);
        name.append(ScrollItems.spellName(binding.scroll().spell()));
        var color=switch (SpellKnowledge.visibleRarity(binding.scroll().spell())) {
            case COMMON -> ChatFormatting.WHITE;case UNCOMMON -> ChatFormatting.YELLOW;
            case RARE -> ChatFormatting.AQUA;case MYTHIC -> ChatFormatting.LIGHT_PURPLE;
        };
        return Component.translatable("item.vestige.wand.named",name).withStyle(color);
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand) {
        if (level.isClientSide) return InteractionResultHolder.success(player.getItemInHand(hand));
        return WandCasting.cast(player,hand) ? InteractionResultHolder.consume(player.getItemInHand(hand)) : InteractionResultHolder.fail(player.getItemInHand(hand));
    }
    @Override public boolean isEnchantable(ItemStack stack) { return false; }
}
