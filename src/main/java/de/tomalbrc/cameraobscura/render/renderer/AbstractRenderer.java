package de.tomalbrc.cameraobscura.render.renderer;

import de.tomalbrc.cameraobscura.render.Raytracer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public abstract class AbstractRenderer<T> implements Renderer<T> {
    protected final int width;
    protected final int height;

    protected final LivingEntity entity;
    protected final Raytracer raytracer;

    protected final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public AbstractRenderer(LivingEntity entity, int width, int height, int renderDistance) {
        this.entity = entity;
        this.width = width;
        this.height = height;
        this.raytracer = new Raytracer(this.entity, renderDistance);
        this.raytracer.preloadChunks(entity.getOnPos());
    }

    public static Vec3 yawPitchRotation(Vec3 base, double angleYaw, double anglePitch) {
        double oldX = base.x();
        double oldY = base.y();
        double oldZ = base.z();

        double sinOne = Math.sin(angleYaw);
        double sinTwo = Math.sin(anglePitch);
        double cosOne = Math.cos(angleYaw);
        double cosTwo = Math.cos(anglePitch);

        double newX = oldX * cosOne * cosTwo - oldY * cosOne * sinTwo - oldZ * sinOne;
        double newY = oldX * sinTwo + oldY * cosTwo;
        double newZ = oldX * sinOne * cosTwo - oldY * sinOne * sinTwo + oldZ * cosOne;

        return new Vec3(newX, newY, newZ);
    }

    public static Vec3 doubleYawPitchRotation(Vec3 base, double firstYaw, double firstPitch, double secondYaw,
                                              double secondPitch) {
        return yawPitchRotation(yawPitchRotation(base, firstYaw, firstPitch), secondYaw, secondPitch);
    }

    protected void iterateRays(LivingEntity entity, TriConsumer<Vec3, Integer, Integer> consumer) {
        double yawRad = (entity.yHeadRot + 90) * Mth.DEG_TO_RAD;
        double pitchRad = -entity.xRotO * Mth.DEG_TO_RAD;

        // this is incorrect but the math is not mathing when using 0,0,-1...
        Vec3 baseVec = new Vec3(1, 0, 0);

        // from viewer to screen to worldspace
        Vec3 lowerLeft = doubleYawPitchRotation(baseVec, -FOV_YAW_RAD, -FOV_PITCH_RAD, yawRad, pitchRad);
        Vec3 upperLeft = doubleYawPitchRotation(baseVec, -FOV_YAW_RAD, FOV_PITCH_RAD, yawRad, pitchRad);
        Vec3 lowerRight = doubleYawPitchRotation(baseVec, FOV_YAW_RAD, -FOV_PITCH_RAD, yawRad, pitchRad);
        Vec3 upperRight = doubleYawPitchRotation(baseVec, FOV_YAW_RAD, FOV_PITCH_RAD, yawRad, pitchRad);

        Vec3 leftFraction = upperLeft.subtract(lowerLeft).scale(1. / (height - 1.));
        Vec3 rightFraction = upperRight.subtract(lowerRight).scale(1. / (height - 1.));

        for (int pitch = 0; pitch < height; pitch++) {
            Vec3 leftPitch = upperLeft.subtract(leftFraction.scale(pitch));
            Vec3 rightPitch = upperRight.subtract(rightFraction.scale(pitch));
            Vec3 yawFraction = rightPitch.subtract(leftPitch).scale(1. / (width - 1.));

            for (int yaw = 0; yaw < width; yaw++) {
                Vec3 ray = leftPitch.add(yawFraction.scale(yaw)).normalize();
                consumer.accept(ray, yaw, pitch);
            }
        }
    }
}
