package de.tomalbrc.cameraobscura.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import xyz.nucleoid.packettweaker.PacketContext;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    private static final Map<ResourceLocation, RPModel> modelResources = new ConcurrentHashMap<>();
    private static final Map<BlockState, RPBlockState> blockStateResources = new ConcurrentHashMap<>();

    private static final Map<ResourceLocation, BufferedImage> textureCache = new ConcurrentHashMap<>();

    final public static Gson gson = new GsonBuilder()
            .registerTypeAdapter(ResourceLocation.class, new CachedResourceLocationDeserializer())
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

    public static RPBlockState loadBlockState(BlockState blockState) {
        if (blockStateResources.containsKey(blockState)) {
            return blockStateResources.get(blockState);
        }

        ResourceLocation location = BuiltInRegistries.BLOCK.getKey(blockState.getBlock());
        byte[] data = getBuilder().getDataOrSource("assets/" + location.getNamespace() + "/blockstates/" + location.getPath() + ".json");
        var resource = gson.fromJson(new InputStreamReader(new ByteArrayInputStream(data)), RPBlockState.class);
        blockStateResources.put(blockState, resource);
        return resource;
    }

    public static RPModel.View loadModelView(ResourceLocation resourceLocation, Vector3fc blockRotation, boolean uvlock) {
        return new RPModel.View(loadModel(resourceLocation), blockRotation, uvlock);
    }

    public static RPModel loadModel(ResourceLocation resourceLocation) {
        if (modelResources.containsKey(resourceLocation)) {
            return modelResources.get(resourceLocation);
        }

        byte[] data = getBuilder().getDataOrSource("assets/"+ resourceLocation.getNamespace() +"/models/" + resourceLocation.getPath() + ".json");
        if (data != null) {
            RPModel model = loadModel(new ByteArrayInputStream(data));
            modelResources.put(resourceLocation, model);
            return model;
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

    public static byte[] loadTextureBytes(ResourceLocation path) {
        return getBuilder().getDataOrSource("assets/" + path.getNamespace() + "/textures/" + path.getPath() + ".png");
    }

    public static BufferedImage loadTextureImage(ResourceLocation path) {
        if (textureCache.containsKey(path)) {
            return textureCache.get(path);
        }

        if (path.getNamespace().equals(Constants.DYNAMIC_PLAYER_TEXTURE)) {
            var img = imageFromBytes(getPlayerTexture(path.getPath()));
            textureCache.put(path, img);
            return img;
        } else if (path.getNamespace().equals(Constants.DYNAMIC_SIGN_TEXTURE)) {
            var img = imageFromBytes(getPlayerTexture(path.getPath()));
            textureCache.put(path, img);
            return img;
        } else if (path.getNamespace().equals(Constants.DYNAMIC_MAP_TEXTURE)) {
            var img = imageFromBytes(getPlayerTexture(path.getPath()));
            textureCache.put(path, img);
            return img;
        }

        byte[] data = loadTextureBytes(path);
        BufferedImage img = imageFromBytes(data);
        textureCache.put(path, img);


        return img;
    }

    private static BufferedImage imageFromBytes(byte[] data) {
        BufferedImage img = null;
        try {
            img = ImageIO.read(new ByteArrayInputStream(data));
            if (img.getType() == 10) {
                img = TextureHelper.darkenGrayscale(img);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return img;
    }

    @NotNull
    private static byte[] getPlayerTexture(String uuid) {
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

    public static List<RPModel.View> loadModel(RPBlockState rpBlockState, BlockState blockState) {
        if (rpBlockState != null && rpBlockState.variants != null) {
            for (Map.Entry<String, Variant> entry: rpBlockState.variants.entrySet()) {
                boolean matches = true;
                if (!entry.getKey().isEmpty()) {
                    try {
                        String str = String.format("%s[%s]", BuiltInRegistries.BLOCK.getKey(blockState.getBlock()).getPath(), entry.getKey());
                        BlockStateParser.BlockResult blockResult = BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK, str, false);

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
                    var model = RPHelper.loadModelView(entry.getValue().model, new Vector3f(entry.getValue().x, entry.getValue().y, entry.getValue().z), entry.getValue().uvlock);
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
                        var model = RPHelper.loadModelView(apply.model, new Vector3f(apply.x, apply.y, apply.z), apply.uvlock);
                        list.add(model);
                    }
                }
            }
            return list;
        }

        return null;
    }

    // returning a list for multipart blocks like multiple vines/lichen in a block
    public static List<RPModel.View> loadBlockModelViews(BlockState blockState) {
        BlockState block = safePolymerBlockState(blockState);
        RPBlockState rpBlockState = RPHelper.loadBlockState(block);
        return loadModel(rpBlockState, block);
    }

    public static RPModel loadItemModel(ItemStack itemStack) {
        ResourceLocation resourceLocation = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
        return loadModel(resourceLocation.withPath("item/"+resourceLocation.getPath()));
    }

    private static BlockState safePolymerBlockState(BlockState blockState) {
        if (blockState.getBlock() instanceof PolymerBlock polymerBlock) {
            blockState = polymerBlock.getPolymerBlockState(blockState, PacketContext.get());
        }
        return blockState;
    }
}
