package de.tomalbrc.cameraobscura.render.model.resource;

import de.tomalbrc.cameraobscura.util.RPHelper;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;

public class RPModel {
    private ResourceLocation parent;
    private Map<String, String> textures;
    private List<RPElement> elements;
    public transient Vector3f blockRotation;

    public Map<String, ResourceLocation> collectTextures() {
        Map<String, ResourceLocation> collectedTextures = new Object2ObjectOpenHashMap<>();
        for (Map.Entry<String, String> entry : this.textures.entrySet()) {
            collectedTextures.put(entry.getKey(), new ResourceLocation(entry.getValue().replace("#", "")));
        }

        ResourceLocation parent = this.parent;
        while (parent != null && parent.getPath() != null && !parent.getPath().isEmpty()) {
            var child = RPHelper.loadModel(parent.getPath(), this.blockRotation);
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
            var child = RPHelper.loadModel(parent.getPath(), this.blockRotation);
            if (child != null) {
                if (child.elements != null) elementsList.addAll(child.elements);
                parent = child.parent;
            } else {
                break;
            }
        }
        return elementsList;
    }
}
