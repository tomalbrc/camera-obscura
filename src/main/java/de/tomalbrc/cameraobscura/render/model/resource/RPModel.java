package de.tomalbrc.cameraobscura.render.model.resource;

import com.google.gson.annotations.SerializedName;
import de.tomalbrc.cameraobscura.json.CachedResourceLocationDeserializer;
import de.tomalbrc.cameraobscura.util.RPHelper;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3fc;
import org.spongepowered.include.com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RPModel {
    @SerializedName("texture_size")
    public List<Integer> textureSize = ImmutableList.of(16,16);
    public ResourceLocation parent;
    public Object2ObjectOpenHashMap<String, String> textures;
    public List<RPElement> elements;

    public record View(RPModel model, Vector3fc blockRotation, Vector3fc offset, boolean uvlock) {
        public View(RPModel model, Vector3fc blockRotation, Vector3fc offset) {
            this(model, blockRotation, offset, false);
        }

        public View(RPModel model, Vector3fc blockRotation, boolean uvlock) {
            this(model, blockRotation, Vec3.ZERO.toVector3f(), uvlock);
        }

        public View(RPModel model, Vector3fc blockRotation) {
            this(model, blockRotation, Vec3.ZERO.toVector3f(), false);
        }

        public Map<String, ResourceLocation> collectTextures() {
            Map<String, ResourceLocation> collectedTextures = new Object2ObjectOpenHashMap<>();

            if (this.model.textures != null && !this.model.textures.isEmpty()) {
                for (Map.Entry<String, String> entry : this.model.textures.entrySet()) {
                    collectedTextures.put(entry.getKey(), CachedResourceLocationDeserializer.get(entry.getValue().replace("#", "")));
                }
            }

            ResourceLocation parent = this.model.parent;
            while (parent != null && !parent.getPath().isEmpty()) {
                View child = RPHelper.loadModelView(parent, this.blockRotation, this.uvlock);
                if (child.model != null) {
                    if (child.model.textures != null) child.model.textures.forEach((key,value) -> collectedTextures.putIfAbsent(key, CachedResourceLocationDeserializer.get(value.replace("#",""))));
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
            while (parent != null) {
                RPModel.View child = RPHelper.loadModelView(parent, this.blockRotation, this.uvlock);

                if (child.model != null) {
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            View view = (View) o;
            return uvlock == view.uvlock && model == view.model && Objects.equals(blockRotation, view.blockRotation) && Objects.equals(offset, view.offset);
        }
    }
}
