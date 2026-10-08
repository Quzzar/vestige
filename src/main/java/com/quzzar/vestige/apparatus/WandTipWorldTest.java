package com.quzzar.vestige.apparatus;

import com.mojang.authlib.GameProfile;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.definition.*;
import com.quzzar.vestige.magic.effect.SpellEffects;
import com.quzzar.vestige.magic.expression.SpellValue;
import com.quzzar.vestige.magic.runtime.*;
import com.quzzar.vestige.magic.world.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import java.util.*;

@GameTestHolder(VestigeMainMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class WandTipWorldTest {
    private static net.minecraft.resources.ResourceLocation id(String name){return VestigeMainMod.location(name);}
    private static Player player(GameTestHelper h) {
        for(int x=0;x<9;x++)for(int z=0;z<9;z++){h.setBlock(new BlockPos(x,0,z),Blocks.STONE);h.setBlock(new BlockPos(x,1,z),Blocks.AIR);h.setBlock(new BlockPos(x,2,z),Blocks.AIR);}
        var p=new Player(h.getLevel(),h.absolutePos(new BlockPos(4,1,4)),0,new GameProfile(UUID.randomUUID(),"quiet-tip-player")) {
            public boolean isCreative(){return false;}public boolean isSpectator(){return false;}
            public boolean isCrouching(){return isShiftKeyDown();}
            public void displayClientMessage(net.minecraft.network.chat.Component text,boolean overlay){throw new AssertionError("Tip emitted gameplay text");}
            public void sendSystemMessage(net.minecraft.network.chat.Component text){throw new AssertionError("Tip emitted gameplay text");}
        };
        p.setPos(Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(4,1,4))));NativeMana.set(p,100);
        NativeMagic.session(p.getServer()).world().registerActor(p);return p;
    }
    private static Player ritualPlayer(GameTestHelper h) {
        return new Player(h.getLevel(),h.absolutePos(new BlockPos(4,1,1)),0,new GameProfile(UUID.randomUUID(),"ritual-tip-player")) {
            public boolean isCreative(){return false;}public boolean isSpectator(){return false;}
        };
    }
    private static SpellEffects.Action action(String name,double amount) {
        return new SpellEffects.Action(id(name),Map.of("amount",new SpellValue.Constant(amount),"ignore_invulnerability",new SpellValue.Constant(1)),Map.of());
    }
    private static SpellRuntime.Cast cast(Player p,WandTips.Tip tip,List<SpellEffect> plan,Entity target) {
        plan=List.of(new SpellEffects.ForEach(new TargetSpec(TargetSpec.Selection.EVENT_TARGET,new SpellValue.Constant(0)),plan));
        var spell=new SpellDefinition(id("tip_fixture"),Set.of(Tradition.ARCANE),TraitProfile.empty(),List.of(new SpellCost.Mana(6)),
                List.of(new SpellTrigger(id("primary"),id("interact"),List.of())),plan);
        var c=WandComponents.compile(spell,new ScrollItems.Scroll(spell.id(),List.of(),CastShaping.NONE,List.of()),WandComponents.Base.STICK,MagicalThreadRecipe.Type.CALLOUS,Optional.of(tip));
        return NativeMagic.session(p.getServer()).runtime().cast(c.cast().spell(),SpellEvent.of(id("interact"),p.getUUID(),new SpellSubject.Entity(target.getUUID())),
                c.cast().modifiers(),true,Optional.empty(),false,c.cast().shaping(),new CastReservation(){public boolean valid(){return true;}public void commit(){}public CastObserver observer(){return new WandTipEffects(tip,p,spell.traits());}});
    }
    private static LivingEntity cow(GameTestHelper h,BlockPos pos){return h.spawnWithNoFreeWill(EntityType.COW,pos);}
    @GameTest(template="empty_9x3x9",batch="wand_tip_echo",timeoutTicks=65)
    public static void resonatingEchoUsesActualDamageOnceAndCannotTriggerItself(GameTestHelper h) {
        var p=player(h);var victim=cow(h,new BlockPos(4,1,6));cast(p,WandTips.Tip.AMETHYST,List.of(action("damage",4)),victim);
        h.runAfterDelay(23,() -> h.assertTrue(Math.abs(victim.getHealth()-6)<.01,"Primary damage wrong: "+victim.getHealth()));
        h.runAfterDelay(55,() -> {h.assertTrue(Math.abs(victim.getHealth()-5.2)<.01,"Echo missing or repeated");h.succeed();});
    }
    @GameTest(template="empty_9x3x9",batch="wand_tip_fangs",timeoutTicks=65)
    public static void fangsReportsActualPrimaryDamageToResonating(GameTestHelper h) {
        var p=player(h);var victim=cow(h,new BlockPos(4,1,6));cast(p,WandTips.Tip.AMETHYST,List.of(action("fangs",4)),victim);
        h.runAfterDelay(55,() -> {h.assertTrue(Math.abs(victim.getHealth()-5.2)<.01,"Fangs did not report actual damage");h.succeed();});
    }
    @GameTest(template="empty_9x3x9",batch="wand_tip_relationship",timeoutTicks=65)
    public static void delayedEchoCannotDamageARecipientThatBecameFriendly(GameTestHelper h) {
        var p=player(h);var victim=cow(h,new BlockPos(4,1,6));cast(p,WandTips.Tip.AMETHYST,List.of(action("damage",4)),victim);
        h.runAfterDelay(25,() -> {
            var board=p.getScoreboard();var team=board.addPlayerTeam("tip_"+p.getUUID().toString().substring(0,8));
            board.addPlayerToTeam(p.getScoreboardName(),team);board.addPlayerToTeam(victim.getScoreboardName(),team);
        });
        h.runAfterDelay(55,() -> {h.assertTrue(Math.abs(victim.getHealth()-6)<.01,"Echo ignored changed relationship");h.succeed();});
    }
    @GameTest(template="empty_9x3x9",batch="wand_tip_heal",timeoutTicks=65)
    public static void resonatingEchoAlsoHealsWithoutBankingOverheal(GameTestHelper h) {
        var p=player(h);p.setHealth(10);cast(p,WandTips.Tip.AMETHYST,List.of(action("heal",4)),p);
        h.runAfterDelay(55,() -> {h.assertTrue(Math.abs(p.getHealth()-14.8)<.01,"Healing echo wrong");h.succeed();});
    }
    @GameTest(template="empty_9x3x9",batch="wand_tip_splash",timeoutTicks=50)
    public static void refractingSharesOnePoolAcrossNearbyHostiles(GameTestHelper h) {
        var p=player(h);var victim=cow(h,new BlockPos(4,1,6));var a=cow(h,new BlockPos(3,1,6));var b=cow(h,new BlockPos(5,1,6));
        cast(p,WandTips.Tip.DIAMOND,List.of(action("damage",4)),victim);
        h.runAfterDelay(25,() -> {h.assertTrue(Math.abs(victim.getHealth()-6)<.01 && Math.abs(a.getHealth()+b.getHealth()-19.2)<.02,"Splash duplicated its pool or missed recipients");h.succeed();});
    }
    @GameTest(template="empty_9x3x9",batch="wand_tip_refund",timeoutTicks=50)
    public static void reclaimingProtectionRefundsActualPaymentAndSpendsTwoWear(GameTestHelper h) {
        var p=player(h);SpellKnowledge.identify(p,id("pf2_shield"));
        p.setItemInHand(InteractionHand.MAIN_HAND,WandData.create(WandComponents.Base.STICK,MagicalThreadRecipe.Type.CALLOUS,Optional.of(WandTips.Tip.EMERALD),ScrollItems.scroll(id("pf2_shield"))));
        h.assertTrue(WandCasting.cast(p,InteractionHand.MAIN_HAND),"Tipped wand rejected");
        h.runAfterDelay(25,() -> {h.assertTrue(Math.abs(NativeMana.amount(p)-94.6)<.01 && p.getMainHandItem().getDamageValue()==2,"Refund/wear wrong");h.succeed();});
    }
    @GameTest(template="empty_9x3x9",batch="wand_tip_overheal",timeoutTicks=45)
    public static void reclaimingDoesNotRefundOverhealing(GameTestHelper h) {
        var p=player(h);cast(p,WandTips.Tip.EMERALD,List.of(action("heal",4)),p);
        h.runAfterDelay(25,() -> {h.assertTrue(NativeMana.amount(p)==94,"Overheal produced mana");h.succeed();});
    }
    @GameTest(template="empty_9x3x9",batch="wand_tip_utility",timeoutTicks=45)
    public static void completedDetectionIsAnInformativeUtilityResult(GameTestHelper h) {
        var p=player(h);cast(p,WandTips.Tip.EMERALD,List.of(action("detect_magic",0)),p);
        h.runAfterDelay(25,() -> {h.assertTrue(Math.abs(NativeMana.amount(p)-94.6)<.01,"Utility result did not refund");h.succeed();});
    }
    @GameTest(template="empty_9x3x9",batch="wand_tip_backstep",timeoutTicks=45)
    public static void elusiveMovesOnlyCrouchingCastersToSafeSupportedSpace(GameTestHelper h) {
        for(int x=2;x<7;x++)for(int z=1;z<8;z++)h.setBlock(new BlockPos(x,0,z),Blocks.STONE);
        var p=player(h);p.setShiftKeyDown(true);p.setPose(Pose.CROUCHING);p.setYRot(0);Vec3 before=p.position();cast(p,WandTips.Tip.ENDER_PEARL,List.of(action("detect_magic",0)),p);
        h.runAfterDelay(25,() -> {h.assertTrue(p.position().distanceTo(before.add(0,0,-1.5))<.01,"Backstep wrong: "+p.position().subtract(before));h.succeed();});
    }
    @GameTest(template="empty_9x3x9",batch="wand_tip_blocked",timeoutTicks=45)
    public static void elusiveCannotHopThroughAWallAndDoesNotCancelThePaidSpell(GameTestHelper h) {
        for(int x=2;x<7;x++)for(int z=1;z<8;z++)h.setBlock(new BlockPos(x,0,z),Blocks.STONE);
        var p=player(h);h.setBlock(new BlockPos(4,1,3),Blocks.STONE);h.setBlock(new BlockPos(4,2,3),Blocks.STONE);p.setShiftKeyDown(true);p.setPose(Pose.CROUCHING);p.setYRot(0);Vec3 before=p.position();var cast=cast(p,WandTips.Tip.ENDER_PEARL,List.of(action("detect_magic",0)),p);
        h.runAfterDelay(25,() -> {h.assertTrue(p.position().equals(before) && cast.paymentCommitted() && cast.status()==SpellRuntime.Status.COMPLETED,"Blocked backstep moved or canceled");h.succeed();});
    }
    @GameTest(template="empty_9x3x9",batch="wand_tip_conductive",timeoutTicks=50)
    public static void conductiveHasOneSharedThreeHpBudgetAcrossRecipients(GameTestHelper h) {
        var p=player(h);var cows=List.of(cow(h,new BlockPos(2,1,6)),cow(h,new BlockPos(3,1,6)),cow(h,new BlockPos(5,1,6)),cow(h,new BlockPos(6,1,6)));
        var each=new SpellEffects.ForEach(new TargetSpec(TargetSpec.Selection.NEARBY_ENTITIES,new SpellValue.Constant(6),true,Map.of(),TargetSpec.Relationship.HOSTILE),List.of(action("damage",1)));
        cast(p,WandTips.Tip.COPPER,List.of(each),p);
        h.runAfterDelay(25,() -> {h.assertTrue(Math.abs(cows.stream().mapToDouble(LivingEntity::getHealth).sum()-33)<.01,"Lightning exceeded or missed cast budget: "+cows.stream().map(LivingEntity::getHealth).toList());h.succeed();});
    }
    @GameTest(template="empty_9x3x9",batch="wand_tip_repelling",timeoutTicks=50)
    public static void repellingProtectsTheFriendlyRecipientAndRespectsResistance(GameTestHelper h) {
        var p=player(h);p.setHealth(10);var enemy=cow(h,new BlockPos(5,1,4));enemy.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
        cast(p,WandTips.Tip.IRON,List.of(action("heal",4)),p);Vec3 before=enemy.position();
        h.runAfterDelay(25,() -> {h.assertTrue(p.getHealth()==14 && enemy.position().subtract(before).multiply(1,0,1).length()<.01,"Repulsion moved an immune/friendly target");h.succeed();});
    }
    @GameTest(template="empty_9x3x9",batch="wand_tip_renewing",timeoutTicks=115)
    public static void renewingSupportsUtilityWithOneGradualCasterRecoveryPool(GameTestHelper h) {
        var p=player(h);p.setHealth(10);cast(p,WandTips.Tip.GHAST_TEAR,List.of(action("detect_magic",0),action("detect_magic",0)),p);
        h.runAfterDelay(30,() -> h.assertTrue(p.getHealth()==10,"Recovery was immediate"));
        h.runAfterDelay(70,() -> h.assertTrue(p.getHealth()==11,"First recovery pulse wrong"));
        h.runAfterDelay(105,() -> {h.assertTrue(p.getHealth()==12,"Repeated utility multiplied recovery");h.succeed();});
    }
    @GameTest(template="empty_9x3x9",batch="wand_tip_steadfast",timeoutTicks=60)
    public static void steadfastOwnsAndRemovesItsPreparationModifier(GameTestHelper h) {
        var p=player(h);cast(p,WandTips.Tip.NETHERITE,List.of(action("detect_magic",0)),p);
        h.assertTrue(Math.abs(p.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)-.5)<.001,"Preparation stance absent");
        h.runAfterDelay(35,() -> {h.assertTrue(p.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)==0,"Preparation stance leaked");h.succeed();});
    }
    @GameTest(template="empty_9x3x9",batch="wand_tip_ritual",timeoutTicks=85)
    public static void tippedRitualConsumesSixAndRetainsTheExactSourceVariant(GameTestHelper h) {
        var geometry=new LeylineShaping.Geometry(8,LeylineShaping.Shape.CROSS,2,0,LeylineShaping.Shape.DIAGONAL,3,0);var center=new BlockPos(4,1,4);
        h.setBlock(center,ApparatusBlocks.SPELLSTONE.get());for(int i=0;i<8;i++)h.setBlock(center.offset(geometry.offset(i)),ApparatusBlocks.PLINTH.get());
        var layout=LeylineStructure.find((OfferingBlockEntity)h.getBlockEntity(center),8).getFirst();var source=ScrollItems.shapedScroll(id("fireball"),new LeylineShaping.Modifiers(1.1,1.2,.9,1.1),List.of(new Spellshaping.Selection(id("reaching"),1)));
        layout.stands().get(0).insert(new ItemStack(Items.BONE));layout.stands().get(1).insert(new ItemStack(MagicalThreadRecipe.Type.CALLOUS.item()));layout.stands().get(3).insert(new ItemStack(Items.DIAMOND));
        for(int seat:List.of(2,4,6))layout.stands().get(seat).insert(source.copy());
        h.assertTrue(RitualCrafting.activate(ritualPlayer(h),layout.center())==RitualCrafting.Outcome.CRAFTING,"Tipped binding rejected");
        h.runAfterDelay(65,() -> {var binding=WandData.binding(RitualTestOutput.stack(layout.center())).orElseThrow();h.assertTrue(binding.tip().equals(Optional.of(WandTips.Tip.DIAMOND)) && binding.scroll().equals(ScrollItems.scroll(source).orElseThrow()) && layout.items().stream().allMatch(ItemStack::isEmpty),"Tipped result lost source or consumption");h.succeed();});
    }
}
