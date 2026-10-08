# Native vanilla-style readiness preview

October 8, 2026. The owner requested the same vanilla shade and clearing direction for mana and cooldown. [Review animation](../../art/item-readiness-overlay-v3/README.md).

The isolated Java 21 / Minecraft 1.21.1 / NeoForge 21.1.72 preview uses `0x7FFFFFFF` and the pinned native cooldown rectangle for both conditions. Each layer is drawn once; overlap composites the two white fills. The custom comparison screen renders actual items and native inventory slots at GUI scales three and two. Server mana regeneration and a native client cooldown timer drive a controlled twelve-mana / 300-tick display example. No new item ability, payment, production synchronization or normal inventory interaction is implemented by this preview.

The driver reuses the prepared robe-review build and compiles only the opt-in comparison screen and capture-router exclusion. A three-second settling interval precedes the first mana reset. The previously documented unrelated standing-stone collision fixture is isolated, never packaged, and restored in `finally`. Approved robe resource hashes and packaged/loaded preview classes are verified. Optional Kithkyn is absent.

`encode_and_verify.py` checks both views, native dimensions, increasing timestamps, initial shortage, mana clearing before cooldown, final readiness, restored collision and approved robe assets. Silent videos preserve observed elapsed timing. The loop crops the comparison panel without resizing. `verification.json` freezes source, frame, video and runtime hashes. Native and decoded playback inspection establishes the appearance separately from those checks.

Reproduce with the recorded host-specific prepared runtime:

```sh
python3 docs/verification/item-readiness-vanilla-2026-10-08/capture_client.py
python3 docs/verification/item-readiness-vanilla-2026-10-08/encode_and_verify.py
```

This is an appearance review; no new unit/world suite, optional co-load, installed testing-pack update or publication is claimed. The owner subsequently accepted the appearance: “Yeah, this is fine. This is sufficient.” [Acceptance](../../art/item-readiness-overlay-v3/approval.json) freezes the reviewed evidence separately from production integration. The original verification receipt preserves the state at capture time.

**Executed result:** all checks pass for **192 unedited native frames**, all retained in two silent 1920×1080 clips. The cropped loop is 912×390, with 160 frames over sixteen seconds. Full, mid and final native states at both GUI scales were inspected, along with decoded overlap/video and GIF frames. Both conditions use the same vanilla white and top-to-bottom clearing; the doubly covered area is visibly more opaque. Mana clears before cooldown and both final layers disappear. The settling interval avoids the earlier study's first-view mana-delay difference. Exact source, runtime, frame and video hashes are in `verification.json`.
