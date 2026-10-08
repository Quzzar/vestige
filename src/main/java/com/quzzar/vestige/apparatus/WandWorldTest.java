package com.quzzar.vestige.apparatus;

import com.mojang.authlib.GameProfile;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.world.NativeMagic;
import com.quzzar.vestige.magic.world.NativeMana;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import java.util.*;
import java.util.function.Consumer;

@GameTestHolder(VestigeMainMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class WandWorldTest {
    private static final BlockPos CENTER=new BlockPos(4,1,4);
    private static ResourceLocation id(String path) { return VestigeMainMod.location(path); }
    private static Player player(GameTestHelper h,boolean known,String spell) {
        var player=new Player(h.getLevel(),h.absolutePos(new BlockPos(4,1,1)),0,new GameProfile(UUID.randomUUID(),"quiet-wand-player")) {
            public boolean isSpectator() { return false; }
            public boolean isCreative() { return false; }
            public void displayClientMessage(net.minecraft.network.chat.Component text,boolean overlay) { throw new AssertionError("Wand emitted gameplay text"); }
            public void sendSystemMessage(net.minecraft.network.chat.Component text) { throw new AssertionError("Wand emitted gameplay text"); }
        };
        player.setPos(Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(4,1,1))));
        NativeMana.set(player,100);if (known) SpellKnowledge.identify(player,id(spell));
        player.setItemInHand(InteractionHand.MAIN_HAND,wand(spell,MagicalThreadRecipe.Type.CALLOUS));
        return player;
    }
    private static ItemStack wand(String spell,MagicalThreadRecipe.Type thread) {
        return WandData.create(WandComponents.Base.STICK,thread,ScrollItems.scroll(id(spell)));
    }
    private static RitualCrafting.Layout ritual(GameTestHelper h,int rotation) {
        var geometry=new LeylineShaping.Geometry(8,LeylineShaping.Shape.CROSS,2,0,LeylineShaping.Shape.DIAGONAL,3,0);
        h.setBlock(CENTER,ApparatusBlocks.SPELLSTONE.get());
        for (int i=0;i<8;i++) h.setBlock(CENTER.offset(geometry.offset(i)),ApparatusBlocks.PLINTH.get());
        var center=(OfferingBlockEntity)h.getBlockEntity(CENTER);var layout=LeylineStructure.find(center,8).getFirst();
        var source=ScrollItems.shapedScroll(id("fireball"),new LeylineShaping.Modifiers(1.1,1.2,.9,1.2),List.of(new Spellshaping.Selection(id("reaching"),2)));
        layout.stands().get(rotation).insert(new ItemStack(Items.BLAZE_ROD));
        layout.stands().get((rotation+1)%8).insert(new ItemStack(MagicalThreadRecipe.Type.CALLOUS.item()));
        for (int offset:List.of(2,4,6)) layout.stands().get((rotation+offset)%8).insert(source.copy());
        for (var stand:layout.stands()) stand.installMaterial(new ItemStack(Items.DIAMOND_BLOCK));
        return layout;
    }
    @GameTest(template="empty_9x3x9",batch="wand_binding",timeoutTicks=90)
    public static void rotatedBindingKeepsShapingOnceConsumesFiveAndRetainsSockets(GameTestHelper h) {
        var layout=ritual(h,2);var inputs=RitualInputs.capture(layout);var source=ScrollItems.scroll(layout.items().get(4)).orElseThrow();
        var crafter=player(h,false,"fireball");
        h.assertTrue(RitualCrafting.activate(crafter,layout.center())==RitualCrafting.Outcome.CRAFTING,"Binding rejected");
        h.assertTrue(RitualCrafting.activate(crafter,layout.center())==RitualCrafting.Outcome.BUSY,"Duplicate binding accepted");
        h.runAfterDelay(65,() -> {
            var output=RitualTestOutput.stack(layout.center());var binding=WandData.binding(output).orElseThrow();
            h.assertTrue(binding.scroll().equals(source) && binding.base()==WandComponents.Base.BLAZE_ROD
                    && binding.thread()==MagicalThreadRecipe.Type.CALLOUS && output.getMaxDamage()==28,"Binding changed magic or components");
            h.assertTrue(layout.items().stream().allMatch(ItemStack::isEmpty) && layout.center().displayedItem().isEmpty(),"Wrong offering/reference consumption");
            h.assertTrue(inputs.nodes().stream().allMatch(n -> ItemStack.matches(n.material(),layout.stands().get(n.seat()).materialItem())),"Sockets changed");
            h.assertTrue(!SpellKnowledge.identified(crafter,id("fireball")),"Binding identified its spell");
            h.assertTrue(layout.blocks().stream().noneMatch(OfferingBlockEntity::busy),"Binding retained reservations");h.succeed();
        });
    }
    @GameTest(template="empty_9x3x9",batch="wand_cancel",timeoutTicks=80)
    public static void changingReservedEmptyTipSeatCancelsWithoutConsumption(GameTestHelper h) {
        var layout=ritual(h,0);
        h.assertTrue(RitualCrafting.activate(player(h,true,"fireball"),layout.center())==RitualCrafting.Outcome.CRAFTING,"Binding rejected");
        h.runAfterDelay(10,() -> layout.stands().get(3).insert(new ItemStack(Items.DIAMOND)));
        h.runAfterDelay(65,() -> {
            h.assertTrue(RitualTestOutput.stack(layout.center()).isEmpty() && layout.items().stream().filter(s -> !s.isEmpty()).count()==6,
                    "Changed empty seat spent ingredients or produced a wand");h.succeed();
        });
    }
    @GameTest(template="empty_9x3x9",batch="wand_spawn",timeoutTicks=80)
    public static void canceledOutputSpawnPreservesAllFiveOfferings(GameTestHelper h) {
        var layout=ritual(h,0);
        Consumer<EntityJoinLevelEvent> cancel=event -> {
            if (event.getLevel()==h.getLevel() && event.getEntity() instanceof ItemEntity item && item.getItem().is(ScrollItems.WAND.get())
                    && item.position().distanceToSqr(Vec3.atCenterOf(layout.center().getBlockPos()))<4) event.setCanceled(true);
        };
        NeoForge.EVENT_BUS.addListener(cancel);
        h.assertTrue(RitualCrafting.activate(player(h,true,"fireball"),layout.center())==RitualCrafting.Outcome.CRAFTING,"Binding rejected");
        h.runAfterDelay(65,() -> {
            NeoForge.EVENT_BUS.unregister(cancel);
            h.assertTrue(RitualTestOutput.stack(layout.center()).isEmpty() && layout.items().stream().filter(s -> !s.isEmpty()).count()==5,
                    "Rejected spawn spent ingredients");h.succeed();
        });
    }
    @GameTest(template="empty_9x3x9",batch="wand_persistence")
    public static void sourceComponentsAndUsedDurabilitySurviveSaving(GameTestHelper h) {
        var source=ScrollItems.shapedScroll(id("fireball"),new LeylineShaping.Modifiers(1.1,1.2,.9,.8),List.of(new Spellshaping.Selection(id("bleeding"),1)));
        for (var base:WandComponents.Base.values()) for (var core:MagicalThreadRecipe.types()) {
            var item=WandData.create(base,core,source);item.setDamageValue(7);
            var restored=ItemStack.parse(h.getLevel().registryAccess(),item.save(h.getLevel().registryAccess())).orElseThrow();
            h.assertTrue(ItemStack.matches(item,restored) && WandData.binding(restored).orElseThrow().scroll().equals(ScrollItems.scroll(source).orElseThrow()),"Save changed source or wear");
        }
        var malformed=wand("pf2_shield",MagicalThreadRecipe.Type.CALLOUS);malformed.set(DataComponents.MAX_DAMAGE,1000);
        var caster=player(h,true,"pf2_shield");caster.setItemInHand(InteractionHand.MAIN_HAND,malformed);
        h.assertTrue(!WandCasting.cast(caster,InteractionHand.MAIN_HAND) && NativeMana.amount(caster)==100,"Forged capacity cast or paid");h.succeed();
    }
    @GameTest(template="empty_9x3x9",batch="wand_payment",timeoutTicks=80)
    public static void repeatAndSwappedWandsEachPrepareAndPayWithoutAddedRecovery(GameTestHelper h) {
        var caster=player(h,true,"pf2_shield");
        h.assertTrue(WandCasting.cast(caster,InteractionHand.MAIN_HAND),"Cast rejected");
        h.assertTrue(caster.getMainHandItem().getDamageValue()==0 && NativeMana.amount(caster)==100,"Preparation paid early");
        h.runAfterDelay(25,() -> {
            h.assertTrue(caster.getMainHandItem().getDamageValue()==1 && NativeMana.amount(caster)==94,"Commitment did not pay/wear exactly once");
            h.assertTrue(WandCasting.cast(caster,InteractionHand.MAIN_HAND),"Same wand retained added recovery");
            h.assertTrue(caster.getMainHandItem().getDamageValue()==1 && NativeMana.amount(caster)==94,"Repeat preparation paid early");
        });
        h.runAfterDelay(50,() -> {
            h.assertTrue(caster.getMainHandItem().getDamageValue()==2 && NativeMana.amount(caster)==88,"Repeat commitment skipped or duplicated payment");
            caster.getInventory().setItem(5,caster.getMainHandItem().copy());
            caster.setItemInHand(InteractionHand.MAIN_HAND,wand("pf2_shield",MagicalThreadRecipe.Type.ENSORCELLED));
            h.assertTrue(WandCasting.cast(caster,InteractionHand.MAIN_HAND),"Swapped wand retained added recovery");
            h.assertTrue(caster.getMainHandItem().getDamageValue()==0 && NativeMana.amount(caster)==88,"Swapped preparation paid early");
        });
        h.runAfterDelay(70,() -> {
            h.assertTrue(caster.getMainHandItem().getDamageValue()==1 && caster.getInventory().getItem(5).getDamageValue()==2
                    && NativeMana.amount(caster)==83,"Economy core must save one mana on the swapped cast");h.succeed();
        });
    }
    @GameTest(template="empty_9x3x9",batch="wand_scroll_recovery",timeoutTicks=120)
    public static void wandAndScrollCastsCanFollowEachOtherWithoutAddedRecovery(GameTestHelper h) {
        var caster=player(h,true,"pf2_shield");h.assertTrue(WandCasting.cast(caster,InteractionHand.MAIN_HAND),"Wand cast rejected");
        h.runAfterDelay(25,() -> {
            caster.setItemInHand(InteractionHand.MAIN_HAND,ScrollItems.scroll(id("pf2_shield")));
            h.assertTrue(ScrollCasting.cast(caster,caster.getMainHandItem()) && caster.getMainHandItem().isEmpty() && NativeMana.amount(caster)==88,
                    "Wand blocked the scroll or charged the wrong amount");
            caster.setItemInHand(InteractionHand.MAIN_HAND,wand("pf2_shield",MagicalThreadRecipe.Type.CALLOUS));
            h.assertTrue(WandCasting.cast(caster,InteractionHand.MAIN_HAND),"Scroll blocked the next wand preparation");
            h.assertTrue(caster.getMainHandItem().getDamageValue()==0 && NativeMana.amount(caster)==88,"New wand preparation paid early");
        });
        h.runAfterDelay(55,() -> {
            h.assertTrue(caster.getMainHandItem().getDamageValue()==1 && NativeMana.amount(caster)==82,"Wand after scroll did not pay normally");h.succeed();
        });
    }
    @GameTest(template="empty_9x3x9",batch="wand_switch",timeoutTicks=80)
    public static void switchingTheHeldSourceDuringPreparationCancelsPayment(GameTestHelper h) {
        var caster=player(h,true,"pf2_shield");var original=caster.getMainHandItem().copy();
        h.assertTrue(WandCasting.cast(caster,InteractionHand.MAIN_HAND),"Cast rejected");
        h.runAfterDelay(5,() -> caster.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.STICK)));
        h.runAfterDelay(25,() -> {
            h.assertTrue(NativeMana.amount(caster)==100 && original.getDamageValue()==0,"Canceled preparation charged a resource");
            caster.setItemInHand(InteractionHand.MAIN_HAND,original);h.assertTrue(WandCasting.cast(caster,InteractionHand.MAIN_HAND),"Cancellation acquired recovery");
        });
        h.runAfterDelay(55,() -> {h.assertTrue(NativeMana.amount(caster)==94 && caster.getMainHandItem().getDamageValue()==1,"Retry did not commit normally");h.succeed();});
    }
    @GameTest(template="empty_9x3x9",batch="wand_failed",timeoutTicks=85)
    public static void failedPaymentKeepsTheWandAndAllowsAnAffordableRetry(GameTestHelper h) {
        var caster=player(h,true,"pf2_shield");NativeMana.set(caster,0);
        h.assertTrue(WandCasting.cast(caster,InteractionHand.MAIN_HAND),"Preparation rejected too early");
        h.runAfterDelay(25,() -> {
            h.assertTrue(caster.getMainHandItem().getDamageValue()==0 && NativeMana.amount(caster)==0,"Failed payment wore the wand");
            NativeMana.set(caster,100);h.assertTrue(WandCasting.cast(caster,InteractionHand.MAIN_HAND),"Failed payment acquired recovery");
        });
        h.runAfterDelay(55,() -> {h.assertTrue(caster.getMainHandItem().getDamageValue()==1 && NativeMana.amount(caster)==94,"Affordable retry failed");h.succeed();});
    }
    @GameTest(template="empty_9x3x9",batch="wand_last",timeoutTicks=70)
    public static void theLastUseBreaksOnlyTheHeldWandIncludingOffhand(GameTestHelper h) {
        var caster=player(h,true,"pf2_shield");var spare=caster.getMainHandItem().copy();
        caster.getInventory().setItem(5,spare);
        caster.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);var finalUse=spare.copy();finalUse.setDamageValue(finalUse.getMaxDamage()-1);
        caster.setItemInHand(InteractionHand.OFF_HAND,finalUse);
        h.assertTrue(WandCasting.cast(caster,InteractionHand.OFF_HAND),"Final use rejected");
        h.runAfterDelay(25,() -> {
            h.assertTrue(caster.getOffhandItem().isEmpty() && caster.getInventory().getItem(5).getDamageValue()==0 && NativeMana.amount(caster)==94,
                    "Final cast failed to break or charged a spare wand");
            caster.setItemInHand(InteractionHand.MAIN_HAND,caster.getInventory().getItem(5));caster.getInventory().setItem(5,ItemStack.EMPTY);
            h.assertTrue(WandCasting.cast(caster,InteractionHand.MAIN_HAND),"Broken offhand wand blocked its replacement");
            h.assertTrue(caster.getMainHandItem().getDamageValue()==0 && NativeMana.amount(caster)==94,"Replacement preparation paid early");
        });
        h.runAfterDelay(55,() -> {
            h.assertTrue(caster.getMainHandItem().getDamageValue()==1 && NativeMana.amount(caster)==88,"Replacement wand did not pay normally");h.succeed();
        });
    }
    @GameTest(template="empty_9x3x9",batch="wand_unknown",timeoutTicks=70)
    public static void wandCastingNeverIdentifiesAnUnknownSpell(GameTestHelper h) {
        var caster=player(h,false,"pf2_shield");h.assertTrue(WandCasting.cast(caster,InteractionHand.MAIN_HAND),"Unknown wand rejected");
        h.runAfterDelay(30,() -> {
            h.assertTrue(!SpellKnowledge.identified(caster,id("pf2_shield")) && caster.getMainHandItem().getDamageValue()==1 && NativeMana.amount(caster)==94,
                    "Unknown wand identified its spell or skipped its committed payment");h.succeed();
        });
    }
    @GameTest(template="empty_9x3x9",batch="wand_core_healing",timeoutTicks=75)
    public static void consecratedHealingCleansesTheActualRecipient(GameTestHelper h) {
        var caster=player(h,true,"heal");caster.setHealth(10);
        caster.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,200));caster.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,200));
        caster.setItemInHand(InteractionHand.MAIN_HAND,wand("heal",MagicalThreadRecipe.Type.CONSECRATED));
        h.assertTrue(WandCasting.cast(caster,InteractionHand.MAIN_HAND),"Consecrated healing rejected");
        h.runAfterDelay(30,() -> {
            h.assertTrue(caster.getHealth()==15 && !caster.hasEffect(MobEffects.WEAKNESS) && !caster.hasEffect(MobEffects.BLINDNESS)
                    && caster.getMainHandItem().getDamageValue()==1 && NativeMana.amount(caster)==83,"Healing core did not heal, cleanse or charge once");h.succeed();
        });
    }
    @GameTest(template="empty_9x3x9",batch="wand_core_protection",timeoutTicks=70)
    public static void consecratedProtectionWorksWithoutAHealingLeaf(GameTestHelper h) {
        var caster=player(h,true,"pf2_shield");caster.addEffect(new MobEffectInstance(MobEffects.DARKNESS,200));
        caster.setItemInHand(InteractionHand.MAIN_HAND,wand("pf2_shield",MagicalThreadRecipe.Type.CONSECRATED));
        h.assertTrue(WandCasting.cast(caster,InteractionHand.MAIN_HAND),"Protective core rejected");
        h.runAfterDelay(2,() -> {
            h.assertTrue(!caster.hasEffect(MobEffects.DARKNESS) && caster.getMainHandItem().getDamageValue()==1 && NativeMana.amount(caster)==93,
                    "Protection core did not cleanse or charge the protected recipient");h.succeed();
        });
    }
    @GameTest(template="empty_9x3x9",batch="wand_core_damage",timeoutTicks=75)
    public static void smolderingProjectileIgnitesAfterPositivePrimaryContact(GameTestHelper h) {
        var caster=player(h,true,"force_arrow");caster.setXRot(10);
        caster.setItemInHand(InteractionHand.MAIN_HAND,wand("force_arrow",MagicalThreadRecipe.Type.SMOLDERING));
        var victim=h.spawnWithNoFreeWill(EntityType.COW,new BlockPos(4,1,3));
        h.assertTrue(WandCasting.cast(caster,InteractionHand.MAIN_HAND),"Smoldering projectile rejected");
        h.runAfterDelay(8,() -> {
            h.assertTrue(victim.getHealth()<10 && victim.isOnFire() && caster.getMainHandItem().getDamageValue()==2 && NativeMana.amount(caster)==84,
                    "Projectile missed its core effect or did not charge two wear");h.succeed();
        });
    }
}
