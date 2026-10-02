#!/usr/bin/env python3
"""Behavior tests for symbolic balance math and composed boosts."""
import json
from pathlib import Path
import sys
import unittest

sys.dont_write_bytecode = True
from audit_spell_balance import analyze, polynomial, response
from document_spell_balance import direct_ceiling


class BalanceMathTest(unittest.TestCase):
    def setUp(self):
        self.policy = json.loads((Path(__file__).parent / 'spell-balance-policy.json').read_text())
        self.traits = {'vestige:evocation': 4, 'vestige:fire': 4, 'vestige:amplify': 1, 'vestige:range': 1, 'vestige:area': 1}

    def test_doubling_an_unused_school_does_not_change_damage(self):
        boosted = dict(self.traits, **{'vestige:evocation': 8})
        damage = {'product': [10, {'trait': 'vestige:amplify'}]}
        self.assertEqual(1, response(polynomial(damage, self.traits), polynomial(damage, boosted)))

    def test_world_facts_cancel_only_when_the_response_is_context_independent(self):
        echo = {'product': [0.75, {'fact': 'vestige:event/damage_amount'}, {'trait': 'vestige:amplify'}]}
        boosted = dict(self.traits, **{'vestige:amplify': 2})
        self.assertEqual(2, response(polynomial(echo, self.traits), polynomial(echo, boosted)))
        mixed = {'sum': [echo, {'fact': 'vestige:actor/weapon_damage'}]}
        self.assertIsNone(response(polynomial(mixed, self.traits), polynomial(mixed, boosted)))

    def test_fact_products_and_sums_are_preserved_without_guessing_world_values(self):
        expression = {'product': [{'sum': [2, {'fact': 'vestige:health'}]}, {'fact': 'vestige:health'}]}
        self.assertEqual({('vestige:health',): 2, ('vestige:health', 'vestige:health'): 1}, polynomial(expression, {}))

    def test_zero_is_unchanged_without_dividing_by_zero(self):
        zero = polynomial({'product': [10, {'trait': 'addon:absent'}]}, {})
        self.assertEqual(1, response(zero, zero))
        self.assertIsNone(response(zero, {(): 1}))

    def test_fireball_boosts_record_numeric_and_spatial_changes_separately(self):
        path = Path(__file__).parents[1] / 'src/main/resources/data/vestige/runtime_spells/fireball.json'
        report = analyze('fireball', json.loads(path.read_text()), self.policy)
        area = report['scenarios']['double:vestige:area']
        self.assertEqual(2, area['max_exact_numeric_ratio'])
        self.assertEqual(4, area['max_planar_coverage_proxy'])
        self.assertEqual(8, area['max_volume_coverage_proxy'])
        self.assertEqual(4, report['scenarios']['amplify_stacked_2x']['max_exact_numeric_ratio'])
        self.assertEqual(1, report['scenarios']['double:vestige:evocation']['max_exact_numeric_ratio'])

    def test_large_addon_rating_is_evaluated_through_its_formula_without_a_budget(self):
        spell = {'rarity': 'common', 'traits': {'addon:crystal': 1000},
                 'effects': [{'type': 'damage', 'values': {'amount': {'product': [0.001, {'trait': 'addon:crystal'}]}}}]}
        report = analyze('crystal', spell, self.policy)
        self.assertEqual([{'facts': [], 'coefficient': 1}], report['baseline_parameters'][0]['terms'])
        self.assertEqual(2, report['scenarios']['double:addon:crystal']['max_exact_numeric_ratio'])
        self.assertNotIn('base', report)

    def test_equivalent_scales_preserve_baseline_and_multiplicative_boost_response(self):
        for rating, coefficient in [(1, 1), (5, 0.2), (100, 0.01)]:
            expression = {'product': [coefficient, {'trait': 'vestige:fire'}, {'fact': 'vestige:factor'}]}
            baseline = polynomial(expression, {'vestige:fire': rating})
            doubled = polynomial(expression, {'vestige:fire': rating * 2})
            self.assertEqual({('vestige:factor',): 1}, baseline)
            self.assertEqual(2, response(baseline, doubled))

    def test_fixed_additions_need_compensation_when_trait_units_change(self):
        low = {'product': [1, {'trait': 'vestige:fire'}]}
        high = {'product': [0.01, {'trait': 'vestige:fire'}]}
        self.assertEqual(polynomial(low, {'vestige:fire': 1}), polynomial(high, {'vestige:fire': 100}))
        self.assertEqual(2, response(polynomial(low, {'vestige:fire': 1}), polynomial(low, {'vestige:fire': 2})))
        self.assertAlmostEqual(1.01, response(polynomial(high, {'vestige:fire': 100}), polynomial(high, {'vestige:fire': 101})))
        self.assertEqual(2, response(polynomial(high, {'vestige:fire': 100}), polynomial(high, {'vestige:fire': 200})))

    def test_repeated_deliveries_respect_per_creature_caps(self):
        for name,expected in [('burning_dash',6),('volt_strike',12),('arrow_volley',12),('starfall',40),('telekinesis',12)]:
            with self.subTest(spell=name):
                spell=self.definition(name)
                self.assertEqual(expected,direct_ceiling(spell)['damage'])

    def test_channels_and_fields_compare_cumulative_outcomes_not_trait_totals(self):
        for name,damage,healing in [('sunbeam',24,0),('earthquake',24,0),('healing_circle',0,12),('ray_of_siphoning',16,8),('thunderstorm',60,0)]:
            with self.subTest(spell=name):
                spell=self.definition(name)
                self.assertEqual({'damage':damage,'healing':healing},direct_ceiling(spell))
                self.assertEqual({'damage':damage*2,'healing':healing*2},direct_ceiling(spell,2))

    def test_weapon_terms_preserve_their_external_units(self):
        self.assertEqual(7,direct_ceiling(self.definition('throw'))['damage'])
        self.assertEqual(8,direct_ceiling(self.definition('throw'),2)['damage'])
        self.assertEqual(9,direct_ceiling(self.definition('divine_smite'))['damage'])
        self.assertEqual(15,direct_ceiling(self.definition('divine_smite'),2)['damage'])

    def test_pathfinder_mutually_exclusive_branches_do_not_add_damage_or_omit_healing(self):
        self.assertEqual({'damage':6,'healing':0},direct_ceiling(self.definition('pf2_ignition')))
        self.assertEqual({'damage':12,'healing':6},direct_ceiling(self.definition('pf2_field_of_life')))
        self.assertEqual({'damage':24,'healing':12},direct_ceiling(self.definition('pf2_field_of_life'),2))

    def test_pathfinder_meteor_caps_and_multiphase_channel_resolve_separately(self):
        self.assertEqual(30,direct_ceiling(self.definition('pf2_falling_stars'))['damage'])
        self.assertEqual(60,direct_ceiling(self.definition('pf2_falling_stars'),2)['damage'])
        self.assertEqual(32,direct_ceiling(self.definition('pf2_cataclysm'))['damage'])

    def test_expansion_dwell_healing_is_once_and_recipient_branches_are_exclusive(self):
        for name in ('pf2_heal','pf2_harm'):
            self.assertEqual({'damage':8,'healing':8},direct_ceiling(self.definition(name)))
            self.assertEqual({'damage':16,'healing':16},direct_ceiling(self.definition(name),2))
        self.assertEqual({'damage':0,'healing':6},direct_ceiling(self.definition('pf2_gentle_breeze')))
        self.assertEqual({'damage':0,'healing':12},direct_ceiling(self.definition('pf2_gentle_breeze'),2))

    def test_expansion_elemental_clouds_have_finite_per_creature_ceilings(self):
        for name in ('pf2_rust_cloud','pf2_cinder_swarm'):
            self.assertEqual(10,direct_ceiling(self.definition(name))['damage'])
            self.assertEqual(20,direct_ceiling(self.definition(name),2)['damage'])

    def test_support_construct_budgets_and_destruction_outcomes_are_counted_once(self):
        self.assertEqual({'damage':0,'healing':6},direct_ceiling(self.definition('pf2_summon_plant_or_fungus')))
        self.assertEqual({'damage':0,'healing':12},direct_ceiling(self.definition('pf2_summon_plant_or_fungus'),2))
        self.assertEqual({'damage':0,'healing':15},direct_ceiling(self.definition('pf2_summon_plant_or_fungus'),4))
        self.assertEqual(0,direct_ceiling(self.definition('pf2_wall_of_ice'))['damage'])
        self.assertEqual(0,direct_ceiling(self.definition('pf2_wall_of_ice'),2)['damage'])

    def test_alternative_modes_compare_outcomes_instead_of_summing_budgets(self):
        elemental=self.definition('pf2_summon_elemental')
        self.assertEqual(12,direct_ceiling(elemental)['damage'])
        # A larger shared pool supports more victims; it cannot add pulses to one.
        self.assertEqual(12,direct_ceiling(elemental,2)['damage'])
        spell={'traits':{},'effects':[{'type':'damage','values':{'amount':4}}],
               'modes':[{'effects':[{'type':'damage','values':{'amount':7}}]}]}
        self.assertEqual(7,direct_ceiling(spell)['damage'])

    @staticmethod
    def definition(name):
        return json.loads((Path(__file__).parents[1]/f'src/main/resources/data/vestige/runtime_spells/{name}.json').read_text())


if __name__ == '__main__':
    unittest.main()
