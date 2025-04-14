package de.tomalbrc.cameraobscura.render.renderer;

import eu.pb4.mapcanvas.api.core.CanvasImage;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class CanvasImageRenderer extends AbstractRenderer<CanvasImage> {
    public CanvasImageRenderer(LivingEntity entity, int width, int height, int renderDistance) {
        super(entity, width, height, renderDistance);
    }

    public CanvasImage render() {
        Vec3 eyes = this.entity.getEyePosition();
        CanvasImage image = new CanvasImage(width, height);


        CompletableFuture<Void>[] futures = new CompletableFuture[width * height];
        AtomicInteger index = new AtomicInteger();

        this.iterateRays(this.entity, (ray, x, y) -> {
            final int pixelX = x;
            final int pixelY = y;

            futures[index.getAndIncrement()] = CompletableFuture.supplyAsync(() -> CanvasUtils.findClosestColor(raytracer.trace(eyes, ray)), executor).thenAccept(color -> {
                try {
                    image.set(pixelX, pixelY, color);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });

        CompletableFuture.allOf(futures).join();

        return image;
    }
}
