# Actual spell casts

The [local gallery](http://127.0.0.1:5175/) shows actual Minecraft casts, with playback controls, MP4 downloads, native descriptions and recorded health/movement. All 214 spells have an actual primary-cast recording, and the full catalog is visible by default. Browser sketches and effect studies have been removed from the gallery and its published media.

Encounters use the native runtime, normal aim, timing and recast inputs. Area scenes place villagers inside and outside a cyan floor boundary; the cyan line is a fixture marking the authored radius, not an additional spell effect. Chain scenes show ordered recipients and an unreachable outsider. Ward scenes apply actual incoming attacks. Healing begins with injured creatures. Formation scenes show growing blocks, player replacements and cleanup. The summoned wolf encounters a hostile, then follows its caster and receives a dismissal recast.

Electric Arc selects at most two hostile creatures for four HP each, with a four-block jump. Pathfinder Chain Lightning selects up to six for twelve HP each, with a five-block jump. The latter uses a longer, thicker blue-white path with tightly coiling strands and rays; Electric Arc uses a compact violet coil with drifting glints. Hydraulic Push uses a turbulent blue water jet, travelling pressure rings and splash particles, dealing seven direct HP (landing can add fall damage) and applying physical knockback. These native values are independent of tabletop rank or source rarity.

Mirror Image projects three copies of the caster's live model, equipment and pose. Each qualifying melee hit consumes one, reducing up to five HP. It remains deterministic finite mitigation rather than random target redirection. Shield displays a six-panel runic shell that breaks when its one-hit ward is consumed.

Protector Tree grows four temporary oak logs and seventeen leaves. Its shared sixteen-HP protection covers up to three captured consenting nearby allies. Missing trunks reduce maximum remaining protection; all surviving owned cells retract after ten seconds. Wall of Water uses real water appearance and immersion/extinguishing physics, confined to its five-by-three owned volume, so no unowned sources spread. Both preserve player replacements, broken cells and holes; neither yields harvested blocks or water. Placement rejects obstructed or unsupported volumes. Ice retains its safe lifting behavior.

Summon Animal creates one owned wolf with sixteen HP, three attack damage and a twenty-second maximum lifetime. Shared minion handling acquires threats/hostiles and follows its owner; recast dismisses it. It is a finite combat companion, with one creature form in this adaptation.

These are original Vestige visuals built from shared procedural rendering, native entities and vanilla Minecraft blocks/particles. Iron supplies behavior references; Electroblob supplies design inspiration. Their original animation code, textures, models and sounds are not imported. Villagers retain vanilla poses. Custom casting gestures and broader multiplayer appearance review remain future work.

The [art-direction ledger](spell-art-direction.md) identifies each spell's main silhouette, secondary movement and material. Thirty-two shared shapes support distinct coiling blood, crystalline frost, surging fire, rooted growth, singularities, tooth/jaw reactions, feathered wings, clocks and sensing eyes. Persistent fields own their main form; tick recipients do not receive duplicate field bodies. Projectile hits have finite feedback even when the impact graph applies damage directly; the built-in damage path also reuses its projectile material for a larger contact burst. Cosmetic changes preserve gameplay tuning. Current capture/verification results are in [development status](development-status.md).

## Record native footage

The opt-in development client creates a disposable flat world in its separate `run/effects-capture` directory, exports Minecraft's rendered framebuffer as PNGs with timestamps/provenance, and exits. Normal clients do not capture. Cast recordings wait for the requested amount of server simulation as well as elapsed time, so a slow client cannot silently truncate an encounter.

```bash
./gradlew runEffectsCapture -Pcapture_kind=cast -Pcapture_spells=pf2_fireball,pf2_chain_lightning -Pcapture_seconds=8 -Pcapture_output=build/new-cast-pass
python3 tools/encode_native_capture.py build/new-cast-pass
python3 tools/encode_native_capture.py --check --complete
python3 tools/export_spell_effects.py --check
```

Use a fresh output directory for every pass. Omit `capture_spells` to select the whole catalog; the scene supplies eligible creatures, allied/owned targets, held items, containers, stone, water and contact walls from the primary plan. Player-only spells record their real native inventory, information or camera view. Longer formation and summon scenes select a minimum lifecycle length automatically. Raw frames stay in ignored build output. Published MP4s are H.264, silent, 960×540; timestamp-based encoding preserves the actual elapsed frame spacing rather than claiming a constant capture rate. The manifest validates definition/video hashes and rejects failed casts. Later supplied capture directories supersede earlier takes of the same identity. Renderer changes require recapturing affected clips. Video hashes version playback URLs to avoid stale caches.

`--complete` requires an actual cast for every spell and rejects incomplete collections. The published 214-spell gallery passes this check. Each recording demonstrates one primary-plan encounter; it does not cover every optional mode or gameplay situation. Current counts and executed results are in [development status](development-status.md).

**Cooldown-removal revision, October 8:** the operator recordings bypass cooldowns. Their original metadata/video hashes remain intact, and [214 exact recorded definitions](../tools/recorded-spell-definitions/README.md) preserve the bytes named by those hashes. Verification accepts current definitions only when they are identical to that evidence with explicit spell/mode cooldown cost entries removed. Effects, visuals, preparation, mana and all other changes still reject. This proves compatibility with the earlier cooldown removal; it does not claim a fresh capture or survival payment verification.

## Cast in Minecraft

```text
/vestige_magic cast vestige:pf2_fireball
/vestige_magic cast vestige:pf2_summon_elemental vestige:air
/vestige_magic dispel
/vestige_magic mana 200
/vestige_magic cast_balanced vestige:pf2_wall_of_water
```

Construction needs safe open ground; targeted spells need eligible creatures. Same-team players opt into cooperative movement/protection with `/vestige_magic accept_magic true`. Wands/discovery/progression remain deferred. The developer-only `/vestige_magic effects <spell> [phase]` diagnostic still inspects cosmetic cues separately; it is not part of the cast viewer. See [spell breakdowns](spell-reference.md) and [conversion differences](design/pathfinder-spell-conversions.md).
