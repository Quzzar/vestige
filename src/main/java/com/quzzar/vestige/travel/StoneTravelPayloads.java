package com.quzzar.vestige.travel;

import com.quzzar.vestige.VestigeMainMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import java.util.*;

/** Bounded pages and ID-only requests keep membership and coordinates authoritative on the server. */
@EventBusSubscriber(modid = VestigeMainMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class StoneTravelPayloads {
    private StoneTravelPayloads() { }
    public record Destination(UUID id, ResourceLocation dimension, BlockPos position, String name,
                              StandingStonePayment.Quote quote, boolean affordable) {
        public Destination(UUID id, ResourceLocation dimension, BlockPos position, String name, int xpCost) {
            this(id, dimension, position, name, new StandingStonePayment.Quote(StandingStonePayment.EXPERIENCE, xpCost), true);
        }
        public Destination {
            Objects.requireNonNull(quote);
            if (name == null || name.isBlank() || name.length() > 64) throw new IllegalArgumentException("Invalid travel destination");
        }
        /** Ordinary-XP source compatibility; new presentation and transactions use the typed quote. */
        public int xpCost() { return quote.amount(); }
    }
    public record View(UUID source, String key, ResourceLocation dimension, BlockPos position,
                       String sourceName, List<Destination> destinations, int page, int total, boolean refreshOnly) implements CustomPacketPayload {
        public View(UUID source, String key, ResourceLocation dimension, BlockPos position, String sourceName,
                    List<Destination> destinations, int page, int total) {
            this(source, key, dimension, position, sourceName, destinations, page, total, false);
        }
        public static final Type<View> TYPE = new Type<>(VestigeMainMod.location("stone_network"));
        public static final StreamCodec<RegistryFriendlyByteBuf, View> CODEC = StreamCodec.of((buffer, value) -> {
            buffer.writeUUID(value.source()); buffer.writeUtf(value.key(), 64); buffer.writeResourceLocation(value.dimension()); buffer.writeBlockPos(value.position());
            buffer.writeUtf(value.sourceName(), 64); buffer.writeVarInt(value.destinations().size());
            for (var node : value.destinations()) {
                buffer.writeUUID(node.id()); buffer.writeResourceLocation(node.dimension()); buffer.writeBlockPos(node.position()); buffer.writeUtf(node.name(), 64);
                buffer.writeEnum(node.quote().route()); buffer.writeVarInt(node.quote().amount()); buffer.writeBoolean(node.affordable());
            }
            buffer.writeVarInt(value.page()); buffer.writeVarInt(value.total()); buffer.writeBoolean(value.refreshOnly());
        }, buffer -> {
            UUID source = buffer.readUUID(); String key = buffer.readUtf(64); ResourceLocation dimension = buffer.readResourceLocation(); BlockPos position = buffer.readBlockPos();
            String sourceName = buffer.readUtf(64);
            int size = buffer.readVarInt(); if (size < 0 || size > StoneTravel.PAGE_SIZE) throw new IllegalArgumentException("Invalid network page");
            List<Destination> nodes = new ArrayList<>(size);
            for (int i = 0; i < size; i++) nodes.add(new Destination(buffer.readUUID(), buffer.readResourceLocation(), buffer.readBlockPos(), buffer.readUtf(64),
                    new StandingStonePayment.Quote(buffer.readEnum(StandingStonePayment.class), buffer.readVarInt()), buffer.readBoolean()));
            return new View(source, key, dimension, position, sourceName, nodes, buffer.readVarInt(), buffer.readVarInt(), buffer.readBoolean());
        });
        public View {
            destinations = List.copyOf(destinations);
            if (!StoneNetwork.validKey(key) || sourceName == null || sourceName.isBlank() || sourceName.length() > 64
                    || destinations.size() > StoneTravel.PAGE_SIZE || page < 0 || total < destinations.size()
                    || destinations.stream().map(Destination::id).distinct().count() != destinations.size())
                throw new IllegalArgumentException("Invalid standing stone view");
        }
        @Override public Type<View> type() { return TYPE; }
    }
    public record Request(UUID source, UUID destination) implements CustomPacketPayload {
        public static final Type<Request> TYPE = new Type<>(VestigeMainMod.location("stone_travel"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Request> CODEC = StreamCodec.of(
                (buffer, value) -> { buffer.writeUUID(value.source()); buffer.writeUUID(value.destination()); },
                buffer -> new Request(buffer.readUUID(), buffer.readUUID()));
        @Override public Type<Request> type() { return TYPE; }
    }
    public record Page(UUID source, int page) implements CustomPacketPayload {
        public static final Type<Page> TYPE = new Type<>(VestigeMainMod.location("stone_page"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Page> CODEC = StreamCodec.of(
                (buffer, value) -> { buffer.writeUUID(value.source()); buffer.writeVarInt(value.page()); },
                buffer -> new Page(buffer.readUUID(), buffer.readVarInt()));
        @Override public Type<Page> type() { return TYPE; }
    }
    public record Rename(UUID source, String name, int page) implements CustomPacketPayload {
        public static final Type<Rename> TYPE = new Type<>(VestigeMainMod.location("stone_rename"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Rename> CODEC = StreamCodec.of(
                (buffer, value) -> { buffer.writeUUID(value.source()); buffer.writeUtf(value.name(), 64); buffer.writeVarInt(value.page()); },
                buffer -> new Rename(buffer.readUUID(), buffer.readUtf(64), buffer.readVarInt()));
        @Override public Type<Rename> type() { return TYPE; }
    }
    @SubscribeEvent public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("5");
        registrar.playToClient(View.TYPE, View.CODEC, (payload, context) -> com.quzzar.vestige.travel.client.StoneNetworkScreen.open(payload));
        registrar.playToServer(Request.TYPE, Request.CODEC, (payload, context) -> {
            if (context.player() instanceof ServerPlayer player) StoneTravel.travel(player, payload.source(), payload.destination());
        });
        registrar.playToServer(Page.TYPE, Page.CODEC, (payload, context) -> {
            if (context.player() instanceof ServerPlayer player) StoneTravel.page(player, payload.source(), payload.page());
        });
        registrar.playToServer(Rename.TYPE, Rename.CODEC, (payload, context) -> {
            if (context.player() instanceof ServerPlayer player) StoneTravel.rename(player, payload.source(), payload.name(), payload.page());
        });
    }
}
