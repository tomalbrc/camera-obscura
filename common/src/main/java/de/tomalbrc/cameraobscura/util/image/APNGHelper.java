package de.tomalbrc.cameraobscura.util.image;


import de.tomalbrc.cameraobscura.apng.APNGCollector;
import de.tomalbrc.cameraobscura.apng.APNGSeparator;
import de.tomalbrc.cameraobscura.apng.Frame;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static de.tomalbrc.cameraobscura.apng.APNGCollector.APNG_BLEND_OP_SOURCE;
import static de.tomalbrc.cameraobscura.apng.APNGCollector.APNG_DISPOSE_OP_NONE;

public class APNGHelper {
    public static byte[] buildAPNG(List<BufferedImage> frames, int frameRate, boolean loop) throws IOException {
        if (frames.isEmpty()) throw new IOException("No frames");

        APNGCollector apng = new APNGCollector(frames.getFirst(), 0);
        short delayNum = 1;
        short delayDen = (short) frameRate;

        for (int i = 1; i < frames.size(); i++) {
            apng.addFrame(frames.get(i), 0, 0, delayNum, delayDen, APNG_DISPOSE_OP_NONE, APNG_BLEND_OP_SOURCE);
        }

        return apng.build();
    }

    public static List<BufferedImage> readAllFrames(byte[] apngData) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(apngData);
        APNGSeparator separator = new APNGSeparator(bais);
        List<BufferedImage> frames = new ArrayList<>();

        for (Frame f : separator.getRawFrames()) {
            frames.add(f.image);
        }

        bais.close();
        return frames;
    }

}