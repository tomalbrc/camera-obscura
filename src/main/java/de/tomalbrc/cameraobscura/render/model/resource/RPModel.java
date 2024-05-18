package de.tomalbrc.cameraobscura.render.model.resource;

import de.tomalbrc.cameraobscura.util.RPHelper;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;

public class RPModel {
    public ResourceLocation parent;
    public Map<String, String> textures;
    public List<RPElement> elements;

    public record View(RPModel model, Vector3f blockRotation, boolean uvlock) {
        public View(RPModel model, Vector3f blockRotation) {
            this(model, blockRotation, false);
        }

        public Map<String, ResourceLocation> collectTextures() {
            Map<String, ResourceLocation> collectedTextures = new Object2ObjectOpenHashMap<>();
            for (Map.Entry<String, String> entry : this.model.textures.entrySet()) {
                collectedTextures.put(entry.getKey(), new ResourceLocation(entry.getValue().replace("#", "")));
            }

            ResourceLocation parent = this.model.parent;
            while (parent != null && parent.getPath() != null && !parent.getPath().isEmpty()) {
                var child = RPHelper.loadModel(parent.getPath(), this.blockRotation, this.uvlock);
                if (child != null) {
                    if (child.model.textures != null) child.model.textures.forEach((key,value) -> {
                        collectedTextures.putIfAbsent(key, new ResourceLocation(value.replace("#","")));
                    });

                    parent = child.model.parent;
                } else {
                    break;
                }
            }
            return collectedTextures;
        }

        public List<RPElement> collectElements() {
            if (this.model.elements != null) {
                return this.model.elements;
            }

            ResourceLocation parent = this.model.parent;
            while (parent != null && parent.getPath() != null && !parent.getPath().isEmpty()) {
                var child = RPHelper.loadModel(parent.getPath(), this.blockRotation, this.uvlock);
                if (child != null) {
                    if (child.model.elements != null) {
                        return child.model.elements;
                    }
                    parent = child.model.parent;
                } else {
                    break;
                }
            }
            return new ObjectArrayList<>();
        }

        public RPModel.View combine(RPModel.View other) {
            RPModel model1 = new RPModel();
            model1.parent = this.model.parent;
            model1.elements = this.collectElements();
            model1.elements.addAll(other.collectElements());
            model1.textures = this.model().textures;
            return new RPModel.View(model1, this.blockRotation, this.uvlock);
        }
    }
}
