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
    @Test void everySpellDisplayPreservesOutputAndCapacityWithoutDisclosingAnyIngredients()throws Exception {
        for(var id:RitualCatalog.builtinIds()) {
            try(var reader=new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/data/vestige/ritual_recipes/"+id.getPath()+".json")))) {
                var recipe=RitualRecipe.read(id,JsonParser.parseReader(reader).getAsJsonObject());var display=RitualDisplays.spell(recipe);
                assertEquals(recipe.circle(),display.capacity());assertEquals(Optional.of(id),display.spell());assertFalse(display.shapeless());
                assertTrue(display.concealed());assertTrue(display.offerings().isEmpty(),id.toString());
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
                buffer.writeBoolean(false);buffer.writeUtf(rarity,16);buffer.writeByte(4);buffer.writeVarInt(0);
                assertThrows(IllegalArgumentException.class,() -> RitualDisplayPayload.CODEC.decode(buffer));
            } finally {buffer.release();}
        }
    }
}
