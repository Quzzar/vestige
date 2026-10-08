package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.VestigeMainMod;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * A Staff guard is intentionally distinct from a shield. It reduces a frontal hit after the normal
 * raise delay, but does not create vanilla shield knockback, axe disabling, or shield durability rules.
 */
@EventBusSubscriber(modid = VestigeMainMod.MOD_ID)
public final class StaffGuard {
    private StaffGuard() { }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void mitigate(LivingIncomingDamageEvent event) {
        var defender = event.getEntity();
        if (defender.level().isClientSide() || !StaffCombat.guarding(defender) || !facesHit(defender, event.getSource())) return;

        event.setAmount(event.getAmount() * (1.0F - StaffCombat.guardRatio(defender.getUseItem())));
        wear(defender);
    }

    private static boolean facesHit(LivingEntity defender, DamageSource source) {
        if (source.is(DamageTypeTags.BYPASSES_SHIELD)) return false;
        if (source.getDirectEntity() instanceof AbstractArrow arrow && arrow.getPierceLevel() > 0) return false;
        Vec3 sourcePosition = source.getSourcePosition();
        if (sourcePosition == null) return false;
        Vec3 incoming = sourcePosition.vectorTo(defender.position());
        Vec3 view = defender.calculateViewVector(0.0F, defender.getYHeadRot());
        incoming = new Vec3(incoming.x, 0.0D, incoming.z).normalize();
        return incoming.dot(view) < 0.0D;
    }

    private static void wear(LivingEntity defender) {
        if (defender instanceof Player player && player.hasInfiniteMaterials()) return;
        var staff = defender.getUseItem();
        var slot = defender.getUsedItemHand() == net.minecraft.world.InteractionHand.MAIN_HAND
                ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
        staff.hurtAndBreak(1, defender, slot);
        if (staff.isEmpty()) defender.stopUsingItem();
    }
}
