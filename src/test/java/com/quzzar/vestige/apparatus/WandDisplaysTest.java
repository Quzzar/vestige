package com.quzzar.vestige.apparatus;

import com.google.gson.JsonParser;
import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.recipeviewer.*;
import com.quzzar.vestige.magic.data.SpellJson;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;
import java.io.InputStreamReader;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class WandDisplaysTest {
    private WandDisplays.Source source()throws Exception {
        try(var reader=new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/data/vestige/runtime_spells/force_arrow.json")))){
            var spell=SpellJson.read(VestigeMainMod.location("force_arrow"),JsonParser.parseReader(reader).getAsJsonObject());
            var scroll=ScrollItems.shapedScroll(spell.id(),new LeylineShaping.Modifiers(1.1,1.2,.9,.8),List.of(new Spellshaping.Selection(VestigeMainMod.location("reaching"),2)));
            return WandDisplays.compile(spell,scroll);
        }
    }
    @Test void eachDisplayedCombinationIsTheRealRelativeRecipeWithThreeExactScrolls()throws Exception {
        var source=source();assertTrue(source.options().size()>8);
        for(var option:source.options())for(var base:new WandDisplays.Entry(source,option).bases()){
            var entry=new WandDisplays.Entry(source,option);var inputs=new ArrayList<>(Collections.nCopies(8,net.minecraft.world.item.ItemStack.EMPTY));
            for(int seat=0;seat<8;seat++)inputs.set(seat,entry.offering(seat,base));
            var match=WandRecipe.match(inputs).orElseThrow();var output=entry.output(base);var binding=WandData.binding(output).orElseThrow();
            assertEquals(base,match.base());assertEquals(option.thread(),match.thread());assertEquals(option.tip(),match.tip());assertEquals(binding.scroll(),ScrollItems.scroll(source.scroll()).orElseThrow());
            assertEquals(option.tip().isPresent()?6:5,entry.diagram().offerings().size());assertEquals(8,entry.diagram().capacity());assertTrue(entry.diagram().imbuements().isEmpty());
            var data=output.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.EMPTY);
            assertSame(WandDisplays.identity(data),WandDisplays.identity(data));
            output.setDamageValue(3);assertEquals(WandDisplays.identity(data),WandDisplays.identity(output.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.EMPTY)));
            assertEquals(WandDisplays.subtype(entry.output(base)),WandDisplays.subtype(output));
        }
    }
    @Test void compactPayloadRoundTripsTheExactShapedSourceAndApprovedOptions()throws Exception {
        var source=source();var original=new WandDisplayPayload(0,true,true,List.of(source));var buffer=new RegistryFriendlyByteBuf(Unpooled.buffer(),RegistryAccess.EMPTY);
        try{WandDisplayPayload.CODEC.encode(buffer,original);assertTrue(buffer.writerIndex()<2000);assertEquals(original,WandDisplayPayload.CODEC.decode(buffer));}finally{buffer.release();}
        var copy=source.magic();copy.putString("vestige_spell","vestige:fireball");assertNotEquals(copy,source.magic());
        assertThrows(IllegalArgumentException.class,() -> new WandDisplayPayload(128,true,false,List.of(source)));
    }
}
