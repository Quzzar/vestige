# Wand export correction · preserve the reference artwork

**October 7, 2026.** The owner correctly found that the generated reference sheet and the first simplified in-game export differed. The v2 exporter stretched the bodies into a 12×11 box and allowed a global palette/majority reduction to discard their gold bands and thin branches. The generated sheet itself is **1254×1254**, with irregular enlarged source cells; it is not an already-authored native 16×16 sprite sheet despite that requested grid in the generation prompt.

This revision retains the **same source image, unchanged**, and corrects the production conversion. Each body fits thirteen pixels tall, with width derived from its source aspect ratio and a shared upper attachment. Six body shades and two reserved gold shades preserve small grip bands; band coverage is resolved together before choosing its gold shade. One-third shape coverage retains narrow branches as native pixels. All colour indexing is nondithered. Tips remain four pixels square, with at most four shades. All actual game textures are **16×16 RGBA** with binary alpha; all **63 combinations** have a connected silhouette and a one-pixel clear outside border. Every body retains visible gold pixels. The five thread textures, models and gameplay are unchanged.

The source has more detail than fits into sixteen pixels; this is a source-preserving native adaptation, not an assertion that the generated image was already a pixel-exact game asset. Future large artwork reviews should show the actual exported PNGs enlarged with nearest-neighbour pixels before treating them as production art.

## Review evidence

- `inventory/wands-and-threads.png` shows all seven untipped bodies and eight tipped examples alongside ordinary Minecraft items, using actual item stacks and the vanilla survival inventory renderer.
- `inventory/beside-vanilla-materials.png` puts each native wand beside its base material; the five approved threads sit beside String, Lead, Amethyst and Diamond.
- Both inventory images are unedited Minecraft 1.21.1 / NeoForge 21.1.72 framebuffers. The temporary [inspection harness](../inventory/NativeWandInventoryCapture.java) is excluded from production compilation.
- `sprite-contact-sheet.png` enlarges all 63 actual PNG unions with nearest-neighbour sampling on plain grey. It is an engineering inspection, not a game screenshot.
- `verification.json` pins the unchanged source image, native resource hashes, all layer palettes/bounds, retained gold, frame hashes and packaged textures. `tools/author_wand_models.py --check` verifies pixel/model drift and the palette, hard-alpha, padding, connectedness and gold-retention constraints. Compressed complete native/assembly logs retain hashes of their original bytes.

The [reference image](../source-art/wand-components-v2.png) and [original built-in image prompt](../source-art/prompts-v2.json) are preserved. This correction changes the export code only; no new artwork generation or source-image edit is made. [The previous export](../padded-v2/README.md) remains historical evidence of the mismatch.

The texture-only testing-pack update and verified backup are pinned in `installation.json`. Every jar entry outside the wand PNGs is preserved byte-for-byte; the artifact does not include concurrent shared-source work. Restart Minecraft to load the corrected textures.
