package de.tomalbrc.cameraobscura.util;

import org.joml.Vector2f;
import org.joml.Vector4i;

public class TextureHelper {
    public static Vector2f remapUV(Vector2f uv, Vector4i quadUV, int textureWidth, int textureHeight) {
        int quadSMin = quadUV.x();
        int quadTMin = quadUV.y();
        int quadSMax = quadUV.z();
        int quadTMax = quadUV.w();

        float oldS = uv.x();
        float oldT = uv.y();

        float newS = (oldS * (quadSMax - quadSMin) + quadSMin) / textureWidth;
        float newT = (oldT * (quadTMax - quadTMin) + quadTMin) / textureHeight;

        return new Vector2f(newS, newT);
    }
}
