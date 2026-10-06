#!/usr/bin/env python3
"""Explicit native ritual recipes, informed by pinned Iron material uses rather than material quality."""
import argparse
from collections import Counter
from itertools import combinations
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
# Each row is deliberately authored: ID, four through eight seats, clockwise or counterclockwise,
# then every non-Paper component. There is no hash assignment or catch-all recipe.
RECIPES = """
abyssal_shroud 8 ccw echo ward obsidian phantom ink chorus amethyst
acid_orb 4 cw poison slime emerald
acupuncture 4 cw blood iron flint
angel_wing 5 cw holy feather phantom gold
arcane_lock 4 cw iron chain amethyst
arcane_shackle 6 ccw ender chain iron ward obsidian
arrow_volley 5 cw emerald arrow string feather
ascension 5 cw copper feather ender phantom
ball_lightning 5 cw copper slime emerald glow
black_hole 8 ccw ender echo obsidian chorus iron compass amethyst
blaze_storm 6 cw blaze firecharge powder feather emerald
blessing_of_life 4 cw holy carrot amethyst
blight 6 ccw poison blood rotten bone mushroom
blizzard 6 ccw snow ice feather powder emerald
blood_needles 4 cw blood iron arrow
blood_slash 4 ccw blood flint emerald
blood_step 4 ccw blood ender ink
burning_dash 4 cw blaze feather firecharge
chain_creeper 6 cw emerald powder slime chain flint
chain_lightning 4 cw copper chain emerald
charge 4 cw copper carrot feather
cleanse 4 ccw holy milk ward
cone_of_cold 6 cw snow ice feather emerald phantom
counterspell 5 ccw ender ward amethyst book
devour 6 ccw blood rotten holy bone bread
divine_smite 4 cw holy glow emerald
dragon_breath 6 cw ender dragon firecharge emerald obsidian
earthquake 6 ccw stone flint iron powder emerald
echoing_strikes 6 cw ender clock iron amethyst flint
eldritch_blast 6 ccw echo emerald obsidian powder amethyst
electrocute 6 cw copper iron chain emerald glow
evasion 4 ccw ender phantom ward
fang_strike 4 cw emerald bone flint
fang_swirl 4 ccw emerald bone slime
fang_ward 4 cw emerald bone ward
fire_arrow 4 cw blaze arrow emerald
fire_breath 5 cw blaze feather firecharge emerald
fireball 4 cw blaze powder emerald
firebolt 4 cw blaze flint emerald
firecracker 4 ccw emerald powder flint
firefly_swarm 5 cw nature glow honey string
flaming_barrage 4 ccw blaze arrow powder
flaming_strike 4 cw blaze iron emerald
force_arrow 4 cw amethyst arrow emerald
fortify 4 cw holy iron ward
frost_step 4 ccw snow ender ink
frostbite 4 ccw snow blood amethyst
frostwave 4 cw snow feather emerald
gluttony 4 cw bread amethyst honey
gravity_fissure 6 ccw ender echo iron compass slime
greater_heal 6 cw holy carrot honey amethyst gold
guiding_bolt 4 cw holy glow arrow
gust 4 cw feather emerald phantom
haste 4 cw holy clock copper
heal 4 cw holy amethyst carrot
healing_circle 6 cw holy carrot honey nature ward
heartstop 8 ccw blood clock ward iron holy rotten echo
heat_surge 4 cw blaze magma emerald
ice_block 6 cw snow ice stone ward amethyst
ice_spikes 6 cw snow stone flint ice emerald
ice_tomb 6 ccw snow ice ward blood amethyst
icicle 4 cw snow arrow emerald
interposing_earth 4 cw stone ward iron
invisibility 4 ccw ink glass phantom
lightning_bolt 4 cw copper flint emerald
lightning_lance 4 ccw copper arrow iron
lob_creeper 4 cw emerald powder slime
magic_arrow 4 ccw amethyst arrow ender
magic_missile 4 cw amethyst flint emerald
magma_bomb 6 cw blaze magma stone powder emerald
oakskin 4 cw oak iron ward
pf2_air_bubble 4 cw feather glass ward
pf2_arctic_rift 8 ccw snow ice ender stone emerald echo phantom
pf2_bind_undead 5 ccw bone chain blood rotten
pf2_breathe_fire 4 cw blaze feather emerald
pf2_cataclysm 8 cw stone blaze snow copper powder ender emerald
pf2_caustic_blast 4 ccw poison powder emerald
pf2_chain_lightning 6 cw copper chain iron emerald glow
pf2_cinder_swarm 4 ccw blaze honey string
pf2_clairvoyance 5 cw amethyst glass ender compass
pf2_collective_transposition 7 ccw ender compass chorus string carrot amethyst
pf2_containment 6 cw amethyst ward chain iron glass
pf2_create_water 4 cw kelp clay amethyst
pf2_creation 4 cw oak nature amethyst
pf2_detect_magic 4 cw amethyst compass book
pf2_divine_lance 4 cw holy arrow glow
pf2_eclipse_burst 8 ccw snow echo glow obsidian ender powder emerald
pf2_electric_arc 4 cw copper string emerald
pf2_enfeeble 4 ccw blood rotten bone
pf2_enlarge 4 cw leather mushroom slime
pf2_falling_stars 8 cw blaze stone powder ender glow amethyst emerald
pf2_false_vitality 4 ccw blood carrot bone
pf2_fear 4 ccw rotten ink amethyst
pf2_field_of_life 6 cw holy carrot rotten nature honey
pf2_figment 4 cw ink note amethyst
pf2_fire_shield 5 cw blaze ward iron snow
pf2_fireball 6 cw blaze powder emerald firecharge flint
pf2_fleet_step 4 cw carrot feather copper
pf2_flicker 5 ccw ender phantom ward clock
pf2_floating_flame 4 cw blaze firecharge slime
pf2_force_barrage 4 ccw amethyst emerald arrow
pf2_freezing_rain 4 cw snow kelp feather
pf2_frostbite 4 cw snow blood emerald
pf2_gecko_grip 4 ccw slime nature leather
pf2_gentle_breeze 4 ccw feather nature holy
pf2_gentle_landing 4 cw feather phantom ward
pf2_glass_shield 4 cw glass ward amethyst
pf2_gouging_claw 4 ccw leather flint bone
pf2_gravity_well 4 ccw ender iron slime
pf2_grease 4 cw slime clay coal
pf2_grim_tendrils 4 ccw echo bone string
pf2_harm 4 ccw rotten holy bone
pf2_haste 4 cw clock copper carrot
pf2_heal 4 cw holy carrot rotten
pf2_hydraulic_push 4 cw kelp iron slime
pf2_ignition 4 ccw blaze flint emerald
pf2_illusory_creature 4 cw ink bone amethyst
pf2_illusory_object 4 ccw ink glass amethyst
pf2_invisibility 4 cw ink phantom glass
pf2_item_facade 4 cw ink leather amethyst
pf2_lightning_bolt 6 cw copper flint emerald iron glow
pf2_magic_passage 6 ccw stone ender compass obsidian amethyst
pf2_magnetic_attraction 4 ccw iron copper compass
pf2_mirror_image 4 cw glass ink amethyst
pf2_mud_pit 4 ccw clay kelp slime
pf2_needle_darts 4 cw iron arrow flint
pf2_peaceful_bubble 4 ccw honey glass ward
pf2_pet_cache 4 cw ender leather bread
pf2_protection 4 cw holy ward iron
pf2_protector_tree 6 cw oak nature ward holy honey
pf2_puff_of_poison 4 cw poison feather emerald
pf2_read_aura 4 ccw amethyst glow book
pf2_regenerate 6 cw holy honey carrot amethyst nature
pf2_repulsion 6 ccw amethyst iron ward slime phantom
pf2_resist_energy 4 ccw blaze ward iron
pf2_revealing_light 4 cw glow amethyst glass
pf2_rust_cloud 5 ccw iron kelp feather blood
pf2_scatter_scree 4 ccw stone gravel flint
pf2_see_the_unseen 4 ccw glow glass amethyst
pf2_shape_stone 4 cw stone clay iron
pf2_share_life 4 cw holy blood carrot
pf2_shield 4 cw amethyst iron ward
pf2_shrink 4 ccw leather mushroom slime
pf2_silence 4 ccw note wool ward
pf2_slashing_gust 4 ccw feather flint emerald
pf2_slow 4 ccw clock slime iron
pf2_soothe 4 cw holy honey amethyst
pf2_spirit_blast 6 cw amethyst holy echo powder emerald
pf2_spiritual_armament 4 cw holy iron amethyst
pf2_spout 4 cw kelp slime emerald
pf2_status 4 cw holy compass amethyst
pf2_summon_animal 4 cw bone bread leather
pf2_summon_elemental 7 cw stone blaze kelp feather amethyst ward
pf2_summon_fey 4 ccw nature ink honey
pf2_summon_plant_or_fungus 4 cw nature mushroom oak
pf2_tangle_vine 4 ccw nature string ward
pf2_telekinetic_projectile 4 cw stone amethyst feather
pf2_thunderstrike 4 cw copper feather emerald
pf2_time_jump 7 ccw clock ender compass copper amethyst ward
pf2_translocate 4 cw ender compass amethyst
pf2_vampiric_feast 4 ccw blood rotten holy
pf2_vitality_lash 4 cw holy rotten string
pf2_void_warp 4 ccw echo blood bone
pf2_wall_of_ice 6 cw snow ice ward stone amethyst
pf2_wall_of_stone 6 cw stone iron ward gravel amethyst
pf2_wall_of_water 4 cw kelp ward clay
pf2_water_breathing 4 cw kelp glass ward
pf2_water_walk 4 ccw kelp feather slime
pf2_weapon_storm 6 cw iron flint arrow feather emerald
pf2_wooden_double 4 ccw oak ink ward
pf2_zephyr_slip 4 ccw feather phantom iron
planar_sight 7 ccw echo amethyst glass ender compass glow
pocket_dimension 8 ccw ender obsidian echo chest compass chorus amethyst
poison_arrow 4 cw poison arrow emerald
poison_breath 6 cw poison feather emerald mushroom phantom
poison_splash 4 ccw poison slime powder
portal 7 cw ender obsidian compass chorus amethyst iron
raise_dead 7 ccw bone rotten blood chain amethyst iron
raise_hell 6 cw blaze magma stone powder bone
ray_of_frost 6 cw snow arrow ice emerald phantom
ray_of_siphoning 6 ccw blood arrow holy bone amethyst
recall 4 ccw ender compass phantom
root 4 cw nature string ward
sacrifice 6 ccw blood holy rotten bone echo
scapegoat 4 ccw wool ink amethyst
scorch 4 cw blaze magma stone
sculk_tentacles 6 ccw echo sculk string emerald obsidian
shadow_slash 4 ccw ink ender flint
shield 4 ccw iron ward amethyst
shockwave 4 cw copper slime emerald
slow 4 ccw clock amethyst slime
snowball 4 ccw snow slime emerald
sonic_boom 6 cw note feather echo powder emerald
spectral_hammer 4 cw iron amethyst slime
spider_aspect 4 ccw poison string leather
starfall 8 cw ender glow emerald powder amethyst stone echo
stomp 4 cw stone iron slime
summon_ender_chest 4 cw ender chest amethyst
summon_horse 4 cw leather carrot bone
summon_polar_bear 5 cw snow fish bone leather
summon_swords 7 cw ender iron flint emerald amethyst feather
summon_vex 6 ccw amethyst bone iron echo feather
summon_zombie 4 ccw rotten bone amethyst
sunbeam 6 cw holy glow arrow emerald amethyst
telekinesis 6 ccw amethyst iron feather slime ender
teleport 4 cw ender compass feather
throw 4 cw slime feather emerald
thunderstorm 8 cw copper feather emerald powder iron glow amethyst
touch_dig 4 cw stone iron flint
volt_strike 5 cw copper iron feather emerald
wall_of_fire 6 cw blaze firecharge stone ward emerald
wisp 6 cw holy amethyst glow feather honey
wither_skull 4 ccw bone coal emerald
wololo 4 ccw wool carrot amethyst
"""

# Spell-specific composition notes. These describe native recipe choices, never
# properties implicitly granted to a spell by an item or its upstream quality.
RECIPE_MOTIFS = """
abyssal_shroud | Echo, darkness and spatial escape enclose a protection seal; the membrane recalls its finite dodges.
acid_orb | A harmful eye supplies the acid motif, slime gives it an orb body, and Emerald directs the splash.
acupuncture | Draining focus feeds an Iron needle sharpened with Flint; the ordered circuit suggests a converging puncture.
angel_wing | Living light and a feather describe flight, a membrane its temporary duration, and Gold the Holy construction family.
arcane_lock | Iron and Chain bind the container; Amethyst seals that binding with a native inscription motif.
arcane_shackle | Ender focus fixes an anchor, Chain tethers the victim, and protective stonework contains the moving body.
arrow_volley | Emerald directs real arrows; String supplies the bow motif and a feather spreads their downward flight.
ascension | Copper supplies lightning, a feather lifts the caster, and passage and recovery motifs describe the safe ascent.
ball_lightning | Copper energizes a slime-shaped orb; Emerald directs its impact and Glowstone represents the lingering luminous field.
black_hole | Ender and Echo converge on Obsidian; Chorus folds space while Iron and a Compass draw matter toward one anchor.
blaze_storm | Blaze and Fire Charge feed repeated burning bolts; Powder and a feather spread the channeled barrage.
blessing_of_life | A healing focus faces Amethyst across the inscription while a Golden Carrot represents the allied recipient.
blight | Poison, draining focus and decay choke living growth, with Bone and Mushroom marking its corruption.
blizzard | Snow and Ice establish cold, a feather carries the weather, and Powder spreads it through an Emerald-directed field.
blood_needles | Draining focus flows into Iron and an Arrow, describing a fan of piercing needles that returns life.
blood_slash | Draining focus crosses a Flint edge and an Emerald-directed sweep; counterclockwise order distinguishes the slash.
blood_step | Draining focus, Ender passage and dark Ink describe a short blood-themed blink followed by concealment.
burning_dash | Blaze feeds a feather's forward motion, ending at Fire Charge to describe the burning contact trail.
chain_creeper | Emerald shapes the conjured blast, Powder explodes, and Slime and Chain suggest bounded death-triggered propagation.
chain_lightning | Copper feeds a Chain before Emerald directs the linked lightning strikes.
charge | Copper's speed association meets allied sustenance and a feather's movement motif.
cleanse | Living light surrounds Milk's cleansing motif and the Protective Rune's Pufferfish ingredient.
cone_of_cold | Snow and Ice follow a feather's breath direction; Emerald focuses the sustained freezing cone.
counterspell | Ender focus, a protection seal and Amethyst unmake the spell represented by a Book.
devour | Draining focus draws from decaying flesh into living light; Bone supplies the bite and Bread represents stolen sustenance.
divine_smite | Living light and Glowstone illuminate an Emerald-directed Holy strike.
dragon_breath | Ender focus and Dragon's Breath define the source; Fire Charge and Emerald direct the cone through an Obsidian boundary.
earthquake | Stone, Flint and Iron describe broken ground; Powder distributes the repeated Emerald-directed shocks.
echoing_strikes | Ender focus repeats the Iron-and-Flint weapon motif through a Clock and resonant Amethyst.
eldritch_blast | Echo focuses the otherworldly beam; Emerald, Obsidian, Powder and Amethyst describe directed piercing releases.
electrocute | Copper energizes Iron and Chain, with Emerald and Glowstone shaping a sustained electrical cone.
evasion | Ender passage stands between a recovery membrane and a protection seal, describing a single escape from a hit.
fang_strike | Emerald evokes a Bone fang sharpened by Flint into a forward ground eruption.
fang_swirl | Emerald evokes Bone fangs; Slime rounds their arrangement into a remote circular eruption.
fang_ward | Emerald evokes Bone fangs beside a protection seal to guard the caster's perimeter.
fire_arrow | Blaze ignites an Arrow that Emerald directs into an explosive burning shot.
fire_breath | Blaze and a feather describe fiery breath, with Fire Charge and Emerald directing the sustained cone.
fireball | Blaze feeds Powder before Emerald directs the charged explosive ball.
firebolt | Blaze feeds a Flint spark before Emerald directs the fast burning bolt.
firecracker | Emerald evokes a Powder-and-Flint firework; the reversed sequence distinguishes its lobbed knockback burst.
firefly_swarm | Nature focus, luminous dust, Honeycomb and String describe a living swarm that follows and propagates.
flaming_barrage | Blaze, Arrow and Powder describe separately aimed explosive shots in a reversed recast sequence.
flaming_strike | Blaze burns an Iron weapon that Emerald directs into a melee sweep.
force_arrow | Resonant Amethyst supplies the native force motif; Arrow and Emerald give the projectile its aimed shape.
fortify | Living light protects Iron armor behind a Pufferfish protection seal.
frost_step | Cold focus meets Ender passage and dark Ink, leaving a concealing freezing field behind the blink.
frostbite | Cold and draining motifs turn resonant Amethyst into the mark for repeated attacks and a death-triggered shatter.
frostwave | Cold focus feeds a feather's outward motion before Emerald shapes the repelling wave.
gluttony | Bread provides nutrition, Amethyst recalls mana brewing, and Honeycomb binds food to restored spell energy.
gravity_fissure | Ender and Echo feed an Iron-weighted anchor; Compass and Slime give the moving singularity a directed pull.
greater_heal | Living light, allied sustenance and honey feed Amethyst's healing precedent, with Gold reinforcing the Holy crafting family.
guiding_bolt | Living light and Glowstone mark the target that an Arrow guides other projectiles toward.
gust | A feather's broad airflow meets Emerald direction and a recovery membrane's brief motion motif.
haste | Living light and a Clock meet Copper's Haste and attack-speed associations to accelerate allied work.
heal | Living light crosses healing-associated Amethyst before reaching allied sustenance.
healing_circle | Living light, allied sustenance and Honeycomb join Nature focus inside a protection seal's persistent circle.
heartstop | Draining focus and a Clock delay harm behind warded Iron; living light, decay and Echo describe the eventual reckoning.
heat_surge | Blaze heats Magma Cream before Emerald shapes the outward burning burst.
ice_block | Cold focus and Ice form the heavy body; Stone adds weight and a warded Amethyst circuit bounds its impact.
ice_spikes | Cold focus hardens Stone and Flint into Ice points; Emerald directs their ground line.
ice_tomb | Cold and Ice enclose a protection seal; draining and healing-associated Amethyst describe the immobilized recovery.
icicle | Cold focus becomes an Arrow-shaped point directed by Emerald.
interposing_earth | Stone stands beside a protection seal and Iron's resistance association, describing raised defensive cover.
invisibility | Dark Ink, clear Glass and a recovery membrane describe a temporary body hidden from sight.
lightning_bolt | Copper feeds a Flint spark before Emerald directs the electrical burst.
lightning_lance | Copper feeds an Arrow-shaped Iron point; reversed order distinguishes the piercing lance.
lob_creeper | Emerald evokes a Powder-filled Slime body, describing a conjured gravity-driven bomb.
magic_arrow | Resonant Amethyst and an Arrow meet Ender focus for the charged piercing projectile.
magic_missile | Amethyst feeds a Flint point that Emerald directs as a homing force missile.
magma_bomb | Blaze heats Magma Cream and Stone; Powder and Emerald describe the lobbed blast and persistent ground field.
oakskin | Oak follows its actual Iron Oakskin brewing precedent, meeting Iron resistance and a protection seal.
pf2_air_bubble | A feather's air is enclosed in Glass and protected by a seal for temporary emergency breathing.
pf2_arctic_rift | Cold and Ice tear through Ender passage and Stone; Emerald, Echo and a membrane widen and sustain the rift.
pf2_bind_undead | Bone and decay identify an undead body, while Chain and draining focus describe temporary control.
pf2_breathe_fire | Blaze feeds a feather's breath and Emerald directs the short cone.
pf2_cataclysm | Stone, Blaze, Snow and Copper encode four elemental surges, bounded by Powder, Ender focus and Emerald.
pf2_caustic_blast | A harmful eye feeds Powder and Emerald in reversed order to describe the small acid splash.
pf2_chain_lightning | Copper and Chain link Iron contacts; Emerald and Glowstone describe the longer lightning chain.
pf2_cinder_swarm | Blaze lights Honeycomb's swarm and String binds that swarm to its chosen target.
pf2_clairvoyance | Amethyst and Glass form the sensing eye, while Ender focus and Compass separate its view from the body.
pf2_collective_transposition | Ender and Chorus move a Compass-led formation; String, allied sustenance and Amethyst preserve the shared destination.
pf2_containment | Amethyst's force motif closes a warded Chain-and-Iron boundary around a visible Glass interior.
pf2_create_water | Kelp represents water and Clay its cauldron-like vessel; Amethyst focuses the small conjuration.
pf2_creation | Oak and Nature focus supply the wooden construction, with Amethyst describing its temporary conjured form.
pf2_detect_magic | Amethyst, Compass and Book describe sensing nearby inscriptions and native magical presences.
pf2_divine_lance | Living light becomes an Arrow-shaped Glowstone lance of spiritual damage.
pf2_eclipse_burst | Cold and Echo eclipse Glowstone within Obsidian; Ender, Powder and Emerald release the dark area burst.
pf2_electric_arc | Copper crosses String's short link before Emerald directs the two-target arc.
pf2_enfeeble | Draining focus and decay weaken the living body represented by Bone.
pf2_enlarge | Leather represents a body, Mushroom suggests growth, and Slime its enlarged flexible shape.
pf2_falling_stars | Blaze ignites falling Stone and Powder; Ender, Glowstone, Amethyst and Emerald direct the repeated celestial impacts.
pf2_false_vitality | Draining focus and Bone surround allied sustenance to suggest temporary borrowed health.
pf2_fear | Decay and dark Ink project an unsettling Amethyst-focused image into the mind.
pf2_field_of_life | Allied sustenance stands opposite decay while living light, Nature and honey sustain a field that heals life and hurts undead.
pf2_figment | Ink supplies illusion, a Note Block supplies sound, and Amethyst binds the light-and-sound lure.
pf2_fire_shield | Blaze and a protection seal meet Iron resistance and opposing cold to ward and burn nearby attackers.
pf2_fireball | Blaze, Powder and Emerald are extended by Fire Charge and Flint for the larger aimed fireball.
pf2_fleet_step | Allied sustenance, a feather and Copper's speed association describe a brief burst of movement.
pf2_flicker | Ender passage, a membrane and a protection seal cycle through a Clock for repeated short defensive blinks.
pf2_floating_flame | Blaze and Fire Charge float in a Slime-shaped suspended body that pulses heat.
pf2_force_barrage | Amethyst's force motif, Emerald and an Arrow describe repeated homing projectiles in a reversed sequence.
pf2_freezing_rain | Cold focus meets water-bearing Kelp and a feather's weather motif to describe a slipping rain patch.
pf2_frostbite | Cold and a draining motif meet Emerald for a directly aimed chilling weakness.
pf2_gecko_grip | Slime's adhesion binds Nature focus to the Leather body for wall climbing.
pf2_gentle_breeze | A feather carries Nature and living light into a restorative airflow.
pf2_gentle_landing | A feather and recovery membrane descend behind a protection seal for safe slow falling.
pf2_glass_shield | Glass supplies the brittle ward, protection seals it, and Amethyst recalls its retaliating shard.
pf2_gouging_claw | Leather shapes the transformed limb, Flint its edge, and Bone its claw.
pf2_gravity_well | Ender focus draws Iron weight into one Slime-shaped converging anchor.
pf2_grease | Slime, wet Clay and Coal's native oily-soot motif describe the slippery ground patch.
pf2_grim_tendrils | Echo feeds Bone and String to describe a narrow grasping line of void injury.
pf2_harm | Decay feeds living light and Bone in reversed order, describing a pulse that heals undead while harming living flesh.
pf2_haste | A Clock leads Copper's Haste association into allied sustenance, describing accelerated movement and work.
pf2_heal | Living light separates allied sustenance from opposing decay, expressing healing living bodies and harming undead.
pf2_hydraulic_push | Water-bearing Kelp meets Iron knockback and a Slime-shaped jet to drive one target away.
pf2_ignition | Blaze and Flint precede Emerald in reversed order for an immediately aimed burning spark.
pf2_illusory_creature | Dark Ink depicts the Bone body of a creature, held as an Amethyst-focused decoy.
pf2_illusory_object | Dark Ink and Glass depict a visible but non-solid Amethyst-focused object.
pf2_invisibility | Dark Ink, a membrane and Glass describe concealment; their order distinguishes it from the other invisibility recipe.
pf2_item_facade | Ink dresses a Leather surface with an Amethyst-focused pristine appearance.
pf2_lightning_bolt | Copper sparks across Flint and Emerald into an Iron-and-Glowstone line of multiple electrical hits.
pf2_magic_passage | Stone crosses Ender passage, Compass and Obsidian; Amethyst binds the temporary opening and restoration.
pf2_magnetic_attraction | Iron and Copper face a Compass, expressing a directed pull on loose metal.
pf2_mirror_image | Glass reflects Ink's depiction around Amethyst; ordering distinguishes copies from a single illusory object.
pf2_mud_pit | Clay meets water-bearing Kelp and Slime in reversed order for a sticky slowing patch.
pf2_needle_darts | Iron forms Arrow-shaped needles sharpened with Flint for the converging metal volley.
pf2_peaceful_bubble | Honeycomb's shelter motif and Glass enclose a protection seal that conceals its occupants.
pf2_pet_cache | Ender passage shelters a Leather body beside Bread's feeding motif, representing the existing companion rather than a new summon.
pf2_protection | Living light meets a protection seal and Iron resistance for finite incoming-hit mitigation.
pf2_protector_tree | Oak and Nature grow the body, while a seal, living light and honey shelter neighboring allies.
pf2_puff_of_poison | A harmful eye follows a feather's breath and Emerald's direction into a close poison puff.
pf2_read_aura | Amethyst and Glowstone examine the inscription represented by a Book.
pf2_regenerate | Living light, honey and allied sustenance meet Amethyst's regeneration association and Nature for sustained healing.
pf2_repulsion | Amethyst's force motif drives Iron knockback through a seal, Slime boundary and temporary membrane.
pf2_resist_energy | Blaze is contained by a protection seal and Iron resistance; it marks the protected energy rather than a damaging output.
pf2_revealing_light | Glowstone leads Amethyst and Glass to reveal concealed creatures with visible light.
pf2_rust_cloud | Iron meets wet Kelp, airborne feather and a harmful brewing motif, describing the abrasive obscuring cloud.
pf2_scatter_scree | Stone fractures into Gravel and Flint, describing the burst and remaining slowing rubble.
pf2_see_the_unseen | Glowstone, Glass and Amethyst reverse revealing light's order to describe private perception through concealment.
pf2_shape_stone | Stone follows moldable Clay and Iron structure into a moved and reshaped block.
pf2_share_life | Living light meets draining focus and allied sustenance, describing the transfer of an ally's incoming harm.
pf2_shield | Amethyst feeds Iron resistance before the protection seal, describing the brief personal force ward.
pf2_shrink | Leather, Mushroom and Slime reverse the growth recipe to describe a smaller flexible body.
pf2_silence | A Note Block's sound is smothered by Wool behind a protection seal.
pf2_slashing_gust | A feather carries a Flint edge before Emerald directs the narrow cutting gust.
pf2_slow | A Clock loses motion to sticky Slime and Iron's native weight motif; its immunity parameter is not a slowing bonus.
pf2_soothe | Living light and honey meet healing-associated Amethyst for recovery and removal of weakness.
pf2_spirit_blast | Amethyst, living light and Echo release a Powder-spread, Emerald-directed spiritual burst.
pf2_spiritual_armament | Living light animates an Iron weapon through Amethyst's temporary force motif.
pf2_spout | Water-bearing Kelp and Slime's lift meet Emerald to shape the aimed spout.
pf2_status | Living light and Compass report on Amethyst-bound allied health and direction.
pf2_summon_animal | Bone, Bread and Leather describe the body, feeding and companionship of the allied wolf.
pf2_summon_elemental | Stone, Blaze, Kelp and a feather encode the four elemental bodies; Amethyst and a seal bind their finite manifestation.
pf2_summon_fey | Nature focus, Ink and honey describe the fragile woodland illusion that hinders enemies.
pf2_summon_plant_or_fungus | Nature, Mushroom and Oak describe the living support plant or protective fungus.
pf2_tangle_vine | Nature grows a String tether that a protection seal anchors to its victim.
pf2_telekinetic_projectile | Stone is lifted by resonant Amethyst and a feather into the telekinetically thrown projectile.
pf2_thunderstrike | Copper joins a feather's air and Emerald's direction for an aimed electrical strike with thunder.
pf2_time_jump | A Clock enters Ender passage; Compass anchors the return while Copper, Amethyst and a seal suspend a protected instant.
pf2_translocate | Ender passage follows a Compass-directed Amethyst circuit toward a supported destination.
pf2_vampiric_feast | Draining focus pulls decaying flesh toward living light, describing damage converted into temporary health.
pf2_vitality_lash | Living light lashes decay through String, expressing selective harm to undead.
pf2_void_warp | Echo and draining focus distort the Bone body's vitality, while leaving undead outside the motif.
pf2_wall_of_ice | Cold and Ice rise beside a seal, Stone and Amethyst to describe a temporary breakable frozen wall.
pf2_wall_of_stone | Stone and Iron structure stand behind a seal; Gravel and Amethyst define the raised solid panel.
pf2_wall_of_water | Water-bearing Kelp stands beside a seal and Clay, describing the bounded traversable water wall.
pf2_water_breathing | Kelp marks submerged life, Glass encloses its breath, and a protection seal sustains air for allies.
pf2_water_walk | Kelp's water surface meets a feather's light step and Slime's buoyant shape.
pf2_weapon_storm | Iron, Flint and Arrow form weapons that a feather spreads into an Emerald-directed sweep.
pf2_wooden_double | Oak and Ink make the false wooden body that a protection seal sheds on a strong hit.
pf2_zephyr_slip | A feather and membrane move past Iron's knockback motif for a triggered defensive retreat.
planar_sight | Echo and Amethyst focus a Glass eye; Ender, Compass and Glowstone describe directed perception through walls.
pocket_dimension | Ender and Obsidian bound an Echo-hidden room; Chest, Compass, Chorus and Amethyst preserve storage and the return path.
poison_arrow | A harmful eye feeds an Arrow directed by Emerald into its poisonous impact cloud.
poison_breath | A harmful eye follows feather-breath and Emerald direction; Mushroom and a membrane describe the sustained toxic cone.
poison_splash | A harmful eye fills a Slime-shaped splash that Powder disperses.
portal | Ender passage links Obsidian boundaries through Compass and Chorus, with Amethyst and Iron preserving the paired anchors.
raise_dead | Bone and decay make undead bodies; draining focus, Chain, Amethyst and Iron express their owned service.
raise_hell | Blaze and Magma Cream erupt through Stone and Powder, with Bone recalling the weapon-backed ground slam.
ray_of_frost | Cold focus follows an Arrow line through Ice; Emerald and a membrane sustain the piercing freezing ray.
ray_of_siphoning | Draining focus follows an Arrow ray into living light, with Bone and Amethyst binding the sustained return of health.
recall | Ender passage follows a Compass home through a recovery membrane's return motif.
root | Nature grows a String tether anchored by the protection seal.
sacrifice | Draining focus, living light and decay cross Bone and Echo, describing a summon whose remaining life fuels its detonation.
scapegoat | Wool marks the animal decoy, dark Ink its false body, and Amethyst its attracting illusion.
scorch | Blaze heats Magma Cream beneath Stone, describing the delayed ground eruption.
sculk_tentacles | Echo and Sculk grow String-like tendrils; Emerald and Obsidian direct their dark pulsing field.
shadow_slash | Dark Ink crosses Ender passage into a Flint blade, describing the blink and weapon-scaled slash.
shield | Iron resistance stands beside a protection seal before Amethyst, describing a constructed stationary projectile ward.
shockwave | Copper energizes a Slime-shaped wave that Emerald directs outward with knockback.
slow | A Clock meets Amethyst and sticky Slime in reversed order to retard movement and work.
snowball | Cold focus fills a Slime-shaped ball that Emerald directs into a freezing burst.
sonic_boom | A Note Block sends air through Echo, Powder and Emerald, describing the piercing sonic beam.
spectral_hammer | Iron and Amethyst shape a Slime-broad spectral tool face for tool-aware mining.
spider_aspect | A harmful eye, String web and Leather body describe poisonous spider attacks applied to the caster.
starfall | Ender and Glowstone direct Emerald-guided explosive stars through Amethyst, falling Stone and Echo.
stomp | Stone and Iron weight drive a Slime-shaped forward ground wave that launches enemies.
summon_ender_chest | Ender focus opens the Chest through a native Amethyst inscription.
summon_horse | Leather, allied sustenance and Bone describe the tamed mount's saddle, feeding and body.
summon_polar_bear | Cold focus meets Fish, Bone and Leather to describe a living allied polar bear.
summon_swords | Ender focus animates Iron-and-Flint blades; Emerald, Amethyst and a feather describe their directed flying bodies.
summon_vex | Amethyst animates Bone and Iron through Echo and a feather into owned flying spirit fighters.
summon_zombie | Decay and Bone form the undead body that Amethyst binds to its owner.
sunbeam | Living light and Glowstone align an Arrow beam through Emerald and Amethyst for sustained piercing Holy damage.
telekinesis | Amethyst's force motif holds Iron weight in feather-air and Slime, with Ender focus preserving the remote target.
teleport | Ender passage follows Compass direction and a feather's movement into the short safe blink.
throw | Slime's flexible launch meets a feather and Emerald, describing a directed recoverable held-item throw.
thunderstorm | Copper, a feather and Emerald surround Powder, Iron, Glowstone and Amethyst for the repeated following electrical storm.
touch_dig | Stone, Iron toolwork and Flint describe contact with a hardness-limited block and its normal drops.
volt_strike | Copper energizes Iron and feather-motion before Emerald directs the contact dash.
wall_of_fire | Blaze and Fire Charge trace a Stone-bounded protection seal that Emerald directs into a burning segment.
wisp | Living light, Amethyst and Glowstone meet a feather and Honeycomb to describe a luminous following spirit field.
wither_skull | Bone and Coal darken the Emerald-directed skull into a slow withering blast.
wololo | Wool receives allied sustenance and Amethyst to describe a harmless change to a living sheep's appearance.
"""

# Primary roles cite source facts; purely native motifs are explicitly labeled.
MATERIALS = {
    'blaze': ('blaze_rod', 'flame focus', 'Iron Fire focus', ['irons_spellbooks:fire_rune']),
    'snow': ('snowball', 'cold focus', 'Native cold substitute for Iron Frozen Bone', ['irons_spellbooks:frozen_bone','irons_spellbooks:ice_rune']),
    'copper': ('copper_ingot', 'lightning channel', 'Iron Copper lightning action and Haste/attack-speed associations; selected recipe role only', ['irons_spellbooks:lightning_bottle','irons_spellbooks:lightning_rune']),
    'holy': ('glistering_melon_slice', 'living light', 'Native healing ingredient; optional Iron Holy focus', ['irons_spellbooks:divine_pearl','irons_spellbooks:holy_rune']),
    'ender': ('ender_pearl', 'passage focus', 'Iron Ender focus', ['irons_spellbooks:ender_rune']),
    'blood': ('fermented_spider_eye', 'draining focus', 'Native harmful brewing motif; optional Iron Blood focus', ['irons_spellbooks:blood_vial','irons_spellbooks:blood_rune']),
    'emerald': ('emerald', 'evocation focus', 'Iron Evocation focus', ['irons_spellbooks:evocation_rune']),
    'nature': ('poisonous_potato', 'nature focus', 'Iron Nature focus', ['irons_spellbooks:nature_rune']),
    'echo': ('echo_shard', 'lost echo', 'Iron Eldritch focus metadata and Timeless Slurry reagent; there is no Eldritch Rune in the pinned catalog', []),
    'ward': ('pufferfish', 'protection seal', 'Iron Protective Rune ingredient', ['irons_spellbooks:protection_rune']),
    'phantom': ('phantom_membrane', 'recovery veil', 'Iron Recovery Rune ingredient', ['irons_spellbooks:cooldown_rune']),
    'amethyst': ('amethyst_shard', 'resonant facet', 'Iron mana/regeneration/cast-time crafting family; native focusing role', ['irons_spellbooks:arcane_essence']),
    'iron': ('iron_ingot', 'bound metal', 'Iron armor/knockback material channels', []),
    'gold': ('gold_ingot', 'gilded metal', 'Iron ignition material and Divine Pearl/Holy construction ingredient; selected recipe role only', []),
}
NATIVE = {
 'paper':'paper', 'poison':'spider_eye', 'slime':'slime_ball', 'flint':'flint', 'obsidian':'obsidian',
 'ink':'ink_sac', 'chorus':'chorus_fruit', 'feather':'feather', 'glow':'glowstone_dust', 'chain':'chain',
 'arrow':'arrow', 'string':'string', 'firecharge':'fire_charge', 'powder':'gunpowder', 'bone':'bone',
 'rotten':'rotten_flesh', 'mushroom':'brown_mushroom', 'dragon':'dragon_breath', 'honey':'honeycomb',
 'carrot':'golden_carrot', 'magma':'magma_cream', 'milk':'milk_bucket', 'bread':'bread', 'clock':'clock',
 'stone':'stone', 'ice':'ice', 'glass':'glass', 'oak':'oak_log', 'note':'note_block', 'kelp':'kelp',
 'clay':'clay_ball', 'compass':'compass', 'leather':'leather', 'coal':'coal', 'gravel':'gravel',
 'wool':'white_wool', 'sculk':'sculk', 'chest':'chest', 'fish':'cod', 'book':'book',
}
for key, item in NATIVE.items():
    MATERIALS[key] = (item, {'paper':'blank inscription','carrot':'ally sustenance','rotten':'enemy decay'}.get(key,key.replace('_',' ')+' motif'), 'Native spell-specific form/relationship motif', [])

SEATS = {
    4: (0, 1, 2, 3),
    5: (0, 1, 2, 4, 6),
    6: (0, 1, 2, 4, 5, 6),
    7: (0, 1, 2, 3, 4, 5, 6),
    8: tuple(range(8)),
}
RECIPE_DIR = ROOT / 'src/main/resources/data/vestige/ritual_recipes'
SPELL_DIR = ROOT / 'src/main/resources/data/vestige/runtime_spells'


def require(condition, message):
    if not condition:
        raise ValueError(message)


def physical_seat(recipe, seat):
    """Map either recipe circle to the eight actual apparatus positions."""
    return seat * 2 if recipe['circle'] == 4 else seat


def layout(recipe, turns=0):
    result = [frozenset()] * 8
    for part in recipe['parts']:
        result[(physical_seat(recipe, part['seat']) + turns * 2) % 8] = frozenset(part['ingredient']['items'])
    return tuple(result)


def layouts_overlap(left, right):
    """True if at least one concrete arrangement satisfies both selectors."""
    return all(bool(a & b) if a and b else a == b for a, b in zip(left, right))


def validate_recipes(recipes, spell_ids):
    identities = [r['spell'] for r in recipes]
    require(len(set(identities)) == len(identities), 'Duplicate ritual spell identity')
    require(set(identities) == set(spell_ids), 'Ritual recipes must cover exactly the current native spell catalog')
    reference = json.loads((ROOT / 'tools/iron-item-uses.json').read_text())
    foreign_ids = {v['id'] for v in walk(reference) if isinstance(v, dict) and v.get('kind') == 'item'}
    foreign_ids.update(v for focus in reference['school_focuses'] for v in focus['values'])
    for recipe in recipes:
        identity = recipe['spell']
        circle, parts = recipe['circle'], recipe['parts']
        require(circle in (4, 8) and 4 <= len(parts) <= 8, f'{identity}: invalid size')
        seats = [p['seat'] for p in parts]
        require(all(type(s) is int and 0 <= s < circle for s in seats) and len(set(seats)) == len(seats), f'{identity}: invalid or duplicate seats')
        require(circle != 4 or len(parts) == 4, f'{identity}: basic ritual needs four parts')
        require(circle != 8 or len(parts) > 4 and {0, 2, 4, 6}.issubset(seats), f'{identity}: advanced ritual retains all four Stone parts')
        paper = [p for p in parts if 'minecraft:paper' in p['ingredient']['items']]
        require(len(paper) == 1 and paper[0]['ingredient']['items'] == ['minecraft:paper'], f'{identity}: every route must consume exactly one Paper')
        require(isinstance(recipe.get('rationale'), str) and recipe['rationale'].strip(), f'{identity}: missing composition rationale')
        require(recipe.get('clues'), f'{identity}: missing placement clues')
        require(len(recipe['color']) == 6 and all(c in '0123456789abcdef' for c in recipe['color']), f'{identity}: invalid RGB color')
        for part in parts:
            items = part['ingredient']['items']
            require(items and len(items) <= 8 and items[0].startswith('minecraft:'), f'{identity}: no vanilla baseline')
            require(len(set(items)) == len(items), f'{identity}: duplicate ingredient alternative')
            # Open-ended tags cannot be proven disjoint across provider combinations.
            # The authored catalog uses bounded explicit alternatives for that reason.
            require(not part['ingredient'].get('tags'), f'{identity}: authored alternatives must use explicit items')
            require(part.get('role') and part.get('rationale'), f'{identity}: missing material rationale')
            for item in items:
                require(item.startswith('minecraft:') or item in foreign_ids and item != 'irons_spellbooks:arcane_salvage', f'{identity}: unsupported optional item {item}')
    for left, right in combinations(recipes, 2):
        for turn in range(4):
            require(not layouts_overlap(layout(left), layout(right, turn)), f"Ambiguous recipes: {left['spell']} and {right['spell']} (rotation {turn})")


def outputs():
    spells = {p.stem:json.loads(p.read_text()) for p in sorted(SPELL_DIR.glob('*.json'))}
    rows = [line.split() for line in RECIPES.strip().splitlines()]
    require(len({r[0] for r in rows}) == len(rows), 'Duplicate authored row')
    authored_ids = {r[0] for r in rows}
    require(authored_ids == set(spells), f'Authored coverage mismatch: missing {set(spells)-authored_ids}, extra {authored_ids-set(spells)}')
    motif_rows = [line.split(' | ', 1) for line in RECIPE_MOTIFS.strip().splitlines()]
    motifs = dict(motif_rows)
    require(len(motifs) == len(motif_rows) and set(motifs) == set(spells), 'Every spell requires an explicit composition rationale')
    result = {}
    recipes = []
    counts = Counter(int(r[1]) for r in rows)
    ledger = [
        '# Native spell ritual recipes', '',
        f'Authored October 3, 2026: **{len(rows)} spell-scroll recipes** covering every current native spell. '
        f'**{counts[4]} basic recipes** use four inner Plinths; **{len(rows)-counts[4]} advanced recipes** use four inner Plinths plus one through four outer Plinth offerings. '
        'These are initial recipe designs; acquisition cost and puzzle pacing still need gameplay review.', '',
        'Anyone can craft the correct arrangement. Knowledge and a reference scroll are not required. '
        'Each craft consumes one offering from each required pedestal, including exactly one Paper, and produces one native spell scroll. '
        'A reference scroll placed on the Spellstone supplies hints and is retained. Crafting does not identify the spell.', '',
        'Arrange ingredients by their relationships. Every whole-layout quarter-turn works; reflections reverse the order and do not match. '
        'Paper is the reference point in the descriptions below, not a prescribed compass direction. '
        'The displayed arrangement uses North only to draw one example orientation: inner and outer slots each run clockwise around their layer; either layer may be Cross or Diagonal. '
        'Unused slots within an active eight-slot recipe remain empty. Four-slot recipes ignore all outer nodes, offerings and materials.', '',
        '| Occupied slots | Inner | Outer | Recipes |', '|---:|---:|---:|---:|',
        *[f'| {size} | 4 | {size-4} | {counts[size]} |' for size in range(4, 9)], '',
        'Material meanings consult [the complete pinned Iron reference](research/iron-material-uses.md) '
        'and [the accepted material standard](design/material-crafting.md). '
        'All first ingredients are vanilla items. Optional Iron alternatives are accepted directly when installed; they never replace the standalone route. '
        'Upstream school-focus uses and Jewelry channels provide specific precedents. Native motifs such as slime for shape, feathers for motion, '
        'and Golden Carrot/Rotten Flesh for living/undead contrast are independently authored recipe choices. '
        'They do not become automatic trait semantics. Neither source quality nor source rarity/rank changes the crafted spell. '
        'Embedded Plinth material sockets are independent of offerings; material-specific bonuses remain unimplemented. The active geometry shapes the created scroll through the accepted leyline matrix.', '',
        'The shared material roles below are inscription vocabulary. Each spell composition selects the relevant association; '
        'an ingredient does not grant every property of its reference material. '
        'For example, Copper can suggest lightning or quickening, and Amethyst can recall healing, regeneration or focusing. '
        'Glistering Melon and Fermented Spider Eye are vanilla healing/harmful brewing motifs, not claims that Iron uses those items as its Holy/Blood focuses.', '',
        '| Material key | Standalone item | Optional direct alternatives | Reference or native motif |', '|---|---|---|---|',
    ]
    for key, (item, role, meaning, alternatives) in sorted(MATERIALS.items()):
        ledger.append(f"| `{key}` — {role} | `{item}` | {', '.join(f'`{a}`' for a in alternatives) or '—'} | {meaning} |")
    ledger.extend(['', '| Spell | Slots | One example orientation | Composition rationale | Relative placement clues |', '|---|---:|---|---|---|'])
    positions = ('N','NE','E','SE','S','SW','W','NW')
    for row in rows:
        identity, size, direction, *keys = row; size = int(size)
        require(len(keys) == size-1 and direction in ('cw','ccw') and size in SEATS, f'{identity}: invalid authored row')
        circle = 4 if size == 4 else 8
        seats = list(SEATS[size])
        if direction == 'ccw': seats = [(-s) % circle for s in seats]
        components = ['paper', *keys]
        # Life-versus-death is an authored opposing relationship, including across the outer ring.
        if 'carrot' in keys and 'rotten' in keys:
            a, b = components.index('carrot'), components.index('rotten')
            target = (seats[a] + circle//2) % circle
            require(target in seats, f'{identity}: opposing life/decay relationship needs an occupied opposite position')
            c = seats.index(target)
            seats[b], seats[c] = seats[c], seats[b]
        parts = []
        for seat, key in zip(seats, components):
            item, role, meaning, alternatives = MATERIALS[key]
            parts.append(dict(seat=seat, role=role, ingredient=dict(items=['minecraft:'+item, *alternatives]), rationale=meaning))
        name = spells[identity].get('source',{}).get('name') or identity.replace('_',' ').title()
        color = next((layer['color'] for e in walk(spells[identity]['effects']) if isinstance(e,dict) for layer in e.get('layers',[]) if isinstance(layer,dict) and 'color' in layer), 'ac73e8')
        ordered = sorted(parts,key=lambda p:p['seat'])
        paper_seat = next(p['seat'] for p in parts if p['ingredient']['items'] == ['minecraft:paper'])
        clues=[]
        if circle == 8:
            outer = [p['role'] for p in ordered if p['seat'] % 2 == 1]
            clues.append(f"Outer Plinths hold {', '.join(outer)}; the other required ingredients use Inner Plinths.")
            for empty in sorted(set(range(8)) - set(seats)):
                offset = (empty - paper_seat) % 8
                ordinal = ('first', 'second', 'third', 'fourth')[(offset - 1) // 2]
                clues.append(f'Leave the {ordinal} outer Plinth clockwise from Paper empty.')
        for i, p in enumerate(ordered):
            after = ordered[(i+1)%len(ordered)]
            distance = (after['seat'] - p['seat']) % circle
            clues.append(f"{after['role'].capitalize()} follows {p['role']} clockwise" + (", across an unused outer Plinth." if distance > 1 else "."))
        if 'carrot' in keys and 'rotten' in keys: clues.append('Ally sustenance stands opposite enemy decay.')
        recipe = dict(spell='vestige:'+identity,name=name,circle=circle,color=color,rationale=motifs[identity],parts=parts,clues=clues)
        recipes.append(recipe)
        result[ROOT/'src/main/resources/data/vestige/ritual_recipes'/f'{identity}.json'] = json.dumps(recipe,indent=2)+'\n'
        arrangement = ', '.join(f"{positions[p['seat']*2 if circle==4 else p['seat']]}: {p['ingredient']['items'][0].split(':')[1].replace('_',' ')}" for p in ordered)
        ledger.append(f"| {name} (`{identity}`) | {size} | {arrangement} | {motifs[identity]} | {' '.join(clues)} |")
    validate_recipes(recipes, ['vestige:'+i for i in spells])
    index = dict(spells=['vestige:'+i for i in sorted(spells)], traits=sorted({t for s in spells.values() for t in s['traits']}))
    result[ROOT/'src/main/resources/data/vestige/ritual_catalog.json'] = json.dumps(index,indent=2)+'\n'
    result[ROOT/'docs/ritual-recipes.md'] = '\n'.join(ledger)+'\n'
    return result

def walk(value):
    yield value
    if isinstance(value,dict):
        for child in value.values(): yield from walk(child)
    elif isinstance(value,list):
        for child in value: yield from walk(child)

def main():
    parser=argparse.ArgumentParser(); parser.add_argument('--check',action='store_true'); args=parser.parse_args()
    expected=outputs(); directory=ROOT/'src/main/resources/data/vestige/ritual_recipes'
    require(not set(directory.glob('*.json')) - set(expected), 'Stale ritual recipes require removal')
    for path, text in expected.items():
        if args.check: require(path.exists() and path.read_text()==text, f'Drift: {path.relative_to(ROOT)}')
        else: path.parent.mkdir(parents=True,exist_ok=True); path.write_text(text)
    count = sum(p.parent == directory for p in expected)
    print(f"{'Checked' if args.check else 'Authored'} {count} explicit ritual recipes, item index and material/placement ledger; complete coverage and no rotational/alternative collisions.")

if __name__=='__main__': main()
