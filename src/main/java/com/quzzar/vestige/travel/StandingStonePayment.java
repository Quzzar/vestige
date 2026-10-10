package com.quzzar.vestige.travel;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.definition.SpellCost;
import com.quzzar.vestige.magic.presentation.MagicAdjectives;
import com.quzzar.vestige.magic.runtime.ResourceValuation;
import com.quzzar.vestige.magic.world.NativeMana;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import java.util.*;

/** Trusted travel policy; items retain a route, never prices or a replacement attunement. */
public enum StandingStonePayment {
    EXPERIENCE("experience", null),
    ERUDITE("erudite", "minecraft:lapis_block"),
    MANA("mana", "minecraft:amethyst_block"),
    HUNGER("hunger", "minecraft:moss_block"),
    HEALTH("health", "minecraft:soul_sand");

    public static final ResourceLocation FAMILY = VestigeMainMod.location("standing_stone");
    private static final String VERSION = "vestige_stone_payment_version", ROUTE = "vestige_stone_payment";
    private final String id;
    private final ResourceLocation material;
    StandingStonePayment(String id, String material) { this.id = id; this.material = material == null ? null : ResourceLocation.parse(material); }
    public String id() { return id; }
    public Optional<ResourceLocation> material() { return Optional.ofNullable(material); }
    public List<MagicAdjectives.Adjustment> adjectives() {
        return this == EXPERIENCE ? List.of() : List.of(new MagicAdjectives.Adjustment(VestigeMainMod.location("standing_stone/" + id), 1));
    }
    public static Optional<StandingStonePayment> fromMaterial(ItemStack stack) {
        if (stack.isEmpty()) return Optional.of(EXPERIENCE);
        var item = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return Arrays.stream(values()).filter(route -> item.equals(route.material)).findFirst();
    }
    public void write(CompoundTag tag) { tag.putInt(VERSION, 1); tag.putString(ROUTE, id); }
    public static Optional<StandingStonePayment> read(CompoundTag tag) {
        if (!tag.contains(VERSION) && !tag.contains(ROUTE)) return Optional.of(EXPERIENCE);
        if (!tag.contains(VERSION, Tag.TAG_INT) || tag.getInt(VERSION) != 1 || !tag.contains(ROUTE, Tag.TAG_STRING)) return Optional.empty();
        return Arrays.stream(values()).filter(route -> route.id.equals(tag.getString(ROUTE))).findFirst();
    }
    public Quote quote(double budget) {
        if (!Double.isFinite(budget) || budget <= 0 || budget > Integer.MAX_VALUE) throw new IllegalArgumentException("Invalid travel budget");
        int amount = switch (this) {
            case EXPERIENCE, MANA -> Math.max(1, Math.toIntExact(Math.round(budget)));
            case ERUDITE -> Math.max(1, Math.toIntExact(Math.round(budget * .75)));
            case HEALTH -> ResourceValuation.healthForMana(budget);
            case HUNGER -> ResourceValuation.foodForMana(budget);
        };
        return new Quote(this, amount);
    }
    public record Quote(StandingStonePayment route, int amount) {
        public Quote { Objects.requireNonNull(route); if (amount < 1) throw new IllegalArgumentException("Positive travel cost required"); }
        public SpellCost cost() {
            return switch (route) {
                case EXPERIENCE, ERUDITE -> new SpellCost.Experience(amount);
                case MANA -> new SpellCost.Mana(amount);
                case HUNGER -> new SpellCost.Hunger(amount);
                case HEALTH -> new SpellCost.Health(amount);
            };
        }
        public boolean affordable(Player player) {
            if (player == null || !player.isAlive()) return false;
            if (player.isCreative()) return true;
            return switch (route) {
                case EXPERIENCE, ERUDITE -> PlayerExperience.available(player) >= amount;
                case MANA -> NativeMana.amount(player) >= amount;
                case HUNGER -> player.getFoodData().getFoodLevel() >= amount;
                case HEALTH -> player.getHealth() > amount;
            };
        }
    }
}
