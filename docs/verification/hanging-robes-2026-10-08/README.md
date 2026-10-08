# Hanging robe verification

October 8, 2026. Visual-only refinement of Wardweave and Cinderweave, prompted by the owner's request for robes that extend over the legs. Art remains a review candidate. Gameplay approval is unchanged.

The full Gradle pass uses `review.init.gradle` to isolate both project state and build outputs from other ongoing work. `review-client.log` records successful Java compilation, packaging, all 179 unit tests and the first 72-view client pass. Final client-only waist/hip closure and preview-label refinements are compiled against that exact pinned runtime and packaged by `final_client.py`; `final-native.log` records their final 72-view run. The gameplay unit suite is not repeated for those visual-only refinements. Earlier logs preserve the separate-leg draft and the rejected first connected-skirt/camera attempt; those are not final appearance evidence.

Reproduce the final capture with:

```sh
./gradlew --no-daemon --max-workers=2 --no-parallel --no-configuration-cache \
  --project-cache-dir /private/tmp/vestige-robes-hanging-review-gradle-state \
  test jar runEffectsCapture \
  -I docs/verification/hanging-robes-2026-10-08/review.init.gradle \
  -Pcapture_kind=magic_equipment \
  -Pcapture_directory=run/robes-hanging-v3-2026-10-08 \
  -Pcapture_output=docs/art/magic-equipment-hanging/captures
python3 tools/author_robe_armor.py --check
python3 tools/author_robe_inventory.py --check
python3 docs/verification/hanging-robes-2026-10-08/verify.py
```

After preparing the Gradle runtime, `python3 docs/verification/hanging-robes-2026-10-08/final_client.py` also recompiles/packages the two client-only refinements with Java 21 and repeats the preview with the generated NeoForge launch arguments. It avoids another cold full-project startup on a heavily loaded host. It uses the verified Kithkyn jar and the same native models, assets and capture harness; it does not compose or alter screenshots.

`verify.py` checks the final capture count, current unit results, exact source/build-resource/packaged texture equality, hard alpha and atlas dimensions, then writes hashes to `verification.json`. Native screenshot inspection remains required; these structural checks alone do not establish visual quality.

The capture harness renders the real registered items and armor extension through Minecraft's renderer. It includes all sixteen colors at GUI scales two and three, WIDE/SLIM front/side/back/walk/crouch views, two white outfits over iron leggings/boots, and six normal third-person world views. The camera is released from mouse input and held level for world captures; the client waits for rendered world sections and server equipment synchronization. Screenshots come directly from the main render target, without compositing.

No combat behavior changed and the Minecraft combat suite was not rerun for this artwork pass. The accepted 312-world-test gameplay baseline remains in [October 7 verification](../native-robes-2026-10-07/README.md). Swimming, riding and elytra appearances remain additional playtest cases; the garment uses native copied poses rather than cloth physics.
