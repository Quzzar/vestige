package com.quzzar.vestige.magic.world;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.*;
import com.quzzar.vestige.magic.presentation.ManaReadiness;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.gametest.*;
import java.util.List;

@GameTestHolder(VestigeMainMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ItemManaTest {
    @GameTest(template = "empty_3x3x3", batch = "item_mana")
    public static void pricesFollowExactShapingCoreTipAndStaffSelection(GameTestHelper h) {
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        var fire = ScrollItems.scroll(VestigeMainMod.location("fireball"));
        var arrow = ScrollItems.scroll(VestigeMainMod.location("force_arrow"));
        var shaped = ScrollItems.shapedScroll(VestigeMainMod.location("fireball"), new LeylineShaping.Modifiers(1, 1, 1, 2));
        var wand = WandData.create(WandComponents.Base.STICK, MagicalThreadRecipe.Type.ENSORCELLED, fire);
        var tipped = WandData.create(WandComponents.Base.STICK, MagicalThreadRecipe.Type.ENSORCELLED, java.util.Optional.of(WandTips.Tip.AMETHYST), fire);
        var staff = StaffData.bind(StaffData.create(VestigeMainMod.location("evocation")), fire, NativeMagic.spells().spells().get(VestigeMainMod.location("fireball")));
        staff = StaffData.bind(StaffData.select(staff, 1), arrow, NativeMagic.spells().spells().get(VestigeMainMod.location("force_arrow")));
        var items = List.of(fire, arrow, shaped, wand, staff, tipped);
        for (int i = 0; i < items.size(); i++) player.getInventory().setItem(i, items.get(i));
        var prices = NativeItemMana.snapshot(player);
        h.assertTrue(prices.get(ManaItemCosts.source(fire).orElseThrow().key()) == 28, "Wrong base mana price");
        h.assertTrue(prices.get(ManaItemCosts.source(shaped).orElseThrow().key()) == 56, "Leyline cost was not applied");
        h.assertTrue(prices.get(ManaItemCosts.source(wand).orElseThrow().key()) == 24, "Core economy did not round through the casting compiler");
        h.assertTrue(prices.get(ManaItemCosts.source(tipped).orElseThrow().key()) == 27, "Core and tip costs did not compose before final rounding");
        h.assertTrue(prices.get(ManaItemCosts.source(staff).orElseThrow().key()) == 14, "Staff used an inactive slot");
        var selected = StaffData.select(staff, 0);
        h.assertTrue(prices.get(ManaItemCosts.source(selected).orElseThrow().key()) == 28, "Staff selection did not update its price identity");
        var original = ManaItemCosts.source(wand).orElseThrow().key(); wand.setDamageValue(5);
        h.assertTrue(original.equals(ManaItemCosts.source(wand).orElseThrow().key()), "Durability altered mana price identity");
        h.assertTrue(ManaItemCosts.source(fire.copyWithCount(16)).orElseThrow().key().equals(ManaItemCosts.source(fire).orElseThrow().key()), "Stack quantity altered mana price identity");
        h.assertTrue(ManaReadiness.shortage(14, 14) == 0 && ManaReadiness.shortage(14, 28) == .5f, "Prices did not produce different affordability overlays");
        h.succeed();
    }
    @GameTest(template = "empty_3x3x3", batch = "item_mana")
    public static void menuCursorDevicesAndRemovedItemsRefreshWithoutClientCatalogState(GameTestHelper h) {
        var player = h.makeMockPlayer(GameType.SURVIVAL);
        var fire = ScrollItems.scroll(VestigeMainMod.location("fireball"));
        var arrow = ScrollItems.scroll(VestigeMainMod.location("force_arrow"));
        var eye = HomeboundEyeItem.bound("a".repeat(64), Level.OVERWORLD, BlockPos.ZERO, HomeboundEyeItem.Payment.MANA);
        var contents = new SimpleContainer(27); contents.setItem(0, fire);
        player.containerMenu = ChestMenu.threeRows(1, player.getInventory(), contents); player.containerMenu.setCarried(arrow);
        player.getInventory().setItem(0, eye);
        var prices = NativeItemMana.snapshot(player);
        h.assertTrue(prices.size() == 3 && prices.get(ManaItemCosts.source(eye).orElseThrow().key()) == 30, "Menu/cursor/device prices were missing");
        contents.clearContent(); player.containerMenu.setCarried(net.minecraft.world.item.ItemStack.EMPTY); player.getInventory().clearContent();
        h.assertTrue(NativeItemMana.snapshot(player).isEmpty(), "Removed items retained stale prices");
        player.getInventory().setItem(0, ScrollItems.scroll(VestigeMainMod.location("not_a_loaded_spell")));
        h.assertTrue(NativeItemMana.snapshot(player).isEmpty(), "Unknown server spell was given a client-derived price");
        h.succeed();
    }
}
