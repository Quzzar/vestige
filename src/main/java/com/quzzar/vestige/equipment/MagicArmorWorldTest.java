package com.quzzar.vestige.equipment;

import com.mojang.authlib.GameProfile;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.ScrollItems;
import com.quzzar.vestige.magic.definition.*;
import com.quzzar.vestige.magic.effect.SpellEffects;
import com.quzzar.vestige.magic.expression.SpellValue;
import com.quzzar.vestige.magic.runtime.*;
import com.quzzar.vestige.magic.world.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.enchantment.*;
import net.neoforged.neoforge.gametest.*;
import java.util.*;

@GameTestHolder(VestigeMainMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MagicArmorWorldTest {
    private static ResourceLocation id(String name) { return VestigeMainMod.location(name); }
    private static Player player(GameTestHelper h, boolean cinder) {
        var p = new Player(h.getLevel(), h.absolutePos(new BlockPos(1,1,1)), 0, new GameProfile(UUID.randomUUID(), "quiet-robe-player")) {
            public boolean isSpectator() { return false; }
            public boolean isCreative() { return false; }
            public void displayClientMessage(net.minecraft.network.chat.Component text, boolean overlay) { throw new AssertionError("Robe emitted gameplay text"); }
            public void sendSystemMessage(net.minecraft.network.chat.Component text) { throw new AssertionError("Robe emitted gameplay text"); }
        };
        NativeMagic.session(h.getLevel().getServer()).world().registerActor(p);
        NativeMana.set(p, 100); p.setItemSlot(EquipmentSlot.CHEST, new ItemStack(cinder ? MagicEquipment.CINDERWEAVE.get() : MagicEquipment.WARDWEAVE.get()));
        return p;
    }
    private static void hit(Player p, float amount) { p.invulnerableTime = 0; p.hurt(p.damageSources().generic(), amount); }
    private static void boost(Player p, Map<String,Double> traits, int duration) {
        var ability = new ItemAbilityDefinition(id("test/robe_boost"), TraitProfile.empty(), Map.of(), List.of(),
                List.of(new SpellTrigger(id("test/use"), SpellTriggerTypes.INTERACT, List.of())),
                List.of(new SpellEffects.GrantTraits(id("test/robe_traits"), traits.entrySet().stream().map(e -> new TraitModifier(id(e.getKey()), TraitModifier.Operation.MULTIPLY,e.getValue())).toList(),
                        new SpellValue.Constant(duration), TargetSpec.self())), ItemAbilityDefinition.Activation.REACTIVE);
        NativeMagic.session(p.getServer()).runtime().activate(ability, SpellEvent.of(SpellTriggerTypes.INTERACT,p.getUUID(),null), List.of(),CastReservation.NONE);
    }
    @GameTest(template="empty_3x3x3",batch="robes")
    public static void firstHitArmsOneWardSecondHitSpendsItAndChargesWearOnce(GameTestHelper h) {
        var p=player(h,false);var robe=p.getItemBySlot(EquipmentSlot.CHEST);
        hit(p,4);h.assertTrue(p.getHealth()==16 && robe.getDamageValue()==0,"First hit was reduced or armor-bypassing hit wore ordinary armor");
        hit(p,4);h.assertTrue(p.getHealth()==14 && robe.getDamageValue()==1,"Ward did not prevent exactly two HP with one wear");
        hit(p,4);h.assertTrue(p.getHealth()==10 && robe.getDamageValue()==1,"Ward refreshed or cooldown was bypassed");h.succeed();
    }
    @GameTest(template="empty_3x3x3",batch="robes")
    public static void ordinaryAndProtectedPhysicalDamageEachWearOnce(GameTestHelper h) {
        var p=player(h,false);var robe=p.getItemBySlot(EquipmentSlot.CHEST);var source=p.damageSources().cactus();
        p.hurt(source,8);h.assertTrue(robe.getDamageValue()==2,"Ordinary chest armor wear missing");
        p.invulnerableTime=0;p.hurt(source,8);h.assertTrue(robe.getDamageValue()==4,"Protected physical hit charged wear twice");h.succeed();
    }
    @GameTest(template="empty_3x3x3",batch="robes")
    public static void fullWardProtectionStillBreaksTheRobeAndClampsMana(GameTestHelper h) {
        var p=player(h,false);var robe=p.getItemBySlot(EquipmentSlot.CHEST);
        hit(p,2);NativeMana.set(p,125);robe.setDamageValue(79);hit(p,2);
        h.assertTrue(p.getHealth()==18 && robe.isEmpty(),"Final ward charge failed to protect or did not break");
        h.assertTrue(NativeMana.amount(p)==100 && NativeMana.maximum(p)==100,"Broken robe retained bonus mana");h.succeed();
    }
    @GameTest(template="empty_3x3x3",batch="robes")
    public static void removalAndInventorySwapClearChargeWithoutResettingRecovery(GameTestHelper h) {
        var p=player(h,false);hit(p,2);var robe=p.getItemBySlot(EquipmentSlot.CHEST);
        p.getInventory().setItem(38,ItemStack.EMPTY);p.getInventory().setItem(38,robe);hit(p,2);hit(p,2);
        h.assertTrue(p.getHealth()==14 && robe.getDamageValue()==0,"Unequip retained charge or reset wearer recovery");h.succeed();
    }
    @GameTest(template="empty_3x3x3",batch="robes",timeoutTicks=110)
    public static void wardSnapshotsTraitsAndExpiresWithoutRefreshing(GameTestHelper h) {
        var p=player(h,false);boost(p,Map.of("time",1.25,"force",2d),2);hit(p,2);
        h.runAfterDelay(90,()->{hit(p,4);h.assertTrue(p.getHealth()==18,"Ward lost its snapshotted strength or duration after boost expiry");h.succeed();});
    }
    @GameTest(template="empty_3x3x3",batch="robes",timeoutTicks=100)
    public static void expiredWardCannotProtect(GameTestHelper h) {
        var p=player(h,false);hit(p,2);
        h.runAfterDelay(82,()->{hit(p,4);h.assertTrue(p.getHealth()==14,"Expired ward still protected");h.succeed();});
    }
    @GameTest(template="empty_3x3x3",batch="robes",timeoutTicks=30)
    public static void zeroDurationDoesNotStartAFalseWardOrSpendRecovery(GameTestHelper h) {
        var p=player(h,false);boost(p,Map.of("time",0d),2);hit(p,2);
        h.runAfterDelay(5,()->{hit(p,2);hit(p,2);h.assertTrue(p.getHealth()==16 && p.getItemBySlot(EquipmentSlot.CHEST).getDamageValue()==1,
                "Zero-duration ward spent recovery or blocked a later valid ward");h.succeed();});
    }
    @GameTest(template="empty_3x3x3",batch="robes")
    public static void fireReductionUsesActualFireTagsBudgetAndWear(GameTestHelper h) {
        var p=player(h,true);var robe=p.getItemBySlot(EquipmentSlot.CHEST);
        p.hurt(p.damageSources().onFire(),8);h.assertTrue(p.getHealth()==14 && robe.getDamageValue()==2,"Baseline burning reduction/wear incorrect");
        hit(p,4);h.assertTrue(p.getHealth()==10 && robe.getDamageValue()==2,"Nonfire hit received cinder protection");
        p.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,100));p.invulnerableTime=0;p.hurt(p.damageSources().onFire(),8);
        h.assertTrue(p.getHealth()==10 && robe.getDamageValue()==2,"Immune fire hit spent wear");h.succeed();
    }
    @GameTest(template="empty_3x3x3",batch="robes")
    public static void boostedFireCanFullyPreventDamageButStillWears(GameTestHelper h) {
        var p=player(h,true);var robe=p.getItemBySlot(EquipmentSlot.CHEST);boost(p,Map.of("fire",4d,"amplify",4d),100);
        p.hurt(p.damageSources().onFire(),8);h.assertTrue(p.getHealth()==20 && robe.getDamageValue()==2,"Full fire protection failed or avoided wear");h.succeed();
    }
    @GameTest(template="empty_3x3x3",batch="robes")
    public static void ignoredRepeatedHitDoesNotSpendWard(GameTestHelper h) {
        var p=player(h,false);hit(p,4);p.hurt(p.damageSources().generic(),4);
        h.assertTrue(p.getHealth()==16 && p.getItemBySlot(EquipmentSlot.CHEST).getDamageValue()==0,"Ignored repeated hit spent ward");
        hit(p,4);h.assertTrue(p.getHealth()==14,"Ignored repeat consumed charge");h.succeed();
    }
    @GameTest(template="empty_3x3x3",batch="robes")
    public static void capacityDoesNotRefillAndRemovalPermanentlyClamps(GameTestHelper h) {
        var p=player(h,true);var robe=p.getItemBySlot(EquipmentSlot.CHEST);
        h.assertTrue(NativeMana.amount(p)==100 && NativeMana.maximum(p)==125,"Equip refilled mana or did not raise capacity");
        NativeMana.set(p,125);h.assertTrue(NativeMana.spend(p,110) && NativeMana.amount(p)==15,"Payment cannot spend mana above 100");
        NativeMana.set(p,125);p.setItemSlot(EquipmentSlot.CHEST,ItemStack.EMPTY);p.setItemSlot(EquipmentSlot.CHEST,robe);
        h.assertTrue(NativeMana.amount(p)==100,"Reequip restored hidden excess mana");
        for(int i=0;i<351;i++)NativeMana.recover(p);
        h.assertTrue(NativeMana.amount(p)==125,"Recovery stops at old capacity");h.succeed();
    }
    @GameTest(template="empty_3x3x3",batch="robes")
    public static void enchantingAndRepairFollowTheAcceptedPolicy(GameTestHelper h) {
        var p=player(h,false);var robe=p.getItemBySlot(EquipmentSlot.CHEST);var registry=h.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var unbreaking=registry.getOrThrow(Enchantments.UNBREAKING);var mending=registry.getOrThrow(Enchantments.MENDING);var protection=registry.getOrThrow(Enchantments.PROTECTION);
        h.assertTrue(!robe.supportsEnchantment(unbreaking) && !robe.isPrimaryItemFor(unbreaking) && !robe.supportsEnchantment(mending)
                && robe.supportsEnchantment(protection) && robe.isPrimaryItemFor(protection),"Enchantment eligibility incorrect");
        robe.enchant(unbreaking,3);robe.enchant(mending,1);robe.enchant(protection,1);
        h.assertTrue(robe.getEnchantmentLevel(unbreaking)==0 && robe.getAllEnchantments(registry).getLevel(mending)==0,"Excluded enchantment remained active");
        robe.set(DataComponents.DYED_COLOR,new DyedItemColor(0x205080,false));robe.setDamageValue(60);
        var menu=new AnvilMenu(1,p.getInventory(),ContainerLevelAccess.create(h.getLevel(),h.absolutePos(new BlockPos(1,1,1))));
        menu.getSlot(0).set(robe.copy());menu.getSlot(1).set(new ItemStack(ScrollItems.CALLOUS_THREAD.get()));menu.createResult();
        var repaired=menu.getSlot(2).getItem();h.assertTrue(!repaired.isEmpty() && repaired.getDamageValue()==40 && repaired.get(DataComponents.DYED_COLOR).rgb()==0x205080
                && repaired.getEnchantmentLevel(protection)==1,"Thread repair failed to restore 25% or lost components");
        for(var forbidden:List.of(unbreaking,mending)) {
            var book=new ItemStack(Items.ENCHANTED_BOOK);book.enchant(forbidden,1);menu.getSlot(0).set(new ItemStack(MagicEquipment.WARDWEAVE.get()));menu.getSlot(1).set(book);menu.createResult();
            h.assertTrue(menu.getSlot(2).getItem().isEmpty(),"Anvil accepted excluded enchantment");
        }
        h.succeed();
    }
}
