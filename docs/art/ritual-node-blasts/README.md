# Distributed ritual backfires

The owner requested a large explosion around the Spellstone and local bursts around each active Plinth, so distant or elevated nodes remain dangerous without filling the entire circle with one enormous damage sphere.

The Spellstone retains radius 4. Each active Plinth has radius 2. Four-slot recipes burst at the center and four inner Plinths; eight-slot recipes burst at the center and all eight Plinths, including empty recipe positions. Each creature receives the strongest visible exposure once, with the knockback from that same node. Distance falloff, wall occlusion, failure probability and tick-20 commitment remain unchanged. Only participating offerings are forfeited; terrain, apparatus, socket materials, the reference and loose items survive.

The native capture fixtures exercise a compact four-slot circle, a compact eight-slot circle and an eight-slot Cross/Cross circle with radii 8/16 and heights +3/+6. The elevated fixture uses ordinary masonry platforms. Its empty outer node has a nearby creature, and another creature occupies a safe gap. Captures use the actual Minecraft framebuffer and ordinary seeded failure rolls.

The silent [backfire review](backfire-review.mp4) combines those three actual recordings. The native frames show the large central emitter and smaller local clouds; the wide view shows all nine nodes bursting across the elevated circle.

**Verified:** Java 21 build/packaging and Kithkyn compatibility pass, with 104 unit tests and all 186 required Minecraft world tests. Four new world checks cover distant four/eight-node geometry, empty active nodes, inactive outer isolation, vertical/local range, wall shielding, strongest single-hit overlap, protected sockets/reference/drops/terrain and pre-commit cancellation. The model author/audit and 490 canonical leyline fixtures also pass. [Build evidence](build-verification.json) pins all 318 compiled classes and all 1,444 source assets/data files to the tested jar. An existing server-side full-name assertion was updated to respect the newly concealed scroll-name contract; typed augments and persistence retain their checks.

[Capture evidence](capture-verification.json) records 174 native frames and actual server health/reference/consumption outcomes. The empty outer-node creature is hurt in both eight-slot layouts, while outside/gap creatures remain at 20 health. Loaded ritual code matches the tested jar. Raw ritual displacement fields are not used as movement evidence because those fixtures do not initialize their starting-position map. Multiplayer observation, audio mixing and an actual Prism profile launch remain unverified by these silent native captures.

The tested jar is [installed in Kithkyn Testing](prism-install.json), with an exact recoverable backup of the previous jar. Restart that Minecraft instance to load it. Its existing NeoForge 21.1.248 profile differs from the pinned 21.1.72 native test/capture baseline. The earlier A recordings retain the preceding single-center failure behavior.

## Reproduction

Use Java 21 and fresh capture/world directories. The isolated build and Gradle working state avoid modifying classes loaded by an existing review client. A separate build directory alone is insufficient: concurrent builds share Gradle task history and can delete another run’s classes as stale output.

```bash
./gradlew -I docs/art/ritual-node-blasts/isolated-build.gradle --project-cache-dir build/ritual-node-blasts-gradle-state build verifyKithkynCompatibility runGameTestServer -Pgametest_directory=run/ritual-node-blasts-gametest --no-build-cache --no-configuration-cache -Dorg.gradle.parallel=false
node tools/sync_leyline_rules.mjs --check
./gradlew -I docs/art/ritual-node-blasts/isolated-build.gradle --project-cache-dir build/ritual-node-blasts-gradle-state runEffectsCapture -x compileJava -x processResources -Pcapture_directory=run/ritual-node-blasts-capture -Pcapture_kind=ritual -Pcapture_spells=ritual_failure,ritual_failure_eight,ritual_failure_spread -Pcapture_material=stone -Pcapture_seconds=6 -Pcapture_output=build/ritual-node-blasts-native --no-build-cache --no-configuration-cache -Dorg.gradle.parallel=false
python3 tools/encode_ritual_capture.py build/ritual-node-blasts-native docs/art/ritual-node-blasts/native --build-directory build/ritual-node-blasts-final
python3 docs/art/spellstone-corner-details/locked-a/encode-review.py docs/art/ritual-node-blasts --backfires
```

Encoding verifies loaded ritual class and asset hashes against the build and source. The review reel adds captions above the unchanged 960×540 game image and checks a complete decode; the recordings are silent.
