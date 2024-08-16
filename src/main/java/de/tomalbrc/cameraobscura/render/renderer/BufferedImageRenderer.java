package de.tomalbrc.cameraobscura.render.renderer;

import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class BufferedImageRenderer extends AbstractRenderer<BufferedImage> {
    public BufferedImageRenderer(LivingEntity entity, int width, int height, int renderDistance) {
        super(entity, width, height, renderDistance);
    }

    public BufferedImage render() {
        Vec3 eyes = this.entity.getEyePosition();
        var imgFile = new BufferedImage(width,height, BufferedImage.TYPE_INT_RGB);

        this.iterateRays(this.entity, (ray, x, y) -> {
            imgFile.setRGB(x, y, raytracer.trace(eyes, ray));

        });

        return imgFile;
    }
}
