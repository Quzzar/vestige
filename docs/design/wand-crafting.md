# Wand crafting and magical string cores

**Native wand implementation, October 7, 2026.** Sixteen-scroll stacking, five magical thread components, all seven bases and eight additional tips are implemented. Five offerings bind an untipped wand; six bind a tipped wand on the eight-node ritual. Trusted components compile with one exact source scroll through the shared immutable runtime. Base and tip art uses independent actual 16×16 sprites; JEI/EMI displays derive dynamically from the same component compiler. Numerical profiles remain initial playtest tuning. [Verification](../verification/native-wand-tips-2026-10-07/README.md) records the tested source, world checks and client evidence.

## Accepted wand construction

A wand consumes three copies of the same scroll, one base and one magical string core, with an optional tip. Each scroll occupies its own Plinth offering surface; a stack of three on one Plinth is not a replacement for the three recipe inputs. Five filled surfaces craft a wand without a tip; six craft a tipped wand. Both use the eight-slot apparatus. The optional tip position and the other unused active positions remain empty. Relative relationships and whole quarter-turn equivalence follow the existing ritual model; the implemented inner/outer arrangement is recorded below.

Every base can hold every native spell. The accepted bases are Stick, Bamboo, Bone, Blaze Rod, Breeze Rod, End Rod and Lightning Rod. A finished wand is a native casting item; it does not inherit its ingredient's vanilla block-placement behavior.

The three scrolls supply one spell, including its existing Spellshaping. They are three matching copies, not three contributions that multiply the spell's shaping degree. Their base spell, augment IDs/degrees, stored leyline trait modifiers and Casting Cost adjustment must agree. The implementation must validate actual item data, not rendered names: unrelated unknown scrolls can share the same visible name. Wand binding preserves the selected scroll's stored magic once. Any later geometry contribution from the binding ritual needs an explicit rule; it must not silently apply the scroll's original layout bonuses twice.

The three components have separate roles:

- **Base:** appearance and an authored affinity modifier for selected traits. Bone increasing Death by 50% is the owner's example, not accepted final tuning. A trait change affects only effects that actually read that trait; a Death label alone does not increase damage.
- **Core:** a magical string made through Plinth imbuement and reusable in other future equipment recipes. Its own authored modification composes with the scroll and other components.
- **Tip:** an optional additional equipment contribution beyond the source scroll's existing Spellshaping. The owner clarified that the tip is not simply selecting or automatically upgrading an augment already carried by the scroll. It may add its own trait changes, compatible secondary effects or wand properties, with an explicit compatibility and interaction rule. Material associations remain independently authored; an installed block's existing offering-dependent rules do not automatically define a wand tip.

All modifications use the native trait, composed-effect and typed-cost machinery. Base definitions stay immutable. No inherited Wizardry wand runtime or separate spell definition per component combination is introduced. Wands pay the bound spell's ordinary resource channels with explicit component adjustments and never identify spells. Rebinding and repair remain separate decisions.

**Accepted breadth requirement, October 6:** tips must be universal enough to serve casting itself or broad spell capabilities rather than a few specific spells. Derive compatibility from executable effects, including callbacks and bindings, rather than named-spell exceptions. Initial tips must include several meaningful utility choices as well as damage, healing and protection choices. The [tip catalogue](wand-tip-catalog.md) revises the proposed palette accordingly; the eight material/effect directions are selected, and actual tip coverage must be audited before shipping.

**Accepted tip direction and tradeoff requirement, October 6:** the owner supports proceeding with the existing broad effect direction provided each tip has a meaningful cost in mana, wear, preparation or another explicitly authored payment. The [tradeoff table](wand-tip-catalog.md#accepted-tradeoff-requirement) records provisional costs for the preferred eight; exact coefficients and implementation specifications still require tuning. Tip-specific prices compose with the bound scroll and core once, and cannot silently disappear when a percentage is applied to a zero-cost resource channel. Both binding forms now execute these starting profiles.

## Accepted casting limits

**October 7, 2026:** the owner removed the separate **60-second wand recovery** after removing ordinary spell cooldowns. Wands are limited by the bound spell's composed resources, preparation/channel time and deterministic durability. The initial twenty-durability foundation remains subject to component adjustments and later playtest tuning; it replaced the briefly considered five-percent random break chance.

Component combinations adjust durability alongside trait changes, compatible effects and costs. The initial profiles use eighteen through twenty-four base durability, with Callous adding eight. These are implemented starting values, not final encounter balance. Smoldering spends two durability per committed cast; other current cores spend one.

Wear commits with successful initial payment, before effects or a volatile forfeit. Canceled preparation and failed payment spend neither resources nor durability. A final use with less remaining durability than its wear price still casts and breaks the held wand; no random break roll is used. Recasts retain the paid cast and spend nothing again.

Wands, scrolls and staffs have no added equipment recovery. Once a cast is complete, the same wand or another copy can prepare again immediately if its resources and durability allow. Each new cast pays and wears its own reserved source; an already-paid recast remains part of its original session. Explicit data-pack spell cooldown costs remain supported, but no shipped spell has one.

## Native wand implementation

`vestige:wand` is a native nonplacing, nonstacking casting item. Version-1 custom data stores a base ID, thread ID and the validated scroll's spell/shaping fields. Maximum durability must agree with the trusted component profile; malformed bindings reject quietly. Only the wand in the reserved hand pays wear, including offhand casts. Changing held components during preparation cancels safely.

The eight-node relative pattern is:

| Layer | Seats in relative clockwise order |
| --- | --- |
| Inner 0/2/4/6 | Base, Scroll, Scroll, Scroll |
| Outer 1/3/5/7 | Thread, optional Tip, empty, empty |

Whole quarter-turns match; reflections reverse the thread/base relationship and reject. The optional Tip position must be empty for untipped binding or hold one supported tip. Three separate, completely component-equal scroll offerings are required. Binding consumes five or six offerings through the atomic ritual, retains sockets, keeps the center empty and produces an ordinary centered drop. It copies source magic once and adds no binding-layout/socket shaping. All eight active nodes, including empty seats, participate in cancellation checks.

`WandComponents` compiles source shaping once, checks affinity before equipment seeds, then composes the core through native trait/effect/cost definitions. Ensorcelled and Callous add literal preparation after source timing/layout changes. Laced must produce an actual shorter preparation after final rounding. Mana premiums retain a minimum one-mana payment; explicit health/hunger/material costs remain separate. Overlapping ignition/cleanse augments reject the corresponding core. Consecrated handles actual healing and protection with one shared recipient budget.

The unshaped 214-spell catalog audit finds Callous **214**, Ensorcelled **214**, Laced **126**, Smoldering **86** and Consecrated **25** compatible spells. Every spell supports every base through Callous. These are executable-plan counts, not encounter balance or all possible shaped variants. Source IDs, augments, geometry modifiers and used durability survive saving. Wand casting never identifies spells; a scroll cannot take over a pending wand continuation to identify it.

The seven body sprites and eight tip sprites compose through ordinary handheld models. All packaged component/thread PNGs are actual 16×16. The [earlier foundation verification](../verification/native-wands-2026-10-06/verification.json) remains historical; [current evidence](../verification/native-wand-tips-2026-10-07/README.md) records tips, artwork, viewers and installation separately.

## Scroll stacking

Native scrolls have a maximum stack of **16**. Minecraft's item-component equality preserves the complete stored scroll variant during merging, splitting and saving. Different spells, augment degrees, augment choices, leyline multipliers or Casting Cost values stay separate. Per-player identification is external to the item and does not alter its stack identity. Arbitrary additional item components retain vanilla separation semantics.

Casting consumes one scroll on committed payment, and failed payment or cooldown rejection preserves the stack. Dismantling takes one scroll and produces three fragments. Plinth surfaces continue holding one offering each; changing the inventory stack limit does not change ritual quantity or slot rules.

## Magical thread direction

**Presentation approved October 8:** all five magical threads use vanilla enchantment shimmer through their default Glint Override component. The approved 16×16 v6 artwork stays unchanged. This is cosmetic: it adds no actual enchantments, tooltip instructions, crafting input or gameplay bonus, and threads still stack to 64.

The owner accepts a common string-crafting recipe whose String offering's installed imbuement determines which magical thread is produced. Magical threads are obtained through this imbuement step; dyes, loot or a plain crafting-table recipe must not bypass it. In the coordinated chat **Define magical string types**, the owner confirmed the complete five-type set below and accepted the common String/Amethyst Shard/Honeycomb recipe. This selection supersedes the earlier Amethyst/Copper/Stone/Soul Sand/Moss shortlist. Component effects remain proposals.

| Accepted selector in the String offering's Plinth | Current thread name | Existing native flavor that can inform its design |
| --- | --- | --- |
| **Diamond Block** | **Ensorcelled Thread**, accepted | Offering-dependent Focused, Piercing, Restoring, Absorbing, Conjuring and Reflecting pairings. The thread still needs one explicitly authored contribution. |
| **Iron Block** | **Callous Thread**, accepted | Warded offers a protective precedent; other ingredients produce Repelling, Unbalancing, Gathering or Exhausting. Protection is a candidate thread identity, not an inherent Iron effect. |
| **Gold Block** | **Smoldering Thread**, accepted | Iron's source meanings include mining speed and ignition. Native Gold pairings include Kindled ignition, Excavating and Paper/Gold's Votive potency with a Coal payment. The name does not finalize the thread's contribution. |
| **Emerald Block** | **Laced Thread**, accepted | Iron's source meanings include movement speed and Emerald generation; Spellbooks uses Emerald as its Evocation focus. Native Paper or Golden Carrot/Emerald produces Greedy potency with an Emerald payment. The precise thread effect remains undecided. |
| **Glowstone** | **Consecrated Thread**, accepted | Amethyst Shard or optional Arcane Essence/Glowstone produces Purifying; Glowstone Dust or Compass/Glowstone produces Revealing. The precise thread effect remains undecided. |

Each core should give a distinct reason to choose its material. The table records existing offering/block precedents, not new String pairings or accepted numerical bonuses. A name does not automatically add its namesake Spellshaping rule. Traits, effect eligibility and typed payments still need explicit definitions. Cores remain reusable crafting components; a different future item can interpret them through an explicitly authored recipe. A core whose wand contribution is incompatible or saturated must communicate rejection visually and avoid consuming inputs.

### Accepted shared recipe

**Accepted October 6:** consume **1 String + 1 Amethyst Shard + 1 Honeycomb** to produce **1 magical thread**. String supplies the fibres, Amethyst is the magical focusing ingredient and Honeycomb binds/waxes the fibres. All five outputs use these same offerings and quantities; the installed material in the String offering's own Plinth selects the thread type. This supersedes the earlier String/Honeycomb/Glow Ink Sac proposal. These are Vestige recipe roles, not universal upstream Iron material properties.

Use the ordered inner-layer pattern **String → Amethyst Shard → Honeycomb → empty** around an empty Spellstone. Whole quarter-turns match; arbitrary permutations and distinct reflections reject. The displayed pattern records relative positions without an absolute compass direction. Outer nodes on a larger table are inactive. The String keeps its installed selector when the pattern rotates. No reference scroll, Paper or outer layer is required. Four Plinths remain required even though only three hold offerings. Consume the three offerings once, retain all socket materials, and spawn the ordinary dropped output above the Spellstone through the existing atomic ritual lifecycle.

Only the socket paired with the String offering selects its type. Other sockets must not select another core or add unspecified traits. A missing or unsupported String selector produces no magical-thread output. Craft ordinary thread items without geometry-derived shaping; exact core effects belong to the later equipment recipe.

Core selection belongs to this component recipe. It must not reassign ordinary spell recipe pairings: String/Copper currently selects Forked while shaping a spell, and that route remains intact. The selected five blocks require new component-recipe mappings, with separately authored wand contributions that can reuse compatible existing effect/cost behavior.

During core crafting, consume the prescribed offerings and retain the installed selector block. Produce an ordinary stackable component whose identity is the selected core type, without carrying incidental structure geometry, apparatus finish, coordinates or cosmetic socket colors. The initial implementation uses vanilla's default maximum stack of 64; final stack tuning is undecided. This keeps cores useful as interchangeable crafting components. If geometry should affect them later, that needs a separate owner decision before changing identity.

### Recipe-viewer requirement

**Accepted owner requirement, October 6:** recipe viewers must show both the common consumed ingredients and the required imbuement installed in the String's specific Plinth. Looking up a thread must reveal how to obtain that output, including its selector block; the block must not appear as another consumed top offering.

Recommended presentation uses the existing **Spellstone** category in JEI and EMI, with five output-specific public entries backed by the one shared ingredient recipe. Each entry shows three native ingredient icons, an empty fourth offering seat and its exact thread output. A smaller native selector-block icon is attached to the String's drawn Plinth/socket. A short hover role can identify it as an installed imbuement that is retained. The illustration must make the local String/socket relationship visible without relying on hover text. Exact socket art and placement remain a visual proposal requiring native inspection.

Represent installed-material requirements separately from consumed offerings in the common display model and bounded synchronization. Associate each requirement with its offering seat; JEI and EMI should index the retained block as a catalyst and show its normal item hover/lookup behavior. Both adapters must derive the five entries from the same authoritative thread recipe definitions used by the server. Do not maintain a second viewer-only selector table. This structure can also describe later device recipes whose output or payment depends on a specific offering's imbuement. Preserve existing spell recipe concealment; thread recipes are public.

The common viewer model now includes offering-linked installed-material requirements. The coordinated thread-design chat owns that implementation; see [recipe viewers](recipe-viewers.md) for its current native evidence. The dynamically compiled wand category is described below.

**Wand display direction, October 7:** derive wand inputs, relative seats and the bound output from the authoritative wand recipe/component rules through one reusable adapter into the existing shared JEI/EMI diagram. Do not manually author a display for each spell/base/thread/tip combination. The adapter must represent three identical source scroll variants, preserve source shaping in the preview, and retain unknown spell-name and hidden scroll-recipe behavior. The procedural binding compiler now feeds that common display model automatically.

### Component implementation

The five native thread items and shared imbuement-selected ritual are implemented and verified separately from the viewer pass. `MagicalThreadRecipe.types()` exposes each output ID, selector material and native item; `ingredients()` supplies the common offerings. `result(RitualInputs)` resolves the String-local selector from the captured four-node input. The existing ritual engine handles presentation, reservation and atomic consumption without compiling scroll shaping or unlocking spell knowledge.

The production models use five distinct 16×16 silhouettes and palettes. Stack capacity currently uses vanilla's default 64, pending final tuning. Thread items carry no equipment bonus, durability, attunement identity or geometry data themselves; the wand compiler interprets their selected identity through the profiles above. [Verification](../verification/magical-threads-2026-10-06/verification.json) records 118 passing unit tests and 15 passing focused Minecraft tests, including twelve new thread tests. Those checks predate the current artwork and combined-build installation evidence.

## Material reference and artwork

The [complete pinned Iron material reference](../research/iron-material-uses.md) establishes String as a Flimsy Journal/Conjurer's Talisman construction ingredient, Arcane Essence as a general magical crafting material and Arcane Cloth as a reusable component. It does not establish these string variants or a universal enchantment property for each selector. Native material behavior is authored independently, with Iron's optional quality values remaining inert.

All seven bases need tipped and untipped appearances. The owner clarified that thread choice does **not** change wand artwork; only base and optional tip determine it. The eight-tip direction is selected; its effects and artwork are implemented. The [full tip catalogue](wand-tip-catalog.md) proposes twenty standalone and eight optional Iron material directions, with eight standalone directions now selected for the starting palette. Eight tips would give **7 × (8 + 1) = 63** appearances; the earlier five-tip shortlist would give 42. A shared base/tip composition avoids redrawing every full combination. Five separate thread ingredient sprites are implemented. The owner approved the new wand direction and requested actual 16×16 pixels, which the production export and native capture verify.

## Component palette proposal

The [October 6 component palette](wand-components.md) proposes bases for trait affinity and durability, threads for casting/wear profiles, and independently authored additional tip effects. The owner supports the affinity and thread concepts; exact values remain provisional. The [full catalogue](wand-tip-catalog.md) expands the initial five-material shortlist with distinct secondary, support, movement and economy choices. Original tip-to-augment mappings are superseded by the owner's clarification. Both documents record direct trait-consumer requirements, typed cost composition and explicit interactions with existing shaping. The current baseline spell plans read Amplify, Range and Area; an elemental affinity therefore needs an explicit equipment consumer to influence those outcomes. The base/thread profiles are implemented starting tuning; tip effects are implemented and the coefficients are not final balance.

## Next steps

1. Implement the selected eight additional tip contributions with broad executable eligibility, bounded secondary effects and explicit costs.
2. Audit utility and shaped-source coverage; verify each tip in native Minecraft behavior tests.
3. Add public wand recipe-viewer displays and review base/tip artwork in the client.
4. Playtest component tradeoffs, then tune the initial coefficients.

The [current executable Spellshaping ledger](../spellshaping-recipes.md) describes shipped scroll rules. Wand core contributions use trusted native definitions separately; the tip catalogue records the locked direction and later candidates.

## Compositional artwork and dynamic displays

`tools/author_wand_models.py` exports seven body sprites, eight tip sprites and five thread sprites as actual 16×16 RGBA PNGs with hard alpha. Vanilla handheld models layer a body and optional tip into 63 appearances; the thread changes no wand artwork. Generated high-resolution source art and prompts are retained as provenance outside the packaged assets. `--check` verifies the exact production pixels and model definitions.

`WandDisplays` invokes the binding compiler for each source/core/tip and records an eligible-base mask. A display offers matching body/output alternatives and three copies of the exact source scroll; it is never manually authored per spell. Two unused outer seats remain empty, as does the tip seat on an untipped recipe. The source scroll's own recipe remains subject to discovery concealment. JEI focus links and EMI synchronized slot cycling keep base and resulting appearance aligned. Worn wands retain the same recipe identity.

Login/reload sends bounded pages of at most sixteen source variants each through the independent `wand_displays` payload (version 1). Source NBT is schema-validated and capped at 16 KiB, with at most 45 compiler-approved core/tip options and seven base bits. The session retains at most 64 acquired shaped variants in addition to the packaged catalog, scanned every forty ticks; reconnect rescans inventory. The client publishes only a complete snapshot and clears it on disconnect. Shaped sources resolve to their exact binding recipes. These are session display entries, not a permanent player collection or gameplay authority. EMI owns the category when both viewers are present; optional APIs remain isolated so neither is required.

EMI live refresh coalesces snapshot changes until both its reload manager and asynchronous recipe worker have finished. The pinned 1.1.24 implementation exposes the latter phase after its public recipe manager is already visible, so an isolated optional reflection bridge checks completion before posting resource events. Unsupported bridges fail closed for live refresh; neither JEI nor ordinary client loading references EMI classes. Structured immutable wand identities use weak caching, exclude damage/wear and avoid reparsing/formatting source scrolls during viewer comparison loops. Disconnect clears pending refresh work.
