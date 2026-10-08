# Wand recovery removal · October 7, 2026

The owner removed the separate sixty-second wand recovery. Wands retain composed resources and preparation/channel time, existing component durability and deterministic initial wear. Complete casts and paid forfeits can prepare again immediately. Explicit data-pack spell cooldown costs remain supported; the shipped catalog has none.

The obsolete reservation recovery hook, source-family timer map and wand cooldown constant are removed. Source validation, initial payment/wear, cancellation, final-use breaking, observer behavior and paid recasts remain on the shared runtime.

**Passed:** production build, sibling Kithkyn compatibility, 177 JUnit checks and 56 native Minecraft tests in a fresh world. The world filter covers wands, eight tips, staffs, source preparation and mana. Repeated use of the same wand, changing to an economy-core copy, scroll/wand transitions and offhand final-use replacement verify the new behavior with real mana and wear. Model/texture/trait authoring checks and both changed generated documentation checks also pass.

The initial native run passed 55 checks and exposed an incorrect expected balance in the economy-core repeat test. The cost rounds six × 0.85 to five mana, leaving 83 rather than 82. Only the expectation changed; the final fresh-world run passed all 56. Compressed initial and final logs are retained.

The source snapshot is `/private/tmp/vestige-wand-no-recovery-20261007-source`. All seven changed Java files matched the shared workspace when verification was recorded. [Source hashes](source-sha256.json) pin all source files; [verification](verification.json) pins the staged artifact and exact results. The tested jar is staged at `build/wand-no-recovery-review/libs/vestige-0.1.0.jar`; the testing pack was not modified. Other chats remain active, so this is a specific tested snapshot.

No UI rendering changes or new client capture were needed for this server-side timer removal. The accepted crosshair edge, straight draw-back and item-relative mana shading retain their previous presentation evidence. Repairing and shared ritual-input volatility are separate design/implementation work.
