package de.tomalbrc.cameraobscura.sore;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import de.tomalbrc.cameraobscura.util.resource.RPHelper;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Texture {
    public static final Texture DEFAULT_TEXTURE = debugPlaceholder();

    private final int[] sheetPixels;
    private final byte[] sheetIndices;
    private final int[] palette;
    private final int paletteSize;
    private final boolean isPalette;

    private final int sheetWidth, sheetHeight;
    private final int frameWidth, frameHeight;
    private final int[] frameOffsets;
    private final int[] frameTimes;
    private final int totalDuration;
    private final boolean interpolate;

    private boolean hasTranslucency;

    private Texture(int[] argbPixels, int width, int height, int frameWidth, int frameHeight, int[] frameOffsets, int[] frameTimes, boolean interpolate) {
        this.sheetPixels = argbPixels.clone();
        this.sheetIndices = null;
        this.palette = null;
        this.paletteSize = 0;
        this.isPalette = false;

        this.sheetWidth = width;
        this.sheetHeight = height;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.frameOffsets = frameOffsets;
        this.frameTimes = frameTimes;
        this.totalDuration = Arrays.stream(frameTimes).sum();
        this.interpolate = interpolate;

        this.hasTranslucency = false;
        for (int argb : this.sheetPixels) {
            var alpha = ((argb >>> 24) & 0xFF);
            if (alpha < 255 && alpha > 0) {
                this.hasTranslucency = true;
                break;
            }
        }
    }

    private Texture(byte[] indices, int[] palette, int width, int height,
                    int frameWidth, int frameHeight,
                    int[] frameOffsets, int[] frameTimes, boolean interpolate) {
        this.sheetIndices = indices.clone();
        this.palette = palette.clone();
        this.paletteSize = palette.length;
        this.isPalette = true;
        this.sheetPixels = null;

        this.sheetWidth = width;
        this.sheetHeight = height;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.frameOffsets = frameOffsets;
        this.frameTimes = frameTimes;
        this.totalDuration = Arrays.stream(frameTimes).sum();
        this.interpolate = interpolate;

        this.hasTranslucency = false;
        for (int i = 0; i < this.sheetIndices.length; i++) {
            int paletteIndex = this.sheetIndices[i] & 0xFF;
            int color = this.palette[paletteIndex];
            var alpha = ((color >>> 24) & 0xFF);
            if (alpha < 255 && alpha > 0) {
                this.hasTranslucency = true;
                break;
            }
        }
    }

    public static Texture fromARGB(int[] argb, int w, int h) {
        return new Texture(argb, w, h, w, h, new int[]{0}, new int[]{1}, false);
    }

    public static Texture fromPng(InputStream inputStream) throws IOException {
        BufferedImage img = ImageIO.read(inputStream);
        if (img == null) throw new IOException("Failed to read PNG");

        int w = img.getWidth();
        int h = img.getHeight();

        if (img.getType() == BufferedImage.TYPE_BYTE_INDEXED) {
            return createPaletteTexture(img, w, h, w, h, new int[]{0}, new int[]{1}, false);
        }

        int[] argb = imageToARGB(img);
        return new Texture(argb, w, h, w, h, new int[]{0}, new int[]{1}, false);
    }

    public static Texture fromPng(byte[] textureBytes, AnimationMeta meta) throws IOException {
        if (meta == null) {
            return fromPng(new ByteArrayInputStream(textureBytes));
        }
        return createAnimatedTexture(textureBytes, meta);
    }

    private static Texture createAnimatedTexture(byte[] textureBytes, AnimationMeta meta) throws IOException {
        BufferedImage sheet = ImageIO.read(new ByteArrayInputStream(textureBytes));
        if (sheet == null) throw new IOException("Failed to read animated texture sheet");

        int sheetWidth = sheet.getWidth();
        int sheetHeight = sheet.getHeight();
        int frameCount = sheetHeight / sheetWidth;

        IntList orderedIndices = new IntArrayList();
        IntList times = new IntArrayList();
        if (meta.frames != null && !meta.frames.isEmpty()) {
            for (AnimationMeta.Frame frame : meta.frames) {
                int idx = frame.index;
                int time = (frame.time != null) ? frame.time : meta.frametime;
                if (idx >= 0 && idx < frameCount) {
                    orderedIndices.add(idx);
                    times.add(time);
                }
            }
        } else {
            for (int i = 0; i < frameCount; i++) {
                orderedIndices.add(i);
                times.add(meta.frametime);
            }
        }

        int[] frameOffsets = new int[orderedIndices.size()];
        for (int i = 0; i < orderedIndices.size(); i++) {
            frameOffsets[i] = orderedIndices.getInt(i) * sheetWidth;
        }
        int[] frameTimesArr = times.toIntArray();

        if (sheet.getType() == BufferedImage.TYPE_BYTE_INDEXED) {
            return createPaletteTexture(sheet, sheetWidth, sheetHeight,
                    sheetWidth, sheetWidth, frameOffsets, frameTimesArr, meta.interpolate);
        } else {
            int[] sheetPixels = imageToARGB(sheet);
            return new Texture(sheetPixels, sheetWidth, sheetHeight,
                    sheetWidth, sheetWidth, frameOffsets, frameTimesArr, meta.interpolate);
        }
    }

    private static Texture createPaletteTexture(BufferedImage img, int width, int height,
                                                int frameWidth, int frameHeight,
                                                int[] frameOffsets, int[] frameTimes,
                                                boolean interpolate) {
        IndexColorModel cm = (IndexColorModel) img.getColorModel();
        int mapSize = cm.getMapSize();
        int[] palette = new int[mapSize];
        byte[] r = new byte[mapSize];
        byte[] g = new byte[mapSize];
        byte[] b = new byte[mapSize];
        byte[] a = new byte[mapSize];
        cm.getReds(r);
        cm.getGreens(g);
        cm.getBlues(b);
        cm.getAlphas(a);
        for (int i = 0; i < mapSize; i++) {
            palette[i] = ((a[i] & 0xFF) << 24) |
                    ((r[i] & 0xFF) << 16) |
                    ((g[i] & 0xFF) << 8) |
                    (b[i] & 0xFF);
        }

        byte[] indices = (byte[]) img.getRaster().getDataElements(0, 0, width, height, null);
        return new Texture(indices, palette, width, height, frameWidth, frameHeight,
                frameOffsets, frameTimes, interpolate);
    }

    private static int[] imageToARGB(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        int[] argb = new int[width * height];

        if (img.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            img.getRGB(0, 0, width, height, argb, 0, width);
            fixGammaMaybe(argb);
        } else {
            img.getRGB(0, 0, width, height, argb, 0, width);
            if (isGrayscale(img)) {
                fixGammaMaybe(argb);
            }
        }
        return argb;
    }

    private static boolean isGrayscale(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = img.getRGB(x, y);
                int a = (rgb >> 24) & 0xFF;
                if (a == 0) continue;
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                if (r != g || g != b) return false;
            }
        }
        return true;
    }

    private static void fixGammaMaybe(int[] pixels) {
        double gamma = 2.2f;
        double[] gammaLUT = new double[256];
        for (int i = 0; i < 256; i++) {
            gammaLUT[i] = Math.pow(i / 255.0, gamma) * 255.0f;
        }
        for (int i = 0; i < pixels.length; i++) {
            int argb = pixels[i];
            int a = (argb >> 24) & 0xFF;
            int r = (argb >> 16) & 0xFF;
            int g = (argb >> 8) & 0xFF;
            int b = argb & 0xFF;
            int newR = (int) gammaLUT[r];
            int newG = (int) gammaLUT[g];
            int newB = (int) gammaLUT[b];
            pixels[i] = (a << 24) | (newR << 16) | (newG << 8) | newB;
        }
    }

    private static int floorMod(int value, int mod) {
        int r = value % mod;
        return r < 0 ? r + mod : r;
    }

    private static int blendColors(int c1, int c2, double blend) {
        int a1 = (c1 >> 24) & 0xFF, r1 = (c1 >> 16) & 0xFF, g1 = (c1 >> 8) & 0xFF, b1 = c1 & 0xFF;
        int a2 = (c2 >> 24) & 0xFF, r2 = (c2 >> 16) & 0xFF, g2 = (c2 >> 8) & 0xFF, b2 = c2 & 0xFF;
        int a = (int) (a1 + (a2 - a1) * blend);
        int r = (int) (r1 + (r2 - r1) * blend);
        int g = (int) (g1 + (g2 - g1) * blend);
        int b = (int) (b1 + (b2 - b1) * blend);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static Texture generateSolid(int argb) {
        int[] pixels = new int[]{argb};
        return new Texture(pixels, 1, 1, 1, 1, new int[]{0}, new int[]{1}, false);
    }

    public static Texture debugPlaceholder() {
        int w = 16, h = 16;
        int[] pixels = new int[w * h];
        int purple = 0xFFF800F8;
        int black = 0xFF000000;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int cellX = x / 8;
                int cellY = y / 8;
                boolean even = (cellX + cellY) % 2 == 0;
                pixels[y * w + x] = even ? purple : black;
            }
        }
        return new Texture(pixels, w, h, w, h, new int[]{0}, new int[]{1}, false);
    }

    public boolean hasTranslucency() {
        return hasTranslucency;
    }

    public void setHasTranslucency(boolean hasTranslucency) {
        this.hasTranslucency = hasTranslucency;
    }

    public int sample(double u, double v) {
        return sampleFrame(0, u, v);
    }

    public int sampleAnimated(double u, double v, long ticks) {
        if (frameOffsets.length == 1) return sample(u, v);
        long tick = ticks % totalDuration;
        int frameIndex = getFrameIndex(tick);
        if (interpolate) {
            double blend = getBlendFactor(tick);
            int nextIndex = (frameIndex + 1) % frameOffsets.length;
            return blendColors(sampleFrame(frameIndex, u, v),
                    sampleFrame(nextIndex, u, v), blend);
        } else {
            return sampleFrame(frameIndex, u, v);
        }
    }

    private int sampleFrame(int frameIdx, double u, double v) {
        int x = floorMod((int) Math.floor(u * frameWidth), frameWidth);
        int y = floorMod((int) Math.floor(v * frameHeight), frameHeight);
        int offsetY = frameOffsets[frameIdx];
        int pixelIndex = (offsetY + y) * sheetWidth + x;

        if (isPalette) {
            int index = sheetIndices[pixelIndex] & 0xFF;
            return palette[index];
        } else {
            return sheetPixels[pixelIndex];
        }
    }

    private int getFrameIndex(long tick) {
        int accum = 0;
        for (int i = 0; i < frameTimes.length; i++) {
            accum += frameTimes[i];
            if (tick < accum) return i;
        }
        return 0;
    }

    private double getBlendFactor(long tick) {
        int accum = 0;
        for (int i = 0; i < frameTimes.length; i++) {
            int dur = frameTimes[i];
            if (tick < accum + dur) {
                return (tick - accum) / (double) dur;
            }
            accum += dur;
        }
        return 0f;
    }

    public int frameWidth() {
        return frameWidth;
    }

    public int frameHeight() {
        return frameHeight;
    }

    public static class AnimationMeta {
        @SerializedName("frametime")
        int frametime = 1;

        @SerializedName("interpolate")
        boolean interpolate = false;

        @SerializedName("frames")
        List<Frame> frames = new ArrayList<>();

        public static AnimationMeta fromJson(String json) {
            return RPHelper.gson.fromJson(json, AnimationMeta.class);
        }

        public static class Deserializer implements JsonDeserializer<AnimationMeta> {
            @Override
            public AnimationMeta deserialize(JsonElement json, Type typeOfT,
                                             JsonDeserializationContext context) throws JsonParseException {
                AnimationMeta meta = new AnimationMeta();
                JsonObject obj = json.getAsJsonObject();

                if (obj.has("animation") && obj.get("animation").isJsonObject()) {
                    obj = obj.getAsJsonObject("animation");
                } else return null;

                meta.frametime = obj.has("frametime") ? obj.get("frametime").getAsInt() : 1;
                meta.interpolate = obj.has("interpolate") && obj.get("interpolate").getAsBoolean();

                if (obj.has("frames") && obj.get("frames").isJsonArray()) {
                    JsonArray arr = obj.getAsJsonArray("frames");
                    for (JsonElement el : arr) {
                        if (el.isJsonPrimitive()) {
                            int idx = el.getAsInt();
                            meta.frames.add(new Frame(idx, null));
                        } else if (el.isJsonObject()) {
                            Frame f = context.deserialize(el, Frame.class);
                            meta.frames.add(f);
                        }
                    }
                }
                return meta;
            }
        }

        record Frame(@SerializedName("index") int index, @SerializedName("time") Integer time) {
        }
    }
}