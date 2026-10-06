package com.quzzar.vestige.apparatus;

import com.google.gson.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class LeylineShapingTest {
    private ResourceLocation id(String name) { return ResourceLocation.fromNamespaceAndPath("vestige",name); }
    @Test void nativeMathMatchesAllCanonicalFixtures() throws IOException {
        try (var stream=getClass().getResourceAsStream("/leyline-parity.json")) {
            var cases=JsonParser.parseReader(new InputStreamReader(Objects.requireNonNull(stream),StandardCharsets.UTF_8)).getAsJsonArray();
            assertTrue(cases.size()>400);
            for (var entry:cases) {
                var fixture=entry.getAsJsonObject(); var l=fixture.getAsJsonObject("layout");
                var geometry=new LeylineShaping.Geometry(l.get("slots").getAsInt(),shape(l,"innerShape"),l.get("inner").getAsInt(),l.get("innerHeight").getAsInt(),
                        l.has("outerShape") ? shape(l,"outerShape") : LeylineShaping.Shape.CROSS,l.has("outer") ? l.get("outer").getAsInt() : 0,l.has("outerStep") ? l.get("outerStep").getAsInt() : 0);
                var traits=new ArrayList<ResourceLocation>();fixture.getAsJsonArray("traits").forEach(t->traits.add(id(t.getAsString())));
                var actual=LeylineShaping.resolve(geometry,traits);var expected=fixture.getAsJsonObject("modifiers");
                assertArrayEquals(new double[]{expected.get("amplify").getAsDouble(),expected.get("range").getAsDouble(),expected.get("area").getAsDouble(),expected.get("cost").getAsDouble()},
                        new double[]{actual.amplify(),actual.range(),actual.area(),actual.cost()},1e-12,l.toString());
            }
        }
    }
    @Test void inactiveOuterAndUnknownTraitsHaveNoResponseAndBothHeightStepsRemainIndependent() {
        var a=new LeylineShaping.Geometry(4,LeylineShaping.Shape.CROSS,2,1,LeylineShaping.Shape.DIAGONAL,99,99);
        var b=a.innerOnly();assertEquals(a,b);
        var traits=List.of(id("fire"),id("evocation"));
        assertEquals(LeylineShaping.resolve(a,traits),LeylineShaping.resolve(b,List.of(id("fire"),id("fire"),id("evocation"),id("amplify"),id("volatile"),id("unknown"),ResourceLocation.parse("addon:fire"))));
        var upThenDown=new LeylineShaping.Geometry(8,LeylineShaping.Shape.CROSS,2,2,LeylineShaping.Shape.CROSS,4,-2);
        var flat=new LeylineShaping.Geometry(8,LeylineShaping.Shape.CROSS,2,0,LeylineShaping.Shape.CROSS,4,0);
        assertNotEquals(LeylineShaping.resolve(upThenDown,traits),LeylineShaping.resolve(flat,traits));
        assertEquals(2,upThenDown.d2());
        assertThrows(IllegalArgumentException.class,()->new LeylineShaping.Geometry(8,LeylineShaping.Shape.CROSS,8,0,LeylineShaping.Shape.CROSS,17,0));
    }
    @Test void recipeSeatsComeFromTheSelectedGeometryRatherThanAFixedLayout() {
        var geometry=new LeylineShaping.Geometry(8,LeylineShaping.Shape.DIAGONAL,3,2,LeylineShaping.Shape.CROSS,8,-4);
        assertEquals(new BlockPos(3,2,-3),geometry.offset(0));
        assertEquals(new BlockPos(0,-2,-8),geometry.offset(1));
        assertEquals(new BlockPos(-3,2,3),geometry.offset(4));
        assertEquals(new BlockPos(-8,-2,0),geometry.offset(7));
        assertThrows(IllegalArgumentException.class,()->geometry.innerOnly().offset(1));
        assertThrows(IllegalArgumentException.class,()->geometry.offset(8));
    }
    private static LeylineShaping.Shape shape(JsonObject l,String key) { return LeylineShaping.Shape.valueOf(l.get(key).getAsString().toUpperCase(Locale.ROOT)); }
}
