# Fluxed Flint verification — October 8, 2026

**Follow-up:** the owner corrected the inner-only shapeless restriction. [Shapeless capacity verification](../shapeless-rituals-2026-10-08/README.md) records the newer four/eight-slot implementation and review jar. This initial receipt remains a historical snapshot.

Isolated Java 21 source/build: `/private/tmp/vestige-fluxed-flint-20261008-source`. Minecraft 1.21.1 / NeoForge 21.1.72. [Design and provisional values](../../design/magical-repair.md), [art/export evidence](../../art/fluxed-flint-v1/README.md), [machine-readable results and hashes](verification.json).

The final complete run passed all **179 unit tests** with zero failures/errors/skips, packaging, Kithkyn version compatibility and all **329 required Minecraft behavior tests**. The same run completed the native client review successfully. [Complete build/world/client log](full-suite-and-client.log.gz).

The full pass included twelve Fluxed Flint cases. One additional test then verified that an offering-triggered explosion consumes a nontriggering center reference and releases every reservation. The final build passed all **thirteen focused Flint world cases**; production code was unchanged from the full pass. [Final focused log](final-thirteen-world-tests.log.gz).

Coverage includes construction, complete target/catalyst component preservation (stored scrolls, shaping, selection, name, enchantments, container contents, identity and arbitrary custom metadata), partial repair, final-point exhaustion, trigger-specific backfire protection, independent risk rolls and exact probability boundaries, ignored outer offerings, retained sockets, input edits, rejected output spawn, event-driven callback edits, shared volatility during ordinary Staff expansion, reference forfeiture and reservation release. Actual crafting, grindstone and anvil menus reject unsafe Wand/Staff/Flint combining while anvil renaming remains available.

The earlier integration run passed all Flint tests but exposed an unrelated staff-guard fixture failure: GameTest mock players did not advance held-use time automatically. Its owning chat fixed the fixture's five-tick clock advancement; the final full pass includes that fix. A second intermediate test exposed NeoForge 21.1.72 retaining a prior rename result when an anvil hook is canceled. `MagicalRepairPolicy` now clears the actual menu result and maximum cost before cancellation; the real-menu regression test passes.

## Native appearance

Five unedited framebuffer captures were inspected: [inventory beside vanilla](../../art/fluxed-flint-v1/native/inventory-beside-vanilla.png), [held/offered item](../../art/fluxed-flint-v1/native/held-and-offered.png), [repair in progress](../../art/fluxed-flint-v1/native/repair-in-progress.png), [repaired dropped output](../../art/fluxed-flint-v1/native/repaired-output.png), and [final durability bars](../../art/fluxed-flint-v1/native/durability-after-repair.png).

The client passed four assertions: translated item name, production repair activation, an actual dropped Staff differing only by ten restored durability, and exactly ten catalyst points spent with the target consumed once. The registered model and texture hashes loaded by Minecraft equal the production files and packaged jar bytes. The source study, final 16×16 export, built-in generation prompt and palette/export settings are retained in the art folder. `tools/author_fluxed_flint.py --check` passes. Owner artwork acceptance is pending.

## Reproduction and review artifact

The full frozen run used `test build verifyKithkynCompatibility runGameTestServer runEffectsCapture` with `-Pwith_kithkyn=false`, `-Pcapture_kind=fluxed_flint`, the repository native-output path and a separate capture directory. The final follow-up used `build runGameTestServer -Pgametest_filter=fluxed_flint`, also with Kithkyn co-load disabled. Kithkyn version compatibility was checked separately; a prior integration run loaded Kithkyn successfully, but a passing final full co-loaded suite is not claimed.

The combined review jar is staged at `build/fluxed-flint-review/libs/vestige-0.1.0.jar`, alongside its source jar. SHA-256: `3b2a805f12a438ca9a8811ef8df135d2ecafd9c06b9c18e692874bc01dd5c1b4`. The manifest pins the changed source/resources and staged artifacts; those production files matched the root checkout when staged. This is a frozen combined working build, not a scoped publication branch. No installed testing jar or published branch was changed.

Survival balance, the provisional expensive recipe and final artwork need owner playtesting. Stabilization upgrades, dynamic repair viewer entries and remote multiplayer/long-session testing remain separate work. Optional viewers have a construction entry, but new JEI/EMI client captures are not claimed.
