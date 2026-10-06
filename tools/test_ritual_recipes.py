#!/usr/bin/env python3
"""Catalog contracts for unreferenced scroll crafting and optional materials."""
from copy import deepcopy
import json
import sys
import unittest

sys.dont_write_bytecode = True
import author_ritual_recipes as author


class RitualCatalogTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.outputs = author.outputs()
        cls.recipes = {
            json.loads(value)['spell']: json.loads(value)
            for path, value in cls.outputs.items() if path.parent == author.RECIPE_DIR
        }
        cls.ids = set(cls.recipes)

    def changed_catalog(self, *changed):
        changed_by_id = {r['spell']: r for r in changed}
        return [deepcopy(changed_by_id.get(identity, recipe))
                for identity, recipe in self.recipes.items()]

    def test_checked_in_catalog_covers_every_spell_and_has_no_drift(self):
        expected_ids = {'vestige:' + p.stem for p in author.SPELL_DIR.glob('*.json')}
        self.assertEqual(expected_ids, self.ids)
        self.assertEqual({p.name for p in author.RECIPE_DIR.glob('*.json')},
                         {p.name for p in self.outputs if p.parent == author.RECIPE_DIR})
        for path, value in self.outputs.items():
            with self.subTest(file=path.name):
                self.assertEqual(value, path.read_text())

    def test_catalog_uses_all_allowed_complexities_with_exactly_one_paper(self):
        self.assertEqual({4, 5, 6, 7, 8}, {len(r['parts']) for r in self.recipes.values()})
        for recipe in self.recipes.values():
            paper = [p for p in recipe['parts'] if 'minecraft:paper' in p['ingredient']['items']]
            self.assertEqual(1, len(paper))
            self.assertEqual(['minecraft:paper'], paper[0]['ingredient']['items'])
            self.assertNotIn('supporting_blocks', recipe)
            self.assertNotIn('required_knowledge', recipe)
            self.assertNotIn('required_reference', recipe)

    def test_rotations_preserve_pedestal_types_and_required_empty_positions(self):
        for recipe in self.recipes.values():
            base = author.layout(recipe)
            for turn in range(4):
                turned = author.layout(recipe, turn)
                self.assertEqual(sum(bool(v) for v in base), sum(bool(v) for v in turned))
                for seat in range(8):
                    self.assertEqual(base[seat], turned[(seat + turn * 2) % 8])
                self.assertTrue(all(turned[s] for s in (0, 2, 4, 6)))
                self.assertEqual(len(recipe['parts']) - 4, sum(bool(turned[s]) for s in (1, 3, 5, 7)))

    def test_mirroring_a_directional_recipe_does_not_match_its_rotations(self):
        original = deepcopy(self.recipes['vestige:firebolt'])
        mirrored = deepcopy(original)
        for part in mirrored['parts']:
            part['seat'] = -part['seat'] % original['circle']
        self.assertFalse(any(author.layouts_overlap(author.layout(original), author.layout(mirrored, turn))
                             for turn in range(4)))

    def test_quarter_turn_collision_is_rejected_without_a_reference(self):
        firebolt = deepcopy(self.recipes['vestige:firebolt'])
        firebolt['spell'] = 'vestige:fireball'
        for part in firebolt['parts']:
            part['seat'] = (part['seat'] + 1) % 4
        with self.assertRaisesRegex(ValueError, 'Ambiguous recipes'):
            author.validate_recipes(self.changed_catalog(firebolt), self.ids)

    def test_optional_item_overlap_is_rejected_even_when_vanilla_routes_differ(self):
        changed = []
        for identity, primary in [('vestige:firebolt', 'minecraft:flint'), ('vestige:fireball', 'minecraft:gunpowder')]:
            recipe = deepcopy(self.recipes[identity])
            part = next(p for p in recipe['parts'] if p['ingredient']['items'][0] == primary)
            part['ingredient']['items'].append('irons_spellbooks:blank_rune')
            changed.append(recipe)
        with self.assertRaisesRegex(ValueError, 'Ambiguous recipes'):
            author.validate_recipes(self.changed_catalog(*changed), self.ids)

    def test_a_paper_substitute_cannot_avoid_consuming_paper(self):
        recipe = deepcopy(self.recipes['vestige:firebolt'])
        recipe['parts'][0]['ingredient']['items'].append('minecraft:book')
        with self.assertRaisesRegex(ValueError, 'exactly one Paper'):
            author.validate_recipes(self.changed_catalog(recipe), self.ids)

    def test_a_foreign_only_route_is_rejected(self):
        recipe = deepcopy(self.recipes['vestige:firebolt'])
        recipe['parts'][1]['ingredient']['items'] = ['irons_spellbooks:fire_rune']
        with self.assertRaisesRegex(ValueError, 'vanilla baseline'):
            author.validate_recipes(self.changed_catalog(recipe), self.ids)

    def test_a_nonexistent_pinned_foreign_item_is_rejected(self):
        recipe = deepcopy(self.recipes['vestige:firebolt'])
        recipe['parts'][1]['ingredient']['items'].append('irons_spellbooks:eldritch_rune')
        with self.assertRaisesRegex(ValueError, 'unsupported optional item'):
            author.validate_recipes(self.changed_catalog(recipe), self.ids)

    def test_an_advanced_recipe_cannot_replace_an_inner_stone_slot(self):
        recipe = deepcopy(self.recipes['vestige:portal'])
        part = next(p for p in recipe['parts'] if p['seat'] == 2)
        part['seat'] = next(s for s in range(8) if s not in {p['seat'] for p in recipe['parts']})
        with self.assertRaisesRegex(ValueError, 'all four Stone'):
            author.validate_recipes(self.changed_catalog(recipe), self.ids)

    def test_catalog_missing_a_spell_is_rejected(self):
        with self.assertRaisesRegex(ValueError, 'cover exactly'):
            author.validate_recipes(list(self.recipes.values())[1:], self.ids)


if __name__ == '__main__':
    unittest.main()
