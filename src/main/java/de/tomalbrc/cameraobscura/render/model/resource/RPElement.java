package de.tomalbrc.cameraobscura.render.model.resource;

import com.google.gson.annotations.SerializedName;
import org.joml.Vector3f;
import org.joml.Vector4i;

import java.util.Map;

public class RPElement {
    public Vector3f from;
    public Vector3f to;
    public Map<String, TextureInfo> faces;

    public boolean shade = true;

    public static class TextureInfo {
        @SerializedName("tintindex")
        public int tintIndex = -1;
        public String texture;

        public Vector4i uv;
    }
}
