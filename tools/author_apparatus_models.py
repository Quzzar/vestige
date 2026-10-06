#!/usr/bin/env python3
"""Package the approved apparatus geometry and canonical construction palette."""
import argparse
import copy
import itertools
import json
import math
from pathlib import Path
from apparatus_columns import column_model, tent_spellstone, PLINTH_SOCKET_CENTER_Y, PLINTH_SOCKET_FACE_Z, PLINTH_SOCKET_BACK_Z, PLINTH_SOCKET_SIZE, SPELLSTONE_SURFACE
from apparatus_framing import plinth_framing, spellstone_framing, TRIM_TINT_INDEX, TRIM_TINT_COLOR

ROOT = Path(__file__).resolve().parents[1]
REVIEW = ROOT / 'docs/art/apparatus-concepts'
RESOURCES = ROOT / 'src/main/resources'
JAVA = ROOT / 'src/main/java/com/quzzar/vestige/apparatus'
PALETTE = ROOT / 'docs/design/apparatus-materials.json'


def block_name(material, role):
    return role if material['id'] == 'stone_bricks' else material['id'] + '_' + role


def physical(model):
    result = copy.deepcopy(model)
    result['elements'] = [e for e in result['elements'] if not any(
        f['texture'] == '#runes' for f in e['faces'].values())]
    return result


def review_model(material, role):
    return json.loads((REVIEW/'models'/(role+'-'+material+'.json')).read_text())


def native_model(material, role):
    original=review_model(material,role)
    return plinth_framing(column_model(original,'single'),'single') if role=='plinth' else spellstone_framing(tent_spellstone(original))


def native_column_model(material, part):
    return plinth_framing(column_model(review_model(material,'plinth'),part),part)


def corners(element):
    points = list(itertools.product(*zip(element['from'], element['to'])))
    if 'rotation' in element:
        rotation = element['rotation']
        assert not rotation.get('rescale', False), 'Collision generation needs unscaled rotations'
        axis = 'xyz'.index(rotation['axis'])
        other = [i for i in range(3) if i != axis]
        # In the x/z plane, a positive Y rotation sends x toward negative z.
        angle = math.radians(rotation['angle'] * (-1 if axis == 1 else 1))
        cosine, sine = math.cos(angle), math.sin(angle)
        transformed = []
        for p in points:
            q = [p[i] - rotation['origin'][i] for i in range(3)]
            a, b = other
            q[a], q[b] = q[a]*cosine - q[b]*sine, q[a]*sine + q[b]*cosine
            transformed.append([q[i] + rotation['origin'][i] for i in range(3)])
        points = transformed
    return points


def bounds(element):
    points=corners(element)
    return [min(p[i] for p in points) for i in range(3)] + [max(p[i] for p in points) for i in range(3)]


def collision_boxes(element):
    """Enclose tilted supports in quarter-unit horizontal slices, preserving the opening."""
    if not all(a<b for a,b in zip(element['from'],element['to'])):
        return []
    rotation=element.get('rotation')
    if not rotation or rotation['axis']!='z':
        return [bounds(element)]
    assert not rotation.get('rescale',False), 'Collision generation needs unscaled rotations'
    ox,oy,_=rotation['origin'];angle=math.radians(rotation['angle'])
    cosine,sine=math.cos(angle),math.sin(angle)
    x,y,z=element['from'];X,Y,Z=element['to']
    polygon=[(ox+(px-ox)*cosine-(py-oy)*sine,
              oy+(px-ox)*sine+(py-oy)*cosine) for px,py in ((x,y),(X,y),(X,Y),(x,Y))]
    bottom=min(p[1] for p in polygon);top=max(p[1] for p in polygon)
    boxes=[]
    for index in range(math.ceil((top-bottom)/.25)):
        low=bottom+index*.25;high=min(top,low+.25)
        xs=[px for px,py in polygon if low<=py<=high]
        for (ax,ay),(bx,by) in zip(polygon,polygon[1:]+polygon[:1]):
            if abs(by-ay)<1e-12:continue
            for cut in (low,high):
                if min(ay,by)<=cut<=max(ay,by):
                    xs.append(ax+(bx-ax)*(cut-ay)/(by-ay))
        if xs:boxes.append([min(xs),low,z,max(xs),high,Z])
    return boxes


def generated_files(manifest):
    materials = manifest['materials']
    assert len(materials) == 36 and len({m['id'] for m in materials}) == 36
    outputs = {}
    names = []
    for material in materials:
        for role in manifest['roles']:
            name = block_name(material, role)
            names.append('vestige:' + name)
            model = native_model(material['id'],role)
            outputs[RESOURCES / 'assets/vestige/models/block' / (name + '.json')] = physical(model)
            # The placed Spellstone renderer owns the floating seal; its item shows the stone body.
            outputs[RESOURCES / 'assets/vestige/models/item' / (name + '.json')] = (
                model if role == 'spellstone' else {'parent': 'vestige:block/' + name})
            variants={'part=single':{'model':'vestige:block/'+name}}
            for part in ('base','shaft','cap'):
                part_name=name+'_'+part if role=='plinth' else name
                if role=='plinth':
                    outputs[RESOURCES / 'assets/vestige/models/block' / (part_name+'.json')] = native_column_model(material['id'],part)
                variants['part='+part]={'model':'vestige:block/'+part_name}
            outputs[RESOURCES / 'assets/vestige/blockstates' / (name + '.json')] = {'variants':variants}
            recipe = manifest['construction_recipes'][role]
            key = {c: {'item': value} for c, value in recipe.get('common_items', {}).items()}
            key.update({c: {'item': material[field]} for c, field in recipe['variant_items'].items()})
            outputs[RESOURCES / 'data/vestige/recipe' / (name + '.json')] = {
                'type': 'minecraft:crafting_shaped', 'category': 'misc', 'group': 'vestige_' + role,
                'pattern': recipe['pattern'], 'key': key,
                'result': {'id': 'vestige:' + name, 'count': recipe['count']}}
            outputs[RESOURCES / 'data/vestige/loot_table/blocks' / (name + '.json')] = {
                'type': 'minecraft:block', 'pools': [{'rolls': 1, 'entries': [
                    {'type': 'minecraft:item', 'name': 'vestige:' + name}],
                    'conditions': [{'condition': 'minecraft:survives_explosion'}]}]}
            outputs[RESOURCES / 'data/vestige/advancement/recipes' / (name + '.json')] = {
                'parent': 'minecraft:recipes/root', 'criteria': {
                    'has_material': {'trigger': 'minecraft:inventory_changed', 'conditions': {
                        'items': [{'items': material['body_item']}]}},
                    'has_the_recipe': {'trigger': 'minecraft:recipe_unlocked', 'conditions': {
                        'recipe': 'vestige:' + name}}},
                'requirements': [['has_material', 'has_the_recipe']],
                'rewards': {'recipes': ['vestige:' + name]}}
    outputs[RESOURCES / 'data/minecraft/tags/block/mineable/pickaxe.json'] = {'replace': False, 'values': names + ['#vestige:standing_stones']}
    lang_path = RESOURCES / 'assets/vestige/lang/en_us.json'
    language = json.loads(lang_path.read_text())
    for m in materials:
        for role in manifest['roles']:
            language['block.vestige.' + block_name(m, role)] = m['name'] + ' ' + role.title()
    outputs[lang_path] = language
    enum = ',\n'.join('    ' + m['id'].upper() + '(' + ', '.join(json.dumps(m[k]) for k in ('id', 'body_item', 'slab_item')) + ')' for m in materials)
    outputs[JAVA / 'ApparatusMaterials.java'] = '''package com.quzzar.vestige.apparatus;

import net.minecraft.resources.ResourceLocation;

/** Generated by tools/author_apparatus_models.py. Cosmetic carriers have identical behavior. */
public enum ApparatusMaterials {
''' + enum + ''';

    private final String id;
    private final ResourceLocation body;
    private final ResourceLocation slab;
    ApparatusMaterials(String id, String body, String slab) {
        this.id = id;
        this.body = ResourceLocation.parse(body);
        this.slab = ResourceLocation.parse(slab);
    }
    public String id() { return id; }
    public ResourceLocation body() { return body; }
    public ResourceLocation slab() { return slab; }
    public String blockName(ApparatusBlock.Role role) {
        return this == STONE_BRICKS ? role.path() : id + "_" + role.path();
    }
}
'''
    outputs[JAVA / 'client/ApparatusColors.java'] = '''package com.quzzar.vestige.apparatus.client;

import com.quzzar.vestige.VestigeMainMod;
import com.quzzar.vestige.apparatus.ApparatusBlocks;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

/** Generated by tools/author_apparatus_models.py; colors only the approved edging. */
@EventBusSubscriber(modid = VestigeMainMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ApparatusColors {
''' + f'''    public static final int TINT_INDEX = {TRIM_TINT_INDEX};
    public static final int TINT_COLOR = 0xff{TRIM_TINT_COLOR[1:]};
''' + '''
    private ApparatusColors() { }
    private static int color(int tint) { return tint == TINT_INDEX ? TINT_COLOR : -1; }

    @SubscribeEvent public static void blocks(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tint) -> color(tint), ApparatusBlocks.all().toArray(Block[]::new));
    }

    @SubscribeEvent public static void items(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tint) -> color(tint), ApparatusBlocks.all().stream().map(Block::asItem).toArray(Item[]::new));
    }
}
'''
    shapes = []
    shape_models={role:physical(native_model('stone_bricks',role)) for role in manifest['roles']}
    for part in ('base','shaft','cap'):
        shape_models['plinth_'+part]=native_column_model('stone_bricks',part)
    for role,model in shape_models.items():
        boxes = sorted({tuple(round(v, 9) for v in b) for e in model['elements'] for b in collision_boxes(e)})
        boxes = [b for b in boxes if all(b[i+3] > b[i] for i in range(3))]
        shapes.append('    public static final VoxelShape ' + role.upper() + ' = Shapes.or(\n' +
                      ',\n'.join('            Block.box(' + ', '.join(str(v) for v in b) + ')' for b in boxes) + ').optimize();')
    outline=[corners(e) for e in shape_models['spellstone']['elements']
             if all(a<b for a,b in zip(e['from'],e['to']))]
    outline_java=',\n'.join('        {\n'+',\n'.join('            new Vec3('+', '.join(str(round(v/16,9)) for v in p)+')' for p in points)+'\n        }' for points in outline)
    outputs[JAVA / 'ApparatusShapes.java'] = '''package com.quzzar.vestige.apparatus;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.Vec3;

/** Generated from solid model elements; thin slices preserve openings between tilted supports. */
public final class ApparatusShapes {
    private ApparatusShapes() { }
''' + f'''    public static final double PLINTH_SOCKET_CENTER_Y = {PLINTH_SOCKET_CENTER_Y / 16};
    public static final double PLINTH_SOCKET_FACE_Z = {PLINTH_SOCKET_FACE_Z / 16};
    public static final double PLINTH_SOCKET_BACK_Z = {PLINTH_SOCKET_BACK_Z / 16};
    public static final double PLINTH_SOCKET_SIZE = {PLINTH_SOCKET_SIZE / 16};
    public static final double SPELLSTONE_SURFACE = {SPELLSTONE_SURFACE / 16};
    /** Eight corners per authored solid, indexed by x/y/z bits; no collision-slice edges. */
    public static final Vec3[][] SPELLSTONE_OUTLINE = {{
''' + outline_java + '\n    };\n' + '\n'.join(shapes) + '\n}\n'
    return outputs


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--check', action='store_true')
    args = parser.parse_args()
    outputs = generated_files(json.loads(PALETTE.read_text()))
    for path, value in outputs.items():
        data = value if isinstance(value, bytes) else (value if isinstance(value, str) else json.dumps(value, indent=2) + '\n').encode()
        if args.check:
            assert path.is_file() and path.read_bytes() == data, 'Apparatus drift: ' + str(path.relative_to(ROOT))
        else:
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_bytes(data)
    print('36 finishes, 72 apparatus blocks/models/construction recipes: ' + ('checked' if args.check else 'generated'))


if __name__ == '__main__':
    main()
