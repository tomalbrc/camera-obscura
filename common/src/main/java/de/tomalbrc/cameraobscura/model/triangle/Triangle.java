package de.tomalbrc.cameraobscura.model.triangle;

import de.tomalbrc.cameraobscura.model.resource.RPElement;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class Triangle {
    public final Vector3f v0;
    public final Vector3f v1;
    public final Vector3f v2;

    public final Vector2f uv0;
    public final Vector2f uv1;
    public final Vector2f uv2;
    public RPElement.TextureInfo textureInfo;
    public boolean shade;
    public int tintindex = -1;
    public boolean light;
    private Vector3f normal;

    public Triangle(Vector3f v0, Vector3f v1, Vector3f v2, Vector2f uv0, Vector2f uv1, Vector2f uv2, boolean shade) {
        this.v0 = v0;
        this.v1 = v1;
        this.v2 = v2;

        this.uv0 = uv0;
        this.uv1 = uv1;
        this.uv2 = uv2;

        this.calcNormal();

        this.shade = shade;
    }

    public void calcNormal() {
        Vector3f v0v1 = v1.sub(this.v0, new Vector3f());
        Vector3f v0v2 = v2.sub(this.v0, new Vector3f());
        this.normal = v0v1.cross(v0v2).normalize();
    }

    public Vector3fc getNormal() {
        return normal;
    }
}
