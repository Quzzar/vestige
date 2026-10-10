package com.quzzar.vestige.magic.presentation.client;

import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.client.ApparatusColors;
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
    private static boolean previewSelected;
    private NativeEffectsCapture() { }
    /** Exercise vanilla food consumption through the actual client input and use-item packet. */
    @SubscribeEvent public static void input(net.neoforged.neoforge.client.event.ClientTickEvent.Pre event) {
        if(OUTPUT==null || List.of("crane_recipe","wardweave","fractious_flint","fluxed_flint_imbuements","fluxed_flint","magic_equipment","wayfarer","wand_recipes","wands","items","apparatus_items","mundane_staffs","mundane_staff_motion","recipes","thread_recipes","equipment_studies","staff_slots","staff_combat","scroll_knowledge","stone_network","standing_models","standing_family","mana","item_mana","preparation","shell_finished").contains(System.getProperty("vestige.capture.kind","cast")))return;
        Minecraft mc=Minecraft.getInstance();
        mc.options.keyUse.setDown(state==4 && SCENE.eating());
        var camera=SCENE.apparatusCamera();
        if(camera!=null && state>=3 && state<=4 && mc.player!=null) {
            // Opt-in still-life capture: ordinary desktop mouse motion must not change the comparison.
            mc.mouseHandler.releaseMouse();
            for(var key:List.of(mc.options.keyUp,mc.options.keyDown,mc.options.keyLeft,mc.options.keyRight,
                    mc.options.keyJump,mc.options.keyShift,mc.options.keySprint))key.setDown(false);
            var eye=camera.eye();double feet=eye.y-mc.player.getEyeHeight();
            mc.player.setPos(eye.x,feet,eye.z);mc.player.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
            mc.player.xo=eye.x;mc.player.yo=feet;mc.player.zo=eye.z;
            mc.player.setYRot(camera.yaw());mc.player.yRotO=camera.yaw();
            mc.player.setXRot(camera.pitch());mc.player.xRotO=camera.pitch();
        }
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post event) {
        if (OUTPUT==null || state==9 || List.of("crane_recipe","wardweave","fractious_flint","fluxed_flint_imbuements","fluxed_flint","magic_equipment","wayfarer","wand_recipes","wands","items","apparatus_items","mundane_staffs","mundane_staff_motion","recipes","thread_recipes","equipment_studies","staff_slots","staff_combat","scroll_knowledge","stone_network","standing_models","standing_family","mana","item_mana","preparation","shell_finished").contains(System.getProperty("vestige.capture.kind","cast"))) return;
        Minecraft mc=Minecraft.getInstance(); long now=System.nanoTime();
        if(state>=2 && state<=5 && mc.getSingleplayerServer()==null) {
            state=9;mc.options.keyUse.setDown(false);
            LogUtils.getLogger().warn("Native capture interrupted: saved {}/{} jobs",index,jobs.size());
            return;
        }
        try {
            if (state==0 && mc.screen instanceof TitleScreen) {
                if(!previewSelected) {
                    previewSelected=true;
                    var repository=mc.getResourcePackRepository();repository.reload();
                    var selected=new ArrayList<>(repository.getSelectedIds());
                    selected.remove("file/vestige-apparatus-simplified");
                    selected.remove("file/vestige-apparatus-middle-ground");
                    selected.remove("file/vestige-spellstone-designs");
                    selected.remove("file/vestige-sealstone-refinements");
                    selected.remove("file/vestige-spellstone-rune-options");
                    selected.remove("file/vestige-astral-bodies");
                    selected.remove("file/vestige-gilded-finishes");
                    selected.remove("file/vestige-diamond-trim");
                    selected.remove("file/vestige-crystal-core");
                    selected.remove("file/vestige-fractured-spellstone");
                    selected.remove("file/vestige-corner-details");
                    selected.remove("file/vestige-plinth-framing");
                    String previewPack=switch(System.getProperty("vestige.capture.apparatus_preview","current")) {
                        case "current" -> null;
                        case "simplified" -> "file/vestige-apparatus-simplified";
                        case "middle" -> "file/vestige-apparatus-middle-ground";
                        case "spellstone-designs" -> "file/vestige-spellstone-designs";
                        case "seal-refinements" -> "file/vestige-sealstone-refinements";
                        case "rune-options" -> "file/vestige-spellstone-rune-options";
                        case "astral-bodies" -> "file/vestige-astral-bodies";
                        case "gilded-finishes" -> "file/vestige-gilded-finishes";
                        case "diamond-trim" -> "file/vestige-diamond-trim";
                        case "crystal-core" -> "file/vestige-crystal-core";
                        case "fractured" -> "file/vestige-fractured-spellstone";
                        case "corner-details" -> "file/vestige-corner-details";
                        case "plinth-framing" -> "file/vestige-plinth-framing";
                        default -> throw new IllegalArgumentException("Unknown apparatus preview");
                    };
                    if(previewPack!=null) {
                        if(repository.getPack(previewPack)==null)throw new IllegalStateException("Generate the apparatus comparison resource pack first");
                        selected.add(previewPack);
                    }
                    repository.setSelected(selected);pending=mc.reloadResourcePacks();return;
                }
                if(!ready())return;
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
                mc.options.fov().set(job.spell().endsWith("failure_spread") ? 80 : 48);
                captureSeconds=SCENE.minimumSeconds(job,SECONDS);
                if (Files.exists(directory.resolve("capture.json"))) throw new IllegalStateException("Capture already exists: "+directory);
                frame=0; TIMES.clear(); WRITES.clear();
                pending=server(()->SCENE.prepare(mc.getSingleplayerServer(),job)); state=3; deadline=now+800_000_000L;
            } else if (state==3 && now>deadline && ready()) {
                mc.options.setCameraType(SCENE.thirdPerson()?net.minecraft.client.CameraType.THIRD_PERSON_BACK:net.minecraft.client.CameraType.FIRST_PERSON);
                pending=server(()->SCENE.play(mc.getSingleplayerServer())); state=4;
                started=now; nextFrame=now;
            } else if (state==4 && ready()) {
                if (now-started>captureSeconds*1e9 && (!(jobs.get(index).kind().equals("cast") || jobs.get(index).kind().equals("ritual")) || SCENE.elapsedTicks()>=Math.ceil(captureSeconds*20))) {
                    var job=jobs.get(index);
                    var metadata=new LinkedHashMap<String,Object>();
                    metadata.put("spell",job.spell());metadata.put("phase",job.phase());metadata.put("label",job.label());metadata.put("kind",job.kind());
                    metadata.put("engine","Minecraft 1.21.1 / NeoForge 21.1.72 / Vestige");
                    metadata.put("source","Minecraft main render target");metadata.put("width",960);metadata.put("height",540);
                    metadata.put("renderStage",SCENE.showHud()?"after_frame_with_native_gui":"after_level_before_gui");
                    if(!System.getProperty("vestige.capture.augments","").isBlank() || job.spell().endsWith("spellshaping")) {
                        try(var rules=NativeEffectsCapture.class.getResourceAsStream("/data/vestige/spellshaping_rules.json")) {
                            metadata.put("spellshapingRulesSha256",HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(Objects.requireNonNull(rules).readAllBytes())));
                        }
                    }
                    if(job.kind().equals("apparatus") || job.kind().equals("ritual")) {
                        var feedbackHashes=new LinkedHashMap<String,String>();
                        for(String name:List.of("apparatus/RitualPresentation","apparatus/ScrollCasting","apparatus/SpellScrollItem",
                                "apparatus/client/OfferingRenderer","apparatus/client/ScrollTooltip","apparatus/client/ApparatusColors")) {
                            String path="/com/quzzar/vestige/"+name+".class";
                            try(var code=NativeEffectsCapture.class.getResourceAsStream(path)) {
                                feedbackHashes.put(path.substring(1),HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256")
                                        .digest(Objects.requireNonNull(code).readAllBytes())));
                            }
                        }
                        metadata.put("feedbackCodeSha256",feedbackHashes);
                        metadata.put("apparatusTrim",Map.of("tintIndex",ApparatusColors.TINT_INDEX,
                                "tintColor",String.format("#%06x",ApparatusColors.TINT_COLOR & 0xffffff),
                                "brightness",(ApparatusColors.TINT_COLOR & 0xff)/255.0));
                        var hashes=new LinkedHashMap<String,String>();
                        var material=com.quzzar.vestige.apparatus.ApparatusMaterials.valueOf(System.getProperty("vestige.capture.material","stone_bricks").toUpperCase(Locale.ROOT));
                        metadata.put("apparatusMaterial",material.id());
                        metadata.put("apparatusPresentation",System.getProperty("vestige.capture.apparatus_preview","current"));
                        var camera=SCENE.apparatusCamera();
                        if(camera!=null)metadata.put("apparatusCamera",Map.of("eye",List.of(camera.eye().x,camera.eye().y,camera.eye().z),
                                "yaw",camera.yaw(),"pitch",camera.pitch(),"fixed",true));
                        var modelPaths=new java.util.ArrayList<>(List.of("models/block/spellstone.json","models/block/plinth.json"));
                        for(var role:com.quzzar.vestige.apparatus.ApparatusBlock.Role.values()) {
                            String path="models/block/"+material.blockName(role)+".json";
                            if(!modelPaths.contains(path))modelPaths.add(path);
                        }
                        for(String part:List.of("base","shaft","cap"))modelPaths.add("models/block/"+material.blockName(com.quzzar.vestige.apparatus.ApparatusBlock.Role.PLINTH)+"_"+part+".json");
                        if(job.spell().startsWith("vestige:apparatus_trim_")) {
                            for(var finish:com.quzzar.vestige.apparatus.ApparatusMaterials.values()) {
                                for(var role:com.quzzar.vestige.apparatus.ApparatusBlock.Role.values()) {
                                    String name=finish.blockName(role);
                                    for(String path:List.of("models/block/"+name+".json","models/item/"+name+".json"))
                                        if(!modelPaths.contains(path))modelPaths.add(path);
                                }
                                for(String part:List.of("base","shaft","cap")) {
                                    String path="models/block/"+finish.blockName(com.quzzar.vestige.apparatus.ApparatusBlock.Role.PLINTH)+"_"+part+".json";
                                    if(!modelPaths.contains(path))modelPaths.add(path);
                                }
                            }
                        }
                        if(job.spell().endsWith("finishes") || List.of("spellstone-designs","seal-refinements","rune-options","astral-bodies","gilded-finishes","diamond-trim").contains(System.getProperty("vestige.capture.apparatus_preview","current"))) {
                            modelPaths.add("models/block/tuff_spellstone.json");
                            modelPaths.add("models/block/quartz_spellstone.json");
                        }
                        if(List.of("gilded-finishes","diamond-trim").contains(System.getProperty("vestige.capture.apparatus_preview","current"))) {
                            modelPaths.add("models/block/sandstone_spellstone.json");
                            var textureHashes=new LinkedHashMap<String,String>();
                            var tiles=System.getProperty("vestige.capture.apparatus_preview","current").equals("diamond-trim")?
                                    List.of("smooth_stone","polished_blackstone","quartz_block_bottom","sandstone_top","polished_deepslate","cut_sandstone","diamond_block"):
                                    List.of("smooth_stone","polished_blackstone","quartz_block_bottom","sandstone_top","gold_block");
                            for(String tile:tiles) {
                                String path="textures/block/"+tile+".png";
                                try(var asset=mc.getResourceManager().getResourceOrThrow(net.minecraft.resources.ResourceLocation.withDefaultNamespace(path)).open()) {
                                    textureHashes.put(path,HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(asset.readAllBytes())));
                                }
                            }
                            metadata.put("materialTextureSha256",textureHashes);
                        }
                        if(System.getProperty("vestige.capture.apparatus_preview","current").equals("plinth-framing")) {
                            for(String carrier:List.of("stone","andesite","diorite","granite","cobblestone"))
                                for(String suffix:List.of("","_base","_shaft","_cap"))modelPaths.add("models/block/"+carrier+"_plinth"+suffix+".json");
                            for(String carrier:List.of("stone","andesite"))modelPaths.add("models/block/"+carrier+"_spellstone.json");
                            var textureHashes=new LinkedHashMap<String,String>();
                            for(String tile:List.of("stone","polished_deepslate","diamond_block")) {
                                String path="textures/block/"+tile+".png";
                                try(var asset=mc.getResourceManager().getResourceOrThrow(net.minecraft.resources.ResourceLocation.withDefaultNamespace(path)).open()) {
                                    textureHashes.put(path,HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(asset.readAllBytes())));
                                }
                            }
                            metadata.put("materialTextureSha256",textureHashes);
                            metadata.put("framingTrim",Map.of("texture","minecraft:block/stone",
                                    "tintIndex",ApparatusColors.TINT_INDEX,
                                    "tintColor",String.format("#%06x",ApparatusColors.TINT_COLOR & 0xffffff),
                                    "brightness",(ApparatusColors.TINT_COLOR & 0xff)/255.0));
                            try(var tint=ApparatusColors.class.getResourceAsStream("ApparatusColors.class")) {
                                metadata.put("framingTintCodeSha256",HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(Objects.requireNonNull(tint).readAllBytes())));
                            }
                        }
                        if(System.getProperty("vestige.capture.apparatus_preview","current").equals("corner-details")) {
                            for(String carrier:List.of("tuff","quartz","sandstone","polished_blackstone","smooth_quartz","smooth_sandstone"))
                                modelPaths.add("models/block/"+carrier+"_spellstone.json");
                            var textureHashes=new LinkedHashMap<String,String>();
                            for(String tile:List.of("smooth_stone","polished_blackstone","quartz_block_bottom","sandstone_top","diamond_block")) {
                                String path="textures/block/"+tile+".png";
                                try(var asset=mc.getResourceManager().getResourceOrThrow(net.minecraft.resources.ResourceLocation.withDefaultNamespace(path)).open()) {
                                    textureHashes.put(path,HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(asset.readAllBytes())));
                                }
                            }
                            metadata.put("materialTextureSha256",textureHashes);
                        }
                        if(System.getProperty("vestige.capture.apparatus_preview","current").equals("crystal-core")) {
                            var textureHashes=new LinkedHashMap<String,String>();
                            for(String tile:List.of("smooth_stone","amethyst_block","diamond_block")) {
                                String path="textures/block/"+tile+".png";
                                try(var asset=mc.getResourceManager().getResourceOrThrow(net.minecraft.resources.ResourceLocation.withDefaultNamespace(path)).open()) {
                                    textureHashes.put(path,HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(asset.readAllBytes())));
                                }
                            }
                            metadata.put("materialTextureSha256",textureHashes);
                        }
                        if(System.getProperty("vestige.capture.apparatus_preview","current").equals("fractured")) {
                            String path="textures/block/stone.png";
                            try(var asset=mc.getResourceManager().getResourceOrThrow(net.minecraft.resources.ResourceLocation.withDefaultNamespace(path)).open()) {
                                metadata.put("materialTextureSha256",Map.of(path,HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(asset.readAllBytes()))));
                            }
                        }
                        for(String path:modelPaths) {
                            try(var asset=mc.getResourceManager().getResourceOrThrow(VestigeMainMod.location(path)).open()) {
                                hashes.put(path,HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(Objects.requireNonNull(asset).readAllBytes())));
                            }
                        }
                        metadata.put("assetSha256",hashes);
                        try(var gameplay=NativeEffectsCapture.class.getResourceAsStream("/com/quzzar/vestige/apparatus/RitualCrafting.class")) {
                            metadata.put("ritualCraftingSha256",HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(Objects.requireNonNull(gameplay).readAllBytes())));
                        }
                        try(var renderer=NativeEffectsCapture.class.getResourceAsStream("/com/quzzar/vestige/apparatus/client/AstralSealRenderer.class")) {
                            metadata.put("astralRendererSha256",HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(Objects.requireNonNull(renderer).readAllBytes())));
                        }
                        if(System.getProperty("vestige.capture.apparatus_preview","current").equals("rune-options")) {
                            try(var renderer=NativeEffectsCapture.class.getResourceAsStream("/com/quzzar/vestige/apparatus/client/SpellstoneRuneOptions.class")) {
                                metadata.put("runeRendererSha256",HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(Objects.requireNonNull(renderer).readAllBytes())));
                            }
                            metadata.put("runeLighting",job.spell().endsWith("_night")?"midnight":"noon");
                            metadata.put("glow","Native additive unlit ribbons and motes; no shader pack, bloom postprocessing or dynamic world light");
                        }
                    } else {
                        try(var definition=NativeEffectsCapture.class.getResourceAsStream("/data/vestige/runtime_spells/"+job.spell().split(":",2)[1]+".json")) {
                            metadata.put("definitionSha256",HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(Objects.requireNonNull(definition).readAllBytes())));
                        }
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
                if (index>=jobs.size()) {
                    LogUtils.getLogger().info("Native capture complete: {}",OUTPUT);
                    if(Boolean.getBoolean("vestige.capture.review")) { pending=server(()->SCENE.review(mc.getSingleplayerServer()));state=6; }
                    else { state=9;mc.stop(); }
                }
                else { state=2; deadline=now+200_000_000L; }
            } else if(state==6 && ready()) {
                mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);
                mc.options.fov().set(70);mc.options.hideGui=false;
                state=9;LogUtils.getLogger().info("Native ritual review ready: four creative-mode stations in the fresh capture world");
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
