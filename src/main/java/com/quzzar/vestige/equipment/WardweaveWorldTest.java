package com.quzzar.vestige.equipment;

import com.mojang.authlib.GameProfile;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.*;
import com.quzzar.vestige.magic.definition.*;
import com.quzzar.vestige.magic.effect.SpellEffects;
import com.quzzar.vestige.magic.expression.SpellValue;
import com.quzzar.vestige.magic.runtime.*;
import com.quzzar.vestige.magic.world.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.gametest.*;
import java.util.*;

@GameTestHolder(VestigeMainMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class WardweaveWorldTest {
    private static final class Wearer extends Player {
        Wearer(GameTestHelper h) { super(h.getLevel(),h.absolutePos(new BlockPos(1,1,1)),0,new GameProfile(UUID.randomUUID(),"quiet-wardweave")); }
        public boolean isSpectator() { return false; }
        public boolean isCreative() { return false; }
        public void displayClientMessage(Component text,boolean overlay) { throw new AssertionError("Wardweave emitted gameplay text"); }
        public void sendSystemMessage(Component text) { throw new AssertionError("Wardweave emitted gameplay text"); }
    }
    private static Wearer wearer(GameTestHelper h,int mask) {
        var p=new Wearer(h);NativeMagic.session(h.getLevel().getServer()).world().registerActor(p);
        p.setItemSlot(EquipmentSlot.CHEST,WardweaveImbuements.create(WardweaveDisplays.choices(mask)));NativeMana.set(p,100);return p;
    }
    private static void hit(Player p,float damage) { p.invulnerableTime=0;p.hurt(p.damageSources().generic(),damage); }
    @GameTest(template="empty_3x3x3",batch="wardweave")
    public static void everyVariantFormsOneChargeWithSharedProtectionRecoveryAndWear(GameTestHelper h) {
        var ability=NativeMagic.abilities().abilities().get(WardweaveImbuements.ABILITY);
        for(int mask=0;mask<16;mask++) {
            var p=wearer(h,mask);var robe=p.getItemBySlot(EquipmentSlot.CHEST);var v=WardweaveImbuements.read(robe).orElseThrow();
            hit(p,2);h.assertTrue(p.getHealth()==18 && robe.getDamageValue()==0,"Trigger hit used its new charge: "+mask);
            var recovery=NativeMagic.session(p.getServer()).runtime().recovery(p.getUUID(),WardweaveImbuements.ABILITY).orElseThrow();
            h.assertTrue(recovery.totalTicks()==((SpellCost.Cooldown)v.shaping().costs(ability.costs()).getFirst()).ticks(),"Wrong shared recovery: "+mask);
            double protection=MagicResolution.resolve(ability,v.modifiers()).variable(VestigeMainMod.location("protection"));
            hit(p,6);h.assertTrue(Math.abs(p.getHealth()-(12+protection))<1e-5 && robe.getDamageValue()==1,"Protection or protected wear mismatch: "+mask);
            hit(p,2);h.assertTrue(Math.abs(p.getHealth()-(10+protection))<1e-5 && robe.getDamageValue()==1 && NativeMana.amount(p)==100,"Charge stacked, rearmed or charged mana: "+mask);
            EquipmentMagic.removed(p);
        }
        h.succeed();
    }
    @GameTest(template="empty_3x3x3",batch="wardweave")
    public static void replacementAndInPlaceVariantEditsCannotReuseChargeOrShortenRecovery(GameTestHelper h) {
        var p=wearer(h,2);hit(p,2);var runtime=NativeMagic.session(p.getServer()).runtime();
        var before=runtime.recovery(p.getUUID(),WardweaveImbuements.ABILITY).orElseThrow();
        p.setItemSlot(EquipmentSlot.CHEST,WardweaveImbuements.create(WardweaveDisplays.choices(4)));hit(p,2);hit(p,2);
        h.assertTrue(p.getHealth()==14 && before.equals(runtime.recovery(p.getUUID(),WardweaveImbuements.ABILITY).orElseThrow()),"Shorter-recovery replacement bypassed wearer recovery");
        var edited=wearer(h,1);hit(edited,2);var robe=edited.getItemBySlot(EquipmentSlot.CHEST);
        ItemImbuements.write(robe,WardweaveImbuements.FAMILY,new WardweaveImbuements.Variant(WardweaveDisplays.choices(4)).selections());
        hit(edited,4);h.assertTrue(edited.getHealth()==14 && robe.getDamageValue()==0,"In-place variant edit retained its previous charge");h.succeed();
    }
    @GameTest(template="empty_3x3x3",batch="wardweave",timeoutTicks=115)
    public static void combinedTraitsSnapshotAtFormationAfterWearerBoostExpires(GameTestHelper h) {
        var p=wearer(h,9);
        var boost=new ItemAbilityDefinition(VestigeMainMod.location("test/wardweave_boost"),TraitProfile.empty(),Map.of(),List.of(),
                List.of(new SpellTrigger(VestigeMainMod.location("test/use"),SpellTriggerTypes.INTERACT,List.of())),
                List.of(new SpellEffects.GrantTraits(VestigeMainMod.location("test/wardweave_traits"),List.of("time","force","amplify").stream()
                        .map(name->new TraitModifier(VestigeMainMod.location(name),TraitModifier.Operation.MULTIPLY,1.25)).toList(),new SpellValue.Constant(2),TargetSpec.self())),ItemAbilityDefinition.Activation.REACTIVE);
        NativeMagic.session(p.getServer()).runtime().activate(boost,SpellEvent.of(SpellTriggerTypes.INTERACT,p.getUUID(),null),List.of(),CastReservation.NONE);
        hit(p,2);
        h.runAfterDelay(70,()->{hit(p,4);h.assertTrue(Math.abs(p.getHealth()-17.75)<1e-5 && p.getItemBySlot(EquipmentSlot.CHEST).getDamageValue()==1,"Variant or wearer boost was applied twice or not snapshotted");h.succeed();});
    }
    @GameTest(template="empty_3x3x3",batch="wardweave",timeoutTicks=140)
    public static void shortAndLongChargeLifetimesAreRealAndOccupiedChargeCannotRefresh(GameTestHelper h) {
        var shortWard=wearer(h,1);var longWard=wearer(h,2);hit(shortWard,2);hit(longWard,2);
        h.runAfterDelay(66,()->{hit(shortWard,4);hit(longWard,4);h.assertTrue(shortWard.getHealth()==14 && longWard.getHealth()==16,"Authored charge durations did not affect expiry");});
        var occupied=wearer(h,2);hit(occupied,2);
        // A smaller invulnerable repeated hit is ignored and cannot refresh the held charge.
        h.runAfterDelay(60,()->occupied.hurt(occupied.damageSources().generic(),1));
        h.runAfterDelay(123,()->{hit(occupied,4);h.assertTrue(occupied.getHealth()<=14 && occupied.getItemBySlot(EquipmentSlot.CHEST).getDamageValue()==0,"Expired occupied charge refreshed");h.succeed();});
    }
    @GameTest(template="empty_3x3x3",batch="wardweave")
    public static void allVariantsPersistRepairDyeRenameAndKeepEnchantments(GameTestHelper h) {
        var p=wearer(h,0);var registry=h.getLevel().registryAccess();var protection=registry.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.PROTECTION);
        var menu=new AnvilMenu(1,p.getInventory(),ContainerLevelAccess.create(h.getLevel(),h.absolutePos(new BlockPos(1,1,1))));
        for(int mask=0;mask<16;mask++) {
            var original=WardweaveDisplays.create(DyeColor.CYAN,mask);original.setDamageValue(60);original.enchant(protection,1);
            var restored=ItemStack.parse(registry,original.save(registry)).orElseThrow();h.assertTrue(ItemStack.matches(original,restored),"Saved variant changed: "+mask);
            menu.getSlot(0).set(restored);menu.getSlot(1).set(new ItemStack(ScrollItems.CALLOUS_THREAD.get()));menu.createResult();var repaired=menu.getSlot(2).getItem();
            h.assertTrue(!repaired.isEmpty() && repaired.getDamageValue()==60-original.getMaxDamage()/4 && repaired.getHoverName().equals(original.getHoverName())
                    && repaired.getEnchantmentLevel(protection)==1 && repaired.get(DataComponents.DYED_COLOR).equals(original.get(DataComponents.DYED_COLOR)),"Native repair lost variant or repaired wrong amount: "+mask);
            var dyed=DyedItemColor.applyDyes(repaired,List.of((DyeItem)Items.RED_DYE));
            h.assertTrue(WardweaveImbuements.read(dyed).equals(WardweaveImbuements.read(repaired)) && dyed.getDamageValue()==repaired.getDamageValue(),"Dyeing changed mechanical state: "+mask);
            menu.getSlot(0).set(dyed);menu.getSlot(1).set(ItemStack.EMPTY);menu.setItemName("Keepsake");menu.createResult();
            var named=menu.getSlot(2).getItem();h.assertTrue(!named.isEmpty() && WardweaveImbuements.read(named).equals(WardweaveImbuements.read(dyed))
                    && named.getDamageValue()==dyed.getDamageValue() && named.getHoverName().getString().contains("Keepsake"),"Renaming changed variant: "+mask);
            menu.setItemName("");
        }
        h.succeed();
    }
    @GameTest(template="empty_3x3x3",batch="wardweave")
    public static void reinforcedFinalProtectedWearBreaksNormallyAndClampsMana(GameTestHelper h) {
        var p=wearer(h,8);var robe=p.getItemBySlot(EquipmentSlot.CHEST);hit(p,2);robe.setDamageValue(119);NativeMana.set(p,125);hit(p,1);
        h.assertTrue(p.getHealth()==18 && robe.isEmpty() && NativeMana.amount(p)==100 && NativeMana.maximum(p)==100,"Reinforced final ward avoided break, protection or mana clamp");h.succeed();
    }
    private static RitualCrafting.Layout ritual(GameTestHelper h,int rotation) {
        var center=new BlockPos(4,1,4);var geometry=new LeylineShaping.Geometry(8,LeylineShaping.Shape.CROSS,2,0,LeylineShaping.Shape.DIAGONAL,3,0);
        h.setBlock(center,ApparatusBlocks.SPELLSTONE.get());for(int i=0;i<8;i++)h.setBlock(center.offset(geometry.offset(i)),ApparatusBlocks.PLINTH.get());
        var layout=RitualCrafting.layout((OfferingBlockEntity)h.getBlockEntity(center));
        var display=WardweaveDisplays.entries().stream().filter(e->e.id().getPath().equals("ritual/wardweave_robes/cyan/15")).findFirst().orElseThrow();
        for(var offering:display.offerings())layout.stands().get((offering.seat()+rotation)%8).insert(new ItemStack(BuiltInRegistries.ITEM.get(offering.ingredient().items().getFirst())));
        for(var material:display.imbuements())layout.stands().get((material.seat()+rotation)%8).installMaterial(material.stack());return layout;
    }
    @GameTest(template="empty_9x3x9",batch="wardweave_ritual",timeoutTicks=90)
    public static void rotatedFullPaletteRitualCommitsExactlyOnceAndKeepsAllSockets(GameTestHelper h) {
        var layout=ritual(h,2);var inputs=RitualInputs.capture(layout);var p=new Wearer(h);
        h.assertTrue(RitualCrafting.activate(p,layout.center())==RitualCrafting.Outcome.CRAFTING,"Full palette ritual rejected");
        h.assertTrue(RitualCrafting.activate(p,layout.center())==RitualCrafting.Outcome.BUSY,"Duplicate activation accepted");
        h.runAfterDelay(65,()->{h.assertTrue(ItemStack.matches(RitualTestOutput.stack(layout.center()),WardweaveDisplays.create(DyeColor.CYAN,15))
                    && layout.items().stream().allMatch(ItemStack::isEmpty),"Atomic ritual lost output or consumed wrong offerings");
            h.assertTrue(inputs.nodes().stream().allMatch(n->ItemStack.matches(n.material(),layout.stands().get(n.seat()).materialItem()))
                    && layout.blocks().stream().noneMatch(OfferingBlockEntity::busy),"Ritual consumed sockets or retained locks");h.succeed();});
    }
    @GameTest(template="empty_9x3x9",batch="wardweave_ritual",timeoutTicks=90)
    public static void changedSocketCancelsCraftWithoutConsumingInputs(GameTestHelper h) {
        var layout=ritual(h,0);h.assertTrue(RitualCrafting.activate(new Wearer(h),layout.center())==RitualCrafting.Outcome.CRAFTING,"Initial ritual rejected");
        h.runAfterDelay(10,()->{var stand=layout.stands().get(0);stand.unlock();stand.removeMaterial();});
        h.runAfterDelay(65,()->{h.assertTrue(RitualTestOutput.stack(layout.center()).isEmpty() && layout.items().stream().allMatch(s->s.getCount()==1)
                && layout.blocks().stream().noneMatch(OfferingBlockEntity::busy),"Canceled ritual spent inputs or retained locks");h.succeed();});
    }
    @GameTest(template="empty_9x3x9",batch="wardweave_ritual")
    public static void duplicateRecognizedPairsRejectBeforeLockingOrConsuming(GameTestHelper h) {
        var layout=ritual(h,0);layout.stands().get(2).installMaterial(new ItemStack(Items.AMETHYST_BLOCK));
        h.assertTrue(RitualCrafting.activate(new Wearer(h),layout.center())==RitualCrafting.Outcome.INVALID && layout.items().stream().allMatch(s->s.getCount()==1)
                && layout.blocks().stream().noneMatch(OfferingBlockEntity::busy),"Duplicate Enduring pair mutated ritual");h.succeed();
    }
}
