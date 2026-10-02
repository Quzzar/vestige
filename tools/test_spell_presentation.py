"""Checks that automatic decoration respects stealth and native phase ordering."""
import unittest
from copy import deepcopy
from spell_presentation import decorate, without_cosmetics
from spell_art import ART
import json
from pathlib import Path
from export_spell_effects import build

class PresentationTest(unittest.TestCase):
    def test_persistent_fields_own_one_hero_visual_and_projectiles_have_impact_feedback(self):
        damage={'type':'damage','values':{'amount':2}}
        area={'effects':[{'type':'create_manifestation','manifestation':{'kind':'vestige:area','duration':160,
              'on_tick':[{'type':'for_each','target':{'selection':'near_target','distance':5},'effects':[damage]}]}}]}
        field=decorate(area,'black_hole')['effects'][0]['manifestation']
        self.assertIn('vortex',[l['shape'] for l in field['visual']['layers']])
        self.assertNotIn('visual',field['on_tick'][0])
        projectile={'effects':[{'type':'create_manifestation','manifestation':{'kind':'vestige:projectile','duration':200,'on_hit':[damage]}}]}
        styled=decorate(projectile,'blood_slash')
        impact=styled['effects'][0]['manifestation']['on_hit'][-1]
        self.assertEqual('visual',impact['type'])
        self.assertIn('slash',[l['shape'] for l in impact['visual']['layers']])
        self.assertEqual(without_cosmetics(projectile),without_cosmetics(styled))

    def test_every_spell_has_an_explicit_distinct_art_direction_and_bounded_layers(self):
        files=list((Path(__file__).resolve().parents[1]/'src/main/resources/data/vestige/runtime_spells').glob('*.json'))
        self.assertEqual({p.stem for p in files},set(ART))
        self.assertEqual(214,len(set(ART.values())))
        self.assertEqual(214,len({(a.motif,a.accent,a.count,a.speed,a.scale) for a in ART.values()}))
        for path in files:
            spell=json.loads(path.read_text())
            def check(node):
                if isinstance(node,dict):
                    if 'visual' in node:
                        self.assertTrue(1<=len(node['visual']['layers'])<=8,path.stem)
                        for layer in node['visual']['layers']:
                            self.assertTrue(1<=layer.get('count',8)<=24,path.stem)
                            self.assertTrue(-3<=layer.get('speed',1)<=3,path.stem)
                    for value in node.values():check(value)
                elif isinstance(node,list):
                    for value in node:check(value)
            check(spell)

    def test_art_direction_only_changes_cosmetics_and_does_not_mutate_the_input(self):
        source={'effects':[{'type':'for_each','target':{'selection':'near_target','distance':3},
                           'effects':[{'type':'damage','values':{'amount':8}}]}], 'traits':{'vestige:fire':1}}
        original=deepcopy(source)
        styled=decorate(source,'fireball')
        self.assertEqual(original,source)
        self.assertEqual(without_cosmetics(source),without_cosmetics(styled))
        self.assertIn('flare',[layer['shape'] for layer in styled['effects'][0]['visual']['layers']])
    def test_invisibility_keeps_transition_cues_without_a_public_tracking_shell(self):
        status={'type':'status','identifiers':{'effect':'minecraft:invisibility'},'values':{'duration':2}}
        plan={'traits':{},'effects':[{'type':'create_manifestation','manifestation':{
            'kind':'vestige:status','duration':160,'on_tick':[{'type':'for_each','target':{'selection':'self'},'effects':[status]}]}}]}
        original=deepcopy(plan)
        styled=decorate(plan)
        self.assertEqual(plan,original)
        lease=styled['effects'][0]['manifestation']
        self.assertNotIn('visual',lease)
        self.assertNotIn('visual',lease['on_tick'][0])
        self.assertEqual('visual',styled['effects'][-1]['type'])

    def test_existing_visual_effect_is_not_decorated_twice(self):
        plan={'traits':{},'effects':[{'type':'for_each','target':{'selection':'self'},
              'effects':[{'type':'visual','visual':{'authored':True}}]}]}
        self.assertEqual(plan,decorate(plan))

    def test_cast_library_does_not_export_illustrative_geometry_and_normalizes_titles(self):
        rows={row['id']:row for row in build()}
        self.assertNotIn('phases',rows['vestige:pf2_summon_elemental'])
        self.assertEqual('Ray of Frost',rows['vestige:ray_of_frost']['name'])

    def test_area_impacts_use_the_authored_footprint_without_changing_damage(self):
        distance={'product':[3,{'trait':'vestige:area'}]}
        damage={'type':'damage','values':{'amount':8}}
        plan={'traits':{'vestige:fire':1},'effects':[{'type':'for_each',
              'target':{'selection':'near_target','distance':distance},'effects':[damage]}]}
        original=deepcopy(plan);styled=decorate(plan)
        self.assertEqual(plan,original)
        self.assertEqual([damage],styled['effects'][0]['effects'])
        self.assertEqual(distance,styled['effects'][0]['visual']['radius'])
        self.assertIn('fire',[layer['shape'] for layer in styled['effects'][0]['visual']['layers']])

if __name__=='__main__': unittest.main()
