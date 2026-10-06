package com.quzzar.vestige.apparatus.recipeviewer;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.RitualCrafting;
import com.quzzar.vestige.apparatus.RitualRecipe;
import com.quzzar.vestige.apparatus.SpellKnowledge;
import com.quzzar.vestige.magic.definition.SpellRarity;
import com.quzzar.vestige.magic.definition.SpellDefinition;
import com.quzzar.vestige.magic.world.NativeMagic;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import java.util.*;

/** Bounded, personalized knowledge snapshot; uncrafted spells contain no offerings. */
public record RitualDisplayPayload(int page, boolean last, boolean refresh, List<RitualDisplays.Entry> entries) implements CustomPacketPayload {
    public static final int PAGE_SIZE=32, MAX_PAGES=128;
    public static final Type<RitualDisplayPayload> TYPE=new Type<>(VestigeMainMod.location("ritual_displays"));
    public RitualDisplayPayload {
        entries=List.copyOf(entries);
        if(page<0 || page>=MAX_PAGES || entries.size()>PAGE_SIZE)throw new IllegalArgumentException("Invalid ritual display page");
    }
    public static final StreamCodec<RegistryFriendlyByteBuf,RitualDisplayPayload> CODEC=new StreamCodec<>() {
        @Override public RitualDisplayPayload decode(RegistryFriendlyByteBuf b) {
            int page=b.readVarInt();boolean last=b.readBoolean();boolean refresh=b.readBoolean();int count=bounded(b,PAGE_SIZE);
            var entries=new ArrayList<RitualDisplays.Entry>();
            for(int i=0;i<count;i++) {
                var id=id(b);var spell=b.readBoolean() ? Optional.of(id(b)) : Optional.<ResourceLocation>empty();boolean identified=b.readBoolean();
                var rarity=SpellRarity.fromId(b.readUtf(16)).orElseThrow(() -> new IllegalArgumentException("Invalid scroll rarity"));int capacity=b.readUnsignedByte();
                var offerings=new ArrayList<RitualDisplays.Offering>();int parts=bounded(b,8);
                for(int j=0;j<parts;j++)offerings.add(new RitualDisplays.Offering(b.readUnsignedByte(),new RitualRecipe.Ingredient(ids(b,8),ids(b,4))));
                entries.add(new RitualDisplays.Entry(id,spell,identified,rarity,capacity,offerings));
            }
            return new RitualDisplayPayload(page,last,refresh,entries);
        }
        @Override public void encode(RegistryFriendlyByteBuf b,RitualDisplayPayload p) {
            b.writeVarInt(p.page);b.writeBoolean(p.last);b.writeBoolean(p.refresh);b.writeVarInt(p.entries.size());
            for(var e:p.entries) {
                id(b,e.id());b.writeBoolean(e.spell().isPresent());e.spell().ifPresent(s -> id(b,s));b.writeBoolean(e.identified());b.writeUtf(e.rarity().id(),16);b.writeByte(e.capacity());b.writeVarInt(e.offerings().size());
                for(var o:e.offerings()){b.writeByte(o.seat());ids(b,o.ingredient().items());ids(b,o.ingredient().tags());}
            }
        }
    };
    private static int bounded(RegistryFriendlyByteBuf b,int max){int n=b.readVarInt();if(n<0 || n>max)throw new IllegalArgumentException("Oversized ritual display");return n;}
    private static ResourceLocation id(RegistryFriendlyByteBuf b){return ResourceLocation.parse(b.readUtf(256));}
    private static void id(RegistryFriendlyByteBuf b,ResourceLocation id){b.writeUtf(id.toString(),256);}
    private static List<ResourceLocation> ids(RegistryFriendlyByteBuf b,int max){var ids=new ArrayList<ResourceLocation>();int n=bounded(b,max);for(int i=0;i<n;i++)ids.add(id(b));return ids;}
    private static void ids(RegistryFriendlyByteBuf b,List<ResourceLocation> ids){b.writeVarInt(ids.size());ids.forEach(id -> id(b,id));}
    @Override public Type<RitualDisplayPayload> type(){return TYPE;}
    @EventBusSubscriber(modid=VestigeMainMod.MOD_ID,bus=EventBusSubscriber.Bus.MOD)
    public static final class Registration {
        @SubscribeEvent public static void register(RegisterPayloadHandlersEvent event) {
            event.registrar("4").playToClient(TYPE,CODEC,(payload,context) -> RitualViewerClient.accept(payload));
        }
    }
    @EventBusSubscriber(modid=VestigeMainMod.MOD_ID)
    public static final class Sync {
        @SubscribeEvent public static void sync(OnDatapackSyncEvent event) {
            event.getRelevantPlayers().forEach(player -> send(player, false));
        }
        /** Only this player receives their identified names and successfully crafted recipes. */
        public static void send(Player recipient, boolean refresh) {
            if (!(recipient instanceof ServerPlayer player) || player.connection==null
                    || !NetworkRegistry.hasChannel(player.connection,TYPE.id())) return;
            var entries=RitualCrafting.catalog().recipes().values().stream()
                    .sorted(Comparator.comparing(r -> r.spell().toString()))
                    .map(recipe -> RitualDisplays.spell(recipe,SpellKnowledge.crafted(player,recipe.spell()),
                            SpellKnowledge.identified(player,recipe.spell()),Optional.ofNullable(NativeMagic.spells().spells().get(recipe.spell()))
                                    .map(SpellDefinition::rarity).orElse(SpellRarity.COMMON))).toList();
            if(entries.size()>PAGE_SIZE*MAX_PAGES)throw new IllegalStateException("Too many ritual recipes for viewer synchronization");
            int pages=Math.max(1,(entries.size()+PAGE_SIZE-1)/PAGE_SIZE);
            for(int page=0;page<pages;page++) {
                var payload=new RitualDisplayPayload(page,page==pages-1,refresh,entries.subList(page*PAGE_SIZE,Math.min(entries.size(),(page+1)*PAGE_SIZE)));
                PacketDistributor.sendToPlayer(player,payload);
            }
        }
    }
}
