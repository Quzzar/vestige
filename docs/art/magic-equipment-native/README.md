# Native Wardweave and Cinderweave

**Historical October 7 appearance.** The owner requested a hanging robe on October 8; [the new visual review](../magic-equipment-hanging/README.md) supersedes this short chest-armor silhouette. These captures and hashes remain evidence of the earlier implementation. The current worn exporter targets the new 64×64 atlas; it no longer reproduces this historical worn texture.

October 7, 2026. The owner approved the two gameplay packages. Wardweave follows the preferred Folded Mantle direction. Cinderweave uses **Coal Cuffs provisionally**; its A/B art choice and final worn appearance remain available for review. The other four clothing families remain proposals.

These are the registered production items, rendered by Minecraft 1.21.1 / NeoForge 21.1.72 beside vanilla armor, wool, String, Honeycomb, Amethyst Shard and Blaze Powder. Each capture also shows the actual armor layer on both WIDE and SLIM player models. No inventory icons or worn appearances were composited into the screenshots.

- [Wardweave, blue, native GUI scale 3](captures/wardweave-blue-gui-3.png)
- [Wardweave, white, native GUI scale 2](captures/wardweave-white-gui-2.png)
- [Cinderweave, blue, native GUI scale 3](captures/cinderweave-blue-gui-3.png)
- [Cinderweave, black, native GUI scale 2](captures/cinderweave-black-gui-2.png)

[capture.json](captures/capture.json) records all **64 views**: two robes × sixteen wool colors × two GUI scales. Each view validates the real item fabric tint, fixed trim color, model types and 80 durability. [Asset verification](asset-verification.json) records exact sizes, alpha/palette checks and source asset hashes. White, blue and black examples were visually inspected after capture.

Inventory textures are genuine **16×16** sprites. Worn textures use the vanilla **64×32 chest armor UV layout**, with dyeable fabric and untinted overlays. Their current worn silhouette uses Minecraft's ordinary chest armor geometry; a longer flowing robe mesh is not introduced. Cinderweave cuffs/brass and Wardweave's dark belt retain their colors when dyed. Normal armor dye recipes preserve other item components.

## Sources and reproducible exports

`sources/` preserves four built-in imagegen refinements of the shortlisted designs: inventory sprites and worn cloth atlases. The initial inventory references are the earlier dye-review masters. `vanilla-leather-layer-1.png` is the pinned Minecraft reference used to explain the exact armor UV layout; it is not shipped as a Vestige texture. No Iron code or artwork was copied.

The generated masters require native-resolution export and UV packing. `tools/author_robe_inventory.py` downsamples and separates fabric/trim. `tools/author_robe_armor.py` repacks generated cloth faces into exact vanilla body/arm UV rectangles and separates their trim. Both retain hard pixels and provide `--check` drift verification. `sources/generation-prompts.json` preserves the complete prompts and local reference paths.

Gameplay, recipes, approval and verification are in [the robe specification](../../design/robe-behavior-review.md) and [native verification](../../verification/native-robes-2026-10-07/README.md). The unrelated four-item proposals remain in the existing equipment review. The previous local review page's slider layout remains unverified because browser security blocked it; these native screenshots are separate evidence.
