package de.tomalbrc.cameraobscura.render;

import net.minecraft.core.Direction;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Triangle {

    private final Vector3f v0;
    private final Vector3f v1;
    private final Vector3f v2;

    private final Vector2f uv0;
    private final Vector2f uv1;
    private final Vector2f uv2;


    private final Vector3f v0v1;
    private final Vector3f v0v2;

    private final Vector3f N;
    public final int color;

    public RPElement element;

    public Triangle(Vector3f v0, Vector3f v1, Vector3f v2, Vector2f uv0, Vector2f uv1, Vector2f uv2, int color) {
        this.v0 = v0;
        this.v1 = v1;
        this.v2 = v2;

        this.uv0 = uv0;
        this.uv1 = uv1;
        this.uv2 = uv2;

        this.v0v1 = v1.sub(v0, new Vector3f());
        this.v0v2 = v2.sub(v0, new Vector3f());
        this.N = v0v1.cross(v0v2,  new Vector3f()).normalize(); // N

        this.color = color;
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

        return new TriangleHit(t, new Vector2f(u,v), this.N);
    }

    public TriangleHit hitAlt(Vector3f orig, Vector3f dir) {
        Vector3f pvec = dir.cross(v0v2, new Vector3f());
        float det = v0v1.dot(pvec);

        // if the determinant is negative, the triangle is 'back facing'
        // if the determinant is close to 0, the ray misses the triangle
        if (det < 0.0001f) return null;

        float invDet = 1 / det;

        Vector3f tvec = orig.sub(v0, new Vector3f());
        var u = tvec.dot(pvec) * invDet;
        if (u < 0 || u > 1) return null;

        Vector3f qvec = tvec.cross(v0v1, new Vector3f());
        var v = dir.dot(qvec) * invDet;
        if (v < 0 || u + v > 1) return null;

        var t = v0v2.dot(qvec) * invDet;

        float w = 1 - u - v;

        // Interpolate texture coordinates
        float uCoord = u * uv0.x + v * uv1.x + w * uv2.x;
        float vCoord = u * uv0.y + v * uv1.y + w * uv2.y;

        return new TriangleHit(t, new Vector2f(uCoord, vCoord), this.N);
    }

    static public class TriangleHit {
        private final float t; // Distance along the ray
        private final Vector2f uv;

        private final Vector3f normal;

        public TriangleHit(float t, Vector2f uv, Vector3f normal) {
            this.t = t;
            this.uv = uv;
            this.normal = normal;
        }

        public Direction getDirection() {
            return Direction.fromDelta((int) normal.x(), (int) normal.y(), (int) normal.z());
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
