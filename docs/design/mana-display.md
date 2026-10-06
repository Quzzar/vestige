# Native mana display

**Owner-approved first release, October 6, 2026.** The owner rejected the centered meter above the main HUD and clarified that health/food costs should show the actual count of symbols. The current direction moves mana into the bottom-right corner and replaces number-plus-heart/food marks with full/half icon groups. It supersedes the preceding centered-meter review; its shared violet mana rune remains.

## Shared symbol and palette

Mana uses one **9×9 GUI-pixel open diamond rune with a light core**, rendered directly by native GUI rectangles. It is a resource mark, not an Amethyst item or an attunement signature. Standing Stone mana previews and the native HUD use the same drawing.

The retained palette is `#21172f` frame, `#453458` empty track, `#ac73e8` ink, `#dbc0fc` highlight and `#68419a` lower shade. Disabled menu marks dim the same colors. Minecraft's ordinary font supplies whole mana/XP numbers. No bitmap, external font or dependency is added.

## HUD

A **128-GUI-pixel horizontal fill**, five pixels high with a one-pixel frame, is followed by the rounded whole balance and the shared rune. The full meter occupies 167×9 GUI pixels, ending eight pixels from the right edge. Where it fits beside the hotbar, it ends eight pixels above the bottom edge and leaves the vanilla center HUD untouched. In compact windows, it stays aligned to the right and lifts clear of the occupied vanilla rows, reserving twelve pixels before the held-item name. It never shrinks the bar or covers the hotbar merely to force the corner placement.

This changes display placement and proportions, not the 100-mana capacity, precision, payments or recovery. The HUD stays hidden at full mana, visible at zero, and suppressed in spectator mode or Hide GUI. The owner approved the corner direction for shipping; later visual refinements remain possible.

The [current native inspection](../art/standing-stone-icons-corner/README.md) records partial, empty, armored, full-hidden, underwater and compact-window HUD states. The [preceding centered HUD](../art/standing-stone-resources/README.md) is historical evidence. Native source/health/food sprites remain the main product reference. Mobbin's [repeated hearts](https://mobbin.com/screens/94580d57-9d23-45b2-afec-a411f005d929), [peripheral progress](https://mobbin.com/screens/93b340a1-da34-49ad-83fa-0721f24a6700) and [uniform answer rows](https://mobbin.com/screens/d96b1a9f-2524-4181-93d6-0b482c186e4e) supplied visual precedents for count, edge placement and fixed row rhythm. Their web styling is not adopted in Minecraft.

## Resource costs and rows

XP/mana still show the amount followed by one resource mark. Health and hunger show **the actual full/half icon count**: two HP are one heart; five HP are two full hearts and a half. Food points follow the same two-per-icon convention. Half icons are drawn over vanilla empty containers so the remainder is readable. These costs have no numeric multiplier beside a single heart/food mark. Exact native points remain in tooltip/narration.

Destination buttons have one fixed **20-GUI-pixel height** and two pixels between rows, across resources, pages, short lists and GUI scales. Six destinations per server page fit the normal minimum 320×240 GUI without squeezing rows. Pagination controls appear only above six peers; shorter final pages shrink their panel, not their buttons. Payload registration version 4 requires matching client/server builds with that same capacity; its field format remains unchanged.

Only XP travel currently debits a live resource. Health/mana/hunger travel fixtures are illustrative appearance previews with no travel/payment requests. This display decision does not alter spell/device recipes, integer payment quantities, whole-heart spell-cost rounding, capacity, recovery or network keys.
