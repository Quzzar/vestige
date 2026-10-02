# Spell balance audit

Current catalog: **214 spells** with explicit rarity assignments. Gameplay balance is assessed through resolved outcomes, costs, timing, and constraints. Trait magnitudes and totals are authoring choices, not power scores or rarity limits.

This report evaluates every numerical expression in effects, modes, targets, impact/tick/end callbacks, and bindings. World facts remain symbolic: a ratio is exact only when it holds for every assignment of those facts. Mixed expressions with a context-dependent response are marked separately. Guards, hit chance, target count, event frequency, and state changes can alter final outcomes and are not simulated.

The detailed report includes baseline parameters after trait substitution, retaining world facts symbolically. Equivalent trait scales with compensated formulas preserve baseline outcomes and multiplicative boost responses. Fixed additions and thresholds require corresponding unit changes if ratings are rescaled; the runtime does not automatically normalize profiles.

Area coverage is a geometric proxy: doubling a queried radius gives 4× planar footprint or 8× volume. Neither figure promises that many targets. Amplify and area boosts together can compound to 8× aggregate damage under uniform planar target density, or 16× under uniform volumetric density, before target limits and world constraints.

Current attack formulas generally read amplify, while semantic school traits such as evocation classify the spell. Doubling an unused evocation rating leaves numerical parameters unchanged. Original Iron cooldowns remain provenance. Native cooldown costs are enforced by paid casting; the development `cast` command bypasses payment and recovery. See the complete balance review for role/outcome tuning.

Policy and formulas: [Spell rarity and balance](design/spell-balance.md). Detailed expression responses: [JSON audit](spell-balance-audit.json).

## Rarity groups

| Rarity | Spells |
|---|---|
| common | 51 |
| uncommon | 89 |
| rare | 64 |
| mythic | 10 |

## Rarity and evocation response

Rarity is independent of raw trait magnitudes. The response column records the largest exact change to a numerical parameter when only evocation doubles. A response of 1× means those parameters are unchanged; mixed responses depend on symbolic facts. This is not a whole-spell power score.

| Spell | Rarity | Double evocation parameter response | Mixed expressions |
|---|---|---|---|
| [Abyssal Shroud](spell-reference.md#abyssal-shroud) | mythic | 1× | 0 |
| [Acid Spit](spell-reference.md#acid-spit) | common | 1× | 0 |
| [Acupuncture](spell-reference.md#acupuncture) | uncommon | 1× | 0 |
| [Angel Wings](spell-reference.md#angel-wings) | rare | 1× | 0 |
| [Arcane Lock](spell-reference.md#arcane-lock) | uncommon | 1× | 0 |
| [Arcane Shackle](spell-reference.md#arcane-shackle) | rare | 1× | 0 |
| [Arrow Volley](spell-reference.md#arrow-volley) | rare | 1× | 0 |
| [Ascension](spell-reference.md#ascension) | rare | 1× | 0 |
| [Ball Lightning](spell-reference.md#ball-lightning) | rare | 1× | 0 |
| [Black Hole](spell-reference.md#black-hole) | mythic | 1× | 0 |
| [Blaze Storm](spell-reference.md#blaze-storm) | rare | 1× | 0 |
| [Blessing Of Life](spell-reference.md#blessing-of-life) | common | 1× | 0 |
| [Blight](spell-reference.md#blight) | rare | 1× | 0 |
| [Blizzard](spell-reference.md#blizzard) | rare | 1× | 0 |
| [Blood Needles](spell-reference.md#blood-needles) | uncommon | 1× | 0 |
| [Blood Slash](spell-reference.md#blood-slash) | uncommon | 1× | 0 |
| [Blood Step](spell-reference.md#blood-step) | uncommon | 1× | 0 |
| [Burning Dash](spell-reference.md#burning-dash) | uncommon | 1× | 0 |
| [Chain Creeper](spell-reference.md#chain-creeper) | rare | 1× | 0 |
| [Chain Lightning](spell-reference.md#chain-lightning) | uncommon | 1× | 0 |
| [Charge](spell-reference.md#charge) | uncommon | 1× | 0 |
| [Cleanse](spell-reference.md#cleanse) | uncommon | 1× | 0 |
| [Cone Of Cold](spell-reference.md#cone-of-cold) | rare | 1× | 0 |
| [Counterspell](spell-reference.md#counterspell) | rare | 1× | 0 |
| [Devour](spell-reference.md#devour) | rare | 1× | 0 |
| [Divine Smite](spell-reference.md#divine-smite) | uncommon | 1× | 0 |
| [Dragon Breath](spell-reference.md#dragon-breath) | rare | 1× | 0 |
| [Earthquake](spell-reference.md#earthquake) | rare | 1× | 0 |
| [Echoing Strikes](spell-reference.md#echoing-strikes) | rare | 1× | 0 |
| [Eldritch Blast](spell-reference.md#eldritch-blast) | rare | 1× | 0 |
| [Electrocute](spell-reference.md#electrocute) | rare | 1× | 0 |
| [Evasion](spell-reference.md#evasion) | uncommon | 1× | 0 |
| [Fang Strike](spell-reference.md#fang-strike) | uncommon | 1× | 0 |
| [Fang Swirl](spell-reference.md#fang-swirl) | uncommon | 1× | 0 |
| [Fang Ward](spell-reference.md#fang-ward) | uncommon | 1× | 0 |
| [Fire Arrow](spell-reference.md#fire-arrow) | uncommon | 1× | 0 |
| [Fire Breath](spell-reference.md#fire-breath) | rare | 1× | 0 |
| [Fireball](spell-reference.md#fireball) | uncommon | 1× | 0 |
| [Firebolt](spell-reference.md#firebolt) | common | 1× | 0 |
| [Firecracker](spell-reference.md#firecracker) | common | 1× | 0 |
| [Firefly Swarm](spell-reference.md#firefly-swarm) | rare | 1× | 0 |
| [Flaming Barrage](spell-reference.md#flaming-barrage) | uncommon | 1× | 0 |
| [Flaming Strike](spell-reference.md#flaming-strike) | uncommon | 1× | 0 |
| [Force Arrow](spell-reference.md#force-arrow) | common | 1× | 0 |
| [Fortify](spell-reference.md#fortify) | uncommon | 1× | 0 |
| [Frost Step](spell-reference.md#frost-step) | uncommon | 1× | 0 |
| [Frostbite](spell-reference.md#frostbite) | uncommon | 1× | 0 |
| [Frostwave](spell-reference.md#frostwave) | uncommon | 1× | 0 |
| [Gluttony](spell-reference.md#gluttony) | common | 1× | 0 |
| [Gravity Fissure](spell-reference.md#gravity-fissure) | rare | 1× | 0 |
| [Greater Heal](spell-reference.md#greater-heal) | rare | 1× | 0 |
| [Guiding Bolt](spell-reference.md#guiding-bolt) | uncommon | 1× | 0 |
| [Gust](spell-reference.md#gust) | uncommon | 1× | 0 |
| [Haste](spell-reference.md#haste) | uncommon | 1× | 0 |
| [Heal](spell-reference.md#heal) | common | 1× | 0 |
| [Healing Circle](spell-reference.md#healing-circle) | rare | 1× | 0 |
| [Heartstop](spell-reference.md#heartstop) | mythic | 1× | 0 |
| [Heat Surge](spell-reference.md#heat-surge) | uncommon | 1× | 0 |
| [Ice Block](spell-reference.md#ice-block) | rare | 1× | 0 |
| [Ice Spikes](spell-reference.md#ice-spikes) | rare | 1× | 0 |
| [Ice Tomb](spell-reference.md#ice-tomb) | rare | 1× | 0 |
| [Icicle](spell-reference.md#icicle) | common | 1× | 0 |
| [Interposing Earth](spell-reference.md#interposing-earth) | common | 1× | 0 |
| [Invisibility](spell-reference.md#invisibility) | uncommon | 1× | 0 |
| [Lightning Bolt](spell-reference.md#lightning-bolt) | uncommon | 1× | 0 |
| [Lightning Lance](spell-reference.md#lightning-lance) | uncommon | 1× | 0 |
| [Lob Creeper](spell-reference.md#lob-creeper) | uncommon | 1× | 0 |
| [Magic Arrow](spell-reference.md#magic-arrow) | uncommon | 1× | 0 |
| [Magic Missile](spell-reference.md#magic-missile) | common | 1× | 0 |
| [Magma Bomb](spell-reference.md#magma-bomb) | rare | 1× | 0 |
| [Oakskin](spell-reference.md#oakskin) | uncommon | 1× | 0 |
| [Air Bubble](spell-reference.md#air-bubble) | common | 1× | 0 |
| [Arctic Rift](spell-reference.md#arctic-rift) | mythic | 1× | 0 |
| [Bind Undead](spell-reference.md#bind-undead) | rare | 1× | 0 |
| [Breathe Fire](spell-reference.md#breathe-fire) | uncommon | 1× | 0 |
| [Cataclysm](spell-reference.md#cataclysm) | mythic | 1× | 0 |
| [Caustic Blast](spell-reference.md#caustic-blast) | common | 1× | 0 |
| [Chain Lightning](spell-reference.md#chain-lightning) | rare | 1× | 0 |
| [Cinder Swarm](spell-reference.md#cinder-swarm) | uncommon | 1× | 0 |
| [Clairvoyance](spell-reference.md#clairvoyance) | rare | 1× | 0 |
| [Collective Transposition](spell-reference.md#collective-transposition) | rare | 1× | 0 |
| [Containment](spell-reference.md#containment) | rare | 1× | 0 |
| [Create Water](spell-reference.md#create-water) | common | 1× | 0 |
| [Creation](spell-reference.md#creation) | uncommon | 1× | 0 |
| [Detect Magic](spell-reference.md#detect-magic) | common | 1× | 0 |
| [Divine Lance](spell-reference.md#divine-lance) | common | 1× | 0 |
| [Eclipse Burst](spell-reference.md#eclipse-burst) | mythic | 1× | 0 |
| [Electric Arc](spell-reference.md#electric-arc) | common | 1× | 0 |
| [Enfeeble](spell-reference.md#enfeeble) | common | 1× | 0 |
| [Enlarge](spell-reference.md#enlarge) | uncommon | 1× | 0 |
| [Falling Stars](spell-reference.md#falling-stars) | mythic | 1× | 0 |
| [False Vitality](spell-reference.md#false-vitality) | common | 1× | 0 |
| [Fear](spell-reference.md#fear) | common | 1× | 0 |
| [Field of Life](spell-reference.md#field-of-life) | rare | 1× | 0 |
| [Figment](spell-reference.md#figment) | common | 1× | 0 |
| [Fire Shield](spell-reference.md#fire-shield) | rare | 1× | 0 |
| [Fireball](spell-reference.md#fireball) | rare | 1× | 0 |
| [Fleet Step](spell-reference.md#fleet-step) | common | 1× | 0 |
| [Flicker](spell-reference.md#flicker) | uncommon | 1× | 0 |
| [Floating Flame](spell-reference.md#floating-flame) | uncommon | 1× | 0 |
| [Force Barrage](spell-reference.md#force-barrage) | uncommon | 1× | 0 |
| [Freezing Rain](spell-reference.md#freezing-rain) | uncommon | 1× | 0 |
| [Frostbite](spell-reference.md#frostbite) | common | 1× | 0 |
| [Gecko Grip](spell-reference.md#gecko-grip) | uncommon | 1× | 0 |
| [Gentle Breeze](spell-reference.md#gentle-breeze) | uncommon | 1× | 0 |
| [Gentle Landing](spell-reference.md#gentle-landing) | common | 1× | 0 |
| [Glass Shield](spell-reference.md#glass-shield) | common | 1× | 0 |
| [Gouging Claw](spell-reference.md#gouging-claw) | common | 1× | 0 |
| [Gravity Well](spell-reference.md#gravity-well) | uncommon | 1× | 0 |
| [Grease](spell-reference.md#grease) | common | 1× | 0 |
| [Grim Tendrils](spell-reference.md#grim-tendrils) | uncommon | 1× | 0 |
| [Harm](spell-reference.md#harm) | uncommon | 1× | 0 |
| [Haste](spell-reference.md#haste) | uncommon | 1× | 0 |
| [Heal](spell-reference.md#heal) | uncommon | 1× | 0 |
| [Hydraulic Push](spell-reference.md#hydraulic-push) | uncommon | 1× | 0 |
| [Ignition](spell-reference.md#ignition) | common | 1× | 0 |
| [Illusory Creature](spell-reference.md#illusory-creature) | uncommon | 1× | 0 |
| [Illusory Object](spell-reference.md#illusory-object) | common | 1× | 0 |
| [Invisibility](spell-reference.md#invisibility) | uncommon | 1× | 0 |
| [Item Facade](spell-reference.md#item-facade) | common | 1× | 0 |
| [Lightning Bolt](spell-reference.md#lightning-bolt) | rare | 1× | 0 |
| [Magic Passage](spell-reference.md#magic-passage) | rare | 1× | 0 |
| [Magnetic Attraction](spell-reference.md#magnetic-attraction) | common | 1× | 0 |
| [Mirror Image](spell-reference.md#mirror-image) | uncommon | 1× | 0 |
| [Mud Pit](spell-reference.md#mud-pit) | uncommon | 1× | 0 |
| [Needle Darts](spell-reference.md#needle-darts) | common | 1× | 0 |
| [Peaceful Bubble](spell-reference.md#peaceful-bubble) | uncommon | 1× | 0 |
| [Pet Cache](spell-reference.md#pet-cache) | uncommon | 1× | 0 |
| [Protection](spell-reference.md#protection) | uncommon | 1× | 0 |
| [Protector Tree](spell-reference.md#protector-tree) | rare | 1× | 0 |
| [Puff of Poison](spell-reference.md#puff-of-poison) | common | 1× | 0 |
| [Read Aura](spell-reference.md#read-aura) | common | 1× | 0 |
| [Regenerate](spell-reference.md#regenerate) | rare | 1× | 0 |
| [Repulsion](spell-reference.md#repulsion) | rare | 1× | 0 |
| [Resist Energy](spell-reference.md#resist-energy) | uncommon | 1× | 0 |
| [Revealing Light](spell-reference.md#revealing-light) | common | 1× | 0 |
| [Rust Cloud](spell-reference.md#rust-cloud) | rare | 1× | 0 |
| [Scatter Scree](spell-reference.md#scatter-scree) | common | 1× | 0 |
| [See the Unseen](spell-reference.md#see-the-unseen) | uncommon | 1× | 0 |
| [Shape Stone](spell-reference.md#shape-stone) | uncommon | 1× | 0 |
| [Share Life](spell-reference.md#share-life) | uncommon | 1× | 0 |
| [Shield](spell-reference.md#shield) | common | 1× | 0 |
| [Shrink](spell-reference.md#shrink) | uncommon | 1× | 0 |
| [Silence](spell-reference.md#silence) | uncommon | 1× | 0 |
| [Slashing Gust](spell-reference.md#slashing-gust) | common | 1× | 0 |
| [Slow](spell-reference.md#slow) | uncommon | 1× | 0 |
| [Soothe](spell-reference.md#soothe) | uncommon | 1× | 0 |
| [Spirit Blast](spell-reference.md#spirit-blast) | rare | 1× | 0 |
| [Spiritual Armament](spell-reference.md#spiritual-armament) | uncommon | 1× | 0 |
| [Spout](spell-reference.md#spout) | common | 1× | 0 |
| [Status](spell-reference.md#status) | common | 1× | 0 |
| [Summon Animal](spell-reference.md#summon-animal) | uncommon | 1× | 0 |
| [Summon Elemental](spell-reference.md#summon-elemental) | rare | 1× | 0 |
| [Summon Fey](spell-reference.md#summon-fey) | uncommon | 1× | 0 |
| [Summon Plant or Fungus](spell-reference.md#summon-plant-or-fungus) | uncommon | 1× | 0 |
| [Tangle Vine](spell-reference.md#tangle-vine) | common | 1× | 0 |
| [Telekinetic Projectile](spell-reference.md#telekinetic-projectile) | common | 1× | 0 |
| [Thunderstrike](spell-reference.md#thunderstrike) | uncommon | 1× | 0 |
| [Time Jump](spell-reference.md#time-jump) | rare | 1× | 0 |
| [Translocate](spell-reference.md#translocate) | uncommon | 1× | 0 |
| [Vampiric Feast](spell-reference.md#vampiric-feast) | uncommon | 1× | 0 |
| [Vitality Lash](spell-reference.md#vitality-lash) | common | 1× | 0 |
| [Void Warp](spell-reference.md#void-warp) | common | 1× | 0 |
| [Wall of Ice](spell-reference.md#wall-of-ice) | rare | 1× | 0 |
| [Wall of Stone](spell-reference.md#wall-of-stone) | rare | 1× | 0 |
| [Wall of Water](spell-reference.md#wall-of-water) | uncommon | 1× | 0 |
| [Water Breathing](spell-reference.md#water-breathing) | common | 1× | 0 |
| [Water Walk](spell-reference.md#water-walk) | uncommon | 1× | 0 |
| [Weapon Storm](spell-reference.md#weapon-storm) | rare | 1× | 0 |
| [Wooden Double](spell-reference.md#wooden-double) | uncommon | 1× | 0 |
| [Zephyr Slip](spell-reference.md#zephyr-slip) | uncommon | 1× | 0 |
| [Planar Sight](spell-reference.md#planar-sight) | rare | 1× | 0 |
| [Pocket Dimension](spell-reference.md#pocket-dimension) | mythic | 1× | 0 |
| [Poison Arrow](spell-reference.md#poison-arrow) | common | 1× | 0 |
| [Poison Spray](spell-reference.md#poison-spray) | rare | 1× | 0 |
| [Poison Splash](spell-reference.md#poison-splash) | uncommon | 1× | 0 |
| [Portal](spell-reference.md#portal) | rare | 1× | 0 |
| [Raise Dead](spell-reference.md#raise-dead) | rare | 1× | 0 |
| [Raise Hell](spell-reference.md#raise-hell) | rare | 1× | 0 |
| [Ray Of Frost](spell-reference.md#ray-of-frost) | rare | 1× | 0 |
| [Ray Of Siphoning](spell-reference.md#ray-of-siphoning) | rare | 1× | 0 |
| [Recall](spell-reference.md#recall) | uncommon | 1× | 0 |
| [Root](spell-reference.md#root) | uncommon | 1× | 0 |
| [Sacrifice](spell-reference.md#sacrifice) | rare | 1× | 0 |
| [Scapegoat](spell-reference.md#scapegoat) | uncommon | 1× | 0 |
| [Scorch](spell-reference.md#scorch) | uncommon | 1× | 0 |
| [Sculk Tentacles](spell-reference.md#sculk-tentacles) | rare | 1× | 0 |
| [Shadow Slash](spell-reference.md#shadow-slash) | uncommon | 1× | 0 |
| [Shield](spell-reference.md#shield) | uncommon | 1× | 0 |
| [Shockwave](spell-reference.md#shockwave) | uncommon | 1× | 0 |
| [Slow](spell-reference.md#slow) | common | 1× | 0 |
| [Snowball](spell-reference.md#snowball) | common | 1× | 0 |
| [Sonic Boom](spell-reference.md#sonic-boom) | rare | 1× | 0 |
| [Spectral Hammer](spell-reference.md#spectral-hammer) | uncommon | 1× | 0 |
| [Aspect of the Spider](spell-reference.md#aspect-of-the-spider) | uncommon | 1× | 0 |
| [Starfall](spell-reference.md#starfall) | mythic | 1× | 0 |
| [Stomp](spell-reference.md#stomp) | uncommon | 1× | 0 |
| [Summon Ender Chest](spell-reference.md#summon-ender-chest) | uncommon | 1× | 0 |
| [Summon Horse](spell-reference.md#summon-horse) | uncommon | 1× | 0 |
| [Summon Polar Bear](spell-reference.md#summon-polar-bear) | rare | 1× | 0 |
| [Summon Swords](spell-reference.md#summon-swords) | rare | 1× | 0 |
| [Summon Vex](spell-reference.md#summon-vex) | rare | 1× | 0 |
| [Summon Zombie](spell-reference.md#summon-zombie) | uncommon | 1× | 0 |
| [Sunbeam](spell-reference.md#sunbeam) | rare | 1× | 0 |
| [Telekinesis](spell-reference.md#telekinesis) | rare | 1× | 0 |
| [Teleport](spell-reference.md#teleport) | common | 1× | 0 |
| [Throw](spell-reference.md#throw) | common | 1× | 0 |
| [Thunderstorm](spell-reference.md#thunderstorm) | mythic | 1× | 0 |
| [Touch Dig](spell-reference.md#touch-dig) | common | 1× | 0 |
| [Volt Strike](spell-reference.md#volt-strike) | rare | 1× | 0 |
| [Wall Of Fire](spell-reference.md#wall-of-fire) | rare | 1× | 0 |
| [Wisp](spell-reference.md#wisp) | rare | 1× | 0 |
| [Wither Skull](spell-reference.md#wither-skull) | uncommon | 1× | 0 |
| [Wololo](spell-reference.md#wololo) | common | 1× | 0 |

## Numerical responses to boosts

These columns show the largest exact change to an individual numerical parameter, which can be damage, healing, reach, or another value. They are not whole-spell power multipliers. “Mixed” counts expressions whose response depends on world or stored facts. Coverage counts only queried radii; it does not infer hit count or combine unrelated outcomes.

| Spell | Double amplify | Double range | Double area | Area planar proxy | Area volume proxy | Mixed expressions under amplify |
|---|---|---|---|---|---|---|
| [Abyssal Shroud](spell-reference.md#abyssal-shroud) | 1× | 1× | 1× | 1× | 1× | 0 |
| [Acid Spit](spell-reference.md#acid-spit) | 2× | 2× | 2× | 4× | 8× | 0 |
| [Acupuncture](spell-reference.md#acupuncture) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Angel Wings](spell-reference.md#angel-wings) | 1× | 1× | 1× | 1× | 1× | 0 |
| [Arcane Lock](spell-reference.md#arcane-lock) | 1× | 2× | 1× | 1× | 1× | 0 |
| [Arcane Shackle](spell-reference.md#arcane-shackle) | 1× | 2× | 2× | 4× | 8× | 0 |
| [Arrow Volley](spell-reference.md#arrow-volley) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Ascension](spell-reference.md#ascension) | 2× | 1× | 2× | 4× | 8× | 0 |
| [Ball Lightning](spell-reference.md#ball-lightning) | 2× | 2× | 2× | 4× | 8× | 0 |
| [Black Hole](spell-reference.md#black-hole) | 2× | 2× | 2× | 4× | 8× | 0 |
| [Blaze Storm](spell-reference.md#blaze-storm) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Blessing Of Life](spell-reference.md#blessing-of-life) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Blight](spell-reference.md#blight) | 1× | 2× | 1× | 1× | 1× | 0 |
| [Blizzard](spell-reference.md#blizzard) | 2× | 2× | 2× | 4× | 8× | 0 |
| [Blood Needles](spell-reference.md#blood-needles) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Blood Slash](spell-reference.md#blood-slash) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Blood Step](spell-reference.md#blood-step) | 1× | 2× | 1× | 1× | 1× | 0 |
| [Burning Dash](spell-reference.md#burning-dash) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Chain Creeper](spell-reference.md#chain-creeper) | 2× | 2× | 2× | 4× | 8× | 0 |
| [Chain Lightning](spell-reference.md#chain-lightning) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Charge](spell-reference.md#charge) | 1× | 1× | 1× | 1× | 1× | 0 |
| [Cleanse](spell-reference.md#cleanse) | 1× | 1× | 2× | 4× | 8× | 0 |
| [Cone Of Cold](spell-reference.md#cone-of-cold) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Counterspell](spell-reference.md#counterspell) | 1× | 2× | 1× | 1× | 1× | 0 |
| [Devour](spell-reference.md#devour) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Divine Smite](spell-reference.md#divine-smite) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Dragon Breath](spell-reference.md#dragon-breath) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Earthquake](spell-reference.md#earthquake) | 2× | 2× | 2× | 4× | 8× | 0 |
| [Echoing Strikes](spell-reference.md#echoing-strikes) | 2× | 1× | 1× | 1× | 1× | 0 |
| [Eldritch Blast](spell-reference.md#eldritch-blast) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Electrocute](spell-reference.md#electrocute) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Evasion](spell-reference.md#evasion) | 1× | 1× | 1× | 1× | 1× | 0 |
| [Fang Strike](spell-reference.md#fang-strike) | 2× | 1× | 1× | 1× | 1× | 0 |
| [Fang Swirl](spell-reference.md#fang-swirl) | 2× | 2× | 2× | 1× | 1× | 0 |
| [Fang Ward](spell-reference.md#fang-ward) | 2× | 1× | 2× | 1× | 1× | 0 |
| [Fire Arrow](spell-reference.md#fire-arrow) | 2× | 2× | 2× | 4× | 8× | 0 |
| [Fire Breath](spell-reference.md#fire-breath) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Fireball](spell-reference.md#fireball) | 2× | 2× | 2× | 4× | 8× | 0 |
| [Firebolt](spell-reference.md#firebolt) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Firecracker](spell-reference.md#firecracker) | 2× | 2× | 2× | 4× | 8× | 0 |
| [Firefly Swarm](spell-reference.md#firefly-swarm) | 2× | 2× | 2× | 4× | 8× | 0 |
| [Flaming Barrage](spell-reference.md#flaming-barrage) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Flaming Strike](spell-reference.md#flaming-strike) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Force Arrow](spell-reference.md#force-arrow) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Fortify](spell-reference.md#fortify) | 1× | 1× | 2× | 4× | 8× | 0 |
| [Frost Step](spell-reference.md#frost-step) | 1× | 2× | 2× | 4× | 8× | 0 |
| [Frostbite](spell-reference.md#frostbite) | 2× | 1× | 2× | 4× | 8× | 0 |
| [Frostwave](spell-reference.md#frostwave) | 2× | 1× | 2× | 4× | 8× | 0 |
| [Gluttony](spell-reference.md#gluttony) | 1× | 1× | 1× | 1× | 1× | 0 |
| [Gravity Fissure](spell-reference.md#gravity-fissure) | 2× | 1× | 2× | 4× | 8× | 0 |
| [Greater Heal](spell-reference.md#greater-heal) | 2× | 1× | 1× | 1× | 1× | 0 |
| [Guiding Bolt](spell-reference.md#guiding-bolt) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Gust](spell-reference.md#gust) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Haste](spell-reference.md#haste) | 1× | 1× | 2× | 4× | 8× | 0 |
| [Heal](spell-reference.md#heal) | 2× | 1× | 1× | 1× | 1× | 0 |
| [Healing Circle](spell-reference.md#healing-circle) | 2× | 2× | 2× | 4× | 8× | 0 |
| [Heartstop](spell-reference.md#heartstop) | 1× | 1× | 1× | 1× | 1× | 0 |
| [Heat Surge](spell-reference.md#heat-surge) | 2× | 1× | 2× | 4× | 8× | 0 |
| [Ice Block](spell-reference.md#ice-block) | 2× | 2× | 2× | 4× | 8× | 0 |
| [Ice Spikes](spell-reference.md#ice-spikes) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Ice Tomb](spell-reference.md#ice-tomb) | 2× | 1× | 1× | 1× | 1× | 0 |
| [Icicle](spell-reference.md#icicle) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Interposing Earth](spell-reference.md#interposing-earth) | 2× | 1× | 1× | 1× | 1× | 0 |
| [Invisibility](spell-reference.md#invisibility) | 1× | 1× | 1× | 1× | 1× | 0 |
| [Lightning Bolt](spell-reference.md#lightning-bolt) | 2× | 2× | 2× | 4× | 8× | 0 |
| [Lightning Lance](spell-reference.md#lightning-lance) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Lob Creeper](spell-reference.md#lob-creeper) | 2× | 2× | 2× | 4× | 8× | 0 |
| [Magic Arrow](spell-reference.md#magic-arrow) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Magic Missile](spell-reference.md#magic-missile) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Magma Bomb](spell-reference.md#magma-bomb) | 2× | 2× | 2× | 4× | 8× | 0 |
| [Oakskin](spell-reference.md#oakskin) | 1× | 1× | 1× | 1× | 1× | 0 |
| [Air Bubble](spell-reference.md#air-bubble) | 1× | 1× | 1× | 1× | 1× | 0 |
| [Arctic Rift](spell-reference.md#arctic-rift) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Bind Undead](spell-reference.md#bind-undead) | 1× | 2× | 1× | 1× | 1× | 0 |
| [Breathe Fire](spell-reference.md#breathe-fire) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Cataclysm](spell-reference.md#cataclysm) | 2× | 2× | 2× | 4× | 8× | 0 |
| [Caustic Blast](spell-reference.md#caustic-blast) | 2× | 2× | 2× | 4× | 8× | 0 |
| [Chain Lightning](spell-reference.md#chain-lightning) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Cinder Swarm](spell-reference.md#cinder-swarm) | 2× | 2× | 2× | 1× | 1× | 0 |
| [Clairvoyance](spell-reference.md#clairvoyance) | 1× | 2× | 1× | 1× | 1× | 0 |
| [Collective Transposition](spell-reference.md#collective-transposition) | 1× | 2× | 1× | 1× | 1× | 0 |
| [Containment](spell-reference.md#containment) | 2× | 2× | 2× | 1× | 1× | 0 |
| [Create Water](spell-reference.md#create-water) | 1× | 2× | 1× | 1× | 1× | 0 |
| [Creation](spell-reference.md#creation) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Detect Magic](spell-reference.md#detect-magic) | 1× | 2× | 1× | 1× | 1× | 0 |
| [Divine Lance](spell-reference.md#divine-lance) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Eclipse Burst](spell-reference.md#eclipse-burst) | 2× | 2× | 2× | 4× | 8× | 0 |
| [Electric Arc](spell-reference.md#electric-arc) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Enfeeble](spell-reference.md#enfeeble) | 1× | 2× | 1× | 1× | 1× | 0 |
| [Enlarge](spell-reference.md#enlarge) | 1× | 1× | 1× | 1× | 1× | 0 |
| [Falling Stars](spell-reference.md#falling-stars) | 2× | 2× | 2× | 4× | 8× | 0 |
| [False Vitality](spell-reference.md#false-vitality) | 1× | 1× | 1× | 1× | 1× | 0 |
| [Fear](spell-reference.md#fear) | 1× | 2× | 1× | 1× | 1× | 0 |
| [Field of Life](spell-reference.md#field-of-life) | 2× | 2× | 2× | 4× | 8× | 0 |
| [Figment](spell-reference.md#figment) | 1× | 2× | 2× | 1× | 1× | 0 |
| [Fire Shield](spell-reference.md#fire-shield) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Fireball](spell-reference.md#fireball) | 2× | 2× | 2× | 4× | 8× | 0 |
| [Fleet Step](spell-reference.md#fleet-step) | 1× | 1× | 1× | 1× | 1× | 0 |
| [Flicker](spell-reference.md#flicker) | 1× | 1× | 1× | 1× | 1× | 0 |
| [Floating Flame](spell-reference.md#floating-flame) | 2× | 2× | 2× | 4× | 8× | 0 |
| [Force Barrage](spell-reference.md#force-barrage) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Freezing Rain](spell-reference.md#freezing-rain) | 1× | 2× | 2× | 1× | 1× | 0 |
| [Frostbite](spell-reference.md#frostbite) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Gecko Grip](spell-reference.md#gecko-grip) | 1× | 1× | 1× | 1× | 1× | 0 |
| [Gentle Breeze](spell-reference.md#gentle-breeze) | 2× | 2× | 2× | 4× | 8× | 0 |
| [Gentle Landing](spell-reference.md#gentle-landing) | 1× | 1× | 1× | 1× | 1× | 0 |
| [Glass Shield](spell-reference.md#glass-shield) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Gouging Claw](spell-reference.md#gouging-claw) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Gravity Well](spell-reference.md#gravity-well) | 2× | 2× | 2× | 4× | 8× | 0 |
| [Grease](spell-reference.md#grease) | 1× | 2× | 2× | 4× | 8× | 0 |
| [Grim Tendrils](spell-reference.md#grim-tendrils) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Harm](spell-reference.md#harm) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Haste](spell-reference.md#haste) | 1× | 1× | 1× | 1× | 1× | 0 |
| [Heal](spell-reference.md#heal) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Hydraulic Push](spell-reference.md#hydraulic-push) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Ignition](spell-reference.md#ignition) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Illusory Creature](spell-reference.md#illusory-creature) | 1× | 2× | 1× | 1× | 1× | 0 |
| [Illusory Object](spell-reference.md#illusory-object) | 1× | 2× | 1× | 1× | 1× | 0 |
| [Invisibility](spell-reference.md#invisibility) | 1× | 1× | 1× | 1× | 1× | 0 |
| [Item Facade](spell-reference.md#item-facade) | 1× | 1× | 1× | 1× | 1× | 0 |
| [Lightning Bolt](spell-reference.md#lightning-bolt) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Magic Passage](spell-reference.md#magic-passage) | 1× | 2× | 1× | 1× | 1× | 0 |
| [Magnetic Attraction](spell-reference.md#magnetic-attraction) | 1× | 2× | 1× | 1× | 1× | 0 |
| [Mirror Image](spell-reference.md#mirror-image) | 2× | 1× | 1× | 1× | 1× | 0 |
| [Mud Pit](spell-reference.md#mud-pit) | 1× | 2× | 2× | 4× | 8× | 0 |
| [Needle Darts](spell-reference.md#needle-darts) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Peaceful Bubble](spell-reference.md#peaceful-bubble) | 1× | 1× | 2× | 1× | 1× | 0 |
| [Pet Cache](spell-reference.md#pet-cache) | 1× | 2× | 1× | 1× | 1× | 0 |
| [Protection](spell-reference.md#protection) | 1× | 1× | 1× | 1× | 1× | 0 |
| [Protector Tree](spell-reference.md#protector-tree) | 2× | 2× | 2× | 1× | 1× | 0 |
| [Puff of Poison](spell-reference.md#puff-of-poison) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Read Aura](spell-reference.md#read-aura) | 1× | 1× | 1× | 1× | 1× | 0 |
| [Regenerate](spell-reference.md#regenerate) | 2× | 1× | 1× | 1× | 1× | 0 |
| [Repulsion](spell-reference.md#repulsion) | 1× | 1× | 2× | 1× | 1× | 0 |
| [Resist Energy](spell-reference.md#resist-energy) | 1× | 1× | 1× | 1× | 1× | 0 |
| [Revealing Light](spell-reference.md#revealing-light) | 1× | 2× | 2× | 4× | 8× | 0 |
| [Rust Cloud](spell-reference.md#rust-cloud) | 2× | 2× | 2× | 4× | 8× | 0 |
| [Scatter Scree](spell-reference.md#scatter-scree) | 2× | 2× | 2× | 4× | 8× | 0 |
| [See the Unseen](spell-reference.md#see-the-unseen) | 1× | 2× | 1× | 1× | 1× | 0 |
| [Shape Stone](spell-reference.md#shape-stone) | 1× | 2× | 1× | 1× | 1× | 0 |
| [Share Life](spell-reference.md#share-life) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Shield](spell-reference.md#shield) | 2× | 1× | 1× | 1× | 1× | 0 |
| [Shrink](spell-reference.md#shrink) | 1× | 1× | 1× | 1× | 1× | 0 |
| [Silence](spell-reference.md#silence) | 1× | 2× | 2× | 1× | 1× | 0 |
| [Slashing Gust](spell-reference.md#slashing-gust) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Slow](spell-reference.md#slow) | 1× | 2× | 1× | 1× | 1× | 0 |
| [Soothe](spell-reference.md#soothe) | 2× | 1× | 1× | 1× | 1× | 0 |
| [Spirit Blast](spell-reference.md#spirit-blast) | 2× | 2× | 2× | 4× | 8× | 0 |
| [Spiritual Armament](spell-reference.md#spiritual-armament) | 2× | 2× | 2× | 1× | 1× | 0 |
| [Spout](spell-reference.md#spout) | 2× | 2× | 2× | 4× | 8× | 0 |
| [Status](spell-reference.md#status) | 1× | 2× | 1× | 1× | 1× | 0 |
| [Summon Animal](spell-reference.md#summon-animal) | 1× | 1× | 1× | 1× | 1× | 0 |
| [Summon Elemental](spell-reference.md#summon-elemental) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Summon Fey](spell-reference.md#summon-fey) | 1× | 2× | 2× | 1× | 1× | 0 |
| [Summon Plant or Fungus](spell-reference.md#summon-plant-or-fungus) | 2× | 2× | 2× | 1× | 1× | 0 |
| [Tangle Vine](spell-reference.md#tangle-vine) | 1× | 2× | 1× | 1× | 1× | 0 |
| [Telekinetic Projectile](spell-reference.md#telekinetic-projectile) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Thunderstrike](spell-reference.md#thunderstrike) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Time Jump](spell-reference.md#time-jump) | 1× | 1× | 1× | 1× | 1× | 0 |
| [Translocate](spell-reference.md#translocate) | 1× | 2× | 1× | 1× | 1× | 0 |
| [Vampiric Feast](spell-reference.md#vampiric-feast) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Vitality Lash](spell-reference.md#vitality-lash) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Void Warp](spell-reference.md#void-warp) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Wall of Ice](spell-reference.md#wall-of-ice) | 1× | 2× | 2× | 1× | 1× | 0 |
| [Wall of Stone](spell-reference.md#wall-of-stone) | 2× | 2× | 2× | 1× | 1× | 0 |
| [Wall of Water](spell-reference.md#wall-of-water) | 1× | 2× | 2× | 1× | 1× | 0 |
| [Water Breathing](spell-reference.md#water-breathing) | 1× | 2× | 1× | 1× | 1× | 0 |
| [Water Walk](spell-reference.md#water-walk) | 1× | 1× | 1× | 1× | 1× | 0 |
| [Weapon Storm](spell-reference.md#weapon-storm) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Wooden Double](spell-reference.md#wooden-double) | 2× | 1× | 1× | 1× | 1× | 0 |
| [Zephyr Slip](spell-reference.md#zephyr-slip) | 1× | 1× | 2× | 1× | 1× | 0 |
| [Planar Sight](spell-reference.md#planar-sight) | 1× | 1× | 2× | 4× | 8× | 0 |
| [Pocket Dimension](spell-reference.md#pocket-dimension) | 1× | 1× | 1× | 1× | 1× | 0 |
| [Poison Arrow](spell-reference.md#poison-arrow) | 2× | 2× | 2× | 4× | 8× | 0 |
| [Poison Spray](spell-reference.md#poison-spray) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Poison Splash](spell-reference.md#poison-splash) | 2× | 2× | 2× | 4× | 8× | 0 |
| [Portal](spell-reference.md#portal) | 1× | 2× | 1× | 1× | 1× | 0 |
| [Raise Dead](spell-reference.md#raise-dead) | 1× | 1× | 1× | 1× | 1× | 0 |
| [Raise Hell](spell-reference.md#raise-hell) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Ray Of Frost](spell-reference.md#ray-of-frost) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Ray Of Siphoning](spell-reference.md#ray-of-siphoning) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Recall](spell-reference.md#recall) | 1× | 1× | 1× | 1× | 1× | 0 |
| [Root](spell-reference.md#root) | 1× | 2× | 1× | 1× | 1× | 0 |
| [Sacrifice](spell-reference.md#sacrifice) | 1× | 2× | 2× | 1× | 1× | 1 |
| [Scapegoat](spell-reference.md#scapegoat) | 1× | 2× | 1× | 1× | 1× | 0 |
| [Scorch](spell-reference.md#scorch) | 2× | 2× | 2× | 4× | 8× | 0 |
| [Sculk Tentacles](spell-reference.md#sculk-tentacles) | 2× | 2× | 2× | 4× | 8× | 0 |
| [Shadow Slash](spell-reference.md#shadow-slash) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Shield](spell-reference.md#shield) | 1× | 2× | 1× | 1× | 1× | 0 |
| [Shockwave](spell-reference.md#shockwave) | 2× | 1× | 2× | 4× | 8× | 0 |
| [Slow](spell-reference.md#slow) | 1× | 2× | 1× | 1× | 1× | 0 |
| [Snowball](spell-reference.md#snowball) | 1× | 2× | 2× | 4× | 8× | 0 |
| [Sonic Boom](spell-reference.md#sonic-boom) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Spectral Hammer](spell-reference.md#spectral-hammer) | 1× | 2× | 1× | 1× | 1× | 0 |
| [Aspect of the Spider](spell-reference.md#aspect-of-the-spider) | 2× | 1× | 1× | 1× | 1× | 0 |
| [Starfall](spell-reference.md#starfall) | 2× | 2× | 2× | 4× | 8× | 0 |
| [Stomp](spell-reference.md#stomp) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Summon Ender Chest](spell-reference.md#summon-ender-chest) | 1× | 1× | 1× | 1× | 1× | 0 |
| [Summon Horse](spell-reference.md#summon-horse) | 1× | 1× | 1× | 1× | 1× | 0 |
| [Summon Polar Bear](spell-reference.md#summon-polar-bear) | 1× | 1× | 1× | 1× | 1× | 0 |
| [Summon Swords](spell-reference.md#summon-swords) | 1× | 1× | 1× | 1× | 1× | 0 |
| [Summon Vex](spell-reference.md#summon-vex) | 1× | 1× | 1× | 1× | 1× | 0 |
| [Summon Zombie](spell-reference.md#summon-zombie) | 1× | 1× | 1× | 1× | 1× | 0 |
| [Sunbeam](spell-reference.md#sunbeam) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Telekinesis](spell-reference.md#telekinesis) | 1× | 2× | 1× | 1× | 1× | 0 |
| [Teleport](spell-reference.md#teleport) | 1× | 2× | 1× | 1× | 1× | 0 |
| [Throw](spell-reference.md#throw) | 1× | 2× | 1× | 1× | 1× | 1 |
| [Thunderstorm](spell-reference.md#thunderstorm) | 2× | 1× | 2× | 4× | 8× | 0 |
| [Touch Dig](spell-reference.md#touch-dig) | 1× | 2× | 1× | 1× | 1× | 0 |
| [Volt Strike](spell-reference.md#volt-strike) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Wall Of Fire](spell-reference.md#wall-of-fire) | 2× | 2× | 1× | 1× | 1× | 0 |
| [Wisp](spell-reference.md#wisp) | 2× | 2× | 2× | 4× | 8× | 0 |
| [Wither Skull](spell-reference.md#wither-skull) | 2× | 2× | 2× | 4× | 8× | 0 |
| [Wololo](spell-reference.md#wololo) | 1× | 2× | 1× | 1× | 1× | 0 |

## Gameplay review flags

| Spell | Review required |
|---|---|
| [Abyssal Shroud](spell-reference.md#abyssal-shroud) | event-driven value depends on event frequency |
| [Angel Wings](spell-reference.md#angel-wings) | utility requires comparison beyond HP |
| [Arcane Lock](spell-reference.md#arcane-lock) | utility requires comparison beyond HP |
| [Ball Lightning](spell-reference.md#ball-lightning) | persistent pulses depend on lifetime and tick scheduling |
| [Black Hole](spell-reference.md#black-hole) | persistent pulses depend on lifetime and tick scheduling |
| [Blaze Storm](spell-reference.md#blaze-storm) | repeated plan needs cumulative outcome review |
| [Blight](spell-reference.md#blight) | event-driven value depends on event frequency |
| [Blizzard](spell-reference.md#blizzard) | persistent pulses depend on lifetime and tick scheduling |
| [Blood Step](spell-reference.md#blood-step) | utility requires comparison beyond HP |
| [Burning Dash](spell-reference.md#burning-dash) | repeated plan needs cumulative outcome review |
| [Chain Creeper](spell-reference.md#chain-creeper) | event-driven value depends on event frequency |
| [Cone Of Cold](spell-reference.md#cone-of-cold) | repeated plan needs cumulative outcome review |
| [Devour](spell-reference.md#devour) | event-driven value depends on event frequency |
| [Dragon Breath](spell-reference.md#dragon-breath) | repeated plan needs cumulative outcome review |
| [Earthquake](spell-reference.md#earthquake) | persistent pulses depend on lifetime and tick scheduling |
| [Echoing Strikes](spell-reference.md#echoing-strikes) | event-driven value depends on event frequency |
| [Eldritch Blast](spell-reference.md#eldritch-blast) | recast or staged execution; repeated plan needs cumulative outcome review |
| [Electrocute](spell-reference.md#electrocute) | repeated plan needs cumulative outcome review |
| [Evasion](spell-reference.md#evasion) | event-driven value depends on event frequency |
| [Fire Breath](spell-reference.md#fire-breath) | repeated plan needs cumulative outcome review |
| [Firefly Swarm](spell-reference.md#firefly-swarm) | event-driven value depends on event frequency; persistent pulses depend on lifetime and tick scheduling |
| [Flaming Barrage](spell-reference.md#flaming-barrage) | recast or staged execution; repeated plan needs cumulative outcome review |
| [Frost Step](spell-reference.md#frost-step) | persistent pulses depend on lifetime and tick scheduling; utility requires comparison beyond HP |
| [Frostbite](spell-reference.md#frostbite) | event-driven value depends on event frequency |
| [Gluttony](spell-reference.md#gluttony) | event-driven value depends on event frequency |
| [Gravity Fissure](spell-reference.md#gravity-fissure) | persistent pulses depend on lifetime and tick scheduling |
| [Guiding Bolt](spell-reference.md#guiding-bolt) | persistent pulses depend on lifetime and tick scheduling |
| [Healing Circle](spell-reference.md#healing-circle) | persistent pulses depend on lifetime and tick scheduling |
| [Heartstop](spell-reference.md#heartstop) | event-driven value depends on event frequency |
| [Ice Tomb](spell-reference.md#ice-tomb) | event-driven value depends on event frequency; persistent pulses depend on lifetime and tick scheduling |
| [Interposing Earth](spell-reference.md#interposing-earth) | event-driven value depends on event frequency |
| [Invisibility](spell-reference.md#invisibility) | event-driven value depends on event frequency |
| [Magma Bomb](spell-reference.md#magma-bomb) | persistent pulses depend on lifetime and tick scheduling |
| [Oakskin](spell-reference.md#oakskin) | event-driven value depends on event frequency |
| [Cinder Swarm](spell-reference.md#cinder-swarm) | persistent pulses depend on lifetime and tick scheduling |
| [Falling Stars](spell-reference.md#falling-stars) | repeated plan needs cumulative outcome review |
| [Field of Life](spell-reference.md#field-of-life) | persistent pulses depend on lifetime and tick scheduling |
| [Figment](spell-reference.md#figment) | persistent pulses depend on lifetime and tick scheduling |
| [Fire Shield](spell-reference.md#fire-shield) | event-driven value depends on event frequency |
| [Flicker](spell-reference.md#flicker) | event-driven value depends on event frequency; persistent pulses depend on lifetime and tick scheduling |
| [Floating Flame](spell-reference.md#floating-flame) | persistent pulses depend on lifetime and tick scheduling |
| [Gentle Breeze](spell-reference.md#gentle-breeze) | persistent pulses depend on lifetime and tick scheduling |
| [Glass Shield](spell-reference.md#glass-shield) | event-driven value depends on event frequency |
| [Gravity Well](spell-reference.md#gravity-well) | persistent pulses depend on lifetime and tick scheduling |
| [Grease](spell-reference.md#grease) | persistent pulses depend on lifetime and tick scheduling |
| [Invisibility](spell-reference.md#invisibility) | event-driven value depends on event frequency; persistent pulses depend on lifetime and tick scheduling |
| [Mud Pit](spell-reference.md#mud-pit) | persistent pulses depend on lifetime and tick scheduling |
| [Protection](spell-reference.md#protection) | event-driven value depends on event frequency |
| [Regenerate](spell-reference.md#regenerate) | event-driven value depends on event frequency; persistent pulses depend on lifetime and tick scheduling |
| [Resist Energy](spell-reference.md#resist-energy) | event-driven value depends on event frequency |
| [Rust Cloud](spell-reference.md#rust-cloud) | persistent pulses depend on lifetime and tick scheduling |
| [Scatter Scree](spell-reference.md#scatter-scree) | persistent pulses depend on lifetime and tick scheduling |
| [Shape Stone](spell-reference.md#shape-stone) | recast or staged execution |
| [Shield](spell-reference.md#shield) | event-driven value depends on event frequency |
| [Spiritual Armament](spell-reference.md#spiritual-armament) | persistent pulses depend on lifetime and tick scheduling |
| [Summon Animal](spell-reference.md#summon-animal) | recast or staged execution; summon value depends on lifetime and AI |
| [Tangle Vine](spell-reference.md#tangle-vine) | persistent pulses depend on lifetime and tick scheduling |
| [Translocate](spell-reference.md#translocate) | utility requires comparison beyond HP |
| [Planar Sight](spell-reference.md#planar-sight) | persistent pulses depend on lifetime and tick scheduling |
| [Pocket Dimension](spell-reference.md#pocket-dimension) | utility requires comparison beyond HP |
| [Poison Arrow](spell-reference.md#poison-arrow) | persistent pulses depend on lifetime and tick scheduling |
| [Poison Spray](spell-reference.md#poison-spray) | repeated plan needs cumulative outcome review |
| [Portal](spell-reference.md#portal) | recast or staged execution; utility requires comparison beyond HP |
| [Raise Dead](spell-reference.md#raise-dead) | recast or staged execution; summon value depends on lifetime and AI |
| [Ray Of Siphoning](spell-reference.md#ray-of-siphoning) | repeated plan needs cumulative outcome review |
| [Recall](spell-reference.md#recall) | utility requires comparison beyond HP |
| [Root](spell-reference.md#root) | persistent pulses depend on lifetime and tick scheduling |
| [Sculk Tentacles](spell-reference.md#sculk-tentacles) | persistent pulses depend on lifetime and tick scheduling |
| [Shadow Slash](spell-reference.md#shadow-slash) | utility requires comparison beyond HP |
| [Spectral Hammer](spell-reference.md#spectral-hammer) | utility requires comparison beyond HP |
| [Aspect of the Spider](spell-reference.md#aspect-of-the-spider) | event-driven value depends on event frequency |
| [Starfall](spell-reference.md#starfall) | repeated plan needs cumulative outcome review |
| [Summon Ender Chest](spell-reference.md#summon-ender-chest) | utility requires comparison beyond HP |
| [Summon Horse](spell-reference.md#summon-horse) | recast or staged execution; summon value depends on lifetime and AI |
| [Summon Polar Bear](spell-reference.md#summon-polar-bear) | recast or staged execution; summon value depends on lifetime and AI |
| [Summon Swords](spell-reference.md#summon-swords) | recast or staged execution; summon value depends on lifetime and AI |
| [Summon Vex](spell-reference.md#summon-vex) | recast or staged execution; summon value depends on lifetime and AI |
| [Summon Zombie](spell-reference.md#summon-zombie) | summon value depends on lifetime and AI |
| [Sunbeam](spell-reference.md#sunbeam) | repeated plan needs cumulative outcome review |
| [Telekinesis](spell-reference.md#telekinesis) | repeated plan needs cumulative outcome review |
| [Teleport](spell-reference.md#teleport) | utility requires comparison beyond HP |
| [Thunderstorm](spell-reference.md#thunderstorm) | persistent pulses depend on lifetime and tick scheduling |
| [Touch Dig](spell-reference.md#touch-dig) | utility requires comparison beyond HP |
| [Volt Strike](spell-reference.md#volt-strike) | repeated plan needs cumulative outcome review |
| [Wall Of Fire](spell-reference.md#wall-of-fire) | recast or staged execution |
| [Wisp](spell-reference.md#wisp) | persistent pulses depend on lifetime and tick scheduling |

## Refreshing the audit

```bash
python3 tools/audit_spell_balance.py
python3 tools/audit_spell_balance.py --check
```

Check mode verifies rarity coverage, assignment agreement, and report freshness. Passing means baseline parameters and boost responses match the definitions. Comparable gameplay value within a rarity still requires outcome and encounter testing.
