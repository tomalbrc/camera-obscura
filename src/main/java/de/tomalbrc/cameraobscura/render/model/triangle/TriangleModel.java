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

    private void readModel(RPModel.View modelView) {
        var elementList = modelView.collectElements();
        for (var element: elementList) {
            var from = new Vector3f(element.from);
            var to = new Vector3f(element.to);

            Vector3f posOffset = new Vector3f(0.5f); // center block
            from.div(16).sub(posOffset);
            to.div(16).sub(posOffset);

            Quaternionf elementRotation = null;
            Vector3f rotation = null;
            float rotationLength = 0;
            if (element.rotation != null) {
                elementRotation = element.rotation.toQuaternionf();
                rotation = element.rotation.getOrigin();
                rotationLength = element.rotation.getOrigin().length();
            }

            List<Triangle> tris = generateCubeTriangles(from, to, element, new Vector3f(modelView.blockRotation()), modelView.uvlock());
            for (int i = 0; i < tris.size(); i++) {
                var n = tris.get(i).getNormal().get(new Vector3f());

                if (element.rotation != null) {
                    if (rotationLength > 0.f)
                        tris.get(i).translate(rotation.x, rotation.y, rotation.z);

                    tris.get(i).rotate(elementRotation);

                    if (rotationLength > 0.f)
                        tris.get(i).translate(-rotation.x, -rotation.y, -rotation.z);
                }

                // rotate triangle vertices and normal (needed so we can get a "Direction" from the normal without taking element rotation into account)
                var rot = modelView.blockRotation().mul(-Mth.DEG_TO_RAD, new Vector3f());


                tris.get(i).rotate(new Quaternionf().rotateX(rot.x()));
                n.rotate(new Quaternionf().rotateX(rot.x()));

                tris.get(i).rotate(new Quaternionf().rotateY(rot.y()));
                n.rotate(new Quaternionf().rotateY(rot.y()));

                tris.get(i).rotate(new Quaternionf().rotateZ(rot.z()));
                n.rotate(new Quaternionf().rotateZ(rot.z()));

                tris.get(i).recalculateVectors();
                tris.get(i).setDirection(n);

                tris.get(i).translate(modelView.offset().x(), modelView.offset().y(), modelView.offset().z());
            }

            this.modelTriangles.addAll(tris);
        }
        this.textureMap.putAll(modelView.collectTextures());
    }

    private List<Triangle> generateCubeTriangles(Vector3f from, Vector3f to, RPElement element, Vector3f normal, boolean uvlock) {
        List<Triangle> triangles = new ObjectArrayList<>();

        float minX = from.x;
        float minY = from.y;
        float minZ = from.z;
        float maxX = to.x;
        float maxY = to.y;
        float maxZ = to.z;

        //if (!uvlock)
            normal.set(0);

        var list = new Vector2f[]{
                new Vector2f(0,0),
                new Vector2f(1,0),
                new Vector2f(1,1),
                new Vector2f(0,1)
        };

        Vector2f corner00, corner10, corner11, corner01;
        int offset;

        // change offset: (int)(rotationInDegrees/90)
        // vanilla "only" supports 90° rotations for textures

        if (element.faces.containsKey("down")) {
            offset = 4- (int)(normal.y) / 90 - element.faces.get("down").rotation / 90;
            corner00 = list[(0+offset)%4];
            corner10 = list[(1+offset)%4];
            corner11 = list[(2+offset)%4];
            corner01 = list[(3+offset)%4];

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
        }

        if (element.faces.containsKey("up")) {
            offset = 4- (int)(normal.y) / 90 - element.faces.get("up").rotation / 90;
            corner00 = list[(0+offset)%4];
            corner10 = list[(1+offset)%4];
            corner11 = list[(2+offset)%4];
            corner01 = list[(3+offset)%4];

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
        }

        if (element.faces.containsKey("north")) {
            offset = 4- (int)(normal.x) / 90 - element.faces.get("north").rotation / 90;
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
        }

        if (element.faces.containsKey("south")) {
            offset = 4- (int)(normal.x) / 90 - element.faces.get("south").rotation / 90;
            corner00 = list[(0+offset)%4];
            corner10 = list[(1+offset)%4];
            corner11 = list[(2+offset)%4];
            corner01 = list[(3+offset)%4];

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
        }

        if (element.faces.containsKey("west")) {
            offset = 4- (int)(normal.z) / 90 - element.faces.get("west").rotation / 90;
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
        }

        if (element.faces.containsKey("east")) {
            offset = 4- (int)(normal.z) / 90 - element.faces.get("east").rotation / 90;
            corner00 = list[(0+offset)%4];
            corner10 = list[(1+offset)%4];
            corner11 = list[(2+offset)%4];
            corner01 = list[(3+offset)%4];

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
        }


        var radNormalRot = normal.mul(-Mth.DEG_TO_RAD, new Vector3f());
        for (Triangle triangle : triangles) {
            var d = triangle.getDirection();
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
            Direction normalDir = hit.triangle().getDirection(); // normal direction of the hit triangle
            RPElement.TextureInfo textureInfo = triangle.textureInfo;

            // transparent face
            if (textureInfo == null) continue;

            String texKey = textureInfo.texture.replace("#","");
            //resolve texture key in case of placeholders (starting with #)
            while (textureMap.containsKey(texKey)) {
                texKey = textureMap.get(texKey).getPath();
            }

            BufferedImage img = RPHelper.loadTextureImage(texKey);
            if (img == null) continue;

            int width = img.getWidth();
            // animated textures...
            int realHeight = (int)(img.getHeight() / (img.getHeight()/(float)img.getWidth()));

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
