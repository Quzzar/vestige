# Mage armor robe verification

October 8, 2026. Appearance-only redesign after the owner explicitly rejected the cape. [Accepted artwork and native review](../../art/magic-equipment-armor-v2/README.md). The owner subsequently approved these designs; [acceptance and exact approved asset hashes](../../art/magic-equipment-armor-v2/approval.json) are recorded separately from the verification below. This approval pass changes no production artwork or gameplay and does not rerun the client, publish or install a build.

The executed `capture_client.py` compiles the three robe/capture client class families with Java 21 against the prepared Minecraft 1.21.1 / NeoForge 21.1.72 development runtime, copies the four current worn textures into the loaded resources, and updates those same classes/textures in the review jar. The final art client loads **Minecraft, NeoForge and Vestige without optional Kithkyn**, in a fresh isolated flat world. The capture harness's opt-in GUI-only mode saves its actual completion manifest after the 64 dye/pose views and two armor-layering views. No ordinary third-person world view is claimed for the final fit. `client.log` records the launch; the prepared build is `run/robes-mantles-review-build`.

`verify.py` checks the **66 native screenshots** and completion metadata, exact source/loaded-resource/package/frozen-export texture equality, neutral dyeable cloth, disjoint masks, RGBA 64×64/hard alpha, current packaged/loaded client class equality and absence of the rejected back panel. `verification.json` freezes source, artwork, package and native capture hashes. The prior isolated combined build's **181 passing unit tests are a baseline**, not claimed as rerun for this visual revision. This pass changes no gameplay.

The native suite covers sixteen colors at GUI scales two and three, wide/slim players and front/side/back/walking/crouching poses, and two iron leggings/boots layering sheets. Screenshots are unedited Minecraft render-target output. Ordinary world views, riding, swimming and flight remain additional visual playtests.

## Startup fixture and review iterations

The host reached a load average above 220. The first launch exceeded its startup allowance; the second was stopped while constructing unrelated standing-stone collision. Logs and the thread dump preserve those attempts. Subsequent art launches use [a temporary collision fixture](fixture/StandingStoneCollision.java) and two JVM processors. No standing stones appear in the review scene. Robe classes/assets, registered items, dyes, native armor rendering and screenshot output stay real. The driver restores the original collision class in `finally` and never packages the fixture. `fixture.json` freezes both class hashes; verification checks that the restored class matches the package. This is robe presentation evidence, not standing-stone gameplay verification.

The first native fit completed 84 views including ordinary world views. Two [diagnosis frames](first-fit-captures/) retain the initial narrow cut and cuff artifacts. The second fit completed 66 GUI views but timed out waiting for the first ordinary-world view on the overloaded host; [its retained frames](second-fit-captures/) exposed the vanilla leggings waistband drawing over the cloth. The final fit separates coplanar cuff surfaces, widens/dampens lower coat panels and increases torso/lower-back clearance beyond vanilla leggings. The final run intentionally completes after its 66 GUI views and omits optional Kithkyn to reduce startup load. Earlier model/export snapshots are retained beside those diagnosis frames.

The preceding final-fit attempt closed before capturing and is retained as `client-closed-before-capture.log`. The successful retry reused its already-compiled current client classes with `VESTIGE_CAPTURE_SKIP_COMPILE=1`; it completed all 66 captures, exited normally and restored the production collision class. `verify.py` passed afterward. Final white Wardweave, blue/black Wardweave, blue/black Cinderweave and both white iron-layering sheets were visually inspected, including the blue Wardweave GUI-scale-three sheet. Both body types and the five poses are present. Cloth and fixed trim stay separated; the leggings no longer cover the robe belts, boots remain exposed and no cape/back panel appears. The exaggerated walking pose still exposes the moving leg through the coat opening; this is not a continuous skirt.

## Reproduce

Use the configured Python/Pillow runtime:

```sh
python3 tools/author_robe_armor.py --check
python3 tools/author_robe_inventory.py --check
python3 docs/verification/armor-robes-2026-10-08/capture_client.py
python3 docs/verification/armor-robes-2026-10-08/verify.py
```

The driver records local Java/prepared runtime paths. The standard Gradle `runEffectsCapture` route with `-Pcapture_kind=magic_equipment` remains available for an ordinary combined client/world pass. No installed testing pack, existing user world or published branch was changed.
