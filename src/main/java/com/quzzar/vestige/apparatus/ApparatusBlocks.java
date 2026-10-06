package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.VestigeMainMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/** Standalone apparatus construction and item display; discovery recipes remain separate. */
@EventBusSubscriber(modid = VestigeMainMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ApparatusBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(VestigeMainMod.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(VestigeMainMod.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, VestigeMainMod.MOD_ID);
    public static final Map<ApparatusMaterials, DeferredBlock<ApparatusBlock>> SPELLSTONES = variants(ApparatusBlock.Role.SPELLSTONE);
    public static final Map<ApparatusMaterials, DeferredBlock<ApparatusBlock>> PLINTHS = variants(ApparatusBlock.Role.PLINTH);
    public static final DeferredBlock<ApparatusBlock> SPELLSTONE = SPELLSTONES.get(ApparatusMaterials.STONE_BRICKS);
    public static final DeferredBlock<ApparatusBlock> PLINTH = PLINTHS.get(ApparatusMaterials.STONE_BRICKS);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<OfferingBlockEntity>> OFFERING = ENTITIES.register(
            "offering", () -> BlockEntityType.Builder.of(OfferingBlockEntity::new,
                    all().toArray(Block[]::new)).build(null));

    private ApparatusBlocks() { }

    private static Map<ApparatusMaterials, DeferredBlock<ApparatusBlock>> variants(ApparatusBlock.Role role) {
        var variants = new EnumMap<ApparatusMaterials, DeferredBlock<ApparatusBlock>>(ApparatusMaterials.class);
        for (var material : ApparatusMaterials.values()) variants.put(material, block(material.blockName(role), role));
        return java.util.Collections.unmodifiableMap(variants);
    }

    public static List<ApparatusBlock> all() {
        return Stream.concat(SPELLSTONES.values().stream(), PLINTHS.values().stream()).map(DeferredBlock::get).toList();
    }

    public static boolean isSpellstone(BlockState state) {
        return state.getBlock() instanceof ApparatusBlock block && block.role() == ApparatusBlock.Role.SPELLSTONE;
    }

    public static boolean isPlinth(BlockState state) {
        return state.getBlock() instanceof ApparatusBlock block && block.role() == ApparatusBlock.Role.PLINTH;
    }

    private static DeferredBlock<ApparatusBlock> block(String name, ApparatusBlock.Role role) {
        var block = BLOCKS.register(name, () -> new ApparatusBlock(role,
                BlockBehaviour.Properties.ofFullCopy(Blocks.CHISELED_STONE_BRICKS).noOcclusion()
                        .isSuffocating((state, level, pos) -> false).isViewBlocking((state, level, pos) -> false)));
        ITEMS.registerSimpleBlockItem(name, block);
        return block;
    }

    @SubscribeEvent
    public static void creativeContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            all().forEach(event::accept);
        }
    }
}
