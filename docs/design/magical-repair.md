# Fluxed Flint and magical repair

**Owner resumed work, October 8, 2026:** the Dissentient Diamond is defined and its artwork approved. The owner requested continuing Fluxed Flint, lifting the earlier recipe/art hold. The implemented construction now uses **Flint → Netherite Ingot → Dissentient Diamond → Netherite Ingot** in the inner layer, with whole rotations equivalent. The two ingots occupy separate, opposite Plinths. This replaces the provisional Diamond Block/Echo Shard composition. Repair remains unordered on any two available seats and keeps the full selected table empty otherwise.

**Owner direction, October 8; first implementation for playtesting.** Fluxed Flint is the selected name for the expensive, volatile repair catalyst. The owner accepted finite catalyst durability, exact target preservation except improved damage, and the shared input-volatility failure rule. The 128-point budget, 25% operation cap and Volatile 2 remain initial playtest tuning. The original violet artwork is superseded by the [diamond-blue revision](../art/fluxed-flint-v2/README.md); its exact silhouette and five stone shades are preserved. The owner subsequently requested closeout and authorized shipping the completed package with its final native shimmer; [the authorization record](../art/fluxed-flint-v2/approval.json) pins the current artwork separately from verification and delivery.

The owner's correction also applies to material attribution: Echo Shard makes Timeless Slurry, which combines with Pyrium in specific Iron restoration recipes, but those recipes do not establish that either ingredient universally supplies restoration. Likewise, a healing ingredient paired with Amethyst does not establish Amethyst as the source of healing. [Pinned material facts](../research/iron-material-uses.md) remain distinct from new Vestige ingredient conventions. Diamond's pinned Jewelry parameters include Armor and a magic-damage action; the explicit Spell Power attribute belongs to Mithril. Gunpowder's proposed broader identity does not automatically make every existing Gunpowder offering volatile.

## Dissentient Diamond: selected name and ingredients

**Owner selection, October 8:** the reusable intermediate is named **Dissentient Diamond** and uses **one Wither Skeleton Skull, two Gunpowder and one Diamond**. The two Gunpowder units are intentional. This replaces the earlier Ender Pearl suggestion; Soul Sand, Echo Shard, Crying Obsidian and Nether Star were discussed as alternatives, not selected ingredients.

The owner's concept is a highly ordered diamond crystal whose fractured structure has entered a chaotic, heretical state: a crystal in dissent against its own natural order. This is an original Vestige material identity, not an upstream claim that Diamond or Wither Skeleton Skull inherently grants a universal chaos property. The ordinary ingredient is registered as `vestige:dissentient_diamond`. Its selected ingredients now craft **one** intermediate through an ordered four-Plinth ritual: **Diamond → Gunpowder → Wither Skeleton Skull → Gunpowder**, with whole rotations equivalent and an empty Spellstone. The two Gunpowder units occupy separate Plinths opposite each other. Presentation/order and one-item yield are implementation choices consistent with the existing component rituals. It has no intrinsic volatility rating; making a chaotic material does not invent a new trait or random loss during its construction. Additional uses and any future intrinsic trait rating remain separate decisions.

**Owner artwork direction, October 8:** start with the texture, then implement the ingredient. It is a crafting ingredient like the thread components; it does not need enchantment shimmer or an overt magical visual effect. Work from a **32×32 cyan diamond with dark fissures**, then reduce to **16×16 for Minecraft inventory comparison**. The owner rejected v7's intact diamond with small cracks, clarifying that v6's fractured shape was good and only its noise should be reduced. V8 preserved that split structure with quieter faces. V9 replaced the black bottom edge with vanilla Diamond's dark teal. V10 changed the thirteen near-black fissure pixels to dark petrol/navy blue. The owner liked v10 and requested horizontal inventory centering. [V11 artwork and review](../art/dissentient-diamond-v11/README.md) preserves the exact v10 texture and adds a half-texel GUI X translation to center its odd-width footprint. The owner approved this exact artwork and alignment on October 8; [acceptance](../art/dissentient-diamond-v11/approval.json) pins the texture/model hashes. The registered ingredient uses native **Uncommon** rarity and a **yellow** name, matching Minecraft 1.21.1's Wither Skeleton Skull. It is an ordinary stackable ingredient with no glint, durability or direct-use/casting behavior; an intrinsic trait rating remains unspecified. Earlier native art previews used a Diamond carrier. The approved Diamond texture/model remain unchanged by the later Flint continuation.

## First playable recipe and repair

**Item-name rarity, owner approved October 8:** Fluxed Flint uses native Rare/aqua. This changes its name presentation only; finite repair budget and Volatile 2 are unchanged. See the [shared item-name policy](item-imbuement-and-readiness.md#accepted-item-name-rarities).

**Owner presentation follow-up, October 8:** Fluxed Flint has the native enchantment shimmer through its default `ENCHANTMENT_GLINT_OVERRIDE` component, including ordinary crafted stacks. This is a visual effect; it adds no enchantment or new enchantment support. The diamond-blue texture/model, name rarity, repair budget and volatility are unchanged. Dissentient Diamond remains an ordinary ingredient without shimmer.

Craft one Fluxed Flint by offering **one Flint, one Netherite Ingot, one Dissentient Diamond and one Netherite Ingot** in that clockwise order on the four inner Plinths around an empty Spellstone. Whole rotations match; the two identical ingots give the pattern a natural reflection symmetry. Outer nodes are inactive and socket materials are retained. Both optional recipe viewers expose this public construction recipe and the intermediate's recipe. Dissentient Diamond supplies the reusable fractured-crystal ingredient identity; no Iron dependency or generic ingredient-quality system is introduced.

For repair, offer **one Fluxed Flint and one damaged magical item** on any two different Plinths in a valid four- or eight-slot layout. They may be on either ring, adjacent or far apart. Leave every other available offering surface and the Spellstone empty; activate normally. The Flint returns to its original Plinth with wear. The repaired item drops above the Spellstone through the ordinary atomic ritual lifecycle. No additional repair bar, menu, chat or actionbar message is added. Flint uses vanilla's item durability bar.

| Initial tuning | Value |
|---|---|
| Flint durability / total repair budget | 128 points |
| Maximum repair per activation | Ceiling of 25% of the target's maximum durability |
| Actual restoration | Minimum of the per-activation cap, existing damage and Flint's remaining budget |
| Catalyst wear on success | Exactly one point per restored target durability |
| Intrinsic trait | `vestige:volatile` 2 |
| Inherent backfire chance per activation | 10%, through the shared five-percent-per-point policy |
| Catalyst wear on backfire or cancellation | None |
| Last remaining budget | Repairs that many points, then removes exhausted Flint |

The extensible `vestige:magical_repairable` item tag currently contains native Wands, Staffs, Wardweave Robes, Cinderweave Robes and Homebound Eyes. Items must be single, damaged and damageable, with positive remaining durability. Full, exhausted/broken, unbreakable, vanilla-only and catalyst-to-catalyst inputs reject freely. Flint is explicitly excluded even if added to the target tag. Its two-item crafting/grindstone/anvil combining and ordinary enchantment routes are disabled, avoiding renewable catalyst budget. Native Wands and Staffs also reject two-item crafting/grindstone/anvil combining, which can discard one or both stored bindings. Renaming and otherwise valid book-enchantment use remain available; single-item grindstone disenchanting retains its normal behavior. Existing robe anvil repairs remain available; this adds a ritual route rather than removing an established robe mechanic. Wands and Staffs still break on their last paid use; this change does not retain broken items.

**Owner clarification, October 8:** slot count is capacity. Shapeless recipes use any seats in the available valid four- or eight-slot table; the inner ring does not need offerings before the outer ring can be used. A complete eight-slot layout validates and reserves all eight seats, including empty ones, rather than silently falling back to its inner four. Placement does not change repair amount or risk.

## Exact preservation and atomic commitment

`FluxedFlintRecipe.repair` copies the actual offered target stack and changes only its damage component. It never reconstructs from the item registry or enumerates a whitelist of metadata. Names, enchantments, inventory/container contents, persistent ownership/identities, source scrolls, shaping, bindings, Staff affinity/capacity/selection and unrelated or future data components survive exactly. The Flint remainder similarly copies its actual input and changes only damage, unless exhausted.

Both inputs, every active empty seat, the reference and socket materials are captured before activation. The native pending ritual compiles per-seat remainders and reserves the arrangement. Before commitment, and again after output-spawn callbacks, it revalidates the complete snapshots. A rejected spawn, callback edit, changed input, unload, reload or shutdown cancels without catalyst wear or target consumption. Successful output creation, target consumption and Flint replacement are one server-thread commit. Ordinary recipes retain their container remainders through the same machinery.

## Shared input volatility

All activated, matched ritual operations use `RitualVolatility`, including construction, expansion, binding, discovery, spell crafting and repair. An incomplete inspection or unmatched unreferenced arrangement does not roll. Completed incorrect reference arrangements retain their existing ignorance floor through `ForfeitPolicy`; correctly matched operations use intrinsic volatility without adding an ignorance penalty.

Each participating input has its own roll: center first when present, then ascending active Plinth seats. Zero-risk inputs draw nothing. Stop at the first trigger. Ratings are not summed. The probability of any trigger is `1 - product(1 - p_i)`; for two Flint inputs, this would be 19%, rather than 20%. The canonical order determines the survivor when more than one input could have triggered. This is an explicit starting roll policy for the owner's proposed separate-roll model.

The triggering input survives **unchanged**. Every other participating input is destroyed, including a nontriggering reference when present. No output or success remainder is created. Terrain, apparatus, socket materials, offerings outside the selected valid layout and existing loose items remain protected by the native distributed failure bursts. A Flint-triggered repair therefore loses the target but preserves Flint without wear. A volatile target can instead trigger, preserve itself and destroy the Flint; this follows the same general rule.

Intrinsic item traits come from `RitualTraitSource`. Flint resolves its immutable base profile with its validated crafted imbuements through `TraitProfile.resolve`; the shared failure policy consumes that result. A native Staff's one fixed affinity supplies one trait point; contained scrolls do not become intrinsic Staff traits. A scroll offered as an input uses its actual native spell profile and stored trait modifiers. Wands do not automatically inherit the traits of their stored source spell. Future magical items can expose their own intrinsic profiles through the same interface rather than introducing another probability system.

## Crafted imbuements

**Owner authorized October 8, 2026:** implement the previously discussed Fluxed Flint imbuements. The following two choices and their numerical trade-offs are starting implementation tuning. They use the unchanged ordered construction recipe and existing retained socket interactions; the shipped blue sprite, Rare name color and native shimmer stay the same.

| Offering | Installed socket block | Choice | Authored factors |
| --- | --- | --- | --- |
| Dissentient Diamond | Quartz Block | Stabilized | Volatile ×0.5; maximum repair budget ×0.75 |
| Either Netherite Ingot | Iron Block | Reinforced | Maximum repair budget ×1.5; Volatile ×1.5 |
| Flint | Magma Block | Fractious | Per-activation repair cap ×2; Volatile ×2; total budget unchanged |

**Owner authorized October 9, 2026:** add a stronger, riskier repair choice. Fractious uses retained Magma beneath the Flint offering during the same construction. The 50% cap and doubled intrinsic volatility are initial implementation tuning. It raises the amount transferred in one activation without adding total repair points. A Flint-triggered backfire still preserves that exact Flint without wear and destroys the target.

| Full item name | Total repair budget | Maximum repair per activation | Volatile | Flint's own backfire chance |
| --- | ---: | ---: | ---: | ---: |
| Fluxed Flint | 128 | 25% | 2 | 10% |
| *Stabilized* Fluxed Flint | 96 | 25% | 1 | 5% |
| *Reinforced* Fluxed Flint | 192 | 25% | 3 | 15% |
| *Braced* Fluxed Flint | 144 | 25% | 1.5 | 7.5% |
| *Fractious* Fluxed Flint | 128 | 50% | 4 | 20% |
| *Restive* Fluxed Flint | 96 | 50% | 2 | 10% |
| *Audacious* Fluxed Flint | 192 | 50% | 6 | 30% |
| *Impetuous* Fluxed Flint | 144 | 50% | 3 | 15% |

The cap uses the target's maximum durability and rounds upward; actual restoration is limited by missing durability and remaining Flint points. Every successful restored point spends exactly one Flint point. An 80-durability target can receive at most 20 points normally or 40 with Fractious in any combination. If it is missing only three points, either spends three. If the Flint has seven points left, either restores seven and breaks.

All three choices compose once. Braced combines Stabilized/Reinforced, Restive combines Stabilized/Fractious, Audacious combines Reinforced/Fractious, and Impetuous combines all three. Each is the shared catalog's exact full-set alias, preserving its complete contributions; only the prefix is italic. Stabilization still risks losing a nontriggering target, and any volatile target retains its separate independent roll. These percentages describe the Flint input, not a promise about the total ritual risk.

Quartz's stabilizing role is an explicitly authored Vestige crystal-structure convention for this one pairing, not an Iron fact or a universal material property. Iron's reinforcement role follows the established authored carrier-durability convention without adopting upstream quality values. Magma is a Vestige-local selector for this stronger, riskier transfer; this pairing does not assign generic volatility to the block or claim an upstream material effect. All three blocks already belong to the accepted socket set. Whole rotations keep each offering/socket pair together. Either of the identical opposite ingot offerings can select Reinforced, but installing Iron on both rejects a duplicate degree-one contribution freely. Known spell-only offering/socket routes, such as Flint / Gold Block's Excavating, reject rather than consuming an ineffective imbuement. Unmatched decorative pairings remain neutral. Outer sockets and offerings stay inactive during four-slot construction.

Imbuements are selected while making a fresh catalyst; an existing Flint is never re-crafted or refilled. Plain existing stacks remain valid without rewriting their components. `ItemImbuements` stores trusted versioned IDs at degree one; coefficients and maximum budgets come from native code. Unknown IDs, duplicate selections, wrong family/degrees, mismatched maximum durability, unbreakable or exhausted stacks cannot repair. Invalid data conservatively retains the ordinary intrinsic volatility when offered in another ritual, rather than supplying a risk-free forged input. Successful repair copies the exact offered Flint and spends only restored points, retaining its selections, custom components and existing wear; save/load preserves the variant. The cap is 25% normally and 50% with Fractious, including its combinations. Installed materials during repair do not modify a previously crafted catalyst.

All eight public construction variants are derived from the same live palette in both optional recipe viewers. Their diagrams show the retained socket materials on the correct offering. Dynamic per-target repair outputs remain separate work.

## Remaining design choices

The expensive composition, base and imbued repair budgets, 25%/50% operation caps and volatility trade-offs need survival playtesting. A public dynamic repair viewer presentation is also separate work: its exact output depends on the offered target's complete data, so it is not represented by a misleading fixed output recipe. Craft-time stabilization, reinforcement and Fractious transfer are implemented; no post-crafting upgrade/refill operation is introduced.

Historical per-equipment Amethyst/Iron/thread repair proposals are superseded by this shared catalyst route. [Restoration folklore research](../research/magical-restoration-folklore.md) supplied thematic context; Fluxed Flint is an original name and mechanic, not a claim that folklore describes this exact object.
