"""Authored catalog presentation grammar. Generates explicit visuals; runtime traits have no artistic semantics."""
from copy import deepcopy
import spell_visuals as v
from spell_art import ART, compose

PALETTE={'blood':'eb6684','fire':'ff9d42','ice':'a7ddff','water':'72cafa','lightning':'dfd9ff','acid':'b0db5a',
         'poison':'a4cc64','plant':'88c57c','wood':'bf9b68','earth':'baa389','stone':'c0c9d1','metal':'bacbd9',
         'holy':'fff0b6','unholy':'b88ddd','void':'b397e0','death':'b299d0','life':'8be6c0','spirit':'b4e9f1',
         'air':'c4eeeb','space':'aac8ff','time':'c9beff','sonic':'cad7e5','mind':'e0b6ed','illusion':'d2b8f2'}


def without_cosmetics(node):
    """Discard cosmetic cues for regeneration/comparison, preserving every executable outcome."""
    if isinstance(node,dict):
        return {key:without_cosmetics(value) for key,value in node.items() if key!='visual'}
    if isinstance(node,list):
        return [without_cosmetics(value) for value in node if not isinstance(value,dict) or value.get('type')!='visual']
    return node


def decorate(spell, identity=None):
    spell=deepcopy(spell)
    traits={key.removeprefix('vestige:') for key in spell.get('traits',{})}
    color=next((value for key,value in PALETTE.items() if key in traits),'bbc6f2')
    def invisible(node):
        if isinstance(node,dict):
            return (node.get('type')=='status' and node.get('identifiers',{}).get('effect')=='minecraft:invisibility') or any(invisible(value) for value in node.values())
        if isinstance(node,list): return any(invisible(value) for value in node)
        return False
    hides_body=invisible(spell['effects']) or any(invisible(mode['effects']) for mode in spell.get('modes',[]))
    def walk(effects,quiet=False):
        for effect in effects:
            kind=effect['type'].removeprefix('vestige:')
            if kind=='for_each':
                walk(effect['effects'],quiet)
                if not quiet and 'visual' not in effect and effect['effects'] and not any(e['type'] in ('visual','create_manifestation','teleport','transpose','shape_stone') for e in effect['effects']):
                    selection=effect['target']['selection']
                    if selection=='near_target' and any(e['type']=='damage' for e in effect['effects']):
                        # The impact footprint follows the authored area selector, rather than a generic tiny cue.
                        effect['visual']=v.blast(deepcopy(effect['target'].get('distance',.8)),color,'fire' in traits)
                    else:
                        effect['visual']=v.beam(color) if selection in ('entity_ray','beam','chain','cone','event_attacker') else v.field(12,.8,color)
            elif kind=='create_manifestation':
                m=effect['manifestation']; mk=m['kind'].removeprefix('vestige:'); duration=max(1,min(2400,m['duration'] if m['duration']>0 else 200))
                behavior=m.get('identifiers',{}).get('behavior','').removeprefix('vestige:')
                # Private sensing owns observer-specific cues in its adapter; never broadcast invisible targets.
                if not quiet and 'visual' not in m and not (mk=='sensor' and behavior=='unseen') and not (mk=='status' and hides_body):
                    if mk=='projectile': m['visual']=v.orb(color,'fire' in traits)
                    elif mk in ('status','barrier','guard','mobility'): m['visual']=v.ward(duration,color,mk=='guard' or bool(m.get('bindings')))
                    elif mk=='construct': m['visual']=v.visual(duration,1,v.layer('box',color,.45),v.layer('sparks',color,.5))
                    else: m['visual']=v.field(duration,m.get('values',{}).get('radius',1),color,'fire' in traits)
                # A persistent body already owns its field/shell. Repainting it at every recipient
                # pulse creates extra singularities, roots and wards at unrelated anchors.
                for callback in ('on_hit','on_tick','on_end'): walk(m.get(callback,[]),quiet or callback=='on_tick')
                def visible(node):
                    if isinstance(node,dict):return 'visual' in node or any(visible(value) for value in node.values())
                    if isinstance(node,list):return any(visible(value) for value in node)
                    return False
                if mk=='projectile' and m.get('on_hit') and not quiet and not visible(m['on_hit']):
                    m['on_hit'].append({'type':'visual','visual':v.blast(.9,color,'fire' in traits)})
                for binding in m.get('bindings',[]): walk(binding['effects'])
            elif kind=='install_binding': walk(effect['binding']['effects'])
            elif kind=='branch': walk(effect.get('then',[]),quiet); walk(effect.get('else',[]),quiet)
            elif kind in ('repeat','sequence'): walk(effect['effects'],quiet)
    walk(spell['effects'])
    for mode in spell.get('modes',[]): walk(mode['effects'])
    def has_visual(node):
        if isinstance(node,dict): return 'visual' in node or any(has_visual(value) for value in node.values())
        if isinstance(node,list): return any(has_visual(value) for value in node)
        return False
    if not has_visual(spell['effects']): spell['effects'].append({'type':'visual','visual':v.field(16,1,color)})
    if identity is not None:
        art=ART[identity]
        def style(node,quiet=False):
            if isinstance(node,dict):
                if 'visual' in node:
                    role=node.get('kind','').removeprefix('vestige:')
                    if node.get('type')=='for_each' and any(e.get('type')=='food_mana' for e in node['effects']):role='feedback'
                    node['visual']=compose(node['visual'],art,role=role,quiet=quiet)
                for key,value in node.items():
                    if key!='visual':style(value,quiet or key=='on_tick')
            elif isinstance(node,list):
                for value in node:style(value,quiet)
        style(spell['effects'])
        for mode in spell.get('modes',[]):style(mode['effects'])
    return spell
