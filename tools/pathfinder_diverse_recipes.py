"""36 explicit native utility recipes. No source-name branches exist in the runtime."""
from spell_authoring import *
import spell_visuals as v

MODES = {}

def mesh(duration,color,shape='box',radius=1,alpha=.5):
    return v.visual(duration,radius,v.layer(shape,color,alpha,width=.08 if shape=='rain' else .04),v.layer('sparks',color,.5),height=0)

def zone(behavior,duration,radius,color,shape='sphere',**values):
    opacity={'wave':.35,'rain':.75}.get(shape,.12)
    return manifest('zone',duration,ids={'behavior':'vestige:'+behavior},radius=area(radius),particles=0,
                    visual=mesh(duration,color,shape,area(radius),opacity),**values)

def construct(duration,color,behavior='none',**values):
    return manifest('construct',duration,ids={'behavior':'vestige:'+behavior},visual=mesh(duration,color),**values)

def mobile(behavior,duration,color,**values):
    return manifest('mobility',duration,ids={'behavior':'vestige:'+behavior},visual=v.ward(duration,color,False),**values)

def sensor(behavior,duration,color,**values):
    return manifest('sensor',duration,ids={'behavior':'vestige:'+behavior},visual=v.field(duration,.7,color),**values)

def register(recipe):
    recipe('wall_of_stone',[aim(20,construct(300,'a8b0b8',width=area(7),height=3,depth=.5,solid=1,health=amp(40)))],['earth','stone','conjuration','abjuration'],
           'Raise a seven-by-three solid, 40-HP stone panel for fifteen seconds.',
           'One rectangular entity collision panel replaces arbitrary permanent stone terrain; finite durability/lifetime, occupied placement rejected and no block drops. It also blocks native sight targeting.')
    ice=manifest('block_wall',200,width=area(5),height=3,depth=1,rise_ticks=24,collapse_ticks=20,
                 visual=v.visual(28,area(2.5),v.layer('sparks','d7f5ff',.95,scale=1.5),height=.1))
    ice_start=each(target('aimed_position',rng(18)),ice,visual=v.field(24,area(2.5),'abe9ff'))
    recipe('wall_of_ice',[ice_start],['ice','conjuration','abjuration'],
           'Raise a five-by-three wall of individually breakable ice blocks, lifting occupants as rows emerge in a frost burst. After ten seconds, surviving conjured blocks sink and shatter.',
           'A temporary one-block-thick wall replaces the former HP slab and fracture damage. Width scales with area up to nine blocks; loaded supported air cells and safe lifting headroom are required. Broken or replaced cells permanently lose ownership; expiry, dispel and unload preserve player replacements. Conjured ice yields no items or melting water.')
    water=manifest('zone',200,ids={'behavior':'vestige:water','formation':'vestige:water'},radius=area(3),width=area(5),height=3,
                   visual=v.visual(28,area(2.5),v.layer('splash','9de4ff',.95,scale=1.5),height=.1))
    recipe('wall_of_water',[aim(18,water)],['water','abjuration','conjuration'],
           'Raise a five-by-three traversable wall of real conjured water for ten seconds, extinguishing creatures and slowing physical projectiles in its cells.',
           'Water uses native water rendering and immersion physics, confined to owned cells rather than spreading source blocks. Supported loaded air is required; broken or replaced cells lose their claims. Expiry drains surviving water, preserving player replacements. No harvested water; native spell projectiles bypass the additional drag.')
    recipe('illusory_object',[aim(16,construct(200,'c7a8ef',width=3,height=2,depth=2,health=1,solid=0))],['illusion','dream'],
           'Show a stationary, non-solid object silhouette for ten seconds.',
           'An original translucent crate-sized preset replaces unrestricted object modeling; collision and resources stay unchanged and an attack removes the fragile image.')
    recipe('wooden_double',[self_(manifest('guard',160,ids={'behavior':'vestige:heavy'},threshold=8,budget=amp(6),visual=v.ward(160,'ba9564')))],['wood','illusion','abjuration'],
           'The first hit of at least eight HP sheds up to six HP and attempts a safe sideways step.',
           'Automatic finite interception replaces a chosen reaction and critical-hit trigger. Overflow remains; failed relocation never grants extra mitigation.')
    tree=construct(200,'85bb72','protect',width=.7,height=3,depth=.7,solid=1,health=amp(16),radius=area(3),count=3)
    tree['manifestation']['identifiers']['formation']='vestige:tree'
    tree['manifestation']['visual']=v.field(200,area(3),'85bb72')
    recipe('protector_tree',[aim(12,tree)],['plant','wood','abjuration'],
           'Grow an oak tree from real temporary logs and leaves. Its sixteen-HP pool protects three consenting adjacent allies for ten seconds.',
           'Loaded supported air and room for the entire canopy are required. Recipients are captured on placement and must stay within three blocks. Each missing trunk removes a quarter of the maximum protection; damage spends the shared pool. Only surviving owned blocks retract at expiry; edits, replacements and holes are preserved. No log or leaf loot.')
    recipe('collective_transposition',[aim(16,action('transpose',radius=8,count=3))],['space','conjuration'],
           'Move three willing nearby allies into a supported formation at the aimed destination.',
           'Automatic fixed formation replaces independently chosen destinations. Every destination validates before any creature moves; players opt in through the development consent control.')
    recipe('zephyr_slip',[self_(zone('slip',120,2,'d9f9e9','ring',distance=3))],['air','motion','abjuration'],
           'The first enemy entering a two-block boundary triggers a safe three-block retreat away from it.',
           'Finite proximity entry replaces a chosen reaction; no damage immunity and an obstructed retreat keeps the effect waiting until expiry.')
    recipe('share_life',[ray(12,manifest('guard',160,ids={'behavior':'vestige:share'},budget=amp(8),range=rng(16),visual=v.ward(160,'fc9bae',False)),relationship='ally')],['life','blood','abjuration'],
           'Transfer half an ally’s incoming damage to yourself, up to eight actual HP total, while preserving one HP.',
           'Only actual caster health/absorption loss reduces the original pending hit. Transfer cannot recursively transfer again; same dimension, consent and distance required.')
    unseen=sensor('unseen',200,'a9dcff',radius=rng(16),private_visual=1)
    recipe('see_the_unseen',[self_(unseen)],['light','divination'],
           'Privately outline nearby invisible creatures for ten seconds.',
           'Caster-only silhouettes replace tabletop concealed checks; no invisibility removal or public glowing. Native perception boundaries still apply.')
    recipe('freezing_rain',[aim(18,zone('rain',160,4,'b7e4fa','rain'))],['ice','water','conjuration'],
           'An eight-second rain patch makes moving grounded creatures slide and obscures native perception across its edge.',
           'A velocity traction approximation and scoped creature rendering replace general weather, tabletop concealment and all block visibility; no terrain freezing or damage.')
    plant=construct(300,'83c574','heal',width=.5,height=1,depth=.5,health=8,budget=amp(6),amount=1,radius=area(4),count=3)
    plant['manifestation']['visual']=mesh(300,'83c574','tree',.65,.7)
    fungus=construct(300,'c6a9e3','protect',width=.6,height=1.2,depth=.6,health=amp(8),radius=area(3),count=3)
    recipe('summon_plant_or_fungus',[aim(12,plant)],['plant','life','conjuration'],
           'Summon a stationary healing plant with six shared healing HP, or a fungus with eight shared guard HP.',
           'Two original support presets replace a bestiary. No melee minion AI; actual healed HP spends the plant budget, and destruction/expiry ends support.')
    MODES['summon_plant_or_fungus']={'fungus':[aim(12,fungus)]}
    fey=construct(240,'e8bbfa','debilitate',width=.4,height=1.4,depth=.4,health=6,budget=4,radius=area(4))
    fey['manifestation']['visual']=mesh(240,'e8bbfa','body',.45,.18)
    recipe('summon_fey',[aim(12,fey)],['mind','illusion','conjuration'],
           'A fragile fey support silhouette spends four tricks briefly slowing nearby enemies.',
           'A stationary four-use support role replaces bestiary selection, spell lists and independent initiative. No copied creature model or vex melee reskin.')
    earth=construct(240,'bc9977',width=2,height=2,depth=1,solid=1,health=20)
    elemental_modes={
        'air':[aim(12,construct(240,'d3f8f1','intercept',width=.6,height=2,depth=.6,health=8,radius=3))],
        'water':[aim(12,construct(240,'6fc3f3','extinguish',width=.6,height=2,depth=.6,health=12,radius=4))],
        'fire':[aim(12,construct(120,'ff954d','pressure',width=.6,height=2,depth=.6,health=8,radius=3,budget=amp(12),amount=2))]}
    recipe('summon_elemental',[aim(12,earth)],['earth','air','water','fire','conjuration'],
           'Choose earth cover, air projectile drag, water firefighting or a fire-pressure body with twelve shared damage HP.',
           'Four original, stationary elemental roles replace full summoned creature stat blocks. Every mode shares native costs and lifetime/budget constraints; no bestiary or automatic source-rank scaling.')
    MODES['summon_elemental']=elemental_modes
    recipe('create_water',[each(target('block_ray',rng(12)),action('create_water'))],['water','conjuration'],
           'Add one water layer to an aimed cauldron.',
           'A finite Minecraft utility volume replaces a gallon pool. Full cauldrons fail; no fluid source block, item duplication or damaging water attack.')
    cover=construct(400,'b58f60',width=3,height=1,depth=.5,solid=1,health=amp(16))
    recipe('creation',[aim(12,cover)],['wood','plant','conjuration'],
           'Assemble a temporary wooden cover, step or platform for twenty seconds.',
           'Finite presets replace arbitrary mundane objects. Geometry has collision and durability but produces no inventory items, containers or loot.')
    MODES['creation']={'step':[aim(12,construct(400,'b58f60',width=2,height=.5,depth=2,solid=1,health=amp(16)))],
                       'platform':[aim(12,construct(400,'b58f60',width=3,height=.25,depth=3,solid=1,health=amp(16)))]}
    recipe('enlarge',[self_(mobile('scale',160,'efa77a',factor=1.5))],['polymorph','transmutation'],
           'Become physically one-and-a-half size for eight seconds, with one extra block of player reach.',
           'Actual synced body scale and explicit reach replace tabletop size categories and weapon damage bonuses; occupied growth is rejected.')
    recipe('shrink',[self_(mobile('scale',160,'adc8f6',factor=.5))],['polymorph','transmutation'],
           'Become physically half size for eight seconds, with reduced player reach.',
           'Actual body clearance replaces tabletop Tiny rules. Expiry removes only its owned modifiers and searches above an obstructed restoring body; no stealth immunity.')
    recipe('water_breathing',[self_(each(target('near_target',rng(6),'ally',False,count=3,consenting=1),status('water_breathing',600)))],['water','life','transmutation'],
           'Give up to three willing nearby allies thirty seconds of Water Breathing.',
           'Finite group environmental adaptation replaces long tabletop duration. Vanilla status merging preserves longer external Water Breathing effects.')
    recipe('water_walk',[self_(mobile('water_walk',160,'85def6'))],['water','motion','transmutation'],
           'Stand and move on nearby water surfaces for eight seconds; crouch to enter the water.',
           'Shared client/server contact support replaces fluid collision hooks. It does not lift submerged bodies or provide airborne flight; source/flowing-water heights are sampled.')
    recipe('gecko_grip',[self_(mobile('climb',160,'9ee59b'))],['plant','motion','transmutation'],
           'Climb when touching a wall for eight seconds; crouch to release the grip.',
           'Client/server wall-contact velocity replaces arbitrary surface adhesion and ceiling crawling; no general flight.')
    recipe('magic_passage',[each(target('block_ray',rng(16)),manifest('passage',240,depth=3,visual=mesh(240,'bbd1e9','wave',1,.18)))],['space','stone','transmutation'],
           'Open a two-high, three-deep ordinary-stone passage for twelve seconds, then restore unchanged air cells.',
           'Fixed tunnel geometry replaces arbitrary Pathfinder passage dimensions. Ores/containers are excluded; exit and permissions validate first, occupants move to a checked exit before restoration.')
    recipe('shape_stone',[each(target('block_ray',rng(12)),store('first_anchor')),wait(100),each(target('aimed_position',rng(12)),action('shape_stone'))],['earth','stone','transmutation'],
           'Capture one ordinary stone cell; recast within five seconds to move it to a nearby empty destination.',
           'One permanent volume-conserving cell move replaces freeform reshaping. At most six blocks between ends; no ore, containers, drops, occupied destination or invented stone.')
    recipe('item_facade',[self_(sensor('facade',200,'d5c0ff'))],['illusion','transmutation'],
           'Overlay an illusory pristine-condition label and held-item glimmer for ten seconds.',
           'The initial native facade is a clearly labeled cosmetic tooltip/glimmer, not a full model replacement. Actual item identity, durability, enchantments and inventory data remain authoritative.')
    recipe('silence',[aim(12,zone('silence',160,3,'b9b8d2','ring'))],['sonic','abjuration'],
           'Suppress positional sounds and explicitly vocal native delivery within a three-block region for eight seconds.',
           'Scoped sound emission/playback and an executable utterance gate replace tabletop verbal components. Existing ambient loops and non-native spell systems are outside the gate.')
    recipe('mirror_image',[self_(manifest('guard',160,ids={'behavior':'vestige:images'},budget=3,per_hit=amp(5),visual=v.field(160,1.6,'cbc0fc')))],['illusion','light'],
           'Three copies of the caster’s live model and equipment each absorb up to five HP of an eligible direct incoming attack.',
           'Finite deterministic mitigation replaces random target redirection. Projectiles, environmental fire and explosions bypass this initial direct-melee form; images do not block area damage.')
    recipe('time_jump',[self_(mobile('absence',20,'c7c9ff'))],['time','space','transmutation'],
           'Briefly step out of time for one second, anchoring the body and suppressing attacks, uses and incoming damage.',
           'A bounded one-second absence replaces advancing the whole world’s time. No global time stop, movement during absence, resource regeneration or guaranteed tabletop action economy.')
    recipe('gravity_well',[aim(18,manifest('area',1,particles=0,on_tick=[near(4,action('pull',strength=amp(.7),up=0),count=4)],interval=1)),aim(18,{'type':'visual','visual':v.field(16,area(4),'ac9ee9')})],['force','space','motion'],
           'Collapse the formation of at most four enemies toward one aimed anchor with a single inward impulse.',
           'One zero-damage native impulse replaces Reflex saves and forced grid distance; no persistent vortex or repeating damage.')
    recipe('repulsion',[self_(zone('repel',120,3,'d4c2fa','sphere'))],['force','abjuration'],
           'A six-second boundary rejects enemies crossing inward, while allies and outgoing movement pass.',
           'A stationary boundary replaces save-based aura movement restrictions. Creatures already inside remain; ranged attacks pass. Position changes crossing the sampled boundary are rejected at the next tick.')
    recipe('containment',[ray(14,zone('containment',100,2,'a7ccff','sphere',health=amp(16)))],['force','abjuration'],
           'Enclose the aimed enemy within a two-way movement boundary for five seconds; outside hits spend a shared sixteen-HP pool.',
           'Movement boundary and finite outside-attack durability replace an invulnerable tabletop sphere. It is traversable after breaking; inside attacks and terrain are not blocked.')
    recipe('magnetic_attraction',[self_(action('gather_items',radius=rng(8),count=8))],['metal','motion','transmutation'],
           'Pull up to eight nearby loose metal stacks toward your hand.',
           'A concrete ingot/nugget/raw-metal whitelist replaces arbitrary metal-material rules. Existing item entities/stacks remain intact; no equipped-item theft or copying.')
    recipe('pet_cache',[each(target('entity_ray',rng(12),'any',True),manifest('pet_cache',200,visual=v.field(200,.8,'b8cdf8')))],['space','life','conjuration'],
           'Shelter your existing tame companion for ten seconds, returning the same UUID and inventory afterward.',
           'Owned non-riding, non-leashed companions only. Uses an isolated private-dimension cell and persistent emergency-return journal; an unavailable private world rejects the cast.')
    recipe('clairvoyance',[aim(20,sensor('camera',160,'c2e9ff'))],['divination','space'],
           'See through a stationary sensor up to twenty blocks away for eight seconds; damage immediately ends the view.',
           'Client camera changes while the server body stays vulnerable. Fixed sensor facing and normal tracked chunks replace arbitrary remote locations/panning; casting and interaction are blocked while viewing.')
    recipe('status',[self_(sensor('status',300,'9fe7d6',radius=rng(8),count=3))],['life','divination'],
           'Privately monitor three consenting nearby allies’ coarse health, affliction count, distance and direction for fifteen seconds.',
           'Recipients captured at cast time; same-world loaded bodies only. No through-boundary public glowing, exact private health display or universal creature tracking.')
    recipe('air_bubble',[self_(manifest('guard',100,ids={'behavior':'vestige:air'},budget=1,visual=v.ward(100,'aedff6',False)))],['air','life','abjuration'],
           'Breathe through water or suffocation until safe air resumes, with a five-second maximum.',
           'A self-cast conditional rescue replaces a reaction targeting another creature. Only drowning/suffocation is prevented; ordinary attacks are unaffected.')
    recipe('peaceful_bubble',[self_(zone('privacy',200,4,'d6d9c3','veil'))],['dream','illusion','abjuration'],
           'A traversable ten-second bubble conceals creatures across its edge from native targeting and rendering.',
           'Scoped creature privacy replaces universal sound, weather, scrying and shelter rules. No damage prevention, solid wall, private-room teleport or general sleep guarantee.')
