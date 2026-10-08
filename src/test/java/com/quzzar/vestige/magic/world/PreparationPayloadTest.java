package com.quzzar.vestige.magic.world;

import com.quzzar.vestige.magic.runtime.SpellRuntime;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class PreparationPayloadTest {
    @Test void progressAndClearingRoundTripAndInvalidTimingsReject() {
        var buffer=new RegistryFriendlyByteBuf(Unpooled.buffer(),RegistryAccess.EMPTY);
        try {
            for(var preparation:java.util.List.of(Optional.of(new SpellRuntime.Preparation(UUID.randomUUID(),19,40)),
                    Optional.<SpellRuntime.Preparation>empty(),Optional.of(new SpellRuntime.Preparation(UUID.randomUUID(),Integer.MAX_VALUE,2L*Integer.MAX_VALUE)))) {
                var payload=new PreparationPayload(preparation);PreparationPayload.CODEC.encode(buffer,payload);
                assertEquals(payload,PreparationPayload.CODEC.decode(buffer));assertEquals(0,buffer.readableBytes());
            }
            buffer.writeBoolean(true);buffer.writeUUID(UUID.randomUUID());buffer.writeVarLong(40);buffer.writeVarLong(40);
            assertThrows(IllegalArgumentException.class,()->PreparationPayload.CODEC.decode(buffer));
            assertThrows(IllegalArgumentException.class,()->new SpellRuntime.Preparation(UUID.randomUUID(),0,0));
            assertThrows(IllegalArgumentException.class,()->new SpellRuntime.Preparation(UUID.randomUUID(),-1,40));
        } finally {buffer.release();}
    }
    @Test void bothHandsAndOpaqueSourceRoundTripButOrphanedSourcesReject() {
        var buffer=new RegistryFriendlyByteBuf(Unpooled.buffer(),RegistryAccess.EMPTY);
        try {
            var progress=Optional.of(new SpellRuntime.Preparation(UUID.randomUUID(),5,40));
            for(var hand:InteractionHand.values()) {
                var source=Optional.of(new PreparationPayload.HeldSource(hand,UUID.randomUUID()));
                var payload=new PreparationPayload(progress,source);
                PreparationPayload.CODEC.encode(buffer,payload);
                assertEquals(payload,PreparationPayload.CODEC.decode(buffer));assertEquals(0,buffer.readableBytes());
                assertThrows(IllegalArgumentException.class,()->new PreparationPayload(Optional.empty(),source));
            }
            buffer.writeBoolean(false);buffer.writeBoolean(true);buffer.writeEnum(InteractionHand.MAIN_HAND);buffer.writeUUID(UUID.randomUUID());
            assertThrows(IllegalArgumentException.class,()->PreparationPayload.CODEC.decode(buffer));
        } finally {buffer.release();}
    }
}
