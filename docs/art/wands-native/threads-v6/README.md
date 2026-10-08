# Magical threads · strand and shadow together

**Owner-approved appearance, October 8, 2026.** The [approval record](approval.json) pins the five exact accepted production PNGs. The reusable lessons are now in [Minecraft art and pixel scale](../../../design/minecraft-art.md), required by the project instructions. This documentation pass preserves the reviewed pixels; it does not publish or install them.

The October 7 review below records the source and client evidence. The owner rejected the sparse v5 export: it was too thin in Minecraft and left inconsistent dark edge patches. The built-in image edit uses an enlargement of the actual fuller v4 production pixels as its target, rather than the earlier detailed source sheet. It restores a readable pale strand with a modest companion shadow and reduces the largest corners without reducing whole strands to alternating light-only/dark-only runs.

- [Actual native inventory beside vanilla String and Lead](inventory/threads-beside-string-and-lead.png)
- [Native inventory with wands](inventory/wands-and-threads.png)
- [Native inventory beside vanilla materials](inventory/beside-vanilla-materials.png)
- [Rejected sparse exports above, new exports below; vanilla String at right](v5-above-v6-below-with-vanilla.png)
- [Exact production-pixel edit reference](native-v4-edit-target.png)
- [Generated edit source](../source-art/magical-threads-v6.png) and [complete built-in image prompt](../source-art/prompts-threads-v6.json)
- [Verification manifest](verification.json)

The enlarged generated sheet remains source artwork. Only the exported 16×16 RGBA sprites ship. These retain at most four shades, binary alpha, one-pixel padding and connected silhouettes. The exporter uses fifty-percent logical-cell coverage for these fuller thread drawings. Wand sampling and all fifteen restored v4 wand PNGs remain byte-identical; item models and production Java are unchanged.

The exported pixels, rather than the generated atlas, determine whether the shading survives. Three unedited Minecraft 1.21.1 / NeoForge 21.1.72 framebuffer captures use the vanilla survival inventory renderer in a fresh review world. The cursor remains over the avatar and notifications are cleared. Resource-loaded and packaged hashes cover all twenty textures. Opaque texel centers are compared against production PNGs with the same small renderer tolerance as the native String control. The [pinned vanilla reference](../threads-v3/vanilla-reference.json) is not packaged as Vestige artwork.

The temporary inventory capture class is retained here as evidence and removed from production before packaging. Export checks cover the five threads and 63 composed wand appearances. A packaging build excludes the unchanged Java unit suite; no world behavior suite is repeated for this art-only revision. The original verification manifest preserves its October 7 review status; the separate October 8 approval record captures the subsequent owner decision. Publication and testing-pack installation remain separate from this appearance approval.
