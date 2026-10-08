# Mage armor robe revision

October 8, 2026. **Owner-approved appearance.** After viewing the native Minecraft previews, the owner said, “Let's lock these in and move on.” [The approval record](approval.json) pins the four exact worn textures and two model sources. The owner rejected the cape, withdrew the earlier hanging-back direction and requested this complete magic-armor treatment inspired by Electroblob's Wizardry and Iron's armor textures.

Wardweave has slate shoulder and collar panels, silver-toned folded lapels, a blue ward clasp, dark cuffs, a belt and a divided lower robe. Cinderweave has a coal yoke, brass fastenings, leather belt and ember stitching on its shoulders, cuffs and hem. Both have new chest, back, sleeve and lower-garment textures. The model adds shallow folded shoulders/cuffs and a collar. Two broad five-by-eight-unit lower coat halves overlap at the waist and center. They follow native leg positions with 35% of their stride rotation, ending above the feet and draping more gently than trousers. There is no cape, cape animation or separate hanging back panel. The registered chest-slot items, inventory sprites, recipes and approved gameplay are unchanged.

## In Minecraft

[Interactive color review](index.html) shows all sixteen wool colors. Main sheets show wide/slim players and front, side, back, walking and crouching poses. Screenshots are unedited captures of Minecraft's main render target:

- [Wardweave white](captures/wardweave-white-gui-2.png), [blue](captures/wardweave-blue-gui-2.png), [black](captures/wardweave-black-gui-2.png)
- [Cinderweave white](captures/cinderweave-white-gui-2.png), [blue](captures/cinderweave-blue-gui-2.png), [black](captures/cinderweave-black-gui-2.png)
- [Wardweave over iron leggings/boots](captures/wardweave-white-armor-layers.png), [Cinderweave over iron leggings/boots](captures/cinderweave-white-armor-layers.png)

[Capture metadata](captures/capture.json) and [verification](../../verification/armor-robes-2026-10-08/README.md) record the 66 native inventory/pose/layering views and exact production, loaded-resource and packaged hashes. The final art client uses Minecraft, NeoForge and Vestige without optional Kithkyn. Ordinary third-person world capture timed out in the preceding overloaded run and is not claimed for this final fit. Source studies and exported-pixel boards are labeled separately from game evidence. Visual approval belongs to the owner.

## Artwork and model

The **built-in image generation tool** created the two new [source studies](source/) using the actual upstream armor atlases as style references. [Full prompts](prompts.json) are retained. The unmodified study references are confined to this documentation folder; production artwork is independently generated and exported, not copied from upstream pixels. [Pinned reference sources](references/sources.json) record repository revisions and asset paths. Vanilla Minecraft 1.21.1 leather and iron references establish physical UV density and dye separation.

`tools/author_robe_armor.py` deterministically samples the face studies at native density, quantizes the palette, packs the cuboid UVs and separates neutral cloth from fixed trim. The 64×64 RGBA atlases have hard alpha and one logical texel per model unit. Wide sleeves are four pixels across; narrow sleeves use an independent three-pixel UV net. Cuffs, shoulders, collar and lower coat parts each have appropriately sized face UVs. Pure neutral cloth shades receive Minecraft's dye tint, while the clasp, belt and colored trim stay fixed.

[Exported pixel inspection](exported-pixels.png) enlarges the exact native face/atlas pixels. The [four frozen exports](export/) and [model/exporter snapshots](source/) preserve this revision independently of future changes. The `*-atlas-8x.png` boards show those same pixels on light and dark backgrounds. [The rejected cape revision](../magic-equipment-mantles/README.md) remains historical evidence.
