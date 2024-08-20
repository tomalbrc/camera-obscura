package de.tomalbrc.cameraobscura.render;

import de.tomalbrc.cameraobscura.ModConfig;
import de.tomalbrc.cameraobscura.color.BlockColors;
import de.tomalbrc.cameraobscura.color.MiscColors;
import de.tomalbrc.cameraobscura.render.model.RenderModel;
import de.tomalbrc.cameraobscura.render.model.resource.RPModel;
import de.tomalbrc.cameraobscura.render.model.triangle.TriangleModel;
import de.tomalbrc.cameraobscura.util.BuiltinEntityModels;
import de.tomalbrc.cameraobscura.util.BuiltinModels;
import de.tomalbrc.cameraobscura.util.ColorHelper;
import de.tomalbrc.cameraobscura.util.RPHelper;
import de.tomalbrc.cameraobscura.world.BlockIterator;
import de.tomalbrc.cameraobscura.world.EntityIterator;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.FastColor;
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

    private final Map<BlockState, List<RenderModel>> renderModelCache = new Reference2ObjectArrayMap<>();
    private final Map<UUID, RenderModel> entityRenderModelCache = new Reference2ObjectArrayMap<>();

    private final BlockIterator blockIterator;
    private final EntityIterator entityIterator;

    private final int distance;

    private final int skyDarken;

    private TriangleModel localSkyModel;

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
        // Color change for liquids
        double[] shadeTint = new double[]{1, 1, 1, 1};
        boolean blockLight = true;

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
                rpModel = BuiltinModels.shulkerModel(blockState, Optional.ofNullable(ShulkerBoxBlock.getColorFromBlock(result.blockState().getBlock())));
            else if (blockState.is(BlockTags.BEDS))
                rpModel = BuiltinModels.bedModel(blockState, Optional.ofNullable(((BedBlock)blockState.getBlock()).getColor()));
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

            LevelChunk chunk = this.blockIterator.getChunkAt(blockPos); // get cached chunk
            int blockColor = result.isWaterOrWaterlogged() ? BlockColors.biomeWaterColor(chunk, blockPos) : BlockColors.get(chunk, blockState, blockPos);

            List<RenderModel.ModelHit> hits = new ObjectArrayList<>();



            for (int i = 0; i < renderModels.size(); i++) {
                hits.addAll(renderModels.get(i).intersect(pos.toVector3f(), direction.toVector3f(), blockPos.getCenter().toVector3f(), blockColor));
            }

            if (ModConfig.getInstance().renderEntities) {
                for (EntityIterator.EntityHit hit : entityHits) {
                    RPModel.View view = BuiltinEntityModels.getModel(hit.type(), blockPos.getCenter().toVector3f().sub(hit.position()).add(0, -0.5f, 0), hit.rotation(), hit.uuid(), hit.data());
                    TriangleModel entityModel = new TriangleModel(view);
                    hits.addAll(entityModel.intersect(pos.toVector3f(), direction.toVector3f(), blockPos.getCenter().toVector3f(), blockColor));
                }
            }

            hits.sort(Comparator.comparingDouble(RenderModel.ModelHit::t));

            for (int i = 0; i < hits.size(); i++) {
                RenderModel.ModelHit modelHit = hits.get(i);

                if (modelHit.direction() != null && blockState.isSolidRender(this.level, result.blockPos()))
                    lightPos = result.blockPos().relative(modelHit.direction());

                modelColor = ColorHelper.alphaComposite(modelColor, modelHit.color());

                // shading from a global light source
                if (modelHit.shade() && modelHit.direction() != null) {
                    Vector3f normal = new Vector3f(modelHit.direction().getNormal().getX(), modelHit.direction().getNormal().getY(), modelHit.direction().getNormal().getZ());
                    modelColor = this.getShaded(modelColor, normal);
                }
                else {
                    blockLight = false;
                }

                // no need to keep going if color is opaque
                if ((modelColor >> 24 & 0xff) >= 255) {
                    break;
                } else {
                    blockLight = true;
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

    private List<RenderModel> createOrGetCached(BlockState blockState, List<RPModel.View> views) {
        if (this.renderModelCache.containsKey(blockState)) {
            return this.renderModelCache.get(blockState);
        }
        else {
            List<RenderModel> list = this.renderModelCache.get(blockState);
            if (list == null) {
                list = new ObjectArrayList();
                this.renderModelCache.put(blockState, list);
            }

            for (int i = 0; i < views.size(); i++) {
                var model = new TriangleModel(views.get(i));
                list.add(model);
            }
            return list;
        }
    }

    private RenderModel createOrGetCached(UUID entityUUID, RPModel.View view) {
        if (this.entityRenderModelCache.containsKey(entityUUID)) {
            return this.entityRenderModelCache.get(entityUUID);
        }
        else {
            var model = new TriangleModel(view);
            this.entityRenderModelCache.put(entityUUID, model);
            return model;
        }
    }

    private List<RenderModel> getBlockRenderModels(List<RPModel.View> views, BlockIterator.WorldHit result, boolean allowWater) {
        List<RenderModel> renderModels = new ReferenceArrayList<>();
        if (views == null || views.isEmpty()) return renderModels;

        if (allowWater && result.isWaterOrWaterlogged() && !result.blockState().is(Blocks.WATER)) {
            RPModel.View lm = BuiltinModels.liquidModel(result.fluidState(), result.fluidStateAbove());
            views.add(lm);
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
        if (localSkyModel == null) {
            RPModel.View modelView = BuiltinModels.skyModel(pos);
            localSkyModel = new TriangleModel(modelView);
        }
        List<RenderModel.ModelHit> hits = localSkyModel.intersect(pos.toVector3f(), direction.toVector3f().mul(this.distance), new Vector3f((int)pos.x(), 0, (int)pos.z), 0);
        var color = 0;
        if (hits.size() >= 1) {
            // test if cloud was hit (transparent if not)
            if ((hits.get(0).color() >> 24 & 0xff) > 0)
                color = ColorHelper.alphaComposite(color, (hits.get(0).color() & 0x00_ff_ff_ff) | 0x44_00_00_00);
        }

        float darkness = this.skyDarken / 12.f;
        var skyColor = ColorHelper.alphaComposite(FastColor.ARGB32.color((int)(darkness*255), 0,0,0), MiscColors.SKY_COLOR);

        return ColorHelper.alphaComposite(color, skyColor);
    }
}
