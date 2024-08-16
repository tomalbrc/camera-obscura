package de.tomalbrc.cameraobscura.render.model.resource;

import com.google.gson.annotations.SerializedName;
import de.tomalbrc.cameraobscura.util.RPHelper;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;

public class RPModel {
    @SerializedName("texture_size")
    public List<Integer> textureSize;
    public ResourceLocation parent;
    public Map<String, String> textures;
    public List<RPElement> elements;

    public record View(RPModel model, Vector3f blockRotation, Vector3f offset, boolean uvlock) {
        public View(RPModel model, Vector3f blockRotation, Vector3f offset) {
            this(model, blockRotation, offset, false);
        }

        public View(RPModel model, Vector3f blockRotation, boolean uvlock) {
            this(model, blockRotation, Vec3.ZERO.toVector3f(), uvlock);
        }

        public View(RPModel model, Vector3f blockRotation) {
            this(model, blockRotation, Vec3.ZERO.toVector3f(), false);
        }

        public Map<String, ResourceLocation> collectTextures() {
            Map<String, ResourceLocation> collectedTextures = new Object2ObjectOpenHashMap<>();

            if (this.model.textures != null && !this.model.textures.isEmpty()) {
                for (Map.Entry<String, String> entry : this.model.textures.entrySet()) {
                    collectedTextures.put(entry.getKey(), ResourceLocation.parse(entry.getValue().replace("#", "")));
                }

                ResourceLocation parent = this.model.parent;
                while (parent != null && !parent.getPath().isEmpty()) {
                    var child = RPHelper.loadModel(parent.getNamespace(), parent.getPath(), this.blockRotation, this.uvlock);
                    if (child != null) {
                        if (child.model.textures != null) child.model.textures.forEach((key,value) -> collectedTextures.putIfAbsent(key, ResourceLocation.parse(value.replace("#",""))));
                        parent = child.model.parent;
                    } else {
                        break;
                    }
                }
            }

            return collectedTextures;
        }

        public List<RPElement> collectElements() {
            if (this.model.elements != null) {
                return this.model.elements;
            }

            ResourceLocation parent = this.model.parent;
            while (parent != null && !parent.getPath().isEmpty()) {
                var child = RPHelper.loadModel(parent.getNamespace(), parent.getPath(), this.blockRotation, this.uvlock);
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
    }
}
