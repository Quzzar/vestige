# Short mantle robe verification

October 8, 2026. Visual refinement of Wardweave and Cinderweave after the owner rejected the connected skirt and clarified the hanging-back detail. The owner rejected this cape direction; this is historical verification. See [the replacement armor review](../../art/magic-equipment-armor-v2/README.md). Gameplay is unchanged.

## Executed checks

- `build.log`: full isolated Java 21 Gradle build, packaging and Kithkyn compatibility passed. All **181 unit tests** passed, with zero failures, errors or skips.
- `client.log`: the native Minecraft 1.21.1 / NeoForge 21.1.72 client loaded the current compiled robe classes/resources and completed **84 unedited main-render-target captures**. The final capture-angle fixture was compiled/packaged by `capture_client.py` using the prepared pinned NeoForge runtime. The driver uses the fresh combined build's classes/resources, the sibling Kithkyn jar and an isolated fresh world, with Kithkyn's LLM disabled.
- `tools/author_robe_armor.py --check` and `tools/author_robe_inventory.py --check` passed.
- `verify.py` passed: exact equality between current production PNGs, loaded build resources and the packaged jar; RGBA 64×64, hard alpha, neutral dyeable cloth, disjoint cloth/trim masks and exact packaged/loaded robe class bytes. [Frozen evidence](verification.json) records the artifact, source and capture hashes.

The native suite contains 64 dye/GUI views (two robes × sixteen colors × two GUI scales), two iron-armor layering views and eighteen normal world views (two robes × white/blue/black × front/side/back). Every main sheet shows both player body types and front/side/back/walking/crouching poses. The actual world fixture fixes torso/head yaw while changing camera yaw, so its three angles show different sides of the garment.

Inspected the white/blue/black robe sheets, Wardweave and Cinderweave over iron leggings/boots, Wardweave's world rear view and Cinderweave's world side view. The back panel is visibly separate, its hem is around the upper thigh, and the native legs remain independent in a stride and crouch. Dyed cloth stays tinted across the shoulders, sleeves and back panel; charcoal/ember trim stays fixed. The PNG inspection boards enlarge the exact exported pixels on dark/light backgrounds and are labeled separately from native evidence.

## Reproduce

```sh
JAVA_HOME=/path/to/java21 ./gradlew --no-daemon --max-workers=2 --no-parallel \
  --no-configuration-cache --project-cache-dir /private/tmp/vestige-robes-mantles-review-gradle-state \
  build verifyKithkynCompatibility \
  -I docs/verification/mantle-robes-2026-10-08/review.init.gradle
JAVA_HOME=/path/to/java21 ./gradlew runEffectsCapture \
  -I docs/verification/mantle-robes-2026-10-08/review.init.gradle \
  -Pcapture_kind=magic_equipment -Pcapture_directory=run/robes-mantles-2026-10-08 \
  -Pcapture_output=docs/art/magic-equipment-mantles/captures
python3 tools/author_robe_armor.py --check
python3 tools/author_robe_inventory.py --check
python3 docs/verification/mantle-robes-2026-10-08/verify.py
```

Use the configured Pillow runtime for the Python checks. `capture_client.py` records the faster executed launch, reusing the previous review's prepared local NeoForge argument/dependency files while compiling and loading this new build's actual robe classes. It updates the three client class families in the combined jar; no artwork or screenshot is composited into a native capture. The ordinary Gradle capture route above regenerates its own launch metadata.

## Limits

Cape movement is a bounded pose/stride response, not cloth simulation. Riding, swimming, flight and other mods' back-slot equipment remain additional visual playtests. The local HTML page is a convenience index over the saved native images. No combat world-test rerun was warranted by this appearance-only change; no installed testing pack, published branch or existing world was replaced.
