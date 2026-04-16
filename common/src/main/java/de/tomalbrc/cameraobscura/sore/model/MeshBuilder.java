package de.tomalbrc.cameraobscura.sore.model;

import de.tomalbrc.cameraobscura.sore.Texture;
import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;

/**
 * Immediate‑mode style mesh builder.
 * <p>
 * Usage:
 * <pre>
 * MeshBuilder builder = new MeshBuilder();
 * builder.beginTriangles();
 * builder.vertex(0,0,0, 0,1,0, 0,0, 0, true, texture);
 * builder.vertex(1,0,0, 0,1,0, 1,0, 0, true, texture);
 * builder.vertex(0,1,0, 0,1,0, 0,1, 0, true, texture);
 * var mesh = builder.end();
 * </pre>
 */
public class MeshBuilder {
    private final FloatArrayList positions = new FloatArrayList();
    private final FloatArrayList normals = new FloatArrayList();
    private final FloatArrayList uvs = new FloatArrayList();
    private final IntArrayList tintIndices = new IntArrayList();
    private final BooleanArrayList shades = new BooleanArrayList();
    private final java.util.ArrayList<Texture> textures = new java.util.ArrayList<>();
    private final IntArrayList indices = new IntArrayList();

    private boolean building = false;
    private int vertexCount = 0;
    private boolean hasTranslucency = false;

    /**
     * Starts building a triangle mesh.
     * Call this before adding any vertices with {@link #vertex}.
     * Resets all internal buffers.
     */
    public void beginTriangles() {
        if (building) throw new IllegalStateException("Already building a mesh");
        reset();
        building = true;
    }

    /**
     * Adds a single vertex.
     * For triangles, you must call this exactly three times per triangle,
     * in clockwise or counter‑clockwise order.
     *
     * @param x     X coordinate
     * @param y     Y coordinate
     * @param z     Z coordinate
     * @param nx    X component of the vertex normal
     * @param ny    Y component of the vertex normal
     * @param nz    Z component of the vertex normal
     * @param u     U texture coordinate (0‑1 range, but can be arbitrary)
     * @param v     V texture coordinate
     * @param tint  tint index
     * @param shade Whether lighting should affect this vertex
     * @param tex   The texture to use
     */
    public void vertex(float x, float y, float z,
                       float nx, float ny, float nz,
                       float u, float v,
                       int tint, boolean shade, Texture tex) {
        if (!building) throw new IllegalStateException("Call beginTriangles() first");

        positions.add(x);
        positions.add(y);
        positions.add(z);
        normals.add(nx);
        normals.add(ny);
        normals.add(nz);
        uvs.add(u);
        uvs.add(v);
        tintIndices.add(tint);
        shades.add(shade);
        textures.add(tex);
        indices.add(vertexCount);
        vertexCount++;

        if (tex != null && tex.hasTranslucency()) {
            hasTranslucency = true;
        }
    }

    /**
     * Ends the current mesh and builds the final {@link Mesh} object.
     * After calling this, you can call {@link #beginTriangles} again to start a new mesh.
     *
     * @return The built Mesh, or null if no vertices were added.
     */
    public Mesh end() {
        if (!building) throw new IllegalStateException("Not building a mesh");
        building = false;

        if (vertexCount == 0) return null;

        return new Mesh(
                positions.toFloatArray(),
                normals.toFloatArray(),
                uvs.toFloatArray(),
                indices.toIntArray(),
                tintIndices.toIntArray(),
                shades.toBooleanArray(),
                textures.toArray(new Texture[0]),
                true,
                hasTranslucency
        );
    }

    /**
     * Resets the builder without requiring a begin/end cycle.
     */
    public void reset() {
        positions.clear();
        normals.clear();
        uvs.clear();
        tintIndices.clear();
        shades.clear();
        textures.clear();
        indices.clear();
        vertexCount = 0;
        hasTranslucency = false;
        building = false;
    }
}