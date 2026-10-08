package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.magic.world.NativeMana;
import com.quzzar.vestige.magic.runtime.ResourceValuation;
import com.quzzar.vestige.magic.presentation.MagicAdjectives;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.travel.PlayerExperience;
import com.quzzar.vestige.travel.StoneNetwork;
import com.quzzar.vestige.travel.NearbyTeleport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import java.util.*;

/** A fixed crafting-site address, copied attunement and independently selected payment. */
public final class HomeboundEyeItem extends Item {
    public static final int DURABILITY = 30;
    public static final int RESOURCE_RETURN_MANA = ResourceValuation.MANA_PER_HEART;
    public enum Payment {
        DURABILITY(6, 0), HEALTH(2, ResourceValuation.healthForMana(RESOURCE_RETURN_MANA)),
        HUNGER(2, ResourceValuation.foodForMana(RESOURCE_RETURN_MANA)), EXPERIENCE(2, ResourceValuation.experienceForMana(RESOURCE_RETURN_MANA)), MANA(2, RESOURCE_RETURN_MANA);
        public final int wear, cost;
        Payment(int wear, int cost) { this.wear = wear; this.cost = cost; }
        public String id() { return name().toLowerCase(Locale.ROOT); }
        public List<MagicAdjectives.Adjustment> adjectives() {
            return this == DURABILITY ? List.of() : List.of(new MagicAdjectives.Adjustment(
                    VestigeMainMod.location("homebound_eye/" + id()), 1));
        }
        public Component description() { return Component.translatable("item.vestige.homebound_eye.payment." + id()); }
    }
    public record Binding(String key, ResourceKey<Level> dimension, BlockPos origin, Payment payment) {
        public Binding { origin = origin.immutable(); if (!StoneNetwork.validKey(key)) throw new IllegalArgumentException("Invalid attunement"); }
    }
    public HomeboundEyeItem(Properties properties) { super(properties); }
    public static ItemStack bound(String key, ResourceKey<Level> dimension, BlockPos origin, Payment payment) {
        var binding = new Binding(key, dimension, origin, payment);
        ItemStack item = new ItemStack(ScrollItems.HOMEBOUND_EYE.get());
        CustomData.update(DataComponents.CUSTOM_DATA, item, tag -> {
            tag.putInt("vestige_homebound_version", 1); tag.putString("vestige_attunement", binding.key());
            tag.putString("dimension", dimension.location().toString());
            tag.putIntArray("origin", new int[]{origin.getX(), origin.getY(), origin.getZ()}); tag.putString("payment", payment.id());
        });
        return item;
    }
    public static Optional<Binding> binding(ItemStack item) {
        if (!item.is(ScrollItems.HOMEBOUND_EYE.get())) return Optional.empty();
        var tag = item.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.contains("vestige_homebound_version", Tag.TAG_INT) || tag.getInt("vestige_homebound_version") != 1
                || !tag.contains("origin", Tag.TAG_INT_ARRAY)) return Optional.empty();
        int[] xyz = tag.getIntArray("origin"); var dimension = ResourceLocation.tryParse(tag.getString("dimension"));
        if (xyz.length != 3 || dimension == null || Math.abs((long) xyz[0]) > 30_000_000 || Math.abs((long) xyz[2]) > 30_000_000) return Optional.empty();
        try { return Optional.of(new Binding(tag.getString("vestige_attunement"), ResourceKey.create(Registries.DIMENSION, dimension),
                new BlockPos(xyz[0], xyz[1], xyz[2]), Payment.valueOf(tag.getString("payment").toUpperCase(Locale.ROOT)))); }
        catch (IllegalArgumentException invalid) { return Optional.empty(); }
    }
    @Override public boolean isFoil(ItemStack item) { return binding(item).isPresent(); }
    @Override public Component getName(ItemStack item) {
        return binding(item).<Component>map(value -> MagicAdjectives.prefix(
                VestigeMainMod.location("homebound_eye"), value.payment().adjectives()).append(super.getName(item)))
                .orElseGet(() -> super.getName(item));
    }
    @Override public void appendHoverText(ItemStack item, TooltipContext context, List<Component> text, TooltipFlag flag) {
        binding(item).ifPresent(value -> {
            text.add(AttunementMark.fromKey(value.key()).component());
            text.add(Component.translatable("item.vestige.homebound_eye.destination")); text.add(value.payment().description());
        });
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack item = player.getItemInHand(hand);
        if (level.isClientSide()) return InteractionResultHolder.success(item);
        if (!(player instanceof ServerPlayer server)) return InteractionResultHolder.fail(item);
        return returnHome(server, item) ? InteractionResultHolder.success(item) : InteractionResultHolder.fail(item);
    }
    /** All validation and payment occur on one server thread; canceled travel refunds the debit. */
    public static boolean returnHome(ServerPlayer player, ItemStack item) {
        var binding = binding(item).orElse(null);
        if (binding == null) return false;
        if (!player.isAlive() || player.isSpectator() || player.isPassenger()) return false;
        var level = player.getServer().getLevel(binding.dimension());
        if (level == null || !level.getWorldBorder().isWithinBounds(binding.origin())
                || binding.origin().getY() < level.getMinBuildHeight() || binding.origin().getY() >= level.getMaxBuildHeight()) return false;
        level.getChunkAt(binding.origin());
        if (!ApparatusBlocks.isSpellstone(level.getBlockState(binding.origin()))) return false;
        var arrival = NearbyTeleport.arrival(player, level, binding.origin());
        if (arrival.isEmpty()) return false;
        Payment payment = binding.payment(); boolean creative = player.isCreative();
        if (!creative && (item.getMaxDamage() != DURABILITY || item.getDamageValue() < 0
                || item.getDamageValue() + payment.wear > DURABILITY)) return false;
        if (!creative && switch (payment) {
            case HEALTH -> player.getHealth() <= payment.cost;
            case HUNGER -> player.getFoodData().getFoodLevel() < payment.cost;
            case EXPERIENCE -> PlayerExperience.available(player) < payment.cost;
            case MANA -> NativeMana.amount(player) < payment.cost;
            case DURABILITY -> false;
        }) return false;
        float health = player.getHealth(); int hunger = player.getFoodData().getFoodLevel();
        var xp = PlayerExperience.Snapshot.of(player); var mana = player.getPersistentData().copy();
        if (!creative) {
            switch (payment) {
                case HEALTH -> player.setHealth(health - payment.cost);
                case HUNGER -> player.getFoodData().setFoodLevel(hunger - payment.cost);
                case MANA -> { if (!NativeMana.spend(player, payment.cost)) return false; }
                case EXPERIENCE -> { if (!PlayerExperience.spend(player, payment.cost)) return false; }
                case DURABILITY -> { }
            }
        }
        var from = player.position(); var fromLevel = player.serverLevel(); var to = arrival.get();
        boolean moved = player.teleportTo(level, to.x, to.y, to.z, Set.of(), player.getYRot(), player.getXRot());
        if (!moved || player.level() != level || player.position().distanceToSqr(to) > .01) {
            if (!creative) switch (payment) {
                case HEALTH -> player.setHealth(health);
                case HUNGER -> player.getFoodData().setFoodLevel(hunger);
                case EXPERIENCE -> xp.restore(player);
                case MANA -> {
                    for (String key : List.of("vestige:mana", "vestige:mana_recovery")) {
                        if (mana.contains(key)) player.getPersistentData().put(key, mana.get(key).copy()); else player.getPersistentData().remove(key);
                    }
                    NativeMana.sync(player, true);
                }
                case DURABILITY -> { }
            }
            return false;
        }
        player.fallDistance = 0;
        if (!creative) {
            item.setDamageValue(item.getDamageValue() + payment.wear);
            if (item.getDamageValue() >= DURABILITY) {
                item.shrink(1); level.playSound(null, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, .8f, 1);
            }
        }
        for (var site : List.of(Map.entry(fromLevel, from), Map.entry(level, to))) {
            var at = site.getValue(); site.getKey().sendParticles(ParticleTypes.PORTAL, at.x, at.y + .9, at.z, 32, .3, .5, .3, .1);
            site.getKey().playSound(null, at.x, at.y, at.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, .8f, 1);
        }
        return true;
    }
}
