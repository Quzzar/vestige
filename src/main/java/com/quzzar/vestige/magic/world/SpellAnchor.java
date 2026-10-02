package com.quzzar.vestige.magic.world;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;

/** Temporary field backing with authored hit points, rather than vanilla armor-stand break rules. */
public class SpellAnchor extends ArmorStand {
    public SpellAnchor(EntityType<? extends ArmorStand> type, Level level) { super(type, level); }
    @Override public boolean hurt(DamageSource source, float amount) {
        if (level().isClientSide || isInvulnerableTo(source) || !Float.isFinite(amount) || amount <= 0) return false;
        setHealth(Math.max(0, getHealth() - amount));
        if (getHealth() == 0) discard();
        return true;
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        // Active manifestation ownership is intentionally not restored after unloading.
        discard();
    }
}
