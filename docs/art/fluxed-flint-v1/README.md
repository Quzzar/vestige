# Fluxed Flint v1

First independently generated sprite study, October 8, 2026. The imagegen built-in tool used [this prompt](prompt.txt) and the pinned vanilla 1.21.1 Flint as a style/pixel-scale reference. This is a new asset, not a recolor of vanilla pixels. [Source](source.png) is a generated study; it is not a literal native-resolution production sprite.

**Owner design hold, October 8:** defer further Fluxed Flint artwork changes while defining the **Dissentient Diamond** intermediate. Its selected ingredients are one Wither Skeleton Skull, two Gunpowder and one Diamond; its artwork and precise behavior remain open. The owner liked the current Flint appearance and suggested replacing violet fractures with diamond-blue accents for the possible revised composition. That recolor has not been made; the existing sprite and captures below remain the current playtest asset. [Recipe and material discussion](../../design/magical-repair.md) records the hold and distinguishes proposals from implemented behavior.

`tools/author_fluxed_flint.py` crops at alpha >=128, exports an 11×13 footprint into a 16×16 RGBA canvas with clear margins, and uses nondithered five-gray/three-purple palette reduction with dominant per-cell sampling and preserved fracture accents. The [preview](export-preview.png) is an integer nearest-neighbor enlargement of those exact production pixels on light and dark backgrounds. Production asset: `src/main/resources/assets/vestige/textures/item/fluxed_flint.png`; ordinary generated-item model scale.

Native evidence and hashes are recorded under [verification](../../verification/fluxed-flint-2026-10-08/README.md). Visual review and owner acceptance are separate; v1 is an initial playtest asset, not yet owner-approved final artwork. Pinned vanilla Flint/Echo Shard/Amethyst Shard PNGs here are comparison references, not shipped Vestige item textures.

## Native review

Five unedited Minecraft 1.21.1 / NeoForge 21.1.72 framebuffer captures were inspected: [inventory beside vanilla](native/inventory-beside-vanilla.png), [held and offered](native/held-and-offered.png), [repair lift](native/repair-in-progress.png), [dropped repaired output](native/repaired-output.png), and [durability after repair](native/durability-after-repair.png). The inventory comparison shows literal pixel-scale gray Flint with violet fractures beside native Flint, Echo Shard and Amethyst Shard. The held sprite uses normal generated-item rendering. The final inventory shows native durability bars for worn Flint and the repaired Staff.

[Native assertions and loaded asset hashes](native/verification.json) confirm translated naming, production activation, a dropped target differing only by ten repaired durability, and exactly ten catalyst points spent. This is an inspected first visual candidate; owner acceptance remains pending.

## Capacity correction

The owner clarified that shapeless offerings may sit on either ring. A follow-up [native eight-slot scene](native-shapeless/held-and-offered.png) places Flint and Staff on two opposite outer Plinths, with every inner offering surface empty. [The repair lift](native-shapeless/repair-in-progress.png), [actual dropped output](native-shapeless/repaired-output.png) and [final durability bars](native-shapeless/durability-after-repair.png) were inspected. [Five native assertions and matching asset hashes](native-shapeless/verification.json) verify the two-offering fixture, production activation and exact ten-point repair/wear. Sprite/model bytes are unchanged from v1. [Capacity verification](../../verification/shapeless-rituals-2026-10-08/README.md) pins the newer build; earlier captures retain the original four-slot behavior as historical evidence.
