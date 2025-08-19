package de.tomalbrc.cameraobscura.render.renderer;

import de.tomalbrc.cameraobscura.render.Raytracer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractRenderer<T> implements Renderer<T> {
    protected final int width;
    protected final int height;

    protected final LivingEntity entity;
    protected final Raytracer raytracer;

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

    protected Vec3 rayAt(float yaw, float pitch, int x, int y) {
        double yawRad = (yaw + 90) * Mth.DEG_TO_RAD;
        double pitchRad = -pitch * Mth.DEG_TO_RAD;

        // forward
        Vec3 baseVec = new Vec3(1, 0, 0);

        // from viewer to screen to worldspace
        Vec3 lowerLeft  = doubleYawPitchRotation(baseVec, -FOV_YAW_RAD, -FOV_PITCH_RAD, yawRad, pitchRad);
        Vec3 upperLeft  = doubleYawPitchRotation(baseVec, -FOV_YAW_RAD,  FOV_PITCH_RAD, yawRad, pitchRad);
        Vec3 lowerRight = doubleYawPitchRotation(baseVec,  FOV_YAW_RAD, -FOV_PITCH_RAD, yawRad, pitchRad);
        Vec3 upperRight = doubleYawPitchRotation(baseVec,  FOV_YAW_RAD,  FOV_PITCH_RAD, yawRad, pitchRad);

        // vertical lerp between top and bottom for this row
        double v = (double) y / (height - 1);
        Vec3 leftEdge  = upperLeft.lerp(lowerLeft, v);
        Vec3 rightEdge = upperRight.lerp(lowerRight, v);

        // horizontal lerp between left and right edge for this column
        double u = (double) x / (width - 1);
        Vec3 ray = leftEdge.lerp(rightEdge, u);

        return ray;
    }
}
