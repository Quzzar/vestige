# Vestige: Traditions of Lost Magic

Minecraft magic, exploration, and adventure mod. The active baseline is Minecraft 1.21.1, NeoForge 21.1.72, and Java 21. This is one Gradle project under `src/`; the former common/Fabric/Forge layout is retired.

## Current scope

Build native spells first. The inherited Wizardry gameplay, wands, upgrades, spellbooks, discovery, XP tiers, artifacts, world generation, recipes, networking, attachments, and compatibility shims have been removed. Vestige's own wands, discovery, and progression are explicitly deferred. Do not reconnect the old systems to the native runtime.

The 110 default-enabled spells in the pinned Iron catalog have explicit native adaptations. Iron is a behavioral source for this content; it is not required to execute it. Exact source spell/school IDs and the pinned revision remain provenance. Future interoperability with foreign Iron casts is a separate adapter concern. Pathfinder 2e batches add independently authored native adaptations with frozen AoN provenance. Source rank/cantrip/rarity are inert reference facts; native rarity and costs are independent.

## Structure

- `src/main/java/com/quzzar/vestige/VestigeMod.java` — NeoForge bootstrap; registers reusable native delivery entities.
- `src/main/java/com/quzzar/vestige/magic/definition/` — immutable spells, traditions, traits, costs, targeting, provenance, and primitive catalog.
- `magic/condition/`, `magic/expression/`, `magic/effect/` — reusable conditions, numerical expressions, and composed plans.
- `magic/runtime/` — server-thread casts, recasts, bindings, manifestations, pending outcomes, causal history.
- `magic/presentation/` — layered immutable visuals, bounded client payloads and shared procedural rendering; author presets in `tools/spell_visuals.py`.
- `magic/world/` — Minecraft targeting, actions, delivery/backing entities, client renderer, private spaces, events, and operator commands.
- `src/main/resources/data/vestige/runtime_spells/` — native Iron/Pathfinder adaptations and four native examples; generated totals are in the conversion/reference/balance ledgers.
- `src/main/resources/data/vestige/dimension*/` — private-space dimension definition.
- `src/main/java/com/quzzar/vestige/gametest/` and `magic/world/PrivateSpaceTest.java` — Minecraft behavior tests.
- `src/test/java/` — JUnit model/runtime and complete catalog parsing tests.
- `tools/irons-spells.json` — frozen 110-spell source catalog.
- `tools/pathfinder-spells.json` — frozen AoN references for implemented Pathfinder batches; the broader inventory remains a separate review queue.
- `tools/convert_pathfinder_spells.py` — explicit PF2 recipes and ledger; `--check` detects drift.
- `tools/spell_authoring.py` — shared source-independent effect-graph authoring helpers.
- `tools/convert_irons_spells.py` — deterministic explicit recipes; regenerates converted JSON and its ledger. No fallback conversion and no upstream checkout required.
- `src/main/templates/` — expanded mod metadata; `docs/design/` and `docs/research/` — decisions and historical evidence.

## Commands

```bash
./gradlew build
./gradlew test
./gradlew verifyKithkynCompatibility
./gradlew runGameTestServer
./gradlew runClient
./gradlew runServer
./gradlew runClientJoinLocal
python3 tools/convert_irons_spells.py
python3 tools/convert_pathfinder_spells.py
```

Java 21 is used by both the toolchain and Gradle daemon. Development runs build/load the sibling Kithkyn project, verifying matching Minecraft/NeoForge versions. Override its location with `-Pkithkyn_project_dir=/absolute/path`. `runClientJoinLocal` defaults to localhost:25565; override with `-Pjoinport=25566`. There is no supported `runData` task.

## Design and conventions

Check `BREAKING_CHANGES.md` before changing public APIs. Read `CONTEXT.md`, `docs/design/trait-catalog.md`, `docs/design/spell-runtime.md`, `docs/design/iron-spell-conversions.md`, and `docs/design/pathfinder-spell-conversions.md` before changing spell structure. For balance changes, read `docs/design/spell-balance.md` and `docs/spell-balance-review.md`; author outcomes in the relevant `tools/convert_irons_spells.py` or `tools/convert_pathfinder_spells.py` recipes and costs in `tools/spell-balance-policy.json`. Preserve relative trait units while tuning actual outcomes, timing, and constraints. Research/prototype documents retain historical proposals; their headers identify superseded scope decisions.

- Mod ID `vestige`, root package `com.quzzar.vestige`; use `VestigeMainMod.location(path)`.
- Spell definitions are immutable. A cast owns resolved traits, scalar state, target anchors, and its continuation.
- Traits are a flat, open namespaced repertoire. Effects explicitly read scaling traits. `volatile` is the sole trait with inherent runtime semantics; another semantic trait requires an explicit decision with the project owner.
- Derive capabilities from executable plans, including callbacks and bindings. Delivery/outcome words are not traits.
- Compose reusable effects and targeting in data. Avoid a class or bespoke runtime branch per spell.
- Own cleanup of spawned entities, transient modifiers, callbacks, and bindings. Server stop/reload closes active state. Private rooms and container lock ownership are persistent; ordinary active spells are not serialized.
- Calculating damage/healing outcomes may be changed before commit. Preserve causal lineage when reactions produce further events.
- Preserve historical attribution in `CREDITS.md` and `LICENSE.md`; do not copy Iron code/assets as part of recipe authoring.

## Verification

Run `test` for model/runtime/catalog changes, `build` for packaging, and `runGameTestServer` for world behavior. Add targeted behavior tests for new mechanics; a successful parse is not gameplay verification. For catalog changes, regenerate both conversion tools and run `tools/convert_pathfinder_spells.py --check`. Regenerate and run `--check` for `tools/document_spells.py`, `tools/audit_spell_balance.py`, `tools/document_spell_balance.py`, and `tools/document_pathfinder_inventory.py`; run `tools/test_spell_balance.py`. Completion requires every policy entry and generated definition to agree, appropriate unit/world tests to pass, and actual results/limitations recorded in `docs/development-status.md`. Inspect actual client appearance before claiming presentation verification.

Operator `cast` bypasses resources, cooldowns and discovery while retaining timing/recasts. `cast_balanced` enforces native mana/recovery; `mana [0..200]` controls test energy. See `docs/design/spell-runtime.md` for payment, cooldown, and channel rules. These controls are the development entrypoint while wands/progression remain deferred.
