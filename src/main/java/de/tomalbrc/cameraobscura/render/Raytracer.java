package de.tomalbrc.cameraobscura.render;

import de.tomalbrc.cameraobscura.render.model.RenderModel;
import de.tomalbrc.cameraobscura.render.model.resource.RPModel;
import de.tomalbrc.cameraobscura.render.model.triangle.TriangleModel;
import de.tomalbrc.cameraobscura.util.BlockColors;
import de.tomalbrc.cameraobscura.util.ColorHelper;
import de.tomalbrc.cameraobscura.util.MiscColors;
import de.tomalbrc.cameraobscura.util.RPHelper;
import de.tomalbrc.cameraobscura.world.BlockIterator;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;

public class Raytracer {
    public record ColorWithDepth(int color, float depth) {}

    private static Vector3f SUN = new Vector3f(-1,2,1).normalize();

    private final Level level;

    private final Map<BlockState, RPModel> stateModels;
    private final Map<BlockState, RenderModel> renderModels;

    private final BlockIterator iterator;

    public Raytracer(Level level) {
        this.level = level;
        this.stateModels = new Reference2ObjectArrayMap<>();
        this.renderModels = new Reference2ObjectArrayMap<>();

        this.iterator = new BlockIterator(level);
    }

    public ColorWithDepth trace(Vec3 pos, Vec3 direction) {
        var scaledDir = new Vec3(direction.x, direction.y, direction.z).scale(128).add(pos);
        List<BlockIterator.WorldHitResult> result = this.iterator.raycast(new ClipContext(pos, scaledDir, null, ClipContext.Fluid.ANY, CollisionContext.empty()));

        int color = 0x00ffffff;
        float depth = 1.f;

        boolean hasHitWater = false; // only get water color once

        for (int i = 0; i < result.size(); i++) {
            boolean waterState = result.get(i).fluidState().is(Fluids.WATER) || result.get(i).fluidState().is(Fluids.FLOWING_WATER);

            if (hasHitWater && waterState && result.get(i).blockState().is(Blocks.WATER))
                continue; // only trace water once, maybe should be done

            hasHitWater |= waterState;

            var rayRes = colorFromRaycast(pos, direction, result.get(i));
            depth = rayRes.depth;

            var c1 = ColorHelper.unpackColor(color);
            var c2 = ColorHelper.unpackColor(rayRes.color);

            color = ColorHelper.packColor(ColorHelper.alphaComposite(c1, c2));

            if ((color >> 24 & 0xff) >= 255)
                return new ColorWithDepth(color, depth);
        }

        if ((color >> 24 & 0xff) < 255) {
            // apply sky and clouds
            var time = (this.level.dayTime()%24000) / 24000.f;
            color = ColorHelper.alphaComposite(color, ColorHelper.interpolateColors(MiscColors.SKY_COLORS, time));
        }

        // color may contain transparency if no sky color was set (or may be black)
        return new ColorWithDepth(color, depth);
    }

    private ColorWithDepth colorFromRaycast(Vec3 pos, Vec3 direction, BlockIterator.WorldHitResult result) {
        // Color change for liquids
        boolean lava = false;
        boolean water = false;
        double[] tint = new double[] { 1, 1,1,1 }; // maybe separate for shade?
        boolean transparentWater = true;
        boolean shadows = true;
        if (transparentWater) {
            if (result.fluidState() != null && !result.fluidState().isEmpty()) {
                var fs = result.fluidState();
                if (fs.is(Fluids.WATER) || fs.is(Fluids.FLOWING_WATER)) {
                    tint[0] = MiscColors.WATER_TINT[0];
                    tint[1] = MiscColors.WATER_TINT[1];
                    tint[2] = MiscColors.WATER_TINT[2];
                    tint[3] = MiscColors.WATER_TINT[3];
                    water = true;
                }
                if (fs.is(Fluids.LAVA) || fs.is(Fluids.FLOWING_LAVA)) {
                    tint[0] = MiscColors.LAVA_TINT[0];
                    tint[1] = MiscColors.LAVA_TINT[1];
                    tint[2] = MiscColors.LAVA_TINT[2];
                    tint[3] = MiscColors.LAVA_TINT[3];
                    lava = true;
                }
            }
        }

        BlockPos blockPos = result.blockPos();
        BlockState blockState = this.level.getBlockState(blockPos);
        MapColor mapColor = blockState.getMapColor(this.level, blockPos);

        CanvasColor canvasColor = CanvasColor.from(mapColor, MapColor.Brightness.NORMAL);

        float depth = (float)pos.distanceTo(new Vec3(result.blockPos().getX(), result.blockPos().getY(), result.blockPos().getZ()));
        int modelColor = canvasColor.getRgbColor();

        BlockPos lightPos = result.blockPos();
        if (!blockState.isAir() && !blockState.is(Blocks.WATER) && !blockState.is(Blocks.LAVA)) {
            RPModel rpModel;
            if (!this.stateModels.containsKey(blockState)) {
                rpModel = RPHelper.loadModel(blockState);
                this.stateModels.put(blockState, rpModel);
            } else {
                rpModel = this.stateModels.get(blockState);
            }

            if (rpModel == null) {
                System.out.println("Could not load or find model: " + blockState.getBlock().getName().getString());
            } else {
                RenderModel renderModel;
                if (!this.renderModels.containsKey(blockState)) {
                    renderModel = new TriangleModel(rpModel);
                    this.renderModels.put(blockState, renderModel);
                } else {
                    renderModel = this.renderModels.get(blockState);
                }

                int blockColor = BlockColors.get(this.level, blockState, blockPos);

                RenderModel.ModelHitResult modelHitResult = renderModel.intersect(pos.toVector3f(), direction.toVector3f(), blockPos.getCenter().toVector3f(), blockColor);
                if (modelHitResult != null) {
                    if (modelHitResult.direction() != null && blockState.isSolidRender(this.level, result.blockPos()))
                        lightPos = result.blockPos().relative(modelHitResult.direction());

                    modelColor = modelHitResult.color();

                    depth = modelHitResult.t();

                    // some shading from a global light source
                    var normal = new Vector3f(modelHitResult.direction().getNormal().getX(), modelHitResult.direction().getNormal().getY(), modelHitResult.direction().getNormal().getZ());
                    float b = Math.max(0, normal.dot(SUN));
                    for(int i = 1; i < tint.length; i++) {
                        tint[i] = tint[i] * (b/3.f+0.7f); // scale from 0.75 to 1.03333
                    }
                }
            }
        }

        if (shadows) {
            float lightLevel = this.level.getBrightness(LightLayer.BLOCK, lightPos);
            //var time = (this.level.dayTime()%24000) / 24000.f;
            lightLevel = Mth.clamp(lightLevel, Math.max(Math.max(2, (int)(level.getTimeOfDay(0) * 14)), (int)(this.level.dimensionType().ambientLight()*15)),15);

            for(int i = 1; i < tint.length; i++) {
                tint[i] = tint[i] * (lightLevel / 15.f);
            }
        }

        // Apply tint
        int tintedColor;
        if (!transparentWater || lava || blockState.is(Blocks.WATER)) {
            tintedColor = ColorHelper.packColor(tint);
        } else {
            tintedColor = ColorHelper.packColor(
                    ColorHelper.multiplyColor(ColorHelper.unpackColor(modelColor), tint)
            );
        }

        return new ColorWithDepth(tintedColor, depth/128);
    }
}
