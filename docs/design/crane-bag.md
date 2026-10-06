# Crane Bag

**Accepted item behavior and implementation authorization, October 6, 2026.** The owner describes Crane Bag as a vanilla-style Bundle whose contents are shared by every bag with the same inherited full Attunement Shard signature. It combines portable, mixed-item bundle storage with shared access. Unlike a vanilla Ender Chest, the shared inventory belongs to an attunement rather than an individual player. The owner authorized gameplay implementation first, explicitly deferring the survival recipe and final art until afterward.

## Shared contents and capacity

Every matching bag is an access point to one item pool. Inserting through one bag makes the items available through another; removing an item through either removes it for both. Additional matching bags provide more access points, not more storage capacity or copies of the contents. Distinct full signatures have distinct pools.

The item should behave like a Bundle rather than opening a chest-sized inventory. The owner's vanilla-capacity direction corresponds to Minecraft 1.21.1's one-stack weight limit: 64 ordinary stack-to-64 items, 16 stack-to-16 items or one unstackable item, including equivalent mixtures. It is not 64 separate item slots. A bundle's inventory-click insertion/extraction and contents preview are the interaction reference.

This baseline was checked against the local pinned Minecraft 1.21.1 `BundleItem` and `BundleContents` sources. Native `BundleContents.Mutable` supplies fractional capacity and component-preserving merging. The implemented starting policy rejects Bundle/Crane Bag nesting in both directions, item types that cannot fit inside container items and items carrying container components. This is a deliberate restriction of the vanilla nesting behavior. Ordinary chests can store physical bag access points.

## Signature presentation

The item name is followed by the same four colored rune marks as its source shard. This applies to every attuned item that inherits that key: all Standing Stone finishes, Homebound Eye, Crane Bag and Whispering Shell. Different item types, finishes, payment choices or crafting sites must not change the displayed mark for a copied key. The complete verified key establishes shared membership; the abbreviated rune marks remain recognition aids.

Attunement Shard, Standing Stone, Homebound Eye and now Crane Bag append `AttunementMark.fromKey(key).component()` beneath the name. Whispering Shell must reuse that formatter when implemented. Crane Bag also uses vanilla fullness text, the contents image tooltip and fullness bar. Its client gather event moves the vanilla image below the rune line, preserving name → runes → contents order in normal and advanced tooltips. Existing other devices' extra details are separate from this requested common signature line.

## Implemented server storage and controls

`CraneBagStorage` owns one persistent shared inventory per full key in the Overworld's `vestige_crane_bags` SavedData, accessible across dimensions of the same server save. Version-1 bag binding data copies the full key unchanged. Each bag's vanilla contents component is display data; neither a client packet nor a stale bag snapshot supplies authoritative transferable items. Physical bags do not own the stored items. Malformed, duplicate or unsupported saved pools fail loading explicitly instead of silently discarding items.

Secondary-click a bag with items on the cursor to insert them; secondary-click with an empty cursor to withdraw the most recent stack. A bag on the cursor can insert from or withdraw into a normal menu slot. Client hooks handle the click without predicting ownership changes. Server hooks validate the actual bound bag, active menu, slot/cursor identity and permissions. Read-only/output-only/fake slots are excluded. Vanilla slot limits and component equality apply, and partial transfers retain their exact remainder. A failed cursor write or rejected insertion leaves contents unchanged. Per-key locks reject reentrant operations from drop/slot callbacks, and all transfers execute on the server thread.

If two players request the last diamond, the server processes one withdrawal first. The next request sees no diamond and cannot produce another copy. No custom client transfer packet accepts a key or replacement contents: Minecraft's normal container-click handler executes against the current server menu and treats client item claims as synchronization expectations. Repeated clicks act on the current real cursor and bag, preserving item counts; changing the bag binding changes which pool the actual menu can access.

Each server player tick checks bag bindings/revisions in the inventory, offhand, armor, open menu and cursor. Only changed revisions update contents components and send ordinary vanilla slot synchronization; there is no constant contents broadcast or separate network channel. A server-session token plus key/revision refreshes offline/stored/newly acquired bags when exposed to a player. Transfers refresh the acting player's previews immediately; peers refresh on their next tick. Capacity is bounded to 64 distinct stacks and 32 KiB of encoded pool data, with oversized insertions rejected before consuming items. Only changed, previously used pools are saved; reading an empty new key creates no pool.

Right-click in hand drops the shared contents once through validated server entity spawns. A canceled stack spawn leaves that stack in the shared pool; accepted stacks leave the pool. Destroying a physical bag never spills its cached preview or removes its shared pool. A later matching bag can recover those contents. No automated item-handler capability is exposed.

## Recipe and image deferred

The registered `vestige:crane_bag` currently references vanilla Bundle art and has no survival recipe. The owner requested removal of its temporary test command; no Crane Bag command is registered. An unbound creative-tab bag has no storage access. The item factory already validates/copies shard identity for the eventual recipe and gameplay tests. Recipe materials and the final image are the next owner decisions; no tentative recipe is shipped.

## Required verification

Native behavior tests cover competing player withdrawals, mixed weighted capacity, partial slot remainders, refused cursor writes/slots, distinct full keys, exact components, forged previews, actual click packets/replays, changed bindings, cross-dimension/offline-preview refresh, nesting and payload limits, SavedData reload, destroyed bags and canceled/reentrant world drops. The opt-in client capture uses the actual item/bar/BundleTooltip renderer with fixture snapshots to inspect normal/advanced rune placement; it does not claim a live multiplayer UI session. Actual run results are recorded in [development status](../development-status.md).

Normal save/reload is supported. Hard-crash atomicity across Minecraft's separate player, chunk and SavedData files is not established by server-thread ordering or these tests. No crash journal is implemented, and there is no absolute duplication guarantee for process interruption or third-party mods bypassing the item controls.
