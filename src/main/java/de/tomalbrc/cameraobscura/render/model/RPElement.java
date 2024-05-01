package de.tomalbrc.cameraobscura.render.model;

import com.google.gson.annotations.SerializedName;
import org.joml.Vector3f;
import org.joml.Vector4i;

import java.awt.image.BufferedImage;
import java.util.Map;

public class RPElement {
    Vector3f from;
    Vector3f to;
    Map<String, TextureInfo> faces;

    public static class TextureInfo {
        @SerializedName("tintindex")
        int tintIndex = -1;
        String texture;

        Vector4i uv;

        transient BufferedImage bufferedImage;
    }
}
