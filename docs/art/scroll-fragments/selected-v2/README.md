# Selected tiny scroll fragment and gold symbols

**Owner-approved and locked October 6, 2026.** The final native scrap, all 52 glyphs and muted-gold name symbols are accepted. [Approval record](approval.json) pins the source, packaged texture, Minecraft inspection and verified jar.

October 6, 2026. The owner approved the [second torn-corner revision](../options-v1/02-torn-corner-v2.png) after requesting a less geometric shape without an arrowhead tip, then authorized finishing the item with a differently colored trait symbol in its name.

![Actual Minecraft framebuffer](native.png)

The native texture is [16×16](scroll_fragment.png), with a 7×8 opaque footprint and 41 opaque pixels. [Enlarged exact pixels](texture-preview.png) show the final import. `tools/prepare_scroll_fragment_texture.py` converts the approved source with nearest-neighbor sampling and binary alpha, fitting the long side into eight pixels. No additional image was generated during integration. The earlier [generation prompts and mode](../options-v1/README.md) and [exact final editing prompt](../options-v1/02-torn-corner-v2-prompt.txt) retain the source provenance.

Names display **Scroll Fragment: [symbol]**. All 52 built-in/shipped traits have distinct original glyphs. Only the symbol uses the custom font and muted gold (`#d6b46a`); the ordinary text stays in Minecraft's default font/color. Unauthored traits retain their text fallback, including foreign namespaces.

The unchanged 1920×1080 [Minecraft framebuffer](native.png) was visually inspected: all 52 symbols and small paper items render; the actual Fire hover has white prefix text and a gold glyph; `addon:fire` uses its ordinary name. [Capture metadata](capture.json) pins all five loaded fragment resources and the selected color. The screen creates no world and exits after capture. The [inspected class hashes](inspected-class-hashes.json) preserve the four fragment class files used by the client.

**Verified:** Java 21 `build`, `verifyKithkynCompatibility` and all **104 unit tests** pass, with zero failures/errors/skips. The [focused fragment results](fragment-tests.xml) include font/color assignment, distinct visible glyph masks and unknown/foreign fallbacks. Fragment/font authoring checks, texture drift/binary alpha checks and Standing Stone authoring drift checks pass. [Package verification](package-verification.json) checks ZIP integrity, compares all five inspected resources and four fragment classes to the jar, and verifies the colon name format. Raw [final build](validation.log), [focused build](fragment-validation.log) and [native client](native-client.log) logs are archived. The verified jar is saved in `run/scroll-fragments-package-selected-v2/vestige-0.1.0.jar`.

The initial build encountered a generated Standing Stone initializer exceeding Java's method-size limit after concurrent expansion to sixteen forms. `tools/author_standing_stones.py` now emits one initializer per profile; all 1,882 ordered collision coordinate tuples were verified unchanged at this repair step. No profile selection, geometry or shape-building logic was changed by that repair. The first broad test run also saw two preceding three-profile test expectations; [those initial results](standing-stone-initial-tests.xml) and its [log](full-suite-initial.log) are retained. The concurrent Standing Stone work subsequently updated those tests, and the final full suite passes.

Both build output and Gradle project-cache directories were isolated from other workspace jobs. World behavior tests were not repeated for this fragment presentation change. No Prism installation occurred in this task.
