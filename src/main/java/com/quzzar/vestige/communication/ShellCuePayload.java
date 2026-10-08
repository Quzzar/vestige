package com.quzzar.vestige.communication;

import com.quzzar.vestige.VestigeMainMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import java.util.List;

/** At most nine hotbar channels and one offhand channel, once per delivered conversation. */
@EventBusSubscriber(modid = VestigeMainMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public record ShellCuePayload(boolean sending, List<String> keys) implements CustomPacketPayload {
    public static final Type<ShellCuePayload> TYPE = new Type<>(VestigeMainMod.location("shell_cue"));
    public ShellCuePayload {
        keys = List.copyOf(keys);
        if (keys.isEmpty() || keys.size() > 10 || keys.stream().distinct().count() != keys.size()
                || keys.stream().anyMatch(key -> !key.matches("[0-9a-f]{64}"))) throw new IllegalArgumentException("Invalid shell cue");
    }
    public static final StreamCodec<RegistryFriendlyByteBuf, ShellCuePayload> CODEC = new StreamCodec<>() {
        public ShellCuePayload decode(RegistryFriendlyByteBuf buffer) {
            boolean sending = buffer.readBoolean(); int count = buffer.readVarInt();
            if (count < 1 || count > 10) throw new IllegalArgumentException("Invalid shell cue count");
            var keys = new java.util.ArrayList<String>();
            for (int i = 0; i < count; i++) keys.add(buffer.readUtf(64));
            return new ShellCuePayload(sending, keys);
        }
        public void encode(RegistryFriendlyByteBuf buffer, ShellCuePayload cue) {
            buffer.writeBoolean(cue.sending()); buffer.writeVarInt(cue.keys().size());
            cue.keys().forEach(key -> buffer.writeUtf(key, 64));
        }
    };
    @Override public Type<ShellCuePayload> type() { return TYPE; }
    @SubscribeEvent public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(TYPE, CODEC, (payload, context) ->
                com.quzzar.vestige.communication.client.ShellFeedback.accept(payload));
    }
}
