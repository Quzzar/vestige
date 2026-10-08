# Thread imbuement layout study

**October 6, 2026. Browser prototype only.** The shared thread recipe is accepted: one String, one Amethyst Shard and one Honeycomb produce one thread. Only the installed material in the String's own Plinth selects the result. The installed material is retained.

[Open the interactive study](index.html) to compare the fitted material frame with the original stone frame, select all five threads and reduce the recipe size. The expandable close-ups show the rim's fit and relief. This reuses the approved parchment, Plinth and Spellstone artwork without changing the production assets. The output uses vanilla String as an explicit placeholder for unfinished thread artwork.

## Proposal

Keep the three consumed offerings on their Plinth surfaces. The owner's latest proposal fills the existing square rim around the String with the installed block's actual texture. The center and String icon stay clear. Hovering the rim shows the installed block and **Installed imbuement · retained**; hovering the String shows the consumed offering. The material still needs native catalyst indexing and lookup even though it has no separate visible block icon. Each thread has its own public recipe entry and exact output:

| Thread | Installed selector |
| --- | --- |
| Ensorcelled Thread | Diamond Block |
| Callous Thread | Iron Block |
| Smoldering Thread | Gold Block |
| Laced Thread | Emerald Block |
| Consecrated Thread | Glowstone |

The current mask traces the pale rim and its corner pieces in the **1254×1254** source illustration, then maps them with the existing 34-pixel Plinth. The block's unchanged 16×16 face is clipped to those contours. Multiplying the original illustrated rim over the material preserves its cracks and relief; the original ink boundary remains visible. No extra square or outline is drawn. The same contours define the hollow material hover region, while the String owns its inner hover. Only the String's Plinth receives the required material. Other Plinths retain their ordinary illustration and make no imbuement claim. Iron remains subtler against the parchment and benefits from its exact hover name. Native small-scale hover/lookup remains pending.

The previous study compared attached and inset badges. The owner preferred the material-frame direction, then rejected its first execution because the 22-pixel rectangular overlay looked like a square placed on top of the artwork. The correction must fill the illustrated rim itself, including its bevels and corner contours. Both badge code and the extra rectangle are removed from the current study; the original frame provides the comparison.

**Alignment correction:** the owner then spotted remaining offsets on the right and bottom. The first traced version incorrectly treated the source as 1280×1280. Its actual PNG is 1254×1254. The drawing and hover masks now use that same true coordinate space, with an explicit source-dimension check. Pixel sampling also corrected the central right-hand band to source x943–989 (its outer path ends at x990); the bottom band spans y922–957 at the center. Refreshed magnified Gold, Diamond and Glowstone renders were inspected along with the reduced narrow layout and material hover. This correction changes the mask mapping and right edge, not the source illustration.

## Inspection

The actual browser renders were inspected at 1100 and 390 CSS pixels, with normal and 68% recipe scales. [Normal comparison](desktop-normal.png), [reduced comparison](desktop-reduced.png), [narrow comparison](narrow-normal.png), [narrow reduced comparison](narrow-reduced.png) and [material hover](desktop-hover.png) retain the current fitted/original frame captures. The 184×150 diagram uses the existing four-seat positions. All five selectors were exercised; [gold close-up](rim-detail-gold_block.png), [diamond close-up](rim-detail-diamond_block.png), [iron close-up](rim-detail-iron_block.png), [emerald close-up](rim-detail-emerald_block.png) and [glowstone close-up](rim-detail-glowstone.png) retain magnified renders. These are the same illustration and masked rendering, not new generated art. There were no page errors, horizontal overflow or clipped material markers. Frame hover shows the retained material, the String owns its inner hover, and keyboard focus reaches the material with its exact label. [Inspection data](inspection.json) records measured bounds and selected materials. This is not native Minecraft presentation verification.

The item and block-face PNGs in `icons/` are unchanged 16×16 textures extracted from the locally cached Minecraft 1.21.1 client archive. The block faces stand in for the native block item icons that the actual viewer should render. Existing `../ritual-image-concepts/parchment-v7.png`, `plinth-v7.png` and `spellstone-v7.png` provide the unchanged approved illustration. No new thread artwork was generated.

`capture.mjs` repeats the browser inspection using Playwright. Optional `VESTIGE_PLAYWRIGHT_PATH` and `VESTIGE_BROWSER_PATH` select an existing library/browser installation; no browser download is required. Runtime, packaging and Minecraft behavior tests were not run for this documentation and browser study.

## Native integration

Extend the common display model with bounded per-seat installed-material requirements, separately from consumed offerings. Populate the five entries from the authoritative thread definitions; do not repeat the recipe or material mapping in each viewer. Attach each requirement to the displayed seat holding String, so shapeless example placement keeps the pairing intact. Both viewers must index the retained material and preserve native lookup, with the frame's hollow hover region distinct from the offering slot. Render the material through the active Minecraft block atlas rather than bundling copied vanilla textures. Share frame placement with the existing diagram and scale it with EMI's holder.

The concurrent thread implementation now supplies `MagicalThreadRecipe.types()` and `ingredients()`, with each type exposing `id()`, `material()` and `item()`. These are the source for public thread entries. `RitualViewerClient.displays()` currently appends the shard and two device displays locally; the thread entries belong on that public route. `RitualDisplays.Entry.output()` must also support their actual output items instead of falling through to its existing generic shard result.

Any synchronized schema extension must use the project's explicit protocol versioning. Preserve concealed spell recipes and personalized recipe knowledge; public thread displays may expose their ingredients and selector immediately. Validate actual native lookup, hover, small-holder appearance and coexistence behavior after implementation. The current viewer has no socket-requirement field or overlay, and this prototype changes no native Java code.

**Native follow-through:** The owner approved the corrected frame and authorized shipping it. [Native implementation, final Minecraft captures and packaged-build verification](../../art/thread-imbuement-native/README.md) now cover JEI, EMI, both and neither viewer. This browser directory remains the preceding design study.
