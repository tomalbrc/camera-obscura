package de.tomalbrc.cameraobscura.render.model.triangle;

import de.tomalbrc.cameraobscura.render.model.resource.RPElement;
import net.minecraft.core.Direction;
import org.joml.*;

public class Triangle {

    private final Vector3f v0;
    private final Vector3f v1;
    private final Vector3f v2;

    private final Vector2f uv0;
    private final Vector2f uv1;
    private final Vector2f uv2;

    private Vector3f v0v1;
    private Vector3f v0v2;

    private Vector3f normal;

    public RPElement.TextureInfo textureInfo;
    public boolean shade;

    public boolean light;

    private Direction direction;

    public Triangle(Vector3f v0, Vector3f v1, Vector3f v2, Vector2f uv0, Vector2f uv1, Vector2f uv2) {
        this.v0 = v0;
        this.v1 = v1;
        this.v2 = v2;

        this.uv0 = uv0;
        this.uv1 = uv1;
        this.uv2 = uv2;

        this.recalculateVectors();

        this.setDirection(normal);

        this.shade = true;
    }

    public void setDirection(Vector3fc dir) {
        this.direction = Direction.fromDelta(
                (int) dir.x(),
                (int) dir.y(),
                (int) dir.z()
        );
    }

    public Direction getDirection() {
        return this.direction;
    }

    public void rotate(Quaternionf quaternionf) {
        this.v0.rotate(quaternionf);
        this.v1.rotate(quaternionf);
        this.v2.rotate(quaternionf);
    }

    public void translate(float x, float y, float z) {
        this.v0.sub(x, y, z);
        this.v1.sub(x, y, z);
        this.v2.sub(x, y, z);
    }

    public void recalculateVectors() {
        this.v0v1 = v1.sub(this.v0, new Vector3f());
        this.v0v2 = v2.sub(this.v0, new Vector3f());
        this.normal = v0v1.cross(this.v0v2,  new Vector3f()).normalize(); // N
    }

    public Vector3fc getNormal() {
        return normal;
    }

    // Function to check ray intersection with the triangle (Möller-Trumbore algorithm)
    public TriangleHit rayIntersect(Vector3f orig, Vector3f dir) {
        Vector3f pvec = dir.cross(v0v2, new Vector3f());
        float det = v0v1.dot(pvec);

        // if the determinant is negative, the triangle is 'back facing'
        // if the determinant is close to 0, the ray misses the triangle
        if (det < 0.00001f) return null;

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

        return new TriangleHit(t, new Vector2f(uCoord, vCoord), this);
    }

    public record TriangleHit(
            float t, // Distance along the ray
            Vector2fc uv,
            Triangle triangle
    ) {
    }
}
