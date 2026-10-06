# Approved Plinth and Spellstone framing

**Owner approved and locked, October 6:** “Yeah, this is perfect. Lock it in.” The approved narrow corner strips and cap/foot edges now ship on all **36 cosmetic finishes**, with matching cap/support edging on the Spellstone. Native material textures retain their own color and pattern; only the trim faces use **217/255 brightness** (`#d9d9d9`), about 15% darker. The world and inventory color handlers are part of the normal mod.

The shared [geometry author](../../../../tools/apparatus_framing.py) preserves the original solid elements, collision, receiving heights, diamond details and sockets. Plinth column strips continue across joins with rim trim only at the actual cap/foot. Quartz and Purpur corners follow their pillar shaft textures. Cosmetic finishes remain outside recipe and attunement identity.

## Native appearance

These images are unchanged copies of actual Minecraft 1.21.1 / NeoForge 21.1.72 framebuffer captures using the final production assets, without a preview resource pack:

- [Approved Plinth](plinth.png) and [Spellstone](spellstone.png), with the same cameras as the approved comparison.
- [Stone, Quartz and Polished Deepslate](variants.png).
- [Stacked Plinths](columns.png) and [independent imbuements](imbuements.png).
- [All 72 inventory icons](items.png), covering all 36 finishes.

All five world views and the inventory sheet were actually inspected. The comparison page was inspected at narrow and desktop widths with no horizontal overflow; all images loaded after reloading, and the temporary viewport was reset. [Browser view](browser.jpg).

## Verification

The isolated Java 21 production build, **105 unit tests** (zero failures/errors/skips) and Kithkyn compatibility passed. Model authoring, historical preview authoring and the independent geometry/seam audit pass. The audit checks every finish, unchanged original solids, face tint/texture routing and continuous column joins. No new mechanic was added; the collision class is byte-identical to the pre-trim approved capture. The world behavior suite was not repeated for this cosmetic change.

The five final world jobs recorded **145 frames**; inventory recorded **48 frames**. Loaded assets and presentation classes match the tested package. The three full-palette world jobs each verify all **252 apparatus model hashes**; the inventory view verifies **144 apparatus models** plus four existing item assets. [Native verification](native-verification.json), [model and seam audit](model-and-seam-audit.json), [package verification](package-verification.json).

The immutable reviewed package is `run/apparatus-framing-locked-package/vestige-0.1.0.jar`, SHA-256 `caa02ebb439257ead82d4678d3264f84a4c93c47eeedd42fca2257d2c7057f98`. The testing pack contains the newer shared Standing Stone menu package, SHA-256 `2cd4a31461e8ca8943ae5a5e947ec4a2bdc72881075136323535df9e8812eca5`. All 252 approved apparatus models and the production tint, renderer and collision classes are byte-identical to this reviewed package; [shared package parity](shared-package-verification.json) and [independently verified installation and backup](prism-install.json) record the details. Source language JSON was then normalized to the canonical Unicode escaping required by both authors; all keys and values remain identical to the installed jar. The existing Prism profile uses Minecraft 1.21.1 / NeoForge 21.1.248. Installation does not launch or restart it, and the complete Prism pack was not playtested by this capture pass.
