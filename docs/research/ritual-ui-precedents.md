# Ritual UI precedents

**Researched October 6, 2026.** The owner prefers archaeological concept A, without corner symbols, writing or decorative scenery, and proposed Antique Atlas as a visual reference shared with Standing Stones. This document records the inspected UI evidence separately from runtime compatibility and final art approval.

## Antique Atlas

The [owner-linked original project](https://www.curseforge.com/minecraft/mc-mods/antique-atlas) publishes a [Minecraft 1.21.1 NeoForge 8.0.1 release](https://www.curseforge.com/minecraft/mc-mods/antique-atlas/files/6849155). The listing points at the older AntiqueAtlasTeam repository and credits Stereowalker for the port. Its gallery includes exported map artwork rather than a reliable current full-screen client capture.

Inspected the complete Stereowalker source archive at revision [`5b8740d17576872edee8d019d595ecb4fca07b46`](https://github.com/Stereowalker/AntiqueAtlas/tree/5b8740d17576872edee8d019d595ecb4fca07b46). That source identifies a Forge build of 8.0.1; it does not prove exact parity with the published NeoForge jar. The following files establish how the inspected UI is constructed:

- [`GuiComponent.java`](https://github.com/Stereowalker/AntiqueAtlas/blob/5b8740d17576872edee8d019d595ecb4fca07b46/src/main/java/hunternif/mc/impl/atlas/client/gui/core/GuiComponent.java) extends Minecraft's `Screen` and implements Atlas's own child-component hierarchy.
- [`GuiAtlas.java`](https://github.com/Stereowalker/AntiqueAtlas/blob/5b8740d17576872edee8d019d595ecb4fca07b46/src/main/java/hunternif/mc/impl/atlas/client/gui/GuiAtlas.java) draws the book, map content, book-frame overlays, markers and controls in layers.
- [`Textures.java`](https://github.com/Stereowalker/AntiqueAtlas/blob/5b8740d17576872edee8d019d595ecb4fca07b46/src/main/java/hunternif/mc/impl/atlas/client/Textures.java) points to Atlas-owned book, frame, bookmark, scrollbar and icon textures. The book's logical dimensions are 310 × 218.
- [`GuiBookmarkButton.java`](https://github.com/Stereowalker/AntiqueAtlas/blob/5b8740d17576872edee8d019d595ecb4fca07b46/src/main/java/hunternif/mc/impl/atlas/client/gui/GuiBookmarkButton.java) draws its controls from the Atlas bookmark sheet.
- [`build.gradle`](https://github.com/Stereowalker/AntiqueAtlas/blob/5b8740d17576872edee8d019d595ecb4fca07b46/build.gradle) declares UnionLib, and the source properties pin 12.0.18. This dependency does not supply the inspected parchment textures or their Atlas-owned widgets. No standalone reusable Atlas-themed UI library was verified.

Viewed the actual book and bookmark assets, as well as the project's exported map artwork. The useful visual pattern is quiet pale parchment, dark brown ink, restrained wear, crisp pixel edges and small controls along the outer edge. The paper is much simpler than the first generated A concept. For recipe displays, retain the host JEI/EMI chrome, native item icons and native font, with the ink diagram as the distinctive content. For Standing Stones, the same paper/ink palette can be shared while the picker keeps its own selection and travel logic. This is a design recommendation, not an approved replacement for the other agent's current screen.

The map API exposes map/marker functions rather than a general menu theme. See [Standing Stone map research](standing-stone-maps.md) for API and loader limitations. No Atlas code, textures or runtime dependencies have been incorporated into Vestige's distributed mod during this pass. The browser comparison loads reference images directly from their authors' sources.

## Other references

[Patchouli's official getting-started screenshot](https://vazkiimods.github.io/Patchouli/docs/patchouli-basics/getting-started/) shows an actual book menu with pale pages, Minecraft icons, game text and restrained controls. Its [book-format reference](https://vazkiimods.github.io/Patchouli/docs/reference/book-json/) documents book and crafting textures. Patchouli is a book/documentation framework; it is not necessary to host a JEI/EMI ritual category. This provides precedent that a parchment treatment can fit a modded Minecraft interface.

Minecraft's [official brush article](https://www.minecraft.net/en-us/article/brush) describes archaeology introduced in Trails & Tales as brushing suspicious blocks in the world. Its brush recipe uses the ordinary crafting screen, which was inspected as a visual baseline. Archaeology itself does not provide an archaeological recipe-menu theme for this design.

Create's [recipe category source](https://github.com/Creators-of-Create/Create/blob/mc1.21.1/dev/src/main/java/com/simibubi/create/compat/jei/category/ProcessingViaFanCategory.java) uses native JEI ingredient/output slots alongside a rendered machine, shadows and arrows. The inspected source supports keeping the recipe viewer's familiar interactions while expressing the crafting process in the category drawing. This pass inspected its source; it did not capture Create's current client screen.

## Revised A study

`docs/previews/ritual-image-concepts/archaeological-clean-v2.png` is a newly generated two-panel image asset. It removes the corner symbols, handwriting, ruined landscapes and central decorative glyph; retains four/eight item landing spots, inward arrows and a rotation arc; and uses quieter paper with clearer brown linework. A faint lavender rim distinguishes the result recess.

The owner approved the revised parchment style, then corrected its eight-slot arrangement: four inner Plinths at the cardinal positions and four outer Plinths at the corners. The original eight-seat single-ring studies are superseded. Both layers should have faint circular guides with small neutral ticks instead of directional rotation arrows, with distinct inward arrows toward the Spellstone. These visual guides do not change recipe matching or permit independent rotation of the two layers.

The browser preview now shows the four/eight arrangements with separate Minecraft/Vestige item textures, plus Atlas, Patchouli and vanilla references. The revised square diagram is 150 × 150 inside a 184 × 150 recipe area, leaving native grey margins. This is a smaller layout study intended to fit narrow native viewers. Actual JEI/EMI rendering with these image assets has not yet been verified; approval of the visual direction does not establish native rendering verification.

The subsequent alignment pass separates the generated parchment and transparent stone illustrations from precise UI geometry. The owner requested a few subtle arrow-like bends on the ticks and exact circular alignment with the slot centers. Shared coordinates now place every illustration/item and define the guide radii. The [current prototype record](../previews/ritual-image-concepts/README.md) distinguishes its audited offline export from the final browser inspection, which was blocked at the existing error-page tab. The current built-in generation/extraction prompts are in `docs/previews/ritual-image-concepts/v7-prompts.md`.

The Standing Stones agent was sent the source location and these palette/widget findings. Earlier prompts retain the visual exploration history.
