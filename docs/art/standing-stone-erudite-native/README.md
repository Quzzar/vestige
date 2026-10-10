# Erudite Standing Stone — native client follow-up

October 10, 2026. These eighteen images are unedited Minecraft 1.21.1 / NeoForge 21.1.72 framebuffers from a fresh client with Kithkyn and JEI. They supersede the initial XP-only Erudite menu evidence.

Erudite now pays **75% XP plus one-third mana**, resolved from the unrounded ordinary distance budget: **24 XP becomes 18 XP + 8 mana**. The exchanged quarter carries a 4/3 mana premium. The server-authored menu renders both amounts and resource marks side by side, dims unavailable destinations and preserves the current screen during affordability updates. Neither resource automatically substitutes for the other.

- [Funded paired fare](jei/menu-erudite.png).
- [Enough XP, insufficient mana](jei/menu-erudite-no-mana.png).
- [Enough mana, insufficient XP](jei/menu-erudite-no-xp.png).
- [Both resources insufficient](jei/menu-erudite-unaffordable.png).
- [Unchanged Lapis/Pearl ritual](jei/recipe-erudite.png).

The fixture also rechecks all five routes, all **185 public and bound-output recipe variants**, the inventory and all five recipe screens. Its [native verification record](jei/verification.json) includes the exact paired quotes and verifies that balance refresh does not reopen a closed menu. JEI indexes 741 rituals in the scoped publication build. The shared menu is viewer-independent; unchanged EMI integration retains its prior evidence rather than claiming a new EMI run.

**Passed:** 228 scoped and 246 combined unit tests, Java 21 builds and Kithkyn compatibility, and all 50 targeted Minecraft tests with Kithkyn. Actual world tests cover exact paired debit, either insufficient currency, nonlethal/food boundaries, payload round trips, all recipes/finishes, canceled/redirected/throwing travel, callback resource changes, reentrancy and refund of both costs and mana recovery delay. A callback removing mana cannot waive its payment. [Release receipt](release-receipt.json) records both artifacts and the atomic combined-build installation with backup.

No item/block textures changed. Resource-generation economics, repeated long-distance balance and multiplayer persistence remain playtest follow-ups. Matching protocol-6 builds are required; saved route identities and shard keys are unchanged.
