package de.tomalbrc.cameraobscura.render;

import org.joml.Vector3f;

import java.util.Map;

public class RPElement {
    Vector3f from;
    Vector3f to;
    Map<String, TextureInfo> faces;

    public static class TextureInfo {
        String texture;
    }
}
