# Magical threads · finer strands

Local art review, not installed or published. The owner liked the preceding softer shading but found the physical string too thick. A precise generated edit narrows the strands and opens the loop gaps while retaining the general arrangements, colour identities, pale highlights and modest shadows.

- [Actual native inventory beside vanilla String and Lead](inventory/threads-beside-string-and-lead.png)
- [Native inventory with wands](inventory/wands-and-threads.png)
- [Native inventory beside vanilla materials](inventory/beside-vanilla-materials.png)
- [Previous exports above, thinner exports below; vanilla String at right](v4-above-v5-below-with-vanilla.png)
- [Generated edit source](../source-art/magical-threads-v5.png) and [complete built-in image prompt](../source-art/prompts-threads-v5.json)
- [Verification manifest](verification.json)

The large generated atlas is source artwork, not a native-resolution sprite sheet. Only the five actual 16×16 RGBA sprites ship as textures. They use at most four opaque shades, binary alpha, connected strands and one-pixel padding. Crops fit uniformly within fourteen occupied pixels. Forty-percent logical-cell coverage preserves the fine strands; this is ordinary export sampling, without hand-painted corrections. Wand body and tip export rules and all fifteen restored v4 wand PNGs remain fixed.

Across the five exports, occupied pixels fall from 436 to 306, about 30%, with broadly similar icon footprints. Solid 2×2 patches fall from 196 to 45; these are simple density measurements, not a claim that every local strand has identical width. Individual comparisons are recorded in the verification manifest. Item models and production gameplay are unchanged.

Three unedited Minecraft 1.21.1 / NeoForge 21.1.72 framebuffer captures use the vanilla survival inventory renderer in a fresh review world. The cursor stays over the avatar and notifications are cleared for comparison. Loaded/packaged hashes cover twenty textures. Opaque texel centers are checked against production PNGs with the same small rendering tolerance as the vanilla String control. The [pinned vanilla reference provenance](../threads-v3/vanilla-reference.json) remains reference-only and is not shipped as Vestige artwork.

The retained capture Java is review-only and removed before production packaging. Export checks cover all five threads and all 63 composed wand appearances; the isolated production build verifies packaging. No world behavior suite is repeated for this art-only revision. The installed testing jar and published branch retain their existing artwork.
