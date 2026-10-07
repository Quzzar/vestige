# Optional pedestal conversions · October 6, 2026

[Approved shipment and exact-package verification](release.md) supersede this entry's pre-shipment status.

Accepted implementation: one Iron's Spells 'n Spellbooks pedestal or one Supplementaries pedestal converts into one **Stone Bricks Plinth** in any crafting-grid slot. Each inspected provider exposes only a Stone Brick pedestal item; Supplementaries' connected models are stacking states. Neither provider is a Vestige dependency. Exact provider/item conditions guard both recipes and their recipe-book unlocks; production code uses no provider API.

## Verified matrix

Minecraft 1.21.1, Java 21, fresh isolated worlds, Kithkyn disabled for standalone verification:

| Providers | NeoForge test version | Result |
|---|---|---|
| Neither | 21.1.72, unchanged project baseline | Build, 121 unit tests and all four focused GameTests pass |
| Iron's only | 21.1.247, ValeCraft version | All four focused GameTests pass |
| Supplementaries only | 21.1.247, ValeCraft version | All four focused GameTests pass |
| Both | 21.1.247, ValeCraft version | All four focused GameTests pass |

The four tests comprise two existing apparatus construction/stacking tests and two new conditional conversion tests. They verify recipe and advancement presence/absence, both crafting-grid sizes and every grid position, normal recipe-manager selection, exactly one Stone Bricks output, no crafting remainder, and rejection of multiple occupied inputs/native blocks/other masonry. The existing tests retain coverage of all 72 native construction recipes and mismatched slab rejection. These are four unique tests executed in four provider configurations, not sixteen distinct tests.

Actual ValeCraft jars were read from the existing local pack: Spellbooks **3.16.2**, Iron's Library **2.1.0**, GeckoLib **4.9.2**, Curios **9.5.1**, Player Animator **2.0.4**, Supplementaries **3.8.5**, and Moonlight **3.3.0**. An external temporary Gradle init script supplied these only to the selected test runtime. Neither build configuration nor dependency metadata gains either provider. Installed modpacks and existing worlds were not changed.

The initial both-provider attempt on 21.1.72 could not start Minecraft because the installed provider libraries require newer NeoForge versions: GeckoLib 21.1.150, SableCompanion 21.1.80 and Iron's Library 21.1.200. Its Gradle task nevertheless reported success; it is explicitly **not** counted as a passing world test. Subsequent provider tests use ValeCraft's actual 21.1.247 loader through a command-line override; Vestige's baseline remains 21.1.72.

## Retained evidence and reproduction

[Verification manifest](verification.json) records source/resource/package hashes, actual provider jar hashes, the test commands and unit totals. [Log excerpts](results.txt) retain the batch completion markers and initial fixture failure. Full logs and fresh worlds remain under `/private/tmp/vestige-pedestal-compat-*`; their paths are recorded in the manifest. The final production jar packages all four compatibility JSON resources, with no dependency declaration for either provider.

Each fixture ran `runGameTestServer` with `-Pwith_kithkyn=false`, `-Pgametest_filter=pedestal_compat|apparatus_recipes`, a separate world directory, and an external init script selecting `none`, `irons`, `supplementaries` or `both`. Provider runs additionally use `-Pneo_version=21.1.247` and a separate build directory. The final no-provider run executes `build runGameTestServer` on the original baseline. The manifest retains the init script and full arguments for reproduction.

All **544** generated apparatus files other than the shared translation file match the authoring tool's expected bytes. The full apparatus `--check` encounters unrelated existing `en_us.json` drift from concurrent work; this compatibility change does not regenerate or overwrite those translations. No geometry/assets were altered. No actual client appearance, remote multiplayer, installed-pack shipment or in-world pedestal replacement is claimed by this recipe-only change.
