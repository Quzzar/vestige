# Native-color charging edge verification · October 7, 2026

The final Java 21 production build, Kithkyn platform compatibility and **173 unit tests** passed with zero failures/errors/skips. The native client completed normally with **268 main-render-target captures** and nine successful release/cancel checks. Existing scroll/staff 40-tick and shaped wand 50-tick timing, mana payments, remaining source counts and free cancellation retain their results. The final complete combined log is `build-client.log.gz`.

`verification.json` pins four owned client-source hashes, their exact workspace match and the staged package. The final verified source is `/private/tmp/vestige-crosshair-edge-20261007-source`; it continues the preceding isolated snapshot with the final native-color renderer and capture metadata. Unrelated later shared-workspace changes are excluded.

[Native appearance](../../art/crosshair-spell-preparation-native-edge/README.md) covers source/hand variations, two display/GUI sizes, cancellation and light/dark ground. Every completed inner edge sampled across all nine views equals the inverted local background. All 17 occupied crosshair pixels remain identical to idle throughout 35 charged frames on stable contrast surfaces. Edges clear in all nine views. Actual stills, enlarged crops and decoded demo frames were inspected.

The renderer uses the same inversion factors as the pinned Minecraft `Gui.renderCrosshair`, and samples neighboring texels from the current sprite. Each edge pixel is drawn once, excluding the entire occupied native mask. Progress interpolates inversion strength in RGB because the native inversion blend ignores source alpha. Cached masks refresh when the loaded sprite identity changes.

This is client presentation only. No new world GameTest suite was run. Resource-pack reload, full F3 appearance, remote multiplayer and third-person gestures were not inspected. No installed profile was replaced. The review jar is `build/crosshair-native-edge-review/libs/vestige-0.1.0.jar`.
