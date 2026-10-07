# Wand tip ideas and combinations

**Initial eight-tip direction locked, October 6, 2026; implementation authorized.** Amethyst/Resonating, Diamond/Refracting, Emerald/Reclaiming, Ender Pearl/Elusive, Copper/Conductive, Iron/Repelling, Ghast Tear/Renewing and Netherite/Steadfast are the selected starting direction. Preserve broad compatibility and a meaningful tradeoff for each. The coefficients below are starting tuning values. The native implementation now covers these eight tips, base/thread profiles and both binding forms, with executable capability coverage and world tests. This catalogue retains twelve other standalone materials and eight optional Iron directions as later candidates.

**Reconfirmed October 7, 2026:** the owner explicitly locked these eight effect directions and requested publication. Quartz, Redstone, Nautilus Shell and Magma Cream remain deferred, along with the other specialist candidates. Broad casting and common damage/healing/protection capabilities define eligibility; each tip must have a meaningful tradeoff, and utility spells must have several useful choices. The coefficients remain initial playtest tuning. The implementation and verification section below records actual behavior separately from later candidates.

The [component palette](wand-components.md) contains the seven base profiles and five thread profiles. The [wand direction](wand-crafting.md) records accepted ingredients, exact-scroll matching, a sixty-second cooldown and initial twenty durability. The full proposal therefore covers seven bases, five threads and twenty-eight candidate tips. A wand has one base, one thread and at most one tip; it cannot equip several tips at once.

## Component roles and metrics

| Component | Main contribution | Useful adjustment channels |
| --- | --- | --- |
| Source scroll | The spell being bound, including its existing shaping | Existing traits, outcomes, geometry modifiers and typed costs, preserved once |
| Base | Affinity and the body's wear capacity | Conditional trait scaling and maximum durability |
| Thread | How the wand conducts and pays for magic | Mana, preparation, maximum durability, wear and compatible riders |
| Tip | Another property beyond the source's existing magic | Secondary outcomes, movement, delivery, protection, resource recovery and explicit trait scaling |

Twenty maximum durability and sixty seconds between uses remain the accepted starting constraints. A preparation reduction does not shorten that minute. Maximum durability, durability spent per cast, mana, health, hunger, material costs, charge time and finite effect lifetime are separate channels. Increasing one must not silently change the others.

Affinity checks the bound scroll's resolved traits before equipment contributions. A Copper tip cannot seed Lightning and thereby make a Lightning Rod qualify for its own affinity bonus. Source Spellshaping may already supply a relevant trait; equipment must not create a feedback loop that repeatedly qualifies and boosts itself.

## Accepted breadth requirement

Initial tips must apply across whole spell capabilities or casting itself. Some can work with every cast, while others serve broad damage, healing or protection families. A damage tip must not require Fireball specifically, a Fire school label or one projectile implementation. Derive eligibility from actual executable plans and shared primary-outcome events, including callbacks and bindings. Use explicit trait consumers and generic composed effects rather than a separate version of each spell.

Utility spells must have several meaningful initial tip choices too. A tip does not have to work with literally every spell, but the untipped option alone is not sufficient coverage for utility. Do not invent an unrelated per-spell fallback merely to claim universal compatibility. A common caster-side effect or the same bounded damage/healing operation can supply coherent breadth.

Before shipping the selected palette, audit actual compatible spells and delivery forms across the full native catalogue, including pure utility and already shaped sources. Flag tips that cover only a few entries or one unusual delivery. The family descriptions below are design targets, not measured implementation coverage. Water-drag, teleport-blocking, delayed-field and follow-up-mark mechanics remain specialist candidates outside the initial eight.

## Accepted tradeoff requirement

The owner supports proceeding with this direction provided tips remain broadly reusable and each has its own meaningful tradeoff. A tip is optional; the untipped wand preserves its source magic without a tip-specific surcharge. The following prices are provisional starting values, not accepted final balance.

| Preferred tip | Proposed price beyond the untipped wand |
| --- | --- |
| Amethyst / Resonating | Mana ×1.15 |
| Diamond / Refracting | Mana ×1.20 |
| Emerald / Reclaiming | One extra wear per committed cast, exchanging useful life for a conditional mana rebate |
| Ender Pearl / Elusive | Mana ×1.20 |
| Copper / Conductive | Mana ×1.15 |
| Iron / Repelling | Mana ×1.08 |
| Ghast Tear / Renewing | Mana ×1.15 |
| Netherite / Steadfast | Mana ×1.10 and an extra 0.5 seconds of preparation |

Apply prices through the existing typed payment machinery, with deterministic whole wear and final payment rounding. Mana-only percentages must not become free benefits on a zero-mana source: such tips need an explicitly authored small flat mana fee or another meaningful price. Set that generic fee policy during tuning rather than creating named-spell exceptions or excluding utility wholesale. Emerald requires actually paid mana for a rebate; an all-nonmana cast cannot create mana from nothing.

The sixty-second reuse constraint remains separate from preparation. Source shaping and core effects compose with these prices once. The goal is several useful choices rather than one tip that dominates every spell; the current draft numbers still require compatibility audits and actual gameplay balancing.

## Alignment with the pinned Iron materials

The proposed tip catalogue mixes close material associations with independently invented Vestige effects. Broad spell compatibility does not by itself establish Iron alignment. The [complete pinned material reference](../research/iron-material-uses.md) and [Jewelry channel reference](../research/irons-jewelry-materials.md) support the following distinctions. Their attributes, actions, available status parameters and finished-item recipes remain separate facts; none automatically grants all associated properties to a wand.

The owner supports proceeding with the existing broad effects, subject to meaningful tradeoffs. The source audit does not supersede the current candidate palette or require replacing it with literal Iron stat selections. The [accepted material direction](material-crafting.md#accepted-native-material-direction) uses Iron's material meanings as ingredient inspiration while independently authoring native contributions. Keep the existing effect ideas as the preferred proposal, describe original interpretations honestly and resolve any loose material association during selection. This direction does not approve exact coefficients or finalize implementation specifications.

| Proposed tip | Pinned Iron facts | Alignment of the current proposal |
| --- | --- | --- |
| Copper / Conductive | Lightning action; Attack Speed; Haste parameter; Recovery Ring and Energized Core ingredients | Close association. Additional lightning follows a real material action; native trigger, costs and budgets are independently authored. |
| Iron / Repelling | Knockback action; Armor; Resistance parameter | Close association. Repulsion follows the action; extending its target selection to support spells is Vestige's design. |
| Ender Pearl / Elusive | Ender focus/rune, Portal Frame and Evasion Elixir ingredient | Strong thematic association. A short backstep interprets the spatial/evasion uses; it is not an existing Iron wand behavior. |
| Diamond / Refracting | Magic-damage action; Armor; Resistance parameter; Mana Ring construction | Partial association. Extra magical damage fits; splash distribution and healing distribution are new Vestige behaviors. A brief protective ward would follow its defensive channels more closely. |
| Amethyst / Resonating | Armor Pierce, Thorns, Regeneration parameter; healing/mana brewing; cast-time ring and mana-regeneration charm | The material supports magic/recovery associations, but the delayed echo is an original Vestige idea. The Resonance Charm name does not establish repeated spell outcomes. Regeneration or a deliberate recovery profile would have closer precedent. |
| Emerald / Reclaiming | Movement Speed, Speed parameter, Emerald-generation action, Evocation focus and currency | The mana rebate is an original Vestige idea. Emerald generation is not mana recovery. A brief movement-speed benefit would have closer precedent. |
| Netherite / Steadfast | Attack Damage, wearer Strength action/parameter and Battlemage equipment construction | Casting knockback resistance is a loose interpretation. Temporary Strength or an explicitly authored offensive contribution would have closer precedent. |
| Ghast Tear / Renewing | No Ghast Tear entry in the pinned ingredient-use inventory or Jewelry material catalogue | Native/vanilla-inspired recovery, without a demonstrated Iron material precedent in those catalogues. This is not a claim about every Ghast Tear occurrence anywhere in Iron's code. |

A closer initial palette could use **Amethyst/recovery, Diamond/warding, Emerald/movement speed, Ender Pearl/spatial evasion, Copper/lightning, Iron/repulsion, Gold/fire protection and Netherite/Strength**. Gold has a Fire Resistance parameter and ignition action. This is an alternative proposal, not an accepted remapping or a change to the twenty-material candidate list. Its broad caster-side effects would still be additional to the scroll's existing shaping.

The current eight are independently authored Vestige effects with different degrees of Iron material precedent. The closer stat palette above is a comparison alternative, not a required correction. Source correspondence is one selection consideration alongside coherent material flavor, broad spell coverage and distinct additional behavior; it does not require importing Iron's quality ladder, exact numerical values or complete equipment system.

## Initial base profiles

All seven bodies can hold every spell. An affinity is a bonus when it matches, not an eligibility restriction. The values below are starting playtest suggestions.

| Base | Matching source traits | Proposed benefit | Proposed maximum durability |
| --- | --- | --- | --- |
| Stick | Neutral | Broad use without an affinity bonus | 24 |
| Bamboo | Plant or Wood | One Amplify ×1.20 contribution on a match | 18 |
| Bone | Death or Necromancy | One Amplify ×1.20 contribution on a match | 20 |
| Blaze Rod | Fire | One Amplify ×1.20 contribution on a match | 20 |
| Breeze Rod | Air or Motion | One Amplify ×1.20 contribution on a match | 18 |
| End Rod | Ender or Space | One Amplify ×1.20 contribution on a match | 20 |
| Lightning Rod | Lightning | One Amplify ×1.20 contribution on a match | 22 |

An either/or match contributes once. Effects must actually read Amplify for this proposal to change their outcomes. A different affinity consumer could adjust another relevant trait where the spell explicitly reads it; multiplying a descriptive Death rating does not independently increase damage. Rarity, material price and Iron's upstream quality values do not establish a universal power multiplier.

## Initial thread profiles

The five names, selector blocks and common **String + Amethyst Shard + Honeycomb** recipe are accepted. The untipped foundation implements the base/thread profiles as starting tuning; tip effects remain implementation targets. The retained selector belongs to the String's own Plinth; a finished thread is an ordinary interchangeable component.

| Thread and selector | Casting identity | Proposed benefit | Proposed tradeoff |
| --- | --- | --- | --- |
| Ensorcelled / Diamond Block | Economical | Mana payment ×0.85 | Add 0.5 seconds of preparation |
| Callous / Iron Block | Durable | Add 8 maximum durability | Add 1 second of preparation |
| Smoldering / Gold Block | Aggressive | A compatible Kindled ignition contribution | Mana ×1.12 and one extra wear per cast |
| Laced / Emerald Block | Quick | Preparation ×0.85 | Mana ×1.12; needs reducible preparation |
| Consecrated / Glowstone | Restorative | A compatible Purifying cleanse contribution | Mana ×1.14; needs eligible healing or protection |

The foundation rejects cores overlapping source ignition or cleanse augments, rather than consuming materials for a duplicate rider. Native ignition also rejects Smoldering. Consecrated supports both eligible healing and protection with shared contact limits. Callous supplies a broadly usable choice even when a spell has no mana cost or no elemental affinity.

## Locked first eight tips

These eight offer distinct decisions across offense, support, mobility and resource use. Emerald, Ender Pearl, Ghast Tear and Netherite provide broad caster-side choices; the other four cover common outcome capabilities. The same material can be represented on every base; thread choice does not change its artwork. No tip remains available when an additional property is unwanted or incompatible.

| Tip material | Suggested adjective | Additional property | Useful spell plans |
| --- | --- | --- | --- |
| Amethyst Shard | Resonating | One weaker delayed damage or healing echo | Positive damage/healing outcomes |
| Diamond | Refracting | Share a small secondary damage/healing pool with nearby eligible recipients | Positive damage/healing outcomes |
| Emerald | Reclaiming | Recover some actually spent mana after a successful primary effect | Mana-bearing damage, healing, protection and utility |
| Ender Pearl | Elusive | A short optional caster backstep after casting | Broad casting and mobility |
| Copper Ingot | Conductive | A bounded extra lightning strike on contact | Living-target damage |
| Iron Ingot | Repelling | Push a struck enemy, or repel nearby enemies from a healing/protection recipient | Damage, healing and protection |
| Ghast Tear | Renewing | Brief gradual recovery for a friendly beneficiary or the caster | Broad casting, including pure utility |
| Netherite Ingot | Steadfast | Resist displacement during the wand's preparation | Broad casting; the tip supplies a short preparation window |

### Amethyst Shard and Resonating

One qualifying primary damage or healing outcome produces a weaker echo on that same recipient after **0.75 seconds**. Start with **20% of the actual primary amount**, capped at **2 HP total per cast**, and mana ×1.15. A supported finite field could echo one of its real outcome pulses through this same bounded rule; the tip does not simply extend its lifetime.

The echo is a secondary outcome, not another cast of the source spell. It does not repeat block changes, teleports, summons, payments, identification or the source's other riders. Damage requires a still-valid hostile recipient; healing cannot resurrect a dead recipient. Amplify already influencing the primary amount must not be applied a second time merely because that amount is reused. Sonic and Time can be explicit scaling inputs for the additional echo where authored. This needs an equipment echo consumer with finite scheduling and cleanup.

### Diamond and Refracting

A qualifying primary damage or healing outcome sends a small magical burst toward nearby eligible recipients. Damage spreads to hostiles; healing spreads to eligible allies. Start with a **2-block radius**, at most **three secondary recipients**, and a shared pool of **20% of the primary actual damage or healing, capped at 4 HP per cast**. Divide that pool among recipients rather than granting the whole amount to each one. Proposed mana factor: ×1.20.

This adds another contact behavior to an already shaped spell. It does not enlarge the source area, recast its explosion or duplicate its rider budget. Respect line of sight, hostility and ownership; no healing is sent to a hostile merely because it is nearby. Force/Area can drive the damage burst, and Life/Area the healing burst through explicitly authored consumers. Full splash after every field tick is excluded; the first qualifying contact owns the burst. Both channels use the same bounded distribution mechanism rather than named-spell adaptations.

### Emerald and Reclaiming

After one successfully resolved primary effect, recover **10% of the mana this cast actually paid**, with a proposed **5-mana maximum rebate** and **one extra wear per cast**. Support damage, healing, applied protection, detection, movement, created manifestations and world changes through reusable primary-effect result signals. Define success at the effect-primitive level rather than maintaining a named-spell list. A completed detection query can be informative even when it finds nothing; reaching that actual result differs from an activation that never resolves its primary effect.

Refund only the mana component after exchanges, discounts and final rounding. A cast paid entirely through health, hunger or materials has no mana to reclaim. Cap the rebate at mana actually spent and available mana capacity. Misses, fully blocked damage, overhealing and volatile forfeits yield no qualifying-outcome refund unless another actual primary effect succeeds. Several recipients or pulses cannot multiply it. This is an economy choice with faster wear, distinct from Ensorcelled's lower up-front cost. Those shared success consumers must be built before claiming utility compatibility; limiting the initial implementation to HP outcomes would not meet this proposed initial scope.

### Ender Pearl and Elusive

After a committed cast, a crouching caster can make a short **1.5-block horizontal backstep**, away from their facing direction. Start with mana ×1.20. Ordinary standing use still casts the spell; crouching intentionally requests the movement, making it predictable for healing and utility wands too.

The movement uses an explicitly authored Space/Motion effect and a safe destination check. It must not move through a wall, across dimensions or onto an invalid unsupported destination. If the step is blocked, the original spell still resolves and the Pearl can flash briefly; it does not refund a committed spell or move the spell's target. Ownership and movement restrictions remain those of the native movement action. This is a new equipment movement direction, not an automatic Reaching upgrade.

### Copper Ingot and Conductive

A qualifying primary damaging contact adds a small lightning outcome. Start with **1 HP per eligible recipient**, at most **3 HP total per cast**, and mana ×1.15. An explicit Lightning consumer can adjust the secondary strike inside those bounds; seeding its needed trait must not alter base-affinity selection.

The source retains its damage type and other shaping. The strike is additional secondary damage, not a conversion of the whole spell to Lightning. It cannot activate another Shocking or Conductive rider. A source that already has electrical riders needs a shared additional-outcome budget and an explicit interaction rule; the tip is not an automatic degree upgrade.

### Iron Ingot and Repelling

After eligible primary damage, push the struck enemy slightly away from the caster. After positive healing or applied protection, send the same small repulsion outward against eligible hostiles within **2 blocks of the actual friendly recipient**. Start with **0.35 additional impulse strength**, at most **three affected hostiles per cast**, and mana ×1.08. Motion supplies an explicit contribution to the impulse; normal knockback resistance and authored velocity limits still apply.

Direction is away from the caster for the damaging contact or away from the protected/healed recipient for the support pulse. It never knocks the friendly beneficiary away. These are capability-based target selectors for one repulsion effect, not exceptions for specific spells. It does not convert a stationary zone into a moving zone. Existing push effects must share the velocity cap, and a saturated push cannot be sold as another independent full-strength impulse. Iron's pinned knockback channel gives this proposal a clear source flavor.

### Ghast Tear and Renewing

After a successful cast passes native activation and volatile gates, grant a small gradual recovery effect over **4 seconds**. The first successful primary outcome chooses the recovery beneficiary: its friendly healing/protection recipient, otherwise the caster. Later contacts do not redirect or multiply that owned pool. Start with a **single 2 HP recovery pool per cast**, and mana ×1.15. This replaces the earlier healing-only percentage profile. Life and Time can drive an explicit bounded healing plan; the target resolver is shared across all spell plans.

Full health wastes the corresponding recovery rather than banking it indefinitely. The gradual healing is secondary, so it cannot repeatedly trigger Purifying, Renewing or a mana rebate. A damaging or utility spell retains its original targeting and outcomes while the tip separately supports its caster. Several recipients do not create several pools. Ghast Tear is a Vestige regeneration/healing interpretation, not a claim that Iron assigns it this wand effect.

### Netherite Ingot and Steadfast

During the wand's preparation window, reduce incoming knockback by a proposed **50%**, using a temporary owned modifier. Add **0.5 seconds of preparation** and mana ×1.10. The added preparation supplies a real window even for an otherwise instant spell, so eligibility does not depend on a few long-channel spells. Metal/Motion can explicitly drive the resistance calculation. Remove the modifier immediately when preparation finishes, is canceled, loses its owner or closes with the runtime.

This does not prevent silence, invalid targets or explicit cast cancellation. It does not replace a spell's interruption policy or grant permanent armor while merely holding a wand. Netherite's source Strength and equipment roles motivate a firm casting stance, but the exact resistance behavior is independently proposed. It supplies a different function rather than making every cheaper tip weaker by a material-quality ratio.

## October 7 implementation and bounds

`WandTips` derives eligibility from executable primary plans, modes, callbacks and protective bindings. Secondary source riders do not independently qualify an otherwise unsupported tip. `WandTipEffects` owns one cast's finite contributions; damage/heal signals report actual HP changes, protection uses the actual beneficiary, and successful utility primitives report resolution. Damage immunity, overhealing and paid volatile forfeits supply no artificial qualifying outcome. A completed detection query remains an informative utility outcome even if empty.

Extra outcomes use detached shared-runtime secondary tasks with copied cast traits, scalar state and causal lineage. They survive a primary projectile's removal without reserving another player action, cannot feed tip/rider loops and close with the runtime. Delayed echoes recheck life and the current relationship to the caster. Refracting can originate at a killed hostile's final position while selecting living nearby recipients.

Explicit consumers normalize current Sonic/Time/Force/Life/Space/Lightning/Motion/Metal/Area against the original source rating (minimum denominator one), clamped to 0.1–4. Missing inputs are seeded once after affinity selection. These are authored equipment consumers, not inherent new trait semantics. Primary Amplify is already represented in actual HP and is not applied again to echoes/bursts.

Hard bounds are 2 HP/one echo (delay 1–60 ticks); 4 HP/shared refraction over at most three recipients (radius at most four blocks); refund at most five actually paid mana; safe crouching backstep at most two blocks; Conductive at most three HP/three unique hostiles; Repelling at most three hostiles with impulse at most 0.7 and shared horizontal speed at most 1.5; Renewing at most two HP in two delayed pulses; Steadfast at most 75% of remaining knockback susceptibility while preparing. The starting coefficients above remain the normal unboosted profile.

Conductive and an existing Shocking source share a 35 HP electrical pool: the native Shocking maximum is four contacts × eight HP, with at most three additional Conductive HP. Each keeps its original contact/amount cap. Repelling's compiler also caps existing source push actions, including trusted secondary plans. Emerald's extra integer wear adds to the core's wear. Percentage-priced tips on a zero-mana source require at least one mana; Reclaiming requires a positive actual mana channel. No tip duplicates source shaping or changes the one-minute cooldown.

[Verification and the complete compatibility ledger](../verification/native-wand-tips-2026-10-07/README.md) distinguish gameplay checks, client captures and remaining playtest limits. All eight tips are part of this implementation; the following materials are later proposals only.

## Twelve further standalone tips

These broaden the catalogue beyond the suggested initial eight. Narrow situational mechanics remain here rather than defining the initial palette. Several need new reusable consumers; availability is an idea for later selection, not a promise that the current runtime already supports every behavior.

| Tip material | Suggested adjective | Additional property | Starting price |
| --- | --- | --- | --- |
| Lapis Lazuli | Weighing | Briefly slow a damaged recipient | Mana ×1.10 |
| Prismarine Crystals | Buoyant | Brief breathing protection when casting underwater | Mana ×1.05 |
| Prismarine Shard | Skimming | Preserve eligible projectile movement through water | Mana ×1.05 |
| Nautilus Shell | Sheltering | Add one bounded arrow-return charge to protection | Mana ×1.15 |
| Echo Shard | Reverberating | Leave one weaker delayed pulse at a field's location | Mana ×1.25, extra wear +1 |
| Nether Quartz | Inscribing | A short mark that rewards a follow-up native spell | Mana ×1.10 |
| Obsidian | Sealing | Briefly inhibit a damaged recipient's native teleport | Mana ×1.20 |
| Redstone Dust | Accelerating | Faster eligible projectile travel | Mana ×1.10 |
| Glowstone Dust | Illuminating | Briefly outline a successfully affected recipient | Mana ×1.08 |
| Fermented Spider Eye | Withering | Briefly weaken a damaged recipient | Mana ×1.12 |
| Magma Cream | Cauterizing | Add brief fire protection after healing or protection | Mana ×1.12 |
| Flint | Rending | Moving after a hit can trigger one small wound | Mana ×1.15, extra wear +1 |

### Lapis Lazuli and Weighing

Apply **Slowness I for 2 seconds** after primary damage, once per eligible recipient, with a finite cast recipient cap. Motion and Time can explicitly control strength/duration inside authored limits. Stronger existing slowing effects retain their meaning; this is not repeated stacking on every damage tick. Lapis's pinned Slowness action supplies the material association. It changes positioning pressure rather than adding another damage multiplier.

### Prismarine Crystals and Buoyant

Give the caster **5 seconds of Water Breathing** after a committed cast while submerged. This supports utility and combat wands used underwater. Water and Time can drive the explicit status effect. It does not replenish health, grant permanent underwater breathing or refresh from the source's later pulses. This is a native aquatic interpretation; no upstream universal Prismarine wand property is assumed.

### Prismarine Shard and Skimming

For a delivery that actually suffers water drag, reduce that drag while preserving the spell's authored maximum distance, lifetime, collision and hit limits. Start by removing **half the extra water drag**. Water/Motion can be explicit inputs in an eligible projectile consumer. A projectile that already ignores water drag is incompatible with this paid property. This changes delivery in water, not the caster's breathing, distinguishing it from the crystal tip.

### Nautilus Shell and Sheltering

After an eligible protection outcome, add a **3-second halo** with **one extra return charge** against a supported incoming vanilla arrow, spectral arrow or trident. Start with a shared maximum of **three returns per cast** across this tip and existing source protection. Use the existing ownership and direction rules; native and foreign spell projectiles remain excluded unless an adapter is explicitly built.

This can reuse the current Reflecting primitives, but the extra equipment charge is authored separately and does not rename the scroll's degree. A source already saturating the shared return budget would need another tip. It benefits protective recipients rather than granting unrelated casting invulnerability.

### Echo Shard and Reverberating

An eligible spatial damage/healing field leaves one afterimage that pulses **2 seconds later** at its captured location. Start with **15% of a qualifying primary pulse's actual outcome pool**, capped at **4 HP total**, divided among up to eight valid recipients. Preserve the field's targeting constraints and owned cleanup.

This differs from Amethyst's echo on the original recipient: the afterimage belongs to a location and can affect valid occupants there when it resolves. It does not recreate a summon, execute a teleport, extend the whole original field or replay block changes. It needs a reusable spatial afterimage consumer with a captured finite budget. Echo's Eldritch associations are source flavor, not permission to duplicate an arbitrary spell.

### Nether Quartz and Inscribing

A qualifying primary damaging hit leaves a visible mark on that recipient for **6 seconds**. The caster's next qualifying primary native spell hit can consume it for **15% additional damage, capped at 2 HP**. Start with mana ×1.10 for the marking wand. The follow-up may come from a scroll or a different native spell; it does not require waiting through the wand's minute-long cooldown.

A weapon hit or secondary rider cannot consume or refresh the mark. The extra outcome retains secondary lineage and cannot create another mark. Keep at most one mark per caster/recipient pair and a finite per-cast recipient limit. Memory and Time can be explicit inputs for an owned mark definition. This direction needs a reusable mark and next-primary-outcome consumer; it is not an existing universal trait effect. The mark belongs to its caster, so another player's spell cannot spend it accidentally. Its two-cast combat sequence makes it a later candidate rather than a foundation for the broad initial palette.

### Obsidian and Sealing

After qualifying primary damage, inhibit that recipient's **native teleport attempts for 3 seconds**. The owned seal uses explicitly authored Space/Time conditions and must end normally. It needs a reusable native teleport guard; a descriptive Space trait alone cannot enforce the rule.

Ordinary movement and the caster's teleport spells continue to operate. Foreign teleport systems are outside this candidate unless their adapters expose the necessary ownership/guard checks. This is a specialized anti-escape tip, not permission to cancel all movement, server commands or every third-party device. Obsidian's construction use is source precedent; the sealing association is a Vestige idea.

### Redstone Dust and Accelerating

Increase an eligible projectile's travel speed by a proposed **25%**, using Motion in an explicit delivery expression. Keep its maximum distance, lifetime ceiling and per-target hit budget. Reaching already on the scroll still controls its own supported range.

This changes time to contact, not charge time or the wand's sixty-second cooldown. Instant beams and already saturated or unsupported delivery implementations receive no paid change. A delivery consumer must resolve speed/lifetime constraints together so greater speed does not accidentally grant greater range.

### Glowstone Dust and Illuminating

After eligible primary damage, healing or protection, outline that actual living recipient for **5 seconds**. Light and Time can explicitly scale the bounded status. This can help track a struck invisible foe or a protected ally without revealing every creature in the area.

It does not remove Invisibility. A source already guaranteeing a longer outline needs a meaningful authored interaction or another tip; an identical duplicate outline is not a new benefit. The effect remains a visual cue, without chat or instructions.

### Fermented Spider Eye and Withering

Apply **Weakness I for 3 seconds** after qualifying primary damage. Curse/Time can be explicit scaling inputs for the bounded status, once per recipient and within a cast-wide cap. This is a temporary attack penalty, not arbitrary suppression of spells or AI.

Keep it distinct from Lapis's movement slow and Flint's movement-triggered wound. A nondamaging spell requires a different compatible tip. Existing stronger weakness retains its normal precedence; secondary Withering outcomes cannot apply another curse rider.

### Magma Cream and Cauterizing

After eligible positive healing or protection, give the actual recipient **5 seconds of Fire Resistance**. Fire and Time can drive the explicit status effect. Start with mana ×1.12 and a finite recipient cap shared across all source pulses.

It adds support against fire rather than igniting the recipient or granting extra fire damage. It is not a universal removal of every Bleeding or Burn effect. The name is a candidate; if it suggests a broader cleanse than the actual effect, use **Tempering** instead. Both labels describe one material/effect idea, not two tip types.

### Flint and Rending

After a primary damaging contact, observe the recipient for **4 seconds**. Its first **2 blocks of accumulated movement** can trigger one extra wound, starting at **2 HP maximum per cast**. Blood/Motion/Time would be explicit inputs in a finite movement-and-damage plan.

It needs a reusable owned movement observer; it is not inherently provided by those traits. Actual movement may include an authored push, making it a possible synergy with existing source movement effects. One wound consumes the observation, and secondary damage cannot create another observation. This is pressure to remain still, distinct from continuous Bleeding; it does not restart on every tick.

## Optional Iron material directions

These are separate future compatibility candidates. They do not make Iron mods required for baseline wand crafting. Only resolve a material when its installed provider actually supplies it; accepting a common tag alone is not proof that an item exists. Material descriptions below draw on the complete [pinned material reference](../research/iron-material-uses.md) and [Jewelry channel reference](../research/irons-jewelry-materials.md). Native magnitudes and costs remain independently authored.

| Material candidate | Suggested adjective | Additional equipment idea | Source flavor and proposed constraint |
| --- | --- | --- | --- |
| Moonstone | Bursting | A small outward wind burst on one qualifying impact | Source wind burst; finite impulse/recipient budget, no full spell explosion |
| Onyx | Siphoning | Heal the caster for a fraction of positive actual spell damage | Source heals the wearer; start at 10%, cap 2 HP per cast, mana ×1.15 |
| Sapphire | Riming | A short chill after primary damage | Source freezing; brief bounded slow, not guaranteed indefinite immobilization |
| Ruby | Scorching | One stronger, short ignition contribution | Source ignition; shares source/core burn limits, extra wear can pay for added pressure |
| Peridot | Gathering | Pull a struck recipient slightly toward the caster | Source pulls counterpart; preserves knockback resistance, movement limits and ownership |
| Topaz | Pursuing | Pull the caster a short distance toward the struck recipient | Source pulls wearer; safe destination/line checks, no teleport through solids |
| Garnet | Defiant | A short owned damage-resistance window after a committed cast | Source Resistance; finite time and absorption/reduction budget |
| Divine Pearl | Benediction | Restore a little caster health after actual ally healing | Source wearer healing/Fortify; require useful nonself healing, cap once per cast |

Siphoning and Reclaiming are deliberately different: one returns health from actual damage, the other returns paid mana after a qualifying useful outcome. Gathering and Pursuing move different actors. Bursting differs from Refracting by adding movement pressure rather than a damage splash. If Sapphire and Lapis produce indistinguishable slowing in playtests, treat Sapphire as an explicitly authored alternative instead of adding another art/mechanical entry merely because its material exists.

## Combination ideas

The following are examples of selecting a base, thread and one tip around a shaped source scroll. Their labels describe play styles, not additional hidden bonuses or automatic compound recipes.

**Still valid after the lock-in:** combinations using the selected eight retain their intended interactions with source shaping. Quartz, Redstone, Nautilus Shell and Magma Cream combinations in this table remain later-tip examples; they are not in the initial eight. The current untipped foundation supplies their shared binding/payment/component path, but does not yet craft or execute tipped wands.

| Combination | Suitable source | Resulting play style |
| --- | --- | --- |
| Blaze Rod + Ensorcelled + Diamond | Reaching Fireball with Fire present | Stronger matching fire outcomes, economical preparation, extra impact splash |
| Bone + Callous + Amethyst | Death/Necromancy damage with Bleeding | Durable matching magic, retained wounds, one weaker delayed primary echo |
| Breeze Rod + Laced + Ender Pearl | Air/Motion projectile with reducible preparation | Quicker preparation, matching potency and an optional retreat after casting |
| Stick + Callous + Ghast Tear | Healing, including a Purifying scroll | Neutral durable body, retained cleanse, a little subsequent recovery |
| End Rod + Ensorcelled + Quartz | Ender/Space damage | Affinity and lower mana payment, followed by a brief opportunity for another native spell |
| Lightning Rod + Smoldering + Copper | Lightning damage without saturated ignition | Lightning affinity, short ignition and a separately bounded electrical contact |
| Bamboo + Consecrated + Ghast Tear | Plant/Wood healing without saturated cleansing | Matching healing, supported cleanse and gradual recovery |
| Stick + Ensorcelled + Emerald | Mana-bearing spell with a real useful-outcome signal | Lower up-front mana and a conditional rebate, paid for with faster wear |
| Stick + Callous + Netherite | A spell with preparation | More total uses and a firmer casting stance, with extra preparation cost |
| Breeze Rod + Laced + Redstone | An eligible Air/Motion projectile | Less charge time and faster travel, while retaining the minute cooldown |
| Any base + a compatible thread + Nautilus | A protection spell | A protective recipient gains an extra bounded arrow-return charge |
| Any base + a compatible thread + Magma Cream | Healing/protection | Fire-safe support for the actual recipient |

Pure detection, movement and world-changing spells can select Emerald's primary-resolution rebate, the Pearl's optional backstep, Ghast Tear's caster recovery or Netherite's protected preparation. These shared caster effects do not require a damage projectile or a healing recipient. They preserve the source's original action rather than inventing spell-specific replacements.

Some pairings intentionally compete. Repelling creates space; Gathering brings a target closer; Pursuing moves the caster instead. Resonating follows a recipient; Reverberating follows a location. Renewing gives gradual recovery to a friendly beneficiary or the caster; Siphoning specifically returns caster health from actual damage. These decisions are more useful than several different materials all granting a differently sized generic damage bonus.

## Modifier names

Retain the source spell name and italic augment words. Possible names include **Wand of *Reaching, Refracting* Fireball**, **Wand of *Renewing* Healing** and **Wand of *Conductive* Lightning Bolt**. Base and thread names need not be appended to every wand name; the item appearance and casting cues can express those choices.

An explicitly authored combination may have a shorter adjective: **Reaching + Refracting → Prismatic** is a naming idea, for example. Such a label aliases the exact contributions already present; it grants no extra degree, rider or price. It must not collapse item identity or lose the scroll's identification state. Naming combinations are separate from choosing the effect catalogue, and unknown spells must follow the existing concealment rules.

Player-facing feedback remains visual/audio. A small delayed echo, a mark on the recipient, a Pearl flash or mana returning on the bar communicates the behavior. This catalogue does not authorize instructional chat, actionbar explanations or expanded scroll tooltips.

## Composition and balance rules

1. **Preserve the source once.** Three matching scrolls bind one exact stored spell variant. Neither their count nor the binding structure multiplies the source's shaping again.
2. **Compile trusted definitions.** Store the verified source variant and component identities. Resolve the native trait changes, secondary plans and typed payments from trusted packaged equipment definitions; an item cannot supply arbitrary effect graphs.
3. **Check real capability.** Eligibility comes from the executable plan, including callbacks and bindings. Damage, healing, projectile and protection are capabilities, not new descriptive traits. School or rarity alone cannot prove that a tip works.

   **Select for broad coverage.** Initial tips serve casting itself or large capability families. Audit actual catalogue coverage before locking the palette; specialist tips stay optional later work. No spell-ID allowlist substitutes for a reusable consumer.
4. **Use explicit trait consumers.** Add or multiply existing traits where a planned effect reads them; explicitly seed a missing needed trait. No new inherent trait semantics are created. Secondary consumers must not accidentally scale an already scaled primary amount twice.
5. **Preserve causal budgets.** Secondary echoes, burns, bursts and recovery cannot trigger more equipment/Spellshaping riders. Inherit the existing contact/recipient ceilings and add the smaller authored tip-wide limits. One callback, field tick or projectile cannot reset the budget.
6. **Compose prices by channel.** Source, body, thread and tip contributions resolve through the same typed cost system. Round final payments once. Mana discounts do not discount wear, health, hunger or items unless an explicit rule changes that channel. Refunds use the committed amount in the eligible resource channel.
7. **Keep deterministic wear.** Start with one wear per committed cast plus authored whole extra wear. Maximum durability changes the total capacity; wear changes how quickly it is spent. The proposed last use follows ordinary durability breaking rather than leaving an otherwise unusable partial charge; precise commitment still belongs to native implementation design.
8. **Keep the accepted reuse floor.** A cast may require at least the accepted sixty seconds and any longer resolved native recovery. Preparation is separate. Per-player cooldown sharing and inventory-swap prevention remain a separate owner decision.
9. **Own temporary effects.** Marks, resistance, movement observations, pulses and protection have finite recipients, durations and cleanup. Canceled preparation removes preparation-only modifiers. No active equipment effect becomes permanent through saving, disconnecting or rebinding.
10. **Reject structural no-ops.** An incompatible plan or saturated existing effect must not consume construction inputs for a tip that cannot change anything. A conditional tip can still have no payoff on an individual miss, blocked move or full-health recipient; compatibility does not guarantee success on every cast.

After-cast tip behavior also passes the source's native activation and volatile gates. A forfeited spell does not become a free Pearl movement or resource-return activation. Payments, wear and recovery still follow their committed native boundary; preparation-only modifiers clean up even when the cast never produces an outcome.

For example, Ensorcelled ×0.85 and a Refracting tip ×1.20 produce **×1.02 new mana scaling**, before final rounding and in addition to the source's existing supported modifiers. A Blaze Rod Fire match adds its single affinity contribution. The source's Reaching contribution remains intact, and the extra splash uses its own bounded outcome budget.

Stick's proposed 24 maximum durability plus Callous's 8 gives **32**. Ordinary one-wear casts offer 32 uses; an Emerald tip's extra wear makes that 16 committed uses if no other wear changes apply. Adding Smoldering and an extra-wear tip proposes three wear per cast, not a percentage chance of breaking. All these values remain balance proposals.

## Artwork scope and implementation order

| Palette | Base and tip appearances including no tip | Maximum thread combinations before eligibility |
| --- | --- | --- |
| Earlier five-tip shortlist | 7 × 6 = 42 | 42 × 5 = 210 |
| Suggested first eight standalone tips | 7 × 9 = 63 | 63 × 5 = 315 |
| All twenty standalone candidates | 7 × 21 = 147 | 147 × 5 = 735 |
| All twenty-eight candidates including optional providers | 7 × 29 = 203 | 203 × 5 = 1,015 |

These are compositional appearances, not separate images for every spell or thread. Layer a base and tip where practical. A block used as a consumed tip ingredient does not add it to the Plinth socket allowlist or grant the finished wand vanilla placement behavior. New optional materials do not silently become interchangeable with a standalone tip unless their rule explicitly declares that alternative.

Choose the desired tip count and actual effects before final art. Reuse supported bounded damage/healing, movement, status and protection primitives where they express the chosen behavior. Echo scheduling, next-hit marks, mana rebates, specialized drag, native teleport guards and movement wounds need explicit reusable consumers rather than per-spell branches. Verify each selected rule with compatible native spells and persistence/payment/cancellation behavior; inspect the actual client for artwork and cues.

The first implementation remains native wand binding, stored source/component identities, deterministic durability and coherent cooldown/payment commitment. Component behaviors follow after their selection and tuning. The [executable Spellshaping ledger](../spellshaping-recipes.md) continues to contain shipped rules only; none of this proposed equipment catalogue changes those material/socket recipes.
