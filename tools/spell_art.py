"""Per-spell art direction, resolved by the authoring tools into ordinary visual layers.

These are reviewed choices, never a hash, runtime spell switch or interpretation of
traits. Shape, secondary motif, rhythm and density distinguish related spells.
"""
from dataclasses import dataclass
from copy import deepcopy
import spell_visuals as v

PALETTES = {
    'blood': ('c92551', 'ffd0b7'), 'ember': ('f85b25', 'ffe4a1'),
    'sun': ('ffd45a', 'fffbe1'), 'frost': ('48badf', 'e7fbff'),
    'storm': ('5594ff', 'e5f4ff'), 'violet': ('b67cff', 'f4deff'),
    'void': ('57418f', 'd992ff'), 'leaf': ('4bba6b', 'd7ff9d'),
    'venom': ('8bac29', 'e0ff8d'), 'earth': ('af8057', 'f2d3a6'),
    'water': ('239dcc', 'c2f4ff'), 'spirit': ('57d0c7', 'e2fff3'),
    'silver': ('99aabf', 'eef5ff'), 'rose': ('d98bd0', 'fff0fa'),
    'rust': ('bf663d', 'dfb075'), 'sculk': ('216278', '53ead3'),
}

@dataclass(frozen=True)
class Art:
    motif: str
    accent: str
    palette: str
    count: int
    speed: float
    scale: float

# id, silhouette, secondary motion, palette, density, signed rhythm, footprint multiplier.
# Similar schools share an artistic vocabulary, but do not share a whole composition.
DIRECTIONS = """
abyssal_shroud vortex tendrils void 5 -0.65 1.15
acid_orb motes ripple venom 7 1.25 1.6
acupuncture shards helix blood 5 1.35 1.25
angel_wing wings rays sun 10 0.5 1.4
arcane_lock sigil chain violet 7 0.35 1.0
arcane_shackle chain helix violet 9 -0.7 1.2
arrow_volley shards rays silver 12 1.8 0.9
ascension helix rays storm 8 1.8 1.45
ball_lightning helix motes storm 6 1.5 1.25
black_hole vortex rays void 6 -1.25 0.85
blaze_storm flare vortex ember 12 1.2 1.0
blessing_of_life rays motes sun 8 0.65 1.1
blight tendrils motes venom 7 -0.4 1.05
blizzard shards vortex frost 18 1.3 1.0
blood_needles shards rays blood 7 1.75 0.9
blood_slash slash helix blood 8 1.3 2.6
blood_step slash motes blood 6 -1.3 1.2
burning_dash flare helix ember 8 2.0 1.1
chain_creeper rays shards venom 10 1.6 1.0
chain_lightning helix rays storm 5 2.3 1.0
charge helix sigil storm 5 1.6 1.05
cleanse rays leaves sun 12 1.3 1.2
cone_of_cold shards ripple frost 11 1.2 1.5
counterspell sigil slash violet 9 -1.7 1.4
devour fangs helix blood 6 0.9 1.5
divine_smite slash rays sun 5 1.8 1.7
dragon_breath tendrils flare violet 7 1.7 1.45
earthquake shards sigil earth 10 -0.6 1.0
echoing_strikes slash motes violet 6 1.6 1.15
eldritch_blast tendrils helix void 5 -2.0 1.7
electrocute helix ripple storm 9 2.4 1.4
evasion motes vortex violet 4 1.8 1.1
fang_strike fangs shards silver 8 1.2 2.0
fang_swirl fangs vortex silver 16 1.1 1.3
fang_ward fangs sigil silver 12 -0.5 1.8
fire_arrow flare rays ember 5 2.2 1.0
fire_breath flare helix ember 11 1.6 1.4
fireball flare rays ember 12 1.4 1.25
firebolt flare motes ember 5 2.1 0.85
firecracker rays motes rose 16 1.9 1.2
firefly_swarm motes wings sun 18 1.65 1.1
flaming_barrage flare sigil ember 8 1.9 1.15
flaming_strike slash flare ember 6 1.6 1.4
force_arrow shards helix violet 4 2.2 1.0
fortify shield sigil sun 5 0.35 1.2
frost_step shards slash frost 7 -1.3 1.1
frostbite shards helix frost 6 0.6 1.15
frostwave shards ripple frost 16 1.8 1.0
gluttony leaves helix leaf 6 0.8 0.95
gravity_fissure vortex helix void 4 -1.5 1.1
greater_heal wings rays spirit 9 0.7 1.3
guiding_bolt rays sigil sun 6 1.3 1.3
gust ripple leaves spirit 5 2.1 1.5
haste clock helix sun 8 1.8 0.85
heal leaves rays spirit 8 0.7 1.0
healing_circle sigil rays sun 12 0.5 1.0
heartstop clock helix blood 4 -0.25 1.15
heat_surge flare rays ember 14 1.5 1.1
ice_block shards ripple frost 6 0.5 1.85
ice_spikes shards fangs frost 8 1.3 2.0
ice_tomb shards shield frost 6 0.15 1.4
icicle shards helix frost 3 2.3 1.4
interposing_earth shards shield earth 5 0.3 1.25
invisibility motes vortex violet 5 -1.1 0.8
lightning_bolt rays helix storm 12 2.5 1.2
lightning_lance shards helix storm 4 2.5 1.2
lob_creeper motes rays venom 6 0.7 1.4
magic_arrow shards sigil violet 3 2.1 1.6
magic_missile helix motes violet 5 1.8 1.25
magma_bomb flare shards ember 7 0.75 1.6
oakskin leaves shield earth 9 0.35 1.1
pf2_air_bubble ripple motes water 3 0.4 0.9
pf2_arctic_rift shards slash frost 12 1.5 1.4
pf2_bind_undead chain sigil void 6 -0.5 1.0
pf2_breathe_fire flare ripple ember 9 1.7 1.55
pf2_cataclysm shards flare sun 16 1.8 1.2
pf2_caustic_blast ripple motes venom 12 1.2 1.1
pf2_chain_lightning helix rays storm 8 2.7 1.25
pf2_cinder_swarm motes flare ember 20 2.1 1.1
pf2_clairvoyance eye sigil violet 8 0.5 0.8
pf2_collective_transposition helix sigil violet 12 -1.2 1.3
pf2_containment chain sigil silver 10 -0.3 1.05
pf2_create_water ripple motes water 6 0.85 1.2
pf2_creation sigil shards silver 6 0.6 1.0
pf2_detect_magic eye rays violet 6 1.1 1.2
pf2_divine_lance rays helix sun 5 1.9 1.5
pf2_eclipse_burst vortex rays void 12 -1.6 1.05
pf2_electric_arc helix motes violet 3 2.0 0.65
pf2_enfeeble tendrils helix void 4 -0.6 1.0
pf2_enlarge shards helix earth 8 0.6 1.0
pf2_falling_stars rays motes sun 14 1.7 1.2
pf2_false_vitality helix sigil blood 7 -0.45 1.05
pf2_fear eye tendrils void 5 -1.2 1.1
pf2_field_of_life leaves rays spirit 14 0.6 1.0
pf2_figment motes eye rose 7 0.65 1.0
pf2_fire_shield flare shield ember 6 0.85 1.1
pf2_fireball flare ripple ember 16 1.9 1.15
pf2_fleet_step helix leaves spirit 6 2.1 0.9
pf2_flicker vortex motes violet 3 -2.0 1.0
pf2_floating_flame flare helix ember 6 0.7 1.4
pf2_force_barrage shards helix violet 6 2.0 1.4
pf2_freezing_rain shards ripple frost 20 1.5 1.0
pf2_frostbite shards motes frost 5 0.9 1.0
pf2_gecko_grip leaves sigil leaf 5 0.5 0.85
pf2_gentle_breeze ripple leaves spirit 4 0.5 1.0
pf2_gentle_landing wings motes spirit 8 0.4 1.1
pf2_glass_shield shards shield silver 7 0.6 1.1
pf2_gouging_claw slash fangs blood 3 1.5 1.5
pf2_gravity_well vortex sigil void 8 -1.1 1.0
pf2_grease ripple motes sun 4 -0.35 1.0
pf2_grim_tendrils tendrils helix void 9 -1.3 1.4
pf2_harm tendrils rays void 6 -0.8 1.2
pf2_haste clock rays sun 6 2.0 1.0
pf2_heal wings leaves spirit 6 0.8 1.1
pf2_hydraulic_push ripple helix water 5 2.0 1.3
pf2_ignition flare slash ember 4 1.6 1.0
pf2_illusory_creature eye motes rose 6 0.9 1.1
pf2_illusory_object sigil eye rose 8 -0.65 1.0
pf2_invisibility motes ripple rose 7 -1.4 0.85
pf2_item_facade eye motes silver 4 0.4 0.7
pf2_lightning_bolt rays ripple storm 10 2.3 1.4
pf2_magic_passage sigil vortex earth 9 -0.85 1.0
pf2_magnetic_attraction helix shards silver 7 -1.8 1.2
pf2_mirror_image motes sigil rose 3 0.45 0.8
pf2_mud_pit ripple shards earth 6 -0.45 1.0
pf2_needle_darts shards motes silver 3 1.7 1.15
pf2_peaceful_bubble leaves ripple spirit 8 0.25 1.0
pf2_pet_cache sigil wings violet 4 -0.8 1.0
pf2_protection shield wings sun 4 0.4 1.05
pf2_protector_tree leaves rays leaf 12 0.3 0.85
pf2_puff_of_poison motes tendrils venom 9 -0.7 1.05
pf2_read_aura eye sigil sun 5 0.55 0.9
pf2_regenerate leaves helix spirit 10 0.6 1.05
pf2_repulsion ripple shield violet 6 1.1 1.0
pf2_resist_energy shield helix frost 8 0.45 1.1
pf2_revealing_light rays eye sun 9 1.2 1.2
pf2_rust_cloud motes shards rust 14 0.55 1.0
pf2_scatter_scree shards rays earth 9 1.6 1.1
pf2_see_the_unseen eye motes spirit 8 0.65 0.85
pf2_shape_stone shards sigil earth 7 -0.4 1.0
pf2_share_life helix leaves blood 4 0.65 1.0
pf2_shield shield sigil violet 6 0.6 1.0
pf2_shrink motes helix earth 5 -1.0 0.75
pf2_silence ripple sigil silver 3 -0.2 1.0
pf2_slashing_gust slash ripple spirit 5 2.0 1.5
pf2_slow clock chain violet 5 -0.4 1.0
pf2_soothe leaves motes rose 6 0.35 1.0
pf2_spirit_blast rays wings spirit 16 1.35 1.15
pf2_spiritual_armament slash sigil sun 7 1.25 1.4
pf2_spout ripple rays water 7 1.6 1.3
pf2_status eye helix spirit 4 0.6 0.8
pf2_summon_animal leaves sigil leaf 7 0.7 0.9
pf2_summon_elemental shards sigil frost 9 0.85 1.0
pf2_summon_fey wings motes rose 7 0.8 1.1
pf2_summon_plant_or_fungus tendrils leaves leaf 8 0.5 1.1
pf2_tangle_vine tendrils leaves leaf 5 -0.5 1.0
pf2_telekinetic_projectile shards ripple silver 4 1.5 1.0
pf2_thunderstrike rays helix storm 7 2.1 1.15
pf2_time_jump clock vortex violet 7 2.5 1.2
pf2_translocate vortex sigil violet 5 -1.8 1.0
pf2_vampiric_feast fangs helix blood 10 -0.9 1.35
pf2_vitality_lash helix leaves spirit 5 1.3 1.35
pf2_void_warp tendrils vortex void 4 -1.5 1.2
pf2_wall_of_ice shards motes frost 12 0.4 0.8
pf2_wall_of_stone shards sigil earth 6 0.25 0.8
pf2_wall_of_water ripple motes water 8 0.7 0.8
pf2_water_breathing ripple helix water 4 0.5 0.9
pf2_water_walk ripple leaves water 5 0.75 0.85
pf2_weapon_storm slash shards silver 16 1.8 1.25
pf2_wooden_double leaves sigil earth 4 0.45 0.9
pf2_zephyr_slip wings ripple spirit 5 1.4 1.0
planar_sight eye vortex void 12 0.65 0.1
pocket_dimension vortex sigil violet 10 -0.5 1.3
poison_arrow tendrils shards venom 5 1.8 1.0
poison_breath tendrils ripple venom 8 -1.4 1.3
poison_splash ripple tendrils venom 14 1.4 1.0
portal vortex sigil violet 7 -0.7 1.15
raise_dead sigil tendrils void 8 -0.75 0.9
raise_hell flare shards ember 10 1.6 1.4
ray_of_frost shards helix frost 9 1.9 1.25
ray_of_siphoning helix tendrils blood 6 -1.6 1.6
recall sigil rays violet 6 -0.9 1.2
root tendrils leaves earth 6 -0.35 1.2
sacrifice rays fangs blood 12 1.7 1.1
scapegoat motes sigil rose 8 1.1 0.9
scorch flare shards ember 8 1.2 1.3
sculk_tentacles tendrils vortex sculk 10 -0.75 1.05
shadow_slash slash vortex void 7 -1.4 1.6
shield shield sigil violet 8 0.3 1.1
shockwave ripple rays storm 8 2.0 1.15
slow clock helix violet 4 -0.25 1.0
snowball motes shards frost 8 0.8 1.5
sonic_boom ripple rays sculk 7 2.5 1.7
spectral_hammer shards slash violet 4 0.85 1.65
spider_aspect tendrils motes venom 8 0.85 1.05
starfall rays helix violet 10 1.8 1.3
stomp shards ripple earth 12 1.4 1.3
summon_ender_chest sigil motes violet 5 0.55 1.0
summon_horse wings sigil spirit 6 0.7 0.85
summon_polar_bear shards sigil frost 8 0.4 0.9
summon_swords slash motes violet 3 0.8 1.0
summon_vex wings sigil violet 5 1.0 0.9
summon_zombie sigil shards void 6 -0.65 0.9
sunbeam rays helix sun 12 1.25 1.8
telekinesis helix sigil violet 8 -0.6 1.2
teleport vortex motes violet 6 -1.4 1.0
throw shards rays silver 5 1.35 1.0
thunderstorm helix rays storm 14 1.6 1.0
touch_dig shards sigil earth 4 0.9 1.1
volt_strike slash helix storm 8 2.7 1.3
wall_of_fire flare sigil ember 16 0.8 1.0
wisp motes rays sun 5 0.75 1.15
wither_skull tendrils motes void 5 -0.7 1.45
wololo eye helix rose 9 0.7 1.0
"""
ART = {}
for row in DIRECTIONS.strip().splitlines():
    name, motif, accent, palette, count, speed, scale = row.split()
    assert name not in ART, name
    ART[name] = Art(motif, accent, palette, int(count), float(speed), float(scale))


def compose(base, art, *, role='', quiet=False):
    """Give a resolved cosmetic cue its authored silhouette without moving any gameplay anchors."""
    result = deepcopy(base)
    main, light = PALETTES[art.palette]
    old = result['layers']
    structural = {'arc', 'beam', 'jet', 'splash', 'shield', 'box', 'wave', 'rain', 'veil'}
    retained = [layer for layer in old if layer['shape'] in structural]
    # Bright path cores, fluid sheets and physical construct bounds remain readable.
    retained = retained[:3]
    if isinstance(result['radius'], (int, float)) and result['radius'] < .5 and role != 'projectile':
        result['radius'] = .85
    if role == 'projectile':
        retained = [v.layer('sphere', light, .7, scale=.5)]
    density = art.count
    thickness = {'vortex':.22, 'tendrils':.15, 'slash':.18, 'helix':.09, 'rays':.085}.get(art.motif,.045)
    if role == 'feedback':
        result['duration'] = max(18, result['duration'])
    if not quiet and result['duration'] < 24:
        result['duration'] = 24
    result['layers'] = retained + [
        v.layer(art.motif, main, .85, thickness, art.scale, speed=art.speed, count=density),
        v.layer(art.accent, light, .7, .04, art.scale*.7, speed=-art.speed*.65, phase=1.2, count=max(3,density//2)),
    ]
    # Glints support the authored form; they are never its entire silhouette.
    result['layers'].append(v.layer('sparks', light, .65, scale=.8))
    return result
