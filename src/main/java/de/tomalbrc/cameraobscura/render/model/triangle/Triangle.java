package de.tomalbrc.cameraobscura.render.model.triangle;

import de.tomalbrc.cameraobscura.render.model.resource.RPElement;
import net.minecraft.core.Direction;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class Triangle {

    private final Vector3f v0;
    private final Vector3f v1;
    private final Vector3f v2;

    private final Vector2f uv0;
    private final Vector2f uv1;
    private final Vector2f uv2;


    private final Vector3f v0v1;
    private final Vector3f v0v2;

    private final Vector3fc N;

    private final int color;

    public RPElement.TextureInfo textureInfo;
    public boolean shade;

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

        this.shade = true;
    }

    public int getColor() {
        return this.color;
    }

    public Vector3fc getNormal() {
        return N;
    }

    // Function to check ray intersection with the triangle (Möller-Trumbore algorithm)
    public TriangleHit rayIntersect(Vector3f orig, Vector3f dir) {
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

    public record TriangleHit(
            float t, // Distance along the ray
            Vector2fc uv,
            Vector3fc normal
    ) {
        public Direction getDirection() {
            return Direction.fromDelta((int) normal.x(), (int) normal.y(), (int) normal.z());
        }
    }
}
