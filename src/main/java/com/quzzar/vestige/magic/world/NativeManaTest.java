package com.quzzar.vestige.magic.world;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.definition.SpellTriggerTypes;
import com.quzzar.vestige.magic.runtime.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.gametest.*;
import java.util.List;

@GameTestHolder(VestigeMainMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class NativeManaTest {
    @GameTest(template = "empty_3x3x3", batch = "player_mana")
    public static void firstSpawnDeathAndEndReturnHaveTheRequiredManaLifecycle(GameTestHelper h) {
        var original = h.makeMockPlayer(GameType.SURVIVAL); NativeMana.initialize(original);
        h.assertTrue(NativeMana.amount(original) == 100, "New player did not start full");
        NativeMana.spend(original, 30); NativeMana.login(new PlayerEvent.PlayerLoggedInEvent(original));
        h.assertTrue(NativeMana.amount(original) == 70, "Relog refilled mana");
        var dead = h.makeMockPlayer(GameType.SURVIVAL); NativeMana.clonePlayer(new PlayerEvent.Clone(dead, original, true));
        h.assertTrue(NativeMana.amount(dead) == 100, "Death clone did not refill");
        var endReturn = h.makeMockPlayer(GameType.SURVIVAL); NativeMana.clonePlayer(new PlayerEvent.Clone(endReturn, original, false));
        h.assertTrue(NativeMana.amount(endReturn) == 70, "Nondeath clone lost mana"); h.succeed();
    }
    @GameTest(template = "empty_3x3x3", batch = "player_mana")
    public static void recoveryWaitsFiveSecondsRestartsOnlyForSuccessfulPaymentAndStopsAtFull(GameTestHelper h) {
        var player = h.makeMockPlayer(GameType.SURVIVAL); NativeMana.set(player, 0);
        for (int i = 0; i < 100; i++) NativeMana.recover(player);
        h.assertTrue(NativeMana.amount(player) == 0, "Recovered during the delay");
        for (int i = 0; i < 20; i++) NativeMana.recover(player);
        h.assertTrue(Math.abs(NativeMana.amount(player) - 2) < 1e-7, "Recovery was not two mana per second");
        h.assertTrue(!NativeMana.spend(player, 3), "Unaffordable payment succeeded"); NativeMana.recover(player);
        h.assertTrue(NativeMana.amount(player) > 2, "Failed payment restarted the delay");
        h.assertTrue(NativeMana.spend(player, 1), "Affordable payment failed"); double remaining = NativeMana.amount(player);
        for (int i = 0; i < 100; i++) NativeMana.recover(player);
        h.assertTrue(NativeMana.amount(player) == remaining, "Successful payment did not restart delay");
        for (int i = 0; i < 1200; i++) NativeMana.recover(player);
        h.assertTrue(NativeMana.amount(player) == 100, "Recovery did not cap exactly at full"); h.succeed();
    }
    @GameTest(template = "empty_3x3x3", batch = "player_mana")
    public static void realPaidSpellUsesThePlayerPoolAndStartsRecoveryDelay(GameTestHelper h) {
        try (var player = com.quzzar.vestige.gametest.SurvivalTestPlayer.create(h)) {
        var session = NativeMagic.session(h.getLevel().getServer()); session.world().registerActor(player);
        NativeMana.initialize(player);
        var spell = NativeMagic.spells().spells().get(VestigeMainMod.location("force_arrow"));
        var event = SpellEvent.of(SpellTriggerTypes.INTERACT, player.getUUID(), null);
        var cast = session.runtime().cast(spell, event, List.of(), true);
        h.assertTrue(cast.failure().isEmpty() && NativeMana.amount(player) == 86, "Paid spell ignored the 100-mana pool");
        h.assertTrue(session.runtime().cast(spell, event, List.of(), true).status() == SpellRuntime.Status.COOLDOWN
                && NativeMana.amount(player) == 86, "Rejected repeat spent mana");
        for (int i = 0; i < 100; i++) NativeMana.recover(player);
        h.assertTrue(NativeMana.amount(player) == 86, "Spell payment did not start delay");
        NativeMana.recover(player); h.assertTrue(NativeMana.amount(player) > 86, "Mana never recovered");
        session.runtime().interruptActor(player.getUUID()); session.runtime().dispelActor(player.getUUID()); h.succeed();
        }
    }
}
