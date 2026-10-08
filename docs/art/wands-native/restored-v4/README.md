# Previous wand palette restored

**October 7, 2026. Local working-art restoration.** Following the owner's request to return toward the previous wand look, the active exporter uses `wand-components-v4.png` again. All fifteen exported body/tip PNGs match the [v4 review manifest](../soft-v4/verification.json) exactly. The five thread textures are separate; the owner found the rope artwork poor, and their visual redesign remains pending.

The existing unedited native inventory screenshots [wands and threads](../soft-v4/inventory/wands-and-threads.png) and [beside vanilla materials](../soft-v4/inventory/beside-vanilla-materials.png) show these exact restored bytes, verified against the resource-loaded hashes in their capture manifest. Those earlier captures are reused here; this restoration did not run a new client session. Actual 16×16 sprites, hard alpha, gold bindings, all 63 connected unions and one-pixel padding pass `tools/author_wand_models.py --check`.

`verification.json` records source and packaged hashes, the referenced native evidence, unchanged thread files and the complete compressed build log. Item models and production Java are unchanged. The v5 source image, prompts and review evidence remain historical comparisons. This restoration is uncommitted and has not replaced the installed testing pack or published branch.
