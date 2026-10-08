package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.gametest.SurvivalTestPlayer;
import com.quzzar.vestige.magic.definition.SpellCost;
import com.quzzar.vestige.magic.world.*;
import com.quzzar.vestige.travel.PlayerExperience;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import net.neoforged.neoforge.gametest.*;
import java.util.*;

@GameTestHolder(VestigeMainMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class HourglassWorldTest {
    private static final BlockPos CENTER=new BlockPos(4,1,4);
    private static Vec3 start(GameTestHelper h) { return h.absolutePos(new BlockPos(2,1,2)).getBottomCenter(); }
    private static ItemStack equip(SurvivalTestPlayer player,HourglassData.Variant variant) {
        var stack=HourglassData.create(variant);player.getInventory().setItem(0,stack);player.setNoGravity(true);return stack;
    }
    private static SurvivalTestPlayer player(GameTestHelper h) {
        for(int x=0;x<7;x++)for(int z=0;z<5;z++)for(int y=1;y<=4;y++)h.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        var player=SurvivalTestPlayer.create(h);player.setPos(start(h));return player;
    }
    private static HourglassData.Variant plain() { return new HourglassData.Variant(Set.of(),HourglassData.NEUTRAL); }
    @GameTest(template="empty_9x3x9",batch="hourglass_craft",timeoutTicks=80)
    public static void orderedRitualConsumesFourOfferingsKeepsSocketsAndSpawnsAnExactCenteredDrop(GameTestHelper h) {
        h.setBlock(CENTER,ApparatusBlocks.SPELLSTONE.get());var geometry=HourglassData.NEUTRAL;
        for(int seat:List.of(0,2,4,6))h.setBlock(CENTER.offset(geometry.offset(seat)),ApparatusBlocks.PLINTH.get());
        var layout=RitualCrafting.layout((OfferingBlockEntity)h.getBlockEntity(CENTER));
        for(int i=0;i<4;i++)layout.stands().get(i*2).insert(new ItemStack(HourglassRecipe.ingredients().get(i)));
        layout.stands().get(0).installMaterial(new ItemStack(Items.COPPER_BLOCK));layout.stands().get(2).installMaterial(new ItemStack(Items.QUARTZ_BLOCK));layout.stands().get(4).installMaterial(new ItemStack(Items.LAPIS_BLOCK));
        h.assertTrue(RitualCrafting.activate(h.makeMockPlayer(GameType.SURVIVAL),layout.center())==RitualCrafting.Outcome.CRAFTING,"Hourglass ritual did not begin");
        h.runAfterDelay(45,() -> {
            var output=RitualTestOutput.stack(layout.center());var variant=HourglassData.read(output).orElseThrow();
            h.assertTrue(variant.choices().equals(Set.of(HourglassData.Choice.FLEETING,HourglassData.Choice.FRUGAL,HourglassData.Choice.ERUDITE)) && output.getMaxDamage()==6,"Output lost its crafted contributions");
            h.assertTrue(layout.items().stream().allMatch(ItemStack::isEmpty) && layout.stands().get(4).materialItem().is(Items.LAPIS_BLOCK),"Commit consumed socket or duplicated inputs");h.succeed();
        });
    }
    @GameTest(template="empty_9x3x9",batch="hourglass_cancel_craft",timeoutTicks=80)
    public static void changedSocketCancelsQueuedCraftWithoutConsumingAnyOffering(GameTestHelper h) {
        h.setBlock(CENTER,ApparatusBlocks.SPELLSTONE.get());var geometry=HourglassData.NEUTRAL;
        for(int seat:List.of(0,2,4,6))h.setBlock(CENTER.offset(geometry.offset(seat)),ApparatusBlocks.PLINTH.get());
        var layout=RitualCrafting.layout((OfferingBlockEntity)h.getBlockEntity(CENTER));
        for(int i=0;i<4;i++)layout.stands().get(i*2).insert(new ItemStack(HourglassRecipe.ingredients().get(i)));
        h.assertTrue(RitualCrafting.activate(h.makeMockPlayer(GameType.SURVIVAL),layout.center())==RitualCrafting.Outcome.CRAFTING,"Did not begin");
        h.runAfterDelay(10,() -> { var stand=layout.stands().get(4);stand.unlock();stand.installMaterial(new ItemStack(Items.MOSS_BLOCK)); });
        h.runAfterDelay(50,() -> {h.assertTrue(RitualTestOutput.stack(layout.center()).isEmpty() && layout.items().stream().filter(i -> !i.isEmpty()).count()==4,"Changed selector partially committed");h.succeed();});
    }
    @GameTest(template="empty_9x3x9",batch="hourglass_variants",timeoutTicks=70)
    public static void everyVariantReturnsToPartialHistoryAndPaysOnlyItsAuthoredResources(GameTestHelper h) {
        var player=player(h);equip(player,plain());HourglassMagic.record(player);
        h.runAfterDelay(2,() -> {
            try(player) {
                for(var variant:HourglassData.variants()) {
                    player.setHealth(20);player.getFoodData().setFoodLevel(20);NativeMana.set(player,100);player.setExperienceLevels(10);player.setExperiencePoints(0);
                    var xp=PlayerExperience.available(player);var stack=equip(player,variant);player.setPos(start(h).add(3,0,0));
                    h.assertTrue(HourglassMagic.returnToPast(player,stack),"Variant failed: "+variant.choices()+" / "+HourglassMagic.inspect(player,stack));
                    var ability=NativeMagic.abilities().abilities().get(HourglassData.FAMILY);double mana=0,health=0;int food=0,experience=0;
                    for(var cost:variant.costs(ability))switch(cost) {
                        case SpellCost.Mana m -> mana+=m.amount();case SpellCost.Health v -> health+=v.amount();case SpellCost.Hunger v -> food+=v.amount();case SpellCost.Experience v -> experience+=v.amount();default -> { }
                    }
                    h.assertTrue(player.position().distanceToSqr(start(h))<1e-8 && Math.abs(NativeMana.amount(player)-(100-mana))<1e-8
                            && player.getHealth()==20-health && player.getFoodData().getFoodLevel()==20-food && PlayerExperience.available(player)==xp-experience
                            && stack.getDamageValue()==1,"Variant payment, position or wear incorrect: "+variant.choices());
                }
                h.succeed();
            }
        });
    }
    @GameTest(template="empty_9x3x9",batch="hourglass_history",timeoutTicks=350)
    public static void fullFifteenSecondWindowSelectsRealRecordedPositionAndPreservesPresentState(GameTestHelper h) {
        var player=player(h);var stack=equip(player,plain());HourglassMagic.record(player);
        h.onEachTick(() -> HourglassMagic.record(player));
        h.runAfterDelay(305,() -> {
            try(player) {
                player.setPos(start(h).add(3,0,0));player.setHealth(13);player.setYRot(123);player.setXRot(24);player.fallDistance=5;player.setDeltaMovement(.1,-.2,.3);
                player.getInventory().setItem(9,new ItemStack(Items.DIAMOND,7));NativeMana.set(player,100);
                h.assertTrue(HourglassMagic.returnToPast(player,stack) && player.position().distanceToSqr(start(h))<1e-8,"Full window selected wrong historical position / "+HourglassMagic.inspect(player,stack));
                h.assertTrue(player.getHealth()==13 && player.getInventory().getItem(9).getCount()==7 && player.getYRot()==123 && player.getXRot()==24
                        && player.fallDistance==5 && player.getDeltaMovement().equals(new Vec3(.1,-.2,.3)),"Return rewound present state");h.succeed();
            }
        });
    }
    @GameTest(template="empty_9x3x9",batch="hourglass_failures",timeoutTicks=70)
    public static void blockedArrivalAndInsufficientManaAreFreeAndTheTenthSuccessBreaksTheItem(GameTestHelper h) {
        var player=player(h);var stack=equip(player,plain());HourglassMagic.record(player);
        h.runAfterDelay(2,() -> {
            try(player) {
                player.setPos(start(h).add(3,0,0));NativeMana.set(player,59);
                h.assertTrue(!HourglassMagic.returnToPast(player,stack) && NativeMana.amount(player)==59 && stack.getDamageValue()==0,"Failed mana payment spent resources");
                NativeMana.set(player,100);var feet=BlockPos.containing(start(h));h.getLevel().setBlockAndUpdate(feet,Blocks.STONE.defaultBlockState());
                h.assertTrue(!HourglassMagic.returnToPast(player,stack) && NativeMana.amount(player)==100 && stack.getDamageValue()==0,"Blocked arrival spent resources");h.getLevel().setBlockAndUpdate(feet,Blocks.AIR.defaultBlockState());
                for(int i=0;i<10;i++) { NativeMana.set(player,100);player.setPos(start(h).add(3,0,0));h.assertTrue(HourglassMagic.returnToPast(player,stack),"Valid return failed at use "+i); }
                h.assertTrue(stack.isEmpty(),"Tenth successful use did not break the hourglass");h.succeed();
            }
        });
    }
    @GameTest(template="empty_9x3x9",batch="hourglass_cancel_return",timeoutTicks=70)
    public static void canceledOrRedirectedTeleportRefundsManaXpAndRecoveryDelayWithoutWear(GameTestHelper h) {
        var player=player(h);var stack=equip(player,new HourglassData.Variant(Set.of(HourglassData.Choice.ERUDITE),HourglassData.NEUTRAL));HourglassMagic.record(player);
        h.runAfterDelay(2,() -> {
            try(player) {
                player.setPos(start(h).add(3,0,0));NativeMana.set(player,100);player.setExperienceLevels(10);player.setExperiencePoints(0);var before=PlayerExperience.Snapshot.of(player);
                java.util.function.Consumer<EntityTeleportEvent> cancel=e -> {if(e.getEntity()==player)e.setCanceled(true);};NeoForge.EVENT_BUS.addListener(cancel);
                try {h.assertTrue(!HourglassMagic.returnToPast(player,stack),"Canceled teleport moved");}finally {NeoForge.EVENT_BUS.unregister(cancel);}
                h.assertTrue(before.equals(PlayerExperience.Snapshot.of(player)) && NativeMana.amount(player)==100 && player.getPersistentData().getInt("vestige:mana_recovery")==0 && stack.getDamageValue()==0,"Canceled teleport failed rollback");
                java.util.function.Consumer<EntityTeleportEvent> redirect=e -> {if(e.getEntity()==player)e.setTargetX(e.getTargetX()+1);};NeoForge.EVENT_BUS.addListener(redirect);
                try {h.assertTrue(!HourglassMagic.returnToPast(player,stack),"Redirected teleport moved");}finally {NeoForge.EVENT_BUS.unregister(redirect);}
                java.util.function.Consumer<PlayerXpEvent.XpChange> cancelXp=e -> {if(e.getEntity()==player)e.setCanceled(true);};NeoForge.EVENT_BUS.addListener(cancelXp);
                try {h.assertTrue(!HourglassMagic.returnToPast(player,stack),"Canceled XP debit moved");}finally {NeoForge.EVENT_BUS.unregister(cancelXp);}
                h.assertTrue(before.equals(PlayerExperience.Snapshot.of(player)) && NativeMana.amount(player)==100 && stack.getDamageValue()==0,"Canceled payment mutated state");h.succeed();
            }
        });
    }
    @GameTest(template="empty_9x3x9",batch="hourglass_inventory",timeoutTicks=70)
    public static void offhandRecordsAndRemovalReacquisitionStartsFreshWithFreeNoMovementUse(GameTestHelper h) {
        var player=player(h);player.setNoGravity(true);var stack=HourglassData.create(plain());player.getInventory().setItem(40,stack);HourglassMagic.record(player);
        h.runAfterDelay(2,() -> {
            try(player) {
                player.setPos(start(h).add(3,0,0));NativeMana.set(player,100);h.assertTrue(HourglassMagic.returnToPast(player,stack),"Offhand did not record / "+HourglassMagic.inspect(player,stack));
                player.getInventory().setItem(40,ItemStack.EMPTY);HourglassMagic.record(player);player.setPos(start(h).add(3,0,0));player.getInventory().setItem(0,stack);NativeMana.set(player,100);
                h.assertTrue(!HourglassMagic.returnToPast(player,stack) && stack.getDamageValue()==1 && NativeMana.amount(player)==100,"Fresh acquisition used stale history or spent on no movement");h.succeed();
            }
        });
    }
    @GameTest(template="empty_9x3x9",batch="hourglass_resource_failures",timeoutTicks=70)
    public static void MissingHealthFoodOrExperienceRejectsTheWholePaymentWithoutFallback(GameTestHelper h) {
        var player=player(h);var stack=equip(player,plain());HourglassMagic.record(player);
        h.runAfterDelay(2,() -> {
            try(player) {
                for(var choice:List.of(HourglassData.Choice.BLOODBOUND,HourglassData.Choice.FASTING,HourglassData.Choice.ERUDITE)) {
                    stackCopyCheck(h,player,choice);
                }
                h.succeed();
            }
        });
    }
    private static void stackCopyCheck(GameTestHelper h,SurvivalTestPlayer player,HourglassData.Choice choice) {
        var stack=equip(player,new HourglassData.Variant(Set.of(choice),HourglassData.NEUTRAL));
        player.setPos(start(h).add(3,0,0));player.setHealth(choice==HourglassData.Choice.BLOODBOUND ? 2 : 20);
        player.getFoodData().setFoodLevel(choice==HourglassData.Choice.FASTING ? 3 : 20);
        player.setExperienceLevels(0);player.setExperiencePoints(0);player.giveExperiencePoints(choice==HourglassData.Choice.ERUDITE ? 29 : 100);
        NativeMana.set(player,100);float health=player.getHealth();int food=player.getFoodData().getFoodLevel();long xp=PlayerExperience.available(player);
        h.assertTrue(!HourglassMagic.returnToPast(player,stack) && NativeMana.amount(player)==100 && player.getHealth()==health
                && player.getFoodData().getFoodLevel()==food && PlayerExperience.available(player)==xp && stack.getDamageValue()==0,"Missing alternate resource partially paid or substituted mana: "+choice);
    }
    @GameTest(template="empty_9x3x9",batch="hourglass_holders",timeoutTicks=70)
    public static void transferringHourglassesNeverTransfersAnotherPlayersHistory(GameTestHelper h) {
        var first=player(h);var second=player(h);first.setPos(start(h));second.setPos(start(h).add(0,0,2));
        var item=equip(first,plain());equip(second,plain());HourglassMagic.record(first);HourglassMagic.record(second);
        h.runAfterDelay(2,() -> {
            try(first;second) {
                first.getInventory().setItem(0,ItemStack.EMPTY);HourglassMagic.record(first);second.getInventory().setItem(0,item);second.setPos(start(h).add(3,0,2));NativeMana.set(second,100);
                h.assertTrue(HourglassMagic.returnToPast(second,item) && second.position().distanceToSqr(start(h).add(0,0,2))<1e-8,"Transfer copied the former holder's destination");
                h.assertTrue(!HourglassMagic.returnToPast(first,item),"Former holder activated a transferred item");h.succeed();
            }
        });
    }
    @GameTest(template="empty_9x3x9",batch="hourglass_lifecycle",timeoutTicks=70)
    public static void deathLogoutAndDimensionEventsDiscardTheRecordedTrail(GameTestHelper h) {
        var player=player(h);var stack=equip(player,plain());HourglassMagic.record(player);
        h.runAfterDelay(2,() -> {
            try(player) {
                player.setPos(start(h).add(3,0,0));NativeMana.set(player,100);
                HourglassMagic.death(new net.neoforged.neoforge.event.entity.living.LivingDeathEvent(player,player.damageSources().generic()));
                h.assertTrue(!HourglassMagic.returnToPast(player,stack),"Death retained history");
                HourglassMagic.logout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));
                player.setPos(start(h));h.assertTrue(!HourglassMagic.returnToPast(player,stack),"Logout retained history");
                HourglassMagic.dimension(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent(player,player.level().dimension(),net.minecraft.world.level.Level.NETHER));
                player.setPos(start(h).add(3,0,0));h.assertTrue(!HourglassMagic.returnToPast(player,stack) && NativeMana.amount(player)==100 && stack.getDamageValue()==0,"Dimension reset spent resources or retained history");h.succeed();
            }
        });
    }

}
