package de.tomalbrc.cameraobscura.renderer.animation;

import net.minecraft.util.Mth;
import org.joml.Matrix4d;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public record AnimationChannel(Target target, Keyframe... keyframes) {
    public interface Interpolation {
        Vector3d apply(final Vector3d vector, final double alpha, final Keyframe[] keyframes, final int prev, final int next, final double targetScale);
    }

    public interface Target {
        void apply(final Matrix4d matrix, Vector3d val);
    }

    public static class Interpolations {
        public static final Interpolation LINEAR = (vector, alpha, keyframes, prev, next, targetScale) -> {
            Vector3dc point0 = keyframes[prev].postTarget();
            Vector3dc point1 = keyframes[next].preTarget();
            return point0.lerp(point1, alpha, vector).mul(targetScale);
        };
        public static final Interpolation CATMULLROM = (vector, alpha, keyframes, prev, next, targetScale) -> {
            Vector3dc point0 = keyframes[Math.max(0, prev - 1)].postTarget();
            Vector3dc point1 = keyframes[prev].postTarget();
            Vector3dc point2 = keyframes[next].postTarget();
            Vector3dc point3 = keyframes[Math.min(keyframes.length - 1, next + 1)].postTarget();
            vector.set(
                    Mth.catmullrom((float) alpha, (float) point0.x(), (float) point1.x(), (float) point2.x(), (float) point3.x()) * targetScale,
                    Mth.catmullrom((float) alpha, (float) point0.y(), (float) point1.y(), (float) point2.y(), (float) point3.y()) * targetScale,
                    Mth.catmullrom((float) alpha, (float) point0.z(), (float) point1.z(), (float) point2.z(), (float) point3.z()) * targetScale
            );
            return vector;
        };
    }

    public static class Targets {
        public static final Target POSITION = Matrix4d::translate;
        public static final Target ROTATION = (m, v) -> m.rotateXYZ(Mth.DEG_TO_RAD * v.x, Mth.DEG_TO_RAD * v.y, Mth.DEG_TO_RAD * v.z);
        public static final Target SCALE = Matrix4d::scale;
    }

}