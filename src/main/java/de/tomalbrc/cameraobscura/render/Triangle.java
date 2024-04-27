package de.tomalbrc.cameraobscura.render;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Triangle {

    private final Vector3f v0;
    private final Vector3f v1;
    private final Vector3f v2;

    private final Vector3f v0v1;
    private final Vector3f v0v2;

    private final Vector3f N;
    private final Direction cubeFace;
    public final int color;

    public Triangle(Vector3f v0, Vector3f v1, Vector3f v2, Direction cubeFace, int color) {
        this.v0 = v0;
        this.v1 = v1;
        this.v2 = v2;

        this.v0v1 = v1.sub(v0, new Vector3f());
        this.v0v2 = v2.sub(v0, new Vector3f());
        this.N = v0v1.cross(v0v2,  new Vector3f()); // N

        this.color = color;
        
        this.cubeFace = cubeFace;
    }

    // Function to check ray intersection with the triangle (Möller-Trumbore algorithm)
    public TriangleHit rayIntersect(Vector3f orig, Vector3f dir) {
        float u,v;

        // compute the plane's normal
        float denom = N.dot(N);

        // Step 1: finding P

        // check if the ray and plane are parallel.
        float NdotRayDirection = N.dot(dir);
        if (Math.abs(NdotRayDirection) < 0.0001) // almost 0
            return null; // they are parallel so they don't intersect!

        // compute d parameter using equation 2
        float d = -N.dot(v0);

        // compute t (equation 3)
        float t = -(N.dot(orig) + d) / NdotRayDirection;
        // check if the triangle is behind the ray
        if (t < 0) return null; // the triangle is behind

        // compute the intersection point using equation 1
        Vector3f P = orig.add(dir.mul(t, new Vector3f()), new Vector3f());

        // Step 2: inside-outside test
        Vector3f C; // vector perpendicular to triangle's plane

        // edge 0
        Vector3f vp0 = P.sub(v0, new Vector3f());
        Vector3f edge0 = v1.sub(v0, new Vector3f());
        C = edge0.cross(vp0, new Vector3f());
        if (N.dot(C) < 0) return null; // P is on the right side

        // edge 1
        Vector3f edge1 = v2.sub(v1, new Vector3f());
        Vector3f vp1 = P.sub(v1, new Vector3f());
        C = edge1.cross(vp1);
        if ((u = N.dot(C)) < 0)  return null; // P is on the right side

        // edge 2
        Vector3f edge2 = v0.sub(v2, new Vector3f());
        Vector3f vp2 = P.sub(v2, new Vector3f());
        C = edge2.cross(vp2, new Vector3f());
        if ((v = N.dot(C)) < 0) return null; // P is on the right side;

        u /= denom;
        v /= denom;

        return new TriangleHit(null, t, new Vector2f(u,v), this.cubeFace);
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
