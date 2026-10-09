# Kairotic Hourglass

**Owner approved and implemented October 8, 2026.** The owner requested finishing the actual item with the settled imbuements. This decision supersedes the earlier pending-implementation/review labels. The item uses the common immutable item ability, trait resolver, typed resource payments and visual plan. No per-spell implementation or engine-semantic Time trait is introduced.

## Baseline and controls

Right-click the carried **Kairotic Hourglass** to return to the exact recorded position from **15 game seconds (300 ticks)** earlier. If its holder has carried an hourglass for less time, use the oldest available position. Recording works anywhere in the direct inventory, including offhand; holding it throughout is unnecessary. Multiple carried hourglasses share one player-owned trail, and transferring an item never transfers its previous holder's trail.

The neutral baseline is **60 mana, 10 maximum durability, one deterministic wear per successful return**. There is no extra cooldown or random break roll. A no-movement use, blocked destination, invalid source, failed payment or canceled/redirected teleport is silent and free. Successful returns preserve current inventory, health, effects, facing, velocity and fall accumulation, apart from their explicit resource payment. Only position returns; terrain and other actors remain current. Passengers, sleeping players, spectators and dead players cannot activate it. Creative mode retains the shared resource/wear bypass.

The initial implementation is **current dimension only**. Clear history on dimension change, death, logout, server stop or losing every valid carried hourglass. Reacquiring one starts fresh. Keep recording after use: the return is part of actual ongoing movement. A destination must have its entire current collision volume in loaded chunks, inside border/build bounds and open. Do not load remote chunks, search for a nearby alternative, force passengers off vehicles or silently change coordinates. Hazardous but collision-free locations remain possible; this is positional recall rather than safe-home travel.

## Ordered construction

The empty Spellstone's inner layer uses **Clock → Glass block → Echo Shard → Ender Pearl**, one each. Whole quarter-turn rotations are equivalent; reflection and arbitrary permutation are distinct. Four-slot recipes retain their ordinary inner-layer behavior on larger apparatuses. Outer offerings do not become extra ingredients. Sockets belong to their own offerings, remain installed and are revalidated alongside all active offerings and empty seats before commitment.

Clock supplies time, Glass the vessel, Echo Shard memory and Ender Pearl relocation. There is no generic Amethyst ingredient or Attunement Shard. Successful crafting consumes the four offerings once and drops an ordinary output exactly above the Spellstone with zero initial velocity. Saved trusted selections and physical geometry survive copies, saves and later station reconstruction.

## Approved imbuements

Choose at most one temporal contribution on Clock, one vessel contribution on Glass and one alternate-payment contribution on Echo Shard. Ender Pearl is neutral in this palette. Including neutral choices gives **3 × 3 × 4 = 36 valid sets**. These are initial playtest prices; approval of the package does not imply survival-economy measurements.

| Offering | Installed block | Adjective | Contribution | Neutral-layout result alone |
| --- | --- | --- | --- | --- |
| Clock | Copper Block | Fleeting | Time ×2/3; mana ×0.75 | 10 seconds, 45 mana, 10 uses |
| Clock | Amethyst Block | Enduring | Time ×1.20; mana ×1.12 | 18 seconds, 67 mana, 10 uses |
| Glass | Iron Block | Reinforced | Maximum durability ×1.5; mana ×1.25 | 15 seconds, 75 mana, 15 uses |
| Glass | Quartz Block | Frugal | Maximum durability ×0.6; mana ×0.75 | 15 seconds, 45 mana, 6 uses |
| Echo Shard | Soul Sand | Bloodbound | Capped mana exchange for health | 30 mana + 1 heart, 10 uses |
| Echo Shard | Moss Block | Fasting | Capped mana exchange for hunger | 30 mana + 2 full hunger icons, 10 uses |
| Echo Shard | Lapis Block | Erudite | Capped mana exchange for XP points | 30 mana + 30 XP points, 10 uses |

Use the [shared valuation](resource-payments.md): **1 full heart (2 HP) = 2 full hunger icons (4 food points) = 30 XP points = 30 mana**. Let `M = 60 × temporal mana factor × vessel mana factor × stored geometry cost`. For an alternate payment, exchange `E = min(30, 0.75 × M)`. Final mana is `floor(M − E + 0.5)`; health is `2 × ceil(E / 30)` HP, hunger `ceil(E / 7.5)` food points, and XP `ceil(E)` points through `ResourceValuation`. Geometry applies **before** the exchange and its cap; do not reuse the older spell shaping exchange-before-layout order for this device. All required resources are mandatory together; health must leave the user alive. A short or partially recorded window pays its selected variant's price.

Examples at neutral geometry:

- Enduring + Reinforced: 18 seconds, 84 mana, 15 uses. Adding Erudite gives 54 mana + 30 XP.
- Reinforced + Bloodbound: 15 seconds, 45 mana + 1 heart, 15 uses.
- Fleeting + Frugal: 10 seconds, 34 mana, 6 uses. Adding Erudite gives 8 mana + 26 XP.

Each full combination uses its one italic display adjective from the shared [catalog](../magic-adjectives.md), while retaining every contribution separately. Ephemeral is Fleeting + Frugal; Stalwart is Enduring + Reinforced; Relentless is Enduring + Reinforced + Bloodbound. Aliases do not replace effects.

Preserve established exact pair meanings. Glass / Diamond Block is Reflecting, Clock / Stone is Patient, Clock / Redstone Block is Delayed and Ender Pearl / Lodestone is Anchored. These recognized but incompatible paid effects reject hourglass construction. Unknown neutral pairs remain neutral. The seven device choices are explicit trusted routes, not universal meanings for every offered Echo Shard or Glass. All selector blocks already belong to the accepted socket set.

## Traits, history and commitment

`data/vestige/item_abilities/kairotic_hourglass.json` authors Time, Memory, Space, Teleportation, Transmutation and Amplify at 1. The explicit interval variable is `300 × resolved Time × resolved Amplify` ticks. Stored v3 geometry contributes ordinary Amplify/Range/Area and casting-cost multipliers; only the explicitly consumed values affect this ability. Temporary actor boosts enter the common resolver once for each future activation. Durability changes only through the vessel choices. There is no arbitrary 5–30-second power clamp.

Record once each server tick, including an acquisition/use seed between ticks. Retain the longest currently resolved carried interval plus its boundary sample. A newly available longer interval uses the oldest actually retained sample while its window fills. Each player retains at most **4096 samples**: very long windows compact the older half, preserving the oldest boundary and the newest 2048 samples. Recent windows stay tick-exact; long boosted windows can return an older coarse sample at or before the requested tick. This bounds memory without pretending a clamped interval or fabricated history is the requested time.

Shared `SpellCost.Experience` is measured in current points, never experience levels or vanilla's historical total counter. `CastReservation.Atomic` permits a fallible physical commitment inside native resource payment. A canceled/redirected standard NeoForge teleport event restores health, food, current XP and mana/recovery delay and spends no source wear. Existing infallible reservations retain their ordinary commitment path; unsupported nonnative worlds reject atomic sources before payment. Hourglass callbacks contain destination/history validation and physical movement, while shared definitions own traits, variable resolution, payment and the return visual.

## Time materials: upstream facts and native conventions

The complete [pinned Iron material reference](../research/iron-material-uses.md) establishes no dedicated time mineral or general rewind reagent. Keep the following narrower facts separate:

- Iron's Pyrium jewelry material supplies **5% casting-time reduction**, not historical-position recovery. [Pinned material definition](https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/main/resources/data/irons_spellbooks/irons_jewelry/material/pyrium.json).
- Iron brews **Timeless Slurry from an Echo Shard and a Mundane Potion**. That is an actual temporal-named recipe connection, but does not isolate a universal time property in either ingredient. Echo Shard also serves as Iron's Eldritch focus. [Pinned brewing recipe](https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/src/generated/resources/data/irons_spellbooks/recipe/alchemist_cauldron/brew_timeless_slurry.json), [school focuses](https://iron.wiki/schools/).
- Vanilla uses Echo Shards to craft a Recovery Compass that points to the player's last death location. This supports a past-location/memory motif; it is not literal time travel. [Minecraft's Echo Shard article](https://www.minecraft.net/en-us/article/echo-shard).
- Vestige already uses **Clocks** in Haste and Time Jump recipes. Its shipped Enduring augment pairs Clock or Phantom Membrane with Amethyst Block to raise Time by 20% for compatible finite effects. Patient uses Clock/Stone; Delayed uses Clock/Redstone. These specific offering/socket rules are the current conventions, not a universal raw mineral-to-Time table.

Amethyst is therefore not justified as a mandatory generic magical focus in this recipe. The earlier proposed Amethyst Shard ingredient and arbitrary five-/thirty-second Copper/Amethyst selectors are withdrawn pending a justified material design. No new ore or universal temporal material is selected by this document.

## Verification and artwork

`HourglassTest` covers all 36 recipes and quarter-turns, reflected/invalid layouts, saved trusted state, geometry-before-exchange prices, actor Time scaling, malformed inputs, XP grammar, full/partial history, dimension reset and bounded long-window retention. `HourglassWorldTest` covers native crafting/cancellation, all variant payments, recorded returns, failure rollback, final-use breakage and offhand/removal behavior. Native framebuffer captures and loaded resource hashes accompany the independent generated concept and actual 16×16 export in [the initial art folder](../art/kairotic-hourglass-v1/). The owner requested a simpler sprite and then an unsupported vessel; [the accepted v3 artwork](../art/kairotic-hourglass-v3/README.md) removes side rails, is installed for testing and has exact owner-approved pixels pinned in its approval record. Check [development status](../development-status.md) for executed results and shipment status; source tests and an exported PNG alone do not establish client verification or owner artwork acceptance.
