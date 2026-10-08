# Native diagonal readiness study

October 8, 2026. The owner preferred blue-gray and requested top-left to bottom-right and top-right to bottom-left clearing sweeps. [Visual comparison](../../art/item-readiness-overlay-v2/README.md). The previous vertical study remains preserved in its v1 folder.

The isolated driver reuses the prepared Java 21 / Minecraft 1.21.1 / NeoForge 21.1.72 build and compiles only the opt-in comparison screen and frozen capture-router exclusion. The screen compares the previous opposite vertical directions with crossing diagonals at GUI scales three and two. It uses actual item icons, native inventory slots, server mana regeneration and the native client cooldown timer. The twelve-mana display price and 300-tick timer are example inputs rather than new item abilities.

Diagonal fills use integer horizontal runs inside each 16×16 icon. The boundary inverts the square's triangular area, keeping coverage proportional to the remaining fraction within pixel rounding. A focused geometry check verifies monotonic coverage, mirrored layer area, icon bounds and full/empty endpoints. Native rendering and decoded playback still require visual inspection.

The driver freezes the executed Java sources, native cooldown source excerpt and runtime hashes. A temporary collision fixture avoids unrelated standing-stone initialization only on the isolated classpath; no standing stone is displayed, the fixture is never packaged, and original bytes are restored in `finally`. The preview jar contains the original collision implementation. Optional Kithkyn is absent.

The encoder verifies both views, initial shortage/cooldown, increasing timestamps, mana clearing independently before cooldown, final readiness, loaded/package study equality, restored collision and the approved robe resources. MP4s use native framebuffer dimensions and observed elapsed capture timing. The GIF crops the GUI-scale-three comparison panel without resizing, then reduces its palette. Every native frame, source and video is hashed in `verification.json`.

Reproduce using the driver-recorded host-specific prepared runtime:

```sh
python3 docs/verification/item-readiness-diagonal-2026-10-08/capture_client.py
python3 docs/verification/item-readiness-diagonal-2026-10-08/encode_and_verify.py
```

This is an isolated appearance study. Production UI, recovery synchronization, item mechanics and approved art remain unchanged. No new unit/world suite, optional Kithkyn co-load, installed testing-pack update or publication is claimed. Final diagonal appearance remains an owner review decision.

**Executed result:** all checks pass for **160 unedited native frames**, all retained in two silent 1920×1080 videos. The GUI-scale-three loop is a 912×516 panel crop with 161 frames over 16.1 seconds. Native full/mid/final states and both GUI scales were inspected; decoded crossing, cooldown-only and GIF frames were also inspected. The fills remain inside their icons, crossing edges and compounded overlap are visible, mana clears independently and final-ready icons are clear. Approved robe hashes and restored/packaged collision checks pass. Exact hashes and timings are in `verification.json`.

**Timing limitation:** the first view resumes mana regeneration earlier in wall-clock playback than the second, and the startup log records a server backlog. These clips review appearance and independent clearing, rather than validating recovery timing or gameplay balance. The second view retains the expected initial mana delay. Production mana rules were not modified.
