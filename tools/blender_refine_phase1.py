import bpy

def obj(name):
    return bpy.data.objects.get(name)

# Face: soften the over-inflated cheeks and reveal the lower eye contour.
for name in ('Cheek_L', 'Cheek_R'):
    o = obj(name)
    if o:
        o.scale = (0.80, 0.70, 0.82)
        o.location.z += 0.04
        o.location.y += 0.05

# Rounder, slightly smaller eye assemblies.
for side in (-1, 1):
    ew = obj(f'EyeWhite_{side}')
    ir = obj(f'Iris_{side}')
    pu = obj(f'Pupil_{side}')
    hi = obj(f'Highlight_{side}')
    if ew:
        ew.scale = (0.94, 1.0, 0.88)
        ew.location.z -= 0.01
    if ir:
        ir.scale = (0.94, 1.0, 0.92)
    if pu:
        pu.scale = (0.92, 1.0, 0.94)
    if hi:
        hi.scale = (0.92, 1.0, 0.92)
    lid = obj(f'UpperLid_{side}')
    if lid:
        bpy.data.objects.remove(lid, do_unlink=True)
    brow = obj(f'Brow_{side}')
    if brow:
        brow.location.z += 0.10
        brow.scale = (0.90, 0.90, 0.90)

# Larger, fuller ears like the turnaround sheet.
for side in (-1, 1):
    for prefix in ('EarOuter', 'EarInner', 'EarCore'):
        o = obj(f'{prefix}_{side}')
        if o:
            o.scale = (1.18, 1.10, 1.22)
            o.location.x *= 1.025
    inner = obj(f'EarInner_{side}')
    if inner:
        inner.location.y -= 0.035

# Smaller button nose and a gentler smile.
if obj('Nose'):
    obj('Nose').scale = (0.86, 0.84, 0.82)
    obj('Nose').location.z += 0.02
for name in ('Nostril_L', 'Nostril_R'):
    if obj(name):
        obj(name).scale = (0.82, 0.85, 0.82)
if obj('SmileGroove'):
    obj('SmileGroove').scale = (1.0, 0.92, 0.88)
if obj('LowerLip'):
    obj('LowerLip').scale = (0.94, 0.92, 0.86)

# Slightly less spherical head and a little more chin/face length.
if obj('Head'):
    obj('Head').scale = (0.98, 0.98, 1.025)

# Warmer gold bronze, closer to the supplied character sheet.
bronze = bpy.data.materials.get('Warm aged bronze')
if bronze and bronze.use_nodes:
    bs = bronze.node_tree.nodes.get('Principled BSDF')
    if bs:
        bs.inputs['Base Color'].default_value = (0.66, 0.31, 0.055, 1.0)
        bs.inputs['Metallic'].default_value = 0.78
        bs.inputs['Roughness'].default_value = 0.43

# Cream studio background for rendered view.
world = bpy.context.scene.world
world.use_nodes = True
bg = world.node_tree.nodes.get('Background')
if bg:
    bg.inputs['Color'].default_value = (0.055, 0.038, 0.020, 1.0)
    bg.inputs['Strength'].default_value = 0.32

bpy.ops.wm.save_as_mainfile(filepath=bpy.data.filepath)
print('PHASE1_REFINED')
