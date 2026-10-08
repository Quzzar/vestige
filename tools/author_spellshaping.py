#!/usr/bin/env python3
"""Author the complete executable Spellshaping set; each local pairing has one result."""
import argparse, json, pathlib, collections
ROOT=pathlib.Path(__file__).resolve().parents[1]
# Names and local routes for implemented rules only.
ROUTES = {'reaching': {'name': 'Reaching',
              'effect': 'Range ×1.3 per contribution.',
              'pairs': [{'ingredient': 'minecraft:emerald', 'foundation': 'minecraft:end_stone'},
                        {'ingredient': 'minecraft:blaze_rod', 'foundation': 'minecraft:end_stone'}]},
 'widening': {'name': 'Widening',
              'effect': 'Area ×1.2 per contribution.',
              'pairs': [{'ingredient': 'minecraft:gunpowder', 'foundation': 'minecraft:quartz_block'}]},
 'focused': {'name': 'Focused',
             'effect': 'Amplify ×1.12 per contribution.',
             'pairs': [{'ingredient': 'minecraft:emerald', 'foundation': 'minecraft:diamond_block'}]},
 'piercing': {'name': 'Piercing',
              'effect': 'Extra native projectile penetration, limited to four pierces.',
              'pairs': [{'ingredient': 'minecraft:arrow', 'foundation': 'minecraft:diamond_block'}]},
 'seeking': {'name': 'Seeking',
             'effect': 'Native projectiles steer toward an eligible target found at release.',
             'pairs': [{'ingredient': 'minecraft:compass', 'foundation': 'minecraft:lodestone'},
                       {'ingredient': 'minecraft:arrow', 'foundation': 'minecraft:lodestone'}]},
 'forked': {'name': 'Forked',
            'effect': 'Increase a native projectile fan; existing hit and rider budgets remain shared.',
            'pairs': [{'ingredient': 'minecraft:string', 'foundation': 'minecraft:copper_block'},
                      {'ingredient': 'minecraft:chain', 'foundation': 'minecraft:quartz_block'}]},
 'enduring': {'name': 'Enduring',
              'effect': 'Extend finite supported manifestations without independently extending event '
                        'bindings.',
              'pairs': [{'ingredient': 'minecraft:clock', 'foundation': 'minecraft:amethyst_block'},
                        {'ingredient': 'minecraft:phantom_membrane',
                         'foundation': 'minecraft:amethyst_block'}]},
 'patient': {'name': 'Patient',
             'effect': 'Twenty extra charge ticks per contribution reduce mana payment.',
             'pairs': [{'ingredient': 'minecraft:clock', 'foundation': 'minecraft:stone'}]},
 'hurried': {'name': 'Hurried',
             'effect': 'Shorter charge at a higher mana price.',
             'pairs': [{'ingredient': 'minecraft:feather', 'foundation': 'minecraft:copper_block'}]},
 'delayed': {'name': 'Delayed',
             'effect': 'A visible cue precedes a two-second delayed release.',
             'pairs': [{'ingredient': 'minecraft:clock', 'foundation': 'minecraft:redstone_block'}]},
 'bleeding': {'name': 'Bleeding',
              'effect': 'Positive primary damage opens one bounded wound.',
              'pairs': [{'ingredient': 'minecraft:gunpowder', 'foundation': 'minecraft:bone_block'},
                        {'ingredient': 'minecraft:fermented_spider_eye',
                         'foundation': 'minecraft:bone_block'}]},
 'shocking': {'name': 'Shocking',
              'effect': 'Add a small electrical rider after an eligible hit.',
              'pairs': [{'ingredient': 'minecraft:emerald', 'foundation': 'minecraft:copper_block'},
                        {'ingredient': 'minecraft:iron_ingot', 'foundation': 'minecraft:copper_block'}]},
 'kindled': {'name': 'Kindled',
             'effect': 'Ignite a compatible damaged recipient.',
             'pairs': [{'ingredient': 'minecraft:blaze_rod', 'foundation': 'minecraft:gold_block'},
                       {'ingredient': 'minecraft:emerald', 'foundation': 'minecraft:gold_block'}]},
 'chilling': {'name': 'Chilling',
              'effect': 'Cold contact leaves brief movement impairment.',
              'pairs': [{'ingredient': 'minecraft:ice', 'foundation': 'minecraft:blue_ice'}]},
 'freezing': {'name': 'Freezing',
              'effect': 'A sufficiently qualified cold impact builds a finite frozen state.',
              'pairs': [{'ingredient': 'minecraft:snowball', 'foundation': 'minecraft:blue_ice'},
                        {'ingredient': 'minecraft:slime_ball', 'foundation': 'minecraft:blue_ice'}]},
 'hindering': {'name': 'Hindering',
               'effect': 'Apply brief Slow after a qualifying outcome.',
               'pairs': [{'ingredient': 'minecraft:emerald', 'foundation': 'minecraft:lapis_block'}]},
 'envenomed': {'name': 'Envenomed',
               'effect': 'Add bounded poison to an eligible damaged recipient.',
               'pairs': [{'ingredient': 'minecraft:spider_eye', 'foundation': 'minecraft:moss_block'},
                         {'ingredient': 'minecraft:poisonous_potato', 'foundation': 'minecraft:moss_block'}]},
 'interrupting': {'name': 'Interrupting',
                  'effect': 'A qualifying impact interrupts one active supported channel.',
                  'pairs': [{'ingredient': 'minecraft:chain', 'foundation': 'minecraft:lapis_block'}]},
 'marking': {'name': 'Marking',
             'effect': 'Leave a visible tracking mark after a qualified contact.',
             'pairs': [{'ingredient': 'minecraft:glowstone_dust', 'foundation': 'minecraft:sea_lantern'}]},
 'repelling': {'name': 'Repelling',
               'effect': 'Drive a hit recipient away from the declared impact origin.',
               'pairs': [{'ingredient': 'minecraft:emerald', 'foundation': 'minecraft:iron_block'},
                         {'ingredient': 'minecraft:gunpowder', 'foundation': 'minecraft:iron_block'}]},
 'drawing': {'name': 'Drawing',
             'effect': 'Bring a qualified recipient toward an explicit anchor.',
             'pairs': [{'ingredient': 'minecraft:chain', 'foundation': 'minecraft:slime_block'}]},
 'lifting': {'name': 'Lifting',
             'effect': 'Briefly lift a qualified subject.',
             'pairs': [{'ingredient': 'minecraft:feather', 'foundation': 'minecraft:quartz_pillar'}]},
 'unbalancing': {'name': 'Unbalancing',
                 'effect': 'Apply a short physical stumble after contact.',
                 'pairs': [{'ingredient': 'minecraft:feather', 'foundation': 'minecraft:iron_block'}]},
 'echoing': {'name': 'Echoing',
             'effect': 'A supported direct outcome repeats at reduced strength.',
             'pairs': [{'ingredient': 'minecraft:echo_shard', 'foundation': 'minecraft:sculk'}]},
 'mending': {'name': 'Mending',
             'effect': 'Add finite regeneration after an eligible heal.',
             'pairs': [{'ingredient': 'minecraft:golden_carrot', 'foundation': 'minecraft:amethyst_block'},
                       {'ingredient': 'minecraft:glistering_melon_slice',
                        'foundation': 'minecraft:amethyst_block'}]},
 'restoring': {'name': 'Restoring',
               'effect': 'Strengthen or replace a specifically eligible recovery outcome.',
               'pairs': [{'ingredient': 'minecraft:glistering_melon_slice',
                          'foundation': 'minecraft:diamond_block'}]},
 'cleansing': {'name': 'Cleansing',
               'effect': 'Couple eligible healing to a limited removal of harmful statuses.',
               'pairs': [{'ingredient': 'minecraft:milk_bucket', 'foundation': 'minecraft:moss_block'},
                         {'ingredient': 'minecraft:glistering_melon_slice',
                          'foundation': 'minecraft:moss_block'}]},
 'purifying': {'name': 'Purifying',
               'effect': 'Remove explicitly selected harmful statuses after an eligible protective/healing '
                         'result.',
               'pairs': [{'ingredient': 'minecraft:milk_bucket', 'foundation': 'minecraft:amethyst_block'},
                         {'ingredient': 'minecraft:amethyst_shard', 'foundation': 'minecraft:glowstone'}]},
 'invigorating': {'name': 'Invigorating',
                  'effect': 'Healing briefly energizes a recipient.',
                  'pairs': [{'ingredient': 'minecraft:golden_carrot',
                             'foundation': 'minecraft:copper_block'}]},
 'quieting': {'name': 'Quieting',
              'effect': 'Suppress authored casting cues that own a sound.',
              'pairs': [{'ingredient': 'minecraft:white_wool', 'foundation': 'minecraft:white_wool'},
                        {'ingredient': 'minecraft:paper', 'foundation': 'minecraft:white_wool'}]},
 'veiled': {'name': 'Veiled',
            'effect': 'A compatible non-damaging utility cast grants brief invisibility.',
            'pairs': [{'ingredient': 'minecraft:ink_sac', 'foundation': 'minecraft:white_wool'},
                      {'ingredient': 'minecraft:paper', 'foundation': 'minecraft:white_concrete'}]},
 'excavating': {'name': 'Excavating',
                'effect': 'Increase the supported mining hardness threshold.',
                'pairs': [{'ingredient': 'minecraft:flint', 'foundation': 'minecraft:gold_block'},
                          {'ingredient': 'minecraft:gravel', 'foundation': 'minecraft:gold_block'}]},
 'gathering': {'name': 'Gathering',
               'effect': 'Range ×1.2 per contribution.',
               'pairs': [{'ingredient': 'minecraft:compass', 'foundation': 'minecraft:iron_block'}]},
 'shaping': {'name': 'Shaping',
             'effect': 'Range ×1.2 per contribution.',
             'pairs': [{'ingredient': 'minecraft:clay_ball', 'foundation': 'minecraft:stone'},
                       {'ingredient': 'minecraft:stone', 'foundation': 'minecraft:stone'}]},
 'warded': {'name': 'Warded',
            'effect': 'After successful entity protection, add an owned four-second ward with 2 charges and '
                      '1 base mitigation per hit.',
            'pairs': [{'ingredient': 'minecraft:pufferfish', 'foundation': 'minecraft:iron_block'}]},
 'absorbing': {'name': 'Absorbing',
               'effect': 'After successful entity protection, add an owned four-second ward with 1 charges '
                         'and 4 base mitigation per hit.',
               'pairs': [{'ingredient': 'minecraft:obsidian', 'foundation': 'minecraft:diamond_block'}]},
 'conjuring': {'name': 'Conjuring',
               'effect': 'Increase explicitly authored construct or summon health.',
               'pairs': [{'ingredient': 'minecraft:amethyst_shard',
                          'foundation': 'minecraft:diamond_block'}]},
 'frugal': {'name': 'Frugal',
            'effect': 'Amplify ×0.85 per contribution.',
            'pairs': [{'ingredient': 'minecraft:paper', 'foundation': 'minecraft:stone'}]},
 'reckless': {'name': 'Reckless',
              'effect': 'Amplify ×1.15 per contribution.',
              'pairs': [{'ingredient': 'minecraft:gunpowder', 'foundation': 'minecraft:magma_block'}]},
 'votive': {'name': 'Votive',
            'effect': 'Amplify ×1.12 per contribution.',
            'pairs': [{'ingredient': 'minecraft:paper', 'foundation': 'minecraft:gold_block'}]},
 'greedy': {'name': 'Greedy',
            'effect': 'Amplify ×1.15 per contribution.',
            'pairs': [{'ingredient': 'minecraft:paper', 'foundation': 'minecraft:emerald_block'},
                      {'ingredient': 'minecraft:golden_carrot', 'foundation': 'minecraft:emerald_block'}]},
 'sacrificial': {'name': 'Sacrificial',
                 'effect': 'Amplify ×1.1 per contribution.',
                 'pairs': [{'ingredient': 'minecraft:iron_ingot', 'foundation': 'minecraft:soul_sand'}]},
 'famished': {'name': 'Famished',
              'effect': 'Amplify ×1.1 per contribution.',
              'pairs': [{'ingredient': 'minecraft:golden_carrot', 'foundation': 'minecraft:moss_block'}]},
 'bloodbound': {'name': 'Bloodbound',
                'effect': 'Amplify ×1.05 per contribution.',
                'pairs': [{'ingredient': 'minecraft:fermented_spider_eye',
                           'foundation': 'minecraft:soul_sand'}]},
 'fasting': {'name': 'Fasting',
             'effect': 'Exchange a quarter of adjusted mana for one food point per 7.5 mana, rounded up.',
             'pairs': [{'ingredient': 'minecraft:bread', 'foundation': 'minecraft:amethyst_block'}]},
 'exhausting': {'name': 'Exhausting',
                'effect': 'Amplify ×1.25 and mana ×1.35 per contribution.',
                'pairs': [{'ingredient': 'minecraft:phantom_membrane',
                           'foundation': 'minecraft:iron_block'}]},
 'charged': {'name': 'Charged',
             'effect': 'Amplify ×1.15 per contribution.',
             'pairs': [{'ingredient': 'minecraft:copper_ingot', 'foundation': 'minecraft:redstone_block'}]},
 'vampiric': {'name': 'Vampiric',
              'effect': 'One owned wound whose qualifying ticks return a bounded restoration portion to the '
                        'caster.',
              'pairs': [{'ingredient': 'minecraft:fermented_spider_eye',
                         'foundation': 'minecraft:bone_block'},
                        {'ingredient': 'minecraft:amethyst_shard', 'foundation': 'minecraft:amethyst_block'},
                        {'ingredient': 'minecraft:paper', 'foundation': 'minecraft:soul_sand'}]},
 'hemorrhagic': {'name': 'Hemorrhagic',
                 'effect': 'One finite wound reserves a calibrated portion for a normal-expiry terminal '
                           'pulse.',
                 'pairs': [{'ingredient': 'minecraft:fermented_spider_eye',
                            'foundation': 'minecraft:bone_block'},
                           {'ingredient': 'minecraft:bone', 'foundation': 'minecraft:soul_sand'},
                           {'ingredient': 'minecraft:paper', 'foundation': 'minecraft:bookshelf'}]},
 'tempestuous': {'name': 'Tempestuous',
                 'effect': 'One charged windburst deals a calibrated electrical outcome and pushes eligible '
                           'recipients from its center.',
                 'pairs': [{'ingredient': 'minecraft:copper_ingot', 'foundation': 'minecraft:copper_block'},
                           {'ingredient': 'minecraft:feather', 'foundation': 'minecraft:quartz_block'},
                           {'ingredient': 'minecraft:gunpowder', 'foundation': 'minecraft:iron_block'}]},
 'glacial': {'name': 'Glacial',
             'effect': 'One lingering cold field slows or builds freeze on qualified occupants.',
             'pairs': [{'ingredient': 'minecraft:snowball', 'foundation': 'minecraft:blue_ice'},
                       {'ingredient': 'minecraft:ice', 'foundation': 'minecraft:honey_block'},
                       {'ingredient': 'minecraft:phantom_membrane',
                        'foundation': 'minecraft:amethyst_block'}]},
 'stormbound': {'name': 'Stormbound',
                'effect': 'One temporary electrical relay field joins a bounded set of selected contacts.',
                'pairs': [{'ingredient': 'minecraft:copper_ingot', 'foundation': 'minecraft:copper_block'},
                          {'ingredient': 'minecraft:chain', 'foundation': 'minecraft:copper_block'},
                          {'ingredient': 'minecraft:emerald', 'foundation': 'minecraft:end_stone'}]},
 'sustaining': {'name': 'Sustaining',
                'effect': 'One recovery plan uses a declared hunger payment to support calibrated ongoing '
                          'restoration.',
                'pairs': [{'ingredient': 'minecraft:glistering_melon_slice',
                           'foundation': 'minecraft:amethyst_block'},
                          {'ingredient': 'minecraft:golden_carrot', 'foundation': 'minecraft:moss_block'},
                          {'ingredient': 'minecraft:amethyst_shard',
                           'foundation': 'minecraft:amethyst_block'}]},
 'revealing': {'name': 'Revealing',
               'effect': 'Reveal bounded nearby invisible creatures after detection or illumination.',
               'pairs': [{'ingredient': 'minecraft:glowstone_dust', 'foundation': 'minecraft:glowstone'},
                         {'ingredient': 'minecraft:compass', 'foundation': 'minecraft:glowstone'}]},
 'anchored': {'name': 'Anchored',
              'effect': 'Hold a moving, spatially queried field at its original location.',
              'pairs': [{'ingredient': 'minecraft:ender_pearl', 'foundation': 'minecraft:lodestone'}]},
 'reflecting': {'name': 'Reflecting',
                'effect': 'A protective recipient returns a bounded number of incoming vanilla arrows or '
                          'tridents.',
                'pairs': [{'ingredient': 'minecraft:glass', 'foundation': 'minecraft:diamond_block'}]}}
RULES=[]
def trait(t):return {'trait':'vestige:'+t}
def product(*xs):return {'product':list(xs)}
def clamp(x,lo,hi):return {'clamp':{'value':x,'minimum':lo,'maximum':hi}}
def mod(t,n,op='MULTIPLY'):return {'trait':t,'operation':op,'amount':n}
def action(t,values=None,**ids):return {'type':t,'values':values or {},'identifiers':ids}
def delay(t=20):return {'type':'delay','ticks':t}
def repeat(n,plan,interval=20):return {'type':'repeat','count':n,'interval':interval,'effects':plan}
def status(effect,duration=80):return action('status',{'duration':clamp(product(duration,trait('time')),20,400),'amplifier':0},effect='minecraft:'+effect)
def damage(amount,element):return action('damage',{'amount':clamp(product(amount,trait(element),trait('amplify')),0,4),'ignore_invulnerability':1})
def cue(color,shape='sparks'):
 return {'type':'visual','visual':{'duration':16,'radius':.65,'height':.5,'layers':[{'shape':shape,'color':color,'alpha':.8,'width':.035,'scale':1,'count':8}]}}
def add(key,description,**fields):
 c=ROUTES[key];r={'id':key,'name':c['name'],'description':description,'pairs':[{'offering':p['ingredient'],'material':p['foundation']} for p in c['pairs']]}
 r.update(fields);RULES.append(r);return r

def numeric(key,axis,multiplier,cost=1.1,**fields):
 return add(key,f'{axis.title()} ×{multiplier:g} per contribution.',reads=[axis],traits=[mod(axis,multiplier)],cost_factors={'mana':cost},**fields)
def rider(key,attachment,element,plan,cost=1.12,seeds=(),**fields):
 return add(key,ROUTES[key]['effect'],attachment=attachment,effects=plan,seeds=list(dict.fromkeys([element,'time',*seeds])),traits=[mod(element,1.15),mod('time',1.1)],cost_factors={'mana':cost},**fields)

numeric('reaching','range',1.3)
numeric('widening','area',1.2,1.15)
numeric('focused','amplify',1.12,1.12)
# A degree changes the existing metal/motion trait; a shared delivery parameter consumes it.
add('piercing','Extra native projectile penetration, limited to four pierces.',capabilities=['projectile'],kinds=['projectile'],defaults={'pierce':1},parameters={'pierce':clamp(trait('metal'),1,4)},seeds=['metal'],traits=[mod('metal',2)],cost_factors={'mana':1.3},max_degree=2)
add('seeking','Native projectiles steer toward an eligible target found at release.',capabilities=['projectile'],kinds=['projectile'],defaults={'homing':.08},parameters={'homing':clamp(trait('motion'),1,3)},seeds=['motion'],traits=[mod('motion',.35,'ADD')],cost_factors={'mana':1.15})
add('forked','Increase a native projectile fan; existing hit and rider budgets remain shared.',capabilities=['projectile'],kinds=['projectile'],defaults={'count':1},parameters={'count':clamp(trait('motion'),1,4)},seeds=['motion'],traits=[mod('motion',2),mod('amplify',.85)],cost_factors={'mana':1.4},max_degree=3)
add('enduring','Extend finite supported manifestations without independently extending event bindings.',lifetime=True,seeds=['time'],traits=[mod('time',1.2)],cost_factors={'mana':1.12})
add('patient','Twenty extra charge ticks per contribution reduce mana payment.',costs=[{'type':'time','ticks':20}],cost_factors={'mana':.9})
add('hurried','Shorter charge at a higher mana price.',cost_factors={'time':.85,'mana':1.12},capabilities=[],max_degree=4)
add('delayed','A visible cue precedes a two-second delayed release.',attachment='before',effects=[cue('e49d45','ring')],delay=40,cost_factors={'mana':.9},max_degree=1)
rider('bleeding','damage','blood',[cue('ae365a'),delay(),repeat(3,[damage(.75,'blood')])])
rider('shocking','damage','lightning',[cue('79dfff','arc'),damage(1.5,'lightning')])
rider('kindled','damage','fire',[cue('f4963e'),action('ignite',{'seconds':clamp(product(6,trait('fire'),trait('time')),1,12)})])
rider('chilling','damage','ice',[cue('b0e7ff'),status('slowness',40),action('freeze',{'ticks':clamp(product(30,trait('ice')),0,80)})])
rider('freezing','damage','ice',[cue('84d7fa'),action('freeze',{'ticks':clamp(product(120,trait('ice')),0,250)})],1.16)
rider('hindering','damage','motion',[status('slowness',100),cue('8e91ee')])
rider('envenomed','damage','poison',[cue('81b942'),status('poison',80)])
rider('interrupting','damage','sonic',[cue('dbccaa','ring'),action('interrupt')],1.2,max_degree=1)
rider('marking','damage','light',[status('glowing',120),cue('ebd48c')])
rider('repelling','damage','force',[cue('b2babf'),action('knockback',{'strength':clamp(product(.45,trait('force')),0,1.5),'up':.1})])
rider('drawing','damage','motion',[cue('ad74cd','ring'),action('pull',{'strength':clamp(product(.35,trait('motion')),0,1.2),'up':0})])
rider('lifting','damage','air',[cue('d3e8ed'),action('launch',{'strength':0,'up':clamp(product(.35,trait('air')),0,.8)})])
rider('unbalancing','damage','force',[status('weakness',40),action('knockback',{'strength':clamp(product(.2,trait('force')),0,.6),'up':.05})],1.08)
rider('echoing','damage','amplify',[cue('a1b8e8','ring'),delay(),action('damage',{'amount':clamp(product({'fact':'vestige:last_damage'},.25,trait('amplify')),0,4),'ignore_invulnerability':1})],1.2)
rider('mending','heal','life',[cue('93e7bb'),delay(),repeat(3,[action('heal',{'amount':clamp(product(.75,trait('life'),trait('amplify')),0,3)})])])
rider('restoring','heal','life',[cue('e3b6fa'),action('heal',{'amount':clamp(product(2,trait('life'),trait('amplify')),0,4)})],1.15)
rider('cleansing','heal','life',[cue('a9e7ca'),*[action('remove_status',effect='minecraft:'+e) for e in ['poison','wither','slowness']]],1.16,max_degree=1)
rider('purifying','heal','holy',[cue('ecd698'),*[action('remove_status',effect='minecraft:'+e) for e in ['blindness','darkness','weakness']]],1.14,max_degree=1)
rider('invigorating','heal','motion',[cue('c0ec91'),status('speed',100)])
add('quieting','Suppress authored casting cues that own a sound.',quiet=True,cost_factors={'mana':1.08},max_degree=1)
add('veiled','A compatible non-damaging utility cast grants brief invisibility.',attachment='before',effects=[{'type':'for_each','target':{'selection':'self'},'effects':[status('invisibility',60)]}],capabilities=['inspect_item','detect_magic','unlock','gather_items'],seeds=['time'],traits=[mod('time',1.1)],cost_factors={'mana':1.12})
for key,kind,axis,mult in [('excavating','break_blocks','amplify',1.15),('gathering','gather_items','range',1.2),('shaping','shape_stone','range',1.2)]:
 numeric(key,axis,mult,capabilities=[kind])
add('warded','Increase an existing native guard budget and per-hit allowance.',kinds=['guard'],capabilities=['guard'],parameters={'budget':trait('abjuration'),'per_hit':trait('abjuration')},seeds=['abjuration'],traits=[mod('abjuration',1.15)],cost_factors={'mana':1.12})
add('absorbing','Increase an existing finite guard damage budget.',kinds=['guard'],capabilities=['guard'],parameters={'budget':trait('abjuration')},seeds=['abjuration'],traits=[mod('abjuration',1.3)],cost_factors={'mana':1.18})
add('conjuring','Increase explicitly authored construct or summon health.',kinds=['construct','summon'],capabilities=['construct','summon'],parameters={'health':trait('conjuration')},seeds=['conjuration'],traits=[mod('conjuration',1.15)],cost_factors={'mana':1.12})
add('revealing','After detection or illumination, briefly outline up to eight nearby invisible creatures per cast without removing their invisibility.',attachment='after',after_actions=['detect_magic'],after_statuses=['minecraft:glowing'],effects=[action('reveal_hidden',{'radius':clamp(product(4,trait('light'),trait('range')),1,16),'duration':clamp(product(80,trait('time')),20,200),'count':8})],seeds=['light','range','time'],traits=[mod('light',1.15),mod('time',1.1)],cost_factors={'mana':1.2},max_degree=2)
add('anchored','Freeze a moving field whose actual pulses query around its own position; target-locked and already stationary fields are incompatible.',anchor=True,cost_factors={'mana':1.08},max_degree=1)
add('reflecting','Add a three-second protective halo that returns up to two incoming vanilla arrows or tridents; native spell deliveries are excluded.',attachment='protection',capabilities=['guard','barrier','reduce_pending_damage','defer_pending_damage'],effects=[{'type':'create_manifestation','target':{'selection':'current'},'manifestation':{'kind':'status','duration':60,'interval':1,'values':{},'identifiers':{},'bindings':[],'on_hit':[],'on_end':[],'on_tick':[action('reflect_projectiles',{'radius':2,'count':clamp(product(2,trait('abjuration')),1,4)})],'visual':{'duration':60,'radius':1,'height':1,'layers':[{'shape':'shield','color':'a4e8f4','alpha':.5,'width':.035,'scale':1}]}}}],seeds=['abjuration'],traits=[mod('abjuration',1.15)],cost_factors={'mana':1.25},max_degree=2)
numeric('frugal','amplify',.85,.8)
numeric('reckless','amplify',1.15,1.02)['traits'].append(mod('volatile',1,'ADD'))
numeric('votive','amplify',1.12,1.02)['costs']=[{'type':'material','item':'minecraft:coal','operation':'consume','amount':1}]
numeric('greedy','amplify',1.15,1.02)['costs']=[{'type':'material','item':'minecraft:emerald','operation':'consume','amount':1}]
numeric('sacrificial','amplify',1.1,1.02)['costs']=[{'type':'material','item':'minecraft:iron_pickaxe','operation':'damage','amount':4}]
numeric('famished','amplify',1.1,1.02)['costs']=[{'type':'hunger','amount':1}]
numeric('bloodbound','amplify',1.05,1.02)['health_exchange']=.25
add('fasting','Exchange a quarter of adjusted mana for one food point per 7.5 mana, rounded up.',hunger_exchange=.25,cost_factors={'mana':1.02},max_degree=3)
numeric('exhausting','amplify',1.25,1.35)['description']='Increase Amplify by 25% per degree at 35% more mana per degree.'
numeric('charged','amplify',1.15,1.1)['costs']=[{'type':'time','ticks':15}]

# These complete paired patterns replace their constituent individual augments.
rider('vampiric','damage','blood',[cue('b73b76'),delay(),repeat(3,[damage(.75,'blood'),action('leech',{'fraction':clamp(product(.5,trait('life')),0,.75),'maximum':1})])],1.22,seeds=['life'],compound=True,max_degree=2)
rider('hemorrhagic','damage','blood',[cue('d04158'),delay(),repeat(2,[damage(.6,'blood')]),delay(40),damage(2,'blood')],1.22,compound=True,max_degree=2)
rider('tempestuous','damage','lightning',[cue('86dbef','arc'),damage(2,'lightning'),action('knockback',{'strength':clamp(product(.5,trait('air')),0,1.2),'up':.2})],1.24,seeds=['air'],compound=True,costs=[{'type':'time','ticks':10}],max_degree=2)

def field(element,plan,color):
 return {'type':'create_manifestation','target':{'selection':'current'},'manifestation':{'kind':'area','duration':65,'interval':20,'values':{'radius':clamp(product(2,trait('area')),1,4)},'identifiers':{},'bindings':[],'on_hit':[],'on_end':[],'on_tick':[{'type':'for_each','target':{'selection':'near_target','distance':clamp(product(2,trait('area')),1,4),'relationship':'hostile','options':{'count':4}},'effects':[{'type':'limited','group':'vestige:spellshaping_contacts/'+element,'per_target':2,'total':8,'effects':[cue(color),*plan]}]}]}}
rider('glacial','damage','ice',[field('glacial',[status('slowness',40),action('freeze',{'ticks':clamp(product(40,trait('ice')),0,100)})],'83dafa')],1.25,seeds=['area'],compound=True,max_degree=2)
rider('stormbound','damage','lightning',[field('stormbound',[damage(1,'lightning')],'8cdbfa')],1.28,seeds=['area'],compound=True,max_degree=2)
rider('sustaining','heal','life',[cue('bce997'),delay(),repeat(5,[action('heal',{'amount':clamp(product(.75,trait('life'),trait('amplify')),0,3)})])],1.08,compound=True,costs=[{'type':'hunger','amount':2}],max_degree=2)

# Calibrate mining and protection through their actual shared native consumers.
by_id={r['id']:r for r in RULES}
exc=by_id['excavating'];exc.pop('reads');exc['capabilities']=['break_block','break_blocks'];exc['kinds']=['break_block','break_blocks'];exc['parameters']={'hardness':trait('metal')};exc['seeds']=['metal'];exc['traits']=[mod('metal',1.25)];exc['description']='Increase the supported mining hardness threshold.'
def ward(key,budget,charges):
 return {'type':'create_manifestation','target':{'selection':'current'},'manifestation':{'kind':'status','duration':80,'values':{},'identifiers':{},'bindings':[{'id':'vestige:spellshaping/'+key+'/guard','duration':80,'charges':charges,'triggers':[{'id':'vestige:spellshaping/'+key+'/guard_hit','event':'vestige:damage_calculating'}],'effects':[action('reduce_pending_damage',{'amount':clamp(product(budget,trait('abjuration'),trait('amplify')),0,6)})]}],'on_hit':[],'on_tick':[],'on_end':[],'visual':{'duration':80,'radius':1,'height':1,'ends_with_bindings':True,'layers':[{'shape':'shield','color':'b5d8eb','alpha':.4,'width':.025,'scale':1}]}}}
for key,amount,charges in [('warded',1,2),('absorbing',4,1)]:
 r=by_id[key];r.pop('parameters');r.pop('kinds');r['attachment']='protection';r['capabilities']=['guard','barrier','reduce_pending_damage','defer_pending_damage'];r['effects']=[ward(key,amount,charges)];r['description']=f'After successful entity protection, add an owned four-second ward with {charges} charges and {amount} base mitigation per hit.'
# Only explicit same-flavor alternatives already accepted by native base recipes participate.
ALTERNATIVES={'minecraft:emerald':['irons_spellbooks:evocation_rune'], 'minecraft:blaze_rod':['irons_spellbooks:fire_rune'], 'minecraft:glistering_melon_slice':['irons_spellbooks:divine_pearl','irons_spellbooks:holy_rune'], 'minecraft:amethyst_shard':['irons_spellbooks:arcane_essence']}
for r in RULES:
 if not r.get('compound'):
  for p in list(r['pairs']):
   for other in ALTERNATIVES.get(p['offering'],[]):r['pairs'].append(dict(p,offering=other))

# Duplicate routes are authoring errors; there is no selection UI or random tie breaker.
seen={}
for r in RULES:
 if r.get('compound'):continue
 for p in r['pairs']:
  key=(p['offering'],p['material'])
  assert key not in seen,(key,seen.get(key),r['id'])
  seen[key]=r['id']
spells={p.stem:json.loads(p.read_text()) for p in (ROOT/'src/main/resources/data/vestige/ritual_recipes').glob('*.json')}
def candidates(r):
 needed=collections.Counter(p['offering'] for p in r['pairs']) if r.get('compound') else None
 out=[]
 for name,recipe in spells.items():
  available=collections.Counter(p['ingredient']['items'][0] for p in recipe['parts'])
  if (all(available[k]>=v for k,v in needed.items()) if needed else any(available[p['offering']] for p in r['pairs'])):out.append(name)
 return sorted(out)
for r in RULES:
 assert candidates(r),(r['id'],'No matching base ingredient recipe')
OUTPUT=ROOT/'src/main/resources/data/vestige/spellshaping_rules.json'
DOCUMENT=ROOT/'docs/spellshaping-recipes.md'
text=json.dumps({'version':1,'rules':RULES},indent=2)+'\n'
lines=['# Executable Spellshaping recipes','',f'{len([r for r in RULES if not r.get("compound")])} individual rules and {len([r for r in RULES if r.get("compound")])} compound rules. This ledger lists implemented rules only.','',
'Install each material in the Plinth **side socket** and place the paired base-recipe ingredient on **top**. Only block materials named in these rules are accepted as imbuements, including compound-only pieces; unsupported blocks reject without consumption. All 16 wool colors use the White Wool routes, and all 16 concrete colors use the White Concrete routes. Imbuement color affects neither Spellshaping nor Attunement keys; installed stacks and retained shard nodes keep their actual color. Carpets and concrete powder are separate unsupported materials. Keep the ordinary spell recipe unchanged. The Spellstone automatically compiles the result; incompatible outcomes reject safely before consumption. Materials stay installed. Accepted materials paired with an unmatched offering remain neutral.','',
'Duplicate compatible contributions resolve through normal trait ADD/MULTIPLY operations. Degree 1 has the ordinary name; degree 2 is Greater; degree 3 or higher is Grand. Rule-specific saturation limits reject before consumption. Complete compounds consume their matching local pieces and emit one named plan instead of their constituent augments. Larger patterns match first; equal-size patterns use stable identifier order. Whole quarter-turns preserve local associations; moving an ingredient to another socket can change the result.','',
'Added damage/healing contact riders require positive actual primary damage or healing. Detection/illumination extensions run after their supported primary operation; protection extensions attach to its actual living recipient. Each rule permits one initial contact per recipient and eight per cast, across all callbacks and pulses. Secondary outcomes retain lineage, cannot retrigger a Spellshaping rider, and retain their original contact facts through delays. Secondary fields also have shared finite contact budgets. Geometry still applies separately and missing consumers remain absent.','',
'Added trait consumers are calibrated against the base spell rating (at least one), and seed missing traits explicitly. Base trait ratios and published definitions stay unchanged. All final costs and gameplay amounts round once; health payments use whole hearts. Mana exchange uses the price after typed mana adjustments, then one common layout multiplier. Bloodbound/Fasting share at most 75% exchanged mana; one heart per thirty exchanged mana or one food point per 7.5 exchanged mana, rounded up. The owner-accepted valuation is 1 heart = 2 full hunger icons (4 food points) = 30 XP points = 30 mana.','',
'Core routes use vanilla materials. Explicit Arcane Essence/Amethyst, Evocation Rune/Emerald, Fire Rune/Blaze Rod and Divine Pearl or Holy Rune/Melon alternative offerings participate when an installed mod and the base ingredient recipe accept them. Iron quality values do not affect these rules.','',
'Ingredient examples below are candidate base recipes; the native compiler additionally checks executable compatibility. A matching ingredient name alone never grants an incompatible effect.','',
'| Augment | Offering / installed material | Outcome | Candidate ingredient recipes |','| --- | --- | --- | --- |']
for r in RULES:
 pairs='; '.join(p['offering']+' / '+p['material'] for p in r['pairs'])
 lines.append('| '+('**'+r['name']+'**' if r.get('compound') else r['name'])+' | '+pairs+' | '+r['description']+' | '+', '.join(candidates(r)[:4])+' |')
doc='\n'.join(lines)+'\n'
parser=argparse.ArgumentParser();parser.add_argument('--check',action='store_true');args=parser.parse_args()
if args.check:
 assert OUTPUT.read_text()==text,'Spellshaping data drift'
 assert DOCUMENT.read_text()==doc,'Spellshaping documentation drift'
else:OUTPUT.write_text(text);DOCUMENT.write_text(doc)
print(f'{len(RULES)} Spellshaping rules; automatic pairings unique; all have candidate ingredient recipes')
