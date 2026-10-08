package com.quzzar.vestige.magic.world;

import com.quzzar.vestige.VestigeMainMod;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

/** Persistent player energy. Recovery pauses offline and never refills a returning player on login. */
@EventBusSubscriber(modid = VestigeMainMod.MOD_ID)
public final class NativeMana {
    public static final int MAX = 100, RECOVERY_DELAY = 100;
    public static final double PER_TICK = .1;
    private static final String KEY = "vestige:mana", DELAY = "vestige:mana_recovery", SENT = "vestige:mana_sent", SENT_MAX = "vestige:mana_max_sent", RESPAWN = "vestige:mana_reset_on_respawn";
    private NativeMana() { }
    public static double maximum(LivingEntity actor) {
        return MAX + com.quzzar.vestige.equipment.EquipmentMagic.manaBonus(actor);
    }
    /** Persist the clamp so removed capacity cannot be recovered by putting the robe back on. */
    public static void reconcile(LivingEntity actor) {
        if (!(actor instanceof Player) || !actor.getPersistentData().contains(KEY)) return;
        double value = actor.getPersistentData().getDouble(KEY);
        actor.getPersistentData().putDouble(KEY, Double.isFinite(value) ? Math.clamp(value, 0, maximum(actor)) : 0);
    }
    public static double amount(LivingEntity actor) {
        reconcile(actor);
        var data = actor.getPersistentData();
        if (!data.contains(KEY) && actor instanceof Player) return MAX;
        double value = data.getDouble(KEY);
        return Double.isFinite(value) ? Math.max(0, actor instanceof Player ? Math.min(maximum(actor), value) : value) : 0;
    }
    public static void initialize(Player player) {
        if (!player.getPersistentData().contains(KEY)) reset(player);
    }
    public static void reset(Player player) {
        player.getPersistentData().putDouble(KEY, maximum(player));
        player.getPersistentData().putInt(DELAY, 0);
    }
    public static void set(LivingEntity actor, double value) {
        if (!Double.isFinite(value)) throw new IllegalArgumentException("Mana must be finite");
        double previous = amount(actor);
        actor.getPersistentData().putDouble(KEY, Math.clamp(value, 0, maximum(actor)));
        if (value < previous) actor.getPersistentData().putInt(DELAY, RECOVERY_DELAY);
        if (value >= maximum(actor)) actor.getPersistentData().putInt(DELAY, 0);
        sync(actor, true);
    }
    public static boolean spend(LivingEntity actor, double cost) {
        if (!Double.isFinite(cost) || cost < 0 || amount(actor) < cost) return false;
        if (cost > 0) {
            actor.getPersistentData().putDouble(KEY, amount(actor) - cost);
            actor.getPersistentData().putInt(DELAY, RECOVERY_DELAY);
            sync(actor, true);
        }
        return true;
    }
    public static double restore(LivingEntity actor, double quantity) {
        if (!Double.isFinite(quantity) || quantity < 0) throw new IllegalArgumentException("Restored mana must be finite and nonnegative");
        double previous = amount(actor);
        double restored = previous >= maximum(actor) ? previous : Math.min(maximum(actor), previous + quantity);
        actor.getPersistentData().putDouble(KEY, restored);
        sync(actor, true);
        return restored - previous;
    }
    /** One server tick; exposed for deterministic world tests of the real persistent state. */
    public static void recover(Player player) {
        initialize(player);
        if (!player.isAlive()) return;
        var data = player.getPersistentData();
        int delay = Math.clamp(data.getInt(DELAY), 0, RECOVERY_DELAY);
        if (delay > 0) data.putInt(DELAY, delay - 1);
        else if (amount(player) < maximum(player)) {
            double next = amount(player) + PER_TICK;
            data.putDouble(KEY, next >= maximum(player) - 1e-7 ? maximum(player) : next);
        }
    }
    public static void sync(LivingEntity actor, boolean force) {
        if (!(actor instanceof ServerPlayer player) || player.connection == null
                || !NetworkRegistry.hasChannel(player.connection, ManaPayload.TYPE.id())) return;
        double value = amount(player); var data = player.getPersistentData();
        if (force || data.getDouble(SENT) != value || data.getDouble(SENT_MAX) != maximum(player)) {
            PacketDistributor.sendToPlayer(player, new ManaPayload(value, maximum(player))); data.putDouble(SENT, value); data.putDouble(SENT_MAX, maximum(player));
        }
    }
    @SubscribeEvent public static void tick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()) return;
        recover(event.getEntity());
        if (event.getEntity().tickCount % 5 == 0) sync(event.getEntity(), false);
    }
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent event) { initialize(event.getEntity()); sync(event.getEntity(), true); }
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity().getPersistentData().getBoolean(RESPAWN)) {
            reset(event.getEntity()); event.getEntity().getPersistentData().remove(RESPAWN);
        }
        sync(event.getEntity(), true);
    }
    @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event) { sync(event.getEntity(), true); }
    @SubscribeEvent public static void clonePlayer(PlayerEvent.Clone event) {
        if (event.isWasDeath()) { reset(event.getEntity()); event.getEntity().getPersistentData().putBoolean(RESPAWN, true); }
        else {
            event.getEntity().getPersistentData().putDouble(KEY, amount(event.getOriginal()));
            event.getEntity().getPersistentData().putInt(DELAY, Math.clamp(event.getOriginal().getPersistentData().getInt(DELAY), 0, RECOVERY_DELAY));
        }
    }
}
