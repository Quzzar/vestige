# Shapeless ritual capacity — October 8, 2026

The owner clarified that four/eight slots provide crafting capacity. Shapeless recipes can place their offerings anywhere in a valid layout, without filling the inner ring first. The previous Fluxed Flint inner-only restriction was an implementation assumption. [Current ritual rules](../../design/ritual-crafting.md), [repair rules](../../design/magical-repair.md), [results and hashes](verification.json).

## Implemented behavior

Fluxed Flint construction/repair, magical threads, Homebound Eyes, Whispering Shells and Standing Stones use all seats of the complete available four- or eight-slot table. Fragment discovery uses the same table selection; six-offering Attunement Shards already match shapelessly in eight slots. Unused available surfaces remain empty. A complete eight-slot layout does not fall back to four slots to ignore extra outer offerings. All selected seats, including empty ones, are reserved and revalidated atomically.

Flint construction consumes the actual occupied seats rather than hardcoded inner indices. Repair retains the worn catalyst on its actual seat, preserves the target's full data and uses the existing independent volatility rolls. Thread and Eye outputs read the socket paired with String/Spider Eye on either ring. Geometry, repair strength, budget and risk tuning are unchanged.

Shaped spell/equipment recipes retain their authored relative positions and quarter-turn equivalence. The general constructor/viewer requirement that every eight-slot spell shape include all four inner offerings was removed; a regression case accepts a future outer-only shape, preserves rotation equivalence and rejects a rearranged pattern. Existing catalog definitions were not rewritten. Incomplete outer rings do not supply eight-slot capacity. Public shapeless viewer diagrams remain minimum-capacity examples.

## Verification

The isolated Java 21 combined source at `/private/tmp/vestige-shapeless-ritual-20261008-source` passed **181 unit tests**, **342 required Minecraft world tests**, packaging and Kithkyn version compatibility, followed by successful native client capture. No failures, errors or skipped unit tests. [Complete build/world/client log](build-world-client.log.gz).

The world suite includes all thirteen earlier Flint regression cases and twelve new shapeless cases. Coverage includes all 56 ordered repair pairs and 1,680 construction permutations; actual four-slot, mixed-adjacent and opposite-outer repairs; outer-triggered backfire; outer-only construction and exact-seat consumption; extra-item rejection without risk draws or smaller-table fallback; cancellation when a reserved empty outer seat changes; outer String/Eye selectors; and unchanged Shell/Stone attunement keys. Unit checks cover all 336 Shell placements, all five Thread types over 336 placements each, malformed snapshots and future shaped recipes with empty inner seats. Existing catalog and shaped ritual tests remain passing.

The final command ran `test build verifyKithkynCompatibility runGameTestServer runEffectsCapture`, with Kithkyn co-load disabled, `capture_kind=fluxed_flint`, a separate capture directory and the native output path below. This receipt does not claim a new full co-loaded Kithkyn suite or optional-viewer client pass.

## Native outer-ring repair

The native scene places Flint and a Staff on opposite outer Plinths in a complete eight-slot layout; every inner offering surface is empty. Inspected unedited framebuffers: [offered items](../../art/fluxed-flint-v1/native-shapeless/held-and-offered.png), [repair lift](../../art/fluxed-flint-v1/native-shapeless/repair-in-progress.png), [dropped output](../../art/fluxed-flint-v1/native-shapeless/repaired-output.png), and [final durability bars](../../art/fluxed-flint-v1/native-shapeless/durability-after-repair.png).

[Five native assertions](../../art/fluxed-flint-v1/native-shapeless/verification.json) passed: translated name, the two-outer-offering eight-slot fixture, production activation, a dropped Staff differing only by ten restored durability, and ten catalyst points spent with the target consumed once. Loaded model/texture hashes match the unchanged production v1 files and packaged jar. The capture-only camera now sends its rotation to the client so the complete apparatus is centered in view.

## Staged artifact and limits

The frozen combined jar is staged at `build/shapeless-ritual-review/libs/vestige-0.1.0.jar`, with its source jar alongside it. Jar SHA-256: `018086f10edffda9eb8d32a6a756efe8898f53e42fc089916fa6823c49ae82c0`. The manifest records the fourteen changed source/test files and artifact hashes; those files matched the root checkout when staged. This is a combined working snapshot based on the previous verified repair source, not a scoped publication branch. No installed pack or published branch was modified.

Survival balance, stabilizing imbuements, remote multiplayer, long-session playtesting and fresh JEI/EMI captures remain outside this correction. Existing sprite owner acceptance remains pending.
