package com.quzzar.vestige.apparatus.recipeviewer.jei;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.*;
import com.quzzar.vestige.apparatus.recipeviewer.*;
import mezz.jei.api.*;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.subtypes.*;
import mezz.jei.api.recipe.*;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import mezz.jei.api.runtime.IJeiRuntime;
import java.util.List;

/** Discovered only by JEI. EMI owns its native category when both viewers are installed. */
@JeiPlugin
public final class VestigeJeiPlugin implements IModPlugin {
    private static final String VIEWER_KNOWLEDGE="vestige_viewer_identified";
    private static IJeiRuntime runtime;
    private static List<RitualDisplays.Entry> registered=List.of();
    private static List<WandDisplays.Entry> registeredWands=List.of();
    public static IJeiRuntime runtime(){return runtime;}
    @Override public void onRuntimeAvailable(IJeiRuntime available){runtime=available;refresh();refreshWands();}
    @Override public void onRuntimeUnavailable(){runtime=null;registered=List.of();registeredWands=List.of();}
    /** JEI ignores unchanged vanilla recipe events; update only changed native entries through its runtime API. */
    public static void refresh() {
        if(runtime==null || !active())return;
        var current=RitualViewerClient.displays();
        var previous=registered.stream().collect(java.util.stream.Collectors.toMap(RitualDisplays.Entry::id,java.util.function.Function.identity()));
        var next=current.stream().collect(java.util.stream.Collectors.toMap(RitualDisplays.Entry::id,java.util.function.Function.identity()));
        var removed=registered.stream().filter(entry -> !sameRecipe(entry,next.get(entry.id()))).toList();
        var added=current.stream().filter(entry -> !sameRecipe(entry,previous.get(entry.id()))).toList();
        if(!removed.isEmpty())runtime.getRecipeManager().hideRecipes(TYPE,removed);
        if(!added.isEmpty())runtime.getRecipeManager().addRecipes(TYPE,added);
        var renamed=current.stream().filter(entry -> previous.containsKey(entry.id())
                && entry.identified()!=previous.get(entry.id()).identified()).toList();
        if(!renamed.isEmpty()) {
            runtime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK,
                    renamed.stream().map(entry -> viewerStack(previous.get(entry.id()))).toList());
            runtime.getIngredientManager().addIngredientsAtRuntime(VanillaTypes.ITEM_STACK,
                    renamed.stream().map(VestigeJeiPlugin::viewerStack).toList());
        }
        registered=current;
    }
    /** Only JEI index copies carry a fixed stamp, allowing the old cached name to be removed by its old UID. */
    private static ItemStack viewerStack(RitualDisplays.Entry entry) {
        var stack=entry.output();
        if(entry.spell().isPresent())net.minecraft.world.item.component.CustomData.update(
                net.minecraft.core.component.DataComponents.CUSTOM_DATA,stack,tag -> tag.putBoolean(VIEWER_KNOWLEDGE,entry.identified()));
        return stack;
    }
    /** Ingredient-list identity includes name visibility; recipe lookup always retains the base spell identity. */
    private static String subtype(ItemStack stack,UidContext context) {
        var stone=StandingStoneDisplays.subtype(stack);if(!stone.isEmpty())return stone;
        var id=stack.is(ScrollItems.WAND.get())?WandDisplays.subtype(stack):RitualDisplays.subtype(stack);
        var scroll=ScrollItems.scroll(stack);
        if(context==UidContext.Ingredient && scroll.isPresent()) {
            var tag=stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                    net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
            boolean identified=tag.contains(VIEWER_KNOWLEDGE) ? tag.getBoolean(VIEWER_KNOWLEDGE) : SpellKnowledge.visible(scroll.get().spell());
            return id+(identified ? "|identified" : "|unknown");
        }
        return id;
    }
    private static boolean sameRecipe(RitualDisplays.Entry first,RitualDisplays.Entry second) {
        return second!=null && first.id().equals(second.id()) && first.spell().equals(second.spell())
                && first.capacity()==second.capacity() && first.offerings().equals(second.offerings()) && first.imbuements().equals(second.imbuements());
    }
    public static final RecipeType<WandDisplays.Entry> WAND_TYPE=RecipeType.create(VestigeMainMod.MOD_ID,"wand_binding",WandDisplays.Entry.class);
    public static void refreshWands(){
        if(runtime==null || !active())return;
        var next=WandViewerClient.displays();
        var previous=registeredWands.stream().collect(java.util.stream.Collectors.toMap(WandDisplays.Entry::id,java.util.function.Function.identity()));
        var current=next.stream().collect(java.util.stream.Collectors.toMap(WandDisplays.Entry::id,java.util.function.Function.identity()));
        var removed=registeredWands.stream().filter(e -> !e.equals(current.get(e.id()))).toList();
        var added=next.stream().filter(e -> !e.equals(previous.get(e.id()))).toList();
        if(!removed.isEmpty())runtime.getRecipeManager().hideRecipes(WAND_TYPE,removed);
        if(!added.isEmpty())runtime.getRecipeManager().addRecipes(WAND_TYPE,added);registeredWands=next;
    }
    public static final RecipeType<RitualDisplays.Entry> TYPE=RecipeType.create(VestigeMainMod.MOD_ID,"spellstone_ritual",RitualDisplays.Entry.class);
    private static boolean active(){return !ModList.get().isLoaded("emi");}
    @Override public ResourceLocation getPluginUid(){return VestigeMainMod.location("ritual_viewer");}
    @Override public void registerItemSubtypes(ISubtypeRegistration registration) {
        var interpreter=new ISubtypeInterpreter<ItemStack>() {
            @Override public Object getSubtypeData(ItemStack stack,UidContext context){var id=subtype(stack,context);return id.isEmpty()?null:id;}
            @Override public String getLegacyStringSubtypeInfo(ItemStack stack,UidContext context){return subtype(stack,context);}
        };
        registration.registerSubtypeInterpreter(ScrollItems.WAND.get(),interpreter);
        registration.registerSubtypeInterpreter(ScrollItems.SCROLL.get(),interpreter);
        registration.registerSubtypeInterpreter(ScrollItems.FRAGMENT.get(),interpreter);
        for(var item:com.quzzar.vestige.travel.StandingStones.STONE_ITEMS.values())registration.registerSubtypeInterpreter(item.get(),interpreter);
    }
    @Override public void registerCategories(IRecipeCategoryRegistration registration) {
        if(active()){registration.addRecipeCategories(new Category(registration.getJeiHelpers().getGuiHelper()),new WandJeiCategory(registration.getJeiHelpers().getGuiHelper()));}
    }
    @Override public void registerRecipes(IRecipeRegistration registration) {
        if(active()){registered=RitualViewerClient.displays();registration.addRecipes(TYPE,registered);registeredWands=WandViewerClient.displays();registration.addRecipes(WAND_TYPE,registeredWands);}
    }
    @Override public void registerExtraIngredients(IExtraIngredientRegistration registration) {
        if(active())registration.addExtraItemStacks(RitualViewerClient.displays().stream().map(VestigeJeiPlugin::viewerStack).toList());
    }
    @Override public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        if(active()) {
            registration.addRecipeCatalyst(new ItemStack(ApparatusBlocks.SPELLSTONE.get()),TYPE);
            registration.addRecipeCatalyst(new ItemStack(ApparatusBlocks.PLINTH.get()),TYPE);
            registration.addRecipeCatalyst(new ItemStack(ApparatusBlocks.SPELLSTONE.get()),WAND_TYPE);
            registration.addRecipeCatalyst(new ItemStack(ApparatusBlocks.PLINTH.get()),WAND_TYPE);
        }
    }
    private static final class Category implements IRecipeCategory<RitualDisplays.Entry> {
        private final IDrawable icon;
        private Category(IGuiHelper helper){icon=helper.createDrawableIngredient(VanillaTypes.ITEM_STACK,new ItemStack(ApparatusBlocks.SPELLSTONE.get()));}
        @Override public RecipeType<RitualDisplays.Entry> getRecipeType(){return TYPE;}
        @Override public Component getTitle(){return Component.translatable("vestige.viewer.title");}
        @Override public IDrawable getIcon(){return icon;}
        @Override public int getWidth(){return RitualDiagram.WIDTH;}
        @Override public int getHeight(){return RitualDiagram.HEIGHT;}
        @Override public boolean needsRecipeBorder(){return false;}
        @Override public void setRecipe(IRecipeLayoutBuilder builder,RitualDisplays.Entry recipe,IFocusGroup focus) {
            for(var offering:recipe.offerings())builder.addSlot(RecipeIngredientRole.INPUT,RitualDiagram.x(recipe,offering.seat()),RitualDiagram.y(recipe,offering.seat()))
                    .addItemStacks(RitualDisplays.alternatives(offering.ingredient()));
            for(var material:recipe.imbuements())for(var box:ImbuementFrame.hitBoxes(16))
                builder.addSlot(RecipeIngredientRole.CATALYST,RitualDiagram.x(recipe,material.seat())-9+box.x(),
                        RitualDiagram.y(recipe,material.seat())-9+box.y()).addItemStack(material.stack())
                        .setCustomRenderer(VanillaTypes.ITEM_STACK,new FrameIngredient(box.width()));
            builder.addSlot(RecipeIngredientRole.OUTPUT,RitualDiagram.OUTPUT_X,RitualDiagram.OUTPUT_Y).addItemStack(recipe.output());
            builder.addInvisibleIngredients(RecipeIngredientRole.CATALYST).addItemStacks(ApparatusBlocks.SPELLSTONES.values().stream().map(b -> new ItemStack(b.get())).toList());
            builder.addInvisibleIngredients(RecipeIngredientRole.CATALYST).addItemStacks(ApparatusBlocks.PLINTHS.values().stream().map(b -> new ItemStack(b.get())).toList());
        }
        @Override public void draw(RitualDisplays.Entry recipe,IRecipeSlotsView slots,GuiGraphics graphics,double mouseX,double mouseY){RitualDiagram.draw(graphics,recipe);}
    }
    /** Hollow rim segments retain JEI's native catalyst focus, lookup and tooltips without drawing another item. */
    private record FrameIngredient(int width) implements mezz.jei.api.ingredients.IIngredientRenderer<ItemStack> {
        @Override public int getWidth(){return width;}
        @Override public int getHeight(){return 1;}
        @Override public void render(GuiGraphics graphics,ItemStack stack){ }
        @Override public List<Component> getTooltip(ItemStack stack,net.minecraft.world.item.TooltipFlag flag) {
            var minecraft=net.minecraft.client.Minecraft.getInstance();
            var lines=new java.util.ArrayList<>(stack.getTooltipLines(net.minecraft.world.item.Item.TooltipContext.of(minecraft.level),minecraft.player,flag));
            lines.add(Component.translatable("vestige.viewer.imbuement.retained").withStyle(net.minecraft.ChatFormatting.GRAY));return lines;
        }
    }
}
