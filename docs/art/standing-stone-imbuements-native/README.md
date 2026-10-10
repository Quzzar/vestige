# Standing Stone imbuements — native client evidence

October 10, 2026. These are unedited Minecraft 1.21.1 / NeoForge 21.1.72 framebuffers from fresh local worlds with Kithkyn. No generated illustrations or composited UI are used.

Each viewer folder contains ten real destination-menu captures (five funded routes and five unaffordable routes), one inventory capture alongside vanilla ingredients, five ritual recipe screens, and its machine-readable verification record. JEI uses the scoped publication build; EMI uses the combined development build, which has additional independently approved equipment recipes. The total ritual counts differ for that reason.

The live client checks all **185** public and bound-output recipe variants in each viewer. Each of the 36 finishes has five primary routes. Stone Bricks additionally has a separate Chiseled Stone Bricks recipe, so its output lookup correctly returns two entries per route. Outputs carry finish and payment route without a private shard key. The Pearl's material frame indicates the selected socket; paired masonry stays coordinated.

Every menu comes from a real server-authored quote. Empty, Lapis, Amethyst, Moss and Soul Sand select ordinary XP, discounted XP, mana, hunger and nonlethal health respectively. Balance changes refresh the existing screen and disable rows. The final JEI run additionally verifies that a balance refresh after closing the travel screen does not reopen it.

Representative frames:

- [Mana route](jei/menu-mana.png) and [unaffordable mana](jei/menu-mana-unaffordable.png).
- [Health route](jei/menu-health.png) and [hunger route](jei/menu-hunger.png).
- [JEI Pearl/Amethyst recipe](jei/recipe-mana.png) and [EMI counterpart](emi/recipe-mana.png).
- [Actual inventory](jei/inventory.png).

The public construction catalog, menu units and routes are implemented. These screenshots are not balance playtesting, an overnight network-persistence test, or a new review of the existing 36 stone finishes. World tests independently exercise actual payments, crafting, saving/mining and canceled/throwing travel. EMI's development overlay reports existing untranslated Kithkyn support and Vestige repair tags; they are unrelated to this recipe integration. Older temporary-water model warnings are also outside this package. No Standing Stone model or recipe-index error occurred.
