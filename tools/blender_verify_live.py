import bpy

print('FILE=', bpy.data.filepath)
print('OBJECTS=', len(bpy.context.scene.objects))
print('MASTER=', bpy.data.objects.get('BRONZE_BABY_MASTER') is not None)
for window in bpy.context.window_manager.windows:
    for area in window.screen.areas:
        if area.type == 'VIEW_3D':
            print('VIEW=', area.spaces.active.region_3d.view_perspective, area.spaces.active.shading.type)
