# Native ritual footage

Captured October 3, 2026 from Minecraft's main rendered framebuffer, using the registered apparatus, synchronized offerings, native server ritual operations, vanilla item models and the shared visual renderer. These are actual isolated test-world recordings, not browser animations. The selected second Spellstone crown and accepted pedestals are active. Clips are silent and use measured frame times; adjacent JSON stores engine, texture/model hashes, outcome and server tick count.

- [Reference hints and pedestal movement](ritual_hints.mp4): correct Paper, a colored missing-item shadow, wrong-type up/down movement and a misplaced same-type ingredient.
- [Five-part unreferenced crafting](ritual_success.mp4): the exact Flicker recipe raises the full circle, consumes its five ingredients and produces a native scroll.
- [Mixed-fragment discovery](ritual_discovery.mp4): three Fire and one Evocation fragment reconstruct a scroll using the accepted trait-intersection pool.
- [Completed referenced failure](ritual_failure.mp4): seeded ordinary gameplay RNG selects a blast; the nearby villager goes from 20 to approximately 12.11 health and the distant villager remains at 20. The reference and terrain remain intact.

[In-game instructions and limitations](../../design/ritual-crafting.md). [Installed Prism build and hash](prism-install.json). Final 214 recipes and model/texture assets match the installed jar. The Prism instance uses NeoForge 21.1.248; behavioral/client validation used the pinned 21.1.72 baseline, and the installed instance still requires owner launch/playtesting. Iron-provider alternatives are verified against pinned IDs, not a separately launched provider-installed configuration. Slot-local Spellshaping is future work and no global material boost is enabled.

Reproduce with a fresh output folder:

```bash
./gradlew --offline runEffectsCapture -Pwith_kithkyn=false -Pcapture_kind=ritual -Pcapture_seconds=5 -Pcapture_output=build/native-ritual-review-new
python3 tools/encode_ritual_capture.py build/native-ritual-review-new docs/art/ritual-crafting-review-new
```
