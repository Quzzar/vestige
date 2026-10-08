# Locked resource ratio verification

October 8, 2026. The owner accepted **1 full heart = 2 full hunger icons = 30 XP points = 40 mana**. Current decisions and remaining work are in [resource payments](../../design/resource-payments.md) and the [follow-up balance review](../../resource-payment-balance-review.md).

- **189 JUnit tests passed**, zero failures/errors/skips across 43 suites. Covers shared conversion quantities and rounding, mixed 75% exchange bounds, exact Eye prices, compiled source shaping, catalogs and equipment composition. [Suite counts](unit-results.json).
- **33 required Minecraft tests passed**. Covers all Eye resource prices and wear, 29-XP/three-food-point rejection, nonlethal health limits, cancellation/refunds, actual mixed typed payments, actual Exhausting cost/damage, thread-to-wand behavior, Standing Stone XP transactions, item affordability and the unchanged mana recovery/lifecycle. [Final run](final-run.log).
- Build and `verifyKithkynCompatibility` passed. [Source/package receipt](evidence.json) verifies exact packaged Spellshaping/lang resources and key runtime classes.
- Authoring/balance checks passed: 56 Spellshaping rules, 214 explicit recipes and current base balance reports, seventeen Python balance tests and 490 leyline parity fixtures.
- [Complete conversion inventory](conversion-inventory.json): 214 definitions, 220 base mana entries, no base health/hunger/cooldown costs, two fractional exchange rules and no remaining Spellshaping cooldown factors.
- Current documentation links and scoped whitespace checks passed.

The first Minecraft run passed 29 of 30 tests and failed the newly added XP boundary fixture. It initialized level four plus one point, which is 41 XP, while asserting 29. The fixture now uses level three plus two points. The final run passes all thirty prior cases plus three mana lifecycle/recovery cases. [Initial failure log](initial-fixture-failure.log). Production payment logic required no correction for that fixture mistake.

This uses the current combined working tree with an isolated Gradle build/cache. Earlier shape/payment receipts preserve their own provisional builds. Native viewer screenshots remain unverified after the earlier capture timeouts; this numerical correction supplies no new art/client appearance receipt. It does not install a testing pack or publish changes. The common ratio is accepted; survival recovery, resource farms and repeated escape/casting remain playtest work. Hourglass implementation, Standing Stone alternate payment routes and the held Fluxed Flint redesign remain outstanding.
