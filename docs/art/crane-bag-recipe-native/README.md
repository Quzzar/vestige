# Crane Bag · native recipe viewer evidence

October 9, 2026. Unedited Minecraft 1.21.1 / NeoForge 21.1.72 captures with Kithkyn and each optional viewer loaded separately.

- [Actual JEI recipe](jei/crane-bag-recipe.png), [JEI checks](jei/verification.json).
- [Actual EMI recipe](emi/crane-bag-recipe.png), [EMI checks](emi/verification.json).

Both production viewer screens show the four active inner offerings: Attunement Shard → Eye of Ender → Feather → Leather, producing Crane Bag at the center of the existing parchment diagram. Their own surrounding controls are native viewer UI. Each indexes exactly one recipe for both the generic display output and an actually bound bag, and Feather's usage lookup includes this recipe. The recipe is ordered, with whole rotations accepted; it does not reveal the source signature algorithm or a specific key.

The opt-in `NativeCraneRecipeCapture` uses the actual registered adapters, native recipe lookup and framebuffer. World tests separately execute two rotated constructions against one already-populated pool, cancellation after a changed shard, and invalid-order/unbound-key rejection. Approved item artwork and storage behavior remain unchanged. See [the accepted design](../../design/crane-bag.md) and [development status](../../development-status.md).
