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
import java.util.*;

/** Server-authoritative selection and shared nearby arrival; clients send IDs, never coordinates. */
@EventBusSubscriber(modid = VestigeMainMod.MOD_ID)
public final class StoneTravel {
    public static final int PAGE_SIZE = 8;
    private record Session(UUID source, String key, ResourceKey<Level> dimension, BlockPos position, long expires) { }
    private static final Map<MinecraftServer, Map<UUID, Session>> SESSIONS = new IdentityHashMap<>();
    private StoneTravel() { }
    public static void open(ServerPlayer player, StandingStoneEntity stone, int requestedPage) {
        if (stone.getLevel() != player.level() || player.distanceToSqr(Vec3.atCenterOf(stone.getBlockPos())) > 64) return;
        if (!StoneNetwork.validKey(stone.key())) return;
        var view = view(player, stone, requestedPage);
        SESSIONS.computeIfAbsent(player.getServer(), ignored -> new HashMap<>()).put(player.getUUID(),
                new Session(stone.id(), stone.key(), player.level().dimension(), stone.getBlockPos(), player.level().getGameTime() + 1200));
        if (NetworkRegistry.hasChannel(player.connection, StoneTravelPayloads.View.TYPE.id()))
            PacketDistributor.sendToPlayer(player, view);
    }
    /** Every peer remains accessible through bounded pages, with the current name on every page. */
    static StoneTravelPayloads.View view(ServerPlayer player, StandingStoneEntity stone, int requestedPage) {
        var peers = StoneDirectory.get(player.getServer()).network().peers(stone.key()).stream()
                .filter(node -> node.dimension().equals(player.level().dimension().location())).toList();
        int page = Math.clamp(requestedPage, 0, Math.max(0, (peers.size() - 1) / PAGE_SIZE));
        var nodes = peers.stream().skip((long) page * PAGE_SIZE).limit(PAGE_SIZE)
                .map(node -> new StoneTravelPayloads.Destination(node.id(), node.dimension(), node.position(), node.name())).toList();
        return new StoneTravelPayloads.View(stone.id(), stone.key(), player.level().dimension().location(),
                stone.getBlockPos(), stone.name(), nodes, page, peers.size());
    }
    private static Optional<StandingStoneEntity> source(ServerPlayer player, UUID id) {
        var session = SESSIONS.getOrDefault(player.getServer(), Map.of()).get(player.getUUID());
        if (session == null || !session.source().equals(id) || !session.dimension().equals(player.level().dimension())
                || player.level().getGameTime() > session.expires() || player.distanceToSqr(Vec3.atCenterOf(session.position())) > 64
                || !player.level().hasChunkAt(session.position())) return Optional.empty();
        return player.level().getBlockEntity(session.position()) instanceof StandingStoneEntity stone
                && stone.id().equals(id) && stone.key().equals(session.key()) ? Optional.of(stone) : Optional.empty();
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
        var stone = source(player, sourceId).orElse(null);
        if (stone == null) return;
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
        var point = arrival.get(); var departure = player.position(); var departureLevel = player.serverLevel();
        player.stopRiding(); player.teleportTo(level, point.x, point.y, point.z, Set.of(), player.getYRot(), player.getXRot());
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
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            var sessions = SESSIONS.get(player.getServer()); if (sessions != null) sessions.remove(player.getUUID());
        }
    }
    @SubscribeEvent public static void stop(ServerStoppedEvent event) { SESSIONS.remove(event.getServer()); }
}
