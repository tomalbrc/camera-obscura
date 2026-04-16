package de.tomalbrc.cameraobscura.apng;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;
import java.util.zip.Deflater;

/**
 * A class to create an animated PNG.
 */
public class APNGCollector {
    public final static byte APNG_DISPOSE_OP_NONE = 0,
            APNG_DISPOSE_OP_BACKGROUND = 1,
            APNG_DISPOSE_OP_PREVIOUS = 2;
    public final static byte APNG_BLEND_OP_SOURCE = 0,
            APNG_BLEND_OP_OVER = 1;

    private final ByteArrayOutputStream result = new ByteArrayOutputStream();
    private final ByteArrayOutputStream chunks = new ByteArrayOutputStream();
    private final int plays;
    private int frames, index = 0;

    /**
     * Constructs an instance of an object that creates an APNG in which a static image is NOT part of the animation.
     *
     * @param sImg  a static image that displays in a non-animated PNG decoder.
     * @param plays a number of times that this animation should play.
     *              If it is 0, the animation should play indefinitely.
     * @throws IOException if an error occurs during writing.
     */
    public APNGCollector(BufferedImage sImg, int plays) throws IOException {
        this.plays = plays;

        ByteArrayInputStream in = createPNGStream(sImg);
        result.write(in.readNBytes(33));

        chunks.write(in.readNBytes(in.available() - 12));
    }

    /**
     * Constructs an instance of an object that creates an APNG in which a static image is part of the animation.
     *
     * @param sImg        a static image that displays in a non-animated PNG decoder.
     * @param plays       a number of times that this animation should play.
     *                    If it is 0, the animation should play indefinitely.
     * @param numerator   define the numerator of the delay fraction
     * @param denominator define the denominator of the delay fraction.
     * @param dispose     defines the type of frame area disposal to be done after rendering this frame.
     *                    <pre>
     *                     {@summary
     *                     APNG_DISPOSE_OP_NONE
     *                       no disposal is done on this frame before rendering the next; the contents of the output buffer are left as is.
     *
     *                     APNG_DISPOSE_OP_BACKGROUND
     *                       the frame's region of the output buffer is to be cleared to fully transparent black before rendering the next frame.
     *
     *                     APNG_DISPOSE_OP_PREVIOUS
     *                       the frame's region of the output buffer is to be reverted to the previous contents before rendering the next frame.}
     *                     </pre>
     * @param blend       specifies whether the frame is to be alpha blended into the current output buffer content,
     *                    or whether it should completely replace its region in the output buffer.
     *                    <pre>
     *                     {@summary
     *                     APNG_BLEND_OP_SOURCE
     *                       all color components of the frame, including alpha, overwrite the current contents of the frame's output buffer region.
     *
     *                     APNG_BLEND_OP_OVER
     *                       the frame should be composited onto the output buffer based on its alpha.}
     *                     </pre>
     * @throws IOException if an error occurs during writing or reading a static image.
     */
    public APNGCollector(BufferedImage sImg,
                         int plays,
                         short numerator,
                         short denominator,
                         byte dispose,
                         byte blend) throws IOException {
        this.plays = plays;

        ByteArrayInputStream in = createPNGStream(sImg);
        result.write(in.readNBytes(33));

        chunks.write(Chunk.createFcTL(index++, sImg.getWidth(), sImg.getHeight(), 0, 0, numerator, denominator, dispose, blend));
        chunks.write(in.readNBytes(in.available() - 12));
        ++frames;
    }

    /**
     * Add frame in animation.
     *
     * @param frame       animation frame.
     * @param x           define the x position of the following frame.
     * @param y           define the y position of the following frame.
     * @param numerator   define the denominator of the delay fraction.
     * @param denominator defines the type of frame area disposal to be done after rendering this frame.
     * @param dispose     defines the type of frame area disposal to be done after rendering this frame.
     *                    <pre>
     *                     {@summary
     *                     APNG_DISPOSE_OP_NONE
     *                       no disposal is done on this frame before rendering the next; the contents of the output buffer are left as is.
     *
     *                     APNG_DISPOSE_OP_BACKGROUND
     *                       the frame's region of the output buffer is to be cleared to fully transparent black before rendering the next frame.
     *
     *                     APNG_DISPOSE_OP_PREVIOUS
     *                       the frame's region of the output buffer is to be reverted to the previous contents before rendering the next frame.}
     *                     </pre>
     * @param blend       specifies whether the frame is to be alpha blended into the current output buffer content,
     *                    or whether it should completely replace its region in the output buffer.
     *                    <pre>
     *                     {@summary
     *                     APNG_BLEND_OP_SOURCE
     *                       all color components of the frame, including alpha, overwrite the current contents of the frame's output buffer region.
     *
     *                     APNG_BLEND_OP_OVER
     *                       the frame should be composited onto the output buffer based on its alpha.}
     *                     </pre>
     * @throws IOException if an error occurs during writing or reading a frame.
     */
    public void addFrame(BufferedImage frame,
                         int x,
                         int y,
                         short numerator,
                         short denominator,
                         byte dispose,
                         byte blend) throws IOException {
        ByteArrayInputStream in = createPNGStream(frame);
        in.skipNBytes(33);

        chunks.write(Chunk.createFcTL(index++, frame.getWidth(), frame.getHeight(), x, y, numerator, denominator, dispose, blend));
        chunks.write(Chunk.createFdAT(index++, in.readNBytes(in.available() - 12)));
        ++frames;
    }

    /**
     * @return APNG stream in byte array.
     * @throws IOException if an error occurs during writing.
     */
    public byte[] build() throws IOException {
        result.write(Chunk.createAcTL(frames, plays));
        result.write(chunks.toByteArray());
        result.write(Chunk.IEND);
        return result.toByteArray();
    }

    /**
     * Writes the BufferedImage to the byte array stream as a PNG, simplifies PNG stream.
     * <pre>
     * ┏━━━━━━┳━━━━━━┳━━━━━━┳━━━━━━┳━━━━━━┳━━━━━━┓
     * ┃ IHDR ┃ PLTE ┃ gAMA ┃ .... ┃ IDAT ┃ IEND ┃
     * ┗━━━━━━┻━━━━━━┻━━━━━━┻━━━━━━┻━━━━━━┻━━━━━━┛
     *          ↓ ImageIO
     * ┏━━━━━━┳━━━━━━┳━━━━━━┓
     * ┃ IHDR ┃ IDAT ┃ IEND ┃
     * ┗━━━━━━┻━━━━━━┻━━━━━━┛
     * </pre>
     *
     * @param frame image to be recorded.
     * @return PNG stream in {@code ByteArrayInputStream}.
     */
    private ByteArrayInputStream createPNGStream(BufferedImage frame) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        int width = frame.getWidth();
        int height = frame.getHeight();
        int imageType = frame.getType();
        boolean indexed = (imageType == BufferedImage.TYPE_BYTE_INDEXED);

        // sig
        baos.write(new byte[]{(byte) 137, 80, 78, 71, 13, 10, 26, 10});

        // IHDR
        byte bitDepth = (byte) 8; // 8-bit
        byte colorType = indexed ? 3 : (byte) (imageType == BufferedImage.TYPE_INT_ARGB ? 6 : 2);
        ByteArrayOutputStream ihdrData = new ByteArrayOutputStream();
        DataOutputStream d = new DataOutputStream(ihdrData);
        d.writeInt(width);
        d.writeInt(height);
        d.writeByte(bitDepth);
        d.writeByte(colorType);
        d.writeByte(0); // compression
        d.writeByte(0); // filter
        d.writeByte(0); // interlace
        writeChunk(baos, "IHDR", ihdrData.toByteArray());

        // palette
        byte[] palette = null;
        if (indexed) {
            IndexColorModel cm = (IndexColorModel) frame.getColorModel();
            int mapSize = cm.getMapSize();
            palette = new byte[mapSize * 3];
            cm.getReds(palette);

            byte[] r = new byte[mapSize], g = new byte[mapSize], b = new byte[mapSize];
            cm.getReds(r);
            cm.getGreens(g);
            cm.getBlues(b);
            ByteArrayOutputStream pal = new ByteArrayOutputStream();
            for (int i = 0; i < mapSize; i++) {
                pal.write(r[i] & 0xFF);
                pal.write(g[i] & 0xFF);
                pal.write(b[i] & 0xFF);
            }
            writeChunk(baos, "PLTE", pal.toByteArray());
        }

        byte[] rawPixelData;
        int[] pixels = null;
        byte[] indices = null;
        if (indexed) {
            indices = ((java.awt.image.DataBufferByte) frame.getRaster().getDataBuffer()).getData();
            rawPixelData = new byte[height * (1 + width)];
            for (int y = 0; y < height; y++) {
                int rowStart = y * (width + 1);
                rawPixelData[rowStart] = 0;
                System.arraycopy(indices, y * width, rawPixelData, rowStart + 1, width);
            }
        } else {
            pixels = frame.getRGB(0, 0, width, height, null, 0, width);
            int sampleSize = (colorType == 6) ? 4 : 3;
            rawPixelData = new byte[height * (1 + width * sampleSize)];
            for (int y = 0; y < height; y++) {
                int rowStart = y * (1 + width * sampleSize);
                rawPixelData[rowStart] = 0;
                for (int x = 0; x < width; x++) {
                    int argb = pixels[y * width + x];
                    int idx = rowStart + 1 + x * sampleSize;
                    rawPixelData[idx] = (byte) ((argb >> 16) & 0xFF); // R
                    rawPixelData[idx + 1] = (byte) ((argb >> 8) & 0xFF); // G
                    rawPixelData[idx + 2] = (byte) (argb & 0xFF);         // B
                    if (sampleSize == 4)
                        rawPixelData[idx + 3] = (byte) ((argb >> 24) & 0xFF); // A
                }
            }
        }

        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        Deflater def = new Deflater(Deflater.BEST_COMPRESSION);
        def.setInput(rawPixelData);
        def.finish();
        byte[] buf = new byte[8192];
        while (!def.finished()) {
            int len = def.deflate(buf);
            compressed.write(buf, 0, len);
        }
        writeChunk(baos, "IDAT", compressed.toByteArray());
        writeChunk(baos, "IEND", new byte[0]);

        var ba = baos.toByteArray();

        def.close();

        return new ByteArrayInputStream(ba);
    }

    private void writeChunk(OutputStream out, String type, byte[] data) throws IOException {
        DataOutputStream d = new DataOutputStream(out);
        d.writeInt(data.length);
        d.writeBytes(type);
        d.write(data);
        CRC32 crc = new CRC32();
        crc.update(type.getBytes(StandardCharsets.UTF_8));
        crc.update(data);
        d.writeInt((int) crc.getValue());
    }
}