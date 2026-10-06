# Stone Plinth and Spellstone framing previews

These are native Minecraft captures, not painted mockups. All retain the production Stone bodies and original Minecraft texture tiles. The capture-only pack substitutes cosmetic carriers without adding blocks or altering collision, socket behavior, recipes or identity. The owner has now [locked in the final edging](locked/README.md) for all 36 production finishes; the earlier capture-only studies below retain their review history.

## Approved direction: corner strips and outer rim edges

**Owner clarification, October 6:** combine B's narrow corner strips with strips around the outer cap and foot edges. The raised shaft ribs in D were an earlier interpretation; they are not the clarified placement. Give the Spellstone similar edge framing.

- **E — Corner strips + rim edges:** [current image](corner-edges-middle.png), [model](models/corner_edges.json). This combines B's unchanged corner strips with A's flush outer rim trim. The shaft and center imbuement area remain open. Stacked variants retain corners across joins and rim trim only at the actual cap/foot.
- **Matching Spellstone:** [current framed image](spellstone-edges-middle.png), [plain image](spellstone-plain.png), [framed model](models/spellstone_edges.json). Thin edging follows the top/bottom sides of the cap and the angled support edges. Its three solid pieces, Diamond corner pixels, receiving top and native Astral Seal remain unchanged. Framing quads sit behind the existing Diamond pixels.

### Pre-lock-in review history

**Contrast correction, October 6:** the owner found the preceding version too faint. The current trim uses native Stone at **217/255 brightness**, about **15% darker**. The same narrow corners and rim/support strips are retained. Only trim faces have tint index 0; the native apparatus renderer now supplies Minecraft's block color to its model renderer. The opt-in capture handler colors those faces; untinted production models retain their original rendering. No new raster tile is introduced.

**Pre-lock-in verification:** isolated Java 21 compilation/resource processing and all 22 author model checks pass. Two final native views produced **101 frames** after a loading pass. Five loaded model hashes and the loaded tint/OfferingRenderer hashes match their capture sources. Both final views were inspected; the Plinth now has 22,349 darkened pixels relative to the preceding faint view, confirming that the contrast adjustment actually renders. Their reference cameras match. [Verification record](middle-edges-verification.json). Stacked/imbued appearance has not been separately captured. Production model assets and the testing-pack jar were not replaced.

**Previous faint version:** [Plinth](corner-edges-faint.png), [Spellstone](spellstone-edges-faint.png), [archived models and tint source](models/faint-edges/). These 97 frames are the actual images the owner found too faint. Subsequent inspection showed that OfferingRenderer's fixed white model color bypassed the registered tint: the intended 240/255 value was not applied in the world. Their [verification record](faint-edges-verification.json) now carries that correction. An initial 217/255 attempt likewise produced an identical Plinth image and was rejected: [diagnostic record](diagnostic-middle-edges-verification.json), raw frames under `native-middle-edges-tint-bypass/`. Corrected final images use the native block color and are above the faint pair on the page.

**Previous softer contrast:** [Polished Tuff Plinth](corner-edges-soft.png), [Polished Tuff Spellstone](spellstone-edges-soft.png), [archived Tuff models](models/soft-edges/). Their two final native views produced 100 frames with matching hashes and reference cameras: [verification record](soft-edges-verification.json). The first short Plinth capture showed only sky and remains under `native-soft-edges/` as diagnostic evidence. The Tuff pair remains in the collapsed earlier studies.

**Earlier contrast:** [darker Plinth](corner-edges-strong.png), [darker Spellstone](spellstone-edges-strong.png), [original framing models](models/strong-edges/). Their three original captures produced 73 frames with matching resource hashes: [verification record](edge-trim-verification.json). These are in the collapsed earlier studies on the comparison page.

## Earlier studies

- **A — Rim bands:** [image](bands.png), [model](models/bands.json).
- **B — Corner strips:** [image](corners.png), [model](models/corners.json).
- **C — Framed sides:** [image](panels.png), [model](models/panels.json).
- **D — Corner strips + ribs:** [image](corner-ribs.png), [model](models/corner_ribs.json). Narrow corner strips continue along the shaft. Two pairs of shallow raised ribs wrap the shaft just above the foot and below the cap, leaving the socket area clear. Each rib projects 0.18 of a model unit; the original cap, shaft and foot stay unchanged.

[Comparison page](index.html). All displayed images are byte-identical copies of native frames. Earlier studies use frame 19; the Tuff pair uses frame 49; the preceding faint pair uses final frames 49 and 46; the pre-lock-in Plinth/Spellstone views use final frames 50 and 49. A/B/C retain their full frames and resource hashes under `native/`; D is under `native-combined/`; the darker E and Spellstone pair are under `native-edges/`; Tuff views are under `native-soft-edges-ready/`; preceding faint views are under `native-faint-edges/`; current views are under `native-middle-edges/`. `python3 tools/author_plinth_framing.py --check` validates all twenty Plinth single/base/shaft/cap models and both Spellstone models. Stacked D columns continue their corner strips at joins and put ribs only at the base and cap, avoiding repeated collars through the shaft.

**D verification, October 6:** Java 21 compilation/resource processing passed using separate build and Gradle state directories. The isolated Minecraft capture produced 22 frames over 2.07 seconds. All four loaded D model hashes match the preview pack; the final native frame was visually inspected. [Verification record](combined-verification.json). Stacked and imbued D appearance has not been separately captured. This earlier D study was never applied to production or the installed testing pack.
