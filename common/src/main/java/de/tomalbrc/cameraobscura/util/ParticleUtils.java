package de.tomalbrc.cameraobscura.util;

import de.tomalbrc.cameraobscura.Components;
import net.minecraft.client.particle.DustParticle;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.Vec3;

public class ParticleUtils {
    public static final int SHUTTER_CLOSE_TICKS = 10;
    public static final double FRAME_DISTANCE = 1.;
    private static final double PARTICLE_STEP = 0.2;
    private static final int ROTATION_PHASE_TICKS = 10;
    private static final double FULL_ROTATION = 0;
    private static final double ROTATION_SPEED = FULL_ROTATION / ROTATION_PHASE_TICKS;

    static ParticleOptions options() {
        return ParticleTypes.END_ROD;
    }

    public static void spawnStaticFrame(ServerPlayer player, Components.Resolution resolution) {
        ServerLevel level = player.level();
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 right = new Vec3(look.z, 0, -look.x).normalize();
        Vec3 up = right.cross(look).normalize();

        double aspect = (double) resolution.width() / resolution.height();
        double halfW = 0.75 * FRAME_DISTANCE * aspect;
        double halfH = 0.75 * FRAME_DISTANCE;

        Vec3 center = eye.add(look.scale(FRAME_DISTANCE));

        Vec3 tl = center.add(up.scale(halfH)).add(right.scale(-halfW));
        Vec3 tr = center.add(up.scale(halfH)).add(right.scale(halfW));
        Vec3 bl = center.add(up.scale(-halfH)).add(right.scale(-halfW));
        Vec3 br = center.add(up.scale(-halfH)).add(right.scale(halfW));

        drawFrameEdges(level, tl, tr, bl, br);
    }

    public static void spawnShutterCloseParticles(ServerPlayer player, Components.Resolution resolution, int ticksLeft) {
        ServerLevel level = player.level();
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 right = new Vec3(look.z, 0, -look.x).normalize();
        Vec3 up = right.cross(look).normalize();

        double aspect = (double) resolution.width() / resolution.height();
        double halfW = 0.5 * FRAME_DISTANCE * aspect;
        double halfH = 0.5 * FRAME_DISTANCE;

        Vec3 center = eye.add(look.scale(FRAME_DISTANCE));
        Vec3 tl = center.add(up.scale(halfH)).add(right.scale(-halfW));
        Vec3 tr = center.add(up.scale(halfH)).add(right.scale(halfW));
        Vec3 bl = center.add(up.scale(-halfH)).add(right.scale(-halfW));
        Vec3 br = center.add(up.scale(-halfH)).add(right.scale(halfW));

        drawFrameEdges(level, tl, tr, bl, br);

        int elapsed = SHUTTER_CLOSE_TICKS - ticksLeft;
        double t = Math.min(elapsed / (double) SHUTTER_CLOSE_TICKS, 1.0);
        Vec3 topMid = center.add(up.scale(halfH * (1.0 - t)));
        Vec3 botMid = center.add(up.scale(-halfH * (1.0 - t)));

        drawShutterBars(level, topMid, botMid, right, halfW);

        if (ticksLeft <= 2) {
            level.sendParticles(options(), center.x, center.y, center.z, 15, 0.2, 0.2, 0.2, 0.05);
        }
    }

    public static void drawFrameEdges(ServerLevel level, Vec3 tl, Vec3 tr, Vec3 bl, Vec3 br) {
        drawLine(level, tl, tr);
        drawLine(level, tr, br);
        drawLine(level, br, bl);
        drawLine(level, bl, tl);
    }

    public static void drawShutterBars(ServerLevel level, Vec3 topMid, Vec3 botMid, Vec3 right, double halfWidth) {
        Vec3 topLeft = topMid.add(right.scale(-halfWidth));
        Vec3 topRight = topMid.add(right.scale(halfWidth));
        Vec3 botLeft = botMid.add(right.scale(-halfWidth));
        Vec3 botRight = botMid.add(right.scale(halfWidth));

        drawLine(level, topLeft, topRight);
        drawLine(level, botLeft, botRight);
    }

    private static void drawLine(ServerLevel level, Vec3 from, Vec3 to) {
        double dist = from.distanceTo(to);
        int steps = (int) (dist / PARTICLE_STEP);
        Vec3 dir = to.subtract(from).normalize();
        for (int i = 0; i <= steps; i++) {
            Vec3 pos = from.add(dir.scale(i * PARTICLE_STEP));
            level.sendParticles(options(), pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
        }
    }

    public static Vec3 rotateAroundAxis(Vec3 point, Vec3 center, Vec3 axis, double angle) {
        Vec3 v = point.subtract(center);
        Vec3 k = axis.normalize();
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        Vec3 cross = k.cross(v);
        double dot = k.dot(v);
        Vec3 rotated = v.scale(cos).add(cross.scale(sin)).add(k.scale(dot * (1 - cos)));
        return center.add(rotated);
    }

    public static void spawnCountdownParticles(ServerPlayer player, Components.Resolution resolution,
                                               int elapsed, int remaining, boolean shutter) {
        ServerLevel level = player.level();
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 right = new Vec3(look.z, 0, -look.x).normalize();
        Vec3 up = right.cross(look).normalize();

        double aspect = (double) resolution.width() / resolution.height();
        double halfW = 0.5 * FRAME_DISTANCE * aspect;
        double halfH = 0.5 * FRAME_DISTANCE;
        Vec3 center = eye.add(look.scale(FRAME_DISTANCE));

        Vec3 tl0 = center.add(up.scale(halfH)).add(right.scale(-halfW));
        Vec3 tr0 = center.add(up.scale(halfH)).add(right.scale(halfW));
        Vec3 bl0 = center.add(up.scale(-halfH)).add(right.scale(-halfW));
        Vec3 br0 = center.add(up.scale(-halfH)).add(right.scale(halfW));

        if (elapsed < ROTATION_PHASE_TICKS) {
            double outwardSpeed = 0.05;
            double offsetDist = elapsed * outwardSpeed;
            double newDistance = FRAME_DISTANCE + offsetDist;
            double scale = newDistance / FRAME_DISTANCE;

            double halfWScaled = halfW * scale;
            double halfHScaled = halfH * scale;

            Vec3 newCenter = eye.add(look.scale(newDistance));

            Vec3 tl0s = newCenter.add(up.scale(halfHScaled)).add(right.scale(-halfWScaled));
            Vec3 tr0s = newCenter.add(up.scale(halfHScaled)).add(right.scale(halfWScaled));
            Vec3 bl0s = newCenter.add(up.scale(-halfHScaled)).add(right.scale(-halfWScaled));
            Vec3 br0s = newCenter.add(up.scale(-halfHScaled)).add(right.scale(halfWScaled));

            double angle = elapsed * ROTATION_SPEED;

            Vec3 rt = rotateAroundAxis(tl0s, newCenter, look, angle);
            Vec3 rtr = rotateAroundAxis(tr0s, newCenter, look, angle);
            Vec3 rb = rotateAroundAxis(bl0s, newCenter, look, angle);
            Vec3 rbr = rotateAroundAxis(br0s, newCenter, look, angle);

            drawFrameEdges(level, rt, rtr, rb, rbr);
            return;
        }

        if (shutter && remaining <= 20) {
            drawFrameEdges(level, tl0, tr0, bl0, br0);

            int elapsedInLastSec = 20 - remaining;
            double t = Math.min(elapsedInLastSec / 20.0, 1.0);
            Vec3 topMid = center.add(up.scale(halfH * (1.0 - t)));
            Vec3 botMid = center.add(up.scale(-halfH * (1.0 - t)));

            drawShutterBars(level, topMid, botMid, right, halfW);

            if (remaining <= 2) {
                level.sendParticles(options(), center.x, center.y, center.z, 15, 0.2, 0.2, 0.2, 0.05);
            }
        }
    }
}
