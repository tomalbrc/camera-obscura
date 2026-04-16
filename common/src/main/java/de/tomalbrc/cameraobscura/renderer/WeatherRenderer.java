package de.tomalbrc.cameraobscura.renderer;

import de.tomalbrc.cameraobscura.model.resource.RPElement;
import de.tomalbrc.cameraobscura.model.resource.RPModel;
import de.tomalbrc.cameraobscura.model.triangle.ModelTesselator;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4d;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.List;

public class WeatherRenderer {
    public static final String RAIN_TEXTURE = "environment/rain";
    public static final String SNOW_TEXTURE = "environment/snow";

    public static void render(RenderPipeline pipeline,
                              List<WeatherColumn> columns,
                              String texture) {
        if (columns.isEmpty()) return;

        RPModel model = buildColumnModel(columns, texture);
        var view = new RPModel.View(model);
        var mesh = new ModelTesselator(view).build();
        if (mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(mesh), new Matrix4d(), IntList.of(0xFFFFFFFF)));
        }
    }

    private static RPModel buildColumnModel(List<WeatherColumn> columns, String texture) {
        RPModel model = new RPModel();
        model.textures = new Object2ObjectOpenHashMap<>();
        model.textures.put("tex", RPModel.TextureEntry.of(texture));
        model.ambientOcclusion = false;
        model.elements = new ObjectArrayList<>();

        for (WeatherColumn col : columns) {
            float x = col.x + 0.5f;
            float y0 = col.bottomY;
            float y1 = col.topY;
            float z = col.z + 0.5f;
            float halfSize = 0.5f;

            float u0 = col.uOffset();
            float u1 = u0 + 1.0f;
            float v0 = col.bottomY * 0.25f + col.vOffset();
            float v1 = col.topY * 0.25f + col.vOffset();

            RPElement elem = new RPElement();
            elem.from = new Vector3f(x - halfSize, y0, z - halfSize);
            elem.to = new Vector3f(x + halfSize, y1, z + halfSize);
            elem.shade = false;
            elem.light = false;
            elem.faces = new Object2ObjectOpenHashMap<>();

            elem.faces.put(Direction.NORTH,
                    new RPElement.TextureInfo("#tex",
                            new Vector4f(u0 * 16f, v0 * 16f, u1 * 16f, v1 * 16f), 0, 0));
            model.elements.add(elem);
        }
        return model;
    }

    public static void renderWeather(RenderPipeline pipeline, Level level, Vec3 cameraPos, int ticks, float partialTicks) {
        double rainLevel = level.getRainLevel(partialTicks);
        if (rainLevel <= 0.0f) return;

        int weatherRadius = 4;
        double intensity = rainLevel;

        List<WeatherColumn> rainColumns = new ObjectArrayList<>();
        List<WeatherColumn> snowColumns = new ObjectArrayList<>();

        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        RandomSource random = RandomSource.createThreadLocalInstance();
        int camX = Mth.floor(cameraPos.x);
        int camY = Mth.floor(cameraPos.y);
        int camZ = Mth.floor(cameraPos.z);

        for (int z = camZ - weatherRadius; z <= camZ + weatherRadius; ++z) {
            for (int x = camX - weatherRadius; x <= camX + weatherRadius; ++x) {
                int terrainY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
                int y0 = Math.max(camY - weatherRadius, terrainY);
                int y1 = Math.max(camY + weatherRadius, terrainY);
                if (y1 <= y0) continue;

                mpos.set(x, camY, z);
                Biome.Precipitation prec = level.getBiome(mpos).value().getPrecipitationAt(mpos, level.getSeaLevel());
                if (prec == Biome.Precipitation.NONE) continue;

                int lightY = Math.max(camY, terrainY);
                int light = level.getMaxLocalRawBrightness(mpos.set(x, lightY, z));

                int seed = x * x * 3121 + x * 45238971 ^ z * z * 418711 + z * 13761;
                random.setSeed(seed);

                if (prec == Biome.Precipitation.RAIN) {
                    float blockSpeed = 3.0f + random.nextFloat();
                    float texOffset = -((ticks & 131071) + (x * x * 3121 + x * 45238971 + z * z * 418711 + z * 13761 & 0xFF) + partialTicks) / 32.0f * blockSpeed;
                    rainColumns.add(new WeatherColumn(x, z, y0, y1, 0.0f, texOffset % 32.0f, light));
                } else {
                    float time = ticks + partialTicks;
                    float u = random.nextFloat() + time * 0.01f * (float) random.nextGaussian();
                    float v = random.nextFloat() + time * (float) random.nextGaussian() * 0.001f;
                    float vOffset = -((ticks & 511) + partialTicks) / 512.0f;
                    snowColumns.add(new WeatherColumn(x, z, y0, y1, u, vOffset + v, light));
                }
            }
        }

        WeatherRenderer.render(pipeline, rainColumns, "environment/rain");
        WeatherRenderer.render(pipeline, snowColumns, "environment/snow");
    }

    public record WeatherColumn(int x, int z, int bottomY, int topY, float uOffset, float vOffset, int lightCoords) {
    }
}