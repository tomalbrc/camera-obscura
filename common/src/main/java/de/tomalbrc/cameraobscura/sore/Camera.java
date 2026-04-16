package de.tomalbrc.cameraobscura.sore;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.*;

import java.lang.Math;

public class Camera {
    private final Vector3d position = new Vector3d(0, 0, 0);
    private final Vector3d target = new Vector3d(0, 0, -1);
    private final Vector3d up = new Vector3d(0, 1, 0);
    private final Matrix4f viewMatrix = new Matrix4f();
    private final Matrix4f projectionMatrix = new Matrix4f();
    private double fov = Math.toRadians(60);
    private double aspect = 1.0f;
    private double nearPlane = 0.1f;
    private double farPlane = 256.0f;
    private boolean viewDirty = true;
    private boolean projDirty = true;

    public void setPosition(double x, double y, double z) {
        position.set(x, y, z);
        viewDirty = true;
    }

    public Vector3d getPosition() {
        return position;
    }

    public Vec3 position() {
        return new Vec3(position.x(), position.y(), position.z());
    }

    public void setTarget(double x, double y, double z) {
        target.set(x, y, z);
        viewDirty = true;
    }

    public Vector3dc getTarget() {
        return target;
    }

    public void setUp(double x, double y, double z) {
        up.set(x, y, z);
        viewDirty = true;
    }

    public Vector3dc getUp() {
        return up;
    }

    public void setPerspective(double fovRad, double aspect, double near, double far) {
        this.fov = fovRad;
        this.aspect = aspect;
        this.nearPlane = near;
        this.farPlane = far;
        projDirty = true;
    }

    public Matrix4fc getViewMatrix() {
        if (viewDirty) {
            viewMatrix.identity().lookAt(position.get(new Vector3f()), target.get(new Vector3f()), up.get(new Vector3f()));
            viewDirty = false;
        }
        return viewMatrix;
    }

    public Matrix4fc getProjectionMatrix() {
        if (projDirty) {
            projectionMatrix.identity().perspective((float) fov, (float) aspect, (float) nearPlane, (float) farPlane);
            projDirty = false;
        }
        return projectionMatrix;
    }

    public void updateAspect(int width, int height) {
        setPerspective(fov, (double) width / height, nearPlane, farPlane);
    }

    public double farPlane() {
        return farPlane;
    }

    public double nearPlane() {
        return nearPlane;
    }

    public double aspect() {
        return aspect;
    }

    public void setTargetFromYawPitch(double yaw, double pitch) {
        double yawRad = Math.toRadians(yaw + 90);
        double pitchRad = Math.toRadians(-pitch);
        double cosYaw = Math.cos(yawRad);
        double sinYaw = Math.sin(yawRad);
        double cosPitch = Math.cos(pitchRad);
        double sinPitch = Math.sin(pitchRad);
        double dx = cosPitch * cosYaw;
        double dz = cosPitch * sinYaw;
        setTarget(position.x + dx, position.y + sinPitch, position.z + dz);
    }

    public BlockPos blockPosition() {
        return BlockPos.containing(position());
    }
}