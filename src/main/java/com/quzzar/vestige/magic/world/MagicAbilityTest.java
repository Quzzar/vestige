package com.quzzar.vestige.magic.world;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.gametest.SurvivalTestPlayer;
import com.quzzar.vestige.magic.definition.*;
import com.quzzar.vestige.magic.effect.SpellEffects;
import com.quzzar.vestige.magic.expression.SpellValue;
import com.quzzar.vestige.magic.runtime.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.gametest.*;

import java.util.*;

/** Executes shared item ability formulas against native damage and payment events; registers no equipment. */
@GameTestHolder(VestigeMainMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MagicAbilityTest {
    private MagicAbilityTest() { }
    private static ResourceLocation id(String name) { return VestigeMainMod.location(name); }
    private static SpellValue variable(String name) { return new SpellValue.Variable(id(name)); }
    private static ItemAbilityDefinition ward() {
        var binding = new SpellEffects.Binding(id("test/ward"),
                List.of(new SpellTrigger(id("test/ward_hit"), SpellTriggerTypes.DAMAGE_CALCULATING, List.of())),
                List.of(new SpellEffects.Action(id("reduce_pending_damage"), Map.of("amount", variable("protection")), Map.of())),
                80, 1, Optional.of(variable("ward_ticks")));
        return new ItemAbilityDefinition(id("test/item_ward"), new TraitProfile(Map.of(id("time"), 1d, id("amplify"), 1d)),
                Map.of(id("ward_ticks"), new SpellValue.Product(List.of(new SpellValue.Constant(80), new SpellValue.Trait(id("time")))),
                        id("protection"), new SpellValue.Product(List.of(new SpellValue.Constant(2), new SpellValue.Trait(id("amplify"))))),
                List.of(), List.of(new SpellTrigger(id("test/use"), SpellTriggerTypes.INTERACT, List.of())),
                List.of(new SpellEffects.InstallBinding(binding, TargetSpec.self())), ItemAbilityDefinition.Activation.REACTIVE);
    }
    private static SpellEvent use(UUID actor) { return SpellEvent.of(SpellTriggerTypes.INTERACT, actor, null); }

    @GameTest(template = "empty_3x3x3", batch = "item_abilities", timeoutTicks = 115)
    public static void aTraitBoostExtendsTheActualWardAndItsExpiryDoesNotShortenTheWard(GameTestHelper h) {
        var player = SurvivalTestPlayer.create(h);
        var session = NativeMagic.session(h.getLevel().getServer()); session.world().registerActor(player);
        var ward = ward();
        var boost = new ItemAbilityDefinition(id("test/time_boost"), TraitProfile.empty(), Map.of(), List.of(), ward.triggers(),
                List.of(new SpellEffects.GrantTraits(id("test/time"), List.of(new TraitModifier(id("time"), TraitModifier.Operation.MULTIPLY, 1.25)),
                        new SpellValue.Constant(2), TargetSpec.self())), ItemAbilityDefinition.Activation.REACTIVE);
        session.runtime().activate(boost, use(player.getUUID()), List.of(), CastReservation.NONE);
        var cast = session.runtime().activate(ward, use(player.getUUID()), List.of(), CastReservation.NONE);
        h.assertTrue(cast.paymentCommitted() && cast.resolution().variable(id("ward_ticks")) == 100, "25% Time did not produce a five-second ward");
        h.runAfterDelay(5, () -> h.assertTrue(session.runtime().resolve(ward, player.getUUID(), List.of()).variable(id("ward_ticks")) == 80,
                "Expired boost remained in future activations"));
        h.runAfterDelay(90, () -> {
            try (player) {
                player.hurt(player.damageSources().generic(), 10);
                h.assertTrue(Math.abs(player.getHealth() - 12) < .001, "Five-second ward failed after the boost expired: " + player.getHealth());
                session.runtime().dispelActor(player.getUUID()); h.succeed();
            }
        });
    }

    @GameTest(template = "empty_3x3x3", batch = "item_abilities", timeoutTicks = 115)
    public static void anExpiredVariableWardDoesNotMitigateNativeDamage(GameTestHelper h) {
        var player = SurvivalTestPlayer.create(h);
        var session = NativeMagic.session(h.getLevel().getServer()); session.world().registerActor(player);
        session.runtime().activate(ward(), use(player.getUUID()), List.of(new TraitModifier(id("time"), TraitModifier.Operation.MULTIPLY, 1.25)), CastReservation.NONE);
        h.runAfterDelay(102, () -> {
            try (player) {
                player.hurt(player.damageSources().generic(), 10);
                h.assertTrue(Math.abs(player.getHealth() - 10) < .001, "Expired ward prevented damage");
                session.runtime().dispelActor(player.getUUID()); h.succeed();
            }
        });
    }

    @GameTest(template = "empty_3x3x3", batch = "item_abilities", timeoutTicks = 95)
    public static void aReactiveItemProtectsDuringPreparationWithoutSpendingOrInterruptingTheSpell(GameTestHelper h) {
        var player = SurvivalTestPlayer.create(h);
        var session = NativeMagic.session(h.getLevel().getServer()); session.world().registerActor(player);
        NativeMana.set(player, 100);
        var ward = ward();
        var prepared = new SpellDefinition(id("test/prepared"), Set.of(Tradition.ARCANE), TraitProfile.empty(),
                List.of(new SpellCost.Time(4), new SpellCost.Mana(5)), ward.triggers(),
                List.of(new SpellEffects.Action(id("heal"), Map.of("amount", new SpellValue.Constant(1)), Map.of())));
        // Joining server players initially reject ordinary damage; exercise a real hit after that grace period.
        h.runAfterDelay(65, () -> {
            var spell = session.runtime().cast(prepared, use(player.getUUID()), List.of(), true);
            var item = session.runtime().activate(ward, use(player.getUUID()), List.of(), CastReservation.NONE);
            h.assertTrue(item.paymentCommitted() && spell.status() == SpellRuntime.Status.CHARGING && NativeMana.amount(player) == 100,
                    "Reactive item interfered with prepared spell payment");
            player.hurt(player.damageSources().generic(), 10);
            h.assertTrue(Math.abs(player.getHealth() - 12) < .001, "Reactive ward did not protect during preparation: " + player.getHealth());
            h.runAfterDelay(6, () -> {
                try (player) {
                    h.assertTrue(spell.paymentCommitted() && spell.status() == SpellRuntime.Status.COMPLETED,
                            "Reactive protection interrupted preparation");
                    h.assertTrue(NativeMana.amount(player) == 95 && Math.abs(player.getHealth() - 13) < .001,
                            "Prepared spell failed to pay once and resolve its healing");
                    session.runtime().dispelActor(player.getUUID()); h.succeed();
                }
            });
        });
    }
}
