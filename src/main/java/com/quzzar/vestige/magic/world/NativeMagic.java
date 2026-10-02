package com.quzzar.vestige.magic.world;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.data.RuntimeSpellLoader;
import com.quzzar.vestige.magic.definition.SpellTriggerTypes;
import com.quzzar.vestige.magic.definition.SpellDefinition;
import com.quzzar.vestige.magic.runtime.*;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import com.quzzar.vestige.magic.condition.ConditionValue;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import java.util.*;

/** NeoForge adapter for the native magic runtime. All state belongs to a specific running server. */
@EventBusSubscriber(modid = VestigeMainMod.MOD_ID)
public final class NativeMagic {
    private static final Map<MinecraftServer, Session> SERVERS = new IdentityHashMap<>();
    private static RuntimeSpellLoader loader = new RuntimeSpellLoader();
    private NativeMagic() { }
    public record Session(MinecraftSpellWorld world, SpellRuntime runtime) { }
    public static Session session(MinecraftServer server) {
        return SERVERS.computeIfAbsent(server, key -> {
            MinecraftSpellWorld world = new MinecraftSpellWorld(key);
            return new Session(world, new SpellRuntime(world));
        });
    }
    public static RuntimeSpellLoader spells() { return loader; }
    static boolean ownsTemporaryBlock(net.minecraft.server.level.ServerLevel level,net.minecraft.core.BlockPos pos) {
        Session session=SERVERS.get(level.getServer());return session!=null && session.world.features.ownsTemporaryBlock(level,pos);
    }
    static void blockChanged(net.minecraft.server.level.ServerLevel level,net.minecraft.core.BlockPos pos) {
        Session session=SERVERS.get(level.getServer());if(session!=null) session.world.features.blockChanged(level,pos);
    }
    public static void reload() {
        SERVERS.values().forEach(session -> { session.runtime.close(); session.world.close(); });
        SERVERS.clear();
    }
    @SubscribeEvent public static void load(AddReloadListenerEvent event) {
        loader = new RuntimeSpellLoader();
        event.addListener(loader);
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post event) {
        Session session = SERVERS.get(event.getServer());
        if (session != null) { session.runtime.tick(); session.world.tick(); }
        if (event.getServer().getTickCount()%100==0) PetCache.retry(event.getServer());
        event.getServer().getPlayerList().getPlayers().forEach(PrivateSpaces::enforce);
    }
    @SubscribeEvent public static void stop(ServerStoppedEvent event) {
        Session session = SERVERS.remove(event.getServer());
        if (session != null) { session.runtime.close(); session.world.close(); }
    }
    @SubscribeEvent public static void stopping(net.neoforged.neoforge.event.server.ServerStoppingEvent event) {
        Session session = SERVERS.remove(event.getServer());
        if (session != null) { session.runtime.close(); session.world.close(); }
    }
    @SubscribeEvent public static void started(net.neoforged.neoforge.event.server.ServerStartedEvent event) { PetCache.retry(event.getServer()); }
    @SubscribeEvent public static void chunkUnload(net.neoforged.neoforge.event.level.ChunkEvent.Unload event) {
        if (event.getLevel() instanceof net.minecraft.server.level.ServerLevel level) {
            Session session=SERVERS.get(level.getServer()); if (session!=null) session.world.features.unloading(event.getChunk());
        }
    }
    @SubscribeEvent public static void sound(net.neoforged.neoforge.event.PlayLevelSoundEvent.AtPosition event) {
        if (!(event.getLevel() instanceof net.minecraft.server.level.ServerLevel level)) return;
        Session session=SERVERS.get(level.getServer());
        if (session!=null && session.world.features.silent(event.getPosition(),level)) event.setCanceled(true);
    }
    @SubscribeEvent public static void entitySound(net.neoforged.neoforge.event.PlayLevelSoundEvent.AtEntity event) {
        if (!(event.getLevel() instanceof net.minecraft.server.level.ServerLevel level)) return;
        Session session=SERVERS.get(level.getServer());
        if (session!=null && session.world.features.silent(event.getEntity().position(),level)) event.setCanceled(true);
    }
    @SubscribeEvent public static void damage(LivingIncomingDamageEvent event) {
        MinecraftServer server = event.getEntity().getServer();
        Session session = SERVERS.get(server);
        if (session == null || event.getEntity().level().isClientSide()) return;
        SpellEvent.PendingOutcome pending = new SpellEvent.PendingOutcome(event.getAmount());
        session.world.features.intercept(event.getEntity(), event.getSource(), pending);
        session.runtime.emit(new SpellEvent(SpellTriggerTypes.DAMAGE_CALCULATING, event.getEntity().getUUID(),
                Optional.of(new SpellSubject.Entity(event.getEntity().getUUID())), Optional.of(pending), session.world.cause(event.getSource().getDirectEntity()),
                Map.of(VestigeMainMod.location("event/attacker"), new ConditionValue.Text(event.getSource().getEntity() == null ? "" : event.getSource().getEntity().getUUID().toString()),
                        VestigeMainMod.location("event/magical"), new ConditionValue.Flag(session.world.magical(event.getSource())),
                        VestigeMainMod.location("event/fire"), new ConditionValue.Flag(event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_FIRE)),
                        VestigeMainMod.location("event/freezing"), new ConditionValue.Flag(event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_FREEZING)))));
        event.setAmount((float) pending.commit());
        if (event.getAmount() == 0) event.setCanceled(true);
    }
    @SubscribeEvent public static void commands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("vestige_magic").requires(source -> source.hasPermission(2))
                .then(Commands.literal("list").executes(context -> {
                    context.getSource().sendSuccess(() -> Component.literal("Native spells: " + loader.spells().keySet().stream().sorted().toList()), false);
                    return loader.spells().size();
                }))
                .then(Commands.literal("cast").then(Commands.argument("spell", StringArgumentType.word())
                        .suggests((context, builder) -> net.minecraft.commands.SharedSuggestionProvider.suggestResource(loader.spells().keySet(), builder))
                        .executes(context -> executeCast(context, Optional.empty(), true))
                        .then(Commands.argument("mode", StringArgumentType.word()).suggests(NativeMagic::suggestModes).executes(context -> {
                            ResourceLocation mode = ResourceLocation.tryParse(StringArgumentType.getString(context, "mode"));
                            if (mode == null) return 0;
                            return executeCast(context, Optional.of(mode), true);
                        }))))
                .then(Commands.literal("cast_balanced").then(Commands.argument("spell", StringArgumentType.word())
                        .suggests((context, builder) -> net.minecraft.commands.SharedSuggestionProvider.suggestResource(loader.spells().keySet(), builder))
                        .executes(context -> executeCast(context, Optional.empty(), false))
                        .then(Commands.argument("mode", StringArgumentType.word()).suggests(NativeMagic::suggestModes).executes(context -> {
                            ResourceLocation mode = ResourceLocation.tryParse(StringArgumentType.getString(context, "mode"));
                            if (mode == null) return 0;
                            return executeCast(context, Optional.of(mode), false);
                        }))))
                .then(Commands.literal("mana").executes(context -> {
                    var player = context.getSource().getPlayerOrException();
                    context.getSource().sendSuccess(() -> Component.literal("Native test mana: " + player.getPersistentData().getDouble("vestige:mana") + "/200"), false);
                    return 1;
                }).then(Commands.argument("amount", IntegerArgumentType.integer(0, 200)).executes(context -> {
                    var player = context.getSource().getPlayerOrException();
                    player.getPersistentData().putDouble("vestige:mana", IntegerArgumentType.getInteger(context, "amount"));
                    return 1;
                })))
                .then(Commands.literal("dispel").executes(context -> {
                    var player = context.getSource().getPlayerOrException();
                    return session(player.getServer()).runtime.dispelActor(player.getUUID());
                }))
                .then(Commands.literal("interrupt").executes(context -> {
                    var player = context.getSource().getPlayerOrException();
                    return session(player.getServer()).runtime.interruptActor(player.getUUID());
                }))
                .then(Commands.literal("accept_magic").then(Commands.argument("accept",com.mojang.brigadier.arguments.BoolArgumentType.bool()).executes(context -> {
                    context.getSource().getPlayerOrException().getPersistentData().putBoolean("vestige:accept_magic",com.mojang.brigadier.arguments.BoolArgumentType.getBool(context,"accept")); return 1;
                })))
                .then(Commands.literal("effects").then(Commands.argument("spell",StringArgumentType.word())
                        .suggests((context,builder) -> net.minecraft.commands.SharedSuggestionProvider.suggestResource(loader.spells().keySet(),builder))
                        .executes(context -> SpellEffectGallery.show(context.getSource(),findSpell(StringArgumentType.getString(context,"spell")),0))
                        .then(Commands.argument("phase",IntegerArgumentType.integer(0)).executes(context -> SpellEffectGallery.show(context.getSource(),findSpell(StringArgumentType.getString(context,"spell")),IntegerArgumentType.getInteger(context,"phase")))))));
    }
    private static boolean blocked(net.minecraft.world.entity.player.Player player) {
        Session session=SERVERS.get(player.getServer()); return session!=null && (session.world.features.absent(player.getUUID()) || session.world.features.remote(player.getUUID()));
    }
    @SubscribeEvent public static void attack(net.neoforged.neoforge.event.entity.player.AttackEntityEvent event) { if (blocked(event.getEntity())) event.setCanceled(true); }
    @SubscribeEvent public static void use(PlayerInteractEvent.RightClickItem event) { if (blocked(event.getEntity())) event.setCanceled(true); }
    @SubscribeEvent public static void interact(PlayerInteractEvent.EntityInteract event) { if (blocked(event.getEntity())) event.setCanceled(true); }
    @SubscribeEvent public static void mine(PlayerInteractEvent.LeftClickBlock event) { if (blocked(event.getEntity())) event.setCanceled(true); }

    @SubscribeEvent public static void healing(net.neoforged.neoforge.event.entity.living.LivingHealEvent event) {
        Session session = SERVERS.get(event.getEntity().getServer()); if (session == null || event.getEntity().level().isClientSide()) return;
        SpellEvent.PendingOutcome pending = new SpellEvent.PendingOutcome(event.getAmount());
        session.runtime.emit(new SpellEvent(SpellTriggerTypes.HEAL_CALCULATING, event.getEntity().getUUID(),
                Optional.of(new SpellSubject.Entity(event.getEntity().getUUID())), Optional.of(pending), CausalChain.start()));
        event.setAmount((float) pending.commit()); if (event.getAmount() == 0) event.setCanceled(true);
    }

    private static java.util.concurrent.CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestModes(
            com.mojang.brigadier.context.CommandContext<net.minecraft.commands.CommandSourceStack> context,
            com.mojang.brigadier.suggestion.SuggestionsBuilder builder) {
        var spell=findSpell(StringArgumentType.getString(context,"spell"));
        return net.minecraft.commands.SharedSuggestionProvider.suggestResource(spell==null ? Set.<ResourceLocation>of() : spell.modes().keySet(),builder);
    }
    private static SpellDefinition findSpell(String value) {
        ResourceLocation id=ResourceLocation.tryParse(value);
        return id==null ? null : loader.spells().get(id);
    }
    private static int executeCast(com.mojang.brigadier.context.CommandContext<net.minecraft.commands.CommandSourceStack> context,
                                   Optional<ResourceLocation> mode, boolean bypass) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ResourceLocation id = ResourceLocation.tryParse(StringArgumentType.getString(context, "spell"));
        var spell = id == null ? null : loader.spells().get(id);
        if (spell == null) { context.getSource().sendFailure(Component.literal("Unknown native spell")); return 0; }
        var player = context.getSource().getPlayerOrException();
        Session session = session(player.getServer()); session.world.registerActor(player);
        var cast = session.runtime.cast(spell, SpellEvent.of(SpellTriggerTypes.INTERACT, player.getUUID(), null), List.of(), true, mode, bypass);
        context.getSource().sendSuccess(() -> Component.literal(id + ": " + cast.status() + cast.failure().map(s -> " (" + s + ")").orElse("")), false);
        return cast.status() == SpellRuntime.Status.COMPLETED || cast.status() == SpellRuntime.Status.RUNNING || cast.status() == SpellRuntime.Status.CHARGING || cast.status() == SpellRuntime.Status.AWAITING_RECAST ? 1 : 0;
    }
    @SubscribeEvent public static void dealt(LivingDamageEvent.Post event) {
        Session session = SERVERS.get(event.getEntity().getServer()); if (session == null) return;
        var cause = session.world.cause(event.getSource().getDirectEntity());
        var facts = Map.<ResourceLocation, ConditionValue>of(VestigeMainMod.location("event/attacker"), new ConditionValue.Text(event.getSource().getEntity() == null ? "" : event.getSource().getEntity().getUUID().toString()),
                VestigeMainMod.location("event/damage_amount"), new ConditionValue.Decimal(event.getNewDamage()),
                VestigeMainMod.location("event/magical"), new ConditionValue.Flag(session.world.magical(event.getSource())));
        if (event.getSource().getEntity() instanceof net.minecraft.world.entity.LivingEntity source) {
            session.runtime.emit(new SpellEvent(SpellTriggerTypes.DAMAGE_DEALT, source.getUUID(), Optional.of(new SpellSubject.Entity(event.getEntity().getUUID())), Optional.empty(), cause, facts), new SpellSubject.Entity(source.getUUID()));
        }
        session.runtime.emit(new SpellEvent(SpellTriggerTypes.DAMAGE_TAKEN, event.getEntity().getUUID(), Optional.of(new SpellSubject.Entity(event.getEntity().getUUID())), Optional.empty(), cause, facts));
    }
    @SubscribeEvent public static void died(LivingDeathEvent event) {
        Session session = SERVERS.get(event.getEntity().getServer()); if (session == null) return;
        var subject = new SpellSubject.Entity(event.getEntity().getUUID()); var cause = session.world.cause(event.getSource().getDirectEntity());
        session.runtime.emit(new SpellEvent(SpellTriggerTypes.DIE, event.getEntity().getUUID(), Optional.of(subject), Optional.empty(), cause));
        if (event.getSource().getEntity() instanceof net.minecraft.world.entity.LivingEntity source)
            session.runtime.emit(new SpellEvent(SpellTriggerTypes.KILL, source.getUUID(), Optional.of(subject), Optional.empty(), cause), new SpellSubject.Entity(source.getUUID()));
    }
    @SubscribeEvent public static void ate(LivingEntityUseItemEvent.Finish event) {
        Session session = SERVERS.get(event.getEntity().getServer()); if (session == null) return;
        var food = event.getItem().get(net.minecraft.core.component.DataComponents.FOOD); if (food == null) return;
        session.runtime.emit(new SpellEvent(SpellTriggerTypes.ITEM_USE_FINISHED, event.getEntity().getUUID(), Optional.empty(), Optional.empty(), CausalChain.start(),
                Map.of(VestigeMainMod.location("event/food_nutrition"), new ConditionValue.Decimal(food.nutrition()))));
    }
    @SubscribeEvent public static void lock(PlayerInteractEvent.RightClickBlock event) {
        if (blocked(event.getEntity())) { event.setCanceled(true); return; }
        if (event.getLevel().isClientSide()) return;
        var block = event.getLevel().getBlockEntity(event.getPos()); if (block == null) return;
        String owner = block.getPersistentData().getString("vestige:lock_owner");
        if (!owner.isEmpty() && !owner.equals(event.getEntity().getUUID().toString())) { event.setCanceled(true); event.setCancellationResult(net.minecraft.world.InteractionResult.FAIL); }
    }
}
