import bpy

def get(name):
    return bpy.data.objects.get(name)

# Merge the cheek masses visually back into the face.
for side in (-1, 1):
    c = get('Cheek_L' if side == -1 else 'Cheek_R')
    if c:
        c.scale = (0.62, 0.48, 0.60)
        c.location.x = side * 0.76
        c.location.y = -1.02
        c.location.z = 5.12

# Put the smile directly below the nose instead of on the chin/neck boundary.
for name, dz, sy in (('SmileGroove', 0.27, 0.82), ('LowerLip', 0.29, 0.80)):
    o = get(name)
    if o:
        o.location.z += dz
        o.location.y -= 0.09
        o.scale.x *= 0.88
        o.scale.z *= sy

# Recover visible brows with a clean, friendly arch.
for side in (-1, 1):
    b = get(f'Brow_{side}')
    if b:
        b.location.y -= 0.10
        b.location.z += 0.02
        b.scale = (0.88, 1.0, 0.88)

# Refine the toy-like spherical knees and shoe-like feet.
for side in (-1, 1):
    knee = get(f'Knee_{side}')
    if knee:
        knee.scale = (0.82, 0.76, 0.78)
        knee.location.y += 0.06
    foot = get(f'Foot_{side}')
    if foot:
        foot.scale = (0.90, 0.84, 0.82)
        foot.location.z += 0.015
    for i in range(5):
        toe = get(f'Toe_{side}_{i}')
        nail = get(f'ToeNail_{side}_{i}')
        if toe:
            toe.location.y += 0.13
            toe.location.z += 0.035
            toe.scale = (1.00, 0.86, 0.92)
        if nail:
            nail.location.y += 0.13
            nail.location.z += 0.035

# Make hands a touch fuller and less comb-like.
for side in (-1, 1):
    palm = get(f'Palm_{side}')
    if palm:
        palm.scale = (1.08, 1.08, 1.06)
    for i in range(4):
        finger = get(f'Finger_{side}_{i}')
        if finger:
            finger.scale = (1.08, 1.05, 0.90)

bpy.ops.wm.save_as_mainfile(filepath=bpy.data.filepath)
print('PHASE2_REFINED')
