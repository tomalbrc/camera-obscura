package de.tomalbrc.cameraobscura.util;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4d;
import org.joml.Matrix4dc;
import org.joml.Vector3fc;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Constants {
    public static final String MOD_ID = "camera-obscura";
    public static final String DYNAMIC_PLAYER_TEXTURE = "dyn.p"; // player models and player heads
    public static final String DYNAMIC_SIGN_TEXTURE = "dyn.s";
    public static final String DYNAMIC_MAP_TEXTURE = "dyn.m";

    public static final Vector3fc ZERO_VEC3 = Vec3.ZERO.toVector3f();
    public static final Vector3fc UNIT_VEC3 = new Vec3(1, 1, 1).toVector3f();
    public static final Matrix4dc ZERO_MATRIX = new Matrix4d();

    public static ExecutorService RENDER_EXEC = Executors.newVirtualThreadPerTaskExecutor();
    public static ExecutorService CHUNK_EXEC = Executors.newVirtualThreadPerTaskExecutor();

    public static final Direction[][] FACE_TANGENTS = new Direction[6][2];

    static {
        // its faster than map lookups
        for (Direction face : Direction.values()) {
            Direction t1, t2;
            t2 = switch (face.getAxis()) {
                case Y -> {
                    t1 = Direction.EAST;
                    yield Direction.SOUTH;
                }
                case Z -> {
                    t1 = Direction.EAST;
                    yield Direction.UP;
                }
                case X -> {
                    t1 = Direction.SOUTH;
                    yield Direction.UP;
                }
                default -> throw new IllegalStateException();
            };
            FACE_TANGENTS[face.ordinal()] = new Direction[]{t1, t2};
        }
    }

    public static void stop() {
        RENDER_EXEC.shutdownNow();
        CHUNK_EXEC.shutdownNow();
    }
}
