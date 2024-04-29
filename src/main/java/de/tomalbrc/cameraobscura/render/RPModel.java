package de.tomalbrc.cameraobscura.render;

import de.tomalbrc.cameraobscura.util.RPHelper;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Vector2f;
import org.joml.Vector3f;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class RPModel {
    private static int DEFAULT_COLOR = CanvasColor.PURPLE_HIGH.getRgbColor();
    private ResourceLocation parent;
    private Map<String, String> textures;
    private List<RPElement> elements;

    //---

    private Map<String, ResourceLocation> textureMap;
    private List<RPElement> allElements;
    private List<Triangle> modelTriangles = new ObjectArrayList<>();

    public RPModel prepare() {
        this.textureMap = collectTextures();
        this.allElements = collectElements();
        for (var element: this.allElements) {
            var offset = new Vector3f(0.5f); // center block
            element.from.div(16).sub(offset);
            element.to.div(16).sub(offset);
        }
        return this;
    }

    public RPModel buildGeometry() {
        for (var element: this.allElements) {
            List<Triangle> tris = generateCubeTriangles(element);
            if (tris != null)
                this.modelTriangles.addAll(tris);
        }
        return this;
    }

    private Map<String, ResourceLocation> collectTextures() {
        Map<String, ResourceLocation> collectedTextures = new Object2ObjectOpenHashMap<>();
        this.textures.forEach((key,value) -> {
            collectedTextures.put(key, new ResourceLocation(value.replace("#","")));
        });

        ResourceLocation parent = this.parent;
        while (parent != null && parent.getPath() != null && !parent.getPath().isEmpty()) {
            var child = RPHelper.loadModel(parent.getPath());
            if (child != null) {
                if (child.textures != null) child.textures.forEach((key,value) -> {
                    collectedTextures.put(key, new ResourceLocation(value.replace("#","")));
                });

                parent = child.parent;
            } else {
                break;
            }
        }
        return collectedTextures;
    }

    private List<RPElement> collectElements() {
        List<RPElement> elementsList = new ObjectArrayList<>();
        if (this.elements != null) elementsList.addAll(this.elements);

        ResourceLocation parent = this.parent;
        while (parent != null && parent.getPath() != null && !parent.getPath().isEmpty()) {
            var child = RPHelper.loadModel(parent.getPath());
            if (child != null) {
                if (child.elements != null) elementsList.addAll(child.elements);
                parent = child.parent;
            } else {
                break;
            }
        }
        return elementsList;
    }

    private List<Triangle> generateCubeTriangles(RPElement element) {
        List<Triangle> triangles = new ObjectArrayList<>();

        float maxX = element.to.x;
        float maxY = element.to.y;
        float maxZ = element.to.z;
        float minX = element.from.x;
        float minY = element.from.y;
        float minZ = element.from.z;

        // Bottom face
        triangles.add(new Triangle(
                new Vector3f(maxX, minY, maxZ),
                new Vector3f(minX, minY, minZ),
                new Vector3f(maxX, minY, minZ),
                new Vector2f(1,1),
                new Vector2f(0,0),
                new Vector2f(1,0), Direction.DOWN, -1419412));

        triangles.add(new Triangle(
                new Vector3f(minX, minY, minZ),
                new Vector3f(maxX, minY, maxZ),
                new Vector3f(minX, minY, maxZ),
                new Vector2f(0,0),
                new Vector2f(1,1),
                new Vector2f(0,1), Direction.DOWN, -2419412));

        // Top face
        triangles.add(new Triangle(
                new Vector3f(maxX, maxY, minZ),
                new Vector3f(minX, maxY, minZ),
                new Vector3f(maxX, maxY, maxZ),
                new Vector2f(1,1),
                new Vector2f(0,0),
                new Vector2f(1,0), Direction.UP, 6315465));

        triangles.add(new Triangle(
                new Vector3f(minX, maxY, maxZ),
                new Vector3f(maxX, maxY, maxZ),
                new Vector3f(minX, maxY, minZ),
                new Vector2f(0,0),
                new Vector2f(1,1),
                new Vector2f(0,1), Direction.UP, 5315465));

        // Front face
        triangles.add(new Triangle(
                new Vector3f(minX, minY, minZ),
                new Vector3f(minX, maxY, minZ),
                new Vector3f(maxX, maxY, minZ),
                new Vector2f(0,0),
                new Vector2f(0,1),
                new Vector2f(1,1), Direction.NORTH, -32522));

        triangles.add(new Triangle(
                new Vector3f(minX, minY, minZ),
                new Vector3f(maxX, maxY, minZ),
                new Vector3f(maxX, minY, minZ),
                new Vector2f(0,0),
                new Vector2f(1,1),
                new Vector2f(1,0), Direction.NORTH, 432522));

        // Back face
        triangles.add(new Triangle(
                new Vector3f(maxX, minY, maxZ),
                new Vector3f(maxX, maxY, maxZ),
                new Vector3f(minX, maxY, maxZ),
                new Vector2f(1,0),
                new Vector2f(1,1),
                new Vector2f(0,1), Direction.SOUTH, 832453));

        triangles.add(new Triangle(
                new Vector3f(maxX, minY, maxZ),
                new Vector3f(minX, maxY, maxZ),
                new Vector3f(minX, minY, maxZ),
                new Vector2f(1,0),
                new Vector2f(0,1),
                new Vector2f(0,0), Direction.SOUTH, -832453));

        // Left face
        triangles.add(new Triangle(
                new Vector3f(minX, minY, minZ),
                new Vector3f(minX, minY, maxZ),
                new Vector3f(minX, maxY, maxZ),
                new Vector2f(0,0),
                new Vector2f(0,1),
                new Vector2f(1,1), Direction.EAST, -52151));

        triangles.add(new Triangle(
                new Vector3f(minX, minY, minZ),
                new Vector3f(minX, maxY, maxZ),
                new Vector3f(minX, maxY, minZ),
                new Vector2f(0,0),
                new Vector2f(1,1),
                new Vector2f(1,0), Direction.EAST, 252151));

        // Right face
        triangles.add(new Triangle(
                new Vector3f(maxX, minY, minZ),
                new Vector3f(maxX, maxY, minZ),
                new Vector3f(maxX, maxY, maxZ),
                new Vector2f(0,0),
                new Vector2f(1,0),
                new Vector2f(1,1), Direction.WEST, 41245));

        triangles.add(new Triangle(
                new Vector3f(maxX, minY, minZ),
                new Vector3f(maxX, maxY, maxZ),
                new Vector3f(maxX, minY, maxZ),
                new Vector2f(0,0),
                new Vector2f(1,1),
                new Vector2f(0,1), Direction.WEST, 141245));

        triangles.forEach(triangle -> triangle.element = element);

        return triangles;
    }

    public int intersect(Vector3f origin, Vector3f direction, Vector3f offset) {
        Triangle triangle = null;
        Triangle.TriangleHit hit = null;
        float smallestT = Float.MAX_VALUE;
        for (var tri: modelTriangles) {
            var res = tri.hitAlt(origin.sub(offset, new Vector3f()), direction);
            if (res != null && res.getT() < smallestT) {
                smallestT = res.getT();
                triangle = tri;
                hit = res;
            }
        }

        if (hit != null) {
            Vector2f uv = hit.getUV();
            String face = hit.getDirection().toString().toLowerCase(); // to help determine which texture to use
            RPElement.TextureInfo ti = triangle.element.faces.get(face);

            if (ti == null)
                return DEFAULT_COLOR; // no face, skip

            String texKey = ti.texture.replace("#","");
            //resolve texture key in case of placeholders (starting with #)
            while (textureMap.containsKey(texKey)) {
                texKey = textureMap.get(texKey).getPath();
            }

            byte[] data = RPHelper.loadTexture(texKey);
            if (data != null) {
                BufferedImage img = null;
                try {
                    img = ImageIO.read(new ByteArrayInputStream(data));
                } catch (IOException ex) {
                    ex.printStackTrace();
                    return CanvasColor.ORANGE_NORMAL.getRgbColor();
                }

                boolean debug = false;
                int imgData = debug ? triangle.color : img.getRGB((int) ((img.getWidth()) * uv.x), (int) ((img.getHeight()) * uv.y));
                return imgData;
            }
        }

        return DEFAULT_COLOR;
    }

    public RPModel rotate(RPBlockState.Variant v) {
        // TODO: rotation cube, get new corners and rotate direction
        if (true) return this;
        for (RPElement e: allElements) {
            if (v.x != 0) {
                e.from.rotateX(v.x * Mth.DEG_TO_RAD);
                e.to.rotateX(v.x * Mth.DEG_TO_RAD);
            }
            if (v.y != 0) {
                e.from.rotateY(v.y * Mth.DEG_TO_RAD);
                e.to.rotateY(v.y * Mth.DEG_TO_RAD);
            }
            if (v.z != 0) {
                e.from.rotateZ(v.z * Mth.DEG_TO_RAD);
                e.to.rotateZ(v.z * Mth.DEG_TO_RAD);
            }
        }
        return this;
    }

    public boolean wantsTint() {
        if (allElements != null) for (var e : allElements) {
            for (var f : e.faces.entrySet()) {
                if (f.getValue().tintIndex != -1)
                    return true;
            }
        }
        return false;
    }
}
