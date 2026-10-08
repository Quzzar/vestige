# Wayfarer and magical adjective shipment

October 8, 2026. The owner approved the full Wayfarer package, combined adjective naming across implemented spells/items, publication and installation in Kithkyn Testing. The isolated publication checkout carries the approved native equipment, device, ordered-ritual and shared-runtime foundations required by this package. The shared development checkout and other agents' index were preserved.

## Shipped behavior

Wayfarer Boots use the shared immutable trait/effect/payment runtime: grounded sprint-jump, five mana, three seconds at +20% movement, one protected landing up to four HP, fifteen-second wearer-owned recovery and 65 durability. Swift, Enduring, Reinforced and Quickened compose into sixteen named, craftable variants. Same-tick removal revokes active benefits; changing copies or variants retains recovery. Ordered eight-seat rituals, retained imbuements, matching-thread repair, ordinary wear and supported enchantments are covered.

The [shared naming catalog](../../magic-adjectives.md) has 79 source entries and 145 exact combined names. Scrolls, wands, stored staff scroll labels, boots and existing Homebound Eye routes use the same italic-prefix convention. Authored mixtures use one exact adjective; other mixtures use the reserved *Confluent*. Effects, complete contribution IDs/degrees, costs, identification, stack equality and exact scroll binding remain independent of the shorter display name. All 36 hourglass design sets have reserved names; this does not implement the hourglass.

## Final verification

- [Combined build log](combined-build.log.gz): **202 unit tests / 45 suites**, zero failures/errors/skips; **354 required Minecraft behavior tests passed**, with Kithkyn loaded; packaging and version compatibility passed. This final run includes the boots' two armor-slot hooks, same-tick remove/reinsert assertion and mounted/flying/native-water eligibility assertions, plus all ten Wayfarer and twelve robe tests.
- [Cast gallery log](cast-gallery.log.gz): six tests passed and production build completed. [Recording verification](recordings.log.gz) passed all 214 native clips. Their original media/definition hashes are preserved; frozen original definitions permit only the documented removal of cooldown costs bypassed by operator capture.
- All 21 recorded authoring/test commands passed: [results](evidence.json), [catalog checks](authoring-0.log.gz), [component/recipe exporters](authoring-1.log.gz), [Python behavior checks](authoring-2.log.gz). Both conversion tools regenerated identical definitions/ledgers; all 490 leyline parity fixtures passed.
- [Actual native boots presentation](../../art/wayfarer-native/README.md) was inspected: registered inventory/worn art, literal plain/Nimble/Unfaltering tooltips, adjective-only italics and independent mana/recovery overlays. The capture's loaded sprite/atlas/model hashes match production. Its client ran without Kithkyn; the combined world suite above loaded it. Optional JEI/EMI screens were not opened.

The first combined run found two stale assertions: Homebound Eye's previous forty-mana price and the former staff BLOCK animation. The final tests expect the already-approved thirty-mana price and current SPEAR brace while retaining rejection of full shield behavior for both mundane and empty magical staffs. All 354 pass in the final run.

## Package and installation

[Evidence](evidence.json) pins the exact jar and [tested source hashes](tested-source.json). The installed jar SHA-256 is `3ba781c1883c9eaf473cb22b29507090d2cd7dc2a64957a1b8e116105fd4e353`. [Installation receipt](installation.json) records the old jar hash, timestamped backup and verified atomic replacement in **Kithkyn Testing**. Minecraft must restart to load it; an already-running client was not restarted.

Publication uses the `codex/wayfarer-adjectives` pull request and the repository's normal checks; its Git history records the final merge. This receipt pins local verification and installation. No release tag is created. Survival tuning, the hourglass implementation and other clothing packages remain separate work; completing names does not complete their gameplay.
