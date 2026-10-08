# Mana affordability through item overlays

**Owner-approved replacement, October 7, 2026.** The pooled mana balance and regeneration remain, but no standalone mana meter, number or rune is drawn on the HUD. Each mana-using item instead carries Minecraft’s translucent cooldown-style shade while its next mana payment is unaffordable. This supersedes the October 6 corner meter and retains its mana symbol for resource-cost artwork.

## Item-relative feedback

The item’s readiness is `min(current mana / final mana cost, 1)`. The shade covers the missing fraction, `1 - readiness`, using the same sixteen-pixel geometry, direction and translucent white ink as vanilla item cooldowns. Zero mana gives a fully shaded icon; half the item’s required mana gives half shading; enough mana removes the shade immediately, even if the player is below their full capacity. Zero-mana items remain clear. Stack count does not multiply the price of one cast.

For example, with 15 mana a ten-mana item is clear, a thirty-mana item is half shaded, and a sixty-mana item is three-quarters shaded. A player can cast a cheap spell repeatedly while retaining enough for another cast and never see its overlay. The pool still starts at 100 and regenerates two per second after the existing five-second expenditure delay. There is no numerical pool display.

Scrolls use their exact stored source shaping. Wands use the same compiler as casting, including cores and tips. A staff follows its selected spell; unused bindings do not affect its displayed price. Mana-paid Homebound Eyes use their native thirty-mana payment; their other payment routes have no mana shading. Count, cosmetic names, wear and unrelated staff slots do not alter the price identity. Creative casts bypass resource payment and remain unshaded.

The server compiles and privately synchronizes costs for the player’s inventory, current container and carried stack. The client derives bounded source fingerprints without loading or trusting a local spell catalog. Wear/count changes preserve the fingerprint; changing shaping, equipment or staff selection selects another price. Catalog replacement invalidates cached definitions, missing/incompatible definitions have no price, and removed items clear their metadata. Each snapshot is bounded to 256 distinct sources and contains only opaque source fingerprints and finite mana prices. Regeneration snapshots retain the server’s double precision so a rounded float cannot advertise affordability too early. Disconnect, clone and server shutdown clear transient state.

NeoForge item decorators apply the shade in ordinary hotbar, inventory and container rendering. They do not install timed cooldowns or block vanilla item input. If a real vanilla cooldown already shades an item, the mana decoration fills only the additional missing area, avoiding double opacity. This cue communicates mana affordability; other conditions, resource channels and explicitly authored timer costs retain their own gameplay checks. Initial spell preparation uses a subtle camera zoom and straight held-item draw-back; the crosshair remains unchanged.

## Shared symbol asset

The retained open violet diamond with a light core is now an actual transparent **9×9 PNG**, `assets/vestige/textures/gui/mana.png`. Standing Stone mana-cost previews render that same asset at native GUI size; disabled previews dim its colors. It is a resource mark, not an Amethyst item or an attunement signature.

The palette remains `#21172f` frame, `#453458` track, `#ac73e8` ink, `#dbc0fc` highlight and `#68419a` shade. The superseded preparation bar used the track and frame. The preparation bar and expanding crosshair edge are superseded by a 3% camera zoom. The crosshair retains Minecraft's native rendering, independently of the retained violet mana symbol. No font or dependency is added. The old corner/centered meter captures remain historical evidence.

The primary product reference is the [actual vanilla cooldown and attack HUD captures](../art/vanilla-hud-indicators/README.md). The Mobbin pass inspected [Duolingo ABC](https://mobbin.com/screens/bc13be11-f17e-4318-ba22-bbbcd34f33ba), [Life Reset](https://mobbin.com/screens/28db14a2-e6a1-4f49-a304-a048fa1880b4) and [Apple Games](https://mobbin.com/screens/c2089331-e1c4-4387-85aa-3a44db91c4ea). They support keeping progress adjacent to its subject and using stable icon positions. Separate profile meters, large cards and explicit percentages do not fit this owner-selected native item treatment.

## Resource costs and rows

XP/mana still show the amount followed by one resource mark. Health and hunger show **the actual full/half icon count**: two HP are one heart; five HP are two full hearts and a half. Food points follow the same two-per-icon convention. Half icons are drawn over vanilla empty containers so the remainder is readable. These costs have no numeric multiplier beside a single heart/food mark. Exact native points remain in tooltip/narration.

Destination buttons have one fixed **20-GUI-pixel height** and two pixels between rows, across resources, pages, short lists and GUI scales. Six destinations per server page fit the normal minimum 320×240 GUI without squeezing rows. Pagination controls appear only above six peers; shorter final pages shrink their panel, not their buttons. Payload registration version 4 requires matching client/server builds with that same capacity; its field format remains unchanged.

Only XP travel currently debits a live resource. Health/mana/hunger travel fixtures are illustrative appearance previews with no travel/payment requests. This display decision does not alter spell/device recipes, integer payment quantities, whole-heart spell-cost rounding, capacity, recovery or network keys.
