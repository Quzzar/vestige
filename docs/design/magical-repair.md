# Fluxed Flint and magical repair

**Recipe order correction, October 8:** construction now uses **Flint → Diamond Block → Netherite Ingot → Echo Shard** in the inner layer, with whole rotations equivalent. This order-only correction leaves the ingredient/art redesign hold below in force. Repair remains unordered on any two available seats and keeps the full selected table empty otherwise. Earlier shapeless-construction descriptions below are superseded by this correction.

**Owner direction, October 8, 2026; first implementation for playtesting.** Fluxed Flint is the selected name for the expensive, volatile repair catalyst. The owner accepted finite catalyst durability, exact target preservation except improved damage, and the shared input-volatility failure rule. Numerical balance and the recipe below are initial implementation choices, not owner-approved final tuning.

**Owner design hold, October 8:** pause further Fluxed Flint recipe and artwork changes while defining the reusable **Dissentient Diamond** intermediate below. The owner favors Gunpowder as a volatility/chaos ingredient and proposed a later Flint recipe using two Netherite Ingots, one Flint and that intermediate, with diamond-blue accents replacing the current violet. The revised Flint composition and recolor remain proposals, not implemented or final. The existing playtest implementation remains available; this hold does not disable it.

The owner's correction also applies to material attribution: Echo Shard makes Timeless Slurry, which combines with Pyrium in specific Iron restoration recipes, but those recipes do not establish that either ingredient universally supplies restoration. Likewise, a healing ingredient paired with Amethyst does not establish Amethyst as the source of healing. [Pinned material facts](../research/iron-material-uses.md) remain distinct from new Vestige ingredient conventions. Diamond's pinned Jewelry parameters include Armor and a magic-damage action; the explicit Spell Power attribute belongs to Mithril. Gunpowder's proposed broader identity does not automatically make every existing Gunpowder offering volatile.

## Dissentient Diamond: selected name and ingredients

**Owner selection, October 8:** the reusable intermediate is named **Dissentient Diamond** and uses **one Wither Skeleton Skull, two Gunpowder and one Diamond**. The two Gunpowder units are intentional. This replaces the earlier Ender Pearl suggestion; Soul Sand, Echo Shard, Crying Obsidian and Nether Star were discussed as alternatives, not selected ingredients.

The owner's concept is a highly ordered diamond crystal whose fractured structure has entered a chaotic, heretical state: a crystal in dissent against its own natural order. This is an original Vestige material identity, not an upstream claim that Diamond or Wither Skeleton Skull inherently grants a universal chaos property. Its intrinsic trait rating, crafting presentation, output quantity and additional uses remain to be defined. The ordinary ingredient is registered as `vestige:dissentient_diamond`; its selected crafting recipe is not yet implemented.

**Owner artwork direction, October 8:** start with the texture, then implement the ingredient. It is a crafting ingredient like the thread components; it does not need enchantment shimmer or an overt magical visual effect. Work from a **32×32 cyan diamond with dark fissures**, then reduce to **16×16 for Minecraft inventory comparison**. The owner rejected v7's intact diamond with small cracks, clarifying that v6's fractured shape was good and only its noise should be reduced. V8 preserved that split structure with quieter faces. V9 replaced the black bottom edge with vanilla Diamond's dark teal. V10 changed the thirteen near-black fissure pixels to dark petrol/navy blue. The owner liked v10 and requested horizontal inventory centering. [V11 artwork and review](../art/dissentient-diamond-v11/README.md) preserves the exact v10 texture and adds a half-texel GUI X translation to center its odd-width footprint. The owner approved this exact artwork and alignment on October 8; [acceptance](../art/dissentient-diamond-v11/approval.json) pins the texture/model hashes. The registered ingredient uses native **Uncommon** rarity and a **yellow** name, matching Minecraft 1.21.1's Wither Skeleton Skull. It is an ordinary stackable ingredient with no glint, durability or direct-use/casting behavior; an intrinsic trait rating remains unspecified. Earlier native art previews used a Diamond carrier. Registration does not change the held Fluxed Flint revision.

## First playable recipe and repair

Craft one Fluxed Flint by offering **one Flint, one Diamond Block, one Netherite Ingot and one Echo Shard** in that clockwise order on the four inner Plinths around an empty Spellstone. Whole rotations match; outer nodes are inactive and socket materials are retained. This provisional composition predates the design hold above. Echo Shard's upstream Timeless Slurry use was recipe inspiration, not evidence of an inherent restoration property; this is an original native recipe, without an Iron dependency or copied quality system. Both optional recipe viewers expose this public construction recipe.

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

Intrinsic item traits come from `RitualTraitSource`. Flint supplies its immutable profile. A native Staff's one fixed affinity supplies one trait point; contained scrolls do not become intrinsic Staff traits. A scroll offered as an input uses its actual native spell profile and stored trait modifiers. Wands do not automatically inherit the traits of their stored source spell. Future magical items can expose their own intrinsic profiles through the same interface rather than introducing another probability system.

## Remaining design choices

The expensive composition, 128-point budget, 25% operation cap and Volatile 2 need survival playtesting. Stabilization through imbuements or a permanent upgrade remains a proposal; no material currently reduces Flint risk. A public dynamic repair viewer presentation is also separate work: its exact output depends on the offered target's complete data, so it is not represented by a misleading fixed output recipe. The public Flint construction recipe is available in viewers.

Historical per-equipment Amethyst/Iron/thread repair proposals are superseded by this shared catalyst route. [Restoration folklore research](../research/magical-restoration-folklore.md) supplied thematic context; Fluxed Flint is an original name and mechanic, not a claim that folklore describes this exact object.
