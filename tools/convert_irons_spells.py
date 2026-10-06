#!/usr/bin/env python3
"""Author native effect graphs for the pinned Iron catalog; no upstream code/assets required.

The checked-in JSON is the playable content. This script is an optional, deterministic
content-authoring tool. Edit these recipes, then run it to regenerate the catalog.
"""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CATALOG = json.loads((ROOT / 'tools/irons-spells.json').read_text())
REVISION = CATALOG['revision']
ROWS = {r['id']: r for r in CATALOG['spells']}
BALANCE_POLICY = json.loads((ROOT / 'tools/spell-balance-policy.json').read_text())
RARITIES = {}
for rarity, policy in BALANCE_POLICY['rarities'].items():
    for name in policy['spells']:
        assert name not in RARITIES, f'Duplicate rarity assignment: {name}'
        RARITIES[name] = rarity
assert set(ROWS) <= set(RARITIES), f'Missing rarity assignments: {set(ROWS) - set(RARITIES)}'
RECIPES = {}
NOTES = {}


from spell_presentation import decorate, without_cosmetics
from spell_authoring import (
    action, add, aim, amp, area, binding, branch,
    buff, compare, cone, damage, dismissible, each, fact,
    field, freeze, heal, ignite, leech, manifest, melee,
    mul, near, projectile, ray, repeat, rng, self_,
    status, store, summon, target, trait, wait, ward,
    weapon,
)

def recipe(name, effects, traits, description, adaptation=''):
    assert name in ROWS and name not in RECIPES, name
    RECIPES[name] = (effects, traits)
    NOTES[name] = (description, adaptation)

# Blood: life transfer, death reactions, and deferred damage.
recipe('acupuncture', [projectile(damage(1.5), leech(), count=5, targeted=1, speed=1, item='iron_nugget')], ['blood','necromancy'], 'Five needles converge on the aimed creature and steal life.')
recipe('blood_needles', [projectile(damage(2), leech(), count=5, spread=30, item='iron_nugget')], ['blood','evocation'], 'A fan of five needles transfers a quarter of damage to the caster.')
recipe('blood_slash', [projectile(damage(7), leech(.15), pierce=2, item='redstone')], ['blood','evocation'], 'A piercing slash steals life from each struck creature.')
recipe('blood_step', [aim(8, action('teleport')), self_(status('invisibility', 40), action('aggro_clear'))], ['blood','teleportation','illusion'], 'Short teleport followed by invisibility and cleared pursuit.', 'Aim-point teleport; entity-behind positioning is available through the teleport action but is not auto-selected.')
bonus = manifest('status', 600, target('self'), on_end=[action('remove_attribute')])
devour_kill = binding('devoured', 'die', [self_(action('grant_max_health', amount=amp(2)), bonus)], 20, 1)
recipe('devour', [ray(5, {'type':'install_binding','target':target(),'binding':devour_kill}, action('pull', strength=.3), damage(6), leech(.15))], ['blood','necromancy','life'], 'A bite pulls and drains its victim; a killing bite grants temporary maximum health.')
heart = buff(120, bindings=[binding('heartstop', 'damage_calculating', [action('defer_pending_damage', amount=fact('event/damage_amount'))], 120)], deferred_damage=0)
heart['manifestation']['on_end'] = [self_(action('damage', amount=mul(.5, fact('deferred_damage')), ignore_invulnerability=1))]
recipe('heartstop', [{'type':'set_value','key':'vestige:deferred_damage','value':0}, heart], ['blood','abjuration','time'], 'Defer damage for six seconds, then suffer half the accumulated amount.')
recipe('raise_dead', dismissible(summon('zombie', 2, 'iron_sword'), summon('skeleton', 1, 'bow')), ['death','necromancy','conjuration'], 'Three owned undead fight for the caster; recast dismisses them.', 'Vanilla zombie/skeleton bodies and AI replace Iron custom undead variants.')
recipe('ray_of_siphoning', [repeat(16,4,each(target('beam',rng(12),'hostile',False,count=1),damage(1),leech(.5)))], ['blood','necromancy'], 'A single-target channeled ray heals half the actual damage dealt.')
recipe('wither_skull', [projectile(near(2,damage(6),status('wither',100)),item='wither_skeleton_skull',speed=.56)], ['death','unholy','evocation'], 'Slow skull projectile inflicts a withering blast.')
recipe('sacrifice', [ray(24, action('explode',amount=add(amp(6),mul(.25,fact('target/health'))),radius=area(3),count=6),action('despawn'),relationship='owned')], ['death','blood','necromancy'], 'Detonate an owned summon, scaling damage with its remaining health.', 'Fixed native blast radius; source health-dependent radius is not reproduced.')

# Ender: recasts, geometry, spatial movement, and causal reactions.
recipe('counterspell', [each(target('any_entity_ray',rng(24)),action('dispel'))], ['ender','abjuration'], 'Interrupt casting, dispel attached native effects, remove effects, or banish summons.')
recipe('dragon_breath', [cone(16,10,damage(1.5))], ['ender','draconic','evocation'], 'Channel a damaging dragon cone.', 'Native pulses replace residual pools; secondary per-victim explosions are removed to prevent density multiplying damage.')
recipe('evasion', [ward(100)], ['ender','abjuration','teleportation'], 'A one-charge dodge protects against the next incoming hit for five seconds.')
recipe('magic_arrow', [projectile(damage(8),item='arrow',speed=3,pierce=2,distance=48)], ['force','evocation'], 'Charged piercing arrow.')
recipe('magic_missile', [projectile(damage(6),homing=.2,speed=.9,distance=48)], ['force','evocation'], 'Homing missile steers toward the aimed creature.')
recipe('starfall', [repeat(10,8,projectile(near(2,damage(4,max_hits_per_target=10)),count=2,rain=1,distance=32,item='fire_charge'))], ['ender','evocation'], 'Channel twenty exploding meteors; each creature can take at most ten impacts.')
recipe('teleport', [aim(8,action('teleport'))], ['space','teleportation'], 'Collision-checked short-range teleport.')
recipe('summon_ender_chest', [action('ender_inventory')], ['ender','space','conjuration'], 'Open the caster’s vanilla ender chest inventory.')
recipe('recall', [action('recall')], ['space','teleportation'], 'Return to a valid respawn location or world spawn.')
recipe('portal', [aim(48,store('first_anchor')),wait(2400),aim(48,manifest('portal',1200))], ['ender','space','teleportation'], 'Two casts place linked, bidirectional teleport endpoints.', 'Native particle endpoints replace portal models and oriented portal frames; duration is sixty seconds.')
recipe('echoing_strikes', [buff(200,bindings=[binding('echo', 'damage_dealt', [each(target('event_target'),action('damage',amount=mul(.5,fact('event/damage_amount'),trait('amplify')),ignore_invulnerability=1))],200,3,[compare('event/magical',False)])])], ['ender','evocation','time'], 'Three ordinary attacks within ten seconds produce an extra hit worth 50% of their damage.')
recipe('black_hole', [aim(18,field(160,5,near(5,action('pull',strength=.25),damage(2)),particle='portal',interval=10))], ['ender','void','evocation'], 'A fixed singularity pulls nearby creatures and pulses damage.', 'Pull is toward the field center, with native particles and fixed duration.')
recipe('summon_swords', dismissible(summon('vex',1,'iron_sword'),summon('vex',1,'golden_sword'),summon('vex',1,'diamond_sword')), ['ender','conjuration','metal'], 'Three owned flying weapon carriers fight until dismissed.', 'Vanilla vex carriers substitute for custom floating claymore, rapier, and sword entities.')
recipe('shadow_slash', [ray(12,action('teleport')),melee(5,weapon(5))], ['shadow','teleportation','evocation'], 'Blink behind the aimed target and slash nearby enemies with weapon scaling.')
recipe('arcane_shackle', [projectile(near(3,manifest('tether',80,health=10,radius=1.5),count=3))], ['ender','abjuration'], 'A projectile tethers up to three creatures for four seconds.', 'Native backing markers substitute for the source three-chain model.')
recipe('gravity_fissure', [field(100,3.5,near(3.5,action('pull',strength=.25),damage(1.5)),spec=target('self'),particle='portal',motion=.2)], ['ender','void','motion'], 'A moving singularity drags creatures along its path.')

# Evocation: creature magic, physical delivery, and utility.
def chain_death(depth):
    follow = [near(5,damage(6))]
    if depth:
        follow = [near(5,{'type':'install_binding','target':target(),'binding':binding(f'chain_{depth}', 'die', chain_death(depth-1),200,1)},damage(6))]
    return follow
recipe('chain_creeper', [aim(32,near(3,{'type':'install_binding','target':target(),'binding':binding('chain_start','die',chain_death(2),200,1)},damage(6)))], ['evocation','conjuration'], 'An initial blast marks victims; their deaths propagate bounded nearby blasts.', 'Native death-bound blasts replace the source thrown creeper-head swarm.')
recipe('fang_strike', [action('fangs',max_targets=6,amount=amp(6),count=8,line=1)], ['evocation','conjuration'], 'A line of eight ground eruptions strikes ahead.', 'Particle eruptions replace fang models; authored damage avoids vanilla fang double hits.')
recipe('fang_ward', [self_(action('fangs',max_targets=6,amount=amp(6),count=16,radius=area(3)))], ['evocation','abjuration'], 'A ring of ground eruptions defends the caster.', 'Uses native eruption particles instead of fang models; each creature is hit once.')
recipe('firecracker', [projectile(near(2,damage(4),action('knockback',strength=.4)),item='firework_rocket',gravity=.03)], ['evocation','force'], 'A lobbed firework bursts with damage and knockback.')
recipe('gust', [each(target('cone',rng(8),'hostile',False),damage(6),action('knockback',strength=1.2))], ['air','motion','evocation'], 'A broad gust damages and pushes enemies away.')
inv_break = binding('break_invisibility','damage_dealt',[self_(action('remove_status',{'effect':'minecraft:invisibility'})),{'type':'end_manifestation'}],200,1)
recipe('invisibility', [self_(status('invisibility',200),action('aggro_clear')),buff(200,bindings=[inv_break])], ['illusion'], 'Turn invisible and clear pursuit; attacking ends the effect.')
recipe('lob_creeper', [projectile(near(3,damage(6)),item='creeper_head',gravity=.05,speed=.8)], ['evocation','conjuration'], 'A gravity-driven creeper bomb damages an area.')
recipe('shield', [aim(3,manifest('barrier',200,stationary=1,health=20,radius=2))], ['abjuration','force'], 'A stationary destructible ward intercepts projectiles.', 'Native marker collision does not form the source full solid wall.')
recipe('spectral_hammer', [each(target('block_ray',rng(8)),action('break_blocks',radius=1,depth=1,hardness=10))], ['force','transmutation'], 'Mine a tool-aware 3×3 face of breakable blocks; honor block-break cancellation.')
recipe('summon_horse', dismissible(summon('horse',duration=2400)), ['conjuration','life'], 'Summon a tamed, saddled owned horse for two minutes; recast dismisses it.')
recipe('summon_vex', dismissible(summon('vex',2,'iron_sword')), ['conjuration','spirit'], 'Two owned flying minions fight and follow; recast dismisses them.')
recipe('slow', [ray(24,status('slowness',80,1),status('mining_fatigue',80,0))], ['time','enchantment'], 'Slow the aimed creature’s movement and digging for four seconds.', 'Uses vanilla movement/mining penalties; custom cast-time penalties are outside the native status model.')
recipe('arrow_volley', [projectile(damage(3,max_hits_per_target=4),item='arrow',count=12,rain=1,distance=40,speed=2)], ['evocation','metal'], 'Rain twelve arrows over the aimed area; each creature can take at most four impacts.')
recipe('wololo', [ray(32,branch(compare('target/entity_type',{'id':'minecraft:sheep'}),action('aggro_convert')),relationship='any')], ['transmutation','polymorph'], 'Change an aimed sheep to a random wool color.')
recipe('throw', [{'type':'capture_value','key':'vestige:throw/weapon_damage','value':fact('actor/weapon_damage')},projectile(action('damage',amount=add(amp(1),fact('throw/weapon_damage')),ignore_invulnerability=1),held_item=1,item='iron_sword',gravity=.03)], ['motion','evocation'], 'Throw one held item with weapon damage; recover it at impact or expiry.')
recipe('fang_swirl', [aim(24,action('fangs',max_targets=6,amount=amp(6),count=16,radius=area(3)))], ['conjuration','evocation'], 'A ring of eruptions appears at the aimed position.', 'Uses particle eruptions instead of fang models; each creature is hit once.')
recipe('scapegoat', [aim(12,manifest('decoy',300,ids={'entity':'minecraft:goat'},count=1))], ['conjuration','illusion'], 'A goat decoy attracts nearby hostile mobs for fifteen seconds.')

# Fire: channels, explosions, multi-stage walls, and weapon attacks.
recipe('blaze_storm', [repeat(10,6,projectile(damage(2),ignite(),item='fire_charge',spread=8))], ['fire','evocation'], 'Channel a stream of ten burning firebolts.')
recipe('burning_dash', [self_(action('dash',strength=2)),repeat(8,1,melee(2,damage(6,max_hits_per_target=1),ignite()))], ['fire','motion'], 'Dash forward, burning each contacted creature once.')
recipe('fireball', [projectile(near(3,damage(8),ignite()),item='fire_charge',speed=1)], ['fire','evocation'], 'A charged fireball explodes on impact.')
recipe('firebolt', [projectile(damage(5),ignite(),item='fire_charge',speed=2)], ['fire','evocation'], 'A fast burning bolt.')
recipe('fire_breath', [cone(16,10,damage(1.25),ignite())], ['fire','evocation'], 'Channel a cone of damaging flame.')
recipe('magma_bomb', [projectile(near(3,damage(8),ignite()),field(100,3,near(3,damage(1),ignite()),particle='lava'),item='magma_cream',gravity=.05,speed=.8)], ['fire','earth','evocation'], 'A lobbed bomb leaves a damaging magma field.')
recipe('wall_of_fire', [aim(20,store('first_anchor')),wait(40),aim(20,manifest('wall',120,damage=amp(2),max_length=16,max_targets=6))], ['fire','conjuration'], 'Two casts draw a six-second burning segment that intercepts projectiles.', 'A two-point native segment replaces the source extendable multi-segment wall; maximum length is sixteen blocks.')
recipe('heat_surge', [each(target('nearby_entities',area(4),'hostile',False),damage(7),ignite(),action('knockback',strength=.7))], ['fire','evocation'], 'An outward burst burns and repels nearby enemies.')
recipe('flaming_strike', [melee(3,weapon(4),ignite())], ['fire','evocation'], 'A weapon-scaled burning melee sweep.')
recipe('scorch', [aim(24,{'type':'delay','ticks':15},near(2.5,damage(8),ignite()))], ['fire','earth','evocation'], 'A delayed ground eruption burns the aimed area.')
recipe('flaming_barrage', [projectile(damage(3),ignite(),item='fire_charge'),repeat(4,1,wait(120),projectile(damage(3),ignite(),item='fire_charge'))], ['fire','evocation'], 'Five separately aimed fireballs share one recast session with six seconds to aim each shot.')
recipe('fire_arrow', [projectile(near(2,damage(8),ignite()),item='arrow',speed=3)], ['fire','evocation'], 'An explosive burning arrow.')
recipe('raise_hell', [melee(5,weapon(10),ignite(),action('knockback',strength=.5))], ['fire','earth','evocation'], 'A ground slam adds half the held weapon’s damage to a fiery eruption.', 'One native slam; future progression does not change its present outcome.')

# Holy: healing, protection, movement, and attacks with ally rules.
recipe('angel_wing', [self_(action('flight')),buff(200),], ['holy','air','motion'], 'Grant temporary flight; owned cleanup restores prior flight permission.', 'Native flight permissions replace the source elytra-style angel wing movement and visuals.')
recipe('blessing_of_life', [ray(24,heal(5),relationship='ally')], ['holy','life'], 'Heal an aimed allied creature.')
recipe('fortify', [each(target('nearby_entities',area(4),'ally',False,count=4),status('absorption',200,0))], ['holy','abjuration','life'], 'Up to four nearby allies gain four absorption HP for ten seconds.')
recipe('greater_heal', [self_(heal(12))], ['holy','life'], 'Restore up to twelve HP to the caster after a charge.')
recipe('guiding_bolt', [projectile(damage(3),branch({'type':'exists','path':'vestige:target/entity_type'},manifest('status',200,on_tick=[action('redirect_projectiles',radius=6)],interval=2)),item='glowstone_dust')], ['holy','light','evocation'], 'A bolt marks its target and bends nearby projectiles toward it.')
recipe('healing_circle', [aim(24,field(120,3,near(3,heal(2),relationship='ally',count=4),particle='heart',interval=20))], ['holy','life','conjuration'], 'A persistent circle heals up to four allied creatures inside it.')
recipe('heal', [self_(heal(5))], ['holy','life'], 'Restore a moderate amount of health to the caster.')
recipe('sunbeam', [repeat(16,4,each(target('beam',rng(24),'hostile',False,count=3),damage(1.5)))], ['holy','light','evocation'], 'A sustained piercing beam deals repeated holy damage.', 'Native beam pulses replace the source custom beam entity.')
recipe('wisp', [ray(48,field(100,2,near(2,damage(1)),follow_target=1,particle='end_rod',interval=10))], ['holy','spirit','conjuration'], 'A following wisp field repeatedly damages creatures near its target.', 'Native following field replaces the source autonomous wisp body and return animation.')
recipe('divine_smite', [melee(3,weapon(6))], ['holy','evocation'], 'A holy melee strike adds half the held weapon’s damage.')
recipe('haste', [each(target('nearby_entities',area(4),'ally',False,count=4),status('haste',400,0),status('speed',400,0))], ['holy','time','enchantment'], 'Up to four nearby allies gain mining and movement speed.', 'Custom cast-speed bonus is outside the native status model.')
recipe('cleanse', [each(target('nearby_entities',area(5),'ally',False),action('cleanse'))], ['holy','abjuration'], 'Remove harmful status effects from nearby allies.')

# Ice: freezing, fields, displacement, and protective reactions.
recipe('cone_of_cold', [cone(16,10,damage(1.25),freeze(),status('slowness',40))], ['ice','evocation'], 'Channel a freezing cone that slows victims.')
recipe('frost_step', [field(60,3,near(3,freeze(),status('slowness',100)),spec=target('self'),particle='snowflake'),aim(4,action('teleport'))], ['ice','teleportation','illusion'], 'Teleport away while a freezing decoy field remains at the departure point.', 'Residual frost field replaces the source destructible ice clone and shatter shards.')
recipe('ice_block', [projectile(near(3,damage(12),freeze()),item='ice',speed=.7)], ['ice','conjuration','evocation'], 'Throw a heavy block of ice that bursts on impact.', 'Projectile delivery substitutes for the source falling physical block entity.')
recipe('icicle', [projectile(damage(6),freeze(),item='packed_ice',speed=3)], ['ice','evocation'], 'A fast icicle freezes its victim.')
recipe('summon_polar_bear', dismissible(summon('polar_bear')), ['ice','conjuration','life'], 'An owned polar bear follows and fights; recast dismisses it.')
recipe('ray_of_frost', [each(target('beam',rng(24),'hostile',False,count=3),damage(12),freeze(),status('slowness',60))], ['ice','evocation'], 'A piercing frost ray slows and freezes creatures.')
recipe('frostwave', [each(target('nearby_entities',area(4),'hostile',False),damage(7),freeze(),action('knockback',strength=.7))], ['ice','evocation'], 'A radial frost blast repels creatures.')
recipe('ice_spikes', [action('fangs',max_targets=6,amount=amp(12),count=8,line=1),each(target('cone',rng(11),'hostile',False,angle=15),freeze())], ['ice','earth','conjuration'], 'A ground line of eight eruptions freezes creatures.', 'Native particles replace ice spike models; each creature is hit once.')
tomb_hit = binding('tomb_break','damage_calculating',[action('reduce_pending_damage',amount=fact('event/damage_amount')),{'type':'end_manifestation'}],100,1)
recipe('ice_tomb', [manifest('barrier',100,target('self'),bindings=[tomb_hit],on_tick=[self_(heal(1),status('slowness',25,10))],interval=20)], ['ice','abjuration','life'], 'A protective tomb heals while immobilizing the caster; the first hit breaks it.', 'A native attached ward replaces the source mounted tomb body.')
recipe('snowball', [projectile(near(2.5,freeze(),status('slowness',60,1)),item='snowball',gravity=.03)], ['ice','evocation'], 'An explosive snowball freezes and slows an area for three seconds.')
shatter = binding('shatter','die',[near(3,damage(5),freeze())],200,1)
frostbite = binding('frostbite','damage_dealt',[each(target('event_target'),freeze(),status('slowness',60),{'type':'install_binding','target':target(),'binding':shatter})],200,3,[compare('event/magical',False)])
recipe('frostbite', [buff(200,bindings=[frostbite])], ['ice','enchantment'], 'Three ordinary attacks freeze targets; marked deaths shatter into nearby enemies.')
recipe('blizzard', [aim(24,field(120,5,near(5,damage(1.5),freeze(),status('slowness',25)),particle='snowflake',interval=10))], ['ice','evocation'], 'A lingering blizzard freezes and slows creatures inside it.')

# Lightning: linked targets, movement, channels, and repeated storms.
recipe('ascension', [self_(action('launch',up=1.8,strength=0),status('slow_falling',80)),each(target('nearby_entities',area(4),'hostile',False),damage(5))], ['lightning','air','motion'], 'Launch upward with slow falling while shocking nearby enemies.')
recipe('chain_lightning', [each(target('chain',rng(32),'hostile',True,count=4,jump=6),damage(6))], ['lightning','evocation'], 'Lightning jumps between four visible nearby creatures.')
recipe('charge', [self_(status('speed',200,1),status('haste',200,0))], ['lightning','motion','enchantment'], 'A ten-second charge grants movement and mining speed.', 'Native status bonuses replace source school-power and attack-speed modifiers.')
recipe('electrocute', [cone(16,10,damage(1.5))], ['lightning','evocation'], 'Channel an electrical cone.')
recipe('lightning_bolt', [aim(32,near(2,damage(9)))], ['lightning','evocation'], 'Strike the aimed point with an electrical burst.', 'Native particles/damage avoid vanilla lightning igniting unrelated terrain.')
recipe('lightning_lance', [projectile(damage(8),pierce=2,speed=3,distance=48,item='lightning_rod')], ['lightning','evocation'], 'A piercing lightning projectile.')
recipe('shockwave', [each(target('nearby_entities',area(4.5),'hostile',False),damage(7),action('knockback',strength=1))], ['lightning','evocation'], 'A wide electrical shockwave knocks enemies away.')
recipe('thunderstorm', [field(200,8,near(8,damage(6),count=2,line_of_sight=1),spec=target('self'),follow_target=1,particle='electric_spark',interval=20)], ['lightning','evocation'], 'A ten-second storm follows the caster and strikes up to two visible nearby enemies per pulse.')
recipe('ball_lightning', [projectile(damage(5),field(80,3,near(3,damage(1)),particle='electric_spark',interval=10),item='lightning_rod',speed=.6)], ['lightning','evocation'], 'A slow orb leaves a lingering electrical field on impact.', 'Persistent impact field replaces source bouncing orb movement.')
recipe('volt_strike', [self_(action('dash',strength=2.5)),repeat(8,1,melee(2,damage(12,max_hits_per_target=1)))], ['lightning','motion','evocation'], 'Dash through enemies, damaging each contacted creature once.')

# Nature: poison, terrain, healing suppression, and conditional reactions.
recipe('acid_orb', [projectile(near(2,damage(3),status('weakness',60)),item='slime_ball',gravity=.04)], ['acid','evocation'], 'Spit acid into an area, weakening victims.', 'Weakness substitutes for source Rend armor reduction.')
blight = binding('blight','heal_calculating',[action('reduce_pending_heal',amount=mul(.5,fact('event/damage_amount')))],200)
recipe('blight', [ray(32,manifest('status',200,bindings=[blight]),status('poison',200))], ['curse','poison','necromancy'], 'Halve the victim’s incoming healing and apply poison.')
recipe('poison_arrow', [projectile(damage(4),status('poison',60),field(40,2,near(2,status('poison',40)),particle='spore_blossom_air'),item='arrow',speed=3)], ['poison','evocation'], 'A poison arrow leaves a brief poisonous cloud.')
recipe('poison_breath', [cone(16,10,damage(1),status('poison',40))], ['poison','evocation'], 'Channel a poisonous cone.')
recipe('poison_splash', [aim(24,near(4,damage(3),status('poison',100)))], ['poison','evocation'], 'Splash poison across an aimed area.')
recipe('root', [ray(24,manifest('tether',80,health=10,radius=.5,on_tick=[status('slowness',25,4)],interval=5))], ['plant','abjuration'], 'Anchor and heavily slow an aimed creature for four seconds or until its ten-HP tether breaks.', 'Native marker tether substitutes for the source root body.')
spider = binding('spider_bite','damage_dealt',[each(target('event_target'),branch(compare('target/status/minecraft/poison',True),damage(3)))],200,3,[compare('event/magical',False)])
recipe('spider_aspect', [buff(200,bindings=[spider])], ['poison','enchantment'], 'The next three ordinary attacks within ten seconds add damage when the victim is poisoned.')
def swarm(depth):
    spread = [] if depth == 0 else [near(5,swarm(depth-1),count=2)]
    bindings = [] if not spread else [binding(f'swarm_spread_{depth}','die',spread,200,1)]
    return manifest('status',100,on_tick=[damage(2)],bindings=bindings,interval=20,ids={'particle':'minecraft:spore_blossom_air'})
recipe('firefly_swarm', [ray(32,swarm(2))], ['plant','life','conjuration'], 'A damaging swarm follows its victim and spreads on death through two generations.', 'Native following particles replace individual firefly bodies.')
oak = binding('oakskin','damage_calculating',[action('reduce_pending_damage',amount=mul(.25,fact('event/damage_amount')))],200,6,[compare('event/magical',False),compare('event/fire',False)])
recipe('oakskin', [buff(200,bindings=[oak])], ['wood','abjuration'], 'Reduce up to six ordinary non-fire hits by 25% within ten seconds.', 'Source fire vulnerability and bespoke bark visuals are deferred.')
recipe('earthquake', [aim(24,field(120,5,near(5,damage(2),status('slowness',25)),particle='crit',interval=10))], ['earth','evocation'], 'A ground field repeatedly damages and slows creatures.')
recipe('stomp', [each(target('cone',rng(8),'hostile',False,angle=35),damage(8),action('knockback',strength=1,up=.5))], ['earth','evocation','motion'], 'A forward ground wave damages and launches enemies.')
gluttony = binding('gluttony','item_use_finished',[self_(action('food_mana',amount=mul(6,fact('event/food_nutrition'))))],200,3)
recipe('gluttony', [buff(200,bindings=[gluttony])], ['life','transmutation'], 'Up to three foods consumed within ten seconds convert nutrition into native mana, capped at 100.', 'Uses the native player mana pool; capacity progression remains deferred.')
recipe('touch_dig', [each(target('block_ray',rng(8)),action('break_block',hardness=10))], ['earth','transmutation'], 'Break a hardness-limited aimed block using the held tool and normal drops.')

# Eldritch: perception, force, causal beams, and an owned private dimension.
recipe('abyssal_shroud', [ward(60,3)], ['void','abjuration'], 'Dodge up to three incoming hits within three seconds.')
recipe('sculk_tentacles', [aim(24,field(100,4,near(4,damage(4),status('blindness',25)),particle='sculk_soul',interval=20))], ['shadow','conjuration','evocation'], 'Tentacle pulses damage and briefly blind creatures in an aimed area.', 'Native particles substitute for custom tentacle bodies.')
recipe('sonic_boom', [each(target('beam',rng(20),'hostile',False,through_blocks=1,radius=.75,count=3),damage(14),action('knockback',strength=.8))], ['sonic','evocation'], 'A piercing sonic beam passes through solid blocks.')
recipe('planar_sight', [field(400,24,near(24,status('glowing',40),relationship='any',through_blocks=1,count=16),spec=target('self'),follow_target=1,interval=20)], ['divination','void'], 'Reveal up to sixteen nearby creatures through walls for twenty seconds.', 'Vanilla glowing is visible to all viewers; a caster-only outline renderer remains visual polish.')
recipe('telekinesis', [ray(24,store('target_anchor')),repeat(40,2,each(target('stored_target'),action('grip',distance=6,impact_damage=4,max_hits_per_target=3)))], ['force','motion','transmutation'], 'Hold the originally aimed creature for four seconds; at most three collisions inflict damage.')
recipe('eldritch_blast', [each(target('beam',rng(24),'hostile',False),damage(6)),repeat(2,1,wait(100),each(target('beam',rng(24),'hostile',False),damage(6)))], ['void','evocation'], 'Three separately aimed piercing beams share a recast session with five seconds to aim each shot.')
recipe('pocket_dimension', [action('pocket_dimension')], ['space','void','conjuration','teleportation'], 'Enter a persistent private room; casting inside returns to the saved entry point.', 'Native isolated quartz rooms replace Iron templates, furniture, and ambient presentation.')


RECIPES["sonic_boom"][0].insert(0,action("utterance"))

def write():
    assert set(RECIPES) == set(ROWS), f'Missing: {set(ROWS)-set(RECIPES)}; extra: {set(RECIPES)-set(ROWS)}'
    out = ROOT / 'src/main/resources/data/vestige/runtime_spells'
    out.mkdir(parents=True, exist_ok=True)
    traditions = {'blood':['occult'], 'ender':['arcane'], 'evocation':['arcane'], 'fire':['arcane','primal'],
                  'holy':['divine'], 'ice':['arcane','primal'], 'lightning':['arcane','primal'], 'nature':['primal'], 'eldritch':['occult']}
    aliases = {'angel_wing':'Angel Wings','acid_orb':'Acid Spit','poison_breath':'Poison Spray','spider_aspect':'Aspect of the Spider'}
    for name, row in ROWS.items():
        effects, traits = RECIPES[name]
        costs = balanced_costs(name)
        spell = {'source':{'spell':f'irons_spellbooks:{name}','school':f"irons_spellbooks:{row['school']}",
                 'revision':REVISION,'name':aliases.get(name,name.replace('_',' ').title()),
                 'cast_type':row['cast_type'],'cooldown_ticks':int(row['cooldown_seconds']*20)},
                 'rarity':RARITIES[name], 'traditions':traditions[row['school']],
                 'traits':{**{f'vestige:{t}':4 for t in traits},'vestige:amplify':1,'vestige:range':1,'vestige:area':1},
                 'costs':costs,'triggers':[{'id':'vestige:primary','event':'vestige:interact'}], 'effects':effects}
        (out / f'{name}.json').write_text(json.dumps(decorate(spell,name),indent=2,ensure_ascii=False)+'\n')
    for name in ('force_arrow', 'summon_zombie', 'arcane_lock', 'interposing_earth'):
        path = out / f'{name}.json'
        spell = without_cosmetics(json.loads(path.read_text()))
        spell['costs'] = balanced_costs(name)
        path.write_text(json.dumps(decorate(spell,name),indent=2,ensure_ascii=False)+'\n')
    ledger = ['# Iron spell conversion ledger','',
              f'All **{len(ROWS)} default-enabled spells** in the pinned catalog have native Vestige effect graphs. Source: Iron 3.16.3 / Minecraft 1.21.1 at `{REVISION}`.', '',
              'These native adaptations execute without Iron or the retired Wizardry layer. The complete catalog has a native balance pass with authored outcomes, mana, charge times, cooldowns, and constraints. Models, animation, audio, advanced entity AI, and the explicitly listed mechanic differences are not parity claims.', '',
              'All recipes are checked-in under `src/main/resources/data/vestige/runtime_spells/`. `tools/convert_irons_spells.py` regenerates them from explicit per-spell recipes; it has no fallback conversion. `tools/irons-spells.json` freezes the source catalog.', '',
              'Native costs are authored in `tools/spell-balance-policy.json`; original Iron cooldowns remain provenance. `/vestige_magic cast_balanced` enforces native resource costs and cooldowns. Discovery, progression, and wand controls remain deferred.', '',
              'See [the complete balance review](../spell-balance-review.md) for every spell\'s role and tuning rationale. Rarity does not copy upstream rarity or impose trait-point budgets. These are tested initial balance baselines; future encounter playtesting can refine them.', '',
              '| Spell / native ID | Rarity | School | Native behavior | Adaptation / remaining difference |','|---|---|---|---|---|']
    for name,row in ROWS.items():
        description, note = NOTES[name]
        url = f'https://github.com/iron431/irons-spells-n-spellbooks/blob/{REVISION}/{row["file"]}'
        ledger.append(f'| [{name}]({url}) | {RARITIES[name]} | {row["school"]} | {description} | {note or "Shared native delivery and presentation; native balance baseline."} |')
    (ROOT/'docs/design/iron-spell-conversions.md').write_text('\n'.join(ledger)+'\n')
    print(f'Wrote {len(ROWS)} native spell definitions and the conversion ledger.')

def balanced_costs(name):
    tuning = BALANCE_POLICY['spells'][name]
    costs = [{'type':'mana','amount':tuning['mana']}, {'type':'cooldown','ticks':tuning['cooldown_ticks']}]
    if tuning['charge_ticks']: costs.insert(0, {'type':'time','ticks':tuning['charge_ticks']})
    return costs

if __name__ == '__main__': write()
