package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.VestigeMainMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import java.util.*;
import java.util.stream.IntStream;

/** Ordered inner-layer construction; two-offering repair is unordered in either capacity. */
public final class FluxedFlintRecipe {
    public static final TagKey<Item> REPAIRABLE = TagKey.create(Registries.ITEM, VestigeMainMod.location("magical_repairable"));
    private FluxedFlintRecipe() { }
    public static List<Item> ingredients() { return List.of(Items.FLINT, Items.NETHERITE_INGOT, ScrollItems.DISSENTIENT_DIAMOND.get(), Items.NETHERITE_INGOT); }
    private static List<Integer> occupied(List<ItemStack> seats) {
        if (seats.size()!=8) return List.of();
        return IntStream.range(0,8).filter(i -> !seats.get(i).isEmpty()).boxed().toList();
    }
    public static Optional<ItemStack> create(List<ItemStack> seats) {
        if (!FourSlotPattern.matches(seats, ingredients())) return Optional.empty();
        return Optional.of(new ItemStack(ScrollItems.FLUXED_FLINT.get()));
    }
    public static Optional<ItemStack> result(RitualInputs inputs) {
        var seats = new ArrayList<ItemStack>(Collections.nCopies(8, ItemStack.EMPTY));
        inputs.nodes().forEach(n -> seats.set(n.seat(), n.offering()));
        if (inputs.geometry().slots() != 4 || !FourSlotPattern.matches(seats, ingredients())) return Optional.empty();
        var choices = EnumSet.noneOf(FluxedFlintImbuements.Choice.class);
        for (var node : inputs.nodes()) {
            if (node.material().isEmpty()) continue;
            var pair = new Spellshaping.Pair(BuiltInRegistries.ITEM.getKey(node.offering().getItem()), BuiltInRegistries.ITEM.getKey(node.material().getItem()));
            var choice = Arrays.stream(FluxedFlintImbuements.Choice.values()).filter(c -> c.matches(node.offering(), pair.material())).findFirst();
            if (choice.isPresent()) { if (!choices.add(choice.get())) return Optional.empty(); }
            else if (Spellshaping.rules().values().stream().anyMatch(r -> r.pairs().contains(pair))) return Optional.empty();
        }
        return Optional.of(FluxedFlintImbuements.create(new FluxedFlintImbuements.Variant(choices)));
    }
    public record Repair(int catalystSeat, int targetSeat, ItemStack output, ItemStack remainingCatalyst) {
        public Repair { output=output.copy(); remainingCatalyst=remainingCatalyst.copy(); }
        @Override public ItemStack output() { return output.copy(); }
        @Override public ItemStack remainingCatalyst() { return remainingCatalyst.copy(); }
        public List<Integer> used() { return List.of(catalystSeat,targetSeat).stream().sorted().toList(); }
    }
    public static Optional<Repair> repair(List<ItemStack> seats) {
        var used=occupied(seats);
        if (used.size()!=2 || used.stream().anyMatch(i -> seats.get(i).getCount()!=1)) return Optional.empty();
        var catalysts=used.stream().filter(i -> seats.get(i).is(ScrollItems.FLUXED_FLINT.get())).toList();
        if (catalysts.size()!=1) return Optional.empty();
        int catalyst=catalysts.getFirst(), target=used.getFirst()==catalyst ? used.getLast() : used.getFirst();
        var stone=seats.get(catalyst); var original=seats.get(target);
        if (!original.is(REPAIRABLE) || original.is(ScrollItems.FLUXED_FLINT.get()) || !original.isDamageableItem() || !original.isDamaged()
                || original.getDamageValue()>=original.getMaxDamage() || FluxedFlintImbuements.read(stone).isEmpty()) return Optional.empty();
        int budget=stone.getMaxDamage()-stone.getDamageValue();
        if (budget<=0) return Optional.empty();
        int restored=Math.min(Math.min(original.getDamageValue(), Math.max(1,(int)Math.ceil(original.getMaxDamage()/4.0))),budget);
        // Copy the actual offered stack, never reconstruct an item or whitelist its components.
        var output=original.copy(); output.setDamageValue(original.getDamageValue()-restored);
        var remainder=stone.copy(); remainder.setDamageValue(stone.getDamageValue()+restored);
        if (remainder.getDamageValue()>=remainder.getMaxDamage()) remainder=ItemStack.EMPTY;
        return Optional.of(new Repair(catalyst,target,output,remainder));
    }
}
