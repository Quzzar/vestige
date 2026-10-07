package com.quzzar.vestige.apparatus.recipeviewer;

import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.platform.NativeImage;
import com.quzzar.vestige.VestigeMainMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.Difficulty;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import org.joml.Vector3f;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/** Opt-in actual-viewer captures, source-block lookup, hollow hover and resource-pack reload checks. */
@EventBusSubscriber(modid=VestigeMainMod.MOD_ID,value=Dist.CLIENT)
public final class NativeThreadRecipeCapture {
    private static final boolean ENABLED="thread_recipes".equals(System.getProperty("vestige.capture.kind"));
    private static int state,index="hover_only".equals(System.getProperty("vestige.capture.spells"))?8:0;
    private static long deadline;
    private static Vector3f rim,center,origin;
    private static CompletableFuture<Void> reload;
    private static final List<Map<String,Object>> captures=new ArrayList<>();
    private static final Map<String,Long> indexing=new LinkedHashMap<>();
    private static final List<String> NAMES=List.of("diamond","iron","gold","emerald","glowstone","small-diamond","small-gold","small-glowstone","frame-hover","string-hover","resource-pack-gold");
    private NativeThreadRecipeCapture() { }
    static void diagram(GuiGraphics graphics,RitualDisplays.Entry recipe) {
        if(!ENABLED || recipe.imbuements().isEmpty())return;
        int seat=recipe.imbuements().getFirst().seat();
        float x=RitualDiagram.x(recipe,seat)+8,y=RitualDiagram.y(recipe,seat)+8;
        center=graphics.pose().last().pose().transformPosition(x,y,0,new Vector3f());
        origin=graphics.pose().last().pose().transformPosition(0,0,0,new Vector3f());
        // The left rim remains outside both viewers' offering hover rectangle.
        rim=graphics.pose().last().pose().transformPosition(x-9.5f,y,0,new Vector3f());
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post event)throws Exception {
        if(!ENABLED || state==4)return;
        var mc=Minecraft.getInstance();long now=System.nanoTime();
        boolean emi=ModList.get().isLoaded("emi"),jei=ModList.get().isLoaded("jei"),viewer=emi || jei;
        if(state==0 && mc.screen instanceof TitleScreen && mc.getOverlay()==null) {
            mc.options.pauseOnLostFocus=false;mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
            mc.getWindow().setWindowed(1280,720);mc.options.guiScale().set(4);mc.resizeDisplay();state=1;deadline=now+180_000_000_000L;
            mc.createWorldOpenFlows().createFreshLevel("vestige-thread-viewer-"+UUID.randomUUID(),
                    new LevelSettings("Thread viewer inspection",GameType.CREATIVE,false,Difficulty.PEACEFUL,true,new GameRules(),WorldDataConfiguration.DEFAULT),
                    new WorldOptions(483902,false,false),access -> access.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),mc.screen);
        }else if(state==1 && mc.player!=null && mc.level!=null && (!viewer || count()==RitualViewerClient.displays().size() && count()>200)) {
            if(viewer)for(var entry:RitualDisplays.threads()) {
                var block=entry.imbuements().getFirst().stack();
                long output=emi?EmiInspection.matches(entry.output(),false):JeiInspection.matches(entry.output(),false);
                long catalyst=emi?EmiInspection.catalysts(block):JeiInspection.catalysts(block);
                if(output!=1 || catalyst!=1 || !emi && JeiInspection.matches(block,true)!=0)throw new IllegalStateException("Invalid thread lookup: "+entry.id());
                indexing.put(entry.id().getPath()+"Output",output);indexing.put(entry.id().getPath()+"Catalyst",catalyst);
            }
            if(viewer && RitualViewerClient.displays().stream().filter(RitualDisplays.Entry::concealed).anyMatch(e -> !e.offerings().isEmpty() || !e.imbuements().isEmpty()))throw new IllegalStateException("Hidden recipe leaked");
            mc.setScreen(new InventoryScreen(mc.player));if(viewer)show();state=2;deadline=now+3_000_000_000L;
        }else if(state==2 && now>deadline) {
            var out=Path.of(System.getProperty("vestige.capture.output"),emi?(jei?"both":"emi"):jei?"jei":"none");Files.createDirectories(out);
            if(viewer && (index==8 || index==9)) {
                var point=index==8?rim:center;
                mc.getMainRenderTarget().bindWrite(true);
                var projection=new org.joml.Matrix4f(com.mojang.blaze3d.systems.RenderSystem.getProjectionMatrix());
                com.mojang.blaze3d.systems.RenderSystem.setProjectionMatrix(new org.joml.Matrix4f().setOrtho(0,mc.getWindow().getGuiScaledWidth(),mc.getWindow().getGuiScaledHeight(),0,1000,21000),com.mojang.blaze3d.vertex.VertexSorting.ORTHOGRAPHIC_Z);
                var modelView=com.mojang.blaze3d.systems.RenderSystem.getModelViewStack();modelView.pushMatrix();modelView.translation(0,0,-11000);com.mojang.blaze3d.systems.RenderSystem.applyModelViewMatrix();
                var graphics=new GuiGraphics(mc,mc.renderBuffers().bufferSource());
                mc.screen.renderWithTooltip(graphics,(int)point.x,(int)point.y,0);
                if(!emi)JeiInspection.drawHover(graphics,RitualDisplays.threads().getFirst(),Math.round(origin.x),Math.round(origin.y),(int)point.x,(int)point.y);
                graphics.flush();
                modelView.popMatrix();com.mojang.blaze3d.systems.RenderSystem.applyModelViewMatrix();
                com.mojang.blaze3d.systems.RenderSystem.setProjectionMatrix(projection,com.mojang.blaze3d.vertex.VertexSorting.ORTHOGRAPHIC_Z);
                var actual=emi?EmiInspection.hovered():JeiInspection.hovered(
                        RitualDisplays.threads().getFirst(),(int)Math.floor(point.x-origin.x),(int)Math.floor(point.y-origin.y));
                var expected=index==8?RitualDisplays.threads().getFirst().imbuements().getFirst().stack().getItem():Items.STRING;
                if(!actual.is(expected)) {
                    try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(out.resolve("hover-diagnostic.png"));}
                    Files.writeString(out.resolve("hover-diagnostic.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("rim",rim.toString(),"center",center.toString(),"actual",actual.toString(),"expected",expected.toString())));
                    state=4;throw new IllegalStateException("Wrong hollow frame hover: "+actual+", expected "+expected);
                }
                indexing.put(index==8?"frameHover":"stringHover",1L);
            }
            try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(out.resolve((viewer?NAMES.get(index):"no-viewer")+".png"));}
            captures.add(Map.of("case",viewer?NAMES.get(index):"no-viewer","guiScale",mc.options.guiScale().get(),"screen",mc.screen.getClass().getName(),"registeredRituals",viewer?count():RitualViewerClient.displays().size()));
            if(!viewer || ++index==NAMES.size()) {
                Files.writeString(out.resolve("capture.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("source","Actual Minecraft framebuffer and public viewer APIs","captures",captures,"indexing",indexing))+"\n");state=4;mc.stop();
            }else if(index==10){reloadPack();state=3;deadline=now+180_000_000_000L;}
            else {
                if(index==5){mc.getWindow().setWindowed(960,720);mc.options.guiScale().set(6);mc.resizeDisplay();}
                if(index==8){mc.options.guiScale().set(4);mc.resizeDisplay();show();}
                else if(index<8)show();
                deadline=now+2_000_000_000L;
            }
        }else if(state==3 && reload.isDone() && mc.getOverlay()==null && count()==RitualViewerClient.displays().size()) {
            reload.join();show();state=2;deadline=now+3_000_000_000L;
        }
        if((state==1 || state==3) && now>deadline)throw new IllegalStateException("Thread viewer capture timed out");
    }
    private static long count(){return ModList.get().isLoaded("emi")?EmiInspection.count():JeiInspection.count();}
    private static void show() {
        int selected=index<5?index:index==6 || index==10?2:index==7?4:0;
        var stack=RitualDisplays.threads().get(selected).output();
        if(ModList.get().isLoaded("emi"))EmiInspection.show(stack);else JeiInspection.show(stack);
    }
    private static void reloadPack()throws Exception {
        var mc=Minecraft.getInstance();var pack=mc.gameDirectory.toPath().resolve("resourcepacks/thread-frame-inspection");
        var texture=pack.resolve("assets/minecraft/textures/block/gold_block.png");Files.createDirectories(texture.getParent());
        int format=net.minecraft.SharedConstants.getCurrentVersion().getPackVersion(net.minecraft.server.packs.PackType.CLIENT_RESOURCES);
        Files.writeString(pack.resolve("pack.mcmeta"),"{\"pack\":{\"pack_format\":"+format+",\"description\":\"Isolated thread frame sprite check\"}}");
        try(var image=new NativeImage(16,16,true)){for(int y=0;y<16;y++)for(int x=0;x<16;x++)image.setPixelRGBA(x,y,0xffff8040);image.writeToFile(texture);}
        var repository=mc.getResourcePackRepository();repository.reload();var selected=new ArrayList<>(repository.getSelectedIds());selected.add("file/thread-frame-inspection");repository.setSelected(selected);reload=mc.reloadResourcePacks();
    }
}
