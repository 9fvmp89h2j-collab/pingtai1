import bpy

bronze=bpy.data.materials.get('Warm aged bronze')
root=bpy.data.objects.get('BRONZE_BABY_MASTER')

def uv(name,loc,scale):
    old=bpy.data.objects.get(name)
    if old:
        return old
    bpy.ops.mesh.primitive_uv_sphere_add(segments=56, ring_count=28, location=loc)
    o=bpy.context.object
    o.name=name
    o.scale=scale
    bpy.ops.object.transform_apply(location=False,rotation=False,scale=True)
    for p in o.data.polygons:
        p.use_smooth=True
    o.data.materials.append(bronze)
    o.parent=root
    return o

# Paired soft buttock volumes, visible in back and 3/4 views.
uv('Buttock_L',(-0.48,0.66,2.42),(0.72,0.48,0.68))
uv('Buttock_R',(0.48,0.66,2.42),(0.72,0.48,0.68))

# Small sacral/lower-back transition prevents a hard torso-to-butt step.
uv('LowerBack',(0,0.47,2.93),(0.84,0.34,0.62))

# Round the rear heel mass without changing the toe silhouette.
for side in (-1,1):
    uv(f'Heel_{side}',(side*0.61,0.25,0.34),(0.43,0.47,0.31))

bpy.ops.wm.save_as_mainfile(filepath=bpy.data.filepath)
print('PHASE3_BACK_REFINED')
