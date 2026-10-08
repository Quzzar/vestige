package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.VestigeMainMod;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import java.util.*;

/** One registry-backed shaft catalogue keeps mundane weapon bodies ready for later staff construction. */
@EventBusSubscriber(modid = VestigeMainMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class MundaneStaffs {
    public enum Shaft {
        // These remain deliberately modest utility weapons.  A Staff is never a sword or a shield;
        // its material only gives it a small physical character before it becomes a magical shaft.
        STICK("stick", Items.STICK, 59, 2.0D, 1.2D, 0.40F),
        BAMBOO("bamboo", Items.BAMBOO, 59, 1.0D, 1.6D, 0.30F),
        BONE("bone", Items.BONE, 131, 3.0D, 1.0D, 0.35F),
        BLAZE_ROD("blaze_rod", Items.BLAZE_ROD, 203, 2.0D, 1.4D, 0.35F),
        BREEZE_ROD("breeze_rod", Items.BREEZE_ROD, 203, 1.0D, 1.7D, 0.30F),
        END_ROD("end_rod", Items.END_ROD, 250, 2.0D, 1.2D, 0.45F),
        LIGHTNING_ROD("lightning_rod", Items.LIGHTNING_ROD, 250, 3.0D, 0.9D, 0.45F);

        private final String id;
        private final Item ingredient;
        private final int durability;
        private final double attackDamage;
        private final double attackSpeed;
        private final float guardRatio;
        Shaft(String id, Item ingredient, int durability, double attackDamage, double attackSpeed, float guardRatio) {
            this.id = id; this.ingredient = ingredient; this.durability = durability;
            this.attackDamage = attackDamage; this.attackSpeed = attackSpeed; this.guardRatio = guardRatio;
        }
        public String id() { return id; }
        public Item ingredient() { return ingredient; }
        public int durability() { return durability; }
        /** Total displayed melee damage, including the player's ordinary one-point base damage. */
        public double attackDamage() { return attackDamage; }
        public double attackSpeed() { return attackSpeed; }
        /** Fraction of an eligible frontal hit reduced after the five-tick raise delay. */
        public float guardRatio() { return guardRatio; }
    }

    private static final Map<Shaft, DeferredItem<MundaneStaffItem>> BY_SHAFT;
    static {
        var entries = new EnumMap<Shaft, DeferredItem<MundaneStaffItem>>(Shaft.class);
        for (var shaft : Shaft.values()) entries.put(shaft, ScrollItems.ITEMS.register(shaft.id() + "_staff",
                () -> new MundaneStaffItem(shaft, new Item.Properties().durability(shaft.durability()))));
        BY_SHAFT = Collections.unmodifiableMap(entries);
    }
    private MundaneStaffs() { }

    /** Forces item registration before the shared Vestige item register attaches to the mod bus. */
    public static void bootstrap() { }

    public static Item item(Shaft shaft) { return BY_SHAFT.get(shaft).get(); }
    public static Optional<Shaft> shaft(ItemStack stack) {
        return Arrays.stream(Shaft.values()).filter(shaft -> stack.is(item(shaft))).findFirst();
    }
    public static List<Item> all() { return Arrays.stream(Shaft.values()).map(MundaneStaffs::item).toList(); }

    @SubscribeEvent public static void creative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) all().forEach(event::accept);
    }
}
