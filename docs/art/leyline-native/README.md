# Native leyline apparatus and crafting captures

Recorded and inspected October 4, 2026 with Minecraft 1.21.1 / NeoForge 21.1.72, Vestige alone. These are actual registered blocks and native server ritual operations, captured from Minecraft's main render target before the GUI. No browser sketch or simulated cast is used. Monuments are isolated test fixtures; their masonry is decorative.

The current captures were regenerated in fresh worlds after removing the old pedestal IDs and migration hooks. Only Spellstone and Plinth are registered. The approved rune pixels are unchanged under the current `spellstone_runes` resource name; model/texture hashes below match the cleaned build. The [installation record](prism-install.json) pins the jar copied into Kithkyn Testing; the [world-reset record](../../leyline-world-reset.json) lists the deleted old worlds and backups.

## Apparatus

[Spellstone](apparatus/spellstone.png), [Plinth with embedded Gold Block](apparatus/plinth.png), [Nature henge](apparatus/leyline_henge.png), [pyramid](apparatus/leyline_pyramid.png), [item arrangement](apparatus/apparatus_layout.png) and [native dropped-item comparison](apparatus/apparatus_item_scale.png) are unchanged framebuffer PNGs, frame 15 of their respective captures. Adjacent JSON records pin the model/texture hashes, frame hash, timing, engine and scenario. The final review corrected transparent-rune rendering and an integer-division error in surface/socket width; the archived pictures use both fixes.

## Successful crafting recordings

| Recording | Actual output | Active geometry | Result verified |
|---|---|---|---|
| [Nature henge](ritual/leyline_henge.mp4) | Protector Tree | Cross 5 / Diagonal 4, heights 0 / 0 | Shaped scroll present; all active offerings consumed; reference retained |
| [Pyramid](ritual/leyline_pyramid.mp4) | Clairvoyance | Cross 8 / Cross 16, heights −6 / −6 | Shaped scroll present; all active offerings consumed; reference retained |
| [Reference-preserving craft](ritual/ritual_reference_success.mp4) | Flicker | Cross 2 / Diagonal 3, heights 0 / 0 | Flat reference and finished scroll both retained |

The henge and reference-preserving recordings contain 52 actual frames; the pyramid contains 53. Each runs approximately four seconds and ends at 80–81 server ticks, as recorded in its JSON. The archived `outcome: CRAFTING` is the activation response; the snapshot separately verifies the produced result, empty offerings, retained reference, exact stored modifier values and geometry. PNGs without `-finished` show ingredient lifting; the `-finished` PNGs show the completed state. These recordings demonstrate **crafting**, not the resulting spell being cast.

H.264 encoding uses measured frame timestamps, 30-fps output, no audio and fast-start MP4 metadata. Video SHA-256 and current native model/texture SHA-256 are in each adjacent JSON record. Final frame images are copied unchanged from the raw capture. Regeneration uses the normal native capture task and [encoder](../../../tools/encode_ritual_capture.py); the [playtest guide](../../leyline-playtesting.md) explains actual casting and payment checks.
