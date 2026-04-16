package de.tomalbrc.cameraobscura.renderer.entity;

import de.tomalbrc.cameraobscura.model.resource.RPElement;
import de.tomalbrc.cameraobscura.model.resource.RPModel;
import de.tomalbrc.cameraobscura.model.triangle.ModelTesselator;
import de.tomalbrc.cameraobscura.sore.model.Mesh;
import de.tomalbrc.cameraobscura.util.Constants;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Direction;
import org.joml.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ModelBakery {
    private final String texture;
    private final int width;
    private final int height;

    public ModelBakery(String texture, int width, int height) {
        this.texture = texture;
        this.width = width;
        this.height = height;
    }

    private PolygonData createFace(Vertex[] verts,
                                   float uMin, float vMin, float uMax, float vMax,
                                   boolean mirror, Direction direction) {
        float texScaleU = width;
        float texScaleV = height;
        verts[0] = verts[0].remap(uMin / texScaleU, vMin / texScaleV);
        verts[1] = verts[1].remap(uMax / texScaleU, vMin / texScaleV);
        verts[2] = verts[2].remap(uMax / texScaleU, vMax / texScaleV);
        verts[3] = verts[3].remap(uMin / texScaleU, vMax / texScaleV);

        if (mirror) {
            int n = verts.length;
            for (int i = 0; i < n / 2; i++) {
                Vertex tmp = verts[i];
                verts[i] = verts[n - 1 - i];
                verts[n - 1 - i] = tmp;
            }
        }

        Vector3d normal = direction.step().get(new Vector3d());
        if (mirror) {
            normal.mul(-1, 1, 1);
        }
        return new PolygonData(verts, normal);
    }

    private List<PolygonData> bakeCube(float texU, float texV,
                                       float originX, float originY, float originZ,
                                       float dimX, float dimY, float dimZ,
                                       float growX, float growY, float growZ,
                                       boolean mirror) {
        List<PolygonData> faces = new ObjectArrayList<>();

        float minX = originX;
        float minY = originY;
        float minZ = originZ;
        float maxX = originX + dimX;
        float maxY = originY + dimY;
        float maxZ = originZ + dimZ;

        float s = maxX;
        float t = maxY;
        float u = maxZ;
        minX -= growX;
        minY -= growY;
        minZ -= growZ;
        s += growX;
        t += growY;
        u += growZ;

        if (mirror) {
            float tmp = s;
            s = minX;
            minX = tmp;
        }

        Vertex v1 = new Vertex(minX, minY, minZ, 0, 0);
        Vertex v2 = new Vertex(s, minY, minZ, 0, 8);
        Vertex v3 = new Vertex(s, t, minZ, 8, 8);
        Vertex v4 = new Vertex(minX, t, minZ, 8, 0);
        Vertex v5 = new Vertex(minX, minY, u, 0, 0);
        Vertex v6 = new Vertex(s, minY, u, 0, 8);
        Vertex v7 = new Vertex(s, t, u, 8, 8);
        Vertex v8 = new Vertex(minX, t, u, 8, 0);

        float u0 = texU;
        float v0 = texV;
        float u1 = u0 + dimZ;
        float u2 = u0 + dimZ + dimX;
        float u3 = u0 + dimZ + dimX + dimX;
        float u4 = u0 + dimZ + dimX + dimZ;
        float u5 = u0 + dimZ + dimX + dimZ + dimX;

        float v1v = v0;
        float v2v = v0 + dimZ;
        float v3v = v0 + dimZ + dimY;

        faces.add(createFace(new Vertex[]{v6, v5, v1, v2}, u1, v1v, u2, v2v, mirror, Direction.DOWN));
        faces.add(createFace(new Vertex[]{v3, v4, v8, v7}, u2, v2v, u3, v1v, mirror, Direction.UP));
        faces.add(createFace(new Vertex[]{v1, v5, v8, v4}, u0, v2v, u1, v3v, mirror, Direction.WEST));
        faces.add(createFace(new Vertex[]{v2, v1, v4, v3}, u1, v2v, u2, v3v, mirror, Direction.NORTH));
        faces.add(createFace(new Vertex[]{v6, v2, v3, v7}, u2, v2v, u4, v3v, mirror, Direction.EAST));
        faces.add(createFace(new Vertex[]{v5, v6, v7, v8}, u4, v2v, u5, v3v, mirror, Direction.SOUTH));

        return faces;
    }

    private Mesh buildMesh(List<PolygonData> polygons) {
        List<RPElement> elements = new ObjectArrayList<>();
        for (PolygonData poly : polygons) {
            Vector3f min = new Vector3f(Float.POSITIVE_INFINITY);
            Vector3f max = new Vector3f(Float.NEGATIVE_INFINITY);
            for (Vertex v : poly.vertices) {
                min.min(v.pos());
                max.max(v.pos());
            }

            Vertex vMin = poly.vertices[0];
            Vertex vMax = poly.vertices[0];
            for (Vertex v : poly.vertices) {
                if (v.pos().equals(min)) vMin = v;
                if (v.pos().equals(max)) vMax = v;
            }

            RPElement e = new RPElement();
            e.name = null;
            e.from = min;
            e.to = max;
            e.shade = true;
            e.light = true;
            e.faces = new Object2ObjectOpenHashMap<>();

            Direction dir = Direction.getNearest((int) poly.normal.x, (int) poly.normal.y, (int) poly.normal.z, null);

            assert dir != null;

            if ((dir.getAxisDirection() == Direction.AxisDirection.NEGATIVE) == (dir.getAxis() == Direction.Axis.Z)) {
                dir = dir.getOpposite();
            }

            e.faces.put(dir, new RPElement.TextureInfo("#tex", new Vector4f(vMax.u * 16, vMax.v * 16, vMin.u * 16, vMin.v * 16), 0, 0));

            elements.add(e);
        }

        RPModel model = new RPModel();
        model.textures = new Object2ObjectOpenHashMap<>();
        model.textures.put("tex", RPModel.TextureEntry.of(texture));
        model.ambientOcclusion = true;
        model.elements = new ObjectArrayList<>(elements);
        RPModel.View view = new RPModel.View(model);
        return new ModelTesselator(view).build();
    }

    public record CubeDeformation(float grow) {
        public static final CubeDeformation NONE = new CubeDeformation(0);
    }

    public static class BakedPart {
        public final Mesh mesh;
        public final Vector3d localPivot;   // translation from parent, in block units
        public final PartPose initialPose;
        public final Map<String, BakedPart> children = new LinkedHashMap<>();

        public BakedPart(Mesh mesh, Vector3d localPivot, PartPose initialPose) {
            this.mesh = mesh;
            this.localPivot = localPivot;
            this.initialPose = initialPose;
        }
    }

    public record PolygonData(Vertex[] vertices, Vector3d normal) {
    }

    public record Vertex(float x, float y, float z, float u, float v) {
        Vertex remap(float newU, float newV) {
            return new Vertex(x, y, z, newU, newV);
        }

        Vector3f pos() {
            return new Vector3f(x, y, z);
        }
    }

    public record CubeDef(String name,
                          float fromX, float fromY, float fromZ,
                          float toX, float toY, float toZ,
                          float texU, float texV,
                          boolean mirror,
                          CubeDeformation deformation) {
        public float originX() {
            return fromX;
        }

        public float originY() {
            return fromY;
        }

        public float originZ() {
            return fromZ;
        }

        public float dimX() {
            return toX - fromX;
        }

        public float dimY() {
            return toY - fromY;
        }

        public float dimZ() {
            return toZ - fromZ;
        }

        public float growX() {
            return deformation.grow();
        }

        public float growY() {
            return deformation.grow();
        }

        public float growZ() {
            return deformation.grow();
        }
    }

    public static class CubeListBuilder {
        private final List<CubeDef> cubes = new ObjectArrayList<>();
        private int texOffsU, texOffsV;
        private boolean mirror;

        public static CubeListBuilder create() {
            return new CubeListBuilder();
        }

        public CubeListBuilder texOffs(int u, int v) {
            texOffsU = u;
            texOffsV = v;
            return this;
        }

        public CubeListBuilder mirror() {
            return mirror(true);
        }

        public CubeListBuilder mirror(boolean bl) {
            this.mirror = bl;
            return this;
        }

        public CubeListBuilder addBox(String name, float ox, float oy, float oz, float sx, float sy, float sz, CubeDeformation deformation) {
            cubes.add(new CubeDef(name, ox, oy, oz, ox + sx, oy + sy, oz + sz, texOffsU, texOffsV, mirror, deformation));
            return this;
        }

        public CubeListBuilder addBox(String name, float ox, float oy, float oz, float sx, float sy, float sz) {
            return addBox(name, ox, oy, oz, sx, sy, sz, CubeDeformation.NONE);
        }

        public CubeListBuilder addBox(float ox, float oy, float oz, float sx, float sy, float sz) {
            return addBox(null, ox, oy, oz, sx, sy, sz, CubeDeformation.NONE);
        }

        public CubeListBuilder addBox(float ox, float oy, float oz, float sx, float sy, float sz, CubeDeformation deformation) {
            return addBox(null, ox, oy, oz, sx, sy, sz, deformation);
        }

        public CubeListBuilder addBox(String name, float ox, float oy, float oz, float w, float h, float d, float texU, float texV) {
            // TODO: texU and texV should be scale?
            cubes.add(new CubeDef(name, ox, oy, oz, ox + w, oy + h, oz + d, texU, texV, mirror, CubeDeformation.NONE));
            return this;
        }

        public CubeListBuilder addBox(String name, float ox, float oy, float oz, float w, float h, float d, CubeDeformation deformation, int texU, int texV) {
            cubes.add(new CubeDef(name, ox, oy, oz, ox + w, oy + h, oz + d, texU, texV, mirror, deformation));
            return this;
        }

        public List<CubeDef> getCubes() {
            return cubes;
        }
    }

    public record PartPose(
            float x, float y, float z,
            float xRot, float yRot, float zRot,
            float xScale, float yScale, float zScale
    ) {
        public static final PartPose ZERO = new PartPose(0, 0, 0, 0, 0, 0, 1, 1, 1);

        public static PartPose offset(float x, float y, float z) {
            return new PartPose(x, y, z, 0, 0, 0, 1, 1, 1);
        }

        public static PartPose offsetAndRotation(float x, float y, float z, float xRot, float yRot, float zRot) {
            return new PartPose(x, y, z, xRot, yRot, zRot, 1, 1, 1);
        }

        public static PartPose rotation(float xRot, float yRot, float zRot) {
            return new PartPose(0, 0, 0, xRot, yRot, zRot, 1, 1, 1);
        }
    }

    public record ModelDefinition(PartDefinition root) {
        public ModelDefinition(ModelBakery bakery) {
            this(bakery.new PartDefinition());
        }
    }

    public class PartDefinition {
        private final List<CubeDef> cubes = new ObjectArrayList<>();
        private final Map<String, PartDefinition> children = new LinkedHashMap<>();
        private PartPose pose = PartPose.ZERO;

        public PartDefinition addOrReplaceChild(String name, CubeListBuilder builder, PartPose pose) {
            PartDefinition child = new PartDefinition();
            child.cubes.addAll(builder.getCubes());
            child.pose = pose;
            children.put(name, child);
            return child;
        }

        public List<CubeDef> getCubes() {
            return cubes;
        }

        public Map<String, PartDefinition> getChildren() {
            return children;
        }

        public PartPose getPose() {
            return pose;
        }

        public BakedPart bake() {
            return bakePart(this, Constants.ZERO_MATRIX);
        }

        private BakedPart bakePart(PartDefinition def, Matrix4dc parentWorld) {
            PartPose p = def.getPose();

            Matrix4d local = new Matrix4d()
                    .translate(p.x(), p.y(), p.z());
            if (p.xRot() != 0 || p.yRot() != 0 || p.zRot() != 0)
                local.rotateZYX(p.zRot(), p.yRot(), p.xRot());
            if (p.xScale() != 1 || p.yScale() != 1 || p.zScale() != 1)
                local.scale(p.xScale(), p.yScale(), p.zScale());

            // world transform = parent * local
            Matrix4d world = new Matrix4d(parentWorld).mul(local);

            List<PolygonData> polys = new ObjectArrayList<>();
            for (CubeDef cube : def.getCubes()) {
                polys.addAll(ModelBakery.this.bakeCube(
                        cube.texU(), cube.texV(),
                        cube.originX(), cube.originY(), cube.originZ(),
                        cube.dimX(), cube.dimY(), cube.dimZ(),
                        cube.growX(), cube.growY(), cube.growZ(),
                        cube.mirror()));
            }
            Mesh mesh = polys.isEmpty() ? null : buildMesh(polys);

            Vector3d localPivot = new Vector3d(p.x(), p.y(), p.z()).div(16f);
            BakedPart part = new BakedPart(mesh, localPivot, p);

            for (var entry : def.getChildren().entrySet()) {
                part.children.put(entry.getKey(), bakePart(entry.getValue(), world));
            }
            return part;
        }
    }
}