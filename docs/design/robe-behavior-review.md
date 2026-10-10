# Wardweave and Cinderweave behavior review

**October 9 addition:** [the approved Wardweave imbuement package](wardweave-imbuement-review.md) adds four independent choices, sixteen complete sets and reinforced 120-durability carriers. This document retains the unmodified robe baseline; the linked package supplies its crafted changes and their explicit trade-offs.

October 7, 2026. **Owner-approved Wardweave and Cinderweave gameplay, now implemented; worn artwork approved October 8.** The owner locked in the reviewed packages after approving uncapped trait growth, finite armor durability, absorbed-hit wear and exclusion of Unbreaking/Mending. The two robes use 2 armor, zero toughness, +25 maximum mana, 80 initial durability and matching-thread anvil repair of 20 durability per thread. Other clothing packages remain in review. [The accepted mage armor](../art/magic-equipment-armor-v2/approval.json) supersedes the earlier worn-art comparisons.

The [equipment review](../art/magic-equipment-review/index.html#wardweave) and its [native-readable formulas](../art/magic-equipment-review/ability-proposals.json) supply the current values. [Shared item magic](item-abilities.md) defines resolution and snapshot ownership. All relevant base trait ratings are one; a 25% multiplier makes the corresponding rating 1.25.

## Shared robe recommendation

**Item-name rarity, owner approved October 8:** both robes have native Uncommon/yellow base names. Compatible enchantments retain vanilla's displayed rarity promotion; the abilities and wear rules are unchanged. See the [shared item-name policy](item-imbuement-and-readiness.md#accepted-item-name-rarities).

Both occupy the chest slot, give **2 armor points, zero toughness and +25 maximum mana**. A wearer with no other capacity bonus has a maximum of 125. Two armor points equal one ordinary shield icon, compared with leather's three chest armor points.

The mana bonus is fixed. Equipping does not refill mana; removal clamps the balance to the remaining capacity. Recovery remains two mana per second after the existing expenditure delay. These robes do not themselves grant a global trait boost: their own abilities read the wearer's approved trait modifiers. A wand's source-local modifiers affect that wand's cast, not a robe ability.

Neither defensive effect charges additional mana. Every armor piece has durability. Normal armor damage causes wear; damage prevented by the robe's own magic also wears that robe, including complete prevention and protected damage types that ordinarily bypass armor. Count each eligible hit once using its damage before the robe's reduction; do not add a second charge for forming a ward, passive uptime or mana capacity. An already canceled or invulnerable hit with no robe contribution does not gain wear merely because the robe is equipped. Both begin at 80 durability; one matching thread repairs up to 20 at an anvil.

**Accepted enchantment policy:** ordinary compatible armor enchantments may be used, but **Unbreaking and Mending are excluded** from this magical clothing. Wear follows the authored rules without an enchantment chance to skip it, and XP does not replace magical-material repairs. Enchantment-table, book/anvil, loot and crafting paths must all preserve this exclusion when the equipment is implemented. This decision supersedes the earlier proposal to permit Unbreaking; it does not change existing wand/staff gameplay in this review pass.

## Wardweave: protection against a follow-up hit

**Accepted:** automatically create one protective charge after a hit actually deals health damage. The triggering hit receives only the protections already present; it cannot spend the ward it creates. Normal attacks, projectiles, fire and fall damage can trigger it. Fully blocked, canceled or zero-health-damage events do not arm it. Damage that explicitly bypasses magical effects, including void and operator kill damage, cannot arm or spend it.

The charge lasts **4 seconds** and reduces the next eligible incoming hit by **up to 2 HP (one heart)**, before ordinary armor, enchantments, resistance and absorption. A smaller hit spends the charge too; unused protection is lost. The charge never deals damage or heals.

**Recovery:** 12 seconds from successful activation, owned by the wearer and Wardweave ability. Consuming or expiring the charge does not restart recovery. A hit that spends a Wardweave charge cannot also create another one. Hits while a charge remains do not refresh it. Removing, breaking or changing the worn robe clears its charge while preserving the wearer's remaining recovery; another copy cannot stack or reset it. Death, disconnect or reload clears the transient charge.

Traits: **Abjuration, Force, Time and Amplify**. Explicit variable relationships:

| Value | Formula | Growth rule |
| --- | --- | --- |
| Ward duration | 4 seconds × Time | No tuned power ceiling |
| Next-hit protection | 2 HP × Force × Amplify | No tuned power ceiling; cannot prevent more than the hit |
| Maximum mana bonus | 25 | Fixed |

Resolve and store duration and protection when the ward forms. A boost expiring halfway through does not shorten or weaken that charge; the next activation uses the then-current traits. Abjuration describes the technique and can select the ability for an Abjuration-filtered modifier; an increase to the Abjuration rating alone has no numerical consumer in this proposal. Time does not shorten the fixed recovery.

| Boosts when the ward forms | Duration | Next-hit protection |
| --- | --- | --- |
| None | 4 seconds | 2 HP |
| +25% Time | 5 seconds | 2 HP |
| +25% Force | 4 seconds | 2.5 HP |
| +25% Force and +25% Amplify | 4 seconds | 3.125 HP |
| +200% Time, +100% Force and +100% Amplify | 12 seconds | 8 HP |
| +300% Time, +200% Force and +200% Amplify | 16 seconds | 18 HP |

For example, the first 8 HP hit arms the baseline ward; another 8 HP hit two seconds later enters the ordinary armor pipeline at 6 HP. The next hit has no Wardweave protection. These are pre-armor amounts, not a promise of the same final health loss with every other protection.

Keep other native shields' own budgets and identities. The robe contributes once at `armor_damage_calculating`, after shields and hurt immunity accept the hit and before armor, resistance, enchantments and absorption; it must not replace an existing spell ward or also subtract at the final-damage stage. A zero-remaining hit does not spend its charge. Existing spell wards run in their own earlier calculating phase; this preserves their budgets and avoids consuming a robe charge after they fully prevent damage.

Accepted recipe: **4 matching-color Wool + 2 Callous Thread + Iron Ingot + Pufferfish**, using all eight Plinth offerings around an empty Spellstone. Accepted worn art: **mage armor v2, slate/silver with a blue ward clasp**. The inventory sprite retains the Folded Mantle direction. One brief iron-toned rune cue marks activation and absorption; avoid idle particle clouds or chat feedback.

## Cinderweave: ongoing partial fire protection

**Accepted:** while worn, prevent **25% of incoming fire-tagged damage, capped at 2 HP (one heart) per damage event**. The amount prevented is the smaller of the percentage result and the cap. Use Minecraft's fire damage tag: burning, lava and other tagged sources qualify; the physical impact of a flaming projectile is not automatically fire-tagged.

Apply the robe contribution once at the accepted pre-armor stage, before ordinary armor, enchantments, resistance and absorption. Fire Resistance immunity prevents a fire event before this calculation in the pinned Minecraft version; such an already immune hit does not spend robe wear. Canceled damage and damage that bypasses magical effects receive no robe contribution. There is no activation control, recovery timer, additional mana payment or ignition rider. If the robe itself fully prevents a hit, it still pays its one wear charge.

Traits: **Fire, Abjuration and Amplify**. Explicit variable relationships:

| Value | Formula | Growth rule |
| --- | --- | --- |
| Fire mitigation fraction | 25% × Fire | Actual prevention stops at 100% of the hit |
| Protection budget per fire event | 2 HP × Amplify | No tuned power ceiling |
| Maximum mana bonus | 25 | Fixed |

Resolve current traits once for each eligible fire event. A passive robe has no lasting activation snapshot: an expired boost stops helping on subsequent events. Abjuration has the same descriptive/filter role as on Wardweave. The former 75% and 6 HP ceilings are removed. Large boosts can completely prevent an eligible fire hit when both the fraction and HP budget suffice. Actual prevented damage cannot exceed incoming damage or turn excess protection into healing.

| Boosts | Fraction / cap | Prevented from a 4 HP event | Prevented from an 8 HP event |
| --- | --- | --- | --- |
| None | 25% / 2 HP | 1 HP | 2 HP |
| +25% Fire | 31.25% / 2 HP | 1.25 HP | 2 HP |
| +25% Amplify | 25% / 2.5 HP | 1 HP | 2 HP |
| +25% Fire and +25% Amplify | 31.25% / 2.5 HP | 1.25 HP | 2.5 HP |
| +200% Fire and +200% Amplify | 75% / 6 HP | 3 HP | 6 HP |
| +300% Fire and +400% Amplify | 100% / 10 HP | 4 HP | 8 HP |

The cap interaction is deliberate: Fire helps smaller events, while Amplify lifts the ceiling on larger ones. Increasing only the ceiling does not change an event that already falls below it. Increasing only the percentage does not exceed the unchanged ceiling.

Accepted recipe: **4 matching-color Wool + 2 Smoldering Thread + Blaze Powder + Magma Cream**, using all eight Plinth offerings around an empty Spellstone. Accepted worn art: **mage armor v2, coal shoulders, brass fastenings and ember trim**. The existing inventory sprite is unchanged. A brief warm seam flash accompanies prevented fire damage; there are no idle flames or chat messages.

## Decisions and implementation boundary

The owner approved both reviewed gameplay packages. Their gameplay is implemented; starting values remain playtest tuning. Native inventory sprites are 16×16. On October 8 the owner rejected the cape, explicitly withdrew the earlier hanging-back instruction and requested a complete magic-armor treatment inspired by Electroblob's and Iron's armor textures. The accepted replacement has a designed collar/yoke, layered chest, clasp, belt, folded cuffs and a divided lower robe following native leg poses. Wardweave uses slate/silver with a blue clasp; Cinderweave uses coal, brass and ember seams. There is no cape or separate back panel. A 64×64 atlas preserves native texel density, dedicated narrow-sleeve UVs and dyeable cloth/fixed-trim separation. [The armor review](../art/magic-equipment-armor-v2/README.md) records new independently authored source studies and native evidence; [the rejected cape](../art/magic-equipment-mantles/README.md) and [earlier skirt](../art/magic-equipment-hanging/README.md) remain historical. The owner approved this exact visual revision on October 8 after viewing the native captures; the linked approval record pins its textures and model sources. Gameplay approval is unchanged. No other clothing package is promoted by this approval.

The implementation has targeted model and world checks for triggering, health outcomes, trait snapshots and expiry, large boosts/full prevention, one wear charge, removal/recovery ownership, capacity changes without refills, matching-wool crafting and anvil enchant/repair paths. Native image captures verify dye separation and item/worn rendering. Finite-value validation and engine work limits remain technical requirements; they do not reintroduce tuned 12-second, 6 HP or 75% power ceilings. The common binding engine retains its technical 240,000-tick lifetime bound.

The current mana display is the owner-approved per-item affordability shade, not a pooled gauge. Capacity must feed the existing balance/payment synchronization; it does not change spell prices or justify adding another HUD meter.

Implementation: `equipment/` owns native armor, matching-color eight-offering crafting and the trusted worn-source adapter. `item_abilities/wardweave.json` supplies the reactive ward and its shared binding/recovery; `cinderweave.json` supplies passive formulas. Minecraft normal armor wear remains unchanged on unprotected hits. Protected hits charge that robe once from the pre-protection incoming amount, including armor-bypassing damage and full prevention. Native dyeing and anvil repair preserve components. The repetitive recipe pattern makes a mirrored layout rotation-equivalent; no separate handedness is claimed.
