package de.tomalbrc.cameraobscura.apng;

import java.awt.image.BufferedImage;

/**
 * Represents image and parameters of APNG frame
 */
public class Frame {
    public BufferedImage image;

    int width,
            height,
            x,
            y;

    byte dispose,
            blend;
}