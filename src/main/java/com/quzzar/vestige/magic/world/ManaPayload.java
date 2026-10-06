package com.quzzar.vestige.magic.world;

import com.quzzar.vestige.VestigeMainMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = VestigeMainMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public record ManaPayload(float amount) implements CustomPacketPayload {
    public static final Type<ManaPayload> TYPE = new Type<>(VestigeMainMod.location("mana"));
    public ManaPayload {
        if (!Float.isFinite(amount) || amount < 0 || amount > NativeMana.MAX) throw new IllegalArgumentException("Invalid mana snapshot");
    }
    public static final StreamCodec<RegistryFriendlyByteBuf, ManaPayload> CODEC = new StreamCodec<>() {
        public ManaPayload decode(RegistryFriendlyByteBuf buffer) { return new ManaPayload(buffer.readFloat()); }
        public void encode(RegistryFriendlyByteBuf buffer, ManaPayload payload) { buffer.writeFloat(payload.amount()); }
    };
    @Override public Type<ManaPayload> type() { return TYPE; }
    @SubscribeEvent public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(TYPE, CODEC, (payload, context) ->
                com.quzzar.vestige.magic.world.client.ManaHud.accept(payload.amount()));
    }
}
