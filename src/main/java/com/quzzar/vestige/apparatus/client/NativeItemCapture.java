package com.quzzar.vestige.apparatus.client;

import com.google.gson.GsonBuilder;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import java.nio.file.*;
import java.util.*;

/** Opt-in native GUI inspection; exports Minecraft's framebuffer, never the desktop. */
@EventBusSubscriber(modid=VestigeMainMod.MOD_ID,value=Dist.CLIENT)
public final class NativeItemCapture {
    private static final boolean APPARATUS="apparatus_items".equals(System.getProperty("vestige.capture.kind"));
    private static final boolean ENABLED=APPARATUS || "items".equals(System.getProperty("vestige.capture.kind"));
    private static long start,next;
    private static int frame;
    private static boolean complete;
    private static final List<Double> times=new ArrayList<>();
    private static ItemScreen screen;
    private NativeItemCapture() { }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post event) throws java.io.IOException {
        if(!ENABLED || complete)return;
        var mc=Minecraft.getInstance();
        if(screen==null) {
            if(!(mc.screen instanceof TitleScreen) || mc.getOverlay()!=null)return;
            mc.options.guiScale().set(APPARATUS ? 2 : 4);mc.options.pauseOnLostFocus=false;
            screen=new ItemScreen();mc.setScreen(screen);start=System.nanoTime();next=start+1_000_000_000L;return;
        }
        long now=System.nanoTime();if(now<next)return;
        Path out=Path.of(System.getProperty("vestige.capture.output"),"items");Files.createDirectories(out);
        try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())) {
            image.writeToFile(out.resolve(String.format(Locale.ROOT,"frame-%05d.png",frame++)));
        }
        times.add((now-start)/1e9);next=now+66_666_667L;
        if(frame==48) {
            complete=true;
            var hashes=new LinkedHashMap<String,String>();
            var paths=new ArrayList<>(List.of("textures/item/spell_scroll.png","textures/item/attunement_shard.png","models/item/spell_scroll.json","models/item/attunement_shard.json"));
            if(APPARATUS)for(var material:ApparatusMaterials.values())for(var role:ApparatusBlock.Role.values())
                for(String type:List.of("item","block"))paths.add("models/"+type+"/"+material.blockName(role)+".json");
            for(String path:paths) {
                try(var asset=mc.getResourceManager().getResourceOrThrow(VestigeMainMod.location(path)).open()) {
                    hashes.put(path,HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(asset.readAllBytes())));
                } catch(java.security.NoSuchAlgorithmException impossible) { throw new IllegalStateException(impossible); }
            }
            var metadata=Map.of("source","Minecraft main render target after native GUI","engine","Minecraft 1.21.1 / NeoForge 21.1.72",
                    "frames",times,"font","minecraft:alt","assetSha256",hashes,"apparatusPalette",APPARATUS,
                    "shards",screen.shards.stream().map(s->Map.of("foil",s.hasFoil(),"key",AttunementShardItem.signature(s).map(Attunement.Signature::key).orElse("unattuned"))).toList());
            Files.writeString(out.resolve("capture.json"),new GsonBuilder().setPrettyPrinting().create().toJson(metadata)+"\n");
            mc.stop();
        }
    }
    private static final class ItemScreen extends Screen {
        private final List<ItemStack> shards=List.of(shard(Items.COPPER_BLOCK),shard(Items.COPPER_BLOCK),shard(Items.GOLD_BLOCK),new ItemStack(ScrollItems.ATTUNEMENT_SHARD.get()));
        private final ItemStack scroll=new ItemStack(ScrollItems.SCROLL.get());
        private ItemScreen() { super(Component.literal("Vestige item appearance")); }
        private static ItemStack shard(Item material) {
            var geometry=new LeylineShaping.Geometry(8,LeylineShaping.Shape.CROSS,2,0,LeylineShaping.Shape.DIAGONAL,4,0);
            var offerings=List.of(Items.AMETHYST_SHARD,Items.AMETHYST_SHARD,Items.ECHO_SHARD,Items.IRON_INGOT,Items.DIAMOND,Items.LAPIS_LAZULI,Items.AIR,Items.AIR);
            var nodes=new ArrayList<RitualInputs.Node>();
            for(int i=0;i<8;i++)nodes.add(new RitualInputs.Node(i,geometry.offset(i),new ItemStack(offerings.get(i)),i==0?new ItemStack(material):ItemStack.EMPTY));
            return AttunementShardItem.create(new RitualInputs(geometry,nodes));
        }
        @Override public void render(GuiGraphics graphics,int mouseX,int mouseY,float partialTick) {
            graphics.fill(0,0,width,height,0xff202126);
            if(APPARATUS) {
                graphics.drawString(font,"Approved apparatus finishes · Plinth / Spellstone",18,14,0xffffff,false);
                int columns=6,cell=(width-36)/columns,row=(height-52)/6,index=0;
                for(var material:ApparatusMaterials.values()) {
                    int x=18+(index%columns)*cell,y=38+(index/columns)*row;index++;
                    graphics.renderItem(new ItemStack(ApparatusBlocks.PLINTHS.get(material).get()),x,y);
                    graphics.renderItem(new ItemStack(ApparatusBlocks.SPELLSTONES.get(material).get()),x+20,y);
                    String label=material.id().replace('_',' ');
                    while(font.width(label)>cell-6)label=label.substring(0,label.length()-1);
                    graphics.drawString(font,label,x,y+18,0xbdbfc9,false);
                }
                return;
            }
            graphics.drawString(font,title,18,14,0xffffff,false);
            graphics.drawString(font,"Same attunement       Different       Unattuned       Scroll",18,35,0xbdbfc9,false);
            for(int i=0;i<5;i++) {
                int x=24+i*64;
                graphics.fill(x-1,51,x+17,69,0xff727272);
                var item=i<4?shards.get(i):scroll;
                graphics.renderItem(item,x,52);graphics.renderItemDecorations(font,item,x,52);
                if(i<4)AttunementShardItem.mark(item).ifPresent(mark->graphics.drawString(font,mark.component(),x,78,0xffffffff,false));
            }
            graphics.drawString(font,"Larger views · actual item renderer and enchanted glint",18,103,0xbdbfc9,false);
            graphics.pose().pushPose();graphics.pose().translate(28,124,0);graphics.pose().scale(4,4,4);
            graphics.renderItem(scroll,0,0);graphics.pose().popPose();
            graphics.pose().pushPose();graphics.pose().translate(128,124,0);graphics.pose().scale(4,4,4);
            graphics.renderItem(shards.getFirst(),0,0);graphics.renderItemDecorations(font,shards.getFirst(),0,0);graphics.pose().popPose();
            var tooltip=new ArrayList<Component>();tooltip.add(shards.getFirst().getHoverName());
            ScrollItems.ATTUNEMENT_SHARD.get().appendHoverText(shards.getFirst(),Item.TooltipContext.EMPTY,tooltip,TooltipFlag.NORMAL);
            graphics.renderTooltip(font,tooltip,Optional.empty(),215,135);
        }
        @Override public boolean isPauseScreen() { return false; }
    }
}
