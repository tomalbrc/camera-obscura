package de.tomalbrc.cameraobscura.render.renderer;

import de.tomalbrc.cameraobscura.render.Raytracer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector3d;

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
        this.raytracer = new Raytracer(this.entity.level(), renderDistance);
        this.raytracer.preloadChunks(entity.getOnPos());
    }

    public static Vector3d yawPitchRotation(Vector3d base, double angleYaw, double anglePitch) {
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

        return new Vector3d(newX, newY, newZ);
    }

    public static Vector3d doubleYawPitchRotation(Vector3d base, double firstYaw, double firstPitch, double secondYaw,
                                                  double secondPitch) {
        return yawPitchRotation(yawPitchRotation(base, firstYaw, firstPitch), secondYaw, secondPitch);
    }

    protected List<Vector3d> buildRayMap(LivingEntity entity) {
        double yawRad = (entity.yHeadRot+90) * Mth.DEG_TO_RAD;
        double pitchRad = -entity.xRotO * Mth.DEG_TO_RAD;

        // this is incorrect but the math is not mathing when using 0,0,-1...
        Vector3d baseVec = new Vector3d(1, 0, 0);

        // from viewer to screen to worldspace
        Vector3d lowerLeft = doubleYawPitchRotation(baseVec, -FOV_YAW_RAD, -FOV_PITCH_RAD, yawRad, pitchRad);
        Vector3d upperLeft = doubleYawPitchRotation(baseVec, -FOV_YAW_RAD, FOV_PITCH_RAD, yawRad, pitchRad);
        Vector3d lowerRight = doubleYawPitchRotation(baseVec, FOV_YAW_RAD, -FOV_PITCH_RAD, yawRad, pitchRad);
        Vector3d upperRight = doubleYawPitchRotation(baseVec, FOV_YAW_RAD, FOV_PITCH_RAD, yawRad, pitchRad);

        List<Vector3d> rays = new ObjectArrayList<>(width * height);

        Vector3d leftFraction = new Vector3d(upperLeft).sub(lowerLeft).mul(1.0 / (height - 1.0));
        Vector3d rightFraction = new Vector3d(upperRight).sub(lowerRight).mul(1.0 / (height - 1.0));

        for (int pitch = 0; pitch < height; pitch++) {
            Vector3d leftPitch = new Vector3d(upperLeft).sub(leftFraction.mul(pitch, new Vector3d()));
            Vector3d rightPitch = new Vector3d(upperRight).sub(rightFraction.mul(pitch, new Vector3d()));
            Vector3d yawFraction = new Vector3d(rightPitch).sub(leftPitch).mul(1.0 / (width - 1.0));

            for (int yaw = 0; yaw < width; yaw++) {
                Vector3d ray = new Vector3d(leftPitch).add(yawFraction.mul(yaw, new Vector3d())).normalize();
                rays.add(ray);
            }
        }

        return rays;
    }
}
