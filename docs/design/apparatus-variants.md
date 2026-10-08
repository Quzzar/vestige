# Apparatus variants and embedded imbuements

**Palette accepted October 5, 2026; construction and names updated October 7.** Plinth and Spellstone each support all **36 Minecraft 1.21.1 stone and masonry stair materials**, excluding wood, bamboo mosaic and Copper. Every selected full block has a matching craftable slab, confirmed against the [36 pinned vanilla slab recipes](../research/vanilla-apparatus-slabs-1.21.1.json). These are cosmetic finishes, sharing identical ritual behavior, hardness, offering capacity and saved state.

The [canonical manifest](apparatus-materials.json) supplies block/slab ingredients and texture mappings. The [pinned vanilla inventory](../research/vanilla-stair-materials-1.21.1.json) records source stair recipes and model textures. Raw End Stone and raw Deepslate are superseded by End Stone Bricks and worked Deepslate finishes. Earlier wall-based and fourteen-material palettes remain historical decisions.

## Construction

Use the same material's full block (`B`) and slab (`S`) throughout each recipe. `D` is Diamond and `A` is Amethyst Block. Empty slots remain empty.

```text
Spellstone (1)    Plinths (2)
D S D            S S S
S A S            . B .
S . S            S S S
```

The owner's October 7 grids make one Spellstone from five matching slabs, two Diamonds and one Amethyst Block, and two Plinths from one full block and six matching slabs. Recipes are exact shaped recipes, with individual recipe-book unlocks. Mixed finishes do not match. The October 5 construction grids are superseded.

When Supplementaries is installed, the native Stone Bricks Plinth recipe and its unlock are skipped. Its unchanged pedestal recipe produces two Supplementaries pedestals, each convertible into one Stone Bricks Plinth through the existing optional recipe. The other 35 Plinth finishes and all 36 Spellstone recipes stay native. Iron's optional pedestal conversion remains available independently. [Material crafting](material-crafting.md#optional-pedestal-conversions) records the integrations.

| Finish | Full block | Slab |
| --- | --- | --- |
| Stone | `minecraft:stone` | `minecraft:stone_slab` |
| Cobblestone | `minecraft:cobblestone` | `minecraft:cobblestone_slab` |
| Mossy Cobblestone | `minecraft:mossy_cobblestone` | `minecraft:mossy_cobblestone_slab` |
| Stone Bricks | `minecraft:stone_bricks` | `minecraft:stone_brick_slab` |
| Mossy Stone Bricks | `minecraft:mossy_stone_bricks` | `minecraft:mossy_stone_brick_slab` |
| Granite | `minecraft:granite` | `minecraft:granite_slab` |
| Polished Granite | `minecraft:polished_granite` | `minecraft:polished_granite_slab` |
| Diorite | `minecraft:diorite` | `minecraft:diorite_slab` |
| Polished Diorite | `minecraft:polished_diorite` | `minecraft:polished_diorite_slab` |
| Andesite | `minecraft:andesite` | `minecraft:andesite_slab` |
| Polished Andesite | `minecraft:polished_andesite` | `minecraft:polished_andesite_slab` |
| Sandstone | `minecraft:sandstone` | `minecraft:sandstone_slab` |
| Smooth Sandstone | `minecraft:smooth_sandstone` | `minecraft:smooth_sandstone_slab` |
| Red Sandstone | `minecraft:red_sandstone` | `minecraft:red_sandstone_slab` |
| Smooth Red Sandstone | `minecraft:smooth_red_sandstone` | `minecraft:smooth_red_sandstone_slab` |
| Bricks | `minecraft:bricks` | `minecraft:brick_slab` |
| Mud Bricks | `minecraft:mud_bricks` | `minecraft:mud_brick_slab` |
| Quartz | `minecraft:quartz_block` | `minecraft:quartz_slab` |
| Smooth Quartz | `minecraft:smooth_quartz` | `minecraft:smooth_quartz_slab` |
| Prismarine | `minecraft:prismarine` | `minecraft:prismarine_slab` |
| Prismarine Bricks | `minecraft:prismarine_bricks` | `minecraft:prismarine_brick_slab` |
| Dark Prismarine | `minecraft:dark_prismarine` | `minecraft:dark_prismarine_slab` |
| Nether Bricks | `minecraft:nether_bricks` | `minecraft:nether_brick_slab` |
| Red Nether Bricks | `minecraft:red_nether_bricks` | `minecraft:red_nether_brick_slab` |
| Blackstone | `minecraft:blackstone` | `minecraft:blackstone_slab` |
| Polished Blackstone | `minecraft:polished_blackstone` | `minecraft:polished_blackstone_slab` |
| Polished Blackstone Bricks | `minecraft:polished_blackstone_bricks` | `minecraft:polished_blackstone_brick_slab` |
| Cobbled Deepslate | `minecraft:cobbled_deepslate` | `minecraft:cobbled_deepslate_slab` |
| Polished Deepslate | `minecraft:polished_deepslate` | `minecraft:polished_deepslate_slab` |
| Deepslate Bricks | `minecraft:deepslate_bricks` | `minecraft:deepslate_brick_slab` |
| Deepslate Tiles | `minecraft:deepslate_tiles` | `minecraft:deepslate_tile_slab` |
| Tuff | `minecraft:tuff` | `minecraft:tuff_slab` |
| Polished Tuff | `minecraft:polished_tuff` | `minecraft:polished_tuff_slab` |
| Tuff Bricks | `minecraft:tuff_bricks` | `minecraft:tuff_brick_slab` |
| End Stone Bricks | `minecraft:end_stone_bricks` | `minecraft:end_stone_brick_slab` |
| Purpur | `minecraft:purpur_block` | `minecraft:purpur_slab` |

## Carrier appearance, imbuement and offering

All Plinth appearances can occupy either active layer, and different finishes can be mixed in one structure. All Spellstones share center activation and reference/result collection. Construction ingredients do not grant magical properties.

| Input | What it determines |
| --- | --- |
| Carrier appearance | Body texture, decorative trim, construction recipe and returned apparatus item |
| Embedded imbuement | The independently installed block material and its authored local offering interaction |
| Offering | The actual recipe ingredient on the surface |
| Active geometry | Leyline shaping and the physical part of attunement identity |

A Quartz Plinth is not implicitly imbued with Quartz. A Tuff Spellstone's Amethyst/Diamond construction ingredients do not enter its ritual outputs. Replacing any carrier finish with another leaves matching, Spellshaping and attunement unchanged when offerings, sockets and geometry stay the same. Supporting terrain is decorative. [Material crafting](material-crafting.md) and its pinned Iron references govern authored offering/socket interactions.

## Shared model and runtime

All 36 Plinth finishes connect vertically, including mixed-material columns. Connected states are `part=base`, `part=shaft` and `part=cap`; isolated blocks use `part=single`. Only a clear standalone/cap surface supplies an active recipe node. The owner’s [simple Plinth revision](../art/apparatus-simple-plinth/README.md) uses three solid cuboids in standalone form, two in base/cap and one in a shaft, plus four flat socket plaques on every form. Sockets are consistently centered at block-local y8/16, retain independent materials and remain usable on covered supports. The earlier detailed 51-element and bar-refined 45-element cap are historical alternatives.

The original [review package](../art/apparatus-concepts/README.md) supplies the vanilla finish associations; `tools/apparatus_columns.py` authors the simple Plinth, connected forms and [compact thick three-piece Spellstone](../art/spellstone-tent-table/README.md). Plinth foot/cap width is 12/16, shaft width 10/16, and flat physical/receiving height 14/16. Spellstone has two inward-leaning supports 3.5 model units thick and one smaller 12×12 top stone 3.25 units thick. Its physical/receiving top is y8/16, with the native Astral Seal aligned above it. Four isolated 32×32 cut gemstone sprites sit on the cap’s vertical rim faces, one per side, with bare stone between them and on the top. Quarter-model-unit collision slices follow the tilted supports and retain the opening. Flat texture/socket faces add no collision. Dimensions and behavior are shared by every finish.

One Plinth socket is rendered through four side windows centered on the reviewed panel locations. Installing or recovering it never modifies the offering. All variants share the same offering block entity and save/update schema. Plinth roles are recognized explicitly, independently of registry name, texture and height.

Sockets accept only the 26 material families named by the trusted [Spellshaping rules](spellshaping.md), including compound-only materials. All 16 wool colors share one imbuement identity; all 16 concrete colors share another. Color changes neither shaping nor attunement, while the socket still renders and returns the actual installed block. Arbitrary blocks, carpets and concrete powder reject without consuming the held stack or placing an offering. A block may still be used as a top ingredient regardless of socket eligibility. All cosmetic finishes apply the same restriction.

Spellstone uses the accepted lowered native Astral Seal: an interlocked glyph, counter-rotating broken rings and faint anchors, hovering 0.045 block above the receiving surface with restrained breathing. Native luminous strokes need no shader pack and do not change world light levels. Inventory models show only the stone table; the placed block animates the seal. Reference/result scrolls rest flat and naturally obscure the central glyph. Correct placements glow, wrong placements shake sideways, and only ingredients rise on successful crafting.

Stone Bricks retain the baseline IDs `vestige:spellstone` and `vestige:plinth`. Other finishes use `vestige:<material>_spellstone` and `vestige:<material>_plinth`, for example `vestige:tuff_spellstone` and `vestige:tuff_plinth`. There are two functional roles and 72 cosmetic block/item registrations. The old Stone/Runic Pedestal IDs remain removed; no migration aliases are introduced.

## Authoring and verification

`python3 tools/author_apparatus_models.py` packages the approved models, purple glyph, role shapes, material enum, construction recipes, self loot, pickaxe tags and recipe unlocks. `--check` detects drift. The review author remains `docs/art/apparatus-concepts/author_models.py`.

[World tests](../../src/main/java/com/quzzar/vestige/apparatus/ApparatusTest.java) exercise available native recipes and their counts, mismatched slab and superseded-grid rejection, placement, collision, mining drops, offering interactions, independent socket/result storage, persistence and client synchronization. [Leyline tests](../../src/main/java/com/quzzar/vestige/apparatus/LeylineTest.java) exercise mixed-finish crafting, unchanged shaping and identical attunement keys with retained sockets. Current actual client evidence and verification results are recorded in [apparatus models](apparatus-models.md) and [development status](../development-status.md).
