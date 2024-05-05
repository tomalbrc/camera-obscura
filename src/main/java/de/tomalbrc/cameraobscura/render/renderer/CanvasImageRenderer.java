package de.tomalbrc.cameraobscura.render.renderer;

import eu.pb4.mapcanvas.api.core.CanvasImage;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

import java.util.List;

public class CanvasImageRenderer extends AbstractRenderer<CanvasImage> {
    public CanvasImageRenderer(LivingEntity entity, int width, int height) {
        super(entity, width, height);
    }

    public CanvasImage render() {
        Vec3 eyes = this.entity.getEyePosition();
        List<Vector3d> rays = buildRayMap(this.entity);

        CanvasImage image = new CanvasImage(width, height);

        // loop through every pixel on map
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int index = x+height*y;
                Vec3 rayTraceVector = new Vec3(rays.get(index).x, rays.get(index).y, rays.get(index).z);

                var res = raytracer.trace(eyes, rayTraceVector);

                image.set(x, y, CanvasUtils.findClosestColor(res));
            }
        }

        return image;
    }
}
