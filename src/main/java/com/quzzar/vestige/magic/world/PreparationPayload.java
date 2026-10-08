package com.quzzar.vestige.magic.world;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.runtime.SpellRuntime;
import com.quzzar.vestige.apparatus.ManaItemCosts;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.minecraft.world.InteractionHand;
import java.util.Optional;
import java.util.Objects;
import java.util.UUID;
import java.nio.charset.StandardCharsets;

/** Private progress only: no spell identity or undiscovered spell facts are sent. */
@EventBusSubscriber(modid = VestigeMainMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public record PreparationPayload(Optional<SpellRuntime.Preparation> preparation, Optional<HeldSource> heldSource) implements CustomPacketPayload {
    /** An opaque item fingerprint prevents an in-flight snapshot animating a replacement item. */
    public record HeldSource(InteractionHand hand, UUID itemKey) {
        public HeldSource { Objects.requireNonNull(hand); Objects.requireNonNull(itemKey); }
        /** Item kind participates because a staff and a scroll can share one mana-price key. */
        public static Optional<HeldSource> of(InteractionHand hand, ItemStack stack) {
            return ManaItemCosts.source(stack).map(source -> new HeldSource(hand, UUID.nameUUIDFromBytes(
                    (BuiltInRegistries.ITEM.getKey(stack.getItem()) + "|" + source.key()).getBytes(StandardCharsets.UTF_8))));
        }
        public boolean matches(ItemStack stack) { return of(hand, stack).map(this::equals).orElse(false); }
    }
    public PreparationPayload {
        Objects.requireNonNull(preparation); Objects.requireNonNull(heldSource);
        if (preparation.isEmpty() && heldSource.isPresent()) throw new IllegalArgumentException("Held source without preparation");
    }
    public PreparationPayload(Optional<SpellRuntime.Preparation> preparation) { this(preparation, Optional.empty()); }
    public static final Type<PreparationPayload> TYPE = new Type<>(VestigeMainMod.location("cast_preparation"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PreparationPayload> CODEC = new StreamCodec<>() {
        public PreparationPayload decode(RegistryFriendlyByteBuf buffer) {
            var preparation = buffer.readBoolean() ? Optional.of(new SpellRuntime.Preparation(
                    buffer.readUUID(), buffer.readVarLong(), buffer.readVarLong())) : Optional.<SpellRuntime.Preparation>empty();
            var source = buffer.readBoolean() ? Optional.of(new HeldSource(buffer.readEnum(InteractionHand.class), buffer.readUUID()))
                    : Optional.<HeldSource>empty();
            return new PreparationPayload(preparation, source);
        }
        public void encode(RegistryFriendlyByteBuf buffer, PreparationPayload payload) {
            buffer.writeBoolean(payload.preparation.isPresent());
            payload.preparation.ifPresent(value -> {
                buffer.writeUUID(value.castId()); buffer.writeVarLong(value.elapsedTicks()); buffer.writeVarLong(value.totalTicks());
            });
            buffer.writeBoolean(payload.heldSource.isPresent());
            payload.heldSource.ifPresent(source -> { buffer.writeEnum(source.hand()); buffer.writeUUID(source.itemKey()); });
        }
    };
    @Override public Type<PreparationPayload> type() { return TYPE; }
    @SubscribeEvent public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("2").playToClient(TYPE, CODEC, (payload, context) ->
                com.quzzar.vestige.magic.world.client.PreparationHud.accept(payload));
    }
}
