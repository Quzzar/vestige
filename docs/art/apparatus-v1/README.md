# Apparatus authoring references

Owner-provided recipe screenshots, October 3, 2026, preserved unchanged. The [material design](../../design/material-crafting.md#construction-recipes) is the authoritative explanation of the selected grids and costs; the [model brief](../../design/apparatus-models.md) describes the first native asset pass.

| Block | Original screenshot | Shaped recipe authoring JSON |
|---|---|---|
| Spellstone | [Screenshot](spellstone-recipe.png) | [Recipe](recipes/spellstone.json) |
| Stone Pedestal | [Screenshot](stone-pedestal-recipe.png) | [Recipe](recipes/stone_pedestal.json) |
| Runic Pedestal | [Screenshot](runic-pedestal-recipe.png) | [Recipe](recipes/runic_pedestal.json) |

Recipe files use Minecraft 1.21.1's shaped recipe format and the selected vanilla item IDs, with proposed output IDs `vestige:spellstone`, `vestige:stone_pedestal` and `vestige:runic_pedestal`. Recorded output is one block each; screenshots do not show output counts. The blank first row of each pedestal screenshot is trimmed in the JSON as ordinary shaped-recipe matching permits vertical translation.

These are inert authoring files. Keep them outside `src/main/resources/data/` until the output blocks/items are registered, then wire and verify actual crafting. The Runic Pedestal recipe directly consumes three Chiseled Deepslate, a Diamond Block and two shards, rather than upgrading a Stone Pedestal. The [earlier palette reference](earlier-palette-reference.png) predates the selected grids and is historical only.
