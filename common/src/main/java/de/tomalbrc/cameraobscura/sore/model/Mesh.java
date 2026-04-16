package de.tomalbrc.cameraobscura.sore.model;

import de.tomalbrc.cameraobscura.sore.Texture;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public final class Mesh {
    public final float[] positions, normals, uvs;
    public final int[] indices;
    public final int @Nullable [] tintIndices;  // one per vertex
    public final boolean[] shade;  // one per vertex
    public final Texture[] vertexTextures;
    public final boolean ao;
    public final boolean translucent;

    public Mesh(float[] positions, float[] normals, float[] uvs, int[] indices, int @Nullable [] tintIndices, boolean[] shade, Texture[] vertexTextures, boolean ao, boolean translucent) {
        this.positions = positions;
        this.normals = normals;
        this.uvs = uvs;
        this.indices = indices;
        this.tintIndices = tintIndices;
        this.shade = shade;
        this.vertexTextures = vertexTextures;
        this.ao = ao;
        this.translucent = translucent;
    }

    public static Mesh skybox(Texture texture) {
        float[] positions = {
                // front
                -1, -1, -1, -1, 1, -1, 1, 1, -1, 1, -1, -1,
                // back
                -1, -1, 1, 1, -1, 1, 1, 1, 1, -1, 1, 1,
                // left
                -1, -1, -1, -1, -1, 1, -1, 1, 1, -1, 1, -1,
                // right
                1, -1, -1, 1, 1, -1, 1, 1, 1, 1, -1, 1,
                // top
                -1, 1, -1, -1, 1, 1, 1, 1, 1, 1, 1, -1,
                // back
                -1, -1, -1, 1, -1, -1, 1, -1, 1, -1, -1, 1
        };

        float[] normals = new float[positions.length];
        for (int i = 0; i < positions.length; i += 3) {
            float x = positions[i];
            float y = positions[i + 1];
            float z = positions[i + 2];
            float len = Mth.sqrt(x * x + y * y + z * z);
            normals[i] = -x / len;
            normals[i + 1] = -y / len;
            normals[i + 2] = -z / len;
        }

        float[] uvs = new float[72];
        float[][] faceUVs = {
                {0, 1, 0, 0, 1, 0, 1, 1}, // front
                {0, 1, 1, 1, 1, 0, 0, 0}, // back
                {0, 1, 1, 1, 1, 0, 0, 0}, // left
                {0, 1, 0, 0, 1, 0, 1, 1}, // right
                {0, 1, 1, 1, 1, 0, 0, 0}, // top
                {0, 1, 1, 1, 1, 0, 0, 0}  // bottom
        };
        int idx = 0;
        for (float[] face : faceUVs) {
            System.arraycopy(face, 0, uvs, idx, 8);
            idx += 8;
        }

        int[] indices = new int[36];
        for (int face = 0; face < 6; face++) {
            int base = face * 4;
            indices[face * 6 + 0] = base;
            indices[face * 6 + 1] = base + 2;
            indices[face * 6 + 2] = base + 1;

            indices[face * 6 + 3] = base;
            indices[face * 6 + 4] = base + 3;
            indices[face * 6 + 5] = base + 2;
        }

        Texture[] vertexTextures = new Texture[positions.length / 3];
        Arrays.fill(vertexTextures, texture);

        boolean[] shade = new boolean[vertexTextures.length];
        Arrays.fill(shade, false);

        return new Mesh(
                positions, normals, uvs, indices, null, shade,
                vertexTextures,
                false, false);
    }

    public Texture textureForVertex(int index) {
        if (vertexTextures == null || index < 0 || index >= vertexTextures.length) return Texture.DEFAULT_TEXTURE;

        Texture tex = vertexTextures[index];
        return tex != null ? tex : Texture.DEFAULT_TEXTURE;
    }
}