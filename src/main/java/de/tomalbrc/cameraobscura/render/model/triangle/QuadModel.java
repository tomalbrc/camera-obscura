package de.tomalbrc.cameraobscura.render.model.triangle;

import com.mojang.logging.LogUtils;
import de.tomalbrc.cameraobscura.render.model.RenderModel;
import de.tomalbrc.cameraobscura.render.model.resource.RPElement;
import de.tomalbrc.cameraobscura.render.model.resource.RPModel;
import de.tomalbrc.cameraobscura.util.RPHelper;
import de.tomalbrc.cameraobscura.util.TextureHelper;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

public class QuadModel implements RenderModel {
    private final List<Quad> quads = new ObjectArrayList<>();
    private final Map<String, ResourceLocation> textureMap = new Object2ObjectOpenHashMap<>();

    public QuadModel(RPModel.View view) {
        this.readModel(view);
    }

    private void readModel(RPModel.View modelView) {
        var elementList = modelView.collectElements();
        for (RPElement element : elementList) {
            Vector3f from = new Vector3f(element.from);
            Vector3f to = new Vector3f(element.to);

            Vector3f posOffset = new Vector3f(0.5f);
            from.div(16).sub(posOffset);
            to.div(16).sub(posOffset);

            List<Quad> elementQuads = generateCubeQuads(from, to, element, new Vector3f(modelView.blockRotation()), modelView.uvlock());
            for (Quad q : elementQuads) {
                q.shade = element.shade;
                q.light = element.light;
            }
            this.quads.addAll(elementQuads);
        }
        this.textureMap.putAll(modelView.collectTextures());
    }

    private void applyElementAndBlockRotation(Vector3f v, RPElement element, Vector3f blockRotationDeg) {
        if (element.rotation != null) {
            Quaternionf elemQ = element.rotation.toQuaternionf();
            Vector3f rotOrigin = element.rotation.getOrigin();
            v.sub(rotOrigin).rotate(elemQ).add(rotOrigin);
        }
        if (blockRotationDeg != null) {
            Vector3f rot = new Vector3f(blockRotationDeg).mul(Mth.DEG_TO_RAD);
            Quaternionf blockQ = new Quaternionf().rotateY(-rot.y()).rotateX(-rot.x()).rotateZ(-rot.z());
            v.rotate(blockQ);
        }
    }

    private List<Quad> generateCubeQuads(Vector3f from, Vector3f to, RPElement element, Vector3f blockRotationDeg, boolean uvlock) {
        List<Quad> out = new ObjectArrayList<>();

        float minX = from.x, minY = from.y, minZ = from.z;
        float maxX = to.x, maxY = to.y, maxZ = to.z;

        java.util.function.BiConsumer<Vector3f[], String> buildFace = (corners, faceName) -> {
            Vector3f[] verts = new Vector3f[]{new Vector3f(corners[0]), new Vector3f(corners[1]), new Vector3f(corners[2]), new Vector3f(corners[3])};
            for (Vector3f v : verts) applyElementAndBlockRotation(v, element, blockRotationDeg);

            RPElement.TextureInfo texInfo = element.faces.get(faceName);

            Quad q = new Quad(verts[0], verts[1], verts[3]);
            q.textureInfo = texInfo;
            out.add(q);
        };

        // DOWN
        if (element.faces.get("down") != null) buildFace.accept(new Vector3f[]{
                new Vector3f(minX, minY, minZ), new Vector3f(maxX, minY, minZ),
                new Vector3f(maxX, minY, maxZ), new Vector3f(minX, minY, maxZ)}, "down");

        // UP
        if (element.faces.get("up") != null) buildFace.accept(new Vector3f[]{
                new Vector3f(minX, maxY, minZ), new Vector3f(maxX, maxY, minZ),
                new Vector3f(maxX, maxY, maxZ), new Vector3f(minX, maxY, maxZ)}, "up");

        // NORTH
        if (element.faces.get("north") != null) buildFace.accept(new Vector3f[]{
                new Vector3f(minX, minY, minZ), new Vector3f(maxX, minY, minZ),
                new Vector3f(maxX, maxY, minZ), new Vector3f(minX, maxY, minZ)}, "north");

        // SOUTH
        if (element.faces.get("south") != null) buildFace.accept(new Vector3f[]{
                new Vector3f(maxX, minY, maxZ), new Vector3f(minX, minY, maxZ),
                new Vector3f(minX, maxY, maxZ), new Vector3f(maxX, maxY, maxZ)}, "south");

        // WEST
        if (element.faces.get("west") != null) buildFace.accept(new Vector3f[]{
                new Vector3f(minX, minY, maxZ), new Vector3f(minX, minY, minZ),
                new Vector3f(minX, maxY, minZ), new Vector3f(minX, maxY, maxZ)}, "west");

        // EAST
        if (element.faces.get("east") != null) buildFace.accept(new Vector3f[]{
                new Vector3f(maxX, minY, minZ), new Vector3f(maxX, minY, maxZ),
                new Vector3f(maxX, maxY, maxZ), new Vector3f(maxX, maxY, minZ)}, "east");

        return out;
    }

    public List<ModelHit> intersect(Vector3f origin, Vector3f direction, Vector3f offset, int textureTint) {
        List<Quad.QuadHit> hitList = new ObjectArrayList<>();
        List<ModelHit> modelHitList = new ObjectArrayList<>();
        Vector3f rayOrigin = new Vector3f(origin).sub(offset);

        for (int i = 0; i < this.quads.size(); i++) {
            var res = this.quads.get(i).rayIntersect(rayOrigin, direction);
            if (res != null) hitList.add(res);
        }

        Vector2f uv = new Vector2f();
        for (int i = 0; i < hitList.size(); i++) {
            Quad.QuadHit hit = hitList.get(i);
            Quad quad = hit.quad();
            RPElement.TextureInfo texInfo = quad.textureInfo;
            if (texInfo == null) continue;
            uv.set(1-hit.u(), 1-hit.v());

            String texKey = texInfo.texture.charAt(0) == '#' ? texInfo.texture.substring(1) : texInfo.texture;
            ResourceLocation r = this.textureMap.get(texKey);
            while (this.textureMap.containsKey(texKey)) {
                r = this.textureMap.get(texKey);
                texKey = this.textureMap.get(texKey).getPath();
            }

            BufferedImage img;
            try { img = RPHelper.loadTextureImage(r); } catch (Exception e) { LogUtils.getLogger().error("Could not load {}", r); continue; }
            if (img == null) continue;

            int width = img.getWidth();
            int realHeight = (int)(img.getHeight() / (img.getHeight() / (float) img.getWidth()));
            if (texInfo.uv != null) uv = TextureHelper.remapUV(uv, texInfo.uv, width, realHeight);

            int s = (int)(width * uv.x());
            int t = (int)(realHeight * uv.y());
            int imgData = img.getRGB(Mth.clamp(s, 0, img.getWidth() - 1), Mth.clamp(t, 0, img.getHeight() - 1));

            if (texInfo.tintIndex != -1 && textureTint != -1)
                imgData = ARGB.multiply(imgData, textureTint);

            modelHitList.add(new ModelHit(imgData, quad.getDirection(), quad.shade, quad.light, hit.t()));
        }
        return modelHitList;
    }
}
