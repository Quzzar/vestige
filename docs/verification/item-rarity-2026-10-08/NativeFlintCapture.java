package com.quzzar.vestige.apparatus.client;

import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.*;
import com.quzzar.vestige.equipment.*;
import com.quzzar.vestige.magic.definition.SpellRarity;
import com.quzzar.vestige.magic.world.NativeMagic;
import com.quzzar.vestige.travel.StandingStones;
import net.minecraft.ChatFormatting;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.Difficulty;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/** Isolated native tooltip-color evidence; replaces only the temporary repair capture. */
@EventBusSubscriber(modid=VestigeMainMod.MOD_ID,value=Dist.CLIENT)
public final class NativeFlintCapture {
    private static final boolean ENABLED="fluxed_flint".equals(System.getProperty("vestige.capture.kind"));
    private static final List<Map<String,Object>> CHECKS=new ArrayList<>();
    private static List<ItemStack> extras=List.of();
    private static int state;
    private static long started,next;
    private static CompletableFuture<Void> pending;
    private NativeFlintCapture() { }
    private static Path out(){return Path.of(System.getProperty("vestige.capture.output"));}
    private static void require(boolean value,String description){
        if(!value)throw new IllegalStateException(description);
        CHECKS.add(Map.of("check",description,"passed",true));
    }
    private static List<ItemStack> featured(){
        return List.of(new ItemStack(ScrollItems.DISSENTIENT_DIAMOND.get()),new ItemStack(ScrollItems.ATTUNEMENT_SHARD.get()),
                new ItemStack(ScrollItems.HOMEBOUND_EYE.get()),new ItemStack(ScrollItems.WHISPERING_SHELL.get()),
                StaffData.create(VestigeMainMod.location("fire")),new ItemStack(MagicEquipment.WARDWEAVE.get()),
                new ItemStack(MagicEquipment.CINDERWEAVE.get()),WayfarerImbuements.create(Set.of()),
                new ItemStack(ScrollItems.CRANE_BAG.get()),new ItemStack(ScrollItems.FLUXED_FLINT.get()));
    }
    private static Component title(ItemStack stack){
        var mc=Minecraft.getInstance();
        return stack.getTooltipLines(Item.TooltipContext.of(mc.level),mc.player,TooltipFlag.NORMAL).getFirst();
    }
    private static void color(ItemStack stack,int expected,String description){
        title(stack).visit((style,text)->{
            if(!text.isBlank() && (style.getColor()==null || style.getColor().getValue()!=expected))
                throw new IllegalStateException(description+": incorrect color on "+text);
            return Optional.<Boolean>empty();
        },Style.EMPTY);
        require(true,description);
    }
    private static void prepare(Minecraft mc){
        var server=mc.getSingleplayerServer();var level=server.overworld();
        var player=server.getPlayerList().getPlayers().getFirst();
        level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false,server);
        level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,server);
        level.setDayTime(6000);level.setWeatherParameters(6000,0,false,false);
        for(int x=-7;x<=7;x++)for(int z=-7;z<=7;z++)level.setBlockAndUpdate(new BlockPos(x,0,z),Blocks.SMOOTH_STONE.defaultBlockState());
        player.connection.teleport(3.4,2.8,-4.4,36.75f,19);player.setNoGravity(true);
        for(int i=0;i<player.getInventory().getContainerSize();i++)player.getInventory().setItem(i,ItemStack.EMPTY);
        var shown=new ArrayList<>(featured());
        for(var type:MagicalThreadRecipe.types())shown.add(new ItemStack(type.item()));
        shown.add(new ItemStack(MundaneStaffs.item(MundaneStaffs.Shaft.STICK)));
        shown.add(new ItemStack(ScrollItems.FRAGMENT.get()));shown.add(new ItemStack(ApparatusBlocks.SPELLSTONE.get()));
        shown.add(new ItemStack(ApparatusBlocks.PLINTH.get()));shown.add(new ItemStack(StandingStones.ITEM.get()));
        for(int i=0;i<shown.size();i++)player.getInventory().setItem(9+i,shown.get(i));
        player.inventoryMenu.broadcastChanges();
    }
    private static void verify(Minecraft mc){
        for(int i=0;i<10;i++){
            var stack=mc.player.getInventory().getItem(9+i);var expected=i<8 ? Rarity.UNCOMMON : Rarity.RARE;
            require(stack.is(featured().get(i).getItem()) && stack.getRarity()==expected,"Registered rarity: "+BuiltInRegistries.ITEM.getKey(stack.getItem()));
            color(stack,i<8 ? 0xffff55 : 0x55ffff,"Native title color: "+stack.getHoverName().getString());
        }
        for(var type:MagicalThreadRecipe.types()){
            var stack=new ItemStack(type.item());
            require(stack.getRarity()==Rarity.COMMON && stack.hasFoil(),"Thread stays Common with its existing shimmer: "+type.id());
            color(stack,0xffffff,"White thread title: "+stack.getHoverName().getString());
        }
        var common=new ArrayList<Item>();common.add(ScrollItems.FRAGMENT.get());common.addAll(MundaneStaffs.all());
        ApparatusBlocks.all().forEach(b->common.add(b.asItem()));StandingStones.STONE_ITEMS.values().forEach(h->common.add(h.get()));
        require(common.stream().allMatch(i->new ItemStack(i).getRarity()==Rarity.COMMON),"Fragments, all mundane staffs and every apparatus finish remain Common");
        for(int mask=0;mask<16;mask++){
            var choices=EnumSet.noneOf(WayfarerImbuements.Choice.class);
            for(var c:WayfarerImbuements.Choice.values())if((mask & 1<<c.ordinal())!=0)choices.add(c);
            var stack=WayfarerImbuements.create(choices);
            require(stack.getRarity()==Rarity.UNCOMMON,"Wayfarer variant base rarity "+mask);
            color(stack,0xffff55,"Wayfarer variant full-name color "+mask);
        }
        for(var route:HomeboundEyeItem.Payment.values()){
            var stack=HomeboundEyeItem.bound("0".repeat(64),Level.OVERWORLD,BlockPos.ZERO,route);
            color(stack,0xffff55,"Homebound Eye route full-name color: "+route);
        }
        var staff=StaffData.create(VestigeMainMod.location("fire"));
        for(int capacity:List.of(2,4,6)){
            if(capacity>2)staff=StaffData.expand(staff);
            color(staff,0xffff55,"Staff capacity keeps yellow title: "+capacity);
        }
        var knowledge=new HashMap<net.minecraft.resources.ResourceLocation,SpellRarity>();
        var spells=new EnumMap<SpellRarity,com.quzzar.vestige.magic.definition.SpellDefinition>(SpellRarity.class);
        for(var spell:NativeMagic.spells().spells().values())spells.putIfAbsent(spell.rarity(),spell);
        for(var spell:spells.values())knowledge.put(spell.id(),spell.rarity());
        SpellKnowledge.updateVisible(knowledge);
        for(var entry:spells.entrySet()){
            int rgb=switch(entry.getKey()){case COMMON->0xffffff;case UNCOMMON->0xffff55;case RARE->0x55ffff;case MYTHIC->0xff55ff;};
            var scroll=ScrollItems.scroll(entry.getValue().id());
            color(scroll,rgb,"Known scroll retains spell color: "+entry.getKey());
            color(WandData.create(WandComponents.Base.STICK,MagicalThreadRecipe.Type.CALLOUS,scroll),rgb,"Known wand retains spell color: "+entry.getKey());
        }
        color(new ItemStack(ScrollItems.SCROLL.get()),0xffffff,"Unknown scroll remains white");
        color(new ItemStack(ScrollItems.WAND.get()),0xffffff,"Unknown wand remains white");
        var enchanted=new ItemStack(MagicEquipment.WARDWEAVE.get());
        enchanted.enchant(mc.level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.PROTECTION),1);
        require(enchanted.get(DataComponents.RARITY)==Rarity.UNCOMMON && enchanted.getRarity()==Rarity.RARE,"Vanilla enchantment promotes armor display without changing its base rarity");
        color(enchanted,0x55ffff,"Enchanted robe uses vanilla aqua title");
        var shaped=WayfarerImbuements.create(EnumSet.allOf(WayfarerImbuements.Choice.class));
        var seen=new ArrayList<Boolean>();
        title(shaped).visit((style,text)->{if(text.contains("Unfaltering"))seen.add(style.isItalic());return Optional.<Boolean>empty();},Style.EMPTY);
        require(!seen.isEmpty() && seen.stream().allMatch(Boolean::booleanValue),"Combined adjective preserves italics with inherited yellow color");
        extras=List.of(shaped,HomeboundEyeItem.bound("0".repeat(64),Level.OVERWORLD,BlockPos.ZERO,HomeboundEyeItem.Payment.MANA),staff,enchanted,
                new ItemStack(ScrollItems.ENSORCELLED_THREAD.get()),new ItemStack(MundaneStaffs.item(MundaneStaffs.Shaft.STICK)),
                ScrollItems.scroll(spells.get(SpellRarity.RARE).id()),WandData.create(WandComponents.Base.STICK,MagicalThreadRecipe.Type.CALLOUS,ScrollItems.scroll(spells.get(SpellRarity.RARE).id())),
                new ItemStack(ScrollItems.SCROLL.get()));
    }
    private static void capture(Minecraft mc,String name)throws Exception{
        Files.createDirectories(out());try(var frame=Screenshot.takeScreenshot(mc.getMainRenderTarget())){frame.writeToFile(out().resolve(name+".png"));}
    }
    @SubscribeEvent public static void tooltip(net.neoforged.neoforge.client.event.ScreenEvent.Render.Post event){
        if(!ENABLED || state!=4 && state!=5)return;var mc=Minecraft.getInstance();
        if(!(mc.screen instanceof InventoryScreen))return;
        int left=(mc.getWindow().getGuiScaledWidth()-176)/2,top=(mc.getWindow().getGuiScaledHeight()-166)/2;
        for(int i=0;i<10;i++)event.getGuiGraphics().renderTooltip(mc.font,List.of(title(mc.player.getInventory().getItem(9+i))),Optional.empty(),left-160,top-80+i*18);
        for(int i=0;i<extras.size();i++)event.getGuiGraphics().renderTooltip(mc.font,List.of(title(extras.get(i))),Optional.empty(),left+184,top-80+i*18);
    }
    @SubscribeEvent public static void frame(net.neoforged.neoforge.client.event.RenderFrameEvent.Post event){
        if(!ENABLED || state==99)return;var mc=Minecraft.getInstance();long now=System.nanoTime();if(started==0)started=now;
        try{
            if(now-started>240_000_000_000L)throw new IllegalStateException("Rarity capture timed out at "+state);
            mc.getToasts().clear();mc.gui.getChat().clearMessages(false);
            if(state==0 && mc.screen instanceof TitleScreen && mc.getOverlay()==null){
                mc.options.pauseOnLostFocus=false;mc.options.renderDistance().set(4);mc.options.simulationDistance().set(5);
                mc.options.enableVsync().set(false);mc.getWindow().setFramerateLimit(60);mc.getTutorial().setStep(TutorialSteps.NONE);state=1;
                mc.createWorldOpenFlows().createFreshLevel("vestige-rarity-"+UUID.randomUUID(),new LevelSettings("Item rarity review",GameType.SURVIVAL,false,Difficulty.PEACEFUL,true,new GameRules(),WorldDataConfiguration.DEFAULT),
                        new WorldOptions(72586,false,false),access->access.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),mc.screen);
            }else if(state==1 && mc.player!=null && mc.getSingleplayerServer()!=null && mc.screen==null){
                pending=mc.getSingleplayerServer().submit(()->prepare(mc));state=2;next=now+2_000_000_000L;
            }else if(state==2 && pending.isDone() && now>=next){
                pending.join();verify(mc);mc.getWindow().setWindowed(960,720);mc.options.guiScale().set(3);mc.resizeDisplay();state=3;next=now+1_000_000_000L;
            }else if(state==3 && now>=next){
                mc.setScreen(new InventoryScreen(mc.player));state=4;next=now+800_000_000L;
            }else if(state==4 && now>=next){
                capture(mc,"rarity-scale-3");mc.options.guiScale().set(2);mc.resizeDisplay();state=5;next=now+800_000_000L;
            }else if(state==5 && now>=next){
                capture(mc,"rarity-scale-2");Files.writeString(out().resolve("verification.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of(
                        "engine","Minecraft 1.21.1 / NeoForge 21.1.72","source","Unedited framebuffer; actual registered inventory items; real native tooltip first lines rendered in two comparison columns",
                        "checks",CHECKS))+"\n");state=99;mc.stop();
            }
        }catch(Exception error){state=99;LogUtils.getLogger().error("Rarity capture failed",error);
            try{Files.createDirectories(out());Files.writeString(out().resolve("error.txt"),error.toString());}catch(Exception ignored){}mc.stop();}
    }
}
