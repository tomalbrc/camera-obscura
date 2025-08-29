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

        this.vVec.cross(this.uVec, this.normal).normalize();
        if (this.normal.dot(origin) > 0) {
            this.normal.negate(); // flip if pointing inward
        }

        this.uVecDot = this.uVec.dot(this.uVec);
        this.vVecDot = this.vVec.dot(this.vVec);

        this.setDirection(this.normal);
        this.shade = true;
    }

    public void setDirection(Vector3fc dir) {
        int dx = (int) Math.signum(dir.x());
        int dy = (int) Math.signum(dir.y());
        int dz = (int) Math.signum(dir.z());
        this.direction = fromDelta(dx, dy, dz).getOpposite();
    }

    public static Direction fromDelta(int x, int y, int z) {
        if (x == 0) {
            if (y == 0) {
                if (z > 0) return Direction.SOUTH;
                if (z < 0) return Direction.NORTH;
            } else if (z == 0) {
                if (y > 0) return Direction.UP;
                return Direction.DOWN;
            }
        } else if (y == 0 && z == 0) {
            if (x > 0) return Direction.EAST;
            return Direction.WEST;
        }
        return Direction.UP;
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

        // numer = (origin - orig) * normal
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
