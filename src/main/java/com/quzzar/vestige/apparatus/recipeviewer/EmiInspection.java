package com.quzzar.vestige.apparatus.recipeviewer;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.world.item.ItemStack;

/** Loaded only by the opt-in EMI inspection. */
final class EmiInspection {
    static long wands(){if(EmiApi.getRecipeManager()==null)return 0;return EmiApi.getRecipeManager().getRecipes().stream().filter(r -> r.getId()!=null && r.getId().getNamespace().equals("vestige") && r.getId().getPath().startsWith("/wand/")).count();}
    static long wandMatches(ItemStack stack){return EmiApi.getRecipeManager().getRecipesByOutput(EmiStack.of(stack)).stream().filter(r -> r.getId()!=null && r.getId().getPath().startsWith("/wand/")).count();}
    static long count(){return EmiApi.getRecipeManager().getRecipes().stream().filter(r -> r.getId()!=null && r.getId().getNamespace().equals("vestige") && r.getId().getPath().startsWith("/ritual/")).count();}
    static void show(ItemStack stack){EmiApi.displayRecipes(EmiStack.of(stack));}
    static long matches(ItemStack stack,boolean uses){var manager=EmiApi.getRecipeManager();var recipes=uses?manager.getRecipesByInput(EmiStack.of(stack)):manager.getRecipesByOutput(EmiStack.of(stack));return recipes.stream().filter(r -> r.getId()!=null && r.getId().getNamespace().equals("vestige") && r.getId().getPath().startsWith("/ritual/")).count();}
    static long catalysts(ItemStack stack){return EmiApi.getRecipeManager().getRecipesByInput(EmiStack.of(stack)).stream()
            .filter(r -> r instanceof com.quzzar.vestige.apparatus.recipeviewer.emi.VestigeEmiPlugin.Recipe)
            .filter(r -> r.getCatalysts().stream().flatMap(i -> i.getEmiStacks().stream()).anyMatch(s -> s.getItemStack().is(stack.getItem())))
            .peek(r -> {if(r.getInputs().stream().flatMap(i -> i.getEmiStacks().stream()).anyMatch(s -> s.getItemStack().is(stack.getItem())))throw new IllegalStateException("Material became an EMI offering");}).count();}
    static ItemStack hovered()throws ReflectiveOperationException {
        // RecipeScreen owns the hovered widget; the general API inspects handled-screen providers instead.
        var screen=net.minecraft.client.Minecraft.getInstance().screen;
        var ingredient=(dev.emi.emi.api.stack.EmiIngredient)screen.getClass().getMethod("getHoveredStack").invoke(screen);
        return ingredient.getEmiStacks().stream().findFirst().map(EmiStack::getItemStack).orElse(ItemStack.EMPTY);
    }
    static int fireballCapacity(){return EmiApi.getRecipeManager().getRecipes().stream().filter(r -> com.quzzar.vestige.VestigeMainMod.location("/ritual/vestige/fireball").equals(r.getId())).filter(r -> r instanceof com.quzzar.vestige.apparatus.recipeviewer.emi.VestigeEmiPlugin.Recipe).mapToInt(r -> ((com.quzzar.vestige.apparatus.recipeviewer.emi.VestigeEmiPlugin.Recipe)r).display().capacity()).findFirst().orElse(0);}
}
