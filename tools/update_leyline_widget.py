#!/usr/bin/env python3
"""Embed the accepted design's canonical rules/math into an existing literal fragment."""
import argparse
import json
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
RULES = ROOT / 'docs/design/leyline-calculator-v3.json'
MATH = ROOT / 'tools/leyline_calculator.mjs'


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('fragment', type=Path)
    parser.add_argument('--check', action='store_true')
    args = parser.parse_args()
    source = args.fragment.read_text()
    rules = json.dumps(json.loads(RULES.read_text()), indent=2) + '\n'
    script = re.sub(r'^export ', '', MATH.read_text(), flags=re.MULTILINE).rstrip()
    rule_pattern = r'(<script type="application/json" id="vestige-leyline-rules">\n).*?(</script>)'
    result, count = re.subn(rule_pattern, lambda match: match[1] + rules + match[2], source, count=1, flags=re.DOTALL)
    if count != 1:
        raise SystemExit('Missing embedded rules')
    begin, end = '// BEGIN shared leyline matrix math', '// END shared leyline matrix math'
    math_pattern = re.escape(begin) + r'.*?' + re.escape(end)
    result, count = re.subn(math_pattern, lambda _: begin + '\n' + script + '\n' + end, result, count=1, flags=re.DOTALL)
    if count != 1:
        raise SystemExit('Missing shared math markers')
    if args.check:
        if result != source:
            raise SystemExit('Widget rules/math drift; run update_leyline_widget.py')
        print('Embedded rules and matrix math agree')
    else:
        args.fragment.write_text(result)
        print('Updated embedded rules and matrix math')


if __name__ == '__main__':
    main()
