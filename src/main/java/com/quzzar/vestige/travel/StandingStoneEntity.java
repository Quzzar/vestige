package com.quzzar.vestige.travel;

import net.minecraft.core.*;
import net.minecraft.core.component.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import java.util.UUID;

/** Saved endpoint identity survives chunk unload/restart; item drops retain attunement and name. */
public final class StandingStoneEntity extends BlockEntity {
    private UUID id = UUID.randomUUID();
    private String key = "";
    private String name = "Standing Stone";
    private StandingStonePayment payment = StandingStonePayment.EXPERIENCE;
    public StandingStoneEntity(BlockPos pos, BlockState state) { super(StandingStones.ENTITY.get(), pos, state); }
    public UUID id() { return id; }
    public String key() { return key; }
    public String name() { return name; }
    public java.util.Optional<StandingStonePayment> payment() { return java.util.Optional.ofNullable(payment); }
    /** Only relabel the live endpoint; its key, ID, body and connection remain unchanged. */
    public boolean rename(String label) {
        if (!(level instanceof ServerLevel) || label == null || label.length() > 64) return false;
        String value = StringUtil.filterText(label).strip();
        if (value.isBlank() || value.equals(name)) return false;
        name = value;
        setChanged(); register();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        return true;
    }
    public void configure(String value, String label) {
        configure(value, label, StandingStonePayment.EXPERIENCE);
    }
    public void configure(String value, String label, StandingStonePayment route) {
        unregister();
        key = StoneNetwork.validKey(value) ? value : "";
        payment = route;
        name = label == null || label.isBlank() ? "Standing Stone" : label.substring(0, Math.min(64, label.length())).replace('\n', ' ');
        if (level instanceof ServerLevel) {
            var state = getBlockState().setValue(StandingStoneBlock.PROFILE, StandingStoneShape.fromKey(key));
            level.setBlock(worldPosition, state, 3);
            var upper = level.getBlockState(worldPosition.above());
            if (upper.is(state.getBlock()) && upper.getValue(StandingStoneBlock.HALF) == net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER)
                level.setBlock(worldPosition.above(), state.setValue(StandingStoneBlock.HALF, net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER), 3);
        }
        setChanged(); register();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }
    private void register() {
        if (level instanceof ServerLevel server && StoneNetwork.validKey(key) && payment != null)
            StoneDirectory.get(server.getServer()).put(new StoneNetwork.Node(id, key, server.dimension().location(), worldPosition, name));
    }
    public void unregister() {
        if (level instanceof ServerLevel server) StoneDirectory.get(server.getServer()).remove(id);
    }
    @Override public void onLoad() { super.onLoad(); register(); }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries); tag.putUUID("endpoint", id); tag.putString("attunement", key); tag.putString("name", name);
        if (payment != null) payment.write(tag);
        else { tag.putInt("vestige_stone_payment_version", 1); tag.putString("vestige_stone_payment", "invalid"); }
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries); if (tag.hasUUID("endpoint")) id = tag.getUUID("endpoint");
        key = StoneNetwork.validKey(tag.getString("attunement")) ? tag.getString("attunement") : "";
        payment = StandingStonePayment.read(tag).orElse(null);
        String label = tag.getString("name"); name = label.isBlank() ? "Standing Stone" : label.substring(0, Math.min(64, label.length()));
    }
    @Override protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        CompoundTag tag = new CompoundTag();
        if (StoneNetwork.validKey(key)) tag.putString("vestige_attunement", key);
        if (payment != null) payment.write(tag);
        else { tag.putInt("vestige_stone_payment_version", 1); tag.putString("vestige_stone_payment", "invalid"); }
        builder.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        if (!name.equals("Standing Stone")) builder.set(DataComponents.CUSTOM_NAME, Component.literal(name));
    }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithoutMetadata(registries); }
}
