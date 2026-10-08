package com.quzzar.vestige.equipment.mixin;

import com.quzzar.vestige.equipment.EquipmentMagic;
import com.quzzar.vestige.equipment.WayfarerMagic;
import com.quzzar.vestige.magic.world.NativeMana;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerArmorSlot {
    @Inject(method = "setItemSlot", at = @At("HEAD"))
    private void vestige$remove(EquipmentSlot slot, ItemStack stack, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        if (!player.level().isClientSide() && slot == EquipmentSlot.CHEST && player.getItemBySlot(slot) != stack) EquipmentMagic.removed(player);
        if (!player.level().isClientSide() && slot == EquipmentSlot.FEET && player.getItemBySlot(slot) != stack) WayfarerMagic.remove(player);
    }
    @Inject(method = "setItemSlot", at = @At("RETURN"))
    private void vestige$clamp(EquipmentSlot slot, ItemStack stack, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        if (!player.level().isClientSide() && slot == EquipmentSlot.CHEST) NativeMana.reconcile(player);
    }
}
