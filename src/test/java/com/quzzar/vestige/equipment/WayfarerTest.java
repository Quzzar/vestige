package com.quzzar.vestige.equipment;

import com.google.gson.JsonParser;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.ScrollItems;
import com.quzzar.vestige.magic.data.SpellJson;
import com.quzzar.vestige.magic.definition.*;
import com.quzzar.vestige.magic.runtime.MagicResolution;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import org.junit.jupiter.api.Test;
import java.io.InputStreamReader;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class WayfarerTest {
    private static ItemAbilityDefinition ability() {
        try (var input = new InputStreamReader(Objects.requireNonNull(WayfarerTest.class.getResourceAsStream("/data/vestige/item_abilities/wayfarer.json")))) {
            return SpellJson.readAbility(WayfarerImbuements.ABILITY, JsonParser.parseReader(input).getAsJsonObject());
        } catch (Exception failure) { throw new AssertionError(failure); }
    }
    @Test void everyViewerVariantAndQuarterTurnProducesTheSameTrustedItem() {
        assertEquals(16, WayfarerDisplays.entries().size());
        for (var entry : WayfarerDisplays.entries()) {
            var offerings = new ArrayList<ItemStack>(Collections.nCopies(8, ItemStack.EMPTY));
            var materials = new ArrayList<ItemStack>(Collections.nCopies(8, ItemStack.EMPTY));
            for (var offering : entry.offerings()) offerings.set(offering.seat(), new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(offering.ingredient().items().getFirst())));
            for (var material : entry.imbuements()) materials.set(material.seat(), material.stack());
            for (int rotation = 0; rotation < 8; rotation += 2) {
                var o = new ArrayList<>(offerings); var m = new ArrayList<>(materials);
                Collections.rotate(o, rotation); Collections.rotate(m, rotation);
                assertTrue(ItemStack.matches(entry.output(), WayfarerRecipe.result(o, m).orElseThrow()));
            }
            var item = entry.output(); var variant = WayfarerImbuements.read(item).orElseThrow();
            assertEquals(variant.durability(), item.getMaxDamage());
            assertEquals(1, ((ArmorItem)item.getItem()).getDefense());
            assertEquals(variant.durability() / 4, variant.repair());
            assertEquals(ArmorItem.Type.BOOTS, ((ArmorItem)item.getItem()).getType());
        }
    }
    @Test void duplicateAndIncompatibleSocketsRejectBeforeMutatingInputs() {
        var offerings = new ArrayList<ItemStack>();
        for (int i = 0; i < 8; i++) offerings.add(new ItemStack(i % 2 == 0 ? Items.LEATHER : i == 1 || i == 5 ? ScrollItems.LACED_THREAD.get() : i == 3 ? Items.FEATHER : Items.RABBIT_FOOT));
        var materials = new ArrayList<ItemStack>(Collections.nCopies(8, ItemStack.EMPTY));
        materials.set(0, new ItemStack(Items.IRON_BLOCK)); materials.set(2, new ItemStack(Items.IRON_BLOCK));
        assertTrue(WayfarerRecipe.result(offerings, materials).isEmpty());
        materials.set(2, ItemStack.EMPTY); materials.set(3, new ItemStack(Items.COPPER_BLOCK));
        assertTrue(WayfarerRecipe.result(offerings, materials).isEmpty());
        materials.set(3, new ItemStack(Items.STONE)); assertTrue(WayfarerRecipe.result(offerings, materials).isPresent());
        Collections.swap(offerings, 1, 3); assertFalse(WayfarerRecipe.matches(offerings));
        assertTrue(offerings.stream().allMatch(item -> item.getCount() == 1));
    }
    @Test void storedSelectionsSurviveCopyAndNbtWhileUnknownOrExcessiveRulesFailClosed() throws Exception {
        var item = WayfarerImbuements.create(EnumSet.allOf(WayfarerImbuements.Choice.class)); item.setDamageValue(20);
        var copy = item.copy(); var tag = TagParser.parseTag(copy.get(DataComponents.CUSTOM_DATA).copyTag().toString());
        copy.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        assertEquals(WayfarerImbuements.read(item), WayfarerImbuements.read(copy)); assertEquals(20, copy.getDamageValue());
        tag.getCompound("vestige_item_imbuements").getList("adjustments", Tag.TAG_COMPOUND).getCompound(0).putInt("degree", 2);
        copy.set(DataComponents.CUSTOM_DATA, CustomData.of(tag)); assertTrue(WayfarerImbuements.read(copy).isEmpty());
        var plain = new ItemStack(MagicEquipment.WAYFARER.get()); assertTrue(WayfarerImbuements.read(plain).isPresent());
        plain.set(DataComponents.MAX_DAMAGE, 98); assertTrue(WayfarerImbuements.read(plain).isEmpty());
    }
    @Test void traitsAndTypedCostsComposeOnceAcrossAllVariantsWithoutPowerCaps() {
        var base = ability();
        for (int mask = 0; mask < 16; mask++) {
            var variant = new WayfarerImbuements.Variant(WayfarerDisplays.choices(mask));
            var modifiers = new ArrayList<>(variant.modifiers());
            modifiers.add(new TraitModifier(VestigeMainMod.location("motion"), TraitModifier.Operation.MULTIPLY, 4));
            modifiers.add(new TraitModifier(VestigeMainMod.location("amplify"), TraitModifier.Operation.MULTIPLY, 5));
            var resolved = MagicResolution.resolve(base, modifiers);
            assertEquals(variant.choices().contains(WayfarerImbuements.Choice.SWIFT) ? 1.2 : .8, resolved.variable(VestigeMainMod.location("movement_bonus")), 1e-9);
            assertEquals(20, resolved.variable(VestigeMainMod.location("landing_protection")));
            var costs = variant.shaping().costs(base.costs());
            assertEquals(variant.mana(base), costs.stream().filter(SpellCost.Mana.class::isInstance).map(SpellCost.Mana.class::cast).mapToDouble(SpellCost.Mana::amount).sum());
            assertEquals(variant.choices().contains(WayfarerImbuements.Choice.QUICKENED) ? 240 : 300,
                    costs.stream().filter(SpellCost.Cooldown.class::isInstance).map(SpellCost.Cooldown.class::cast).mapToInt(SpellCost.Cooldown::ticks).sum());
        }
        var all = new WayfarerImbuements.Variant(EnumSet.allOf(WayfarerImbuements.Choice.class));
        assertEquals(9, all.mana(base)); assertEquals(98, all.durability());
        var allItem = WayfarerImbuements.create(all.choices()); assertTrue(allItem.getHoverName().getString().startsWith("Unfaltering "));
    }
}
