package de.tomalbrc.cameraobscura.render.renderer;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.awt.image.BufferedImage;
import java.util.stream.IntStream;

public class BufferedImageRenderer extends AbstractRenderer<BufferedImage> {
    public BufferedImageRenderer(LivingEntity entity, int width, int height, int renderDistance) {
        super(entity, width, height, renderDistance);
    }

    public BufferedImage render() {
        Vec3 eyes = this.entity.getEyePosition();
        var imgFile = new BufferedImage(width,height, BufferedImage.TYPE_INT_RGB);

        IntStream.range(0, width * height).parallel().forEach(i -> {
            int x = i % width;
            int y = i / width;
            imgFile.setRGB(x, y, raytracer.trace(eyes, rayAt(entity.getYRot(), entity.xRotO, x, y)));
        });

        return imgFile;
    }
}
