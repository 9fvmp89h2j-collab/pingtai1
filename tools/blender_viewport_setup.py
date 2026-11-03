import bpy

for window in bpy.context.window_manager.windows:
    for area in window.screen.areas:
        if area.type != 'VIEW_3D':
            continue
        area.spaces.active.shading.type = 'MATERIAL'
        region = next((r for r in area.regions if r.type == 'WINDOW'), None)
        if region:
            with bpy.context.temp_override(window=window, area=area, region=region):
                bpy.ops.view3d.view_camera()

print('LIVE_VIEW_READY')
