# Equipment behavior review

**Current status, October 8, 2026:** Wardweave and Cinderweave gameplay is approved and implemented. The owner also approved their mage armor v2 worn appearance after viewing the native Minecraft captures. [Accepted artwork](../magic-equipment-armor-v2/README.md) and [the exact asset/model approval record](../magic-equipment-armor-v2/approval.json) supersede the older robe artwork comparisons below. The review cards now link to those accepted captures and have no outstanding robe decisions.

| Item | Worn artwork | Recipe | Exact ability and stats |
| --- | --- | --- | --- |
| Wardweave Robes | Mage armor v2 approved | Accepted, implemented | Approved, implemented |
| Cinderweave Robes | Mage armor v2 approved | Accepted, implemented | Approved, implemented |
| Wayfarer Boots | Feather Tabs preferred | Proposed | Awaiting approval |
| Dawnsight Hood | Side Fold preferred | Proposed | Awaiting approval |
| Patchwork Robes | Pocket Coat preferred | Proposed | Awaiting approval |
| Spiderstep Boots | Unselected | Proposed | Awaiting approval |

[Canonical robe behavior](../../design/robe-behavior-review.md) retains the approved crafting, traits, mana, durability and enchantment rules. [The broader equipment design](../../design/magic-equipment.md) records the remaining proposals. This approval does not authorize another clothing package. The next equipment review covers the boots' activation, hood control and Patchwork storage, plus their recipes, durability and repair rules.

This approval pass records existing artwork and updates review labels/data. Production textures and models match the reviewed hashes. It does not rerun Minecraft, publish or install a build. Prior browser receipts below apply to their historical review snapshot, not this label/data update.

## Historical October 7 review snapshot (superseded for robes)

October 7, 2026. The owner asked to continue artwork while retaining final approval of exact item behavior. [Open the six-item review](index.html). The cards pair current native inventory captures with concrete proposed abilities, controls, mana costs, recovery times and recipes.

[The design](../../design/magic-equipment.md) remains the implementation reference. [Review data](review.json) is a presentation snapshot, not executable recipe or balance data. No equipment abilities, survival recipes, worn models or registrations are implemented in this review.

### Approval record

| Item | Artwork | Recipe | Exact ability and stats |
| --- | --- | --- | --- |
| Wardweave Robes | B Folded Mantle preferred | Accepted | Awaiting approval |
| Cinderweave Robes | A Coal Cuffs / B Brick Coat undecided | Accepted | Awaiting approval |
| Wayfarer Boots | C Feather Tabs selected | Proposed | Awaiting approval; activation control also open |
| Dawnsight Hood | D Side Fold preferred | Proposed | Awaiting approval; activation and optional mana capacity also open |
| Patchwork Robes | C Pocket Coat preferred | Proposed | Awaiting approval; object palette and pocket access also open |
| Spiderstep Boots | Unselected | Proposed | Awaiting approval |

Accepted shared decisions: very little robe armor, increased robe maximum mana, chest slot, one additional effect; four matching wool of one vanilla color, color follows wool, fixed identifying trim and later ordinary armor dyeing. Exact armor, capacity, effects, costs and controls remain proposals. The original art preferences retain their status until the owner explicitly locks them.

The owner subsequently accepted **uncapped trait-power growth, durability on every armor piece and wear on hits absorbed by its magic**, with no separate activation or passive-uptime drain. Actual prevention cannot exceed incoming damage. This approval establishes the shared direction; it does not approve every item package or its starting numbers. **Enchantment policy is now accepted:** ordinary compatible armor enchantments are permitted, with **Unbreaking and Mending excluded** because they interfere with authored durability and material repairs.

Remaining finishing recommendations: leather-equivalent durability totals (80 chest / 55 head / 65 feet) and matching-thread anvil repair restoring up to one quarter per thread. Those quantities await approval; the exclusion of Unbreaking/Mending is locked. Vanilla durability values were checked in the pinned Minecraft 1.21.1 `ArmorItem.Type` and `Items` source: chest/head/feet base 16/11/13 multiplied by leather's factor 5. The accepted wear direction includes protected hits even when the robe prevents all health damage; it must charge each hit once, without also charging on activation.

Artwork previews reuse unedited native Minecraft captures. The gallery displays an enlarged region using CSS and links every complete original screenshot. Cinderweave A is an explicitly labeled comparison illustration, not a selected design. Spiderstep intentionally links its four-option review without implying a default art selection.

[Browser verification](verification.json) exercises all six cards: every pending status and accepted/proposed recipe distinction is visible, all applicable native images load, and the normal browser view has no horizontal overflow. [The review screenshot](review.jpg) preserves the first robe proposal. Local artifact links and original model packaging checks pass. The all-color gallery's twenty design/group combinations and smaller-menu disclosure were also exercised with their native images loaded.

### Next decisions

Wardweave and Cinderweave were subsequently approved and implemented, including 2 armor / +25 maximum mana, their shared durability and enchantment policy. Their worn appearance is available for review in the native captures. Then settle the boots' activation, the hood's reveal control, Patchwork's storage purpose and interaction, remaining recipes, and shared durability totals/repair quantities.

The owner requested that focused review on October 7. [The two-robe recommendation](../../design/robe-behavior-review.md) specifies hit eligibility, charge ownership, recovery/removal, damage ordering, trait snapshots and numerical examples. The first capped version was superseded by the owner's accepted durability direction: Wardweave duration/protection and Cinderweave's protection budget now keep growing. Fire prevention can reach the full incoming hit, with robe wear still paid. Exact baseline stats and control/recovery rules remain in review; artwork retains its recorded status. Ordinary compatible armor enchantments exclude Unbreaking and Mending under the later accepted decision.

After approval, implement only the agreed equipment scope on native effects and payments, with targeted world behavior checks. Finish inventory and worn appearance before claiming visual completion. The [fixed-trait staff design](../../design/staff-crafting.md) is a separate accepted native implementation; its earlier equipment-planning paragraph is superseded.

### Shared trait scaling review

The owner approved the shared architecture on October 7. [Shared item magic](../../design/item-abilities.md) documents the implemented foundation and the remaining equipment adapters. Wardweave and Cinderweave are approved and implemented; the other four behavior packages remain pending approval.

`ability-proposals.json` contains pure native-readable trait/variable declarations for all six items. The page's sliders show proposed changes from that same data: +25% Time gives Wardweave five seconds, Wayfarer 3.75 seconds and Dawnsight 7.5 seconds. Maximum mana bonuses, storage counts, payments and recovery remain fixed in these proposals. `review-data.js` bundles both JSON sources so the page needs no fetch when opened locally. Regenerate with `python3 tools/author_equipment_review.py`; `--check` detects drift.

The existing `review.jpg` and layout verification describe the previous page. Browser security blocked the local file URL during this pass; the new slider layout has not been visually verified. Native formula and gameplay checks are recorded in development status.
