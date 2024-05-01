package de.tomalbrc.cameraobscura.util;

import org.joml.Vector2f;
import org.joml.Vector4i;

public class TextureHelper {
    public static Vector2f remapUV(Vector2f uv, Vector4i quadUV, int textureWidth, int textureHeight) {
        int quadSMin = quadUV.x / textureWidth;
        int quadTMin = quadUV.y / textureWidth;
        int quadSMax = quadUV.z / textureWidth;
        int quadTMax = quadUV.w / textureHeight;

        float newS = mapValue(uv.x, 0, 1, quadSMin, quadSMax);
        float newT = mapValue(uv.y, 0, 1, quadTMin, quadTMax);

        return new Vector2f(newS, newT);
    }

    // Linear mapping function
    private static float mapValue(float value, float oldMin, float oldMax, float newMin, float newMax) {
        return (value - oldMin) * (newMax - newMin) / (oldMax - oldMin) + newMin;
    }
}
