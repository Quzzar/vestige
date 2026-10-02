"""Guard against showing stale or incomplete native footage as current spell art."""
import json
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch
import encode_native_capture as capture


class NativeCaptureVerification(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        root = Path(self.temp.name)
        media = root / 'media'; media.mkdir()
        self.source = root / 'src/main/resources/data/vestige/runtime_spells/example.json'
        self.source.parent.mkdir(parents=True); self.source.write_text('{"effects":[]}')
        catalog = root / 'tools/effects-viewer/src/catalog.json'
        catalog.parent.mkdir(parents=True)
        catalog.write_text(json.dumps([{'id': 'vestige:example'},{'id':'vestige:pending'}]))
        self.video = media / 'vestige-example-cast-0.mp4'; self.video.write_bytes(b'fixture')
        self.row = {'spell': 'vestige:example', 'kind': 'cast', 'outcome':'COMPLETED', 'phase': 0, 'frames': 3, 'seconds': .5,
                    'castContext':{'scenario':'Native test','subjects':[{'role':'Caster'}]}, 'audio': False, 'url': '/native/' + self.video.name,
                    'definitionSha256': capture.digest(self.source), 'videoSha256': capture.digest(self.video)}
        self.addCleanup(patch.stopall)
        patch.object(capture, 'ROOT', root).start()
        patch.object(capture, 'OUTPUT', media).start()

    def test_a_changed_spell_cannot_keep_its_previous_native_video(self):
        self.assertEqual(1, capture.check([self.row], False))
        self.source.write_text('{"effects":[{"type":"new"}]}')
        with self.assertRaisesRegex(AssertionError, 'predates spell definition'):
            capture.check([self.row], False)

    def test_replaced_video_bytes_are_detected(self):
        self.video.write_bytes(b'replaced')
        with self.assertRaisesRegex(AssertionError, 'Changed video'):
            capture.check([self.row], False)

    def test_a_partial_collection_cannot_pass_complete_verification(self):
        with self.assertRaisesRegex(AssertionError, 'Missing 1 actual cast clips'):
            capture.check([self.row], True)

    def test_duplicate_and_out_of_range_phases_are_rejected(self):
        with self.assertRaisesRegex(AssertionError, 'Duplicate'):
            capture.check([self.row, self.row], False)
        with self.assertRaises(AssertionError):
            capture.check([self.row | {'kind':'replay'}], False)

    def test_failed_casts_cannot_be_presented_as_demonstrations(self):
        with self.assertRaisesRegex(AssertionError,'did not execute successfully'):
            capture.check([self.row | {'kind':'cast','outcome':'EFFECT_FAILED'}],False)


if __name__ == '__main__':
    unittest.main()
