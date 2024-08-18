package de.tomalbrc.cameraobscura.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import de.tomalbrc.cameraobscura.json.*;
import de.tomalbrc.cameraobscura.render.model.resource.RPBlockState;
import de.tomalbrc.cameraobscura.render.model.resource.RPElement;
import de.tomalbrc.cameraobscura.render.model.resource.RPModel;
import de.tomalbrc.cameraobscura.render.model.resource.state.MultipartDefinition;
import de.tomalbrc.cameraobscura.render.model.resource.state.Variant;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.joml.Vector4f;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RPHelper {
    public static ResourcePackBuilder resourcePackBuilder;
    private static final ResourcePackBuilder vanillaBuilder = PolymerResourcePackUtils.createBuilder(Path.of("polymer/camera-obscura"));

    // Cache resourcepack models
    private static final Map<String, RPModel> modelResources = new ConcurrentHashMap<>();
    private static final Map<String, RPBlockState> blockStateResources = new ConcurrentHashMap<>();

    private static final Map<String, BufferedImage> textureCache = new ConcurrentHashMap<>();


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

    public static ResourcePackBuilder getBuilder() {
        return resourcePackBuilder == null ? vanillaBuilder : resourcePackBuilder;
    }

    public static RPBlockState loadBlockState(String path) {
        if (blockStateResources.containsKey(path)) {
            return blockStateResources.get(path);
        }

        byte[] data = getBuilder().getDataOrSource("assets/minecraft/blockstates/" + path + ".json");
        var resource = gson.fromJson(new InputStreamReader(new ByteArrayInputStream(data)), RPBlockState.class);
        blockStateResources.put(path, resource);
        return resource;
    }

    public static RPModel.View loadModelView(String namespace, String path, Vector3f blockRotation, boolean uvlock) {
        return new RPModel.View(loadModel(namespace, path), blockRotation, uvlock);
    }

    public static RPModel loadModel(String namespace, String path) {
        if (modelResources.containsKey(path)) {
            return modelResources.get(path);
        }

        byte[] data = getBuilder().getDataOrSource("assets/"+ namespace +"/models/" + path + ".json");
        if (data != null) {
            RPModel model = loadModelView(new ByteArrayInputStream(data));
            modelResources.put(path, model);
            return model;
        }
        return null;
    }

    public static RPModel loadModelView(InputStream inputStream) {
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
        return getBuilder().getDataOrSource("assets/minecraft/textures/" + path + ".png");
    }

    public static BufferedImage loadTextureImage(String path) {
        if (textureCache.containsKey(path)) {
            return textureCache.get(path);
        }

        LogUtils.getLogger().error("Path "+path);

        if (path.startsWith("d.")) {
            var img = fromBytes(getTexture(path.substring(2)));
            textureCache.put(path, img);

            return img;
        }

        byte[] data = getBuilder().getDataOrSource("assets/minecraft/textures/" + path + ".png");
        BufferedImage img = fromBytes(data);
        textureCache.put(path, img);


        return img;
    }

    private static BufferedImage fromBytes(byte[] data) {
        BufferedImage img = null;
        try {
            img = ImageIO.read(new ByteArrayInputStream(data));
            if (img.getType() == 10) {
                img = TextureHelper.darkenGrayscale(img);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return img;
    }

    @NotNull
    private static byte[] getTexture(String uuid) {
        InputStreamReader inputStreamReader = null;
        try {
            URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
            inputStreamReader = new InputStreamReader(url.openStream());

            JsonObject textureProperty = new JsonParser().parse(inputStreamReader).getAsJsonObject().get("properties").getAsJsonArray().get(0).getAsJsonObject();
            String texture = textureProperty.get("value").getAsString();
            var newJson = new String(Base64.getDecoder().decode(texture));

            var str = new JsonParser().parse(newJson).getAsJsonObject().get("textures").getAsJsonObject().get("SKIN").getAsJsonObject().get("url").getAsString();

            URL textureUrl = new URL(str);
            byte[] bytes = textureUrl.openStream().readAllBytes();

            return bytes;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<RPModel.View> loadModelView(RPBlockState rpBlockState, BlockState blockState) {
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
                    var model = RPHelper.loadModelView(entry.getValue().model.getNamespace(), entry.getValue().model.getPath(), new Vector3f(entry.getValue().x, entry.getValue().y, entry.getValue().z), entry.getValue().uvlock);
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
                        var model = RPHelper.loadModelView(apply.model.getNamespace(), apply.model.getPath(), new Vector3f(apply.x, apply.y, apply.z), apply.uvlock);
                        list.add(model);
                    }
                }
            }
            return list;
        }

        return null;
    }
    public static List<RPModel.View> loadModelView(BlockState blockState) {
        BlockState block = safePolymerBlockState(blockState);

        String blockName = BuiltInRegistries.BLOCK.getKey(block.getBlock()).getPath();
        RPBlockState rpBlockState = RPHelper.loadBlockState(blockName);

        return loadModelView(rpBlockState, block);
    }

    private static BlockState safePolymerBlockState(BlockState blockState) {
        if (blockState.getBlock() instanceof PolymerBlock polymerBlock) {
            blockState = polymerBlock.getPolymerBlockState(blockState);
        }
        return blockState;
    }
}
