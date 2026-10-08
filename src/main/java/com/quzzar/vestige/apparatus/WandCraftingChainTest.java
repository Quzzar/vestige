package com.quzzar.vestige.apparatus;

import com.mojang.authlib.GameProfile;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.magic.world.NativeMana;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import java.util.List;
import java.util.UUID;

/** Carries a real dropped thread through binding and a paid wand cast. */
@GameTestHolder(VestigeMainMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class WandCraftingChainTest {
    private static final BlockPos CENTER = new BlockPos(4, 1, 4);
    private static final LeylineShaping.Geometry GEOMETRY = new LeylineShaping.Geometry(8,
            LeylineShaping.Shape.CROSS, 2, 0, LeylineShaping.Shape.DIAGONAL, 3, 0);

    @GameTest(template = "empty_9x3x9", batch = "wand_chain_plain", timeoutTicks = 190)
    public static void imbuedStringBecomesAnUntippedCastingWand(GameTestHelper h) { chain(h, false); }

    @GameTest(template = "empty_9x3x9", batch = "wand_chain_shaped", timeoutTicks = 190)
    public static void imbuedStringBecomesATippedShapedCastingWand(GameTestHelper h) { chain(h, true); }

    private static void chain(GameTestHelper h, boolean tipped) {
        h.setBlock(CENTER, ApparatusBlocks.SPELLSTONE.get());
        for (int seat = 0; seat < 8; seat += 2)
            h.setBlock(CENTER.offset(GEOMETRY.offset(seat)), ApparatusBlocks.PLINTH.get());
        var center = (OfferingBlockEntity) h.getBlockEntity(CENTER);
        var inner = LeylineStructure.find(center, 4).getFirst();
        for (int i = 0; i < 3; i++)
            h.assertTrue(inner.stands().get(i * 2).insert(new ItemStack(MagicalThreadRecipe.ingredients().get(i))), "Thread offering rejected");
        h.assertTrue(inner.stands().get(0).installMaterial(new ItemStack(Items.IRON_BLOCK)), "String socket rejected Iron");
        var caster = new Player(h.getLevel(), h.absolutePos(new BlockPos(4, 1, 1)), 0,
                new GameProfile(UUID.randomUUID(), "quiet-wand-chain")) {
            @Override public boolean isSpectator() { return false; }
            @Override public boolean isCreative() { return false; }
            @Override public void displayClientMessage(net.minecraft.network.chat.Component text, boolean overlay) {
                throw new AssertionError("Crafting chain emitted gameplay text");
            }
            @Override public void sendSystemMessage(net.minecraft.network.chat.Component text) {
                throw new AssertionError("Crafting chain emitted gameplay text");
            }
        };
        caster.setPos(Vec3.atBottomCenterOf(h.absolutePos(new BlockPos(4, 1, 1))));
        NativeMana.set(caster, 100);
        var spell = VestigeMainMod.location("pf2_shield");
        SpellKnowledge.identify(caster, spell);
        var scroll = tipped ? ScrollItems.shapedScroll(spell, new LeylineShaping.Modifiers(1.1, 1.2, .9, 1.2), List.of())
                : ScrollItems.scroll(spell);
        var source = ScrollItems.scroll(scroll).orElseThrow();
        h.assertTrue(scroll.getMaxStackSize() == 16, "Source scrolls cannot stack to sixteen");
        h.assertTrue(RitualCrafting.activate(caster, center) == RitualCrafting.Outcome.CRAFTING, "Thread craft rejected");
        h.runAfterDelay(65, () -> {
            var thread = RitualTestOutput.take(center);
            h.assertTrue(thread.is(MagicalThreadRecipe.Type.CALLOUS.item()) && thread.hasFoil(), "Real shimmered thread drop missing");
            h.assertTrue(inner.items().stream().allMatch(ItemStack::isEmpty), "Thread craft left its offerings");
            for (int seat = 1; seat < 8; seat += 2)
                h.setBlock(CENTER.offset(GEOMETRY.offset(seat)), ApparatusBlocks.PLINTH.get());
            var layout = LeylineStructure.find(center, 8).getFirst();
            // A rotated binding uses the actual crafted thread, rather than a synthetic replacement.
            h.assertTrue(layout.stands().get(2).insert(new ItemStack(Items.STICK)), "Wand base rejected");
            h.assertTrue(layout.stands().get(3).insert(thread), "Crafted thread rejected by binding");
            for (int seat : List.of(4, 6, 0))
                h.assertTrue(layout.stands().get(seat).insert(scroll.copy()), "Exact source scroll rejected");
            if (tipped) h.assertTrue(layout.stands().get(5).insert(new ItemStack(Items.NETHERITE_INGOT)), "Tip rejected");
            h.assertTrue(RitualCrafting.activate(caster, center) == RitualCrafting.Outcome.CRAFTING, "Wand binding rejected");
        });
        h.runAfterDelay(130, () -> {
            var wand = RitualTestOutput.take(center);
            var binding = WandData.binding(wand).orElseThrow();
            h.assertTrue(binding.scroll().equals(source) && binding.thread() == MagicalThreadRecipe.Type.CALLOUS
                    && binding.tip().isPresent() == tipped, "Binding lost source shaping, thread or optional tip");
            h.assertTrue(center.displayedItem().isEmpty() && LeylineStructure.find(center, 8).getFirst().items().stream().allMatch(ItemStack::isEmpty),
                    "Binding left an offering or stored the output on the Spellstone");
            h.assertTrue(inner.stands().get(0).materialItem().is(Items.IRON_BLOCK), "Crafting consumed the imbuement");
            caster.setItemInHand(InteractionHand.MAIN_HAND, wand);
            h.assertTrue(WandCasting.cast(caster, InteractionHand.MAIN_HAND), "Newly crafted wand cannot cast");
            h.assertTrue(wand.getDamageValue() == 0 && NativeMana.amount(caster) == 100, "Preparation paid early");
        });
        h.runAfterDelay(175, () -> {
            h.assertTrue(caster.getMainHandItem().getDamageValue() == 1 && NativeMana.amount(caster) < 100,
                    "Crafted wand did not commit mana and deterministic wear");
            h.assertTrue(!SpellKnowledge.identified(caster, VestigeMainMod.location("fireball")), "Unrelated spell identified");
            h.succeed();
        });
    }
}
