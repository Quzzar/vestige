# Scroll discovery and progression

**Item art implemented October 6:** native scrolls use [Electroblob's original 16×16 parchment with its blue binding recolored red](../art/scroll-red-binding-v5/README.md), with 1.125× inventory display scale. The binding is cosmetic and shared across spells. [Native client inspection](../art/attuned-items-native/README.md) verifies the packaged sprite; Prism installation remains pending. These art changes alter no scroll casting, discovery, recipe or trait behavior.

**Scroll fragment presentation, approved and locked October 6:** fragments use the owner-selected irregular torn-corner scrap, imported as a small 7×8 opaque silhouette inside a transparent 16×16 sprite. Resolved fragments display **Scroll Fragment: [symbol]**, with one distinct original glyph for all 50 built-in traits plus Glass and Oil used by shipped Pathfinder spells, in `vestige:fragment_symbols`. The custom font and muted gold (`#d6b46a`) apply only to the symbol; ordinary name text retains its default font and color. The 52 explicit pixel glyphs are authored in `tools/fragment-symbols.json`; `tools/author_fragment_symbols.py --check` validates catalog/shipped coverage, unique characters, unique visible silhouettes and packaged asset drift. Unassigned fragments retain the plain name. Unknown Vestige traits retain their path; other namespaces retain their full trait ID as a name fallback. Names and glyphs are presentation only; saved trait IDs, dismantling rolls and discovery matching retain their existing semantics. Art sources, prompt and native inspection evidence are in [the fragment archive](../art/scroll-fragments/README.md).

Status: **accepted discovery rules, implemented October 3, 2026**. Native scroll casts, identification, dismantling and fragment reconstruction follow the rules below; stronger combined unknown/volatile risk remains open. Mixed fragments replace the earlier matching-only discovery recipe. The owner's later [Plinth direction](plinth-rituals.md) supersedes the three-block apparatus and supporting-block context described in the implementation section below. Variable geometry is implemented; embedded material sockets select the executable Spellshaping rules. [Iron's material standard](material-crafting.md) still informs explicit contributions; the removed Wizardry systems remain retired.

## Knowledge states

There are exactly two spell knowledge states, per player:

| State | Meaning |
|---|---|
| Unknown | The player has not yet successfully cast the spell from a scroll; unknown-spell chaos risk applies |
| Identified | The player has successfully cast the spell from a scroll; unknown-spell chaos risk is removed, while inherent volatility remains |

Craft history is a separate per-player recipe memory, not a third spell knowledge state. A successfully committed ingredient ritual reveals that spell’s actual recipe in optional JEI/EMI viewers. Receiving a scroll, fragment discovery, identification, inspection, canceled rituals and failures do not teach its ingredient recipe. Both records survive saves and player clones. Earlier crafts were not recorded; recipe memory begins with crafts committed by this implementation.

There is no third “learned” state. Earlier language about learning refers to the transition from unknown to identified, not a separate unlock. A successful scroll cast identifies the spell; wand casts and other cast sources do not. Unknown scrolls display **Unknown Scroll** until that player identifies the spell. Unknown tooltips hide spell IDs, augments and shaping details, including in advanced tooltips. Identification reveals the named scroll; every scroll tooltip contains only its name, including advanced tooltips and shaped scrolls. Casting, identification, recasts, cooldowns and failed attempts emit no chat or actionbar text. Identification does not grant a free casting method.

**Scroll name colors, approved October 6.** The owner retained the literal **Unknown Scroll**, in white for every unidentified spell. Identification reveals native Common names in white, Uncommon in yellow, Rare in aqua and Mythic in light purple (Minecraft’s Epic color). Italic augment adjectives inherit that same color. The server sends the identified spell’s native rarity only to its viewing player; crafting alone never reveals rarity. The item’s shared rarity component does not encode this personal knowledge. [Wizardry source review](../research/electroblob-scroll-name-colors.md) distinguishes its stable glyph names and white scroll names from Vestige’s chosen presentation.

## Accepted discovery loop

| Action | Input | Result |
|---|---|---|
| Right-click to cast | One spell scroll | One use of its spell; a successful cast identifies it for that player |
| Dismantle in a crafting table | One spell scroll | Three independently rolled trait fragments |
| Use the structure's spell discovery operation | One fragment in each of at least four pedestal slots; traits may differ | One random spell scroll containing every supplied trait |

A player may attempt an unknown spell from its scroll. Crafting or receiving a scroll does not identify its spell. A chaotic forfeit is not a successful cast and therefore does not identify the intended spell. Identifying a volatile spell does not remove its inherent chaos risk.

## Current implemented crafting structure

Both capacities use one Spellstone and the same Plinth block: four inner Plinths for a four-slot recipe, plus four outer Plinths for an eight-slot recipe. Each ring may be Cross or Diagonal, with independent spacing and signed height steps. The nearest complete ring is inner; ingredients and reference capacity select a unique valid layout. Four-slot ingredient recipes ignore the outer ring entirely. [Playtesting](../leyline-playtesting.md) records bounds, selection and construction grids.

Right-click the top to offer an item; a side click with a block item installs an independent material socket. Sneak-click a side with an empty hand recovers that material. The socket selects an authored material/offering Spellshaping pairing. Every column segment can be imbued independently. Plinth offering surfaces require air or unobstructed fluid above, while side sockets remain accessible under solid covers. Both apparatus roles support waterlogging and submerged crafting. Blocks beneath the Plinth are decorative foundations, without recipe or shaping semantics. The reference rests flat on Spellstone; the result is an ordinary centered dropped item, and collecting it preserves the reference.

For discovery, at least four occupied slots must each supply one fragment. Basic discovery fills all four; advanced discovery may use four through eight. Every participating fragment contributes a required trait and one entry to the average-rating weight. The operation never silently discards a supplied fragment to make the pool eligible. A stack in one slot cannot replace four occupied slots. At commitment each participating slot consumes one fragment and produces an **unshaped base scroll**; ingredient crafting can then create a shaped version. Discovery does not apply geometry modifiers or material quality.

The former Stone/Runic Pedestal IDs and migration code are removed. Fresh worlds use only Spellstone/Plinth; historical construction grids and art remain documented separately. [Spellstone rituals](ritual-crafting.md) describes commitment, failure feedback and cancellation.

## Operations on the same structure

| Operation | Purpose | Confirmed design and result |
|---|---|---|
| Spell discovery | Turn fragments into a random spell scroll | At least four occupied fragment slots; eligible spells contain every supplied trait; outputs one scroll |
| Spell crafting / augmenting | Craft or augment spells | Uses the same basic/advanced structure concept; explicit circular ingredient recipes; outputs a spell scroll |
| Magic gear / equipment crafting | Create magical equipment | To design and implement eventually |

Discovery and spell crafting/augmenting use the same structure concept, replacing the earlier separate-station proposal. Equipment crafting remains eventual work; this clarification does not finalize its recipes or placement in the shared structure. Scroll dismantling remains an ordinary crafting-table recipe. Producing a scroll still does not identify its spell; identification requires a successful scroll cast.

Discovery's pool selection and spell crafting's composition/augmentation remain different operations on the shared structure. Crafting inputs, relative arrangements, success/failure feedback and future slot-local spell shaping are defined in [Spellstone rituals](ritual-crafting.md). No base-scroll-plus-three-fragments recipe or automatic benefit from filling eight slots is adopted here.

### Superseded proposal vocabulary

An earlier proposal distinguished unknown contents, identified contents and a learned spell usable for inscription into a reusable focus. The owner has replaced that proposal with the two states above. Another proposal used “study” for gaining knowledge from magical traces, “distill” for extracting crafting essence, and “weave” for applying their magic to equipment. Those proposed trace-processing actions are not adopted mechanics, station names or extra progression states. The selected actions are casting, dismantling, discovery, spell crafting/augmenting and eventual equipment crafting.

## Dismantling: trait proportions

For a spell's base trait profile, each of the three fragment rolls uses:

```text
P(fragment has trait t) = rating(t) / sum(all positive trait ratings)
```

The rolls are independent, with replacement. A scroll with `[fire 3] [evocation 1]` gives each fragment a **75% fire / 25% evocation** chance. A batch can contain three fire fragments, a mixture, or three evocation fragments. It does not guarantee any particular mix.

| Batch from that example | Probability |
|---|---|
| Three fire | 42.1875% |
| Two fire, one evocation, in any order | 42.1875% |
| One fire, two evocation, in any order | 14.0625% |
| Three evocation | 1.5625% |

The result is unknown until crafting actually happens. Opening the crafting table, previewing a recipe or moving its input must not reveal or reroll the result. Each consumed scroll produces exactly three fragments, including when multiple scrolls are crafted.

Working interpretation: “one of the traits of that spell” includes every positive trait in its immutable base profile, including `amplify`, `range`, `area` and `volatile` when present. There is no confirmed exclusion list. For example, current Fireball has fire 4, evocation 4, amplify 1, range 1 and area 1: fire would have a 4/11 chance, rather than 1/2. Excluding scaling or volatile fragments would require another design decision. Equipment modifies casts, not these crafting weights.

## Reconstruction: ratings across spells

### Eligibility is the intersection of supplied traits

At least four occupied fragment slots produce **one scroll**, not immediate knowledge. Let `T` be the set of distinct traits supplied by every participating fragment. Eligibility uses the immutable base spell profile:

```text
eligible(s) = every trait t in T has base rating(s, t) > 0
pool = all enabled native spell identities satisfying eligible(s)
```

Extra traits are allowed. No particular minimum rating is required beyond being positive: two Fire fragments do not require the spell to have Fire 2. Duplicate fragments repeat an existing requirement rather than introduce another trait type, and contribute that rating again to the weighted average below.

| Fragment setup | Required traits |
|---|---|
| Fire, Fire, Fire, Fire | Fire |
| Fire, Fire, Evocation, Evocation | Fire AND Evocation |
| Fire, Fire, Evocation, Range | Fire AND Evocation AND Range |
| Fire, Evocation, Range, Area | Fire AND Evocation AND Range AND Area |
| Four to eight filled advanced slots | Every distinct trait supplied by those filled slots |

The owner's locked example: four Fire fragments can yield either a Fire-only spell or a Fire + Evocation spell. Mixing Fire and Evocation fragments excludes Fire-only spells, while allowing spells with both traits and any additional traits. Apply this eligibility filter before calculating selection weights.

For Fire + Evocation + Range + Area, a fire-heavy spell lacking Area is excluded, however large its other ratings are. This is an intersection, not separate trait-pool rolls followed by a choice between their results. If no spell contains all required traits, the proposed implementation should reject the recipe without consuming inputs; it must not silently drop a supplied requirement or choose a fallback spell.

### Weighting after filtering

After filtering, selection weights each eligible spell by the arithmetic mean of its ratings across the participating fragment slots. Let `N` be the occupied fragment-slot count and `n(t)` the number of those fragments carrying trait `t`:

```text
weight(s) = sum(n(t) × base rating(s, t), for all supplied traits t) / N
P(scroll contains s) = weight(s) / sum(weights of all eligible spells)
```

For four Fire fragments, `weight(s) = (4 × fire) / 4 = fire`. If the eligible pool contains only one fire-5 spell and one fire-1 spell, their chances are **5/6 and 1/6**, preserving the earlier rule. Adding more identical Fire fragments does not change the rule to `fire^4` or `fire^8`. Fractional positive ratings work directly as weights; the current trait model does not require whole numbers.

For three Fire fragments and one Evocation fragment, `weight(s) = (3 × fire + evocation) / 4`. Consider a pool containing only these two hypothetical spells:

| Candidate | Fire | Evocation | Weight with two Fire + two Evocation | Weight with three Fire + one Evocation |
|---|---:|---:|---:|---:|
| A | 5 | 1 | 3 | 4 |
| B | 1 | 5 | 3 | 2 |

The first setup gives 50% / 50% odds. The second gives **2/3 for A and 1/3 for B**. Both spells still need both traits to enter either pool. A Fire 100 spell with no Evocation stays excluded; a high average cannot replace a missing required trait.

The advanced structure includes every participating fragment in the same average. Six Fire plus two Evocation has the same composition and relative odds as three Fire plus one Evocation. Filling eight slots does not inherently multiply output, improve rarity or increase a spell's potency. Only the supplied trait requirements, their proportions and explicitly designed supporting-block effects determine the recipe.

Do not normalize each candidate by the total of all its unrelated traits without an explicit decision. No rarity multiplier, tradition restriction or preference for unknown spells has been specified. Working default: each enabled native spell identity participates once; already identified spells remain possible results. Initial scroll loot sources and future acquisition restrictions are separate design work.

### Consequence for relative trait units

Trait magnitude still does not measure combat power. However, reconstruction makes the absolute rating meaningful for **acquisition probability**. Fire 5 / evocation 5 and fire 1 / evocation 1 both yield 50% fire fragments, while the former has five times the latter's weight in a fire reconstruction pool.

Consequently, independently simplifying a spell's profile can preserve its fragment proportions and calibrated effects while changing its reconstruction odds. This holds for both the one-trait rule and the slot-counted average for mixed traits. Review pool probabilities whenever changing trait units. Do not silently normalize profiles or add a separate acquisition coefficient to cancel the owner's specified weighting. See [spell balance](spell-balance.md#modifiers-and-trait-units).

## Chaos and identification

| Caster's knowledge | Spell volatility | Intended behavior |
|---|---|---|
| Identified | Nonvolatile | No chaos risk from ignorance or volatility |
| Identified | Volatile | Inherent chaos risk remains |
| Unknown | Nonvolatile | Casting can be chaotic |
| Unknown | Volatile | Very likely to be chaotic; ignorance and volatility must combine |

The exact probabilities, combination formula, and meaning of “very likely” are not set by this decision. Chaos continues to use the existing forfeit concept: the intended cast is replaced by a random chaotic outcome. It does not automatically identify the intended spell or a spell resembling the chaotic result.

**Current executable behavior differs:** [ForfeitPolicy](../../src/main/java/com/quzzar/vestige/magic/runtime/ForfeitPolicy.java) uses `min(1, max(unknown ? 0.20 : 0, volatile × 0.05))`. For example, volatile 4 has 20% risk whether known or unknown. Taking the maximum does not implement the newly requested extra danger for an unknown volatile spell. A future implementation must choose and test stronger combined risk; the old numbers are existing tuning, not newly approved discovery probabilities.

Native scroll casting reads persistent per-player identification through `SpellKnowledge`. Operator commands bypass discovery and do not identify spells for the player. The runtime's existing `discovered` boolean corresponds to identified versus unknown; this design clarification does not rename the Java API.

## Related equipment ideas retained for future design

The owner's October 6 [wand construction and magical string design](wand-crafting.md) now has implemented untipped/tipped binding, seven base and five thread profiles, eight selected tips and deterministic durability. The owner removed additional wand recovery on October 7; composed resources, preparation/channel time and wear govern repeated use. Durability replaces the briefly considered random break chance and is adjusted by components. Scrolls stack to sixteen by exact stored item components; casting and dismantling still consume one scroll per operation. The five thread components and shared imbuement-selected recipe are implemented. Eight locked additional tip effects, dynamically derived wand displays and native artwork are implemented; testing-pack installation remains separate. Wand casting never identifies spells.

These are the owner's earlier proposals, not completed items or finalized tuning:

- **Fire mage hat:** multiply the cast's fire rating by 1.5. Effects must actually read fire to respond; current Fireball's damage reads amplify, so its current damage formula would not respond to that hat without a separate recipe decision.
- **Reaching wand of a spell:** multiply its range rating by 2; right-click casts without mana or other resource costs, with a proposed one-minute item cooldown and perhaps twenty durability uses. Wand casts never identify a spell.
- **Health instead of mana:** equipment replaces mana payment with `ceil(mana / 5)` hearts. A Minecraft heart is two health points; 28 mana would therefore cost six hearts. Other resource costs, lethal payment, stacking and exact item tuning remain undecided.

Earlier exploration, magical clothing, mixed traditions and slow mana regeneration ideas remain direction rather than implemented progression. This scroll loop does not introduce XP tiers, permanent tradition ranks, a learned state or the inherited wand system.

## Implementation boundary and remaining decisions

Native spell definitions, resolved cast traits, resource payments, forfeit outcomes, scrolls, fragments, dismantling, discovery and ingredient rituals are implemented. Persistent identification and separate craft history control scroll names and optional recipe-viewer disclosure. Native wands and equipment progression remain deferred. The [material standard](material-crafting.md) distinguishes implemented apparatus imbuements from equipment proposals and inert upstream facts.

Implement the loop around the native runtime, with the following safeguards proposed for its implementation:

- Carry stable spell and trait identities in native items; store identified spell identities per player. Receiving, reconstructing or inspecting an item must not identify its spell.
- Validate and commit consumption and outputs on the server exactly once. Craft previews perform no gameplay RNG. Three independently rolled fragments can have different identities, so the crafting-table output needs to deliver the entire batch even when it cannot fit in one ordinary output stack; the presentation for that remains undecided.
- Use a deterministic ordering for weighted pools and gameplay randomness separate from presentation randomness. Resolve against the current successfully loaded catalog; a missing spell, empty trait profile or empty reconstruction pool must fail without consuming crafting inputs. Verify the basic/advanced slot count and read participating items and directly supporting blocks together at commitment.
- Define and test a scroll-cast success milestone. Summons, projectiles, sustained spells and sessions awaiting recasts cannot all be treated as instant terminal completion. A miss is not automatically a failed cast, and eventual cleanup of a successfully created manifestation should not revoke identification. The precise milestone is still open.
- Define scroll consumption for charge cancellation, failed payment, invalid targets, effect failure and forfeit. “Single use” is confirmed; exactly when an attempted cast commits that use is still open. Scroll mana/resource payment and cooldown policy are also not specified here.
- Choose the stronger unknown-plus-volatile chaos formula and numerical policy before claiming that risk behavior is implemented. Test that successful scroll casts identify once, while failed attempts and every non-scroll source do not.
- Define base-block effects, final geometry, center function and activation. Test the confirmed all-required-traits filter and slot-counted average weighting, including duplicates, fractional ratings and four/eight-slot equivalence for the same fragment proportions. Keep discovery separate from spell composition/augmentation rules, costs and output identity. Whether an augmented scroll retains its base spell's identification or constitutes a distinct spell identity remains open. Crafting itself must not identify the output spell.

Verification for this design records the probability examples and checks document links. No new gameplay or client appearance is claimed.
