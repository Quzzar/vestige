package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.magic.definition.SpellCost;
import com.quzzar.vestige.magic.definition.SpellDefinition;
import net.minecraft.world.item.ItemStack;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Exact casting-source identities; count, wear, names and unused staff slots cannot change mana cost. */
public final class ManaItemCosts {
    private ManaItemCosts() { }
    public sealed interface Source permits SpellSource, DeviceSource, AbilitySource {
        UUID key();
        double mana(SpellDefinition definition);
        default Optional<net.minecraft.resources.ResourceLocation> recoveryAbility() {
            return this instanceof SpellSource source ? Optional.of(source.scroll().spell()) : Optional.empty();
        }
    }
    public record Equipment(WandComponents.Base base, MagicalThreadRecipe.Type thread, Optional<WandTips.Tip> tip) { }
    public record SpellSource(ScrollItems.Scroll scroll, Optional<Equipment> equipment) implements Source {
        public UUID key() {
            var value = new StringBuilder("mana-source-v1|");
            equipment.ifPresentOrElse(e -> value.append("wand|").append(e.base().id()).append('|')
                    .append(e.thread().id()).append('|').append(e.tip().map(WandTips.Tip::id).orElse("untipped")),
                    () -> value.append("scroll"));
            value.append('|').append(scroll.spell()).append('|').append(Double.toHexString(scroll.shaping().castingCost()))
                    .append('|').append(scroll.shaping().roundAmounts());
            for (var modifier : scroll.modifiers()) value.append('|').append(modifier.trait()).append(':')
                    .append(modifier.operation()).append(':').append(Double.toHexString(modifier.amount()));
            value.append("|augments");
            for (var augment : scroll.augments()) value.append('|').append(augment.id()).append(':').append(augment.degree());
            return UUID.nameUUIDFromBytes(value.toString().getBytes(StandardCharsets.UTF_8));
        }
        public double mana(SpellDefinition definition) {
            if (definition == null) throw new IllegalArgumentException("Unavailable spell");
            var compiled = equipment.map(e -> WandComponents.compile(definition, scroll, e.base(), e.thread(), e.tip()).cast())
                    .orElseGet(() -> Spellshaping.compile(definition, scroll.augments(), scroll.modifiers(), scroll.shaping()));
            return compiled.shaping().costs(compiled.spell().costs()).stream().filter(SpellCost.Mana.class::isInstance)
                    .map(SpellCost.Mana.class::cast).mapToDouble(SpellCost.Mana::amount).sum();
        }
    }
    public enum DeviceSource implements Source {
        HOMEBOUND_EYE;
        public UUID key() { return UUID.nameUUIDFromBytes("mana-source-v1|homebound-eye".getBytes(StandardCharsets.UTF_8)); }
        public double mana(SpellDefinition ignored) { return HomeboundEyeItem.Payment.MANA.cost; }
    }
    public record AbilitySource(com.quzzar.vestige.equipment.WayfarerImbuements.Variant variant) implements Source {
        public UUID key() { return UUID.nameUUIDFromBytes(("mana-source-v1|wayfarer|" + variant.selections()).getBytes(StandardCharsets.UTF_8)); }
        public double mana(SpellDefinition ignored) {
            var ability = com.quzzar.vestige.magic.world.NativeMagic.abilities().abilities().get(com.quzzar.vestige.equipment.WayfarerImbuements.ABILITY);
            if (ability == null) throw new IllegalArgumentException("Unavailable item ability");
            return variant.shaping().costs(ability.costs()).stream().filter(SpellCost.Mana.class::isInstance).map(SpellCost.Mana.class::cast).mapToDouble(SpellCost.Mana::amount).sum();
        }
        public Optional<net.minecraft.resources.ResourceLocation> recoveryAbility() { return Optional.of(com.quzzar.vestige.equipment.WayfarerImbuements.ABILITY); }
    }
    /** Client and server derive the same bounded fingerprint without reading a client-side spell catalog. */
    public static Optional<Source> source(ItemStack stack) {
        var ability = com.quzzar.vestige.equipment.WayfarerImbuements.read(stack);
        if (ability.isPresent()) return Optional.of(new AbilitySource(ability.get()));
        var scroll = ScrollItems.scroll(stack);
        if (scroll.isPresent()) return Optional.of(new SpellSource(scroll.get(), Optional.empty()));
        var wand = WandData.binding(stack);
        if (wand.isPresent()) {
            var binding = wand.get();
            return Optional.of(new SpellSource(binding.scroll(), Optional.of(new Equipment(binding.base(), binding.thread(), binding.tip()))));
        }
        var staff = StaffData.binding(stack);
        if (staff.isPresent()) return staff.get().active().map(s -> new SpellSource(s, Optional.empty()));
        return HomeboundEyeItem.binding(stack).filter(b -> b.payment() == HomeboundEyeItem.Payment.MANA).map(b -> DeviceSource.HOMEBOUND_EYE);
    }
    public static Optional<net.minecraft.resources.ResourceLocation> recoveryAbility(ItemStack stack) {
        var source = source(stack).flatMap(Source::recoveryAbility);
        return source.isPresent() ? source : stack.getItem() instanceof com.quzzar.vestige.equipment.MagicArmorItem armor
                ? Optional.of(armor.ability()) : Optional.empty();
    }
}
