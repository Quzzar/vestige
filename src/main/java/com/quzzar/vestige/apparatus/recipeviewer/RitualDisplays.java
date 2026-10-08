package com.quzzar.vestige.apparatus.recipeviewer;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.*;
import com.quzzar.vestige.magic.definition.SpellRarity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import java.util.*;

/** Player-specific ritual information; offerings are sent only after successful ingredient crafting. */
public final class RitualDisplays {
    private RitualDisplays() { }
    public record Offering(int seat, RitualRecipe.Ingredient ingredient) { }
    /** Installed material belongs to an offering's Plinth and is retained after crafting. */
    public record Imbuement(int seat, ResourceLocation material) {
        public Imbuement {
            Objects.requireNonNull(material);
            if(BuiltInRegistries.BLOCK.getOptional(material).filter(block -> block.asItem()!=net.minecraft.world.item.Items.AIR).isEmpty())
                throw new IllegalArgumentException("Invalid ritual material");
        }
        public ItemStack stack(){return new ItemStack(BuiltInRegistries.BLOCK.get(material));}
    }
    public record Entry(ResourceLocation id, Optional<ResourceLocation> spell, boolean identified, SpellRarity rarity, int capacity, List<Offering> offerings, List<Imbuement> imbuements) {
        public Entry(ResourceLocation id, Optional<ResourceLocation> spell, boolean identified, SpellRarity rarity, int capacity, List<Offering> offerings) {
            this(id,spell,identified,rarity,capacity,offerings,List.of());
        }
        public Entry {
            Objects.requireNonNull(rarity);
            offerings=List.copyOf(offerings);
            imbuements=List.copyOf(imbuements);
            if (capacity!=4 && capacity!=8 || offerings.size()>capacity
                    || !identified && rarity!=SpellRarity.COMMON
                    || spell.isEmpty() && (identified || offerings.isEmpty())
                    || spell.isPresent() && !offerings.isEmpty() && offerings.size()<4
                    || offerings.stream().map(Offering::seat).distinct().count()!=offerings.size()
                    || offerings.stream().anyMatch(o -> o.seat()<0 || o.seat()>7 || capacity==4 && (o.seat()&1)!=0))
                throw new IllegalArgumentException("Invalid ritual display");
            var offeringSeats=offerings.stream().map(Offering::seat).toList();
            if(imbuements.size()>offerings.size() || imbuements.stream().map(Imbuement::seat).distinct().count()!=imbuements.size()
                    || imbuements.stream().anyMatch(m -> m.material()==null || !offeringSeats.contains(m.seat())))
                throw new IllegalArgumentException("Invalid ritual imbuement");
        }
        public boolean shapeless() { return id.equals(VestigeMainMod.location("ritual/attunement_shard")); }
        public boolean concealed() { return spell.isPresent() && offerings.isEmpty(); }
        public ItemStack output() { return com.quzzar.vestige.equipment.WayfarerDisplays.output(id).orElseGet(() -> com.quzzar.vestige.equipment.MagicArmorDisplays.output(id).orElseGet(this::ordinaryOutput)); }
        private ItemStack ordinaryOutput() { return spell.map(ScrollItems::scroll).orElseGet(() -> MagicalThreadRecipe.types().stream()
                .filter(type -> id.equals(VestigeMainMod.location("ritual/"+type.id().getPath())))
                .findFirst().map(type -> new ItemStack(type.item())).orElseGet(() -> new ItemStack(
                id.equals(VestigeMainMod.location("ritual/fluxed_flint")) ? ScrollItems.FLUXED_FLINT.get() :
                id.equals(VestigeMainMod.location("ritual/homebound_eye")) ? ScrollItems.HOMEBOUND_EYE.get() :
                id.equals(VestigeMainMod.location("ritual/whispering_shell")) ? ScrollItems.WHISPERING_SHELL.get() : ScrollItems.ATTUNEMENT_SHARD.get()))); }
        public List<Integer> seats() { return capacity==4 ? List.of(0,2,4,6) : List.of(0,1,2,3,4,5,6,7); }
    }
    public static List<Entry> threads() {
        var ingredients=MagicalThreadRecipe.ingredients();
        var offerings=java.util.stream.IntStream.range(0,ingredients.size()).mapToObj(i -> new Offering(i*2,
                new RitualRecipe.Ingredient(List.of(BuiltInRegistries.ITEM.getKey(ingredients.get(i))),List.of()))).toList();
        return MagicalThreadRecipe.types().stream().map(type -> new Entry(
                VestigeMainMod.location("ritual/"+type.id().getPath()),Optional.empty(),false,SpellRarity.COMMON,4,
                offerings,List.of(new Imbuement(0,type.material())))).toList();
    }
    public static Entry spell(RitualRecipe recipe) {
        return spell(recipe, false, false, SpellRarity.COMMON);
    }
    public static Entry spell(RitualRecipe recipe, boolean crafted, boolean identified, SpellRarity rarity) {
        var offerings = crafted ? recipe.parts().stream().map(part -> new Offering(
                recipe.circle()==4 ? part.seat()*2 : part.seat(), part.ingredient())).toList() : List.<Offering>of();
        return new Entry(VestigeMainMod.location("ritual/"+recipe.spell().getNamespace()+"/"+recipe.spell().getPath()),
                Optional.of(recipe.spell()),identified,identified ? rarity : SpellRarity.COMMON,recipe.circle(),offerings);
    }
    public static Entry shard() {
        var ingredients=AttunementShardItem.ingredients();
        return new Entry(VestigeMainMod.location("ritual/attunement_shard"),Optional.empty(),false,SpellRarity.COMMON,8,
                java.util.stream.IntStream.range(0,ingredients.size()).mapToObj(i ->
                        new Offering(i,new RitualRecipe.Ingredient(List.of(ingredients.get(i)),List.of()))).toList());
    }
    public static Entry whisperingShell() {
        var ingredients=WhisperingShellRecipe.ingredients();
        return new Entry(VestigeMainMod.location("ritual/whispering_shell"),Optional.empty(),false,SpellRarity.COMMON,4,
                java.util.stream.IntStream.range(0,ingredients.size()).mapToObj(i -> new Offering(i*2,
                        new RitualRecipe.Ingredient(List.of(BuiltInRegistries.ITEM.getKey(ingredients.get(i))),List.of()))).toList());
    }
    public static Entry fluxedFlint() {
        var ingredients=FluxedFlintRecipe.ingredients();
        return new Entry(VestigeMainMod.location("ritual/fluxed_flint"),Optional.empty(),false,SpellRarity.COMMON,4,
                java.util.stream.IntStream.range(0,4).mapToObj(i -> new Offering(i*2,
                        new RitualRecipe.Ingredient(List.of(BuiltInRegistries.ITEM.getKey(ingredients.get(i))),List.of()))).toList());
    }
    public static Entry homeboundEye() {
        var ingredients=HomeboundEyeRecipe.ingredients();
        return new Entry(VestigeMainMod.location("ritual/homebound_eye"),Optional.empty(),false,SpellRarity.COMMON,4,
                java.util.stream.IntStream.range(0,4).mapToObj(i -> new Offering(i*2,
                        new RitualRecipe.Ingredient(List.of(BuiltInRegistries.ITEM.getKey(ingredients.get(i))),List.of()))).toList());
    }
    public static List<ItemStack> alternatives(RitualRecipe.Ingredient ingredient) {
        var items=new LinkedHashMap<ResourceLocation,ItemStack>();
        ingredient.items().forEach(id -> BuiltInRegistries.ITEM.getOptional(id).filter(i -> i!=net.minecraft.world.item.Items.AIR)
                .ifPresent(item -> items.put(id,new ItemStack(item))));
        ingredient.tags().forEach(id -> BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM,id)).ifPresent(tag ->
                tag.forEach(holder -> {var item=holder.value();if(item!=net.minecraft.world.item.Items.AIR)items.putIfAbsent(BuiltInRegistries.ITEM.getKey(item),new ItemStack(item));})));
        return List.copyOf(items.values());
    }
    /** Recipe lookup ignores crafted modifiers, custom names and individual attunement keys. */
    public static String subtype(ItemStack stack) {
        return ScrollItems.scroll(stack).map(s -> s.spell().toString()).orElseGet(() -> ScrollItems.fragment(stack).map(Object::toString).orElse(""));
    }
}
