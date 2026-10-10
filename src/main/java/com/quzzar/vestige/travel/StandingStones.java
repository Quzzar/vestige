package com.quzzar.vestige.travel;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.ApparatusMaterials;
import com.quzzar.vestige.apparatus.AttunementMark;
import com.quzzar.vestige.apparatus.AttunementShardItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.*;
import java.util.*;

/** Native teleport stone registration and copied shard identity, independent of map mods. */
@EventBusSubscriber(modid = VestigeMainMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class StandingStones {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(VestigeMainMod.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(VestigeMainMod.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, VestigeMainMod.MOD_ID);
    public static final Map<ApparatusMaterials, DeferredBlock<StandingStoneBlock>> STONES;
    public static final Map<ApparatusMaterials, DeferredItem<BlockItem>> STONE_ITEMS;
    static {
        var blocks = new EnumMap<ApparatusMaterials, DeferredBlock<StandingStoneBlock>>(ApparatusMaterials.class);
        var items = new EnumMap<ApparatusMaterials, DeferredItem<BlockItem>>(ApparatusMaterials.class);
        for (var material : ApparatusMaterials.values()) {
            var block = BLOCKS.register(blockName(material), () -> new StandingStoneBlock(material,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS).noOcclusion()));
            blocks.put(material, block);
            items.put(material, ITEMS.register(blockName(material), () -> new BlockItem(block.get(), new Item.Properties()) {
                @Override public Component getName(ItemStack stack) {
                    return payment(stack).<Component>map(route -> com.quzzar.vestige.magic.presentation.MagicAdjectives.prefix(
                            StandingStonePayment.FAMILY, route.adjectives()).append(super.getName(stack))).orElseGet(() -> super.getName(stack));
                }
                @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> text, TooltipFlag flag) {
                    key(stack).ifPresent(value -> text.add(AttunementMark.fromKey(value).component()));
                }
            }));
        }
        STONES = Collections.unmodifiableMap(blocks);
        STONE_ITEMS = Collections.unmodifiableMap(items);
    }
    public static final DeferredBlock<StandingStoneBlock> STONE = STONES.get(ApparatusMaterials.STONE_BRICKS);
    public static final DeferredItem<BlockItem> ITEM = STONE_ITEMS.get(ApparatusMaterials.STONE_BRICKS);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<StandingStoneEntity>> ENTITY = ENTITIES.register(
            "standing_stone", () -> BlockEntityType.Builder.of(StandingStoneEntity::new,
                    STONES.values().stream().map(DeferredBlock::get).toArray(net.minecraft.world.level.block.Block[]::new)).build(null));
    private StandingStones() { }
    public static String blockName(ApparatusMaterials material) {
        return material == ApparatusMaterials.STONE_BRICKS ? "standing_stone" : material.id() + "_standing_stone";
    }
    public static Optional<ApparatusMaterials> material(ItemStack stack) {
        return stack.getItem() instanceof BlockItem item && item.getBlock() instanceof StandingStoneBlock stone
                ? Optional.of(stone.material()) : Optional.empty();
    }
    public static Optional<String> key(ItemStack stack) {
        if (material(stack).isEmpty()) return Optional.empty();
        String value = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getString("vestige_attunement");
        return StoneNetwork.validKey(value) ? Optional.of(value) : Optional.empty();
    }
    public static Optional<StandingStonePayment> payment(ItemStack stack) {
        return material(stack).isEmpty() ? Optional.empty() : StandingStonePayment.read(
                stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
    }
    /** The device inherits the existing shard key. Its own ingredients do not change it. */
    public static ItemStack fromShard(ItemStack shard) {
        return fromShard(shard, ApparatusMaterials.STONE_BRICKS);
    }
    public static ItemStack fromShard(ItemStack shard, ApparatusMaterials material) {
        return bound(AttunementShardItem.signature(shard).orElseThrow(() -> new IllegalArgumentException("An attuned shard is required")).key(), material);
    }
    public static ItemStack bound(String key) {
        return bound(key, ApparatusMaterials.STONE_BRICKS);
    }
    public static ItemStack bound(String key, ApparatusMaterials material) {
        return bound(key, material, StandingStonePayment.EXPERIENCE);
    }
    public static ItemStack bound(String key, ApparatusMaterials material, StandingStonePayment payment) {
        if (!StoneNetwork.validKey(key)) throw new IllegalArgumentException("Invalid attunement");
        ItemStack stack = preview(material, payment);
        CompoundTag tag = stack.get(DataComponents.CUSTOM_DATA).copyTag(); tag.putString("vestige_attunement", key);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag)); return stack;
    }
    /** Public recipe outputs disclose finish/payment only, without inventing a private network key. */
    public static ItemStack preview(ApparatusMaterials material, StandingStonePayment payment) {
        ItemStack stack = new ItemStack(STONE_ITEMS.get(material).get());
        CompoundTag tag = new CompoundTag(); payment.write(tag);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag)); return stack;
    }
    @SubscribeEvent public static void creative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS)
            for (var item : STONE_ITEMS.values()) event.accept(item.get());
    }
}
