package com.quzzar.vestige.equipment;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.ScrollItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.*;
import java.util.*;

public final class MagicEquipment {
    public static final DeferredRegister<ArmorMaterial> MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, VestigeMainMod.MOD_ID);
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> WARD_MATERIAL = MATERIALS.register("wardweave", () -> material("wardweave", ScrollItems.CALLOUS_THREAD::get));
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> CINDER_MATERIAL = MATERIALS.register("cinderweave", () -> material("cinderweave", ScrollItems.SMOLDERING_THREAD::get));
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> WAYFARER_MATERIAL = MATERIALS.register("wayfarer", () -> new ArmorMaterial(
            Map.of(ArmorItem.Type.BOOTS, 1), 15, SoundEvents.ARMOR_EQUIP_LEATHER, () -> Ingredient.of(ScrollItems.LACED_THREAD.get()),
            List.of(new ArmorMaterial.Layer(VestigeMainMod.location("wayfarer"))), 0, 0));
    public static final DeferredItem<MagicArmorItem> WARDWEAVE = ScrollItems.ITEMS.register("wardweave_robes", () -> new MagicArmorItem(WARD_MATERIAL, VestigeMainMod.location("wardweave")));
    public static final DeferredItem<MagicArmorItem> CINDERWEAVE = ScrollItems.ITEMS.register("cinderweave_robes", () -> new MagicArmorItem(CINDER_MATERIAL, VestigeMainMod.location("cinderweave")));
    public static final DeferredItem<WayfarerBootsItem> WAYFARER = ScrollItems.ITEMS.register("wayfarer_boots", () -> new WayfarerBootsItem(WAYFARER_MATERIAL));
    private MagicEquipment() { }
    private static ArmorMaterial material(String name, java.util.function.Supplier<Item> thread) {
        var asset = VestigeMainMod.location(name);
        return new ArmorMaterial(Map.of(ArmorItem.Type.CHESTPLATE, 2), 15, SoundEvents.ARMOR_EQUIP_LEATHER,
                () -> Ingredient.of(thread.get()), List.of(new ArmorMaterial.Layer(asset, "", true), new ArmorMaterial.Layer(asset, "_overlay", false)), 0, 0);
    }
}
