package de.tomalbrc.cameraobscura;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.commands.SetBlockCommand;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Map;

public class Raytracer {
    private static ResourcePackBuilder resourcePackBuilder = null;

    private final Level level;

    final private Gson gson = new GsonBuilder().registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer()).create();

    public Raytracer(Level level) {
        this.level = level;
        if (resourcePackBuilder == null) {
            var p = Path.of("a/b");
            resourcePackBuilder = PolymerResourcePackUtils.createBuilder(p);
        }
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
                String blockName = BuiltInRegistries.BLOCK.getKey(blockState.getBlock()).getPath();
                RPBlockState rpBlockState = loadBlockState(blockName);
                if (rpBlockState != null) {
                    RPModel rpModel = null;
                    for (var entry: rpBlockState.variants.entrySet()) {
                        BlockState state = null;
                        if (!entry.getKey().isEmpty()) {
                            try {
                                state = BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK.asLookup(), entry.getKey(), false).blockState();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        if (entry.getKey().isEmpty() || state == blockState) {
                            rpModel = loadModel(entry.getValue().model.getPath());
                            break;
                        }
                    }

                    Map<String, ResourceLocation> textures = new Object2ObjectOpenHashMap<>();
                    textures.putAll(rpModel.textures);

                    ResourceLocation parent = rpModel.parent;
                    while (parent != null || parent.getPath() == null || !parent.getPath().isEmpty()) {
                        var child =  loadModel(parent.getPath());
                        if (child != null) {
                            Raytracer.resourcePackBuilder.getDataOrSource("assets/minecraft/" + "models/block/" + BuiltInRegistries.BLOCK.getKey(blockState.getBlock()).getPath() + ".json");
                            parent = child.parent;
                        } else {
                            break;
                        }
                    }

                    byte[] data = Raytracer.resourcePackBuilder.getDataOrSource("assets/minecraft/" + "textures/block/" + BuiltInRegistries.BLOCK.getKey(blockState.getBlock()).getPath() + ".png");
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

    private RPBlockState loadBlockState(String path) {
        byte[] data = Raytracer.resourcePackBuilder.getDataOrSource("assets/minecraft/blockstates/" + path + ".json");
        return gson.fromJson(new InputStreamReader(new ByteArrayInputStream(data)), RPBlockState.class);
    }

    private RPModel loadModel(String path) {
        byte[] data = Raytracer.resourcePackBuilder.getDataOrSource("assets/minecraft/models/" + path + ".json");
        return gson.fromJson(new InputStreamReader(new ByteArrayInputStream(data)), RPModel.class);
    }
}
