# Magical thread shimmer · native client review

The owner approved Minecraft-style magic shimmer on the five magical threads on October 8. Each ordinary thread item now has the default cosmetic Glint Override component. It retains stack size 64, has no actual enchantments, and keeps the exact approved v6 16×16 sprite. The fifteen wand layers also remain unchanged.

These are unedited Minecraft 1.21.1 / NeoForge 21.1.72 framebuffer captures. [Inventory manifest](inventory/capture.json) records renderer, contents, cosmetic foil flags and all twenty resource-loaded PNG hashes. Every loaded hash agrees with the approved production sprite and installed combined jar. [Shipping receipt](../../../verification/wand-thread-shipping-2026-10-08/README.md) records build/world checks, artifact hashes and actual installation.

## Inventory and animation

- [All wands and threads](inventory/wands-and-threads.png).
- [Beside vanilla materials](inventory/beside-vanilla-materials.png).
- [Beside String and Lead](inventory/threads-beside-string-and-lead.png).
- [Same arrangement, later glint frame](inventory/threads-glint-later.png).

The ordinary survival InventoryScreen and native item renderer produced all four frames. Selected comparisons were inspected at native pixel scale. [Paired-frame analysis](animation-check.json) detects animated shimmer on all five sprites; the stationary vanilla String control has zero pixel change. No PNG was recolored, upscaled or regenerated to add this effect.

## Recipe viewer

[Wand JEI manifest](wand-viewer/jei/capture.json) records five native cases: untipped, Resonating, Steadfast utility, exact shaped source and reduced GUI scale. Each expected output resolves to one recipe; the acquired exact source participates in dynamic indexing. The [exact shaped source frame](wand-viewer/jei/exact-shaped-source.png) shows the actual parchment display with four inner and four outer slots.

[Thread JEI manifest](thread-viewer/jei/capture.json) records eleven frames covering all five outputs, smaller GUI sizes, socket-frame/String hovers and a resource-pack variation. Output and imbuement-catalyst lookups each resolve once. Inspected [Diamond recipe](thread-viewer/jei/diamond.png), [socket hover](thread-viewer/jei/frame-hover.png) and [resource-pack frame](thread-viewer/jei/resource-pack-gold.png) show the actual display. The final thread-viewer capture loads the full installed pack mod set, including Kithkyn, JEI and Nature's Compass. EMI was not recaptured in this pass.

The [inventory capture source](NativeWandInventoryCapture.java) is review-only and excluded from the installed jar. Temporary snapshot capture hooks were restored after inspection. Existing production wand/thread viewer capture facilities generated the JEI evidence.
