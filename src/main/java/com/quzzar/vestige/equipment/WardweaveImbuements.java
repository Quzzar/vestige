package com.quzzar.vestige.equipment;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.ImbuementMaterials;
import com.quzzar.vestige.apparatus.ScrollItems;
import com.quzzar.vestige.apparatus.Spellshaping;
import com.quzzar.vestige.magic.definition.TraitModifier;
import com.quzzar.vestige.magic.presentation.MagicAdjectives;
import com.quzzar.vestige.magic.runtime.CastShaping;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import java.util.*;

/** Trusted four-choice palette; stored selections never supply coefficients or executable plans. */
public final class WardweaveImbuements {
    public static final ResourceLocation FAMILY = VestigeMainMod.location("wardweave_robes");
    public static final ResourceLocation ABILITY = VestigeMainMod.location("wardweave");
    public enum Choice {
        WARDED, ENDURING, QUICKENED, REINFORCED;
        public ResourceLocation id() { return VestigeMainMod.location("wardweave/" + name().toLowerCase(Locale.ROOT)); }
    }
    private static final Set<ResourceLocation> IDS = Arrays.stream(Choice.values()).map(Choice::id).collect(java.util.stream.Collectors.toUnmodifiableSet());
    public record Variant(Set<Choice> choices) {
        public Variant { choices = Set.copyOf(choices); }
        public List<MagicAdjectives.Adjustment> selections() {
            return choices.stream().sorted().map(c -> new MagicAdjectives.Adjustment(c.id(), 1)).toList();
        }
        public List<TraitModifier> modifiers() {
            var result = new ArrayList<TraitModifier>();
            if (choices.contains(Choice.WARDED)) { result.add(modifier("force", 1.5)); result.add(modifier("time", .8)); }
            if (choices.contains(Choice.ENDURING)) result.add(modifier("time", 1.5));
            if (choices.contains(Choice.QUICKENED)) result.add(modifier("amplify", .8));
            if (choices.contains(Choice.REINFORCED)) result.add(modifier("amplify", .8));
            return List.copyOf(result);
        }
        public CastShaping shaping() {
            double recovery = (choices.contains(Choice.ENDURING) ? 1.2 : 1) * (choices.contains(Choice.QUICKENED) ? .8 : 1);
            return new CastShaping(1, false, new CastShaping.CostAdjustment(Map.of("cooldown", recovery), List.of(), 0, 0));
        }
        public int durability() { return choices.contains(Choice.REINFORCED) ? 120 : 80; }
        public int repair() { return durability() / 4; }
    }
    private WardweaveImbuements() { }
    private static TraitModifier modifier(String trait, double factor) {
        return new TraitModifier(VestigeMainMod.location(trait), TraitModifier.Operation.MULTIPLY, factor);
    }
    public static Optional<Variant> read(ItemStack stack) {
        if (!stack.is(MagicEquipment.WARDWEAVE.get()) || stack.getCount() != 1) return Optional.empty();
        return ItemImbuements.read(stack, FAMILY, IDS).map(selections -> {
            var choices = EnumSet.noneOf(Choice.class);
            for (var selection : selections) for (var choice : Choice.values()) if (choice.id().equals(selection.id())) choices.add(choice);
            return new Variant(choices);
        }).filter(v -> stack.getMaxDamage() == v.durability() && stack.getDamageValue() >= 0 && stack.getDamageValue() < v.durability());
    }
    public static ItemStack create(Set<Choice> choices) {
        var variant = new Variant(choices);
        var stack = new ItemStack(MagicEquipment.WARDWEAVE.get());
        stack.set(DataComponents.MAX_DAMAGE, variant.durability());
        if (!choices.isEmpty()) ItemImbuements.write(stack, FAMILY, variant.selections());
        return stack;
    }
    public static Optional<Variant> resolve(List<ItemStack> offerings, List<ItemStack> materials) {
        if (offerings.size() != 8 || materials.size() != 8) return Optional.empty();
        var choices = EnumSet.noneOf(Choice.class);
        for (int i = 0; i < 8; i++) {
            var offering = offerings.get(i); var material = materials.get(i);
            Choice choice = offering.is(Items.PUFFERFISH) && material.is(Items.IRON_BLOCK) ? Choice.WARDED
                    : Arrays.stream(DyeColor.values()).anyMatch(color -> offering.is(MagicArmorRecipe.wool(color))) && material.is(Items.AMETHYST_BLOCK) ? Choice.ENDURING
                    : offering.is(ScrollItems.CALLOUS_THREAD.get()) && material.is(Items.COPPER_BLOCK) ? Choice.QUICKENED
                    : offering.is(ScrollItems.CALLOUS_THREAD.get()) && material.is(Items.IRON_BLOCK) ? Choice.REINFORCED : null;
            if (choice != null) { if (!choices.add(choice)) return Optional.empty(); }
            else if (!material.isEmpty()) {
                var item = ImbuementMaterials.canonical(BuiltInRegistries.ITEM.getKey(offering.getItem()));
                var socket = ImbuementMaterials.canonical(BuiltInRegistries.ITEM.getKey(material.getItem()));
                if (Spellshaping.rules().values().stream().anyMatch(rule -> rule.pairs().stream().anyMatch(pair ->
                        ImbuementMaterials.canonical(pair.offering()).equals(item) && ImbuementMaterials.canonical(pair.material()).equals(socket)))) return Optional.empty();
            }
        }
        return Optional.of(new Variant(choices));
    }
}
