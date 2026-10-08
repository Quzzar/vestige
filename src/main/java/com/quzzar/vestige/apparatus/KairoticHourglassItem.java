package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.magic.presentation.MagicAdjectives;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

/** Position-only return; neither carrying nor using this item identifies a spell. */
public final class KairoticHourglassItem extends Item {
    public KairoticHourglassItem(Properties properties) { super(properties); }
    @Override public Component getName(ItemStack stack) {
        return HourglassData.read(stack).<Component>map(v -> MagicAdjectives.prefix(HourglassData.FAMILY,v.selections())
                .append(super.getName(stack))).orElseGet(() -> super.getName(stack));
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand) {
        var stack=player.getItemInHand(hand);
        if(level.isClientSide())return InteractionResultHolder.success(stack);
        return player instanceof ServerPlayer server && HourglassMagic.returnToPast(server,stack)
                ? InteractionResultHolder.consume(stack) : InteractionResultHolder.fail(stack);
    }
}
