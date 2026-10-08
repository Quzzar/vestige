package com.quzzar.vestige.apparatus;

import com.google.gson.JsonParser;
import com.quzzar.vestige.apparatus.recipeviewer.*;
import com.quzzar.vestige.magic.definition.SpellRarity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;
import java.io.InputStreamReader;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class RitualDisplaysTest {
    @Test void threadRecipesKeepTheSelectorOnStringAndOutOfConsumedIngredients() {
        var displays=RitualDisplays.threads();assertEquals(5,displays.size());
        assertEquals(5,displays.stream().map(RitualDisplays.Entry::id).distinct().count());
        for(int i=0;i<displays.size();i++) {
            var display=displays.get(i);var type=MagicalThreadRecipe.types().get(i);
            assertTrue(display.shapeless());assertFalse(display.concealed());assertEquals(4,display.capacity());
            assertEquals(type.item(),display.output().getItem());assertEquals(1,display.output().getCount());
            assertEquals(List.of(0,2,4),display.offerings().stream().map(RitualDisplays.Offering::seat).toList());
            assertEquals(MagicalThreadRecipe.ingredients().stream().map(net.minecraft.core.registries.BuiltInRegistries.ITEM::getKey).toList(),
                    display.offerings().stream().map(o -> o.ingredient().items().getFirst()).toList());
            assertEquals(List.of(new RitualDisplays.Imbuement(0,type.material())),display.imbuements());
            assertFalse(display.imbuements().getFirst().stack().isEmpty());
        }
        var original=new RitualDisplayPayload(0,true,false,displays);
        var buffer=new RegistryFriendlyByteBuf(Unpooled.buffer(),RegistryAccess.EMPTY);
        try{RitualDisplayPayload.CODEC.encode(buffer,original);assertEquals(original,RitualDisplayPayload.CODEC.decode(buffer));}
        finally{buffer.release();}
    }
    @Test void imbuementsCannotAttachToEmptySeatsOrLeakFromConcealedRecipes() {
        var display=RitualDisplays.threads().getFirst();var material=display.imbuements().getFirst();
        assertThrows(IllegalArgumentException.class,() -> new RitualDisplays.Entry(display.id(),display.spell(),false,display.rarity(),4,
                display.offerings(),List.of(new RitualDisplays.Imbuement(6,material.material()))));
        assertThrows(IllegalArgumentException.class,() -> new RitualDisplays.Entry(display.id(),display.spell(),false,display.rarity(),4,
                display.offerings(),List.of(material,material)));
        assertThrows(IllegalArgumentException.class,() -> new RitualDisplays.Entry(display.id(),Optional.of(display.id()),false,display.rarity(),4,
                List.of(),List.of(material)));
    }
    @Test void frameFitsTheOriginalRightAndBottomEdgesAndReservesTheOfferingHover() {
        assertTrue(ImbuementFrame.contains(970,627));assertFalse(ImbuementFrame.contains(991,627));
        assertTrue(ImbuementFrame.contains(627,940));assertFalse(ImbuementFrame.contains(627,959));
        assertFalse(ImbuementFrame.contains(627,627));assertTrue(ImbuementFrame.contains(627,315));
        for(int reserved:List.of(16,18)) {
            var boxes=ImbuementFrame.hitBoxes(reserved);assertFalse(boxes.isEmpty());
            for(var box:boxes)for(int x=box.x();x<box.x()+box.width();x++) {
                assertTrue(ImbuementFrame.contains((x+.5)*1254/34,(box.y()+.5)*1254/34));
                assertFalse(Math.abs(x+.5-17)<reserved/2. && Math.abs(box.y()+.5-17)<reserved/2.);
            }
        }
    }
    @Test void everySpellDisplayPreservesOutputAndCapacityWithoutDisclosingAnyIngredients()throws Exception {
        for(var id:RitualCatalog.builtinIds()) {
            try(var reader=new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/data/vestige/ritual_recipes/"+id.getPath()+".json")))) {
                var recipe=RitualRecipe.read(id,JsonParser.parseReader(reader).getAsJsonObject());var display=RitualDisplays.spell(recipe);
                assertEquals(recipe.circle(),display.capacity());assertEquals(Optional.of(id),display.spell());assertFalse(display.shapeless());
                assertTrue(display.concealed());assertTrue(display.offerings().isEmpty(),id.toString());
                assertTrue(display.imbuements().isEmpty(),id.toString());
                assertEquals(SpellRarity.COMMON,display.rarity());
                assertEquals(recipe.circle(),display.seats().size());
                assertEquals(id,ScrollItems.scroll(display.output()).orElseThrow().spell());
                // Concealment leaves the actual recipe intact for the in-world puzzle.
                assertEquals(1,recipe.parts().stream().filter(p -> p.ingredient().items().contains(ResourceLocation.withDefaultNamespace("paper"))).count());
            }
        }
    }
    @Test void craftedRecipesRevealExactOfferingsIndependentlyOfIdentification() throws Exception {
        for(var id:RitualCatalog.builtinIds()) {
            try(var reader=new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/data/vestige/ritual_recipes/"+id.getPath()+".json")))) {
                var recipe=RitualRecipe.read(id,JsonParser.parseReader(reader).getAsJsonObject());
                SpellRarity rarity;
                try(var spellReader=new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/data/vestige/runtime_spells/"+id.getPath()+".json")))) {
                    rarity=SpellRarity.fromId(JsonParser.parseReader(spellReader).getAsJsonObject().get("rarity").getAsString()).orElseThrow();
                }
                var crafted=RitualDisplays.spell(recipe,true,false,rarity);
                assertFalse(crafted.concealed());assertFalse(crafted.identified());assertFalse(crafted.shapeless());
                assertEquals(SpellRarity.COMMON,crafted.rarity(),"Crafting must not disclose rarity: "+id);
                assertEquals(recipe.parts().size(),crafted.offerings().size());
                for(int i=0;i<recipe.parts().size();i++) {
                    var part=recipe.parts().get(i);var offering=crafted.offerings().get(i);
                    assertEquals(recipe.circle()==4 ? part.seat()*2 : part.seat(),offering.seat());
                    assertEquals(part.ingredient(),offering.ingredient());
                }
                var identified=RitualDisplays.spell(recipe,false,true,rarity);
                assertTrue(identified.identified());assertTrue(identified.concealed());assertTrue(identified.offerings().isEmpty());
                assertEquals(rarity,identified.rarity());
                var original=new RitualDisplayPayload(0,true,true,List.of(crafted,identified));
                var buffer=new RegistryFriendlyByteBuf(Unpooled.buffer(),RegistryAccess.EMPTY);
                try {RitualDisplayPayload.CODEC.encode(buffer,original);assertEquals(original,RitualDisplayPayload.CODEC.decode(buffer));}
                finally {buffer.release();}
            }
        }
    }
    @Test void shardRequiresEightPlinthsWithSixSeparateOfferingsAndNoInventedSignature() {
        var display=RitualDisplays.shard();assertTrue(display.shapeless());assertFalse(display.concealed());assertEquals(8,display.capacity());assertEquals(8,display.seats().size());
        assertEquals(6,display.offerings().size());assertEquals(6,display.offerings().stream().map(RitualDisplays.Offering::seat).distinct().count());
        assertEquals(AttunementShardItem.ingredients(),display.offerings().stream().map(o -> o.ingredient().items().getFirst()).toList());
        assertEquals(2,display.offerings().stream().filter(o -> o.ingredient().items().contains(ResourceLocation.withDefaultNamespace("amethyst_shard"))).count());
        assertTrue(AttunementShardItem.signature(display.output()).isEmpty());
    }
    @Test void recipeLookupsDistinguishSpellsAndTraitsButIgnoreCraftedShapingAndAttunementKeys() {
        var fireball=ResourceLocation.fromNamespaceAndPath("vestige","fireball");var heal=ResourceLocation.fromNamespaceAndPath("vestige","heal");
        var plain=ScrollItems.scroll(fireball);var shaped=ScrollItems.shapedScroll(fireball,new LeylineShaping.Modifiers(1.1,1.2,1,1.1),List.of(new Spellshaping.Selection(ResourceLocation.fromNamespaceAndPath("vestige","reaching"),1)));
        assertEquals(RitualDisplays.subtype(plain),RitualDisplays.subtype(shaped));assertNotEquals(RitualDisplays.subtype(plain),RitualDisplays.subtype(ScrollItems.scroll(heal)));
        assertNotEquals(RitualDisplays.subtype(ScrollItems.fragment(fireball)),RitualDisplays.subtype(ScrollItems.fragment(heal)));
        assertEquals("",RitualDisplays.subtype(RitualDisplays.shard().output()));
    }
    @Test void packetBoundsRejectOversizedPagesAndInvalidSeats() {
        assertThrows(IllegalArgumentException.class,() -> new RitualDisplayPayload(128,true,false,List.of()));
        assertThrows(IllegalArgumentException.class,() -> new RitualDisplayPayload(0,true,false,Collections.nCopies(33,RitualDisplays.shard())));
        assertThrows(IllegalArgumentException.class,() -> new RitualDisplays.Entry(ResourceLocation.withDefaultNamespace("invalid"),Optional.empty(),false,SpellRarity.COMMON,4,RitualDisplays.shard().offerings()));
        assertThrows(IllegalArgumentException.class,() -> new RitualDisplays.Entry(ResourceLocation.withDefaultNamespace("invalid"),Optional.empty(),false,SpellRarity.COMMON,4,List.of()));
        assertThrows(IllegalArgumentException.class,() -> new RitualDisplays.Entry(ResourceLocation.withDefaultNamespace("invalid"),Optional.of(ResourceLocation.withDefaultNamespace("hidden")),false,SpellRarity.COMMON,4,RitualDisplays.shard().offerings()));
    }
    @Test void wireRoundTripPreservesIngredientAlternativesAndRejectsOversizedDecodedPages() {
        var concealed=new RitualDisplays.Entry(ResourceLocation.fromNamespaceAndPath("vestige","ritual/vestige/fireball"),Optional.of(ResourceLocation.fromNamespaceAndPath("vestige","fireball")),false,SpellRarity.COMMON,4,List.of());
        var original=new RitualDisplayPayload(0,true,false,List.of(concealed,RitualDisplays.shard()));
        var buffer=new RegistryFriendlyByteBuf(Unpooled.buffer(),RegistryAccess.EMPTY);
        try {
            RitualDisplayPayload.CODEC.encode(buffer,original);assertEquals(original,RitualDisplayPayload.CODEC.decode(buffer));
            buffer.clear();buffer.writeVarInt(0);buffer.writeBoolean(true);buffer.writeBoolean(false);buffer.writeVarInt(33);
            assertThrows(IllegalArgumentException.class,() -> RitualDisplayPayload.CODEC.decode(buffer));
        } finally {buffer.release();}
    }
    @Test void unknownDisplaysCannotDiscloseRarityAndWireRejectsUnsupportedRarity() {
        var id=ResourceLocation.fromNamespaceAndPath("vestige","fireball");
        assertThrows(IllegalArgumentException.class,() -> new RitualDisplays.Entry(id,Optional.of(id),false,SpellRarity.MYTHIC,4,List.of()));
        for(String rarity:List.of("legendary","mythic")) {
            var buffer=new RegistryFriendlyByteBuf(Unpooled.buffer(),RegistryAccess.EMPTY);
            try {
                buffer.writeVarInt(0);buffer.writeBoolean(true);buffer.writeBoolean(false);buffer.writeVarInt(1);
                buffer.writeUtf(id.toString(),256);buffer.writeBoolean(true);buffer.writeUtf(id.toString(),256);
                buffer.writeBoolean(false);buffer.writeUtf(rarity,16);buffer.writeByte(4);buffer.writeVarInt(0);buffer.writeVarInt(0);
                assertThrows(IllegalArgumentException.class,() -> RitualDisplayPayload.CODEC.decode(buffer));
            } finally {buffer.release();}
        }
    }
}
