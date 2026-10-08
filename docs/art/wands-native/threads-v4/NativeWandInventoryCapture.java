package com.quzzar.vestige.apparatus.client;

import com.google.gson.GsonBuilder;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.Difficulty;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import java.nio.file.*;
import java.util.*;

/** Opt-in native survival inventory comparison. Uses vanilla slots, lighting and item rendering. */
@EventBusSubscriber(modid=VestigeMainMod.MOD_ID,value=Dist.CLIENT)
public final class NativeWandInventoryCapture {
    private static final boolean ENABLED="wand_inventory".equals(System.getProperty("vestige.capture.kind"));
    private static int state,index;
    private static long deadline;
    private static final List<Map<String,Object>> captures=new ArrayList<>();
    private static final String[] NAMES={"wands-and-threads","beside-vanilla-materials","threads-beside-string-and-lead"};
    private NativeWandInventoryCapture(){ }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post event)throws Exception {
        if(!ENABLED || state==5)return;
        var mc=Minecraft.getInstance();long now=System.nanoTime();
        mc.options.pauseOnLostFocus=false;mc.options.framerateLimit().set(30);mc.getWindow().setFramerateLimit(30);
        if(mc.player!=null){mc.getToasts().clear();mc.gui.getChat().clearMessages(false);}
        if(state==0 && mc.screen instanceof TitleScreen && mc.getOverlay()==null){
            mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
            mc.getWindow().setWindowed(960,720);mc.options.guiScale().set(0);mc.resizeDisplay();state=1;deadline=now+180_000_000_000L;
            mc.createWorldOpenFlows().createFreshLevel("vestige-wand-inventory-"+UUID.randomUUID(),
                new LevelSettings("Wand inventory comparison",GameType.SURVIVAL,false,Difficulty.PEACEFUL,true,new GameRules(),WorldDataConfiguration.DEFAULT),
                new WorldOptions(48103,false,false),access -> access.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),mc.screen);
        }else if(state==1 && mc.player!=null && mc.level!=null && mc.getSingleplayerServer()!=null){
            install(mc);state=2;deadline=now+60_000_000_000L;
        }else if(state==2 && matches(mc)){
            mc.setScreen(new ComparisonInventoryScreen(mc.player));state=3;deadline=now+3_000_000_000L;
        }else if(state==3 && now>deadline){
            if(!(mc.screen instanceof InventoryScreen) || !matches(mc))throw new IllegalStateException("Native inventory comparison lost its exact contents");
            var out=Path.of(System.getProperty("vestige.capture.output"),"inventory");Files.createDirectories(out);
            try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(out.resolve(NAMES[index]+".png"));}
            var contents=new ArrayList<Map<String,Object>>();var items=items(index);
            for(int slot=0;slot<items.size();slot++){
                var stack=items.get(slot);var detail=new LinkedHashMap<String,Object>();detail.put("slot",slot);detail.put("item",BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
                WandData.binding(stack).ifPresent(binding -> {detail.put("base",binding.base().id());detail.put("tip",binding.tip().map(WandTips.Tip::id).orElse("untipped"));});contents.add(detail);
            }
            captures.add(Map.of("case",NAMES[index],"screen",mc.screen.getClass().getName(),"renderer",InventoryScreen.class.getName(),"guiWidth",mc.getWindow().getGuiScaledWidth(),"guiHeight",mc.getWindow().getGuiScaledHeight(),"contents",contents));
            if(++index==NAMES.length){
                var hashes=new TreeMap<String,String>();
                var assets=new ArrayList<String>();
                for(var base:WandComponents.Base.values())assets.add("textures/item/wands/base_"+base.id()+".png");
                for(var tip:WandTips.Tip.values())assets.add("textures/item/wands/tip_"+tip.id()+".png");
                for(var thread:MagicalThreadRecipe.types())assets.add("textures/item/"+thread.id().getPath()+".png");
                for(var path:assets)try(var input=mc.getResourceManager().getResourceOrThrow(VestigeMainMod.location(path)).open()){
                    var bytes=input.readAllBytes();try(var image=com.mojang.blaze3d.platform.NativeImage.read(bytes)){if(image.getWidth()!=16 || image.getHeight()!=16)throw new IllegalStateException("Wrong item texture size");}
                    hashes.put(path,HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(bytes)));
                }
                Files.writeString(out.resolve("capture.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("source","Unedited Minecraft framebuffer, vanilla survival InventoryScreen renderer; review cursor kept over avatar and notifications cleared","engine","Minecraft 1.21.1 / NeoForge 21.1.72","captures",captures,"loaded16x16AssetSha256",hashes))+"\n");state=5;mc.stop();return;
            }
            install(mc);state=2;deadline=now+60_000_000_000L;
        }
        if((state==1 || state==2) && now>deadline)throw new IllegalStateException("Native inventory capture timed out at phase "+state);
    }
    private static final class ComparisonInventoryScreen extends InventoryScreen {
        ComparisonInventoryScreen(Player player){super(player);}
        @Override public void render(GuiGraphics graphics,int mouseX,int mouseY,float partialTick){
            // The review keeps the cursor over the avatar, leaving the item comparison unobstructed.
            super.render(graphics,(width-176)/2+51,(height-166)/2+45,partialTick);
        }
    }
    private static void install(Minecraft mc){
        var contents=items(index);var uuid=mc.player.getUUID();
        mc.getSingleplayerServer().execute(() -> {
            var player=mc.getSingleplayerServer().getPlayerList().getPlayer(uuid);
            player.getInventory().clearContent();
            for(int slot=0;slot<contents.size();slot++)player.getInventory().setItem(slot,contents.get(slot).copy());
            player.inventoryMenu.broadcastChanges();
        });
    }
    private static boolean matches(Minecraft mc){
        if(mc.player==null)return false;var expected=items(index);
        for(int slot=0;slot<expected.size();slot++)if(!ItemStack.matches(expected.get(slot),mc.player.getInventory().getItem(slot)))return false;
        return true;
    }
    private static ItemStack wand(WandComponents.Base base,Optional<WandTips.Tip> tip){return WandData.create(base,MagicalThreadRecipe.Type.CALLOUS,tip,ScrollItems.scroll(VestigeMainMod.location("fireball")));}
    private static List<ItemStack> items(int view){
        if(view==2){
            var result=new ArrayList<ItemStack>();
            for(var item:List.of(Items.STRING,Items.LEAD,Items.LEATHER,Items.PAPER,Items.AMETHYST_SHARD,Items.DIAMOND,Items.IRON_INGOT,Items.GOLD_INGOT,Items.EMERALD))result.add(new ItemStack(item));
            var threads=MagicalThreadRecipe.types();
            for(var thread:threads)result.add(new ItemStack(thread.item()));
            for(var item:List.of(Items.STRING,Items.LEAD,Items.GLOWSTONE_DUST,Items.BLAZE_POWDER))result.add(new ItemStack(item));
            for(int i=0;i<4;i++){result.add(new ItemStack(Items.STRING));result.add(new ItemStack(threads.get(i).item()));}
            result.add(new ItemStack(Items.STRING));
            result.add(new ItemStack(threads.get(4).item()));result.add(new ItemStack(Items.LEAD));
            for(var base:WandComponents.Base.values())result.add(wand(base,Optional.empty()));
            if(result.size()!=36)throw new IllegalStateException("Wrong thread comparison fixture size");
            return List.copyOf(result);
        }
        var hotbar=List.of(Items.STICK,Items.BAMBOO,Items.END_ROD,Items.LIGHTNING_ROD,Items.EMERALD,Items.IRON_SWORD,Items.NETHERITE_PICKAXE,Items.BOW,Items.PAPER);
        var result=new ArrayList<ItemStack>();hotbar.forEach(i -> result.add(new ItemStack(i)));
        var bases=WandComponents.Base.values();var tips=WandTips.Tip.values();
        if(view==0){
            for(var base:bases)result.add(wand(base,Optional.empty()));
            result.add(new ItemStack(Items.BONE));result.add(new ItemStack(Items.BLAZE_ROD));
            for(int i=0;i<tips.length;i++)result.add(wand(bases[i%bases.length],Optional.of(tips[i])));
            result.add(new ItemStack(Items.BREEZE_ROD));
        }else{
            for(int i=0;i<bases.length;i++){result.add(new ItemStack(bases[i].ingredient()));result.add(wand(bases[i],Optional.of(tips[i])));}
            result.add(wand(bases[0],Optional.of(tips[7])));
            result.add(new ItemStack(Items.DIAMOND));result.add(new ItemStack(Items.AMETHYST_SHARD));result.add(new ItemStack(Items.NETHERITE_INGOT));
        }
        for(var thread:MagicalThreadRecipe.types())result.add(new ItemStack(thread.item()));
        for(var item:List.of(Items.STRING,Items.LEAD,Items.AMETHYST_SHARD,Items.DIAMOND))result.add(new ItemStack(item));
        if(result.size()!=36)throw new IllegalStateException("Wrong native inventory fixture size");
        return List.copyOf(result);
    }
}
