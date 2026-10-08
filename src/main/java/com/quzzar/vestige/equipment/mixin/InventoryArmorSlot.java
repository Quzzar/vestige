package com.quzzar.vestige.equipment.mixin;

import com.quzzar.vestige.equipment.EquipmentMagic;
import com.quzzar.vestige.equipment.WayfarerMagic;
import com.quzzar.vestige.magic.world.NativeMana;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Inventory.class)
public abstract class InventoryArmorSlot {
    @Shadow @Final public Player player;
    @Inject(method = "setItem", at = @At("HEAD"))
    private void vestige$remove(int slot, ItemStack stack, CallbackInfo ci) {
        if (player != null && !player.level().isClientSide() && slot == 38 && player.getItemBySlot(EquipmentSlot.CHEST) != stack) EquipmentMagic.removed(player);
        if (player != null && !player.level().isClientSide() && slot == 36 && player.getItemBySlot(EquipmentSlot.FEET) != stack) WayfarerMagic.remove(player);
    }
    @Inject(method = "setItem", at = @At("RETURN"))
    private void vestige$clamp(int slot, ItemStack stack, CallbackInfo ci) {
        if (player != null && !player.level().isClientSide() && slot == 38) NativeMana.reconcile(player);
    }
}
