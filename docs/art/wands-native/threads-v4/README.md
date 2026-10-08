# Magical threads · softer shadow runs

Local art review, not installed or published. This refines the preceding native String shading treatment after the owner found its outlines too rigid and prominent. The generated edit keeps the loose arrangements and pale strands, lightens the deepest shadows modestly, and introduces an intermediate shadow along selected stretches. Four coherent shades replace the three-shade ramps; no blur or soft transparency is used.

- [Actual native inventory comparison beside String and Lead](inventory/threads-beside-string-and-lead.png)
- [Native inventory with wands](inventory/wands-and-threads.png)
- [Native inventory beside vanilla materials](inventory/beside-vanilla-materials.png)
- [Previous exports above, refined exports below; vanilla String at right](v3-above-v4-below-with-vanilla.png)
- [Generated edit source](../source-art/magical-threads-v4.png) and [complete built-in image prompt](../source-art/prompts-threads-v4.json)
- [Verification manifest](verification.json)

The generated sheet is larger reference artwork. Only the five exported 16×16 RGBA sprites ship as item textures, with binary alpha, connected strands and a transparent one-pixel border. The exporter fits each crop uniformly within fourteen occupied pixels and selects nondithered logical-cell colours. The fifteen restored v4 wand body/tip PNGs remain byte-identical. Models and production gameplay are unchanged.

Three unedited Minecraft 1.21.1 / NeoForge 21.1.72 framebuffer captures use the vanilla survival inventory renderer in a fresh review world. The cursor is kept over the avatar and notifications cleared for comparison. Native-loaded and packaged hashes cover all twenty textures. Opaque texel centers are compared against production PNGs with the same small rendering tolerance as the vanilla String control. The pinned vanilla style reference and its provenance remain in [the preceding review](../threads-v3/vanilla-reference.json); they are not packaged Vestige textures.

The retained capture source is review-only and is removed from production Java before packaging. Export checks cover all five threads and all 63 composed wand appearances. The isolated build verifies packaging; no world behavior suite is repeated for this art-only change. The installed testing jar and published branch retain their existing artwork.
