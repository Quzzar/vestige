# Ordered rituals and resource payment verification

October 8, 2026. This records the owner-directed correction after the [pre-fix audit](../../ritual-shape-and-payment-audit.md). Current decisions are in [ritual patterns](../../design/ritual-crafting.md) and [shared resource values](../../design/resource-payments.md).

- Full JUnit suite: **189 passed, zero failures/errors/skips**, across 43 suites. Includes all seven changed model/viewer/payment suites and full catalog/rule compatibility checks. [Counts by suite](unit-results.json), [run log](unit.log).
- Focused native Minecraft suite: **69 required tests passed**, with Kithkyn 1.0.0 loaded. Covers four rotations and rejected permutations, all Standing Stone finishes, copied shard keys and local selectors, active empty-seat cancellation, inactive outer edits, ordinary centered outputs, retained materials, repair permutations/backfires/exact components, Homebound Eye payments/refunds and dimension travel, actual Exhausting mana/hit behavior, mixed atomic exchanges and the thread-to-wand chain. [Build/world log](world-and-build.log).
- Package build and `verifyKithkynCompatibility`: pass. Runtime class hashes and exact packaged resource agreement are recorded in the [receipt](evidence.json).
- Authoring checks: 56 Spellshaping rules and 214 explicit ritual recipes pass; 11 ritual recipe tests and 17 spell balance tests pass; 490 canonical leyline fixtures remain synchronized.
- Changed-file whitespace and documentation links: pass.

Initial main/test launch failures were isolated-build output-path problems, before gameplay assertions ran. Pinning destinations after task-graph creation, using a separate Gradle project cache and disabling configuration-cache reuse fixed the harness. Final verification uses the same current shared source and records its hashes; it is not a clean-branch or installed-pack receipt.

Native viewer inspection remains **incomplete**: both the combined JEI/EMI attempt and the EMI-only retry timed out without producing screenshots. Ordered viewer positions are unit-tested; this pass does not claim native presentation verification. [Combined capture diagnostic](client-jei-and-emi-failure.txt), [EMI-only diagnostic](client-emi-failure.txt). These resource values remain starting survival-playtest tuning. Standing Stone alternate payment routes and the Kairotic Hourglass remain future implementation; the held Fluxed Flint composition/art redesign is not part of this correction.
