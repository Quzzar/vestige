package com.quzzar.vestige.apparatus;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import java.util.List;
import java.util.Optional;

/** A communicator carrying the complete, independently verifiable shard blueprint. */
public final class WhisperingShellItem extends Item {
    public WhisperingShellItem(Properties properties) { super(properties); }
    public static ItemStack bound(ItemStack shard) {
        var signature = AttunementShardItem.signature(shard).orElseThrow(() -> new IllegalArgumentException("Invalid shell attunement"));
        var result = new ItemStack(ScrollItems.WHISPERING_SHELL.get());
        CustomData.update(DataComponents.CUSTOM_DATA, result, tag -> {
            tag.putInt("vestige_shell_version", 1);
            tag.put("attunement", AttunementShardItem.encodeSignature(signature));
        });
        return result;
    }
    public static Optional<Attunement.Signature> signature(ItemStack stack) {
        if (!stack.is(ScrollItems.WHISPERING_SHELL.get())) return Optional.empty();
        var tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.getInt("vestige_shell_version") != 1 || !tag.contains("attunement", Tag.TAG_COMPOUND)) return Optional.empty();
        return AttunementShardItem.readSignature(tag.getCompound("attunement"));
    }
    public static Optional<String> key(ItemStack stack) { return signature(stack).map(Attunement.Signature::key); }
    @Override public boolean isFoil(ItemStack stack) { return key(stack).isPresent(); }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> text, TooltipFlag flag) {
        key(stack).ifPresent(key -> text.add(AttunementMark.fromKey(key).component()));
    }
}
