package de.tomalbrc.cameraobscura.render;

import de.tomalbrc.cameraobscura.ModConfig;
import de.tomalbrc.cameraobscura.color.BlockColors;
import de.tomalbrc.cameraobscura.color.MiscColors;
import de.tomalbrc.cameraobscura.render.model.RenderModel;
import de.tomalbrc.cameraobscura.render.model.resource.RPModel;
import de.tomalbrc.cameraobscura.render.model.triangle.QuadModel;
import de.tomalbrc.cameraobscura.util.BuiltinEntityModels;
import de.tomalbrc.cameraobscura.util.BuiltinModels;
import de.tomalbrc.cameraobscura.util.ColorHelper;
import de.tomalbrc.cameraobscura.util.RPHelper;
import de.tomalbrc.cameraobscura.world.BlockIterator;
import de.tomalbrc.cameraobscura.world.EntityIterator;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.*;

public class Raytracer {
    private static final Vector3f SUN = new Vector3f(1, 2, 1).normalize();

    private final ServerLevel level;

    private static final Map<BlockState, List<RenderModel>> renderModelCache = new Reference2ObjectArrayMap<>();
    private static final Int2ObjectArrayMap<RenderModel> fluidRenderModelCache = new Int2ObjectArrayMap<>();
    //private final Long2ReferenceMap<TriangleModel> entityRenderModelCache = new Long2ReferenceArrayMap<>();

    private final BlockIterator blockIterator;
    private final EntityIterator entityIterator;

    private final int distance;

    private final int skyDarken;

    private static final QuadModel localSkyModel = new QuadModel(BuiltinModels.skyModel(Vec3.ZERO));

    public Raytracer(LivingEntity entity, int distance) {
        this.level = (ServerLevel) entity.level();
        this.distance = distance;

        var cache = new Object2ObjectOpenHashMap<Vector2i, LevelChunk>();
        this.blockIterator = new BlockIterator(this.level, cache);
        this.entityIterator = new EntityIterator(this.level, cache, entity);

        this.skyDarken = this.level.getSkyDarken();
    }

    public void preloadChunks(BlockPos center) {
        this.blockIterator.preloadChunks(center, distance);
    }

    public int trace(Vec3 pos, Vec3 direction) {
        var color = traceSingle(pos, direction);
        // todo: ambient occlusion
        // maybe make a normal and depth buffer and calculate ssao,
        // casting another (or multiple) rays may be too expensive for semi-realtime map creation

        if ((color >> 24 & 0xff) < 255) {
            // apply sky and clouds
            var skyColor = level.dimensionType().hasSkyLight() ? skyColorWithClouds(pos, direction) : 0xff_101010;
            color = ColorHelper.alphaComposite(color, skyColor);
        }

        // color may contain transparency if no sky color was set (or may be black)
        return color;
    }

    private int traceSingle(Vec3 pos, Vec3 direction) {
        var scaledDir = new Vec3(direction.x, direction.y, direction.z).scale(128).add(pos);

        ClipContext context = new ClipContext(pos, scaledDir, null, ClipContext.Fluid.ANY, CollisionContext.empty());
        List<BlockIterator.WorldHit> result = this.blockIterator.raycast(context);
        List<EntityIterator.EntityHit> entityResult = this.entityIterator.raycast(context);

        int color = 0x00ffffff;

        boolean hasHitWater = false; // only get water color once

        for (int i = 0; i < result.size(); i++) {
            boolean isWater = result.get(i).isWaterOrWaterlogged();
            boolean transparent = (color >> 24 & 0xff) == 0;

            if (hasHitWater && !transparent) {
                // only trace water once
                if (result.get(i).blockState().is(Blocks.WATER))
                    continue;
            }

            var rayRes = colorFromRaycast(pos, direction, result.get(i), !(hasHitWater && !transparent), entityResult);

            var c1 = ColorHelper.unpackColor(color);
            var c2 = ColorHelper.unpackColor(rayRes);
            color = ColorHelper.packColor(ColorHelper.alphaComposite(c1, c2));

            hasHitWater |= isWater;

            // check if color is opaque already and return early
            if ((color >> 24 & 0xff) >= 255)
                return color;
        }

        return color;
    }

    private int colorFromRaycast(Vec3 pos, Vec3 direction, BlockIterator.WorldHit result, boolean allowWater, List<EntityIterator.EntityHit> entityHits) {
        double[] shadeTint = new double[]{1, 1, 1, 1};
        boolean blockLight = !ModConfig.getInstance().fullbright;

        BlockPos blockPos = result.blockPos();
        BlockState blockState = result.blockState();

        int modelColor = 0x00_ffffff;

        BlockPos lightPos = result.blockPos();
        if (!blockState.isAir() || !entityHits.isEmpty()) {
            List<RPModel.View> rpModels = null;
            RPModel.View rpModel = null;

            if (blockState.is(Blocks.WATER))
                rpModel = BuiltinModels.liquidModel(result.fluidState(), result.fluidStateAbove());
            else if (blockState.is(Blocks.LAVA))
                rpModel = BuiltinModels.liquidModel(result.fluidState(), result.fluidStateAbove());
            else if (blockState.is(Blocks.END_PORTAL))
                rpModel = BuiltinModels.portalModel(true);
            else if (blockState.is(Blocks.END_GATEWAY))
                rpModel = BuiltinModels.portalModel(false);
            else if (blockState.is(Blocks.CHEST))
                rpModel = BuiltinModels.chestModel(blockState);
            else if (blockState.is(Blocks.ENDER_CHEST))
                rpModel = BuiltinModels.chestModel(blockState);
            else if (blockState.is(BlockTags.SHULKER_BOXES))
                rpModel = BuiltinModels.shulkerModel(blockState, Optional.ofNullable(((ShulkerBoxBlock)result.blockState().getBlock()).getColor()));
            else if (blockState.is(BlockTags.BEDS))
                rpModel = BuiltinModels.bedModel(blockState, Optional.of(((BedBlock)blockState.getBlock()).getColor()));
            else if (blockState.is(Blocks.DECORATED_POT))
                rpModel = BuiltinModels.decoratedPotModel();
            else if (blockState.is(Blocks.CONDUIT))
                rpModel = BuiltinModels.conduitModel();
            else if (blockState.is(BlockTags.ALL_SIGNS))
                rpModel = BuiltinModels.signModel(blockState);
            else // load from vanilla rp
                rpModels = RPHelper.loadBlockModelViews(blockState);

            if (rpModels == null) {
                rpModels = rpModel != null ? ObjectArrayList.of(rpModel) : ObjectArrayList.of();
            }

            if (blockState.is(Blocks.BELL)) {
                rpModels.add(BuiltinModels.bellModel(blockState));
            }

            List<RenderModel> renderModels = this.getBlockRenderModels(rpModels, result, allowWater);

            // water/foliage/grass color - getBlurredBiomeWaterOrBlockColor returns the water color if blockState is null, the grass/foliage color otherwise
            int blockColor = getBlurredBiomeWaterOrBlockColor(blockPos, result.isWaterOrWaterlogged() ? null : blockState, ModConfig.getInstance().biomeBlend);

            List<RenderModel.ModelHit> hits = new ObjectArrayList<>();

            for (int i = 0; i < renderModels.size(); i++) {
                hits.addAll(renderModels.get(i).intersect(pos.toVector3f(), direction.toVector3f(), blockPos.getCenter().toVector3f(), blockColor));
            }

            if (ModConfig.getInstance().renderEntities) {
                for (int i = 0; i < entityHits.size(); i++) {
                    EntityIterator.EntityHit hit = entityHits.get(i);
                    RPModel.View view = BuiltinEntityModels.getModel(hit.type(), blockPos.getCenter().toVector3f().sub(hit.position()).add(0, -0.5f, 0), hit.rotation(), hit.uuid(), hit.data());
                    RenderModel entityModel = createOrGetCached(view);
                    hits.addAll(entityModel.intersect(pos.toVector3f(), direction.toVector3f(), blockPos.getCenter().toVector3f(), blockColor));
                }
            }

            hits.sort(Comparator.comparingDouble(RenderModel.ModelHit::t));

            for (int i = 0; i < hits.size(); i++) {
                RenderModel.ModelHit modelHit = hits.get(i);

                if (modelHit.direction() != null && blockState.isSolidRender())
                    lightPos = result.blockPos().relative(modelHit.direction());


                modelColor = ColorHelper.alphaComposite(modelColor, modelHit.color());

                // shading from a global light source
                if (modelHit.shade() && modelHit.direction() != null) {
                    Vector3f normal = new Vector3f(modelHit.direction().getUnitVec3().toVector3f());
                    modelColor = this.getShaded(modelColor, normal);
                }

                blockLight = modelHit.light();

                // no need to keep going if color is opaque
                if ((modelColor >> 24 & 0xff) >= 255) {
                    break;
                }
            }
        }

        if (blockLight) {
            int lightLevel = Math.max(
                    this.level.getBrightness(LightLayer.SKY, lightPos) - this.skyDarken,
                    this.level.getBrightness(LightLayer.BLOCK, lightPos)
            );

            for (int i = 1; i < shadeTint.length; i++) {
                shadeTint[i] = shadeTint[i] * (Mth.clamp(lightLevel + 5, 5, 20) / 20.f);
            }
        }

        // Apply shade tint
        int tintedColor = ColorHelper.multiplyColor(modelColor, ColorHelper.packColor(shadeTint));
        return tintedColor;
    }

    public int getBlurredBiomeWaterOrBlockColor(BlockPos blockPos, BlockState blockState, int radius) {
        if (radius == 0) {
            if (blockState == null)
                return BlockColors.biomeWaterColor(this.blockIterator.getChunkAt(blockPos), blockPos);
            else
                return BlockColors.get(this.blockIterator.getChunkAt(blockPos), blockState, blockPos);
        }

        int aSum = 0, rSum = 0, gSum = 0, bSum = 0;
        int count = 0;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                BlockPos b = blockPos.offset(dx,0, dy);

                int argb;
                if (blockState == null)
                    argb = BlockColors.biomeWaterColor(this.blockIterator.getChunkAt(b), b);
                else
                    argb = BlockColors.get(this.blockIterator.getChunkAt(b), blockState, b);

                int alpha = (argb >> 24) & 0xFF;
                int red = (argb >> 16) & 0xFF;
                int green = (argb >> 8) & 0xFF;
                int blue = argb & 0xFF;

                // Sum up the components
                aSum += alpha;
                rSum += red;
                gSum += green;
                bSum += blue;
                count++;
            }
        }

        // Calculate average color components
        int avgAlpha = aSum / count;
        int avgRed = rSum / count;
        int avgGreen = gSum / count;
        int avgBlue = bSum / count;

        // Combine the averaged ARGB components back into a single integer
        return (avgAlpha << 24) | (avgRed << 16) | (avgGreen << 8) | avgBlue;
    }


    private List<RenderModel> createOrGetCached(BlockState blockState, List<RPModel.View> views) {
        if (renderModelCache.containsKey(blockState)) {
            return renderModelCache.get(blockState);
        }
        else {
            List<RenderModel> list = renderModelCache.computeIfAbsent(blockState, k -> new ObjectArrayList<>());
            for (int i = 0; i < views.size(); i++) {
                var model = new QuadModel(views.get(i));
                list.add(model);
            }
            return list;
        }
    }

    private RenderModel createOrGetCached(int fluidLevel, RPModel.View view) {
        if (fluidRenderModelCache.containsKey(fluidLevel)) {
            return fluidRenderModelCache.get(fluidLevel);
        }
        else {
            var model = new QuadModel(view);
            fluidRenderModelCache.put(fluidLevel, model);
            return model;
        }
    }

    private RenderModel createOrGetCached(RPModel.View view) {
        /*
        TriangleModel model = this.entityRenderModelCache.get(id);
        if (model != null) {
            return model;
        }
        */

        var model = new QuadModel(view);
        //this.entityRenderModelCache.put(id, model);
        return model;

    }

    private List<RenderModel> getBlockRenderModels(List<RPModel.View> views, BlockIterator.WorldHit result, boolean allowWater) {
        List<RenderModel> renderModels = new ReferenceArrayList<>();
        if (views == null || views.isEmpty()) return renderModels;

        if (allowWater && result.isWaterOrWaterlogged() && !result.blockState().is(Blocks.WATER)) {
            RPModel.View lm = BuiltinModels.liquidModel(result.fluidState(), result.fluidStateAbove());
            renderModels.add(createOrGetCached(result.fluidState().getAmount() + (result.fluidState().getType() == result.fluidStateAbove().getType() ? 1:0) + (result.fluidState().is(FluidTags.LAVA) ? 100:0), lm));
        }

        renderModels.addAll(createOrGetCached(result.blockState(), views));

        return renderModels;
    }

    private int getShaded(int color, Vector3f normal) {
        var pc = ColorHelper.unpackColor(color);
        float b = Math.max(0, normal.dot(SUN));
        for (int i = 1; i < pc.length; i++) {
            pc[i] = (pc[i] * (b / 3.f + 0.7f)); // scale from 0.7 to 1
        }

        return ColorHelper.packColor(pc);
    }

    private int skyColorWithClouds(Vec3 pos, Vec3 direction) {
        List<RenderModel.ModelHit> hits = localSkyModel.intersect(pos.toVector3f(), direction.toVector3f().mul(this.distance), new Vector3f((int)pos.x(), 0, (int)pos.z), 0);
        var color = 0;
        if (!hits.isEmpty()) {
            // test if cloud was hit (transparent if not)
            if ((hits.getFirst().color() >> 24 & 0xff) > 0)
                color = ColorHelper.alphaComposite(color, (hits.getFirst().color() & 0x00_ff_ff_ff) | 0x44_00_00_00);
        }

        float darkness = this.skyDarken / 12.f;
        var skyColor = ColorHelper.alphaComposite(ARGB.color((int)(darkness*255), 0,0,0), MiscColors.SKY_COLOR);

        return ColorHelper.alphaComposite(color, skyColor);
    }

    public static void clearCache() {
        renderModelCache.clear();
        fluidRenderModelCache.clear();
    }
}
