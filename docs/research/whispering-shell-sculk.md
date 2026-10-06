# Sculk acquisition for Whispering Shell

**Checked October 6, 2026 against Minecraft Java 1.21.1, NeoForge 21.1.72.** These are vanilla acquisition facts, not an accepted Shell ingredient or a change to vanilla drops.

## Mining

All six placed sculk-family blocks require **Silk Touch at level 1 or greater** in their item-drop loot conditions: Sculk, Sculk Vein, Sculk Sensor, Calibrated Sculk Sensor, Sculk Catalyst and Sculk Shrieker. They can be broken without Silk Touch, but their block item is not dropped. Sculk Vein has no shears alternative in its table.

The evidence is the actual vanilla JSON in the pinned project's Minecraft resource artifact, rather than inference from a Bedrock article or an older snapshot. [Artifact/table hashes and inspected entries](whispering-shell-sculk-1.21.1.json) identify the resource archive and every table. Mojang's [Java Wild Update notes](https://www.minecraft.net/en-us/article/the-wild-update-out-today-java) also document the Silk Touch acquisition rule for Sculk, Veins and Catalysts; the pinned tables establish all six for this project baseline.

## Obtaining sculk without mining

The normal **Ancient City chest loot table** can supply:

| Item | Count for that loot entry |
| --- | --- |
| Sculk | 4–10 |
| Sculk Sensor | 1–3 |
| Sculk Catalyst | 1–2 |

These are possible entries, not guaranteed contents of every chest. No Silk Touch tool is required to take chest loot. The Warden's entity loot table also supplies one Sculk Catalyst on death, but a Warden fight is unnecessary for the proposed sensor recipe. Both tables and their exact relevant entries are retained in the [same evidence record](whispering-shell-sculk-1.21.1.json).

## How common are chest Sensors?

An ordinary Ancient City chest has a **23.24% chance of at least one Sculk Sensor**, approximately one chest in four. Each selected Sensor entry supplies **1–3 Sensors**; repeated entries can supply more. For comparison, the Echo Shard already used by Attunement Shards appears in **29.81%** of these chests.

These percentages are calculated directly from the pinned Java 1.21.1 table: the main pool makes 5–10 uniformly distributed integer rolls, total weight is 86, and Sensor/Echo Shard weights are 3/4. The probability of at least one selected entry is the mean of `1 - (1 - weight / 86)^n` for `n = 5..10`. [Exact calculation and source-table hash](whispering-shell-sensor-rates.json) retain the result. This concerns the normal Ancient City chest table, not ice-box loot, and does not estimate the number of naturally placed Sensors in a city.

Across four such chests, the chance of finding at least one Sensor is **65.28%**; across ten it is **92.90%**. Neither is a guarantee. Finding and exploring an Ancient City remains the main acquisition hurdle. Once a player is already gathering Echo Shards for attunement, a single Sensor is a plausible additional offering without needing Silk Touch.

## Recipe implication

**Proposal:** Nautilus Shell + valid Attunement Shard + Sculk Sensor, on four inner Plinths with one empty offering surface. The sensor fits a communication device because it detects vibrations, and chest loot offers a route without requiring a Silk Touch enchantment. Mojang documents the sensor's vibration behavior in the [Java Wild Update notes](https://www.minecraft.net/en-us/article/the-wild-update-out-today-java).

This does not make the device early-game: its Attunement Shard already uses an Echo Shard, and Ancient City exploration remains a resource gate. Sensor inclusion, range and payment remain proposals until accepted. No vanilla loot changes, item registrations, production textures or native chat changes occurred in this research pass.
