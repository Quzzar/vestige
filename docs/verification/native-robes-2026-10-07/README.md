# Native robe verification · October 7, 2026

Wardweave and Cinderweave were implemented after the owner locked in the reviewed gameplay, durability and enchantment rules. All other clothing packages remain proposals.

**Final result:** 179 model/runtime/catalog tests passed with no failures, errors or skips; all 312 required Minecraft GameTests passed in a fresh world; packaging passed with matching Kithkyn co-load. The final shared-source regression checks actual mitigation notifications on incoming secondary damage and revocation of future bindings after source removal. A preceding focused run passed all 21 selected robe/shared-runtime checks. [Machine-readable evidence](verification.json) and compressed logs are saved here.

The native robe checks exercise ordinary armor wear, two-hit Wardweave behavior, one wear charge, full prevention and breakage, immutable trait snapshots/expiry, zero duration, source swapping and recovery, tagged fire reduction, large boosts, Fire Resistance immunity, ignored repeated hits, capacity/payment/recovery/clamping, anvil repair and enchant eligibility. Four ritual checks cover the two eight-offering recipes, retained sockets, mixed-color rejection and atomic cancellation. Model checks cover every wool color and rotation, malformed inputs, production/review formula agreement, color-specific viewer entries and expanded mana snapshots.

Native client capture passed all **64 views**: sixteen colors for each robe at GUI scales 2 and 3, rendering the registered item beside vanilla items and the worn layer on WIDE/SLIM models. Fabric/trim color and durability checks pass; white/blue/black captures were inspected. [Artwork evidence](../../art/magic-equipment-native/README.md) preserves sources and screenshots. Packaged asset hashes and production ability files match those verified sources.

The first broad run caught an outdated wand assertion: its economy core rounds the third 6-mana cast to 5, so the expected balance is 83. A cancellation assertion could also count the broken Plinth's ordinary Paper offering after it drifted into the output observation box; it now distinguishes that ingredient from a crafted scroll. No wand or ritual production behavior was changed for these corrections.

Independent builds in this shared checkout were deleting stale output paths recorded in the shared Gradle project history. Final verification isolates **both build output and project cache**, using [build.init.gradle](build.init.gradle):

```sh
./gradlew --project-cache-dir /private/tmp/vestige-robes-gradle-state test build runGameTestServer -I docs/verification/native-robes-2026-10-07/build.init.gradle
python3 tools/author_equipment_review.py --check
python3 tools/author_robe_inventory.py --check
python3 tools/author_robe_armor.py --check
```

Cinderweave's Coal Cuffs artwork is provisional until its A/B choice is resolved. The current worn geometry is Minecraft's ordinary chest armor model, without an extended skirt. Combat cues use the existing bounded procedural rune/dust presentation; this pass records inventory/worn captures, not a combat-effects video. JEI/EMI receive the 32 concrete matching-color recipes through their existing shared display adapters; their live category screens were not separately captured. No installed testing jar was changed and no branch was published.
