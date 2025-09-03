package de.tomalbrc.cameraobscura.render.model.triangle;

import de.tomalbrc.cameraobscura.render.model.resource.RPElement;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class Quad {
    public final Vector3f origin;

    private final Vector3f uVec = new Vector3f();   // v1 - origin
    private final Vector3f vVec = new Vector3f();   // v3 - origin
    private final Vector3f normal = new Vector3f();

    private final float uVecDot;
    private final float vVecDot;

    public boolean shade;
    public boolean light;
    public RPElement.TextureInfo textureInfo;

    private Direction direction;

    public Quad(Vector3f origin, Vector3f u, Vector3f v) {
        this.origin = new Vector3f(origin);

        u.sub(origin, this.uVec);
        v.sub(origin, this.vVec);

        this.uVec.cross(this.vVec, this.normal).negate().normalize();

        this.uVecDot = this.uVec.dot(this.uVec);
        this.vVecDot = this.vVec.dot(this.vVec);

        this.setDirection(this.normal);
        this.shade = true;
    }

    /**
     * Map a (possibly non-axis aligned) normal to the closest cardinal Direction.
     * Chooses the axis with the largest absolute component.
     */
    public void setDirection(Vector3fc dir) {
        float dx = dir.x();
        float dy = dir.y();
        float dz = dir.z();

        float adx = Math.abs(dx);
        float ady = Math.abs(dy);
        float adz = Math.abs(dz);

        if (adx >= ady && adx >= adz) {
            this.direction = dx > 0 ? Direction.EAST : Direction.WEST;
        } else if (ady >= adx && ady >= adz) {
            this.direction = dy > 0 ? Direction.UP : Direction.DOWN;
        } else {
            this.direction = dz > 0 ? Direction.SOUTH : Direction.NORTH;
        }
    }

    public Direction getDirection() {
        return this.direction;
    }

    public record QuadHit(float t, float u, float v, Quad quad) {}

    @Nullable
    public QuadHit rayIntersect(Vector3f orig, Vector3f dir) {
        // denom = normal · dir
        float denom = normal.x * dir.x + normal.y * dir.y + normal.z * dir.z;
        if (Math.abs(denom) < 1e-6f) return null; // parallel or nearly parallel

        // Back-face culling: if denom > 0, the ray and normal point roughly the same way
        // which means the ray hits the back side of the face — ignore it.
        if (denom > 1e-6f) return null;

        // numer = (origin - orig) · normal
        float ox = origin.x - orig.x;
        float oy = origin.y - orig.y;
        float oz = origin.z - orig.z;
        float numer = ox * normal.x + oy * normal.y + oz * normal.z;

        float t = numer / denom;
        if (t < 0f) return null; // behind ray origin

        // hitPoint = orig + dir * t
        float hx = orig.x + dir.x * t;
        float hy = orig.y + dir.y * t;
        float hz = orig.z + dir.z * t;

        // rel = hitPoint - origin
        float rx = hx - origin.x;
        float ry = hy - origin.y;
        float rz = hz - origin.z;

        // project onto uVec and vVec
        float relDotU = rx * uVec.x + ry * uVec.y + rz * uVec.z;
        float relDotV = rx * vVec.x + ry * vVec.y + rz * vVec.z;

        float u = relDotU / uVecDot;
        float v = relDotV / vVecDot;

        if (u < 0f || u > 1f || v < 0f || v > 1f) return null;

        return new QuadHit(t, u, v, this);
    }
}
