# Native item-readiness UI study

October 8, 2026. The owner requested native animation/video of two overlapping mana/cooldown indicators and a saved existing-item imbuement work queue. [Visual candidates](../../art/item-readiness-overlay-v1/README.md); [implementation direction](../../design/item-imbuement-and-readiness.md).

The isolated driver copies the previously verified prepared Java 21 / Minecraft 1.21.1 / NeoForge 21.1.72 build, then compiles only an opt-in comparison screen and a capture-router exclusion into its own review classpath/jar. No production UI or item ability changes. Optional Kithkyn is absent. Current accepted worn-robe texture hashes are checked against this loaded resource set.

The screen uses native item/decoration rendering, actual inventory-slot pixels, the normal native client cooldown timer and real private server mana updates/regeneration. The native cooldown reference is unmodified. Candidate rectangles use the same 16-pixel geometry, with two tint choices and two clearing directions. The configured 12-mana example price is purely a display input. No ability activation, payment or normal inventory interaction claim is made for the candidate icons.

`source/vanilla-cooldown-snippet.txt` preserves the relevant pinned `GuiGraphics` source excerpt; `runtime.json` records the full source hash. `source/NativeReadinessPreview.java` is the complete study. The frozen capture router only adds this capture kind to its exclusion list. Neither is added to the production source tree. As in the prior robe review, a temporary unit-cube collision fixture avoids costly unrelated standing-stone initialization; no standing stones appear. It is never packaged and is restored in `finally`.

`encode_and_verify.py` checks all four views, increasing frame timestamps, initial shortage/cooldown, mana clearing before cooldown, final readiness, restored original collision bytes, packaged study class equality and approved robe textures. It encodes actual elapsed timing into four silent videos at the framebuffer's native dimensions, probes their stream/duration, and freezes every frame/video/source hash in `verification.json`. On this Retina host, the requested 960×540 window produces 1920×1080 native frames. One initial opposite-scale-three frame precedes receipt of the private zero-mana packet; it is retained as native evidence but omitted from playback, which begins at the first synchronized state. No new unit/world suite is claimed for this isolated appearance study. Final native frames and decoded video frames must also be visually inspected.

Reproduce:

```sh
python3 docs/verification/item-readiness-overlay-2026-10-08/capture_client.py
python3 docs/verification/item-readiness-overlay-2026-10-08/encode_and_verify.py
```

The driver records host-specific prepared build/runtime paths. The installed testing pack, approved assets, production gameplay and publication remain unchanged. Tint, opacity and clearing direction await owner selection.

**Executed result:** 287 native frames, with 286 used in the four encoded clips after omitting the one initial unsynchronized frame. All checks pass. Native initial/mid/final frames from both clearing directions and GUI scales were inspected; decoded opposite-direction video at seven seconds and matching-direction video at twelve seconds were also inspected. Labels and slots fit, both tint candidates preserve icon readability, overlap remains visible, mana clears independently before cooldown and all final-ready icons clear. The GIF uses the same encoded opposite-direction footage. This is visual comparison evidence, not a completed server-driven recovery indicator for normal gameplay.
