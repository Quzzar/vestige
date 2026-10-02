# Breaking changes

## October 2, 2026: authored spell silhouettes and rhythms

Presentation protocol is now `4`; matching clients are required. Sixteen shared shapes are appended: `SIGIL`, `HELIX`, `SHARDS`, `TENDRILS`, `FLARE`, `VORTEX`, `MOTES`, `LEAVES`, `RAYS`, `RIPPLE`, `SLASH`, `CHAIN`, `WINGS`, `FANGS`, `CLOCK` and `EYE`. Layers add optional `speed` (-3..3), `phase` (0..2π) and integer `count` (1..24). The existing five-argument Java layer constructor and older JSON supply defaults of 1, 0 and 8; network payloads include the new fields.

All 214 recipes have explicit art directions, including projectile impact feedback for direct-hit graphs. Default projectile damage also reuses its authored material for a finite contact burst. Layer composition and cosmetic lifetimes change; gameplay plans, targets, traits, costs and rarities remain unchanged. Food-to-mana now reports the actual restored amount to its player recipient. The capture fixture holds vanilla eat input rather than attempting server-only consumption, flight footage uses a third-person view, and an interrupted capture preserves its completed takes without attempting work on a closed server. Wands, discovery and progression remain deferred.

## October 2, 2026 — actual casts, owned formations and live image models

Presentation protocol is now `3`; matching clients are required. `JET`, `SPLASH` and `SHIELD` are appended to the visual shape enum. Hydraulic Push uses a water jet; Electric Arc and Chain Lightning author different color, width and duration presets. Shield uses a runic shell.

Existing `construct` and `zone` manifestations accept optional `identifiers.formation` values `vestige:tree` and `vestige:water`. Protector Tree now grows four real temporary oak logs and seventeen leaves, with conservative full-volume placement and a shared sixteen-HP pool. Losing each trunk lowers its maximum remaining protection by one quarter. Wall of Water places a five-by-three confined fluid wall with native water appearance and immersion physics. Both formations retract surviving owned cells and preserve broken/replaced cells; neither yields items or leaves unowned fluid descendants. These are deliberate mechanical changes, with existing relative traits, rarities and resource costs preserved.

NPC-owned tame summons remove the vanilla sit goal that treats non-player owners as missing; shared minion targeting and following now execute without that conflicting goal. Player-owned tame summons retain vanilla behavior.

Mirror Image uses tracked `spell_echo` entities that reuse the source entity's live model, pose and equipment. They are visual copies without AI, inventory, collision or persistent actors; direct melee interception consumes one copy. The finite three-hit/five-HP mitigation and exclusions remain unchanged.

The browser gallery exports spell metadata and actual cast videos only. Browser sketches, isolated effect-study media, canvas recording and Three.js are removed. The operator `/vestige_magic effects` remains a developer diagnostic. `encode_native_capture.py --complete` now means an actual cast for every spell; partial curated coverage uses `--check`. Display title casing is normalized separately from frozen source provenance.

## October 2, 2026 — physical ice formation

The `block_wall` manifestation places bounded, temporary terrain with animated block-model rise and collapse. New registry IDs are `vestige:temporary_ice` (block) and `vestige:spell_block_display` (entity); observing clients require this build. The ice uses Minecraft's ice model and collision, drops no items, leaves no meltwater, and clears orphaned cells after loading. Active formations and their displays are not persisted.

Wall of Ice now raises a five-wide, three-high wall of individually breakable ice blocks, safely lifts occupants, and retracts surviving owned cells after ten seconds. Breaking or replacing a cell relinquishes that cell permanently, including replacement with vanilla ice. The former entity slab, twenty-HP shared backing and fracture damage are removed. Its rare rarity, relative trait units and native resource/timing costs are unchanged. No new trait semantics or progression systems are introduced.

## October 1, 2026 — native footage and client rendering correction

`SpellEffectGallery.Phase` adds an immutable `geometry` component containing the manifestation's authored numerical values. Its former two-argument constructor remains available and supplies an empty map. Capture fixtures use construct dimensions instead of treating every box as a cube; spell execution and JSON structure are unchanged.

The shared client renderer now uses the stage pose matrix: Minecraft already applies camera rotation in shader state, so applying the event's model-view matrix to vertices again displaced effects. Standard translucent quads preserve authored surface colors instead of bleaching large surfaces through additive lightning blending. `runEffectsCapture` is an opt-in development client with a separate game directory; ordinary runs do not create capture worlds or export frames.

## October 1, 2026 — complete selected catalog and effects workshop

The 36 remaining selected Pathfinder sources now have explicit native recipes, for 214 spells: 100 Pathfinder, 110 Iron and four examples. Earlier selection proposals remain historical; current bounded forms and differences are in the conversion ledger. New shared manifestation kinds are `construct`, `zone`, `mobility`, `guard`, `sensor`, `pet_cache` and `passage`. Actions add `transpose`, `create_water`, `shape_stone`, `gather_items` and `utterance`. Derived capabilities traverse alternate modes and callbacks; construct behaviors contribute damage/heal/shield capabilities. No retired progression or wand API is restored.

`SpellWorld.canActivate` is a compatible default hook checked before charging/payment/recast. Native remote-camera and time-absence leases suppress activation; Silence gates explicitly authored utterances. Sonic Boom now marks its vocal delivery. Other existing spell gameplay plans/costs are preserved while presentation is added. Ally targeting includes existing owner-bound pets; additional consent requires ownership, team membership for mobs, or team membership plus `/vestige_magic accept_magic true` for players.

Presentation protocol is now `2`; all clients in a session must run the matching build. Six visual shapes are appended to the original seven enum values: box, body, tree, wave, rain and veil. A separate bounded `spell_sense` client payload handles camera, movement prediction and perception leases. Private cues stay owner-only. Camera movement does not teleport the caster. Scale uses physical dimensions and owned reach modifiers; terrain leases restore before unload/save. Pet Cache keeps a persisted emergency-return journal with original UUID/inventory, not a serialized active spell.

`/vestige_magic effects <spell> [phase]` and the local browser workshop expose every visual phase independently. Browser exports are recipe previews, not Minecraft recordings. Actual client appearance and multiplayer transitions still require visual inspection; current executed results are recorded in development status.

## October 1, 2026 — second Pathfinder batch and reusable utility actions

Sixteen new `vestige:pf2_*` identities extend the native catalog to 178 spells (64 Pathfinder, 110 Iron and four examples). Previous 162 definition bytes and tuning remain unchanged. New reusable actions are `dwell_heal`, `random_teleport`, `detect_magic` and `inspect_item`. Occupancy healing and random relocation derive `heal` and `teleport` capabilities as well as their action IDs. Numerical inspection answers are available as `vestige:magic_found`; player messages go only to the caster. No discovery or progression state is created.

The teleport action accepts optional `grounded=1` for loaded, dry supported placement; its default retains previous behavior. Queries accept `exclude_origin=1` to keep their own backing subject out of recipient caps. Field `particles=0` disables generic decoration, and `inert=1` makes a summoned proxy silent with disabled AI. Defaults preserve existing recipes. Taming now precedes authored summon stats so vanilla wolf taming cannot overwrite their health/attack tuning. Following fields close if the captured creature changes dimensions.

Invisibility is composed from a finite, particle-silent attached lease that refreshes vanilla invisibility for two ticks; committed outgoing damage ends its refresh. Expiry/dispel stops renewal without removing an external longer invisibility effect. Ground patches approximate terrain through finite slowing rather than block replacement, illusion proxies are explicit limited lures, and Heal/Harm currently author only the ranged recipient form. See the conversion ledger for every adaptation difference.

## October 1, 2026 — shared native presentation

`SpellEffects.ForEach` adds optional `SpellVisual`; its former two-argument constructor remains available. `SpellEffects.Manifestation` adds optional `SpellVisual`; the former five- and nine-argument constructors remain available. `SpellEffects.Visual` is a new cosmetic effect. Code with exhaustive switches over the sealed effect repertoire must handle it. Visuals contribute no gameplay capability or implicit trait behavior.

`SpellWorld.present` and `ManifestationHandle.bindingsExhausted` are default methods, preserving existing adapters. Presentation receives the same ordered subjects selected for gameplay. The final owned binding's completion/expiry can notify presentation without destroying its gameplay backing. Native client-bound `vestige:spell_visual` payloads synchronize cosmetic state; they introduce no cast, wand, discovery or progression channel. Fourteen Pathfinder definitions add presentation data while their gameplay plans, costs and provenance remain unchanged.

## October 1, 2026 — Pathfinder reference metadata and first batch

The native catalog now contains 162 definitions: 110 Iron, 48 Pathfinder 2e and four native examples. New `vestige:pf2_<source_name>` identities keep similarly named spells separate; existing 114 definition files are preserved.

`SpellSource.school()`, `castType()` and `cooldownTicks()` now return `Optional<ResourceLocation>`, `Optional<String>` and `OptionalInt`, respectively. The record adds `Optional<Reference>` for source system, edition, publication, rank, cantrip flag, source rarity and URL. The former six-argument constructor remains available; existing Iron JSON still parses unchanged. Consumers of the changed accessors must unwrap them. A tabletop source needs no invented school or cooldown. These reference facts do not tune native spells.

The general damage action accepts `identifiers.damage_type`; omitted types retain the old indirect-magic behavior. Typed fire/cold/lightning use Minecraft damage tags while retaining native magical event classification. `TargetSpec.Selection.EVENT_ATTACKER` resolves the new attacker UUID fact for retaliatory plans; damage-event subject semantics are unchanged. New finite `control` effects claim existing mobs and release them on cleanup instead of treating them as summoned bodies to discard. Nearby queries optionally include the caster with `include_self=1` for relationship `any`. Default behavior and previous spell definitions are unchanged.

## September 30, 2026 — complete native balance pass

All 114 spells now author independent native mana, charge, and `cooldown` costs. `SpellCost.Cooldown` is a new sealed cost subtype and `SpellRuntime.Status.COOLDOWN` identifies recovery rejection. Original Iron cooldown metadata remains historical provenance. Paid casting starts per-actor, per-spell recovery after successful payment; continuation inputs share it. A paid effect failure retains recovery. One active charge/channel per actor is allowed. Reload/restart clears recovery with other active session state.

The development `cast` command bypasses both resources and recovery. `cast_balanced` enforces native costs/recovery, and `mana` controls the 200-energy test seam. These controls do not add wands, discovery, passive regeneration, or progression.

Outcome coefficients, channel/field pulse counts, status/control/protection durations and charges, summon stats/lifetimes, ranges, target counts, and weapon fractions are tuned. Trait units and descriptive profiles are preserved. Shared per-cast hit limits prevent dash, volley, and meteor overlaps multiplying damage. Summon replacement now compares the original target, cleaning up previous backing cohorts. Fields use elapsed intervals and can pulse at expiry before cleanup.

Flaming Barrage is uncommon after its finite five-shot tuning; Summon Vex remains rare. See `docs/spell-balance-review.md` for every spell's role, costs and rationale.

## September 30, 2026 — spell rarity

`SpellDefinition` now includes `SpellRarity`: common, uncommon, rare, or mythic. The former Java constructor signatures remain available and default to common; the record's component layout changes. Native JSON accepts optional `rarity` with a common default and rejects unknown or non-text values. All 114 checked-in spells author rarity explicitly. `vestige:spell/rarity` is available as a text condition fact. Rarity does not change trait resolution or implicitly scale effects.

Trait magnitudes and totals do not determine rarity or measure power. Interposing Earth retains its original profile (earth 5, stone 3, abjuration 4, conjuration 2) and execution plan. The balance audit records resolved parameters and boost responses, with no point ceilings or weighted-load rules. See `docs/design/spell-balance.md` for equivalent formula calibration and comparisons within rarity.

## September 30, 2026 — standalone native spells

The project now targets Minecraft 1.21.1, NeoForge 21.1.72, and Java 21 in a single `src/` project. Fabric/Forge 1.20.1 artifacts and the old multi-loader source layout are retired.

The inherited Wizardry API and gameplay layer have been removed at the project owner's request. Legacy registry IDs, wands/upgrades, spellbooks, workbench, discovery, XP tiers, artifact predicates, attachments, networking, world generation, recipes/loot, and renderer/mixin/platform APIs are no longer available. Existing worlds and addons that relied on this content need migration; this is an early development baseline.

Native spells use `com.quzzar.vestige.magic`, immutable definitions, and `data/<namespace>/runtime_spells/` effect graphs. The pinned 110-spell Iron catalog is adapted to native `vestige:<source_path>` IDs with source provenance. Those IDs describe Vestige execution and do not alias Iron's registries or require Iron to be installed.

Normal native wand controls, discovery, and progression are deferred. Operator `/vestige_magic` is the current playtest entrypoint. See `docs/design/spell-runtime.md` and `docs/design/iron-spell-conversions.md` for the supported grammar and explicit adaptation limits.
