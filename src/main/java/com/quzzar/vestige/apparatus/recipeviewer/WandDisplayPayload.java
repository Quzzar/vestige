package com.quzzar.vestige.apparatus.recipeviewer;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.*;
import com.quzzar.vestige.magic.definition.SpellDefinition;
import com.quzzar.vestige.magic.world.NativeMagic;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.*;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import java.util.*;

/** Definitions are not sent: only bounded scroll identities and compiler-approved component choices. */
public record WandDisplayPayload(int page,boolean last,boolean refresh,List<WandDisplays.Source> sources) implements CustomPacketPayload {
    public static final int PAGE_SIZE=16,MAX_PAGES=128;
    public static final Type<WandDisplayPayload> TYPE=new Type<>(VestigeMainMod.location("wand_displays"));
    public WandDisplayPayload {sources=List.copyOf(sources);if(page<0 || page>=MAX_PAGES || sources.size()>PAGE_SIZE)throw new IllegalArgumentException("Invalid wand display page");}
    public static final StreamCodec<RegistryFriendlyByteBuf,WandDisplayPayload> CODEC=new StreamCodec<>() {
        public WandDisplayPayload decode(RegistryFriendlyByteBuf b){
            int page=b.readVarInt();boolean last=b.readBoolean(),refresh=b.readBoolean();int count=bounded(b,PAGE_SIZE);var sources=new ArrayList<WandDisplays.Source>();
            for(int i=0;i<count;i++) {
                var raw=Objects.requireNonNull(b.readNbt(NbtAccounter.create(65536)));
                if(!(raw instanceof CompoundTag magic))throw new IllegalArgumentException("Invalid source tag");var options=new ArrayList<WandDisplays.Option>();int choices=bounded(b,45);
                for(int j=0;j<choices;j++) {
                    int thread=b.readUnsignedByte(),tip=b.readUnsignedByte(),bases=b.readUnsignedByte();
                    if(thread>=MagicalThreadRecipe.types().size() || tip>WandTips.Tip.values().length)throw new IllegalArgumentException("Unknown wand component");
                    options.add(new WandDisplays.Option(MagicalThreadRecipe.types().get(thread),tip==0?Optional.empty():Optional.of(WandTips.Tip.values()[tip-1]),bases));
                }
                sources.add(new WandDisplays.Source(magic,options));
            }
            return new WandDisplayPayload(page,last,refresh,sources);
        }
        public void encode(RegistryFriendlyByteBuf b,WandDisplayPayload p){
            b.writeVarInt(p.page);b.writeBoolean(p.last);b.writeBoolean(p.refresh);b.writeVarInt(p.sources.size());
            for(var source:p.sources){b.writeNbt(source.magic());b.writeVarInt(source.options().size());for(var o:source.options()){b.writeByte(MagicalThreadRecipe.types().indexOf(o.thread()));b.writeByte(o.tip().map(t -> t.ordinal()+1).orElse(0));b.writeByte(o.bases());}}
        }
    };
    private static int bounded(RegistryFriendlyByteBuf b,int max){int n=b.readVarInt();if(n<0 || n>max)throw new IllegalArgumentException("Oversized wand display");return n;}
    public Type<WandDisplayPayload> type(){return TYPE;}
    @EventBusSubscriber(modid=VestigeMainMod.MOD_ID,bus=EventBusSubscriber.Bus.MOD)
    public static final class Registration {
        @SubscribeEvent public static void register(RegisterPayloadHandlersEvent e){e.registrar("1").playToClient(TYPE,CODEC,(p,c) -> WandViewerClient.accept(p));}
    }
    @EventBusSubscriber(modid=VestigeMainMod.MOD_ID)
    public static final class Sync {
        private static final Map<net.minecraft.resources.ResourceLocation,List<WandDisplays.Source>> BUILTIN=new HashMap<>();
        private static final Map<UUID,List<CompoundTag>> SEEN=new HashMap<>();
        @SubscribeEvent public static void sync(OnDatapackSyncEvent event){
            BUILTIN.clear();event.getRelevantPlayers().forEach(p -> send(p,false));
        }
        private static List<WandDisplays.Source> builtins(){
            if(BUILTIN.isEmpty())NativeMagic.spells().spells().values().stream().sorted(Comparator.comparing(s -> s.id().toString())).forEach(spell -> {
                try{BUILTIN.put(spell.id(),List.of(WandDisplays.compile(spell,ScrollItems.scroll(spell.id()))));}catch(IllegalArgumentException unsupported){ }
            });
            return BUILTIN.values().stream().flatMap(List::stream).sorted(Comparator.comparing(WandDisplays.Source::key)).toList();
        }
        /** Newly acquired shaped scrolls/wands get exact recipes without authoring another recipe file. */
        @SubscribeEvent public static void inventory(PlayerTickEvent.Post event){
            if(!(event.getEntity() instanceof ServerPlayer p) || p.tickCount%40!=0 || !NetworkRegistry.hasChannel(p.connection,TYPE.id()))return;
            var seen=new ArrayList<>(SEEN.getOrDefault(p.getUUID(),List.of()));boolean changed=false;
            for(var slot:p.containerMenu.slots){var item=slot.getItem();ItemStack source=item;
                if(WandData.binding(item).isPresent()){source=new ItemStack(ScrollItems.SCROLL.get());source.set(DataComponents.CUSTOM_DATA,CustomData.of(item.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getCompound("scroll")));}
                if(ScrollItems.scroll(source).isEmpty())continue;var magic=WandDisplays.magic(source);
                if(magic.equals(WandDisplays.magic(ScrollItems.scroll(ScrollItems.scroll(source).orElseThrow().spell()))) || seen.contains(magic))continue;
                seen.add(magic);if(seen.size()>64)seen.removeFirst();changed=true;
            }
            if(changed){SEEN.put(p.getUUID(),List.copyOf(seen));send(p,true);}
        }
        public static void send(ServerPlayer p,boolean refresh){
            if(p.connection==null || !NetworkRegistry.hasChannel(p.connection,TYPE.id()))return;
            var sources=new ArrayList<>(builtins());
            for(var magic:SEEN.getOrDefault(p.getUUID(),List.of())){
                var stack=new ItemStack(ScrollItems.SCROLL.get());stack.set(DataComponents.CUSTOM_DATA,CustomData.of(magic));
                var source=ScrollItems.scroll(stack).orElse(null);var spell=source==null?null:NativeMagic.spells().spells().get(source.spell());
                if(spell!=null)try{sources.add(WandDisplays.compile(spell,stack));}catch(IllegalArgumentException unsupported){ }
            }
            int pages=Math.max(1,(sources.size()+PAGE_SIZE-1)/PAGE_SIZE);if(pages>MAX_PAGES)throw new IllegalStateException("Too many wand display sources");
            for(int page=0;page<pages;page++)PacketDistributor.sendToPlayer(p,new WandDisplayPayload(page,page==pages-1,refresh,sources.subList(page*PAGE_SIZE,Math.min(sources.size(),(page+1)*PAGE_SIZE))));
        }
        @SubscribeEvent public static void stop(ServerStoppedEvent e){BUILTIN.clear();SEEN.clear();}
    }
}
