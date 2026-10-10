package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.equipment.ItemImbuements;
import com.quzzar.vestige.magic.definition.*;
import com.quzzar.vestige.magic.presentation.MagicAdjectives;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import java.util.*;

/** Craft-time choices use the shared bounded storage and immutable trait resolver. */
public final class FluxedFlintImbuements {
    public static final ResourceLocation FAMILY = VestigeMainMod.location("fluxed_flint");
    public enum Choice {
        STABILIZED("minecraft:quartz_block", 4), REINFORCED("minecraft:iron_block", 2), FRACTIOUS("minecraft:magma_block", 0);
        public final ResourceLocation material;
        public final int displaySeat;
        Choice(String material, int displaySeat) { this.material = ResourceLocation.parse(material); this.displaySeat = displaySeat; }
        public ResourceLocation id() { return VestigeMainMod.location("fluxed_flint/" + name().toLowerCase(Locale.ROOT)); }
        public boolean matches(ItemStack offering, ResourceLocation socket) {
            return material.equals(socket) && switch (this) {
                case STABILIZED -> offering.is(ScrollItems.DISSENTIENT_DIAMOND.get());
                case REINFORCED -> offering.is(Items.NETHERITE_INGOT);
                case FRACTIOUS -> offering.is(Items.FLINT);
            };
        }
    }
    private static final Set<ResourceLocation> IDS = Arrays.stream(Choice.values()).map(Choice::id).collect(java.util.stream.Collectors.toUnmodifiableSet());
    public record Variant(Set<Choice> choices) {
        public Variant { choices = Set.copyOf(choices); }
        public List<MagicAdjectives.Adjustment> selections() {
            return choices.stream().sorted().map(c -> new MagicAdjectives.Adjustment(c.id(), 1)).toList();
        }
        public int durability() {
            return (int) Math.floor(FluxedFlintItem.DURABILITY * (choices.contains(Choice.STABILIZED) ? .60 : 1)
                    * (choices.contains(Choice.REINFORCED) ? 1.5 : 1) + .5);
        }
        public double repairFraction() { return choices.contains(Choice.FRACTIOUS) ? .5 : .25; }
        public int repairCap(int maximumDurability) { return Math.max(1, (int) Math.ceil(maximumDurability * repairFraction())); }
        public TraitProfile traits() {
            var modifiers = new ArrayList<TraitModifier>();
            if (choices.contains(Choice.STABILIZED)) modifiers.add(new TraitModifier(SpecialTraits.VOLATILE, TraitModifier.Operation.MULTIPLY, .5));
            if (choices.contains(Choice.REINFORCED)) modifiers.add(new TraitModifier(SpecialTraits.VOLATILE, TraitModifier.Operation.MULTIPLY, 1.5));
            if (choices.contains(Choice.FRACTIOUS)) modifiers.add(new TraitModifier(SpecialTraits.VOLATILE, TraitModifier.Operation.MULTIPLY, 2));
            return FluxedFlintItem.TRAITS.resolve(modifiers);
        }
    }
    private FluxedFlintImbuements() { }
    public static List<Variant> variants() {
        return List.of(new Variant(Set.of()), new Variant(Set.of(Choice.STABILIZED)),
                new Variant(Set.of(Choice.REINFORCED)), new Variant(Set.of(Choice.STABILIZED, Choice.REINFORCED)),
                new Variant(Set.of(Choice.FRACTIOUS)), new Variant(Set.of(Choice.STABILIZED, Choice.FRACTIOUS)),
                new Variant(Set.of(Choice.REINFORCED, Choice.FRACTIOUS)), new Variant(Set.of(Choice.STABILIZED, Choice.REINFORCED, Choice.FRACTIOUS)));
    }
    public static ItemStack create(Variant variant) {
        var stack = new ItemStack(ScrollItems.FLUXED_FLINT.get());
        stack.set(DataComponents.MAX_DAMAGE, variant.durability());
        if (!variant.choices().isEmpty()) ItemImbuements.write(stack, FAMILY, variant.selections());
        return stack;
    }
    public static Optional<Variant> read(ItemStack stack) {
        if (!stack.is(ScrollItems.FLUXED_FLINT.get()) || stack.getCount() != 1) return Optional.empty();
        return ItemImbuements.read(stack, FAMILY, IDS).map(selections -> {
            var choices = EnumSet.noneOf(Choice.class);
            for (var selection : selections) for (var choice : Choice.values()) if (choice.id().equals(selection.id())) choices.add(choice);
            return new Variant(choices);
        }).filter(v -> stack.getMaxDamage() == v.durability() && stack.getDamageValue() >= 0
                && stack.getDamageValue() < v.durability() && stack.isDamageableItem());
    }
}
