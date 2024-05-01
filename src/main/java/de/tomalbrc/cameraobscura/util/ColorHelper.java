package de.tomalbrc.cameraobscura.util;

public class ColorHelper {
    public static int packColor(double[] color) {
        int alpha = (int) Math.max(0, Math.min(255, color[0] * 255));
        int red = (int) Math.max(0, Math.min(255, color[1] * 255));
        int green = (int) Math.max(0, Math.min(255, color[2] * 255));
        int blue = (int) Math.max(0, Math.min(255, color[3] * 255));

        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    public static double[] unpackColor(int color) {
        int alpha = (color >> 24) & 0xFF;
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        return new double[] {alpha / 255.0, red / 255.0, green / 255.0, blue / 255.0};
    }

    public static double[] multiplyColor(double[] color1, double[] color2) {
        return new double[]{
                color1[0]*color2[0],
                color1[1]*color2[1],
                color1[2]*color2[2],
                color1[3]*color2[3]
        };
    }

    public static int alphaComposite(int color1, int color2) {
        return packColor(alphaComposite(unpackColor(color1), unpackColor(color2)));
    }

    public static double[] alphaComposite(double[] color1, double[] color2) {
        double alpha1 = color1[0];
        double alpha2 = color2[0];

        double alphaResult = alpha1 + alpha2 * (1 - alpha1);
        double[] result = new double[4];

        if (alphaResult == 0) {
            // Handle division by zero case
            result[0] = 0;
            result[1] = 0;
            result[2] = 0;
            result[3] = 0;
        } else {
            result[0] = alphaResult;
            result[1] = (color1[1] * alpha1 + color2[1] * alpha2 * (1 - alpha1)) / alphaResult;
            result[2] = (color1[2] * alpha1 + color2[2] * alpha2 * (1 - alpha1)) / alphaResult;
            result[3] = (color1[3] * alpha1 + color2[3] * alpha2 * (1 - alpha1)) / alphaResult;
        }

        return result;
    }

    public static int interpolateColors(int[] SKY_COLORS, float fraction) {
        if (fraction <= 0)
            return SKY_COLORS[0];
        else if (fraction >= 1)
            return SKY_COLORS[SKY_COLORS.length - 1];

        float segmentSize = 1.0f / (SKY_COLORS.length - 1);
        float segment = fraction / segmentSize;
        int index1 = (int) Math.floor(segment);
        int index2 = Math.min(index1 + 1, SKY_COLORS.length - 1);
        float segmentFraction = segment - index1;

        int color1 = SKY_COLORS[index1];
        int color2 = SKY_COLORS[index2];

        double[] color1Array = ColorHelper.unpackColor(color1);
        double[] color2Array = ColorHelper.unpackColor(color2);

        double[] interpolatedColorArray = new double[4];
        for (int i = 0; i < 4; i++) {
            interpolatedColorArray[i] = interpolateComponent(color1Array[i], color2Array[i], segmentFraction);
        }

        return ColorHelper.packColor(interpolatedColorArray);
    }

    private static double interpolateComponent(double start, double end, float fraction) {
        return start + (end - start) * fraction;
    }
}
