# Plinth rituals and geometry

**Accepted apparatus direction, October 3, and v3 geometry baseline, October 4, 2026.** Vestige will use a Spellstone and one Plinth block type. Ingredients determine the base spell, embedded materials shape individual ingredient contributions, and the active structure's geometry provides another source of Spellshaping. This allows a compact wizard-tower installation and larger, functional ancient structures to use the same pieces.

The two-block apparatus and accepted v3 geometry are implemented natively. See [ritual crafting](ritual-crafting.md) and [playtesting](../leyline-playtesting.md) for selection, reservation, construction and casting. The shared palette has 36 stone/masonry finishes and the owner’s October 5 recipes. The current [apparatus models](apparatus-models.md) use a thick three-piece Spellstone with the selected smaller A Diamond chips wrapping four vertical cap corners. The owner’s hands-on review raises its physical/receiving top to y10.25/16, lowers the Astral Seal beneath resting items and replaces stepped collision highlighting with the straight authored stone edges. The Plinth has one foot, a square shaft and a flat y14/16 cap. The owner-approved narrow corner/rim edging now follows each finish at 217/255 brightness; Spellstone has matching cap/support trim, with no additional solids or collision changes. Empty segments keep their normal stone texture; installed material plates appear only when imbued. Embedded materials select the unchanged executable Spellshaping rules. Previous octagonal, broad low table, side-gem and carved-socket art below are historical proposals.
For what these inputs change when a spell is cast or a future magic item activates, see [Leyline shaping for spells and magic items](leyline-output-shaping.md). The October 4 baseline keeps schools in the flat trait repertoire; traits condition shared functions that emit Amplify, Range, Area and a uniform Casting Cost factor. Competing preferred layouts remain, while raw school/element rating adjustments are superseded. The v3 functions and coefficients are accepted; earlier alternative numerical examples below are historical evidence.

[The accepted calculator baseline](leyline-calculator.md) supplies flat multi-trait selection, independent six-by-four factor response matrices and a rotatable 3D comparison, with exact cells in [v3 rules](leyline-calculator-v3.json). Casting Cost has its own column alongside the shared output tradeoff. Its horizontal stage-gap convention, bounds and response curves supersede the alternative geometry mathematics below for implementation. [V2](leyline-calculator-v2.md) and [v1](leyline-calculator-v1.md) remain historical evidence. The native evaluator consumes the synchronized accepted rules; 490 comparison fixtures check numerical parity.

The owner clarified on October 4 that a matching structure should be **largely beneficial for that type, with minor balancing offsets**. The current calculator softens positive combined cost responses and negative outcome responses across all traits, retaining full positive outcome bonuses. School Cost cells were refitted to keep efficiency opportunities comparable. The owner also selected nearest-whole final applied amounts, rounded once after all modifiers; factors and intermediate expressions retain precision. The calculator applies this to its cost previews. Native amount consumers and health-cost heart conversion are implemented and tested; earlier fractional examples below are historical alternatives.

## Accepted apparatus direction

- **Spellstone** is the central control, reference-scroll surface and output surface. Its new appearance should be approximately a half-slab with etched runes and a simple construction recipe.
- **Plinth** replaces the Stone/Runic distinction. The same block can occupy either layer, hold an offering, store an independent imbuement material and act as a leyline node even when its offering surface is empty.
- Construction favors stone/masonry, with the initial lodestone inspiration retained in its carved appearance. The owner's October 7 grids use five matching slabs with Amethyst/Diamonds for Spellstone and one matching full block with six slabs for two Plinths. Supplementaries owns the Stone Bricks grid when installed, with its pedestals convertible one-for-one into native Plinths. The current grids and client art are in the playtest guide; former three-block recipes remain historical evidence.
- An installed material replaces the former block-under-the-pedestal augmentation context. It is retained in the Plinth, visibly embedded, and returned when removed or when the Plinth breaks. Ordinary decorative foundations are free to serve architecture.
- The same Spellstone supports four inner ingredient slots and up to four outer slots. These are **layers of the same apparatus**, rather than a material or power tier.
- Ingredients still occupy the correct relative recipe positions. Quarter-turns of the whole arrangement preserve the recipe and shape; relative placement remains meaningful. The earlier distinction between clockwise order and its reflection remains in the existing recipe design.
- Geometry has three factors: **shape**, **distance between successive stages**, and **height ratio between successive stages**. Each layer can have a cardinal cross or diagonal corner shape, giving two inner-only shapes and four inner/outer combinations.
- A four-slot recipe ignores the outer layer entirely, including its shape, height, distance, materials and offerings.

The [material standard](material-crafting.md) still applies: Iron's established identities and ingredient meanings inform explicitly authored contributions, without a material-quality multiplier. Core construction and shaping need standalone vanilla/native routes; foreign materials are optional alternatives. The pinned [complete Iron reference](../research/iron-material-uses.md) remains the source of those associations.

The reference scroll stays laid flat on the center. Completed output becomes a normal dropped item spawned exactly above the center with no sideways toss, and only offered ingredients rise during the ritual. The preceding revision's sideways failure shake remains compatible with this direction.

**Connected columns, October 5:** stacked Plinths form a foot, uninterrupted shaft and one top crown, across cosmetic finishes. Only exposed cap/standalone surfaces are ritual nodes. Supporting Plinth segments do not enter ingredient matching, shaping or attunement identity; only the active cap's position and stored offering/socket do. Covering a reserved surface cancels before consumption. The [column comparison](../art/apparatus-columns/README.md) records native appearance and the separate standalone simplification alternatives.

## Shape distance and height ratio

The owner demonstrated the layout choices in [the unchanged in-game reference screenshot](../art/plinth-layouts/owner-layouts.png). It illustrates spatial intent using existing game blocks; it is not evidence that the new Plinth mechanics have been implemented.

**Shape** has two choices for each active layer: **Cross**, with Plinths north/east/south/west of the center, and **Diagonal**, with Plinths at the four corners. The combinations are:

| Inner layer | Outer layer | Relative alignment |
| --- | --- | --- |
| Cross | Cross | Aligned |
| Cross | Diagonal | Staggered |
| Diagonal | Cross | Staggered |
| Diagonal | Diagonal | Aligned |

All four remain distinct shape configurations in the model, even though the pairs share an aligned or staggered beam topology. A four-slot operation has only the inner Cross/Diagonal choice. The prior proposal to treat every whole 45-degree rotation as equivalent is superseded: rotating a Cross into a Diagonal changes its shape. Quarter-turns preserve either shape and do not depend on which ingredient starts north.

**Distance** has two primary values: `d1`, the horizontal block-center spacing from Spellstone to inner layer, and `d2`, the horizontal block-center spacing from an outer node to its nearest inner node. Outer distance from the center and neighboring-node distances are derived measurements. An inner spacing of two and outer spacing of one is a different input from inner spacing of one and outer spacing of two; both remain available to the shaping rules. Diagonal positions use their physical horizontal lengths, and height is measured separately. This is the accepted v3 convention; full 3D beam length is not an additional distance bonus.

**Height ratio** describes the relative heights of Spellstone, inner layer and outer layer. Represent it with two signed steps rather than dividing absolute world Y coordinates:

```text
dy1 = inner height - Spellstone height
dy2 = outer height - inner height
```

Positive means the next outward layer is higher; negative means it is lower. Raising the whole build leaves both values unchanged. Center 0, inner 2 and outer 0 gives `(+2, -2)`; an entirely flat build gives `(0, 0)`. The two arrangements retain distinct height profiles. Uneven height within a layer remains outside the initial regular-layout scope.

Keep these geometry inputs separate from the ingredient recipe and each Plinth's material/ingredient interaction. They may combine during Spellshaping, but they do not become a generic structure-quality score. Their geometry responses are defined by the accepted v3 matrices; the [executable Spellshaping ledger](../spellshaping-recipes.md) defines material/ingredient interactions.

## Ingredient matching and embedded materials

The processing order is:

```text
ingredients in relative recipe slots -> base spell
embedded material + that slot's ingredient
    -> simple or compound Spellshaping contributions
active Plinth positions and links -> geometry contributions
validated contributions -> resolved traits, composed effects and typed costs
    -> crafted scroll
```

For example, Fireball still uses Paper, Blaze Rod, Gunpowder and Emerald in its authored relative order. Changing the Plinth holding Emerald can shape its delivery contribution; it does not replace the Emerald or silently select a different base spell. The [executable Spellshaping ledger](../spellshaping-recipes.md) defines supported material/ingredient pairings. Authored recipe roles describe clues; they do not change runtime pairing selection.

**Accepted interaction:** use the top face for offering placement/retrieval, and a side socket for installing/removing an imbuement. Sneaking with an empty hand on the socket returns the installed material. This resolves an ambiguity in “right-click a block”: a Block of Amethyst could be either a legitimate recipe offering or an installed material. The owner's October 5 restriction permits only block material IDs used by executable Spellshaping rules; arbitrary blocks reject without consumption. Breaking the Plinth returns its offering and imbuement independently; crafting consumes the offering, retaining the installed material unless a shaping rule explicitly defines wear.

Both apparatus roles support vanilla waterlogging. A Plinth offering surface requires air or unobstructed fluid above; solid covers block offering placement and ritual participation, while preserving and allowing recovery of stored contents. Every segment in a column remains independently imbueable through its side socket.

Store a supported material's identity separately from its rendering. An embedded Gold Block should provide an inlay or socket motif rather than a whole block hovering over the offering. Each interaction chooses a material contribution through its exact offering/material pairing; using Iron does not automatically grant all of Iron's reference properties.

## Measured distances

Use **block-center coordinates**, with one block as one distance unit. Two blocks from the Spellstone means one intervening block along a straight cardinal line. Equal placement layers have zero mechanical height difference, even if the two models have different surface heights; visual beams use the actual offering surfaces.

For a Plinth at `p = (x, y, z)` and Spellstone at `s`, measure horizontal radius and full distance separately:

```text
r(p) = sqrt((x - sx)^2 + (z - sz)^2)
L(p, s) = sqrt(r(p)^2 + (y - sy)^2)
L(a, b) = sqrt((ax - bx)^2 + (ay - by)^2 + (az - bz)^2)
```

Measure three relationships: **Plinth to Spellstone, neighboring Plinths within a layer, and connected Plinths across layers**. These support the two primary stage spacings and the actual beam geometry; the outer radius is not a third independent distance bonus. Preserve individual measurements so later asymmetric layouts need not be flattened into an unexplained score. The physical formulas below remain useful whichever distance-counting convention is selected.

For regular fourfold layers with horizontal radii `r1` and `r2`, equal height within each layer, and relative angle `delta` between an outer node and its nearest inner node:

```text
neighbor spacing in a layer = sqrt(2) * radius
horizontal cross-layer gap q = sqrt(r1^2 + r2^2 - 2*r1*r2*cos(delta))
cross-layer beam length = sqrt(q^2 + (y2 - y1)^2)
```

| Proposed example | Inner radius | Outer radius | Inner neighbor spacing | Outer neighbor spacing | Nearest cross-layer gap |
| --- | ---: | ---: | ---: | ---: | ---: |
| Flat, aligned: inner cardinal offsets 2, outer cardinal offsets 4 | 2 | 4 | 2.828 | 5.657 | 2 |
| Flat, staggered: inner cardinal offsets 2, outer corners at offsets 3 and 3 | 2 | 4.243 | 2.828 | 6 | 3.162 |

These are center-to-center distances. At 45 degrees an outer node has two equally near inner partners. In the first example the footprint is 9 by 9 blocks including the center and boundary blocks; in the second it is 7 by 7. Neither example requires a different Plinth item.

Rotating every position by a quarter-turn preserves distances and shape. A 45-degree turn changes Cross to Diagonal, so equal physical distances alone no longer imply equivalent shaping. On Minecraft's grid, `(2, 0)` and `(2, 2)` have radii 2 and 2.828: using the same coordinate offset diagonally also changes physical distance. Shape identity and the measured distances are both retained; a diagonal layout is not automatically awarded a longer-range bonus just for having more coordinate axes involved.

## Proposed placement and connection rules

Start with the two owner-selected fourfold groups in horizontal projection: cardinal vectors `(a, 0)` or diagonal vectors `(a, a)`, followed by their three quarter-turns. Permit different spacing and height for the two layers. This keeps recipe roles legible while supporting rings, pyramids, bowls and raised central altars. Other bearings and arbitrary asymmetry are not part of the current selected shapes.

**Historical limits:** the first candidate used inner horizontal radius 1–6 blocks, outer radius greater than the inner radius and at most 12 blocks, with each layer at most 4 blocks above or below the preceding stage. The accepted October 4 [v3 baseline](leyline-calculator.md#large-pyramid-observatory) uses inner/outer radius limits 8/16 and independent ±6 height steps, allowing 33×33 pyramid sites with 12 blocks of elevation. Native placement validation remains to be implemented. Validate actual positions, non-overlap and loaded links before activation; a displayed preview should identify the chosen Plinths and any ambiguous competing structure.

Connect the Spellstone to the inner nodes, neighboring inner nodes to each other, and each active outer node to its nearest inner partner. A halfway outer node connects to both equally near partners. Neighboring outer nodes may close the outer circuit when that circuit is active. Use a declared link set so adding decorative Plinths does not create uncontrolled extra bonuses.

Longer links can take longer for energy to converge. A candidate visual/crafting timing rule is `base ritual time + longest active propagation path / propagation speed`, with a chosen finite maximum and a preview of the wait. The base time, propagation speed and maximum remain open. This gives actual link distance a purpose without inventing another potency multiplier from the same radius.

For regular squares, neighbor spacing follows from radius; it is not an independent free bonus. It can govern link validity and propagation timing. If asymmetric layouts are later supported, spacing variation can become an explicitly designed shaping variable.

## Proposed geometry mathematics

Geometry should specialize a spell through the existing trait and cost system. A larger construction should not grant a blanket quality bonus, change rarity or inflate all descriptive traits. Proposed geometry outputs are trait operations or supported cost/effect transformations, just like material Spellshaping:

```text
resolved(t) = max(0, (base(t) + sum(ADD(t))) * product(MULTIPLY(t)))
```

All additive operations precede the product of multipliers. Order of collecting contributions does not change the result. A finished scroll stores the validated shaping choices needed to reproduce its cast; moving the crafting structure later does not retroactively rewrite that scroll.

### Distance between stages

Preserve `d1` and `d2` independently. After choosing their counting convention, an illustrative normalization is:

```text
D1 = log2(d1 / reference_inner_spacing)
D2 = log2(d2 / reference_outer_spacing)  [only with an active outer layer]
```

The two values can feed separate trait/cost operations. A formula may combine them only after their separate roles and weights are chosen; the previous average of absolute inner and outer radii is superseded. Distance inputs remain independent from shape identity and signed height steps, even though physical beam lengths are calculated from all three.

For the inner-only case, the earlier numerical example still illustrates a possible tradeoff if `d1` means physical horizontal radius. Let `range *= 2^(0.5*D1)` and `area *= 2^(-0.5*D1)`, with reference inner spacing 2. Moving four inner Plinths from radius 2 to radius 4 gives `D1 = 1`: range multiplies by 1.414, and area by 0.707. Native Fireball would travel approximately 45.25 instead of 32 blocks and explode with radius 2.12 instead of 3; its primary damage stays 8. This is a proposed example of farther, tighter delivery, **not proof of equal overall value**. Area coverage, recipient caps and encounter geometry require gameplay balance.

A spell that does not read area cannot use area reduction as its payment. Such a spell needs a compatible authored cost increase, meaningful constraint or a different geometry operation. Zero mana or zero charge time also cannot supply a nominal percentage tradeoff. Preview the actual changed parameters and costs, and leave incompatible shaping neutral or reject it with an explanation.

### Height in two stages

Measure both height steps independently:

```text
H1 = (Spellstone height - inner height) / 2
H2 = (inner height - outer height) / 2
```

These proposed normalized inputs equal `-dy1/2` and `-dy2/2` from the accepted relative-height representation. Never substitute the net Spellstone-to-outer height for the two stages. A center at 0, inner layer at 2 and outer layer at 0 has `H1 = -1`, `H2 = 1`; it differs from three flat layers even though its endpoints have equal height.

**Candidate first-stage behavior:** rising inward toward a raised Spellstone concentrates the outcome, exchanging area for amplification. The reverse favors spread. An illustrative pair is `amplify *= 2^(0.25*H1)` and `area *= 2^(-0.25*H1)`, subject to compatible real consumers and bounds.

**Candidate second-stage behavior:** the outer-to-inner height step exchanges mana demand for charge time: `mana *= 2^(-0.25*H2)` and `charge time *= 2^(0.25*H2)`. This intentionally keeps an up-then-down build distinct. It is one possible mapping, not a selected rule; resource floors, integer rounding and spells with zero base charge or mana need authored treatment.

There is no automatic duration-scaling trait. A duration-oriented geometry option needs an explicit shared consumer and cost tradeoff before it can apply to effects, bindings or manifestations.

### Shape and layer separation

The two inner-only shapes and four inner/outer combinations need explicit shaping mappings. Under the proposed links, aligned outer spokes favor a simple path through one inner component, while staggered links weave between two components. This topology alone does not collapse Cross/Cross with Diagonal/Diagonal or Cross/Diagonal with Diagonal/Cross. A candidate distinction is concentrated outcomes versus distribution, exchanging compatible amplification and area/recipient spread. Exact shape operations and weights remain open; adding recipients requires a supported targeting consumer rather than a cosmetic second beam.

Changing the outer spacing changes the second distance input even when the inner arrangement is unchanged. Actual link length may also affect convergence timing under the proposed connection rule. Keep outer radius and neighbor spacing as derived measurements; they do not each award a duplicate universal bonus for that same gap.

## Activation boundaries and balance

**Owner clarification, October 8:** shapeless recipes treat four/eight slots as available capacity, allowing their ingredients anywhere on either ring without filling the inner ring first. A complete eight-slot layout validates/reserves all eight offering seats, with unused seats empty. A shaped four-slot recipe still resolves its authored inner pattern; nodes outside that pattern do not shape its result, add risk or get consumed. These are recipe-shape semantics, not a universal inner-before-outer rule. For five-to-eight-slot recipes, only declared active nodes and used offerings contribute. An empty node can participate as a structural relay only when a connection rule explicitly calls for it; it supplies no imaginary ingredient or material augment. Whether a partial outer recipe requires all four physical outer nodes remains to be settled.

Fragment discovery retains the accepted intersection of all supplied traits and the average rating counting repeated fragment traits. Geometry does not silently alter that probability distribution. Whether discovery should create shaped scrolls is a separate decision.

Set bounds on the combined material and geometry operations, then inspect actual formula outputs and typed costs for eligible spells. The current scroll validation format needs review before supporting decreases, stronger degrees, cost transformations or composed augment effects. Keep reference-scroll retention, safe rejection without a reference and creature-only explosive failures; snapshot all active inputs and links so changes before commitment cancel safely.

## Implementation sequence

1. Select the new construction grids and restrained stone/rune art. Define saved-world migration for existing Stone/Runic blocks and their stored offerings, without deleting player materials.
2. Replace fixed-coordinate/type validation with a reusable layout resolver and explicit logical recipe seats. Audit the current alternating inner/outer recipe indexing and clues so geometric changes preserve each base recipe's relative relationships.
3. Add separately stored imbuements, their interactions and persistent/client state. Select a small material shaping set from the existing proposal catalog.
4. Implement a bounded geometry resolver, scroll representation and supported effect/cost consumers. Expose the selected network, changed outcomes and costs before consuming offerings.
5. Verify all two/four shape cases, quarter-turn/translation invariance, distinct distance and height stages, inactive outer layers, ambiguous networks, breaking/removing inputs, reload and multiplayer. Balance representative range, area, single-target, summon, utility and resource-free spells before extending shaping to the catalog.

No new models, recipes, apparatus migration or gameplay mechanics were installed with this design update. The numerical examples have been checked arithmetically; the proposed shaping has not been implemented or playtested.
