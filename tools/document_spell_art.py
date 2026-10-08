#!/usr/bin/env python3
"""Document the explicit art directions and reject drift between the ledger and catalog."""
import argparse
from pathlib import Path
import json
from spell_art import ART
from spell_title import spell_title

ROOT=Path(__file__).resolve().parents[1]

def document():
    files={p.stem:p for p in (ROOT/'src/main/resources/data/vestige/runtime_spells').glob('*.json')}
    assert set(files)==set(ART), 'Every native spell needs an explicit art direction'
    assert len({(a.motif,a.accent,a.count,a.speed,a.scale) for a in ART.values()})==len(ART), 'Duplicate silhouette/rhythm direction'
    rows=['# Spell art direction', '',
          'For item sprites, block/worn textures and GUI artwork, follow [Minecraft art and pixel scale](design/minecraft-art.md). The spell-effect compositions below have a separate scope.', '',
          'All 214 spells have an explicitly authored combination of silhouette, secondary movement, density, rhythm and scale. Related spells share materials and a visual vocabulary while retaining different compositions. This table describes the intended native rendering; actual appearance is reviewed through Minecraft cast footage in the [gallery](effects-workshop.md). Distinct recipe data alone does not establish artistic quality.', '',
          'The visual brief is readable magic at ordinary encounter distance: a recognizable form, motion tied to its purpose, a visible impact or reaction, and a finite end. Fire rises and surges; frost grows sharp facets; blood coils or cuts; plants climb and unfurl; force bends and forms sigils; protective magic has panels or substantial material. Information and stealth spells use restrained transitions and private cues so ornament does not reveal hidden bodies.', '',
          'Iron\'s [Black Hole renderer](https://github.com/iron431/irons-spells-n-spellbooks/blob/1.21/src/main/java/io/redspace/ironsspellbooks/entity/spells/black_hole/BlackHoleRenderer.java) and [Devour jaw renderer](https://github.com/iron431/irons-spells-n-spellbooks/blob/1.21/src/main/java/io/redspace/ironsspellbooks/entity/spells/devour_jaw/DevourJawRenderer.java) were reviewed as examples of layered motion and recognizable silhouettes. Vestige authors its own geometry and compositions. Iron\'s [license](https://github.com/iron431/irons-spells-n-spellbooks/blob/e4056af90302d37eb1739f5ff05020b020e6e252/LICENSE.md) reserves assets; none were imported. Existing attribution remains in [CREDITS](../CREDITS.md).', '',
          'The data authoring table is [spell_art.py](../tools/spell_art.py). Shared shapes are described in [presentation design](design/spell-presentation-and-expansion.md). The client receives ordinary bounded layers; it does not choose artwork from spell IDs or traits. Gameplay coefficients, targets, costs, rarity and relative trait units remain independent of artwork.', '',
          '| Spell | Main silhouette | Secondary motion | Material |', '|---|---|---|---|']
    for name in sorted(ART):
        definition=json.loads(files[name].read_text()); title=spell_title(definition.get('source',{}).get('name',name.replace('_',' ').title()));art=ART[name]
        rows.append(f'| {title} (`{name}`) | {art.motif.title()} | {art.accent.title()} | {art.palette.title()} |')
    rows+=['', 'Gluttony uses an orbit of leaves and a rising coil, with a separate feeding reaction. Its actual cast demonstration holds the vanilla eat input; one bread consumes a real item and returns thirty native mana. The server displays the actual restored amount after applying the 200-mana cap. Wands, discovery and progression remain deferred.', '']
    return '\n'.join(rows)

if __name__=='__main__':
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--check',action='store_true');args=parser.parse_args()
    path=ROOT/'docs/spell-art-direction.md';text=document()
    if args.check:assert path.read_text()==text, 'Stale spell art ledger'
    else:path.write_text(text)
    print(('Verified' if args.check else 'Wrote')+' 214 explicit spell art directions.')
