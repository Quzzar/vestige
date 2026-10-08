# Charging crosshair contrast verification · October 7, 2026

Java 21 production build, Kithkyn platform compatibility and **173 unit tests** passed with zero failures/errors/skips. The native client completed normally with **276 main-render-target captures** and all nine release/cancel checks passing. Scroll/staff preparation remains 40 ticks; the shaped wand remains 50 ticks. Exact mana and source counts retain their prior results, including free cancellation. The complete combined log is retained as `build-client.log.gz`.

`verification.json` pins the four owned client-source hashes, matching workspace files, native checks and staged jar. The previous verified snapshot was cloned into `/private/tmp/vestige-crosshair-contrast-20261007-source`, then only the crosshair renderer and capture-style metadata were updated. Subsequent unrelated shared-workspace edits are excluded. The prior frozen source and evidence remain intact.

[Actual appearance](../../art/crosshair-spell-preparation-contrast/README.md) records two display/GUI sizes, source/hand variations, cancellation, snow and dark ground. Pixel analysis verifies black centers, white edges and complete restoration in every view. Native captures, enlarged crops and decoded demo frames were visually inspected.

This is a client contrast revision. No new world GameTest suite was run, and no gameplay mechanics or payload formats changed. Resource-pack reload, full F3 appearance, remote multiplayer and third-person gestures were not inspected. No installed profile was replaced. The review artifact is `build/crosshair-preparation-contrast-review/libs/vestige-0.1.0.jar`.
