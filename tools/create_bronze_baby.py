import bpy
import math
import os
from mathutils import Vector

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "outputs", "bronze_baby")
os.makedirs(OUT, exist_ok=True)

bpy.ops.object.select_all(action="SELECT")
bpy.ops.object.delete(use_global=False)
for datablocks in (bpy.data.materials, bpy.data.curves, bpy.data.meshes, bpy.data.cameras, bpy.data.lights):
    pass

def mat(name, color, metallic=0.0, rough=0.45):
    m = bpy.data.materials.new(name)
    m.diffuse_color = (*color, 1)
    m.use_nodes = True
    bs = m.node_tree.nodes.get("Principled BSDF")
    bs.inputs["Base Color"].default_value = (*color, 1)
    bs.inputs["Metallic"].default_value = metallic
    bs.inputs["Roughness"].default_value = rough
    if metallic > 0.7:
        noise = m.node_tree.nodes.new("ShaderNodeTexNoise")
        noise.inputs["Scale"].default_value = 72.0
        noise.inputs["Detail"].default_value = 3.5
        noise.inputs["Roughness"].default_value = 0.72
        bump = m.node_tree.nodes.new("ShaderNodeBump")
        bump.inputs["Strength"].default_value = 0.18
        bump.inputs["Distance"].default_value = 0.045
        m.node_tree.links.new(noise.outputs["Fac"], bump.inputs["Height"])
        m.node_tree.links.new(bump.outputs["Normal"], bs.inputs["Normal"])
    return m

bronze = mat("Warm aged bronze", (0.55, 0.24, 0.045), 0.82, 0.39)
bronze_dark = mat("Dark bronze details", (0.16, 0.055, 0.012), 0.75, 0.32)
patina = mat("Verdigris patina", (0.02, 0.30, 0.27), 0.35, 0.58)
ivory = mat("Warm eye whites", (0.93, 0.78, 0.51), 0.08, 0.25)
iris = mat("Amber irises", (0.30, 0.085, 0.008), 0.35, 0.18)
black = mat("Glossy pupils", (0.006, 0.004, 0.003), 0.15, 0.12)
white = mat("Eye highlights", (1.0, 0.94, 0.72), 0.05, 0.08)
floor_mat = mat("Warm studio floor", (0.78, 0.69, 0.54), 0.0, 0.72)

def smooth(obj, bevel=0.0):
    if obj.type == "MESH":
        for p in obj.data.polygons:
            p.use_smooth = True
        if bevel:
            mod = obj.modifiers.new("Soft bevel", "BEVEL")
            mod.width = bevel
            mod.segments = 3
    return obj

def uv(name, loc, scale, material=bronze, seg=64, rings=32):
    bpy.ops.mesh.primitive_uv_sphere_add(segments=seg, ring_count=rings, location=loc)
    o = bpy.context.object
    o.name = name
    o.scale = scale
    bpy.ops.object.transform_apply(location=False, rotation=False, scale=True)
    o.data.materials.append(material)
    return smooth(o)

def torus(name, loc, major, minor, scale=(1,1,1), rot=(0,0,0), material=bronze):
    bpy.ops.mesh.primitive_torus_add(major_radius=major, minor_radius=minor, major_segments=64, minor_segments=20, location=loc, rotation=rot)
    o = bpy.context.object
    o.name = name
    o.scale = scale
    bpy.ops.object.transform_apply(location=False, rotation=False, scale=True)
    o.data.materials.append(material)
    return smooth(o)

def curve(name, pts, bevel, material, cyclic=False):
    cu = bpy.data.curves.new(name, "CURVE")
    cu.dimensions = "3D"
    cu.resolution_u = 16
    cu.bevel_depth = bevel
    cu.bevel_resolution = 5
    sp = cu.splines.new("BEZIER")
    sp.bezier_points.add(len(pts)-1)
    for bp, co in zip(sp.bezier_points, pts):
        bp.co = co
        bp.handle_left_type = "AUTO"
        bp.handle_right_type = "AUTO"
    sp.use_cyclic_u = cyclic
    ob = bpy.data.objects.new(name, cu)
    bpy.context.collection.objects.link(ob)
    cu.materials.append(material)
    return ob

def capsule(name, a, b, radius, material=bronze, squash=(1,1,1)):
    mid = (Vector(a)+Vector(b))*0.5
    vec = Vector(b)-Vector(a)
    length = vec.length
    bpy.ops.mesh.primitive_uv_sphere_add(segments=48, ring_count=24, location=mid)
    o = bpy.context.object
    o.name = name
    o.scale = (radius*squash[0], radius*squash[1], (length*0.5+radius)*squash[2])
    o.rotation_mode = "QUATERNION"
    o.rotation_quaternion = Vector((0,0,1)).rotation_difference(vec.normalized())
    bpy.ops.object.transform_apply(location=False, rotation=False, scale=True)
    o.data.materials.append(material)
    return smooth(o)

def add_eye(side):
    x = side*0.54
    # Eyes sit on front surface; Y is forward (negative).
    uv(f"EyeWhite_{side}", (x, -1.145, 5.64), (0.48, 0.20, 0.57), ivory)
    uv(f"Iris_{side}", (x, -1.335, 5.62), (0.265, 0.07, 0.31), iris)
    uv(f"Pupil_{side}", (x, -1.398, 5.63), (0.145, 0.035, 0.19), black)
    uv(f"Highlight_{side}", (x-side*0.07, -1.438, 5.78), (0.057, 0.022, 0.068), white, 32, 16)
    # upper lid accent
    curve(f"UpperLid_{side}", [(x-side*0.40,-1.34,5.78),(x,-1.49,6.00),(x+side*0.40,-1.34,5.78)], .035, bronze_dark)

def add_ear(side):
    x = side*1.62
    torus(f"EarOuter_{side}", (x,-0.02,5.54), .40, .145, scale=(0.64,0.42,1.0), rot=(math.pi/2,0,0), material=bronze)
    torus(f"EarInner_{side}", (x,-0.13,5.54), .22, .065, scale=(0.66,0.40,1.0), rot=(math.pi/2,0,0), material=patina)
    uv(f"EarCore_{side}", (x,-0.015,5.52), (.20,.12,.30), bronze_dark, 40, 20)

def add_hand(side):
    x = side*1.62
    uv(f"Palm_{side}", (x,-0.05,2.73), (.32,.26,.40), bronze)
    for i in range(4):
        fx = x + side*(0.25 - i*0.13)
        capsule(f"Finger_{side}_{i}", (fx,-0.20,2.60), (fx,-0.23,2.27-i*0.012), .075, bronze, (1.0,.72,1.0))
        uv(f"Nail_{side}_{i}", (fx,-0.295,2.34-i*0.01), (.052,.018,.066), bronze_dark, 24, 12)
    tx = x-side*.30
    capsule(f"Thumb_{side}", (tx,-.18,2.70), (tx-side*.08,-.25,2.43), .105, bronze)

def add_foot(side):
    x = side*.61
    uv(f"Foot_{side}", (x,-.25,.35), (.58,.82,.30), bronze)
    sizes=[.16,.145,.13,.115,.095]
    for i,s in enumerate(sizes):
        tx=x-side*(.27-i*.145)
        uv(f"Toe_{side}_{i}", (tx,-.99,.30), (s,s*.82,s*.78), bronze, 36, 18)
        uv(f"ToeNail_{side}_{i}", (tx,-1.105,.34), (s*.55,.025,s*.36), bronze_dark, 24, 12)

# Main silhouette, total height ~7.2; head/body ratio ~1:2.6.
uv("Torso", (0,0,3.05), (1.33,.92,1.65), bronze)
uv("Belly", (0,-.73,2.93), (1.12,.64,1.18), bronze)
uv("Chest", (0,-.62,3.83), (1.03,.48,.72), bronze)
uv("Neck", (0,0,4.52), (.60,.52,.28), bronze)
torus("NeckPatina", (0,-.02,4.50), .56,.035,scale=(1,.82,1),material=patina)
uv("Head", (0,0,5.72), (1.66,1.43,1.58), bronze)
uv("Cheek_L", (-.69,-1.10,5.14), (.76,.38,.56), bronze)
uv("Cheek_R", (.69,-1.10,5.14), (.76,.38,.56), bronze)

# Legs, knees, feet.
for side in (-1,1):
    capsule(f"Thigh_{side}", (side*.67,0,2.08),(side*.67,-.02,1.17),.49,bronze,(1,.90,1))
    uv(f"Knee_{side}",(side*.67,-.42,1.38),(.43,.30,.42),bronze)
    capsule(f"Shin_{side}",(side*.67,.02,1.18),(side*.67,.02,.49),.38,bronze,(1,.90,1))
    torus(f"AnklePatina_{side}",(side*.67,-.02,.56),.37,.027,scale=(1,.88,1),material=patina)
    add_foot(side)

# Arms angled outward.
for side in (-1,1):
    capsule(f"UpperArm_{side}",(side*1.06,-.02,3.92),(side*1.47,-.02,3.15),.32,bronze,(1,.9,1))
    uv(f"Elbow_{side}",(side*1.45,-.22,3.15),(.32,.28,.31),bronze)
    capsule(f"Forearm_{side}",(side*1.47,-.01,3.14),(side*1.62,-.02,2.68),.27,bronze,(1,.9,1))
    torus(f"WristPatina_{side}",(side*1.61,-.02,2.72),.25,.022,scale=(1,.86,1),material=patina)
    add_hand(side)

# Ears and eyes.
for side in (-1,1):
    add_ear(side)
    add_eye(side)

# Nose and nostrils.
uv("Nose", (0,-1.43,5.20), (.29,.25,.22), bronze)
uv("Nostril_L", (-.11,-1.655,5.15), (.055,.025,.032), bronze_dark, 24,12)
uv("Nostril_R", (.11,-1.655,5.15), (.055,.025,.032), bronze_dark, 24,12)

# Smile: dark groove plus bronze lower lip.
curve("SmileGroove", [(-.46,-1.455,4.91),(-.23,-1.58,4.82),(0,-1.62,4.79),(.23,-1.58,4.82),(.46,-1.455,4.91)], .045, bronze_dark)
curve("LowerLip", [(-.34,-1.49,4.78),(0,-1.58,4.69),(.34,-1.49,4.78)], .055, bronze)

# Brows.
for side in (-1,1):
    x=side*.55
    curve(f"Brow_{side}",[(x-side*.35,-1.36,6.24),(x,-1.48,6.34),(x+side*.34,-1.35,6.24)],.055,bronze_dark)

# Hairline / patina trim and topknot.
curve("HairlinePatina", [(-1.30,-.89,6.45),(-.72,-1.22,6.67),(0,-1.37,6.38),(.72,-1.22,6.67),(1.30,-.89,6.45)], .035, patina)
curve("HairlineBronze", [(-1.28,-.87,6.48),(-.72,-1.20,6.70),(0,-1.35,6.41),(.72,-1.20,6.70),(1.28,-.87,6.48)], .052, bronze)
torus("TopknotPatina",(0,0,7.22),.43,.055,scale=(1,.88,1),material=patina)
torus("TopknotRing",(0,0,7.29),.42,.14,scale=(1,.90,1),material=bronze)
uv("TopknotBall",(0,0,7.70),(.48,.44,.43),bronze)

# Navel, nipples and subtle hip seams.
torus("Navel",(0,-1.365,3.00),.09,.035,scale=(1,.35,1),rot=(math.pi/2,0,0),material=bronze_dark)
for side in (-1,1):
    uv(f"Nipple_{side}",(side*.49,-1.10,3.82),(.045,.028,.045),bronze_dark,24,12)
curve("HipPatina", [(-1.02,-.66,2.35),(-.52,-1.02,2.17),(0,-1.15,2.08),(.52,-1.02,2.17),(1.02,-.66,2.35)], .026, patina)

# Floor.
bpy.ops.mesh.primitive_plane_add(size=30, location=(0,0,.015))
floor=bpy.context.object
floor.name="Studio Floor"
floor.data.materials.append(floor_mat)

# Camera and soft studio lighting.
bpy.ops.object.camera_add(location=(0,-15.5,5.0))
cam=bpy.context.object
cam.name="Front Camera"
bpy.context.scene.camera=cam
def track(obj, point):
    obj.rotation_euler=(Vector(point)-obj.location).to_track_quat('-Z','Y').to_euler()
track(cam,(0,0,3.75))
cam.data.lens=62

def area(name,loc,energy,size,color):
    bpy.ops.object.light_add(type='AREA',location=loc)
    l=bpy.context.object;l.name=name;l.data.energy=energy;l.data.shape='DISK';l.data.size=size;l.data.color=color
    track(l,(0,0,3.5));return l
area("Key",(-5,-7,9),1150,5.0,(1.0,.72,.40))
area("Fill",(5,-5,6),850,4.5,(.55,.72,1.0))
area("Rim",(0,3,8),1250,4.0,(1.0,.56,.28))

scene=bpy.context.scene
scene.render.engine='BLENDER_EEVEE'
scene.render.resolution_x=700
scene.render.resolution_y=900
scene.render.resolution_percentage=100
scene.render.image_settings.file_format='PNG'
scene.render.film_transparent=False
scene.world.color=(0.035,0.025,0.018)
scene.view_settings.look='AgX - Medium High Contrast'

# Organize everything under one named collection-like parent.
bpy.ops.object.empty_add(type='PLAIN_AXES',location=(0,0,0))
root=bpy.context.object
root.name='BRONZE_BABY_MASTER'
for o in list(scene.objects):
    if o != root and o not in (cam,floor) and o.type not in {'LIGHT'}:
        o.parent=root

blend_path=os.path.join(OUT,'bronze_baby.blend')
preview_path=os.path.join(OUT,'bronze_baby_front.png')
scene.render.filepath=preview_path
bpy.ops.wm.save_as_mainfile(filepath=blend_path)
bpy.ops.render.render(write_still=True)
print('SAVED_BLEND',blend_path)
print('SAVED_PREVIEW',preview_path)
