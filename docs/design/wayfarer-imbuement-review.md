# Wayfarer Boots: imbuement review

**October 8, 2026 — owner-approved package; implementation in verification.** The owner approved the complete baseline, four imbuements, all sixteen combinations, repair convention and selected Feather Tabs artwork in **Design puzzle-based spell crafting** after reviewing this concrete package. Their instruction was “Let's do the boots. Let's do it all. You're approved.” The direct human approval was inspected in that chat. Wayfarer is now registered and uses the shared trait/effect/payment runtime. Native completion evidence is recorded in [the implementation receipt](../verification/wayfarer-2026-10-08/README.md). The previously approved robes retain their behavior and artwork; their own crafted imbuements remain separate work.

## Baseline to carry forward

The approved baseline and exact crafting behavior are listed below. Numeric values remain open to survival playtest tuning without changing the approved control or shared-runtime design.

| Property | Approved baseline |
| --- | --- |
| Equipment | Feet; 1 armor; zero toughness; no maximum-mana bonus |
| Trigger | Deliberate grounded sprint-jump; ordinary walking/jumping remain ordinary |
| Burst | 3 seconds at +20% movement speed |
| Landing benefit | During the burst, prevent up to 4 HP / two hearts of damage on one landing; unused protection expires with the burst |
| Payment | 5 mana, committed once on a successful activation |
| Recovery | 15 seconds **from activation**, owned by the wearer and Wayfarer ability across all copies/variants |
| Durability | 65 starting maximum |
| Wear | Normal armor damage and damage actually prevented by these boots; each eligible hit once, no activation/idle wear |
| Enchantments | Compatible ordinary armor enchantments; exclude Unbreaking and Mending under the accepted clothing policy |
| Appearance | Selected Feather Tabs inventory direction; brown leather and fixed ivory feathers; worn export still needs native review |

Use the approved eight-offering pattern: Leather on inner seats 0/2/4/6; Laced Thread on outer 1/5; Feather on 3; Rabbit's Foot on 7. Accept whole quarter-turns and preserve each offering/socket pair. Keep the Spellstone reference empty. Consume offerings and retain socket materials. The first draft keeps clothing's existing neutral geometry policy; it does not introduce geometry-derived equipment bonuses merely because the shared compiler can represent them.

Time consumes burst duration, Motion consumes the movement bonus and Amplify consumes landing protection. All three base ratings are one. Snapshot these values at activation through the common resolver. Duration does not inherently change recovery, maximum durability or wear. Boots-local modifiers do not boost another item or spell.

Laced Thread repairs one quarter of the variant's maximum in the native anvil, rounded down (16 on plain boots, 24 on Reinforced), without changing variant IDs or other components. This extends the matching-thread repair convention used by robes. Optional leather dyeing is deferred; no recoloring feature is needed to ship the chosen brown design.

## Four initial imbuements

The following IDs, selectors and adjustments are approved and implemented. The reusable [adjective catalog](../magic-adjectives.md) owns the exact names for every combined set. Each contribution has **maximum authored degree one**. This limits the initial recipe vocabulary; it does not cap growth from actor trait boosts.

Figures assume no actor boosts and only the listed contribution. Typed payments use the actual shared final rounding described below.

| Contribution ID / adjective | Offering + retained socket | Exact adjustment | Result / trade-off |
| --- | --- | --- | --- |
| `vestige:wayfarer/swift` / *Swift* | Feather + Emerald Block | Motion ×1.50; Time ×2/3 | +30% speed for 2 seconds, 5 mana. More immediate speed, less time to travel or obtain the protected landing. |
| `vestige:wayfarer/enduring` / *Enduring* | Rabbit's Foot + Amethyst Block | Time ×1.20; mana ×1.12 | +20% for 3.6 seconds, 6 mana. Longer movement and landing window at a higher price. |
| `vestige:wayfarer/reinforced` / *Reinforced* | Leather + Iron Block | Maximum durability ×1.50; mana ×1.25 | 98 maximum durability and 6 mana per activation. More life between material repairs, higher activation price; armor stays 1. |
| `vestige:wayfarer/quickened` / *Quickened* | Laced Thread + Copper Block | Recovery ×0.80; mana ×1.25 | 12-second recovery and 6 mana. More frequent paid bursts; burst duration/speed and wear remain unchanged. |

These are independently authored Vestige routes. Emerald's pinned movement-speed property supports Swift; Copper's Ring of Recovery ingredient use supports Quickened. Iron's protection association informs reinforcement but is not an upstream maximum-durability formula. Amethyst is not inherently a Time material: Enduring deliberately reuses Vestige's existing **Time ×1.20 / mana ×1.12** meaning through a new Rabbit's-Foot route. No upstream quality value is used. See the complete [pinned material reference](../research/iron-material-uses.md) and executable [Spellshaping rules](../../tools/author_spellshaping.py).

Do not redefine existing exact pairs. In particular, Feather/Copper is **Hurried**, which changes preparation time; it is not Quickened recovery. The boots have no preparation consumer, so that recognized incompatible pair rejects before consumption. Unrecognized pairs remain neutral. Matching is item-scoped and must explicitly establish compatibility rather than inheriting every spell rider.

## Repetition, combinations and naming

Any one of the four Leather offerings can provide Reinforced. Either Laced Thread can provide Quickened. No compass position or informal recipe-role label selects a privileged copy. Matching two Leather/Iron pairs or two Laced-Thread/Copper pairs exceeds the authored degree one and rejects before consumption; do not silently discard one or multiply four copies. Stronger degrees can be separately authored later with their own prices and outcomes.

The four choices use different offerings and are mutually compatible. Every subset is reachable: **sixteen sets including plain boots**. Each stored selection contains a trusted contribution ID and degree. Sort selections canonically; rotating the complete pattern or encountering ingredients in another order must not change the result.

For this first set, combinations retain their independent adjustments. A single italic adjective names the complete nonempty set. The coordinated [naming catalog](../magic-adjectives.md) owns the exact aliases, including degree-aware lookup; no alias changes the effects. For example, Swift + Enduring displays as *Striding* Wayfarer Boots, and all four display as *Unfaltering* Wayfarer Boots. A future true compound must explicitly replace its participating mechanics and must not also apply them individually. No such replacement is proposed here.

The authored shared `MagicAdjectives.prefix` helper accepts family `vestige:wayfarer_boots` and the exact contribution IDs/degrees above. It matches complete sets independently of input order and returns an italic prefix with a trailing space for the plain item title. The registered boots use that helper; equipment code does not duplicate the naming matrix. The naming agent completed [helper and existing-item verification](../verification/magic-adjectives-2026-10-08/README.md): all sixteen Wayfarer sets are covered, and 195 unit tests, packaging and Kithkyn compatibility passed. That earlier receipt covers naming only. The newer boots receipt covers variant persistence, effect compilation, native gameplay and presentation separately.

| Chosen contributions | Burst / speed | Mana | Recovery | Maximum durability |
| --- | --- | ---: | ---: | ---: |
| None | 3 seconds / +20% | 5 | 15 seconds | 65 |
| Swift + Enduring | 2.4 seconds / +30% | 6 | 15 seconds | 65 |
| Enduring + Reinforced | 3.6 seconds / +20% | 7 | 15 seconds | 98 |
| Reinforced + Quickened | 3 seconds / +20% | 8 | 12 seconds | 98 |
| All four | 2.4 seconds / +30% | 9 | 12 seconds | 98 |

Every row retains one landing benefit of `4 × resolved Amplify` HP. For example, a wearer multiplier of Motion ×1.5 makes Swift's movement bonus `20% ×1.5 ×1.5 =45%`; the item modifier and actor boost each apply once. A Time ×1.5 wearer boost makes Swift + Enduring last 3.6 seconds rather than 2.4. It does not also shorten recovery or increase carrier durability.

Use the current `CastShaping.costs` payment convention: multiply compatible mana factors, apply any explicitly accepted casting-cost factor once, then round the final amount with `floor(value + 0.5)`. Do not round individual factors first. Thus Reinforced alone costs 6 from raw 6.25, and Reinforced + Quickened costs 8 from raw 7.8125. There is no alternate resource exchange in this first set. Maximum durability separately uses `floor(baseMaximum × carrierFactor + 0.5)`, giving 98 from 97.5; changing a carrier property must preserve existing damage rather than refill the item. This implementation does not change shared runtime rounding.

At ordinary baseline, a three-second burst leaves twelve seconds of the fifteen-second recovery. Quickened leaves nine. Duration boosts may outlast recovery; a later paid activation can replace the old burst but must not stack speed modifiers, accumulate landing charges or reset recovery from item swapping. A jump before recovery ends cannot refresh anything. Unequipping/breaking ends the source's active benefits while preserving the actor's recovery.

## How this extends to the other items

The shared mechanism supports more than numeric improvements. A future authored variant may replace an effect or activation behavior, provided it uses the same runtime and has an explicit trade-off. For example, a Wayfarer variant could replace its running burst with one stronger leap; that is a separate ability fork to review, not an automatic consequence of a Slime socket or a new meaning for Motion.

| Item | Useful next review directions | Constraint to preserve |
| --- | --- | --- |
| Wardweave | Ward lifetime versus protection; carrier durability versus an explicitly priced disadvantage; potentially change how its one charge is spent | Approved unimbued ward, one-charge ownership and absorbed-hit wear; no new passive mana drain without review |
| Cinderweave | Fire fraction versus per-hit protection budget; a deliberately different fire-defense behavior | Resolve each hit through the common pipeline; do not silently grant fire immunity or an ignition attack |
| Dawnsight | Reveal duration versus distance, preparation/recovery trade-offs, an authored alternative detection effect | Private wearer-only reveal and line-of-sight policy unless explicitly changed |
| Spiderstep | Climb speed versus mana drain; an authored attachment/hold behavior | Pay the actual attached time; no free repeated sub-second activation or ceiling movement by accident |
| Patchwork / Crane Bag | Explicit storage behavior or carrier changes | Real persistent contents, channel identity and a reviewed overflow policy before changing capacity |
| Wands / staffs / existing devices | Compatible source shaping, durability/wear and explicit payment/effect alternatives | Preserve stored scrolls, fixed affinity, attunement identities and existing approved routes |

Use the common [resource equivalence](resource-payments.md) when a higher-cost item gains alternate payments: 1 heart = 4 food points = 30 XP = 30 mana. At five mana, whole-heart rounding is poor value; the first boots set leaves Health/Hunger/XP routes unselected. No alternate-payment price is invented here.

## Implementation and review boundary

The resource/imbuement agent confirmed the common recipe → local contributions → single compilation → stored variant direction, explicit degrees/conflicts, and separation of naming aliases from mechanic-replacing compounds. It owns the adjective catalog; equipment work owns boot mechanics, crafting and art. The boots now persist bounded trusted contribution IDs/degrees and compile their modifiers/payment through the common runtime. Other equipment families still need their own compatible authored policies. The existing `Spellshaping.contribute` exchange omission, compiler-local 75% exchange limit and missing shared typed XP payment remain follow-ups in [item imbuement and readiness](item-imbuement-and-readiness.md); this boots set does not depend on alternate payments.

Verification covers: ordinary and shaped recipes; all sixteen reachable sets; duplicate rejection without consumption; rotation/permutation distinctions; trusted component round-trips and repair preservation; current actor boosts applied once; exact composed payment; failed/canceled activation; sprint/jump permissions, swimming/mount/flight transitions; one landing only; normal and fully prevented-hit wear; break/removal cleanup; recovery across copies and variants; and the accepted two-layer readiness display using final payment/recovery. Inspect the actual registered inventory and worn model in the native client before claiming presentation completion.

**Design verification:** exact rational arithmetic checks all sixteen reachable sets, the displayed payment/duration/durability examples and the two wearer-boost examples. Local links and scoped whitespace pass. The [numerical receipt](../verification/wayfarer-imbuement-review-2026-10-08.json) lists every set. This is review evidence, not a gameplay test.

The owner subsequently approved the complete package described above. The implementation receipt records actual tests and client evidence; publication and testing-pack installation are handled by the coordinated shipment chat. Other clothing imbuements and unimplemented clothing packages remain follow-up work.
