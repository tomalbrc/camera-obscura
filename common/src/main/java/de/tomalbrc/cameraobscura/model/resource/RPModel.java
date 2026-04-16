package de.tomalbrc.cameraobscura.model.resource;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.tomalbrc.cameraobscura.json.CachedIdentifierDeserializer;
import de.tomalbrc.cameraobscura.util.Constants;
import de.tomalbrc.cameraobscura.util.resource.RPHelper;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Matrix4d;
import org.joml.Matrix4dc;
import org.joml.Quaternionf;
import org.joml.Vector3fc;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RPModel {
    @SerializedName("ambientocclusion")
    public Boolean ambientOcclusion;
    @SerializedName("parent")
    public Identifier parent;
    @SerializedName("textures")
    public Map<String, TextureEntry> textures;
    @SerializedName("elements")
    public List<RPElement> elements;
    @SerializedName("display")
    public Map<ItemDisplayContext, ItemTransform> display = Map.of();

    public Matrix4dc display(ItemDisplayContext displayContext, Matrix4d transform) {
        if (displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND)
            displayContext = ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;

        if (displayContext == ItemDisplayContext.NONE) {
            return transform.translate(-0.5f, -0.5f, -0.5f);
        }

        if (this.display.containsKey(displayContext)) {
            return this.display.get(displayContext).apply(displayContext, transform);
        }

        Identifier parent = this.parent;
        while (parent != null && !parent.getPath().isEmpty()) {
            var child = RPHelper.loadModel(parent);
            if (child != null) {
                if (child.display.containsKey(displayContext))
                    return child.display.get(displayContext).apply(displayContext, transform);

                parent = child.parent;
            } else {
                break;
            }
        }

        return null;
    }

    public Map<String, Identifier> collectTextures() {
        Map<String, Identifier> collectedTextures = new Object2ObjectOpenHashMap<>();

        if (this.textures != null && !this.textures.isEmpty()) {
            for (Map.Entry<String, TextureEntry> entry : this.textures.entrySet()) {
                collectedTextures.put(entry.getKey(), CachedIdentifierDeserializer.get(entry.getValue().sprite().replace("#", "")));
            }
        }

        Identifier parent = this.parent;
        while (parent != null && !parent.getPath().isEmpty()) {
            var child = RPHelper.loadModel(parent);
            if (child != null) {
                if (child.textures != null)
                    child.textures.forEach((key, value) -> collectedTextures.putIfAbsent(key, CachedIdentifierDeserializer.get(value.sprite().replace("#", ""))));
                parent = child.parent;
            } else {
                break;
            }
        }

        return collectedTextures;
    }

    public boolean ambientOcclusion() {
        if (this.ambientOcclusion != null) {
            return this.ambientOcclusion;
        }

        Identifier parent = this.parent;
        while (parent != null) {
            var child = RPHelper.loadModel(parent);

            if (child != null) {
                if (child.ambientOcclusion != null) {
                    return child.ambientOcclusion;
                }
                parent = child.parent;
            } else {
                break;
            }
        }

        return true;
    }

    public List<RPElement> collectElements() {
        if (this.elements != null) {
            return this.elements;
        }

        Identifier parent = this.parent;
        while (parent != null) {
            var child = RPHelper.loadModel(parent);

            if (child != null) {
                if (child.elements != null) {
                    return child.elements;
                }
                parent = child.parent;
            } else {
                break;
            }
        }
        return new ObjectArrayList<>();
    }

    public record ItemTransform(
            @SerializedName("translation")
            Vector3fc translation,
            @SerializedName("rotation")
            Vector3fc rotation,
            @SerializedName("scale")
            Vector3fc scale
    ) {
        public static final Codec<ItemTransform> CODEC = RecordCodecBuilder.create((i) -> i.group(ExtraCodecs.VECTOR3F.optionalFieldOf("translation").forGetter(matrix -> Optional.of(matrix.translation())), ExtraCodecs.VECTOR3F.optionalFieldOf("rotation").forGetter(matrix -> Optional.of(matrix.rotation())), ExtraCodecs.VECTOR3F.optionalFieldOf("scale").forGetter(matrix -> Optional.of(matrix.scale()))).apply(i, (tr, rot, sc) -> new ItemTransform(tr.orElse(Constants.ZERO_VEC3), rot.orElse(Constants.ZERO_VEC3), sc.orElse(Constants.UNIT_VEC3))));

        public Matrix4d apply(ItemDisplayContext displayContext, Matrix4d transform) {
            float translationX;
            float rotY;
            float rotZ;
            if (displayContext.leftHand()) {
                translationX = -this.translation.x();
                rotY = -this.rotation.y();
                rotZ = -this.rotation.z();
            } else {
                translationX = this.translation.x();
                rotY = this.rotation.y();
                rotZ = this.rotation.z();
            }

            transform.translate(translationX / 16f, this.translation.y() / 16f, this.translation.z() / 16f);
            transform.rotate((new Quaternionf()).rotationXYZ(this.rotation.x() * Mth.DEG_TO_RAD, rotY * Mth.DEG_TO_RAD, rotZ * Mth.DEG_TO_RAD));
            transform.scale(this.scale.x(), this.scale.y(), this.scale.z());
            transform.translate(-0.5F, -0.5F, -0.5F);
            return transform;
        }
    }

    public record TextureEntry(
            @SerializedName("sprite")
            String sprite,
            @SerializedName("force_translucent")
            boolean forceTranslucent
    ) {
        public static TextureEntry of(String sprite) {
            return new TextureEntry(sprite, false);
        }

        public static class Deserializer implements JsonDeserializer<TextureEntry> {
            @Override
            public TextureEntry deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                if (json.isJsonPrimitive()) {
                    return new TextureEntry(json.getAsString(), false);
                }

                JsonObject obj = json.getAsJsonObject();
                return new TextureEntry(obj.get("sprite").getAsString(), obj.has("force_translucent") && obj.get("force_translucent").getAsBoolean());
            }
        }
    }

    public record View(RPModel model, Vector3fc blockRotation, Vector3fc offset, Vector3fc scale, boolean uvlock) {
        public View(RPModel model, Vector3fc blockRotation, Vector3fc offset) {
            this(model, blockRotation, offset, Constants.ZERO_VEC3, false);
        }

        public View(RPModel model, Vector3fc blockRotation, boolean uvlock) {
            this(model, blockRotation, Constants.ZERO_VEC3, Constants.ZERO_VEC3, uvlock);
        }

        public View(RPModel model, Vector3fc blockRotation) {
            this(model, blockRotation, Constants.ZERO_VEC3, Constants.ZERO_VEC3, false);
        }

        public View(RPModel model) {
            this(model, Constants.ZERO_VEC3, Constants.ZERO_VEC3, Constants.ZERO_VEC3, false);
        }
    }
}
