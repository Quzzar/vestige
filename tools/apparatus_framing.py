"""Approved corner/rim/support edging, with the same relative shade on every finish."""
import copy
from apparatus_columns import box

TRIM_TINT_INDEX = 0
TRIM_TINT_COLOR = '#d9d9d9'
TRIM_BRIGHTNESS = int(TRIM_TINT_COLOR[1:3], 16) / 255


def strip(side, left, right, bottom, top, edge, texture='frame', tinted=False):
    """One exposed face, separated from the stone by 0.008 model units."""
    plane = edge + (-.008 if side in ('north', 'west') else .008)
    if side in ('north', 'south'):
        element = box([left, bottom, plane], [right, top, plane], texture)
    else:
        element = box([plane, bottom, left], [plane, top, right], texture)
    element['faces'] = {side: element['faces'][side]}
    if tinted:
        element['faces'][side]['tintindex'] = TRIM_TINT_INDEX
    return element


def plinth_framing(original, part):
    result = copy.deepcopy(original)
    assert 'frame' not in result['textures'], 'Already framed'
    result['textures']['frame'] = result['textures']['stone']
    column_texture = result['textures']['column']
    corner_texture = 'frame'
    if column_texture != result['textures']['stone']:
        result['textures']['column_frame'] = column_texture
        corner_texture = 'column_frame'
    foot, cap = part in ('single', 'base'), part in ('single', 'cap')
    body = original['elements'][1 if foot else 0]
    bottom, top = body['from'][1], body['to'][1]
    for side, edge in (('north', 3), ('south', 13), ('west', 3), ('east', 13)):
        for low, high in ((0, .65),) if foot else ():
            result['elements'].append(strip(side, 2, 14, low, high, 2 if edge == 3 else 14, tinted=True))
        for low, high in ((13.35, 14),) if cap else ():
            result['elements'].append(strip(side, 2, 14, low, high, 2 if edge == 3 else 14, tinted=True))
        for left, right in ((3, 3.75), (12.25, 13)):
            element = strip(side, left, right, bottom, top, edge, corner_texture, True)
            # Preserve the shaft's native texture phase, including bordered tiles and pillars.
            for index in (1, 3):
                if element['faces'][side]['uv'][index] != body['faces'][side]['uv'][index]:
                    element['faces'][side]['uv'][index] = body['faces'][side]['uv'][index]
            result['elements'].append(element)
    assert result['elements'][:len(original['elements'])] == original['elements']
    return result


def spellstone_framing(original):
    result = copy.deepcopy(original)
    assert 'frame' not in result['textures'], 'Already framed'
    result['textures']['frame'] = result['textures']['stone']
    cap = original['elements'][2]
    for side, edge in (('north', 2), ('south', 14), ('west', 2), ('east', 14)):
        for low, high in ((cap['from'][1], cap['from'][1] + .45),
                          (cap['to'][1] - .45, cap['to'][1])):
            result['elements'].append(strip(side, 2, 14, low, high, edge, tinted=True))
    # Diamond corner pixels remain 0.012 units out, in front of the 0.008-unit trim.
    for support in original['elements'][:2]:
        x, y, z = support['from']
        X, Y, Z = support['to']
        for side, edge, left, right in (('north', z, x, X), ('south', Z, x, X),
                                       ('west', x, z, Z), ('east', X, z, Z)):
            for a, b, low, high in ((left, left + .65, y + .025, Y),
                                    (right - .65, right, y + .025, Y),
                                    (left + .65, right - .65, y + .025, y + .475)):
                element = strip(side, a, b, low, high, edge, tinted=True)
                element['rotation'] = copy.deepcopy(support['rotation'])
                result['elements'].append(element)
    assert result['elements'][:len(original['elements'])] == original['elements']
    return result
