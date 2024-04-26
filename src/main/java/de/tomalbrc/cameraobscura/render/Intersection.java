package de.tomalbrc.cameraobscura.render;

import org.joml.Vector3d;

public record Intersection(Vector3d normal, Vector3d point, Vector3d direction, int color) {
    public static Intersection of(Vector3d normal, Vector3d point, Vector3d direction) {
        return of(normal, point, direction, 0);
    }

    public static Intersection of(Vector3d normal, Vector3d point, Vector3d direction, int color) {
        return new Intersection(normal, point, direction, color);
    }
}
