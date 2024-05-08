package de.tomalbrc.cameraobscura.render.model.triangle;

import de.tomalbrc.cameraobscura.render.model.RenderModel;
import de.tomalbrc.cameraobscura.render.model.resource.RPElement;
import de.tomalbrc.cameraobscura.render.model.resource.RPModel;
import de.tomalbrc.cameraobscura.util.RPHelper;
import de.tomalbrc.cameraobscura.util.TextureHelper;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

public class TriangleModel implements RenderModel {
    private final List<Triangle> modelTriangles = new ObjectArrayList<>();

    private final Map<String, ResourceLocation> textureMap = new Object2ObjectOpenHashMap<>();

    public TriangleModel(RPModel.View... rpModel) {
        for (int i = 0; i < rpModel.length; i++) {
            this.readModel(rpModel[i]);
        }
    }

    public TriangleModel combine(TriangleModel other) {
        modelTriangles.addAll(other.modelTriangles);
        textureMap.putAll(other.textureMap);
        return this;
    }

    private void readModel(RPModel.View rpModel) {
        for (var element: rpModel.collectElements()) {
            var from = new Vector3f(element.from);
            var to = new Vector3f(element.to);

            Vector3f posOffset = new Vector3f(0.5f); // center block
            from.div(16).sub(posOffset);
            to.div(16).sub(posOffset);

            //rotate(from, to, rpModel.blockRotation());

            List<Triangle> tris = generateCubeTriangles(from, to, element, new Vector3f(rpModel.blockRotation()));
            /*for (int i = 0; i < tris.size(); i++) {
                var rot = rpModel.blockRotation().mul(-Mth.DEG_TO_RAD, new Vector3f());
                tris.get(i).rotate(new Quaternionf().rotateXYZ(rot.x(), rot.y(), rot.z()).normalize());
            }*/

            if (tris != null)
                this.modelTriangles.addAll(tris);
        }
        this.textureMap.putAll(rpModel.collectTextures());
    }

    public void rotate(Vector3f from, Vector3f to, Vector3f v) {
        //for (int i = 0; i < modelTriangles.size(); i++) {
        //    Triangle t;
        //}

        if (v.x != 0) {
            from.rotateX(v.x * Mth.DEG_TO_RAD);
            to.rotateX(v.x * Mth.DEG_TO_RAD);
        }
        if (v.y != 0) {
            from.rotateY(v.y * Mth.DEG_TO_RAD);
            to.rotateY(v.y * Mth.DEG_TO_RAD);
        }
        if (v.z != 0) {
            from.rotateZ(v.z * Mth.DEG_TO_RAD);
            to.rotateZ(v.z * Mth.DEG_TO_RAD);
        }

        from.set(
                Math.min(from.x, to.x),
                Math.min(from.y, to.y),
                Math.min(from.z, to.z)
        );
        to.set(
                Math.max(from.x, to.x),
                Math.max(from.y, to.y),
                Math.max(from.z, to.z)
        );
    }

    private List<Triangle> generateCubeTriangles(Vector3f from, Vector3f to, RPElement element, Vector3f normal) {
        List<Triangle> triangles = new ObjectArrayList<>();

        float minX = from.x;
        float minY = from.y;
        float minZ = from.z;
        float maxX = to.x;
        float maxY = to.y;
        float maxZ = to.z;

        var list = new Vector2f[]{
                new Vector2f(0,0),
                new Vector2f(1,0),
                new Vector2f(1,1),
                new Vector2f(0,1)
        };


        // change offset: (int)(rotationInDegrees/90)
        // vanilla "only" supports 90° rotations for textures
        int offset = (int)(normal.y) / 90;
        var corner00 = list[(0+offset)%4];
        var corner10 = list[(1+offset)%4];
        var corner11 = list[(2+offset)%4];
        var corner01 = list[(3+offset)%4];


        // Bottom face
        triangles.add(new Triangle(
                new Vector3f(maxX, minY, maxZ),
                new Vector3f(minX, minY, minZ),
                new Vector3f(maxX, minY, minZ),
                corner01,
                corner11,
                corner10,
                -1419412));

        triangles.add(new Triangle(
                new Vector3f(minX, minY, minZ),
                new Vector3f(maxX, minY, maxZ),
                new Vector3f(minX, minY, maxZ),
                corner10,
                corner00,
                corner01,
                -2419412));

        // Top face
        triangles.add(new Triangle(
                new Vector3f(maxX, maxY, minZ),
                new Vector3f(minX, maxY, minZ),
                new Vector3f(maxX, maxY, maxZ),
                corner00,
                corner11,
                corner10,
                6315465));

        triangles.add(new Triangle(
                new Vector3f(minX, maxY, maxZ),
                new Vector3f(maxX, maxY, maxZ),
                new Vector3f(minX, maxY, minZ),
                corner11,
                corner00,
                corner01,
                5315465));


        offset = (int)(normal.x) / 90;
        corner00 = list[(0+offset)%4];
        corner10 = list[(1+offset)%4];
        corner11 = list[(2+offset)%4];
        corner01 = list[(3+offset)%4];

        // Front face
        triangles.add(new Triangle(
                new Vector3f(minX, minY, minZ),
                new Vector3f(minX, maxY, minZ),
                new Vector3f(maxX, maxY, minZ),
                corner10,
                corner00,
                corner11,
                -32522));

        triangles.add(new Triangle(
                new Vector3f(minX, minY, minZ),
                new Vector3f(maxX, maxY, minZ),
                new Vector3f(maxX, minY, minZ),
                corner00,
                corner01,
                corner11,
                432522));

        // Back face
        triangles.add(new Triangle(
                new Vector3f(maxX, minY, maxZ),
                new Vector3f(maxX, maxY, maxZ),
                new Vector3f(minX, maxY, maxZ),
                corner10,
                corner00,
                corner11, 832453));

        triangles.add(new Triangle(
                new Vector3f(maxX, minY, maxZ),
                new Vector3f(minX, maxY, maxZ),
                new Vector3f(minX, minY, maxZ),
                corner00,
                corner01,
                corner11, -832453));

        offset = (int)(normal.z) / 90;
        corner00 = list[(0+offset)%4];
        corner10 = list[(1+offset)%4];
        corner11 = list[(2+offset)%4];
        corner01 = list[(3+offset)%4];


        // Left face
        triangles.add(new Triangle(
                new Vector3f(minX, minY, minZ),
                new Vector3f(minX, minY, maxZ),
                new Vector3f(minX, maxY, maxZ),
                corner11,
                corner10,
                corner01,
                -52151));

        triangles.add(new Triangle(
                new Vector3f(minX, minY, minZ),
                new Vector3f(minX, maxY, maxZ),
                new Vector3f(minX, maxY, minZ),
                corner10,
                corner00,
                corner01,
                252151));

        // Right face
        triangles.add(new Triangle(
                new Vector3f(maxX, minY, minZ),
                new Vector3f(maxX, maxY, minZ),
                new Vector3f(maxX, maxY, maxZ),
                corner10,
                corner00,
                corner11,
                41245));

        triangles.add(new Triangle(
                new Vector3f(maxX, minY, minZ),
                new Vector3f(maxX, maxY, maxZ),
                new Vector3f(maxX, minY, maxZ),
                corner00,
                corner01,
                corner11,
                141245));


        var radNormalRot = normal.mul(-Mth.DEG_TO_RAD, new Vector3f());
        for (Triangle triangle : triangles) {
            var n = triangle.getNormal().rotate(new Quaternionf().rotateXYZ(radNormalRot.x, radNormalRot.y, radNormalRot.z), new Vector3f());
            var d = Direction.fromDelta(
                    (int) n.x(),
                    (int) n.y(),
                    (int) n.z()
            );
            if (d != null) triangle.textureInfo = element.faces.get(d.getName());
            triangle.shade = element.shade;
        }

        return triangles;
    }

    public List<ModelHit> intersect(Vector3f origin, Vector3f direction, Vector3f offset, int textureTint) {

        List<Triangle.TriangleHit> hitList = new ObjectArrayList<>();
        List<ModelHit> modelHitList = new ObjectArrayList<>();

        for (int i = 0; i < this.modelTriangles.size(); i++) {
            var res = this.modelTriangles.get(i).rayIntersect(origin.sub(offset, new Vector3f()), direction);
            if (res != null) {
                hitList.add(res);
            }
        }

        hitList.sort((a,b) -> Float.compare(a.t(), b.t()));

        for (int i = 0; i < hitList.size(); i++) {
            Triangle.TriangleHit hit = hitList.get(i);
            Triangle triangle = hit.triangle();

            Vector2fc uv = hit.uv();
            Direction normalDir = hit.getDirection(); // normal direction of the hit triangle
            RPElement.TextureInfo textureInfo = triangle.textureInfo;

            // transparent face
            if (textureInfo == null) continue;

            uv = rotateUV(uv, new Vector2f(), textureInfo.rotation*Mth.DEG_TO_RAD);

            String texKey = textureInfo.texture.replace("#","");
            //resolve texture key in case of placeholders (starting with #)
            while (textureMap.containsKey(texKey)) {
                texKey = textureMap.get(texKey).getPath();
            }

            BufferedImage img = RPHelper.loadTextureImage(texKey);
            if (img == null) continue;

            int width = img.getWidth();
            // animated textures...
            int realHeight = img.getHeight() / (img.getHeight()/img.getWidth());

            if (textureInfo.uv != null)
                uv = TextureHelper.remapUV(uv, textureInfo.uv, width, realHeight);

            boolean debug = false; // only render triangle colors during debug
            int s = (int) (width * uv.x());
            int t = (int) (realHeight * uv.y());

            int imgData = debug ? triangle.getColor() : img.getRGB(Mth.clamp(s, 0, img.getWidth()-1), Mth.clamp(t, 0, img.getHeight()-1));

            // Apply block specific tint, but only if this face has a tintIndex
            if (textureInfo.tintIndex != -1 && textureTint != -1) {
                imgData = FastColor.ARGB32.multiply(imgData, textureTint);
            }

            modelHitList.add(new ModelHit(imgData, normalDir, triangle.shade));
        }

        return modelHitList;
    }

    Vector2fc rotateUV(Vector2fc uv, Vector2fc pivot, float rotation) {
        float sine = Mth.sin(rotation);
        float cosine = Mth.cos(rotation);

        Vector2f ruv = new Vector2f(uv);
        ruv.sub(pivot);
        ruv.x = uv.x() * cosine - uv.x() * sine;
        ruv.y = uv.x() * sine + uv.y() * cosine;
        ruv.add(pivot);

        return ruv;
    }
}
