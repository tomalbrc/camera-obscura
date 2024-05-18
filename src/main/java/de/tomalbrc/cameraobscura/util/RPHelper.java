package de.tomalbrc.cameraobscura.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.tomalbrc.cameraobscura.json.*;
import de.tomalbrc.cameraobscura.render.model.resource.RPBlockState;
import de.tomalbrc.cameraobscura.render.model.resource.RPElement;
import de.tomalbrc.cameraobscura.render.model.resource.RPModel;
import de.tomalbrc.cameraobscura.render.model.resource.state.MultipartDefinition;
import de.tomalbrc.cameraobscura.render.model.resource.state.Variant;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.joml.Vector3f;
import org.joml.Vector4f;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class RPHelper {
    private static ResourcePackBuilder resourcePackBuilder = PolymerResourcePackUtils.createBuilder(Path.of("a/b"));

    // Cache resourcepack models
    private static Map<String, RPModel> modelResources = new Object2ObjectOpenHashMap<>();
    private static Map<String, RPBlockState> blockStateResources = new Object2ObjectOpenHashMap<>();

    private static Map<String, BufferedImage> textureCache = new Object2ObjectOpenHashMap<>();


    final private static Gson gson = new GsonBuilder()
            .registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
            .registerTypeAdapter(Variant.class, new VariantDeserializer())
            .registerTypeAdapter(MultipartDefinition.class, new MultipartDefinitionDeserializer())
            .registerTypeAdapter(MultipartDefinition.Condition.class, new ConditionDeserializer())
            .registerTypeAdapter(Vector3f.class, new Vector3fDeserializer())
            .registerTypeAdapter(Vector4f.class, new Vector4fDeserializer())
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

    public static RPModel.View loadModel(String path, Vector3f blockRotation, boolean uvlock) {
        if (modelResources.containsKey(path)) {
            return new RPModel.View(modelResources.get(path), blockRotation, uvlock);
        }

        byte[] data = resourcePackBuilder.getDataOrSource("assets/minecraft/models/" + path + ".json");
        if (data != null) {
            RPModel model = loadModel(new ByteArrayInputStream(data));
            modelResources.put(path, model);
            return new RPModel.View(model, blockRotation, uvlock);
        }
        return null;
    }

    public static RPModel loadModel(InputStream inputStream) {
        RPModel model = gson.fromJson(new InputStreamReader(inputStream), RPModel.class);
        if (model.elements != null) {
            for (int i = 0; i < model.elements.size(); i++) {
                RPElement element = model.elements.get(i);
                for (Map.Entry<String, RPElement.TextureInfo> stringTextureInfoEntry : element.faces.entrySet()) {
                    if (stringTextureInfoEntry.getValue().uv == null) {
                        stringTextureInfoEntry.getValue().uv = new Vector4f(
                                element.from.x(),
                                element.from.y(),
                                element.to.x(),
                                element.to.y());
                    }
                }
            }
        }
        return model;
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
                        String str = String.format("%s[%s]", BuiltInRegistries.BLOCK.getKey(blockState.getBlock()).getPath(), entry.getKey());
                        BlockStateParser.BlockResult blockResult = BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK.asLookup(), str, false);

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
                    var model = RPHelper.loadModel(entry.getValue().model.getPath(), new Vector3f(entry.getValue().x, entry.getValue().y, entry.getValue().z), entry.getValue().uvlock);
                    return ObjectArrayList.of(model);
                }
            }
        } else if (rpBlockState != null && rpBlockState.multipart != null) {
            ObjectArrayList<RPModel.View> list = new ObjectArrayList<>();

            int num = rpBlockState.multipart.size();
            for (int i = 0; i < num; i++) {
                MultipartDefinition mp = rpBlockState.multipart.get(i);

                if (mp.when == null || mp.when.canApply(blockState)) {
                    for (int applyIndex = 0; applyIndex < mp.apply.size(); applyIndex++) {
                        var apply = mp.apply.get(applyIndex);
                        var model = RPHelper.loadModel(apply.model.getPath(), new Vector3f(apply.x, apply.y, apply.z), apply.uvlock);
                        list.add(model);
                    }
                }
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
