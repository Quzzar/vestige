"""Reusable authoring presets for the native, layered presentation library.

Each call returns fresh data. Recipes select presets explicitly; traits never
implicitly choose a visual, target, damage type, or gameplay behavior.
"""

def layer(shape, color, alpha=1, width=.04, scale=1, *, speed=1, phase=0, count=8):
    return dict(shape=shape, color=color, alpha=alpha, width=width, scale=scale,
                speed=speed, phase=phase, count=count)


def visual(duration, radius, *layers, height=0, ends_with_bindings=False, sound=None):
    result = dict(duration=duration, radius=radius, height=height, layers=list(layers))
    if ends_with_bindings: result['ends_with_bindings'] = True
    if sound: result['sound'] = dict(id='minecraft:' + sound, volume=.6, pitch=1.1)
    return result


def arc(duration=12, color="87cfff", width=.14):
    return visual(duration, .2, layer('arc', color, .95, width),
                  layer('arc', 'ffffff', 1, width*.32), layer('sparks', 'b8eaff', .9, scale=1.3),
                  height=1.1, sound='block.amethyst_block.chime')


def beam(color='fff1b0', duration=12):
    return visual(duration, .2, layer('beam', color, .9, .25),
                  layer('beam', 'ffffff', 1, .06), layer('sparks', color, scale=1.25), height=1.1)


def ward(duration, color='b9a0ff', consumed=True):
    return visual(duration, 1.15, layer('sphere', color, .07), layer('ring', color, .7, .045),
                  layer('sparks', color, .6, scale=.5), height=1,
                  ends_with_bindings=consumed, sound='block.amethyst_block.resonate')


def orb(color='ff9d35', fire=True):
    return visual(200, .42, layer('sphere', color, .7), layer('sphere', 'fff4d3', .9, scale=.55),
                  layer('fire' if fire else 'sparks', color, .95, scale=1.1),
                  sound='item.firecharge.use' if fire else 'block.amethyst_block.chime')


def blast(radius, color='ffad42', fire=True):
    return visual(16, radius, layer('ring', color, .95, .18), layer('sphere', color, .14),
                  layer('fire' if fire else 'sparks', color, .95, scale=1.2),
                  layer('smoke', '707070', .6 if fire else .2),
                  height=.25, sound='entity.generic.explode')


def field(duration, radius, color, fire=False):
    return visual(duration, radius, layer('ring', color, .8, .085),
                  layer('fire' if fire else 'sparks', color, .7, scale=.7), height=.08)


def water_jet():
    return visual(18,.35,layer('jet','278bcf',.8,.6),layer('splash','a1eaff',.95,scale=1.5),
                  height=1.1,sound='entity.player.splash.high_speed')


def shield(duration=60):
    return visual(duration,1.15,layer('sphere','9e8cf2',.045),layer('shield','ddd3ff',.8,.05),
                  layer('sparks','c4c5ff',.55,scale=.5),height=1,ends_with_bindings=True,
                  sound='block.amethyst_block.resonate')
