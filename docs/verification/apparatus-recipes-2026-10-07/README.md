# Apparatus names and construction recipes · October 7, 2026

The owner selected the two attached crafting grids. All 36 native Plinth finishes use six matching slabs around one full block, yielding two. All 36 Spellstones use five matching slabs, two Diamonds and one Amethyst Block, yielding one. Smooth Quartz Plinth, Spellstone and Standing Stone names omit the redundant "Block" inherited from the shared material label.

With Supplementaries loaded, the native Stone Bricks Plinth recipe and its unlock are skipped. Its own unchanged pedestal construction recipe keeps the shared grid, including its existing Chiseled Stone Bricks alternative. Each pedestal converts one-for-one through the existing optional recipe. Iron's independent conversion remains unchanged. Provider APIs and dependencies are absent from production changes.

| Setup | Platform | Result |
| --- | --- | --- |
| Standalone, stable source copy | NeoForge 21.1.72 / Java 21 | All 5 focused world tests pass |
| Supplementaries 3.8.5 + Moonlight 3.3.0 | NeoForge 21.1.247 / Java 21 | All 5 focused world tests pass |
| Both pedestal mods and their actual dependencies | NeoForge 21.1.247 / Java 21 | All 5 focused world tests pass |

Tests check available native finishes and counts, mixed-slab rejection, superseded-grid rejection, conditional native/foreign recipe-book unlocks, preservation of Supplementaries' two-pedestal output and both center alternatives, and ordinary one-for-one conversions in every 2×2/3×3 slot. The standalone source copy passes `build`, `verifyKithkynCompatibility` and all **139 unit tests** with no failures/errors/skips. Both apparatus and Standing Stone generator checks pass.

The first shared-output standalone run had a missing unrelated compiled test class and did not execute world tests despite Gradle's success marker. The first isolated copy then omitted three catalog fixtures used by unit tests; the exact fixture files were added, and the complete build/test/world run passed. Both setup failures remain in the manifest and are excluded from passing totals.

The staged shared-checkout review jar is `build/apparatus-recipes-review/libs/vestige-0.1.0.jar`. All 72 construction resources, the native Plinth unlock, four existing compatibility resources and three shortened translations match source; the previously missing test class is present. It is a review package, with no installed-pack replacement or actual client-appearance claim.

[Verification manifest](verification.json), [completion excerpts](results.txt) and [temporary provider test configuration](provider-test.init.gradle) retain package/provider hashes and full-log locations. Provider tests use fresh worlds and `-Pgametest_filter=pedestal_compat|apparatus_recipes`, `-Pwith_kithkyn=false`, `-Pneo_version=21.1.247` and `-Ppedestal_providers=supplementaries` or `both`. The standalone uses the original platform and an isolated source copy containing the three `processTestResources` fixtures from `tools/`. Existing packs/worlds are unchanged. Iron-only and remote multiplayer were not repeated.

Supplementaries' [upstream 1.21.1 pedestal recipe](https://github.com/MehVahdJukaar/Supplementaries/blob/1.21.1/common/src/main/resources/data/supplementaries/recipe/pedestal.json) agrees with the inspected 3.8.5 jar. Native suppression uses [NeoForge data-load conditions](https://docs.neoforged.net/docs/1.21.1/resources/server/conditions/).
