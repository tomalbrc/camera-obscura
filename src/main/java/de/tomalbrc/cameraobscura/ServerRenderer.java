package de.tomalbrc.cameraobscura;

import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.mapcanvas.api.core.CanvasImage;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Direction;
import net.minecraft.data.worldgen.DimensionTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

import java.io.IOException;
import java.util.List;

public class ServerRenderer {
    private static final double FOV_YAW_DEG = 53;
    private static final double FOV_PITCH_DEG = 40;

    private static final double FOV_YAW_RAD = Math.toRadians(FOV_YAW_DEG);
    private static final double FOV_PITCH_RAD = Math.toRadians(FOV_PITCH_DEG);


    private final ServerPlayer player;
    private final CanvasImage image;
    public ServerRenderer(ServerPlayer player) {
        this.player = player;
        this.image = new CanvasImage(128, 128);
    }

    public CanvasImage render() throws IOException {
        Vec3 eyes = this.player.getEyePosition();
        List<Vector3d> rays = buildRayMap(this.player);

        Raytracer raytracer = new Raytracer(this.player.level());
        // loop through every pixel on map
        for (int x = 0; x < 128; x++) {
            for (int y = 0; y < 128; y++) {
                int index = x+128*y;
                Vec3 rayTraceVector = new Vec3(rays.get(index).x, rays.get(index).y, rays.get(index).z);
                rayTraceVector = rayTraceVector.scale(256).add(eyes);
                this.image.set(x,y,raytracer.trace(eyes, rayTraceVector));
            }
        }

        this.image.set(0, 0, CanvasUtils.findClosestColor(123));
        return this.image;
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

    public static List<Vector3d> buildRayMap(Player player) {
        Vector3d direction = new Vector3d(player.getLookAngle().toVector3f()).normalize(); // Get normalized direction vector

        double yawRad = Math.atan2(direction.z, direction.x);
        double pitchRad = Math.atan2(direction.y, Math.sqrt(direction.x * direction.x + direction.z * direction.z));

        // this is incorrect but the math is not mathing when using 0,0,-1...
        Vector3d baseVec = new Vector3d(1, 0, 0);

        Vector3d lowerLeft = doubleYawPitchRotation(baseVec, -FOV_YAW_RAD, -FOV_PITCH_RAD, yawRad, pitchRad);
        Vector3d upperLeft = doubleYawPitchRotation(baseVec, -FOV_YAW_RAD, FOV_PITCH_RAD, yawRad, pitchRad);
        Vector3d lowerRight = doubleYawPitchRotation(baseVec, FOV_YAW_RAD, -FOV_PITCH_RAD, yawRad, pitchRad);
        Vector3d upperRight = doubleYawPitchRotation(baseVec, FOV_YAW_RAD, FOV_PITCH_RAD, yawRad, pitchRad);

        double width = 128;
        double height = 128;
        List<Vector3d> rays = new ObjectArrayList<>((int) (width * height));

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
