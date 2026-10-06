# Native cyan rune over the mirrored pedestal top

Historical eighth draft, superseded by the [ninth revision](../apparatus-v9/README.md). The owner preferred the first finer generated rune rather than this 16-by-16 simplification. The PNG and grid links below refer to archived eighth-draft assets.

October 3, 2026. The owner supplied [Pixelated Cyan Rune Stone Tile](cyan-rune-reference.png) as the new inscription direction. Its stone/background is explicitly excluded: the runes sit on the existing bottom-half mirrored Chiseled Deepslate top. This newer circular cyan reference replaces the previous ink-symbol direction and authorizes the formerly deferred rune texture revision.

The inscription uses a broken circular ring, angular rune marks and a small hollow central diamond, simplified to this draft's **16-by-16** texture grid. The archived [native palette/grid](apparatus-rune-texture.json) contains 95 cyan cells in three colors, with a transparent one-cell outer margin and a centered two-by-two aperture. The archived [RGBA overlay](../../previews/apparatus-v8/source-textures/runic_pedestal_runes.png) is 16-by-16. For this draft's block model, the overlay was baked onto the normal mirrored top as one [opaque 16-by-16 tile](../../previews/apparatus-v8/source-textures/runic_pedestal_top.png). Every non-rune stone pixel remains unchanged. No additional planes, emissive layer or tiny rune geometry were added.

Built-in imagegen provided [the generated source](rune-generated-source.png); [the exact selected generation prompt](rune-generation-prompt.txt) is saved. Area reduction preserves thin strokes at native resolution, followed by a 48/255 alpha cutoff and a three-color cyan palette. The editable native grid retains a two-by-two hollow center from the reference. The generated source is preserved unchanged; the original user reference is preserved unchanged. Discarded generation variants are not runtime assets.

[Current design and previews](../../design/apparatus-models.md) · [Model/texture bundle](apparatus-models.zip).

Runic Pedestal now contains 27 cuboids, down from 570. Its flat cap is one cuboid with the final texture only on its upper face. Stone Pedestal, Spellstone, Amethyst, dimensions, heights, other Runic faces and recipes remain unchanged. The retired 96-cell authoring trace is preserved in the [historical sixth archive](../apparatus-v6/apparatus-runic-reference.json), outside active authoring tools.

The bundle contains the six model files, all three native texture PNGs, the native rune grid, the new user reference, the selected generation prompt, this README and three unchanged inert recipe-authoring files. The large generated source is saved here for provenance and excluded from the portable/runtime bundle. The final top tile includes modified vanilla Chiseled Deepslate texels from Minecraft 1.21.1, by Mojang; other vanilla texture references resolve through Minecraft.

Block/item registration, placement, displayed items, crafting and station operations remain future work. Previews are offline asset renders, not Minecraft screenshots.
