package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.world.NativeMana;
import com.quzzar.vestige.travel.PlayerExperience;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import net.neoforged.neoforge.gametest.*;
import java.util.*;

@GameTestHolder(VestigeMainMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class HomeboundEyeWorldTest {
    private static final BlockPos CENTER = new BlockPos(4, 1, 4);
    private static final String KEY = "a".repeat(64);
    private static void site(ServerLevel level, BlockPos pos) {
        for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) {
            level.setBlock(pos.offset(x, -1, z), Blocks.STONE.defaultBlockState(), 3);
            for (int y = 0; y < 3; y++) level.setBlock(pos.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
        }
        level.setBlock(pos, ApparatusBlocks.SPELLSTONE.get().defaultBlockState(), 3);
    }
    private static ItemStack shard() {
        var geometry = new LeylineShaping.Geometry(8, LeylineShaping.Shape.CROSS, 2, 0, LeylineShaping.Shape.DIAGONAL, 3, 0);
        var ingredients = AttunementShardItem.ingredients(); var nodes = new ArrayList<RitualInputs.Node>();
        for (int i = 0; i < 8; i++) nodes.add(new RitualInputs.Node(i, geometry.offset(i),
                i < ingredients.size() ? new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(ingredients.get(i))) : ItemStack.EMPTY, ItemStack.EMPTY));
        return AttunementShardItem.create(new RitualInputs(geometry, nodes));
    }
    private static RitualCrafting.Layout ritual(GameTestHelper h) {
        h.setBlock(CENTER, ApparatusBlocks.SPELLSTONE.get());
        var geometry = new LeylineShaping.Geometry(4, LeylineShaping.Shape.CROSS, 2, 0, LeylineShaping.Shape.CROSS, 1, 0);
        for (int i : List.of(0, 2, 4, 6)) h.setBlock(CENTER.offset(geometry.offset(i)), ApparatusBlocks.PLINTH.get());
        var layout = RitualCrafting.layout((OfferingBlockEntity) h.getBlockEntity(CENTER));
        var inputs = List.of(shard(), new ItemStack(Items.SPIDER_EYE), new ItemStack(Items.ENDER_PEARL), new ItemStack(Items.FLINT));
        for (int i = 0; i < 4; i++) layout.stands().get(i * 2).insert(inputs.get(i));
        return layout;
    }
    @GameTest(template = "empty_9x3x9", batch = "homebound_crafting", timeoutTicks = 80)
    public static void recipeCopiesKeyAndCraftingOriginAndUsesOnlyTheSpiderEyesSocket(GameTestHelper h) {
        var layout = ritual(h); var key = AttunementShardItem.signature(layout.items().get(0)).orElseThrow().key();
        h.assertTrue(layout.stands().get(0).installMaterial(new ItemStack(Items.SOUL_SAND)), "Could not install other socket");
        h.assertTrue(layout.stands().get(2).installMaterial(new ItemStack(Items.AMETHYST_BLOCK)), "Could not install selector");
        h.assertTrue(RitualCrafting.activate(h.makeMockPlayer(GameType.SURVIVAL), layout.center()) == RitualCrafting.Outcome.CRAFTING, "Device recipe rejected");
        h.runAfterDelay(45, () -> {
            var result = RitualTestOutput.stack(layout.center()); var binding = HomeboundEyeItem.binding(result).orElseThrow();
            h.assertTrue(binding.key().equals(key) && binding.origin().equals(layout.center().getBlockPos())
                    && binding.dimension().equals(h.getLevel().dimension()) && binding.payment() == HomeboundEyeItem.Payment.MANA, "Craft lost key/origin/local selector");
            h.assertTrue(result.getMaxDamage() == 30 && result.getDamageValue() == 0 && layout.items().stream().allMatch(ItemStack::isEmpty), "Craft did not consume exactly once");
            h.assertTrue(layout.stands().get(2).materialItem().is(Items.AMETHYST_BLOCK), "Craft consumed the embedded material"); h.succeed();
        });
    }
    @GameTest(template = "empty_9x3x9", batch = "homebound_cancel", timeoutTicks = 80)
    public static void changedSelectorCancelsTheQueuedCraftWithoutConsumption(GameTestHelper h) {
        var layout = ritual(h); layout.stands().get(2).installMaterial(new ItemStack(Items.LAPIS_BLOCK));
        h.assertTrue(RitualCrafting.activate(h.makeMockPlayer(GameType.SURVIVAL), layout.center()) == RitualCrafting.Outcome.CRAFTING, "XP recipe rejected");
        h.runAfterDelay(10, () -> {
            var block = layout.stands().get(2); block.unlock(); block.removeMaterial(); block.installMaterial(new ItemStack(Items.MOSS_BLOCK));
        });
        h.runAfterDelay(50, () -> {
            h.assertTrue(RitualTestOutput.stack(layout.center()).isEmpty() && layout.items().stream().filter(i -> !i.isEmpty()).count() == 4, "Changed selector partially committed"); h.succeed();
        });
    }
    @GameTest(template = "empty_9x3x9", batch = "homebound_payments")
    public static void allRoutesChargeTheirExactResourceAndWearAndTheDefaultBreaksAfterFiveReturns(GameTestHelper h) {
        var origin = h.absolutePos(CENTER); site(h.getLevel(), origin);
        try (var player = com.quzzar.vestige.gametest.SurvivalTestPlayer.create(h)) {
        for (var payment : HomeboundEyeItem.Payment.values()) {
            player.setHealth(20); player.getFoodData().setFoodLevel(20); NativeMana.set(player, 100);
            player.setExperienceLevels(5); player.setExperiencePoints(0); player.totalExperience = 1;
            long xp = PlayerExperience.available(player); player.setPos(Vec3.atBottomCenterOf(origin.east(3)));
            var item = HomeboundEyeItem.bound(KEY, h.getLevel().dimension(), origin, payment);
            h.assertTrue(HomeboundEyeItem.returnHome(player, item), "Return failed for " + payment);
            h.assertTrue(player.position().distanceToSqr(Vec3.atCenterOf(origin)) < 10 && item.getDamageValue() == payment.wear, "Wrong arrival/wear");
            h.assertTrue(player.getHealth() == (payment == HomeboundEyeItem.Payment.HEALTH ? 14 : 20), "Wrong health debit");
            h.assertTrue(player.getFoodData().getFoodLevel() == (payment == HomeboundEyeItem.Payment.HUNGER ? 14 : 20), "Wrong hunger debit");
            h.assertTrue(NativeMana.amount(player) == (payment == HomeboundEyeItem.Payment.MANA ? 70 : 100), "Wrong mana debit");
            h.assertTrue(PlayerExperience.available(player) == xp - (payment == HomeboundEyeItem.Payment.EXPERIENCE ? 25 : 0), "XP relied on stale total counter");
            h.assertTrue(HomeboundEyeItem.binding(item).orElseThrow().key().equals(KEY), "Use changed attunement");
        }
        var ordinary = HomeboundEyeItem.bound(KEY, h.getLevel().dimension(), origin, HomeboundEyeItem.Payment.DURABILITY);
        for (int i = 0; i < 4; i++) h.assertTrue(HomeboundEyeItem.returnHome(player, ordinary) && !ordinary.isEmpty()
                && ordinary.getDamageValue() == (i + 1) * 6, "Default broke before the fifth return");
        h.assertTrue(HomeboundEyeItem.returnHome(player, ordinary) && ordinary.isEmpty(), "Default did not break on its fifth return");
        h.succeed();
        }
    }
    @GameTest(template = "empty_9x3x9", batch = "homebound_denial")
    public static void missingOriginAndInsufficientPaymentRejectButBlockedOriginUsesFallback(GameTestHelper h) {
        var origin = h.absolutePos(CENTER); site(h.getLevel(), origin);
        try (var player = com.quzzar.vestige.gametest.SurvivalTestPlayer.create(h)) { player.setPos(Vec3.atBottomCenterOf(origin.east(3)));
        var start = player.position(); NativeMana.set(player, 29);
        var mana = HomeboundEyeItem.bound(KEY, h.getLevel().dimension(), origin, HomeboundEyeItem.Payment.MANA);
        h.assertTrue(!HomeboundEyeItem.returnHome(player, mana) && NativeMana.amount(player) == 29 && mana.getDamageValue() == 0 && player.position().equals(start), "Unaffordable return spent something");
        player.setHealth(6); var health = HomeboundEyeItem.bound(KEY, h.getLevel().dimension(), origin, HomeboundEyeItem.Payment.HEALTH);
        h.assertTrue(!HomeboundEyeItem.returnHome(player, health) && player.getHealth() == 6 && health.getDamageValue() == 0, "Health route killed player");
        NativeMana.set(player, 100); h.getLevel().setBlock(origin, Blocks.AIR.defaultBlockState(), 3);
        h.assertTrue(!HomeboundEyeItem.returnHome(player, mana) && NativeMana.amount(player) == 100 && mana.getDamageValue() == 0, "Missing Spellstone charged");
        site(h.getLevel(), origin);
        for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) for (int y = 0; y < 3; y++)
            if (x != 0 || z != 0) h.getLevel().setBlock(origin.offset(x, y, z), Blocks.STONE.defaultBlockState(), 3);
        h.assertTrue(HomeboundEyeItem.returnHome(player, mana) && NativeMana.amount(player) == 70 && mana.getDamageValue() == 2
                && !h.getLevel().noCollision(player, player.getBoundingBox()), "Blocked arrival did not use the occupied fallback"); h.succeed();
        }
    }
    @GameTest(template = "empty_9x3x9", batch = "homebound_xp_cancel")
    public static void canceledOrModifiedXpDebitCannotGrantTravel(GameTestHelper h) {
        var origin = h.absolutePos(CENTER); site(h.getLevel(), origin);
        try (var player = com.quzzar.vestige.gametest.SurvivalTestPlayer.create(h)) { player.setPos(Vec3.atBottomCenterOf(origin.east(3)));
        player.setExperienceLevels(5); player.setExperiencePoints(0); var before = PlayerExperience.Snapshot.of(player); var start = player.position();
        var item = HomeboundEyeItem.bound(KEY, h.getLevel().dimension(), origin, HomeboundEyeItem.Payment.EXPERIENCE);
        java.util.function.Consumer<PlayerXpEvent.XpChange> cancel = event -> { if (event.getEntity() == player) event.setCanceled(true); };
        NeoForge.EVENT_BUS.addListener(cancel);
        try { h.assertTrue(!HomeboundEyeItem.returnHome(player, item), "Canceled XP granted travel"); }
        finally { NeoForge.EVENT_BUS.unregister(cancel); }
        java.util.function.Consumer<PlayerXpEvent.XpChange> modify = event -> { if (event.getEntity() == player) event.setAmount(0); };
        NeoForge.EVENT_BUS.addListener(modify);
        try { h.assertTrue(!HomeboundEyeItem.returnHome(player, item), "Modified XP debit granted free travel"); }
        finally { NeoForge.EVENT_BUS.unregister(modify); }
        java.util.function.Consumer<PlayerXpEvent.LevelChange> levelCancel = event -> { if (event.getEntity() == player) event.setCanceled(true); };
        NeoForge.EVENT_BUS.addListener(levelCancel);
        try { h.assertTrue(!HomeboundEyeItem.returnHome(player, item), "Canceled level debit granted travel"); }
        finally { NeoForge.EVENT_BUS.unregister(levelCancel); }
        h.assertTrue(before.equals(PlayerExperience.Snapshot.of(player)) && player.position().equals(start) && item.getDamageValue() == 0, "Canceled XP mutated payment or travel");
        h.succeed();
        }
    }
    @GameTest(template = "empty_9x3x9", batch = "homebound_dimension")
    public static void dimensionReturnWorksAndCanceledTransferRefundsManaAndWear(GameTestHelper h) {
        var targetLevel = h.getLevel().getServer().getLevel(Level.END);
        var origin = new BlockPos(h.absolutePos(CENTER).getX() + 250000, 80, h.absolutePos(CENTER).getZ()); site(targetLevel, origin);
        try (var player = com.quzzar.vestige.gametest.SurvivalTestPlayer.create(h)) { NativeMana.set(player, 100);
        var start = player.position(); var item = HomeboundEyeItem.bound(KEY, Level.END, origin, HomeboundEyeItem.Payment.MANA);
        java.util.function.Consumer<EntityTravelToDimensionEvent> cancel = event -> { if (event.getEntity() == player) event.setCanceled(true); };
        NeoForge.EVENT_BUS.addListener(cancel);
        try { h.assertTrue(!HomeboundEyeItem.returnHome(player, item), "Canceled dimension transfer succeeded"); }
        finally { NeoForge.EVENT_BUS.unregister(cancel); }
        h.assertTrue(player.level() == h.getLevel() && player.position().equals(start) && NativeMana.amount(player) == 100 && item.getDamageValue() == 0, "Canceled transfer retained payment");
        h.assertTrue(HomeboundEyeItem.returnHome(player, item) && player.level() == targetLevel && NativeMana.amount(player) == 70 && item.getDamageValue() == 2, "Valid cross-dimension origin failed");
        targetLevel.setBlock(origin, Blocks.AIR.defaultBlockState(), 3); h.succeed();
        }
    }
}
