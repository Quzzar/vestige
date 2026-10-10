package com.quzzar.vestige.travel;

import com.quzzar.vestige.VestigeMainMod;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import com.quzzar.vestige.magic.runtime.CastReservation;
import com.quzzar.vestige.magic.world.MinecraftSpellWorld;
import java.util.*;

/** Server-authoritative selection and shared nearby arrival; clients send IDs, never coordinates. */
@EventBusSubscriber(modid = VestigeMainMod.MOD_ID)
public final class StoneTravel {
    public static final int PAGE_SIZE = 6;
    private record Session(UUID source, String key, ResourceKey<Level> dimension, BlockPos position, long expires,
                           StandingStonePayment payment, StoneTravelPayloads.View view) { }
    private static final Map<MinecraftServer, Map<UUID, Session>> SESSIONS = new IdentityHashMap<>();
    private static final Map<MinecraftServer, Set<UUID>> TRANSACTIONS = new IdentityHashMap<>();
    private StoneTravel() { }
    public static void open(ServerPlayer player, StandingStoneEntity stone, int requestedPage) {
        if (stone.getLevel() != player.level() || player.distanceToSqr(Vec3.atCenterOf(stone.getBlockPos())) > 64) return;
        if (!StoneNetwork.validKey(stone.key()) || stone.payment().isEmpty()) return;
        var view = view(player, stone, requestedPage);
        SESSIONS.computeIfAbsent(player.getServer(), ignored -> new HashMap<>()).put(player.getUUID(),
                new Session(stone.id(), stone.key(), player.level().dimension(), stone.getBlockPos(), player.level().getGameTime() + 1200,
                        stone.payment().orElseThrow(), view));
        if (NetworkRegistry.hasChannel(player.connection, StoneTravelPayloads.View.TYPE.id()))
            PacketDistributor.sendToPlayer(player, view);
    }
    /** Every peer remains accessible through bounded pages, with the current name on every page. */
    static StoneTravelPayloads.View view(ServerPlayer player, StandingStoneEntity stone, int requestedPage) {
        var peers = StoneDirectory.get(player.getServer()).network().peers(stone.key()).stream()
                .filter(node -> node.dimension().equals(player.level().dimension().location()) && !node.id().equals(stone.id())).toList();
        int page = Math.clamp(requestedPage, 0, Math.max(0, (peers.size() - 1) / PAGE_SIZE));
        var nodes = peers.stream().skip((long) page * PAGE_SIZE).limit(PAGE_SIZE)
                .map(node -> {
                    var quote = StandingStoneFare.quote(stone.getBlockPos(), node.position(), stone.payment().orElseThrow());
                    return new StoneTravelPayloads.Destination(node.id(), node.dimension(), node.position(), node.name(), quote, quote.affordable(player));
                }).toList();
        return new StoneTravelPayloads.View(stone.id(), stone.key(), player.level().dimension().location(),
                stone.getBlockPos(), stone.name(), nodes, page, peers.size());
    }
    private static Optional<StandingStoneEntity> source(ServerPlayer player, UUID id) {
        var session = SESSIONS.getOrDefault(player.getServer(), Map.of()).get(player.getUUID());
        if (session == null || !session.source().equals(id) || !session.dimension().equals(player.level().dimension())
                || player.level().getGameTime() > session.expires() || player.distanceToSqr(Vec3.atCenterOf(session.position())) > 64
                || !player.level().hasChunkAt(session.position())) return Optional.empty();
        return player.level().getBlockEntity(session.position()) instanceof StandingStoneEntity stone
                && stone.id().equals(id) && stone.key().equals(session.key()) && stone.payment().filter(session.payment()::equals).isPresent()
                ? Optional.of(stone) : Optional.empty();
    }
    public static void page(ServerPlayer player, UUID id, int page) {
        source(player, id).ifPresent(stone -> open(player, stone, page));
    }
    /** The same nearby, unexpired source session gates renaming and travel. */
    public static boolean rename(ServerPlayer player, UUID id, String label, int page) {
        var stone = source(player, id).orElse(null);
        if (stone == null) return false;
        boolean changed = stone.rename(label);
        open(player, stone, page);
        return changed;
    }
    public static void travel(ServerPlayer player, UUID sourceId, UUID destinationId) {
        if (!player.isAlive()) return;
        var active = TRANSACTIONS.computeIfAbsent(player.getServer(), ignored -> new HashSet<>());
        if (!active.add(player.getUUID())) return;
        try { travelOnce(player, sourceId, destinationId); }
        finally { active.remove(player.getUUID()); }
    }
    private static void travelOnce(ServerPlayer player, UUID sourceId, UUID destinationId) {
        var stone = source(player, sourceId).orElse(null);
        if (stone == null) return;
        var session = SESSIONS.get(player.getServer()).get(player.getUUID());
        var shown = session.view().destinations().stream().filter(node -> node.id().equals(destinationId)).findFirst().orElse(null);
        if (shown == null) return;
        var directory = StoneDirectory.get(player.getServer());
        var destination = directory.network().get(destinationId).orElse(null);
        if (destination == null || !destination.key().equals(stone.key()) || destinationId.equals(sourceId)
                || !destination.dimension().equals(player.level().dimension().location())) {
            return;
        }
        ServerLevel level = player.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, destination.dimension()));
        if (level == null || !level.getWorldBorder().isWithinBounds(destination.position())) return;
        // Load only the selected destination for validation, without a permanent chunk ticket.
        level.getChunkAt(destination.position());
        if (!(level.getBlockEntity(destination.position()) instanceof StandingStoneEntity target)
                || !target.id().equals(destination.id()) || !target.key().equals(stone.key())) {
            directory.remove(destination.id()); return;
        }
        var arrival = arrival(player, level, destination.position());
        if (arrival.isEmpty()) return;
        var quote = StandingStoneFare.quote(stone.getBlockPos(), target.getBlockPos(), session.payment());
        if (!quote.equals(shown.quote()) || !quote.affordable(player)) return;
        var point = arrival.get(); var departure = player.position(); var departureLevel = player.serverLevel();
        var reservation = new CastReservation.Atomic() {
            @Override public boolean valid() {
                return player.isAlive() && SESSIONS.getOrDefault(player.getServer(), Map.of()).get(player.getUUID()) == session
                        && source(player, sourceId).orElse(null) == stone && level.getBlockEntity(destination.position()) == target
                        && target.id().equals(destinationId) && target.key().equals(stone.key()) && target.payment().isPresent()
                        && directory.network().get(destinationId).filter(node -> node.key().equals(stone.key())).isPresent();
            }
            @Override public boolean tryCommit() {
                if (!valid()) return false;
                var request = new net.neoforged.neoforge.event.entity.EntityTeleportEvent(player, point.x, point.y, point.z);
                if (net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(request).isCanceled()
                        || !request.getTarget().equals(point) || !valid()) return false;
                return player.teleportTo(level, point.x, point.y, point.z, Set.of(), player.getYRot(), player.getXRot())
                        && player.level() == level && player.position().distanceToSqr(point) <= .01;
            }
        };
        if (!MinecraftSpellWorld.payAndCommit(player, List.of(quote.cost()), reservation)) return;
        player.fallDistance = 0;
        SESSIONS.get(player.getServer()).remove(player.getUUID());
        for (var site : List.of(Map.entry(departureLevel, departure), Map.entry(level, point))) {
            var at = site.getValue();
            site.getKey().sendParticles(ParticleTypes.PORTAL, at.x, at.y + .9, at.z, 45, .4, .6, .4, .15);
            site.getKey().playSound(null, at.x, at.y, at.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, .8f, .8f);
        }
    }
    /** Shared random nearby placement; a crowded destination uses the owner's occupied fallback. */
    public static Optional<Vec3> arrival(ServerPlayer player, ServerLevel level, BlockPos stone) {
        return NearbyTeleport.arrival(player, level, stone);
    }
    /** Balance updates refresh an already-open screen; they never reopen a closed menu. */
    @SubscribeEvent public static void tick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        var sessions = SESSIONS.get(player.getServer());
        if (sessions == null) return;
        var session = sessions.get(player.getUUID());
        if (session == null) return;
        if (source(player, session.source()).isEmpty()) { sessions.remove(player.getUUID()); return; }
        var old = session.view();
        var nodes = old.destinations().stream().map(node -> new StoneTravelPayloads.Destination(node.id(), node.dimension(), node.position(), node.name(),
                node.quote(), node.quote().affordable(player))).toList();
        if (nodes.equals(old.destinations())) return;
        var refreshed = new StoneTravelPayloads.View(old.source(), old.key(), old.dimension(), old.position(), old.sourceName(), nodes, old.page(), old.total(), true);
        sessions.put(player.getUUID(), new Session(session.source(), session.key(), session.dimension(), session.position(), session.expires(), session.payment(), refreshed));
        if (NetworkRegistry.hasChannel(player.connection, StoneTravelPayloads.View.TYPE.id())) PacketDistributor.sendToPlayer(player, refreshed);
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            var sessions = SESSIONS.get(player.getServer()); if (sessions != null) sessions.remove(player.getUUID());
        }
    }
    @SubscribeEvent public static void stop(ServerStoppedEvent event) { SESSIONS.remove(event.getServer()); TRANSACTIONS.remove(event.getServer()); }
}
