package de.tomalbrc.cameraobscura.render.renderer;

import eu.pb4.mapcanvas.api.core.CanvasImage;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.ThreadPerTaskExecutor;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

public class CanvasImageRenderer extends AbstractRenderer<CanvasImage> {
    public CanvasImageRenderer(LivingEntity entity, int width, int height, int renderDistance) {
        super(entity, width, height, renderDistance);
    }

    public CanvasImage render() {
        Vec3 eyes = this.entity.getEyePosition();

        List<CompletableFuture<Void>> futureList = new ObjectArrayList<>();

        CanvasImage image = new CanvasImage(width, height);
        this.iterateRays(this.entity, (ray, x, y) -> {
            image.set(x, y, CanvasUtils.findClosestColor(raytracer.trace(eyes, ray)));
        });

        futureList.forEach(x-> x.complete(null));

        return image;
    }
}
