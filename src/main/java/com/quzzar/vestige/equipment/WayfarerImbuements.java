package com.quzzar.vestige.equipment;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.definition.*;
import com.quzzar.vestige.magic.presentation.MagicAdjectives;
import com.quzzar.vestige.magic.runtime.CastShaping;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import java.util.*;

/** Approved authored contributions; item data selects IDs, while all coefficients stay trusted. */
public final class WayfarerImbuements {
    public static final ResourceLocation FAMILY = VestigeMainMod.location("wayfarer_boots");
    public static final ResourceLocation ABILITY = VestigeMainMod.location("wayfarer");
    public enum Choice {
        SWIFT, ENDURING, REINFORCED, QUICKENED;
        public ResourceLocation id() { return VestigeMainMod.location("wayfarer/" + name().toLowerCase(Locale.ROOT)); }
    }
    private static final Set<ResourceLocation> IDS = Arrays.stream(Choice.values()).map(Choice::id).collect(java.util.stream.Collectors.toUnmodifiableSet());
    public record Variant(Set<Choice> choices) {
        public Variant { choices = Set.copyOf(choices); }
        public List<MagicAdjectives.Adjustment> selections() {
            return choices.stream().sorted().map(c -> new MagicAdjectives.Adjustment(c.id(), 1)).toList();
        }
        public List<TraitModifier> modifiers() {
            var modifiers = new ArrayList<TraitModifier>();
            if (choices.contains(Choice.SWIFT)) {
                modifiers.add(modifier("motion", 1.5)); modifiers.add(modifier("time", 2d / 3));
            }
            if (choices.contains(Choice.ENDURING)) modifiers.add(modifier("time", 1.2));
            return List.copyOf(modifiers);
        }
        public CastShaping shaping() {
            double mana = (choices.contains(Choice.ENDURING) ? 1.12 : 1)
                    * (choices.contains(Choice.REINFORCED) ? 1.25 : 1) * (choices.contains(Choice.QUICKENED) ? 1.25 : 1);
            return new CastShaping(1, false, new CastShaping.CostAdjustment(
                    Map.of("mana", mana, "cooldown", choices.contains(Choice.QUICKENED) ? .8 : 1), List.of(), 0, 0));
        }
        public int durability() { return choices.contains(Choice.REINFORCED) ? 98 : 65; }
        public int repair() { return durability() / 4; }
        public double mana(MagicDefinition ability) {
            return shaping().costs(ability.costs()).stream().filter(SpellCost.Mana.class::isInstance)
                    .map(SpellCost.Mana.class::cast).mapToDouble(SpellCost.Mana::amount).sum();
        }
    }
    private WayfarerImbuements() { }
    private static TraitModifier modifier(String trait, double factor) {
        return new TraitModifier(VestigeMainMod.location(trait), TraitModifier.Operation.MULTIPLY, factor);
    }
    public static Optional<Variant> read(ItemStack stack) {
        if (!stack.is(MagicEquipment.WAYFARER.get()) || stack.getCount() != 1) return Optional.empty();
        return ItemImbuements.read(stack, FAMILY, IDS).map(selections -> {
            var choices = EnumSet.noneOf(Choice.class);
            for (var selection : selections) for (var choice : Choice.values()) if (choice.id().equals(selection.id())) choices.add(choice);
            return new Variant(choices);
        }).filter(v -> stack.getMaxDamage() == v.durability() && stack.getDamageValue() >= 0 && stack.getDamageValue() < v.durability());
    }
    public static ItemStack create(Set<Choice> choices) {
        var variant = new Variant(choices);
        var stack = new ItemStack(MagicEquipment.WAYFARER.get());
        stack.set(DataComponents.MAX_DAMAGE, variant.durability());
        ItemImbuements.write(stack, FAMILY, variant.selections());
        return stack;
    }
}
