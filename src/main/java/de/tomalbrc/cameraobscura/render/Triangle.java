package de.tomalbrc.cameraobscura.render;

import net.minecraft.core.Direction;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Triangle {

    private final Vector3f v1;
    private final Vector3f v2;
    private final Vector3f v3;
    private final Vector3f normal; // Pre-calculated triangle normal

    private final Direction cubeFace;

    public Triangle(Vector3f v1, Vector3f v2, Vector3f v3, Direction cubeFace) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;

        this.cubeFace = cubeFace;

        // Pre-calculate the normal vector for faster intersection checks
        this.normal = v2.sub(v1, new Vector3f()).cross(v3.sub(v1, new Vector3f())).normalize();
    }

    // Function to check ray intersection with the triangle (Möller-Trumbore algorithm)
    public TriangleHit rayIntersect(Vector3f origin, Vector3f direction) {
        Vector3f edge1 = v2.sub(v1, new Vector3f());
        Vector3f edge2 = v3.sub(v1, new Vector3f());

        Vector3f h = direction.cross(edge2, new Vector3f());
        float a = edge1.dot(h);

        if (Math.abs(a) < 0.0001f) {
            return null; // Parallel ray or ray is coplanar with triangle
        }

        Vector3f s = origin.sub(v1, new Vector3f());
        float u = s.dot(h);
        if (u < 0.0f || u > a) {
            return null; // Intersection lies outside triangle (edge 1)
        }

        Vector3f q = s.cross(edge1);
        float v = direction.dot(q);
        if (v < 0.0f || u + v > a) {
            return null; // Intersection lies outside triangle (edge 2)
        }

        float t = edge2.dot(q) / a;
        if (t < 0.0f) {
            return null; // Intersection behind the triangle
        }

        var p = origin.add(direction.mul(t, new Vector3f()));
        return new TriangleHit(p, t, this.calculateUVCoordinates(p), this.cubeFace);
    }

    // Function to calculate UV coordinates at a point on the triangle
    public Vector2f calculateUVCoordinates(Vector3f point) {
        // Barycentric coordinates for the point
        var t1 = v3.sub(v1, new Vector3f());
        var t2 = v2.sub(v1, new Vector3f());

        Vector3f w = point.sub(v1);
        float u = w.dot(t1.cross(t2, new Vector3f()));
        float v = w.dot(t2.cross(t1, new Vector3f()));

        return new Vector2f(u,v);
    }

    static public class TriangleHit {
        private final Vector3f intersectionPoint;
        private final float t; // Distance along the ray
        private final Vector2f uv;

        private final Direction direction;

        public TriangleHit(Vector3f intersectionPoint, float t, Vector2f uv, Direction direction) {
            this.intersectionPoint = intersectionPoint;
            this.t = t;
            this.uv = uv;
            this.direction = direction;
        }

        public Direction getDirection() {
            return this.direction;
        }

        public Vector3f getIntersectionPoint() {
            return intersectionPoint;
        }

        public Vector2f getUV() {
            return this.uv;
        }

        public float getT() {
            return t;
        }
    }
}

// Class to hold information about the ray-triangle intersection
