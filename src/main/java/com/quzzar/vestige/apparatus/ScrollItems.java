package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.definition.TraitModifier;
import com.quzzar.vestige.magic.world.NativeMagic;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Native scroll/fragment identities use vanilla persistent item components, never a foreign item registry. */
@EventBusSubscriber(modid = VestigeMainMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ScrollItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(VestigeMainMod.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, VestigeMainMod.MOD_ID);
    public static final DeferredItem<SpellScrollItem> SCROLL = ITEMS.register("spell_scroll", () -> new SpellScrollItem(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<SpellWandItem> WAND = ITEMS.register("wand", () -> new SpellWandItem(new Item.Properties().durability(20)));
    public static final DeferredItem<FluxedFlintItem> FLUXED_FLINT = ITEMS.register("fluxed_flint", () -> new FluxedFlintItem(new Item.Properties().durability(FluxedFlintItem.DURABILITY)));
    public static final DeferredItem<Item> DISSENTIENT_DIAMOND = ITEMS.register("dissentient_diamond", () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<SpellStaffItem> STAFF = ITEMS.register("staff", () -> new SpellStaffItem(new Item.Properties().durability(40)));
    public static final DeferredItem<ScrollFragmentItem> FRAGMENT = ITEMS.register("scroll_fragment", () -> new ScrollFragmentItem(new Item.Properties()));
    public static final DeferredItem<Item> ENSORCELLED_THREAD = ITEMS.register("ensorcelled_thread", () -> new Item(new Item.Properties().component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)));
    public static final DeferredItem<Item> CALLOUS_THREAD = ITEMS.register("callous_thread", () -> new Item(new Item.Properties().component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)));
    public static final DeferredItem<Item> SMOLDERING_THREAD = ITEMS.register("smoldering_thread", () -> new Item(new Item.Properties().component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)));
    public static final DeferredItem<Item> LACED_THREAD = ITEMS.register("laced_thread", () -> new Item(new Item.Properties().component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)));
    public static final DeferredItem<Item> CONSECRATED_THREAD = ITEMS.register("consecrated_thread", () -> new Item(new Item.Properties().component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)));
    public static final DeferredItem<AttunementShardItem> ATTUNEMENT_SHARD = ITEMS.register("attunement_shard", () -> new AttunementShardItem(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<WhisperingShellItem> WHISPERING_SHELL = ITEMS.register("whispering_shell", () -> new WhisperingShellItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<HomeboundEyeItem> HOMEBOUND_EYE = ITEMS.register("homebound_eye", () -> new HomeboundEyeItem(new Item.Properties().durability(HomeboundEyeItem.DURABILITY)));
    public static final DeferredItem<com.quzzar.vestige.storage.CraneBagItem> CRANE_BAG = ITEMS.register("crane_bag", () -> new com.quzzar.vestige.storage.CraneBagItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<ScrollDismantlingRecipe>> DISMANTLE = SERIALIZERS.register(
            "scroll_dismantling", () -> new SimpleCraftingRecipeSerializer<>(ScrollDismantlingRecipe::new));
    private ScrollItems() { }

    public record Scroll(ResourceLocation spell, List<TraitModifier> modifiers, com.quzzar.vestige.magic.runtime.CastShaping shaping,List<Spellshaping.Selection> augments) {
        public Scroll { modifiers = List.copyOf(modifiers); augments=List.copyOf(augments); if (!Spellshaping.validSelections(augments) || modifiers.size() > 3) throw new IllegalArgumentException("Too many scroll modifiers"); }
    }
    public static Optional<Scroll> scroll(ItemStack stack) {
        if (!stack.is(SCROLL.get())) return Optional.empty();
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.getString("vestige_spell").isBlank()) return Optional.empty();
        ResourceLocation spell = ResourceLocation.tryParse(tag.getString("vestige_spell"));
        if (spell == null || spell.getPath().isBlank()) return Optional.empty();
        boolean shaped=tag.contains("vestige_shaping");
        List<TraitModifier> modifiers = new ArrayList<>();
        ListTag list = tag.getList("vestige_modifiers", Tag.TAG_COMPOUND);
        if (list.size() > 3 || !shaped && !list.isEmpty()) return Optional.empty();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            ResourceLocation trait = ResourceLocation.tryParse(entry.getString("trait"));
            double multiplier = entry.getDouble("multiplier");
            if (trait == null || !Double.isFinite(multiplier) || multiplier < (Math.pow(trait.getPath().equals("amplify") ? .75 : .7,.3)-1e-12) || multiplier > 1.5) return Optional.empty();
            modifiers.add(new TraitModifier(trait, TraitModifier.Operation.MULTIPLY, multiplier));
        }
        if (tag.contains("vestige_shaping") && !tag.getString("vestige_shaping").equals("leyline-calculator-v3")) return Optional.empty();
        var shaping=com.quzzar.vestige.magic.runtime.CastShaping.NONE;
        if (tag.contains("vestige_shaping")) {
            double cost=tag.getDouble("vestige_casting_cost");
            if (!Double.isFinite(cost) || cost<.65 || cost>2 || modifiers.size()!=3 || modifiers.stream().map(TraitModifier::trait).distinct().count()!=3
                    || modifiers.stream().anyMatch(m -> !m.trait().getNamespace().equals("vestige") || !List.of("amplify","range","area").contains(m.trait().getPath()))) return Optional.empty();
            shaping=new com.quzzar.vestige.magic.runtime.CastShaping(cost,true);
        }
        var augments=new ArrayList<Spellshaping.Selection>();
        if (tag.contains("vestige_spellshaping") || tag.contains("vestige_augments")) {
            if (!shaped || !tag.contains("vestige_spellshaping",Tag.TAG_INT) || tag.getInt("vestige_spellshaping")!=1 || !tag.contains("vestige_augments",Tag.TAG_LIST)) return Optional.empty();
            ListTag entries=tag.getList("vestige_augments",Tag.TAG_COMPOUND);
            if (entries.isEmpty() || entries.size()>8) return Optional.empty();
            try { for(int i=0;i<entries.size();i++){var entry=entries.getCompound(i);if(!entry.contains("degree",Tag.TAG_INT))return Optional.empty();augments.add(new Spellshaping.Selection(ResourceLocation.tryParse(entry.getString("id")),entry.getInt("degree")));} }
            catch(IllegalArgumentException invalid){return Optional.empty();}
            if (!Spellshaping.validSelections(augments)) return Optional.empty();
        }
        return Optional.of(new Scroll(spell, modifiers,shaping,augments));
    }
    private static ItemStack createScroll(ResourceLocation spell,List<TraitModifier> modifiers) {
        if (modifiers.size()!=3) throw new IllegalArgumentException("Too many scroll modifiers");
        ItemStack stack = new ItemStack(SCROLL.get());
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putString("vestige_spell", spell.toString());
            ListTag list = new ListTag();
            for (TraitModifier modifier : modifiers) {
                if (modifier.operation() != TraitModifier.Operation.MULTIPLY || !Double.isFinite(modifier.amount()) || modifier.amount() < (Math.pow(modifier.trait().getPath().equals("amplify") ? .75 : .7,.3)-1e-12) || modifier.amount() > 1.5) throw new IllegalArgumentException("Invalid leyline shaping");
                CompoundTag entry = new CompoundTag(); entry.putString("trait", modifier.trait().toString()); entry.putDouble("multiplier", modifier.amount()); list.add(entry);
            }
            tag.put("vestige_modifiers", list);
        });
        return stack;
    }
    public static ItemStack shapedScroll(ResourceLocation spell,LeylineShaping.Modifiers modifiers) { return shapedScroll(spell,modifiers,List.of()); }
    public static ItemStack shapedScroll(ResourceLocation spell,LeylineShaping.Modifiers modifiers,List<Spellshaping.Selection> augments) {
        if (!Spellshaping.validSelections(augments)) throw new IllegalArgumentException("Invalid scroll augments");
        new com.quzzar.vestige.magic.runtime.CastShaping(modifiers.cost(),true);
        ItemStack stack=createScroll(spell,modifiers.traits());
        CustomData.update(DataComponents.CUSTOM_DATA,stack,tag->{tag.putString("vestige_shaping","leyline-calculator-v3");tag.putDouble("vestige_casting_cost",modifiers.cost());
            if(!augments.isEmpty()){
                tag.putInt("vestige_spellshaping",1);ListTag entries=new ListTag();
                for(var a:augments){var entry=new CompoundTag();entry.putString("id",a.id().toString());entry.putInt("degree",a.degree());entries.add(entry);}tag.put("vestige_augments",entries);
            }
        });
        return stack;
    }
    public static ItemStack scroll(ResourceLocation spell) {
        ItemStack stack=new ItemStack(SCROLL.get());
        CustomData.update(DataComponents.CUSTOM_DATA,stack,tag -> tag.putString("vestige_spell",spell.toString()));
        return stack;
    }
    public static Optional<ResourceLocation> fragment(ItemStack stack) {
        if (!stack.is(FRAGMENT.get())) return Optional.empty();
        String value=stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getString("vestige_trait");
        return value.isBlank() ? Optional.empty() : Optional.ofNullable(ResourceLocation.tryParse(value)).filter(id -> !id.getPath().isBlank());
    }
    public static ItemStack fragment(ResourceLocation trait) {
        ItemStack stack = new ItemStack(FRAGMENT.get());
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putString("vestige_trait", trait.toString()));
        return stack;
    }
    public static Component spellName(ResourceLocation spell) {
        var definition = NativeMagic.spells().spells().get(spell);
        return Component.literal(definition == null ? title(spell.getPath().replaceFirst("^pf2_", "")) : definition.source().map(s -> s.displayName()).orElse(title(spell.getPath())));
    }
    private static String title(String text) {
        StringBuilder result = new StringBuilder();
        for (String word : text.replace('_', ' ').split(" ")) {
            if (!result.isEmpty()) result.append(' ');
            result.append(List.of("of", "the", "and", "to", "in", "from").contains(word) && !result.isEmpty() ? word : Character.toUpperCase(word.charAt(0)) + word.substring(1));
        }
        return result.toString();
    }
    @SubscribeEvent public static void creative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() != CreativeModeTabs.INGREDIENTS) return;
        // Resource-backed recipes exist on both physical sides before a world is opened.
        RitualCatalog.builtinIds().forEach(id -> event.accept(scroll(id)));
        RitualCatalog.builtinTraits().forEach(id -> event.accept(fragment(id)));
        event.accept(FLUXED_FLINT.get());
        event.accept(DISSENTIENT_DIAMOND.get());
        event.accept(ATTUNEMENT_SHARD.get());
        event.accept(HOMEBOUND_EYE.get());
        event.accept(WHISPERING_SHELL.get());
        event.accept(CRANE_BAG.get());
        event.accept(com.quzzar.vestige.equipment.MagicEquipment.WARDWEAVE.get());
        event.accept(com.quzzar.vestige.equipment.MagicEquipment.CINDERWEAVE.get());
        MagicalThreadRecipe.types().forEach(type -> event.accept(type.item()));
        RitualCatalog.builtinTraits().stream().filter(StaffData::affinityAllowed).forEach(trait -> event.accept(StaffData.create(trait)));
    }
}
