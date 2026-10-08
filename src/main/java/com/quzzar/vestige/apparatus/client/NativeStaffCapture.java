package com.quzzar.vestige.apparatus.client;

import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.*;
import com.quzzar.vestige.magic.world.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import org.lwjgl.glfw.GLFW;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/** Opt-in real client/server menu, slot selection, item model and casting inspection. */
@EventBusSubscriber(modid=VestigeMainMod.MOD_ID,value=Dist.CLIENT)
public final class NativeStaffCapture {
    private static final boolean ENABLED="staff_slots".equals(System.getProperty("vestige.capture.kind"));
    private static final List<Map<String,Object>> CHECKS=new ArrayList<>();
    private static int state,castWear;
    private static long started,next;
    private static CompletableFuture<Void> pending;
    private NativeStaffCapture() { }
    private static net.minecraft.resources.ResourceLocation id(String path) { return VestigeMainMod.location(path); }
    private static void require(boolean value,String detail) {
        if(!value) throw new IllegalStateException(detail);CHECKS.add(Map.of("check",detail,"passed",true));
    }
    private static void prepare(Minecraft mc,int capacity) {
        var server=mc.getSingleplayerServer();var player=server.getPlayerList().getPlayers().getFirst();
        var level=server.overworld();level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false,server);
        level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,server);level.setDayTime(6000);
        level.setWeatherParameters(6000,0,false,false);player.setGameMode(GameType.SURVIVAL);
        player.teleportTo(.5,4,-2.5);player.setYRot(0);player.setXRot(0);player.setNoGravity(true);NativeMana.set(player,100);
        for(var spell:List.of("fireball","firebolt","pf2_heal")) SpellKnowledge.identify(player,id(spell));
        castWear=StaffData.wear(NativeMagic.spells().spells().get(id("fireball")).rarity());
        var staff=StaffData.bind(StaffData.create(id("fire")),ScrollItems.scroll(id("fireball")),NativeMagic.spells().spells().get(id("fireball")));
        staff=StaffData.bind(StaffData.select(staff,1),ScrollItems.scroll(id("firebolt")),NativeMagic.spells().spells().get(id("firebolt")));
        if(capacity==6) {
            staff=StaffData.expand(StaffData.expand(staff));
            var source=ScrollItems.shapedScroll(id("fireball"),new LeylineShaping.Modifiers(1,1,1,1),List.of(new Spellshaping.Selection(id("reaching"),2)));
            staff=StaffData.bind(StaffData.select(staff,2),source,NativeMagic.spells().spells().get(id("fireball")));
        }
        staff=StaffData.select(staff,0);staff.setDamageValue(5);player.setItemInHand(InteractionHand.MAIN_HAND,staff);
        player.getInventory().setItem(9,ScrollItems.scroll(id("firebolt")).copyWithCount(3));
        player.getInventory().setItem(10,ScrollItems.scroll(id("pf2_heal")));
        player.getInventory().setItem(11,net.minecraft.world.item.ItemStack.EMPTY);
        player.getInventory().setItem(12,ScrollItems.scroll(id("fire_arrow")));
        player.inventoryMenu.broadcastChanges();StaffSelection.open(player,InteractionHand.MAIN_HAND);
    }
    private static List<Button> rows(StaffScreen screen) {
        return screen.children().stream().filter(Button.class::isInstance).map(Button.class::cast).toList();
    }
    private static void click(StaffScreen screen,int slot) {
        var row=rows(screen).get(slot);
        screen.mouseClicked(row.getX()+row.getWidth()/2.0,row.getY()+row.getHeight()/2.0,0);
        screen.mouseReleased(row.getX()+row.getWidth()/2.0,row.getY()+row.getHeight()/2.0,0);
    }
    private static void capture(Minecraft mc,String name) throws Exception {
        var out=Path.of(System.getProperty("vestige.capture.output"));Files.createDirectories(out);
        try(var frame=Screenshot.takeScreenshot(mc.getMainRenderTarget())) {
            frame.writeToFile(out.resolve(name+".png"));
            CHECKS.add(Map.of("capture",name,"pixels",List.of(frame.getWidth(),frame.getHeight()),
                    "gui",List.of(mc.getWindow().getGuiScaledWidth(),mc.getWindow().getGuiScaledHeight())));
        }
    }
    private static void slotClick(Minecraft mc,StaffScreen screen,int index) {
        var row=rows(screen).getFirst();var slot=screen.getMenu().getSlot(index);
        double x=row.getX()-30+slot.x+8,y=row.getY()-24+slot.y+8;
        screen.mouseClicked(x,y,0);screen.mouseReleased(x,y,0);
    }
    private static void shift(Minecraft mc,StaffScreen screen,int index) {
        mc.gameMode.handleInventoryMouseClick(screen.getMenu().containerId,index,0,net.minecraft.world.inventory.ClickType.QUICK_MOVE,mc.player);
    }
    private static void scroll(StaffScreen screen,int delta) {
        var row=rows(screen).getFirst();
        for(int i=0;i<Math.abs(delta);i++) screen.mouseScrolled(row.getX()+10,row.getY()+10,0,delta>0 ? -1 : 1);
    }
    private static void inspect(StaffScreen screen,int capacity) {
        require(screen.getTitle().getString().equals("Fire Staff: Fireball"),"Menu heading includes affinity and active spell at capacity "+capacity);
        require(Minecraft.getInstance().player.getMainHandItem().getHoverName().getString().equals(screen.getTitle().getString()),"Item and menu names agree at capacity "+capacity);
        var rows=rows(screen);require(rows.size()==Math.min(3,capacity),"Native menu exposes only the visible rows at capacity "+capacity);
        require(screen.getMenu().capacity()==capacity,"Server menu retains all "+capacity+" slots");
        require(rows.stream().allMatch(b -> b.getHeight()==20),"All slot buttons retain fixed height");
        require(rows.get(0).getMessage().getString().contains("Fireball"),"Identified binding reveals its name");
        require(rows.get(1).getMessage().getString().contains("Firebolt"),"Second identified binding reveals its name");
        require(rows.stream().filter(row -> ((StaffScreen.SpellButton)row).selected()).count()==1
                && ((StaffScreen.SpellButton)rows.get(0)).selected(),"Only the active spell has the persistent selected state");
        require(rows.stream().noneMatch(b -> b.getMessage().getString().matches(".*[1-6]  .*")),"Rows have no slot numbers");
        if(capacity==6) require(rows.get(2).getMessage().getString().contains("Reaching"),"Stored shaping is visible");
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post event) {
        if(!ENABLED || state==99) return;var mc=Minecraft.getInstance();long now=System.nanoTime();if(started==0) started=now;
        try {
            if(now-started>240_000_000_000L) throw new IllegalStateException("Staff capture timed out at step "+state);
            mc.getToasts().clear();mc.gui.getChat().clearMessages(false);
            if(state==0 && mc.screen instanceof TitleScreen && mc.getOverlay()==null) {
                GLFW.glfwSetWindowSize(mc.getWindow().getWindow(),960,720);mc.options.guiScale().set(3);mc.resizeDisplay();
                mc.options.pauseOnLostFocus=false;mc.options.renderDistance().set(4);mc.options.simulationDistance().set(5);
                mc.options.enableVsync().set(false);mc.getWindow().setFramerateLimit(60);mc.getTutorial().setStep(TutorialSteps.NONE);state=1;
                mc.createWorldOpenFlows().createFreshLevel("vestige-staff-review-"+UUID.randomUUID(),
                        new LevelSettings("Native staff review",GameType.SURVIVAL,false,Difficulty.PEACEFUL,true,new GameRules(),WorldDataConfiguration.DEFAULT),
                        new WorldOptions(758934,false,false),access -> access.registryOrThrow(Registries.WORLD_PRESET)
                                .getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),mc.screen);
            } else if(state==1 && mc.player!=null && mc.getSingleplayerServer()!=null && mc.screen==null) {
                state=2;pending=mc.getSingleplayerServer().submit(() -> prepare(mc,2));next=now+1_500_000_000L;
            } else if(state==2 && pending.isDone() && mc.screen instanceof StaffScreen screen && now>=next) {
                pending.join();inspect(screen,2);capture(mc,"two-slots");click(screen,1);state=3;next=now+800_000_000L;
            } else if(state==3 && mc.screen instanceof StaffScreen screen && now>=next && StaffData.binding(mc.player.getMainHandItem()).orElseThrow().selected()==1) {
                require(screen.getTitle().getString().equals("Fire Staff: Firebolt"),"Selection updates the open menu to the newly active known spell");
                require(((StaffScreen.SpellButton)rows(screen).get(1)).selected() && !((StaffScreen.SpellButton)rows(screen).get(0)).selected(),"Persistent selected styling follows the new active spell");
                require(mc.player.getMainHandItem().getDamageValue()==5,"Selecting a slot spends no durability");
                require(mc.player.getMainHandItem().getHoverName().getString().equals(screen.getTitle().getString()),"Selection updates the actual item name");
                capture(mc,"selected-firebolt");screen.onClose();state=31;next=now+500_000_000L;
            } else if(state==31 && mc.screen==null && now>=next) {
                pending=mc.getSingleplayerServer().submit(() -> prepare(mc,6));state=4;next=now+1_500_000_000L;
            } else if(state==4 && pending.isDone() && mc.screen instanceof StaffScreen screen && screen.getMenu().capacity()==6 && now>=next) {
                pending.join();inspect(screen,6);capture(mc,"six-slots");slotClick(mc,screen,2);state=5;next=now+800_000_000L;
            } else if(state==5 && mc.screen instanceof StaffScreen screen && now>=next) {
                var carried=ScrollItems.scroll(screen.getMenu().getCarried()).orElseThrow();
                require(carried.augments().equals(List.of(new Spellshaping.Selection(id("reaching"),2))),"Native cursor receives the exact stored augment variant");
                require(StaffData.binding(mc.player.getMainHandItem()).orElseThrow().slots().get(2).isEmpty(),"Picking up a scroll clears its staff binding");
                capture(mc,"carried-scroll");slotClick(mc,screen,8);state=51;next=now+800_000_000L;
            } else if(state==51 && mc.screen instanceof StaffScreen screen && now>=next) {
                require(screen.getMenu().getCarried().isEmpty() && ScrollItems.scroll(mc.player.getInventory().getItem(11)).isPresent(),"Real inventory slot receives removed scroll");
                shift(mc,screen,8);state=52;next=now+800_000_000L;
            } else if(state==52 && mc.screen instanceof StaffScreen screen && now>=next) {
                require(StaffData.binding(mc.player.getMainHandItem()).orElseThrow().slots().get(2).orElseThrow().augments()
                        .equals(List.of(new Spellshaping.Selection(id("reaching"),2))),"Shift-click restores the same variant without applying shaping again");
                require(mc.player.getInventory().getItem(11).isEmpty(),"Restoring scroll consumes exactly its inventory item");
                scroll(screen,3);shift(mc,screen,6);state=53;next=now+800_000_000L;
            } else if(state==53 && mc.screen instanceof StaffScreen screen && now>=next) {
                require(screen.getMenu().firstVisible()==3 && screen.getMenu().getSlot(3).isActive() && !screen.getMenu().getSlot(0).isActive(),"Scrolling exposes last three native slots and hides first three");
                require(mc.player.getInventory().getItem(9).isEmpty() && StaffData.binding(mc.player.getMainHandItem()).orElseThrow().slots().subList(3,6).stream().allMatch(Optional::isPresent),"Shift-click distributes a stack into three empty bindings, one scroll each");
                capture(mc,"filled-scrolled-slots");slotClick(mc,screen,5);state=531;next=now+800_000_000L;
            } else if(state==531 && mc.screen instanceof StaffScreen screen && now>=next) {
                require(StaffData.binding(mc.player.getMainHandItem()).orElseThrow().slots().get(5).isEmpty(),"Last scrolled scroll slot maps to its original server index");
                slotClick(mc,screen,8);state=532;next=now+800_000_000L;
            } else if(state==532 && mc.screen instanceof StaffScreen screen && now>=next) {
                require(screen.getMenu().getCarried().isEmpty(),"Removed last-slot scroll returns to inventory");
                shift(mc,screen,7);state=54;next=now+800_000_000L;
            } else if(state==54 && mc.screen instanceof StaffScreen screen && now>=next) {
                require(mc.player.getInventory().getItem(10).getCount()==1 && StaffData.binding(mc.player.getMainHandItem()).orElseThrow().slots().get(5).isEmpty(),"Identified scroll without the staff affinity is rejected by server slots");
                require(!screen.getMenu().getSlot(5).mayPlace(mc.player.getInventory().getItem(12)),"Client prevents optimistic insertion of an unknown matching scroll");
                require(!rows(screen).get(2).active && rows(screen).get(2).getMessage().getString().equals("Empty"),"Empty scroll slot keeps its ordinary item square and disabled Empty selection button");
                click(screen,2);capture(mc,"empty-slot");
                shift(mc,screen,9);state=541;next=now+800_000_000L;
            } else if(state==541 && mc.screen instanceof StaffScreen screen && now>=next) {
                require(mc.player.getInventory().getItem(12).getCount()==1 && StaffData.binding(mc.player.getMainHandItem()).orElseThrow().slots().get(5).isEmpty(),"Server rejects unknown Fire scroll sent by a real Shift-click packet");
                require(screen.getMenu().selected()==0,"Clicking an empty disabled row keeps the active spell");
                capture(mc,"scrolled-slots");slotClick(mc,screen,3);state=55;next=now+800_000_000L;
            } else if(state==55 && mc.screen instanceof StaffScreen screen && now>=next) {
                require(StaffData.binding(mc.player.getMainHandItem()).orElseThrow().slots().get(3).isEmpty()
                        && ScrollItems.scroll(screen.getMenu().getCarried()).orElseThrow().spell().equals(id("firebolt")),"Clicking a scrolled slot removes the correct server-indexed binding");
                screen.onClose();state=56;next=now+800_000_000L;
            } else if(state==56 && mc.screen==null && now>=next) {
                require(mc.player.getInventory().items.stream().filter(stack -> ScrollItems.scroll(stack).filter(source -> source.spell().equals(id("firebolt"))).isPresent())
                        .mapToInt(net.minecraft.world.item.ItemStack::getCount).sum()==2,"Closing returns the carried scroll exactly once");
                require(mc.player.getMainHandItem().getDamageValue()==5,"All scroll transfers preserve staff wear");
                pending=mc.getSingleplayerServer().submit(() -> {StaffSelection.open(mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst(),InteractionHand.MAIN_HAND);});
                state=57;next=now+800_000_000L;
            } else if(state==57 && pending.isDone() && mc.screen instanceof StaffScreen screen && now>=next) {
                pending.join();mc.options.guiScale().set(2);mc.resizeDisplay();state=58;next=now+800_000_000L;
            } else if(state==58 && mc.screen instanceof StaffScreen screen && now>=next) {
                inspect(screen,6);capture(mc,"six-slots-small");screen.onClose();state=6;next=now+800_000_000L;
            } else if(state==6 && mc.screen==null && now>=next && StaffData.binding(mc.player.getMainHandItem()).orElseThrow().selected()==0) {
                capture(mc,"held-staff");state=62;next=now+800_000_000L;
            } else if(state==62 && mc.screen==null && now>=next) {
                mc.gameMode.useItem(mc.player,InteractionHand.MAIN_HAND);state=7;next=now+1_500_000_000L;
            } else if(state==7 && mc.screen==null && now>=next && mc.player.getMainHandItem().getDamageValue()==5+castWear) {
                capture(mc,"after-cast");pending=mc.getSingleplayerServer().submit(() -> {
                    var player=mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst();
                    require(NativeMana.amount(player)<100 && player.getMainHandItem().getDamageValue()==5+castWear,"Real use packet casts with normal mana and exactly "+castWear+" rarity-based durability loss");
                    require(!SpellKnowledge.identified(player,id("fire_arrow")),"Rejected unknown scroll remains unidentified");
                });state=8;
            } else if(state==8 && pending.isDone()) {
                pending.join();var hashes=new LinkedHashMap<String,String>();
                for(var path:List.of("models/item/staff.json","textures/item/staff.png")) {
                    try(var stream=mc.getResourceManager().getResourceOrThrow(id(path)).open()) {
                        hashes.put(path,HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(stream.readAllBytes())));
                    }
                }
                Files.writeString(Path.of(System.getProperty("vestige.capture.output"),"verification.json"),new GsonBuilder().setPrettyPrinting()
                        .create().toJson(Map.of("engine","Minecraft 1.21.1 / NeoForge 21.1.72","source","Unedited Minecraft framebuffer",
                                "checks",CHECKS,"assetSha256",hashes))+"\n");state=99;mc.stop();
            }
        } catch(Exception error) {
            state=99;LogUtils.getLogger().error("Native staff inspection failed",error);
            try {var out=Path.of(System.getProperty("vestige.capture.output"));Files.createDirectories(out);Files.writeString(out.resolve("error.txt"),error.toString());} catch(Exception ignored) { }
            mc.stop();
        }
    }
}
