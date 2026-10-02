package com.quzzar.vestige.magic.world;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.condition.ConditionValue;
import com.quzzar.vestige.magic.effect.SpellEffects;
import com.quzzar.vestige.magic.runtime.SpellRuntime;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/** Finite movement, occupancy and inspection operations; no discovery or progression state. */
final class SpellUtilityActions {
    private SpellUtilityActions() { }
    static boolean execute(MinecraftSpellWorld world, SpellEffects.Action action, SpellRuntime.Context context,
                           LivingEntity caster, LivingEntity target, ServerLevel level) {
        return switch (action.type().getPath()) {
            case "random_teleport" -> {
                double radius = SpellActions.value(action, context, "radius", 4);
                if (radius < 1 || radius > 16) yield false;
                boolean moved = false;
                for (int attempt = 0; attempt < 16 && !moved; attempt++) {
                    double angle = caster.getRandom().nextDouble() * Math.PI * 2;
                    double distance = 1 + caster.getRandom().nextDouble() * (radius - 1);
                    Vec3 destination = caster.position().add(Math.cos(angle) * distance, -1, Math.sin(angle) * distance);
                    moved = SpellActions.teleport(caster, (ServerLevel) caster.level(), destination, true);
                }
                // Obstructed terrain skips this pulse without ending the surrounding buff.
                context.setNumber(VestigeMainMod.location("last_teleport"), moved ? 1 : 0);
                yield true;
            }
            case "dwell_heal" -> {
                if (target == null) yield false;
                double amount = SpellActions.value(action, context, "amount", 0);
                int required = (int) SpellActions.value(action, context, "required_ticks", 60);
                int interval = (int) SpellActions.value(action, context, "interval", 5);
                if (amount < 0 || amount > 1000 || required < 1 || required > 2400 || interval < 1 || interval > 100) yield false;
                var startedKey = VestigeMainMod.location("dwell/" + target.getUUID() + "/started");
                var lastKey = VestigeMainMod.location("dwell/" + target.getUUID() + "/last");
                long now = level.getGameTime();
                double last = context.value(lastKey).filter(v -> v instanceof ConditionValue.Decimal)
                        .map(v -> ((ConditionValue.Decimal) v).value()).orElse(Double.NEGATIVE_INFINITY);
                if (now - last > interval) context.setNumber(startedKey, now);
                context.setNumber(lastKey, now);
                double started = ((ConditionValue.Decimal) context.value(startedKey).orElseThrow()).value();
                if (now - started >= required && context.claimHit(target.getUUID(), VestigeMainMod.location("dwell_heal"), 1))
                    target.heal((float) amount);
                yield true;
            }
            case "detect_magic" -> {
                double radius = SpellActions.value(action, context, "radius", 16);
                if (radius < 0 || radius > 64) yield false;
                boolean found = world.hasNativeMagic(caster, radius);
                context.setNumber(VestigeMainMod.location("magic_found"), found ? 1 : 0);
                report(caster, found ? "Magic is nearby." : "No recognized magic nearby.");
                yield true;
            }
            case "inspect_item" -> {
                boolean found = !caster.getMainHandItem().isEmpty() && caster.getMainHandItem().isEnchanted();
                context.setNumber(VestigeMainMod.location("magic_found"), found ? 1 : 0);
                report(caster, caster.getMainHandItem().isEmpty() ? "Hold an object to examine its aura."
                        : found ? "The held object is enchanted." : "No recognized enchantment on the held object.");
                yield true;
            }
            default -> false;
        };
    }
    private static void report(LivingEntity caster, String text) {
        if (caster instanceof ServerPlayer player) player.displayClientMessage(Component.literal(text), false);
    }
}
