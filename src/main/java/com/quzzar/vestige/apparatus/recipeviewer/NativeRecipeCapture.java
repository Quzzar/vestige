package com.quzzar.vestige.apparatus.recipeviewer;

import com.google.gson.GsonBuilder;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.*;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import java.nio.file.*;
import java.util.*;
import com.google.gson.JsonParser;
import net.minecraft.world.level.storage.LevelResource;

/** Opt-in inspection of the actual JEI/EMI screens, lookups and framebuffer in an isolated test world. */
@EventBusSubscriber(modid=VestigeMainMod.MOD_ID,value=Dist.CLIENT)
public final class NativeRecipeCapture {
    private static final boolean QUICK="scroll_knowledge".equals(System.getProperty("vestige.capture.kind"));
    private static final boolean ENABLED=QUICK || "recipes".equals(System.getProperty("vestige.capture.kind"));
    private static int state,index;
    private static long deadline;
    private static final String[] SPELLS={"fireball","starfall","attunement_shard","fireball","attunement_shard","reloaded_fireball","crafted_fireball","identified_magnetic_attraction","scroll_names"};
    private static final List<Map<String,Object>> evidence=new ArrayList<>();
    private static Map<String,Long> indexing=Map.of();
    private static List<Map<String,Object>> scrollNames=List.of();
    private static final String[] COLOR_SPELLS={"firebolt","burning_dash","ball_lightning","black_hole","pf2_magnetic_attraction"};
    private NativeRecipeCapture() { }
    @SubscribeEvent public static void frame(RenderFrameEvent.Post event)throws Exception {
        if(!ENABLED || state==4)return;
        var mc=Minecraft.getInstance();long now=System.nanoTime();
        if(state==0 && mc.screen instanceof TitleScreen && mc.getOverlay()==null) {
            mc.options.pauseOnLostFocus=false;mc.getTutorial().setStep(net.minecraft.client.tutorial.TutorialSteps.NONE);
            mc.getWindow().setWindowed(1280,720);mc.options.guiScale().set(4);mc.resizeDisplay();state=1;
            deadline=now+180_000_000_000L;
            mc.createWorldOpenFlows().createFreshLevel("vestige-recipe-viewer-"+UUID.randomUUID(),
                    new LevelSettings("Vestige recipe inspection",GameType.CREATIVE,false,Difficulty.PEACEFUL,true,new GameRules(),WorldDataConfiguration.DEFAULT),
                    new WorldOptions(483902,false,false),access -> access.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),mc.screen);
        } else if(state==1 && mc.player!=null && mc.level!=null && RitualViewerClient.displays().size()>200 && ready()) {
            var shaped=ScrollItems.shapedScroll(VestigeMainMod.location("fireball"),new LeylineShaping.Modifiers(1.1,1.2,1,1.1));
            indexing=Map.of("baseFireball",matches(ScrollItems.scroll(VestigeMainMod.location("fireball")),false),"shapedFireball",matches(shaped,false),
                    "attunedShard",matches(attunedShard(),false),"lapisUses",matches(new ItemStack(Items.LAPIS_LAZULI),true),
                    "paperSpellUses",matches(new ItemStack(Items.PAPER),true),"blazeRodSpellUses",matches(new ItemStack(Items.BLAZE_ROD),true));
            if(indexing.get("baseFireball")!=1 || indexing.get("shapedFireball")!=1 || indexing.get("attunedShard")!=1 || indexing.get("lapisUses")!=1
                    || indexing.get("paperSpellUses")!=0 || indexing.get("blazeRodSpellUses")!=0 || !concealed())
                throw new IllegalStateException("Invalid ritual viewer indexing: "+indexing);
            if(!ModList.get().isLoaded("emi") && JeiInspection.magneticSearch()!=0)
                throw new IllegalStateException("JEI search revealed an unknown spell name");
            mc.setScreen(new InventoryScreen(mc.player));
            if(QUICK){index=6;knowledge(false);state=5;deadline=now+120_000_000_000L;}
            else {show();state=2;deadline=now+3_000_000_000L;}
        } else if(state==2 && now>deadline) {
            var viewer=ModList.get().isLoaded("emi")?"emi":"jei";
            var out=Path.of(System.getProperty("vestige.capture.output"),viewer);Files.createDirectories(out);
            try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(out.resolve(index+"-"+SPELLS[index]+".png"));}
            evidence.add(Map.of("recipe",SPELLS[index],"guiScale",mc.options.guiScale().get(),"screen",mc.screen.getClass().getName(),"registeredRituals",count(),"guiWidth",mc.getWindow().getGuiScaledWidth(),"guiHeight",mc.getWindow().getGuiScaledHeight()));
            index++;
            if(index==SPELLS.length) {
                Files.writeString(out.resolve("capture.json"),new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("source","Minecraft main render target in actual recipe viewer","viewer",viewer,"lookups",evidence,"indexing",indexing,"scrollNames",scrollNames))+"\n");
                state=4;mc.stop();
            } else {
                if(index==3){mc.getWindow().setWindowed(960,720);mc.options.guiScale().set(6);mc.resizeDisplay();}
                if(index==5){reloadFixture();state=3;deadline=now+120_000_000_000L;}
                else if(index==6){knowledge(false);state=5;deadline=now+120_000_000_000L;}
                else if(index==7){knowledge(true);state=6;deadline=now+120_000_000_000L;}
                else if(index==8){mc.options.guiScale().set(2);mc.resizeDisplay();mc.setScreen(new KnowledgeScreen());deadline=now+2_000_000_000L;}
                else {show();deadline=now+2_000_000_000L;}
            }
        } else if(state==3 && ready() && reloaded()) {
            if(!concealed() || matches(new ItemStack(Items.PAPER),true)!=0)throw new IllegalStateException("Reload exposed hidden spell offerings");
            var checked=new LinkedHashMap<>(indexing);checked.put("reloadedFireballCapacity",8L);indexing=Map.copyOf(checked);
            show();state=2;deadline=now+2_000_000_000L;
        }
        else if(state==5 && ready() && matches(new ItemStack(Items.PAPER),true)==1) {
            var fireball=ScrollItems.scroll(VestigeMainMod.location("fireball"));
            if(!fireball.getHoverName().getString().equals("Unknown Scroll") || display("fireball").concealed())
                throw new IllegalStateException("Crafting did not reveal only the recipe");
            var checked=new LinkedHashMap<>(indexing);checked.put("craftedPaperUses",1L);indexing=Map.copyOf(checked);
            show();state=2;deadline=now+2_000_000_000L;
        } else if(state==6 && ready() && (ModList.get().isLoaded("emi") || JeiInspection.magneticSearch()==1)
                && Arrays.stream(COLOR_SPELLS).allMatch(id -> SpellKnowledge.visible(VestigeMainMod.location(id)))) {
            if(!ready())throw new IllegalStateException("Viewer count after identification: "+count()+" / "+RitualViewerClient.displays().size());
            var known=ScrollItems.scroll(VestigeMainMod.location("pf2_magnetic_attraction"));
            if(!known.getHoverName().getString().equals("Scroll of Magnetic Attraction") || !display("pf2_magnetic_attraction").concealed()
                    || !ScrollItems.scroll(VestigeMainMod.location("fireball")).getHoverName().getString().equals("Unknown Scroll"))
                throw new IllegalStateException("Identification did not preserve separate name/recipe knowledge");
            if(!ModList.get().isLoaded("emi") && JeiInspection.magneticSearch()!=1)
                throw new IllegalStateException("JEI search did not refresh the identified name");
            show();state=2;deadline=now+2_000_000_000L;
        }
        if((state==1 || state==3 || state==5 || state==6) && now>deadline)throw new IllegalStateException("Recipe viewer did not become ready: state="+state+", count="+count()+", identified="+display("pf2_magnetic_attraction").identified()+", visible="+SpellKnowledge.visible(VestigeMainMod.location("pf2_magnetic_attraction"))+", side="+net.neoforged.fml.util.thread.EffectiveSide.get());
    }
    private static RitualDisplays.Entry display(String id) {
        return RitualViewerClient.displays().stream().filter(e -> e.spell().map(s -> s.getPath().equals(id)).orElse(false)).findFirst().orElseThrow();
    }
    private static void knowledge(boolean identify) {
        var mc=Minecraft.getInstance();var uuid=mc.player.getUUID();var server=mc.getSingleplayerServer();
        server.execute(() -> {
            var player=server.getPlayerList().getPlayer(uuid);
            if(identify)for(var id:COLOR_SPELLS)SpellKnowledge.identify(player,VestigeMainMod.location(id));
            else SpellKnowledge.recordCraft(player,VestigeMainMod.location("fireball"));
        });
    }
    /** Native names and advanced tooltips after actual server-to-client knowledge updates. */
    private static final class KnowledgeScreen extends net.minecraft.client.gui.screens.Screen {
        private KnowledgeScreen(){super(net.minecraft.network.chat.Component.literal("Vestige scroll knowledge"));}
        @Override public void render(net.minecraft.client.gui.GuiGraphics g,int mouseX,int mouseY,float delta) {
            g.fill(0,0,width,height,0xff28241f);
            g.drawString(font,title,12,12,0xffe8dcc3,false);
            var unknown=ScrollItems.shapedScroll(VestigeMainMod.location("fireball"),new LeylineShaping.Modifiers(1.1,1.2,1,1.1),
                    List.of(new Spellshaping.Selection(VestigeMainMod.location("reaching"),1)));
            var shapedKnown=ScrollItems.shapedScroll(VestigeMainMod.location("ball_lightning"),new LeylineShaping.Modifiers(1.1,1.2,1,1.1),
                    List.of(new Spellshaping.Selection(VestigeMainMod.location("reaching"),1)));
            var stacks=new ArrayList<ItemStack>();stacks.add(unknown);
            for(var id:COLOR_SPELLS)stacks.add(ScrollItems.scroll(VestigeMainMod.location(id)));
            stacks.add(shapedKnown);
            var labels=List.of("Unknown","Common","Uncommon","Rare","Mythic","Identified","Augmented");
            var colors=List.of(net.minecraft.ChatFormatting.WHITE,net.minecraft.ChatFormatting.WHITE,net.minecraft.ChatFormatting.YELLOW,
                    net.minecraft.ChatFormatting.AQUA,net.minecraft.ChatFormatting.LIGHT_PURPLE,net.minecraft.ChatFormatting.WHITE,net.minecraft.ChatFormatting.AQUA);
            var names=new ArrayList<Map<String,Object>>();
            for(int i=0;i<stacks.size();i++) {
                var stack=stacks.get(i);int y=46+i*38;
                g.drawString(font,labels.get(i),12,y,0xffe8dcc3,false);g.renderItem(stack,104,y-4);
                var lines=nameOnly(stack,net.minecraft.world.item.TooltipFlag.NORMAL,colors.get(i));
                nameOnly(stack,net.minecraft.world.item.TooltipFlag.ADVANCED,colors.get(i));
                g.renderTooltip(font,lines,Optional.empty(),136,y);
                names.add(Map.of("label",labels.get(i),"name",lines.getFirst().getString(),"color",colors.get(i).getName(),"rgb",String.format("#%06X",colors.get(i).getColor())));
            }
            var enchantedUnknown=ScrollItems.scroll(VestigeMainMod.location("heartstop"));
            enchantedUnknown.set(net.minecraft.core.component.DataComponents.RARITY,net.minecraft.world.item.Rarity.EPIC);
            for(var stack:List.of(new ItemStack(ScrollItems.SCROLL.get()),ScrollItems.scroll(VestigeMainMod.location("acid_orb")),enchantedUnknown)) {
                if(!stack.getHoverName().getString().equals("Unknown Scroll"))throw new IllegalStateException("Unknown identity leaked");
                nameOnly(stack,net.minecraft.world.item.TooltipFlag.NORMAL,net.minecraft.ChatFormatting.WHITE);
                nameOnly(stack,net.minecraft.world.item.TooltipFlag.ADVANCED,net.minecraft.ChatFormatting.WHITE);
            }
            var augmentStyles=new ArrayList<net.minecraft.network.chat.Style>();
            shapedKnown.getHoverName().visit((style,text) -> {
                if(text.contains("Reaching"))augmentStyles.add(style);
                return Optional.<Boolean>empty();
            },net.minecraft.network.chat.Style.EMPTY);
            if(augmentStyles.isEmpty() || augmentStyles.stream().anyMatch(style -> !style.isItalic()))
                throw new IllegalStateException("Augment name lost its italics");
            scrollNames=List.copyOf(names);
            var checked=new LinkedHashMap<>(indexing);checked.put("nameOnlyTooltipCases",20L);checked.put("rarityColorTooltipCases",20L);indexing=Map.copyOf(checked);
        }
        private List<net.minecraft.network.chat.Component> nameOnly(ItemStack stack,net.minecraft.world.item.TooltipFlag flag,net.minecraft.ChatFormatting color) {
            var mc=Minecraft.getInstance();
            var lines=stack.getTooltipLines(net.minecraft.world.item.Item.TooltipContext.of(mc.level),mc.player,flag);
            if(lines.size()!=1 || !lines.getFirst().getString().equals(stack.getHoverName().getString()))
                throw new IllegalStateException("Scroll tooltip must contain only its name: "+lines);
            lines.getFirst().visit((style,text) -> {
                if(!text.isEmpty() && (style.getColor()==null || style.getColor().getValue()!=color.getColor()))
                    throw new IllegalStateException("Wrong scroll name color for "+text+": "+style.getColor()+", expected "+color);
                return Optional.<Boolean>empty();
            },net.minecraft.network.chat.Style.EMPTY);
            return lines;
        }
        @Override public boolean isPauseScreen(){return false;}
    }
    private static boolean ready(){return count()>200 && count()==RitualViewerClient.displays().size();}
    private static long count() {
        return ModList.get().isLoaded("emi")?EmiInspection.count():JeiInspection.count();
    }
    private static void show() {
        var stack=SPELLS[index].equals("attunement_shard")?(index==4?attunedShard():RitualDisplays.shard().output()):ScrollItems.scroll(VestigeMainMod.location(index==5 || index==6 ? "fireball" : index==7 ? "pf2_magnetic_attraction" : SPELLS[index]));
        if(ModList.get().isLoaded("emi"))EmiInspection.show(stack);else JeiInspection.show(stack);
    }
    private static long matches(ItemStack stack,boolean uses){return ModList.get().isLoaded("emi")?EmiInspection.matches(stack,uses):JeiInspection.matches(stack,uses);}
    private static ItemStack attunedShard() {
        var geometry=new LeylineShaping.Geometry(8,LeylineShaping.Shape.CROSS,2,0,LeylineShaping.Shape.DIAGONAL,4,0);
        var nodes=new ArrayList<RitualInputs.Node>();var ingredients=AttunementShardItem.ingredients();
        for(int i=0;i<8;i++)nodes.add(new RitualInputs.Node(i,geometry.offset(i),i<6?new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(ingredients.get(i))):ItemStack.EMPTY,ItemStack.EMPTY));
        return AttunementShardItem.create(new RitualInputs(geometry,nodes));
    }
    private static boolean concealed(){return RitualViewerClient.displays().stream().filter(RitualDisplays.Entry::concealed).allMatch(r -> r.offerings().isEmpty());}
    private static boolean reloaded(){return ModList.get().isLoaded("emi")?EmiInspection.fireballCapacity()==8:JeiInspection.fireballCapacity()==8;}
    private static void reloadFixture() {
        var server=Minecraft.getInstance().getSingleplayerServer();
        server.execute(() -> {
            try {
                var pack=server.getWorldPath(LevelResource.DATAPACK_DIR).resolve("vestige-viewer-inspection");
                var recipe=pack.resolve("data/vestige/ritual_recipes/fireball.json");Files.createDirectories(recipe.getParent());
                int format=net.minecraft.SharedConstants.getCurrentVersion().getPackVersion(net.minecraft.server.packs.PackType.SERVER_DATA);
                Files.writeString(pack.resolve("pack.mcmeta"),"{\"pack\":{\"pack_format\":"+format+",\"description\":\"Isolated recipe viewer reload check\"}}");
                try(var reader=new java.io.InputStreamReader(Objects.requireNonNull(NativeRecipeCapture.class.getResourceAsStream("/data/vestige/ritual_recipes/starfall.json")))) {
                    var json=JsonParser.parseReader(reader).getAsJsonObject();
                    json.addProperty("spell","vestige:fireball");json.addProperty("name","Fireball inspection override");
                    Files.writeString(recipe,new GsonBuilder().setPrettyPrinting().create().toJson(json));
                }
                server.getPackRepository().reload();var selected=new ArrayList<>(server.getPackRepository().getSelectedIds());selected.add("file/vestige-viewer-inspection");
                server.reloadResources(selected);
            } catch(java.io.IOException error){throw new java.io.UncheckedIOException(error);}
        });
    }
}
