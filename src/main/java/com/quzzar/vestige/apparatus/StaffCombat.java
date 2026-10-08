package com.quzzar.vestige.apparatus;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import java.util.Optional;

/** Shared physical handling for a mundane Staff and an empty magical Staff. */
final class StaffCombat {
    static final int GUARD_DURATION = 72_000;
    static final int GUARD_RAISE_TICKS = 5;
    static final float GUARD_RATIO = 0.40F;

    private StaffCombat() { }

    /** The generic magical Staff keeps the original wooden-pickaxe physical profile. */
    static ItemAttributeModifiers attributes() { return attributes(2.0D, 1.2D); }

    /** Mundane shafts vary within a deliberately small band rather than replacing normal weapons. */
    static ItemAttributeModifiers attributes(MundaneStaffs.Shaft shaft) {
        return attributes(shaft.attackDamage(), shaft.attackSpeed());
    }

    private static ItemAttributeModifiers attributes(double totalDamage, double attackSpeed) {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID,
                        totalDamage - 1.0D, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID,
                        attackSpeed - 4.0D, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .build();
    }

    static float guardRatio(ItemStack stack) {
        return MundaneStaffs.shaft(stack).map(MundaneStaffs.Shaft::guardRatio).orElse(GUARD_RATIO);
    }

    /** A strike uses ordinary tool wear, separately from the rarity-based wear paid by a successful spell cast. */
    static boolean hurtEnemy(ItemStack stack, LivingEntity attacker) {
        stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
        return true;
    }

    /** Raises the staff into its own held guard stance; the partial reduction is applied by {@link StaffGuard}. */
    static InteractionResultHolder<ItemStack> guard(Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    /**
     * A Staff guard is a planted, outstretched brace rather than the shield's close-to-the-face pose.
     * The partial frontal reduction still comes from {@link StaffGuard}; this only selects the player animation.
     */
    static UseAnim useAnimation() { return UseAnim.SPEAR; }
    static int useDuration() { return GUARD_DURATION; }

    /** Only a raised mundane staff, or a magical staff without an active scroll, can reduce damage. */
    static boolean guarding(LivingEntity entity) {
        if (!entity.isUsingItem() || entity.getTicksUsingItem() < GUARD_RAISE_TICKS) return false;
        var used = entity.getUseItem();
        if (used.getItem() instanceof MundaneStaffItem) return true;
        return used.getItem() instanceof SpellStaffItem
                && StaffData.binding(used).map(StaffData.Binding::active).orElse(Optional.empty()).isEmpty();
    }
}
