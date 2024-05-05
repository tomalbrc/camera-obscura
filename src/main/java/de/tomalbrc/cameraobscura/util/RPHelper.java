package de.tomalbrc.cameraobscura.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.tomalbrc.cameraobscura.json.VariantDeserializer;
import de.tomalbrc.cameraobscura.json.Vector3fDeserializer;
import de.tomalbrc.cameraobscura.json.Vector4iDeserializer;
import de.tomalbrc.cameraobscura.render.RPBlockState;
import de.tomalbrc.cameraobscura.render.model.resource.RPModel;
import de.tomalbrc.cameraobscura.render.model.resource.state.MultipartDefinition;
import de.tomalbrc.cameraobscura.render.model.resource.state.Variant;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.joml.Vector3f;
import org.joml.Vector4i;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class RPHelper {
    private static ResourcePackBuilder resourcePackBuilder = PolymerResourcePackUtils.createBuilder(Path.of("a/b"));

    // Cache resourcepack models
    private static Map<String, RPModel> modelResources = new Object2ObjectOpenHashMap<>();
    private static Map<String, RPBlockState> blockStateResources = new Reference2ObjectArrayMap<>();

    private static Map<String, BufferedImage> textureCache = new Object2ObjectArrayMap<>();


    final private static Gson gson = new GsonBuilder()
            .registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
            .registerTypeAdapter(Variant.class, new VariantDeserializer())
            .registerTypeAdapter(Vector3f.class, new Vector3fDeserializer())
            .registerTypeAdapter(Vector4i.class, new Vector4iDeserializer())
            .create();

    public static void clearCache() {
        modelResources.clear();
        blockStateResources.clear();
        textureCache.clear();
    }

    public static RPBlockState loadBlockState(String path) {
        if (blockStateResources.containsKey(path)) {
            return blockStateResources.get(path);
        }

        byte[] data = resourcePackBuilder.getDataOrSource("assets/minecraft/blockstates/" + path + ".json");
        var resource = gson.fromJson(new InputStreamReader(new ByteArrayInputStream(data)), RPBlockState.class);
        blockStateResources.put(path, resource);
        return resource;
    }

    public static RPModel.View loadModel(String path, Vector3f blockRotation) {
        if (modelResources.containsKey(path)) {
            return new RPModel.View(modelResources.get(path), blockRotation);
        }

        byte[] data = resourcePackBuilder.getDataOrSource("assets/minecraft/models/" + path + ".json");
        if (data != null) {
            RPModel model = gson.fromJson(new InputStreamReader(new ByteArrayInputStream(data)), RPModel.class);
            modelResources.put(path, model);
            return new RPModel.View(model, blockRotation);
        }
        return null;
    }

    public static byte[] loadTexture(String path) {
        byte[] data = resourcePackBuilder.getDataOrSource("assets/minecraft/textures/" + path + ".png");
        return data;
    }

    public static BufferedImage loadTextureImage(String path) {
        if (textureCache.containsKey(path)) {
            return textureCache.get(path);
        }

        byte[] data = resourcePackBuilder.getDataOrSource("assets/minecraft/textures/" + path + ".png");
        BufferedImage img = null;

        try {
            img = ImageIO.read(new ByteArrayInputStream(data));
            if (img.getType() == 10) {
                img = TextureHelper.darkenGrayscale(img);
            }
            textureCache.put(path, img);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return img;
    }

    public static List<RPModel.View> loadModel(RPBlockState rpBlockState, BlockState blockState) {
        if (rpBlockState != null && rpBlockState.variants != null) {
            for (var entry: rpBlockState.variants.entrySet()) {
                boolean matches = true;
                if (!entry.getKey().isEmpty()) {
                    try {
                        var blockResult = BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK.asLookup(), String.format("%s[%s]", BuiltInRegistries.BLOCK.getKey(blockState.getBlock()).getPath(), entry.getKey()), false);

                        for (Map.Entry<Property<?>, Comparable<?>> propertyComparableEntry : blockResult.properties().entrySet()) {
                            if (!blockState.getValue(propertyComparableEntry.getKey()).equals(propertyComparableEntry.getValue())) {
                                matches = false;
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }

                if (entry.getKey().isEmpty() || matches) {
                    var model = RPHelper.loadModel(entry.getValue().model.getPath(), new Vector3f(entry.getValue().x, entry.getValue().y, entry.getValue().z));
                    return ObjectArrayList.of(model);
                }
            }
        } else if (rpBlockState != null && rpBlockState.multipart != null) {
            ObjectArrayList<RPModel.View> list = new ObjectArrayList<>();

            for (int i = 0; i < rpBlockState.multipart.size(); i++) {
                MultipartDefinition mp = rpBlockState.multipart.get(i);

                Variant mpVar = mp.get(blockState);
                var modelPath = mpVar.model;

                var model = RPHelper.loadModel(modelPath.getPath(), new Vector3f(mp.apply.x, mp.apply.y, mp.apply.z));
                list.add(model);
            }
            return list;
        }

        return null;
    }
    public static List<RPModel.View> loadModel(BlockState blockState) {
        String blockName = BuiltInRegistries.BLOCK.getKey(blockState.getBlock()).getPath();
        RPBlockState rpBlockState = RPHelper.loadBlockState(blockName);
        return loadModel(rpBlockState, blockState);
    }
}
