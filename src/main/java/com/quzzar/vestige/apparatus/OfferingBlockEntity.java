package com.quzzar.vestige.apparatus;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** One persisted, server-owned offering; vanilla block-entity packets synchronize its display. */
public final class OfferingBlockEntity extends BlockEntity {
    private ItemStack item = ItemStack.EMPTY;
    private ItemStack material = ItemStack.EMPTY;
    private ItemStack result = ItemStack.EMPTY;
    private ItemStack hint = ItemStack.EMPTY;
    private RitualRecipe.Feedback feedback = RitualRecipe.Feedback.NONE;
    private int color = 0xac73e8;
    private long feedbackStart;
    private long feedbackEnd;
    private long lockedUntil;
    private long nextActivation;

    public OfferingBlockEntity(BlockPos pos, BlockState state) {
        super(ApparatusBlocks.OFFERING.get(), pos, state);
    }

    public ItemStack materialItem() { return material.copy(); }
    public boolean canInstallMaterial(ItemStack stack) {
        return !busy() && ApparatusBlocks.isPlinth(getBlockState()) && material.isEmpty()
                && stack.getItem() instanceof BlockItem
                && (Spellshaping.isImbuementMaterial(BuiltInRegistries.ITEM.getKey(stack.getItem()))
                    || HomeboundEyeRecipe.isPaymentMaterial(BuiltInRegistries.ITEM.getKey(stack.getItem())));
    }
    public boolean installMaterial(ItemStack stack) {
        if (!canInstallMaterial(stack)) return false;
        material=stack.copyWithCount(1); changed(); return true;
    }
    public ItemStack removeMaterial() { if (busy()) return ItemStack.EMPTY; ItemStack value=material; material=ItemStack.EMPTY; if (!value.isEmpty()) changed(); return value; }
    public ItemStack displayedItem() { return item.copy(); }
    public boolean hasOfferingSpace() {
        return level!=null && ApparatusBlock.hasOfferingSpace(getBlockState(),level,worldPosition);
    }
    public ItemStack resultItem() { return result.copy(); }
    public ItemStack hintItem() { return hint.copy(); }
    public RitualRecipe.Feedback feedback() { return level != null && level.getGameTime() < feedbackEnd ? feedback : RitualRecipe.Feedback.NONE; }
    public int feedbackColor() { return color; }
    public float feedbackAge(float partial) { return level == null ? 0 : (float) (level.getGameTime() - feedbackStart + partial); }
    public boolean busy() { return level != null && level.getGameTime() < lockedUntil; }
    public void lock(int ticks) { lockedUntil=level==null ? 0 : level.getGameTime()+ticks; changed(); }
    public void unlock() { lockedUntil=0; changed(); }
    public boolean tryActivate() {
        if (level==null || level.getGameTime()<nextActivation) return false;
        nextActivation=level.getGameTime()+10; return true;
    }
    public void feedback(RitualRecipe.Feedback kind, ItemStack clue, int rgb, int ticks) {
        feedback = kind; hint = clue.copyWithCount(1); color = rgb;
        feedbackStart = level == null ? 0 : level.getGameTime(); feedbackEnd = feedbackStart + ticks; changed();
    }
    public boolean insertResult(ItemStack stack) {
        if (!ApparatusBlocks.isSpellstone(getBlockState()) || !result.isEmpty() || stack.isEmpty()) return false;
        result = stack.copyWithCount(1); changed(); return true;
    }
    public ItemStack removeResult() { ItemStack value=result; result=ItemStack.EMPTY; if (!value.isEmpty()) changed(); return value; }

    public boolean insert(ItemStack stack) {
        if (!hasOfferingSpace() || !item.isEmpty() || stack.isEmpty()) return false;
        item = stack.copyWithCount(1);
        changed();
        return true;
    }

    public ItemStack remove() {
        ItemStack result = item;
        item = ItemStack.EMPTY;
        if (!result.isEmpty()) changed();
        return result;
    }

    private void changed() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!material.isEmpty()) tag.put("imbuement",material.save(registries));
        if (!item.isEmpty()) tag.put("offering", item.save(registries));
        if (!result.isEmpty()) tag.put("ritual_result", result.save(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        material = ItemStack.parseOptional(registries,tag.getCompound("imbuement"));
        if (!material.isEmpty()) material.setCount(1);
        item = ItemStack.parseOptional(registries, tag.getCompound("offering"));
        if (!item.isEmpty()) item.setCount(1);
        result = ItemStack.parseOptional(registries, tag.getCompound("ritual_result"));
        if (!result.isEmpty()) result.setCount(1);
        int ordinal = tag.getInt("feedback");
        feedback = ordinal >= 0 && ordinal < RitualRecipe.Feedback.values().length ? RitualRecipe.Feedback.values()[ordinal] : RitualRecipe.Feedback.NONE;
        hint = ItemStack.parseOptional(registries, tag.getCompound("hint")); color = tag.getInt("feedback_color");
        feedbackStart=tag.getLong("feedback_start"); feedbackEnd=tag.getLong("feedback_end");
        lockedUntil=tag.getLong("ritual_locked_until");
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag=saveWithoutMetadata(registries);
        tag.putInt("feedback",feedback.ordinal()); tag.putInt("feedback_color",color);
        tag.putLong("feedback_start",feedbackStart); tag.putLong("feedback_end",feedbackEnd);
        tag.putLong("ritual_locked_until",lockedUntil);
        if (!hint.isEmpty()) tag.put("hint",hint.save(registries));
        return tag;
    }
}
