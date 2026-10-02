"""Reusable data-authoring helpers for native spells; no source catalog dependencies."""

def trait(name): return {'trait': f'vestige:{name}'}
def fact(name): return {'fact': f'vestige:{name}'}
def mul(*args): return {'product': list(args)}
def add(*args): return {'sum': list(args)}
def amp(n): return mul(n, trait('amplify'))
def rng(n): return mul(n, trait('range'))
def area(n): return mul(n, trait('area'))
def action(kind, ids=None, **values):
    result = {'type': kind}
    if values: result['values'] = values
    if ids: result['identifiers'] = ids
    return result

def target(selection='current', distance=0, relationship='any', required=True, **options):
    if selection in ('near_target', 'nearby_entities', 'beam', 'cone', 'melee'):
        options.setdefault('count', 6 if selection in ('near_target', 'nearby_entities') else 4)
    result = {'selection': selection, 'required': required}
    if distance: result['distance'] = distance
    if relationship != 'any': result['relationship'] = relationship
    if options: result['options'] = options
    return result

def each(spec, *effects, visual=None):
    result = {'type': 'for_each', 'target': spec, 'effects': list(effects)}
    if visual is not None: result['visual'] = visual
    return result
def near(radius, *effects, relationship='hostile', **options):
    return each(target('near_target', area(radius), relationship, False, **options), *effects)
def aim(distance, *effects): return each(target('aimed_position', rng(distance)), *effects)
def ray(distance, *effects, relationship='hostile', required=True):
    return each(target('entity_ray', rng(distance), relationship, required), *effects)
def self_(*effects): return each(target('self'), *effects)
def damage(n, **values): return action('damage', amount=amp(n), ignore_invulnerability=values.pop('ignore_invulnerability',1), **values)
def heal(n): return action('heal', amount=amp(n))
def status(effect, duration=100, amplifier=0):
    return action('status', {'effect': f'minecraft:{effect}'}, duration=duration, amplifier=amplifier)
def freeze(): return action('freeze', ticks=140)
def ignite(): return action('ignite', seconds=2)
def leech(fraction=.25): return action('leech', fraction=fraction)
def compare(path, expected, operation='equal'):
    return {'type': 'compare', 'path': f'vestige:{path}', 'operation': operation, 'expected': expected}
def branch(condition, *effects): return {'type': 'branch', 'condition': condition, 'then': list(effects)}
def repeat(count, interval, *effects): return {'type': 'repeat', 'count': count, 'interval': interval, 'effects': list(effects)}
def wait(ticks): return {'type': 'await_recast', 'timeout': ticks}
def store(key): return {'type': 'store_target', 'key': f'vestige:{key}'}
def binding(name, event, effects, duration=200, charges=10000, conditions=()):
    return {'id': f'vestige:{name}', 'duration': duration, 'charges': charges,
            'triggers': [{'id': f'vestige:{name}/trigger', 'event': f'vestige:{event}', 'conditions': list(conditions)}], 'effects': effects}
def manifest(kind, duration, spec=None, ids=None, bindings=(), on_hit=(), on_tick=(), on_end=(), interval=1, visual=None, **values):
    result = {'type': 'create_manifestation', 'target': spec or target(), 'manifestation': {
        'kind': f'vestige:{kind}', 'duration': duration, 'values': values, 'identifiers': ids or {},
        'bindings': list(bindings), 'on_hit': list(on_hit), 'on_tick': list(on_tick), 'on_end': list(on_end), 'interval': interval}}
    if visual is not None: result['manifestation']['visual'] = visual
    return result
def projectile(*impacts, item='amethyst_shard', **values):
    return manifest('projectile', 200, target('self'), {'item': f'minecraft:{item}'}, on_hit=impacts,
                    speed=values.pop('speed', 1.5), distance=rng(values.pop('distance', 32)), **values)
def buff(duration, *ticks, bindings=(), interval=20, **values):
    return manifest('status', duration, target('self'), bindings=bindings, on_tick=ticks, interval=interval, **values)
def field(duration, radius, *ticks, spec=None, particle='enchant', interval=10, bindings=(), **values):
    return manifest('area', duration, spec, {'particle': f'minecraft:{particle}'}, bindings=bindings,
                    on_tick=ticks, interval=interval, radius=area(radius), **values)
def cone(count, radius, *effects):
    return repeat(count, 4, each(target('cone', rng(radius), 'hostile', False, angle=35), *effects))
def melee(radius, *effects): return each(target('melee', rng(radius), 'hostile', False, angle=65), *effects)
def weapon(n): return action('weapon_damage', amount=amp(n), weapon_fraction=.5, ignore_invulnerability=1)
def summon(entity, count=1, weapon_item=None, duration=600):
    ids = {'entity': f'minecraft:{entity}'}
    if weapon_item: ids['weapon'] = f'minecraft:{weapon_item}'
    if entity in ('zombie', 'skeleton'): ids['head'] = 'minecraft:leather_helmet'
    health = {'zombie':16,'skeleton':12,'vex':8,'horse':20,'polar_bear':24}[entity]
    attack = {'zombie':3,'skeleton':2,'vex':3,'horse':0,'polar_bear':5}[entity]
    return manifest('summon', duration, target('self'), ids, count=count, health=health, attack_damage=attack)
def dismissible(*summons): return [*summons, wait(max(s['manifestation']['duration'] for s in summons)), action('dismiss_manifestations')]
def ward(duration, charges=1):
    return buff(duration, bindings=[binding('evasion', 'damage_calculating',
            [action('reduce_pending_damage', amount=fact('event/damage_amount'))], duration, charges)])
