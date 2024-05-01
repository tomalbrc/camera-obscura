package de.tomalbrc.cameraobscura.render;

import de.tomalbrc.cameraobscura.render.model.RPModel;
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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Raytracer {
    private final Level level;

    private final Map<BlockState, RPModel> stateModels;

    private final BlockIterator iterator;

    BufferedImage GRASS_TEXTURE;
    BufferedImage FOLIAGE_TEXTURE;

    public Raytracer(Level level) {
        this.level = level;
        this.stateModels = new Reference2ObjectArrayMap<>();

        this.loadColorMaps();

        this.iterator = new BlockIterator(level);
    }

    public int trace(Vec3 pos, Vec3 direction) {
        var scaledDir = new Vec3(direction.x, direction.y, direction.z).scale(128).add(pos);
        List<BlockIterator.WorldHitResult> result = this.iterator.raycast(new ClipContext(pos, scaledDir, null, null, CollisionContext.empty()));

        int color = 0x00ffffff;

        boolean hasHitWater = false; // only get water color once

        for (int i = 0; i < result.size(); i++) {
            boolean waterState = result.get(i).fluidState().is(Fluids.WATER) || result.get(i).fluidState().is(Fluids.FLOWING_WATER);

            if (hasHitWater && waterState)
                continue; // only trace water once, maybe should be done

            hasHitWater |= waterState;

            var c1 = ColorHelper.unpackColor(color);
            var c2 = ColorHelper.unpackColor(colorFromRaycast(pos, direction, result.get(i)));

            color = ColorHelper.packColor(ColorHelper.alphaComposite(c1, c2));

            if ((color >> 24 & 0xff) >= 255)
                return color;
        }


        if ((color >> 24 & 0xff) < 255) {
            // apply sky and clouds
            var time = (this.level.dayTime()%24000) / 24000.f;
            color = ColorHelper.alphaComposite(color, ColorHelper.interpolateColors(MiscColors.SKY_COLORS, time));
        }


        // color may contain transparency if no sky color was set (or may be black)
        return color;
    }

    private int colorFromRaycast(Vec3 pos, Vec3 direction, BlockIterator.WorldHitResult result) {
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
        BlockState blockState = level.getBlockState(blockPos);
        MapColor mapColor = blockState.getMapColor(level, blockPos);

        CanvasColor canvasColor = CanvasColor.from(mapColor, MapColor.Brightness.NORMAL);

        int finalColor = canvasColor.getRgbColor();
        if (water) {
            finalColor |= 0xff_000000;
        }

        BlockPos lightPos = result.blockPos();
        if (!blockState.isAir() && !water && !blockState.is(Blocks.LAVA)) {
            RPModel rpModel;
            if (!this.stateModels.containsKey(blockState)) {
                rpModel = RPHelper.loadModel(blockState);
                this.stateModels.put(blockState, rpModel);
            } else {
                rpModel = stateModels.get(blockState);
            }

            if (rpModel == null) {
                System.out.println("Could not load or find model: " + blockState.getBlock().getName().getString());
            } else {
                RPModel.ModelHitResult modelHitResult = rpModel.intersect(pos.toVector3f(), direction.toVector3f(), blockPos.getCenter().toVector3f(), blockState);
                if (modelHitResult.direction() != null && blockState.isSolidRender(level, result.blockPos()))
                    lightPos = result.blockPos().relative(modelHitResult.direction());
                finalColor = modelHitResult.color();
            }
        }

        if (shadows && !water) {
            float lightLevel = this.level.getBrightness(LightLayer.BLOCK, lightPos);
            lightLevel = Mth.clamp(lightLevel, Math.max(Math.max(2, (int)(this.level.getTimeOfDay(0) * 13)), (int)(this.level.dimensionType().ambientLight()*15)),15);

            for(int i = 1; i < tint.length; i++) {
                tint[i] = tint[i] * (lightLevel / 15.f);
            }
        }

        // Apply tint
        int tintedColor;
        if (!transparentWater || lava) {
            tintedColor = ColorHelper.packColor(tint);
        } else {
            tintedColor = ColorHelper.packColor(
                    ColorHelper.multiplyColor(ColorHelper.unpackColor(finalColor), tint)
            );
        }

        return tintedColor;
    }


    private void loadColorMaps() {
        try {
            GRASS_TEXTURE = ImageIO.read(new ByteArrayInputStream(RPHelper.loadTexture("colormap/grass")));
            FOLIAGE_TEXTURE = ImageIO.read(new ByteArrayInputStream(RPHelper.loadTexture("colormap/foliage")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
