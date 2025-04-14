package de.tomalbrc.cameraobscura.render.renderer;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.awt.image.BufferedImage;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class BufferedImageRenderer extends AbstractRenderer<BufferedImage> {
    public BufferedImageRenderer(LivingEntity entity, int width, int height, int renderDistance) {
        super(entity, width, height, renderDistance);
    }

    public BufferedImage render() {
        Vec3 eyes = this.entity.getEyePosition();
        var imgFile = new BufferedImage(width,height, BufferedImage.TYPE_INT_RGB);

        // List to hold the CompletableFutures for each pixel
        CompletableFuture<Void>[] futures = new CompletableFuture[width * height];
        AtomicInteger index = new AtomicInteger();

        // Iterate through rays and create async tasks
        this.iterateRays(this.entity, (ray, x, y) -> {
            final int pixelX = x;
            final int pixelY = y;

            futures[index.getAndIncrement()] = CompletableFuture.supplyAsync(() -> {
                // Trace the ray and compute the color asynchronously
                return raytracer.trace(eyes, ray);
            }, executor).thenAccept(color -> {
                // Update the image with the computed color
                imgFile.setRGB(x, y, color);
            });
        });

        // Shutdown the executor
        CompletableFuture.allOf(futures).join();

        return imgFile;
    }
}
