import bpy
import os

scene = bpy.context.scene
scene.render.resolution_x = 560
scene.render.resolution_y = 720
scene.render.resolution_percentage = 100
scene.render.filepath = os.path.join(os.path.dirname(bpy.data.filepath), 'refined_live_check.png')
bpy.ops.render.render(write_still=True)
print(scene.render.filepath)
