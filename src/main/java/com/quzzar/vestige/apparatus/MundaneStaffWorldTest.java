package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.VestigeMainMod;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Verifies that every physical Staff behaves as a left-click weapon and takes ordinary combat wear. */
@GameTestHolder(VestigeMainMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MundaneStaffWorldTest {
    @GameTest(template="empty_9x3x9",batch="mundane_staffs")
    public static void everyStaffDamagesAndWearsInMelee(GameTestHelper h) {
        for (var shaft : MundaneStaffs.Shaft.values()) {
            var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
            var target = h.spawnWithNoFreeWill(EntityType.COW, new BlockPos(4, 1, 4));
            player.teleportTo(h.absolutePos(new BlockPos(4, 1, 3)).getBottomCenter().x, h.absolutePos(new BlockPos(4, 1, 3)).getY(), h.absolutePos(new BlockPos(4, 1, 3)).getBottomCenter().z);
            var staff = new ItemStack(MundaneStaffs.item(shaft));
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, staff);
            var health = target.getHealth(); player.attack(target);
            h.assertTrue(target.getHealth() < health, shaft + " Staff did not damage a nearby target on attack");
            h.assertTrue(staff.getDamageValue() == 1, shaft + " Staff did not lose one durability on attack");
        }
        h.succeed();
    }

    @GameTest(template="empty_9x3x9",batch="mundane_staffs")
    public static void mundaneAndUnboundMagicalStaffsGuard(GameTestHelper h) {
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        var mundane = new ItemStack(MundaneStaffs.item(MundaneStaffs.Shaft.STICK));
        player.setItemInHand(InteractionHand.MAIN_HAND, mundane);
        var mundaneUse = mundane.getItem().use(h.getLevel(), player, InteractionHand.MAIN_HAND);
        h.assertTrue(mundaneUse.getResult().consumesAction() && player.isUsingItem() && player.getUseItem().is(mundane.getItem()),
                "Mundane Staff did not enter held guard on right-click");
        h.assertTrue(mundane.getItem().getUseAnimation(mundane) == UseAnim.SPEAR
                        && !mundane.getItem().canPerformAction(mundane, ItemAbilities.SHIELD_BLOCK),
                "Mundane Staff incorrectly exposes full vanilla shield behavior");

        player.stopUsingItem();
        var magical = StaffData.create(ResourceLocation.fromNamespaceAndPath("vestige", "fire"));
        player.setItemInHand(InteractionHand.MAIN_HAND, magical);
        var magicalUse = magical.getItem().use(h.getLevel(), player, InteractionHand.MAIN_HAND);
        h.assertTrue(magicalUse.getResult().consumesAction() && player.isUsingItem() && player.getUseItem().is(magical.getItem()),
                "Staff with no active scroll did not enter held guard on right-click");
        h.assertTrue(magical.getItem().getUseAnimation(magical) == UseAnim.SPEAR
                        && !magical.getItem().canPerformAction(magical, ItemAbilities.SHIELD_BLOCK),
                "Unbound magical Staff lost its brace or exposed full vanilla shield behavior");
        h.succeed();
    }

    @GameTest(template="empty_9x3x9",batch="mundane_staffs",timeoutTicks=40)
    public static void staffGuardMitigatesFrontHitsWithoutShieldSideEffects(GameTestHelper h) {
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        var defender = h.absolutePos(new BlockPos(4, 1, 4)).getBottomCenter();
        player.teleportTo(defender.x, defender.y, defender.z);
        player.setYRot(0); player.setYHeadRot(0);
        var staff = new ItemStack(MundaneStaffs.item(MundaneStaffs.Shaft.STICK));
        player.setItemInHand(InteractionHand.MAIN_HAND, staff);
        staff.getItem().use(h.getLevel(), player, InteractionHand.MAIN_HAND);
        var attacker = h.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(4, 1, 6));
        // GameTest mock players are not level-ticked, so advance the held-use clock explicitly.
        for (int tick = 0; tick < StaffCombat.GUARD_RAISE_TICKS; tick++) player.tick();
        h.assertTrue(player.getTicksUsingItem() == StaffCombat.GUARD_RAISE_TICKS, "Staff guard did not finish its raise delay");
        var health = player.getHealth();
        var attackerMotion = attacker.getDeltaMovement();
        player.hurt(player.damageSources().mobAttack(attacker), 10.0F);
        h.assertTrue(Math.abs(player.getHealth() - (health - 6.0F)) < 0.001F,
                "Raised Staff did not reduce a frontal ten-damage hit to six damage");
        h.assertTrue(staff.getDamageValue() == 1, "Guarded hit did not cost exactly one Staff durability");
        h.assertTrue(attacker.getDeltaMovement().distanceToSqr(attackerMotion) < 0.0001D,
                "Staff guard applied vanilla shield attacker knockback");
        h.succeed();
    }

    @GameTest(template="empty_9x3x9",batch="mundane_staffs")
    public static void magicalStaffsRemainPhysicalWeapons(GameTestHelper h) {
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        var target = h.spawnWithNoFreeWill(EntityType.COW, new BlockPos(4, 1, 4));
        player.teleportTo(h.absolutePos(new BlockPos(4, 1, 3)).getBottomCenter().x, h.absolutePos(new BlockPos(4, 1, 3)).getY(), h.absolutePos(new BlockPos(4, 1, 3)).getBottomCenter().z);
        var staff = StaffData.create(ResourceLocation.fromNamespaceAndPath("vestige", "fire"));
        player.setItemInHand(InteractionHand.MAIN_HAND, staff);
        var health = target.getHealth();
        player.attack(target);
        h.assertTrue(target.getHealth() < health && staff.getDamageValue() == 1,
                "Magical Staff did not retain mundane melee damage and ordinary combat wear");
        h.succeed();
    }
}
