package com.quzzar.vestige.travel.client;

import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.AttunementMark;
import com.quzzar.vestige.travel.*;
import com.quzzar.vestige.magic.presentation.client.ManaDisplay;
import com.quzzar.vestige.magic.world.NativeMana;
import com.quzzar.vestige.magic.world.client.ItemManaOverlay;
import net.minecraft.client.*;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.phys.Vec3;
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

/** Opt-in native menu inspection using actual endpoints, server payloads and rename requests. */
@EventBusSubscriber(modid = VestigeMainMod.MOD_ID, value = Dist.CLIENT)
public final class NativeStoneCapture {
    private static final boolean ENABLED = "stone_network".equals(System.getProperty("vestige.capture.kind"));
    private static final String KEY = "76f32f1c38d3f867b7314795817de918434f62f7742e565283a18dff843ed500";
    private static final BlockPos SOURCE = new BlockPos(0, 65, 0), SINGLE = new BlockPos(-4, 65, 0);
    private static final List<String> NAMES = List.of("Ancient Henge", "Ashen Gate", "A Very Long Standing Stone Destination Name",
            "Desert Pyramid", "Forest Shrine", "Harbor", "High Pass", "Island Retreat", "Library", "Marsh Ruins",
            "Mountain Observatory", "Old Ruins", "Quarry", "River Crossing", "Southwatch", "Sunken Temple",
            "Valley Camp", "Westgate", "Windmill", "Winter Hold");
    private static final List<Map<String, Object>> CHECKS = new ArrayList<>();
    private static StoneNetworkScreen previous;
    private static UUID sourceId;
    private static int state;
    private static long next, started;
    private static CompletableFuture<Void> pending;
    private static StoneTravelPayloads.View shortView;
    private static int costPreview;
    private static int hudStage;
    private static final int[] HUD_AMOUNTS = {50, 0, 50, 100, 50, 50};
    private static final String[] HUD_NAMES = {"mana-half", "mana-empty", "mana-armored", "mana-full-hidden", "mana-underwater", "mana-compact"};
    private NativeStoneCapture() { }
    private static StandingStoneEntity place(MinecraftServer server, BlockPos pos, String key, String name) {
        var level = server.overworld(); var lower = StandingStones.STONE.get().defaultBlockState();
        level.setBlock(pos.below(), Blocks.STONE.defaultBlockState(), 3);
        level.setBlock(pos, lower, 3); level.setBlock(pos.above(), lower.setValue(StandingStoneBlock.HALF, DoubleBlockHalf.UPPER), 3);
        var stone = (StandingStoneEntity) level.getBlockEntity(pos); stone.configure(key, name); return stone;
    }
    private static void prepare(MinecraftServer server) {
        var level = server.overworld();
        level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, server);
        level.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, server);
        level.getGameRules().getRule(GameRules.RULE_ANNOUNCE_ADVANCEMENTS).set(false, server);
        level.setWeatherParameters(6000, 0, false, false); level.setDayTime(5500);
        for (int x = -7; x <= 20; x++) for (int z = -7; z <= 15; z++) {
            level.setBlock(new BlockPos(x, 64, z), Blocks.GRASS_BLOCK.defaultBlockState(), 3);
            for (int y = 65; y <= 68; y++) level.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), 3);
        }
        var source = place(server, SOURCE, KEY, "Wizard Tower"); sourceId = source.id();
        // A vertically distant quote stays inside the already loaded fixture chunks.
        for (int i = 0; i < NAMES.size(); i++) place(server, (NAMES.get(i).equals("Desert Pyramid") ? new BlockPos(8,129,4) : new BlockPos(5 + (i % 5) * 3, 65, (i / 5) * 3)), KEY, NAMES.get(i));
        place(server, SINGLE, "d9943647cbf7789d06ff728ebe1ecd8973b9fef68ddb481a7f68a213c91dcfc4", "Lone Standing Stone");
        var player = server.getPlayerList().getPlayers().getFirst();
        player.setNoGravity(true); player.teleportTo(.5, 65, -3.5); player.setYRot(0); player.setXRot(0);
        player.setExperienceLevels(0); player.setExperiencePoints(0); player.giveExperiencePoints(14);
        StoneTravel.open(player, source, 0);
    }
    private static EditBox editor(StoneNetworkScreen screen) {
        return screen.children().stream().filter(EditBox.class::isInstance).map(EditBox.class::cast).findFirst().orElseThrow();
    }
    private static Button button(StoneNetworkScreen screen, String label) {
        return screen.children().stream().filter(Button.class::isInstance).map(Button.class::cast)
                .filter(value -> value.getMessage().getString().equals(label) || value.getMessage().getString().startsWith(label + " · ")).findFirst().orElseThrow();
    }
    private static void click(StoneNetworkScreen screen, Button button) {
        if (!button.active) throw new IllegalStateException("Disabled button: " + button.getMessage().getString());
        screen.mouseClicked(button.getX() + button.getWidth() / 2.0, button.getY() + button.getHeight() / 2.0, 0);
        screen.mouseReleased(button.getX() + button.getWidth() / 2.0, button.getY() + button.getHeight() / 2.0, 0);
    }
    private static void movePointerAway(Minecraft mc) {
        double edge = (System.nanoTime() / 20_000_000L) % 2;
        long window = mc.getWindow().getWindow();
        GLFW.glfwSetCursorPos(window, edge, edge);
        // Deliver through Minecraft's registered callback even when the review window is unfocused.
        var callback = GLFW.glfwSetCursorPosCallback(window, null);
        if (callback != null) { GLFW.glfwSetCursorPosCallback(window, callback); callback.invoke(window, edge, edge); }
        if (mc.screen instanceof StoneNetworkScreen screen && !editor(screen).visible) screen.setFocused(null);
    }
    private static void capture(Minecraft mc, StoneNetworkScreen screen, String name) throws Exception {
        Path out = Path.of(System.getProperty("vestige.capture.output")); Files.createDirectories(out);
        if (mc.screen instanceof StoneNetworkScreen) {
            var rows = screen.children().stream().filter(Button.class::isInstance).map(Button.class::cast)
                    .filter(b -> b.getMessage().getString().contains(" · ")).toList();
            require(rows.stream().allMatch(b -> b.getHeight() == 20), name + " retains fixed 20-pixel destination buttons");
            for (int i = 1; i < rows.size(); i++) require(rows.get(i).getY() - rows.get(i-1).getY() == 22,
                    name + " row " + i + " retains two-pixel spacing");
        }
        try (var image = Screenshot.takeScreenshot(mc.getMainRenderTarget())) {
            image.writeToFile(out.resolve(name + ".png"));
            CHECKS.add(Map.of("capture", name, "pixels", List.of(image.getWidth(), image.getHeight()),
                    "gui", List.of(mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight()), "sourceName", screen.sourceName(), "page", screen.view().page()));
        }
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
        CHECKS.add(Map.of("check", message, "passed", true));
    }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post event) {
        if (!ENABLED || state == 30) return;
        var mc = Minecraft.getInstance(); long now = System.nanoTime();
        if (started == 0) started = now;
        try {
            if (now - started > 240_000_000_000L) throw new IllegalStateException("Native menu inspection timed out at step " + state);
            // Keep opt-in review captures clear of fixture-generated recipe/advancement notices.
            mc.getToasts().clear(); mc.gui.getChat().clearMessages(false);
            if (mc.screen instanceof StoneNetworkScreen) movePointerAway(mc);
            if (state == 0 && mc.screen instanceof TitleScreen && mc.getOverlay() == null) {
                mc.options.guiScale().set(3); mc.resizeDisplay(); mc.options.pauseOnLostFocus = false;
                mc.options.renderDistance().set(4); mc.options.simulationDistance().set(5);
                mc.options.enableVsync().set(false); mc.getWindow().setFramerateLimit(60);
                mc.getTutorial().setStep(TutorialSteps.NONE);
                state = 1;
                mc.createWorldOpenFlows().createFreshLevel("vestige-standing-menu-" + UUID.randomUUID(),
                        new LevelSettings("Standing Stone menu review", GameType.CREATIVE, false, Difficulty.PEACEFUL, true, new GameRules(), WorldDataConfiguration.DEFAULT),
                        new WorldOptions(483902, false, false),
                        access -> access.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(), mc.screen);
            } else if (state == 1 && mc.level != null && mc.player != null && mc.getSingleplayerServer() != null && mc.screen == null) {
                state = 2; pending = mc.getSingleplayerServer().submit(() -> prepare(mc.getSingleplayerServer())); next = now + 2_000_000_000L;
            } else if (state == 2 && pending.isDone() && mc.screen instanceof StoneNetworkScreen screen && now >= next && PlayerExperience.available(mc.player)==14) {
                pending.join(); capture(mc, screen, "list-wide");
                require(screen.panelWidth()==210,"Menu is narrowed to 210 GUI units");
                String clipped=StoneNetworkScreen.ellipsize(mc.font,NAMES.get(2),140);
                require(clipped.endsWith("…") && mc.font.width(clipped)<=140,"Long names have a width-bounded ellipsis");
                require(!editor(screen).visible && !button(screen,"Save").visible, "Name editor and Save are hidden until pencil click");
                require(!button(screen,"Desert Pyramid").active && button(screen,"Ancient Henge").active, "Actual XP balance disables an expensive destination and enables an affordable one");
                click(screen,button(screen,"✎"));
                require(editor(screen).visible && !button(screen,"Save").active,"Pencil reveals editor with unchanged Save disabled");
                var field=editor(screen);field.setValue("");require(!button(screen,"Save").active,"Blank name disables Save");
                field.setValue("Northwatch Sanctum");state=11;next=now+800_000_000L;
            } else if (state == 11 && mc.screen instanceof StoneNetworkScreen screen && now >= next) {
                capture(mc,screen,"editing-wide");
                previous=screen;click(screen,button(screen,"Save"));state=3;next=now+800_000_000L;
            } else if (state == 3 && mc.screen instanceof StoneNetworkScreen screen && screen != previous
                    && screen.sourceName().equals("Northwatch Sanctum") && now >= next) {
                require(!editor(screen).visible && button(screen,"✎").visible,"Saved name returns to compact name-plus-pencil header");
                capture(mc,screen,"renamed-wide");mc.options.guiScale().set(4);mc.resizeDisplay();state=4;next=now+800_000_000L;
            } else if (state == 4 && mc.screen instanceof StoneNetworkScreen screen && now >= next) {
                capture(mc,screen,"list-narrow");previous=screen;click(screen,button(screen,">"));state=5;next=now+800_000_000L;
            } else if (state == 5 && mc.screen instanceof StoneNetworkScreen screen && screen != previous && now >= next) {
                require(screen.sourceName().equals("Northwatch Sanctum"),"Second page retains current name independently of destination rows");
                capture(mc,screen,"second-page");previous=screen;click(screen,button(screen,">"));state=6;next=now+800_000_000L;
            } else if (state == 6 && mc.screen instanceof StoneNetworkScreen screen && screen != previous && now >= next) {
                require(screen.view().page()==2 && button(screen,">").active,"Third fixed-height page still exposes the final page");
                capture(mc,screen,"third-page");previous=screen;click(screen,button(screen,">"));state=12;next=now+800_000_000L;
            } else if (state == 12 && mc.screen instanceof StoneNetworkScreen screen && screen != previous && now >= next) {
                require(screen.view().page()==3 && !button(screen,">").active && button(screen,"<").active && screen.view().total()==20,"All twenty other peers are available across four six-row pages");
                capture(mc,screen,"last-page");previous=screen;state=7;next=now+800_000_000L;
                pending=mc.getSingleplayerServer().submit(()->{
                    var server=mc.getSingleplayerServer();var player=server.getPlayerList().getPlayers().getFirst();
                    player.setExperienceLevels(0);player.setExperiencePoints(0);
                    StoneTravel.open(player,(StandingStoneEntity)server.overworld().getBlockEntity(SOURCE),0);
                });
            } else if (state == 7 && pending.isDone() && mc.screen instanceof StoneNetworkScreen screen && screen != previous && now >= next && PlayerExperience.available(mc.player)==0) {
                pending.join();require(screen.children().stream().filter(Button.class::isInstance).map(Button.class::cast)
                        .filter(b->b.getMessage().getString().contains(" XP points")).noneMatch(b->b.active),"Zero XP disables every destination");
                capture(mc,screen,"unaffordable");previous=screen;state=8;next=now+800_000_000L;
                pending=mc.getSingleplayerServer().submit(()->{
                    var server=mc.getSingleplayerServer();var player=server.getPlayerList().getPlayers().getFirst();player.giveExperiencePoints(14);
                    StoneTravel.open(player,(StandingStoneEntity)server.overworld().getBlockEntity(SOURCE),0);
                });
            } else if (state == 8 && pending.isDone() && mc.screen instanceof StoneNetworkScreen screen && screen != previous && now >= next && PlayerExperience.available(mc.player)==14) {
                pending.join();previous=screen;click(screen,button(screen,"Ancient Henge"));
                require(mc.screen==null,"One destination click sends travel directly and closes the menu");state=9;next=now+800_000_000L;
            } else if (state == 9 && mc.screen==null && now >= next && PlayerExperience.available(mc.player)==1) {
                require(mc.player.position().distanceToSqr(Vec3.atBottomCenterOf(new BlockPos(5,65,0)))<10,"Real server travel arrives at the clicked endpoint and charges thirteen XP points");
                capture(mc,previous,"after-travel");state=10;next=now+800_000_000L;
                pending=mc.getSingleplayerServer().submit(()->{
                    var server=mc.getSingleplayerServer();var player=server.getPlayerList().getPlayers().getFirst();player.teleportTo(-3.5,65,-2.5);
                    StoneTravel.open(player,(StandingStoneEntity)server.overworld().getBlockEntity(SINGLE),0);
                });
            } else if (state == 10 && pending.isDone() && mc.screen instanceof StoneNetworkScreen screen && screen != previous && now >= next) {
                pending.join();require(screen.sourceName().equals("Lone Standing Stone") && screen.view().destinations().isEmpty(),"A source-only network has no duplicate self destination");
                require(screen.children().stream().filter(Button.class::isInstance).map(Button.class::cast)
                        .noneMatch(b->Set.of("<",">").contains(b.getMessage().getString())),"Source-only menu hides all pagination controls");
                capture(mc,screen,"only-current-stone");previous=screen;state=13;next=now+800_000_000L;
                pending=mc.getSingleplayerServer().submit(()->{
                    var server=mc.getSingleplayerServer();var player=server.getPlayerList().getPlayers().getFirst();
                    var source=(StandingStoneEntity)server.overworld().getBlockEntity(SINGLE);
                    source.rename("The Sanctuary Beyond the Ancient Northern Mountains");
                    place(server,new BlockPos(-4,65,4),source.key(),"Ancient Henge");
                    place(server,new BlockPos(-4,65,8),source.key(),"Ashen Gate");
                    place(server,new BlockPos(-4,65,12),source.key(),NAMES.get(2));
                    player.giveExperiencePoints(14);StoneTravel.open(player,source,0);
                });
            } else if (state == 13 && pending.isDone() && mc.screen instanceof StoneNetworkScreen screen && screen != previous && now >= next) {
                pending.join();require(screen.view().destinations().size()==3 && screen.view().total()==3,"Short network displays exactly three real destinations");
                require(screen.children().stream().filter(Button.class::isInstance).map(Button.class::cast)
                        .noneMatch(b->Set.of("<",">").contains(b.getMessage().getString())),"Single-page destination list hides arrows and page count");
                capture(mc,screen,"short-list");shortView=screen.view();costPreview=0;
                mc.setScreen(StoneNetworkScreen.costPreview(shortView,TravelCostDisplay.Type.values()[costPreview]));state=14;next=now+800_000_000L;
            } else if (state == 14 && mc.screen instanceof StoneNetworkScreen screen && now >= next) {
                var type=TravelCostDisplay.Type.values()[costPreview];
                var rows=screen.children().stream().filter(Button.class::isInstance).map(Button.class::cast)
                        .filter(b->b.getMessage().getString().contains(" · ")).toList();
                require(rows.size()==3 && rows.getFirst().active && !rows.getLast().active,type+" preview shows enabled and unaffordable costs");
                if (type==TravelCostDisplay.Type.HEALTH) require(rows.getFirst().getMessage().getString().endsWith("2 health points"),"Heart costs display native health points, not whole hearts");
                if (type==TravelCostDisplay.Type.HUNGER) require(rows.getFirst().getMessage().getString().endsWith("2 hunger points"),"Food costs display native hunger points, not whole food icons");
                if (type==TravelCostDisplay.Type.HEALTH || type==TravelCostDisplay.Type.HUNGER) {
                    var one = new TravelCostDisplay(type,2,true); var mixed = new TravelCostDisplay(type,5,true);
                    require(one.fullIcons()==1 && !one.hasHalfIcon() && one.width(mc.font)==9,type+" draws one full icon for two native points");
                    require(mixed.fullIcons()==2 && mixed.hasHalfIcon() && mixed.width(mc.font)==29,type+" draws two full icons and a half for five points");
                    var half = new TravelCostDisplay(type,1,true);
                    require(half.fullIcons()==0 && half.hasHalfIcon(),type+" preserves a half-only cost");
                }
                long before=PlayerExperience.available(mc.player);click(screen,rows.getFirst());
                require(mc.screen==screen && PlayerExperience.available(mc.player)==before,type+" appearance preview sends no travel/payment request");
                capture(mc,screen,"cost-"+type.name().toLowerCase(Locale.ROOT));
                if (++costPreview<TravelCostDisplay.Type.values().length) {
                    mc.setScreen(StoneNetworkScreen.costPreview(shortView,TravelCostDisplay.Type.values()[costPreview]));next=now+800_000_000L;
                } else {
                    previous=screen;mc.setScreen(null);mc.options.guiScale().set(3);mc.resizeDisplay();hudStage=0;state=15;
                    pending=mc.getSingleplayerServer().submit(()->prepareHud(mc.getSingleplayerServer(),hudStage));next=now+800_000_000L;
                }
            } else if (state == 15 && pending.isDone() && mc.screen==null && now >= next
                    && Math.round(ItemManaOverlay.amount())==HUD_AMOUNTS[hudStage]
                    && (hudStage!=5 || mc.getWindow().getGuiScaledWidth()==320)) {
                pending.join();require(Math.round(ItemManaOverlay.amount())==HUD_AMOUNTS[hudStage],HUD_NAMES[hudStage]+" retains synchronized hidden mana");
                capture(mc,previous,HUD_NAMES[hudStage]);
                if (++hudStage<HUD_AMOUNTS.length) {
                    if (hudStage==5) GLFW.glfwSetWindowSize(mc.getWindow().getWindow(),480,360);
                    pending=mc.getSingleplayerServer().submit(()->prepareHud(mc.getSingleplayerServer(),hudStage));next=now+1_200_000_000L;
                } else { writeMetadata();state=30;mc.stop(); }
            }
        } catch (Exception failure) {
            state = 30; LogUtils.getLogger().error("Standing Stone menu inspection failed", failure);
            try {
                Path out = Path.of(System.getProperty("vestige.capture.output")); Files.createDirectories(out);
                Files.writeString(out.resolve("error.txt"), failure.toString());
            } catch (Exception ignored) { }
            mc.stop();
        }
    }
    private static void prepareHud(MinecraftServer server, int stage) {
        var player=server.getPlayerList().getPlayers().getFirst();player.setGameMode(GameType.SURVIVAL);
        player.setHealth(player.getMaxHealth());player.getFoodData().setFoodLevel(20);
        player.setItemSlot(EquipmentSlot.HEAD,stage==2 || stage==4 ? new ItemStack(Items.IRON_HELMET) : ItemStack.EMPTY);
        player.setItemSlot(EquipmentSlot.CHEST,stage==2 || stage==4 ? new ItemStack(Items.IRON_CHESTPLATE) : ItemStack.EMPTY);
        player.setItemSlot(EquipmentSlot.LEGS,stage==2 || stage==4 ? new ItemStack(Items.IRON_LEGGINGS) : ItemStack.EMPTY);
        player.setItemSlot(EquipmentSlot.FEET,stage==2 || stage==4 ? new ItemStack(Items.IRON_BOOTS) : ItemStack.EMPTY);
        if (stage==4) {
            for (int x=-5;x<=-3;x++) for (int z=-4;z<=-2;z++) for (int y=65;y<=67;y++)
                server.overworld().setBlock(new BlockPos(x,y,z),Blocks.WATER.defaultBlockState(),3);
            player.setAirSupply(100);
        } else if (stage==5) {
            for (int x=-5;x<=-3;x++) for (int z=-4;z<=-2;z++) for (int y=65;y<=67;y++)
                server.overworld().setBlock(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState(),3);
            player.setAirSupply(300);
            player.teleportTo(16.5,65,-3.5);
        }
        NativeMana.set(player,HUD_AMOUNTS[stage]);
    }
    private static void writeMetadata() throws Exception {
        var hashes = new LinkedHashMap<String, String>();
        for (var type : List.of(StoneNetworkScreen.class, StoneTravelPayloads.class, StoneTravelPayloads.View.class, StoneTravelPayloads.Rename.class,
                StoneTravelPayloads.Page.class, StoneTravelPayloads.Request.class, StoneTravel.class, StandingStoneEntity.class,
                StoneDirectory.class, StoneNetwork.class, StandingStoneFare.class, PlayerExperience.class, TravelCostDisplay.class, TravelCostDisplay.Type.class,
                ManaDisplay.class, ItemManaOverlay.class, ItemManaOverlay.Registration.class, NativeMana.class, NativeStoneCapture.class, StandingStoneTest.class)) {
            String path = type.getName().replace('.', '/') + ".class";
            try (var stream = type.getResourceAsStream("/" + path)) {
                hashes.put(path, HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Objects.requireNonNull(stream).readAllBytes())));
            }
        }
        Path out = Path.of(System.getProperty("vestige.capture.output"));
        Files.writeString(out.resolve("capture.json"), new GsonBuilder().setPrettyPrinting().create().toJson(Map.of(
                "engine", "Minecraft 1.21.1 / NeoForge 21.1.72", "source", "Minecraft main render target",
                "world", "isolated creative flat world", "menu", "real server-backed endpoints and payloads",
                "checks", CHECKS, "classSha256", hashes)) + "\n");
    }
}
