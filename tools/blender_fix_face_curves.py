import bpy

def curve_center_z(o):
    zs=[(o.matrix_world @ p.co).z for s in o.data.splines for p in s.bezier_points]
    return (min(zs)+max(zs))*0.5

targets={
    'SmileGroove':4.86,
    'LowerLip':4.73,
    'Brow_-1':6.22,
    'Brow_1':6.22,
}
for name,target in targets.items():
    o=bpy.data.objects.get(name)
    if o:
        o.location.z += target-curve_center_z(o)

bpy.ops.wm.save_as_mainfile(filepath=bpy.data.filepath)
print('FACE_CURVES_FIXED')
