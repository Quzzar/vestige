# Vestige

*Traditions of Lost Magic*

A Minecraft magic, exploration and adventure mod about rediscovering lost traditions.
Vestige currently focuses on native spells: **214 independently authored spells**, with
distinct effects, native balance and a recording of every spell being cast inside Minecraft.

[Source](https://github.com/Quzzar/vestige) · [Issues](https://github.com/Quzzar/vestige/issues) · [Spell reference](docs/spell-reference.md) · [Cast gallery](docs/effects-workshop.md)

[![Build](https://github.com/Quzzar/vestige/actions/workflows/build.yml/badge.svg)](https://github.com/Quzzar/vestige/actions/workflows/build.yml)

A NeoForge mod for **Minecraft 1.21.1**. Vestige grew from Electroblob's Wizardry and
Wizardry Redux; the inherited gameplay systems have been retired in favor of its native runtime.

## What it does

- **214 spells.** 110 adaptations inspired by Iron's Spells, 100 inspired by Pathfinder
  Second Edition, and four native examples. Each has an explicit executable plan and source
  provenance. Iron and a tabletop rules engine are not required to run them.
- **Magic with physical consequences.** Projectiles, area bursts, ordered chains, rays,
  movement, protections, companions and temporary terrain. Wall of Ice raises a real,
  breakable wall; cleanup preserves cells that players have broken or replaced.
- **Composed spell effects.** Original procedural visuals combine 32 shared shapes with
  vanilla particles, sounds and entity models. Spells have authored silhouettes, materials
  and motion. Source-mod animation code and assets are not imported.
- **Independent rarity and balance.** Common, uncommon, rare and mythic spells have native
  mana and recovery costs. Actual outcomes, timing and constraints determine balance;
  trait numbers describe relative scaling rather than a power budget.
- **Actual cast recordings.** The searchable gallery includes a primary Minecraft cast for
  every spell, showing targets, area boundaries, status effects, movement and world changes.
  Recordings are silent excerpts with observed results, not browser recreations.
- **Leyline crafting.** Craft one Spellstone and four or eight Plinths from Chiseled Stone
  Bricks and Iron. All 214 spell recipes support native hints and dynamic Cross/Diagonal
  layouts with independent spacing and height steps. Crafted scrolls store Amplify, Range,
  Area and Casting Cost. Plinths hold separate embedded materials; their numeric bonuses
  remain future work. Only the new Spellstone and Plinth identities remain; use a fresh world.

The [spell reference](docs/spell-reference.md), [balance review](docs/spell-balance-review.md)
and [art breakdown](docs/spell-art-direction.md) describe the complete catalog.

## Requirements

| | |
| --- | --- |
| Minecraft | 1.21.1 |
| Loader | NeoForge; the verified development baseline is 21.1.72 |
| Java | 21 |
| Sides | Both. Install on the server and every client. |
| Other mods | None required; Kithkyn is optional. |

## Try the development build

1. Download `vestige-<version>.jar` from a successful [Build workflow](https://github.com/Quzzar/vestige/actions/workflows/build.yml) run's `vestige-jars` artifact, or build it locally.
2. Install NeoForge for Minecraft 1.21.1 and put the jar in the server and clients' `mods` folders.
3. Enter a world with operator permission and use the spell commands below.

There is no published release yet. Native scroll casting, fragment discovery and Spellstone
crafting are playable; [ritual instructions and layouts](docs/design/ritual-crafting.md) explain the loop.
Native wands and equipment progression remain deferred. Operator commands also support testing. The old equipment and progression
systems are not connected to these spells.

## Commands

```text
/vestige_magic list
/vestige_magic cast vestige:fireball
/vestige_magic cast vestige:pf2_wall_of_ice
/vestige_magic cast vestige:heartstop
/vestige_magic scroll vestige:fireball
/vestige_magic mana 100
/vestige_magic cast_balanced vestige:fireball
/vestige_magic interrupt
/vestige_magic dispel
```

`cast` bypasses resources and recovery while retaining casting timing and recasts.
`cast_balanced` enforces native mana and recovery. Recast a summon to dismiss its cohort;
cast Portal twice while aiming at different positions to connect two points.
See [the runtime guide](docs/design/spell-runtime.md) for payment, targeting and channel rules.

## Try the apparatus

Every scroll displays **Unknown Scroll** until you successfully cast and identify its spell.
Ritual recipes show question marks in JEI/EMI until you successfully craft that spell;
your crafting history reveals its actual ingredients independently of identification.
Both kinds of knowledge persist for each player.

Find **Spellstone** and **Plinth** in Creative's Functional Blocks tab, or craft their
[construction grids](docs/leyline-playtesting.md#build-the-apparatus). Click the top to offer
an ingredient; click a side with a block item to install a material socket. Sneak-click a
side with an empty hand to recover the material. Empty-hand center activation performs
fragment discovery or ingredient crafting; a reference scroll supplies hints and remains
intact when collecting the result. [Leyline playtesting](docs/leyline-playtesting.md) records
layout bounds, selection, worked casting examples and actual Minecraft captures.

## Watch the spells

From the repository root:

```bash
cd tools/effects-viewer
bun install --frozen-lockfile
bun run dev
```

Open [localhost:5175](http://127.0.0.1:5175/) and keep the server running. Search all 214
spells, play a cast, download its MP4 or copy its native cast command. The viewer plays saved
footage; it does not launch Minecraft. [The gallery guide](docs/effects-workshop.md) explains
how to record updated casts.

## Compatibility and current limits

Kithkyn is an optional companion mod. Default development runs build and co-load the sibling
Kithkyn checkout after checking matching platform versions. This does not implement spell
interoperability with Kithkyn, Iron or other mods.

The native conversions adapt source behavior to Minecraft. Custom creature and weapon artwork,
character casting gestures and wider multiplayer playtesting remain future work. The source
ledgers record deliberate differences; [development status](docs/development-status.md) records
the tests, recordings and practical limits.

## Development

Java 21 and the Gradle wrapper:

```bash
./gradlew build
./gradlew test
./gradlew check build runGameTestServer -Pwith_kithkyn=false
./gradlew runClient
./gradlew runServer
```

`runClient` and `runServer` co-load `../kithkyn` by default. Use `-Pwith_kithkyn=false`
for standalone runs, or `-Pkithkyn_project_dir=/absolute/path/to/kithkyn` to choose the
companion checkout. `runClientJoinLocal` joins localhost:25565; override with `-Pjoinport=25566`.

Definitions live in `src/main/resources/data/vestige/runtime_spells/`. Regenerate the authored
catalog with:

```bash
python3 tools/convert_irons_spells.py
python3 tools/convert_pathfinder_spells.py
python3 tools/convert_pathfinder_spells.py --check
```

Read [docs/](docs/README.md) and [AGENTS.md](AGENTS.md) before changing a system.
[Repository and builds](docs/repository.md) describes the GitHub checks and build artifacts.

## Contributing

Issues and pull requests are welcome at the [issue tracker](https://github.com/Quzzar/vestige/issues).
Fork, branch and open a PR. CI verifies the catalog, unit and Minecraft behavior tests,
cast recordings and gallery build on every pull request and push to `main`.

## License

The inherited [license](LICENSE.md) and attribution notices are retained. Vestige's native
spell recipes and procedural visual compositions are independently authored.

Vestige is not affiliated with Mojang, Microsoft, Paizo or the authors of its source inspirations.

## Credits and inspiration

- [Electroblob's Wizardry](https://github.com/Electroblob77/Wizardry), by Electroblob,
  and [Wizardry Redux](https://github.com/Binaris00/ElectroblobsWizardryRedux), by Binaris
  and contributors, are the project's historical origins.
- [Iron's Spells 'n Spellbooks](https://github.com/iron431/irons-spells-n-spellbooks),
  by iron431 and contributors, inspires the 110-spell Iron catalog and visual design references.
- Pathfinder Second Edition, by Paizo, inspires 100 native adaptations. [Archives of Nethys](https://2e.aonprd.com/Spells.aspx)
  provides the canonical references, with Wanderer's Guide and the Foundry PF2e project
  informing taxonomy and the broader source inventory.

[CREDITS.md](CREDITS.md) retains the full historical attribution and pinned sources.
The [Iron ledger](docs/design/iron-spell-conversions.md) and
[Pathfinder ledger](docs/design/pathfinder-spell-conversions.md) document native behavior
and differences from their inspirations.
