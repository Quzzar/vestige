# Crosshair preparation verification · October 7, 2026

The verified frozen source is `/private/tmp/vestige-crosshair-charge-20261007-source`. The shared workspace contains concurrent unrelated work; all five owned Java files match the verified copy byte-for-byte. `verification.json` pins those hashes, the packaged artifact and nine real server cast/cancel results.

Java 21 production `build` and Kithkyn platform compatibility passed. **173 unit tests** passed with zero failures, errors or skips. Both staff trait and texture authoring checks passed. The packaged jar contains the new crosshair renderer, held-item animation and mana symbol, and no obsolete `ManaDisplay.drawBar` method. Full final logs are retained as `build.log.gz` and `client.log.gz`.

The final native client capture completed normally with **277 main-render-target frames**, two window/GUI sizes and all nine source/release checks. Successful scroll/staff casts retain their 40-tick time cost; the shaped wand retains 50 ticks. Mana and scroll counts match existing payment rules, and every preparation clears. Early cancellation retains 100 mana and all four scrolls. The contrast fixture explicitly points the client camera at snow/night ground; setting only server rotation did not change that camera in the first diagnostic run.

[Native evidence](../../art/crosshair-spell-preparation/README.md) retains unmodified stills, metadata, center-to-tip pixel traces and a decoded/inspected demo. Visual inspection covers straight backward motion, ordinary held angles, offhand/left-arm presentation, source completion/cancellation, both GUI scales, and light/dark ground. The tint uses the loaded native crosshair sprite without adding a shape or bar.

The prior 55 focused preparation world regressions remain prior evidence; this client presentation revision did not rerun that suite. Custom resource-pack reload and full F3 live appearance were not inspected. Remote multiplayer and third-person gestures were not inspected. No profile was installed or published. The review jar is staged at `build/crosshair-preparation-review/libs/vestige-0.1.0.jar`; unrelated changes after the frozen snapshot are excluded.
