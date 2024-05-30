package de.tomalbrc.cameraobscura.render.renderer;

import de.tomalbrc.cameraobscura.render.Raytracer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.List;

public abstract class AbstractRenderer<T> implements Renderer<T> {
    protected final int width;
    protected final int height;

    protected final LivingEntity entity;
    protected final Raytracer raytracer;

    public AbstractRenderer(LivingEntity entity, int width, int height, int renderDistance) {
        this.entity = entity;
        this.width = width;
        this.height = height;
        this.raytracer = new Raytracer(this.entity, this.entity.level(), renderDistance);
        this.raytracer.preloadChunks(entity.getOnPos());
    }

    public static Vector3f yawPitchRotation(Vector3f base, float angleYaw, float anglePitch) {
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

        return new Vector3f(
                (float) newX,
                (float) newY,
                (float) newZ
        );
    }

    public static Vector3f doubleYawPitchRotation(Vector3f base, float firstYaw, float firstPitch, float secondYaw,
                                                  float secondPitch) {
        return yawPitchRotation(yawPitchRotation(base, firstYaw, firstPitch), secondYaw, secondPitch);
    }

    protected List<Vector3f> buildRayMap(LivingEntity entity) {
        float yawRad = (entity.yHeadRot + 90) * Mth.DEG_TO_RAD;
        float pitchRad = -entity.xRotO * Mth.DEG_TO_RAD;

        // this is incorrect but the math is not mathing when using 0,0,-1...
        Vector3f baseVec = new Vector3f(1, 0, 0);

        // from viewer to screen to worldspace
        Vector3fc lowerLeft = doubleYawPitchRotation(baseVec, -FOV_YAW_RAD, -FOV_PITCH_RAD, yawRad, pitchRad);
        Vector3fc upperLeft = doubleYawPitchRotation(baseVec, -FOV_YAW_RAD, FOV_PITCH_RAD, yawRad, pitchRad);
        Vector3fc lowerRight = doubleYawPitchRotation(baseVec, FOV_YAW_RAD, -FOV_PITCH_RAD, yawRad, pitchRad);
        Vector3fc upperRight = doubleYawPitchRotation(baseVec, FOV_YAW_RAD, FOV_PITCH_RAD, yawRad, pitchRad);

        List<Vector3f> rays = new ObjectArrayList<>(width * height);

        Vector3f leftFraction = new Vector3f(upperLeft).sub(lowerLeft).mul(1.f / (height - 1.f));
        Vector3f rightFraction = new Vector3f(upperRight).sub(lowerRight).mul(1.f / (height - 1.f));

        for (int pitch = 0; pitch < height; pitch++) {
            Vector3f leftPitch = new Vector3f(upperLeft).sub(leftFraction.mul(pitch, new Vector3f()));
            Vector3f rightPitch = new Vector3f(upperRight).sub(rightFraction.mul(pitch, new Vector3f()));
            Vector3f yawFraction = new Vector3f(rightPitch).sub(leftPitch).mul(1.f / (width - 1.f));

            for (int yaw = 0; yaw < width; yaw++) {
                Vector3f ray = new Vector3f(leftPitch).add(yawFraction.mul(yaw, new Vector3f())).normalize();
                rays.add(ray);
            }
        }

        return rays;
    }
}
