# Pathfinder Second Edition: first native spell batch

Primary-source research checked on **2026-10-01**. This note records 48 concrete spell candidates and their recognizable behavior, using the current Remaster entries on Archives of Nethys wherever a spell was reprinted. It is research for native Vestige recipes, not a claim of exact Pathfinder rules implementation. Wands, discovery, and progression remain deferred.

## Source decisions

Archives of Nethys is the rules reference used for every candidate below. Paizo's July 24, 2026 statement confirms that it remains an official rules archive after changes to the commercial agreement. [Paizo: Moving Forward Together](https://cdn.paizo.com/blog/paizo-archives-of-nethys-moving-forward-together)

The frozen machine-readable companion is [tools/pathfinder-spells.json](../../tools/pathfinder-spells.json). Each entry retains the source slug, display name, canonical AoN ID/URL, base rank, cantrip flag, printed traditions, source rarity, book/page, and the book's current errata marker. `pf2_<slug>` is the proposed native identity, keeping collisions such as Fireball, Shield, Haste, Slow, Lightning Bolt, and Chain Lightning separate from existing Iron-derived spells. The correct current spell name is **Regenerate**, not Regeneration. [Regenerate](https://2e.aonprd.com/Spells.aspx?ID=1648)

All 48 source spells are **common**: none lists another rarity trait, and Pathfinder defaults such entries to common. Pathfinder rarity controls availability; it is distinct from the requested Vestige common/uncommon/rare/mythic classification. Source rank and cantrip status remain provenance and do not dictate native damage, costs, cooldowns, trait magnitudes, or Vestige rarity. [Common](https://2e.aonprd.com/Traits.aspx?ID=557)

The selection contains 16 cantrips and 32 other spells across all four traditions. The two Rage of Elements entries use the compatible newer vocabulary without inventing a reprint in Player Core. Paizo describes the Remaster as compatible with existing Second Edition material. [Paizo: Remaster Project](https://paizo.com/blog/pathfinder-second-edition-remaster-project)

| Publication | Errata shown by AoN on the research date | Source ledger |
| --- | --- | --- |
| Player Core | Spring 2026, 1st Printing; applied 2026-02-06 | [Player Core](https://2e.aonprd.com/Sources.aspx?ID=216) |
| Player Core 2 | 1.1; applied 2024-12-16 | [Player Core 2](https://2e.aonprd.com/Sources.aspx?ID=227) |
| Rage of Elements | 2.0; applied 2024-05-17 | [Rage of Elements](https://2e.aonprd.com/Sources.aspx?ID=205) |

These markers describe the source books' displayed update state. The snapshot key `aon-2026-10-01` means this research date; it is not an upstream commit, guarantee of immutable pages, or claim that every individual spell changed in that errata release.

The first 48-spell batch is now implemented; the [native conversion ledger](../design/pathfinder-spell-conversions.md) is authoritative for actual executable behavior. The candidate boundaries below retain the research proposals.

## Verified candidates and conversion boundaries

**Legend:** A = arcane, D = divine, O = occult, P = primal. C1 means a base-rank-1 cantrip; otherwise the number is the base spell rank. PC = Player Core, PC2 = Player Core 2, RoE = Rage of Elements. Every spell's source rarity is common. The behavior column is an original concise paraphrase of the linked source; the final column is a proposed Vestige adaptation constraint, not a Pathfinder rule.

### Cantrips

| Candidate / canonical source | Rank / traditions | Publication | Recognizable source behavior | Native adaptation boundary |
| --- | --- | --- | --- | --- |
| [Electric Arc](https://2e.aonprd.com/Spells.aspx?ID=1509) (`1509`; `pf2_electric_arc`) | C1; AP | PC p. 328 | Electricity jumps between at most two creatures. | Keep two distinct targets and a short chain; use native hit rules instead of Reflex saves. |
| [Ignition](https://2e.aonprd.com/Spells.aspx?ID=1565) (`1565`; `pf2_ignition`) | C1; AP | PC p. 336 | A targeted flame hits harder within melee reach and can leave a lingering burn. | Retain ranged versus melee tradeoffs; use a bounded burn rather than critical-hit dice. |
| [Frostbite](https://2e.aonprd.com/Spells.aspx?ID=1539) (`1539`; `pf2_frostbite`) | C1; AP | PC p. 332 | Targeted cold damages a creature; a severe failure leaves it vulnerable to bludgeoning. | Cold and brief control fit native actions; document any omitted physical vulnerability. |
| [Caustic Blast](https://2e.aonprd.com/Spells.aspx?ID=1461) (`1461`; `pf2_caustic_blast`) | C1; AP | PC p. 319 | An acid glob splashes a small area and may leave lingering corrosion. | Use a small capped blast; persistent acid needs finite pulses. |
| [Divine Lance](https://2e.aonprd.com/Spells.aspx?ID=1498) (`1498`; `pf2_divine_lance`) | C1; D | PC p. 325 | A divine beam attacks one creature's spirit. | Use one victim and divine provenance; spirit damage must have an explicit native damage mapping. |
| [Void Warp](https://2e.aonprd.com/Spells.aspx?ID=1745) (`1745`; `pf2_void_warp`) | C1; ADO | PC p. 366 | Void energy harms a living creature and may briefly weaken it. | Preserve living-only eligibility; do not damage undead as an ordinary unrestricted beam. |
| [Vitality Lash](https://2e.aonprd.com/Spells.aspx?ID=1744) (`1744`; `pf2_vitality_lash`) | C1; DP | PC p. 366 | Vital energy attacks undead or creatures sustained by void healing. | Preserve undead targeting; do not turn this offensive spell into ordinary healing. |
| [Gouging Claw](https://2e.aonprd.com/Spells.aspx?ID=1546) (`1546`; `pf2_gouging_claw`) | C1; AP | PC p. 333 | A briefly transformed claw delivers a melee strike and bleeding. | Bound melee reach and bleed lifetime; no permanent body transformation is required. |
| [Telekinetic Projectile](https://2e.aonprd.com/Spells.aspx?ID=1718) (`1718`; `pf2_telekinetic_projectile`) | C1; AO | PC p. 363 | Telekinesis hurls a small unattended object; its magic properties do not improve the hit. | Use an object-shaped projectile without consuming or duplicating inventories; record material handling differences. |
| [Needle Darts](https://2e.aonprd.com/Spells.aspx?ID=1375) (`1375`; `pf2_needle_darts`) | C1; ADOP | RoE p. 144 | Three metal needles strike one target; their metal can matter and then returns. | Treat the needles as one grouped hit or cap overlap; omit unsupported special-metal rules explicitly. |
| [Slashing Gust](https://2e.aonprd.com/Spells.aspx?ID=1321) (`1321`; `pf2_slashing_gust`) | C1; AP | RoE p. 71 | Cutting air attacks one or two creatures depending on available hands. | Cap distinct targets at two; free-hand rules are an adaptation choice while wand design is deferred. |
| [Spout](https://2e.aonprd.com/Spells.aspx?ID=2031) (`2031`; `pf2_spout`) | C1; AP | PC2 p. 252 | An upward water blast hits a small space; a suitable body of water enlarges it. | Avoid placing permanent water; note whether the water-dependent larger area is supported. |
| [Scatter Scree](https://2e.aonprd.com/Spells.aspx?ID=2021) (`2021`; `pf2_scatter_scree`) | C1; AP | PC2 p. 250 | Falling rocks damage two neighboring spaces and leave removable difficult terrain. | Use temporary slowing terrain without world griefing; replace the caster's previous scree. |
| [Tangle Vine](https://2e.aonprd.com/Spells.aspx?ID=1713) (`1713`; `pf2_tangle_vine`) | C1; AP | PC p. 362 | A conjured vine slows a target and can immobilize it on a severe hit. | Use a short destructible tether; avoid indefinite root chaining. |
| [Puff of Poison](https://2e.aonprd.com/Spells.aspx?ID=2016) (`2016`; `pf2_puff_of_poison`) | C1; AP | PC2 p. 249 | A short-range toxic breath delivers an initial hit and lingering poison. | Keep short range and a finite damage-over-time budget; vanilla poison behavior differs. |
| [Shield](https://2e.aonprd.com/Spells.aspx?ID=1671) (`1671`; `pf2_shield`) | C1; ADO | PC p. 356 | Brief force protection can absorb a hit; blocking ends it and prevents immediate reuse. | Implement a finite self ward distinct from the existing stationary projectile barrier. |

### Ranks 1–2

| Candidate / canonical source | Rank / traditions | Publication | Recognizable source behavior | Native adaptation boundary |
| --- | --- | --- | --- | --- |
| [Breathe Fire](https://2e.aonprd.com/Spells.aspx?ID=1457) (`1457`; `pf2_breathe_fire`) | 1; AP | PC p. 319 | A brief exhalation damages creatures in a forward cone. | Use one cone burst rather than the existing Iron-derived continuous Fire Breath. |
| [Force Barrage](https://2e.aonprd.com/Spells.aspx?ID=1536) (`1536`; `pf2_force_barrage`) | 1; AO | PC p. 332 | Reliable force shards can be distributed among targets, with more shards for more actions. | Adapt action investment to explicit cast choices or a bounded volley; do not promise automatic hits if projectiles can miss. |
| [Hydraulic Push](https://2e.aonprd.com/Spells.aspx?ID=1561) (`1561`; `pf2_hydraulic_push`) | 1; AP | PC p. 336 | Pressurized water strikes and knocks a creature or unattended object backward. | Use native damage and knockback with collision-safe limits; object targeting is a documented adaptation choice. |
| [Grim Tendrils](https://2e.aonprd.com/Spells.aspx?ID=1548) (`1548`; `pf2_grim_tendrils`) | 1; AO | PC p. 334 | Dark tendrils strike living creatures along a line and cause bleeding. | Keep a narrow living-only line; cap victims and secondary bleed. |
| [Thunderstrike](https://2e.aonprd.com/Spells.aspx?ID=1721) (`1721`; `pf2_thunderstrike`) | 1; AP | PC p. 363 | A distant lightning strike combines electrical and sonic damage; metal targets fare worse. | Separate visible lightning from griefing vanilla lightning; explicitly document omitted metal-armor interaction. |
| [Fear](https://2e.aonprd.com/Spells.aspx?ID=1524) (`1524`; `pf2_fear`) | 1; ADOP | PC p. 331 | Magical fear weakens a creature and can force it to flee. | Minecraft lacks Pathfinder frightened/Will saves; use brief movement or attack disruption with finite duration. |
| [Fleet Step](https://2e.aonprd.com/Spells.aspx?ID=1531) (`1531`; `pf2_fleet_step`) | 1; AP | PC p. 332 | The caster gains a substantial short-lived movement boost. | Use bounded movement speed with cleanup; do not grant permanent progression. |
| [Gentle Landing](https://2e.aonprd.com/Spells.aspx?ID=1542) (`1542`; `pf2_gentle_landing`) | 1; AP | PC p. 333 | A reactive updraft slows a falling creature and prevents the affected fall's damage. | Adapt reaction timing to operator-cast slow falling or a finite fall-damage ward. |
| [Protection](https://2e.aonprd.com/Spells.aspx?ID=1641) (`1641`; `pf2_protection`) | 1; DO | PC p. 351 | A willing creature receives a short-lived defensive ward. | Use limited damage reduction; source AC/saves have no direct Minecraft equivalent. |
| [False Vitality](https://2e.aonprd.com/Spells.aspx?ID=1523) (`1523`; `pf2_false_vitality`) | 2; AO | PC p. 331 | Undead-associated energies grant temporary extra resilience. | Use temporary absorption with replacement behavior; do not increase permanent maximum health. |
| [Soothe](https://2e.aonprd.com/Spells.aspx?ID=1678) (`1678`; `pf2_soothe`) | 1; O | PC p. 357 | Mental reassurance heals a willing creature and fortifies its mental defenses. | Heal a friendly target and document the chosen mental-defense approximation. |
| [Enfeeble](https://2e.aonprd.com/Spells.aspx?ID=1513) (`1513`; `pf2_enfeeble`) | 1; ADO | PC p. 329 | A targeted spell saps strength for a duration determined by resistance. | Use temporary attack weakness; restore any transient modifiers when the spell ends. |
| [Resist Energy](https://2e.aonprd.com/Spells.aspx?ID=1651) (`1651`; `pf2_resist_energy`) | 2; ADOP | PC p. 353 | A touched creature resists one selected form of elemental damage. | Expose explicit supported damage choices; do not silently grant blanket resistance to every hit. |
| [Floating Flame](https://2e.aonprd.com/Spells.aspx?ID=1533) (`1533`; `pf2_floating_flame`) | 2; AP | PC p. 332 | A sustained flame moves under the caster's command and burns creatures it passes. | Use a controlled moving hazard; rate-limit each victim and document sustained steering differences. |
| [Revealing Light](https://2e.aonprd.com/Spells.aspx?ID=1653) (`1653`; `pf2_revealing_light`) | 2; ADOP | PC p. 353 | An area of magical light dazzles creatures and defeats invisibility or concealment. | Use visible marking and bounded status changes; Minecraft concealment differs from Pathfinder. |
| [Spiritual Armament](https://2e.aonprd.com/Spells.aspx?ID=1687) (`1687`; `pf2_spiritual_armament`) | 2; DO | PC p. 359 | A ghostly weapon echo attacks at range and can strike again when sustained. | Prefer a finite spectral attack sequence; record whether native recasts replace sustained target selection. |

### Ranks 3–10

| Candidate / canonical source | Rank / traditions | Publication | Recognizable source behavior | Native adaptation boundary |
| --- | --- | --- | --- | --- |
| [Fireball](https://2e.aonprd.com/Spells.aspx?ID=1530) (`1530`; `pf2_fireball`) | 3; AP | PC p. 331 | A chosen point erupts in a large fire blast. | Keep a separate pf2 identity; tune the authored blast against existing Vestige area spells. |
| [Lightning Bolt](https://2e.aonprd.com/Spells.aspx?ID=1586) (`1586`; `pf2_lightning_bolt`) | 3; AP | PC p. 341 | Lightning damages creatures along a long straight line. | Use a narrow capped beam; preserve difference from an aimed single-target bolt. |
| [Vampiric Feast](https://2e.aonprd.com/Spells.aspx?ID=1736) (`1736`; `pf2_vampiric_feast`) | 3; ADO | PC p. 365 | A draining touch harms a living creature and grants temporary resilience from actual damage. | Use actual damage as the absorption basis; ordinary healing would change the source identity. |
| [Haste](https://2e.aonprd.com/Spells.aspx?ID=1553) (`1553`; `pf2_haste`) | 3; AOP | PC p. 335 | A creature gains an extra action usable only to move or strike. | Adapt to movement and attack speed; avoid implying extra casting actions or importing Pathfinder initiative. |
| [Slow](https://2e.aonprd.com/Spells.aspx?ID=1677) (`1677`; `pf2_slow`) | 3; AOP | PC p. 357 | Time distortion reduces a creature's available actions. | Use temporary movement and attack disruption; ordinary slowness alone is a narrower adaptation. |
| [Bind Undead](https://2e.aonprd.com/Spells.aspx?ID=1449) (`1449`; `pf2_bind_undead`) | 3; ADO | PC p. 318 | A sufficiently weak mindless undead becomes a minion; hostile treatment ends control. | Do not spawn a replacement undead and claim it is possession; ownership changes need eligibility and restoration. |
| [Field of Life](https://2e.aonprd.com/Spells.aspx?ID=1526) (`1526`; `pf2_field_of_life`) | 6; DP | PC p. 331 | A sustained zone restores living creatures and damages undead each round. | Keep both sides of the living/undead distinction; finite pulses and allegiance choices must be explicit. |
| [Regenerate](https://2e.aonprd.com/Spells.aspx?ID=1648) (`1648`; `pf2_regenerate`) | 7; DP | PC p. 352 | Powerful ongoing regeneration repairs injury; acid or fire briefly suppresses it. | Bound healing and suppression; do not claim organ regrowth, death prevention, or limb systems Minecraft does not model. |
| [Fire Shield](https://2e.aonprd.com/Spells.aspx?ID=1529) (`1529`; `pf2_fire_shield`) | 4; AP | PC p. 331 | A fiery blocking shield protects from cold and can burn adjacent attackers. | A finite reactive ward is appropriate; list cold protection or shield-raising mechanics that are approximated. |
| [Weapon Storm](https://2e.aonprd.com/Spells.aspx?ID=1758) (`1758`; `pf2_weapon_storm`) | 4; AP | PC p. 369 | Magical copies of a held weapon strike a cone or surrounding area. | Snapshot held-weapon contribution without duplicating items; bound area and per-victim hit count. |
| [Spirit Blast](https://2e.aonprd.com/Spells.aspx?ID=1685) (`1685`; `pf2_spirit_blast`) | 6; DO | PC p. 358 | A concentrated attack delivers a powerful hit to one creature's spirit. | Preserve single-target impact; native spirit damage mapping must be documented. |
| [Chain Lightning](https://2e.aonprd.com/Spells.aspx?ID=1462) (`1462`; `pf2_chain_lightning`) | 6; AP | PC p. 319 | Lightning jumps through distinct creatures while line of effect remains valid. | Cap native hops, forbid repeat victims, and retain line-of-sight constraints. |
| [Eclipse Burst](https://2e.aonprd.com/Spells.aspx?ID=1508) (`1508`; `pf2_eclipse_burst`) | 7; ADP | PC p. 328 | Freezing darkness hits a large area, harming living creatures with void and potentially blinding. | Use finite blindness and separate living-only void payload; document magical-light counteraction differences. |
| [Arctic Rift](https://2e.aonprd.com/Spells.aspx?ID=1444) (`1444`; `pf2_arctic_rift`) | 8; AP | PC p. 316 | A long freezing rift damages and slows victims; severe hits encase them in breakable ice. | Use destructible temporary restraint with cleanup; no permanent terrain crack is necessary. |
| [Falling Stars](https://2e.aonprd.com/Spells.aspx?ID=1521) (`1521`; `pf2_falling_stars`) | 9; AP | PC p. 330 | Four celestial impacts combine central blunt force and a chosen energy explosion without repeat overlap damage. | Respect per-cast victim budgets across all impact areas; cap projectiles and terrain effects. |
| [Cataclysm](https://2e.aonprd.com/Spells.aspx?ID=1460) (`1460`; `pf2_cataclysm`) | 10; AP | PC p. 319 | A vast eruption combines several elemental disasters with terrain-dependent physical impacts. | Use a bounded multi-element attack; avoid permanent world destruction or unbounded simultaneous damage. |

## Recurring adaptation decisions

The table identifies source distinctions worth preserving while balancing native outcomes independently. Pathfinder's action count, attack rolls, save degrees, initiative rounds, heightening, damage dice, and spell slots cannot be imported by copying numbers into Minecraft. Native recipes need explicit delivery, eligible targets, damage/healing/absorption, pulse counts, repeat-hit limits, cleanup, and paid timing. Numerical constants should be tuned in the native authoring policy while relative trait units remain stable.

Several identity distinctions deserve extra care: Vampiric Feast grants temporary resilience rather than restoring lost health; Field of Life helps living creatures and harms undead; Bind Undead controls an existing eligible undead rather than summoning a new one; Falling Stars limits repeated damage from overlapping explosions. A conversion that changes these features should state that directly. [Vampiric Feast](https://2e.aonprd.com/Spells.aspx?ID=1736), [Field of Life](https://2e.aonprd.com/Spells.aspx?ID=1526), [Bind Undead](https://2e.aonprd.com/Spells.aspx?ID=1449), [Falling Stars](https://2e.aonprd.com/Spells.aspx?ID=1521)

## Wanderer's Guide access and future use

The [Wanderer's Guide homepage](https://wanderersguide.app/) was reachable, but the text browser received an empty client-rendered shell. Its public documentation and [first-party repository](https://github.com/wanderers-guide/wanderers-guide) were readable. This is a limitation of the available text view, not evidence that the site or its content is unavailable.

The documented API uses POST endpoints with bearer authentication, either an account-created API key or Supabase JWT. No authenticated content query was made in this research session, and no Wanderer's Guide spell IDs are invented or silently substituted for AoN IDs. [API introduction](https://docs.wanderersguide.app/api-reference/introduction), [Find spells](https://docs.wanderersguide.app/api-reference/content/find-spell)

Wanderer's Guide documents separate spell and content-source tables, source IDs on content rows, and official-versus-homebrew filtering. An eventual import should retain its source identity and distinguish published Paizo material from user-authored homebrew. This batch uses the directly verified AoN records so execution has no dependence on an external API. [Content data model](https://docs.wanderersguide.app/guides/content-data)

## Attribution and published notices

AoN's [license page](https://2e.aonprd.com/Licenses.aspx) currently publishes a Community Use Policy notice, an Open Game License section, and book-specific attribution. Its credits identify Player Core as Paizo 2023, Player Core 2 as Paizo 2024, and Rage of Elements as Paizo 2023, with their designers/authors. Preserve the book/page links and these attribution sources alongside the native recipes; this research does not import rules paragraphs, artwork, setting text, source implementation code, or logos.

Paizo's [license overview](https://paizo.com/licenses) distinguishes ORC Licensed Material from Reserved Material and distinguishes ORC from older OGL material. Its Remaster announcement identifies ORC as the new core publishing foundation. The overview does not grant access to Paizo art, maps, trademarks, or setting prose merely because mechanical rules are accessible. These are factual summaries of the publishers' notices, not a legal determination for Vestige distribution. [Remaster announcement](https://paizo.com/blog/pathfinder-second-edition-remaster-project)

The direct `paizo.com/orclicense` page returned HTTP 403 to the research browser; the published license overview was readable. No assumption about a particular product's full license notice was substituted for that unavailable page. Distribution attribution should use the actual applicable publication notice, while the native adaptation remains clear about its own authored behavior and limitations.
