# Magical adjective naming

**Owner direction, October 8, 2026:** give combinations of magical adjustments one italicized adjective across spells and items. Keep a reusable vocabulary of individual adjectives and a separate vocabulary reserved for combinations. The owner approved the concrete Wayfarer package and authorized implementation and publication; the earlier final-review hold is resolved.

The generated [adjective catalog](../magic-adjectives.md) is the complete current inventory and naming matrix. The authored policy lives in `tools/magic-adjective-policy.json`; `tools/author_magic_adjectives.py` checks it against the executable spell rules, wand tip names and five thread identities, then generates the packaged naming data and catalog.

## What a name means

An **adjustment adjective** describes one authored contribution. Enduring can describe supported finite spell duration, a longer Wayfarer burst or a longer hourglass recall. Each ability explicitly supplies its compatible consumer and price; the word itself grants no trait or engine behavior. Reusing a word does not copy another item's coefficients or payment routes.

A **combined adjective** describes an exact complete set of contributions. For example, Swift + Quickened becomes *Nimble* Wayfarer Boots. Both adjustments and both trade-offs still resolve once. The alias is presentation only, not a new trait, mechanic, recipe ingredient, saved ability graph or attunement identity component.

A **mechanical compound** is different: the existing Vampiric, Hemorrhagic, Tempestuous, Glacial, Stormbound and Sustaining patterns replace their participating individual effects with one authored rule. Their actual offering/material patterns remain authoritative. Do not infer those mechanics from a display adjective or turn independent adjustments into replacements merely to shorten their name.

## Exact composition and display

- Match stable contribution IDs, their degrees and the item family. Encounter order, compass orientation and the rotation of a valid recipe do not change an alias.
- Match the complete set. A name for two adjustments must not hide a third, a different degree or a different contribution which happens to share a word.
- Keep different source IDs distinct. A Repelling scroll adjustment and an Iron wand tip both use Repelling, but are separate contributions with separate prices and bounds.
- A family with an authored finite palette must have a name for every allowed combination before its naming is considered closed. Wayfarer's four degree-one choices have all sixteen names, including the plain item.
- Reserve combination words for multiple adjustments. They must not also name an individual adjustment or thread component. The same combined word can be reused across item families for the same constituent adjective/degree vocabulary, such as Enduring + Reinforced → Stalwart.
- All mixtures receive one word. Use the exact authored alias when present; otherwise use *Confluent*, a neutral word reserved for multiple contributions. This fallback states only that magic is combined. It does not infer extra effects or reuse a partial match. Distinct stored variants may share a visible name, just as unidentified scrolls already do; crafting, stacking and binding must compare actual variant data.
- Single adjustments retain their existing Ordinary/Greater/Grand degree presentation. A degree qualifier is not another adjustment. Exact mixture aliases include degree in their identity; a different degree uses its own authored alias or Confluent. The full IDs and degrees remain stored and compiled regardless of the shorter display name.
- Only the adjective prefix is italicized. The base item or spell name keeps its normal style, rarity color and knowledge gate. Scroll tooltips still contain only their name.
- Store actual contribution IDs/degrees. Derive names from trusted catalog data so renaming an adjective never changes matching, resources, wear, traits, source scroll identity or discovery.

## Wayfarer closeout

The complete naming matrix is authored and checked in the [catalog](../magic-adjectives.md#scope-and-coverage). Four singles are Swift, Enduring, Reinforced and Quickened. Their eleven combined names are Striding, Surefooted, Nimble, Stalwart, Tireless, Dependable, Dauntless, Restless, Agile, Resolute and Unfaltering.

The [approved equipment package](wayfarer-imbuement-review.md) owns actual selectors, ability formulas, costs, durability, repair and verification. Equipment implementation remains with that work; the naming catalog does not introduce a second boot adapter.

The hourglass vocabulary covers all 36 design sets: one temporal choice, one vessel choice and one payment choice, each optional. Fleeting + Frugal → Ephemeral, Enduring + Reinforced → Stalwart, and Enduring + Reinforced + Bloodbound → Relentless preserve the discussed examples. These reserved names do not implement its history or payment mechanics, finalize its open gameplay details, or register an item. Erudite also names the existing XP-backed Homebound Eye route; shared typed XP payment for future abilities remains separate work.

## Existing spell and device coverage

Scrolls and their stored staff labels share the same aliases. A wand includes its source scroll adjustments and its tip as distinct contributions before matching the full name. For example, Reaching + Widening is *Expansive*, Focused + Bleeding is *Rending*, and a Shocking source with a Conductive Copper tip is *Galvanic*. Any additional contribution requires a new full-set alias or becomes Confluent. The generated catalog records every exact alias and the independently authored underlying effects.

Homebound Eye's existing mutually exclusive payment routes reuse Bloodbound (health), Fasting (food), Erudite (experience points) and Charged (mana). Its ordinary durability route retains the plain name. The names do not change its costs, origin, attunement marks or saved route. Unmodified robes, threads, bags, shells, staffs and cosmetic apparatus variants keep their proper names; a finish, affinity or attunement signature is not automatically a magical adjustment. Future shaped equipment must explicitly supply validated contribution IDs to the same helper.

## Verification

Run `python3 tools/author_magic_adjectives.py --check` after changing source names or policy. The authoring tool rejects ambiguous identities, combination words reused as individual words, unsupported degrees and gaps in a declared complete family. Java tests cover every Wayfarer/hourglass design set, every spell pair at supported degrees, encounter order, exact-set/family behavior, single-degree preservation, duplicate rejection, separate scroll/tip identities, and italic-only prefix formatting. Native Eye tests verify the actual item's name without mutating its saved route/key. Existing scroll/wand name adapters retain knowledge gating outside the helper.

Actual registered-item client review remains required for new equipment presentation. Model tests and a completed naming catalog do not establish boots gameplay or worn-art verification.
