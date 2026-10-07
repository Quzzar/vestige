package com.quzzar.vestige.apparatus;

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
    @Override public Component getName(ItemStack stack) {
        var binding=WandData.binding(stack).orElse(null);
        if (binding==null || !SpellKnowledge.visible(binding.scroll().spell())) return super.getName(stack).copy().withStyle(ChatFormatting.WHITE);
        var name=Component.empty();
        for (var augment:binding.scroll().augments()) name.append(Component.literal(Spellshaping.name(augment)+" ").withStyle(ChatFormatting.ITALIC));
        binding.tip().ifPresent(t -> name.append(Component.literal(t.adjective()+" ").withStyle(ChatFormatting.ITALIC)));
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
