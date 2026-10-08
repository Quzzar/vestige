package com.quzzar.vestige.apparatus;

import com.mojang.authlib.GameProfile;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.world.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.gametest.*;
import java.util.*;
import java.util.function.Consumer;

@GameTestHolder(VestigeMainMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class StaffWorldTest {
    private static ResourceLocation id(String path) { return VestigeMainMod.location(path); }
    private static ItemStack staff(String trait,String spell) {
        return StaffData.bind(StaffData.create(id(trait)),ScrollItems.scroll(id(spell)),NativeMagic.spells().spells().get(id(spell)));
    }
    private static Player player(GameTestHelper h,String trait,String spell,boolean known) {
        var player=new Player(h.getLevel(),h.absolutePos(new BlockPos(4,1,1)),0,new GameProfile(UUID.randomUUID(),"quiet-staff-player")) {
            public boolean isSpectator() { return false; }
            public boolean isCreative() { return false; }
            public void displayClientMessage(net.minecraft.network.chat.Component text,boolean overlay) { throw new AssertionError("Staff emitted gameplay text"); }
            public void sendSystemMessage(net.minecraft.network.chat.Component text) { throw new AssertionError("Staff emitted gameplay text"); }
        };
        player.setPos(Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(4,1,1))));NativeMana.set(player,100);
        if(known) SpellKnowledge.identify(player,id(spell));player.setItemInHand(InteractionHand.MAIN_HAND,staff(trait,spell));return player;
    }
    private static RitualCrafting.Layout ritual(GameTestHelper h,int slots) {
        var centerPos=new BlockPos(4,1,4);var geometry=new LeylineShaping.Geometry(slots,LeylineShaping.Shape.CROSS,2,0,LeylineShaping.Shape.DIAGONAL,3,0);
        h.setBlock(centerPos,ApparatusBlocks.SPELLSTONE.get());
        for(int i=0;i<8;i++) if(slots==8 || i%2==0) h.setBlock(centerPos.offset(geometry.offset(i)),ApparatusBlocks.PLINTH.get());
        return LeylineStructure.find((OfferingBlockEntity)h.getBlockEntity(centerPos),slots).getFirst();
    }
    @GameTest(template="empty_9x3x9",batch="staff_construct",timeoutTicks=90)
    public static void constructionConsumesMundaneShaftAndRetainsSockets(GameTestHelper h) {
        var layout=ritual(h,8);var items=new ItemStack[]{new ItemStack(MundaneStaffs.item(MundaneStaffs.Shaft.BONE)),new ItemStack(Items.IRON_INGOT),ItemStack.EMPTY,
                new ItemStack(ScrollItems.ENSORCELLED_THREAD.get()),ScrollItems.fragment(id("fire")),ItemStack.EMPTY,new ItemStack(Items.AMETHYST_SHARD),ItemStack.EMPTY};
        for(int i=0;i<8;i++) {if(!items[i].isEmpty()) layout.stands().get((i+2)%8).insert(items[i]);layout.stands().get(i).installMaterial(new ItemStack(Items.DIAMOND_BLOCK));}
        var crafter=player(h,"abjuration","pf2_shield",false);
        h.assertTrue(RitualCrafting.activate(crafter,layout.center())==RitualCrafting.Outcome.CRAFTING,"Staff construction rejected");
        h.runAfterDelay(65,() -> {
            var output=RitualTestOutput.stack(layout.center());var binding=StaffData.binding(output).orElseThrow();
            h.assertTrue(binding.affinity().equals(id("fire")) && binding.capacity()==2 && binding.slots().stream().allMatch(Optional::isEmpty),"Constructed wrong staff");
            h.assertTrue(output.getMaxDamage()==40 && output.getDamageValue()==0 && layout.items().stream().allMatch(ItemStack::isEmpty),"Wrong consumption/capacity");
            h.assertTrue(layout.stands().stream().allMatch(s -> s.materialItem().is(Items.DIAMOND_BLOCK)),"Construction consumed sockets");h.succeed();
        });
    }
    private static StaffMenu menu(Player player) {
        return new StaffMenu(1,player.getInventory(),InteractionHand.MAIN_HAND,player.getMainHandItem(),player.getInventory().selected);
    }
    @GameTest(template="empty_9x3x9",batch="staff_menu")
    public static void menuTransfersAndSwapsExactVariantsWithoutWearOrIdentification(GameTestHelper h) {
        var caster=player(h,"fire","firebolt",false);caster.getMainHandItem().setDamageValue(9);
        SpellKnowledge.identify(caster,id("fireball"));
        var source=ScrollItems.shapedScroll(id("fireball"),new LeylineShaping.Modifiers(1.1,1.2,.9,.8),List.of(new Spellshaping.Selection(id("reaching"),2)));
        var menu=menu(caster);menu.setCarried(source.copyWithCount(3));menu.clicked(1,0,net.minecraft.world.inventory.ClickType.PICKUP,caster);
        h.assertTrue(menu.getCarried().getCount()==2 && ItemStack.matches(StaffData.source(caster.getMainHandItem(),1),source),"Insertion did not consume one exact variant");
        menu.setCarried(ItemStack.EMPTY);menu.clicked(1,0,net.minecraft.world.inventory.ClickType.PICKUP,caster);
        h.assertTrue(ItemStack.matches(menu.getCarried(),source) && StaffData.source(caster.getMainHandItem(),1).isEmpty(),"Removal changed source or failed to empty binding");
        menu.clicked(0,0,net.minecraft.world.inventory.ClickType.PICKUP,caster);
        h.assertTrue(ScrollItems.scroll(menu.getCarried()).orElseThrow().spell().equals(id("firebolt"))
                && ItemStack.matches(StaffData.source(caster.getMainHandItem(),0),source),"Replacement lost either original scroll");
        h.assertTrue(caster.getMainHandItem().getDamageValue()==9 && NativeMana.amount(caster)==100
                && !SpellKnowledge.identified(caster,id("firebolt")),"Menu paid wear/mana or identified saved scroll");h.succeed();
    }
    @GameTest(template="empty_9x3x9",batch="staff_menu_known")
    public static void unknownMatchingScrollRejectsEveryInsertionAndUsesEachPlayersKnowledge(GameTestHelper h) {
        var caster=player(h,"fire","fireball",true);var menu=menu(caster);var original=caster.getMainHandItem().copy();
        var unknown=ScrollItems.scroll(id("firebolt")).copyWithCount(3);
        menu.setCarried(unknown.copy());
        for(int slot:List.of(1,0)) menu.clicked(slot,0,net.minecraft.world.inventory.ClickType.PICKUP,caster);
        h.assertTrue(ItemStack.matches(menu.getCarried(),unknown) && ItemStack.matches(caster.getMainHandItem(),original),"Unknown matching scroll inserted or replaced a binding");
        menu.clicked(-999,0,net.minecraft.world.inventory.ClickType.QUICK_CRAFT,caster);
        menu.clicked(1,1,net.minecraft.world.inventory.ClickType.QUICK_CRAFT,caster);
        menu.clicked(-999,2,net.minecraft.world.inventory.ClickType.QUICK_CRAFT,caster);
        h.assertTrue(ItemStack.matches(menu.getCarried(),unknown) && ItemStack.matches(caster.getMainHandItem(),original),"Dragging bypassed identification");
        menu.setCarried(ItemStack.EMPTY);caster.getInventory().setItem(9,unknown.copy());
        menu.clicked(2,0,net.minecraft.world.inventory.ClickType.QUICK_MOVE,caster);
        caster.getInventory().setItem(2,unknown.copy());menu.clicked(1,2,net.minecraft.world.inventory.ClickType.SWAP,caster);
        h.assertTrue(caster.getInventory().getItem(9).getCount()==3 && caster.getInventory().getItem(2).getCount()==3
                && ItemStack.matches(caster.getMainHandItem(),original) && !SpellKnowledge.identified(caster,id("firebolt")),"Shift-click or hotbar swap bypassed identification");
        var other=player(h,"fire","fireball",true);SpellKnowledge.identify(other,id("firebolt"));
        h.assertTrue(menu(other).getSlot(1).mayPlace(unknown) && !menu.getSlot(1).mayPlace(unknown),"Knowledge from another player leaked into insertion");
        SpellKnowledge.identify(caster,id("firebolt"));menu.setCarried(unknown.copy());menu.clicked(1,0,net.minecraft.world.inventory.ClickType.PICKUP,caster);
        h.assertTrue(menu.getCarried().getCount()==2 && StaffData.source(caster.getMainHandItem(),1).getCount()==1
                && caster.getMainHandItem().getDamageValue()==0 && NativeMana.amount(caster)==100,"New identification did not allow insertion into the same open menu");h.succeed();
    }
    @GameTest(template="empty_9x3x9",batch="staff_menu_reject")
    public static void menuRejectsIncompatibleItemsAndLocksOpenedStaff(GameTestHelper h) {
        var caster=player(h,"fire","fireball",false);var menu=menu(caster);var original=caster.getMainHandItem().copy();
        SpellKnowledge.identify(caster,id("pf2_heal"));SpellKnowledge.identify(caster,id("missing_spell"));
        for(var wrong:List.of(ScrollItems.scroll(id("pf2_heal")),new ItemStack(Items.DIAMOND),ScrollItems.scroll(id("missing_spell")))) {
            menu.setCarried(wrong.copy());menu.clicked(1,0,net.minecraft.world.inventory.ClickType.PICKUP,caster);
            h.assertTrue(ItemStack.matches(menu.getCarried(),wrong) && ItemStack.matches(caster.getMainHandItem(),original),"Incompatible item entered staff");
        }
        menu.setCarried(ItemStack.EMPTY);menu.clicked(1,0,net.minecraft.world.inventory.ClickType.SWAP,caster);
        menu.clicked(29,0,net.minecraft.world.inventory.ClickType.PICKUP,caster);
        h.assertTrue(ItemStack.matches(original,caster.getMainHandItem()) && menu.getCarried().isEmpty(),"Opened staff moved through hotbar or direct pickup");
        caster.getMainHandItem().setDamageValue(1);
        h.assertTrue(!menu.stillValid(caster) && !menu.clickMenuButton(caster,0),"Stale staff menu accepted a changed source");h.succeed();
    }
    @GameTest(template="empty_9x3x9",batch="staff_menu_shift")
    public static void shiftTransfersPreserveCountsAndFullInventoryLeavesBinding(GameTestHelper h) {
        var caster=player(h,"fire","fireball",false);var menu=menu(caster);var source=ScrollItems.scroll(id("firebolt"));
        SpellKnowledge.identify(caster,id("firebolt"));
        caster.getInventory().setItem(9,source.copyWithCount(3));
        h.assertTrue(!menu.quickMoveStack(caster,2).isEmpty() && caster.getInventory().getItem(9).getCount()==2
                && ItemStack.matches(StaffData.source(caster.getMainHandItem(),1),source),"Shift insertion lost scroll count");
        for(int i=1;i<36;i++) caster.getInventory().setItem(i,new ItemStack(Items.STONE,64));
        h.assertTrue(menu.quickMoveStack(caster,1).isEmpty() && ItemStack.matches(StaffData.source(caster.getMainHandItem(),1),source),"Full inventory destroyed a binding");
        caster.getInventory().setItem(9,ItemStack.EMPTY);menu.quickMoveStack(caster,1);
        h.assertTrue(StaffData.source(caster.getMainHandItem(),1).isEmpty() && ItemStack.matches(caster.getInventory().getItem(9),source),"Shift removal failed");h.succeed();
    }
    @GameTest(template="empty_9x3x9",batch="staff_menu_stack")
    public static void nativeShiftClickDistributesStackAndNeverOverwritesOccupiedBindings(GameTestHelper h) {
        var caster=player(h,"fire","fireball",false);caster.setItemInHand(InteractionHand.MAIN_HAND,StaffData.expand(StaffData.expand(caster.getMainHandItem())));
        SpellKnowledge.identify(caster,id("firebolt"));
        var menu=menu(caster);caster.getInventory().setItem(9,ScrollItems.scroll(id("firebolt")).copyWithCount(3));
        menu.clicked(6,0,net.minecraft.world.inventory.ClickType.QUICK_MOVE,caster);
        var slots=StaffData.binding(caster.getMainHandItem()).orElseThrow().slots();
        h.assertTrue(caster.getInventory().getItem(9).isEmpty() && slots.subList(1,4).stream().allMatch(Optional::isPresent)
                && slots.get(0).orElseThrow().spell().equals(id("fireball")) && slots.subList(4,6).stream().allMatch(Optional::isEmpty),"Native shift loop lost, duplicated or overwrote a scroll");h.succeed();
    }
    @GameTest(template="empty_9x3x9",batch="staff_menu_offhand")
    public static void offhandMenuLocksItsSourceAndTransfersFromTheOtherHand(GameTestHelper h) {
        var caster=player(h,"fire","fireball",false);var original=caster.getMainHandItem();original.setDamageValue(9);
        SpellKnowledge.identify(caster,id("firebolt"));
        caster.setItemInHand(InteractionHand.OFF_HAND,original);caster.setItemInHand(InteractionHand.MAIN_HAND,ScrollItems.scroll(id("firebolt")).copyWithCount(2));
        var menu=new StaffMenu(1,caster.getInventory(),InteractionHand.OFF_HAND,original,40);
        menu.clicked(1,40,net.minecraft.world.inventory.ClickType.SWAP,caster);
        h.assertTrue(ItemStack.matches(original,caster.getOffhandItem()) && StaffData.source(caster.getOffhandItem(),1).isEmpty(),"Opened offhand source swapped into a scroll slot");
        menu.clicked(1,0,net.minecraft.world.inventory.ClickType.SWAP,caster);
        h.assertTrue(caster.getMainHandItem().getCount()==1 && StaffData.source(caster.getOffhandItem(),1).is(ScrollItems.SCROLL.get())
                && caster.getOffhandItem().getDamageValue()==9 && menu.stillValid(caster),"Other-hand scroll transfer lost count or overwrote source wear");h.succeed();
    }
    @GameTest(template="empty_9x3x9",batch="staff_menu_missing")
    public static void removedDefinitionCanBeTakenOffButCannotBeInsertedAgain(GameTestHelper h) {
        var caster=player(h,"fire","fireball",false);
        SpellKnowledge.identify(caster,id("removed_spell"));
        CustomData.update(DataComponents.CUSTOM_DATA,caster.getMainHandItem(),tag -> tag.getList("slots",Tag.TAG_COMPOUND).getCompound(0).putString("vestige_spell","vestige:removed_spell"));
        var menu=menu(caster);menu.clicked(0,0,net.minecraft.world.inventory.ClickType.PICKUP,caster);
        h.assertTrue(ScrollItems.scroll(menu.getCarried()).orElseThrow().spell().equals(id("removed_spell")) && StaffData.source(caster.getMainHandItem(),0).isEmpty(),"Unavailable source identity was lost on removal");
        menu.clicked(1,0,net.minecraft.world.inventory.ClickType.PICKUP,caster);
        h.assertTrue(!menu.getCarried().isEmpty() && StaffData.source(caster.getMainHandItem(),1).isEmpty(),"Unavailable source inserted without a valid definition");h.succeed();
    }
    @GameTest(template="empty_9x3x9",batch="staff_expand",timeoutTicks=90)
    public static void expansionKeepsBindingsSelectionAndUsedDurability(GameTestHelper h) {
        var original=StaffData.select(staff("fire","fireball"),1);original.setDamageValue(13);
        var layout=ritual(h,4);layout.stands().get(0).insert(original);layout.stands().get(2).insert(new ItemStack(Items.DIAMOND));
        layout.stands().get(4).insert(new ItemStack(Items.AMETHYST_SHARD));layout.stands().get(6).insert(new ItemStack(ScrollItems.ENSORCELLED_THREAD.get()));
        var expected=StaffData.binding(original).orElseThrow();
        h.assertTrue(RitualCrafting.activate(player(h,"fire","fireball",true),layout.center())==RitualCrafting.Outcome.CRAFTING,"Expansion rejected");
        h.runAfterDelay(65,() -> {
            var output=RitualTestOutput.stack(layout.center());var binding=StaffData.binding(output).orElseThrow();
            h.assertTrue(binding.capacity()==4 && binding.selected()==1 && binding.slots().subList(0,2).equals(expected.slots())
                    && output.getDamageValue()==13 && output.getMaxDamage()==80,"Expansion lost bindings/selection/wear");h.succeed();
        });
    }
    @GameTest(template="empty_9x3x9",batch="staff_reject")
    public static void mismatchedAffinityRejectsWithoutConsumption(GameTestHelper h) {
        var layout=ritual(h,4);layout.stands().get(0).insert(staff("fire","fireball"));
        layout.stands().get(2).insert(ScrollItems.scroll(id("pf2_heal")));layout.stands().get(4).insert(new ItemStack(Items.AMETHYST_SHARD));
        var before=layout.items();h.assertTrue(RitualCrafting.activate(player(h,"fire","fireball",true),layout.center())==RitualCrafting.Outcome.INVALID,"Wrong affinity bound");
        h.assertTrue(java.util.stream.IntStream.range(0,8).allMatch(i -> ItemStack.matches(before.get(i),layout.items().get(i))),"Rejected binding spent ingredients");h.succeed();
    }
    @GameTest(template="empty_9x3x9",batch="staff_cancel",timeoutTicks=90)
    public static void changedExpansionIngredientCancelsWithoutConsumption(GameTestHelper h) {
        var layout=ritual(h,4);layout.stands().get(0).insert(staff("fire","firebolt"));
        layout.stands().get(2).insert(new ItemStack(Items.DIAMOND));layout.stands().get(4).insert(new ItemStack(Items.AMETHYST_SHARD));
        layout.stands().get(6).insert(new ItemStack(ScrollItems.ENSORCELLED_THREAD.get()));
        h.assertTrue(RitualCrafting.activate(player(h,"fire","fireball",true),layout.center())==RitualCrafting.Outcome.CRAFTING,"Expansion rejected");
        h.runAfterDelay(10,() -> layout.stands().get(6).remove());
        h.runAfterDelay(65,() -> {
            h.assertTrue(RitualTestOutput.stack(layout.center()).isEmpty() && layout.items().stream().filter(s -> !s.isEmpty()).count()==3
                    && layout.blocks().stream().noneMatch(OfferingBlockEntity::busy),"Canceled expansion consumed or kept locks");h.succeed();
        });
    }
    @GameTest(template="empty_9x3x9",batch="staff_spawn",timeoutTicks=90)
    public static void canceledOutputSpawnPreservesOriginalStaffAndUpgradeIngredients(GameTestHelper h) {
        var layout=ritual(h,4);layout.stands().get(0).insert(staff("fire","firebolt"));
        layout.stands().get(2).insert(new ItemStack(Items.DIAMOND));layout.stands().get(4).insert(new ItemStack(Items.AMETHYST_SHARD));
        layout.stands().get(6).insert(new ItemStack(ScrollItems.ENSORCELLED_THREAD.get()));
        var before=layout.items();Consumer<EntityJoinLevelEvent> cancel=event -> {
            if(event.getLevel()==h.getLevel() && event.getEntity() instanceof ItemEntity item && item.getItem().is(ScrollItems.STAFF.get())
                    && item.position().distanceToSqr(Vec3.atCenterOf(layout.center().getBlockPos()))<4) event.setCanceled(true);
        };
        NeoForge.EVENT_BUS.addListener(cancel);
        h.assertTrue(RitualCrafting.activate(player(h,"fire","fireball",true),layout.center())==RitualCrafting.Outcome.CRAFTING,"Expansion rejected");
        h.runAfterDelay(65,() -> {
            NeoForge.EVENT_BUS.unregister(cancel);h.assertTrue(RitualTestOutput.stack(layout.center()).isEmpty()
                    && java.util.stream.IntStream.range(0,8).allMatch(i -> ItemStack.matches(before.get(i),layout.items().get(i))),"Rejected spawn consumed originals");h.succeed();
        });
    }
    @GameTest(template="empty_9x3x9",batch="staff_payment",timeoutTicks=100)
    public static void castsPayOnceAndCopiesCanRepeatWithoutOrdinarySpellRecovery(GameTestHelper h) {
        var caster=player(h,"abjuration","pf2_shield",true);
        h.assertTrue(StaffCasting.cast(caster,InteractionHand.MAIN_HAND) && NativeMana.amount(caster)==94 && caster.getMainHandItem().getDamageValue()==1,"Staff did not commit one wear and normal mana");
        caster.setItemInHand(InteractionHand.MAIN_HAND,staff("abjuration","pf2_shield"));
        h.assertTrue(StaffCasting.cast(caster,InteractionHand.MAIN_HAND) && NativeMana.amount(caster)==88
                && caster.getMainHandItem().getDamageValue()==1,"Staff retained ordinary spell recovery");
        h.assertTrue(StaffCasting.cast(caster,InteractionHand.MAIN_HAND) && NativeMana.amount(caster)==82
                && caster.getMainHandItem().getDamageValue()==2,"Repeat staff cast did not pay once");h.succeed();
    }
    @GameTest(template="empty_9x3x9",batch="staff_rarity")
    public static void nativeSpellRaritiesSpendOneTwoThreeOrFourDurability(GameTestHelper h) {
        var target=h.spawn(net.minecraft.world.entity.EntityType.VILLAGER,new BlockPos(4,1,4));target.setNoAi(true);
        var names=List.of("pf2_shield","shield","counterspell","abyssal_shroud");
        var rarities=com.quzzar.vestige.magic.definition.SpellRarity.values();
        for(int i=0;i<names.size();i++) {
            var name=names.get(i);var caster=player(h,"abjuration",name,true);caster.setYRot(0);caster.setXRot(0);
            h.assertTrue(NativeMagic.spells().spells().get(id(name)).rarity()==rarities[i],"Rarity fixture changed");
            h.assertTrue(caster.getMainHandItem().getMaxDamage()==40 && StaffCasting.cast(caster,InteractionHand.MAIN_HAND),"Native rarity cast rejected");
            h.assertTrue(caster.getMainHandItem().getDamageValue()==i+1 && NativeMana.amount(caster)<100,"Native "+rarities[i]+" cast spent wrong durability");
        }
        h.succeed();
    }
    /** A trusted temporary contribution forces the real volatility path without altering the loaded catalog. */
    private static void certainChaos(Player player) {
        var session=NativeMagic.session(player.getServer());session.world().registerActor(player);
        var boost=new com.quzzar.vestige.magic.definition.SpellDefinition(id("staff_chaos_fixture"),
                Set.of(com.quzzar.vestige.magic.definition.Tradition.ARCANE),com.quzzar.vestige.magic.definition.TraitProfile.empty(),List.of(),
                List.of(new com.quzzar.vestige.magic.definition.SpellTrigger(id("primary"),id("interact"),List.of())),
                List.of(new com.quzzar.vestige.magic.effect.SpellEffects.GrantTraits(id("staff_test_volatile"),
                        List.of(new com.quzzar.vestige.magic.definition.TraitModifier(id("volatile"),com.quzzar.vestige.magic.definition.TraitModifier.Operation.ADD,20)),
                        new com.quzzar.vestige.magic.expression.SpellValue.Constant(40),com.quzzar.vestige.magic.definition.TargetSpec.self())));
        session.runtime().cast(boost,com.quzzar.vestige.magic.runtime.SpellEvent.of(id("interact"),player.getUUID(),null),List.of(),true);
    }
    @GameTest(template="empty_9x3x9",batch="staff_chaos_wear")
    public static void paidVolatileForfeitUsesNativeRarityWearAndFailedPaymentUsesNone(GameTestHelper h) {
        var caster=player(h,"abjuration","abyssal_shroud",true);certainChaos(caster);NativeMana.set(caster,0);
        h.assertTrue(!StaffCasting.cast(caster,InteractionHand.MAIN_HAND) && caster.getMainHandItem().getDamageValue()==0,"Unpaid chaotic attempt spent wear");
        NativeMana.set(caster,100);
        h.assertTrue(StaffCasting.cast(caster,InteractionHand.MAIN_HAND) && caster.getMainHandItem().getDamageValue()==4
                && NativeMana.amount(caster)==28,"Paid mythic forfeit skipped or duplicated wear/payment");
        h.assertTrue(caster.hasEffect(net.minecraft.world.effect.MobEffects.WEAKNESS)
                || caster.hasEffect(net.minecraft.world.effect.MobEffects.CONFUSION)
                || caster.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN),"Guaranteed volatile cast did not produce the real chaos outcome");h.succeed();
    }
    @GameTest(template="empty_9x3x9",batch="staff_chaos_last")
    public static void mythicChaosBreaksTheReservedOffhandStaffWithLessThanFourRemaining(GameTestHelper h) {
        var caster=player(h,"abjuration","abyssal_shroud",true);var spare=caster.getMainHandItem().copy();caster.getInventory().setItem(5,spare);
        var last=spare.copy();last.setDamageValue(38);caster.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        caster.setItemInHand(InteractionHand.OFF_HAND,last);certainChaos(caster);
        h.assertTrue(StaffCasting.cast(caster,InteractionHand.OFF_HAND) && caster.getOffhandItem().isEmpty()
                && caster.getInventory().getItem(5).getDamageValue()==0 && NativeMana.amount(caster)==28,"Final chaotic mythic cast failed, kept the staff or damaged a spare");h.succeed();
    }
    @GameTest(template="empty_9x3x9",batch="staff_failed")
    public static void insufficientManaKeepsDurabilityAndUnknownCastsNeverIdentify(GameTestHelper h) {
        var caster=player(h,"abjuration","pf2_shield",false);NativeMana.set(caster,0);
        h.assertTrue(!StaffCasting.cast(caster,InteractionHand.MAIN_HAND) && caster.getMainHandItem().getDamageValue()==0,"Failed payment spent wear");
        NativeMana.set(caster,100);h.assertTrue(StaffCasting.cast(caster,InteractionHand.MAIN_HAND),"Affordable unknown cast rejected");
        h.assertTrue(NativeMana.amount(caster)==94 && caster.getMainHandItem().getDamageValue()==1 && !SpellKnowledge.identified(caster,id("pf2_shield")),"Unknown staff skipped payment or identified");h.succeed();
    }
    @GameTest(template="empty_9x3x9",batch="staff_charge",timeoutTicks=85)
    public static void changingSelectionDuringPreparationCancelsWithoutPayment(GameTestHelper h) {
        var caster=player(h,"holy","heal",true);caster.setHealth(10);
        h.assertTrue(StaffCasting.cast(caster,InteractionHand.MAIN_HAND),"Preparation rejected");
        h.runAfterDelay(5,() -> caster.setItemInHand(InteractionHand.MAIN_HAND,StaffData.select(caster.getMainHandItem(),1)));
        h.runAfterDelay(30,() -> {
            h.assertTrue(NativeMana.amount(caster)==100 && caster.getMainHandItem().getDamageValue()==0 && caster.getHealth()==10,"Canceled preparation committed");
            caster.setItemInHand(InteractionHand.MAIN_HAND,StaffData.select(caster.getMainHandItem(),0));h.assertTrue(StaffCasting.cast(caster,InteractionHand.MAIN_HAND),"Canceled cast acquired recovery");
        });
        h.runAfterDelay(55,() -> {h.assertTrue(NativeMana.amount(caster)==85 && caster.getMainHandItem().getDamageValue()==1 && caster.getHealth()==15,"Retry did not pay/heal once");h.succeed();});
    }
    @GameTest(template="empty_9x3x9",batch="staff_last",timeoutTicks=90)
    public static void finalOffhandUseBreaksOnlyReservedStaff(GameTestHelper h) {
        var caster=player(h,"abjuration","pf2_shield",true);var spare=caster.getMainHandItem().copy();caster.getInventory().setItem(5,spare);
        caster.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);var last=spare.copy();last.setDamageValue(39);caster.setItemInHand(InteractionHand.OFF_HAND,last);
        h.assertTrue(StaffCasting.cast(caster,InteractionHand.OFF_HAND) && caster.getOffhandItem().isEmpty() && caster.getInventory().getItem(5).getDamageValue()==0 && NativeMana.amount(caster)==94,"Last offhand use wore a spare or failed");h.succeed();
    }
    @GameTest(template="empty_9x3x9",batch="staff_recast",timeoutTicks=90)
    public static void brokenStaffContinuationKeepsOriginalPaidSpellAndCannotIdentifyViaScroll(GameTestHelper h) {
        var caster=player(h,"conjuration","summon_horse",true);var last=caster.getMainHandItem();last.setDamageValue(39);
        h.assertTrue(StaffCasting.cast(caster,InteractionHand.MAIN_HAND),"Summon preparation rejected");
        h.runAfterDelay(30,() -> {
            h.assertTrue(caster.getMainHandItem().isEmpty() && NativeMana.amount(caster)==76 && StaffCasting.awaiting(caster),"Last use lost continuation");
            var scroll=ScrollItems.scroll(id("summon_horse"));caster.setItemInHand(InteractionHand.MAIN_HAND,scroll);
            h.assertTrue(!ScrollCasting.cast(caster,scroll) && scroll.getCount()==1,"Scroll took over staff continuation");
            caster.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);h.assertTrue(StaffCasting.recast(caster) && NativeMana.amount(caster)==76,"Recast repaid or failed after staff broke");h.succeed();
        });
    }
    @GameTest(template="empty_9x3x9",batch="staff_save")
    public static void sourceVariantSelectionWearAndMissingSpellIdentitySurviveSaving(GameTestHelper h) {
        var original=StaffData.select(StaffData.expand(staff("fire","fireball")),3);original.setDamageValue(9);
        CustomData.update(DataComponents.CUSTOM_DATA,original,t -> t.getList("slots",Tag.TAG_COMPOUND)
                .getCompound(0).putString("vestige_spell","vestige:removed_spell"));
        var restored=ItemStack.parse(h.getLevel().registryAccess(),original.save(h.getLevel().registryAccess())).orElseThrow();
        h.assertTrue(ItemStack.matches(original,restored) && StaffData.binding(original).equals(StaffData.binding(restored)),"Save changed staff");
        var caster=player(h,"fire","firebolt",true);caster.setItemInHand(InteractionHand.MAIN_HAND,StaffData.select(restored,0));
        h.assertTrue(!StaffCasting.cast(caster,InteractionHand.MAIN_HAND) && NativeMana.amount(caster)==100
                && caster.getMainHandItem().getDamageValue()==9,"Unavailable saved spell paid or cast");
        h.assertTrue(StaffData.binding(StaffData.expand(restored)).orElseThrow().slots().get(0).orElseThrow()
                .spell().equals(id("removed_spell")),"Expansion deleted unavailable spell");h.succeed();
    }
    private static final class SelectionWire extends net.minecraft.network.Connection {
        private final List<net.minecraft.network.protocol.Packet<?>> packets=new ArrayList<>();
        private SelectionWire() { super(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);new io.netty.channel.embedded.EmbeddedChannel(this); }
        @Override public void send(net.minecraft.network.protocol.Packet<?> packet,net.minecraft.network.PacketSendListener listener,boolean flush) { packets.add(packet); }
    }
    @GameTest(template="empty_9x3x9",batch="staff_selection")
    public static void serverSelectionRejectsForgedStaleAndReplayedMenus(GameTestHelper h) {
        var caster=new net.minecraft.server.level.ServerPlayer(h.getLevel().getServer(),h.getLevel(),
                new GameProfile(UUID.randomUUID(),"staff-menu-fixture"),net.minecraft.server.level.ClientInformation.createDefault());
        var wire=new SelectionWire();
        net.neoforged.neoforge.network.registration.ChannelAttributes.getOrCreateCommonChannels(wire,net.minecraft.network.ConnectionProtocol.PLAY)
                .add(net.neoforged.neoforge.network.payload.AdvancedOpenScreenPayload.TYPE.id());
        caster.connection=new net.minecraft.server.network.ServerGamePacketListenerImpl(caster.server,wire,caster,
                net.minecraft.server.network.CommonListenerCookie.createInitial(caster.getGameProfile(),false));
        caster.setItemInHand(InteractionHand.MAIN_HAND,staff("fire","fireball"));
        SpellKnowledge.identify(caster,id("firebolt"));
        var opened=StaffSelection.open(caster,InteractionHand.MAIN_HAND).orElseThrow();
        var menu=(StaffMenu)caster.containerMenu;
        h.assertTrue(menu.containerId==opened && !wire.packets.isEmpty(),"Opening did not send the native menu");
        caster.connection.handleContainerButtonClick(new net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket(opened+1,1));
        h.assertTrue(StaffData.binding(caster.getMainHandItem()).orElseThrow().selected()==0,"Forged container ID selected");
        caster.connection.handleContainerButtonClick(new net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket(opened,1));
        h.assertTrue(StaffData.binding(caster.getMainHandItem()).orElseThrow().selected()==0,"Empty slot selected");
        menu.setCarried(ScrollItems.scroll(id("firebolt")));menu.clicked(1,0,net.minecraft.world.inventory.ClickType.PICKUP,caster);
        h.assertTrue(menu.clickMenuButton(caster,1) && StaffData.binding(caster.getMainHandItem()).orElseThrow().selected()==1,"Valid selection failed");
        h.assertTrue(!menu.clickMenuButton(caster,6),"Out-of-capacity slot selected");
        caster.closeContainer();caster.connection.handleContainerButtonClick(new net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket(opened,0));
        h.assertTrue(StaffData.binding(caster.getMainHandItem()).orElseThrow().selected()==1,"Closed menu replay selected");
        h.succeed();
    }
}
