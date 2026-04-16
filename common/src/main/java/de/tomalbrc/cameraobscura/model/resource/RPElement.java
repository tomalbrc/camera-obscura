package de.tomalbrc.cameraobscura.model.resource;

import com.google.gson.annotations.SerializedName;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Map;

public class RPElement {
    @SerializedName("from")
    public Vector3f from;
    @SerializedName("to")
    public Vector3f to;
    @SerializedName("rotation")
    public Rotation rotation;

    @SerializedName("faces")
    public Map<Direction, TextureInfo> faces;

    @SerializedName("name")
    public String name;

    @SerializedName("shade")
    public boolean shade = true;

    transient public boolean light = true;

    public static class TextureInfo {
        @SerializedName("tintindex")
        public int tintIndex = -1;

        @SerializedName("texture")
        public String texture;

        @SerializedName("cullface")
        public Direction cullface;

        @SerializedName("rotation")
        public int rotation;

        @SerializedName("uv")
        public Vector4f uv;

        public TextureInfo(String texture, Vector4f uv, int rotation) {
            this.texture = texture;
            this.uv = uv;
            this.rotation = rotation;
        }

        public TextureInfo(String texture, Vector4f uv, int rotation, int tintIndex) {
            this.texture = texture;
            this.uv = uv;
            this.rotation = rotation;
            this.tintIndex = tintIndex;
        }

        public TextureInfo() {

        }
    }

    public static class Rotation {
        @SerializedName("axis")
        public String axis;
        @SerializedName("angle")
        public float angle;

        @SerializedName("origin")
        public Vector3f origin;

        @SerializedName("rescale")
        public boolean rescale;

        @SerializedName("x")
        float x;
        @SerializedName("y")
        float y;
        @SerializedName("z")
        float z;

        public Quaternionf toQuaternionf(Quaternionf dest) {
            if (axis != null) return new AxisAngle4f(
                    this.angle * Mth.DEG_TO_RAD,
                    axis.equals("x") ? 1 : 0,
                    axis.equals("y") ? 1 : 0,
                    axis.equals("z") ? 1 : 0
            ).get(dest);

            return dest.rotateXYZ((float) Math.toRadians(x + 180), (float) Math.toRadians(y + 180), (float) Math.toRadians(z + 180));
        }

        public Vector3f getOrigin(Vector3f dest) {
            return dest.set(origin).div(16f);
        }
    }
}
