package de.tomalbrc.cameraobscura;

import com.google.gson.annotations.SerializedName;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;

import java.awt.image.BufferedImage;
import java.util.Locale;
import java.util.UUID;

public class Components {

    private static final int[][] BAYER_MATRIX_2X2 = {{0, 2}, {3, 1}};
    private static final int[][] BAYER_MATRIX_4X4 = {{0, 8, 2, 10}, {12, 4, 14, 6}, {3, 11, 1, 9}, {15, 7, 13, 5}};
    private static final int[][] BAYER_MATRIX_8X8 = {{0, 48, 12, 60, 3, 51, 15, 63}, {32, 16, 44, 28, 35, 19, 47, 31}, {8, 56, 4, 52, 11, 59, 7, 55}, {40, 24, 36, 20, 43, 27, 39, 23}, {2, 50, 14, 62, 1, 49, 13, 61}, {34, 18, 46, 30, 33, 17, 45, 29}, {10, 58, 6, 54, 9, 57, 5, 53}, {42, 26, 38, 22, 41, 25, 37, 21}};
    private static final ErrorKernel FLOYD_STEINBERG_KERNEL = new ErrorKernel(new ErrorKernel.Entry(1, 0, 7f / 16), new ErrorKernel.Entry(-1, 1, 3f / 16), new ErrorKernel.Entry(0, 1, 5f / 16), new ErrorKernel.Entry(1, 1, 1f / 16));
    private static final ErrorKernel ATKINSON_KERNEL = new ErrorKernel(new ErrorKernel.Entry(1, 0, 1f / 8), new ErrorKernel.Entry(2, 0, 1f / 8), new ErrorKernel.Entry(-1, 1, 1f / 8), new ErrorKernel.Entry(0, 1, 1f / 8), new ErrorKernel.Entry(1, 1, 1f / 8), new ErrorKernel.Entry(0, 2, 1f / 8));
    private static final ErrorKernel JARVIS_JUDICE_NINKE_KERNEL = new ErrorKernel(new ErrorKernel.Entry(1, 0, 7f / 48), new ErrorKernel.Entry(2, 0, 5f / 48), new ErrorKernel.Entry(-2, 1, 3f / 48), new ErrorKernel.Entry(-1, 1, 5f / 48), new ErrorKernel.Entry(0, 1, 7f / 48), new ErrorKernel.Entry(1, 1, 5f / 48), new ErrorKernel.Entry(2, 1, 3f / 48), new ErrorKernel.Entry(-2, 2, 1f / 48), new ErrorKernel.Entry(-1, 2, 3f / 48), new ErrorKernel.Entry(0, 2, 5f / 48), new ErrorKernel.Entry(1, 2, 3f / 48), new ErrorKernel.Entry(2, 2, 1f / 48));
    private static final ErrorKernel STUCKI_KERNEL = new ErrorKernel(new ErrorKernel.Entry(1, 0, 8f / 42), new ErrorKernel.Entry(2, 0, 4f / 42), new ErrorKernel.Entry(-2, 1, 2f / 42), new ErrorKernel.Entry(-1, 1, 4f / 42), new ErrorKernel.Entry(0, 1, 8f / 42), new ErrorKernel.Entry(1, 1, 4f / 42), new ErrorKernel.Entry(2, 1, 2f / 42), new ErrorKernel.Entry(-2, 2, 1f / 42), new ErrorKernel.Entry(-1, 2, 2f / 42), new ErrorKernel.Entry(0, 2, 4f / 42), new ErrorKernel.Entry(1, 2, 2f / 42), new ErrorKernel.Entry(2, 2, 1f / 42));
    private static final ErrorKernel SIERRA_KERNEL = new ErrorKernel(new ErrorKernel.Entry(1, 0, 5f / 32), new ErrorKernel.Entry(2, 0, 3f / 32), new ErrorKernel.Entry(-2, 1, 1f / 32), new ErrorKernel.Entry(-1, 1, 4f / 32), new ErrorKernel.Entry(0, 1, 5f / 32), new ErrorKernel.Entry(1, 1, 4f / 32), new ErrorKernel.Entry(2, 1, 2f / 32), new ErrorKernel.Entry(-1, 2, 2f / 32), new ErrorKernel.Entry(0, 2, 2f / 32));
    private static final ErrorKernel BURKES_KERNEL = new ErrorKernel(new ErrorKernel.Entry(1, 0, 8f / 32), new ErrorKernel.Entry(2, 0, 4f / 32), new ErrorKernel.Entry(-2, 1, 2f / 32), new ErrorKernel.Entry(-1, 1, 4f / 32), new ErrorKernel.Entry(0, 1, 8f / 32), new ErrorKernel.Entry(1, 1, 4f / 32), new ErrorKernel.Entry(2, 1, 2f / 32));

    public static BufferedImage processImage(BufferedImage src, ColorMode colorMode, DitherMode ditherMode) {
        BufferedImage colored = applyColorMode(src, colorMode);

        if (colorMode == ColorMode.MONOCHROME && ditherMode == DitherMode.NONE) {
            int w = colored.getWidth();
            int h = colored.getHeight();
            BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int rgb = colored.getRGB(x, y);
                    int gray = rgbToGray(rgb);
                    int bw = gray > 127 ? 0xFF_FFFFFF : 0xFF_000000;
                    out.setRGB(x, y, bw);
                }
            }
            return out;
        }

        if (ditherMode != DitherMode.NONE) {
            return applyDither(colored, ditherMode);
        }

        return colored;
    }

    private static BufferedImage applyColorMode(BufferedImage src, ColorMode mode) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        return switch (mode) {
            case GRAYSCALE -> {
                for (int y = 0; y < h; y++) {
                    for (int x = 0; x < w; x++) {
                        int rgb = src.getRGB(x, y);
                        int gray = rgbToGray(rgb);
                        out.setRGB(x, y, grayRgb(gray, src.getRGB(x, y) >> 24 & 0xff));
                    }
                }
                yield out;
            }
            case MONOCHROME -> applyColorMode(src, ColorMode.GRAYSCALE);
            case SEPIA -> {
                for (int y = 0; y < h; y++) {
                    for (int x = 0; x < w; x++) {
                        int rgb = src.getRGB(x, y);
                        int a = rgb >> 24 & 0xff;
                        int r = rgb >> 16 & 0xff;
                        int g = rgb >> 8 & 0xff;
                        int b = rgb & 0xff;

                        int tr = Math.min(255, (int) (0.393 * r + 0.769 * g + 0.189 * b));
                        int tg = Math.min(255, (int) (0.349 * r + 0.686 * g + 0.168 * b));
                        int tb = Math.min(255, (int) (0.272 * r + 0.534 * g + 0.131 * b));

                        out.setRGB(x, y, (a << 24) | (tr << 16) | (tg << 8) | tb);
                    }
                }
                yield out;
            }
            default -> src;
        };
    }

    private static BufferedImage applyDither(BufferedImage grayImage, DitherMode mode) {
        return switch (mode) {
            case BAYER_2X2 -> orderedDither(grayImage, BAYER_MATRIX_2X2, 2);
            case BAYER_4X4 -> orderedDither(grayImage, BAYER_MATRIX_4X4, 4);
            case BAYER_8X8 -> orderedDither(grayImage, BAYER_MATRIX_8X8, 8);
            case FLOYD_STEINBERG -> errorDiffuse(grayImage, FLOYD_STEINBERG_KERNEL);
            case ATKINSON -> errorDiffuse(grayImage, ATKINSON_KERNEL);
            case JARVIS_JUDICE_NINKE -> errorDiffuse(grayImage, JARVIS_JUDICE_NINKE_KERNEL);
            case STUCKI -> errorDiffuse(grayImage, STUCKI_KERNEL);
            case SIERRA -> errorDiffuse(grayImage, SIERRA_KERNEL);
            case BURKES -> errorDiffuse(grayImage, BURKES_KERNEL);
            default -> grayImage;
        };
    }

    private static final int LEVELS = 4;

    private static BufferedImage orderedDither(BufferedImage src, int[][] bayer, int size) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        float step = 255f / (LEVELS - 1);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = src.getRGB(x, y);
                int a = (rgb >> 24) & 0xff;
                float r = (rgb >> 16) & 0xff;
                float g = (rgb >> 8) & 0xff;
                float b = rgb & 0xff;

                float threshold = ((bayer[y % size][x % size] + 0.5f) / (size * size)) * step;

                int newR = quantize(r, step, threshold);
                int newG = quantize(g, step, threshold);
                int newB = quantize(b, step, threshold);

                out.setRGB(x, y, (a << 24) | (newR << 16) | (newG << 8) | newB);
            }
        }
        return out;
    }

    private static int quantize(float value, float step, float threshold) {
        int low = (int) (value / step);
        float remainder = value - low * step;
        if (remainder > threshold) low++;
        if (low >= LEVELS) low = LEVELS - 1;
        return Math.round(low * step);
    }

    private static BufferedImage errorDiffuse(BufferedImage src, ErrorKernel kernel) {
        int w = src.getWidth();
        int h = src.getHeight();

        float[][] errR = new float[h][w];
        float[][] errG = new float[h][w];
        float[][] errB = new float[h][w];

        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        float step = 255f / (LEVELS - 1);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = src.getRGB(x, y);
                int a = (rgb >> 24) & 0xff;
                float r = ((rgb >> 16) & 0xff) + errR[y][x];
                float g = ((rgb >> 8) & 0xff)  + errG[y][x];
                float b = (rgb & 0xff)         + errB[y][x];

                r = Math.min(255, Math.max(0, r));
                g = Math.min(255, Math.max(0, g));
                b = Math.min(255, Math.max(0, b));

                float newR = Math.round(r / step) * step;
                float newG = Math.round(g / step) * step;
                float newB = Math.round(b / step) * step;

                out.setRGB(x, y, (a << 24) | ((int)newR << 16) | ((int)newG << 8) | (int)newB);

                float errorR = r - newR;
                float errorG = g - newG;
                float errorB = b - newB;

                for (ErrorKernel.Entry entry : kernel.entries) {
                    int nx = x + entry.x;
                    int ny = y + entry.y;
                    if (nx >= 0 && nx < w && ny >= 0 && ny < h) {
                        errR[ny][nx] += (float) (errorR * entry.weight);
                        errG[ny][nx] += (float) (errorG * entry.weight);
                        errB[ny][nx] += (float) (errorB * entry.weight);
                    }
                }
            }
        }
        return out;
    }

    private static int rgbToGray(int rgb) {
        int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = rgb & 0xff;
        return (int) (0.299 * r + 0.587 * g + 0.114 * b);
    }

    private static int grayRgb(int gray, int alpha) {
        return (alpha << 24) | (gray << 16) | (gray << 8) | gray;
    }

    public enum ColorMode {
        COLOR, MONOCHROME, GRAYSCALE, SEPIA;

        public static final Codec<ColorMode> CODEC = Codec.STRING.xmap(name -> switch (name.toLowerCase(Locale.ROOT)) {
            case "monochrome" -> MONOCHROME;
            case "grayscale" -> GRAYSCALE;
            case "sepia" -> SEPIA;
            default -> COLOR;
        }, ColorMode::name);
    }
    public enum DitherMode {
        NONE, BAYER_2X2, BAYER_4X4, BAYER_8X8, FLOYD_STEINBERG, ATKINSON, JARVIS_JUDICE_NINKE, STUCKI, SIERRA, BURKES;

        public static final Codec<DitherMode> CODEC = Codec.STRING.xmap(DitherMode::fromName, DitherMode::name);

        private static DitherMode fromName(String name) {
            try {
                return valueOf(name.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                return NONE;
            }
        }
    }

    public record Resolution(int width, int height) {
        public static final Resolution DEFAULT = new Resolution(128, 128);
        public static final Codec<Resolution> CODEC = RecordCodecBuilder.create(instance -> instance.group(Codec.INT.fieldOf("width").forGetter(Resolution::width), Codec.INT.fieldOf("height").forGetter(Resolution::height)).apply(instance, Resolution::new));
    }

    public record VideoParams(@SerializedName("frame_rate") int frameRate, @SerializedName("max_frames") int maxFrames, @SerializedName("loop_playback") boolean loopPlayback) {
        public static final VideoParams DEFAULT = new VideoParams(20, 600, true);
        public static final Codec<VideoParams> CODEC = RecordCodecBuilder.create(instance -> instance.group(Codec.INT.fieldOf("frame_rate").forGetter(VideoParams::frameRate), Codec.INT.fieldOf("max_frames").forGetter(VideoParams::maxFrames), Codec.BOOL.fieldOf("loop_playback").forGetter(VideoParams::loopPlayback)).apply(instance, VideoParams::new));
    }

    public record MediaData(UUID id, @SerializedName("video_params") VideoParams videoParams) {
        public static final Codec<MediaData> CODEC = RecordCodecBuilder.create(instance -> instance.group(UUIDUtil.LENIENT_CODEC.fieldOf("id").forGetter(MediaData::id), VideoParams.CODEC.fieldOf("video_params").forGetter(MediaData::videoParams)).apply(instance, MediaData::new));
    }

    private static class ErrorKernel {
        final Entry[] entries;

        ErrorKernel(Entry... entries) {
            this.entries = entries;
        }

        record Entry(int x, int y, double weight) {
        }
    }
}