# Wand artwork · simplified and padded revision

**October 7, 2026.** This replaces the first wand pixels following the owner's actual-inventory feedback: the old export looked noisy and touched the sprite edges. The seven distinctive material bodies and eight optional tips now use simpler connected colour patches. Bodies have at most six opaque colours, tips at most four; all sprites are actual **16×16 RGBA**, with hard alpha. Every one of the **63 combinations**, including untipped bodies, has a clear **one-pixel transparent outside border** and a connected silhouette. All five thread sprites, models and gameplay remain unchanged.

## Actual Minecraft inspection

- `inventory/wands-and-threads.png`: vanilla survival inventory, seven untipped wands, eight tipped examples and five approved threads beside ordinary Minecraft items.
- `inventory/beside-vanilla-materials.png`: the same native inventory with vanilla base materials directly beside corresponding wands.
- `wands/all-63-wands-and-five-threads.png`: all composed models through Minecraft's ordinary item renderer.

These three images are unedited Minecraft 1.21.1 / NeoForge 21.1.72 framebuffers. The inventory review uses real item stacks in a fresh disposable survival world, with its cursor over the avatar and notifications cleared. The temporary inventory harness is retained in [the earlier review folder](../inventory/NativeWandInventoryCapture.java), outside production compilation. Native capture manifests pin every resource-loaded texture; `verification.json` additionally records palette/border checks, frame hashes and packaging evidence. This artwork-only change does not require another gameplay/world-test run; the earlier [tip implementation checks](../../../verification/native-wand-tips-2026-10-07/README.md) remain separate evidence.

`sprite-contact-sheet.png` is an enlarged nearest-neighbour engineering inspection of the actual texture unions, on a plain grey background; it is not a game screenshot.

## Source and export

The built-in image tool edited `source-art/wand-components-v2.png` from the original atlas, with a genuinely transparent background. [Full prompt and export details](../source-art/prompts-v2.json) are retained. The exporter indexes the opaque source palette without dithering, then takes the dominant colour within each logical pixel. This avoids sampling tiny colour variations in generated flat patches. Geometry and colours derive from the revised image, with no painted additions or blur. Bodies occupy canvas x=1..12/y=4..14; tip overlays occupy x=10..13/y=1..4. The common attachment remains connected for all pairs.

Run `tools/author_wand_models.py --check` with Pillow available to check exact texture/model drift, maximum palettes, hard alpha, composed padding and connectedness. The original atlas and first inventory captures remain historical comparisons.

## Testing-pack update

[Installation evidence](installation.json) pins a verified backup and the texture-only update in **Prism → Kithkyn Testing**. Exactly **15 wand PNG entries** change; all **3,497 other jar entries** are byte-identical to the earlier verified combined artifact. The current shared checkout contains concurrent work, so this update preserves the installed gameplay rather than rebuilding that unreviewed work. The installed hash is `f3220a43e09fe95416d5f697dbabd1a7030e3d4ff488defc57d64e00f76382ff`. Restart the testing client to load the revised art.
