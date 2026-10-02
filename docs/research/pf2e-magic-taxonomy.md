# Pathfinder Second Edition Remaster: traditions, domains, and spellshape

Research checked against Archives of Nethys and Paizo on 2026-09-14. This note describes Pathfinder's current rules vocabulary; any recommendation for Vestige is labeled as an adaptation rather than a Pathfinder rule.

## Short answer

- Pathfinder has four magical traditions: **arcane, divine, occult, and primal**. A tradition identifies a spellcasting source/list, and the class or ability granting a spell normally determines the tradition used to cast it. ([Player Core: Magical Traditions and Focus Spells](https://2e.aonprd.com/Rules.aspx?ID=2221))
- Pathfinder does **not** place domains beneath those four traditions. Domains are subjects within a religion and are associated primarily with deities and faith. A cleric's domain spell is divine because the cleric is the source granting that focus spell, not because the domain itself belongs to a global "divine domain" category. ([Cleric](https://2e.aonprd.com/Classes.aspx?ID=33), [Domain Initiate](https://2e.aonprd.com/Feats.aspx?ID=4644), [focus-spell traditions](https://2e.aonprd.com/Rules.aspx?ID=2221))
- The current remastered Archives of Nethys corpus contains **61 domains**: 37 in *Player Core* and 24 in *Lost Omens: Divine Mysteries*. ([Player Core source index](https://2e.aonprd.com/Sources.aspx?ID=216), [Divine Mysteries source index](https://2e.aonprd.com/Sources.aspx?ID=234), [domain index](https://2e.aonprd.com/Domains.aspx))
- Yes, the Remaster renamed **metamagic** to **spellshape**. Paizo's terminology table states that directly; the current trait describes actions that modify the next spell when used immediately before casting it. ([Paizo Remaster Core Preview, p. 2](https://downloads.paizo.com/RemasterCorePreview.pdf), [Spellshape trait](https://2e.aonprd.com/Traits.aspx?ID=513))

## Complete current domain list

### Player Core (37)

Air, Ambition, Cities, Confidence, Creation, Darkness, Death, Destruction, Dreams, Earth, Family, Fate, Fire, Freedom, Healing, Indulgence, Knowledge, Luck, Magic, Might, Moon, Nature, Nightmares, Pain, Passion, Perfection, Protection, Secrecy, Sun, Travel, Trickery, Truth, Tyranny, Undeath, Water, Wealth, Zeal.

Source: [Archives of Nethys, Player Core domain entries](https://2e.aonprd.com/Sources.aspx?ID=216).

### Divine Mysteries (24)

Abomination, Change, Cold, Decay, Disorientation, Dragon, Dust, Duty, Glyph, Introspection, Lightning, Metal, Naga, Nothingness, Plague, Repose, Sorrow, Soul, Star, Swarm, Time, Toil, Vigil, Wood.

Source: [Archives of Nethys, Divine Mysteries domain entries](https://2e.aonprd.com/Sources.aspx?ID=234).

## What a domain does in Pathfinder

A domain is described as a subject of particular interest within a religion. A deity lists domains, and the cleric feat **Domain Initiate** selects one from that deity's list and grants its initial domain spell. Domain spells are focus spells, paid for with Focus Points rather than spell slots. **Advanced Domain** later grants the corresponding advanced domain spell. ([Domain Initiate](https://2e.aonprd.com/Feats.aspx?ID=4644), [Advanced Domain](https://2e.aonprd.com/Feats.aspx?ID=4666))

This means domains are neither subclasses nor spell lists in Pathfinder. They are compact thematic packages—normally an initial and advanced focus spell—made available through a deity or another rule that explicitly grants domain access. Individual domain pages also enumerate associated deities; the [Knowledge domain](https://2e.aonprd.com/Domains.aspx?ID=80) is a representative example.

### Primary and alternate domains

"Primary" and "alternate" describe a domain's relationship to a particular deity, not two intrinsic categories of domain. Each deity normally grants four primary domains. Alternate domains represent lesser-known or peripheral aspects of that deity and are not available by default. **Expanded Domain Initiate** grants access to one domain on the deity's alternate list. ([Divine Mysteries: Alternate Domains](https://2e.aonprd.com/Rules.aspx?ID=801), [Expanded Domain Initiate](https://2e.aonprd.com/Feats.aspx?ID=7595))

Consequently, the same domain can be primary for one deity and alternate for another. Paizo's current deity table makes this relational structure explicit with separate `DOMAINS` and `ALTERNATE DOMAINS` columns. ([Divine Mysteries supplemental deity table](https://downloads.paizo.com/PZO13003_SupplementalGodTable.pdf))

**Splinter Faith** is a controlled exception: it lets a cleric or champion choose four domains from the deity's primary and alternate lists, plus at most one otherwise unlisted but non-anathematic domain, and treats the unchosen primary domains as alternate domains. ([Splinter Faith](https://2e.aonprd.com/Feats.aspx?ID=7596))

### Apocryphal domain spells

Apocryphal domain spells are another, separate axis. With GM permission, an unusual sect can substitute an apocryphal initial or advanced focus spell for the standard spell of an existing domain. They do not form another list of domains. ([Apocryphal Domain Spells](https://2e.aonprd.com/Rules.aspx?ID=3510))

## Spellshape, formerly metamagic

The current term is **spellshape**. A spellshape action tweaks a spell and must be used directly before casting that spell; an intervening action or the end of the turn loses the benefit. Each spellshape option defines which spells it can modify and the resulting change. ([Spellshape trait](https://2e.aonprd.com/Traits.aspx?ID=513))

Paizo's Remaster preview explicitly maps `Metamagic` to `Spellshape`, and the compatibility FAQ directs older options using the metamagic trait to use spellshape. ([Remaster Core Preview](https://downloads.paizo.com/RemasterCorePreview.pdf), [Remaster FAQ](https://paizo.com/pathfinder/remaster/faq))

Examples include [Conceal Spell](https://2e.aonprd.com/Feats.aspx?ID=4997), [Energy Ablation](https://2e.aonprd.com/Feats.aspx?ID=5026), [Dousing Spell](https://2e.aonprd.com/Feats.aspx?ID=4341), and [Overwhelming Energy](https://2e.aonprd.com/Feats.aspx?ID=4743).

## Implications for Vestige

Using domains as Vestige's paths is a reasonable adaptation, but assigning each domain to exactly one tradition would be an original Vestige rule, not something inherited from Pathfinder. Pathfinder's structure is closer to:

`caster/source -> tradition` and `deity/faith -> available domains -> focus spells`

That distinction matters for concepts such as Air, Earth, Fire, Water, Nature, Dreams, Soul, and Magic, all of which could plausibly support more than one Vestige tradition. A many-to-many mapping—where a domain can have one or more permitted traditions, while a particular path binds one domain to one tradition—would preserve more mix-and-match space than globally nesting every domain under exactly one tradition.

Likewise, **method** and **spellshape** should remain separate design axes. A method can describe how magic is channeled or paid for (blood, runes, song, sacrifice, crafted implements), while a spellshape is an individual modification to a cast spell (reach, area, concealment, energy conversion, chaining, and so on). In a crafting-focused Minecraft system, spellshapes are a natural fit for socketed runes, garment threads, focus attachments, or inscriptions.
