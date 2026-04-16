package de.tomalbrc.cameraobscura.apng;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static de.tomalbrc.cameraobscura.apng.Chunk.*;

public class StreamingAPNGReader implements Iterator<BufferedImage>, AutoCloseable {

    private final PushbackInputStream stream;
    private BufferedImage nextImage;
    private boolean eof;

    private byte[] bHeader = new byte[25];
    private final ByteArrayOutputStream ancillary = new ByteArrayOutputStream();

    public StreamingAPNGReader(InputStream input) throws IOException {
        this.stream = new PushbackInputStream(new BufferedInputStream(input), 8);
        byte[] sig = stream.readNBytes(8);
        if (!Arrays.equals(sig, SIGNATURE)) {
            throw new IOException("Not a valid PNG/APNG");
        }
        nextImage = readImage();
    }

    @Override
    public boolean hasNext() {
        return nextImage != null;
    }

    @Override
    public BufferedImage next() {
        if (nextImage == null) throw new NoSuchElementException();
        BufferedImage current = nextImage;
        try {
            nextImage = readImage();
        } catch (IOException e) {
            nextImage = null;
            throw new UncheckedIOException(e);
        }
        return current;
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }

    private BufferedImage readImage() throws IOException {
        if (eof) return null;

        while (!eof) {
            byte[] bLength = stream.readNBytes(4);
            byte[] bName   = stream.readNBytes(4);
            int    length  = ByteBuffer.wrap(bLength).getInt();
            String name    = new String(bName);

            if ("IEND".equals(name)) {
                eof = true;
                return null;
            }

            switch (name) {
                case "fcTL" -> {
                    Frame frame = new Frame();
                    stream.skipNBytes(4);
                    byte[] chunk = stream.readNBytes(length - 4);
                    bHeader = createIHDR(bHeader, chunk, frame);
                    stream.skipNBytes(4);
                }
                case "IHDR" -> {
                    stream.unread(bName);
                    stream.unread(bLength);
                    byte[] chunk = stream.readNBytes(length + 12);
                    System.arraycopy(chunk, 0, bHeader, 0, chunk.length);
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
                    var img  = new ByteArrayOutputStream();
                    String originalName = name;

                    do {
                        if ("IDAT".equals(name)) {
                            stream.unread(bName);
                            stream.unread(bLength);
                            data.write(stream.readNBytes(length + 12));
                        } else {
                            stream.skipNBytes(4);
                            data.write(createIDAT(stream.readNBytes(length - 4)));
                            stream.skipNBytes(4);
                        }
                        bLength = stream.readNBytes(4);
                        length  = ByteBuffer.wrap(bLength).getInt();
                        bName   = stream.readNBytes(4);
                        name    = new String(bName);
                    } while (originalName.equals(name));

                    stream.unread(bName);
                    stream.unread(bLength);

                    img.write(SIGNATURE);
                    img.write(bHeader);
                    img.write(ancillary.toByteArray());
                    img.write(data.toByteArray());
                    img.write(IEND);

                    BufferedImage image = ImageIO.read(new ByteArrayInputStream(img.toByteArray()));
                    if (image == null) throw new IOException("Failed to decode frame");
                    return image;
                }
                default -> stream.skipNBytes(length + 4);
            }
        }
        return null;
    }
}