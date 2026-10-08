# Attunement Shards

**Item art implemented October 6, 2026:** [S1a1 — Sharp Tip](../art/native-item-textures-v3/textures/shard_sharp_tip.png) is the packaged exact 16×16 texture: a pointed purple Amethyst splinter with an Echo-colored inner seam. Its base sprite is shared across keys. [Native item inspection](../art/attuned-items-native-v2/README.md) verifies enchanted glint, a colored inventory rune and a four-rune tooltip signature derived from the existing key. Prism installation remains pending.

**Implemented October 4; recipe revised October 5, 2026.** An Attunement Shard is crafted by a fixed, shapeless ritual on the **eight-node** Spellstone apparatus. Place **2 Amethyst Shards, 1 Echo Shard, 1 Iron Ingot, 1 Diamond and 1 Lapis Lazuli** on any six of the eight active Plinths, one item per offering surface. Leave two offering surfaces and Spellstone's reference surface empty. No Paper is required for this non-scroll output.

The owner's October 5 ingredient list supersedes the earlier eight-distinct-ingredient recipe. Both Amethyst Shards occupy separate Plinths; a stack on one surface cannot supply both. Recipe acceptance remains strict and shapeless, without a variable ingredient pool. The complete two-layer apparatus is still required, including the two nodes whose offering surfaces remain empty.

## Recipe flavor

These roles explain the **native recipe theme**; they are not separate bonuses or claims that Iron assigns those exact meanings.

| Ingredient quantity | Native role |
| --- | --- |
| 2 Amethyst Shards | Paired resonant crystal carriers |
| 1 Echo Shard | A retained echo of ancient magic |
| 1 Iron Ingot | Stable frame |
| 1 Diamond | Precise, durable binding gem |
| 1 Lapis Lazuli | Inscription pigment |

Consult the [pinned Iron reference](../research/iron-material-uses.md) for actual upstream material uses. This owner-selected recipe and its roles are Vestige decisions. Core access requires only vanilla materials. Source quality values do not make a stronger attunement.

## Shapeless acceptance, paired identity

Recipe acceptance asks whether exactly the six fixed offerings are present, including the repeated Amethyst. Signature identity asks **how those offerings, the two empty offering seats, their imbuements and the active geometry are arranged**. Any arrangement qualifies, but different arrangements can produce different keys.

The ritual captures the same immutable active node snapshot used for atomic commitment. Every node retains its layer, integer position relative to Spellstone, offered item ID/consumed quantity and installed socket material, including an empty material marker. The two empty offering seats retain `minecraft:air`, quantity zero and their own socket materials. Both ring shapes, spacing and independent signed height steps participate. Inner elevation is `dy1`; outer elevation is `dy1 + dy2`, preserving the separate second height step.

**Owner color clarification, October 5:** wool/concrete imbuement colors are cosmetic. Hash encoding treats every wool color as the White Wool family and every concrete color as the White Concrete family. Stored node materials and sockets retain the actual color, so inspecting, rendering and recovering the installed block remain accurate. This applies to sockets on occupied and empty offering nodes. Changing Wool to Concrete still changes the material family and key; changing only its dye color does not. Offering item identities and atomic snapshots remain exact.

Canonicalization rotates the **complete paired arrangement** through four whole quarter-turns and selects a stable serialization. Both layers, their geometry, offerings and socket materials rotate together. It does not rotate ingredients independently of imbuements or normalize the layers separately. A full SHA-256 digest is stored with the bounded, inspectable version-1 blueprint; parsing validates geometry and recomputes the hash.

| Change | Identity |
| --- | --- |
| Recreate the complete blueprint | Same |
| Translate or raise the complete build | Same |
| Rotate the complete build by a quarter-turn, preserving local pairs | Same |
| Swap offerings, imbuements or only one layer | Generally different; natural symmetries can still match |
| Change Cross/Diagonal shape, spacing or relative height | Different |
| Mirror the arrangement | Generally different; mirrors are not canonicalized |
| Replace a cosmetic Plinth variant while preserving its node inputs | Same |
| Change only a wool/concrete imbuement's color | Same |
| Change decorative blocks, world, player or shaping coefficients | Same |

The key uses physical inputs, not Amplify/Range/Area/Casting Cost factors. Two structures may yield identical numerical modifiers and different attunements. Balance changes therefore do not retune an existing blueprint. Stack identity uses full components, not the tooltip's rune signature. Cosmetic item names and incidental ingredient metadata do not enter the recipe identity. See [apparatus variants](apparatus-variants.md).

## Commitment and retained data

The six offerings are consumed once, socket materials remain installed on all eight nodes, and the result rests on Spellstone for collection. Changed active inputs or a broken Plinth cancel before commitment, including changes at either empty offering node. Extra offerings, missing ingredients and incorrect duplicate counts reject safely. A reference scroll is not accepted for this operation. Four-node layouts cannot craft shards.

A crafted shard retains its key even when the apparatus moves or is dismantled. It uses the selected S1a1 sprite with enchanted glint. Unattuned shards also shimmer, but have no attunement mark. Valid key data supplies a small colored inventory rune. The hover tooltip contains only **Attunement Shard** and four colored enchantment-table runes beneath it, without a label, code, hint or blueprint text. The same custom text applies with advanced tooltips enabled; an unattuned shard adds no custom tooltip line. These marks are visual abbreviations: exact matching still uses the complete verified key, never a glyph/color combination. The full key and reproducible blueprint remain stored internally. Previously crafted version-1 shards retain valid keys: presentation changes and the revised eligibility recipe do not reinterpret their saved inputs.

## Standing Stones and future devices

[Standing Stones](standing-stones.md) now implement the first native network: a four-offering device ritual consumes one valid shard and copies its key unchanged. Matching keys link peer stones within the same dimension. A native list picker with current-stone naming and bottom rune signature, server-validated nearby arrival and the shared 36 masonry finishes are implemented. The owner accepted the copied key selecting repeatable upright silhouettes and the existing rune mark; recipe masonry selects the finish separately. The owner approved sixteen native monolith forms with luminous signature runes and drifting glyphs, across all 36 finishes. The accepted payment selector is the imbuement in the Pearl's Plinth, with distance-scaled XP/hunger/health/mana routes; payment implementation and numerical pricing remain unfinished. Current free travel is a development draft, not the accepted final behavior. Optional terrain-map integration remains deferred.

[Whispering Shell and Crane Bag](attuned-devices.md) use matching signatures for communication and shared inventory respectively. Shell binding, Sensor ritual and private communication are implemented; Crane Bag storage and bundle interactions are implemented with its survival recipe and final art deferred. Homebound Eye is implemented and replaces the proposed compass. Its four-offering ritual consumes a shard and copies the verified key unchanged, retaining its own crafting Spellstone address and the Spider Eye's local payment choice separately. Other device recipes should likewise copy the shard's existing key and then apply their own range, permissions and destination rules. They should not rehash the shard with the device's new ingredients. Consumption or return of the shard is a device-specific decision.

An attunement is reproducible, not proof of ownership. Someone recreating the blueprint can recreate its key. Multiple endpoints can share a key; Standing Stones use a destination picker. The historical recipe-based radio precedent is recorded in [Sending Shell research](../research/legacy-radio-attunement.md).

## Verification

Model tests cover canonicalization, ordering, local pairs, empty offering positions/socket materials, rotation, geometry, decoding and immutable keys. Native GameTests cover six-offering crafting on eight nodes, exact duplicate counts, missing/wrong/extra ingredient rejection, reproducibility under whole rotations, component persistence, socket retention and atomic cancellation after an empty node is broken. The [development status](../development-status.md) records actual verification and native capture evidence.
