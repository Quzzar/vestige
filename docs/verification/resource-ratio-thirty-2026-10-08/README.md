# Thirty-mana resource equivalence verification

October 8, 2026. The owner revised the shared equivalent from forty to **thirty mana**, retaining one full heart (2 HP), two full hunger icons (4 food points), and thirty XP points. [Current payment authority](../../design/resource-payments.md). This supersedes the prior forty-mana receipt, which remains historical evidence.

- **189 unit tests pass** across 43 suites, with zero failures/errors/skips. Includes exact thirty-mana equivalents, the 7.5-mana food boundary, whole-heart rounding, mixed capped exchanges and current Homebound Eye prices. [Counts](unit-results.json).
- **11 required Minecraft tests pass**, covering all Eye routes, insufficient resources, nonlethal health, canceled XP/dimension travel and refunds, selector/crafting atomicity, mixed typed spell payments, actual Exhausting behavior and unchanged mana recovery/lifecycle. [Final run](final-run.log).
- Java 21 build and Kithkyn compatibility pass. Shared runtime classes, the Eye payment enum/world checks and packaged Spellshaping data match the compiled files/resources; the main-source archive matches the current affected Java source. [Source/package hashes](evidence.json).
- Spellshaping authoring checks pass for all 56 rules, and all seventeen Python spell-balance tests pass. Updated hourglass draft examples were checked against the thirty-mana exchange ceiling and existing 75% cap; those device variants remain proposals, not playable content.

The initial eight-case world run had one stale test expectation: successful cross-dimension travel still asserted sixty mana remaining instead of seventy. Correcting that expectation resolved the failure; the final run additionally includes three mana lifecycle/recovery checks. [Initial log](initial-old-expectation.log). No payment implementation change was needed for that failure.

The focused suite runs in an isolated temporary world/build against the current combined working tree. No new client/artwork inspection, testing-pack installation or publication is claimed. The hourglass remains unimplemented, and survival-economy balance remains playtest work.
