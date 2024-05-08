package de.tomalbrc.cameraobscura.render.renderer;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class BufferedImageRenderer extends AbstractRenderer<BufferedImage> {
    public BufferedImageRenderer(LivingEntity entity, int width, int height, int renderDistance) {
        super(entity, width, height, renderDistance);
    }

    public BufferedImage render() {
        Vec3 eyes = this.entity.getEyePosition();
        List<Vector3d> rays = this.buildRayMap(this.entity);

        var imgFile = new BufferedImage(width,height, BufferedImage.TYPE_INT_RGB);

        // loop through every pixel on map
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int index = x+height*y;
                Vec3 rayTraceVector = new Vec3(rays.get(index).x, rays.get(index).y, rays.get(index).z);

                var res = raytracer.trace(eyes, rayTraceVector);

                imgFile.setRGB(x, y, res);
            }
        }

        try {
            ImageIO.write(imgFile, "PNG", new File("/tmp/out.png"));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return imgFile;
    }
}
