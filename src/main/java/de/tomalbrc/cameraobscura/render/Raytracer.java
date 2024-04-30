package de.tomalbrc.cameraobscura.render;

import de.tomalbrc.cameraobscura.util.ColorHelper;
import de.tomalbrc.cameraobscura.util.RPHelper;
import de.tomalbrc.cameraobscura.world.BlockIterator;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

public class Raytracer {
    static double[] WATER_TINT = new double[] { .3, .3, 1 };
    static double[] LAVA_TINT = new double[] { 1, .3, .3 };

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
        BlockHitResult result =             this.iterator.raycast(new ClipContext(pos, scaledDir, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, CollisionContext.empty()));
        BlockHitResult resultWithLiquids =  this.iterator.raycast(new ClipContext(pos, scaledDir, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, CollisionContext.empty()));
        BlockHitResult resultWithoutTransparent =  this.iterator.raycast(new ClipContext(pos, scaledDir, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, CollisionContext.empty()));

        var c1 = colorFromRaycast(pos, direction, resultWithLiquids, result);
        if (c1 == -1) {
            // cast again without transparent objects. ideally we would have a list or something else to iterate through the hit blocks
            c1 = colorFromRaycast(pos, direction, resultWithLiquids, resultWithoutTransparent);
        }

        return c1 == -1 ? 0 :c1;
    }

    private int colorFromRaycast(Vec3 pos, Vec3 direction, BlockHitResult resultWithLiquids, BlockHitResult result) {
        // Color change for liquids
        boolean lava = false;
        boolean water = false;
        double[] tint = new double[] { 1,1,1 }; // maybe separate for shade?
        boolean transparentWater = true;
        boolean shadows = true;
        if (transparentWater) {
            if (resultWithLiquids != null) {
                var fs = this.level.getBlockState(resultWithLiquids.getBlockPos()).getFluidState();
                if (fs.is(Fluids.WATER) || fs.is(Fluids.FLOWING_WATER)) {
                    tint[0] = WATER_TINT[0];
                    tint[1] = WATER_TINT[1];
                    tint[2] = WATER_TINT[2];
                    water = true;
                }
                if (fs.is(Fluids.LAVA) || fs.is(Fluids.FLOWING_LAVA)) {
                    tint[0] = LAVA_TINT[0];
                    tint[1] = LAVA_TINT[1];
                    tint[2] = LAVA_TINT[2];
                    lava = true;
                }
            }
        }

        if (result != null) {
            int lightLevel = Math.max(this.level.getLightEngine().getRawBrightness(result.getBlockPos().relative(result.getDirection()), 0), (this.level.dimensionType().hasCeiling()?13:water?4:0));

            if (result.getDirection() != Direction.UP) {
                lightLevel -= 2;
                if (result.getDirection() == Direction.EAST || result.getDirection() == Direction.WEST || result.getDirection() == Direction.DOWN) {
                    lightLevel -= 1;
                }
            }
            lightLevel = Mth.clamp(lightLevel, 4,15);

            if (shadows) {
                double shadowLevel = 15.0;
                for(int i = 0; i < tint.length; i++) {
                    tint[i] = tint[i] * (lightLevel / shadowLevel);
                }
            }

            BlockPos blockPos = result.getBlockPos();
            BlockState blockState = level.getBlockState(blockPos);
            MapColor mapColor = blockState.getMapColor(level, blockPos);

            CanvasColor canvasColor = CanvasColor.from(mapColor, MapColor.Brightness.NORMAL);

            int finalColor = canvasColor.getRgbColor();

            if (!blockState.isAir() && !blockState.is(Blocks.WATER) && !blockState.is(Blocks.LAVA)) {
                RPModel rpModel;
                if (!stateModels.containsKey(blockState)) {
                    rpModel = RPHelper.loadModel(blockState);
                    stateModels.put(blockState, rpModel);
                } else {
                    rpModel = stateModels.get(blockState);
                }

                if (rpModel == null) {
                    System.out.println("Could not load model: " + blockState.getBlock().getName().getString());
                } else {
                    int imgData = rpModel.intersect(pos.toVector3f(), direction.toVector3f(), blockPos.getCenter().toVector3f(), blockState);

                    if (blockState.canOcclude() && imgData == -1) {
                        imgData = 0x00ffffff;
                    }
                    else if (imgData == -1 || (imgData>>24 & 0xff) == 0) {
                        // dont allow non-hits or transparent hits -> we will try to use another raycast result for the color
                        return -1;
                    }

                    finalColor = imgData != -1 ? imgData : canvasColor.getRgbColor(); // fallback = mapcolor
//                             finalColor = ColorHelper.multiplyColor(canvasColor.getRgbColor(), imgData);
                }
            }

            int tintedColor = ColorHelper.multiplyColor(finalColor, ColorHelper.packColor(tint));
            if (!transparentWater || lava) {
                tintedColor = ColorHelper.packColor(tint);
            }

            return tintedColor;
        } else if (resultWithLiquids != null) {
            // TODO: lava/water check
            return ColorHelper.packColor(WATER_TINT);
        }

        return -1;
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
