package de.tomalbrc.cameraobscura.renderer.animation;

import org.joml.Vector3d;

public class KeyframeAnimations {
    public static Vector3d posVec(final double x, final double y, final double z) {
        return new Vector3d(x, -y, z);
    }

    public static Vector3d degreeVec(final double x, final double y, final double z) {
        return new Vector3d(x * (Math.PI / 180.0), y * (Math.PI / 180.0), z * (Math.PI / 180.0));
    }

    public static Vector3d scaleVec(final double x, final double y, final double z) {
        return new Vector3d((x - 1.0), (y - 1.0), (z - 1.0));
    }
}