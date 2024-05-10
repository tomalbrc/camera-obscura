package de.tomalbrc.cameraobscura.render.model.resource;

import com.google.gson.annotations.SerializedName;
import net.minecraft.util.Mth;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4i;

import java.util.Map;

public class RPElement {
    public Vector3f from;
    public Vector3f to;

    public Rotation rotation;

    public Map<String, TextureInfo> faces;

    public boolean shade = true;

    public static class TextureInfo {
        @SerializedName("tintindex")
        public int tintIndex = -1;

        public String texture;

        public int rotation;

        public Vector4i uv;
    }

    public static class Rotation {
        public String axis;
        public float angle;
        public Vector3f origin;

        public Quaternionf toQuaternionf() {
            return new AxisAngle4f(
                    this.angle * Mth.DEG_TO_RAD,
                    axis.equals("x") ? 1 : 0,
                    axis.equals("y") ? 1 : 0,
                    axis.equals("z") ? 1 : 0
            ).get(new Quaternionf());
        }

        public Vector3f getOrigin() {
            return new Vector3f(origin).sub(8,8,8).div(16);
        }
    }
}
