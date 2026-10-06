# Native Spellshaping evidence

**Historical initial release:** the revised shard recipe and three additional augments are recorded in [October 4 refinement evidence](../spellshaping-refinements/README.md).

Captured from the actual Minecraft 1.21.1 / NeoForge 21.1.72 client, using registered apparatus blocks and the native server ritual/cast runtime. These recordings are not browser studies. Clips have no audio and are encoded from main render-target frames at their measured timestamps.

- [Automatic shaped Fireball crafting](ritual_spellshaping.mp4): the normal Paper / Blaze Rod / Gunpowder / Emerald recipe, with End Stone / Bone Block / Copper local sockets. The flat reference and installed materials remain; the output saves Bleeding, Reaching and Shocking.
- [Eight-slot shard crafting](ritual_attunement.mp4): all eight fixed ingredients are consumed. The result saves its full reproducible key and geometry/ingredient/material blueprint; sockets remain. [Finished shard](attunement_result.png).
- [Actual shaped Fireball cast](shaped_fireball.mp4): three villagers inside the three-block footprint finish at 7/20 HP; two outside and the caster stay at 20/20. The original blast and bounded electrical/wound riders execute. The development cast bypasses resources while retaining charge and native effects; automated gameplay tests separately verify real typed payment.

The adjacent JSON files retain native frame times, outcome snapshots, source and hashes. The cast also hashes its packaged Spellshaping rules. Crafting captures hash their block models/rune texture. Full source frames remain in `build/spellshaping-client-rituals` and `build/spellshaping-client-final-cast`. [Installation record](prism-install.json) verifies the built jar matches the Prism Kithkyn Testing copy. Restart that instance to load it.

78 unit tests and 130 gameplay tests pass. The executable set is 47 individual augments and six compounds; the broader design codex and devices using shards remain future work. These samples verify current native appearance and execution, not exhaustive presentation or balance for every augment.
