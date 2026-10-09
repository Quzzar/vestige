# Fractious Fluxed Flint · October 9, 2026

The owner approved a stronger repair choice with more chance of backfire. Retained **Magma Block beneath the Flint offering** selects Fractious during the existing ordered construction. It repairs up to **50% of target maximum durability per activation**, rounded upward, with **20% intrinsic Flint backfire chance**. Its 128-point total budget and one-to-one transfer cost remain unchanged. Missing damage and remaining Flint budget limit every transaction. A Flint-triggered explosion preserves the exact Flint without wear and destroys the repair target.

The new choice combines with Stabilized/Reinforced in eight exact sets; Restive, Audacious and Impetuous name the new combinations. [The complete table](../../design/magical-repair.md#crafted-imbuements) records caps, budgets and risks. Existing variants and public recipe IDs are preserved. Both optional viewers derive all eight public construction patterns from the live palette. Artwork, Rare name color and shimmer are unchanged.

## Verification

- **217 model tests across 47 suites**, including all eight variants, full names, budgets, risk and every construction rotation with either Reinforced ingot.
- **All 376 required Minecraft tests with Kithkyn**, including four new cases for combined construction, forty-point exact-component transfer/save/load, higher-risk backfire and every variant's rounding/damage/budget bounds.
- **13 integrated-client assertions and five inspected native frames**: [inventory](native/inventory-beside-vanilla.png), [held/offered](native/held-and-offered.png), [ritual lift](native/repair-in-progress.png), [output](native/repaired-output.png) and [durability](native/durability-after-repair.png). The actual outer-only repair restores twenty Staff points and spends twenty Flint points while retaining its Fractious identity and 128-point budget.
- Packaging and Kithkyn version compatibility pass. The packaged changed classes match the tested compilation; loaded Flint assets and generated names match the release JAR.
- Authoring checks pass: **56 Spellshaping rules**, **490 leyline fixtures**, **82 adjective entries and 149 combined names**.

The initial world attempt exposed a test timing mistake: cleanup was asserted at tick 25, before the existing 32-tick explosion animation ends. Only that scheduled assertion changed to tick 35. An interrupted rerun ended before suite completion; the final fresh run above passed in full. Raw diagnostics remain local outside this repository. [Machine-readable results and hashes](verification.json) retain the evidence without publishing diagnostic payloads.

Optional JEI/EMI screens and hovered-tooltip pixels were not inspected. Budgets, transfer caps and risks remain initial survival tuning.

## Installation

The tested gameplay is installed in **Kithkyn Testing**, with the previous JAR retained in a verified timestamped backup. The installation preserves the Hourglass chat's already-installed texture preview as its sole difference from this scoped release; every release class and data file matches. Worlds and other mod JARs are unchanged. [Installation hashes](installation.json) distinguish release and installed packages. Restart Minecraft to load the update.
