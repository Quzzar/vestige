# Approved ritual recipe viewer · October 6, 2026

The owner locked the archaeological parchment and stone art, then requested question marks for spell ingredients. These are **actual Minecraft framebuffer captures** from fresh isolated worlds, opened through each viewer’s recipe API. Both adapters show the result scroll and capacity but register no hidden spell inputs. Public shard recipes show six separate offerings and two empty Plinths.

| Screen | JEI | EMI |
| --- | --- | --- |
| Four-slot spell | [Fireball](jei/0-fireball.png) | [Fireball](emi/0-fireball.png) |
| Eight-slot spell | [Starfall](jei/1-starfall.png) | [Starfall](emi/1-starfall.png) |
| Public recipe | [Shard](jei/2-attunement_shard.png) | [Shard](emi/2-attunement_shard.png) |
| Small-screen spell | [Fireball](jei/3-fireball.png) | [Fireball](emi/3-fireball.png) |
| Small-screen public recipe | [Attuned shard lookup](jei/4-attunement_shard.png) | [Attuned shard lookup](emi/4-attunement_shard.png) |
| Reloaded concealed spell | [Eight-slot override](jei/5-reloaded_fireball.png) | [Eight-slot override](emi/5-reloaded_fireball.png) |

Each standalone viewer indexes 215 entries: 214 concealed spell recipes plus the public shard recipe. Base/shaped Fireball and real attuned shard output lookups each find one recipe. Paper and Blaze Rod inputs find zero ritual recipes, while Lapis finds the public shard. The isolated datapack reload changes Fireball from four to eight slots and retains concealment. [JEI evidence](jei/capture.json) and [EMI evidence](emi/capture.json) record actual screen classes, two GUI sizes and lookup counts.

The small-screen EMI inspection initially caught a clipped diagram. Its final adapter scales the drawing, item rendering and hover bounds together using the public holder dimensions. All six final EMI views were captured without the synthetic-ID recipe error border. EMI’s remaining development warning concerns an unrelated Kithkyn tag; it is not a ritual warning.

[Original prompts and asset provenance](../../previews/ritual-image-concepts/README.md) retain the generated sources. [Packaged artwork hashes](art-provenance.json) match those unchanged assets. Atlas/Patchouli served as references; none of their art is shipped here. The separate browser export is a design illustration, not native capture evidence.

With both viewers installed, [combined evidence](both/capture.json) confirms the same 215 entries and lookup results; the EMI-native category owns the display. [Combined small-screen shard](both/4-attunement_shard.png) and [concealed reload](both/5-reloaded_fireball.png) preserve the complete arrangement.

A default `recipe_viewer=none` client also starts and completes the existing native item preview without loading missing viewer APIs. Its [framebuffer](none/frame-00000.png) and [capture record](none/capture.json) are startup evidence, not a ritual recipe screen.

Final Java 21 build and **97 unit tests** pass with no failures/errors/skips. [Verification record](verification.json) pins the packaged asset hashes and distinguishes native checks from the browser export. Gameplay crafting and world behavior were not changed.
