package com.quzzar.vestige.magic.world;

import com.quzzar.vestige.VestigeMainMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;

@EventBusSubscriber(modid = VestigeMainMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class SpellEntities {
    public static final DeferredRegister<EntityType<?>> TYPES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, VestigeMainMod.MOD_ID);
    public static final Supplier<EntityType<SpellProjectile>> PROJECTILE = TYPES.register("spell_projectile", () ->
            EntityType.Builder.<SpellProjectile>of(SpellProjectile::new, MobCategory.MISC).sized(0.25f, 0.25f)
                    .clientTrackingRange(8).updateInterval(1).build("vestige:spell_projectile"));
    public static final Supplier<EntityType<SpellAnchor>> ANCHOR = TYPES.register("spell_anchor", () ->
            EntityType.Builder.<SpellAnchor>of(SpellAnchor::new, MobCategory.MISC).sized(.5f, 1.975f)
                    .clientTrackingRange(8).updateInterval(1).build("vestige:spell_anchor"));
    public static final Supplier<EntityType<SpellConstruct>> CONSTRUCT = TYPES.register("spell_construct", () ->
            EntityType.Builder.<SpellConstruct>of(SpellConstruct::new, MobCategory.MISC).sized(1, 1)
                    .clientTrackingRange(8).updateInterval(1).build("vestige:spell_construct"));
    public static final Supplier<EntityType<net.minecraft.world.entity.Display.BlockDisplay>> BLOCK_DISPLAY = TYPES.register("spell_block_display", () ->
            EntityType.Builder.<net.minecraft.world.entity.Display.BlockDisplay>of(SpellBlockDisplay::new,MobCategory.MISC).sized(1,1)
                    .clientTrackingRange(8).updateInterval(1).build("vestige:spell_block_display"));
    public static final Supplier<EntityType<SpellEcho>> ECHO=TYPES.register("spell_echo",()->
            EntityType.Builder.<SpellEcho>of(SpellEcho::new,MobCategory.MISC).sized(.6f,1.8f)
                    .clientTrackingRange(8).updateInterval(1).build("vestige:spell_echo"));
    @SubscribeEvent public static void attributes(EntityAttributeCreationEvent event) {
        event.put(ANCHOR.get(), LivingEntity.createLivingAttributes().add(Attributes.MAX_HEALTH, 20).build());
        event.put(CONSTRUCT.get(), LivingEntity.createLivingAttributes().add(Attributes.MAX_HEALTH, 100).build());
    }
    private SpellEntities() { }
}
