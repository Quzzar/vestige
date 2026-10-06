# Iron’s Jewelry: complete material and ingredient reference

**Status:** pinned upstream research, checked October 2, 2026. These are Iron’s rules, not implemented Vestige equipment or recipes. Vestige accepts distinct material contributions and optional interoperability; the owner explicitly rejected adopting Iron’s generic material-quality power ladder. The source quality column below is historical reference only. Exact Vestige values, acquisition, station recipes, and augmentation behavior remain undecided.

This document covers **all 37 ingredient-bearing Jewelry material definitions** in the frozen catalog: **15 core materials** (8 vanilla materials and 7 Jewelry gemstones), **7 optional compatibility metals**, and **15 Spellbooks integration materials**. It also covers **all 15 Jewelry assembly patterns**, the **1 Spellbooks integration pattern**, the **3 ordinary Jewelry crafting recipes**, and gemstone acquisition. Jewelry has one additional generated `example` definition with no ingredient or parameters; it is a non-craftable template, not a missing 38th usable material. The broader Spellbooks recipe and ingredient survey is in [the Spellbooks material research](irons-spell-materials.md). The accepted native design is in [Material Crafting](../design/material-crafting.md).

Pinned sources:

- Iron’s Gems ’n Jewelry **1.21.1-2.0.2**, revision `bd3fae5579376472c04d9090e42a7a932d604333`.
- Iron’s Spells ’n Spellbooks **3.16.3**, revision `e4056af90302d37eb1739f5ff05020b020e6e252`.
- The checked definitions and individual SHA-256 digests are frozen in [the material catalog](../../tools/iron-materials.json). All 37 stored file digests were checked against the pinned snapshots.

## What a material means

A Jewelry material is a choice of ingredient with up to four independent parameter channels. A pattern selects the channel for each bonus-bearing part. **Making something from Amethyst does not grant every Amethyst entry at once.** Parts may be purely structural, supply a bonus, or determine upstream quality. An explicit pattern parameter can replace the material-selected value, and a missing material channel yields no bonus for that selection. [Jewelry resolution][jewelry-data], [material generator][material-generator]

| Channel | Meaning in Iron’s Jewelry | Example |
| --- | --- | --- |
| Attribute | A stat modifier while the jewelry is equipped. | A gem slot in a Gemset Ring selects Amethyst’s Armor Pierce or Diamond’s Armor. |
| Action | A behavior triggered by the pattern, such as a direct attack, projectile hit, taking damage, or shield block. The material also specifies whether it affects the wearer or the other entity. | Amethyst supplies Thorns damage; Iron supplies knockback; Onyx supplies healing. |
| Positive effect | A status-effect selection used by a bonus that grants a beneficial effect when the wearer takes damage. It is distinct from an action that explicitly applies an effect. | Copper selects Haste; Emerald selects Speed. |
| Negative effect | A status-effect selection used by an immunity bonus. **It does not automatically curse or poison the wearer.** | An Amethyst Tearstone Ring selects Poison immunity; a Diamond one selects Weakness immunity. |

The effect and action routing is verified in [bonus events][bonus-events], [positive-effect bonus][positive-effect-bonus], and [immunity bonus][immunity-bonus]. The positive-effect channel is implemented by the source runtime, but none of the 15 pinned built-in Jewelry patterns or the 1 Spellbooks pattern selects it. Thus “Copper = Haste” is an available material parameter, not a promise that every existing Copper piece grants Haste.

**How to read the numbers:** attributes and actions below show their authored base values at **effective quality 1**, before any pattern/material quality adjustment. They are not final values for a particular crafted item. `+10%` means an authored `add_multiplied_total` amount of `0.10`; plain `+` entries are `add_value` amounts. Damage and health use Minecraft HP (2 HP = 1 heart). Action timers are converted from 20 ticks per second. A dash means the material has no parameter in that channel. Source quality is a separate field and is not a Vestige multiplier.

## Vanilla materials: every core metal and gemstone

| Material / ingredient selector | Attribute base | Action base and target | Positive effect | Negative effect / immunity selection | Source quality only | Pinned definition |
| --- | --- | --- | --- | --- | --- | --- |
| Amethyst · `#c:gems/amethyst` | +1 Armor Pierce | 3 Thorns damage to other entity | Regeneration | Poison | 1 | [amethyst] |
| Diamond · `#c:gems/diamond` | +2 Armor | 1 magic damage to other entity | Resistance | Weakness | 2 | [diamond] |
| Emerald · `#c:gems/emerald` | +5% Movement Speed | 5% chance to spawn 1–4 Emeralds at other entity | Speed | Infested | 1.5 | [emerald] |
| Lapis Lazuli · `#c:gems/lapis` | +10% Experience Gained | Slowness I for 4 seconds on other entity | — | Glowing | 1 | [lapis] |
| Copper · `#c:ingots/copper` | +10% Attack Speed | 3 lightning-type damage to other entity | Haste | Poison | 0.5 | [copper] |
| Gold · `#c:ingots/gold` | +10% Mining Speed | Ignite other entity for 2 seconds | Fire Resistance | Poison | 1.5 | [gold] |
| Iron · `#c:ingots/iron` | +2 Armor | Push other entity away; strength 2 | Resistance | Slowness | 0.75 | [iron] |
| Netherite · `#c:ingots/netherite` | +5% Attack Damage | Strength I for 3 seconds on wearer | Strength | Wither | 2.5 | [netherite] |

These selectors are shared item tags, not requirements for a specific mod’s duplicate ingot or gemstone. Vanilla items satisfy the vanilla families in the normal dependency/tag environment. The four vanilla metals are members of Jewelry’s [metal material group][metal-tag]; the four vanilla gemstones are in its [gem material group][gem-tag]. Lapis is an experience/Slowness/Glowing choice here, not a universal mana resource.

## All seven Jewelry gemstones

All seven below are new Jewelry items with matching common gem tags. They share the same Jewelry acquisition channels described below; these definitions do not assign one gemstone exclusively to a particular dimension, ore tier, or biome.

| Material / ingredient selector | Attribute base | Action base and target | Positive effect | Negative effect / immunity selection | Source quality only | Pinned definition |
| --- | --- | --- | --- | --- | --- | --- |
| Garnet · `#c:gems/garnet` | +4% Attack Damage | Resistance I for 4 seconds on wearer | — | Wither | 2 | [garnet] |
| Moonstone · `#c:gems/moonstone` | +10% Arrow Damage | Wind burst centered on other entity; radius 2 | Invisibility | Weaving | 2 | [moonstone] |
| Onyx · `#c:gems/onyx` | +0.15 Crit Damage | Heal wearer 4 HP | — | Blindness | 2 | [onyx] |
| Peridot · `#c:gems/peridot` | +10% Mining Speed | Pull other entity toward wearer; strength −2 | Haste | Poison | 2 | [peridot] |
| Ruby · `#c:gems/ruby` | +2 Max Health | Ignite other entity for 2 seconds | — | Hunger | 2 | [ruby] |
| Sapphire · `#c:gems/sapphire` | +0.03 Dodge Chance | Freeze other entity; authored amount 40 | — | Slowness | 2 | [sapphire] |
| Topaz · `#c:gems/topaz` | +2 Armor Toughness | Pull wearer toward other entity; strength −2 | — | Levitation | 2 | [topaz] |

**Target details matter.** Peridot moves the counterpart toward the wearer; Topaz moves the wearer toward the counterpart. The current wiki’s generic “pull attacker in” summary does not capture Topaz’s `targetSelf: true`; the pinned definition and [knockback implementation][knockback-action] are authoritative. Moonstone and Silver use a non-damaging wind-burst explosion, preserve blocks, and exclude the wearer from the explosion’s effects. [Explosion implementation][explode-action]

Sapphire’s `40` is a source action parameter, not “40 damage” or a guaranteed 2-second immobilization. The pinned [freeze implementation][freeze-action] alters the entity’s frozen-tick state and has its own doubling/threshold behavior. No client behavior or final status duration is claimed here.

### How the seven gems are obtained

**Structure loot:** all seven gem tags are in the [lootable gem pool][lootable-gems]. Jewelry injects that shared pool into these vanilla chest families:

| Loot group | Chests that receive the pool | Rule in the pinned pool |
| --- | --- | --- |
| Generic | Jungle Temple; Pillager Outpost; Dungeon; Stronghold Crossing and Corridor; large and small Underwater Ruins; Woodland Mansion; Village Temple; Nether Fortress; Bastion Bridge and Other; Ancient City. | One potential roll with a 35% gate; selected gem count is a binomial draw with `n=5`, `p=0.6`, so a draw can yield zero. [Locations][generic-gem-injection], [pool][generic-gem-pool] |
| Wealthy | End City Treasure; Bastion Treasure; Buried Treasure; Desert Pyramid; Abandoned Mineshaft. | 0–2 rolls; each selected gem count uses the same `n=5`, `p=0.6` binomial draw. [Locations][wealthy-gem-injection], [pool][wealthy-gem-pool] |
| Village Jeweler building | Jewelry’s own Village Jeweler chest. | An additional shared gem-pool roll with binomial `n=5`, `p=0.5`; the chest also contains other loot. [Chest definition][village-jeweler-loot] |

Additional loot-table-specific integrations cover structures from other mods, including Spellbooks’ coffins, magic bookshelves, crypts, and magical treasure. These are extra loot routes when their loot tables exist. The compatibility file named `compat_wealthy_gems` actually references the **generic** gem pool in this pin; its name alone is not evidence of richer drops. [Generic integrations][compat-gem-injection], [wealthy-named integrations][compat-wealthy-gem-injection]

**Jeweler trading:** the Jewelcrafting Station supplies the Jeweler job site. Apprentice Jewelers offer a randomly selected sellable gem for **10 Emeralds**; Expert Jewelers offer one for **8 Emeralds**, before normal merchant pricing changes. The sellable pool contains exactly the seven new gemstone tags. Buy trades also accept Diamond, Amethyst, and Lapis as well as the seven new gems; Emerald is the currency rather than a member of that buyable-gem list. Novices buy **6 Gold Ingots or 9 Copper Ingots for 1 Emerald**. The source also offers finished jewelry and pattern scrolls at other profession levels. [Trade registration][trade-setup], [trade construction][trades], [sell pool][sellable-gems], [buy pool][buyable-gems], [job site][villager-registry]

The verified native routes are chest loot and trading. The pinned repository has no gemstone ore recipes or gemstone ore world-generation definitions. A datapack or another mod may add acquisition routes; those are outside this pin.

## All seven optional compatibility metals

These are definitions for common ingot tags supplied by other mods. Jewelry does **not** manufacture Brass, Bronze, Platinum, Silver, Allthemodium, Unobtainium, or Vibranium. The usable item set depends on which provider populates each tag; there is no single Jewelry-owned mining or alloying recipe for these metals. [Metal material group][metal-tag], [material generator][material-generator]

| Material / ingredient selector | Attribute base | Action base and target | Positive effect | Negative effect / immunity selection | Source quality only | Pinned definition |
| --- | --- | --- | --- | --- | --- | --- |
| Brass · `#c:ingots/brass` | +0.10 Knockback Resistance | — | — | — | 0.75 | [brass] |
| Bronze · `#c:ingots/bronze` | +10% Swim Speed | — | — | — | 1 | [bronze] |
| Platinum · `#c:ingots/platinum` | +1 Armor Pierce | Push other entity away; strength 2 | Resistance | Weakness | 2 | [platinum] |
| Silver · `#c:ingots/silver` | +5% Movement Speed | Wind burst centered on wearer; radius 2 | Speed | Bad Omen | 1 | [silver] |
| Allthemodium · `#c:ingots/allthemodium` | +2 Armor Toughness | Strength I for 6 seconds on wearer | Resistance | Slowness | 3 | [allthemodium] |
| Unobtainium · `#c:ingots/unobtainium` | +2 Armor Toughness | Strength I for 6 seconds on wearer | Resistance | Slowness | 4 | [unobtainium] |
| Vibranium · `#c:ingots/vibranium` | +2 Armor Toughness | Strength I for 6 seconds on wearer | Resistance | Slowness | 3.5 | [vibranium] |

The pinned Brass attribute is **0.10**, despite the current wiki displaying `+1`. Allthemodium, Unobtainium, and Vibranium have the same four nominal channel selections and differ in source quality; Vestige has not accepted those qualities as a native power progression.

## All fifteen Spellbooks integration materials

These definitions describe how Spellbooks ingredients become Jewelry material choices when the compatible mods are present. Their source stats concern Iron’s mana, schools, and statuses; they do not create inherent semantics for Vestige traits or require Vestige to implement Iron’s stat system.

| Material / ingredient selector | Attribute base | Action base and target | Positive effect | Negative effect / immunity selection | Source quality only | Pinned definition |
| --- | --- | --- | --- | --- | --- | --- |
| Arcane Ingot · `irons_spellbooks:arcane_ingot` | +50 Max Mana | 4% chance to spawn 1–3 Arcane Essence at other entity | — | — | 1.5 | [arcane] |
| Divine Pearl · `irons_spellbooks:divine_pearl` | +10% Casting Movement Speed | Heal wearer 4 HP | Fortify | Rend | 1 | [divine_pearl] |
| Mithril · `#c:ingots/mithril` | +5% Spell Power | — | Charged | Rend | 3 | [mithril] |
| Pyrium · `#c:ingots/pyrium` | +5% Cast Time Reduction | — | Vigor | Guided | 3 | [pyrium] |
| Arcane Rune · `irons_spellbooks:arcane_rune` | +25 Max Mana | — | — | — | 1.5 | [rune_arcane] |
| Blood Rune · `irons_spellbooks:blood_rune` | +2.5% Blood Spell Power | — | — | — | 1.5 | [rune_blood] |
| Recovery Rune (`cooldown_rune`) · `irons_spellbooks:cooldown_rune` | +5% Cooldown Reduction | — | — | — | 1.5 | [rune_cooldown] |
| Ender Rune · `irons_spellbooks:ender_rune` | +2.5% Ender Spell Power | — | — | — | 1.5 | [rune_ender] |
| Evocation Rune · `irons_spellbooks:evocation_rune` | +2.5% Evocation Spell Power | — | — | — | 1.5 | [rune_evocation] |
| Fire Rune · `irons_spellbooks:fire_rune` | +2.5% Fire Spell Power | — | — | — | 1.5 | [rune_fire] |
| Holy Rune · `irons_spellbooks:holy_rune` | +2.5% Holy Spell Power | — | — | — | 1.5 | [rune_holy] |
| Ice Rune · `irons_spellbooks:ice_rune` | +2.5% Ice Spell Power | — | — | — | 1.5 | [rune_ice] |
| Lightning Rune · `irons_spellbooks:lightning_rune` | +2.5% Lightning Spell Power | — | — | — | 1.5 | [rune_lightning] |
| Nature Rune · `irons_spellbooks:nature_rune` | +2.5% Nature Spell Power | — | — | — | 1.5 | [rune_nature] |
| Protection Rune (item name: Protective Rune) · `irons_spellbooks:protection_rune` | +2.5% Spell Resist | — | — | — | 1.5 | [rune_protection] |

The eight school runes are Blood, Ender, Evocation, Fire, Holy, Ice, Lightning, and Nature; Arcane, Recovery (`cooldown_rune`), and Protection have other stat roles. Material names do not establish a universal ingredient meaning outside their explicit recipes, school-focus metadata, and Jewelry parameters. See [the Spellbooks material research](irons-spell-materials.md) for crafting, alchemy, apparatus, and equipment uses of these ingredients.

## Pattern assembly: every pinned pattern

A pattern specifies the required parts, how many ingredient items each part consumes, and which part supplies each bonus. Most band/chain parts accept the **metal** material group; most gem parts accept the **gem** group. The Bane Ring’s skull accepts **metal or gem**. The Haggler stone is restricted to the **Emerald** material and the Piglin signet to **Gold**. These are material-registry groups, distinct from the item tags used to recognize the ingot or gem. [Part generator][material-generator], [metal group][metal-tag], [gem group][gem-tag], [Emerald group][emerald-tag], [Gold group][gold-tag]

“4 metal” below means four items of the chosen metal material’s ingredient, not four different metals. Each separately listed part has its own material choice. These are Jewelry’s inventory-based assemblies, not Vestige’s proposed four/eight pedestal slots.

| Pattern | Required ingredient parts | What the pattern selects | Pinned pattern |
| --- | --- | --- | --- |
| Simple Band | 4 metal | Band attribute | [simple_band] |
| Simple Chain | 4 metal | Chain attribute | [simple_chain] |
| Gemset Ring | 4 metal + 1 gem | Gem attribute | [gemset_ring] |
| Simple Amulet | 4 metal + 1 gem | Gem attribute | [simple_amulet] |
| Improved Gemset Ring | 6 metal + 1 gem | Gem attribute | [improved_gemset_ring] |
| Superior Gemset Ring | 6 metal + 1 side-gem + 2 center-gems | Attributes from the two gem parts | [superior_gemset_ring] |
| Rhinestone Amulet | 6 metal + 1 gem each for three separate gem parts | Three gem attributes | [rhinestone_amulet] |
| Amulet of Protection | 6 metal + 2 gems | Gem action when wearer takes damage | [amulet_of_protection] |
| Stalwart Ring | 6 metal | Metal action on successful shield block | [stalwart_ring] |
| Sharpshooter Loop | 6 metal + 2 gems | Gem action on projectile hit | [sharpshooter_loop] |
| Bane Ring | 6 metal + 4 metal-or-gem skull ingredients | Skull material action on direct attack | [bane_ring] |
| Tearstone Ring | 4 metal + 4 gems | Gem negative-effect channel as immunity | [tearstone_ring] |
| Haggler Ring | 4 metal + 4 Emeralds | Pattern-specific trade discount; no Emerald action or attribute channel | [haggler_ring] |
| Piglin Signet Ring | 4 metal + 4 Gold Ingots | Pattern-specific Piglin neutrality; no Gold ignite or attribute channel | [piglin_signet_ring] |
| Barbed Band | 8 metal + 3 gems | Metal attribute; direct-attack trigger explicitly damages wearer with Thorns, overriding material action | [barbed_band] |

The first four patterns are unlocked by default in their data. The other eleven require unlocking. In the source, advanced patterns are obtained through loot/trades for Artisan Scrolls; that discovery system is an upstream fact, not Vestige’s progression design. [Pattern defaults][pattern-definition], [trade registration][trade-setup]

Spellbooks adds **Rune-Inscribed Ring**: **6 band ingredients from the metal-or-gem material group + 1 rune**. The rune inscription selects the rune attribute; the band controls upstream quality. Its part metadata also declares a metal type label, but its concrete `allowedMaterials` selector is `#irons_jewelry:metal_or_gem`; the selector is reported literally here. The rune part declares `spellbooks_rune` and selects `#irons_spellbooks:rune`. [Pattern][rune-ring-pattern], [band selector][rune-band-part], [rune selector][rune-inscription-part]

## Ordinary crafting and other ingredient roles

There are exactly **three ordinary recipe JSON files** in the pinned Jewelry tree:

| Ingredients | Result | Role | Pinned recipe |
| --- | --- | --- | --- |
| 2 Copper Ingots (`#c:ingots/copper`) + 4 Planks (`#minecraft:planks`) | 1 Jewelcrafting Station | Construction and Jeweler job site; the planks do not provide a material bonus. | [Station][station-recipe] |
| 1 Book + 1 Copper Ingot (`#c:ingots/copper`) | 1 Jewelcrafting Guide | Guide creation. | [Guide][guide-recipe] |
| 1 existing Jewelcrafting Guide + 1 Book | 2 Jewelcrafting Guides | Guide duplication. | [Duplication][guide-copy-recipe] |

**Emeralds** additionally pay for gemstone, jewelry, and pattern trades; large generated-jewelry prices can use **Emerald Blocks**. **Gold** and **Copper** have buy trades as noted above. **Artisan Scrolls** (`irons_jewelry:recipe`) carry a pattern-unlock reference; they are obtained by the source’s loot/trade system rather than one of the three ordinary crafting recipes. [Trade construction][trades], [trade registration][trade-setup]

There is no fixed Leather, String, Paper, Lapis, Deepslate, or Crying Obsidian input in those three Jewelry recipes. Jewelry’s material assemblies still accept Lapis through its gem definition. This is a scoped statement about the pinned Jewelry recipes, not about Spellbooks or all modded recipes; Spellbooks’ much broader ingredient inventory is separate.

## Source quality: upstream fact, explicitly excluded from Vestige’s power model

In Jewelry, one designated part may supply the source quality, multiplied by the pattern’s and individual bonus’s quality multipliers. Without a designated quality part the source factor is 1. Attribute base amounts are multiplied by this effective quality; actions/timers use their authored scalar functions. An action’s result at effective quality 1 is its base value; a nonzero slope changes it at other qualities. Thus the table’s nominal values are enough to show material identity, but not enough to calculate final gear strength without its pattern. [Resolution][jewelry-data], [attribute scaling][attribute-bonus], [scalar evaluation][quality-scalar]

For example, in the pinned Gemset Ring an **Iron band + Amethyst gem** selects Armor Pierce from Amethyst, while its strength uses the band’s Iron source quality. An **Iron Tearstone Ring + Amethyst gem** selects Poison immunity instead. This shows why “Amethyst means Poison” is misleading without the selected pattern channel. [Gemset Ring][gemset_ring], [Tearstone Ring][tearstone_ring], [Iron][iron], [Amethyst][amethyst]

Vestige can use those distinct ingredient associations as inspiration and support installed Iron ingredients through authored alternatives. The accepted design does **not** use more expensive materials as a universal quality/power multiplier, inherit Jewelry’s fixed assemblies, or treat these parameters as automatic spell-trait semantics. See [the accepted material design](../design/material-crafting.md).

## Completeness and limitations

- Every ingredient-bearing pinned material definition is present: **8 vanilla + 7 Jewelry gemstones + 7 compatibility metals + 15 Spellbooks integration materials = 37**. All four channels are shown for every material, with missing entries made explicit.
- The generated `irons_jewelry:example` definition has quality 1, no ingredient, and an empty parameter map. It is a template and is intentionally excluded from usable materials. [Template][example-material]
- All **15 Jewelry patterns**, **1 Spellbooks pattern**, and **3 ordinary Jewelry recipe files** are represented. Pattern material counts come from JSON; allowed ingredient families come from source parts/material groups.
- Availability is based on the pinned material selectors and verified acquisition data. Optional-provider metal production, changes made by modpacks/datapacks, and versions other than the pins are not audited here.
- This is a source/data inventory. No upstream Jewelry gameplay session was run, and no claim of client appearance or final gear balance is made. Apparent discrepancies in the current public wiki (Brass amount and Topaz target) are resolved in favor of the pin. The [official material index](https://ironsjewelry.wiki/materials/) is a readable overview, but it omits several integration definitions.

[allthemodium]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/generated/resources/data/irons_jewelry/irons_jewelry/material/allthemodium.json
[amethyst]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/generated/resources/data/irons_jewelry/irons_jewelry/material/amethyst.json
[brass]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/generated/resources/data/irons_jewelry/irons_jewelry/material/brass.json
[bronze]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/generated/resources/data/irons_jewelry/irons_jewelry/material/bronze.json
[copper]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/generated/resources/data/irons_jewelry/irons_jewelry/material/copper.json
[diamond]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/generated/resources/data/irons_jewelry/irons_jewelry/material/diamond.json
[emerald]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/generated/resources/data/irons_jewelry/irons_jewelry/material/emerald.json
[garnet]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/generated/resources/data/irons_jewelry/irons_jewelry/material/garnet.json
[gold]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/generated/resources/data/irons_jewelry/irons_jewelry/material/gold.json
[iron]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/generated/resources/data/irons_jewelry/irons_jewelry/material/iron.json
[lapis]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/generated/resources/data/irons_jewelry/irons_jewelry/material/lapis.json
[moonstone]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/generated/resources/data/irons_jewelry/irons_jewelry/material/moonstone.json
[netherite]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/generated/resources/data/irons_jewelry/irons_jewelry/material/netherite.json
[onyx]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/generated/resources/data/irons_jewelry/irons_jewelry/material/onyx.json
[peridot]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/generated/resources/data/irons_jewelry/irons_jewelry/material/peridot.json
[platinum]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/generated/resources/data/irons_jewelry/irons_jewelry/material/platinum.json
[ruby]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/generated/resources/data/irons_jewelry/irons_jewelry/material/ruby.json
[sapphire]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/generated/resources/data/irons_jewelry/irons_jewelry/material/sapphire.json
[silver]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/generated/resources/data/irons_jewelry/irons_jewelry/material/silver.json
[topaz]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/generated/resources/data/irons_jewelry/irons_jewelry/material/topaz.json
[unobtainium]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/generated/resources/data/irons_jewelry/irons_jewelry/material/unobtainium.json
[vibranium]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/generated/resources/data/irons_jewelry/irons_jewelry/material/vibranium.json
[arcane]: https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/resources/data/irons_spellbooks/irons_jewelry/material/arcane.json
[divine_pearl]: https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/resources/data/irons_spellbooks/irons_jewelry/material/divine_pearl.json
[mithril]: https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/resources/data/irons_spellbooks/irons_jewelry/material/mithril.json
[pyrium]: https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/resources/data/irons_spellbooks/irons_jewelry/material/pyrium.json
[rune_arcane]: https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/resources/data/irons_spellbooks/irons_jewelry/material/rune_arcane.json
[rune_blood]: https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/resources/data/irons_spellbooks/irons_jewelry/material/rune_blood.json
[rune_cooldown]: https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/resources/data/irons_spellbooks/irons_jewelry/material/rune_cooldown.json
[rune_ender]: https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/resources/data/irons_spellbooks/irons_jewelry/material/rune_ender.json
[rune_evocation]: https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/resources/data/irons_spellbooks/irons_jewelry/material/rune_evocation.json
[rune_fire]: https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/resources/data/irons_spellbooks/irons_jewelry/material/rune_fire.json
[rune_holy]: https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/resources/data/irons_spellbooks/irons_jewelry/material/rune_holy.json
[rune_ice]: https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/resources/data/irons_spellbooks/irons_jewelry/material/rune_ice.json
[rune_lightning]: https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/resources/data/irons_spellbooks/irons_jewelry/material/rune_lightning.json
[rune_nature]: https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/resources/data/irons_spellbooks/irons_jewelry/material/rune_nature.json
[rune_protection]: https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/resources/data/irons_spellbooks/irons_jewelry/material/rune_protection.json
[simple_band]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/generated/resources/data/irons_jewelry/irons_jewelry/pattern/simple_band.json
[simple_chain]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/generated/resources/data/irons_jewelry/irons_jewelry/pattern/simple_chain.json
[gemset_ring]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/generated/resources/data/irons_jewelry/irons_jewelry/pattern/gemset_ring.json
[simple_amulet]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/generated/resources/data/irons_jewelry/irons_jewelry/pattern/simple_amulet.json
[improved_gemset_ring]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/generated/resources/data/irons_jewelry/irons_jewelry/pattern/improved_gemset_ring.json
[superior_gemset_ring]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/generated/resources/data/irons_jewelry/irons_jewelry/pattern/superior_gemset_ring.json
[rhinestone_amulet]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/generated/resources/data/irons_jewelry/irons_jewelry/pattern/rhinestone_amulet.json
[amulet_of_protection]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/generated/resources/data/irons_jewelry/irons_jewelry/pattern/amulet_of_protection.json
[stalwart_ring]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/generated/resources/data/irons_jewelry/irons_jewelry/pattern/stalwart_ring.json
[sharpshooter_loop]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/generated/resources/data/irons_jewelry/irons_jewelry/pattern/sharpshooter_loop.json
[bane_ring]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/generated/resources/data/irons_jewelry/irons_jewelry/pattern/bane_ring.json
[tearstone_ring]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/generated/resources/data/irons_jewelry/irons_jewelry/pattern/tearstone_ring.json
[haggler_ring]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/generated/resources/data/irons_jewelry/irons_jewelry/pattern/haggler_ring.json
[piglin_signet_ring]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/generated/resources/data/irons_jewelry/irons_jewelry/pattern/piglin_signet_ring.json
[barbed_band]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/generated/resources/data/irons_jewelry/irons_jewelry/pattern/barbed_band.json
[jewelry-data]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/main/java/io/redspace/ironsjewelry/core/data/JewelryData.java
[material-generator]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/main/java/io/redspace/ironsjewelry/datagen/JewelryDataRegistryGenerator.java
[bonus-events]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/main/java/io/redspace/ironsjewelry/event/JewelryBonusEvents.java
[positive-effect-bonus]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/main/java/io/redspace/ironsjewelry/core/bonuses/EffectOnHitBonusType.java
[immunity-bonus]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/main/java/io/redspace/ironsjewelry/core/bonuses/EffectImmunityBonusType.java
[metal-tag]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/main/resources/data/irons_jewelry/tags/irons_jewelry/material/metal.json
[gem-tag]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/main/resources/data/irons_jewelry/tags/irons_jewelry/material/gem.json
[gold-tag]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/main/resources/data/irons_jewelry/tags/irons_jewelry/material/gold.json
[emerald-tag]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/main/resources/data/irons_jewelry/tags/irons_jewelry/material/emerald.json
[knockback-action]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/main/java/io/redspace/ironsjewelry/core/actions/KnockbackAction.java
[explode-action]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/main/java/io/redspace/ironsjewelry/core/actions/ExplodeAction.java
[freeze-action]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/main/java/io/redspace/ironsjewelry/core/actions/ApplyFreezeAction.java
[lootable-gems]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/main/resources/data/irons_jewelry/tags/item/lootable_gems.json
[generic-gem-injection]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/main/resources/data/irons_jewelry/loot_modifiers/chest_loot/gems/vanilla_generic_gems.json
[wealthy-gem-injection]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/main/resources/data/irons_jewelry/loot_modifiers/chest_loot/gems/vanilla_wealthy_gems.json
[generic-gem-pool]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/main/resources/data/irons_jewelry/loot_table/modifiers/generic_gem_loot.json
[wealthy-gem-pool]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/main/resources/data/irons_jewelry/loot_table/modifiers/wealthy_gem_loot.json
[village-jeweler-loot]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/main/resources/data/irons_jewelry/loot_table/chests/village/village_jeweler.json
[compat-gem-injection]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/main/resources/data/irons_jewelry/loot_modifiers/chest_loot/gems/compat_generic_gems.json
[compat-wealthy-gem-injection]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/main/resources/data/irons_jewelry/loot_modifiers/chest_loot/gems/compat_wealthy_gems.json
[trade-setup]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/main/java/io/redspace/ironsjewelry/event/SetupEvents.java
[trades]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/main/java/io/redspace/ironsjewelry/utils/Trades.java
[sellable-gems]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/main/resources/data/irons_jewelry/tags/item/jeweler_sellable_gems.json
[buyable-gems]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/main/resources/data/irons_jewelry/tags/item/jeweler_buyable_gems.json
[villager-registry]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/main/java/io/redspace/ironsjewelry/registry/VillagerRegistry.java
[pattern-definition]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/main/java/io/redspace/ironsjewelry/core/data/PatternDefinition.java
[station-recipe]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/main/resources/data/irons_jewelry/recipe/jewelcrafting_station.json
[guide-recipe]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/main/resources/data/irons_jewelry/recipe/jewelcrafting_guide.json
[guide-copy-recipe]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/main/resources/data/irons_jewelry/recipe/jewelcrafting_guide_duplicate.json
[attribute-bonus]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/main/java/io/redspace/ironsjewelry/core/bonuses/AttributeBonusType.java
[quality-scalar]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/main/java/io/redspace/ironsjewelry/core/data/QualityScalar.java
[example-material]: https://github.com/iron431/irons-jewelry/blob/bd3fae5579376472c04d9090e42a7a932d604333/src/generated/resources/data/irons_jewelry/irons_jewelry/material/example.json
[rune-ring-pattern]: https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/resources/data/irons_spellbooks/irons_jewelry/pattern/rune_inscribed_ring.json
[rune-band-part]: https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/resources/data/irons_spellbooks/irons_jewelry/part/band_rune_inscribed.json
[rune-inscription-part]: https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/resources/data/irons_spellbooks/irons_jewelry/part/rune_inscription.json
