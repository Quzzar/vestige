package com.quzzar.vestige.apparatus;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/** A physical staff is a simple left-click weapon; it has no spell runtime or magical behavior. */
public final class MundaneStaffItem extends Item {
    public MundaneStaffItem(MundaneStaffs.Shaft shaft, Properties properties) { super(properties.attributes(StaffCombat.attributes(shaft))); }

    /** Matches ordinary handheld weapons: successful left-click attacks consume one point of durability. */
    @Override public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return StaffCombat.hurtEnemy(stack, attacker);
    }

    /** A mundane Staff is a simple weapon, so holding right-click raises it to guard. */
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return StaffCombat.guard(player, hand);
    }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return StaffCombat.useAnimation(); }
    @Override public int getUseDuration(ItemStack stack, LivingEntity entity) { return StaffCombat.useDuration(); }
}
