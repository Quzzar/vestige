package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.magic.world.NativeMagic;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import java.util.ArrayList;
import java.util.List;

/** Trait identity and committed dismantling rolls; crafting previews never consume RNG. */
public final class ScrollFragmentItem extends Item {
    public ScrollFragmentItem(Properties properties) { super(properties); }
    @Override public Component getName(ItemStack stack) {
        return ScrollItems.fragment(stack).<Component>map(trait -> Component.translatable("item.vestige.scroll_fragment.named", FragmentSymbols.label(trait))).orElse(super.getName(stack));
    }
    public static ItemStack pending(ResourceLocation spell) {
        ItemStack stack = new ItemStack(ScrollItems.FRAGMENT.get(), 3);
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putString("vestige_dismantled_spell", spell.toString()));
        return stack;
    }
    /** Called for owned inventory and cursor stacks only, never a recipe-result preview. */
    public static void resolve(Player player, ItemStack stack) {
        if (!stack.is(ScrollItems.FRAGMENT.get())) return;
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        ResourceLocation id = ResourceLocation.tryParse(tag.getString("vestige_dismantled_spell"));
        if (id == null) return;
        var spell = NativeMagic.spells().spells().get(id);
        if (spell == null || spell.traits().ratings().isEmpty()) return;
        var traits = spell.traits().ratings().entrySet().stream().sorted(java.util.Map.Entry.comparingByKey()).toList();
        double largest=traits.stream().mapToDouble(java.util.Map.Entry::getValue).max().orElseThrow();
        double total = traits.stream().mapToDouble(e -> e.getValue()/largest).sum();
        int count = stack.getCount(); List<ItemStack> results = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            double draw = player.getRandom().nextDouble() * total;
            ResourceLocation picked = traits.getLast().getKey();
            for (var entry : traits) { draw -= entry.getValue()/largest; if (draw < 0) { picked = entry.getKey(); break; } }
            results.add(ScrollItems.fragment(picked));
        }
        stack.setCount(1); stack.set(DataComponents.CUSTOM_DATA, results.getFirst().get(DataComponents.CUSTOM_DATA));
        for (int i = 1; i < results.size(); i++) if (!player.getInventory().add(results.get(i))) player.drop(results.get(i), false);
        if (player instanceof ServerPlayer serverPlayer) serverPlayer.containerMenu.broadcastChanges();
    }
}
