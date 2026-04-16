package de.tomalbrc.cameraobscura.sore.model;

import de.tomalbrc.cameraobscura.sore.Texture;
import org.jetbrains.annotations.Nullable;

public final class Model {
    public static final Model SKY = new Model(Mesh.skybox(Texture.generateSolid(0x7f_FFFFFF)), 1, 1);
    public final Mesh mesh;
    public final double @Nullable [] ao;

    public double @Nullable [] skyLight;
    public double @Nullable [] blockLight;

    public Model(Mesh mesh, double @Nullable [] skyLight, double @Nullable [] blockLight, double @Nullable [] ao) {
        this.mesh = mesh;
        this.skyLight = skyLight;
        this.blockLight = blockLight;
        this.ao = ao;
    }

    public Model(Mesh mesh) {
        this.mesh = mesh;
        ao = null;
    }

    public Model(Mesh mesh, double block, double sky) {
        this.mesh = mesh;
        ao = null;
        skyLight = new double[]{sky};
        blockLight = new double[]{block};
    }

    public float[] positions() {
        return mesh.positions;
    }

    public float[] normals() {
        return mesh.normals;
    }

    public float[] uvs() {
        return mesh.uvs;
    }

    public int[] indices() {
        return mesh.indices;
    }

    public Texture[] vertexTextures() {
        return mesh.vertexTextures;
    }

    public Texture textureForVertex(int i) {
        return mesh.textureForVertex(i);
    }
}