package de.tomalbrc.cameraobscura.render;

import de.tomalbrc.cameraobscura.util.ColorHelper;
import de.tomalbrc.cameraobscura.util.RPHelper;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.joml.Vector3f;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Raytracer {
    static double[] WATER_TINT = new double[] { .3, .3, 1 };
    static double[] LAVA_TINT = new double[] { 1, .3, .3 };

    private final Level level;

    public Raytracer(Level level) {
        this.level = level;

    }

    public int trace(Vec3 pos, Vec3 direction) throws IOException {
        var scaledDir = new Vec3(direction.x, direction.y, direction.z).scale(128).add(pos);
        BlockHitResult result =             this.level.clip(new ClipContext(pos, scaledDir, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, CollisionContext.empty()));
        BlockHitResult resultWithLiquids =  this.level.clip(new ClipContext(pos, scaledDir, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, CollisionContext.empty()));

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
                    tint = WATER_TINT;
                    water = true;
                }
                if (fs.is(Fluids.LAVA) || fs.is(Fluids.FLOWING_LAVA)) {
                    tint = LAVA_TINT;
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

            BlockState blockState = level.getBlockState(result.getBlockPos());
            MapColor mapColor = blockState.getMapColor(level, result.getBlockPos());

            CanvasColor canvasColor = CanvasColor.from(mapColor, MapColor.Brightness.NORMAL);

            int finalColor = canvasColor.getRgbColor();

            if (!blockState.isAir() && !blockState.is(Blocks.WATER) && !blockState.is(Blocks.LAVA)) {
                RPModel rpModel = RPHelper.loadModel(blockState);

                if (rpModel == null) {
                    System.out.println("Could not load model: " + blockState.getBlock().getName().getString());
                } else {
                    //Map<String, ResourceLocation> textures = rpModel.collectTextures();

                    // TODO: find ray intersection in model geometry aka cubes
                    int imgData = rpModel.intersect(pos.toVector3f(), direction.toVector3f(), result.getBlockPos().getCenter().toVector3f());
                    finalColor = imgData != -1 ? imgData : canvasColor.getRgbColor(); // fallback = mapcolor
//                             finalColor = ColorHelper.multiplyColor(canvasColor.getRgbColor(), imgData);
                }
            }

            int col2 = ColorHelper.multiplyColor(finalColor, ColorHelper.packColor(tint));
            if (!transparentWater || lava) {
                col2 = ColorHelper.packColor(tint);
            }

            return col2;
        } else if (resultWithLiquids != null) {
            // TODO: lava/water check
            return ColorHelper.packColor(WATER_TINT);
        }

        return -1;
    }
}
