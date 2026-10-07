# Material crafting standard

**Two-role apparatus, construction updated October 5.** [Plinth rituals](plinth-rituals.md) defines Spellstone and one Plinth role, each with the 36 cosmetic stone/masonry finishes in [apparatus variants](apparatus-variants.md). Embedded material sockets replace supporting-block influences. The current grids are recorded below; preceding three-block grids retain historical status. The 56 executable local Spellshaping rules are recorded in [their ledger](../spellshaping-recipes.md); the unimplemented augment palette has been removed.

Status: use Iron's material identities, properties and flavor as ingredient inspiration, October 2, 2026. **Materials select different contributions; Iron's quality scaling is not adopted, and its patterns/parts are loose recipe inspiration.** This supersedes the earlier proposal to adopt the complete quality and pattern model. Core progression must work standalone; Iron mods supply optional ingredient/material integrations. The preceding three-block design selected **Spellstone**, four inner **Stone Pedestals** and four additional outer **Runic Pedestals**, with the Chiseled Deepslate/Amethyst/Diamond grids below. Their old recipe/art records remain historical; [native apparatus](apparatus-models.md) now supplies the two-block construction for fresh worlds. Native scroll discovery and all 214 ritual recipes are implemented in [Spellstone rituals](ritual-crafting.md), including vanilla baselines and optional Iron alternatives. Spellshaping uses a Plinth's embedded material to alter its individual offering's contribution; 50 individual material effects and six compounds are implemented in the executable ledger; native equipment remains future work.

The [frozen material reference](../../tools/iron-materials.json) records **37 real material definitions**: fifteen core Gems 'n Jewelry materials, seven optional compatibility metals, and fifteen Spellbooks integration materials. Jewelry's noncraftable developer example is excluded. Sources are pinned independently to Minecraft 1.21.1: [Gems 'n Jewelry 2.0.2](https://github.com/iron431/irons-jewelry/tree/bd3fae5579376472c04d9090e42a7a932d604333) and [Spellbooks 3.16.3](https://github.com/iron431/irons-spells-n-spellbooks/tree/e4056af90302d37eb1739f5ff05020b020e6e252). These identify reference behavior; Vestige remains NeoForge 21.1.72 / Java 21 with no new mod dependency.

For the full item-by-item source list, read [Iron's material and ingredient reference](../research/iron-material-uses.md). It includes all Amethyst forms, all 37 Jewelry definitions, all 261 committed Spellbooks recipe files, alchemy, focus ingredients, equipment repairs and mundane construction inputs. This design document's property table is only a selection of examples; the full reference preserves each distinct use without inventing one universal meaning per material.

## Accepted native material direction

Material choice determines **what an ingredient contributes**: an attribute, action, effect, protection or other explicitly authored recipe role. Align those contributions with Iron's established meanings where appropriate. Copper can inform speed/lightning contributions, Iron armor/knockback, Gold mining/ignition and Netherite attack/Strength. Exact native recipes and contributions are still to be authored; these associations do not grant every property whenever the material appears.

Do not build a generic material power ladder. Replacing a Copper component with Netherite does not multiply the same finished bonus by the ratio of their upstream quality values. Netherite may instead contribute a different effect or attribute. Native amounts, durations, costs and constraints are balanced for the selected effect and recipe; the source's numerical quality and nominal bonus values remain reference facts, not automatically accepted native tuning. This does not prohibit an explicitly authored attribute bonus such as attack speed or a spell modifier; it rejects generic material-quality scaling of those bonuses.

Follow patterns and parts loosely as ways to organize ingredient roles and select contributions. Vestige does not require Iron's exact jewelry assembly, a designated quality part, pattern/bonus quality multipliers, pattern unlocks or the same recipe layout. Spell crafting/augmenting still uses the owner's shared structure, with its recipe rules to be designed.

## Apparatus construction references

The owner's selected apparatus uses Amethyst and **Chiseled Deepslate**, with three block types: **Spellstone**, **Stone Pedestal** and **Runic Pedestal**. It serves many schools and traditions. The basic form has four inner Stone Pedestals; the advanced form adds four outer Runic Pedestals. The owner supplied the construction recipes below; appearance remains in review. Deepslate already has a direct Iron apparatus precedent: its Scroll Forge uses three Polished Deepslate and four Crying Obsidian. Its Arcane Anvil uses three Amethyst Blocks, two Polished Deepslate, a diamond-tagged gem and a vanilla Anvil. Vestige's selected Chiseled variant is its own recipe choice. [Pinned construction recipes](../research/irons-spell-materials.md#runes-and-base-crafting-materials), [official block guide](https://iron.wiki/blocks/)

Lapis as a general rune-inscription ingredient was a Vestige proposal based on vanilla enchanting, not an established general-magic standard in Iron's. Iron's Jewelry Lapis properties are experience gain, a Slowness action and a Glowing effect-immunity parameter. Spellbooks' general magical crafting resource is Arcane Essence. [Jewelry material catalog](https://ironsjewelry.wiki/materials/#lapis-lazuli), [Arcane Essence](https://iron.wiki/items/#arcane-essence)

Amethyst and Deepslate provide a standalone palette consistent with those apparatus ingredient families. Crying Obsidian remains source precedent rather than a required ingredient in the selected recipes. Construction materials do not automatically define supporting-block contributions or grant Jewelry properties, spell bonuses or school restrictions.

<a id="proposed-construction-recipes"></a>

### Current two-block construction

The owner's October 5 recipes use matching full blocks and slabs for every finish. Spellstone uses **five full blocks, one slab, two Diamonds and one Amethyst Block**, producing one Spellstone. Plinth uses **three full blocks and four slabs**, producing **two Plinths**. [The playtest guide](../leyline-playtesting.md) shows the exact grids; [the material table](apparatus-variants.md#construction) lists all 36 verified block/slab pairs.

Carrier construction and decorative trim are cosmetic. Each Plinth's independently installed socket selects local offering/material Spellshaping. Install on a side, recover by sneak-clicking a side with an empty hand; it saves, synchronizes and drops independently of its offering. Supporting blocks have no gameplay contribution. The earlier October 4 Chiseled Stone Brick/Iron grids are superseded.

### Optional pedestal conversions

**Accepted October 6, 2026:** keep foreign display pedestals separate and offer automatic, one-way crafting-grid conversions into the matching Vestige Plinth finish. Material identity is preserved; these recipes do not unlock the other masonry finishes.

| Source item | Crafting-grid output |
|---|---|
| Iron's Spells 'n Spellbooks `irons_spellbooks:pedestal` | 1 Stone Bricks Plinth (`vestige:plinth`) |
| Supplementaries `supplementaries:pedestal` | 1 Stone Bricks Plinth (`vestige:plinth`) |

The inspected ValeCraft jars, Spellbooks 3.16.2 and Supplementaries 3.8.5 for Minecraft 1.21.1, each have one Stone Brick pedestal item. Supplementaries' connected top/middle/base models are stack states, not different masonry items. Put one pedestal in any crafting-grid slot to obtain one Plinth; no extra ingredient is needed. Ordinary crafted items are converted, so take displayed contents off a placed pedestal before breaking and crafting it. There is no in-world block replacement or reverse conversion.

Each recipe and its recipe-book unlock require both the provider mod and its exact pedestal item. Missing or disabled source items skip the integration cleanly. This uses NeoForge's [standard data load conditions](https://docs.neoforged.net/docs/1.21.1/resources/server/conditions/), with no foreign Java API and no dependency declaration for either mod. Native construction recipes retain their original costs and outputs. Any future source finish needs its own explicit material-matched conversion.

### Historical three-block construction recipes

**Owner-supplied grids, October 3, 2026; superseded by the two-block construction above.** These construct apparatus blocks separately from their planned discovery/augmentation operations. Output is one block per craft, retaining the authoring assumption because screenshots do not show quantities. Source images and original JSON are preserved under [apparatus artwork](../art/apparatus-v1/README.md). Those original authoring grids remain historical evidence; the old pedestal crafting recipes, IDs and migration code are removed.

| Block | Crafting-table ingredients | Construction role |
|---|---|---|
| Spellstone | 4 Chiseled Deepslate + 1 Amethyst Block + 2 Diamonds + 2 Amethyst Shards | The shared magical centerpiece |
| Stone Pedestal | 4 Chiseled Deepslate + 2 Amethyst Shards | One of four inner ingredient stands |
| Runic Pedestal | 3 Chiseled Deepslate + 1 Diamond Block + 2 Amethyst Shards | One of four additional outer ingredient stands |

Legend: `D` = Chiseled Deepslate, `A` = Amethyst Shard, `B` = Amethyst Block, `I` = Diamond, `X` = Diamond Block, `.` = empty slot. Rows show the owner's full three-by-three grids:

```text
Spellstone       Stone Pedestal    Runic Pedestal
D B D            . . .             . . .
I D I            D D D             D X D
A D A            A D A             A D A
```

All three are shaped crafting-table recipes. The pedestal patterns occupy three columns and two rows; ordinary Minecraft shaped-recipe matching may shift them vertically within the table. Runic Pedestal is crafted directly from its materials; no Stone Pedestal is consumed. These replace the earlier proposed recipes and shapeless pedestal-upgrade route.

The confirmed basic structure is one Spellstone and four inner Stone Pedestals. Its advanced form preserves those blocks and adds four outer Runic Pedestals. This supersedes the earlier suggestion to use eight advanced pedestals. Exact distances, validation and activation are implemented in [Spellstone rituals](ritual-crafting.md). Totals count apparatus blocks only; supporting blocks and ingredients placed on the pedestals are additional recipe context.

| Structure | Chiseled Deepslate | Amethyst Shards, including crafted Amethyst Block | Diamonds, including crafted Diamond Blocks |
|---|---:|---:|---:|
| Basic: one Spellstone + four Stone Pedestals | 20 | 14 | 2 |
| Advanced from scratch: one Spellstone + four Stone + four Runic Pedestals | 32 | 22 | 38 |
| Additional materials to extend a complete basic structure | 12 | 8 | 36 |

The extension uses four Diamond Blocks (36 Diamonds). The original four Stone Pedestals and Spellstone remain in place. One Amethyst Block crafts from four shards; one Diamond Block from nine Diamonds. The selected recipes make the outer extension a substantial Diamond investment. Acquisition pacing has not been playtested.

The center and pedestals provide shared construction and capacity. Fragments, other placed ingredients and explicitly authored supporting-block contributions determine the magical route. A Diamond in the advanced block recipe does not apply Iron's Diamond Jewelry parameters or multiply spell strength. Advanced discovery retains the [same intersection and average-weight rules](spell-discovery.md#weighting-after-filtering); eight slots alone grant no rarity or potency bonus.

Proposed visual direction: weathered Deepslate, carved markings and recessed Amethyst; the advanced stand can carry more crystal facets while retaining the same stone family. This carries the lost-magic theme without making a rare ruin drop a prerequisite. Ruin-based acquisition, alternate recipes and any new ancient component remain future proposals rather than required progression.

## Standalone recipes and optional Iron support

The owner's accepted direction is **Iron's standards and flavor, implemented by standalone Vestige, with seamless optional Iron support**. Required apparatus construction, discovery, spell crafting/augmenting and eventual equipment progression must remain accessible using Minecraft and native Vestige items, such as its scrolls and fragments, without installing any Iron mod. Prefer existing vanilla ingredients where they fit the construction role; native components are also allowed if Vestige provides their acquisition independently. Advanced eight-slot structures must have a standalone acquisition path as well.

The selected material meanings apply even when neither Iron mod is installed. Existing Minecraft materials retain Iron's established property associations: Copper offers attack-speed/lightning inspiration, and Gold mining-speed/ignition inspiration. Their source quality values remain inert reference data. Preserve the selected meanings through explicit native consumers rather than assigning unrelated meanings in the standalone configuration. Likewise, native components inspired by Iron should retain the appropriate crafting role and flavor; this does not prescribe an entire cloned item catalog or make every item named "arcane" an arcane-tradition bonus. Foreign attribute/effect translation remains explicit design work.

When an Iron mod is installed, its appropriate materials should become additional accepted ingredients or explicitly authored material options. Matching Iron components should be usable directly in the relevant Vestige recipe without manual conversion into a duplicate Vestige component. Load support according to the available provider, respecting that provider's own dependencies; Vestige must not introduce a requirement for the other Iron mod. Additional Iron mods need explicit supported-item mappings. Namespace resemblance alone does not establish equivalence.

- Preserve the standalone recipes when integrations are enabled. Foreign-only alternatives may add routes, but cannot replace the only route to required progression. Installing Iron must not silently change the established meaning of a vanilla material.
- Define alternatives by a recipe's construction role, using item/tag eligibility and conditional integration data. Do not accept every ingot, gem or rune indiscriminately.
- Keep recipe acceptance separate from material resolution. An Arcane Ingot can be an alternative frame ingredient without granting mana or generating Essence. When a recipe uses material properties, resolve the selected material's authored contribution without adding upstream quality multipliers. Ingredient alternatives do not automatically have identical properties.
- Adopting the standard does not require recreating Iron's entire ore, gem, Essence or rune catalog as Vestige items. Add native materials only when a Vestige mechanic calls for them; otherwise prefer existing Minecraft ingredients.
- Earlier apparatus suggestions requiring foreign Arcane Essence, Arcane Ingots, Arcane Runes and Mithril are optional integration candidates, not mandatory baseline ingredients or accepted recipes. Standalone acquisition of any proposed native counterpart must be defined before it becomes required. Exact substitutions, quantities and costs remain to be designed.

These examples illustrate the proposed construction roles, rather than finalized recipes or claims that the materials share properties:

| Construction role | Proposed vanilla baseline | Proposed optional Iron ingredient |
|---|---|---|
| Metal frame | Iron or Gold Ingots | Arcane Ingot |
| Neutral focusing/inscription component | Amethyst and a carved stone component | Arcane Rune or Arcane Essence, as the particular recipe permits |
| Reinforcement option | Diamond or Netherite-based construction | Mithril |

The frozen reference remains upstream evidence, including foreign IDs; it is not a mandatory runtime item registry. Optional integrations must resolve existing items only when their provider is available and isolate any foreign API use. Missing integrations must leave baseline recipes loadable, with no missing-item failures or unavailable-class errors. Future verification must cover Vestige alone and valid provider combinations, including both together, checking baseline progression, unchanged vanilla material meanings and direct use of the added alternatives. Provider-only test combinations must respect the provider's own dependency requirements.

## Iron's pattern model as reference

The following records upstream behavior. Its ingredient-role and property-selection ideas inform Vestige; its quality machinery and exact assembly system are not adopted.

- A **pattern** chooses the construction's parts and the kinds of bonuses it provides.
- A **part** accepts specified material families or identities and has an ingredient quantity.
- A **material** matches an item or item tag and supplies quality plus named bonus parameters. Attribute, positive effect, negative effect and action are separate parameter channels.
- A bonus selects the channel it needs from the material assigned to its part. An explicit pattern parameter can override that selection. A missing channel supplies no bonus.
- The pattern may designate one part whose material determines quality. The material selecting an effect and the material supplying quality can be different.

This describes the [official data format](https://ironsjewelry.wiki/data-format/) and the pinned [material definition](https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/main/java/io/redspace/ironsjewelry/core/data/MaterialDefinition.java), [part definition](https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/main/java/io/redspace/ironsjewelry/core/data/PartDefinition.java) and [bonus resolution](https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/main/java/io/redspace/ironsjewelry/core/data/JewelryData.java).

Ruby, for example, offers a max-health attribute parameter and an ignition action. An attribute pattern uses health; an on-attack pattern can use ignition. Using Ruby does not grant both automatically. A negative-effect parameter can select immunity in a pattern rather than impose a penalty on the wearer: bonus types own that interpretation. [Ruby material](https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/generated/resources/data/irons_jewelry/irons_jewelry/material/ruby.json), [official bonus types](https://ironsjewelry.wiki/data-format/#bonus-types)

## Quality calculation

**Upstream reference only.** This formula and the ring example below describe Iron's behavior. The owner has rejected using them as Vestige's material scaling or progression rule.

```text
source quality = designated part's material quality, or 1 if none is designated
effective quality = source quality × pattern multiplier × bonus multiplier
attribute amount = material's base attribute amount × effective quality
scalar amount = base + (effective quality - 1) × slope
```

Actions, durations and cooldowns use their authored slope, not an automatic multiplication by quality. Constants have slope zero. Bounds are sign-aware: nonnegative bases use a minimum then an optional maximum; negative bases reverse those comparisons. Attribute operations retain their source meaning (`add_value`, `add_multiplied_base`, or `add_multiplied_total`). [Quality resolution](https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/main/java/io/redspace/ironsjewelry/core/data/JewelryData.java), [scalar sampling](https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/main/java/io/redspace/ironsjewelry/core/data/QualityScalar.java), [attribute scaling](https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/main/java/io/redspace/ironsjewelry/core/bonuses/AttributeBonusType.java)

The basic gemset ring uses its **band** for quality. Ruby's nominal +2 health becomes +3 with a Gold band (quality 1.5), or +5 with a Netherite band (quality 2.5). Ruby's own quality of 2 is not multiplied in again. The superior pattern supplies another multiplier and separate multipliers for its two gem bonuses. [Basic pattern](https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/generated/resources/data/irons_jewelry/irons_jewelry/pattern/gemset_ring.json), [superior pattern](https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/generated/resources/data/irons_jewelry/irons_jewelry/pattern/superior_gemset_ring.json)

## Example properties

These are upstream nominal parameters before Iron's quality evaluation, not finalized Vestige bonuses. The Quality column is source reference only. Full records retain ingredient selectors, source attribute IDs and operations, effects, action parameters, bounds, source links and source-file hashes.

| Material | Quality | Attribute channel | Action channel | Source |
|---|---:|---|---|---|
| Copper | 0.5 | +10% attack speed | Lightning damage | [Definition](https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/generated/resources/data/irons_jewelry/irons_jewelry/material/copper.json) |
| Iron | 0.75 | +2 armor | Knockback | [Definition](https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/generated/resources/data/irons_jewelry/irons_jewelry/material/iron.json) |
| Gold | 1.5 | +10% mining speed | Ignition | [Definition](https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/generated/resources/data/irons_jewelry/irons_jewelry/material/gold.json) |
| Ruby | 2 | +2 max health | Ignition | [Definition](https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/generated/resources/data/irons_jewelry/irons_jewelry/material/ruby.json) |
| Sapphire | 2 | +0.03 dodge chance | Freezing | [Definition](https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/generated/resources/data/irons_jewelry/irons_jewelry/material/sapphire.json) |
| Mithril | 3 | +5% general spell power | None declared | [Definition](https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/resources/data/irons_spellbooks/irons_jewelry/material/mithril.json) |
| Pyrium | 3 | +5% cast-time reduction | None declared | [Definition](https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/resources/data/irons_spellbooks/irons_jewelry/material/pyrium.json) |
| Fire Rune | 1.5 | +2.5% fire spell power | None declared | [Definition](https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/resources/data/irons_spellbooks/irons_jewelry/material/rune_fire.json) |

Core availability includes eight vanilla materials and seven Jewelry gemstones. Seven further metals are optional compatibility references and require a supplied matching ingredient. Spellbooks adds four materials and eleven runes. [Jewelry catalog](https://github.com/iron431/irons-jewelry/tree/bd3fae5579376472c04d9090e42a7a932d604333/src/generated/resources/data/irons_jewelry/irons_jewelry/material), [ingredient availability](https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/main/java/io/redspace/ironsjewelry/core/data/MaterialDefinition.java), [Spellbooks integration](../research/irons-spell-materials.md#spellbooks-jewelry-materials)

## Spell materials and native translation

School-focus ingredients, Jewelry parameters and named equipment bonuses remain distinct source facts. A Blaze Rod's Fire-focus role does not assign it an invented `fire` rating. Mithril's Jewelry parameter likewise differs from a named staff's or armor set's bonuses. The [Spellbooks research](../research/irons-spell-materials.md) records nine focus tags, eleven runes, crafting components and equipment statistics.

Following material meanings does not resolve every foreign attribute/effect into a native consumer. Retain source targets and operations in the factual reference; define native mappings and tuning explicitly, without upstream quality scaling. Vanilla attributes, trait modifiers, mana, timing, conditional actions and resistance are different consumers. An Iron Fire-power bonus and the owner's literal multiplier of a cast's `fire` rating are not interchangeable: effects must read the trait they scale from. Define native consumers explicitly when implementing equipment; only `volatile` remains an inherent semantic trait. [Trait catalog](trait-catalog.md), [current scaling](spell-balance.md#numerical-boost-response)

The [scroll discovery rules](spell-discovery.md) remain authoritative: at least four participating fragments may have different traits, and eligible spells must contain every supplied trait. Iron's material properties and flavor inform each slot's ingredient and supporting-block contributions; the native mapping from block IDs/tags and exact effects remain to be specified. Upstream material quality is not used to change crafted potency, fragment odds, spell rarity or base traits. Iron's ink tiers, spell levels, pattern-learning progression, station layout and upgrade rules remain source examples rather than adopted Vestige progression.

## Implementation boundary

This step accepts material-property/flavor alignment, loose recipe inspiration and standalone/optional integration requirements. It preserves upstream facts, including rejected quality mechanics, in an independently structured reference. It adds no runtime items, foreign attribute dependency, recipes, equipment slots or station UI. Palette paths, textures, particles, sounds, translations and implementation code are absent from the reference.

Future gameplay needs native consumers, recipe/ingredient eligibility, block-to-material contribution mappings, authored effects and balancing, atomic resource consumption and equipped-modifier cleanup. Unsupported properties must remain explicit. Verify different material contributions, absent properties, ingredient availability, optional integrations and base-block changes before claiming implementation. Source quality values must remain inert in Vestige's recipes and bonuses.
