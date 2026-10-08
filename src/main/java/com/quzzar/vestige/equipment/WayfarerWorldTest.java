package com.quzzar.vestige.equipment;

import com.mojang.authlib.GameProfile;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.runtime.SpellRuntime;
import com.quzzar.vestige.magic.runtime.SpellEvent;
import com.quzzar.vestige.magic.world.*;
import com.quzzar.vestige.apparatus.*;
import com.quzzar.vestige.magic.definition.*;
import com.quzzar.vestige.magic.effect.SpellEffects;
import com.quzzar.vestige.magic.expression.SpellValue;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.gametest.*;
import java.util.*;

@GameTestHolder(VestigeMainMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class WayfarerWorldTest {
    private static final class Wearer extends Player {
        Wearer(GameTestHelper h) { super(h.getLevel(), h.absolutePos(new BlockPos(1,1,1)), 0, new GameProfile(UUID.randomUUID(), "quiet-wayfarer")); }
        public boolean isSpectator() { return false; }
        public boolean isCreative() { return false; }
        public void actualJump() { setOnGround(true); jumpFromGround(); }
        public void updateFluids() { updateInWaterStateAndDoFluidPushing(); }
        public void displayClientMessage(net.minecraft.network.chat.Component text, boolean overlay) { throw new AssertionError("Boots emitted gameplay text"); }
        public void sendSystemMessage(net.minecraft.network.chat.Component text) { throw new AssertionError("Boots emitted gameplay text"); }
    }
    private static Wearer wearer(GameTestHelper h, Set<WayfarerImbuements.Choice> choices) {
        var p = new Wearer(h); NativeMagic.session(h.getLevel().getServer()).world().registerActor(p);
        p.setItemSlot(EquipmentSlot.FEET, WayfarerImbuements.create(choices)); NativeMana.set(p, 100); p.setSprinting(true); return p;
    }
    @GameTest(template = "empty_3x3x3", batch = "wayfarer") public static void everyVariantPaysOnceAndUsesTheRealGroundJump(GameTestHelper h) {
        for (int mask = 0; mask < 16; mask++) {
            var choices = WayfarerDisplays.choices(mask); var p = wearer(h, choices); var variant = new WayfarerImbuements.Variant(choices);
            double before = p.getAttributeValue(Attributes.MOVEMENT_SPEED); p.actualJump();
            h.assertTrue(WayfarerMagic.active(p), "Real jump failed to activate variant " + mask);
            double mana = variant.mana(NativeMagic.abilities().abilities().get(WayfarerImbuements.ABILITY));
            h.assertTrue(Math.abs(NativeMana.amount(p) - (100 - mana)) < 1e-9, "Wrong shared composed payment " + mask);
            double expected = choices.contains(WayfarerImbuements.Choice.SWIFT) ? 1.3 : 1.2;
            h.assertTrue(Math.abs(p.getAttributeValue(Attributes.MOVEMENT_SPEED) / before - expected) < 1e-6, "Movement scaling was duplicated " + mask);
            h.assertTrue(p.getItemBySlot(EquipmentSlot.FEET).getDamageValue() == 0, "Activation charged wear");
            p.actualJump(); h.assertTrue(Math.abs(NativeMana.amount(p) - (100 - mana)) < 1e-9 && WayfarerMagic.active(p), "Blocked bunny hop changed payment or revoked the existing burst");
            var recovery = NativeMagic.session(p.getServer()).runtime().recovery(p.getUUID(), WayfarerImbuements.ABILITY).orElseThrow();
            h.assertTrue(recovery.totalTicks() == (choices.contains(WayfarerImbuements.Choice.QUICKENED) ? 240 : 300), "Recovery used wrong source price");
            WayfarerMagic.remove(p);
        }
        h.succeed();
    }
    @GameTest(template = "empty_3x3x3", batch = "wayfarer") public static void oneActualLandingPreventsDamageAndStillWearsBoots(GameTestHelper h) {
        var p = wearer(h, Set.of()); p.actualJump(); var boots = p.getItemBySlot(EquipmentSlot.FEET);
        p.causeFallDamage(7, 1, p.damageSources().fall());
        h.assertTrue(p.getHealth() == 20 && boots.getDamageValue() == 1, "Protected real fall did not prevent four HP/wear exactly once");
        p.invulnerableTime = 0; p.causeFallDamage(7, 1, p.damageSources().fall());
        h.assertTrue(p.getHealth() == 16 && boots.getDamageValue() == 1, "A second landing reused protection or invented fall armor wear");
        WayfarerMagic.remove(p); h.succeed();
    }
    @GameTest(template = "empty_3x3x3", batch = "wayfarer") public static void harmlessLandingSpendsProtectionAndFailuresSpendNothing(GameTestHelper h) {
        var p = wearer(h, Set.of()); NativeMana.set(p, 4); p.actualJump();
        h.assertTrue(!WayfarerMagic.active(p) && NativeMana.amount(p) == 4 && p.getItemBySlot(EquipmentSlot.FEET).getDamageValue() == 0, "Failed payment changed resources/source");
        NativeMana.set(p, 100); p.getAbilities().flying = true; p.actualJump(); h.assertTrue(!WayfarerMagic.active(p), "Flying jump activated boots"); p.getAbilities().flying = false;
        var mount = h.spawn(net.minecraft.world.entity.EntityType.PIG, new BlockPos(1,1,1)); p.startRiding(mount, true); p.actualJump();
        h.assertTrue(!WayfarerMagic.active(p), "Mounted jump activated boots"); p.stopRiding(); mount.discard();
        h.setBlock(new BlockPos(1,1,1), net.minecraft.world.level.block.Blocks.WATER); p.setPos(net.minecraft.world.phys.Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(1,1,1)))); p.updateFluids();
        h.assertTrue(p.isInWater(), "Native fluid fixture did not enter water"); p.actualJump(); h.assertTrue(!WayfarerMagic.active(p), "Swimming jump activated boots");
        h.setBlock(new BlockPos(1,1,1), net.minecraft.world.level.block.Blocks.AIR); p.updateFluids();
        h.assertTrue(NativeMana.amount(p) == 100 && p.getItemBySlot(EquipmentSlot.FEET).getDamageValue() == 0, "Ineligible jump spent resources");
        p.setSprinting(false); p.actualJump(); h.assertTrue(!WayfarerMagic.active(p), "Ordinary jump activated boots");
        p.setSprinting(true); p.actualJump(); p.causeFallDamage(1, 1, p.damageSources().fall());
        p.causeFallDamage(7, 1, p.damageSources().fall()); h.assertTrue(p.getHealth() == 16, "Harmless first landing banked protection");
        WayfarerMagic.remove(p); h.succeed();
    }
    @GameTest(template = "empty_3x3x3", batch = "wayfarer") public static void removalAndVariantSwappingPreserveActorRecovery(GameTestHelper h) {
        var p = wearer(h, Set.of(WayfarerImbuements.Choice.QUICKENED)); p.actualJump();
        var original = p.getItemBySlot(EquipmentSlot.FEET);
        p.getInventory().setItem(36, ItemStack.EMPTY); p.getInventory().setItem(36, original);
        h.assertTrue(!WayfarerMagic.active(p), "Same-tick remove/reinsert retained its source benefits");
        p.setItemSlot(EquipmentSlot.FEET, new ItemStack(MagicEquipment.WAYFARER.get()));
        var result = WayfarerMagic.activate(p);
        h.assertTrue(result != null && result.status() == SpellRuntime.Status.COOLDOWN && NativeMana.amount(p) == 94, "Variant swap bypassed actor recovery");
        h.assertTrue(!WayfarerMagic.active(p), "Removed exact source retained landing protection");
        WayfarerMagic.remove(p); h.succeed();
    }
    @GameTest(template = "empty_3x3x3", batch = "wayfarer") public static void saveRepairAndEnchantmentPolicyPreserveEveryVariant(GameTestHelper h) {
        var p = wearer(h, Set.of()); var enchants = h.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var protection = enchants.getOrThrow(Enchantments.PROTECTION);
        var menu = new AnvilMenu(1, p.getInventory(), ContainerLevelAccess.create(h.getLevel(), h.absolutePos(new BlockPos(1,1,1))));
        for (int mask = 0; mask < 16; mask++) {
            var original = WayfarerImbuements.create(WayfarerDisplays.choices(mask)); original.setDamageValue(40); original.enchant(protection, 1);
            var restored = ItemStack.parse(h.getLevel().registryAccess(), original.save(h.getLevel().registryAccess())).orElseThrow();
            h.assertTrue(ItemStack.matches(original, restored), "Full item persistence lost variant " + mask);
            menu.getSlot(0).set(restored); menu.getSlot(1).set(new ItemStack(ScrollItems.LACED_THREAD.get())); menu.createResult();
            var result = menu.getSlot(2).getItem();
            h.assertTrue(!result.isEmpty() && result.getDamageValue() == 40 - original.getMaxDamage()/4
                    && result.getHoverName().equals(original.getHoverName()) && result.getEnchantmentLevel(protection) == 1, "Native anvil repair changed variant " + mask);
        }
        for (var key : List.of(Enchantments.UNBREAKING, Enchantments.MENDING)) {
            var enchant = enchants.getOrThrow(key); var boots = WayfarerImbuements.create(Set.of());
            h.assertTrue(!boots.supportsEnchantment(enchant) && !boots.isPrimaryItemFor(enchant), "Excluded enchantment is eligible");
            boots.enchant(enchant, 3); h.assertTrue(boots.getEnchantmentLevel(enchant) == 0, "Forced excluded enchantment is active");
            var book = new ItemStack(Items.ENCHANTED_BOOK); book.enchant(enchant, 1);
            menu.getSlot(0).set(WayfarerImbuements.create(Set.of())); menu.getSlot(1).set(book); menu.createResult();
            h.assertTrue(menu.getSlot(2).getItem().isEmpty(), "Native anvil accepted excluded enchantment");
        }
        h.succeed();
    }
    @GameTest(template = "empty_3x3x3", batch = "wayfarer") public static void ordinaryWearAndProtectedFinalWearBreakNormally(GameTestHelper h) {
        var p = wearer(h, Set.of()); var boots = p.getItemBySlot(EquipmentSlot.FEET);
        p.hurt(p.damageSources().cactus(), 8); h.assertTrue(boots.getDamageValue() == 2, "Ordinary armor wear is missing");
        p.setHealth(20); p.invulnerableTime = 0; boots.setDamageValue(64); p.actualJump(); p.causeFallDamage(7, 1, p.damageSources().fall());
        h.assertTrue(p.getHealth() == 20 && boots.isEmpty() && !WayfarerMagic.active(p), "Protected final wear failed to break/revoke boots");
        h.assertTrue(NativeMana.maximum(p) == 100, "Boots invented a capacity bonus"); h.succeed();
    }
    private static void boost(Player p, double time, double motion, double amplify, int ticks) {
        var ability = new ItemAbilityDefinition(VestigeMainMod.location("test/wayfarer_boost"), TraitProfile.empty(), Map.of(), List.of(),
                List.of(new SpellTrigger(VestigeMainMod.location("test/use"), SpellTriggerTypes.INTERACT, List.of())),
                List.of(new SpellEffects.GrantTraits(VestigeMainMod.location("test/wayfarer_traits"), List.of(
                        new TraitModifier(VestigeMainMod.location("time"), TraitModifier.Operation.MULTIPLY, time),
                        new TraitModifier(VestigeMainMod.location("motion"), TraitModifier.Operation.MULTIPLY, motion),
                        new TraitModifier(VestigeMainMod.location("amplify"), TraitModifier.Operation.MULTIPLY, amplify)),
                        new SpellValue.Constant(ticks), TargetSpec.self())), ItemAbilityDefinition.Activation.REACTIVE);
        NativeMagic.session(p.getServer()).runtime().activate(ability, SpellEvent.of(SpellTriggerTypes.INTERACT, p.getUUID(), null), List.of(), com.quzzar.vestige.magic.runtime.CastReservation.NONE);
    }
    @GameTest(template = "empty_3x3x3", batch = "wayfarer", timeoutTicks = 140) public static void wearerTraitsSnapshotAndUnusedLandingExpires(GameTestHelper h) {
        var p = wearer(h, Set.of()); boost(p, 2, 3, 2, 2); double before = p.getAttributeValue(Attributes.MOVEMENT_SPEED); p.actualJump();
        h.assertTrue(Math.abs(p.getAttributeValue(Attributes.MOVEMENT_SPEED)/before - 1.6) < 1e-6, "Wearer Motion was capped or applied twice");
        h.runAfterDelay(65, () -> { h.assertTrue(WayfarerMagic.active(p), "Burst lost snapshotted Time"); p.causeFallDamage(11, 1, p.damageSources().fall()); h.assertTrue(p.getHealth() == 20, "Landing lost snapshotted Amplify"); });
        h.runAfterDelay(122, () -> { h.assertTrue(!WayfarerMagic.active(p) && Math.abs(p.getAttributeValue(Attributes.MOVEMENT_SPEED)/before - 1) < 1e-6, "Expired speed remained"); h.succeed(); });
    }
    @GameTest(template = "empty_3x3x3", batch = "wayfarer", timeoutTicks = 330) public static void recoveryExpiryReplacesLongBurstWithoutStacking(GameTestHelper h) {
        var p = wearer(h, Set.of()); boost(p, 10, 1, 1, 1000); double before = p.getAttributeValue(Attributes.MOVEMENT_SPEED); p.actualJump();
        h.runAfterDelay(302, () -> { p.actualJump(); h.assertTrue(WayfarerMagic.active(p) && NativeMana.amount(p) == 90
                && Math.abs(p.getAttributeValue(Attributes.MOVEMENT_SPEED)/before - 1.2) < 1e-6, "Long burst stacked or failed to replace after recovery"); WayfarerMagic.remove(p); h.succeed(); });
    }
    private static RitualCrafting.Layout ritual(GameTestHelper h, int rotation) {
        var center = new BlockPos(4,1,4); var geometry = new LeylineShaping.Geometry(8, LeylineShaping.Shape.CROSS, 2, 0, LeylineShaping.Shape.DIAGONAL, 3, 0);
        h.setBlock(center, ApparatusBlocks.SPELLSTONE.get()); for (int i=0;i<8;i++) h.setBlock(center.offset(geometry.offset(i)), ApparatusBlocks.PLINTH.get());
        var layout = RitualCrafting.layout((OfferingBlockEntity) h.getBlockEntity(center));
        var display = WayfarerDisplays.entries().getLast();
        for (var offering : display.offerings()) layout.stands().get((offering.seat()+rotation)%8).insert(new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(offering.ingredient().items().getFirst())));
        for (var material : display.imbuements()) layout.stands().get((material.seat()+rotation)%8).installMaterial(material.stack());
        return layout;
    }
    @GameTest(template = "empty_9x3x9", batch = "wayfarer_ritual", timeoutTicks = 90) public static void rotatedEightSeatRitualCommitsOnceAndKeepsSockets(GameTestHelper h) {
        var layout = ritual(h, 2); var inputs = RitualInputs.capture(layout);
        h.assertTrue(RitualCrafting.activate(new Wearer(h), layout.center()) == RitualCrafting.Outcome.CRAFTING, "Native boot ritual rejected");
        h.assertTrue(RitualCrafting.activate(new Wearer(h), layout.center()) == RitualCrafting.Outcome.BUSY, "Duplicate ritual accepted");
        h.runAfterDelay(65, () -> { var output = RitualTestOutput.stack(layout.center());
            h.assertTrue(ItemStack.matches(output, WayfarerImbuements.create(EnumSet.allOf(WayfarerImbuements.Choice.class)))
                    && layout.items().stream().allMatch(ItemStack::isEmpty) && layout.center().displayedItem().isEmpty(), "Ritual lost variant or consumed wrong inputs");
            h.assertTrue(inputs.nodes().stream().allMatch(n -> ItemStack.matches(n.material(), layout.stands().get(n.seat()).materialItem()))
                    && layout.blocks().stream().noneMatch(OfferingBlockEntity::busy), "Sockets consumed or locks retained"); h.succeed(); });
    }
    @GameTest(template = "empty_9x3x9", batch = "wayfarer_ritual", timeoutTicks = 90) public static void socketEditsCancelWithoutSpendingIngredients(GameTestHelper h) {
        var layout = ritual(h, 0); h.assertTrue(RitualCrafting.activate(new Wearer(h), layout.center()) == RitualCrafting.Outcome.CRAFTING, "Initial boot ritual rejected");
        h.runAfterDelay(10, () -> { var stand = layout.stands().get(3); stand.unlock(); stand.removeMaterial(); stand.installMaterial(new ItemStack(Items.COPPER_BLOCK)); });
        h.runAfterDelay(65, () -> { h.assertTrue(RitualTestOutput.stack(layout.center()).isEmpty() && layout.items().stream().allMatch(s -> s.getCount() == 1)
                && layout.blocks().stream().noneMatch(OfferingBlockEntity::busy), "Canceled boot ritual spent ingredients or retained locks"); h.succeed(); });
    }
}
