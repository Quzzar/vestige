package com.quzzar.vestige.equipment.mixin;

import com.quzzar.vestige.equipment.EquipmentMagic;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

/** Keep the eligible hit in the original damage container; vanilla records combined magic/armor reduction once. */
@Mixin(LivingEntity.class)
public abstract class ArmorProtection {
    @ModifyVariable(method = "getDamageAfterArmorAbsorb", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float vestige$protect(float amount, DamageSource source, float original) {
        return EquipmentMagic.protect((LivingEntity) (Object) this, source, amount);
    }
}
