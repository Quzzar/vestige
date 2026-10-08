package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.magic.definition.SpellRarity;
import com.quzzar.vestige.magic.presentation.MagicAdjectives;
import com.quzzar.vestige.VestigeMainMod;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** A scroll enters the existing native runtime; receiving one never identifies its spell. */
public final class SpellScrollItem extends Item {
    public SpellScrollItem(Properties properties) { super(properties); }
    @Override public Component getName(ItemStack stack) {
        return ScrollItems.scroll(stack).<Component>map(scroll -> SpellKnowledge.visible(scroll.spell())
                ? Component.translatable("item.vestige.spell_scroll.named", scrollName(scroll)).withStyle(nameColor(SpellKnowledge.visibleRarity(scroll.spell())))
                : super.getName(stack).copy().withStyle(ChatFormatting.WHITE))
                .orElseGet(() -> super.getName(stack).copy().withStyle(ChatFormatting.WHITE));
    }
    /** Native spell rarity selects Minecraft's ordinary item-name palette. */
    private static ChatFormatting nameColor(SpellRarity rarity) {
        return switch(rarity) {
            case COMMON -> ChatFormatting.WHITE;
            case UNCOMMON -> ChatFormatting.YELLOW;
            case RARE -> ChatFormatting.AQUA;
            case MYTHIC -> ChatFormatting.LIGHT_PURPLE;
        };
    }
    private static Component scrollName(ScrollItems.Scroll scroll) {
        var name=MagicAdjectives.prefix(VestigeMainMod.location("spell_scroll"), scroll.augments().stream()
                .map(a -> MagicAdjectives.Adjustment.spellshaping(a.id(), a.degree())).toList());
        return name.append(ScrollItems.spellName(scroll.spell()));
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) return InteractionResultHolder.success(stack);
        boolean accepted = ScrollCasting.cast(player, stack);
        return accepted ? InteractionResultHolder.consume(player.getItemInHand(hand)) : InteractionResultHolder.fail(player.getItemInHand(hand));
    }
}
