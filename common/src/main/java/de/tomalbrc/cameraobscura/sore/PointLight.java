package de.tomalbrc.cameraobscura.sore;

import org.joml.Vector3d;

public record PointLight(Vector3d position, Vector3d color, double linearAttenuation, double quadraticAttenuation) {
    public static PointLight fromRange(Vector3d pos, Vector3d color, double range) {
        double linear = 2.0f / range;
        double quadratic = 1.0f / (range * range);
        return new PointLight(pos, color, linear, quadratic);
    }
}
