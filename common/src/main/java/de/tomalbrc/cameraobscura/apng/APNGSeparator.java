package de.tomalbrc.cameraobscura.apng;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import static de.tomalbrc.cameraobscura.apng.Chunk.IEND;
import static de.tomalbrc.cameraobscura.apng.Chunk.SIGNATURE;


/**
 * A class to separate an animated PNG into frames.
 */
public class APNGSeparator {
    static final Color TRANSPARENT = new Color(0, 0, 0, 0);
    private final ArrayList<Frame> frames;

    /**
     * Constructs an instance with the described APNG stream.
     *
     * @param input argument of function {@code separateStream}.
     *              APNG stream.
     * @throws IOException if an error occurs during invoking {@code APNGSeparator#separateStream}.
     * @see APNGSeparator#separateStream(PushbackInputStream)
     */
    public APNGSeparator(InputStream input) throws IOException {
        frames = separateStream(new PushbackInputStream(input, 8));
    }

    /**
     * Merges frames on top of each other depending on dispose_op and blend_op, simulating animation rendering.
     *
     * @param frames       Frames list.
     * @param includeFirst if true the images will be as if the static image was included into the animation
     *                     (does not depend on whether a static image is included in the animation).
     * @return Frames list that are written in APNG as they are (always return static image first).
     * @see BufferedImage
     */
    public static ArrayList<BufferedImage> mergeFrames(ArrayList<Frame> frames, boolean includeFirst) {
        ArrayList<BufferedImage> list = new ArrayList<>();
        list.add(frames.getFirst().image);

        final int width = frames.getFirst().image.getWidth(),
                height = frames.getFirst().image.getHeight(),
                type = BufferedImage.TYPE_INT_ARGB;
        int x, y;
        byte dispose, blend;

        BufferedImage background = new BufferedImage(width, height, type);
        Graphics2D bkg = (Graphics2D) background.getGraphics();
        bkg.setBackground(TRANSPARENT);

        if (!includeFirst)
            frames.removeFirst();

        for (Frame frame : frames) {
            var result = new BufferedImage(width, height, type);

            x = frame.x;
            y = frame.y;
            dispose = frame.dispose;
            blend = frame.blend;

            Graphics2D res = (Graphics2D) result.getGraphics();
            res.setBackground(TRANSPARENT);
            res.drawImage(background, 0, 0, null);
            if (blend == 0)
                res.clearRect(x, y, frame.width, frame.height);
            res.drawImage(frame.image, x, y, frame.width, frame.height, null);

            switch (dispose) {
                case 0, 1 -> {
                    bkg.clearRect(0, 0, background.getWidth(), background.getHeight());
                    bkg.drawImage(result, 0, 0, null);

                    if (dispose == 1)
                        bkg.clearRect(x, y, frame.width, frame.height);
                }
            }
            list.add(result);
        }

        return list;
    }

    /**
     * @return Frames list that are written in APNG as they are.
     * @see Frame
     */
    public ArrayList<Frame> getRawFrames() {
        return frames;
    }

    /**
     * @param includeFirst argument of function {@code mergeFrames}.
     *                     If true the images will be as if the static image was included into the animation
     *                     (does not depend on whether a static image is included in the animation).
     * @return BufferedImage list - frames as if they were displayed in the animation.
     * @see APNGSeparator#mergeFrames(ArrayList, boolean)
     * @see BufferedImage
     */
    public ArrayList<BufferedImage> getFrames(boolean includeFirst) {
        return mergeFrames(frames, includeFirst);
    }

    /**
     * @param stream {@code PushbackInputStream} of APNG stream with a pushback buffer size 8.
     * @return Frames list that are written in APNG as they are.
     * @throws IOException if an error occurs during reading, unreading or skipping bytes in InputStream
     *                     or reading ImageIO image.
     * @see PushbackInputStream
     */
    private ArrayList<Frame> separateStream(PushbackInputStream stream) throws IOException {
        ArrayList<Frame> images = new ArrayList<>();

        if (Arrays.equals(stream.readNBytes(8), SIGNATURE)) {
            byte[] bLength, bName, bHeader = new byte[25];
            int length;
            String name = "";
            var ancillary = new ByteArrayOutputStream();
            Frame frame = new Frame();

            while (!name.equals("IEND")) {
                bLength = stream.readNBytes(4);
                bName = stream.readNBytes(4);
                length = ByteBuffer.wrap(bLength).getInt();
                name = new String(bName);

                switch (name) {
                    case "fcTL" -> {
                        frame = new Frame();
                        stream.skipNBytes(4);
                        byte[] chunk = stream.readNBytes(length - 4);
                        bHeader = Chunk.createIHDR(bHeader, chunk, frame);

                        stream.skipNBytes(4);
                    }
                    case "IHDR" -> {
                        stream.unread(bName);
                        stream.unread(bLength);
                        bHeader = stream.readNBytes(length + 12);
                    }
                    case "cHRM", "cICP", "gAMA", "iCCP",
                         "mDCv", "cLLi", "sBIT",
                         "sRGB", "bKGD", "hIST",
                         "tRNS", "PLTE", "eXIf",
                         "pHYs", "sPLT" -> {
                        stream.unread(bName);
                        stream.unread(bLength);
                        ancillary.write(stream.readNBytes(length + 12));
                    }
                    case "IDAT", "fdAT" -> {
                        var data = new ByteArrayOutputStream();
                        var img = new ByteArrayOutputStream();

                        do {
                            if (name.equals("IDAT")) {
                                stream.unread(bName);
                                stream.unread(bLength);
                                data.write(stream.readNBytes(length + 12));
                            } else {
                                stream.skipNBytes(4);
                                data.write(Chunk.createIDAT(stream.readNBytes(length - 4)));
                                stream.skipNBytes(4);
                            }
                            bLength = stream.readNBytes(4);
                            length = ByteBuffer.wrap(bLength).getInt();
                            bName = stream.readNBytes(4);

                        } while (new String(bName).equals(name));
                        stream.unread(bName);
                        stream.unread(bLength);

                        img.write(SIGNATURE);
                        img.write(bHeader);
                        img.write(ancillary.toByteArray());
                        img.write(data.toByteArray());
                        img.write(IEND);

                        frame.image = ImageIO.read(new ByteArrayInputStream(img.toByteArray()));
                        frame.width = frame.image.getWidth();
                        frame.height = frame.image.getHeight();
                        images.add(frame);
                    }
                    default -> stream.skipNBytes(length + 4);
                }
            }
        }

        return images;
    }
}