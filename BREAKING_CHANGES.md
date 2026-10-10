# Breaking changes

## October 9, 2026: crafted Wardweave imbuements

Wardweave's existing ordered, same-color eight-offering recipe now compiles retained local sockets into four independent choices: Warded, Enduring, Quickened and Reinforced. All sixteen sets have shared full-set names and source-local trait/recovery adjustments. Reinforced carriers have 120 durability and repair 30 per Callous Thread; other sets retain 80/20. Existing plain robes remain valid without rewriting. Saved variants use the shared bounded item-selection format; invalid families, selections or carrier budgets cannot activate a ward. All variants share wearer-owned recovery. Public plain-color display IDs are preserved, and both optional viewers derive 256 color/variant patterns from the live palette. Matching client/server builds are required for new selections. Approved artwork, mana capacity and the unmodified ward behavior remain unchanged.

## October 9, 2026: Fractious Fluxed Flint

Flint / Magma Block now selects the third craft-time imbuement, Fractious. It doubles the target-relative repair cap from 25% to 50% and intrinsic volatility from 10% to 20%, retaining the total budget and one-to-one wear. It composes with Stabilized/Reinforced in eight exact sets; Restive, Audacious and Impetuous name the new combinations. Existing saved variants, budgets and public recipe IDs remain unchanged. No migration or refill is introduced. Both optional viewers derive all eight construction patterns from the live palette. Matching client/server builds are required for the new selection.

## October 8, 2026: crafted Fluxed Flint imbuements

Fluxed Flint construction now compiles retained local socket selections: Dissentient Diamond / Quartz selects Stabilized, and either Netherite Ingot / Iron selects Reinforced. The four exact variants have 128/96/192/144 repair points and 10/5/15/7.5% intrinsic failure chances. Existing ordinary stacks remain valid without rewriting or migration. Imbued stacks store shared bounded item selections and an authored maximum-durability component; invalid selections or forged budgets reject repair. Names use the shared adjective catalog, including Braced for the full combination. Base ingredients, artwork, rarity, shimmer, operation cap, exact copies and independent volatility order remain unchanged. Optional recipe viewers add three public variant patterns.

## October 8, 2026: revised Fluxed Flint ingredient chain

Dissentient Diamond now crafts through the four-seat inner ritual Diamond → Gunpowder → Wither Skeleton Skull → Gunpowder, producing one ingredient. Fluxed Flint construction becomes Flint → Netherite Ingot → Dissentient Diamond → Netherite Ingot. Whole quarter-turns are equivalent; duplicate offerings occupy separate Plinths. The provisional Diamond Block/Echo Shard recipe is removed. Both public viewer patterns follow the new recipes. Flint's violet accents become diamond blue, preserving its silhouette/model, repair budget, volatility and exact target-copy semantics. Saved item and network formats are unchanged; there is no migration or installation.

## October 8, 2026: Kairotic Hourglass and atomic device payments

Adds native `vestige:kairotic_hourglass`, its four-offering ordered ritual, all 36 settled imbuement sets and server-owned position history. Saved item data contains trusted selection IDs and bounded physical geometry. The shared sealed `SpellCost` adds `Experience(int)` and the JSON `experience` cost; exhaustive consumers must handle it. `CastReservation.Atomic` / `SpellWorld.payAndCommit` allow fallible device movement with native payment rollback; adapters without transaction support reject those sources before spending. Ordinary reservations retain their prior semantics. Mana-only/resource-only payment no longer replaces unchanged inventory stacks with copies, preserving reserved source identity. Existing spell payment exchange order, old items and protocol formats are unchanged.

## October 8, 2026: shared magical adjective presentation

Scroll, wand and staff stored-spell labels now use the shared `MagicAdjectives` prefix helper and a generated naming catalog. Single adjustments retain their words and degree labels; complete mixtures now receive one italicized adjective from an exact alias or the reserved Confluent fallback. Rarity styling and identification gates remain intact. The authored Wayfarer matrix covers all sixteen approved and implemented sets. Naming aliases never replace effects or alter saved items, costs, protocol formats or attunement identity. Run `tools/author_magic_adjectives.py --check` when changing source names or aliases. No migration or installation is introduced.

## October 8, 2026: resource mana equivalent revised to thirty

The latest accepted conversion is 1 full heart (2 HP) = 2 full hunger icons (4 food points) = 30 XP points = 30 mana. This supersedes the forty-mana equivalence below. `ResourceValuation.MANA_PER_FOOD` is now a double (7.5); dependents must recompile rather than assume integer food pricing. Bloodbound/Fasting inherit current rates and Homebound Eye's mana route now costs thirty, retaining its other prices and wear. Saved routes, keys, formats and mana regeneration are unchanged.

## October 8, 2026: owner-locked resource equivalence

The accepted conversion is one full heart (2 HP) = two full hunger icons (4 food points) = 30 XP points = 40 mana. `ResourceValuation` now applies it to Bloodbound/Fasting and Homebound Eye; the Eye resource routes cost two wear plus 1 heart / 4 food points / 30 XP / 40 mana. This supersedes the provisional prices in the earlier October 8 entry below. Existing items keep their route, origin and key while paying current prices. Save/network schemas, mana recovery and ordinary spell costs are unchanged.

## October 8, 2026: ordered construction and shared resource prices

Threads, Homebound Eye, Whispering Shell, Standing Stone and Fluxed Flint construction require the relative inner-layer patterns in `docs/design/ritual-crafting.md`; whole rotations remain valid. Outer offerings no longer participate in these four-slot constructions. Attunement, fragment combination, scroll dismantling and two-input repair retain unordered acceptance. Bloodbound now buys twenty mana per heart; Fasting keeps five per food point. Exhausting changes to Amplify ×1.25 / mana ×1.35 per degree with no cooldown factor. Homebound Eye resource routes now spend two wear plus 2 hearts / 8 food / 80 XP / 40 mana. Existing items retain their route, origin and key while using current pricing; no save or network schema changes. Matching client/server builds are required for consistent viewer semantics. The Flint ingredient/art redesign remains on hold.

## October 7, 2026: subtle preparation zoom

Replaces the expanding crosshair edge with a first-person camera zoom of up to 3% over the reserved held source's composed preparation time. The native FOV interpolation smooths release/cancellation, and FOV Effects scales the cue. Removes `PreparationCrosshair` and the preparation GUI layer; the native crosshair is unchanged. Straight item draw-back, preparation payload version 2, reservations, costs, automatic release, saved items and mana shading retain their behavior and formats. This supersedes the earlier crosshair preparation entry and isolated 8% trial.

## October 7, 2026: native magical robes and capacity snapshots

Wardweave/Cinderweave register native armor and material IDs with no inherited equipment migration. Mana payload version 3 now carries amount and maximum; all clients must use matching Vestige. `NativeMana.maximum` derives worn capacity and removal permanently clamps the balance. The shared runtime adds `CastReservation.continues` for revocable worn sources, actual `CastObserver.mitigated` notifications, and `armor_damage_calculating` after shields/hurt immunity but before armor. Ordinary paid wand/staff continuations keep the default continued-source behavior.

## October 7, 2026: remove additional wand recovery

Removes the separate sixty-second wand timer without changing component durability, initial wear, composed resource costs or preparation/channel time. Completed and paid-forfeited casts can prepare again immediately when their resources permit. Each initial payment still commits wear once; canceled preparation, failed payment and paid recasts retain their prior behavior.

Removes the obsolete `CastReservation.Recovery` record, `recovery()` hook and `WandComponents.COOLDOWN_TICKS` constant, along with the source-family timer map. Explicit authored `SpellCost.Cooldown` costs remain supported; all shipped spells omit them. Saved item and network formats are unchanged. This supersedes earlier October 6/7 references to retained wand recovery.

## October 7, 2026: crosshair preparation and straight draw-back

Replaces the separate preparation bar with a center-out expanding edge around the existing crosshair, using its current resource-pack sprite, Minecraft's default contrast colors and final shaped time cost. The native crosshair pixels retain their original rendering. Held items now translate straight toward the camera without inward/upward movement or rotation. The obsolete preparation-bar renderer is removed. Preparation payload version 2, source reservations, costs, automatic release, saved items and ordinary mana shading retain their formats and behavior.

## October 7, 2026: native rarity-based Staff wear

Staff initial casts now spend 1/2/3/4 durability for Common/Uncommon/Rare/Mythic native spells instead of a flat one. Wear commits with payment before the chaos roll, so paid volatile forfeits also spend it. Failed payment, canceled preparation and paid continuations retain their prior wear behavior. The final paid cast can break its reserved staff even with fewer remaining points than the spell's wear. Maximum durability stays 40/80/120 for two/four/six slots; saved item and protocol formats are unchanged.

## October 7, 2026: identified Staff scroll insertion

Staff menu slots now reject scrolls whose spell is unknown to the inserting player, in addition to requiring the staff's positive base affinity and valid source shaping. This applies to clicks, replacements, dragging, hotbar swaps and Shift-click; rejection adds no instructional text. Saved or borrowed bindings remain removable and retain knowledge-aware names. Saved item and protocol formats are unchanged. The selected button now stays visibly inset with a bright outline and pixel checkmark.

## October 7, 2026: held-item spell preparation

Private `vestige:cast_preparation` registration advances to version 2, adding the reserved hand and an opaque item/source fingerprint. Matching client/server builds are required. Scrolls, wands and staffs use one first-person draw-back alongside the existing crosshair bar. Runtime timing, automatic release, costs, reservations, recovery, wear and saved item formats retain their behavior.

## October 7, 2026: shared trait-driven item abilities

`MagicDefinition` becomes the shared executable contract for spells and trusted `ItemAbilityDefinition` programs. `SpellDefinition` adds an immutable named-variable map; former Java constructor signatures remain available with an empty map. `SpellRuntime.cast` and `SpellWorld.canActivate` accept the common definition. World/effect adapters use `Context.definition()` in place of the former spell-only accessor. Exhaustive expression/effect switches must handle `SpellValue.Variable` and `SpellEffects.GrantTraits`.

Spells and item abilities use the same source/wearer/actor trait resolver. The shared catalog loader also accepts server-authored `item_abilities` definitions; no built-in clothing ability definitions or registrations ship yet. Binding records add an optional lifetime expression with the former constructor retained. Existing spell JSON, scroll/wand/staff data, attunement keys, registry IDs, protocol formats and baseline gameplay retain their formats. Reactive abilities can execute during preparation without occupying the casting lane. Six equipment formula fixtures remain pending review and are included only in test resources. See `docs/design/item-abilities.md`.


## October 7, 2026: item-relative mana shading

Removes the standalone mana HUD and replaces it with vanilla cooldown-style shading on scrolls, wands, selected staff spells and mana-paid Homebound Eyes. The private `vestige:item_mana` version-1 payload carries bounded server-compiled source prices. `vestige:mana` advances to registration version 2 and double precision; matching client/server builds are required. The retained mana rune becomes a transparent 9×9 GUI asset. Capacity, regeneration, spell payment, charge time, source wear and saved item formats are unchanged.

## October 7, 2026: native Staff inventory menu

Registers the native `vestige:staff` menu type and replaces the select-only `vestige:staff_slots` / `vestige:staff_select` custom payloads with the standard container opening, click and button protocols. Matching client/server builds are required. Shift-right-click opens a scrolling scroll-slot list and player inventory. Removing or swapping a binding now returns its stored source-scroll variant; the old consuming Staff/Scroll/Amethyst binding ritual is removed. Staff construction and capacity upgrades remain atomic rituals. Version-1 saved staff sources, affinity, selected slot and wear retain their format; no migration is introduced.


## October 7, 2026: tipped native wands, shared result signals and component artwork

The native wand binding format retains version 1 with an optional trusted `tip` identity; absent tips remain untipped. Eight materials now compile additional effects and typed costs with the exact stored source. Models use `vestige:wand_appearance` rather than the body-only predicate, with actual 16×16 body/tip layers. Five thread sprites are also actual 16×16 and visually distinct.

`CastReservation.observer`, `CastObserver`, actual paid mana/outcome reporting, `Context.emitSecondary` and `claimAmount` add shared runtime extension points. `target/allied`, `optional_backstep`, optional fractional amount quantization, amount budgets and authored push caps support the trusted component effects. Normal spell definitions and ordinary action defaults are unchanged. Ritual viewer protocol advances to 5 for retained per-seat material frames; an independent version-1 wand display payload carries bounded compiler-approved combinations and exact source magic. Matching client/server builds are required; no legacy registry or migration is introduced.


## October 7, 2026: spell preparation display and ordinary cooldown removal

All 214 shipped spell definitions and modes omit ordinary `cooldown` costs; policy entries retain zero values. Resource amounts, charge time, outcomes, traits and source provenance remain unchanged. Wands retain their separate sixty-second source recovery. Explicit data-pack cooldown costs remain supported. Adds the immutable `SpellRuntime.Preparation` snapshot/accessor and private clientbound `vestige:cast_preparation` payload, version 1, requiring matching client/server builds. The short crosshair bar clears at release or interruption. Saved items and apparatus formats are unchanged.

## October 7, 2026: missing-Plinth ritual cue

Adds the reusable `PULSE` visual shape for an outward reach followed by a collapse. Spell-visual payload registration moves from version 4 to 5, requiring matching client/server builds; spell-sense packets retain version 4. Eight-slot scroll references on a valid four-Plinth setup return `NEEDS_PLINTHS` and send this visual without starting a ritual or consuming inputs. Saved apparatus/items and all recipe compositions retain their formats.

## October 7, 2026: native trait staffs

Registers `vestige:staff` with version-1 fixed affinity, two/four/six bounded source-scroll slots and an explicit selected slot. Amplify, Range and Area are unavailable affinities. Native construction, selected-slot binding and capacity expansions use the existing atomic Plinth ritual; each binding consumes one scroll. Casting retains normal typed spell payment/recovery and commits deterministic source wear once, including the final use; staff continuations cannot identify spells or cross into scroll/wand casting. Adds mandatory clientbound `vestige:staff_slots` and serverbound `vestige:staff_select` payloads, version 1, requiring matching client/server builds. Saved scrolls/wands and other registries retain their formats. No legacy migration is introduced. Initial recipes, durability and item art are playtest choices.

## October 7, 2026: apparatus construction grids and Smooth Quartz names

All 36 Spellstone recipes now use five matching slabs, two Diamonds and one Amethyst Block; all 36 Plinth grids use six matching slabs around one full block and still yield two Plinths. With Supplementaries installed, the native Stone Bricks Plinth recipe and unlock are skipped so its pedestal construction recipe retains the grid. Each pedestal converts one-for-one through the existing optional recipe. Iron's optional conversion remains independent. Smooth Quartz Plinth, Spellstone and Standing Stone names drop the redundant word "Block". Registry IDs, saved items and apparatus behavior are unchanged.

## October 6, 2026: native untipped wand foundation

Registers `vestige:wand`, with version-1 base/thread IDs and one validated source scroll payload. Matching client/server builds are required. The relative five-offering binding recipe uses the eight-node apparatus; the reserved optional tip seat is currently empty. Existing scroll and apparatus identities are unchanged. The item checks its trusted component capacity and persists ordinary used durability; no legacy wand migration is provided.

`SpellRuntime.cast` gains an optional `CastReservation` overload, with default no-op behavior for existing callers. Reserved sources validate during preparation, commit once at initial payment, and may declare a separate per-actor/per-spell recovery group. Wand recovery is sixty seconds across copies, without extending scroll recovery. `CastShaping.CostAdjustment` gains literal post-source preparation and minimum-mana fields; its existing four-argument constructor supplies zero defaults. Scroll casts retain their prior payment behavior.

Native wands never identify spells. Empty-hand continuation input also accepts an already-paid wand continuation; its payload format is unchanged. The foundation implements base/thread profiles, while the locked eight-tip effects, final wand artwork and wand recipe-viewer displays remain outstanding.

## October 6, 2026: Whispering Shell center flash removed

The owner removed the extra channel-symbol flash beneath the crosshair. The client deletes its Shell GUI layer and temporary pulse state; the existing cue packet now plays the quiet sound only. Compact channel chat and inventory corner symbols remain. Channel keys, item bindings, server routing and cue protocol are unchanged.

## October 6, 2026: retained imbuements in Spellstone recipe viewers

Ritual display protocol version 5 adds a bounded per-seat installed-material list, separate from consumed offerings. Matching client/server builds are required. The five public thread recipes derive their offerings and selectors from the native thread recipe; JEI and EMI index the installed block as a retained catalyst and fit its active resource-pack texture to the String Plinth's illustrated rim. Existing spell concealment, saved items, crafting rules and source artwork are unchanged.

## October 6, 2026: compact Whispering Shell conversation symbols

Adds the data-defined native `vestige:whispering_shell` chat type and its `%s - %s` translation. Shell conversations use each channel's single inventory-corner symbol, gray ampersands between channels, and ` > ` before the original sender name. The existing native player-chat packet retains its original body/signature. Matching client/server resources are required for the new decoration. Full verified channel keys, version-1 item bindings and cue payloads are unchanged; the compact symbols also replace full signatures in the brief client pulse. Saved devices need no migration.

## October 6, 2026: fixed-height Standing Stone pages

Standing Stone pages now carry six destinations rather than eight, matching fixed 20-pixel client buttons. Payload registration version 4 requires matching client/server builds to share the new capacity; its existing field format, endpoint/key data and saved worlds are unchanged. Counted full/half health/food previews and the relocated mana HUD are client presentation changes. No migration or alternate payment route is added.

## October 6, 2026: native magical thread components

Registers `vestige:ensorcelled_thread`, `vestige:callous_thread`, `vestige:smoldering_thread`, `vestige:laced_thread` and `vestige:consecrated_thread`. One shared shapeless inner-layer ritual consumes String, Amethyst Shard and Honeycomb, with the String's own retained socket selecting the output. Ordinary spell shaping and saved scroll/device identities are unchanged. Matching client/server builds are required for the new items. The initial components use vanilla's default 64-item stack limit and temporary String models; final artwork, stack tuning and equipment bonuses remain separate decisions. No legacy migration or wand runtime is introduced.

## October 6, 2026: native Whispering Shell

- Registers `vestige:whispering_shell` with version-1 custom data containing the complete independently verified shard blueprint/key. Normal and advanced custom tooltips show the shared four-rune mark; cosmetic names do not affect channels.
- Adds the three-offering Sculk Sensor/Nautilus Shell/valid Attunement Shard device ritual on four inner Plinths, with one empty seat. Existing atomic commitment and public JEI/EMI views apply.
- Every hotbar Shell and offhand Shell diverts ordinary typed chat to matching direct-inventory/offhand holders, online across dimensions, once per player. Invalid active Shells fail closed. No per-message resource cost; nested inventories and offline storage do not participate.
- Two required communication mixins target the pinned Java 1.21.1 submission/recipient seam. Vanilla signed/filtered messages, cancellation, visibility, ordering, logging and spam accounting remain in use. Platform updates must reverify these seams.
- Adds mandatory clientbound `vestige:shell_cue` protocol version 1, bounded to ten channel keys. Matching updated clients receive one brief rune/audio cue per delivered conversation. No historical Paper review fixture is migrated into a Shell.


## October 6, 2026: sixteen-scroll stacks

Native spell scrolls now stack to sixteen when their item components agree. Spell identity, existing augments/degrees, leyline modifiers and Casting Cost values retain their existing data format and equality; distinct variants stay separate. Casting and dismantling consume one scroll per operation, and Plinths still hold one offering each. Matching client/server builds are required for the new stack limit. Existing scroll item data needs no migration. Future wand binding and magical string crafting remain design work.

## October 6, 2026: compact Standing Stone buttons and XP fares

The chosen narrow menu uses name-plus-pencil editing, direct destination buttons with right-aligned XP-orb/amount and an unlabelled centered signature. The source is omitted from its own destination list. Payload registration version 3 adds a positive whole-point `xpCost` to each destination; matching client/server builds are required. Travel now charges the accepted distance fare in XP points through the existing event-aware current-balance helper. Insufficient or canceled payments reject; endpoint invalidation during payment and failed transfers refund. Successful trips consume the source session. Other resource routes/material discounts remain deferred. No registry, attunement key, recipe or saved endpoint format changes and no migration.

## October 6, 2026: approved apparatus edging

All 36 Plinth/Spellstone finishes use the owner-approved narrow corner/rim/support strips, shaded relative to each native tile at 217/255 brightness in world and inventory. Column corners retain continuous UV phase and pillar textures. Original solids, collision/picking, receiving heights, Diamond details, recipes, sockets, registry IDs and attunement identity are unchanged. The shared native color registration replaces the development-only framing tint. Restart the client to load the final art; no migration.

## October 6, 2026: Crane Bag shared storage

Adds `vestige:crane_bag` and version-1 world-owned `vestige_crane_bags` SavedData. Each full attunement key identifies one Bundle-sized pool across dimensions; item contents components are display snapshots. Bag bindings use version-1 custom data with the copied full key. Vanilla container-click/slot synchronization provides the interactions and bounded previews without a new custom payload. Matching client/server registries are required. Existing shard, Standing Stone and Homebound Eye keys/formats are unchanged; no migration is introduced.

The starting policy blocks Bundle/shared-bag nesting, container-component items and read-only/output-only/fake slots. Destroyed access points retain the shared pool. The survival recipe and final art are explicitly deferred; the prototype `/vestige_magic crane_bag` test command was subsequently removed at the owner's request. Existing unreadable storage files are preserved and access fails instead of creating replacement empty pools. Normal save/reload is supported; hard-crash atomicity across Minecraft save files is not guaranteed.

## October 6, 2026: Standing Stone list and in-menu naming

The native Leyline Network screen replaces its schematic map with a destination list, current-stone name editor and bottom signature. Eight-row pages expose every same-dimension peer. The Standing Stone payload registration version is 2: `View` adds a bounded `sourceName`, and `vestige:stone_rename` carries a source ID, bounded name and requested page. Matching client/server builds are required. Renaming is gated by the existing nearby source session and preserves endpoint ID, full key, physical appearance, save format and named item drops. Third-party maps and Waystones remain deferred optional compatibility; no dependency or migration is added.

## October 6, 2026: selected Flint Homebound Eye

The Homebound Eye ritual now requires Flint instead of Gold Ingot, alongside its Attunement Shard, Spider Eye and Ender Pearl. The selected item texture is a grey chipped flint talisman with an Ender Pearl teal-green eye recess and a crimson center. Existing crafted Eyes retain their origin, key, payment and durability data; no component format or registry change is introduced. Matching recipe code and refreshed client resources provide the new craft and appearance.

## October 6, 2026: identified scroll rarity colors

Unknown scroll names remain literal **Unknown Scroll** in white. Identification reveals native Common/Uncommon/Rare/Mythic names in white/yellow/aqua/light purple, including italic augment names. The personalized ritual-viewer payload now uses registration version 4 and carries server-authored rarity only for identified spells; unknown entries carry neutral Common. `RitualDisplays.Entry` adds a `SpellRarity` field, its identified display builder takes rarity, and `SpellKnowledge.updateVisible` accepts an ID-to-rarity map. Matching client/server builds are required. Craft history, item identity and saved scroll/knowledge formats retain their semantics; no migration is introduced.

## October 6, 2026: sixteen Standing Stone forms

The native profile palette grows from three to sixteen, across all 36 registered masonry finishes. A fixed sixteen-bucket policy replaces the three-way prototype selection: the copied key still determines model and runes, masonry still determines finish, and the full key still determines same-dimension network membership. Original review keys retain their forms; other prototype keys may select a different form upon new placement. No keys, rune marks, recipes or endpoint data are migrated. Profile-specific rune anchors keep inscriptions fitted to shorter and leaning stones. Collision and block-state palettes expand with the meshes, so matching client/server builds are required. Use fresh development worlds.

## October 6, 2026: visual ritual feedback and name-only scrolls

Ritual beams match the Spellstone’s pinkish-white rune core; ingredient hints become opaque black silhouettes. Player-facing ritual, scroll, travel-device and mana-restoration chat/actionbar messages are removed. Scroll tooltips show only their name, including advanced tooltips and augmented scrolls, preserving per-player identification and italic augment adjectives. Native detection/inspection uses private visual rings, and status sensing uses health-colored outlines and condition motes. Operator command responses remain available for development. Matching gameplay outcomes, recipes, risk, costs, item data and reservations are unchanged; no migration is introduced.

## October 6, 2026: centered world outputs and apparatus interactions

Ritual successes now create ordinary dropped items exactly above the Spellstone center, with zero initial velocity, instead of inserting a stored center result. Walking into the item collects it; the reference stays on the stone. Rejected output spawns preserve inputs and release locks. Earlier stored results remain recoverable. Stacking a Plinth ejects an existing offering once while retaining its imbuement. Amethyst sounds move to imbuements; quieter item-frame cues handle ordinary offerings. All Spellstone caps rise 1/64 block with the support geometry unchanged. Matching client/server builds are required for the slightly changed collision/receiving surface. Registry IDs, recipes and item data formats remain unchanged. Three darker Plinth framing studies are preview-only.

## October 6, 2026: scroll identity and remembered recipes

The primary mod name is **Vestige**, with *Traditions of Lost Magic* retained as a subtitle. Scrolls display **Unknown Scroll** and hide spell-specific tooltip details until the viewing player identifies their base spell. Successfully committed ingredient crafts now persist separate per-player recipe memory; JEI/EMI reveal those recipes, while identification alone and fragment discovery do not. Both records survive saves and death/nondeath clones. Previous crafts were not tracked and are not inferred from owned scrolls. The ritual-viewer payload uses registration version 3 and personalized identification flags/offerings, including live recipe-index refreshes. Matching client/server builds are required. Item identity and shaping formats are unchanged; no migration is introduced.

## October 6, 2026: torn scroll fragments and trait symbols

Scroll Fragments replace the temporary Paper/Amethyst texture layers with the owner-selected tiny irregular parchment scrap. Resolved names use `Scroll Fragment: [symbol]`, with original glyphs for the 50 catalog traits plus shipped Glass/Oil. Unassigned fragments keep the plain name; unauthored traits fall back to their name, retaining foreign namespaces. Only the symbol uses the custom font and muted gold color. Saved trait IDs, item components, stacking, dismantling rolls, discovery and recipes are unchanged. Rebuild/restart the client to load the new naming code and assets; no migration.

## October 6, 2026: distributed ritual backfires

Completed risky failures now burst at every active ritual node: a four-block-radius Spellstone blast and two-block-radius bursts at its four/eight participating Plinths, including empty positions in an active eight-node circle. Heights and distances follow actual block positions. Creatures receive one strongest visible damage/knockback result where bursts overlap. Risk, tick-20 commitment, active offering consumption, cancellation, reference/socket storage and terrain/drop protection retain their prior semantics. No registry, payload or save-format changes; restart the server/client build for the new gameplay and particle feedback.

## October 6, 2026: player mana and Homebound Eye

Players now have 100 native mana, full on first spawn and death/respawn, recovering 2 per second after a five-second expenditure delay. Login and nondeath clones preserve mana and remaining delay. The `mana` operator argument is now 0–100. Gluttony's restoration cap is 100; authored spell costs are unchanged. The new bounded clientbound `vestige:mana` payload drives a gauge hidden at full. Matching clients and servers are required.

Adds `vestige:homebound_eye` with 30 durability and version-1 stored origin/payment/key data. The final accepted default is 6 wear per return, for five uses; resource-route prices/wear retain their locked values. The four-offering ritual consumes an Attunement Shard, Spider Eye, Ender Pearl and Gold Ingot. The Spider Eye's Plinth selects payment; outer offerings and other sockets do not change it. Lapis Block becomes a supported device imbuement without adding a scroll augment. Recipe-viewer ingredients may now start with a native Vestige component as well as a vanilla item; foreign-only baselines remain invalid. Returns support live Spellstone addresses across available dimensions, refund canceled transfer payments and share `NearbyTeleport` with Standing Stones: random open ground within two horizontal blocks, or a nearby occupied position when no opening exists. No migration or Prism installation accompanies these additions.

## October 6, 2026: luminous Standing Stone signatures

Standing Stone's client renderer adds a soft pulsing halo to its full-bright signature and small camera-facing glyphs that drift from that signature and fade. Nearby motes respect All/Decreased/Minimal particle settings, with three/one/zero bounded trajectories. Body meshes, collision, recipes, registry/state IDs, endpoint data and network identity retain their existing implementation. The effect needs no particle packets or saved state. Restart the client to load the renderer; no migration.

## October 6, 2026: simpler Standing Stone edges

All 36 Standing Stone finishes remove the front inset and angled bevel band from the three shared native meshes. Whole-body face counts fall from 34 to 25, retaining outer outlines, depth, height and generated collision. The unused edge material is removed. This is a client art replacement; registry/state IDs, recipes, endpoint data, signature renderer and travel mechanics keep their existing implementation. Restart Minecraft to load the new models. No migration.

## October 6, 2026: in-game apparatus refinements

All 36 Spellstone finishes raise the physical/receiving top from y8/16 to y10/16 while preserving cap/support thickness and the smaller A corner pattern. Normal walking now requires a jump to reach the top. Picking/collision still follow the tilted supports; client hover outlines use their straight model edges. The Astral Seal sits below resting items. Plinths remove empty carved socket decals and retain their ordinary shaft texture until imbued. Matching client/server builds are required for the changed height. Registry/state IDs, recipes, offerings, socket storage and shaping formats are unchanged; no migration. Restart Minecraft to load this revision.

## October 6, 2026: native two-block Standing Stone profiles

All 36 Standing Stone finishes now use two occupied blocks with `half`, horizontal `facing` and signature-selected `profile` states. Three authored faceted meshes replace temporary cubes. The lower half alone owns the endpoint, bound drop and native glyph renderer; interacting with either half opens the same source. Upper occupancy is required before item placement; either-half removal tears down both halves, with one identity-preserving survival drop and no creative duplicate. Quarter-unit collision bands follow the silhouettes within the reserved column. Profile and rune selection use the copied full key; body finish does not change either. Matching clients and servers are required. No migration is provided; use fresh test worlds. Payment implementation remains separate and unfinished.


## October 6, 2026: selected smaller A Spellstone corners

All 36 Spellstone block/item finishes replace the rejected side-center gem sprite with the owner-selected smaller A pixel chips wrapping four vertical cap corners. This is a client art replacement using original Minecraft Diamond colours. Geometry, collision, receiving height, registry/state IDs, recipes, storage, ritual mechanics and Astral effects retain their existing behavior. The rejected custom sprite is removed from the packaged resources. Restart Minecraft to load the new artwork; no migration.

## October 6, 2026: concealed spell recipe displays

The ritual-viewer payload uses registration version 2. Spell entries now carry only identity and four/eight-slot capacity, with no offering data; the model rejects spell entries containing ingredients. Both optional viewers display question marks in every spell Plinth and omit hidden inputs from usage indexes. EMI recipe-tree construction is disabled for concealed recipes. Public offerings and blank unused seats remain visible. Matching Vestige client/server builds are required. Actual crafting recipes, identification, ritual hints, scrolls and shard storage are unchanged. The approved generated parchment/stone artwork replaces the interim diagram.

## October 6, 2026: Standing Stone masonry finishes

Standing Stone adds 35 material-prefixed block/item IDs, sharing the existing 36-entry apparatus masonry palette. Stone Bricks retain `vestige:standing_stone`. The block constructor/codec now requires its cosmetic `material`; every finish shares one endpoint entity schema. The four-offering recipe accepts two matching full masonry blocks and selects their finish; two Chiseled Stone Bricks remain an alternative for Stone Bricks. Existing keys, endpoint directory data and default helper methods remain unchanged. Matching client/server registries are required. No migrations or aliases are introduced. Temporary cube art remains until the signature-selected upright family is finalized.

## October 6, 2026: compact Spellstone with four rim gemstones

All 36 Spellstone block/item finishes reduce the top from 14×14 to 12×12 model units, thicken it from 2.25 to 3.25 units, and thicken/shorten the supports to 3.5×10 units with a 22.5-degree inward lean. The physical/receiving top rises from y7/16 to y8/16; scroll placement and the native Astral Seal rise with it. Four isolated gemstone cutout faces on the vertical rim replace twenty flat top-chip faces, with a shared 32×32 sprite. The top and stone between gems remain bare. Collision/selection follow the compact stonework and retain the opening. Matching client/server builds are required for the changed shapes and receiving height. Registry/state IDs, recipes, storage, Plinth geometry and shaping formats are unchanged. No migration.

## October 6, 2026: thicker Spellstone and faceted Diamond chips

All 36 Spellstone block/item finishes thicken their inclined supports from 2 to 3 model units and the tabletop from 1.75 to 2.25 units. Four flush chips replace the tiny cyan squares, with cut outlines and contrasting native Diamond facets. Collision/selection follow the thicker supports and retain the triangular opening. The receiving/physical top stays y7/16 and the Astral Seal remains aligned. Matching updated art and collision require the rebuilt client/server jar; recipes, IDs and stored formats are unchanged. No migration.

## October 6, 2026: low tent-table Spellstone

All 36 Spellstone block/item finishes now use the approved three-piece body: two inward-leaning supports, one flat tabletop and four subtle Diamond top insets. Collision/selection use thin horizontal slices for the tilted supports, keeping the triangular opening clear. The physical/receiving top remains 7/16 and square envelope remains 14/16; the native Astral Seal is unchanged. Matching updated art and collision require the rebuilt client/server jar. Registry/state IDs, storage, recipes, Plinths, waterlogging, shaping and attunement formats are unchanged. No migration.

## October 6, 2026: optional ritual recipe viewers

Adds the bounded clientbound `vestige:ritual_displays` payload for server-authoritative recipe-viewer data on login and datapack reload. Matching Vestige client/server builds are required. It contains display identities, four/eight-slot capacities and offering alternatives, without shaping calculations or attunement keys. Existing recipe, scroll and shard formats remain unchanged.

JEI and EMI are optional client integrations compiled against their APIs; neither is bundled or required to run Vestige. If both are present, EMI owns the ritual category to avoid duplicate bridge recipes. Recipe lookup distinguishes base spells while ignoring crafted modifiers; all Attunement Shard keys share their one crafting recipe.

## October 5, 2026: Astral Spellstone

The owner-selected Astral Seal replaces the chunky raster glyph on all 36 Spellstone finishes, with hover lowered from 0.085 to 0.045 block. The body is one seven-cuboid octagonal stone with texture-only trim and four Diamond corner notches. Its collision/physical top changes from 8/16 to 7/16 and width from approximately 14.43/16 to 14/16; the receiving surface stays at 7/16. Items show the stone body; placed blocks render the floating seal. The unused raster glyph is removed from packaged resources. Plinths, recipes, registry/state IDs, serialization, waterlogging and shaping remain unchanged. No migration.

## October 5, 2026: clipped-corner Spellstone foundation

The Spellstone foundation's square slab is replaced by an octagonal perimeter with four 45-degree corner cuts, across all 36 finishes. Its 14/16 outer width and 1.3/16 thickness are retained, with no additional tiers. Seven standard cuboids form the base; all body/crown/rune elements above it are unchanged. Inventory models have 38 elements and placed models 37, with the renderer supplying the rune. Collision retains the existing enclosing-box convention for rotated geometry. Plinth geometry, raster assets, receiving surfaces, recipes, registry/state IDs, serialization and shaping are unchanged. No migration.

## October 5, 2026: simple Plinth and consistent column sockets

The owner’s in-game review supersedes the elaborate Plinth. Standalone geometry is three solid cuboids plus four flat carved socket faces (7 elements / 20 baked faces); base/shaft/cap use 2/1/2 solids plus the same socket faces. Every segment’s material is centered at local y8/16 and rendered as a thin plate per side, protruding 1/32 block; five visible faces per plate. Plinth’s cap is flat at y14/16, foot/cap width is 12/16 and shaft width 10/16; generated collision follows this geometry. Spellstone and raster textures remain unchanged. Independent socket storage, waterlogging, offering clearance, recipes, shaping, registry/state IDs and serialization are retained. No migration.

## October 5, 2026: remove the column-head lower frame rail

Across all 36 finishes, stacked Plinth caps omit the four lower horizontal rails of the large architectural face frame (49→45 elements). The small imbuement outlines and all other cap details remain intact. Standalone Plinths, Spellstone, base/shaft models, texture bytes, receiving heights and sockets are unchanged. Collision is regenerated to match the four removed decorative bars. No registry, state, recipe, shaping or serialized-format change; no migration.

## October 5, 2026: waterlogged apparatus and offering clearance

Both apparatus roles add the vanilla `waterlogged` block-state property and water-bucket support, including all 36 finishes and connected column forms. Placement in source or flowing water retains water. Column neighbor updates and bucket filling/draining preserve stored offerings and independent imbuements. Plinth offering placement, rendering, node selection and atomic commitment require air or unobstructed fluid above; ordinary solid covers now invalidate a ritual surface before consumption, retaining existing items. Every column segment still accepts/retrieves its own side imbuement. Geometry, recipes, shaping math, registry IDs and scroll/shard field formats are unchanged. Matching client/server builds are required; no migration is introduced.

## October 5, 2026: detailed apparatus with foot-only refinement

The owner clarified the art direction: restore the original detailed models and simplify only their bottom steps, retaining the complete Plinth imbuement outline. Native Plinth now has 51 elements (three foot tiers become two); Spellstone has 32 including its rune (two foot tiers become one). All original standalone elements above the feet are identical. Connected columns retain seamless masonry/trim and restore the detailed 49-element cap. Physical collision is regenerated for these local shape changes; maximum bounds, receiving surfaces, socket alignment, registry IDs, block-state properties and scroll/shard formats remain unchanged. The earlier 24/25 middle-ground models are historical. No migration is introduced.

## October 5, 2026: middle-ground apparatus models and continuous column mapping

All 36 finishes adopt the owner's middle-ground art direction: 24-element Plinth and 25-element Spellstone, retaining framed panels and the octagonal center. Column models add uninterrupted corner trim and fixed-phase masonry UVs, with border-free polished interiors and native Quartz/Purpur pillar shafts. Physical model bounds, offering heights, socket alignment, registry IDs, block-state properties and scroll/shard formats remain unchanged. Small Diamond inlays are decorative near-flush decal planes. Collision is regenerated from physical geometry. Install the same model revision on client/server; no migration or alias is introduced. The earlier minimal standalone preview remains historical art.

## October 5, 2026: cosmetic wool/concrete imbuement colors

All 16 vanilla wool colors and all 16 concrete colors qualify as their existing material families. Spellshaping matching and Attunement hash encoding share that normalization. The 26 families now accept 56 block IDs; carpets, concrete powder and unrelated blocks remain rejected. Installed stacks and shard node data retain their actual color for display, persistence and recovery. White and noncolored key encodings remain unchanged. Previously created color-sensitive keys for nonwhite imbuements are superseded; no migration is added. Scroll/shard field formats and base ingredient identities remain unchanged. This supersedes the white-only eligibility noted below.

## October 5, 2026: connected Plinth columns

Apparatus block states add `part=single|base|shaft|cap`. Plinths connect vertically across cosmetic finishes; Spellstone remains `single`. Covered Plinth segments no longer accept new offerings or count as ritual nodes. Existing stored offerings and sockets remain intact and recoverable. Adding a segment above a reserved ritual surface invalidates that attempt before consumption. Construction recipes, registry IDs, scroll/shard formats and active-node shaping are unchanged. Matching client/server builds are required; no migration is introduced. Simpler standalone models remain review-only alternatives.

## October 5, 2026: supported imbuement blocks only

Plinth side sockets now reject block items absent from the packaged Spellshaping material routes. The 26 eligible IDs include compound-only materials; White Wool and White Concrete do not match other colors. Both interaction prediction and server installation apply the same eligibility check. Unsupported side clicks consume nothing and do not fall through to the offering slot; top recipe ingredients remain unrestricted by this material list. Scroll/shard formats, recipe pairings and cosmetic identities are unchanged. No migration is added.

## October 5, 2026: cosmetic apparatus finishes and owner construction grids

Spellstone/Plinth now each have 36 stone/masonry variants. Stone Bricks retain `vestige:spellstone`/`vestige:plinth`; the other 35 finishes use material-prefixed IDs. All share explicit `ApparatusBlock.Role` recognition and one offering block-entity schema. The block constructor/codec uses `role` instead of numeric `height`; shared collision and display dimensions adopt the approved models. Construction requires matching full blocks/slabs: Spellstone outputs one; Plinth outputs two. Old Iron/Chiseled Stone Brick construction grids are superseded. Clients and servers require the same updated jar. Scroll/shard formats and attunement identity are unchanged; carrier appearances are excluded. No legacy aliases or migrations are added.

## October 5, 2026: six-offering Attunement Shard recipe

Attunement's fixed shapeless recipe now consumes 2 Amethyst Shards, 1 Echo Shard, 1 Iron Ingot, 1 Diamond and 1 Lapis Lazuli on six separate Plinth surfaces. Both four-node layers remain required, with two offering surfaces empty. The matcher preserves duplicate counts and rejects missing, wrong or extra offerings before consumption. All eight nodes, including empty offering positions and their sockets, remain part of the attunement blueprint and atomic commitment checks. Item IDs and version-1 key/blueprint encoding are unchanged. Clients and servers should use the updated build for this recipe.

## October 4, 2026: Spellshaping refinement

Attunement's fixed shapeless recipe now consumes one each Amethyst Shard, Copper Ingot, Iron Ingot, Ender Pearl, Quartz, Lapis Lazuli, Redstone Dust and Diamond. Item IDs and version-1 key/blueprint encoding are unchanged; existing saved shards remain valid.

Trusted Spellshaping rules add `after` attachment with `after_actions`/`after_statuses`, and `anchor` for eligible moving spatial area pulses. The executable set adds Revealing, Anchored and Reflecting; the unimplemented design palette has been removed. The pure compiler still rejects incompatible outcomes before consumption.

The action grammar adds bounded `reveal_hidden` and `reflect_projectiles`. `SpellRuntime.Context.claimContact` shares per-recipient and total budgets across callbacks/fields; `Limited` uses that same seam. Returned vanilla projectiles preserve secondary causal roots through further reflections, reject repeated activation loops, and release tracked lineage on removal/world close. Native spell projectile reflection remains unsupported.

## October 4, 2026: native Spellshaping and Attunement Shards

Added `vestige:attunement_shard` and version-1 scroll augment descriptors. `ScrollItems.Scroll` now exposes bounded selections; `shapedScroll` has an overload accepting those selections. Base scrolls and v3 leyline scrolls keep their existing formats. Runtime-only compiled views retain original spell IDs/provenance; no permanent catalog variant is registered. `CastShaping` adds a validated typed cost adjustment, retaining its two-argument constructor. Matching clients/servers are required for the new item. No migration aliases are introduced.

Effect grammar adds `limited`, `secondary` and bounded `clamp` expressions. Secondary tasks carry explicit lineage and scoped contact snapshots, run without blocking the primary plan and do not emit further shaping riders. Manifestation `lifetime` resolves once into the backing and runtime expiry. Pure capability traversal covers the added graph forms. Actual healing reports `last_heal`; leech accepts an explicit maximum. Executing native leaf lineage takes precedence over a backing actor's historical lineage.

Attunement uses both active four-node layers and a canonical paired blueprint/hash. Its recipe consumes eight offerings while retaining sockets. It does not implement communication or teleportation devices.

## October 4, 2026: remove obsolete apparatus and save migration

The owner explicitly rejected migration and authorized deleting old test worlds. `vestige:stone_pedestal` and `vestige:runic_pedestal` blocks/items, aliases, loot, mining entries, language keys and on-load migration hooks are deleted. Only Spellstone and Plinth remain. Old Deepslate recipe unlocking and five unused ingredient-specific textures are removed from the packaged mod; old art/scripts remain historical documentation only. The reused approved rune is now named `spellstone_runes`.

The obsolete fixed `RitualCrafting.OFFSETS` API is removed; use the selected `LeylineShaping.Geometry.offset(seat)` for relative positions. Scrolls are either native base scrolls or versioned leyline scrolls. Unversioned arbitrary augmentation and its generic creator/compatibility constructor are removed; use `ScrollItems.scroll(spell)` or `shapedScroll(spell, modifiers)`. Matching clients/servers and fresh worlds are required.

## October 4, 2026: native leyline crafting

Historical initial implementation; the cleanup entry above supersedes its migration policy. Added `vestige:plinth` and replaced the creative/construction Stone/Runic split with one block. The initial migration aliases have since been removed at the owner's request. Spellstone becomes a half-slab; collision and offering surfaces follow the new models. Matching clients and servers are required for the new registry/model and socket state. Old three-block construction recipes are retired.

Recipes now match dynamic regular rings; the nearest complete ring is inner. Four-slot recipes ignore outer nodes entirely. Crafted scrolls persist v3 Amplify/Range/Area multipliers and uniform Casting Cost; old unshaped scrolls remain compatible. New additive `CastShaping` runtime options preserve shaping through callbacks/recasts, scale every existing cost and quantize final quantities. Health costs round in hearts at the HP boundary. Effects remain explicit scaling-trait consumers; raw descriptive ratings, rarity and discovery odds are unchanged. Material sockets save/sync and recover separately, with numeric material effects still deferred. See [playtesting](docs/leyline-playtesting.md).

## October 3, 2026: broader grounded pedestals and flat scroll collection

Stone and Runic Pedestals now share a 10½-unit cap/foot and 9½-unit shaft, with heights of 7 and 5 units. Collision and offering surfaces match the models. Wrong placements retain sideways shaking; the removed `MOVE_UP` and `MOVE_DOWN` feedback variants no longer supply vertical clues. Because transient feedback ordinals changed, server and clients must use the same build. Existing block IDs, saved offerings and independent ritual results remain compatible.

Successful rituals lift only the pedestal ingredients, leaving the stone and reference scroll grounded. The reference and finished scroll rest flat on the Spellstone; the result has a small stack offset when a reference is present. Empty-hand collection still takes the result first without removing the reference. Native dropped-item scale and all ingredient recipes remain unchanged.

## October 3, 2026: wider, flatter pedestals and native item scale

Stone and Runic Pedestals now share an 8½-unit cap/foot and 7½-unit shaft. Their heights are 8 and 6 units respectively. Models, collision, offering elevations, glow outlines and ritual connections follow the new dimensions; existing placed block IDs and persisted offerings load normally. Resting offerings, ingredient hints and produced scrolls use the native `GROUND` item transform with no additional scaling, matching single dropped-item model size. Flat offerings stay laid on the surface. Pedestal movement remains active as requested.

## October 3, 2026: native scrolls and Spellstone rituals

Adds item IDs `vestige:spell_scroll` and `vestige:scroll_fragment`, serializer `vestige:scroll_dismantling`, data directory `ritual_recipes`, and server continuation payload `vestige:scroll_recast` version 1. Matching server/client builds are required. Scroll identities and bounded modifiers use vanilla Custom Data; player identification persists under `PlayerPersisted/vestige:identified_spells`. The runtime exposes additive `Cast.paymentCommitted()` so scroll lifecycle follows the existing resource transaction.

All 214 spells have native ritual recipes with vanilla baselines and optional Iron item alternatives. Correct arrangements work without a reference. References provide hints and completed-failure risk; incorrect unreferenced attempts reject safely. One Paper pedestal ingredient is consumed on success; the center reference is preserved. Offering block entities now save an independent `ritual_result`; transient feedback and locks sync without persisting active rituals. Apparatus models are rendered with their offerings to animate movement while retaining original collision. See [ritual design](docs/design/ritual-crafting.md) for exact layout, container remainders, creature-only blasts and future slot-local foundation augmentation. Spell definitions and base balance policies are unchanged.

## October 3, 2026: native apparatus construction and offerings

New block/item IDs are `vestige:spellstone`, `vestige:stone_pedestal` and `vestige:runic_pedestal`; `vestige:offering` is their shared block-entity type. Servers and clients need this build to place, save and display them. The owner-supplied vanilla construction recipes are active, with one block per craft. These are new native items and do not restore retired Wizardry registry IDs or equipment.

All three stands store one item, synchronize its display through vanilla block-entity packets, preserve its components across saves and return it on removal. The current Spellstone artwork supersedes the previous unregistered draft. Discovery, spell augmentation, supporting-block effects and multiblock activation remain deferred; placing offerings has no spell or material-quality semantics. Spell definitions and presentation protocol are unchanged.

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
