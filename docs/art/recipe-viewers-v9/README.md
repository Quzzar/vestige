# Paper-only ritual presentation · October 6, 2026

The owner requested removing the normal Minecraft-style frame around the parchment artwork. The gray bevel and duplicate category title in the temporary art preview were our mock UI. The [current paper-only export](../../previews/ritual-image-concepts/paper-only-export-v9.png) removes them, preserving the approved artwork and exact circle/slot alignment. It is an exported illustration, not an in-game screenshot.

The native diagram already draws paper and overlays alone. JEI's category now disables its supported additional per-recipe border through `needsRecipeBorder()`. These fresh screenshots show that border is absent, while JEI's outer gray window, navigation, tabs, workstations and item list remain its own UI. The pinned EMI public API has no equivalent background-removal switch; its adapter is unchanged. [Earlier native EMI/both/none evidence](../recipe-viewers-v8/README.md) remains applicable to those unchanged paths.

| Actual JEI screen | Native framebuffer capture |
| --- | --- |
| Fireball, four concealed seats · 640×360 GUI | [Open](jei/0-fireball.png) |
| Starfall, eight concealed seats · 640×360 GUI | [Open](jei/1-starfall.png) |
| Public shard, six offerings and two empty seats · 640×360 GUI | [Open](jei/2-attunement_shard.png) |
| Fireball · 320×240 GUI | [Open](jei/3-fireball.png) |
| Actual attuned shard lookup · 320×240 GUI | [Open](jei/4-attunement_shard.png) |
| Concealed Fireball after four-to-eight-slot reload · 320×240 GUI | [Open](jei/5-reloaded_fireball.png) |

All six screenshots are unmodified Minecraft main-render-target captures, visually inspected and copied from the opt-in native capture run. [Capture data](jei/capture.json) records the actual screens, dimensions, 215 ritual entries and lookup/concealment/reload results. [Verification](verification.json) records the successful Java 21 build, 97 passing unit tests, asset hashes, screenshot hashes and matching packaged category class. World tests were not repeated for this presentation-only change.

**Subsequent owner-requested installation:** [Installation record](prism-install.json) confirms Vestige and the exact tested Minecraft 1.21.1 NeoForge JEI artifact copied into **Kithkyn Testing** on October 6. The previous Vestige jar is backed up outside `mods`; both installed hashes match the staged originals and pass ZIP integrity checks. This shared packaged build also contains the coordinating agent's current Standing Stone work. The Prism instance retains Minecraft 1.21.1 / NeoForge 21.1.248. Restart Minecraft in that pack, then hover a spell scroll and press `R` to inspect its ritual. This installation check does not claim the Prism instance was launched or manually playtested here.
