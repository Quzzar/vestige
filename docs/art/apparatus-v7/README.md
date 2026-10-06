# Mirrored pedestal tops and native Amethyst texture

October 3, 2026. The owner requested the full bottom half of Chiseled Deepslate mirrored upward, eliminating the original top-left/right notches. Both pedestal tops now use that UV mapping. The older tiny centered top treatment is superseded. Pedestal proportions, flat heights, recipes and Spellstone remain unchanged.

Fitted Amethyst now uses a real **16-by-16 RGB PNG**, mapped into one shallow rectangular socket per side. This replaces the old 64-cell facet geometry and flat vanilla-color swatches. The full texture fits U 6..10/V 6..9 of the source carving, with the same 1¼-unit socket height on both pedestal heights.

- [Archived Amethyst texture](../apparatus-v12/retired-resources/pedestal_amethyst.png).
- [Generated source](amethyst-generated-source.png), preserved unchanged.
- [Exact generation prompt](amethyst-generation-prompt.txt).
- Method: built-in imagegen, using the [owner's purple gemstone](../apparatus-v6/amethyst-gem-reference.png) as a shape/color reference. Its output was reduced by nearest-neighbor sampling to the requested 16-by-16 runtime size. Enlarged previews also use nearest-neighbor sampling; they do not add texture detail.

The rune should eventually be a **16-by-16 texture overlay on the normal mirrored pedestal top**. Its redesign is explicitly deferred. Every existing rune element and UV remains identical to the sixth model draft; the frozen 96-cell reference trace is carried over temporarily. Do not treat that fine geometry as the accepted final rune implementation or replace the supplied reference with an invented symbol.

[Current design and previews](../../design/apparatus-models.md) · [Model/texture bundle](apparatus-models.zip).

The bundle contains three block models, their three inventory parents, the native Amethyst texture, three unchanged inert recipe-authoring files, this README, the generation prompt, both owner's reference screenshots and the unchanged rune trace. Vanilla textures resolve through Minecraft and are not bundled. The large generated source is retained here for provenance, outside the runtime and portable bundle. Native block/item registration, placement, displayed items, crafting and station operations remain future work. Previews are offline asset renders.
