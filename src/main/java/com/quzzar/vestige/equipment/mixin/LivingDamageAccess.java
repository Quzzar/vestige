package com.quzzar.vestige.equipment.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import java.util.Stack;

/** Exact damage-sequence identity prevents nested hits from sharing an armor-wear charge. */
@Mixin(LivingEntity.class)
public interface LivingDamageAccess {
    @Accessor("damageContainers") Stack<DamageContainer> vestige$damageContainers();
    @Accessor("lastHurt") float vestige$lastHurt();
}
