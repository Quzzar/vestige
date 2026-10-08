package com.quzzar.vestige.magic.world;

import com.quzzar.vestige.VestigeMainMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = VestigeMainMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public record ManaPayload(double amount, double maximum) implements CustomPacketPayload {
    public static final Type<ManaPayload> TYPE = new Type<>(VestigeMainMod.location("mana"));
    public ManaPayload(double amount) { this(amount, NativeMana.MAX); }
    public ManaPayload {
        if (!Double.isFinite(amount) || amount < 0 || !Double.isFinite(maximum) || maximum < NativeMana.MAX || amount > maximum) throw new IllegalArgumentException("Invalid mana snapshot");
    }
    public static final StreamCodec<RegistryFriendlyByteBuf, ManaPayload> CODEC = new StreamCodec<>() {
        public ManaPayload decode(RegistryFriendlyByteBuf buffer) { return new ManaPayload(buffer.readDouble(), buffer.readDouble()); }
        public void encode(RegistryFriendlyByteBuf buffer, ManaPayload payload) { buffer.writeDouble(payload.amount()); buffer.writeDouble(payload.maximum()); }
    };
    @Override public Type<ManaPayload> type() { return TYPE; }
    @SubscribeEvent public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("3").playToClient(TYPE, CODEC, (payload, context) ->
                com.quzzar.vestige.magic.world.client.ItemManaOverlay.acceptMana(payload.amount()));
    }
}
