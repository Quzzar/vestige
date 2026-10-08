# Spell art direction

For item sprites, block/worn textures and GUI artwork, follow [Minecraft art and pixel scale](design/minecraft-art.md). The spell-effect compositions below have a separate scope.

All 214 spells have an explicitly authored combination of silhouette, secondary movement, density, rhythm and scale. Related spells share materials and a visual vocabulary while retaining different compositions. This table describes the intended native rendering; actual appearance is reviewed through Minecraft cast footage in the [gallery](effects-workshop.md). Distinct recipe data alone does not establish artistic quality.

The visual brief is readable magic at ordinary encounter distance: a recognizable form, motion tied to its purpose, a visible impact or reaction, and a finite end. Fire rises and surges; frost grows sharp facets; blood coils or cuts; plants climb and unfurl; force bends and forms sigils; protective magic has panels or substantial material. Information and stealth spells use restrained transitions and private cues so ornament does not reveal hidden bodies.

Iron's [Black Hole renderer](https://github.com/iron431/irons-spells-n-spellbooks/blob/1.21/src/main/java/io/redspace/ironsspellbooks/entity/spells/black_hole/BlackHoleRenderer.java) and [Devour jaw renderer](https://github.com/iron431/irons-spells-n-spellbooks/blob/1.21/src/main/java/io/redspace/ironsspellbooks/entity/spells/devour_jaw/DevourJawRenderer.java) were reviewed as examples of layered motion and recognizable silhouettes. Vestige authors its own geometry and compositions. Iron's [license](https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/LICENSE.md) reserves assets; none were imported. Existing attribution remains in [CREDITS](../CREDITS.md).

The data authoring table is [spell_art.py](../tools/spell_art.py). Shared shapes are described in [presentation design](design/spell-presentation-and-expansion.md). The client receives ordinary bounded layers; it does not choose artwork from spell IDs or traits. Gameplay coefficients, targets, costs, rarity and relative trait units remain independent of artwork.

| Spell | Main silhouette | Secondary motion | Material |
|---|---|---|---|
| Abyssal Shroud (`abyssal_shroud`) | Vortex | Tendrils | Void |
| Acid Spit (`acid_orb`) | Motes | Ripple | Venom |
| Acupuncture (`acupuncture`) | Shards | Helix | Blood |
| Angel Wings (`angel_wing`) | Wings | Rays | Sun |
| Arcane Lock (`arcane_lock`) | Sigil | Chain | Violet |
| Arcane Shackle (`arcane_shackle`) | Chain | Helix | Violet |
| Arrow Volley (`arrow_volley`) | Shards | Rays | Silver |
| Ascension (`ascension`) | Helix | Rays | Storm |
| Ball Lightning (`ball_lightning`) | Helix | Motes | Storm |
| Black Hole (`black_hole`) | Vortex | Rays | Void |
| Blaze Storm (`blaze_storm`) | Flare | Vortex | Ember |
| Blessing of Life (`blessing_of_life`) | Rays | Motes | Sun |
| Blight (`blight`) | Tendrils | Motes | Venom |
| Blizzard (`blizzard`) | Shards | Vortex | Frost |
| Blood Needles (`blood_needles`) | Shards | Rays | Blood |
| Blood Slash (`blood_slash`) | Slash | Helix | Blood |
| Blood Step (`blood_step`) | Slash | Motes | Blood |
| Burning Dash (`burning_dash`) | Flare | Helix | Ember |
| Chain Creeper (`chain_creeper`) | Rays | Shards | Venom |
| Chain Lightning (`chain_lightning`) | Helix | Rays | Storm |
| Charge (`charge`) | Helix | Sigil | Storm |
| Cleanse (`cleanse`) | Rays | Leaves | Sun |
| Cone of Cold (`cone_of_cold`) | Shards | Ripple | Frost |
| Counterspell (`counterspell`) | Sigil | Slash | Violet |
| Devour (`devour`) | Fangs | Helix | Blood |
| Divine Smite (`divine_smite`) | Slash | Rays | Sun |
| Dragon Breath (`dragon_breath`) | Tendrils | Flare | Violet |
| Earthquake (`earthquake`) | Shards | Sigil | Earth |
| Echoing Strikes (`echoing_strikes`) | Slash | Motes | Violet |
| Eldritch Blast (`eldritch_blast`) | Tendrils | Helix | Void |
| Electrocute (`electrocute`) | Helix | Ripple | Storm |
| Evasion (`evasion`) | Motes | Vortex | Violet |
| Fang Strike (`fang_strike`) | Fangs | Shards | Silver |
| Fang Swirl (`fang_swirl`) | Fangs | Vortex | Silver |
| Fang Ward (`fang_ward`) | Fangs | Sigil | Silver |
| Fire Arrow (`fire_arrow`) | Flare | Rays | Ember |
| Fire Breath (`fire_breath`) | Flare | Helix | Ember |
| Fireball (`fireball`) | Flare | Rays | Ember |
| Firebolt (`firebolt`) | Flare | Motes | Ember |
| Firecracker (`firecracker`) | Rays | Motes | Rose |
| Firefly Swarm (`firefly_swarm`) | Motes | Wings | Sun |
| Flaming Barrage (`flaming_barrage`) | Flare | Sigil | Ember |
| Flaming Strike (`flaming_strike`) | Slash | Flare | Ember |
| Force Arrow (`force_arrow`) | Shards | Helix | Violet |
| Fortify (`fortify`) | Shield | Sigil | Sun |
| Frost Step (`frost_step`) | Shards | Slash | Frost |
| Frostbite (`frostbite`) | Shards | Helix | Frost |
| Frostwave (`frostwave`) | Shards | Ripple | Frost |
| Gluttony (`gluttony`) | Leaves | Helix | Leaf |
| Gravity Fissure (`gravity_fissure`) | Vortex | Helix | Void |
| Greater Heal (`greater_heal`) | Wings | Rays | Spirit |
| Guiding Bolt (`guiding_bolt`) | Rays | Sigil | Sun |
| Gust (`gust`) | Ripple | Leaves | Spirit |
| Haste (`haste`) | Clock | Helix | Sun |
| Heal (`heal`) | Leaves | Rays | Spirit |
| Healing Circle (`healing_circle`) | Sigil | Rays | Sun |
| Heartstop (`heartstop`) | Clock | Helix | Blood |
| Heat Surge (`heat_surge`) | Flare | Rays | Ember |
| Ice Block (`ice_block`) | Shards | Ripple | Frost |
| Ice Spikes (`ice_spikes`) | Shards | Fangs | Frost |
| Ice Tomb (`ice_tomb`) | Shards | Shield | Frost |
| Icicle (`icicle`) | Shards | Helix | Frost |
| Interposing Earth (`interposing_earth`) | Shards | Shield | Earth |
| Invisibility (`invisibility`) | Motes | Vortex | Violet |
| Lightning Bolt (`lightning_bolt`) | Rays | Helix | Storm |
| Lightning Lance (`lightning_lance`) | Shards | Helix | Storm |
| Lob Creeper (`lob_creeper`) | Motes | Rays | Venom |
| Magic Arrow (`magic_arrow`) | Shards | Sigil | Violet |
| Magic Missile (`magic_missile`) | Helix | Motes | Violet |
| Magma Bomb (`magma_bomb`) | Flare | Shards | Ember |
| Oakskin (`oakskin`) | Leaves | Shield | Earth |
| Air Bubble (`pf2_air_bubble`) | Ripple | Motes | Water |
| Arctic Rift (`pf2_arctic_rift`) | Shards | Slash | Frost |
| Bind Undead (`pf2_bind_undead`) | Chain | Sigil | Void |
| Breathe Fire (`pf2_breathe_fire`) | Flare | Ripple | Ember |
| Cataclysm (`pf2_cataclysm`) | Shards | Flare | Sun |
| Caustic Blast (`pf2_caustic_blast`) | Ripple | Motes | Venom |
| Chain Lightning (`pf2_chain_lightning`) | Helix | Rays | Storm |
| Cinder Swarm (`pf2_cinder_swarm`) | Motes | Flare | Ember |
| Clairvoyance (`pf2_clairvoyance`) | Eye | Sigil | Violet |
| Collective Transposition (`pf2_collective_transposition`) | Helix | Sigil | Violet |
| Containment (`pf2_containment`) | Chain | Sigil | Silver |
| Create Water (`pf2_create_water`) | Ripple | Motes | Water |
| Creation (`pf2_creation`) | Sigil | Shards | Silver |
| Detect Magic (`pf2_detect_magic`) | Eye | Rays | Violet |
| Divine Lance (`pf2_divine_lance`) | Rays | Helix | Sun |
| Eclipse Burst (`pf2_eclipse_burst`) | Vortex | Rays | Void |
| Electric Arc (`pf2_electric_arc`) | Helix | Motes | Violet |
| Enfeeble (`pf2_enfeeble`) | Tendrils | Helix | Void |
| Enlarge (`pf2_enlarge`) | Shards | Helix | Earth |
| Falling Stars (`pf2_falling_stars`) | Rays | Motes | Sun |
| False Vitality (`pf2_false_vitality`) | Helix | Sigil | Blood |
| Fear (`pf2_fear`) | Eye | Tendrils | Void |
| Field of Life (`pf2_field_of_life`) | Leaves | Rays | Spirit |
| Figment (`pf2_figment`) | Motes | Eye | Rose |
| Fire Shield (`pf2_fire_shield`) | Flare | Shield | Ember |
| Fireball (`pf2_fireball`) | Flare | Ripple | Ember |
| Fleet Step (`pf2_fleet_step`) | Helix | Leaves | Spirit |
| Flicker (`pf2_flicker`) | Vortex | Motes | Violet |
| Floating Flame (`pf2_floating_flame`) | Flare | Helix | Ember |
| Force Barrage (`pf2_force_barrage`) | Shards | Helix | Violet |
| Freezing Rain (`pf2_freezing_rain`) | Shards | Ripple | Frost |
| Frostbite (`pf2_frostbite`) | Shards | Motes | Frost |
| Gecko Grip (`pf2_gecko_grip`) | Leaves | Sigil | Leaf |
| Gentle Breeze (`pf2_gentle_breeze`) | Ripple | Leaves | Spirit |
| Gentle Landing (`pf2_gentle_landing`) | Wings | Motes | Spirit |
| Glass Shield (`pf2_glass_shield`) | Shards | Shield | Silver |
| Gouging Claw (`pf2_gouging_claw`) | Slash | Fangs | Blood |
| Gravity Well (`pf2_gravity_well`) | Vortex | Sigil | Void |
| Grease (`pf2_grease`) | Ripple | Motes | Sun |
| Grim Tendrils (`pf2_grim_tendrils`) | Tendrils | Helix | Void |
| Harm (`pf2_harm`) | Tendrils | Rays | Void |
| Haste (`pf2_haste`) | Clock | Rays | Sun |
| Heal (`pf2_heal`) | Wings | Leaves | Spirit |
| Hydraulic Push (`pf2_hydraulic_push`) | Ripple | Helix | Water |
| Ignition (`pf2_ignition`) | Flare | Slash | Ember |
| Illusory Creature (`pf2_illusory_creature`) | Eye | Motes | Rose |
| Illusory Object (`pf2_illusory_object`) | Sigil | Eye | Rose |
| Invisibility (`pf2_invisibility`) | Motes | Ripple | Rose |
| Item Facade (`pf2_item_facade`) | Eye | Motes | Silver |
| Lightning Bolt (`pf2_lightning_bolt`) | Rays | Ripple | Storm |
| Magic Passage (`pf2_magic_passage`) | Sigil | Vortex | Earth |
| Magnetic Attraction (`pf2_magnetic_attraction`) | Helix | Shards | Silver |
| Mirror Image (`pf2_mirror_image`) | Motes | Sigil | Rose |
| Mud Pit (`pf2_mud_pit`) | Ripple | Shards | Earth |
| Needle Darts (`pf2_needle_darts`) | Shards | Motes | Silver |
| Peaceful Bubble (`pf2_peaceful_bubble`) | Leaves | Ripple | Spirit |
| Pet Cache (`pf2_pet_cache`) | Sigil | Wings | Violet |
| Protection (`pf2_protection`) | Shield | Wings | Sun |
| Protector Tree (`pf2_protector_tree`) | Leaves | Rays | Leaf |
| Puff of Poison (`pf2_puff_of_poison`) | Motes | Tendrils | Venom |
| Read Aura (`pf2_read_aura`) | Eye | Sigil | Sun |
| Regenerate (`pf2_regenerate`) | Leaves | Helix | Spirit |
| Repulsion (`pf2_repulsion`) | Ripple | Shield | Violet |
| Resist Energy (`pf2_resist_energy`) | Shield | Helix | Frost |
| Revealing Light (`pf2_revealing_light`) | Rays | Eye | Sun |
| Rust Cloud (`pf2_rust_cloud`) | Motes | Shards | Rust |
| Scatter Scree (`pf2_scatter_scree`) | Shards | Rays | Earth |
| See the Unseen (`pf2_see_the_unseen`) | Eye | Motes | Spirit |
| Shape Stone (`pf2_shape_stone`) | Shards | Sigil | Earth |
| Share Life (`pf2_share_life`) | Helix | Leaves | Blood |
| Shield (`pf2_shield`) | Shield | Sigil | Violet |
| Shrink (`pf2_shrink`) | Motes | Helix | Earth |
| Silence (`pf2_silence`) | Ripple | Sigil | Silver |
| Slashing Gust (`pf2_slashing_gust`) | Slash | Ripple | Spirit |
| Slow (`pf2_slow`) | Clock | Chain | Violet |
| Soothe (`pf2_soothe`) | Leaves | Motes | Rose |
| Spirit Blast (`pf2_spirit_blast`) | Rays | Wings | Spirit |
| Spiritual Armament (`pf2_spiritual_armament`) | Slash | Sigil | Sun |
| Spout (`pf2_spout`) | Ripple | Rays | Water |
| Status (`pf2_status`) | Eye | Helix | Spirit |
| Summon Animal (`pf2_summon_animal`) | Leaves | Sigil | Leaf |
| Summon Elemental (`pf2_summon_elemental`) | Shards | Sigil | Frost |
| Summon Fey (`pf2_summon_fey`) | Wings | Motes | Rose |
| Summon Plant or Fungus (`pf2_summon_plant_or_fungus`) | Tendrils | Leaves | Leaf |
| Tangle Vine (`pf2_tangle_vine`) | Tendrils | Leaves | Leaf |
| Telekinetic Projectile (`pf2_telekinetic_projectile`) | Shards | Ripple | Silver |
| Thunderstrike (`pf2_thunderstrike`) | Rays | Helix | Storm |
| Time Jump (`pf2_time_jump`) | Clock | Vortex | Violet |
| Translocate (`pf2_translocate`) | Vortex | Sigil | Violet |
| Vampiric Feast (`pf2_vampiric_feast`) | Fangs | Helix | Blood |
| Vitality Lash (`pf2_vitality_lash`) | Helix | Leaves | Spirit |
| Void Warp (`pf2_void_warp`) | Tendrils | Vortex | Void |
| Wall of Ice (`pf2_wall_of_ice`) | Shards | Motes | Frost |
| Wall of Stone (`pf2_wall_of_stone`) | Shards | Sigil | Earth |
| Wall of Water (`pf2_wall_of_water`) | Ripple | Motes | Water |
| Water Breathing (`pf2_water_breathing`) | Ripple | Helix | Water |
| Water Walk (`pf2_water_walk`) | Ripple | Leaves | Water |
| Weapon Storm (`pf2_weapon_storm`) | Slash | Shards | Silver |
| Wooden Double (`pf2_wooden_double`) | Leaves | Sigil | Earth |
| Zephyr Slip (`pf2_zephyr_slip`) | Wings | Ripple | Spirit |
| Planar Sight (`planar_sight`) | Eye | Vortex | Void |
| Pocket Dimension (`pocket_dimension`) | Vortex | Sigil | Violet |
| Poison Arrow (`poison_arrow`) | Tendrils | Shards | Venom |
| Poison Spray (`poison_breath`) | Tendrils | Ripple | Venom |
| Poison Splash (`poison_splash`) | Ripple | Tendrils | Venom |
| Portal (`portal`) | Vortex | Sigil | Violet |
| Raise Dead (`raise_dead`) | Sigil | Tendrils | Void |
| Raise Hell (`raise_hell`) | Flare | Shards | Ember |
| Ray of Frost (`ray_of_frost`) | Shards | Helix | Frost |
| Ray of Siphoning (`ray_of_siphoning`) | Helix | Tendrils | Blood |
| Recall (`recall`) | Sigil | Rays | Violet |
| Root (`root`) | Tendrils | Leaves | Earth |
| Sacrifice (`sacrifice`) | Rays | Fangs | Blood |
| Scapegoat (`scapegoat`) | Motes | Sigil | Rose |
| Scorch (`scorch`) | Flare | Shards | Ember |
| Sculk Tentacles (`sculk_tentacles`) | Tendrils | Vortex | Sculk |
| Shadow Slash (`shadow_slash`) | Slash | Vortex | Void |
| Shield (`shield`) | Shield | Sigil | Violet |
| Shockwave (`shockwave`) | Ripple | Rays | Storm |
| Slow (`slow`) | Clock | Helix | Violet |
| Snowball (`snowball`) | Motes | Shards | Frost |
| Sonic Boom (`sonic_boom`) | Ripple | Rays | Sculk |
| Spectral Hammer (`spectral_hammer`) | Shards | Slash | Violet |
| Aspect of the Spider (`spider_aspect`) | Tendrils | Motes | Venom |
| Starfall (`starfall`) | Rays | Helix | Violet |
| Stomp (`stomp`) | Shards | Ripple | Earth |
| Summon Ender Chest (`summon_ender_chest`) | Sigil | Motes | Violet |
| Summon Horse (`summon_horse`) | Wings | Sigil | Spirit |
| Summon Polar Bear (`summon_polar_bear`) | Shards | Sigil | Frost |
| Summon Swords (`summon_swords`) | Slash | Motes | Violet |
| Summon Vex (`summon_vex`) | Wings | Sigil | Violet |
| Summon Zombie (`summon_zombie`) | Sigil | Shards | Void |
| Sunbeam (`sunbeam`) | Rays | Helix | Sun |
| Telekinesis (`telekinesis`) | Helix | Sigil | Violet |
| Teleport (`teleport`) | Vortex | Motes | Violet |
| Throw (`throw`) | Shards | Rays | Silver |
| Thunderstorm (`thunderstorm`) | Helix | Rays | Storm |
| Touch Dig (`touch_dig`) | Shards | Sigil | Earth |
| Volt Strike (`volt_strike`) | Slash | Helix | Storm |
| Wall of Fire (`wall_of_fire`) | Flare | Sigil | Ember |
| Wisp (`wisp`) | Motes | Rays | Sun |
| Wither Skull (`wither_skull`) | Tendrils | Motes | Void |
| Wololo (`wololo`) | Eye | Helix | Rose |

Gluttony uses an orbit of leaves and a rising coil, with a separate feeding reaction. Its actual cast demonstration holds the vanilla eat input; one bread consumes a real item and returns thirty native mana. The server displays the actual restored amount after applying the 200-mana cap. Wands, discovery and progression remain deferred.
