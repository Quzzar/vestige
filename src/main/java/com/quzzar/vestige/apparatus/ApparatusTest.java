package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.VestigeMainMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import java.util.List;

@GameTestHolder(VestigeMainMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ApparatusTest {
    private ApparatusTest() { }

    @GameTest(template = "empty_3x3x3", batch = "apparatus_placement")
    public static void blockItemsPlaceWithMatchingCollisionAndPickaxeDrops(GameTestHelper h) {
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        var blocks = ApparatusBlocks.all();
        for (int i = 0; i < blocks.size(); i++) {
            BlockPos relative = new BlockPos(1, 1, 1), ground = relative.below(), absolute = h.absolutePos(ground);
            h.setBlock(ground, Blocks.STONE);
            // The test structure frame can occupy edge cells; placement needs clear space.
            h.setBlock(relative, Blocks.AIR);
            ItemStack stack = new ItemStack(blocks.get(i));
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            var interaction = stack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false)));
            var state = h.getBlockState(relative);
            h.assertTrue(state.is(blocks.get(i)) && player.getMainHandItem().isEmpty(),
                    "Block item did not place/consume: " + i + ", result=" + interaction + ", state=" + state + ", held=" + player.getMainHandItem());
            h.assertTrue(h.getBlockEntity(relative) instanceof OfferingBlockEntity, "Placed apparatus has no offering state");
            var bounds = state.getCollisionShape(h.getLevel(), h.absolutePos(relative)).bounds();
            var block=blocks.get(i);
            h.assertTrue(Math.abs(bounds.maxY-block.physicalHeight())<1e-8 && bounds.maxY>=block.offeringHeight(), "Offering surface exceeds collision or model height");
            double expectedWidth=12.0/16;
            double expectedHeight=block.role()==ApparatusBlock.Role.SPELLSTONE ? 10.25/16 : 14.0/16;
            h.assertTrue(Math.abs(bounds.getXsize()-expectedWidth)<1e-8 && Math.abs(bounds.maxY-expectedHeight)<1e-8, "Collision differs from approved model bounds");
            h.assertTrue(state.is(BlockTags.MINEABLE_WITH_PICKAXE), "Apparatus is missing pickaxe mining tag");
            var drops = Block.getDrops(state, h.getLevel(), h.absolutePos(relative), h.getBlockEntity(relative), player, new ItemStack(Items.DIAMOND_PICKAXE));
            h.assertTrue(drops.size() == 1 && drops.getFirst().is(blocks.get(i).asItem()), "Apparatus has no matching block drop");
        }
        h.succeed();
    }

    @GameTest(template = "empty_3x3x3", batch = "apparatus_tent_collision")
    public static void tentOpeningIsClearWhileBothSupportsAndFlatTopRemainSolid(GameTestHelper h) {
        BlockPos pos=new BlockPos(1,1,1),absolute=h.absolutePos(pos);
        for(var variant:ApparatusBlocks.SPELLSTONES.values()) {
            h.setBlock(pos,variant.get());var state=h.getBlockState(pos);
            var collision=state.getCollisionShape(h.getLevel(),absolute);
            var outline=state.getShape(h.getLevel(),absolute);
            for(var shape:List.of(collision,outline)) {
                for(double height:List.of(.5,1.5,2.5,4.5)) {
                    var start=new Vec3(absolute.getX()+.5,absolute.getY()+height/16,absolute.getZ()-1);
                    h.assertTrue(shape.clip(start,start.add(0,0,3),absolute)==null,
                            "Tent opening blocks movement or selection at height "+height);
                }
                for(double x:List.of(5.0,11.0)) {
                    var start=new Vec3(absolute.getX()+x/16,absolute.getY()+1.5/16,absolute.getZ()-1);
                    h.assertTrue(shape.clip(start,start.add(0,0,3),absolute)!=null,"An angled support has no collision/selection");
                }
                var above=Vec3.atLowerCornerOf(absolute).add(.5,1,.5);
                var hit=shape.clip(above,above.add(0,-1,0),absolute);
                h.assertTrue(hit!=null && hit.getDirection()==Direction.UP
                        && Math.abs(hit.getLocation().y-absolute.getY()-variant.get().offeringHeight())<1e-8,
                        "Top ray does not meet the flat scroll surface");
            }
        }
        h.succeed();
    }

    @GameTest(template = "empty_9x3x9", batch = "apparatus_tent_collision")
    public static void walkingStepsOntoASlabButRequiresAJumpForTheSpellstone(GameTestHelper h) {
        BlockPos pos=new BlockPos(4,1,4),absolute=h.absolutePos(pos);
        for(int x=2;x<=6;x++)for(int z=2;z<=6;z++)h.setBlock(new BlockPos(x,0,z),Blocks.STONE);
        var player=h.makeMockPlayer(GameType.SURVIVAL);
        Vec3 start=Vec3.atLowerCornerOf(absolute).add(.5,0,-.5);
        h.setBlock(pos,Blocks.STONE_SLAB);
        player.setPos(start);player.setOnGround(true);
        player.move(net.minecraft.world.entity.MoverType.SELF,new Vec3(0,-.01,.8));
        h.assertTrue(Math.abs(player.getY()-start.y-.5)<1e-6,"Walking control did not step onto a half slab");
        for(var variant:ApparatusBlocks.SPELLSTONES.values()) {
            h.setBlock(pos,variant.get());
            for(var direction:List.of(new Vec3(0,0,1),new Vec3(0,0,-1),new Vec3(1,0,0),new Vec3(-1,0,0))) {
                Vec3 approach=Vec3.atLowerCornerOf(absolute).add(.5,0,.5).subtract(direction);
                player.setPos(approach);player.setOnGround(true);
                player.move(net.minecraft.world.entity.MoverType.SELF,direction.scale(.8).add(0,-.01,0));
                h.assertTrue(Math.abs(player.getY()-approach.y)<1e-6 && player.position().distanceTo(approach)<.7,
                        "Walking stepped onto the taller Spellstone from "+direction);
            }
        }
        h.succeed();
    }

    @GameTest(template = "empty_3x3x3", batch = "apparatus_offering")
    public static void survivalOffersOneItemRejectsOverwriteAndReturnsIt(GameTestHelper h) {
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        for (var block : ApparatusBlocks.all()) {
            BlockPos pos = new BlockPos(1, 1, 1);
            h.setBlock(pos, block);
            ItemStack source = new ItemStack(Items.AMETHYST_SHARD, 3);
            source.set(DataComponents.CUSTOM_NAME, Component.literal("Preserved offering"));
            player.setItemInHand(InteractionHand.MAIN_HAND, source);
            h.useBlock(pos, player);
            var offering = (OfferingBlockEntity) h.getBlockEntity(pos);
            h.assertTrue(source.getCount() == 2 && offering.displayedItem().getCount() == 1, "Offering did not consume exactly one item");
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND, 2));
            h.useBlock(pos, player);
            h.assertTrue(player.getMainHandItem().getCount() == 2 && offering.displayedItem().is(Items.AMETHYST_SHARD), "Occupied offering was overwritten");
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            h.useBlock(pos, player);
            h.assertTrue(offering.displayedItem().isEmpty(), "Empty-hand retrieval did not clear the offering");
            h.assertTrue(player.getInventory().items.stream().anyMatch(item -> item.getCount() == 1 && ItemStack.isSameItemSameComponents(item, source)), "Returned item lost its components");
            player.getInventory().clearContent();
        }
        h.succeed();
    }

    @GameTest(template = "empty_3x3x3", batch = "apparatus_persistence")
    public static void savedOfferingKeepsComponentsAndBreakingDropsItOnce(GameTestHelper h) {
        BlockPos pos = new BlockPos(1, 1, 1);
        h.setBlock(pos, ApparatusBlocks.PLINTH.get());
        var original = (OfferingBlockEntity) h.getBlockEntity(pos);
        ItemStack item = new ItemStack(Items.DIAMOND);
        item.set(DataComponents.CUSTOM_NAME, Component.literal("Recovered relic"));
        original.insert(item);
        var registries = h.getLevel().registryAccess();
        var restored = new OfferingBlockEntity(h.absolutePos(pos), h.getBlockState(pos));
        restored.loadWithComponents(original.saveWithFullMetadata(registries), registries);
        h.assertTrue(ItemStack.matches(restored.displayedItem(), item), "Persisted offering lost components/count");
        var clientCopy = new OfferingBlockEntity(h.absolutePos(pos), h.getBlockState(pos));
        clientCopy.loadWithComponents(original.getUpdateTag(registries), registries);
        h.assertTrue(ItemStack.matches(clientCopy.displayedItem(), item), "Initial client sync lost offering data");
        h.setBlock(pos, Blocks.AIR);
        var drops = h.getLevel().getEntitiesOfClass(ItemEntity.class, h.getBounds()).stream().filter(entity -> entity.getItem().is(Items.DIAMOND)).toList();
        h.assertTrue(drops.size() == 1 && ItemStack.matches(drops.getFirst().getItem(), item), "Removing apparatus lost or duplicated its offering");
        h.succeed();
    }

    @GameTest(template = "empty_3x3x3", batch = "apparatus_recipes")
    public static void stackingAPlinthEjectsTheOfferingOnceAndPreservesItsSocket(GameTestHelper h) {
        var player=h.makeMockPlayer(GameType.SURVIVAL);
        BlockPos lower=new BlockPos(1,1,1), upper=lower.above(), absolute=h.absolutePos(lower);
        h.setBlock(upper,Blocks.AIR);
        h.setBlock(lower,ApparatusBlocks.PLINTH.get());
        var node=(OfferingBlockEntity)h.getBlockEntity(lower);
        var item=new ItemStack(Items.PAPER);
        item.set(DataComponents.CUSTOM_NAME,Component.literal("Covered offering"));
        h.assertTrue(node.insert(item) && node.installMaterial(new ItemStack(Items.GOLD_BLOCK)),"Offering/socket fixture rejected");
        var plinth=new ItemStack(ApparatusBlocks.PLINTHS.get(ApparatusMaterials.TUFF).get());
        player.setItemInHand(InteractionHand.MAIN_HAND,plinth);
        plinth.useOn(new UseOnContext(player,InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atLowerCornerOf(absolute).add(.5,14.0/16,.5),Direction.UP,absolute,false)));
        h.assertTrue(ApparatusBlocks.isPlinth(h.getBlockState(upper)) && plinth.isEmpty(),"Second Plinth did not place above occupied surface");
        h.assertTrue(node.displayedItem().isEmpty(),"Covered Plinth retained its hidden offering instead of ejecting it");
        var drops=h.getLevel().getEntitiesOfClass(ItemEntity.class,h.getBounds()).stream()
                .filter(entity -> ItemStack.isSameItemSameComponents(entity.getItem(),item)).toList();
        h.assertTrue(drops.size()==1 && drops.getFirst().getItem().getCount()==1,"Stacking lost, duplicated or changed the offering");
        h.assertTrue(node.materialItem().is(Items.GOLD_BLOCK),"Stacking ejected the embedded imbuement");
        h.setBlock(upper,Blocks.AIR);
        h.assertTrue(node.displayedItem().isEmpty() && node.hasOfferingSpace(),"Uncovering restored a hidden duplicate or failed to expose the slot");
        h.setBlock(upper,ApparatusBlocks.PLINTH.get());
        h.assertTrue(h.getLevel().getEntitiesOfClass(ItemEntity.class,h.getBounds()).stream()
                .filter(entity -> ItemStack.isSameItemSameComponents(entity.getItem(),item)).count()==1,"Repeated stacking duplicated the offering drop");
        h.succeed();
    }

    @GameTest(template = "empty_3x3x3", batch = "apparatus_recipes")
    public static void allSeventyTwoRecipesUseMatchingBlocksAndSlabsAndCorrectOutputCounts(GameTestHelper h) {
        for (var material : ApparatusMaterials.values()) for (var role : ApparatusBlock.Role.values()) {
            var b=BuiltInRegistries.ITEM.get(material.body()); var s=BuiltInRegistries.ITEM.get(material.slab());
            h.assertTrue(b!=Items.AIR && s!=Items.AIR, "Missing vanilla full block/slab: "+material.id());
            var grid=(role==ApparatusBlock.Role.SPELLSTONE
                    ? List.of(Items.DIAMOND,s,Items.DIAMOND,b,Items.AMETHYST_BLOCK,b,b,b,b)
                    : List.of(s,b,s,Items.AIR,b,Items.AIR,s,b,s)).stream().map(ItemStack::new).toList();
            var input = CraftingInput.of(3, 3, grid);
            var recipe = h.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, h.getLevel()).orElseThrow();
            h.assertTrue(recipe.id().equals(VestigeMainMod.location(material.blockName(role))), "Wrong construction finish: "+material.id());
            var result = recipe.value().assemble(input, h.getLevel().registryAccess());
            var output=(role==ApparatusBlock.Role.SPELLSTONE ? ApparatusBlocks.SPELLSTONES : ApparatusBlocks.PLINTHS).get(material).get();
            h.assertTrue(result.is(output.asItem()) && result.getCount() == (role==ApparatusBlock.Role.PLINTH ? 2 : 1), "Wrong apparatus/count");
            var mixed=new java.util.ArrayList<>(grid);
            mixed.set(role==ApparatusBlock.Role.SPELLSTONE ? 1 : 0, new ItemStack(material==ApparatusMaterials.STONE ? Items.TUFF_SLAB : Items.STONE_SLAB));
            h.assertTrue(h.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, CraftingInput.of(3,3,mixed), h.getLevel()).isEmpty(), "Mixed slabs incorrectly accepted");
        }
        h.succeed();
    }

    @GameTest(template = "empty_3x3x3", batch = "apparatus_variant_storage")
    public static void allVariantsPersistIndependentSocketsReferencesAndResults(GameTestHelper h) {
        BlockPos pos=new BlockPos(1,1,1);var registries=h.getLevel().registryAccess();
        for (var block : ApparatusBlocks.all()) {
            h.setBlock(pos,block);var original=(OfferingBlockEntity)h.getBlockEntity(pos);
            var offered=new ItemStack(Items.PAPER);offered.set(DataComponents.CUSTOM_NAME,Component.literal("Reference"));
            original.insert(offered);
            if (block.role()==ApparatusBlock.Role.PLINTH) h.assertTrue(original.installMaterial(new ItemStack(Items.GOLD_BLOCK)),"Variant rejects imbuement");
            else h.assertTrue(original.insertResult(new ItemStack(Items.DIAMOND)),"Variant rejects result");
            var restored=new OfferingBlockEntity(h.absolutePos(pos),h.getBlockState(pos));
            restored.loadWithComponents(original.saveWithFullMetadata(registries),registries);
            var client=new OfferingBlockEntity(h.absolutePos(pos),h.getBlockState(pos));
            client.loadWithComponents(original.getUpdateTag(registries),registries);
            for (var copy : List.of(restored,client)) {
                h.assertTrue(ItemStack.matches(copy.displayedItem(),offered),"Reference lost components");
                h.assertTrue(block.role()==ApparatusBlock.Role.PLINTH ? copy.materialItem().is(Items.GOLD_BLOCK) && copy.resultItem().isEmpty() : copy.resultItem().is(Items.DIAMOND) && copy.materialItem().isEmpty(),"Variant storage/sync differs");
            }
            original.remove();original.removeMaterial();original.removeResult();
        }
        h.succeed();
    }

    @GameTest(template = "empty_3x3x3", batch = "apparatus_imbuement_rejection")
    public static void everyPlinthRejectsUnsupportedSocketsWithoutConsumingOrCreatingOfferings(GameTestHelper h) {
        var player=h.makeMockPlayer(GameType.SURVIVAL);
        BlockPos pos=new BlockPos(1,1,1), absolute=h.absolutePos(pos);
        var side=new BlockHitResult(Vec3.atCenterOf(absolute),Direction.NORTH,absolute,false);
        var top=new BlockHitResult(Vec3.atCenterOf(absolute),Direction.UP,absolute,false);
        for(var variant:ApparatusBlocks.PLINTHS.values()) {
            h.setBlock(pos,variant.get());var node=(OfferingBlockEntity)h.getBlockEntity(pos);
            h.setBlock(pos.north(),Blocks.AIR);h.setBlock(pos.north().below(),Blocks.STONE);
            for(var item:List.of(Items.DIRT,Items.OAK_PLANKS,Items.STONE_BRICKS,Items.RED_CARPET,Items.BLUE_CONCRETE_POWDER,variant.get().asItem())) {
                var source=new ItemStack(item,3);player.setItemInHand(InteractionHand.MAIN_HAND,source);
                h.assertTrue(!node.installMaterial(source),"Unsupported direct socket installation accepted: "+item);
                var result=h.getBlockState(pos).useItemOn(source,h.getLevel(),player,InteractionHand.MAIN_HAND,side);
                h.assertTrue(result==ItemInteractionResult.CONSUME_PARTIAL && source.getCount()==3 && node.materialItem().isEmpty() && node.displayedItem().isEmpty(),
                        "Unsupported side click consumed, installed or offered: "+item);
                h.useBlock(pos,player,side);
                h.assertTrue(source.getCount()==3 && h.getBlockState(pos.north()).isAir() && node.materialItem().isEmpty() && node.displayedItem().isEmpty(),
                        "Unsupported socket click fell through into vanilla block placement: "+item);
            }
            h.assertTrue(!node.installMaterial(new ItemStack(Items.DIAMOND)),"Non-block item accepted as imbuement");
            var offered=new ItemStack(Items.DIRT,2);player.setItemInHand(InteractionHand.MAIN_HAND,offered);
            h.getBlockState(pos).useItemOn(offered,h.getLevel(),player,InteractionHand.MAIN_HAND,top);
            h.assertTrue(offered.getCount()==1 && node.displayedItem().is(Items.DIRT) && node.materialItem().isEmpty(),
                    "Socket restriction also rejected or imbued a top ingredient");
            node.remove();
        }
        h.succeed();
    }

    @GameTest(template = "empty_3x3x3", batch = "apparatus_imbuement_acceptance")
    public static void everyAuthoredImbuementInstallsExactlyOneAndOccupiedOrLockedSocketsReject(GameTestHelper h) {
        var player=h.makeMockPlayer(GameType.SURVIVAL);
        BlockPos pos=new BlockPos(1,1,1), absolute=h.absolutePos(pos);
        h.setBlock(pos,ApparatusBlocks.PLINTH.get());var node=(OfferingBlockEntity)h.getBlockEntity(pos);
        var side=new BlockHitResult(Vec3.atCenterOf(absolute),Direction.NORTH,absolute,false);
        var materials=new java.util.LinkedHashSet<>(Spellshaping.rules().values().stream().flatMap(rule -> rule.pairs().stream())
                .map(Spellshaping.Pair::material).toList());
        h.assertTrue(materials.size()==26,"Unexpected authored imbuement set");
        for(var color:DyeColor.values()) for(var type:List.of("wool","concrete"))
            materials.add(net.minecraft.resources.ResourceLocation.withDefaultNamespace(color.getName()+"_"+type));
        h.assertTrue(materials.size()==56,"Unexpected imbuement/color variant set");
        for(var material:materials) {
            var source=new ItemStack(BuiltInRegistries.ITEM.get(material),3);
            source.set(DataComponents.CUSTOM_NAME,Component.literal("Retained imbuement"));
            player.setItemInHand(InteractionHand.MAIN_HAND,source);
            h.assertTrue(node.canInstallMaterial(source),"Supported material rejected: "+material);
            h.getBlockState(pos).useItemOn(source,h.getLevel(),player,InteractionHand.MAIN_HAND,side);
            h.assertTrue(source.getCount()==2 && node.materialItem().getCount()==1 && ItemStack.isSameItemSameComponents(source,node.materialItem()) && node.displayedItem().isEmpty(),
                    "Supported material lost quantity/components or became an offering: "+material);
            var restored=new OfferingBlockEntity(absolute,h.getBlockState(pos));
            restored.loadWithComponents(node.saveWithFullMetadata(h.getLevel().registryAccess()),h.getLevel().registryAccess());
            var client=new OfferingBlockEntity(absolute,h.getBlockState(pos));
            client.loadWithComponents(node.getUpdateTag(h.getLevel().registryAccess()),h.getLevel().registryAccess());
            h.assertTrue(ItemStack.matches(node.materialItem(),restored.materialItem()) && ItemStack.matches(node.materialItem(),client.materialItem()),
                    "Persistence or client sync changed the actual installed color: "+material);
            var replacement=new ItemStack(Items.GOLD_BLOCK,2);player.setItemInHand(InteractionHand.MAIN_HAND,replacement);
            var result=h.getBlockState(pos).useItemOn(replacement,h.getLevel(),player,InteractionHand.MAIN_HAND,side);
            h.assertTrue(result==ItemInteractionResult.CONSUME_PARTIAL && replacement.getCount()==2 && ItemStack.isSameItemSameComponents(source,node.materialItem()),
                    "Occupied socket was overwritten or charged");
            var returned=node.removeMaterial();
            h.assertTrue(returned.getCount()==1 && ItemStack.isSameItemSameComponents(source,returned),"Supported imbuement was not recoverable");
            node.lock(20);player.setItemInHand(InteractionHand.MAIN_HAND,source);
            result=h.getBlockState(pos).useItemOn(source,h.getLevel(),player,InteractionHand.MAIN_HAND,side);
            h.assertTrue(result==ItemInteractionResult.CONSUME_PARTIAL && source.getCount()==2 && !node.installMaterial(source) && node.materialItem().isEmpty(),
                    "Locked socket accepted or charged an installation");
            node.unlock();
        }
        h.setBlock(pos,ApparatusBlocks.SPELLSTONE.get());
        h.assertTrue(!((OfferingBlockEntity)h.getBlockEntity(pos)).installMaterial(new ItemStack(Items.GOLD_BLOCK)),"Spellstone accepted a Plinth imbuement");
        h.succeed();
    }
    @GameTest(template="empty_3x3x3",batch="apparatus_sounds")
    public static void offeringAndImbuementSoundsFollowSuccessfulInteractionsOnly(GameTestHelper h) {
        var pos=new BlockPos(1,1,1);h.setBlock(pos,ApparatusBlocks.PLINTH.get());var absolute=h.absolutePos(pos);
        var player=h.makeMockPlayer(GameType.SURVIVAL);var state=h.getBlockState(pos);
        var top=new BlockHitResult(Vec3.atCenterOf(absolute),Direction.UP,absolute,false);
        var side=new BlockHitResult(Vec3.atCenterOf(absolute),Direction.NORTH,absolute,false);
        var sounds=new java.util.ArrayList<net.minecraft.sounds.SoundEvent>();
        java.util.function.Consumer<net.neoforged.neoforge.event.PlayLevelSoundEvent.AtPosition> listener=event->{
            if(event.getLevel()==h.getLevel() && event.getPosition().distanceToSqr(Vec3.atCenterOf(absolute))<.01 && event.getSound()!=null)sounds.add(event.getSound().value());
        };
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(listener);
        try {
            var paper=new ItemStack(Items.PAPER);player.setItemInHand(InteractionHand.MAIN_HAND,paper);
            state.useItemOn(paper,h.getLevel(),player,InteractionHand.MAIN_HAND,top);
            player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);state.useWithoutItem(h.getLevel(),player,top);
            var gold=new ItemStack(Items.GOLD_BLOCK);player.setItemInHand(InteractionHand.MAIN_HAND,gold);
            state.useItemOn(gold,h.getLevel(),player,InteractionHand.MAIN_HAND,side);
            player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);player.setShiftKeyDown(true);state.useWithoutItem(h.getLevel(),player,side);
            var unsupported=new ItemStack(Items.DIRT);state.useItemOn(unsupported,h.getLevel(),player,InteractionHand.MAIN_HAND,side);
            state.useWithoutItem(h.getLevel(),player,side);
            h.assertTrue(sounds.equals(List.of(net.minecraft.sounds.SoundEvents.ITEM_FRAME_ADD_ITEM,net.minecraft.sounds.SoundEvents.ITEM_FRAME_REMOVE_ITEM,
                    net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_PLACE,net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_HIT)),"Wrong sound routing or rejected interaction played sound: "+sounds);
            h.succeed();
        } finally {net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(listener);}
    }

}
