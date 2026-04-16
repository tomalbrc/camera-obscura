package de.tomalbrc.cameraobscura.sore.rasterizer;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

public class Framebuffer {
    public final int width, height;
    private final int[] colorBuffer;
    private final double[] depthBuffer;

    public Framebuffer(int width, int height) {
        this.width = width;
        this.height = height;
        this.colorBuffer = new int[width * height];
        this.depthBuffer = new double[width * height];
    }

    public int[] getColorBuffer() {
        return colorBuffer;
    }

    public double[] getDepthBuffer() {
        return depthBuffer;
    }

    public void clearColor(int color) {
        Arrays.fill(colorBuffer, color);
    }

    public void clearDepth(double value) {
        Arrays.fill(depthBuffer, value);
    }

    public void setPixel(int x, int y, int color) {
        colorBuffer[y * width + x] = color;
    }

    public int getPixel(int x, int y) {
        return colorBuffer[y * width + x];
    }

    public boolean depthTest(int x, int y, double depth) {
        int idx = y * width + x;
        if (depth < depthBuffer[idx]) {
            depthBuffer[idx] = depth;
            return true;
        }
        return false;
    }

    public BufferedImage toImage(int scale) {
        int targetWidth = width / scale;
        int targetHeight = height / scale;
        BufferedImage img = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        int sampleCount = scale * scale;
        int[] destPixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();

        int[] srcYStarts = new int[targetHeight];
        for (int y = 0; y < targetHeight; y++) {
            srcYStarts[y] = (targetHeight - 1 - y) * scale * width;
        }

        for (int y = 0; y < targetHeight; y++) {
            int srcYBase = srcYStarts[y];
            int destIndex = y * targetWidth;

            for (int x = 0; x < targetWidth; x++) {
                int r = 0, g = 0, b = 0;
                int srcXBase = x * scale;

                for (int sy = 0; sy < scale; sy++) {
                    int rowOffset = srcYBase + sy * width;
                    for (int sx = 0; sx < scale; sx++) {
                        int pixel = colorBuffer[rowOffset + srcXBase + sx];
                        r += (pixel >> 16) & 0xFF;
                        g += (pixel >> 8) & 0xFF;
                        b += pixel & 0xFF;
                    }
                }

                destPixels[destIndex++] = ((r / sampleCount) << 16) |
                        ((g / sampleCount) << 8) |
                        (b / sampleCount);
            }
        }
        return img;
    }
}