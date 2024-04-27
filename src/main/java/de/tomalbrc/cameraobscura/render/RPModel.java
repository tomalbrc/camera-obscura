package de.tomalbrc.cameraobscura.render;

import de.tomalbrc.cameraobscura.util.RPHelper;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
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

    public Map<String, ResourceLocation> collectTextures() {
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

    public List<RPElement> collectElements() {
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

        // Define vertices for each face (assuming clockwise winding order)
        Vector3f v1 = new Vector3f(min.x, min.y, min.z);
        Vector3f v2 = new Vector3f(max.x, min.y, min.z);
        Vector3f v3 = new Vector3f(max.x, max.y, min.z);
        Vector3f v4 = new Vector3f(min.x, max.y, min.z);
        Vector3f v5 = new Vector3f(min.x, min.y, max.z);
        Vector3f v6 = new Vector3f(max.x, min.y, max.z);
        Vector3f v7 = new Vector3f(max.x, max.y, max.z);
        Vector3f v8 = new Vector3f(min.x, max.y, max.z);

        // Front face
        triangles.add(new Triangle(v4, v3, v2, Direction.NORTH));
        triangles.add(new Triangle(v4, v2, v1, Direction.NORTH));

        // Back face
        triangles.add(new Triangle(v8, v7, v6, Direction.SOUTH));
        triangles.add(new Triangle(v8, v6, v5, Direction.SOUTH));

        // Right face
        triangles.add(new Triangle(v7, v3, v2, Direction.WEST));
        triangles.add(new Triangle(v7, v2, v6, Direction.WEST));

        // Left face
        triangles.add(new Triangle(v5, v1, v4, Direction.EAST));
        triangles.add(new Triangle(v5, v4, v8, Direction.EAST));

        // Top face
        triangles.add(new Triangle(v4, v8, v7, Direction.UP));
        triangles.add(new Triangle(v4, v7, v3, Direction.UP));

        // Bottom face
        triangles.add(new Triangle(v1, v5, v6, Direction.DOWN));
        triangles.add(new Triangle(v1, v6, v2, Direction.DOWN));

        return triangles;
    }

    public int intersect(Vector3f origin, Vector3f direction, Vector3f offset) {
        var allTextures = collectTextures();
        var e = collectElements();
        for (var element: e) {
            var tris = generateCubeTriangles(element.from, element.to);
            for (var tri: tris) {
                var res = tri.rayIntersect(origin.sub(offset, new Vector3f()), new Vector3f(direction));
                if (res != null) {
                    var uv = res.getUV();
                    var face = res.getDirection().toString().toLowerCase();

                    RPElement.TextureInfo ti = element.faces.get(face);
if (ti == null)
    System.out.println("FOOKIN FACE: "+face);

                    var texKey = ti.texture.replace("#","");

                    //resolve texture key in case of placeholders (starting with #)
                    while (allTextures.containsKey(texKey)) {
                        texKey = allTextures.get(texKey).getPath();
                    }

                    byte[] data = RPHelper.loadTexture(texKey);
                    if (data != null) {
                        BufferedImage img = null;
                        try {
                            img = ImageIO.read(new ByteArrayInputStream(data));
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            return -1;
                        }

                        int imgData = img.getRGB((int) (img.getWidth() * uv.x), (int) (img.getHeight() * uv.y));
                        return imgData;
                    }
                }
            }
        }

        return -1;
    }
}
