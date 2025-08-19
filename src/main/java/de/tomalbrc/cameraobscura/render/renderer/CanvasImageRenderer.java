package de.tomalbrc.cameraobscura.render.renderer;

import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.mapcanvas.api.core.CanvasImage;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.stream.IntStream;

public class CanvasImageRenderer extends AbstractRenderer<CanvasImage> {
    public CanvasImageRenderer(LivingEntity entity, int width, int height, int renderDistance) {
        super(entity, width, height, renderDistance);
    }

    public CanvasImage render() {
        Vec3 eyes = this.entity.getEyePosition();
        CanvasImage image = new CanvasImage(width, height);

        IntStream.range(0, width * height).parallel().forEach(i -> {
            int x = i % width;
            int y = i / width;
            CanvasColor color = CanvasUtils.findClosestColor(raytracer.trace(eyes, rayAt(entity.getYRot(), entity.xRotO, x, y)));
            image.set(x, y, color);
        });

        return image;
    }
}
