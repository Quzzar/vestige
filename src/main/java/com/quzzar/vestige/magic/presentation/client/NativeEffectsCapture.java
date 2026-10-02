package com.quzzar.vestige.magic.presentation.client;

import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.world.NativeCaptureScene;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

/** Opt-in development artifact exporter: reads Minecraft's own rendered framebuffer, never the desktop. */
@EventBusSubscriber(modid=VestigeMainMod.MOD_ID,value=Dist.CLIENT)
public final class NativeEffectsCapture {
    private static final String OUTPUT=System.getProperty("vestige.capture.output");
    private static final double SECONDS=Double.parseDouble(System.getProperty("vestige.capture.seconds","8"));
    private static final NativeCaptureScene SCENE=new NativeCaptureScene();
    private static final List<Double> TIMES=new ArrayList<>();
    private static final List<CompletableFuture<Void>> WRITES=new ArrayList<>();
    private static final Executor WRITER=new ThreadPoolExecutor(1,1,0,TimeUnit.SECONDS,new ArrayBlockingQueue<>(4),work->{
        Thread thread=new Thread(work,"vestige-native-frame-writer");thread.setDaemon(true);return thread;
    },new ThreadPoolExecutor.CallerRunsPolicy());
    private static List<NativeCaptureScene.Job> jobs;
    private static CompletableFuture<Void> pending;
    private static volatile int state;
    private static int index,frame;
    private static long deadline,started,nextFrame;
    private static Path directory;
    private static double captureSeconds;
    private NativeEffectsCapture() { }
    /** Exercise vanilla food consumption through the actual client input and use-item packet. */
    @SubscribeEvent public static void input(net.neoforged.neoforge.client.event.ClientTickEvent.Pre event) {
        if(OUTPUT==null)return;
        Minecraft mc=Minecraft.getInstance();
        mc.options.keyUse.setDown(state==4 && SCENE.eating());
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post event) {
        if (OUTPUT==null || state==9) return;
        Minecraft mc=Minecraft.getInstance(); long now=System.nanoTime();
        if(state>=2 && state<=5 && mc.getSingleplayerServer()==null) {
            state=9;mc.options.keyUse.setDown(false);
            LogUtils.getLogger().warn("Native capture interrupted: saved {}/{} jobs",index,jobs.size());
            return;
        }
        try {
            if (state==0 && mc.screen instanceof TitleScreen) {
                if (!Double.isFinite(SECONDS) || SECONDS<.5 || SECONDS>120) throw new IllegalArgumentException("Capture seconds must be .5..120");
                state=1;
                // The framebuffer is exported before HUD rendering; leave entity nameplates visible.
                mc.options.pauseOnLostFocus=false; mc.options.hideGui=false;
                mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
                mc.options.renderDistance().set(4); mc.options.fov().set(48); mc.options.enableVsync().set(false);
                mc.getWindow().setFramerateLimit(60);
                mc.createWorldOpenFlows().createFreshLevel("vestige-capture-"+UUID.randomUUID(),
                        new LevelSettings("Vestige native capture",GameType.CREATIVE,false,Difficulty.NORMAL,true,new GameRules(),WorldDataConfiguration.DEFAULT),
                        new WorldOptions(483902,false,false),
                        access->access.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),mc.screen);
            } else if (state==1 && mc.level!=null && mc.player!=null && mc.getSingleplayerServer()!=null && mc.screen==null) {
                jobs=NativeCaptureScene.jobs(System.getProperty("vestige.capture.spells",""),System.getProperty("vestige.capture.kind","cast"));
                pending=server(()->SCENE.build(mc.getSingleplayerServer())); state=2;
                deadline=now+5_000_000_000L;
            } else if (state==2 && now>deadline && ready()) {
                SpellVisualClient.clear();
                mc.particleEngine.setLevel(mc.level);
                var job=jobs.get(index); directory=Path.of(OUTPUT,job.file()); Files.createDirectories(directory);
                captureSeconds=SCENE.minimumSeconds(job,SECONDS);
                if (Files.exists(directory.resolve("capture.json"))) throw new IllegalStateException("Capture already exists: "+directory);
                frame=0; TIMES.clear(); WRITES.clear();
                pending=server(()->SCENE.prepare(mc.getSingleplayerServer(),job)); state=3; deadline=now+800_000_000L;
            } else if (state==3 && now>deadline && ready()) {
                mc.options.setCameraType(SCENE.thirdPerson()?net.minecraft.client.CameraType.THIRD_PERSON_BACK:net.minecraft.client.CameraType.FIRST_PERSON);
                pending=server(()->SCENE.play(mc.getSingleplayerServer())); state=4;
                started=now; nextFrame=now;
            } else if (state==4 && ready()) {
                if (now-started>captureSeconds*1e9 && (!jobs.get(index).kind().equals("cast") || SCENE.elapsedTicks()>=Math.ceil(captureSeconds*20))) {
                    var job=jobs.get(index);
                    var metadata=new LinkedHashMap<String,Object>();
                    metadata.put("spell",job.spell());metadata.put("phase",job.phase());metadata.put("label",job.label());metadata.put("kind",job.kind());
                    metadata.put("engine","Minecraft 1.21.1 / NeoForge 21.1.72 / Vestige");
                    metadata.put("source","Minecraft main render target");metadata.put("width",960);metadata.put("height",540);
                    metadata.put("renderStage",SCENE.showHud()?"after_frame_with_native_gui":"after_level_before_gui");
                    try(var definition=NativeEffectsCapture.class.getResourceAsStream("/data/vestige/runtime_spells/"+job.spell().split(":",2)[1]+".json")) {
                        metadata.put("definitionSha256",HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(Objects.requireNonNull(definition).readAllBytes())));
                    }
                    metadata.put("seconds",(now-started)/1e9);metadata.put("times",List.copyOf(TIMES));
                    // The outcome is sampled on the server thread, alongside runtime state.
                    CompletableFuture<Map<String,Object>> outcome=new CompletableFuture<>();
                    mc.getSingleplayerServer().execute(()->outcome.complete(SCENE.snapshot()));
                    Path metadataFile=directory.resolve("capture.json");
                    pending=outcome.thenCombine(CompletableFuture.allOf(WRITES.toArray(CompletableFuture[]::new)),(result,ignored)->result).thenAccept(result->{
                        metadata.putAll(result);
                        try { Files.writeString(metadataFile,new GsonBuilder().setPrettyPrinting().create().toJson(metadata)+"\n"); }
                        catch(java.io.IOException error) { throw new java.io.UncheckedIOException(error); }
                    });
                    LogUtils.getLogger().info("Native capture {}/{}: {} ({} frames)",index+1,jobs.size(),job.file(),frame);
                    index++;state=5;mc.options.keyUse.setDown(false);
                }
            } else if (state==5 && ready()) {
                if (index>=jobs.size()) { state=9; LogUtils.getLogger().info("Native capture complete: {}",OUTPUT); mc.stop(); }
                else { state=2; deadline=now+200_000_000L; }
            }
            if(state==4 && SCENE.showHud() && ready())captureFrame(now);
        } catch(Exception error) {
            state=9;LogUtils.getLogger().error("Native effects capture failed",error);mc.stop();
        }
    }
    @SubscribeEvent public static void level(RenderLevelStageEvent event) {
        if (OUTPUT==null || state!=4 || SCENE.showHud() || event.getStage()!=RenderLevelStageEvent.Stage.AFTER_LEVEL || !ready()) return;
        captureFrame(System.nanoTime());
    }
    private static void captureFrame(long now) {
        if (now<nextFrame) return;
        Minecraft mc=Minecraft.getInstance();
        try(NativeImage screenshot=Screenshot.takeScreenshot(mc.getMainRenderTarget())) {
            NativeImage scaled=new NativeImage(960,540,false);
            screenshot.resizeSubRectTo(0,0,screenshot.getWidth(),screenshot.getHeight(),scaled);
            Path file=directory.resolve(String.format(Locale.ROOT,"frame-%05d.png",frame++));
            WRITES.add(CompletableFuture.runAsync(()->{
                try(scaled) { scaled.writeToFile(file); }
                catch(java.io.IOException error) { throw new java.io.UncheckedIOException(error); }
            },WRITER));
            TIMES.add((now-started)/1e9); nextFrame=now+66_666_667L;
        } catch(Exception error) {
            state=9;LogUtils.getLogger().error("Native frame export failed",error);mc.stop();
        }
    }
    private static boolean ready() { if (!pending.isDone()) return false; pending.join(); return true; }
    @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.HIGHEST)
    public static void tick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post event) {
        if(OUTPUT!=null && state==4) SCENE.tick(event.getServer());
    }
    private static CompletableFuture<Void> server(Runnable work) {
        var result=new CompletableFuture<Void>();
        Minecraft.getInstance().getSingleplayerServer().execute(()->{try { work.run();result.complete(null); }catch(Throwable error) { result.completeExceptionally(error); }});
        return result;
    }
}
