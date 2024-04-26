package de.tomalbrc.cameraobscura;

public class ColorHelper {
    public static int packColor(double[] color) {
        // Check for valid color channel value range (0.0 to 1.0)
        if (color.length != 3 || color[0] < 0.0 || color[0] > 1.0 || color[1] < 0.0 || color[1] > 1.0 || color[2] < 0.0 || color[2] > 1.0) {
            throw new IllegalArgumentException("Color channel values must be between 0.0 and 1.0");
        }

        // Clamp values to ensure they are within 0-255 range (adjust bits for different color depths)
        int red = (int) Math.max(0, Math.min(255, color[0] * 255));
        int green = (int) Math.max(0, Math.min(255, color[1] * 255));
        int blue = (int) Math.max(0, Math.min(255, color[2] * 255));

        // Pack the color channels into a single integer (ARGB) using bit shifting
        return (red << 16) | (green << 8) | blue;
    }

    public static double[] unpackColor(int color) {
        // Extract individual color channels from the integer using bit masking and shifting
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        // Normalize color channels to range between 0.0 and 1.0 (adjust scaling for different color depths)
        return new double[] {red / 255.0, green / 255.0, blue / 255.0};
    }

    public static double[] multiplyColor(double[] color1, double[] color2) {
        return new double[]{
                color1[0]*color2[0],
                color1[1]*color2[1],
                color1[2]*color2[2]
        };
    }

    public static int multiplyColor(int color1, int color2) {
        if (color1 == -1) {
            return color2;
        } else if (color2 == -1) {
            return color1;
        }

        var c1 = unpackColor(color1);
        var c2 = unpackColor(color2);
        return packColor(multiplyColor(c1, c2));
    }
}
