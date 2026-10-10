package com.quzzar.vestige.travel;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.*;
import com.quzzar.vestige.apparatus.recipeviewer.StandingStoneDisplays;
import com.quzzar.vestige.gametest.SurvivalTestPlayer;
import com.quzzar.vestige.magic.world.NativeMana;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import net.neoforged.neoforge.gametest.*;
import java.util.*;
import java.util.function.Consumer;

@GameTestHolder(VestigeMainMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class StandingStoneImbuementTest {
    private static final BlockPos CENTER=new BlockPos(4,1,4);
    private static final LeylineShaping.Geometry FOUR=new LeylineShaping.Geometry(4,LeylineShaping.Shape.CROSS,2,0,LeylineShaping.Shape.DIAGONAL,3,0);
    private static String key() { return UUID.randomUUID().toString().replace("-", "").repeat(2); }
    private static ItemStack shard() {
        var geometry=new LeylineShaping.Geometry(8,LeylineShaping.Shape.CROSS,2,0,LeylineShaping.Shape.DIAGONAL,3,0);
        var items=AttunementShardItem.ingredients();var nodes=new ArrayList<RitualInputs.Node>();
        for(int i=0;i<8;i++)nodes.add(new RitualInputs.Node(i,geometry.offset(i),i<items.size()?new ItemStack(BuiltInRegistries.ITEM.get(items.get(i))):ItemStack.EMPTY,ItemStack.EMPTY));
        return AttunementShardItem.create(new RitualInputs(geometry,nodes));
    }
    private static ItemStack selector(StandingStonePayment route) { return route.material().map(id->new ItemStack(BuiltInRegistries.ITEM.get(id))).orElse(ItemStack.EMPTY); }
    private static RitualInputs inputs(StandingStoneRecipe.Variant variant,ItemStack shard,int rotation) {
        var nodes=new ArrayList<RitualInputs.Node>();
        for(int i=0;i<4;i++) {
            int seat=(i*2+rotation*2)%8;
            nodes.add(new RitualInputs.Node(seat,FOUR.offset(seat),i==0?shard:new ItemStack(variant.ingredients().get(i)),i==2?selector(variant.payment()):new ItemStack(Items.COPPER_BLOCK)));
        }
        return new RitualInputs(FOUR,nodes);
    }
    @GameTest(template="empty_9x3x9",batch="stone_imbuements")
    public static void all185RecipesRotateAndTheirPublicDisplaysRetainOnlyFinishAndRoute(GameTestHelper h) {
        var shard=shard();String key=AttunementShardItem.signature(shard).orElseThrow().key();
        var variants=StandingStoneRecipe.variants();var displays=StandingStoneDisplays.entries();
        h.assertTrue(variants.size()==185 && displays.size()==185,"Incomplete dynamic catalog");
        for(int index=0;index<variants.size();index++) {
            var variant=variants.get(index);var display=displays.get(index);
            h.assertTrue(StandingStones.key(display.output()).isEmpty() && StandingStones.payment(display.output()).orElseThrow()==variant.payment()
                    && StandingStones.material(display.output()).orElseThrow()==variant.material(),"Public display invented a private key or lost its route");
            h.assertTrue(display.imbuements().size()==(variant.payment()==StandingStonePayment.EXPERIENCE?0:1)
                    && display.imbuements().stream().allMatch(value->value.seat()==4 && value.material().equals(variant.payment().material().orElseThrow())),"Selector not attached to Pearl");
            for(int rotation=0;rotation<4;rotation++) {
                var output=StandingStoneRecipe.result(inputs(variant,shard.copy(),rotation)).orElseThrow();
                h.assertTrue(StandingStones.key(output).orElseThrow().equals(key) && StandingStones.payment(output).orElseThrow()==variant.payment()
                        && StandingStones.material(output).orElseThrow()==variant.material(),"Rotation changed key, finish or payment: "+variant.id());
                h.assertTrue(StandingStoneDisplays.subtype(output).equals(StandingStoneDisplays.subtype(display.output())),"Private key split recipe lookup");
            }
        }
        h.succeed();
    }
    @GameTest(template="empty_9x3x9",batch="stone_imbuements")
    public static void wrongOrderAndUnsupportedPearlSocketRejectWithoutAHiddenFallback(GameTestHelper h) {
        var variant=new StandingStoneRecipe.Variant(ApparatusMaterials.STONE_BRICKS,StandingStonePayment.MANA,Items.STONE_BRICKS);
        var correct=inputs(variant,shard(),0);var nodes=new ArrayList<>(correct.nodes());
        var a=nodes.get(1);var b=nodes.get(2);
        nodes.set(1,new RitualInputs.Node(a.seat(),a.offset(),b.offering(),a.material()));nodes.set(2,new RitualInputs.Node(b.seat(),b.offset(),a.offering(),b.material()));
        h.assertTrue(StandingStoneRecipe.result(new RitualInputs(FOUR,nodes)).isEmpty(),"Arbitrary permutation accepted");
        nodes=new ArrayList<>(correct.nodes());var pearl=nodes.get(2);
        nodes.set(2,new RitualInputs.Node(pearl.seat(),pearl.offset(),pearl.offering(),new ItemStack(Items.DIAMOND_BLOCK)));
        h.assertTrue(StandingStoneRecipe.result(new RitualInputs(FOUR,nodes)).isEmpty(),"Unsupported Pearl material fell back to XP");
        nodes.set(2,new RitualInputs.Node(pearl.seat(),pearl.offset(),new ItemStack(Items.ENDER_PEARL,2),selector(StandingStonePayment.MANA)));
        h.assertTrue(StandingStoneRecipe.result(new RitualInputs(FOUR,nodes)).isEmpty(),"Stacked Pearl accepted");h.succeed();
    }
    private static RitualCrafting.Layout fixture(GameTestHelper h) {
        h.setBlock(CENTER,ApparatusBlocks.SPELLSTONE.get());
        for(int seat:List.of(0,2,4,6))h.setBlock(CENTER.offset(FOUR.offset(seat)),ApparatusBlocks.PLINTH.get());
        return RitualCrafting.layout((OfferingBlockEntity)h.getBlockEntity(CENTER));
    }
    private static void fill(RitualCrafting.Layout layout,StandingStonePayment route) {
        var variant=new StandingStoneRecipe.Variant(ApparatusMaterials.STONE_BRICKS,route,Items.STONE_BRICKS);
        var captured=inputs(variant,shard(),0);
        for(var node:captured.nodes()) {
            var stand=layout.stands().get(node.seat());stand.unlock();stand.removeMaterial();stand.insert(node.offering());
            if(!node.material().isEmpty())stand.installMaterial(node.material());
        }
    }
    @GameTest(template="empty_9x3x9",batch="stone_imbuement_craft",timeoutTicks=400)
    public static void everyRouteActuallyCraftsConsumesOfferingsAndRetainsEmbeddedSelectors(GameTestHelper h) {
        var layout=fixture(h);var player=h.makeMockPlayer(GameType.SURVIVAL);
        for(var route:StandingStonePayment.values()) {
            int tick=route.ordinal()*70;
            h.runAfterDelay(tick+1,()-> {fill(layout,route);h.assertTrue(RitualCrafting.activate(player,layout.center())==RitualCrafting.Outcome.CRAFTING,"Route did not begin "+route);});
            h.runAfterDelay(tick+46,()-> {
                var output=RitualTestOutput.take(layout.center());h.assertTrue(StandingStones.payment(output).orElseThrow()==route,"Craft lost route "+route);
                h.assertTrue(layout.items().stream().allMatch(ItemStack::isEmpty) && ItemStack.matches(layout.stands().get(4).materialItem(),selector(route)),"Commit consumed socket or retained offering");
            });
        }
        h.runAfterDelay(350,h::succeed);
    }
    @GameTest(template="empty_9x3x9",batch="stone_imbuement_cancel_craft",timeoutTicks=75)
    public static void editingPearlSelectorCancelsTheEntireQueuedCraft(GameTestHelper h) {
        var layout=fixture(h);fill(layout,StandingStonePayment.MANA);
        h.assertTrue(RitualCrafting.activate(h.makeMockPlayer(GameType.SURVIVAL),layout.center())==RitualCrafting.Outcome.CRAFTING,"Did not begin");
        h.runAfterDelay(10,()-> {var stand=layout.stands().get(4);stand.unlock();stand.removeMaterial();stand.installMaterial(selector(StandingStonePayment.HEALTH));});
        h.runAfterDelay(50,()-> {h.assertTrue(RitualTestOutput.stack(layout.center()).isEmpty() && layout.items().stream().filter(i->!i.isEmpty()).count()==4,"Changed selector partially committed");h.succeed();});
    }
    private static StandingStoneEntity place(GameTestHelper h,BlockPos pos,String key,StandingStonePayment route) {
        var level=h.getLevel();var state=StandingStones.STONE.get().defaultBlockState();
        level.setBlock(pos,state,3);level.setBlock(pos.above(),state.setValue(StandingStoneBlock.HALF,DoubleBlockHalf.UPPER),3);
        var stone=(StandingStoneEntity)level.getBlockEntity(pos);stone.configure(key,"Standing Stone",route);return stone;
    }
    private static void floor(GameTestHelper h,BlockPos pos) {
        for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++) {h.getLevel().setBlock(pos.offset(x,-1,z),Blocks.STONE.defaultBlockState(),3);for(int y=0;y<=3;y++)h.getLevel().setBlock(pos.offset(x,y,z),Blocks.AIR.defaultBlockState(),3);}
    }
    @GameTest(template="empty_9x3x9",batch="stone_imbuements")
    public static void all180FinishesAndRoutesSurviveMiningAndSavingWithoutChangingTheKey(GameTestHelper h) {
        var pos=h.absolutePos(CENTER);floor(h,pos);String key=key();var player=h.makeMockPlayer(GameType.SURVIVAL);
        for(var material:ApparatusMaterials.values())for(var route:StandingStonePayment.values()) {
            var item=StandingStones.bound(key,material,route);player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,item);
            item.useOn(new net.minecraft.world.item.context.UseOnContext(player,net.minecraft.world.InteractionHand.MAIN_HAND,new BlockHitResult(pos.below().getCenter(),Direction.UP,pos.below(),false)));
            h.assertTrue(player.getMainHandItem().isEmpty(),"Placement did not consume exactly one bound stone");var state=h.getLevel().getBlockState(pos);
            var stone=(StandingStoneEntity)h.getLevel().getBlockEntity(pos);stone.configure(key,"Standing Stone",route);
            var restored=new StandingStoneEntity(pos,state);restored.loadWithComponents(stone.saveWithFullMetadata(h.getLevel().registryAccess()),h.getLevel().registryAccess());
            h.assertTrue(restored.key().equals(key) && restored.payment().orElseThrow()==route && restored.id().equals(stone.id()),"Saved route changed endpoint");
            h.getLevel().destroyBlock(pos.above(route.ordinal()%2),true);
            var drops=h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(2));
            h.assertTrue(drops.size()==1 && StandingStones.payment(drops.getFirst().getItem()).orElseThrow()==route
                    && StandingStones.material(drops.getFirst().getItem()).orElseThrow()==material && StandingStones.key(drops.getFirst().getItem()).orElseThrow().equals(key),"Mined output lost its key or route");
            h.assertTrue(!drops.getFirst().getItem().has(DataComponents.CUSTOM_NAME),"Default endpoint label masked adjective");drops.forEach(ItemEntity::discard);
        }
        h.succeed();
    }
    @GameTest(template="empty_9x3x9",batch="stone_imbuements")
    public static void unboundAndMalformedRouteDropsPreserveTheirRouteWithoutJoiningAnyNetwork(GameTestHelper h) {
        var pos=h.absolutePos(CENTER);floor(h,pos);
        for(var route:StandingStonePayment.values()) {
            var stone=place(h,pos,"",route);var drops=Block.getDrops(stone.getBlockState(),h.getLevel(),pos,stone);
            h.assertTrue(drops.size()==1 && StandingStones.key(drops.getFirst()).isEmpty() && StandingStones.payment(drops.getFirst()).orElseThrow()==route,"Unbound preview lost route or invented a key");
            h.assertTrue(StoneDirectory.get(h.getLevel().getServer()).network().get(stone.id()).isEmpty(),"Unbound preview joined network");
        }
        var stone=place(h,pos,key(),null);var drops=Block.getDrops(stone.getBlockState(),h.getLevel(),pos,stone);
        h.assertTrue(StandingStones.payment(drops.getFirst()).isEmpty() && StoneDirectory.get(h.getLevel().getServer()).network().get(stone.id()).isEmpty(),"Malformed route gained fallback after mining");h.succeed();
    }
    private record Pair(StandingStoneEntity source,StandingStoneEntity target,Vec3 start) { }
    private static Pair pair(GameTestHelper h) {
        var source=h.absolutePos(new BlockPos(2,1,2));var target=source.east(6);floor(h,source);floor(h,target);String key=key();
        return new Pair(place(h,source,key,StandingStonePayment.EXPERIENCE),place(h,target,key,StandingStonePayment.HEALTH),source.north().getBottomCenter());
    }
    private static void fund(SurvivalTestPlayer player,Pair pair,StandingStonePayment route) {
        pair.source().configure(pair.source().key(),"Standing Stone",route);player.setPos(pair.start());player.setHealth(20);player.getFoodData().setFoodLevel(20);
        player.setExperienceLevels(0);player.setExperiencePoints(0);player.giveExperiencePoints(100);NativeMana.set(player,100);StoneTravel.open(player,pair.source(),0);
    }
    @GameTest(template="empty_9x3x9",batch="stone_imbuement_travel")
    public static void departureAloneDeterminesExactPaymentAndAConsumedSessionCannotBeReplayed(GameTestHelper h) {
        var pair=pair(h);try(var player=SurvivalTestPlayer.create(h)) {
            for(var route:StandingStonePayment.values()) {
                fund(player,pair,route);var quote=StandingStoneFare.quote(pair.source().getBlockPos(),pair.target().getBlockPos(),route);
                var view=StoneTravel.view(player,pair.source(),0);var buffer=new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),h.getLevel().registryAccess());
                try {StoneTravelPayloads.View.CODEC.encode(buffer,view);h.assertTrue(StoneTravelPayloads.View.CODEC.decode(buffer).equals(view),"Typed quote or affordability lost in codec");}finally{buffer.release();}
                StoneTravel.travel(player,pair.source().id(),pair.target().id());
                h.assertTrue(player.position().distanceToSqr(pair.start())>1,"Funded travel failed "+route);
                int amount=quote.amount();h.assertTrue(PlayerExperience.available(player)==100-((route==StandingStonePayment.EXPERIENCE || route==StandingStonePayment.ERUDITE)?amount:0)
                        && NativeMana.amount(player)==100-(route==StandingStonePayment.MANA?amount:quote.manaAmount()) && player.getFoodData().getFoodLevel()==20-(route==StandingStonePayment.HUNGER?amount:0)
                        && player.getHealth()==20-(route==StandingStonePayment.HEALTH?amount:0),"Wrong typed payment "+route);
                var after=player.position();StoneTravel.travel(player,pair.source().id(),pair.target().id());h.assertTrue(player.position().equals(after),"Session replayed");
            }
        }h.succeed();
    }
    @GameTest(template="empty_9x3x9",batch="stone_imbuement_travel")
    public static void eruditeRequiresBothResourcesAndPaysExactBalancesTogether(GameTestHelper h) {
        var pair=pair(h);try(var player=SurvivalTestPlayer.create(h)) {
            for(boolean missingMana:List.of(true,false)) {
                fund(player,pair,StandingStonePayment.ERUDITE);var quote=StandingStoneFare.quote(pair.source().getBlockPos(),pair.target().getBlockPos(),StandingStonePayment.ERUDITE);
                player.setExperienceLevels(0);player.setExperiencePoints(0);player.giveExperiencePoints(quote.amount()-(missingMana?0:1));
                NativeMana.set(player,quote.manaAmount()-(missingMana?1:0));
                long xp=PlayerExperience.available(player);double mana=NativeMana.amount(player);var recovery=player.getPersistentData().get("vestige:mana_recovery");
                h.assertTrue(!quote.affordable(player) && !StoneTravel.view(player,pair.source(),0).destinations().getFirst().affordable(),"Erudite ignored one missing resource");
                StoneTravel.travel(player,pair.source().id(),pair.target().id());
                h.assertTrue(player.position().equals(pair.start()) && PlayerExperience.available(player)==xp && NativeMana.amount(player)==mana
                        && Objects.equals(recovery,player.getPersistentData().get("vestige:mana_recovery")),"Rejected Erudite trip spent part of its payment");
            }
            fund(player,pair,StandingStonePayment.ERUDITE);var quote=StandingStoneFare.quote(pair.source().getBlockPos(),pair.target().getBlockPos(),StandingStonePayment.ERUDITE);
            player.setExperienceLevels(0);player.setExperiencePoints(0);player.giveExperiencePoints(quote.amount());NativeMana.set(player,quote.manaAmount());
            h.assertTrue(quote.affordable(player),"Exact paired balances rejected");StoneTravel.travel(player,pair.source().id(),pair.target().id());
            h.assertTrue(player.position().distanceToSqr(pair.start())>1 && PlayerExperience.available(player)==0 && NativeMana.amount(player)==0,"Exact paired payment failed");
        }h.succeed();
    }
    @GameTest(template="empty_9x3x9",batch="stone_imbuement_travel")
    public static void insufficientResourcesAndLethalHealthAreRejectedButHungerMayReachZero(GameTestHelper h) {
        var pair=pair(h);try(var player=SurvivalTestPlayer.create(h)) {
            for(var route:StandingStonePayment.values()) {
                fund(player,pair,route);var quote=StandingStoneFare.quote(pair.source().getBlockPos(),pair.target().getBlockPos(),route);
                switch(route) {case EXPERIENCE,ERUDITE -> {player.setExperienceLevels(0);player.setExperiencePoints(0);}case MANA -> NativeMana.set(player,quote.amount()-1);case HUNGER -> player.getFoodData().setFoodLevel(quote.amount()-1);case HEALTH -> player.setHealth(quote.amount());}
                h.assertTrue(!quote.affordable(player),"Insufficient route appears affordable");StoneTravel.travel(player,pair.source().id(),pair.target().id());h.assertTrue(player.position().equals(pair.start()),"Unaffordable travel moved player "+route);
            }
            fund(player,pair,StandingStonePayment.HUNGER);var quote=StandingStoneFare.quote(pair.source().getBlockPos(),pair.target().getBlockPos(),StandingStonePayment.HUNGER);player.getFoodData().setFoodLevel(quote.amount());
            StoneTravel.travel(player,pair.source().id(),pair.target().id());h.assertTrue(player.getFoodData().getFoodLevel()==0 && player.position().distanceToSqr(pair.start())>1,"Exact hunger payment failed");
            fund(player,pair,StandingStonePayment.HEALTH);player.setGameMode(GameType.CREATIVE);player.setHealth(1);StoneTravel.travel(player,pair.source().id(),pair.target().id());h.assertTrue(player.getHealth()==1 && player.position().distanceToSqr(pair.start())>1,"Creative charged health");
        }h.succeed();
    }
    @GameTest(template="empty_9x3x9",batch="stone_imbuement_cancel_travel")
    public static void canceledRedirectedAndThrowingTeleportsRefundEveryRouteAndManaRecoveryDelay(GameTestHelper h) {
        var pair=pair(h);try(var player=SurvivalTestPlayer.create(h)) {
            for(var route:StandingStonePayment.values())for(int mode=0;mode<3;mode++) {
                fund(player,pair,route);var before=player.getPersistentData().copy();int action=mode;
                Consumer<EntityTeleportEvent> listener=e->{if(e.getEntity()==player){if(action==0)e.setCanceled(true);else if(action==1)e.setTargetX(e.getTargetX()+1);else throw new IllegalStateException("Stone test cancellation");}};
                NeoForge.EVENT_BUS.addListener(listener);
                try {StoneTravel.travel(player,pair.source().id(),pair.target().id());}catch(IllegalStateException failure){h.assertTrue(action==2 && failure.getMessage().equals("Stone test cancellation"),"Unexpected exception");}finally {NeoForge.EVENT_BUS.unregister(listener);}
                h.assertTrue(player.position().equals(pair.start()) && PlayerExperience.available(player)==100 && NativeMana.amount(player)==100 && player.getFoodData().getFoodLevel()==20 && player.getHealth()==20,"Canceled travel charged "+route);
                h.assertTrue(Objects.equals(before.get("vestige:mana_recovery"),player.getPersistentData().get("vestige:mana_recovery")),"Canceled mana altered recovery delay");
            }
        }h.succeed();
    }
    @GameTest(template="empty_9x3x9",batch="stone_imbuement_cancel_travel")
    public static void eruditeXpCallbacksCannotWaiveManaOrCommitPartialPayment(GameTestHelper h) {
        var pair=pair(h);try(var player=SurvivalTestPlayer.create(h)) {
            for(boolean cancel:List.of(true,false)) {
                fund(player,pair,StandingStonePayment.ERUDITE);var before=player.getPersistentData().copy();
                Consumer<PlayerXpEvent.XpChange> listener=e->{if(e.getEntity()==player && e.getAmount()<0){if(cancel)e.setCanceled(true);else NativeMana.set(player,0);}};
                NeoForge.EVENT_BUS.addListener(listener);try {StoneTravel.travel(player,pair.source().id(),pair.target().id());}finally {NeoForge.EVENT_BUS.unregister(listener);}
                h.assertTrue(player.position().equals(pair.start()) && PlayerExperience.available(player)==100 && NativeMana.amount(player)==100
                        && Objects.equals(before.get("vestige:mana_recovery"),player.getPersistentData().get("vestige:mana_recovery")),"XP callback waived mana or left a partial debit");
            }
        }h.succeed();
    }
    @GameTest(template="empty_9x3x9",batch="stone_imbuement_cancel_travel")
    public static void xpCallbacksCannotReenterOrChangeTheQuotedRouteDuringCommit(GameTestHelper h) {
        var pair=pair(h);try(var player=SurvivalTestPlayer.create(h)) {
            for(var route:List.of(StandingStonePayment.EXPERIENCE,StandingStonePayment.ERUDITE)) {
            fund(player,pair,route);int[] callbacks={0};var before=player.getPersistentData().copy();
            Consumer<PlayerXpEvent.XpChange> listener=e->{if(e.getEntity()==player && e.getAmount()<0) {callbacks[0]++;StoneTravel.travel(player,pair.source().id(),pair.target().id());pair.source().configure(pair.source().key(),"Standing Stone",StandingStonePayment.HEALTH);}};
            NeoForge.EVENT_BUS.addListener(listener);try {StoneTravel.travel(player,pair.source().id(),pair.target().id());}finally {NeoForge.EVENT_BUS.unregister(listener);}
            h.assertTrue(callbacks[0]==1 && PlayerExperience.available(player)==100 && NativeMana.amount(player)==100 && player.position().equals(pair.start())
                    && Objects.equals(before.get("vestige:mana_recovery"),player.getPersistentData().get("vestige:mana_recovery")),"Reentrant or stale quote consumed resources");
            StoneTravel.travel(player,pair.source().id(),pair.target().id());h.assertTrue(player.position().equals(pair.start()),"Stale route session accepted");
            }
            pair.source().configure(pair.source().key(),"Standing Stone",null);StoneTravel.open(player,pair.source(),0);StoneTravel.travel(player,pair.source().id(),pair.target().id());h.assertTrue(player.position().equals(pair.start()),"Malformed route opened travel");
        }h.succeed();
    }
}
