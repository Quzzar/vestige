#!/usr/bin/env python3
"""Explicit native adaptations of the frozen Pathfinder 2e batches; no fallback recipes."""
import argparse
import json
from pathlib import Path
from spell_authoring import (
    action, aim, amp, binding, branch, buff, compare,
    area, damage, each, fact, field, freeze, heal, ignite,
    manifest, mul, near, projectile, ray, repeat, rng, dismissible,
    self_, status, store, target,
)
import spell_visuals as visuals

ROOT = Path(__file__).resolve().parents[1]
CATALOG = json.loads((ROOT / 'tools/pathfinder-spells.json').read_text())
ROWS = {row['id']: row for row in CATALOG['spells']}
POLICY = json.loads((ROOT / 'tools/spell-balance-policy.json').read_text())
RARITIES = {name: rarity for rarity, policy in POLICY['rarities'].items() for name in policy['spells']}
RECIPES, NOTES = {}, {}


def typed_damage(n, kind, **values):
    result = damage(n, **values)
    result['identifiers'] = {'damage_type': 'minecraft:' + kind}
    return result


def undead(*effects, otherwise=()):
    return {'type': 'branch', 'condition': {'type': 'tagged', 'path': 'vestige:target/entity_type', 'tag': 'minecraft:undead'},
            'then': list(effects), 'else': list(otherwise)}


def protection(duration, amount, charges, name, conditions=()):
    return buff(duration, bindings=[binding('pf2/' + name, 'damage_calculating',
                [action('reduce_pending_damage', amount=amount)], duration, charges, conditions)])


def recipe(name, effects, traits, description, adaptation):
    assert name in ROWS and name not in RECIPES, name
    native = 'pf2_' + name
    RECIPES[name] = (effects, traits)
    NOTES[native] = (description, adaptation)
    assert native in POLICY['spells'] and native in RARITIES, f'Missing native policy: {native}'

# Cantrips: paid small native spells, without Pathfinder automatic level heightening.
recipe('electric_arc', [each(target('chain',rng(16),'hostile',True,count=2,jump=4),damage(4))], ['lightning','evocation'],
       'Arc to the aimed creature and at most one nearby enemy, dealing four HP to each.',
       'A visible four-block jump replaces independently selected Pathfinder targets; no Reflex save or heightening.')
recipe('ignition', [ray(16,{'type':'branch','condition':compare('target/distance',3,'less_than_or_equal'),'then':[typed_damage(6,'in_fire')],'else':[typed_damage(4,'in_fire')]},ignite())], ['fire','evocation'],
       'Burn one aimed creature; six direct HP within three blocks, four farther away, plus a short burn.',
       'Melee reach gives a deterministic bonus; normal hits burn rather than using a critical-hit gate.')
recipe('frostbite', [ray(16,typed_damage(4,'freeze'),freeze(),status('weakness',40))], ['ice','evocation'],
       'Chill an aimed creature for four HP and briefly weaken it.',
       'Weakness on hit replaces critical-save physical vulnerability; vanilla freezing replaces cold presentation.')
recipe('caustic_blast', [aim(16,near(1.5,damage(3),status('weakness',40),count=3))], ['acid','evocation'],
       'Splash acid over a small three-target area.',
       'Three direct HP and brief Weakness replace acid dice and critical persistent damage; native damage is magic.')
recipe('divine_lance', [ray(20,damage(5))], ['spirit','holy','evocation'],
       'Strike one creature with a five-HP spiritual lance.',
       'Spirit damage uses native magic; deity sanctification and tabletop immunities are not imported.')
recipe('void_warp', [ray(16,undead(otherwise=[damage(5),status('weakness',40)]))], ['void','necromancy'],
       'Warp living flesh for five HP and a brief weakness; undead are unaffected.',
       'Undead immunity uses the Minecraft undead entity-type tag; no Fortitude save.')
recipe('vitality_lash', [ray(16,undead(damage(7)))], ['life','holy'],
       'A vitality lash deals seven HP to undead and leaves living creatures unharmed.',
       'Minecraft undead membership replaces negative-healing rules; no damage to living targets.')
recipe('gouging_claw', [each(target('melee',rng(3),'hostile',True,count=1,angle=45),damage(6),status('wither',40))], ['polymorph','transmutation'],
       'A short claw sweep wounds one enemy for six HP and a brief lingering injury.',
       'Wither substitutes for persistent bleed; no weapon equipment change or attack roll.')
recipe('telekinetic_projectile', [projectile(damage(6),item='cobblestone',speed=1.8,distance=20)], ['force','motion'],
       'Hurl a six-HP telekinetic stone.',
       'A native visual stone replaces choosing a loose physical object; no inventory consumption or damage-type selection.')
recipe('needle_darts', [projectile(damage(2,max_hits_per_target=3),item='iron_nugget',count=3,targeted=1,distance=20)], ['metal','evocation'],
       'Three needles converge on one aimed creature for at most six direct HP.',
       'Iron-looking delivery replaces held-metal material selection, weaknesses, and critical bleed; no material is consumed.')
recipe('slashing_gust', [each(target('beam',rng(16),'hostile',True,count=2,radius=1),damage(4))], ['air','evocation'],
       'A narrow gust cuts at most two creatures in its path.',
       'A narrow two-target corridor replaces independent attack rolls and one/two-free-hand selection.')
recipe('spout', [aim(16,near(2,damage(3),action('knockback',strength=.25,up=.35),count=3))], ['water','motion'],
       'An aimed water spout damages and lifts up to three creatures.',
       'Always uses the small splash variant; water-surface size changes and critical effects are simplified.')
recipe('scatter_scree', [aim(16,field(60,2,near(2,status('slowness',25),count=3),interval=10,particle='crit')),aim(16,near(2,damage(3),count=3))], ['earth','stone'],
       'An initial three-HP stone burst leaves a three-second slowing patch.',
       'A particle field replaces persistent solid rubble and difficult-terrain blocks; does not change terrain.')
recipe('tangle_vine', [ray(16,manifest('tether',60,radius=.75,health=6,on_tick=[status('slowness',15,2)],interval=5))], ['plant','abjuration'],
       'A breakable vine marker tethers and slows one enemy for three seconds.',
       'A six-HP backing marker replaces escape checks and critical immobilization; no terrain vines.')
recipe('puff_of_poison', [ray(10,damage(3),status('poison',60))], ['poison','evocation'],
       'A close poison puff deals three HP and poisons one creature for three seconds.',
       'Native magic impact plus vanilla Poison replaces poison dice and critical persistent damage; vanilla immunities apply to the status.')
recipe('shield', [protection(60,amp(3),1,'shield')], ['force','abjuration'],
       'A three-second ward reduces the next incoming hit by three HP.',
       'Automatic one-hit reduction substitutes for AC, a chosen Shield Block reaction, and the tabletop recast lockout.')

# Early ranks: outcomes and costs selected for Minecraft rather than copied dice/DCs.
recipe('breathe_fire', [each(target('cone',rng(7),'hostile',False,count=4,angle=35),typed_damage(7,'in_fire'),ignite())], ['fire','evocation'],
       'Breathe a short cone of fire into at most four enemies.',
       'A single native cone replaces saves and damage dice; burning is deterministic on contact.')
recipe('force_barrage', [projectile(damage(3,max_hits_per_target=3),count=3,targeted=1,homing=.25,speed=.9,distance=24)], ['force','evocation'],
       'Three homing force bolts deliver up to nine HP to an aimed enemy.',
       'Fixed three-bolt version replaces one/two/three-action options; native projectiles can miss or be blocked.')
recipe('hydraulic_push', [ray(20,damage(7),action('knockback',strength=1.2,up=.35))], ['water','motion'],
       'A water jet deals seven HP and forcefully pushes one creature.',
       'Native velocity replaces fixed-grid push distance and critical doubling.')
recipe('grim_tendrils', [each(target('beam',rng(14),'hostile',False,count=4,radius=.5),damage(6),status('wither',60))], ['void','necromancy'],
       'A narrow void line damages four creatures at most, leaving brief lingering injuries.',
       'Wither replaces persistent bleed; eligible undead are excluded from void damage.')
# Apply the living-only filter to the complete tendril impact, including its status.
effects, traits = RECIPES['grim_tendrils']
effects[0]['effects'] = [undead(otherwise=effects[0]['effects'])]
recipe('thunderstrike', [ray(24,typed_damage(9,'lightning_bolt'),status('weakness',60))], ['lightning','air'],
       'A thunderbolt deals nine HP and briefly weakens an aimed creature.',
       'One typed lightning impact replaces separate electricity/sonic rolls and critical clumsy/deafened conditions.')
recipe('fear', [ray(16,status('weakness',80,1),status('slowness',60))], ['emotion','mind','illusion'],
       'Frighten one enemy, reducing its melee strength and movement briefly.',
       'Weakness II and Slowness replace frightened values and critical fleeing; native AI does not run a fear state.')
recipe('fleet_step', [self_(status('speed',100,2))], ['motion','transmutation'],
       'Gain Speed III for five seconds.',
       'Vanilla movement speed replaces the fixed tabletop speed bonus.')
recipe('gentle_landing', [self_(status('slow_falling',100))], ['air','abjuration'],
       'Gain five seconds of Slow Falling.',
       'A self buff replaces the falling-target reaction; must be cast before impact.')
recipe('protection', [protection(160,mul(.2,fact('event/damage_amount')),4,'protection')], ['holy','abjuration'],
       'Reduce four incoming hits by twenty percent within eight seconds.',
       'Percentage damage prevention replaces AC and saving-throw bonuses; no alignment filtering.')
recipe('false_vitality', [self_(status('absorption',160))], ['life','necromancy'],
       'Gain four temporary absorption HP for eight seconds.',
       'Vanilla Absorption I replaces tabletop temporary HP; recasts follow vanilla status replacement instead of adding pools.')
recipe('soothe', [self_(heal(7),action('remove_status',{'effect':'minecraft:weakness'}))], ['emotion','life','enchantment'],
       'Heal seven HP and remove Weakness from yourself.',
       'Self healing and removing Weakness replace touch healing and the mental-save bonus.')
recipe('enfeeble', [ray(20,status('weakness',120,1))], ['life','necromancy'],
       'Weaken one creature’s melee attacks for six seconds.',
       'Weakness II replaces save-dependent enfeebled degrees; no damage.')
recipe('resist_energy', [protection(200,mul(.5,fact('event/damage_amount')),6,'resist_fire',[compare('event/fire',True)])], ['fire','abjuration'],
       'Halve six incoming fire hits within ten seconds.',
       'This baseline selects fire; acid/cold/electricity/sonic choices are deferred. Uses Minecraft fire damage tags.')
recipe('floating_flame', [aim(18,field(100,2,near(2,typed_damage(1.5,'in_fire'),count=3),interval=10,particle='flame'))], ['fire','conjuration'],
       'A fixed floating flame pulses fifteen direct HP over five seconds to each lingering enemy.',
       'A stationary field replaces sustained manual movement; no tabletop action-per-round sustain.')
recipe('revealing_light', [aim(20,near(3,action('remove_status',{'effect':'minecraft:invisibility'}),status('glowing',120),status('weakness',40),relationship='any',count=4))], ['light','divination'],
       'Reveal up to four creatures with Glowing and strip their invisibility.',
       'Glowing/short Weakness replace concealed, dazzled and blinded degrees; does not create a true anti-stealth perimeter.')
recipe('spiritual_armament', [ray(20,store('target_anchor'),field(100,1,each(target('stored_target'),damage(1.5)),follow_target=1,interval=10,particle='end_rod'))], ['spirit','holy','conjuration'],
       'A spirit weapon follows one target for ten one-and-a-half-HP strikes.',
       'A target-following field replaces attack rolls, a shaped weapon, and manual sustained attacks.')

# Larger ranks: rare/mythic native effects constrained by delivery, duration and costs.
recipe('fireball', [projectile(near(3,typed_damage(14,'in_fire'),ignite(),count=5),item='fire_charge',speed=1.2,distance=28)], ['fire','evocation'],
       'A fireball bursts for fourteen direct HP and burning against up to five enemies.',
       'Native capped hostile-only blast differs from indiscriminate tabletop area damage; no terrain destruction.')
recipe('lightning_bolt', [each(target('beam',rng(28),'hostile',False,count=5,radius=.5),typed_damage(14,'lightning_bolt'))], ['lightning','evocation'],
       'A narrow lightning line deals fourteen HP to up to five enemies.',
       'Native line and target cap replace Reflex saves and uncapped grid line; no terrain ignition.')
recipe('vampiric_feast', [ray(4,damage(8),branch(compare('last_damage',0,'greater_than'),self_(status('absorption',100))))], ['void','blood','necromancy'],
       'A close draining touch deals eight HP and grants four temporary HP if it causes health loss.',
       'Fixed Absorption I replaces temporary HP equal to half damage; it does not heal the caster. Undead are immune to the harmful drain.')
effects, traits = RECIPES['vampiric_feast']; effects[0]['effects'] = [undead(otherwise=effects[0]['effects'])]
recipe('haste', [self_(status('speed',160,1),status('haste',160,1))], ['time','motion','transmutation'],
       'Gain Speed II and Haste II for eight seconds.',
       'Movement and mining speed replace a restricted extra action; no extra spell casts or attack timer bypass.')
recipe('slow', [ray(20,status('slowness',100,2),status('mining_fatigue',100,1))], ['time','motion','transmutation'],
       'Slow one creature’s movement and block work for five seconds.',
       'Slowness III and Mining Fatigue II replace action loss; casting channels are not automatically interrupted.')
recipe('bind_undead', [ray(16,undead(manifest('status',160),action('control')))], ['death','necromancy','enchantment'],
       'Temporarily control one existing undead mob for eight seconds.',
       'Finite ownership replaces permanent obedience and spoken orders; players and native summons cannot be claimed. Existing AI follows/fights for the caster.')
recipe('field_of_life', [aim(18,field(120,3,near(3,undead(damage(2),otherwise=[heal(1)]),relationship='any',count=6,include_self=1),interval=20,particle='happy_villager'))], ['life','holy'],
       'A six-second life field heals living creatures and hurts undead each second.',
       'Indiscriminate living healing/undead damage is retained; no sustain action, restores HP rather than limbs.')
recipe('regenerate', [buff(160,self_(heal(1.5)),interval=20,bindings=[binding('pf2/regenerate_stop','damage_calculating',[{'type':'end_manifestation'}],160,1,[compare('event/fire',True)])])], ['life','transmutation'],
       'Regenerate twelve HP over eight seconds unless fire damage ends the effect.',
       'Fire permanently ends this cast; source acid suppression, regrowing anatomy and death prevention are simplified.')
fire_block = binding('pf2/fire_shield','damage_calculating',[action('reduce_pending_damage',amount=amp(3)),each(target('event_attacker',rng(4),required=False),typed_damage(2,'in_fire'))],160,3)
cold_resist = binding('pf2/fire_shield_cold','damage_calculating',[action('reduce_pending_damage',amount=mul(.5,fact('event/damage_amount')))],160,4,[compare('event/freezing',True)])
recipe('fire_shield', [buff(160,bindings=[fire_block,cold_resist])], ['fire','abjuration'],
       'An eight-second fire shield blocks three hits and burns nearby attackers, with limited freezing resistance.',
       'Automatic finite reactions replace choosing Shield Block and a physical held shield; only Minecraft freezing-tagged damage counts as cold.')
recipe('weapon_storm', [each(target('cone',rng(9),'hostile',False,count=5,angle=55),action('weapon_damage',amount=amp(10),weapon_fraction=.5,ignore_invulnerability=1))], ['metal','evocation'],
       'A weapon storm sweeps up to five enemies with ten HP plus half the caster’s attack value.',
       'Uses one cone instead of a cone/burst selection; flat additive weapon context replaces dice-size multiplication.')
recipe('spirit_blast', [aim(24,near(3,damage(16),count=5))], ['spirit','evocation'],
       'A spiritual detonation strikes up to five enemies for sixteen HP.',
       'Native magic can strike ordinary living bodies; spirit-only interactions and possession-specific effects are deferred.')
recipe('chain_lightning', [each(target('chain',rng(24),'hostile',True,count=6,jump=5),typed_damage(12,'lightning_bolt'))], ['lightning','evocation'],
       'Lightning jumps through up to six visible enemies for twelve HP each.',
       'A six-victim no-repeat chain replaces save-based chain continuation; each jump requires visibility.')
recipe('eclipse_burst', [aim(28,near(4,typed_damage(20,'freeze'),status('blindness',60),status('weakness',100),count=6))], ['ice','void','evocation'],
       'An eclipse blast deals twenty HP and briefly blinds and weakens up to six enemies.',
       'One cold impact replaces cold plus void rolls; undead take the cold component, with no permanent blindness or darkness terrain.')
recipe('arctic_rift', [each(target('beam',rng(32),'hostile',False,count=6,radius=1.2),typed_damage(22,'freeze'),freeze(),status('slowness',100,2))], ['ice','evocation'],
       'A wide cold rift deals twenty-two HP and heavily slows at most six enemies.',
       'Freezing/Slowness replace enfeebling, slowed actions and exact line geometry; no physical terrain rift.')
recipe('falling_stars', [repeat(6,8,projectile(near(2,typed_damage(5,'in_fire',max_hits_per_target=6),ignite(),count=5),count=2,rain=1,distance=28,item='fire_charge'))], ['fire','earth','evocation'],
       'Channel twelve falling stars; each creature can take at most six five-HP impacts.',
       'Rain projectiles replace four independently located elemental bursts; shared hit caps prevent overlap multiplication.')
recipe('cataclysm', [aim(28,near(5,typed_damage(8,'lightning_bolt'),count=6),{'type':'delay','ticks':20},near(5,typed_damage(8,'in_fire'),ignite(),count=6),{'type':'delay','ticks':20},near(5,typed_damage(8,'freeze'),status('slowness',80),count=6),{'type':'delay','ticks':20},near(5,damage(8),action('launch',strength=.3,up=.7),count=6))], ['earth','fire','ice','lightning','evocation'],
       'Channel four elemental surges into one fixed area for up to thirty-two direct HP per creature.',
       'Four explicit native phases replace six simultaneous damage categories, massive terrain effects and flying-creature exceptions.')


# Second batch: reviewed utility, terrain, stealth and elemental behavior.
glass_block = binding('pf2/glass_shield', 'damage_calculating',
    [action('reduce_pending_damage', amount=amp(3)),
     each(target('event_attacker', rng(3), required=False), damage(3), visual=visuals.beam('d0f5ff', 6))], 60, 1)
recipe('glass_shield', [manifest('status',60,target('self'),bindings=[glass_block],visual=visuals.ward(60,'d0f5ff'))],
       ['glass','force','abjuration'], 'A three-second glass ward blocks three HP once and lashes a nearby attacker for three HP.',
       'Automatic mitigation and magic retaliation replace raising/blocking a glass shield, saves, and critical shards; terrain is unchanged.')
recipe('translocate', [aim(20,action('teleport',grounded=1),self_({'type':'visual','visual':visuals.field(10,.9,'bba4ff')}))],
       ['space','teleportation','conjuration'], 'Teleport to a visible dry, supported empty position within twenty blocks.',
       'Only local visible travel; loaded-space and collision validation can reject a paid cast. No distant memory destination or dimension transfer; arrival plays after success.')
recipe('figment', [aim(12,field(80,.6,action('aggro_decoy',radius=6),interval=5,health=1,particles=0,
       visual=visuals.visual(80,.6,visuals.layer('ring','caabff',.7),visuals.layer('sparks','caabff',.8),height=.6,sound='block.amethyst_block.chime')))],
       ['mind','illusion','sonic'], 'A stationary four-second light-and-sound figment draws nearby hostile mobs toward its one-hit marker.',
       'A fixed wisp/lure replaces arbitrary images or sounds, skill checks and per-viewer disbelief. It deals no damage and can be struck to end it.')
recipe('illusory_creature', [aim(12,manifest('decoy',120,ids={'entity':'minecraft:wolf'},count=1,health=1,attack_damage=0,inert=1,
       visual=visuals.field(120,.65,'cbb4ff')))], ['mind','illusion'],
       'A stationary one-hit wolf-shaped decoy distracts hostile mobs for at most six seconds.',
       'Uses a silent inert vanilla wolf silhouette with no attacks; movement, belief damage and per-observer disbelief are omitted. The proxy is targetable, not a fully nonphysical image.')
invisibility_release = binding('pf2/invisibility_release','damage_dealt',[{'type':'end_manifestation'}],160,1)
recipe('invisibility', [self_({'type':'visual','visual':visuals.field(6,.7,'cbb4ff')}),
       buff(160,self_(status('invisibility',2)),interval=1,particles=0,bindings=[invisibility_release],
            on_end=[self_({'type':'visual','visual':visuals.field(6,.7,'cbb4ff')})])], ['shadow','illusion'],
       'Stay invisible for eight seconds; dealing committed damage ends this cast and its two-tick refresh.',
       'Uses vanilla invisibility: armor/held items remain visible. Only committed damage ends the buff; failed swings and other hostile actions are not detected. External longer invisibility is not removed.')
recipe('grease', [aim(14,field(80,2,near(2,status('slowness',15,1),action('knockback',strength=.08,up=0),count=3),interval=10,
       particles=0,visual=visuals.field(80,area(2),'c5cc79')))], ['oil','earth','conjuration'],
       'A four-second patch slows and nudges up to three hostile creatures each half-second.',
       'Slowness and small caster-relative impulses approximate slippery footing. No prone state, real friction changes, object greasing or permanent blocks.')
recipe('mud_pit', [aim(16,field(120,2.5,near(2.5,status('slowness',15,2),count=4),interval=10,
       particles=0,visual=visuals.field(120,area(2.5),'9b8065')))], ['earth','water','conjuration'],
       'A six-second mud patch strongly slows at most four hostile creatures, fading shortly after they leave.',
       'Slowness III approximates difficult terrain; existing blocks and actual friction are unchanged. A destructible marker owns the finite field.')
recipe('heal', [each(target('entity_ray',rng(16),'any'),undead(damage(8),otherwise=[heal(8)]),visual=visuals.beam('a8ffc9'))], ['life','holy','necromancy'],
       'A ranged vitality pulse heals one living creature for eight HP or harms undead for eight HP.',
       'One ranged form only; no action-count variants, area form, self fallback, saves or automatic heightening. Living enemies can be healed.')
recipe('harm', [each(target('entity_ray',rng(16),'any'),undead(heal(8),otherwise=[damage(8)]),visual=visuals.beam('c895ff'))], ['void','death','necromancy'],
       'A ranged void pulse heals undead for eight HP or harms one living creature for eight HP.',
       'One ranged form only; eligibility uses the undead tag rather than a tabletop negative-healing rule. No area/touch variants or automatic heightening.')
recipe('detect_magic', [self_(action('detect_magic',radius=rng(16)),{'type':'visual','visual':visuals.field(12,rng(16),'94caff')})],
       ['mind','divination'], 'Privately report whether native manifestations or equipped enchanted items are within sixteen blocks.',
       'Presence only, from loaded native state/equipped items. It does not inspect other mods, inventories or blocks, reveal hidden locations, or unlock discovery/progression.')
recipe('read_aura', [self_(action('inspect_item'),{'type':'visual','visual':visuals.field(10,.45,'a6d8ff')})],
       ['mind','divination'], 'After two seconds of examination, privately report whether the currently held object is enchanted.',
       'Main-hand item at completion is the inspection target; only vanilla enchantment presence is recognized. No full item identification, stored discovery or observer-wide result.')
recipe('rust_cloud', [aim(18,field(100,3,near(3,damage(2),status('blindness',25),count=4),interval=20,
       particles=0,visual=visuals.visual(100,area(3),visuals.layer('ring','b77845',.6),visuals.layer('smoke','777777',.8))))],
       ['metal','air','evocation'], 'A five-second abrasive cloud deals at most ten direct HP per creature and briefly obscures four hostile occupants.',
       'Blindness approximates obscured sight. Metal-specific expansion/weakness and item rust are omitted; no durability or terrain changes.')
recipe('cinder_swarm', [ray(18,store('target_anchor'),field(100,1.2,
       each(target('stored_target',required=False),typed_damage(2,'in_fire'),status('blindness',25)),
       interval=20,follow_target=1,health=8,particles=0,visual=visuals.field(100,area(1.2),'ffac53',True)))],
       ['fire','conjuration'], 'A following firefly swarm burns its captured creature for ten direct HP over five seconds and briefly blinds it.',
       'Only the firefly-inspired variant, with a breakable marker and fixed five pulses. The ant variant, insect models, saves and forced movement are omitted; nearby bystanders take no pulse damage.')
recipe('summon_animal', dismissible(manifest('summon',400,target('self'),{'entity':'minecraft:wolf'},count=1,health=16,attack_damage=3,
       visual=visuals.field(400,.8,'a0d6a8'))), ['life','conjuration'],
       'Summon one sixteen-HP allied wolf for twenty seconds; recasting dismisses it.',
       'A single curated vanilla wolf uses native ownership/follow/combat AI; no tabletop animal roster, source creature stats or automatic rank heightening.')
flicker_block = binding('pf2/flicker','damage_calculating',
       [action('reduce_pending_damage',amount=mul(.25,fact('event/damage_amount')))],120,4,[compare('event/magical',False)])
recipe('flicker', [buff(120,self_(action('random_teleport',radius=3),
       branch(compare('last_teleport',1),{'type':'visual','visual':visuals.field(8,.8,'bdb1ff')})),
       interval=40,particles=0,bindings=[flicker_block])], ['space','teleportation','abjuration'],
       'For six seconds, reduce four nonmagical hits by a quarter and attempt a short safe random teleport every two seconds.',
       'Native magical damage bypasses the finite mitigation, a broader exception than source force. Unsafe random destinations skip that pulse; no phased entity state or unseen geometry.')
recipe('gentle_breeze', [aim(14,field(120,3,near(3,undead(otherwise=[action('dwell_heal',amount=amp(6),required_ticks=60,interval=5)]),
       relationship='any',count=4,include_self=1,exclude_origin=1),interval=5,particles=0,
       visual=visuals.field(120,area(3),'b1efcf')))], ['air','life','conjuration'],
       'A six-second restorative field heals each living visitor six HP once after three seconds of uninterrupted sampled occupancy.',
       'A missing five-tick sample resets dwell; reentry cannot earn a second heal in the same cast. Undead are excluded; medical skill, affliction saves and temperature systems are omitted.')

# Explicit cosmetic composition for existing native gameplay recipes.
# These attachments do not change outcomes, trait units, costs or targeting.
RECIPES['electric_arc'][0][0]['visual'] = visuals.arc(10,'d7baff',.065)
RECIPES['chain_lightning'][0][0]['visual'] = visuals.arc(24,'4bafff',.19)
RECIPES['hydraulic_push'][0][0]['visual'] = visuals.water_jet()
for name, color in (('divine_lance', 'fff1b0'), ('vitality_lash', 'a8ffc9')):
    RECIPES[name][0][0]['visual'] = visuals.beam(color)
for name, duration, color in (('shield',60,'b9a0ff'), ('protection',160,'ffe6a4'),
                              ('resist_energy',200,'ff9654'), ('fire_shield',160,'ffbc70')):
    RECIPES[name][0][0]['manifestation']['visual'] = visuals.ward(duration, color)
RECIPES['shield'][0][0]['manifestation']['visual'] = visuals.shield()
for name, color, fire in (('fireball','ff9d35',True), ('force_barrage','b9a0ff',False),
                          ('telekinetic_projectile','c5d9ee',False)):
    RECIPES[name][0][0]['manifestation']['visual'] = visuals.orb(color, fire)
RECIPES['fireball'][0][0]['manifestation']['on_hit'].insert(0, {'type':'visual','visual':visuals.blast(area(3))})
for name, color, fire in (('scatter_scree','b5a28a',False),
                          ('floating_flame','ff9544',True),
                          ('field_of_life','98ffb7',False)):
    # The authored fields are nested inside the aimed-position selection.
    effects = RECIPES[name][0]
    def attach_field(entries):
        for entry in entries:
            if entry['type'] == 'create_manifestation' and entry['manifestation']['kind'] == 'vestige:area':
                manifestation = entry['manifestation']
                manifestation['visual'] = visuals.field(manifestation['duration'], manifestation['values']['radius'], color, fire)
            elif entry['type'] == 'for_each': attach_field(entry['effects'])
    attach_field(effects)


from pathfinder_diverse_recipes import register, MODES
from spell_presentation import decorate
register(recipe)

def definitions():
    assert len(ROWS) == 100 and set(RECIPES) == set(ROWS), 'Every frozen source needs exactly one explicit recipe'
    policy = json.loads((ROOT / 'tools/spell-balance-policy.json').read_text())
    rarity_map = {name: rarity for rarity, data in policy['rarities'].items() for name in data['spells']}
    result = {}
    for name, row in ROWS.items():
        native = 'pf2_' + name
        assert rarity_map[native] == RARITIES[native], native
        tuning = policy['spells'][native]
        costs = [{'type':'mana','amount':tuning['mana']}]
        if tuning['cooldown_ticks']: costs.append({'type':'cooldown','ticks':tuning['cooldown_ticks']})
        if tuning['charge_ticks']: costs.insert(0,{'type':'time','ticks':tuning['charge_ticks']})
        effects, traits = RECIPES[name]
        result[native] = {'source': {'spell':f'pathfinder2e:aon/{row["aon_id"]}', 'revision':row['revision'], 'name':row['display_name'],
                                    'reference': {'system':'pathfinder_second_edition','edition':row['edition'],
                                                  'publication':f'{row["publication"]} p. {row["page"]}', 'rank':row['rank'],
                                                  'cantrip':row['cantrip'],'rarity':row['rarity'],'url':row['url']}},
                          'rarity':RARITIES[native], 'traditions':row['traditions'],
                          'traits':{**{'vestige:'+trait:1 for trait in traits},'vestige:amplify':1,'vestige:range':1,'vestige:area':1},
                          'costs':costs,'triggers':[{'id':'vestige:primary','event':'vestige:interact'}], 'effects':effects}
        if name in MODES:
            result[native]['modes'] = [{'id':'vestige:'+mode,'costs':costs,'effects':plan} for mode,plan in MODES[name].items()]
    return {name: decorate(spell,name) for name, spell in result.items()}


def ledger():
    lines = ['# Pathfinder 2e spell conversion ledger','',
             '**100 native adaptations** across the first 48-spell batch, a 16-spell expansion and 36 utility conversions, added October 1, 2026. The complete Vestige catalog now has **214 spells**: 110 Iron adaptations, 100 Pathfinder adaptations and four native examples.', '',
             'Source rules were verified against [Archives of Nethys](https://2e.aonprd.com/Spells.aspx). Exact page IDs, publications, ranks, source rarity and the applied errata snapshot are frozen in [pathfinder-spells.json](../../tools/pathfinder-spells.json). See the [first-batch research](../research/pathfinder-spell-batch.md), [expansion research](../research/pathfinder-expansion-batch.md), [diverse-batch research](../research/pathfinder-diverse-batch.md) and [credits](../../CREDITS.md).', '',
             'These are Minecraft adaptations with original native plans and balance coefficients. They do not execute a tabletop rules engine: no d20 saves, spell slots, automatic heightening, three-action economy, sanctification, critical degrees, or copied text/assets. Source-common does not imply native-common. Source ranks and rarity do not multiply traits or costs. Native mana and charge are authored independently; ordinary spell cooldowns were removed on October 7, 2026 in [the balance policy](../../tools/spell-balance-policy.json). All descriptive traits use relative unit 1; only explicitly read scaling traits affect the plans.', '',
             'New IDs use `vestige:pf2_<source_name>` so names such as Shield, Fireball, Chain Lightning and Slow coexist with existing adaptations. The Pathfinder Shield is a one-hit personal ward; Iron Shield remains a stationary destructible barrier. No existing definition is replaced. Wands, discovery and progression remain deferred.', '',
             'Regenerate with `python3 tools/convert_pathfinder_spells.py`; `--check` verifies both definitions and this ledger. Shared authoring helpers live in `tools/spell_authoring.py`; recipes are explicit, with no generic fallback. Development casting uses `/vestige_magic cast vestige:pf2_<name>` or paid `/vestige_magic cast_balanced vestige:pf2_<name>` after setting test mana.', '',
             '| Native spell / source | Source rank | Native rarity | Native behavior | Adaptation differences |','|---|---|---|---|---|']
    for name, row in ROWS.items():
        native = 'pf2_' + name; description, note = NOTES[native]
        rank = ('cantrip ' if row['cantrip'] else '') + str(row['rank'])
        lines.append(f'| [`{native}` — {row["display_name"]}]({row["url"]}) | {rank} | {RARITIES[native]} | {description} | {note} |')
    lines += ['', 'See the [complete spell reference](../spell-reference.md), [outcome/cost review](../spell-balance-review.md), [formula audit](../spell-balance-audit.md) and [actual verification results](../development-status.md).', '']
    return '\n'.join(lines)


def write(check=False):
    files = {ROOT / 'src/main/resources/data/vestige/runtime_spells' / (name + '.json'):json.dumps(spell,indent=2,ensure_ascii=False)+'\n' for name, spell in definitions().items()}
    files[ROOT/'docs/design/pathfinder-spell-conversions.md'] = ledger()
    for path, text in files.items():
        if check:
            assert path.exists() and path.read_text() == text, f'Stale Pathfinder conversion: {path}'
        else: path.write_text(text)
    print(('Verified' if check else 'Wrote') + f' {len(ROWS)} Pathfinder adaptations and conversion ledger.')

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__); parser.add_argument('--check',action='store_true')
    write(parser.parse_args().check)
