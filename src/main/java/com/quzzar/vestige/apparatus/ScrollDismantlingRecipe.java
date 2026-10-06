package com.quzzar.vestige.apparatus;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/** One scroll becomes three independently rolled fragments after the output is taken. */
public final class ScrollDismantlingRecipe extends CustomRecipe {
    public ScrollDismantlingRecipe(CraftingBookCategory category) { super(category); }
    private ItemStack input(CraftingInput input) {
        ItemStack scroll = ItemStack.EMPTY;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (!scroll.isEmpty() || ScrollItems.scroll(stack).isEmpty()) return ItemStack.EMPTY;
            scroll = stack;
        }
        return scroll;
    }
    @Override public boolean matches(CraftingInput input, Level level) { return !input(input).isEmpty(); }
    @Override public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return ScrollItems.scroll(input(input)).map(scroll -> ScrollFragmentItem.pending(scroll.spell())).orElse(ItemStack.EMPTY);
    }
    @Override public boolean canCraftInDimensions(int width, int height) { return width * height >= 1; }
    @Override public RecipeSerializer<?> getSerializer() { return ScrollItems.DISMANTLE.get(); }
}
