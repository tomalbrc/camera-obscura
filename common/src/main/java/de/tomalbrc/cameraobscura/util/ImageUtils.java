package de.tomalbrc.cameraobscura.util;

import de.tomalbrc.cameraobscura.platform.Platform;
import de.tomalbrc.cameraobscura.platform.Platforms;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ImageUtils {
    private static final MapColor[] MATERIAL_COLORS;

    static {
        try {
            Field field = MapColor.class.getDeclaredField("MATERIAL_COLORS");
            field.setAccessible(true);
            MATERIAL_COLORS = (MapColor[]) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to access MapColor.MATERIAL_COLORS", e);
        }
    }

    private static class LazyColorLookup {
        private static final byte[] LUT = new byte[256 * 256 * 256];
        private static final byte TRANSPARENT;

        static {
            List<TargetColor> targets = new ArrayList<>(256);
            MapColor.Brightness[] brightnesses = MapColor.Brightness.values();
            for (MapColor color : MATERIAL_COLORS) {
                if (color == null || color == MapColor.NONE) continue;
                for (MapColor.Brightness brightness : brightnesses) {
                    int argb = color.calculateARGBColor(brightness);
                    byte packedId = color.getPackedId(brightness);
                    targets.add(new TargetColor(argb, packedId));
                }
            }

            for (int r = 0; r < 256; r++) {
                for (int g = 0; g < 256; g++) {
                    for (int b = 0; b < 256; b++) {
                        int bestDist = Integer.MAX_VALUE;
                        byte bestPackedId = 0;
                        for (TargetColor target : targets) {
                            int dr = r - ARGB.red(target.argb);
                            int dg = g - ARGB.green(target.argb);
                            int db = b - ARGB.blue(target.argb);
                            int dist = dr * dr + dg * dg + db * db;
                            if (dist < bestDist) {
                                bestDist = dist;
                                bestPackedId = target.packedId;
                            }
                        }
                        int index = (r << 16) | (g << 8) | b;
                        LUT[index] = bestPackedId;
                    }
                }
            }

            TRANSPARENT = MapColor.COLOR_BLACK.getPackedId(MapColor.Brightness.LOWEST);
        }

        static byte lookup(int pixel) {
            int alpha = (pixel >> 24) & 0xFF;
            if (alpha < 128) {
                return TRANSPARENT;
            }
            int r = (pixel >> 16) & 0xFF;
            int g = (pixel >> 8) & 0xFF;
            int b = pixel & 0xFF;
            int index = (r << 16) | (g << 8) | b;
            return LUT[index];
        }
    }

    private record TargetColor(int argb, byte packedId) {}

    public static BufferedImage cropCenterSquare(BufferedImage source) {
        int w = source.getWidth();
        int h = source.getHeight();
        if (w == h) return source;
        int size = Math.min(w, h);
        int x = (w - size) / 2;
        int y = (h - size) / 2;
        return source.getSubimage(x, y, size, size);
    }

    public static BufferedImage scaleDownByIntegerFactor(BufferedImage source, int factor) {
        int w = source.getWidth();
        int h = source.getHeight();
        int targetW = w / factor;
        int targetH = h / factor;

        int[] srcPixels = source.getRGB(0, 0, w, h, null, 0, w);
        BufferedImage result = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_RGB);
        int[] destPixels = ((java.awt.image.DataBufferInt) result.getRaster().getDataBuffer()).getData();

        int sampleCount = factor * factor;
        int[] srcYStarts = new int[targetH];
        for (int y = 0; y < targetH; y++) {
            srcYStarts[y] = (targetH - 1 - y) * factor * w;
        }

        for (int y = 0; y < targetH; y++) {
            int srcYBase = srcYStarts[y];
            int destIdx = y * targetW;
            for (int x = 0; x < targetW; x++) {
                int r = 0, g = 0, b = 0;
                int srcXBase = x * factor;
                for (int sy = 0; sy < factor; sy++) {
                    int rowOffset = srcYBase + sy * w;
                    for (int sx = 0; sx < factor; sx++) {
                        int pixel = srcPixels[rowOffset + srcXBase + sx];
                        r += (pixel >> 16) & 0xFF;
                        g += (pixel >> 8) & 0xFF;
                        b += pixel & 0xFF;
                    }
                }
                destPixels[destIdx++] = ((r / sampleCount) << 16) | ((g / sampleCount) << 8) | (b / sampleCount);
            }
        }
        return result;
    }

    private static BufferedImage scaleTo128(BufferedImage image) {
        int size = image.getWidth();
        if (size == 128) return image;

        BufferedImage scaled;
        if (size > 128) {
            if (size % 128 == 0) {
                scaled = scaleDownByIntegerFactor(image, size / 128);
            } else {
                scaled = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
                double step = size / 128.0;
                for (int y = 0; y < 128; y++) {
                    int srcY = (int) (y * step);
                    for (int x = 0; x < 128; x++) {
                        int srcX = (int) (x * step);
                        scaled.setRGB(x, y, image.getRGB(
                                Math.min(srcX, size - 1),
                                Math.min(srcY, size - 1)
                        ));
                    }
                }
            }
        } else {
            scaled = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
            int xOff = (128 - size) / 2;
            int yOff = (128 - size) / 2;
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    scaled.setRGB(xOff + x, yOff + y, image.getRGB(x, y));
                }
            }
        }
        return scaled;
    }

    public static byte[] imageToMapColors(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        byte[] colors = new byte[w * h];
        int[] argbPixels = image.getRGB(0, 0, w, h, null, 0, w);
        for (int i = 0; i < argbPixels.length; i++) {
            colors[i] = rgbToMapColor(argbPixels[i]);
        }
        return colors;
    }

    public static int[] mapColorsToRGB(byte[] colors) {
        int width = 128;
        int height = 128;
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = x + y * width;
                if (index < colors.length) {
                    byte packed = colors[index];

                    int baseId = (packed >> 2) & 0x3F;
                    int brightnessOrdinal = packed & 0x3;
                    MapColor.Brightness brightness = MapColor.Brightness.values()[brightnessOrdinal];

                    MapColor color = null;
                    if (baseId < MATERIAL_COLORS.length) {
                        color = MATERIAL_COLORS[baseId];
                    }

                    if (color == null) {
                        color = MapColor.NONE;
                    }
                    int argb = color.calculateARGBColor(brightness);

                    argb = 0xFF000000 | (argb & 0x00FFFFFF);

                    pixels[index] = argb;
                } else {
                    pixels[index] = 0x00000000;
                }
            }
        }
        return pixels;
    }

    private static byte rgbToMapColor(int pixel) {
        return LazyColorLookup.lookup(pixel);
    }

    public static ItemStack createMapItem(BufferedImage image, ServerLevel level) {
        BufferedImage square = cropCenterSquare(image);
        BufferedImage scaled = scaleTo128(square);

        var mid = ImageUtils.createMapItems(scaled, level, null);
        return mid.getFirst();
    }

    public static List<ItemStack> createMapItems(BufferedImage image, ServerLevel level, @Nullable LivingEntity entity) {
        var xSections = Mth.ceil(image.getWidth() / 128d);
        var ySections = Mth.ceil(image.getHeight() / 128d);

        var xDelta = (xSections * 128 - image.getWidth()) / 2;
        var yDelta = (ySections * 128 - image.getHeight()) / 2;

        var items = new ArrayList<ItemStack>();

        for (int ys = 0; ys < ySections; ys++) {
            for (int xs = 0; xs < xSections; xs++) {
                var id = level.getFreeMapId();
                var state = MapItemSavedData.createFresh(0, 0, (byte) 0, false, true, ResourceKey.create(Registries.DIMENSION, Platform.GENERATED));

                for (int xl = 0; xl < 128; xl++) {
                    for (int yl = 0; yl < 128; yl++) {
                        var x = xl + xs * 128 - xDelta;
                        var y = yl + ys * 128 - yDelta;

                        if (x >= 0 && y >= 0 && x < image.getWidth() && y < image.getHeight()) {
                            state.colors[xl + yl * 128] = rgbToMapColor(image.getRGB(x, y));
                        }
                    }
                }

                level.setMapData(id, state);

                var stack = new ItemStack(Items.FILLED_MAP);
                stack.set(DataComponents.MAP_ID, id);

                if (entity != null) {
                    Platforms.get().getItemDataStore().setEntityRef(stack, EntityReference.of(entity));
                    Platforms.get().getItemDataStore().setLiveMap(stack, true);
                }

                items.add(stack);
            }
        }

        return items;
    }
}