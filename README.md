# Vestige: Traditions of Lost Magic

Vestige is a Minecraft magic, exploration, and adventure mod about rediscovering lost traditions. Development currently focuses on a standalone, data-driven spell system for **Minecraft 1.21.1 / NeoForge 21.1.72 / Java 21**.

The inherited Wizardry gameplay layer has been removed. Vestige's own wands, discovery and progression remain deferred. The current catalog contains **214 native spells: 110 Iron adaptations, 100 Pathfinder 2e adaptations and four native examples**. They execute without Iron or a tabletop rules engine. The [Iron ledger](docs/design/iron-spell-conversions.md) and [Pathfinder ledger](docs/design/pathfinder-spell-conversions.md) record provenance, behavior and deliberate adaptation differences.

Spells are immutable data composed from rarity, traditions, relative traits, costs, targeting and reusable effect plans. All 214 have independent **common, uncommon, rare or mythic** assignments, native mana/recovery tuning and shared animation recipes. The latest 36 Pathfinder spells add solid constructs, physical scale/traversal, ally formations, protection budgets, private sensing and object/companion utility. The [100-spell selection](docs/pathfinder-spell-selection.md) lists their actual behavior; the [1,992-source inventory](docs/pathfinder-spell-inventory.md) remains a separate future review queue.

The [cast gallery](docs/effects-workshop.md) searches all 214 spells and shows **actual Minecraft casts only**. All **214 spells have a recorded primary cast**, including area boundaries, ordered chains, water knockback, live model copies, a combat companion, and real ice/tree/water terrain with safe per-cell cleanup. The full catalog is visible by default. Browser sketches and effect studies are removed. `runEffectsCapture` exports the isolated native client; `/vestige_magic cast` is the development entrypoint. [Spell breakdowns](docs/spell-reference.md), [balance](docs/spell-balance-review.md), [runtime design](docs/design/spell-runtime.md) and [verification](docs/development-status.md) record behavior and limits. Visuals use original procedural rendering and vanilla assets; source-mod animation assets are not imported.

## Development

Install Java 21, then:

```bash
./gradlew build
./gradlew test
./gradlew runGameTestServer
./gradlew runClient
```

Development runs also build and load Kithkyn from `../kithkyn`, checking matching Minecraft and NeoForge versions. Override with `-Pkithkyn_project_dir=/absolute/path/to/kithkyn`. Kithkyn co-loading does not implement spell interoperability.

`runServer` starts a dedicated server. `runClientJoinLocal` joins localhost:25565; use `-Pjoinport=25566` for another port. `verifyKithkynCompatibility` checks platform versions without launching the game.

In-game operators can try the catalog:

```text
/vestige_magic list
/vestige_magic effects vestige:pf2_wall_of_ice
/vestige_magic effects vestige:pf2_wall_of_ice 0
/vestige_magic cast vestige:fireball
/vestige_magic cast vestige:heartstop
/vestige_magic cast vestige:portal
/vestige_magic interrupt
/vestige_magic dispel
```

Cast Portal twice while aiming at different positions. Recast a summon spell to dismiss its cohort. The operator command bypasses resources and discovery while preserving timing; normal equipment controls and progression are deferred.

Definitions live in `src/main/resources/data/vestige/runtime_spells/`. To regenerate both explicitly authored conversion batches and verify the Pathfinder snapshot:

```bash
python3 tools/convert_irons_spells.py
python3 tools/convert_pathfinder_spells.py
python3 tools/convert_pathfinder_spells.py --check
```

The pinned source catalog is `tools/irons-spells.json`. Generation requires neither an Iron checkout nor a runtime dependency. See [CREDITS.md](CREDITS.md) for attribution.

For balance playtesting in Survival, use `/vestige_magic mana 200` then `/vestige_magic cast_balanced vestige:<spell>`. The ordinary `cast` development command bypasses mana and cooldowns. Wands, discovery and progression remain deferred.
