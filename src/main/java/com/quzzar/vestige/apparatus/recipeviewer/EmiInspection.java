package com.quzzar.vestige.apparatus.recipeviewer;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.world.item.ItemStack;

/** Loaded only by the opt-in EMI inspection. */
final class EmiInspection {
    static long count(){return EmiApi.getRecipeManager().getRecipes().stream().filter(r -> r.getId()!=null && r.getId().getNamespace().equals("vestige") && r.getId().getPath().startsWith("/ritual/")).count();}
    static void show(ItemStack stack){EmiApi.displayRecipes(EmiStack.of(stack));}
    static long matches(ItemStack stack,boolean uses){var manager=EmiApi.getRecipeManager();var recipes=uses?manager.getRecipesByInput(EmiStack.of(stack)):manager.getRecipesByOutput(EmiStack.of(stack));return recipes.stream().filter(r -> r.getId()!=null && r.getId().getNamespace().equals("vestige") && r.getId().getPath().startsWith("/ritual/")).count();}
    static int fireballCapacity(){return EmiApi.getRecipeManager().getRecipes().stream().filter(r -> com.quzzar.vestige.VestigeMainMod.location("/ritual/vestige/fireball").equals(r.getId())).filter(r -> r instanceof com.quzzar.vestige.apparatus.recipeviewer.emi.VestigeEmiPlugin.Recipe).mapToInt(r -> ((com.quzzar.vestige.apparatus.recipeviewer.emi.VestigeEmiPlugin.Recipe)r).display().capacity()).findFirst().orElse(0);}
}
