package de.tomalbrc.cameraobscura.render;

import de.tomalbrc.cameraobscura.util.RPHelper;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class RPModel {
    ResourceLocation parent;

    Map<String, String> textures;

    List<RPElement> elements;

    Map<String, ResourceLocation> textureMap;

    List<RPElement> allElements;

    public RPModel prepare() {
        this.textureMap = collectTextures();
        this.allElements = collectElements();
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

    private List<Triangle> generateCubeTriangles(Vector3f min, Vector3f max) {
        List<Triangle> triangles = new ObjectArrayList<>();

        float maxX = max.x;
        float maxY = max.y; // temporary value holder for y
        float maxZ = max.z; // swap y and z
        float minX = min.x;
        float minY = min.y; // swap y and z
        float minZ = min.z;

        // Bottom face
        triangles.add(new Triangle(
                new Vector3f(maxX, minY, maxZ),
                new Vector3f(minX, minY, minZ),
                new Vector3f(maxX, minY, minZ), Direction.DOWN, -1419412));

        triangles.add(new Triangle(
                new Vector3f(minX, minY, minZ),
                new Vector3f(maxX, minY, maxZ),
                new Vector3f(minX, minY, maxZ), Direction.DOWN, -2419412));

        // Top face
        triangles.add(new Triangle(
                new Vector3f(maxX, maxY, maxZ),
                new Vector3f(minX, maxY, minZ),
                new Vector3f(maxX, maxY, minZ), Direction.UP, 6315465));

        triangles.add(new Triangle(
                new Vector3f(minX, maxY, minZ),
                new Vector3f(maxX, maxY, maxZ),
                new Vector3f(minX, maxY, maxZ), Direction.UP, 5315465));

        // Front face
        triangles.add(new Triangle(
                new Vector3f(minX, minY, minZ),
                new Vector3f(minX, maxY, minZ),
                new Vector3f(maxX, maxY, minZ), Direction.NORTH, -32522));

        triangles.add(new Triangle(
                new Vector3f(minX, minY, minZ),
                new Vector3f(maxX, maxY, minZ),
                new Vector3f(maxX, minY, minZ), Direction.NORTH, 432522));

        // Back face
        triangles.add(new Triangle(
                new Vector3f(maxX, minY, maxZ),
                new Vector3f(maxX, maxY, maxZ),
                new Vector3f(minX, maxY, maxZ), Direction.SOUTH, 832453));

        triangles.add(new Triangle(
                new Vector3f(maxX, minY, maxZ),
                new Vector3f(minX, maxY, maxZ),
                new Vector3f(minX, minY, maxZ), Direction.SOUTH, -832453));

        // Left face
        triangles.add(new Triangle(
                new Vector3f(minX, minY, minZ),
                new Vector3f(minX, minY, maxZ),
                new Vector3f(minX, maxY, maxZ), Direction.EAST, -52151));

        triangles.add(new Triangle(
                new Vector3f(minX, minY, minZ),
                new Vector3f(minX, maxY, maxZ),
                new Vector3f(minX, maxY, minZ), Direction.EAST, 252151));

        // Right face
        triangles.add(new Triangle(
                new Vector3f(maxX, minY, minZ),
                new Vector3f(maxX, maxY, minZ),
                new Vector3f(maxX, maxY, maxZ), Direction.WEST, 41245));

        triangles.add(new Triangle(
                new Vector3f(maxX, minY, minZ),
                new Vector3f(maxX, maxY, maxZ),
                new Vector3f(maxX, minY, maxZ), Direction.WEST, 141245));

        return triangles;
    }

    public int intersect(Vector3f origin, Vector3f direction, Vector3f offset) {
        for (var element: allElements) {
            var l = new Vector3f(-0.5f);
            var tris = generateCubeTriangles(element.from.div(16, new Vector3f()).add(l).add(offset), element.to.div(16, new Vector3f()).add(l).add(offset));

            Triangle triangle = null;
            Triangle.TriangleHit hit = null;
            float smallestT = Float.MAX_VALUE;
            for (var tri: tris) {
                var res = tri.rayIntersect(origin, direction);
                if (res != null && res.getT() < smallestT) {
                    smallestT = res.getT();
                    triangle = tri;
                    hit = res;
                }
            }

            if (hit != null) {
                var uv = hit.getUV();
                var face = hit.getDirection().toString().toLowerCase(); // to help determine which texture to use
                RPElement.TextureInfo ti = element.faces.get(face);

                if (ti == null)
                    continue; // no face, skip

                var texKey = ti.texture.replace("#","");

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
                    int imgData = debug ? triangle.color : img.getRGB((int) ((img.getWidth()-1) * uv.x), (int) ((img.getHeight()-1) * uv.y));
                    return imgData;
                }
            }
        }

        return CanvasColor.PURPLE_HIGH.getRgbColor();
    }

    public RPModel rotate(RPBlockState.Variant v) {

        if (true) return this;
        for (var e: allElements) {
            e.from.div(16);
            e.from.sub(new Vector3f(0.5f));
            e.to.div(16);
            e.to.sub(new Vector3f(0.5f));

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

            e.from.add(new Vector3f(-0.5f));
            e.from.mul(16);

            e.to.add(new Vector3f(-0.5f));
            e.to.mul(16);
        }
        return this;
    }
}
