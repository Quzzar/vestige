# Vestige scroll knowledge · native verification

October 6, 2026 · Minecraft 1.21.1 / NeoForge 21.1.72 / Java 21.

The primary mod name is **Vestige**, with *Traditions of Lost Magic* as its subtitle. Scroll names and recipe ingredients follow separate persistent per-player records:

| Player record | Scroll name | Spell recipe offerings |
| --- | --- | --- |
| Neither | Unknown Scroll | Question marks |
| Identified only | Scroll of the named spell | Question marks |
| Crafted only | Unknown Scroll | Actual ingredients and empty seats |
| Both | Scroll of the named spell | Actual ingredients and empty seats |

Successful native scroll casts identify their base spell. Successfully committed ingredient rituals teach their recipe to the crafter. Receiving a scroll, fragment discovery, hints, canceled crafts and failures teach neither recipe memory nor identification. Saves and player clones preserve both records. Previous crafts were not recorded and are not backfilled from owned scrolls.

## Results and actual appearance

`test`, `build`, `verifyKithkynCompatibility` and `runGameTestServer` pass: **104 unit tests and all 187 required Minecraft tests**, zero failures. Coverage includes all 214 authored spell recipe layouts, bounded payload roundtrips, independent identification/crafting flags, native successful casts/crafts, rejected output spawns, cancellation before commitment and persistent player clones. The final jar has the correct mod display name and Unknown Scroll translation, passes ZIP integrity and agrees with all 320 compiled classes and 2,888 source resources.

All nine unchanged Minecraft framebuffer PNGs were visually inspected. JEI `19.22.1.316`, EMI `1.1.24+1.21.1` and both together each retain **216** native rituals, one base/shaped Fireball lookup, one actual-key shard lookup and the public Lapis usage. Paper has zero spell usages initially and one after the server's Fireball craft record updates. The unidentified crafted Fireball still says Unknown Scroll. Identification changes Magnetic Attraction's name while its uncrafted recipe stays concealed. JEI name search moves from zero to one matching item without duplicate rituals. Eight real normal/advanced item-tooltip cases check base/shaped unknown/identified names.

| Runtime | Crafted Fireball | Identified but uncrafted Magnetic Attraction | Scroll name test screen |
| --- | --- | --- | --- |
| JEI | [Ingredients](jei/6-crafted_fireball.png) | [Question marks](jei/7-identified_magnetic_attraction.png) | [Names](jei/8-scroll_names.png) |
| EMI | [Ingredients](emi/6-crafted_fireball.png) | [Question marks](emi/7-identified_magnetic_attraction.png) | [Names](emi/8-scroll_names.png) |
| Both; EMI owns rituals | [Ingredients](both/emi/6-crafted_fireball.png) | [Question marks](both/emi/7-identified_magnetic_attraction.png) | [Names](both/emi/8-scroll_names.png) |

Recipe screenshots use the real viewer screens. The name screen is a development-only Minecraft screen using actual ItemStack names and tooltip rendering. The client harness updates the actual integrated-server player's knowledge records to test synchronization and live indexing; world tests separately verify the real craft/cast transitions. No simulated browser UI is used.

## Evidence and limits

[Verification](verification.json) records test totals, captures, lookups, logs and hashes. [Package verification](package-verification.json) pins the staged `build/scroll-knowledge/vestige-0.1.0.jar`. The captured scroll, diagram, payload and viewer classes agree with the jar. The general effects-capture driver changed concurrently; it is excluded from this capture mode and its differing hash is retained in the record. Isolated build outputs, project caches, capture worlds and server worlds avoid other active tasks' build files. Initial cancellation and stale Standing Stone fixture failures are recorded as preceding runs; the final complete suite passes.

EMI's `ui.append-item-mod-id: false` setting applies only to the isolated EMI/both test profiles. Its default mixin adds a mod-name footer after NeoForge's tooltip event; Vestige's own tooltip still contains only the name. Both viewers together report pinned JEMI runtime/startup and duplicate tag-display recipe warnings, visible in the development overlay. The native Vestige category, uniqueness and refresh assertions pass, but this is not a clean general bridge-compatibility claim. Use the logs to distinguish those warnings from the passing native feature checks.

No Prism installation, remote multiplayer visual verification or ordinary survival playtest occurred. The item and recipe data formats retain their previous identities; matching client/server builds are required for the version-3 knowledge payload.

To reproduce the focused client checks, run `runEffectsCapture` with `-Pcapture_kind=scroll_knowledge`, `-Precipe_viewer=jei`, `emi` or `both`, a fresh `-Pcapture_directory=/absolute/path`, and a separate `-Pcapture_output=docs/art/new-output`. The default `recipes` capture still includes GUI-size/reload checks before the three knowledge stages. Run ordinary `test build verifyKithkynCompatibility runGameTestServer` for model/package/world verification.
