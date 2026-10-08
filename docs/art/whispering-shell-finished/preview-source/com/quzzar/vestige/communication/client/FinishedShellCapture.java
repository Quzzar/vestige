package com.quzzar.vestige.communication.client;

import com.google.gson.GsonBuilder;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.*;
import com.quzzar.vestige.communication.WhisperingShellWorldTest;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.security.MessageDigest;

/** Review-only actual registered items, native hover/menu, held models and a client/server chat roundtrip. */
@EventBusSubscriber(modid=VestigeMainMod.MOD_ID,value=Dist.CLIENT)
public final class FinishedShellCapture {
    private static final boolean ENABLED="shell_finished".equals(System.getProperty("vestige.capture.kind"));
    private static int phase;
    private static long ready;
    private static MenuHost menu;
    private static CompletableFuture<Void> pending;
    private static WhisperingShellWorldTest.ChatPlayer peer, outsider;
    private static final List<String> captures=new ArrayList<>();
    private static final List<String> clientConversation=new ArrayList<>();
    private static final Map<String,Object> evidence=new LinkedHashMap<>();
    private static ItemStack shell() { return WhisperingShellItem.bound(WhisperingShellWorldTest.shard(Items.COPPER_BLOCK)); }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post event) throws Exception {
        if(!ENABLED || phase==99)return;
        var mc=Minecraft.getInstance(); long now=System.nanoTime();
        if(phase==0) {
            if(!(mc.screen instanceof TitleScreen) || mc.getOverlay()!=null)return;
            mc.options.guiScale().set(3); mc.options.pauseOnLostFocus=false; mc.resizeDisplay();
            menu=new MenuHost(false); mc.setScreen(menu); ready=now+2_000_000_000L; phase=1; return;
        }
        if(phase==1 && mc.screen==menu && now>ready && mc.getOverlay()==null) {
            save(mc,"shell-native-menu.png"); menu=new MenuHost(true);mc.setScreen(menu);ready=now+2_000_000_000L;phase=2;return;
        }
        if(phase==2 && mc.screen==menu && now>ready && mc.getOverlay()==null) {
            save(mc,"shell-bound-hover.png");
            for(var id:List.of("homebound_eye","whispering_shell")) for(var type:List.of("models/item/"+id+".json","textures/item/"+id+".png")) {
                try(var stream=mc.getResourceManager().getResourceOrThrow(VestigeMainMod.location(type)).open()) {
                    evidence.put(type,HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(stream.readAllBytes())));
                }
            }
            mc.setScreen(new TitleScreen());phase=3;
            mc.options.renderDistance().set(2);mc.options.enableVsync().set(false);mc.options.hideGui=false;
            mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
            mc.createWorldOpenFlows().createFreshLevel("shell-review-"+UUID.randomUUID(),
                new LevelSettings("Whispering Shell review",GameType.CREATIVE,false,Difficulty.PEACEFUL,true,new GameRules(),WorldDataConfiguration.DEFAULT),
                new WorldOptions(77132,false,false), access->access.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),mc.screen);
            return;
        }
        if(phase==3 && mc.level!=null && mc.player!=null && mc.screen==null && mc.getSingleplayerServer()!=null) {
            phase=4;
            var server=mc.getSingleplayerServer();pending=CompletableFuture.runAsync(()->{
                var player=server.getPlayerList().getPlayers().getFirst();var level=player.serverLevel();var pos=new BlockPos(0,81,0);
                for(int x=-8;x<=8;x++)for(int z=-8;z<=8;z++) {
                    level.setBlock(pos.offset(x,-1,z),Blocks.STONE.defaultBlockState(),3);
                    for(int y=0;y<4;y++)level.setBlock(pos.offset(x,y,z),Blocks.AIR.defaultBlockState(),3);
                }
                level.setDayTime(6000);player.teleportTo(level,.5,81,.5,Set.of(),0,12);
                player.getInventory().clearContent();player.getInventory().setItem(0,shell());player.getInventory().setItem(1,new ItemStack(Items.IRON_PICKAXE));
                player.getInventory().offhand.set(0,HomeboundEyeItem.bound(WhisperingShellItem.key(shell()).orElseThrow(),level.dimension(),pos,HomeboundEyeItem.Payment.DURABILITY));
                player.getInventory().selected=0;player.containerMenu.broadcastChanges();
                peer=new WhisperingShellWorldTest.ChatPlayer(level,"ShellReviewPeer");peer.getInventory().setItem(8,shell());
                outsider=new WhisperingShellWorldTest.ChatPlayer(level,"ReviewOutsider");
            },server);ready=now+4_000_000_000L;return;
        }
        if(phase>=4 && phase<=7 && mc.player!=null && mc.level!=null) {
            mc.getToasts().clear();
            mc.options.setCameraType(CameraType.FIRST_PERSON);mc.options.fov().set(70);mc.mouseHandler.releaseMouse();
            mc.player.setYRot(0);mc.player.yRotO=0;mc.player.setXRot(12);mc.player.xRotO=12;
        }
        if(phase==4 && pending.isDone() && now>ready) {
            pending.join();mc.gui.getChat().clearMessages(false);save(mc,"shell-and-eye-held.png");
            mc.getConnection().sendChat("A whisper from the shell.");phase=5;ready=now+1_000_000_000L;return;
        }
        if(phase==5 && now>ready) {
            pending=CompletableFuture.runAsync(()->{
                if(peer.chat().stream().noneMatch(p->p.body().content().equals("A whisper from the shell.")))throw new IllegalStateException("Client chat did not reach matching peer");
                if(!outsider.chat().isEmpty())throw new IllegalStateException("Client chat leaked publicly");
                peer.speak("Your whisper reached the matching shell.");
            },mc.getSingleplayerServer());phase=6;ready=now+180_000_000L;return;
        }
        if(phase==6 && pending.isDone() && now>ready) {
            pending.join();save(mc,"shell-conversation-cue.png");
            if(!outsider.chat().isEmpty())throw new IllegalStateException("Private reply leaked publicly");
            evidence.put("clientToMatchingPeer",true);evidence.put("outsiderReceived",outsider.chat().size());
            evidence.put("peerConversations",peer.chat().stream().map(p->p.body().content()).toList());
            phase=7;ready=now+800_000_000L;return;
        }
        if(phase==7 && now>ready) {
            save(mc,"shell-conversation.png");
            // Raw generated screenshots, including the production cue during a real chat roundtrip.
            evidence.put("captures",captures);evidence.put("source","Minecraft native framebuffer, registered production items; one real game client and two in-memory server packet clients");
            evidence.put("texturePixelsChanged",false);evidence.put("tooltip",List.of("Whispering Shell","four colored shard runes"));
            Files.writeString(output().resolve("capture.json"),new GsonBuilder().setPrettyPrinting().create().toJson(evidence)+"\n");
            pending=CompletableFuture.runAsync(()->{peer.close();outsider.close();},mc.getSingleplayerServer());phase=8;return;
        }
        if(phase==8 && pending.isDone()) {pending.join();phase=99;mc.stop();}
    }
    private static Path output() {return Path.of(System.getProperty("vestige.capture.output"));}
    private static void save(Minecraft mc,String name) throws Exception {Files.createDirectories(output());try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(output().resolve(name));}captures.add(name);}
    private static final class MenuHost extends Screen {
        private final ContainerScreen nativeMenu;
        private final boolean hover;
        private final ItemStack item;
        private MenuHost(boolean hover) {
            super(Component.literal("Whispering Shell"));this.hover=hover;item=hover?shell():new ItemStack(ScrollItems.WHISPERING_SHELL.get());
            var inventory=new Inventory(null);var contents=new SimpleContainer(27);
            var neighbors=List.of(Items.BONE,Items.COAL,Items.IRON_INGOT,Items.FLINT,Items.ENDER_PEARL,Items.SPIDER_EYE,Items.NAUTILUS_SHELL,Items.STRING,Items.LEATHER,
                Items.FEATHER,Items.REDSTONE,Items.LAPIS_LAZULI,Items.AMETHYST_SHARD,Items.PAPER,Items.ECHO_SHARD,Items.GLASS_BOTTLE,Items.HONEY_BOTTLE,Items.SNOWBALL,
                Items.ARROW,Items.IRON_SWORD,Items.IRON_PICKAXE,Items.BOW,Items.SHEARS,Items.FISHING_ROD,Items.BUCKET,Items.BREAD,Items.APPLE);
            for(int i=0;i<27;i++)contents.setItem(i,new ItemStack(neighbors.get(i)));contents.setItem(13,item);
            nativeMenu=new ContainerScreen(ChestMenu.threeRows(0,inventory,contents),inventory,title);
            if(hover)for(var flag:List.of(TooltipFlag.NORMAL,TooltipFlag.ADVANCED)) {
                var lines=item.getTooltipLines(Item.TooltipContext.EMPTY,null,flag);
                if(!lines.get(1).equals(AttunementMark.fromKey(WhisperingShellItem.key(item).orElseThrow()).component()))throw new IllegalStateException("Native tooltip signature missing");
            }
        }
        @Override protected void init(){nativeMenu.init(minecraft,width,height);}
        @Override public void render(GuiGraphics graphics,int mouseX,int mouseY,float tick){nativeMenu.render(graphics,0,0,tick);if(hover)graphics.renderTooltip(font,item,width/2+10,height/2-27);}
        @Override public boolean isPauseScreen(){return false;}
    }
}
