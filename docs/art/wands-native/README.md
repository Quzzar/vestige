# Native wand and thread art

## October 8 · approved art shipped with native shimmer

All five magical threads now have Minecraft's native cosmetic enchantment shimmer, with the exact approved v6 PNGs and fifteen retained wand layers unchanged. [Actual inventory/JEI frames and animation evidence](thread-shimmer/README.md) confirm the final in-game appearance. The verified combined build is installed in Kithkyn Testing with a previous-jar backup. [Shipping evidence](../../verification/wand-thread-shipping-2026-10-08/README.md) records full and focused gameplay tests, exact hashes and publication scope. Restart Minecraft to load it. Earlier uninstalled/unpublished descriptions below are historical.

## October 8 · approved Minecraft thread artwork

The owner approved [threads v6](threads-v6/README.md). Its [approval record](threads-v6/approval.json) pins the exact five reviewed PNGs, which remain unchanged. [Minecraft art and pixel scale](../../design/minecraft-art.md) records the reusable workflow and is linked from the project instructions. This pass records approval and documentation without publishing or installing artwork.

## October 7 · native strand and shadow correction

The [subsequently approved thread revision](threads-v6/README.md) restores a fuller pale strand and more consistent modest shadow after the owner rejected the sparse native v5 result. Its edit target is an enlargement of the exact fuller production pixels, helping avoid details that vanish in reduction. Actual exported pixels and native inventory frames determine the result. All fifteen restored wand PNGs remain fixed. The original review and capture evidence are preserved.

## October 7 · thinner thread strands

The [rejected sparse thread review](threads-v5/README.md) narrows the physical strands and opens the loop gaps. The owner subsequently found its native appearance too thin and its dark edge inconsistent. Actual native inventory captures preserve the production result beside vanilla String and Lead. Structural checks passed, but did not establish visual acceptance. All fifteen restored wand PNGs remained fixed.

## October 7 · softer thread shadows

The [preceding thread review](threads-v4/README.md) gently lightens and interrupts the rigid dark border runs with a fourth intermediate shade while keeping pale String-like strands. The owner then requested thinner physical string. Actual native inventory captures show the production 16×16 sprites beside vanilla String and Lead. All fifteen restored v4 wand PNGs remained fixed.

## October 7 · native String shading review

The [preceding thread review](threads-v3/README.md) uses dark shadows and pale strands following native Minecraft String, with three shades and subtle ingredient tints. The previous midtone treatment was too flat; the owner subsequently requested subtler, less rigid outlines. Actual inventory captures and texel comparisons show the exported 16×16 results beside vanilla String. All fifteen restored v4 wand PNGs remained fixed.

## October 7 · simpler magical thread review

The [superseded thread review](threads-v2/README.md) redraws the five ingredients as cleaner loops and folded strands, with restrained four-shade palettes and native inventory comparisons beside vanilla String and Lead. The owner requested stronger dark/pale String-style shading afterward. All fifteen restored wand PNGs remained exactly v4.

## October 7 · previous wand palette restored

The [active working artwork](restored-v4/README.md) returns the fifteen wand components exactly to v4, the version just before the latest softening. The owner preferred the previous wand appearance and rejected the rope/thread artwork, which remains a separate art task. Exact sprite hashes match the existing native v4 captures. This restoration is local and uncommitted; the installed pack and published branch retain v3.

## October 7 · further palette softening

The [superseded v5 review](soft-v5/README.md) lightens the material-coloured outlines and quiets the highlights further, while preserving all fifteen v4 component silhouettes exactly. Actual inventory captures show the exports beside vanilla items. Threads and models remain unchanged. The owner subsequently requested the previous wand appearance above.

## October 7 · softer artwork review

The preceding [v4 review version](soft-v4/README.md) reduces harsh outline/highlight contrast and blocky contour expansion, with actual native inventory captures beside vanilla items. Source art and the built-in edit prompt are retained. This is an uncommitted preview; the testing pack and published wand branch still contain the previous reference-preserving export below.

## October 7 · preserve the generated reference in the actual export

The [current export correction](faithful-v3/README.md) keeps the original body proportions, thin branches and gold grip bands at actual 16×16. It supersedes the simpler v2 conversion below, which discarded those details. Bodies have at most eight shades including two reserved for gold, tips at most four. Every one of the 63 appearances remains padded and connected. Both the native inventory frames and the enlarged inspection now show the actual exported sprites. The source reference is retained unchanged and is explicitly identified as a larger generated image rather than an already-native 16×16 asset.

## October 7 · quieter shading and clear padding

The [padded revision](padded-v2/README.md) supersedes the first wand pixels after native inventory review. All seven body and eight tip layers now have simpler flat colour patches: **at most six opaque colours per body and four per tip**, compared with forty to forty-nine in the previous bodies. Every one of the **63 composed appearances** has a transparent one-pixel border, binary alpha and a connected body/tip silhouette. The five approved thread sprites are unchanged.

The built-in image tool revised the source artwork; its [complete prompt and export provenance](source-art/prompts-v2.json) are saved beside `wand-components-v2.png`. The exporter indexes opaque source colours without dithering and takes the dominant colour in each logical pixel, avoiding isolated shading variations from point sampling. Its checks cover both the individual layers and their rendered unions. Native inventory and full-palette captures are recorded in the revision folder. Earlier images below remain historical comparisons.

## First artwork and gameplay review

The production assets are actual **16×16 RGBA sprites**, with opaque/transparent alpha and ordinary Minecraft item shading. Seven separately generated body silhouettes and eight separately generated tip sprites compose into **63 handheld models**; thread choice adds no wand picture. The five thread components use distinct loops, coils, knots and braids as well as their palettes.

`tools/author_wand_models.py --check` verifies every production pixel and generated model. `source-art/` retains the original generated PNGs and complete prompts. These images were generated with the built-in image tool, true transparent background, then exported by nearest-neighbor format conversion; the large source images are not packaged game textures. No Iron artwork was copied.

`wands/all-63-wands-and-five-threads.png` is an actual Minecraft main-framebuffer capture using the ordinary item renderer. Its manifest verifies the resource-loaded PNG dimensions and hashes, separately from an enlarged art contact sheet. The recipe-viewer folders record native lookup/capture runs; a run is verified only when its complete manifest is present. See [gameplay and publication evidence](../../verification/native-wand-tips-2026-10-07/README.md) for exact builds and limitations.
