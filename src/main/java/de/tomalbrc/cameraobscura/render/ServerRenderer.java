package de.tomalbrc.cameraobscura.render;

import eu.pb4.mapcanvas.api.core.CanvasImage;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class ServerRenderer {
    private static final double FOV_YAW_DEG = 53;
    private static final double FOV_PITCH_DEG = 40;

    private static final double FOV_YAW_RAD = Math.toRadians(FOV_YAW_DEG);
    private static final double FOV_PITCH_RAD = Math.toRadians(FOV_PITCH_DEG);

    private final int width;
    private final int height;

    private final ServerPlayer player;
    private final CanvasImage image;
    public ServerRenderer(ServerPlayer player, int width, int height) {
        this.player = player;
        this.image = new CanvasImage(width, height);
        this.width = width;
        this.height = height;
    }

    public CanvasImage render() throws IOException {
        Vec3 eyes = this.player.getEyePosition();
        List<Vector3d> rays = buildRayMap(this.player);

        var imgFile = new BufferedImage(width,height, BufferedImage.TYPE_INT_RGB);

        var time = (this.player.level().dayTime()%24000) / 24000.f;
        System.out.println("daytime: " + time);

        Raytracer raytracer = new Raytracer(this.player.level());

        // loop through every pixel on map
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int index = x+height*y;
                Vec3 rayTraceVector = new Vec3(rays.get(index).x, rays.get(index).y, rays.get(index).z);

                var col = raytracer.trace(eyes, rayTraceVector);

                imgFile.setRGB(x, y, col);
                this.image.set(x,y, CanvasUtils.findClosestColor(col));
            }
        }

        ImageIO.write(imgFile, "PNG", new File("/tmp/out.png"));

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

    public List<Vector3d> buildRayMap(Player player) {
        Vector3d direction = new Vector3d(player.getLookAngle().toVector3f()).normalize(); // Get normalized direction vector

        double yawRad = (player.yHeadRot+90) * Mth.DEG_TO_RAD;
        double pitchRad = -player.xRotO * Mth.DEG_TO_RAD;

        // this is incorrect but the math is not mathing when using 0,0,-1...
        Vector3d baseVec = new Vector3d(1, 0, 0);

        Vector3d lowerLeft = doubleYawPitchRotation(baseVec, -FOV_YAW_RAD, -FOV_PITCH_RAD, yawRad, pitchRad);
        Vector3d upperLeft = doubleYawPitchRotation(baseVec, -FOV_YAW_RAD, FOV_PITCH_RAD, yawRad, pitchRad);
        Vector3d lowerRight = doubleYawPitchRotation(baseVec, FOV_YAW_RAD, -FOV_PITCH_RAD, yawRad, pitchRad);
        Vector3d upperRight = doubleYawPitchRotation(baseVec, FOV_YAW_RAD, FOV_PITCH_RAD, yawRad, pitchRad);

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
