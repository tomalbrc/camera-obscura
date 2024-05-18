package de.tomalbrc.cameraobscura.util;

import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector4fc;

import java.awt.image.BufferedImage;

public class TextureHelper {
    public static Vector2f remapUV(Vector2fc uv, Vector4fc quadUV, int textureWidth, int textureHeight) {
        float quadSMin = quadUV.x();
        float quadTMin = quadUV.y();
        float quadSMax = quadUV.z();
        float quadTMax = quadUV.w();

        float oldS = uv.x();
        float oldT = uv.y();

        float newS = (oldS * (quadSMax - quadSMin) + quadSMin) / (float)textureWidth;
        float newT = (oldT * (quadTMax - quadTMin) + quadTMin) / (float)textureHeight;

        return new Vector2f(newS, newT);
    }

    public static BufferedImage darkenGrayscale(BufferedImage image) {
        BufferedImage darkenedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

        // gamma correction to darken the grayscale values
        float gamma = 2.0f;
        float[] gammaCorrection = new float[256];
        for (int i = 0; i < 256; i++) {
            gammaCorrection[i] = (float) Math.pow(i / 255.0, gamma) * 255;
        }

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixelValue = image.getRGB(x, y) & 0xFF; // Grayscale value
                int darkenedValue = (int) gammaCorrection[pixelValue];
                darkenedImage.setRGB(x, y, darkenedValue << 16 | darkenedValue << 8 | darkenedValue);
            }
        }

        return darkenedImage;
    }
}
