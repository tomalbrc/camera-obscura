package de.tomalbrc.cameraobscura.render;

import de.tomalbrc.cameraobscura.util.ColorHelper;
import de.tomalbrc.cameraobscura.util.RPHelper;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
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

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

public class Raytracer {

    private final Level level;

    public Raytracer(Level level) {
        this.level = level;

    }

    public CanvasColor trace(Vec3 pos, Vec3 direction) throws IOException {
        BlockHitResult result =             this.level.clip(new ClipContext(pos, direction, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, CollisionContext.empty()));
        BlockHitResult resultWithLiquids =  this.level.clip(new ClipContext(pos, direction, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, CollisionContext.empty()));

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
                    tint = new double[] { .3, .3, 1 };
                    water = true;
                }
                if (fs.is(Fluids.LAVA) || fs.is(Fluids.FLOWING_LAVA)) {
                    tint = new double[] { 1, .3, .3 };
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
            MapColor col;
            col = blockState.getMapColor(level, result.getBlockPos());

            CanvasColor cc = CanvasColor.from(col, MapColor.Brightness.NORMAL);

            int finalColor = cc.getRgbColor();

            if (!blockState.isAir() && !blockState.is(Blocks.WATER) && !blockState.is(Blocks.LAVA) && !(blockState.getBlock() instanceof BaseEntityBlock)) {
                RPModel rpModel = RPHelper.loadModel(blockState);

                if (rpModel == null) {
                    System.out.println("Could not load model: " + blockState.getBlock().getName().getString());
                } else {
                    Map<String, ResourceLocation> textures = rpModel.collectTextures();

                    // TODO: find ray intersection in model geometry aka cubes

                    byte[] data = RPHelper.loadTexture(textures.values().iterator().next().getPath());
                    if (data != null) {
                        var out = new ByteArrayInputStream(data);
                        var img = ImageIO.read(out);

                        var imgData = img.getRGB(level.random.nextInt(0, 15), level.random.nextInt(0, 15));
                        if (img.getColorModel().getPixelSize() == 8) {
                            finalColor = ColorHelper.multiplyColor(cc.getRgbColor(), imgData);
                        } else {
                            finalColor = imgData;
                        }
                    }
                }
            }

            int col2 = ColorHelper.multiplyColor(finalColor, ColorHelper.packColor(tint));
            if (!transparentWater || lava) {
                col2 = ColorHelper.packColor(tint);
            }

            return CanvasUtils.findClosestColor(col2);
        } else if (resultWithLiquids != null) {
            var col = level.getBlockState(resultWithLiquids.getBlockPos()).getMapColor(level, result.getBlockPos());
            return CanvasColor.from(col, MapColor.Brightness.NORMAL);
        }

        return CanvasColor.YELLOW_HIGH;
    }
}
