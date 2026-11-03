import bpy

for name in ('Head','Nose','SmileGroove','LowerLip','Brow_-1','Brow_1','Chest'):
    o=bpy.data.objects.get(name)
    if not o:
        print(name,'MISSING')
        continue
    zs=[(o.matrix_world @ v.co).z for v in o.data.vertices] if o.type=='MESH' else [(o.matrix_world @ p.co).z for s in o.data.splines for p in s.bezier_points]
    ys=[(o.matrix_world @ v.co).y for v in o.data.vertices] if o.type=='MESH' else [(o.matrix_world @ p.co).y for s in o.data.splines for p in s.bezier_points]
    print(name,'loc',tuple(round(x,3) for x in o.location),'z',round(min(zs),3),round(max(zs),3),'y',round(min(ys),3),round(max(ys),3))
