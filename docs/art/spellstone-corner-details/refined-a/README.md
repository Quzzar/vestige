# Refined Spellstone A after in-game review

The owner selected the smaller A corner pattern, then requested lower runes beneath resting items, normal stone on empty Plinths, a taller Spellstone and a clean hover outline. All 36 finishes now implement those refinements. The cap is 12×12 and 3.25 model units thick, at y10/16; the supports stay 3.5 units thick at 22.5 degrees. A chip size and palette are unchanged.

The Astral Seal’s highest layer is less than 0.008 block above the surface; flat items rest at +0.02. Empty Plinths have no extra socket tile. Installed material plates and independent socket contents keep their prior behavior. Accurate collision and picking preserve the opening; the client outlines the three authored cuboids with straight edges.

[Model/seam audit](model-and-seam-audit.json) checks all finishes, the exact translated A chips, clear openings, sampled support coverage and uninterrupted column joins. [Native views](native/) show the actual Minecraft geometry, scroll depth occlusion, hover outline, waterlogging, finish variants and empty/imbued columns. [Ritual review](ritual-review.mp4) records six native states: ready, missing/misplaced, wrong, successful ingredient lift, failed blast and fragment discovery. Videos are silent; captions are above the unchanged game image.

A [fresh refined creative review](handson-review.json) is open with four labelled ritual stations and ordinary keyboard/mouse control. The earlier client saved and exited normally at 12:30:49 EDT without a stop request from this task. New recordings and review use a separate game directory and isolated build folder, preserving its save and loaded runtime. Prism requires a restart to load the updated jar.

## Verification and reproduction

Java 21 `build`, compatibility verification, 97 unit tests and 165 required world tests pass. The walking regression checks every finish from all four directions. [Packaged-build verification](build-verification.json) pins the final tested jar, 180 apparatus block models and all 1,438 current assets/data files; [Prism installation](prism-install.json) records the atomic installation and recoverable previous jar. The development baseline is NeoForge 21.1.72; the existing Prism profile uses NeoForge 21.1.248.

```bash
python3 tools/author_apparatus_models.py --check
python3 tools/audit_apparatus_detail.py --check
./gradlew -I docs/art/spellstone-corner-details/locked-a/isolated-build.gradle -Preview_build_directory=build/spellstone-a-refined-isolated build verifyKithkynCompatibility runGameTestServer -Pgametest_directory=run/spellstone-a-refined-gametest --no-build-cache
./gradlew -I docs/art/spellstone-corner-details/locked-a/isolated-build.gradle -Preview_build_directory=build/spellstone-a-refined-isolated runEffectsCapture -Pcapture_directory=run/spellstone-a-refined-capture -Pcapture_kind=apparatus -Pcapture_spells=spellstone_astral,spellstone_astral_side,spellstone_astral_night,spellstone_astral_scrolls,spellstone_astral_underwater,spellstone_astral_finishes,spellstone_astral_hover,spellstone_astral_reference,plinth_column_imbuements,plinth_column_head -Pcapture_material=stone -Pcapture_seconds=4 -Pcapture_output=build/spellstone-a-refined-native
python3 tools/archive_rune_captures.py build/spellstone-a-refined-native --refined-a --build-directory build/spellstone-a-refined-isolated
./gradlew -I docs/art/spellstone-corner-details/locked-a/isolated-build.gradle -Preview_build_directory=build/spellstone-a-refined-isolated runEffectsCapture -Pcapture_directory=run/spellstone-a-refined-capture -Pcapture_kind=ritual -Pcapture_spells=ritual_idle,ritual_hints,ritual_wrong,ritual_reference_success,ritual_failure,ritual_discovery -Pcapture_material=stone -Pcapture_seconds=5 -Pcapture_output=build/spellstone-a-refined-rituals
python3 tools/encode_ritual_capture.py build/spellstone-a-refined-rituals docs/art/spellstone-corner-details/refined-a/rituals
python3 docs/art/spellstone-corner-details/locked-a/encode-review.py docs/art/spellstone-corner-details/refined-a
```

Capture directories must be fresh. Do not compile into an isolated directory while its Minecraft client is running. All recordings use the actual main framebuffer and real server crafting; no shader pack, simulated silhouettes or added world light.

A concurrent recipe-viewer installation replaced the first refined jar with older apparatus art. The final `build/spellstone-a-final-package` build combines the shared current source, including that viewer and current Standing Stone meshes, and passes the complete unit/world suites again. Its apparatus classes exactly match those inspected in the recordings. Both overwritten jars have verified backups; [final installation evidence](prism-install.json) records the latest artifact.
