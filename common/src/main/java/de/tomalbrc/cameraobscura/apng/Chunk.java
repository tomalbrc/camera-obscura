package de.tomalbrc.cameraobscura.apng;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.CRC32;

/**
 * Utility class responsible for converting one type of chunks into another.
 */
class Chunk {
    static final byte[] SIGNATURE = {(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a};
    static final byte[] IEND = {0, 0, 0, 0, 73, 69, 78, 68, (byte) 174, 66, 96, (byte) 130};
    static final byte[] IHDR = {73, 72, 68, 82};
    static final byte[] IDAT = {73, 68, 65, 84};
    static final byte[] ACTL = {97, 99, 84, 76};
    static final byte[] FCTL = {102, 99, 84, 76};
    static final byte[] FDAT = {102, 100, 65, 84};
    static final CRC32 crc = new CRC32();

    /**
     * Makes a IHDR chunk from the IHDR chunk of a static image and frame dimensions from the fcTL chunk.
     *
     * @param previous IHDR chunk of a static image or previous frame.
     * @param input    data from fcTL chunk.
     * @param frame    instance of the frame to which this chunk will attach.
     * @return byte array with length of 25; IHDR chunk.
     */
    static byte[] createIHDR(byte[] previous, byte[] input, Frame frame) {
        frame.width = ByteBuffer.wrap(input, 0, 4).getInt();
        frame.height = ByteBuffer.wrap(input, 4, 4).getInt();
        frame.x = ByteBuffer.wrap(input, 8, 4).getInt();
        frame.y = ByteBuffer.wrap(input, 12, 4).getInt();
        frame.dispose = input[20];
        frame.blend = input[21];

        byte[] chunk = new byte[25];
        byte[] data = new byte[17];

        System.arraycopy(IHDR, 0, data, 0, 4);
        System.arraycopy(input, 0, data, 4, 8);
        data[12] = previous[16];
        data[13] = previous[17];
        data[16] = previous[20];

        crc.reset();
        crc.update(data);

        System.arraycopy(new byte[]{0, 0, 0, 13}, 0, chunk, 0, 4);
        System.arraycopy(data, 0, chunk, 4, data.length);
        System.arraycopy(asArray((int) crc.getValue()), 0, chunk, 21, 4);

        return chunk;
    }

    /**
     * Makes a IDAT chunk from the fdAT chunk of an animation frame.
     *
     * @param input data from fdAT chunk.
     * @return byte array with length of fdAT - 4; IDAT chunk.
     */
    static byte[] createIDAT(byte[] input) {
        byte[] chunk = new byte[input.length + 12];
        byte[] data = new byte[input.length + 4];

        System.arraycopy(IDAT, 0, data, 0, 4);
        System.arraycopy(input, 0, data, 4, input.length);

        crc.reset();
        crc.update(data);

        System.arraycopy(asArray(input.length), 0, chunk, 0, 4);
        System.arraycopy(data, 0, chunk, 4, data.length);
        System.arraycopy(asArray((int) crc.getValue()), 0, chunk, data.length + 4, 4);

        return chunk;
    }

    /**
     * Makes a fcTL chunk from given parameters.
     *
     * @param index       chunk sequence number.
     * @param width       frame width.
     * @param height      frame height.
     * @param x           frame pos x offset.
     * @param y           frame pos x offset.
     * @param numerator   define the numerator of the delay fraction
     * @param denominator define the denominator of the delay fraction.
     * @param dispose     defines the type of frame area disposal to be done after rendering this frame.
     * @param blend       specifies whether the frame is to be alpha blended into the current output buffer content,
     *                    or whether it should completely replace its region in the output buffer.
     * @return byte array with length of 38; fcTL chunk.
     */
    static byte[] createFcTL(int index, int width, int height, int x, int y,
                             short numerator, short denominator,
                             byte dispose, byte blend) {
        byte[] chunk = new byte[38];
        byte[] data = new byte[30];

        System.arraycopy(FCTL, 0, data, 0, 4);
        System.arraycopy(asArray(index), 0, data, 4, 4);
        System.arraycopy(asArray(width), 0, data, 8, 4);
        System.arraycopy(asArray(height), 0, data, 12, 4);
        System.arraycopy(asArray(x), 0, data, 16, 4);
        System.arraycopy(asArray(y), 0, data, 20, 4);
        System.arraycopy(asArray(numerator), 0, data, 24, 2);
        System.arraycopy(asArray(denominator), 0, data, 26, 2);
        data[28] = dispose;
        data[29] = blend;

        crc.reset();
        crc.update(data);

        System.arraycopy(new byte[]{0, 0, 0, 26}, 0, chunk, 0, 4);
        System.arraycopy(data, 0, chunk, 4, data.length);
        System.arraycopy(asArray((int) crc.getValue()), 0, chunk, data.length + 4, 4);

        return chunk;
    }

    /**
     * Makes a fdAT chunk from the IDAT and sequence number.
     *
     * @param index sequence number.
     * @param input data from IDAT chunk.
     * @return byte array with length of IDAT + 4; fdAT chunk.
     */
    static byte[] createFdAT(int index, byte[] input) throws IOException {
        ByteArrayOutputStream d = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(input);

        d.write(FDAT);
        d.write(asArray(index));

        while (in.available() > 4) {
            int size = ByteBuffer.wrap(in.readNBytes(4)).getInt();
            in.skipNBytes(4);
            d.write(in.readNBytes(size));
            in.skipNBytes(4);
        }

        byte[] data = d.toByteArray();
        byte[] chunk = new byte[8 + data.length];

        crc.reset();
        crc.update(data);

        System.arraycopy(asArray(data.length - 4), 0, chunk, 0, 4);
        System.arraycopy(data, 0, chunk, 4, data.length);
        System.arraycopy(asArray((int) crc.getValue()), 0, chunk, data.length + 4, 4);

        return chunk;
    }

    /**
     * Makes a acTL chunk from the given parameters.
     *
     * @param frames number of frames in animation. Must equal the number of fcTL chunks.
     * @param plays  number of times that this animation should play.
     * @return byte array with length of 20; acTL chunk.
     */
    static byte[] createAcTL(int frames, int plays) {
        byte[] chunk = new byte[20];
        byte[] data = new byte[12];

        System.arraycopy(ACTL, 0, data, 0, 4);
        System.arraycopy(asArray(frames), 0, data, 4, 4);
        System.arraycopy(asArray(plays), 0, data, 8, 4);

        crc.reset();
        crc.update(data);

        System.arraycopy(new byte[]{0, 0, 0, 8}, 0, chunk, 0, 4);
        System.arraycopy(data, 0, chunk, 4, data.length);
        System.arraycopy(asArray((int) crc.getValue()), 0, chunk, data.length + 4, 4);

        return chunk;
    }

    /**
     * Represents integer as an array of bytes.
     *
     * @param i integer.
     * @return byte array with length of 4.
     */
    static byte[] asArray(int i) {
        byte[] res = new byte[4];

        res[0] = (byte) ((i & 0xFF000000) >>> 24);
        res[1] = (byte) ((i & 0x00FF0000) >> 16);
        res[2] = (byte) ((i & 0x0000FF00) >> 8);
        res[3] = (byte) (i & 0x000000FF);

        return res;
    }

    /**
     * Represents short integer as an array of bytes.
     *
     * @param s short integer.
     * @return byte array with length of 2.
     */
    static byte[] asArray(short s) {
        byte[] res = new byte[2];

        res[0] = (byte) ((s & 0x0000FF00) >>> 8);
        res[1] = (byte) (s & 0x000000FF);

        return res;
    }
}