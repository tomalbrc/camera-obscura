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

        CompletableFuture<Void>[] futures = new CompletableFuture[width * height];
        AtomicInteger index = new AtomicInteger();

        this.iterateRays(this.entity, (ray, x, y) -> {
            futures[index.getAndIncrement()] = CompletableFuture.supplyAsync(() -> raytracer.trace(eyes, ray), executor).thenAccept(color -> {
                imgFile.setRGB(x, y, color);
            });
        });

        CompletableFuture.allOf(futures).join();

        return imgFile;
    }
}
