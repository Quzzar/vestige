package com.quzzar.vestige.apparatus;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.definition.*;
import com.quzzar.vestige.magic.runtime.*;
import com.quzzar.vestige.magic.world.NativeMagic;
import com.quzzar.vestige.travel.PositionTrail;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.*;

/** A native travel adapter: shared trait resolution/payment/visual plan, server-owned positional memory. */
@EventBusSubscriber(modid=VestigeMainMod.MOD_ID)
public final class HourglassMagic {
    private static final Map<UUID,PositionTrail> TRAILS=new HashMap<>();
    private HourglassMagic() { }
    private static ItemAbilityDefinition ability() { return NativeMagic.abilities().abilities().get(HourglassData.FAMILY); }
    private static long interval(SpellRuntime runtime,ItemAbilityDefinition ability,Player player,HourglassData.Variant variant) {
        double ticks=runtime.resolve(ability,player.getUUID(),variant.modifiers(ability)).variable(VestigeMainMod.location("return_ticks"));
        if(!Double.isFinite(ticks) || ticks<=0 || ticks>Long.MAX_VALUE/2d)throw new IllegalArgumentException("Invalid lookback");
        return Math.max(1,(long)Math.ceil(ticks));
    }
    public static void record(ServerPlayer player) {
        if(!player.isAlive() || player.isSpectator()) { clear(player);return; }
        var ability=ability();if(ability==null) { clear(player);return; }
        var session=NativeMagic.session(player.getServer());session.world().registerActor(player);
        long window=0;
        for(int i=0;i<player.getInventory().getContainerSize();i++) {
            var variant=HourglassData.read(player.getInventory().getItem(i));
            if(variant.isPresent())try { window=Math.max(window,interval(session.runtime(),ability,player,variant.get())); }
            catch(IllegalArgumentException ignored) { }
        }
        if(window==0) { clear(player);return; }
        TRAILS.computeIfAbsent(player.getUUID(),id -> new PositionTrail()).record(player.serverLevel().getGameTime(),
                player.level().dimension().location(),player.position(),window);
    }
    public static boolean returnToPast(ServerPlayer player,ItemStack stack) {
        if(!player.isAlive() || player.isSpectator() || player.isPassenger() || player.isSleeping())return false;
        var variant=HourglassData.read(stack);var ability=ability();
        if(variant.isEmpty() || ability==null || !carried(player,stack))return false;
        record(player);
        var trail=TRAILS.get(player.getUUID());if(trail==null)return false;
        var session=NativeMagic.session(player.getServer());
        long ticks;
        try { ticks=interval(session.runtime(),ability,player,variant.get()); }catch(IllegalArgumentException invalid) { return false; }
        var sample=trail.destination(player.serverLevel().getGameTime(),ticks).orElse(null);
        if(sample==null || !sample.dimension().equals(player.level().dimension().location()) || !arrival(player,sample.position()))return false;
        var source=new Return(player,stack,sample.position());
        var compiled=new ItemAbilityDefinition(ability.id(),ability.traits(),ability.variables(),variant.get().costs(ability),ability.triggers(),ability.effects(),ability.activation());
        session.runtime().activate(compiled,SpellEvent.of(VestigeMainMod.location("interact"),player.getUUID(),null),variant.get().modifiers(ability),source);
        if(source.moved)record(player);
        return source.moved;
    }
    private static boolean carried(Player player,ItemStack stack) {
        for(int i=0;i<player.getInventory().getContainerSize();i++)if(player.getInventory().getItem(i)==stack)return true;
        return false;
    }
    /** Exact historical coordinates; no search that could silently choose another destination. */
    private static boolean arrival(ServerPlayer player,Vec3 destination) {
        var level=player.serverLevel();var box=player.getBoundingBox().move(destination.subtract(player.position()));
        if(player.position().distanceToSqr(destination)<1e-8 || destination.y<level.getMinBuildHeight()
                || box.maxY>=level.getMaxBuildHeight() || !level.getWorldBorder().isWithinBounds(box))return false;
        for(int x=BlockPos.containing(box.minX,0,0).getX()>>4;x<=BlockPos.containing(box.maxX,0,0).getX()>>4;x++)
            for(int z=BlockPos.containing(0,0,box.minZ).getZ()>>4;z<=BlockPos.containing(0,0,box.maxZ).getZ()>>4;z++)
                if(!level.hasChunk(x,z))return false;
        return level.noCollision(player,box);
    }
    private static final class Return implements CastReservation.Atomic {
        final ServerPlayer player;final ItemStack stack;final Vec3 destination;
        boolean moved;
        Return(ServerPlayer player,ItemStack stack,Vec3 destination) { this.player=player;this.stack=stack;this.destination=destination; }
        @Override public boolean valid() { return !moved && player.isAlive() && !player.isPassenger() && !player.isSleeping()
                && carried(player,stack) && HourglassData.read(stack).isPresent() && arrival(player,destination); }
        @Override public boolean tryCommit() {
            if(!valid())return false;
            var request=new net.neoforged.neoforge.event.entity.EntityTeleportEvent(player,destination.x,destination.y,destination.z);
            if(net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(request).isCanceled() || !request.getTarget().equals(destination) || !valid())return false;
            var level=player.serverLevel();var from=player.position();var motion=player.getDeltaMovement();float fall=player.fallDistance;
            if(!player.teleportTo(level,destination.x,destination.y,destination.z,Set.of(),player.getYRot(),player.getXRot())
                    || player.level()!=level || player.position().distanceToSqr(destination)>1e-8)return false;
            // Return only position. Current velocity, fall accumulation and view direction are not rewound.
            player.setDeltaMovement(motion);player.fallDistance=fall;moved=true;
            if(!player.isCreative()) {
                stack.setDamageValue(stack.getDamageValue()+1);
                if(stack.getDamageValue()>=stack.getMaxDamage()) { stack.shrink(1);level.playSound(null,player.blockPosition(),SoundEvents.ITEM_BREAK,SoundSource.PLAYERS,.8f,1); }
            }
            level.sendParticles(ParticleTypes.REVERSE_PORTAL,from.x,from.y+.9,from.z,24,.25,.5,.25,.03);
            level.playSound(null,destination.x,destination.y,destination.z,SoundEvents.ENDERMAN_TELEPORT,SoundSource.PLAYERS,.65f,1.35f);
            return true;
        }
    }
    /** Operator/test inspection only; no player-facing instructions or status. */
    static String inspect(ServerPlayer player,ItemStack stack) {
        var trail=TRAILS.get(player.getUUID());var sample=trail==null ? Optional.<PositionTrail.Sample>empty() : trail.destination(player.level().getGameTime(),300);
        return "ability="+(ability()!=null)+", valid="+HourglassData.read(stack).isPresent()+", carried="+carried(player,stack)
                +", alive="+player.isAlive()+", now="+player.level().getGameTime()+", samples="+(trail==null ? 0 : trail.size())+", sample="+sample
                +", arrival="+sample.map(v -> arrival(player,v.position())).orElse(false)+", position="+player.position();
    }
    public static void clear(Player player) { TRAILS.remove(player.getUUID()); }
    @SubscribeEvent public static void tick(PlayerTickEvent.Post event) { if(event.getEntity() instanceof ServerPlayer player)record(player); }
    @SubscribeEvent public static void death(LivingDeathEvent event) { if(event.getEntity() instanceof Player player)clear(player); }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) { clear(event.getEntity()); }
    @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event) { clear(event.getEntity()); }
    @SubscribeEvent public static void stop(ServerStoppedEvent event) { TRAILS.clear(); }
}
