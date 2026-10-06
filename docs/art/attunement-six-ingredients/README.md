# Six-offering Attunement Shard · October 5, 2026

The owner's selected recipe is **2 Amethyst Shards, 1 Echo Shard, 1 Iron Ingot, 1 Diamond and 1 Lapis Lazuli**. It is shapeless across six separate offering surfaces on the eight-node apparatus. Two surfaces stay empty; their nodes and socket materials still participate in the attunement blueprint and atomic commitment checks.

[The recording](ritual_attunement.mp4) captures the actual Minecraft main render target at 960×540, using the live Spellstone/Plinth assets. The six ingredients rise while all eight Plinths stay grounded, then one keyed shard rests on Spellstone. [The rising-ingredient frame](ingredients-rising.png) and the encoded recording were inspected. The 108 measured native frames span eight seconds and 160 server ticks. No audio was captured.

The neighboring [metadata](ritual_attunement.json) confirms `ATTUNING`, one produced shard, eight empty offering surfaces after commitment and a validated version-1 blueprint containing exactly the six offerings plus two zero-count `minecraft:air` nodes. The blueprint retains all eight paired socket materials; native behavior tests additionally verify socket retention after crafting.

`build`, `verifyKithkynCompatibility`, **82 unit tests and all 135 required Minecraft tests pass**. Tests cover missing second Amethyst, extra Echo in its place, a seventh offering, an incorrect replacement ingredient, safe rejection followed by successful crafting, complete quarter-turn identity, component persistence and cancellation after a previously empty node is broken.

The [installation record](prism-install.json) verifies matching source/destination jar hashes in **Prism / Kithkyn Testing**. Restart Minecraft there to load the updated build. Automated testing and this capture use NeoForge 21.1.72; that instance uses 21.1.248, so its manual gameplay test remains separate. Earlier eight-ingredient recordings are historical evidence superseded by this recipe.
