package com.quzzar.vestige.magic.presentation;

import com.google.gson.JsonParser;
import com.quzzar.vestige.magic.data.SpellJson;
import com.quzzar.vestige.magic.effect.SpellCapabilities;
import com.quzzar.vestige.magic.expression.SpellValue;
import com.quzzar.vestige.magic.world.SpellEffectGallery;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class SpellVisualTest {
    @Test void castCatalogContainsExactlyTheCurrentNativeSpellsWithoutIllustrativePhases() throws Exception {
        try(var catalog=getClass().getResourceAsStream("/catalog.json")) {
            assertNotNull(catalog);
            var rows=JsonParser.parseReader(new java.io.InputStreamReader(catalog,java.nio.charset.StandardCharsets.UTF_8)).getAsJsonArray();
            java.util.Set<String> ids=new java.util.HashSet<>();
            for(var entry:rows) {
                var row=entry.getAsJsonObject();var id=ResourceLocation.parse(row.get("id").getAsString());
                assertTrue(ids.add(id.toString()));assertFalse(row.has("phases"));
                try(var source=getClass().getResourceAsStream("/data/vestige/runtime_spells/"+id.getPath()+".json")) {
                    assertNotNull(source);var definition=JsonParser.parseReader(new java.io.InputStreamReader(source,java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
                    assertEquals(definition.get("rarity").getAsString(),row.get("rarity").getAsString());
                    assertEquals(definition.get("costs"),row.get("costs"));
                }
            }
            assertEquals(214,ids.size());
        }
    }
    @Test void nativeGalleryKeepsIceFormationDimensionsAndSeparateStartCue() throws Exception {
        try(var stream=getClass().getResourceAsStream("/data/vestige/runtime_spells/pf2_wall_of_ice.json")) {
            assertNotNull(stream);
            var spell=SpellJson.read(ResourceLocation.parse("vestige:pf2_wall_of_ice"),
                    JsonParser.parseReader(new java.io.InputStreamReader(stream,java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject());
            var phases=SpellEffectGallery.phases(spell);
            assertEquals(2,phases.size());
            assertTrue(phases.getFirst().label().contains("selection"));
            assertTrue(phases.getFirst().geometry().isEmpty());
            assertEquals(5,phases.getLast().geometry().get("width").resolve(spell.traits()));
            assertEquals(3,phases.getLast().geometry().get("height").resolve(spell.traits()));
            assertEquals(1,phases.getLast().geometry().get("depth").resolve(spell.traits()));
            assertTrue(phases.getLast().label().contains("block_wall"));
            assertThrows(UnsupportedOperationException.class,()->phases.getLast().geometry().clear());
            var capabilities=com.quzzar.vestige.magic.effect.SpellCapabilities.of(spell);
            assertTrue(capabilities.contains(ResourceLocation.parse("vestige:alter_blocks")));
            assertTrue(capabilities.contains(ResourceLocation.parse("vestige:lift")));
            assertFalse(capabilities.contains(ResourceLocation.parse("vestige:damage")));
        }
    }
    @Test void arcsRetainExactEndpointsAndDeterministicBoundedShape() {
        Vec3 from = new Vec3(1, 2, 3), to = new Vec3(8, 4, 5);
        var path = VisualGeometry.arc(from, to, 17);
        assertEquals(from, path.getFirst()); assertEquals(to, path.getLast());
        assertEquals(path, VisualGeometry.arc(from, to, 17));
        assertNotEquals(path, VisualGeometry.arc(from, to, 18));
        assertTrue(path.size() <= 33);
        assertTrue(path.stream().allMatch(point -> Double.isFinite(point.length())));
        assertEquals(List.of(from, from), VisualGeometry.arc(from, from, 1));
        assertTrue(VisualGeometry.arc(Vec3.ZERO, new Vec3(0, 128, 0), 1).stream().allMatch(point -> Double.isFinite(point.length())));
    }
    @Test void fadeIsFiniteAndHasNoVisibleTailAfterExpiry() {
        assertEquals(0, VisualGeometry.opacity(-1, 8));
        assertTrue(VisualGeometry.opacity(3, 8) > .9);
        assertTrue(VisualGeometry.opacity(7.9f, 8) < .1);
        assertEquals(0, VisualGeometry.opacity(8, 8));
    }
    @Test void pulseReachesOutOnceThenReturnsCompletelyToItsOrigin() {
        assertEquals(0,VisualGeometry.pulseRadius(-1));
        assertEquals(0,VisualGeometry.pulseRadius(0));
        assertEquals(1,VisualGeometry.pulseRadius(.65f),1e-6);
        assertTrue(VisualGeometry.pulseRadius(.4f)>VisualGeometry.pulseRadius(.2f));
        assertTrue(VisualGeometry.pulseRadius(.75f)>VisualGeometry.pulseRadius(.9f));
        assertEquals(0,VisualGeometry.pulseRadius(1));
        assertEquals(0,VisualGeometry.pulseRadius(2));
    }
    @Test void coilingAndOrganicPathsRemainFiniteAndKeepTheirPhysicalAnchors() {
        for(Vec3 end:List.of(new Vec3(8,2,0),new Vec3(0,8,0),Vec3.ZERO)) {
            var path=VisualGeometry.helix(Vec3.ZERO,end,1.2,2,4);
            assertEquals(Vec3.ZERO,path.getFirst());
            assertTrue(end.distanceTo(path.getLast())<1e-9);
            assertTrue(path.size()<=65);
            assertTrue(path.stream().allMatch(point->Double.isFinite(point.length())));
            for(Vec3 point:path)assertTrue(point.distanceTo(Vec3.ZERO)<=end.length()+1.21);
        }
        var stem=VisualGeometry.tendril(Vec3.ZERO,4,1,2,8);
        assertEquals(13,stem.size());assertEquals(stem,VisualGeometry.tendril(Vec3.ZERO,4,1,2,8));
        assertNotEquals(stem,VisualGeometry.tendril(Vec3.ZERO,4,2,2,8));
        for(Vec3 point:stem) {
            assertTrue(Math.hypot(point.x,point.z)<=2.81);
            assertTrue(point.y>=-.25 && point.y<=3);
        }
    }
    @Test void everyNewSilhouetteAndAuthoredRhythmSurvivesTheWireFormat() {
        for(var shape:SpellVisual.Shape.values()) {
            var layer=new SpellVisual.Layer(shape,0xc92551,.85f,.08f,1.4f,-1.6f,1.2f,12);
            var payload=new SpellVisualPayload(UUID.randomUUID(),ResourceLocation.parse("minecraft:overworld"),
                    new SpellVisual.Resolved(24,2,List.of(layer)),List.of(point(1),point(2)),57,4,true,false,false);
            var buffer=new RegistryFriendlyByteBuf(Unpooled.buffer(),RegistryAccess.EMPTY);
            try {
                SpellVisualPayload.STREAM_CODEC.encode(buffer,payload);
                assertEquals(payload,SpellVisualPayload.STREAM_CODEC.decode(buffer));assertFalse(buffer.isReadable());
            } finally {buffer.release();}
        }
        assertThrows(IllegalArgumentException.class,()->new SpellVisual.Layer(SpellVisual.Shape.SIGIL,0xffffff,1,.04f,1,Float.NaN,0,8));
        assertThrows(IllegalArgumentException.class,()->new SpellVisual.Layer(SpellVisual.Shape.SIGIL,0xffffff,1,.04f,1,1,7,8));
        assertThrows(IllegalArgumentException.class,()->new SpellVisual.Layer(SpellVisual.Shape.SIGIL,0xffffff,1,.04f,1,1,0,25));
    }
    @Test void largeEmitterFootprintsAndOrderedBeamSamplingRemainBounded() {
        double furthest=0;
        for(int i=0;i<18;i++) {
            var offset=VisualGeometry.emissionOffset(17,8,6,i);
            assertEquals(offset,VisualGeometry.emissionOffset(17,8,6,i));
            assertTrue(Math.hypot(offset.x,offset.z)<=6);
            assertTrue(offset.y>=0 && offset.y<=1.2);
            furthest=Math.max(furthest,Math.hypot(offset.x,offset.z));
            assertTrue(VisualGeometry.emissionOffset(17,8,128,i).length()<9);
        }
        assertTrue(furthest>4,"Large fields were reduced to a tiny particle cluster");
        var points=List.of(new Vec3(-3,1,0),new Vec3(3,1,0),new Vec3(3,1,2));
        assertEquals(points.getFirst(),VisualGeometry.pathPoint(points,0));
        assertEquals(points.get(1),VisualGeometry.pathPoint(points,.5));
        assertEquals(points.getLast(),VisualGeometry.pathPoint(points,1));
        assertEquals(new Vec3(0,1,0),VisualGeometry.pathPoint(points,.25));
    }
    @Test void payloadRoundTripsLayersOrderedAnchorsAndLifecycle() {
        var payload = new SpellVisualPayload(UUID.randomUUID(), ResourceLocation.parse("minecraft:overworld"),
                visual().resolve(3), List.of(point(1), point(2), point(3)), 57, 4, true, false, false);
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
        try {
            SpellVisualPayload.STREAM_CODEC.encode(buffer, payload);
            assertEquals(payload, SpellVisualPayload.STREAM_CODEC.decode(buffer));
            assertFalse(buffer.isReadable());
        } finally { buffer.release(); }
    }
    @Test void oversizedPayloadListsAndInvalidResolvedValuesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new SpellVisual.Resolved(8, Float.NaN, visual().layers()));
        assertThrows(IllegalArgumentException.class, () -> new SpellVisual.Resolved(2401, 1, visual().layers()));
        assertThrows(IllegalArgumentException.class, () -> new SpellVisualPayload(UUID.randomUUID(), ResourceLocation.parse("minecraft:overworld"),
                visual().resolve(1), java.util.Collections.nCopies(34, point(1)), 0, 0, false, false, false));
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
        try {
            buffer.writeUUID(UUID.randomUUID()); buffer.writeResourceLocation(ResourceLocation.parse("minecraft:overworld"));
            buffer.writeVarInt(8); buffer.writeFloat(1); buffer.writeVarInt(Integer.MAX_VALUE);
            assertThrows(IllegalArgumentException.class, () -> SpellVisualPayload.STREAM_CODEC.decode(buffer));
        } finally { buffer.release(); }
    }
    @Test void visualRecipesParseWithoutCreatingGameplayCapabilities() {
        var json = JsonParser.parseString("""
                {"traditions":["arcane"],"traits":{},"triggers":[{"id":"primary","event":"interact"}],"effects":[{"type":"visual","visual":{
                  "duration":8,"radius":1,"layers":[{"shape":"beam","color":"aabbcc","alpha":0.8,"width":0.05,"scale":1}]
                }}]}
                """).getAsJsonObject();
        var spell = SpellJson.read(ResourceLocation.parse("vestige:visual_test"), json);
        assertTrue(SpellCapabilities.of(spell).isEmpty());
        var layer = json.getAsJsonArray("effects").get(0).getAsJsonObject().getAsJsonObject("visual").getAsJsonArray("layers").get(0).getAsJsonObject();
        layer.addProperty("color", "#aabbcc");
        assertThrows(RuntimeException.class, () -> SpellJson.read(spell.id(), json));
        layer.addProperty("color", "aabbcc"); layer.addProperty("alpha", 1.01);
        assertThrows(RuntimeException.class, () -> SpellJson.read(spell.id(), json));
        layer.addProperty("alpha", .8); layer.addProperty("shape", "unknown");
        assertThrows(RuntimeException.class, () -> SpellJson.read(spell.id(), json));
    }
    private static SpellVisual visual() {
        return new SpellVisual(8, new SpellValue.Constant(1), 1,
                List.of(new SpellVisual.Layer(SpellVisual.Shape.ARC, 0xaabbcc, .8f, .05f, 1)), false, Optional.empty());
    }
    private static SpellVisualPayload.Point point(int id) {
        return new SpellVisualPayload.Point(new Vec3(id, 2, 3), id, new UUID(0, id), 1);
    }
}
