# Leyline structure response calculator

**Historical v1 proposal, superseded October 4, 2026.** The owner corrected the separate school/associated-trait selectors, raw-rating adjustments and mana/additive-charge tradeoff. The current [calculator](leyline-calculator.md) selects traits from one repertoire and emits Amplify, Range, Area and a uniform Casting Cost multiplier. This document preserves the earlier interpretation for reference.

**Interactive design calculator, October 3, 2026. Proposed tuning only.** The owner asked for a complete candidate system and a calculator extending the existing 3D layout. This version gives every classical school and descriptive trait a preferred shape, distance and height profile. Distinct preferences make mixed magic reward a compromise. It does not change Minecraft gameplay, recipes, trait classifications, imbuements or the installed jar.

**Reading update, October 4:** the owner clarified that the issue was locating the layout controls and crafting results. The calculator now groups them as **Set layout** and **Crafting impact**, side by side at desktop widths and stacked on narrow screens. Selected item ratings show before and after crafting; the hypothetical cast outcome/cost estimate is optional. The response functions and coefficients are unchanged.

The accepted apparatus direction remains [Plinth rituals](plinth-rituals.md). [Output shaping](leyline-output-shaping.md) records the earlier discussion and explicit trait-consumer requirements. The exact numerical specification is [the calculator rules](leyline-calculator-v1.json). These mappings are independently authored Vestige proposals, not rules from Iron or Pathfinder.

## Reading and adjusting the calculator

Use **Set layout** to choose four or eight active slots, the inner and outer shapes, and the offsets and height steps under **Positions and height steps**. Those are the actual structural edits. Dragging the diagram or using **Rotate view** changes only the camera.

Use **Crafting impact** to choose the item's school and an optional associated trait, then enter their base ratings. The table directly below updates automatically as the structure changes. These sections sit beside each other on desktop; Crafting impact follows Set layout on narrow screens. The displayed example also includes Amplify, Range and Area at baseline 1. These are a hypothetical profile, not an imported gear recipe.

Change the active slot count, inner/outer shapes, grid offsets and height steps. Both grid offsets locate their layer from the Spellstone; the outer gap used by the response functions is calculated from the closest inner node. The inner height step is relative to the Spellstone and the outer step is relative to the inner layer. Positive means higher and negative means lower.

Read the selected-item table. Each crafted rating equals its base rating multiplied by that trait's geometry factor. For example, Fire 4 with ×1.20 becomes Fire 4.80, a 20% increase. A factor of ×0.80 makes Fire 3.20, a 20% decrease. ×1.00 is unchanged.

The complete school/element/other-trait tables show how the current geometry would modify an output containing each trait. They do not add all those traits to the selected item. The contribution breakdown explains the selected school and associated trait by input factor. Camera movement changes only the viewing angle.

The optional cast estimate applies the proposed affinity consumer and payment heuristic. It does not establish the damage, lifetime, activation rules or costs of an arbitrary item. Those require explicitly compatible effect and item consumers.

## What selects and shapes the output

Ingredients and their relative positions select the base recipe. Geometry supplies global trait modifiers. Imbuements supply local ingredient/material interactions through the existing trait and cost model. This calculator isolates geometry; it does not simulate the saved material augment catalog.

The active inputs are inner shape, outer shape, center-to-inner distance, inner-to-outer distance, inner height relative to center, and outer height relative to inner. Four-slot crafting has only the three inner inputs. Every outer input, modifier contribution and cost contribution is absent for that operation. A whole quarter-turn or whole-build translation changes nothing.

For this proposal, distances use **horizontal block-center lengths**. The first gap is the inner radius. The second is the horizontal distance from an outer node to its nearest inner node. Height is evaluated separately, so raising a layer does not also produce a distance bonus. Full 3D beam length and adjacent-Plinth spacing remain visible measurements, not additional potency inputs.

On a Minecraft grid, diagonal offsets have radius offset × square root of two. The calculator keeps an outer radius strictly beyond the inner radius and retains the earlier candidate bounds of inner radius at most 6, outer radius at most 12 and each height step from −4 to +4. These are calculator limits, not installed placement rules.

## Neutral reference and response curves

The neutral inner-only layout is a Cross with radius 2 and height step 0. The neutral two-layer layout is Cross/Cross, inner radius 2, outer radius 4, both height steps 0. Every descriptive trait multiplier and direct scaling multiplier is exactly 1 at its appropriate reference.

A trait profile contains six preferred values: inner shape, outer shape, first gap, second gap, first height step and second height step. Preferred gaps are physical distances, not grid offsets. The profile describes an ideal response, not a promise that every preferred combination can be built exactly on the block grid.

Distance response is a smooth peak around a preferred gap. Doubling or halving that gap has the same distance from the peak. Height response is a smooth peak around a preferred signed step. Moving beyond a preferred height or radius eventually weakens the response; repeatedly enlarging a build does not grant unbounded benefit.

The exact functions are:

```text
distancePeak(d, preferred) = exp(-0.5 * (log2(d / preferred) / 0.8)^2)
heightPeak(y, preferred)   = exp(-0.5 * ((y - preferred) / 2)^2)
shapeMatch(actual, preferred) = +1 if equal, otherwise -1
```

Each contribution subtracts its value at the neutral reference. Shape, gap and height therefore retain separate meanings:

```text
inner shape contribution = 0.10 * (match(actual, preferred) - match(Cross, preferred))
first gap contribution   = 0.24 * (distancePeak(d1, preferred) - distancePeak(2, preferred))
first height contribution= 0.16 * (heightPeak(dy1, preferred) - heightPeak(0, preferred))

outer shape contribution = 0.06 * (match(actual, preferred) - match(Cross, preferred))
second gap contribution  = 0.14 * (distancePeak(d2, preferred) - distancePeak(2, preferred))
second height contribution=0.10 * (heightPeak(dy2, preferred) - heightPeak(0, preferred))

trait multiplier = clamp(2^(sum of active contributions), 0.75, 1.40)
```

Contributions are log-base-two exponents. The calculator's breakdown shows the corresponding multiplicative factors, whose product produces the unclamped result; the final row includes bounds. Inactive outer terms display a dash. These bounds belong to this geometry producer and do not change the core trait resolver or secretly cap unrelated imbuements.

## Proposed school preferences

Positive height means the next outward layer is higher. For example, −2/−2 puts the Spellstone above the inner layer, with the outer layer lower again.

| School | Inner / outer shape | Preferred gaps | Height steps | Motif |
| --- | --- | --- | --- | --- |
| Abjuration | Cross / Cross | 2 / 1 | +2 / 0 | Compact raised enclosure |
| Conjuration | Diagonal / Cross | 3 / 2 | 0 / +2 | Anchored inner seats and raised outer boundary |
| Divination | Cross / Diagonal | 5 / 3 | +2 / +2 | Outward reach and ascending observation |
| Enchantment | Diagonal / Diagonal | 2 / 1 | 0 / −2 | Close, inward-focused influence |
| Evocation | Cross / Cross | 3 / 2 | −2 / −2 | Central release descending through spokes |
| Illusion | Diagonal / Diagonal | 3 / 3 | +2 / −2 | Broad weaving around a raised middle |
| Necromancy | Diagonal / Cross | 2 / 1 | −2 / +2 | A lowered inner binding enclosed by an outer rise |
| Transmutation | Cross / Diagonal | 2 / 2 | 0 / +2 | Balanced inner work with a raised outer frame |

These motifs justify different numbers; they do not create new casting behavior, automatic allies/enemies, physical containment or extra effects. Divination's staggered geometry, for example, may force a larger achievable second gap than its ideal preference. Smooth curves permit near matches.

## Proposed element and substance preferences

| Trait | Inner / outer shape | Preferred gaps | Height steps |
| --- | --- | --- | --- |
| Acid | Diagonal / Diagonal | 2 / 2 | −2 / 0 |
| Air | Cross / Diagonal | 5 / 3 | +2 / +2 |
| Blood | Diagonal / Cross | 1 / 1 | −2 / +2 |
| Earth | Cross / Cross | 1 / 1 | 0 / 0 |
| Fire | Cross / Cross | 1 / 1 | −2 / −2 |
| Force | Cross / Cross | 3 / 2 | −2 / 0 |
| Ice | Cross / Cross | 2 / 1 | 0 / 0 |
| Light | Cross / Diagonal | 4 / 3 | +2 / 0 |
| Lightning | Cross / Cross | 4 / 2 | −2 / +2 |
| Metal | Cross / Cross | 1 / 2 | 0 / −2 |
| Plant | Diagonal / Diagonal | 3 / 2 | +2 / +2 |
| Poison | Diagonal / Diagonal | 2 / 1 | −2 / 0 |
| Shadow | Diagonal / Cross | 3 / 1 | −2 / 0 |
| Sonic | Cross / Diagonal | 4 / 3 | 0 / 0 |
| Stone | Cross / Cross | 1 / 1 | 0 / −2 |
| Water | Diagonal / Diagonal | 3 / 3 | +2 / +2 |
| Wood | Diagonal / Cross | 2 / 2 | +2 / 0 |

Fire prefers a first gap of 1 while Evocation prefers 3. Their combination offers different optima depending on relative trait ratings and whether the player values potency, reach, coverage or payment. Water and Illusion share diagonal shapes but favor different outer heights. School and element functions are independent, and individual motifs can overlap without being required to have globally unique coefficients.

The calculator also covers the 21 existing essences, realms and other descriptive traits. Their exact six-value profiles are saved in the rules file. Related concepts share motifs deliberately: Life follows growing Plant, Death follows Necromancy, Dream follows Illusion, and Nether follows Fire. Space/Ender favor extended connections; Memory and Time favor a regular flat layout. These traits remain ordinary authored inputs, not automatic engine behavior.

**Volatile is excluded and unchanged.** Geometry does not create, remove or secretly modify chaos risk. A separate owner decision is required before proposing that interaction.

## Direct scaling exchanges

The proposal also supplies small, consistent adjustments to Amplify, Range and Area. Let D1 and D2 be log2(gap / 2), bounded from −1 to +2, and let diagonal shape flags be 1 for Diagonal and 0 for Cross.

```text
Range   = clamp(2^(0.16*D1 - 0.06*innerDiagonal), 0.80, 1.30)
Amplify = clamp(2^(-0.0175*dy1 - activeOuter*0.00875*dy2), 0.90, 1.10)

Area, inner-only = clamp(2^(-0.10*D1 + 0.05*innerDiagonal), 0.80, 1.35)
Area, both layers= clamp(2^(-0.10*D1 + 0.14*D2 + 0.08*outerDiagonal), 0.80, 1.35)
```

Greater first distance favors reach and tightens area. An active outer gap can expand area. A center raised above its outward stages slightly favors Amplify. The different layer-specific terms make active outer inputs explicit and prevent outer geometry from influencing four-slot operations.

## Example output and real costs

The calculator lets the player select one school and an optional associated trait, with their relative base ratings. Its example is hypothetical, not a selected spell or an actual installed cast. This keeps all four shape combinations available without pretending that a four-slot Fireball recipe has become an eight-slot recipe.

For the proposed primary outcome consumer:

```text
affinity = (schoolRating * schoolMultiplier + traitRating * traitMultiplier)
           / (schoolRating + traitRating)
proposed primary potency multiplier = Amplify * affinity
```

With no associated trait, the consumer reads only the school modifier. Scaling both ratings by the same factor leaves the result unchanged. A future consumer with several schools/elements can use explicitly authored weights rather than applying a complete bonus once for every tag.

The preview models primary potency, range and area as relative parameters with baseline 100%. It does not promise those parameters exist on every school or item. Current Fireball damage still reads only Amplify; the proposed affinity consumer requires deliberate effect authoring. A duration, summon, information or protection consumer needs behavior-specific constraints. Area radius is not target count or total damage.

The first cost proposal charges for positive changes without accepting irrelevant penalties as payment:

```text
benefit = max(0, log2(primaryPotency))
        + 0.65 * max(0, log2(Range))
        + 0.90 * max(0, log2(Area))
mana multiplier = 2^(0.90 * benefit)
additional charge ticks = ceil(20 * benefit)
```

The model has editable baseline mana and charge time. Any meaningful positive benefit adds charging time even when base mana is zero. Cost is not discounted for an unused trait reduction. This is a candidate pricing heuristic, not a separate spell power statistic and not evidence of equal gameplay value. Radius changes can have nonlinear coverage effects, so cost weights require encounter tests.

This initial example models an active spell with all three displayed consumers. A self-only utility spell must not be taxed for a range change it cannot use, and a spell without area must omit that benefit term. Passive equipment cannot be balanced through imaginary casting time; it needs its own explicit payment, trigger, duration, durability or uptime rule. Material and geometry stacking likewise need a combined preview and outcome review before gameplay adoption.

## Calculator scope and verification

The calculator preserves the rotatable 3D geometry, physical measurements, four shape combinations, inner-only operation and remembered interaction state. It adds live multipliers for all eight schools, 17 elements/substances and 21 other descriptive traits, three direct scaling traits, a selected-profile output and payment preview, and a contribution breakdown.

The example starts with Evocation 4 and Fire 4. The saved geometry remains Cross/Diagonal with inner offset 4, outer offset 3 and height steps −2/+4. Other configurations can be selected interactively. Candidate preferences, neutral reference and outer omission are independently checkable from the rules.

No Minecraft cast, actual model asset, new recipe, gear crafting or material imbuement is simulated. Outcome compatibility and gameplay balance remain implementation work. Validation results are recorded in [development status](../development-status.md).
