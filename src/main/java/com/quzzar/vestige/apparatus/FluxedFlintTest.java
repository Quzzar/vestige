package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.world.NativeMagic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.gametest.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@GameTestHolder(VestigeMainMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FluxedFlintTest {
    private static final BlockPos CENTER=new BlockPos(4,1,4);
    private static final LeylineShaping.Geometry GEOMETRY=new LeylineShaping.Geometry(8,LeylineShaping.Shape.CROSS,2,0,LeylineShaping.Shape.DIAGONAL,3,0);
    private static OfferingBlockEntity structure(GameTestHelper h,boolean outer) {
        h.setBlock(CENTER,ApparatusBlocks.SPELLSTONE.get());
        for (int i=0;i<8;i++) h.setBlock(CENTER.offset(GEOMETRY.offset(i)),(i&1)==0 || outer ? ApparatusBlocks.PLINTH.get() : Blocks.AIR);
        return (OfferingBlockEntity)h.getBlockEntity(CENTER);
    }
    private static net.minecraft.world.entity.player.Player player(GameTestHelper h) { return h.makeMockPlayer(GameType.SURVIVAL); }
    private static ItemStack flint(int damage) { var stack=new ItemStack(ScrollItems.FLUXED_FLINT.get());stack.setDamageValue(damage);return stack; }
    private static ItemStack staff() {
        var spell=NativeMagic.spells().spells().get(VestigeMainMod.location("fireball"));
        var source=ScrollItems.shapedScroll(spell.id(),new LeylineShaping.Modifiers(1.1,1.2,1.3,1.1));
        var stack=StaffData.bind(StaffData.create(VestigeMainMod.location("fire")),source,spell);
        stack=StaffData.expand(stack);stack=StaffData.select(stack,3);stack.setDamageValue(39);
        stack.set(DataComponents.CUSTOM_NAME,Component.literal("A specific staff"));
        stack.set(DataComponents.CONTAINER,ItemContainerContents.fromItems(List.of(new ItemStack(Items.DIAMOND,3),source)));
        CustomData.update(DataComponents.CUSTOM_DATA,stack,tag -> {tag.putUUID("test_owner",UUID.fromString("00000000-0000-0000-0000-000000000017"));tag.putString("test_arbitrary","keep everything");});
        return stack;
    }
    private static RitualCrafting.Layout offer(GameTestHelper h,ItemStack target,ItemStack catalyst) {
        var layout=LeylineStructure.find(structure(h,true),4).getFirst();
        layout.stands().get(0).insert(catalyst);layout.stands().get(4).insert(target);
        return layout;
    }
    @GameTest(template="empty_9x3x9",batch="fluxed_flint",timeoutTicks=90)
    public static void fractiousCombinesAllThreeSocketsThroughRotatedConstruction(GameTestHelper h) {
        var center=structure(h,true);var layout=LeylineStructure.find(center,4).getFirst();
        for(int i=0;i<4;i++) layout.stands().get((i*2+2)%8).insert(new ItemStack(FluxedFlintRecipe.ingredients().get(i)));
        layout.stands().get(2).installMaterial(new ItemStack(Items.MAGMA_BLOCK));
        layout.stands().get(0).installMaterial(new ItemStack(Items.IRON_BLOCK));
        layout.stands().get(6).installMaterial(new ItemStack(Items.QUARTZ_BLOCK));
        var outer=(OfferingBlockEntity)h.getBlockEntity(CENTER.offset(GEOMETRY.offset(1)));outer.insert(new ItemStack(Items.PAPER));
        h.assertTrue(RitualCrafting.activate(player(h),center,()->{throw new AssertionError("Construction ingredients rolled");})==RitualCrafting.Outcome.CRAFTING,"Three-choice construction rejected");
        h.runAfterDelay(45,()->{
            h.assertTrue(ItemStack.matches(FluxedFlintImbuements.create(FluxedFlintImbuements.variants().get(7)),RitualTestOutput.stack(center)),"Three choices did not compose");
            h.assertTrue(layout.items().stream().allMatch(ItemStack::isEmpty) && outer.displayedItem().is(Items.PAPER),"Wrong layer consumed");
            h.assertTrue(layout.stands().get(2).materialItem().is(Items.MAGMA_BLOCK) && layout.stands().get(0).materialItem().is(Items.IRON_BLOCK) && layout.stands().get(6).materialItem().is(Items.QUARTZ_BLOCK),"Construction consumed selectors");h.succeed();
        });
    }
    @GameTest(template="empty_9x3x9",batch="fluxed_flint",timeoutTicks=90)
    public static void fractiousRepairTransfersFortyPointsAndPreservesCompleteSavedComponents(GameTestHelper h) {
        var catalyst=FluxedFlintImbuements.create(FluxedFlintImbuements.variants().get(4));catalyst.setDamageValue(5);
        CustomData.update(DataComponents.CUSTOM_DATA,catalyst,tag -> tag.putString("unrelated","keep"));
        catalyst.set(DataComponents.CONTAINER,ItemContainerContents.fromItems(List.of(new ItemStack(Items.DIAMOND,2))));
        catalyst=ItemStack.parse(h.getLevel().registryAccess(),catalyst.save(h.getLevel().registryAccess())).orElseThrow();
        var before=catalyst.copy();var target=staff();target.setDamageValue(59);var layout=offer(h,target,catalyst);
        h.assertTrue(RitualCrafting.activate(player(h),layout.center(),()->.21)==RitualCrafting.Outcome.CRAFTING,"Valid Fractious repair rejected");
        h.runAfterDelay(45,()->{
            var expected=target.copy();expected.setDamageValue(19);var worn=before.copy();worn.setDamageValue(45);
            h.assertTrue(ItemStack.matches(expected,RitualTestOutput.stack(layout.center())),"Target did not preserve components with forty-point repair");
            h.assertTrue(ItemStack.matches(worn,layout.stands().get(0).displayedItem()),"Catalyst did not spend exactly forty points");
            var saved=ItemStack.parse(h.getLevel().registryAccess(),worn.save(h.getLevel().registryAccess())).orElseThrow();
            h.assertTrue(ItemStack.matches(worn,saved) && FluxedFlintImbuements.read(saved).orElseThrow().repairFraction()==.5 && saved.getMaxDamage()==128,"Reload changed Fractious budget or transfer cap");h.succeed();
        });
    }
    @GameTest(template="empty_9x3x9",batch="fluxed_flint",timeoutTicks=90)
    public static void fractiousBackfireUsesHigherRiskAndPreservesExactTriggerWithoutWear(GameTestHelper h) {
        var catalyst=FluxedFlintImbuements.create(FluxedFlintImbuements.variants().get(4));catalyst.setDamageValue(17);
        CustomData.update(DataComponents.CUSTOM_DATA,catalyst,tag -> tag.putString("unrelated","keep"));
        var layout=offer(h,staff(),catalyst);
        // Fifteen percent passes ordinary Flint's roll and fails Fractious Flint's roll.
        h.assertTrue(RitualCrafting.activate(player(h),layout.center(),()->.15)==RitualCrafting.Outcome.EXPLOSION_PENDING,"Fractious did not increase risk");
        h.runAfterDelay(35,()->{
            h.assertTrue(ItemStack.matches(catalyst,layout.stands().get(0).displayedItem()),"Backfire damaged or reconstructed its trigger");
            h.assertTrue(layout.stands().get(4).displayedItem().isEmpty() && RitualTestOutput.stack(layout.center()).isEmpty(),"Backfire kept target or produced a repair");
            h.assertTrue(h.getBlockEntity(CENTER)==layout.center() && !layout.center().busy(),"Backfire damaged apparatus or leaked reservation");h.succeed();
        });
    }
    @GameTest(template="empty_9x3x9",batch="fluxed_flint")
    public static void allEightVariantsRoundCapsAndLimitTransfersToDamageAndRemainingBudget(GameTestHelper h) {
        var seats=new ArrayList<ItemStack>(Collections.nCopies(8,ItemStack.EMPTY));
        for(int i=0;i<8;i++) {
            var variant=FluxedFlintImbuements.variants().get(i);var target=staff();target.set(DataComponents.MAX_DAMAGE,81);target.setDamageValue(59);
            var catalyst=FluxedFlintImbuements.create(variant);seats.set(7,target);seats.set(1,catalyst);
            var repair=FluxedFlintRecipe.repair(seats).orElseThrow();int restored=i<4 ? 21 : 41;
            h.assertTrue(repair.output().getDamageValue()==59-restored && repair.remainingCatalyst().getDamageValue()==restored,"Variant cap rounding or one-to-one wear failed");
            target.setDamageValue(3);repair=FluxedFlintRecipe.repair(seats).orElseThrow();
            h.assertTrue(repair.output().getDamageValue()==0 && repair.remainingCatalyst().getDamageValue()==3,"Repair overspent missing damage");
            target.setDamageValue(59);catalyst.setDamageValue(variant.durability()-7);repair=FluxedFlintRecipe.repair(seats).orElseThrow();
            h.assertTrue(repair.output().getDamageValue()==52 && repair.remainingCatalyst().isEmpty(),"Repair overspent remaining Flint budget");
            h.assertTrue(target.getDamageValue()==59 && catalyst.getDamageValue()==variant.durability()-7,"Planning mutated offered inputs");
        }
        h.succeed();
    }
    @GameTest(template="empty_9x3x9",batch="fluxed_flint",timeoutTicks=90)
    public static void combinedImbuementsCraftWithEitherIngotAndRetainSocketsAndInactiveOuterItems(GameTestHelper h) {
        var center=structure(h,true);var layout=LeylineStructure.find(center,4).getFirst();
        for(int i=0;i<4;i++) layout.stands().get((i*2+2)%8).insert(new ItemStack(FluxedFlintRecipe.ingredients().get(i)));
        layout.stands().get(0).installMaterial(new ItemStack(Items.IRON_BLOCK));
        layout.stands().get(6).installMaterial(new ItemStack(Items.QUARTZ_BLOCK));
        var outer=(OfferingBlockEntity)h.getBlockEntity(CENTER.offset(GEOMETRY.offset(1)));outer.insert(new ItemStack(Items.PAPER));
        h.assertTrue(RitualCrafting.activate(player(h),center,()->{throw new AssertionError("Construction ingredients rolled");})==RitualCrafting.Outcome.CRAFTING,"Imbued construction rejected");
        h.runAfterDelay(45,()->{
            var expected=FluxedFlintImbuements.create(FluxedFlintImbuements.variants().get(3));
            h.assertTrue(ItemStack.matches(expected,RitualTestOutput.stack(center)),"Combined construction did not preserve both choices");
            h.assertTrue(layout.items().stream().allMatch(ItemStack::isEmpty) && outer.displayedItem().is(Items.PAPER),"Wrong layer consumed");
            h.assertTrue(layout.stands().get(0).materialItem().is(Items.IRON_BLOCK) && layout.stands().get(6).materialItem().is(Items.QUARTZ_BLOCK),"Sockets consumed");h.succeed();
        });
    }
    @GameTest(template="empty_9x3x9",batch="fluxed_flint")
    public static void allImbuedRiskBoundariesUseSharedIndependentVolatilityAndProtectTheTrigger(GameTestHelper h) {
        var center=structure(h,true);var layout=LeylineStructure.find(center,8).getFirst();
        double[] chances={.1,.05,.15,.075,.2,.1,.3,.15};
        for(int i=0;i<8;i++) {
            double chance=chances[i];var catalyst=FluxedFlintImbuements.create(FluxedFlintImbuements.variants().get(i));
            layout.stands().get(1).insert(catalyst);layout.stands().get(7).insert(staff());var inputs=RitualInputs.capture(layout);
            h.assertTrue(RitualVolatility.culprit(ItemStack.EMPTY,inputs,List.of(7,1),0,()->Math.nextUp(chance)).isEmpty(),"Above variant boundary backfired");
            h.assertTrue(RitualVolatility.culprit(ItemStack.EMPTY,inputs,List.of(7,1),0,()->Math.nextDown(chance)).orElseThrow()==1,"Below variant boundary did not trigger");
            layout.stands().get(1).remove();layout.stands().get(7).remove();
        }
        h.succeed();
    }
    @GameTest(template="empty_9x3x9",batch="fluxed_flint",timeoutTicks=90)
    public static void stabilizedRepairPreservesAllComponentsAndSelectionsThroughSaveAndReload(GameTestHelper h) {
        var catalyst=FluxedFlintImbuements.create(FluxedFlintImbuements.variants().get(1));catalyst.setDamageValue(5);
        CustomData.update(DataComponents.CUSTOM_DATA,catalyst,tag -> tag.putString("unrelated","keep"));
        catalyst.set(DataComponents.CONTAINER,ItemContainerContents.fromItems(List.of(new ItemStack(Items.DIAMOND,2))));
        catalyst=ItemStack.parse(h.getLevel().registryAccess(),catalyst.save(h.getLevel().registryAccess())).orElseThrow();
        var originalCatalyst=catalyst.copy();var originalTarget=staff();var layout=offer(h,originalTarget,catalyst);
        // Six percent succeeds only after stabilization; the ordinary Flint would backfire.
        h.assertTrue(RitualCrafting.activate(player(h),layout.center(),()->.06)==RitualCrafting.Outcome.CRAFTING,"Stabilized risk not used by activation");
        h.runAfterDelay(45,()->{
            var expected=originalTarget.copy();expected.setDamageValue(19);var worn=originalCatalyst.copy();worn.setDamageValue(25);
            h.assertTrue(ItemStack.matches(expected,RitualTestOutput.stack(layout.center())),"Target components changed");
            h.assertTrue(ItemStack.matches(worn,layout.stands().get(0).displayedItem()),"Remainder lost selections or unrelated data");
            var reloaded=ItemStack.parse(h.getLevel().registryAccess(),worn.save(h.getLevel().registryAccess())).orElseThrow();
            h.assertTrue(ItemStack.matches(worn,reloaded) && FluxedFlintImbuements.read(reloaded).orElseThrow().durability()==96,"Reload lost budget or variant");h.succeed();
        });
    }
    @GameTest(template="empty_9x3x9",batch="fluxed_flint",timeoutTicks=90)
    public static void stabilizedBackfireStillPreservesExactFlintAndDestroysTheTarget(GameTestHelper h) {
        var catalyst=FluxedFlintImbuements.create(FluxedFlintImbuements.variants().get(1));catalyst.setDamageValue(17);var layout=offer(h,staff(),catalyst);
        h.assertTrue(RitualCrafting.activate(player(h),layout.center(),()->.04)==RitualCrafting.Outcome.EXPLOSION_PENDING,"Stabilization removed risk entirely");
        h.runAfterDelay(25,()->{
            h.assertTrue(ItemStack.matches(catalyst,layout.stands().get(0).displayedItem()),"Backfire changed catalyst or selections");
            h.assertTrue(layout.stands().get(4).displayedItem().isEmpty() && RitualTestOutput.stack(layout.center()).isEmpty(),"Backfire retained target or produced output");h.succeed();
        });
    }
    @GameTest(template="empty_9x3x9",batch="fluxed_flint",timeoutTicks=90)
    public static void changingCatalystSelectionCancelsWithoutWearOrTargetConsumption(GameTestHelper h) {
        var catalyst=FluxedFlintImbuements.create(FluxedFlintImbuements.variants().get(1));catalyst.setDamageValue(3);var target=staff();var layout=offer(h,target,catalyst);
        RitualCrafting.activate(player(h),layout.center(),()->1);
        var replacement=FluxedFlintImbuements.create(FluxedFlintImbuements.variants().get(3));replacement.setDamageValue(3);
        h.runAfterDelay(5,()->{layout.stands().get(0).remove();layout.stands().get(0).insert(replacement);});
        h.runAfterDelay(45,()->{h.assertTrue(ItemStack.matches(replacement,layout.stands().get(0).displayedItem()) && ItemStack.matches(target,layout.stands().get(4).displayedItem()),"Canceled change partially paid");h.assertTrue(RitualTestOutput.stack(layout.center()).isEmpty() && !layout.center().busy(),"Canceled change produced output or leaked reservation");h.succeed();});
    }
    @GameTest(template="empty_9x3x9",batch="fluxed_flint",timeoutTicks=90)
    public static void changingConstructionSocketCancelsBeforeConsumingAnyOffering(GameTestHelper h) {
        var center=structure(h,false);var layout=RitualCrafting.layout(center);
        for(int i=0;i<4;i++) layout.stands().get(i*2).insert(new ItemStack(FluxedFlintRecipe.ingredients().get(i)));
        layout.stands().get(4).installMaterial(new ItemStack(Items.QUARTZ_BLOCK));var before=layout.items().stream().map(ItemStack::copy).toList();
        RitualCrafting.activate(player(h),center,()->1);
        h.runAfterDelay(5,()->{var node=layout.stands().get(4);node.unlock();node.removeMaterial();});
        h.runAfterDelay(45,()->{h.assertTrue(java.util.stream.IntStream.range(0,8).allMatch(i->ItemStack.matches(before.get(i),layout.items().get(i))),"Socket edit consumed offerings");h.assertTrue(RitualTestOutput.stack(center).isEmpty() && !center.busy(),"Socket edit produced output or leaked reservation");h.succeed();});
    }
    @GameTest(template="empty_9x3x9",batch="fluxed_flint")
    public static void everyVariantSpendsItsLastPointAndRejectsForgedOrDuplicateImbuementsFreely(GameTestHelper h) {
        var seats=new ArrayList<ItemStack>(Collections.nCopies(8,ItemStack.EMPTY));seats.set(7,staff());
        for(var variant:FluxedFlintImbuements.variants()) {
            var catalyst=FluxedFlintImbuements.create(variant);catalyst.setDamageValue(variant.durability()-1);seats.set(1,catalyst);
            var repair=FluxedFlintRecipe.repair(seats).orElseThrow();h.assertTrue(repair.output().getDamageValue()==38 && repair.remainingCatalyst().isEmpty(),"Last variant point overpaid or survived");
            catalyst.set(DataComponents.MAX_DAMAGE,1000);h.assertTrue(FluxedFlintRecipe.repair(seats).isEmpty(),"Forged budget accepted");
        }
        var center=structure(h,false);var layout=RitualCrafting.layout(center);
        for(int i=0;i<4;i++) layout.stands().get(i*2).insert(new ItemStack(FluxedFlintRecipe.ingredients().get(i)));
        layout.stands().get(2).installMaterial(new ItemStack(Items.IRON_BLOCK));layout.stands().get(6).installMaterial(new ItemStack(Items.IRON_BLOCK));
        h.assertTrue(RitualCrafting.activate(player(h),center,()->{throw new AssertionError("Invalid construction rolled");})==RitualCrafting.Outcome.INVALID,"Duplicate adjustment accepted");
        h.assertTrue(layout.items().stream().filter(s->!s.isEmpty()).count()==4 && !center.busy(),"Rejected construction spent or reserved inputs");h.succeed();
    }
    @GameTest(template="empty_9x3x9",batch="fluxed_flint",timeoutTicks=90)
    public static void expensiveConstructionConsumesFourOfferingsAndProducesOneFlint(GameTestHelper h) {
        var center=structure(h,false);var layout=RitualCrafting.layout(center);
        for (int i=0;i<4;i++) layout.stands().get(i*2).insert(new ItemStack(FluxedFlintRecipe.ingredients().get(i)));
        h.assertTrue(RitualCrafting.activate(player(h),center,()->{throw new AssertionError("Nonvolatile ingredients rolled");})==RitualCrafting.Outcome.CRAFTING,"Construction rejected");
        h.runAfterDelay(45,()->{var out=RitualTestOutput.stack(center);h.assertTrue(out.is(ScrollItems.FLUXED_FLINT.get()) && out.getCount()==1 && out.getDamageValue()==0,"Wrong catalyst output");h.assertTrue(layout.items().stream().allMatch(ItemStack::isEmpty),"Construction inputs survived");h.succeed();});
    }
    @GameTest(template="empty_9x3x9",batch="fluxed_flint",timeoutTicks=150)
    public static void diamondIntermediateThenRevisedFlintCraftThroughTheAtomicLifecycle(GameTestHelper h) {
        var center=structure(h,true);var layout=LeylineStructure.find(center,4).getFirst();
        for(int i=0;i<4;i++) layout.stands().get((i*2+2)%8).insert(new ItemStack(DissentientDiamondRecipe.ingredients().get(i)));
        var outer=(OfferingBlockEntity)h.getBlockEntity(CENTER.offset(GEOMETRY.offset(1)));outer.insert(new ItemStack(Items.PAPER));
        layout.stands().get(0).installMaterial(new ItemStack(Items.AMETHYST_BLOCK));
        h.assertTrue(RitualCrafting.activate(player(h),center,()->{throw new AssertionError("Ordinary ingredients rolled risk");})==RitualCrafting.Outcome.CRAFTING,"Diamond construction rejected");
        h.runAfterDelay(65,()->{
            var diamond=RitualTestOutput.take(center);
            h.assertTrue(diamond.is(ScrollItems.DISSENTIENT_DIAMOND.get()) && diamond.getCount()==1 && !diamond.hasFoil(),"Wrong intermediate output");
            h.assertTrue(layout.items().stream().allMatch(ItemStack::isEmpty) && outer.displayedItem().is(Items.PAPER)
                    && layout.stands().get(0).materialItem().is(Items.AMETHYST_BLOCK),"Intermediate consumed inactive offerings or socket");
            layout.stands().get(2).insert(new ItemStack(Items.FLINT));layout.stands().get(4).insert(new ItemStack(Items.NETHERITE_INGOT));
            layout.stands().get(6).insert(diamond);layout.stands().get(0).insert(new ItemStack(Items.NETHERITE_INGOT));
            h.assertTrue(RitualCrafting.activate(player(h),center,()->{throw new AssertionError("Intermediate acquired an unauthored volatile trait");})==RitualCrafting.Outcome.CRAFTING,"Revised Flint construction rejected");
        });
        h.runAfterDelay(115,()->{
            var result=RitualTestOutput.stack(center);
            h.assertTrue(result.is(ScrollItems.FLUXED_FLINT.get()) && result.getCount()==1 && result.getDamageValue()==0 && result.getMaxDamage()==128,"Wrong catalyst budget or output");
            h.assertTrue(layout.items().stream().allMatch(ItemStack::isEmpty) && outer.displayedItem().is(Items.PAPER)
                    && layout.stands().get(0).materialItem().is(Items.AMETHYST_BLOCK),"Flint construction failed atomic consumption or touched inactive inputs");h.succeed();
        });
    }
    @GameTest(template="empty_9x3x9",batch="fluxed_flint",timeoutTicks=90)
    public static void successClonesAllTargetAndCatalystComponentsChangingOnlyDamage(GameTestHelper h) {
        var target=staff();var catalyst=flint(5);catalyst.set(DataComponents.CUSTOM_NAME,Component.literal("My Flint"));
        target.enchant(h.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                .getOrThrow(net.minecraft.world.item.enchantment.Enchantments.UNBREAKING),3);
        var layout=offer(h,target,catalyst);layout.stands().get(0).installMaterial(new ItemStack(Items.DIAMOND_BLOCK));
        h.assertTrue(RitualCrafting.activate(player(h),layout.center(),()->1)==RitualCrafting.Outcome.CRAFTING,"Repair rejected");
        h.runAfterDelay(45,()->{
            var expected=target.copy();expected.setDamageValue(19);var worn=catalyst.copy();worn.setDamageValue(25);
            h.assertTrue(ItemStack.matches(expected,RitualTestOutput.stack(layout.center())),"Output lost metadata or restored wrong damage");
            h.assertTrue(ItemStack.matches(worn,layout.stands().get(0).displayedItem()),"Catalyst changed beyond deterministic wear");
            h.assertTrue(layout.stands().get(4).displayedItem().isEmpty(),"Target duplicated on its Plinth");
            h.assertTrue(layout.stands().get(0).materialItem().is(Items.DIAMOND_BLOCK),"Socket consumed");
            h.succeed();
        });
    }
    @GameTest(template="empty_9x3x9",batch="fluxed_flint",timeoutTicks=90)
    public static void finalBudgetPointRestoresOneAndDestroysExhaustedFlint(GameTestHelper h) {
        var target=staff();var layout=offer(h,target,flint(127));
        h.assertTrue(RitualCrafting.activate(player(h),layout.center(),()->1)==RitualCrafting.Outcome.CRAFTING,"Final use rejected");
        h.runAfterDelay(45,()->{var expected=target.copy();expected.setDamageValue(38);h.assertTrue(ItemStack.matches(expected,RitualTestOutput.stack(layout.center())),"Final point over-restored or lost data");h.assertTrue(layout.items().stream().allMatch(ItemStack::isEmpty),"Exhausted catalyst survived");h.succeed();});
    }
    @GameTest(template="empty_9x3x9",batch="fluxed_flint",timeoutTicks=90)
    public static void backfirePreservesExactTriggerAndDestroysOnlyOtherParticipatingInputs(GameTestHelper h) {
        var catalyst=flint(17);catalyst.set(DataComponents.CUSTOM_NAME,Component.literal("Survivor"));var layout=offer(h,staff(),catalyst);
        h.assertTrue(RitualCrafting.activate(player(h),layout.center(),()->0)==RitualCrafting.Outcome.EXPLOSION_PENDING,"Volatility did not trigger");
        h.runAfterDelay(25,()->{h.assertTrue(ItemStack.matches(catalyst,layout.stands().get(0).displayedItem()),"Trigger changed or was consumed");h.assertTrue(layout.stands().get(4).displayedItem().isEmpty() && RitualTestOutput.stack(layout.center()).isEmpty(),"Failure repaired or retained target");h.assertTrue(h.getBlockState(CENTER).is(ApparatusBlocks.SPELLSTONE.get()),"Terrain damaged");h.succeed();});
    }
    @GameTest(template="empty_9x3x9",batch="fluxed_flint",timeoutTicks=90)
    public static void changedTargetComponentsCancelWithoutCatalystWear(GameTestHelper h) {
        var catalyst=flint(3);var layout=offer(h,staff(),catalyst);RitualCrafting.activate(player(h),layout.center(),()->1);
        h.runAfterDelay(5,()->{var changed=layout.stands().get(4).remove();changed.set(DataComponents.CUSTOM_NAME,Component.literal("Changed during ritual"));layout.stands().get(4).insert(changed);});
        h.runAfterDelay(45,()->{h.assertTrue(ItemStack.matches(catalyst,layout.stands().get(0).displayedItem()),"Canceled repair spent catalyst");h.assertTrue(!layout.stands().get(4).displayedItem().isEmpty() && RitualTestOutput.stack(layout.center()).isEmpty(),"Canceled repair consumed or duplicated target");h.assertTrue(!layout.center().busy(),"Reservation leaked");h.succeed();});
    }
    private static void outputCallback(GameTestHelper h,boolean mutate) {
        var target=staff();var catalyst=flint(3);var layout=offer(h,target,catalyst);
        Consumer<EntityJoinLevelEvent> listener=event->{
            if (event.getLevel()==h.getLevel() && event.getEntity() instanceof net.minecraft.world.entity.item.ItemEntity item
                    && item.getX()==layout.center().getBlockPos().getX()+.5 && item.getZ()==layout.center().getBlockPos().getZ()+.5) {
                if (!mutate) event.setCanceled(true);
                else {var changed=layout.stands().get(0).remove();changed.setDamageValue(4);layout.stands().get(0).insert(changed);}
            }
        };
        NeoForge.EVENT_BUS.addListener(listener);RitualCrafting.activate(player(h),layout.center(),()->1);
        h.runAfterDelay(45,()->{NeoForge.EVENT_BUS.unregister(listener);h.assertTrue(ItemStack.matches(target,layout.stands().get(4).displayedItem()),"Spawn callback consumed target");h.assertTrue(layout.stands().get(0).displayedItem().getDamageValue()==(mutate?4:3),"Spawn callback partially paid");h.assertTrue(RitualTestOutput.stack(layout.center()).isEmpty(),"Rejected output remained");h.succeed();});
    }
    @GameTest(template="empty_9x3x9",batch="fluxed_flint",timeoutTicks=90)
    public static void rejectedOutputSpawnChargesNeitherInput(GameTestHelper h) { outputCallback(h,false); }
    @GameTest(template="empty_9x3x9",batch="fluxed_flint",timeoutTicks=90)
    public static void outputCallbackMutationRevalidatesBeforeAnyPayment(GameTestHelper h) { outputCallback(h,true); }
    @GameTest(template="empty_9x3x9",batch="fluxed_flint")
    public static void invalidRepairsRejectFreelyAndPartialRepairSpendsOnlyActualRestoration(GameTestHelper h) {
        var seats=new ArrayList<>(Collections.nCopies(8,ItemStack.EMPTY));seats.set(0,flint(0));
        for (var invalid:List.of(new ItemStack(Items.DIAMOND_SWORD),new ItemStack(ScrollItems.STAFF.get()),flint(1))) {
            invalid.setDamageValue(1);seats.set(4,invalid);
            // A damaged native staff item is eligible even without a binding; test full staff separately.
            if (invalid.is(ScrollItems.STAFF.get())) invalid.setDamageValue(0);
            h.assertTrue(FluxedFlintRecipe.repair(seats).isEmpty(),"Invalid repair accepted");
        }
        seats.set(4,staff());seats.set(0,flint(128));h.assertTrue(FluxedFlintRecipe.repair(seats).isEmpty(),"Spent catalyst accepted");
        var target=staff();target.setDamageValue(2);seats.set(0,flint(7));seats.set(4,target);var repair=FluxedFlintRecipe.repair(seats).orElseThrow();
        h.assertTrue(repair.output().getDamageValue()==0 && repair.remainingCatalyst().getDamageValue()==9,"Small repair overspent");
        h.assertTrue(!ScrollItems.FLUXED_FLINT.get().isRepairable(flint(7)) && !ScrollItems.FLUXED_FLINT.get().canGrindstoneRepair(flint(7)),"Catalyst permits bonus repair economy");h.succeed();
    }
    @GameTest(template="empty_9x3x9",batch="fluxed_flint")
    public static void eachVolatileInputRollsSeparatelyAndFirstTriggerIsProtected(GameTestHelper h) {
        var layout=offer(h,staff(),flint(0));layout.stands().get(4).remove();layout.stands().get(4).insert(flint(1));var inputs=RitualInputs.capture(layout);
        var draws=new AtomicInteger();var culprit=RitualVolatility.culprit(ItemStack.EMPTY,inputs,List.of(4,0),0,()->draws.getAndIncrement()==0?.5:0);
        h.assertTrue(draws.get()==2 && culprit.orElseThrow()==4,"Independent rolls or canonical order differ");
        draws.set(0);culprit=RitualVolatility.culprit(ItemStack.EMPTY,inputs,List.of(4,0),0,()->{draws.incrementAndGet();return 0;});
        h.assertTrue(draws.get()==1 && culprit.orElseThrow()==0,"First trigger was not retained");h.succeed();
    }
    @GameTest(template="empty_9x3x9",batch="fluxed_flint",timeoutTicks=90)
    public static void volatileTargetCanBeTheProtectedTriggerAndThenFlintIsLost(GameTestHelper h) {
        var target=StaffData.create(VestigeMainMod.location("volatile"));target.setDamageValue(9);var layout=offer(h,target,flint(0));var draws=new AtomicInteger();
        h.assertTrue(RitualCrafting.activate(player(h),layout.center(),()->draws.getAndIncrement()==0?.5:0)==RitualCrafting.Outcome.EXPLOSION_PENDING,"Second input did not roll");
        h.runAfterDelay(25,()->{h.assertTrue(ItemStack.matches(target,layout.stands().get(4).displayedItem()),"Volatile target trigger did not survive exactly");h.assertTrue(layout.stands().get(0).displayedItem().isEmpty(),"Nontriggering catalyst survived");h.succeed();});
    }
    @GameTest(template="empty_9x3x9",batch="fluxed_flint")
    public static void exactRiskBoundaryAndVanillaRepairBypassesAreRejected(GameTestHelper h) {
        var layout=offer(h,staff(),flint(7));var inputs=RitualInputs.capture(layout);
        h.assertTrue(RitualVolatility.culprit(ItemStack.EMPTY,inputs,List.of(0,4),0,()->.1).isEmpty(),"Ten percent boundary backfired");
        h.assertTrue(RitualVolatility.culprit(ItemStack.EMPTY,inputs,List.of(0,4),0,()->Math.nextDown(.1)).orElseThrow()==0,"Below ten percent boundary did not backfire");
        var grid=net.minecraft.world.item.crafting.CraftingInput.of(2,1,List.of(flint(120),flint(120)));
        h.assertTrue(h.getLevel().getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING,grid,h.getLevel()).isEmpty(),"Vanilla repair recipe creates free catalyst budget");
        for (var bound:List.of(staff(),WandData.create(WandComponents.Base.STICK,MagicalThreadRecipe.Type.CALLOUS,
                Optional.empty(),ScrollItems.scroll(VestigeMainMod.location("fireball"))),flint(120))) {
            bound.setDamageValue(9);var pair=net.minecraft.world.item.crafting.CraftingInput.of(2,1,List.of(bound,bound.copy()));
            h.assertTrue(h.getLevel().getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING,pair,h.getLevel()).isEmpty(),"Vanilla combining would discard stored magic");
            var owner=player(h);var inventory=owner.getInventory();
            var anvil=new net.minecraft.world.inventory.AnvilMenu(1,inventory);owner.containerMenu=anvil;
            anvil.getSlot(0).set(bound.copy());anvil.getSlot(1).set(bound.copy());anvil.createResult();
            h.assertTrue(!anvil.getSlot(2).hasItem(),"Anvil combining bypassed the finite repair economy");
            anvil.getSlot(1).set(ItemStack.EMPTY);anvil.setItemName("Renamed");
            var renamed=bound.copy();renamed.set(DataComponents.CUSTOM_NAME,Component.literal("Renamed"));
            h.assertTrue(ItemStack.matches(renamed,anvil.getSlot(2).getItem()),"Renaming lost stored magic or repaired damage");
            var grindstone=new net.minecraft.world.inventory.GrindstoneMenu(2,inventory);grindstone.getSlot(0).set(bound.copy());grindstone.getSlot(1).set(bound.copy());
            h.assertTrue(!grindstone.getSlot(2).hasItem(),"Grindstone combining bypassed exact item preservation");
        }
        h.succeed();
    }
    @GameTest(template="empty_9x3x9",batch="fluxed_flint",timeoutTicks=90)
    public static void sharedVolatilityAlsoProtectsTriggerDuringOrdinaryStaffExpansion(GameTestHelper h) {
        var center=structure(h,false);var layout=RitualCrafting.layout(center);var staff=StaffData.create(VestigeMainMod.location("volatile"));
        layout.stands().get(0).insert(staff);layout.stands().get(2).insert(new ItemStack(Items.DIAMOND));layout.stands().get(4).insert(new ItemStack(Items.AMETHYST_SHARD));layout.stands().get(6).insert(new ItemStack(ScrollItems.ENSORCELLED_THREAD.get()));
        h.assertTrue(RitualCrafting.activate(player(h),center,()->0)==RitualCrafting.Outcome.EXPLOSION_PENDING,"Ordinary expansion bypassed shared volatility");
        h.runAfterDelay(25,()->{h.assertTrue(ItemStack.matches(staff,layout.stands().get(0).displayedItem()),"Expansion trigger was consumed");h.assertTrue(java.util.stream.IntStream.of(2,4,6).allMatch(i -> layout.stands().get(i).displayedItem().isEmpty()),"Expansion failure retained other components");h.assertTrue(RitualTestOutput.stack(center).isEmpty(),"Failed expansion created output");h.succeed();});
    }
    @GameTest(template="empty_9x3x9",batch="fluxed_flint",timeoutTicks=90)
    public static void volatileOfferingTriggerForfeitsNontriggeringReferenceAndReleasesReservations(GameTestHelper h) {
        var center=structure(h,false);var layout=RitualCrafting.layout(center);var catalyst=flint(7);
        center.insert(ScrollItems.scroll(VestigeMainMod.location("fireball")));layout.stands().get(0).insert(catalyst);
        for(int seat:List.of(2,4,6))layout.stands().get(seat).insert(new ItemStack(Items.IRON_INGOT));
        var owner=player(h);SpellKnowledge.identify(owner,VestigeMainMod.location("fireball"));
        h.assertTrue(RitualCrafting.activate(owner,center,()->0)==RitualCrafting.Outcome.EXPLOSION_PENDING,"Completed wrong arrangement bypassed offering volatility");
        h.runAfterDelay(35,()->{
            h.assertTrue(ItemStack.matches(catalyst,layout.stands().get(0).displayedItem()),"Offering trigger did not survive");
            h.assertTrue(center.displayedItem().isEmpty() && java.util.stream.IntStream.of(2,4,6).allMatch(i -> layout.stands().get(i).displayedItem().isEmpty()),"Nontriggering inputs were protected");
            h.assertTrue(layout.blocks().stream().noneMatch(OfferingBlockEntity::busy),"Consumed reference leaked ritual reservations");h.succeed();
        });
    }
}
