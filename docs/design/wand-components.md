# Wand component palette

**Component direction locked and native implementation authorized, October 6, 2026.** Seven bases, five threads and the [selected eight tips](wand-tip-catalog.md#locked-first-eight-tips) compose with one exact source scroll variant. Artwork depends only on base and optional tip. The source retains its existing Spellshaping, and tips contribute additional equipment effects. The first native implementation covers untipped binding/casting and the profiles below, using their draft coefficients as starting tuning. Tip effects, final artwork, viewers and encounter balance remain outstanding.

## Three component roles

| Component | Main decision | Authored contributions |
| --- | --- | --- |
| **Base** | Which magic suits the wand's body? | Maximum durability and a small affinity bonus selected by existing spell traits. |
| **Magical thread** | How does the wand conduct or pay for magic? | Mana/preparation tradeoffs, durability capacity or wear, and selected compatible riders. |
| **Optional tip** | What additional property does this wand contribute? | Its own authored trait changes, compatible secondary effects or equipment properties, composed with the scroll's existing shaping. |

These roles describe the initial palette, not hard architectural restrictions. A future explicitly authored component may adjust another metric, but the first set should give players a recognizable reason to select each part. The base is always usable with every spell. Its affinity is a benefit when matched, not a school restriction. Two broadly usable cores provide economical or durable choices; specialized cores and tips require compatible executable plans.

## Initial base profiles

Use twenty maximum durability as the foundation. The following differences are implemented starting playtest values, not final balance or upstream material statistics.

| Base | Proposed matching traits | Affinity on a match | Maximum durability before thread |
| --- | --- | --- | --- |
| **Stick** | Neutral | No affinity modifier | **24**: a simple, durable general-purpose choice. |
| **Bamboo** | Plant **or** Wood | Amplify ×1.20 | **18** |
| **Bone** | Death **or** Necromancy | Amplify ×1.20 | **20** |
| **Blaze Rod** | Fire | Amplify ×1.20 | **20** |
| **Breeze Rod** | Air **or** Motion | Amplify ×1.20 | **18** |
| **End Rod** | Ender **or** Space | Amplify ×1.20 | **20** |
| **Lightning Rod** | Lightning | Amplify ×1.20 | **22** |

An either/or affinity contributes once even when both traits are present. It uses trait presence, not the sum of descriptive ratings: Iron profiles often use four while Pathfinder profiles often use one. Raw rating magnitudes are not a power ladder. An unmatched base still casts the complete spell with its authored durability capacity and chosen appearance.

Check the source scroll's resolved trait profile before applying equipment contributions. A tip that adds Lightning cannot make its own Lightning Rod qualify for the base affinity; source Spellshaping may already supply a relevant trait. This prevents equipment from repeatedly qualifying and amplifying itself.

The benefit is an explicit equipment consumer of the trait profile: for example, Bone checks Death/Necromancy and conditionally contributes an Amplify multiplier. This grants no inherent new meaning to Death or Necromancy. The current 214 packaged baseline spell plans explicitly read only Amplify, Range and Area in their numerical expressions. A local traversal found Amplify reads in 127 spells, Range in 153 and Area in 70. Existing Spellshaping riders may add other trait consumers. Multiplying Death alone does not strengthen a baseline outcome that reads Amplify. A spell without a compatible Amplify consumer receives no numerical affinity benefit; its other wand components can still matter. Direct descriptive-trait scaling remains possible where an effect explicitly consumes that trait, but must not be added as a second implicit damage bonus.

## Initial thread profiles

Thread names and their selector blocks are accepted. The profiles below are implemented as starting equipment tuning, giving five distinct choices without changing the shared component recipe.

| Thread | Proposed contribution | Tradeoff and eligibility |
| --- | --- | --- |
| **Ensorcelled — Diamond** | Mana payment ×0.85 | Add ten preparation ticks (half a second). A broadly useful economy core for mana-bearing spells. |
| **Callous — Iron** | Add **8 maximum durability** | Add twenty preparation ticks (one second). A broadly useful endurance core. A twenty-durability body becomes twenty-eight. |
| **Smoldering — Gold** | One compatible **Kindled** contribution | A qualifying damaging hit can ignite; use existing bounded fire/time expressions. Start with mana ×1.12 and one additional wand wear per committed cast. No fire effect is added to an incompatible nondamaging plan. |
| **Laced — Emerald** | Preparation payment ×0.85 | Mana ×1.12. Reuse Hurried's timing consumer; an instant or already saturated preparation receives no benefit and rejects this paid modification. |
| **Consecrated — Glowstone** | One compatible **Purifying** contribution | Start with mana ×1.14. Cleanse the supported harmful statuses after eligible healing/protection, using the existing outcome and ownership rules. It is not a universal heal or cleanse on every spell. |

Preparation is charge time, separate from the wand's one-minute reuse cooldown. Extra preparation is added even to an otherwise instant cast for the two broad economy/endurance profiles. Smoldering is a focused damaging-spell choice; Consecrated is a support choice. The component compiler must check real executable eligibility, including callbacks and existing augments, before consuming the wand's construction inputs.

Iron's material flavors inspire these choices, but do not establish them as facts: Diamond has mana/magic uses, Iron has armor/resistance uses, Gold has ignition uses, Emerald has movement/evocation associations and Glowstone has native Purifying pairings. Assigning each thread a complete equipment profile is a Vestige design decision. Existing ordinary ingredient/socket pairs remain unchanged. These starting costs are the contribution's costs, not a second surcharge added after applying the same Spellshaping rule.

## Additional tip contributions

The owner clarified that the scroll may already carry Reaching, Bleeding or other shaping. A tip adds its own contribution beyond that existing magic. This supersedes the initial proposal that mapped Amethyst/Enduring, Diamond/Focused, Quartz/Widening, Ender Pearl/Reaching and Emerald/Forked and automatically advanced matching source augments to another degree.

The owner requires tips to be broadly reusable across casting or whole spell capabilities, rather than a few specific spells. The [full tip catalogue](wand-tip-catalog.md) proposes **twenty standalone candidates and eight optional Iron material directions**, with effects, draft prices, eligibility, combinations and artwork counts. Its selected eight are Amethyst Shard, Diamond, Emerald, Ender Pearl, Copper Ingot, Iron Ingot, Ghast Tear and Netherite Ingot. Quartz's follow-up mark and other narrow situational ideas move outside that starting set. These eight materials/effect directions are now selected; their effects are not implemented in the untipped foundation. Pure utility spells must have several useful choices; audit actual native compatibility before shipping tipped wands. No tip remains valid for every otherwise compatible wand and adds no tip-specific contribution or price.

Two selected directions illustrate the distinction; their coefficients still need playtesting:

- **Diamond / refraction:** a primary damaging or healing outcome could distribute a small bounded pool to nearby eligible enemies or allies, using the appropriate outcome channel. An already Reaching Fireball keeps its reach and gains the extra damage burst; healing spells can use the same distribution mechanism for nearby allies. It would not recast or duplicate the whole spell.
- **Amethyst / resonance:** a qualifying primary damaging or healing outcome could produce one weaker delayed echo. This would be a separate contribution rather than simply renaming the source's Enduring degree. Its recipients, timing, ownership and cleanup need explicit rules, shared across delivery types rather than restricted to a few finite fields.

Additional trait scaling or wand properties are also possible. Sharing a numerical axis with source shaping is allowed when explicitly authored; it does not automatically select the same augment ID. Use the native trait, composed-effect and typed-cost machinery rather than creating a separate version of every spell. Every candidate needs compatibility rules, meaningful behavior and a balanced price before it becomes an equipment rule. These material/effect associations are Vestige proposals, not upstream Iron facts.

## Combining contributions

Store one verified source scroll variant and the selected component identities. Do not store arbitrary authored effect graphs in the finished item. Resolve trusted component definitions into one temporary immutable cast view and retain the source scroll's spell identity, existing augment selections and original geometry shaping exactly once. Never multiply that shaping by the three scroll inputs or silently add a second binding-ritual geometry bonus.

Keep the channels distinct:

- **Maximum durability:** `20 + base adjustment + core capacity adjustment`. The proposed first palette spans eighteen to thirty-two durability. Round and clamp once when creating the wand; do not reroll capacity on use.
- **Wand wear:** `1 + explicitly authored additional wear`. The first palette uses one per committed cast, or two with Smoldering. Use integer deterministic wear, not a percentage chance of breaking or independently rounded fractional charges. A worn twenty-durability wand with two wear per cast has ten ordinary committed casts. Preserve used durability through splitting/saving and future component changes; repairing/rebinding are undecided.
- **Spell traits and additional effects:** resolve affinity and tip contributions through the existing trait/effect machinery. Preserve source augment identities and degrees. Do not add a parallel direct damage multiplier or count a two-trait affinity twice.
- **Spell payment:** compose the source's existing modifiers and the selected core/tip typed payment changes once, then round the final resource quantity once. A mana-only factor changes mana, not health, hunger, wand wear or recovery. Existing typed nonmana costs are preserved unless an explicitly authored rule changes them.
- **Preparation:** resolve source preparation and its existing shaping, then selected preparation additions/factors in one documented order. The first palette never adds both economy cores together because a wand has one thread.
- **Reuse:** retain the accepted sixty-second wand cooldown as its own constraint. Proposed guard is at least sixty seconds and any longer already-resolved spell recovery. Do not shorten this through a generic mana/preparation discount. The implementation uses one extra per-player/per-spell wand recovery group, shared by all base/thread copies. Ordinary scroll recovery remains its native duration. Both restrictions must permit a new wand cast.

Keep the source scroll's bounded augment selections separate from the tip's own trusted component definition. A tip does not automatically turn Reaching into Greater Reaching or attach another copy of a source rider. If an additional contribution affects the same numerical axis or event, author its interaction and limits explicitly, including shared recipient/hit/rider budgets where needed. Reject an incompatible or saturated paid contribution before consuming materials. The Kindled/Purifying cores reuse existing riders. Existing source augments that ignite or cleanse reject the overlapping core; native ignition also rejects Smoldering. Consecrated supports actual healing and protection, sharing one recipient budget when both exist.

All payments, wear and the cooldown commit together at the native successful payment boundary; canceled preparation, insufficient resources or rejected activation spend nothing. A paid cast can still miss, be resisted or forfeit under its existing volatile policy. Ordinary wear is separate from ritual explosions and volatile forfeits; no new random wand-break policy is proposed.

## Example combinations

**Blaze Rod + Ensorcelled Thread + a proposed refracting Diamond tip**, binding an already Reaching Fireball: the source retains its Reaching contribution and geometry shaping once. The proposed Fire affinity contributes Amplify ×1.20, and Ensorcelled contributes mana ×0.85 plus ten preparation ticks. The tip would add the separately authored bounded impact burst, with its own price still undecided. It does not automatically increase the source's Reaching degree. The proposed body has twenty maximum durability and one ordinary wear per committed cast before any tip-specific changes.

**Stick + Callous Thread, without a tip**, binding a compatible spell: thirty-two proposed maximum durability and one wear per cast, plus twenty preparation ticks. The neutral Stick makes no elemental affinity claim. Existing scroll shaping remains intact, and the untipped wand pays for no tip effect.

The full tipped combination remains an implementation target; the untipped base/thread calculations are implemented and tested. These examples are not encounter balance or final art claims. Mana expenditure retains the bound spell's ordinary resource model; the earlier free-casting equipment idea is historical, not silently chosen by this palette.

## Artwork and next implementation

The owner clarified that **thread choice does not change wand artwork**. The earlier five-tip shortlist would need **42** base/tip appearances. The selected eight-tip palette needs **7 × (8 + 1) = 63**, with up to 315 mechanical component combinations per spell before compatibility restrictions. The eight-tip direction is locked; the [full catalogue](wand-tip-catalog.md) gives counts for larger choices. Reuse base/tip layers or models where practical; optional tip rendering does not need a new spell-specific picture. Separate ingredient icons for the five thread items remain their own small art task.

The owner has locked the eight-tip direction and authorized implementation. Untipped binding, the base/thread compiler, deterministic wear, cooldown commitment and persistence now supply its foundation. Implement each tip's distinct effect and price, audit actual compatible plans, add native world tests, then inspect base/tip artwork and viewer presentation. The [current Spellshaping ledger](../spellshaping-recipes.md) remains authoritative for scroll shaping; equipment interprets trusted contributions separately and introduces no inherent trait semantics.
