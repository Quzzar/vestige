# Softer wand palette · review version

**October 7, 2026. Preview for owner review; not published or installed.** This follows the owner's feedback that v4 was much better but still slightly harsh. The [new source artwork](../source-art/wand-components-v5.png) uses lighter material-coloured edge shades and quieter highlights. The [complete built-in image edit prompts](../source-art/prompts-v5.json) are retained.

The exporter holds all fifteen v4 logical opacity masks and positions exactly fixed while sampling the new colour ramps. Every body and tip remains actual 16×16 with hard alpha; all 63 body/tip unions remain connected and retain one-pixel outside padding. Gold bands stay visible. The five thread PNGs and all item models are unchanged. This is a palette iteration with no casting or recipe changes.

The two images under `inventory/` are unedited Minecraft 1.21.1 / NeoForge 21.1.72 framebuffer captures, using real stacks and the vanilla survival inventory renderer. `capture.json` pins all twenty loaded texture hashes. `sprite-contact-sheet.png` is a nearest-neighbour enlargement of the actual 63 exported unions; `v4-above-v5-below.png` compares the previous and new seven body sprites and is not a game screenshot.

`verification.json` records exact silhouettes, border/palette checks, source and frame hashes, unchanged threads, packaging and complete compressed logs. The temporary inventory capture class is excluded from the production package. Run `tools/author_wand_models.py --check` with Pillow for reproducible sprite/model checks. The [installed v3 artwork](../faithful-v3/README.md) remains in the testing pack; this review is uncommitted and is not owner approval of final artwork.
