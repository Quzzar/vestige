# Registered Dissentient Diamond

The approved v11 texture and model now belong to the real `vestige:dissentient_diamond` ingredient, rather than the earlier CustomModelData art carrier. It uses Minecraft 1.21.1's native **Uncommon** rarity, matching Wither Skeleton Skull's yellow name (`#FFFF55`). Both native tooltips are drawn through Minecraft's tooltip renderer in the inventory screen post-render event. The unrelated cursor's ordinary Netherite Scrap tooltip remains visible.

All six integrated-client checks pass: real registry identity/name synchronization, skull rarity parity, no glint, ordinary stackability without durability, generated-item model and actual 16×16 texture. Inventory at GUI scales 3/2 and held/offered/dropped views were inspected. [Native comparison crop](native/inventory-comparison-crop.png), [receipt and hashes](native-review.json). Root item registration, English name and approved asset hashes match the isolated capture resources.

The capture used the same art-only Standing Stone collision bypass as v11; its original isolated source was restored exactly after the client exited. The scene uses no Standing Stone. This pass assigns no intrinsic traits, adds no direct-use behavior, and leaves the selected ingredient recipe and held Fluxed Flint revision for follow-up. No installation, publication or world-test-suite run is claimed.

The root Java 21 build also passed: **195 unit tests** across 44 suites, zero failures/errors. Packaged texture, model and English name match production exactly; the packaged registration class contains the new ID. [Build receipt](build-review.json).
