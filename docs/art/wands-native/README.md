# Native wand and thread art

The production assets are actual **16×16 RGBA sprites**, with opaque/transparent alpha and ordinary Minecraft item shading. Seven separately generated body silhouettes and eight separately generated tip sprites compose into **63 handheld models**; thread choice adds no wand picture. The five thread components use distinct loops, coils, knots and braids as well as their palettes.

`tools/author_wand_models.py --check` verifies every production pixel and generated model. `source-art/` retains the original generated PNGs and complete prompts. These images were generated with the built-in image tool, true transparent background, then exported by nearest-neighbor format conversion; the large source images are not packaged game textures. No Iron artwork was copied.

`wands/all-63-wands-and-five-threads.png` is an actual Minecraft main-framebuffer capture using the ordinary item renderer. Its manifest verifies the resource-loaded PNG dimensions and hashes, separately from an enlarged art contact sheet. The recipe-viewer folders record native lookup/capture runs; a run is verified only when its complete manifest is present. See [gameplay and publication evidence](../../verification/native-wand-tips-2026-10-07/README.md) for exact builds and limitations.
