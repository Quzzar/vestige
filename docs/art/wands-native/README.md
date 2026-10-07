# Native wand and thread art

## October 7 · quieter shading and clear padding

The [padded revision](padded-v2/README.md) supersedes the first wand pixels after native inventory review. All seven body and eight tip layers now have simpler flat colour patches: **at most six opaque colours per body and four per tip**, compared with forty to forty-nine in the previous bodies. Every one of the **63 composed appearances** has a transparent one-pixel border, binary alpha and a connected body/tip silhouette. The five approved thread sprites are unchanged.

The built-in image tool revised the source artwork; its [complete prompt and export provenance](source-art/prompts-v2.json) are saved beside `wand-components-v2.png`. The exporter indexes opaque source colours without dithering and takes the dominant colour in each logical pixel, avoiding isolated shading variations from point sampling. Its checks cover both the individual layers and their rendered unions. Native inventory and full-palette captures are recorded in the revision folder. Earlier images below remain historical comparisons.

## First artwork and gameplay review

The production assets are actual **16×16 RGBA sprites**, with opaque/transparent alpha and ordinary Minecraft item shading. Seven separately generated body silhouettes and eight separately generated tip sprites compose into **63 handheld models**; thread choice adds no wand picture. The five thread components use distinct loops, coils, knots and braids as well as their palettes.

`tools/author_wand_models.py --check` verifies every production pixel and generated model. `source-art/` retains the original generated PNGs and complete prompts. These images were generated with the built-in image tool, true transparent background, then exported by nearest-neighbor format conversion; the large source images are not packaged game textures. No Iron artwork was copied.

`wands/all-63-wands-and-five-threads.png` is an actual Minecraft main-framebuffer capture using the ordinary item renderer. Its manifest verifies the resource-loaded PNG dimensions and hashes, separately from an enlarged art contact sheet. The recipe-viewer folders record native lookup/capture runs; a run is verified only when its complete manifest is present. See [gameplay and publication evidence](../../verification/native-wand-tips-2026-10-07/README.md) for exact builds and limitations.
