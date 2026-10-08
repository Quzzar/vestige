# Standing Stone inset menu

**Historical review, superseded by the [number-first resource menu and shared mana HUD](../standing-stone-resources/README.md).** Original screenshots, package hashes and verification remain unchanged below.

October 6, 2026. This is the owner's refinement of the [preceding button list](../standing-stone-button-list/README.md): narrower, with explicit ellipses, a centered current name and subtle unboxed pencil, matching recessed name/signature panels and pagination only when it is needed.

The native panel is at most **210 GUI units** wide, down from 240. Header and footer have equal widths, heights and edge treatment, with their text centered on the same axis. The pencil has a 12×12 interaction area, a quiet hover treatment and a visible keyboard-focus outline. Full names remain available through tooltips and narration. Editing still uses the native name field and Save; blank/unchanged input is disabled. The list uses ordinary Minecraft buttons, with a reserved right-hand symbol and whole charge. More than eight destinations produce pages; smaller networks shrink the panel and omit both arrows and the page count. A source-only network retains its name and signature without a self destination.

## Native cost previews

These are **unedited actual Minecraft framebuffer captures**, not browser sketches. Each preview shows the same three real endpoint names, with two affordable rows and one disabled row. Preview amounts are illustrative, not accepted resource exchange rates. Native hover tooltips are visible over the list.

| Resource | Symbol | Display unit | Preview amounts |
| --- | --- | --- | --- |
| XP | Green-tinted vanilla XP orb | XP points | 12, 18, 24 |
| Health | Vanilla full heart | Whole hearts | 1, 2, 3 |
| Mana | Vanilla violet Amethyst Shard | Mana units | 10, 20, 30 |
| Hunger | Vanilla full food icon | Whole hunger icons | 2, 4, 6 |

![XP cost preview](/Users/quzzar/Projects/vestige/docs/art/standing-stone-insets/native/cost-xp.png)

![Health cost preview](/Users/quzzar/Projects/vestige/docs/art/standing-stone-insets/native/cost-health.png)

![Mana cost preview](/Users/quzzar/Projects/vestige/docs/art/standing-stone-insets/native/cost-mana.png)

![Hunger cost preview](/Users/quzzar/Projects/vestige/docs/art/standing-stone-insets/native/cost-hunger.png)

**Live travel still charges ordinary XP**, using the previously accepted distance formula. The alternate-cost screen is an opt-in native appearance fixture: it sends no travel, rename or paging requests and spends no resources. Health/mana/hunger conversions, nonlethal health affordability, food accounting and Pearl/material payment selection remain separate unfinished mechanics. No new art asset or external mod dependency is introduced by these symbols.

## Verification and packaging

The separate final Java 21 production build and Kithkyn version check pass. All **114 shared-checkout unit tests** pass with no failures/errors/skips. The world mechanics suite was not rerun for this client-menu refinement; the preceding XP payment pass retains its eighteen focused world tests. Unrelated concurrent shared-checkout work contributes some of the current unit tests and is excluded from the installed UI package.

All **24 native menu assertions** pass and all **14 final 1920×1080 framebuffer views** were visually inspected at GUI scales three and four. They cover name editing, real Save and page payloads, twenty peers on three pages, exact affordability, zero-XP disabling, width/ellipsis limits, source-only and three-destination lists without pagination, and all four cost symbols. An actual direct destination click charges thirteen XP points from fourteen and arrives with one remaining. Clicking each appearance-only cost preview verifies that no travel/payment occurs.

The capture completes its assertions and writes its metadata before the isolated fresh-world client stalls during shutdown in Minecraft's `ChunkMap.processUnloads`. A thread snapshot was retained, and only that preview process was terminated. The subsequent independent production build completes successfully. This limitation concerns test-world shutdown; it is not a successful clean-exit claim or remote multiplayer validation.

The review artifact applies only the freshly compiled menu, cost-display and opt-in capture class families, plus four cost-unit translations, to the preceding verified installed package. Every other package member is byte-identical to that baseline, preserving unrelated approved gameplay/artwork and excluding concurrent unfinished implementation. All **fifteen loaded class hashes** match the reviewed jar; the approved apparatus tint and all **252 Plinth/Spellstone block/item models** are preserved. [`verification.json`](verification.json) pins the package, source hashes, inspected captures, test counts and complete packaging scope. The review jar is `build/standing-insets-review/vestige-0.1.0.jar`.

Installed in **Prism → Kithkyn Testing**, SHA-256 `931f89b3554d2f41302b97b08a867120678feefda8a0eacb65c4387c6298d7c8`. [`prism-install.json`](prism-install.json) records the atomic single-jar replacement and verified backup. Minecraft was not restarted; other mods and existing worlds were not changed.

![Actual short destination list without paging](/Users/quzzar/Projects/vestige/docs/art/standing-stone-insets/native/short-list.png)

![Source-only network with matching name/signature panels](/Users/quzzar/Projects/vestige/docs/art/standing-stone-insets/native/only-current-stone.png)

![Actual paginated list at GUI scale four](/Users/quzzar/Projects/vestige/docs/art/standing-stone-insets/native/list-narrow.png)

![Native name editor](/Users/quzzar/Projects/vestige/docs/art/standing-stone-insets/native/editing-wide.png)
