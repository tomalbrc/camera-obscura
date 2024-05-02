package de.tomalbrc.cameraobscura.render.model.triangle;

import de.tomalbrc.cameraobscura.render.model.RenderModel;
import de.tomalbrc.cameraobscura.render.model.resource.RPElement;
import de.tomalbrc.cameraobscura.render.model.resource.RPModel;
import de.tomalbrc.cameraobscura.util.ColorHelper;
import de.tomalbrc.cameraobscura.util.MiscColors;
import de.tomalbrc.cameraobscura.util.RPHelper;
import de.tomalbrc.cameraobscura.util.TextureHelper;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class TriangleModel implements RenderModel {
    private List<Triangle> modelTriangles = new ObjectArrayList<>();

    private final Map<String, ResourceLocation> textureMap;


    public TriangleModel(RPModel rpModel) {
        for (var element: rpModel.collectElements()) {
            var from = new Vector3f(element.from);
            var to = new Vector3f(element.to);

            Vector3f posOffset = new Vector3f(0.5f); // center block
            from.div(16).sub(posOffset);
            to.div(16).sub(posOffset);

            //rotate(from, to, rpModel.blockRotation);

            List<Triangle> tris = generateCubeTriangles(from, to, element, new Vector3f(rpModel.blockRotation));
            if (tris != null)
                this.modelTriangles.addAll(tris);
        }
        textureMap = rpModel.collectTextures();
    }

    public void rotate(Vector3f from, Vector3f to, Vector3f v) {
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


        var radNormalRot = normal.mul(Mth.DEG_TO_RAD, new Vector3f());
        for (Triangle triangle : triangles) {
            var n = triangle.getNormal().rotate(new Quaternionf().rotateXYZ(radNormalRot.x, radNormalRot.y, radNormalRot.z), new Vector3f());
            var d = Direction.fromDelta(
                    (int) n.x(),
                    (int) n.y(),
                    (int) n.z()
            );
            if (d != null) triangle.textureInfo = element.faces.get(d.getName());
        }

        return triangles;
    }

    Map<String, BufferedImage> textureCache = new Object2ObjectArrayMap<>();

    public RenderModel.ModelHitResult intersect(Vector3f origin, Vector3f direction, Vector3f offset, int textureTint) {
        Triangle triangle = null;
        Triangle.TriangleHit hit = null;
        float smallestT = Float.MAX_VALUE;

        // FIXME: prevents self intersection of the model, with other triangles, no good
        for (var tri: modelTriangles) {
            var res = tri.hitAlt(origin.sub(offset, new Vector3f()), direction);
            if (res != null && res.getT() < smallestT) {
                smallestT = res.getT();
                triangle = tri;
                hit = res;
            }
        }

        if (hit != null) {
            Vector2fc uv = hit.getUV();
            Direction normalDir = hit.getDirection(); // normal direction of the hit triangle
            RPElement.TextureInfo textureInfo = triangle.textureInfo;

            // transparent face
            if (textureInfo == null)
                return new ModelHitResult(MiscColors.TRANSPARENT_COLOR, normalDir); // no face to render, is transparent, skip

            String texKey = textureInfo.texture.replace("#","");
            //resolve texture key in case of placeholders (starting with #)
            while (textureMap.containsKey(texKey)) {
                texKey = textureMap.get(texKey).getPath();
            }

            byte[] data = RPHelper.loadTexture(texKey);
            if (data != null) {
                BufferedImage img = null;
                if (this.textureCache.get(texKey) != null) {
                    img = this.textureCache.get(texKey);
                } else {
                    try {
                        img = ImageIO.read(new ByteArrayInputStream(data));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        return new ModelHitResult(CanvasColor.PURPLE_NORMAL.getRgbColor(), normalDir);
                    }
                    this.textureCache.put(texKey, img);
                }


                int width = img.getWidth();
                // animated textures...
                int realHeight = img.getHeight() / (img.getHeight()/img.getWidth());

                if (textureInfo.uv != null)
                    uv = TextureHelper.remapUV(uv, textureInfo.uv, width, realHeight);

                boolean debug = false; // only render triangle colors during debug
                int s = (int) (width * uv.x());
                int t = (int) (realHeight * uv.y());

                int imgData = debug ? triangle.color : img.getRGB(Mth.clamp(s, 0, img.getWidth()-1), Mth.clamp(t, 0, img.getHeight()-1));

                if (img.getType() == 10) { // hmmm
                    var xx = ColorHelper.unpackColor(imgData);
                    xx[1] /= 1.4;
                    xx[2] /= 1.4;
                    xx[3] /= 1.4;
                    imgData = ColorHelper.packColor(xx);
                }

                // Apply block specific tint, but only if this face has a tintIndex
                if (textureInfo.tintIndex != -1 && textureTint != -1) {
                    imgData = FastColor.ARGB32.multiply(imgData, textureTint);
                }

                return new ModelHitResult(imgData, normalDir);
            }
        }

        // no intersections
        return null;
    }
}
