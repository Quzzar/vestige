package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.VestigeMainMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** Empty-hand continuation request; the server accepts only an existing awaiting scroll cast. */
@EventBusSubscriber(modid = VestigeMainMod.MOD_ID,bus = EventBusSubscriber.Bus.MOD)
public record ScrollRecastPayload() implements CustomPacketPayload {
    public static final Type<ScrollRecastPayload> TYPE=new Type<>(VestigeMainMod.location("scroll_recast"));
    public static final StreamCodec<RegistryFriendlyByteBuf,ScrollRecastPayload> STREAM_CODEC=StreamCodec.unit(new ScrollRecastPayload());
    @Override public Type<ScrollRecastPayload> type() { return TYPE; }
    @SubscribeEvent public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(TYPE,STREAM_CODEC,(payload,context) -> {
            if (context.player().getMainHandItem().isEmpty() && context.player().getOffhandItem().isEmpty()
                    && !ScrollCasting.recast(context.player()) && !WandCasting.recast(context.player())) StaffCasting.recast(context.player());
        });
    }
}
