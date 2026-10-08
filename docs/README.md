# Vestige documentation

## Current project and decisions

- [Development status](development-status.md): implemented work, verification, and remaining milestones.
- [Minecraft art and pixel scale](design/minecraft-art.md): required texture workflow, native pixel density, shading, conversion pitfalls and actual-client review; includes the approved thread baseline.
- [Repository and builds](repository.md): GitHub checks, downloadable jars, standalone testing and contribution workflow.
- [Spell reference](spell-reference.md): all 214 spells, worked examples, trait ratings, scaling, costs, targeting, effect plans, and current appearance.
- [Spell rarity and balance](design/spell-balance.md): four rarities, relative ratings, formula calibration, boost leverage, and comparisons within rarity.
- [Plinth rituals and geometry](design/plinth-rituals.md): accepted two-block direction and v3 geometry baseline, with two inner-only shapes and four inner/outer combinations. Native geometry, apparatus integration and local material Spellshaping are implemented.
- [Leyline output shaping](design/leyline-output-shaping.md): accepted flat-trait Amplify, Range, Area and Casting Cost responses, competing preferred layouts and explicit outcome consumers; crafted scroll storage and native application are implemented.
- [Leyline calculator baseline](design/leyline-calculator.md): accepted six-by-four trait matrices, independent stage heights/distances, minor offsets, final rounding and exhaustive best-layout search; exact cells are saved in [v3 rules](design/leyline-calculator-v3.json). [V2](design/leyline-calculator-v2.md) preserves the superseded single-response model; [v1](design/leyline-calculator-v1.md) retains the earlier rating/affinity proposal.
- [Leyline school balance audit](leyline-balance-audit.md): exhaustive four/eight-slot sweeps across 82 trait examples, school opportunity envelopes, winning footprint sizes, pyramid/Nature focuses, competing optima and smooth-neighborhood guards; numerical prototype evidence, not gameplay balance.
- [Attunement Shards](design/attunement-shards.md): implemented six-offering ritual on eight nodes, including two Amethyst Shards and one Echo Shard. Reproducible keys retain empty offering positions, active geometry and paired imbuements. Whole-arrangement quarter turns preserve keys; cosmetic carriers are excluded. Standing Stones are the first network device.
- [Attuned devices](design/attuned-devices.md): accepted Crane Bag, Whispering Shell and Homebound Eye directions; Bag storage and Eye returns are implemented, with Bag recipe/art and Shell gameplay deferred.
- [Crane Bag](design/crane-bag.md): native shared Bundle storage and weighted capacity by full signature, server-owned transfers, cross-dimension previews, persistence and common rune tooltips. Survival recipe and final art are deferred.
- [Whispering Shell iteration](design/whispering-shell.md): owner-selected hotbar transmission and Coiled spiral Shell art basis, with cleaner coil and Pearl heart Eye refinements considered separately. Final textures remain unapproved. The normal Amethyst/Soul Sand/Ghast Tear Echo Shard craft is accepted and implemented; the Shell's Sensor ritual and additional communication details remain proposals. Native Shell gameplay remains unimplemented.
- [Standing Stones](design/standing-stones.md): same-dimension matching-shard networks, a native destination list, current-stone naming, bottom signature, nearby arrival and 36 masonry finishes. The [current native menu](art/standing-stone-icons-corner/README.md) replaces the earlier schematic map. Sixteen native faceted profiles use key-selected rune marks with luminous ink and drifting glyph motes. Ordinary distance-based XP payment is implemented; alternate resource rates and the Pearl's Plinth payment selector remain deferred. [Eight whole-stone concepts](art/standing-stone-concepts/README.md) preserve the original appearance studies.
- [Native mana display](design/mana-display.md): shared violet resource rune, whole-point balance and horizontal bottom-right meter; fixed-height travel rows show actual full/half heart and food costs.
- [Standing Stone map APIs](research/standing-stone-maps.md): pinned Antique Atlas, Surveyor and Map Atlases evidence; Antique Atlas/Waystones compatibility is deferred optional work, potentially a future addon.
- [Executable Spellshaping recipes](spellshaping-recipes.md): 50 individual augments, six compounds, automatic routes and typed payments.
- [Spellshaping implementation](design/spellshaping.md): supported behavior, compatibility and limits; the unimplemented proposal catalog has been removed.
- [Scroll discovery and progression](design/spell-discovery.md): implemented unknown/identified states, scroll-only identification and mixed-fragment trait intersections with slot-counted average weights; the shared apparatus direction now follows Plinth rituals.
- [Wand crafting and magical string cores](design/wand-crafting.md): implemented exact three-scroll binding, seven bases, five thread profiles, deterministic wear and sixty-second wand recovery. Threads share String, Amethyst Shard and Honeycomb. Eight locked additional tip effects, dynamically derived wand displays, final artwork and testing-pack installation remain outstanding.
- [Wand component palette](design/wand-components.md): implemented initial base/thread profiles, exact source shaping, typed payments and deterministic wear; selected additional tips remain outstanding.
- [Wand tip ideas and combinations](design/wand-tip-catalog.md): the locked eight-tip direction, draft prices and combinations, plus twenty later candidates. Tips add distinct effects beyond existing scroll shaping; implementation and coverage verification remain outstanding.
- [Original Wizardry scroll-name colors](research/electroblob-scroll-name-colors.md): pinned white unknown/known scroll names, glyph rendering, separate spellbook tier colors and the separately accepted native rarity-color mapping.
- [Native scroll rarity colors](art/scroll-rarity-colors/README.md): inspected unknown/identified colors, italic augments and independent recipe memory in JEI and EMI.
- [Ritual recipe viewers](design/recipe-viewers.md): optional JEI/EMI displays with an ancient four/eight-Plinth sketch, central result, per-player identification, recipes concealed until successful crafting, and server-synchronized capacities.
- [Material crafting standard](design/material-crafting.md): Iron's properties/flavor inspire distinct native ingredient contributions. Records current construction and historical grids; material sockets select authored Spellshaping without a material-quality power ladder.
- [Apparatus variants](design/apparatus-variants.md): accepted interchangeable cosmetic carriers and proposed appearance palette; additional variants remain future work.
- [Apparatus finishes and construction recipes](design/apparatus-variants.md): all 36 stone/masonry stair finishes, matching blocks/slabs, one Spellstone or two Plinths per craft; carrier appearance has no magic effect.
- [Native apparatus](design/apparatus-models.md): the current Spellstone/Plinth blocks, shaped collision, saved offerings/material sockets and flat scroll collection. The [approved low three-piece Spellstone](art/spellstone-tent-table/README.md) is implemented across all 36 finishes; Prism installation remains pending. Successful rituals lift ingredients only; wrong placements shake sideways. Actual Minecraft captures are preserved. Legacy pedestal IDs and migration code are removed; the apparatus uses fresh worlds. See [leyline playtesting](leyline-playtesting.md) for current controls, bounds and worked examples.
- [Complete Iron material and ingredient reference](research/iron-material-uses.md): all 37 Jewelry materials and every input/output in the 261 pinned Spellbooks recipes, including Amethyst forms, metals/gems, school focuses, brewing, repairs, structural ingredients and optional routes.
- [Complete spell balance review](spell-balance-review.md): all 214 native outcome/cost reviews, direct per-creature ceilings, role tradeoffs, and tested limits.
- [Spell balance audit](spell-balance-audit.md): resolved parameters and numerical boost responses for all 214 spells, with detailed JSON results.
- [Pathfinder conversion ledger](design/pathfinder-spell-conversions.md): 100 native adaptations, original source references, native behavior and deliberate rule differences.
- [Pathfinder source batch](research/pathfinder-spell-batch.md): verified AoN references and Wanderer’s Guide access findings.
- [Pathfinder expansion research](research/pathfinder-expansion-batch.md): the sixteen additional verified sources, publication metadata and adaptation boundaries.
- [100-spell Pathfinder selection](pathfinder-spell-selection.md): all 100 implemented recipes, current costs, distinct gameplay families and canonical sources.
- [Diverse Pathfinder source research](research/pathfinder-diverse-batch.md): the 36 canonical source pages, editions, publication metadata and behavior boundaries.
- [Pathfinder candidate inventory](pathfinder-spell-inventory.md): 1,992 pinned source metadata entries across books, adventures, focus spells and rituals; a review queue for future native batches.
- [Pathfinder visual-library batch](research/pathfinder-visual-library-batch.md): 32 additional directly reviewed spell sources across eight gameplay/visual families, with proposed adaptations and missing mechanics.
- [Effects workshop](effects-workshop.md): watch all 214 actual Minecraft primary casts and record updated native footage.
- [Spell art direction](spell-art-direction.md): each spell's authored silhouette, secondary motion and material, with the visual brief and source references.
- [Native construction API research](research/native-construction-api.md): pinned Minecraft/NeoForge behavior supporting geometry, terrain and pet recovery.
- [Native perception API research](research/native-perception-api.md): client camera, physical size, sound and observer boundaries.
- [Spell presentation and expansion](design/spell-presentation-and-expansion.md): shared native visual layers, authoring and lifecycle rules, Wizardry references and remaining visual work.
- [Iron conversion ledger](design/iron-spell-conversions.md): all 110 native recipes, source links, behavior, and mechanic/presentation differences.
- [Spell runtime](design/spell-runtime.md): executable structure, data format, lifecycle rules, and current limits.
- [Trait catalog](design/trait-catalog.md): the accepted 50-trait repertoire, explicit scaling, volatility, and Iron school projection.
- [Magic glossary](../CONTEXT.md): canonical domain terms.
- [Project instructions](../AGENTS.md): current repository architecture and working commands.

## Design evidence

- [EchoLink source review](research/echolink-whispering-shell.md): the owner's newer communication plugin, pinned recipe, slots, routing, identity limitations and native chat constraints.
- [Sculk acquisition for the Shell](research/whispering-shell-sculk.md): verified Java 1.21.1 block-drop conditions and exact Ancient City chest probabilities, including a 23.24% Sensor acquisition chance without Silk Touch.
- [Legacy Sending Shell research](research/legacy-radio-attunement.md): pinned RPChat source and its recipe-derived channel identity, with design inferences for Attunement Shards.
- [Complete Jewelry material research](research/irons-jewelry-materials.md): all four channels for 37 materials, all 16 assembly patterns, three ordinary recipes, gemstone acquisition and optional provider requirements.
- [Iron spell materials and equipment research](research/irons-spell-materials.md): pinned focus ingredients, runes, core crafting components, fifteen Jewelry integration materials and equipment behavior.
- [Spell-conversion prototype](design/spell-conversion-prototype.md): the September 14 pressure tests that motivated sessions, bindings, manifestations, targeting, and event phases.
- [Framework compatibility sample](research/spell-framework-compatibility-sample.md): upstream behavior examined for native conversion and foreign interoperability.
- [Iron spell mapping](research/irons-spell-mapping.md): exact foreign identity and supported event seams.
- [Integration trait compatibility](research/integration-trait-compatibility.md): Iron, Create, and Kithkyn classification and integration considerations.
- [PF2e magic taxonomy](research/pf2e-magic-taxonomy.md): traditions, schools, realms, and trait vocabulary.
- [Wellspring and Iron systems](research/wellspring-and-irons-systems.md): behavior and interoperability research.

Research and historical pseudo-data show the intended direction; the runtime document and development status distinguish that direction from executable behavior.

- [Sixteen Standing Stone forms](art/standing-stone-native-v4/README.md): native family gallery and verification. [Luminous rune inspection](art/standing-stone-native-v3/README.md), [edge comparison](art/standing-stone-native-v2/README.md) and [first pass](art/standing-stone-native-v1/README.md) preserve earlier evidence.
