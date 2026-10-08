package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.runtime.ForfeitPolicy;
import com.quzzar.vestige.magic.world.NativeMagic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/** Server-owned inspection, discovery and atomic ritual commitment, with bounded client-only movement. */
@EventBusSubscriber(modid = VestigeMainMod.MOD_ID)
public final class RitualCrafting {
    public enum Outcome { INVALID, HINTS, WRONG, EXPLOSION_PENDING, CRAFTING, DISCOVERING, ATTUNING, BUSY, NEEDS_PLINTHS }
    private static RitualCatalog catalog = new RitualCatalog();
    private static final Map<MinecraftServer,List<Pending>> PENDING = new IdentityHashMap<>();
    private RitualCrafting() { }
    public static RitualCatalog catalog() { return catalog; }
    @SubscribeEvent public static void load(AddReloadListenerEvent event) { catalog=new RitualCatalog(); event.addListener(catalog); }
    public record Layout(ServerLevel level, OfferingBlockEntity center, List<OfferingBlockEntity> stands, boolean advanced, LeylineShaping.Geometry geometry) {
        public Layout { stands=Collections.unmodifiableList(new ArrayList<>(stands)); }
        public List<ItemStack> items() { return stands.stream().map(s -> s == null ? ItemStack.EMPTY : s.displayedItem()).toList(); }
        public List<OfferingBlockEntity> blocks() {
            List<OfferingBlockEntity> all=new ArrayList<>(); all.add(center); stands.stream().filter(java.util.Objects::nonNull).forEach(all::add); return all;
        }
    }
    /** Inspection helper used by tools. Activation resolves capacity from the selected recipe. */
    public static Layout layout(OfferingBlockEntity center) {
        var advanced=LeylineStructure.find(center,8);
        if (advanced.size()==1) return advanced.getFirst();
        var basic=LeylineStructure.find(center,4);
        return basic.size()==1 ? basic.getFirst() : null;
    }
    private record Match(Layout layout,RitualRecipe recipe,RitualRecipe.Evaluation evaluation) { }
    public static Outcome activate(Player player, OfferingBlockEntity center) {
        return activate(player,center,player.getRandom()::nextDouble);
    }
    /** Draw injection lets behavior tests exercise exact risk boundaries without changing production policy. */
    static Outcome activate(Player player, OfferingBlockEntity center, java.util.function.DoubleSupplier random) {
        ItemStack reference=center.displayedItem();
        var scroll=ScrollItems.scroll(reference).orElse(null);
        RitualRecipe recipe=scroll==null ? null : catalog.recipes().get(scroll.spell());
        var spell=scroll==null ? null : NativeMagic.spells().spells().get(scroll.spell());
        if (!reference.isEmpty() && (recipe==null || spell==null)) return Outcome.INVALID;
        if (!center.resultItem().isEmpty() || center.busy() || !center.tryActivate()) return Outcome.BUSY;
        List<Layout> candidates=LeylineStructure.find(center,recipe==null ? 4 : recipe.circle());
        if (recipe==null) {
            var advanced=LeylineStructure.find(center,8);
            // A complete larger table supplies all eight seats to shapeless operations.
            var shapeless=advanced.isEmpty() ? candidates : advanced;
            var repairs=shapeless.stream().filter(l -> FluxedFlintRecipe.repair(l.items()).isPresent()).toList();
            if (!repairs.isEmpty()) {
                if (repairs.size()!=1) return Outcome.INVALID;
                var selected=repairs.getFirst();
                if (selected.blocks().stream().anyMatch(OfferingBlockEntity::busy)) return Outcome.BUSY;
                var repair=FluxedFlintRecipe.repair(selected.items()).orElseThrow();
                return begin(selected,player,repair.output(),repair.used(),0xf4e5ff,RitualInputs.capture(selected),
                        java.util.Optional.empty(),Map.of(repair.catalystSeat(),repair.remainingCatalyst()),random,Outcome.CRAFTING);
            }
            var diamonds=candidates.stream().filter(l -> DissentientDiamondRecipe.create(l.items()).isPresent()).toList();
            if (!diamonds.isEmpty()) {
                if (diamonds.size()!=1) return Outcome.INVALID;
                var selected=diamonds.getFirst();
                if (selected.blocks().stream().anyMatch(OfferingBlockEntity::busy)) return Outcome.BUSY;
                return begin(selected,player,DissentientDiamondRecipe.create(selected.items()).orElseThrow(),
                        List.of(0,2,4,6),0xf4e5ff,RitualInputs.capture(selected),random,Outcome.CRAFTING);
            }
            var flints=candidates.stream().filter(l -> FluxedFlintRecipe.create(l.items()).isPresent()).toList();
            if (!flints.isEmpty()) {
                if (flints.size()!=1) return Outcome.INVALID;
                var selected=flints.getFirst();
                if (selected.blocks().stream().anyMatch(OfferingBlockEntity::busy)) return Outcome.BUSY;
                var inputs=RitualInputs.capture(selected);var output=FluxedFlintRecipe.result(inputs);
                if (output.isEmpty()) return Outcome.INVALID;
                return begin(selected,player,output.get(),
                        java.util.stream.IntStream.range(0,8).filter(i -> !selected.items().get(i).isEmpty()).boxed().toList(),
                        0xf4e5ff,inputs,random,Outcome.CRAFTING);
            }
            var staffLayouts=new ArrayList<>(candidates);staffLayouts.addAll(advanced);
            var staffs=staffLayouts.stream().filter(l -> StaffRecipe.match(l.items(),l.geometry().slots()).isPresent()).toList();
            if(!staffs.isEmpty()) {
                if(staffs.size()!=1) return Outcome.INVALID;
                var selected=staffs.getFirst();
                if(selected.blocks().stream().anyMatch(OfferingBlockEntity::busy)) return Outcome.BUSY;
                var output=StaffRecipe.result(selected.items(),selected.geometry().slots());if(output.isEmpty()) return Outcome.INVALID;
                return begin(selected,player,output.get(),StaffRecipe.match(selected.items(),selected.geometry().slots()).orElseThrow().occupied(),0xf4e5ff,RitualInputs.capture(selected),random,Outcome.CRAFTING);
            }
            var boots=advanced.stream().filter(l -> com.quzzar.vestige.equipment.WayfarerRecipe.matches(l.items())).toList();
            if (!boots.isEmpty()) {
                if (boots.size()!=1) return Outcome.INVALID;
                var selected=boots.getFirst();
                if (selected.blocks().stream().anyMatch(OfferingBlockEntity::busy)) return Outcome.BUSY;
                var output=com.quzzar.vestige.equipment.WayfarerRecipe.result(selected.items(), selected.stands().stream().map(OfferingBlockEntity::materialItem).toList());
                if (output.isEmpty()) return Outcome.INVALID;
                return begin(selected,player,output.get(),java.util.stream.IntStream.range(0,8).boxed().toList(),
                        0xf4e5ff,RitualInputs.capture(selected),random,Outcome.CRAFTING);
            }
            var robes=advanced.stream().filter(l -> com.quzzar.vestige.equipment.MagicArmorRecipe.result(l.items()).isPresent()).toList();
            if (!robes.isEmpty()) {
                if (robes.size()!=1) return Outcome.INVALID;
                var selected=robes.getFirst();
                if (selected.blocks().stream().anyMatch(OfferingBlockEntity::busy)) return Outcome.BUSY;
                return begin(selected,player,com.quzzar.vestige.equipment.MagicArmorRecipe.result(selected.items()).orElseThrow(),
                        java.util.stream.IntStream.range(0,8).boxed().toList(),0xf4e5ff,RitualInputs.capture(selected),random,Outcome.CRAFTING);
            }
            var wands=advanced.stream().filter(l -> WandRecipe.match(l.items()).isPresent()).toList();
            if (!wands.isEmpty()) {
                if (wands.size()!=1) return Outcome.INVALID;
                var selected=wands.getFirst();
                if (selected.blocks().stream().anyMatch(OfferingBlockEntity::busy)) return Outcome.BUSY;
                var output=WandRecipe.result(selected.items());
                if (output.isEmpty()) return Outcome.INVALID;
                return begin(selected,player,output.get(),WandRecipe.match(selected.items()).orElseThrow().occupied(),0xf4e5ff,RitualInputs.capture(selected),random,Outcome.CRAFTING);
            }
            var threads=candidates.stream().filter(l -> MagicalThreadRecipe.matches(l.items())).toList();
            if (!threads.isEmpty()) {
                if (threads.size()!=1) return Outcome.INVALID;
                var selected=threads.getFirst();
                if (selected.blocks().stream().anyMatch(OfferingBlockEntity::busy)) return Outcome.BUSY;
                var inputs=RitualInputs.capture(selected);
                var output=MagicalThreadRecipe.result(inputs);
                if (output.isEmpty()) return Outcome.INVALID;
                var occupied=inputs.nodes().stream().filter(n -> !n.offering().isEmpty()).map(RitualInputs.Node::seat).toList();
                return begin(selected,player,output.get(),occupied,0xf4e5ff,inputs,random,Outcome.CRAFTING);
            }
            var shells=candidates.stream().filter(l -> WhisperingShellRecipe.result(l.items()).isPresent()).toList();
            if (!shells.isEmpty()) {
                if (shells.size()!=1) return Outcome.INVALID;
                var selected=shells.getFirst();
                if (selected.blocks().stream().anyMatch(OfferingBlockEntity::busy)) return Outcome.BUSY;
                var inputs=RitualInputs.capture(selected);
                var occupied=inputs.nodes().stream().filter(n -> !n.offering().isEmpty()).map(RitualInputs.Node::seat).toList();
                return begin(selected,player,WhisperingShellRecipe.result(selected.items()).orElseThrow(),occupied,0x81ddc3,inputs,random,Outcome.CRAFTING);
            }
            var hourglasses=candidates.stream().filter(l -> HourglassRecipe.matches(l.items())).toList();
            if (!hourglasses.isEmpty()) {
                if(hourglasses.size()!=1)return Outcome.INVALID;
                var selected=hourglasses.getFirst();
                if(selected.blocks().stream().anyMatch(OfferingBlockEntity::busy))return Outcome.BUSY;
                var inputs=RitualInputs.capture(selected);var output=HourglassRecipe.result(inputs);
                if(output.isEmpty())return Outcome.INVALID;
                return begin(selected,player,output.get(),List.of(0,2,4,6),0xf4e5ff,inputs,random,Outcome.CRAFTING);
            }
            var eyes=candidates.stream().filter(l -> HomeboundEyeRecipe.matches(l.items())).toList();
            if (!eyes.isEmpty()) {
                if (eyes.size()!=1) return Outcome.INVALID;
                var selected=eyes.getFirst();
                if (selected.blocks().stream().anyMatch(OfferingBlockEntity::busy)) return Outcome.BUSY;
                var output=HomeboundEyeRecipe.result(selected);
                if (output.isEmpty()) return Outcome.INVALID;
                var inputs=RitualInputs.capture(selected);
                var occupied=inputs.nodes().stream().filter(n -> !n.offering().isEmpty()).map(RitualInputs.Node::seat).toList();
                return begin(selected,player,output.get(),occupied,0xac73e8,inputs,random,Outcome.CRAFTING);
            }
            var stones=candidates.stream().filter(l -> com.quzzar.vestige.travel.StandingStoneRecipe.result(l.items()).isPresent()).toList();
            if (!stones.isEmpty()) {
                if (stones.size()!=1) return Outcome.INVALID;
                var selected=stones.getFirst();
                if (selected.blocks().stream().anyMatch(OfferingBlockEntity::busy)) return Outcome.BUSY;
                var inputs=RitualInputs.capture(selected);
                var occupied=inputs.nodes().stream().filter(n -> !n.offering().isEmpty()).map(RitualInputs.Node::seat).toList();
                return begin(selected,player,com.quzzar.vestige.travel.StandingStoneRecipe.result(selected.items()).orElseThrow(),occupied,0x83d9ef,inputs,random,Outcome.CRAFTING);
            }
            var attunements=advanced.stream().filter(l -> attunementRecipe(l.items())).toList();
            if (!attunements.isEmpty()) {
                if (attunements.size()!=1) return Outcome.INVALID;
                var selected=attunements.getFirst();
                if (selected.blocks().stream().anyMatch(OfferingBlockEntity::busy)) return Outcome.BUSY;
                var inputs=RitualInputs.capture(selected);
                var occupied=inputs.nodes().stream().filter(n -> !n.offering().isEmpty()).map(RitualInputs.Node::seat).toList();
                return begin(selected,player,AttunementShardItem.create(inputs),occupied,0x927be8,inputs,random,Outcome.ATTUNING);
            }
            var discovery=shapeless.stream().filter(l -> l.items().stream().filter(i -> !i.isEmpty()).count()>=4
                    && l.items().stream().filter(i -> !i.isEmpty()).allMatch(i -> ScrollItems.fragment(i).isPresent())).toList();
            if (!discovery.isEmpty()) {
                if (discovery.size()!=1) return Outcome.INVALID;
                Layout layout=discovery.getFirst();
                if (layout.blocks().stream().anyMatch(OfferingBlockEntity::busy)) return Outcome.BUSY;
                List<Integer> occupied=new ArrayList<>(); for (int i=0;i<8;i++) if (!layout.items().get(i).isEmpty()) occupied.add(i);
                var fragments=occupied.stream().map(i -> ScrollItems.fragment(layout.items().get(i)).orElseThrow()).toList();
                var pool=FragmentDiscovery.pool(NativeMagic.spells().spells().values(),fragments);
                if (pool.isEmpty()) return Outcome.INVALID;
                var discovered=FragmentDiscovery.pick(pool,random.getAsDouble()).orElseThrow();
                return begin(layout,player,ScrollItems.scroll(discovered.id()),occupied,0xac73e8,random,Outcome.DISCOVERING);
            }
            List<Match> matches=new ArrayList<>();
            var all=new ArrayList<>(candidates); all.addAll(advanced);
            for (var layout:all) for (var candidate:catalog.recipes().values()) {
                if (candidate.circle()!=layout.geometry().slots()) continue;
                var evaluation=candidate.evaluate(layout.items());
                if (evaluation.correct()) matches.add(new Match(layout,candidate,evaluation));
            }
            if (matches.stream().anyMatch(m -> m.recipe.circle()==8)) matches.removeIf(m -> m.recipe.circle()==4);
            if (matches.size()==1) {
                var match=matches.getFirst();
                if (match.layout.blocks().stream().anyMatch(OfferingBlockEntity::busy)) return Outcome.BUSY;
                return craft(player,match.layout,match.recipe,match.evaluation,random);
            }
            return Outcome.INVALID;
        }
        if (candidates.isEmpty()) {
            if(recipe.circle()==8) {
                var inner=LeylineStructure.find(center,4);
                if(inner.size()==1) {
                    var layout=inner.getFirst();
                    if(layout.blocks().stream().anyMatch(OfferingBlockEntity::busy)) return Outcome.BUSY;
                    RitualPresentation.missingPlinths(layout);
                    layout.level().playSound(null,center.getBlockPos(),SoundEvents.AMETHYST_BLOCK_CHIME,SoundSource.BLOCKS,.5f,.65f);
                    return Outcome.NEEDS_PLINTHS;
                }
            }
            return Outcome.INVALID;
        }
        RitualRecipe selectedRecipe=recipe;
        var correct=candidates.stream().filter(l -> selectedRecipe.evaluate(l.items()).correct()).toList();
        if (correct.size()>1 || correct.isEmpty() && candidates.size()>1) return Outcome.INVALID;
        Layout layout=correct.isEmpty() ? candidates.getFirst() : correct.getFirst();
        if (layout.blocks().stream().anyMatch(OfferingBlockEntity::busy)) return Outcome.BUSY;
        List<ItemStack> items=layout.items();
        var evaluation=recipe.evaluate(items);
        if (evaluation.correct()) return craft(player,layout,recipe,evaluation,random);
        for (int i=0;i<8;i++) {
            OfferingBlockEntity stand=layout.stands().get(i); if (stand==null) continue;
            var expected=evaluation.expected().get(i);
            stand.feedback(evaluation.feedback().get(i),expected==null ? ItemStack.EMPTY : expected.ingredient().hint(),recipe.color(),120);
        }
        center.feedback(RitualRecipe.Feedback.CORRECT,ItemStack.EMPTY,recipe.color(),120);
        RitualPresentation.connections(layout,120,false);
        layout.level().playSound(null,center.getBlockPos(),SoundEvents.AMETHYST_BLOCK_CHIME,SoundSource.BLOCKS,.7f,evaluation.complete() ? .65f : 1.1f);
        if (!evaluation.complete()) return Outcome.HINTS;
        double chance=ForfeitPolicy.DEFAULT.chance(spell.traits().resolve(scroll.modifiers()),SpellKnowledge.identified(player,spell.id()));
        var inputs=RitualInputs.capture(layout);
        var all=inputs.nodes().stream().filter(n -> !n.offering().isEmpty()).map(RitualInputs.Node::seat).toList();
        var culprit=RitualVolatility.culprit(reference,inputs,all,chance,random);
        if (culprit.isPresent()) {
            queue(layout,player,ItemStack.EMPTY,all,true,0xe55669,inputs,java.util.Optional.empty(),Map.of(),culprit.getAsInt());
            return Outcome.EXPLOSION_PENDING;
        }
        return Outcome.WRONG;
    }
    private static Outcome craft(Player player, Layout layout, RitualRecipe recipe, RitualRecipe.Evaluation evaluation,java.util.function.DoubleSupplier random) {
        var spell=NativeMagic.spells().spells().get(recipe.spell());
        if (spell==null) return Outcome.INVALID;
        var used=evaluation.expected().keySet().stream().sorted().toList();
        var shaping=LeylineShaping.resolve(layout.geometry(),spell.traits().ratings().keySet());
        var inputs=RitualInputs.capture(layout);
        List<Spellshaping.Selection> augments;
        try {
            augments=Spellshaping.resolve(inputs,spell);
            Spellshaping.compile(spell,augments,shaping.traits(),new com.quzzar.vestige.magic.runtime.CastShaping(shaping.cost(),true));
        } catch(IllegalArgumentException invalid){return Outcome.INVALID;}
        var output=ScrollItems.shapedScroll(spell.id(),shaping,augments);
        return begin(layout,player,output,used,recipe.color(),inputs,java.util.Optional.of(recipe.spell()),random,Outcome.CRAFTING);
    }
    private static boolean attunementRecipe(List<ItemStack> items) {
        if(items.size()!=8)return false;
        var required=new ArrayList<>(AttunementShardItem.ingredients());
        for(var item:items)if(!item.isEmpty() && !required.remove(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item.getItem())))return false;
        return required.isEmpty();
    }
    private static Outcome begin(Layout layout,Player player,ItemStack output,List<Integer> used,int color,java.util.function.DoubleSupplier random,Outcome success) {
        return begin(layout,player,output,used,color,RitualInputs.capture(layout),random,success);
    }
    private static Outcome begin(Layout layout,Player player,ItemStack output,List<Integer> used,int color,RitualInputs inputs,java.util.function.DoubleSupplier random,Outcome success) {
        return begin(layout,player,output,used,color,inputs,java.util.Optional.empty(),Map.of(),random,success);
    }
    private static Outcome begin(Layout layout,Player player,ItemStack output,List<Integer> used,int color,RitualInputs inputs,java.util.Optional<net.minecraft.resources.ResourceLocation> craftedSpell,java.util.function.DoubleSupplier random,Outcome success) {
        return begin(layout,player,output,used,color,inputs,craftedSpell,Map.of(),random,success);
    }
    private static Outcome begin(Layout layout,Player player,ItemStack output,List<Integer> used,int color,RitualInputs inputs,java.util.Optional<net.minecraft.resources.ResourceLocation> craftedSpell,Map<Integer,ItemStack> retained,java.util.function.DoubleSupplier random,Outcome success) {
        var reference=layout.center().displayedItem();
        var culprit=RitualVolatility.culprit(reference,inputs,used,ForfeitPolicy.DEFAULT.chance(RitualVolatility.traits(reference),true),random);
        if (culprit.isPresent()) {
            queue(layout,player,ItemStack.EMPTY,used,true,0xe55669,inputs,java.util.Optional.empty(),Map.of(),culprit.getAsInt());
            return Outcome.EXPLOSION_PENDING;
        }
        // Compile all remainders before output callbacks, then revalidate the full captured arrangement.
        var remainders=new java.util.HashMap<Integer,ItemStack>();
        for (int seat:used) {
            var ingredient=inputs.nodes().stream().filter(n -> n.seat()==seat).findFirst().orElseThrow().offering();
            remainders.put(seat,retained.containsKey(seat) ? retained.get(seat).copy() : ingredient.getCraftingRemainingItem());
        }
        queue(layout,player,output,used,false,color,inputs,craftedSpell,remainders,RitualVolatility.CENTER);
        return success;
    }
    private static void queue(Layout layout,Player player,ItemStack output,List<Integer> used,boolean explosion,int color,RitualInputs inputs,java.util.Optional<net.minecraft.resources.ResourceLocation> craftedSpell,Map<Integer,ItemStack> remainders,int culprit) {
        Pending pending=new Pending(layout,player,output,used,explosion,inputs,craftedSpell,remainders,culprit); PENDING.computeIfAbsent(layout.level().getServer(),server -> new ArrayList<>()).add(pending);
        for (var block:layout.blocks()) {
            block.lock(explosion ? 32 : 60);
            if (!explosion) block.feedback(RitualRecipe.Feedback.SUCCESS,ItemStack.EMPTY,color,60);
        }
        RitualPresentation.connections(layout,explosion ? 32 : 60,!explosion);
        layout.level().playSound(null,layout.center().getBlockPos(),SoundEvents.BEACON_ACTIVATE,SoundSource.BLOCKS,.65f,explosion ? .6f : 1.3f);
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post event) {
        List<Pending> active=PENDING.get(event.getServer()); if (active==null) return;
        active.removeIf(pending -> {
            if (!pending.valid()) { pending.clear(); return true; }
            long age=pending.layout.level().getGameTime()-pending.start;
            if (!pending.committed && age >= (pending.explosion ? 20 : 40)) {
                if (!pending.explosion) {
                    BlockPos center=pending.layout.center().getBlockPos();
                    double surface=((ApparatusBlock)pending.layout.center().getBlockState().getBlock()).offeringHeight();
                    var result=new ItemEntity(pending.layout.level(),center.getX()+.5,center.getY()+surface+.05,center.getZ()+.5,pending.output.copy());
                    result.setDeltaMovement(Vec3.ZERO);
                    result.setDefaultPickUpDelay();
                    // A cancelled entity spawn must not charge ingredients. Join-event listeners
                    // may also change an input, so revalidate once more before consumption.
                    if (!pending.layout.level().addFreshEntity(result) || !pending.valid() || !result.isAlive()) {
                        result.discard(); pending.clear(); return true;
                    }
                }
                // Every snapshot is revalidated before any consumption. All mutations run on this server thread.
                pending.used.forEach(i -> {
                    if (pending.explosion && i==pending.culprit) return;
                    pending.layout.stands().get(i).remove();
                    if (!pending.explosion) {
                        ItemStack remainder=pending.remainders.get(i);
                        if (!remainder.isEmpty()) pending.layout.stands().get(i).insert(remainder);
                    }
                });
                if (pending.explosion) {
                    if (pending.culprit!=RitualVolatility.CENTER) pending.layout.center().remove();
                    blast(pending.layout,pending.player);
                } else {
                    BlockPos center=pending.layout.center().getBlockPos();
                    pending.layout.level().sendParticles(ParticleTypes.ENCHANT,center.getX()+.5,center.getY()+1.1,center.getZ()+.5,45,.5,.5,.5,.5);
                    pending.layout.level().playSound(null,center,SoundEvents.ENCHANTMENT_TABLE_USE,SoundSource.BLOCKS,1,1.2f);
                }
                pending.committed=true;
                if (!pending.explosion) pending.craftedSpell.ifPresent(spell -> SpellKnowledge.recordCraft(pending.player,spell));
            }
            if (age>=(pending.explosion ? 32 : 60)) { pending.clear(); return true; }
            return false;
        });
        if (active.isEmpty()) PENDING.remove(event.getServer());
    }
    public static void cancelAll() { PENDING.values().forEach(list -> list.forEach(Pending::clear)); PENDING.clear(); }
    private record BlastImpact(double exposure,Vec3 impulse) { }
    /** Each active node bursts locally; overlapping exposure deals one strongest hit and impulse. */
    private static void blast(Layout layout,Player source) {
        var level=layout.level();
        Map<LivingEntity,BlastImpact> impacts=new IdentityHashMap<>();
        for (var node:layout.blocks()) {
            boolean central=node==layout.center();
            double radius=central ? 4 : 2;
            BlockPos pos=node.getBlockPos();
            Vec3 center=Vec3.atBottomCenterOf(pos).add(0,.9,0);
            if (central) level.sendParticles(ParticleTypes.EXPLOSION_EMITTER,center.x,center.y,center.z,1,0,0,0,0);
            else level.sendParticles(ParticleTypes.EXPLOSION,center.x,center.y,center.z,3,.3,.15,.3,0);
            level.playSound(null,pos,SoundEvents.GENERIC_EXPLODE.value(),SoundSource.BLOCKS,central ? 1 : .45f,central ? .85f : 1.15f);
            for (LivingEntity creature:level.getEntitiesOfClass(LivingEntity.class,new AABB(center,center).inflate(radius))) {
                double distance=creature.position().distanceTo(center)/radius;
                if (distance>=1 || creature.isSpectator()) continue;
                double exposure=(1-distance)*Explosion.getSeenPercent(center,creature);
                if (exposure<=0) continue;
                var previous=impacts.get(creature);
                if (previous==null || exposure>previous.exposure()) {
                    impacts.put(creature,new BlastImpact(exposure,creature.position().subtract(center).normalize().scale(exposure)));
                }
            }
        }
        // Resolve all exposure before damage/knockback can move any creature or change its state.
        impacts.forEach((creature,impact)->{
            double exposure=impact.exposure();
            creature.hurt(level.damageSources().explosion(source,source),(float)((exposure*exposure+exposure)*14+1));
            creature.setDeltaMovement(creature.getDeltaMovement().add(impact.impulse()));
            creature.hurtMarked=true;
        });
    }
    @SubscribeEvent public static void stop(ServerStoppedEvent event) { List<Pending> active=PENDING.remove(event.getServer()); if (active!=null) active.forEach(Pending::clear); }
    private static final class Pending {
        final Layout layout; final Player player; final ItemStack output; final List<Integer> used; final boolean explosion;
        final RitualInputs inputs; final ItemStack reference; final long start;
        final java.util.Optional<net.minecraft.resources.ResourceLocation> craftedSpell;
        final Map<Integer,ItemStack> remainders; final int culprit;
        boolean committed;
        Pending(Layout layout,Player player,ItemStack output,List<Integer> used,boolean explosion,RitualInputs inputs,java.util.Optional<net.minecraft.resources.ResourceLocation> craftedSpell,Map<Integer,ItemStack> remainders,int culprit) {
            this.remainders=remainders.entrySet().stream().collect(java.util.stream.Collectors.toUnmodifiableMap(Map.Entry::getKey,e -> e.getValue().copy())); this.culprit=culprit;
            this.craftedSpell=craftedSpell;
            this.layout=layout; this.player=player; this.output=output.copy(); this.used=List.copyOf(used); this.explosion=explosion;
            this.inputs=inputs; reference=layout.center().displayedItem(); start=layout.level().getGameTime();
        }
        boolean valid() {
            for (var block:layout.blocks()) if (!layout.level().hasChunkAt(block.getBlockPos()) || layout.level().getBlockEntity(block.getBlockPos())!=block) return false;
            if (!ItemStack.matches(committed && explosion && culprit!=RitualVolatility.CENTER ? ItemStack.EMPTY : reference,layout.center().displayedItem())) return false;
            if (!committed) {
                if (!layout.center().resultItem().isEmpty()) return false;
                if (!inputs.matches(layout)) return false;
            }
            return true;
        }
        void clear() { for (var block:layout.blocks()) if (layout.level().hasChunkAt(block.getBlockPos()) && layout.level().getBlockEntity(block.getBlockPos())==block) { block.unlock(); block.feedback(RitualRecipe.Feedback.NONE,ItemStack.EMPTY,0,0); } }
    }
}
