# Fluxed Flint continuation — October 8, 2026

The owner resumed Fluxed Flint after approving Dissentient Diamond's exact artwork and finishing the rarity pass. This implements the chosen ingredient chain and requested blue accents. [Current recipes and repair design](../../design/magical-repair.md), [artwork/export/prompt](../../art/fluxed-flint-v2/README.md), [receipt](verification.json), [source hashes](source-snapshot.json).

## Implemented

- **Dissentient Diamond:** Diamond → Gunpowder → Wither Skeleton Skull → Gunpowder on the four inner Plinths; one output, empty Spellstone. Diamond and skull are opposite; two separate Gunpowder offerings flank them.
- **Fluxed Flint:** Flint → Netherite Ingot → Dissentient Diamond → Netherite Ingot; one full-budget output. This replaces the provisional Diamond Block/Echo Shard composition.
- Both patterns accept whole quarter-turns, retain sockets and ignore inactive outer offerings. Their repeated ingredients give them natural reflection symmetry. Stacking two duplicate units on one seat does not replace two separate offerings. Both public viewer entries derive their actual inputs from the matchers.
- The ordinary Dissentient Diamond has no newly invented intrinsic trait. Flint retains Volatile 2, 128 durability, the 25% repair cap, exact data-component copying and the independent-input failure rule. Two-item repair still accepts any two valid seats on either ring.
- The literal 16×16 Flint sprite changes only its 24 violet accent pixels to three pinned vanilla Diamond shades. All five stone shades, alpha, silhouette and model are unchanged; approved Dissentient Diamond bytes are unchanged. Rare/aqua Flint and Uncommon/yellow Diamond stay intact.

## Verification

The root build passes **211 unit tests across 46 suites**, zero failures/errors/skips. A frozen copy at `/private/tmp/vestige-dissentient-diamond-v2-20261008-source` independently passes the same **211 tests**, packaging and **all 26 required focused Minecraft tests** (`fluxed_flint|shapeless_ritual|ordered_ritual`). The frozen build/world run completes in **4 minutes** with the original production collision code. [Frozen build/world log](isolated-build-world.log.gz), [root build log](root-build.log.gz).

The new world case crafts a rotated intermediate, consumes both Gunpowder offerings, then uses that actual output to craft a rotated full-budget Flint. It verifies atomic consumption, unchanged installed material and untouched inactive outer offerings. Updated exhaustive matching covers 1,680 labeled construction placements (eight accepted labelings of four unique rotations because the ingots are identical), all 56 repair pairs and wrong-capacity rejection. Existing repair/backfire cases cover exact metadata/container/binding preservation, final-point exhaustion, canceled/spawn-rejected commitment, independently volatile targets and canonical trigger survival. Unit checks also reject the retired recipe and stacked duplicate shortcuts and validate both live viewer patterns.

Both sprite author checks pass, including the approved Diamond pin. All scoped production files equal the frozen test snapshot. The staged review JAR at `build/fluxed-flint-v2-review/libs/vestige-0.1.0.jar` matches the frozen tested artifact; recipe/runtime classes and all four Flint/Diamond texture/model resources match packaged files. Exact hashes are in the receipt.

## Native scene and limitations

The final native client completes in **1m 46s**, with **five assertions passed**: translated name, only two outer offerings, production activation, an actual dropped Staff changed only by ten restored durability, and exactly ten catalyst points spent with the target consumed once. All five unedited 1920×1440 frames were inspected: [inventory beside vanilla](../../art/fluxed-flint-v2/native/inventory-beside-vanilla.png), [held/offered](../../art/fluxed-flint-v2/native/held-and-offered.png), [ritual lift](../../art/fluxed-flint-v2/native/repair-in-progress.png), [dropped output](../../art/fluxed-flint-v2/native/repaired-output.png), and [final durability bars](../../art/fluxed-flint-v2/native/durability-after-repair.png). [Native checks/hashes](../../art/fluxed-flint-v2/native/verification.json), [client log](native-final.log.gz), [unresampled inventory crop](../../art/fluxed-flint-v2/native/inventory-comparison-crop.png).

Nine recipe/runtime/viewer/capture classes match native compiled files and the staged JAR byte-for-byte. Loaded Flint texture/model hashes match production and packaged resources. The art-only client uses the previously documented isolated Standing Stone collision bypass; the scene places no Standing Stone. The world tests and staged JAR use the original collision implementation. The original isolated source was restored exactly after client exit; root source is untouched, and the staged collision class differs from the preview bypass class.

The first capture attempt's real inventory/held screenshots remain under `docs/art/fluxed-flint-v2/native-initial-timing-attempt`. It inspected before the server reached ritual commitment under load. The capture now waits for 65 actual server ticks after activation rather than inferring completion from 2.5 wall-clock seconds. This changes only the opt-in verification harness; gameplay timing is unchanged.

The shared checkout's initial world startup was disrupted by simultaneous builds replacing loaded classes. The successful frozen run avoids that interference. A one-line Hourglass unit-test call was corrected to the existing public `SpellJson.readCosts` entry point after the combined test compile exposed its private-method call; Hourglass gameplay/artwork remain owned by the other chat.

This pass does not claim the full world suite, Kithkyn co-load, optional viewer screen inspection, survival balance acceptance, testing-pack installation or publication. Stabilization imbuements and dynamic per-target repair viewer output remain follow-up design. Final owner acceptance of the new Flint pixels is separate from native review.
