# Selected Spellstone A and ritual review

**Historical first A review:** the owner’s subsequent hands-on feedback raises the top, lowers the runes, removes empty socket decals and simplifies hover highlighting. [Refined A](../refined-a/README.md) is the current implementation. The preservation manifest and recordings below pin the pre-refinement revision.

The owner selected **A, the smaller corner chip**, and requested actual in-game review plus recorded ritual states. All 36 production block/item finishes now use the selected geometry. Each of four vertical cap corners wraps five coarse Diamond-colour pixels onto each adjacent side face. Pixel width is 0.5525 model units, retaining the approved 35% size reduction. The top and middle of each side stay bare.

The body is unchanged: two 3.5-unit-thick supports at native 22.5 degrees and a 12×12 cap, 3.25 units thick, ending at y8/16. Three structural solids and forty decorative planes give 43 model elements / 58 baked faces. Decorative planes have no collision. Original 16×16 Minecraft Diamond and masonry tiles supply the colours. The rejected generated gem sprite is removed from production and preserved only as [historical art](../../spellstone-tent-table/side-gems/native-gem.png).

## Actual Minecraft recordings

Native apparatus views live under [native](native/). The ritual recordings use the real server matching, feedback, item renderer and commitment lifecycle:

- Ready ingredients before activation.
- Missing-item translucent baked silhouettes, steady correct-position glow and sideways shaking for misplaced/wrong items.
- A complete wrong recipe that does not trigger a blast.
- Successful crafting, including ingredient lift, consumption and a flat collectible result above the retained reference.
- Failed crafting with a blast, consumed offerings and the preserved reference/terrain.
- Mixed-fragment discovery and its resulting scroll.

These recordings show existing native behavior; no replacement visual effects or timing rules are introduced. Videos are silent and retain measured native frame times. The combined review adds state captions outside the game image.

## Hands-on review

An optional capture handoff leaves Minecraft open in a fresh creative-mode world with four labelled stations: **Ready to craft**, **Missing and misplaced items**, **Wrong recipe: may backfire**, and **Fragment discovery**. Nothing is automatically activated in that scene. An empty-hand right-click on a Spellstone activates it; an empty-hand right-click on a Plinth retrieves its offering. Spare Flicker ingredients are supplied in inventory. Normal placement, collection and ritual risk apply.

The handoff is confined to the opt-in capture client and freshly generated world. Existing worlds are not edited. The separate Prism installation is recorded in [prism-install.json](prism-install.json); restart that client to load its new jar.

[Model and collision audit](model-and-seam-audit.json) checks the selected A geometry, legal rotations, native UVs, corner-only pixels, sampled support coverage, clear opening and unchanged Plinth seams. [Preservation manifest](preserved-assets-before.json) pins collision, Astral renderer, Plinth models and construction recipes from before selection.

Recorded pre-refinement commands (historical; current authoring uses refined A):

```bash
python3 tools/author_apparatus_models.py --check
python3 tools/audit_apparatus_detail.py --check
./gradlew build verifyKithkynCompatibility runGameTestServer -Pgametest_directory=run/spellstone-a-gametest
./gradlew runEffectsCapture -Pcapture_kind=apparatus -Pcapture_spells=spellstone_astral,spellstone_astral_side,spellstone_astral_night,spellstone_astral_scrolls,spellstone_astral_underwater,spellstone_astral_finishes -Pcapture_material=stone -Pcapture_seconds=4 -Pcapture_output=build/spellstone-a-native
python3 tools/archive_rune_captures.py build/spellstone-a-native --locked-a
./gradlew runEffectsCapture -Pcapture_kind=ritual -Pcapture_spells=ritual_idle,ritual_hints,ritual_wrong,ritual_reference_success,ritual_failure,ritual_discovery -Pcapture_material=stone -Pcapture_seconds=5 -Pcapture_review=true -Pcapture_output=build/spellstone-a-rituals
python3 tools/encode_ritual_capture.py build/spellstone-a-rituals docs/art/spellstone-corner-details/locked-a/rituals
```
