# Credits

Vestige originated from these projects; their historical attribution and license notices are retained after retiring the inherited gameplay:

- [Electroblob's Wizardry](https://github.com/Electroblob77/Wizardry), created by Electroblob.
- [Wizardry Redux](https://github.com/Binaris00/Wizardry), led by Binaris and built from the original mod and later porting work.

Upstream credits retained from Wizardry Redux:

- Electroblob — original creator of Electroblob's Wizardry.
- WinDanesz — maintainer of the Minecraft 1.12.2 version.
- Min01 — initial Minecraft 1.19 port and logic foundations.
- 19 — initial Minecraft 1.20.1 official-port work.
- NinjaFrito — new textures for the Redux port.
- All upstream contributors who provided code, bug reports, translations, suggestions, and testing.

Vestige's own changes are authored and maintained by Quzzar and future Vestige contributors. The shared visual library uses independently authored procedural geometry and vanilla Minecraft particles/sounds; no Iron, Pathfinder or Wizardry artwork was imported for it.

Native spell recipes draw behavioral inspiration from [Iron’s Spells ’n Spellbooks](https://github.com/iron431/irons-spells-n-spellbooks), by iron431 and its contributors. The 110-spell catalog is pinned to commit `e4056af90302d37eb1739f5ff05020b020e6e252` (3.16.3 / Minecraft 1.21.1). The recipes are independently authored Vestige adaptations; Iron implementation code and assets have not been copied. Individual source links and differences are recorded in `docs/design/iron-spell-conversions.md`. The native art pass also reviews Iron's recognizable jaw and singularity silhouettes as design references, documented in [spell art direction](docs/spell-art-direction.md); those compositions are independently rendered without imported assets or implementation code.

The Pathfinder batches draw behavioral inspiration from **Pathfinder Second Edition**, by Paizo Inc., specifically *Player Core*, *Player Core 2*, and *Rage of Elements*. [Archives of Nethys](https://2e.aonprd.com/Spells.aspx) provided the verified spell references; [Wanderer’s Guide documentation](https://docs.wanderersguide.app/api-reference/introduction) was examined as an alternate reference entrypoint. The 100 native recipes and their tuning are independently authored; no Pathfinder implementation, artwork, or full rules text was imported. Exact spell/page/errata references and differences are in [the conversion ledger](docs/design/pathfinder-spell-conversions.md) and [first-batch research](docs/research/pathfinder-spell-batch.md) and [expansion research](docs/research/pathfinder-expansion-batch.md) and [diverse-batch research](docs/research/pathfinder-diverse-batch.md). Source notices are retained by reference to [AoN’s license and attribution ledger](https://2e.aonprd.com/Licenses.aspx) and [Paizo’s licensing overview](https://paizo.com/licenses). Pathfinder names and publisher attribution identify the inspirations; they do not claim publisher endorsement.

The broader [Pathfinder candidate inventory](docs/pathfinder-spell-inventory.md) uses spell identity and publication metadata from the community-maintained [Foundry Virtual Tabletop PF2e system](https://github.com/foundryvtt/pf2e), by its maintainers and contributors, pinned to commit `6b08de09b3d2b80db785bb6893df9da04e8cae86`. Per-record publication license and Remaster markers are retained as source metadata. The inventory includes no rules descriptions, formulas, implementation code or artwork and adds no runtime dependency on Foundry. The source project's [license and attribution notices](https://github.com/foundryvtt/pf2e/blob/6b08de09b3d2b80db785bb6893df9da04e8cae86/LICENSE) remain available at the pinned revision.

The [32-spell visual-library research batch](docs/research/pathfinder-visual-library-batch.md) reviews additional Archives of Nethys references from *Player Core*, *Player Core 2*, *Rage of Elements* and *Secrets of Magic*. Its behavioral summaries and proposed Minecraft adaptations are independently written; selected sources now have independently authored native recipes; unselected candidates remain research evidence.

The [100-source Pathfinder selection](docs/pathfinder-spell-selection.md) combines the original 64 implemented references with [36 additional directly reviewed AoN sources](docs/research/pathfinder-diverse-batch.md), including current remaster identities and three explicitly legacy entries. All 36 added sources now have explicit native recipes. The summaries and Minecraft designs are original paraphrases/adaptations; no rules paragraphs, source stat blocks, implementation code or assets were imported. Source review alone does not create a native conversion.
