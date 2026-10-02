package com.quzzar.vestige.magic.presentation.client;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.presentation.SpellSensePayload;
import com.quzzar.vestige.magic.world.SpellMobility;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;
import java.util.*;

/** Prediction and perception only; server owns the lease and authoritative outcome. */
@EventBusSubscriber(modid=VestigeMainMod.MOD_ID, value=Dist.CLIENT)
public final class SpellSenseClient {
    private record Lease(SpellSensePayload data, long end) { }
    private static final Map<UUID,Lease> LEASES = new LinkedHashMap<>();
    private static ClientLevel level;
    private static Entity camera;
    private SpellSenseClient() { }
    public static void clear() {
        Minecraft mc = Minecraft.getInstance();
        if (camera != null && mc.getCameraEntity() == camera) mc.setCameraEntity(mc.player);
        camera = null; LEASES.clear(); level = null;
    }
    public static void receive(SpellSensePayload p) {
        ensure(); if (level == null || !level.dimension().location().equals(p.dimension())) return;
        if (p.stop()) LEASES.remove(p.lease());
        else if (LEASES.size() < 128 || LEASES.containsKey(p.lease())) LEASES.put(p.lease(),new Lease(p,level.getGameTime()+p.duration()));
        updateCamera();
    }
    private static void ensure() { if (level != Minecraft.getInstance().level) { clear(); level = Minecraft.getInstance().level; } }
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        ensure(); if (level == null || Minecraft.getInstance().isPaused()) return;
        LEASES.values().removeIf(lease -> level.getGameTime() >= lease.end);
        var player = Minecraft.getInstance().player;
        if (player != null) for (Lease lease : LEASES.values()) if (lease.data.entity().equals(player.getUUID())) {
            SpellMobility.apply(player, lease.data.kind());
            if (lease.data.kind().equals("absence")) { player.input.forwardImpulse=0; player.input.leftImpulse=0; player.setDeltaMovement(Vec3.ZERO); }
        }
        updateCamera();
    }
    private static void updateCamera() {
        Minecraft mc = Minecraft.getInstance();
        Entity desired = null;
        if (level != null && mc.player != null && mc.player.isAlive()) for (Lease lease : LEASES.values()) if (lease.data.kind().equals("camera")) {
            Entity entity = level.getEntity(lease.data.entityId());
            if (entity != null && entity.getUUID().equals(lease.data.entity())) desired = entity;
        }
        if (desired != null && (mc.getCameraEntity() == mc.player || mc.getCameraEntity() == camera)) {
            camera = desired; mc.setCameraEntity(desired);
        }
        else if (desired != null) camera = null; // Another camera owner keeps control.
        else if (camera != null) { if (mc.getCameraEntity() == camera) mc.setCameraEntity(mc.player); camera = null; }
    }
    @SubscribeEvent public static void interaction(InputEvent.InteractionKeyMappingTriggered event) {
        if (camera != null || LEASES.values().stream().anyMatch(l -> l.data.kind().equals("absence"))) {
            event.setCanceled(true); event.setSwingHand(false);
        }
    }
    @SubscribeEvent public static void sound(PlaySoundEvent event) {
        var sound = event.getSound(); if (sound == null || sound.isRelative()) return;
        Vec3 source = new Vec3(sound.getX(),sound.getY(),sound.getZ());
        if (LEASES.values().stream().anyMatch(l -> l.data.kind().equals("silence") && source.distanceToSqr(l.data.center()) <= l.data.radius()*l.data.radius())) event.setSound(null);
    }
    @SubscribeEvent public static void hide(RenderLivingEvent.Pre<?,?> event) {
        Vec3 eye = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        Vec3 point = event.getEntity().position();
        if (LEASES.values().stream().anyMatch(l -> (l.data.kind().equals("privacy") || l.data.kind().equals("rain"))
                && inside(eye,l.data) != inside(point,l.data))) event.setCanceled(true);
    }
    private static boolean inside(Vec3 point, SpellSensePayload p) { return point.distanceToSqr(p.center()) <= p.radius()*p.radius(); }
    @SubscribeEvent public static void tooltip(net.neoforged.neoforge.event.entity.player.ItemTooltipEvent event) {
        var player=Minecraft.getInstance().player;
        if (player!=null && LEASES.values().stream().anyMatch(l -> l.data.kind().equals("facade"))
                && net.minecraft.world.item.ItemStack.isSameItemSameComponents(event.getItemStack(),player.getMainHandItem()))
            event.getToolTip().add(net.minecraft.network.chat.Component.literal("Illusory facade: pristine appearance").withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE));
    }
}
