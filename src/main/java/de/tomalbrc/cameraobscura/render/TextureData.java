package de.tomalbrc.cameraobscura.render;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

public class TextureData {
    private final int width;
    private final int height;
    private final int[] pixels;

    public TextureData(int width, int height) {
        this.width = width;
        this.height = height;
        this.pixels = new int[width * height];
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public int getPixel(int x, int y) { return pixels[y * width + x]; }

    public static TextureData fromBytes(byte[] data) throws Exception {
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(data));
        TextureData buffer = new TextureData(img.getWidth(), img.getHeight());

        if (img.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            for (int y = 0; y < img.getHeight(); y++) {
                for (int x = 0; x < img.getWidth(); x++) {
                    int gray = img.getRaster().getSample(x, y, 0);
                    int argb = 0xFF000000 | (gray << 16) | (gray << 8) | gray;
                    buffer.pixels[y * img.getWidth() + x] = argb;
                }
            }
        } else {
            img.getRGB(0, 0, img.getWidth(), img.getHeight(), buffer.pixels, 0, img.getWidth());
        }

        return buffer;
    }
}