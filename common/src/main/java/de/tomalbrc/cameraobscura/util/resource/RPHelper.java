package de.tomalbrc.cameraobscura.util.resource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import de.tomalbrc.cameraobscura.Components;
import de.tomalbrc.cameraobscura.command.PresetManager;
import de.tomalbrc.cameraobscura.json.CachedIdentifierDeserializer;
import de.tomalbrc.cameraobscura.json.ConditionDeserializer;
import de.tomalbrc.cameraobscura.json.MultipartDefinitionDeserializer;
import de.tomalbrc.cameraobscura.json.VariantDeserializer;
import de.tomalbrc.cameraobscura.model.resource.RPBlockState;
import de.tomalbrc.cameraobscura.model.resource.RPElement;
import de.tomalbrc.cameraobscura.model.resource.RPModel;
import de.tomalbrc.cameraobscura.model.resource.state.MultipartDefinition;
import de.tomalbrc.cameraobscura.model.resource.state.Variant;
import de.tomalbrc.cameraobscura.platform.AssetFetcher;
import de.tomalbrc.cameraobscura.platform.Platforms;
import de.tomalbrc.cameraobscura.renderer.ChunkMeshCache;
import de.tomalbrc.cameraobscura.renderer.EntityRenderers;
import de.tomalbrc.cameraobscura.renderer.entity.ItemStackRenderer;
import de.tomalbrc.cameraobscura.sore.Texture;
import de.tomalbrc.cameraobscura.util.BuiltinEntityModels;
import de.tomalbrc.cameraobscura.util.Constants;
import de.tomalbrc.cameraobscura.util.ImageUtils;
import de.tomalbrc.cameraobscura.util.SimpleCodecDeserializer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RPHelper {
    public final static Map<Identifier, Texture> textureCache2 = new Reference2ObjectArrayMap<>();
    final public static Gson gson = new GsonBuilder()
            .registerTypeAdapter(Identifier.class, new CachedIdentifierDeserializer())
            .registerTypeAdapter(Variant.class, new VariantDeserializer())
            .registerTypeAdapter(MultipartDefinition.class, new MultipartDefinitionDeserializer())
            .registerTypeAdapter(MultipartDefinition.Condition.class, new ConditionDeserializer())
            .registerTypeAdapter(Vector3f.class, new SimpleCodecDeserializer<>(ExtraCodecs.VECTOR3F))
            .registerTypeAdapter(Vector4f.class, new SimpleCodecDeserializer<>(ExtraCodecs.VECTOR4F))
            .registerTypeAdapter(Direction.class, new SimpleCodecDeserializer<>(Direction.CODEC))
            .registerTypeAdapter(ItemDisplayContext.class, new SimpleCodecDeserializer<>(ItemDisplayContext.CODEC))
            .registerTypeAdapter(RPModel.ItemTransform.class, new SimpleCodecDeserializer<>(RPModel.ItemTransform.CODEC))
            .registerTypeAdapter(RPModel.TextureEntry.class, new RPModel.TextureEntry.Deserializer())
            .registerTypeAdapter(Texture.AnimationMeta.class, new Texture.AnimationMeta.Deserializer())

            .registerTypeAdapter(DataComponentMap.class, new PresetManager.ComponentMapDeserializer())
            .registerTypeAdapter(Components.Resolution.class, new SimpleCodecDeserializer<>(Components.Resolution.CODEC))
            .registerTypeAdapter(Components.VideoParams.class, new SimpleCodecDeserializer<>(Components.VideoParams.CODEC))
            .registerTypeAdapter(Components.ColorMode.class, new SimpleCodecDeserializer<>(Components.ColorMode.CODEC))
            .registerTypeAdapter(Components.DitherMode.class, new SimpleCodecDeserializer<>(Components.DitherMode.CODEC))
            .registerTypeAdapter(Components.MediaData.class, new SimpleCodecDeserializer<>(Components.MediaData.CODEC))

            .create();
    private static final Map<Identifier, RPModel> modelResources = new Reference2ObjectArrayMap<>();
    private static final Map<BlockState, RPBlockState> blockStateResources = new Reference2ObjectArrayMap<>();
    private static final Map<Identifier, byte[]> textureCache = new Reference2ObjectArrayMap<>();

    public static void clearCache() {
        BuiltinEntityModels.initModels();
        BuiltinEntityModels.modelMap.clear();

        modelResources.clear();
        blockStateResources.clear();
        textureCache.clear();
        textureCache2.clear();

        ItemStackRenderer.ITEM_MODELS.clear();

        EntityRenderers.ENTITY_TYPE_MODELS.clear();
        ChunkMeshCache.clear();
    }

    public static AssetFetcher getBuilder() {
        return Platforms.get().getAssetFetcher();
    }

    @Nullable
    public static RPBlockState loadBlockState(BlockState blockState) {
        if (blockStateResources.containsKey(blockState)) {
            return blockStateResources.get(blockState);
        }

        Identifier identifier = blockState.getBlock().builtInRegistryHolder().key().identifier();
        byte[] data = getBuilder().getAsset("assets/" + identifier.getNamespace() + "/blockstates/" + identifier.getPath() + ".json");
        if (data != null) {
            var resource = gson.fromJson(new InputStreamReader(new ByteArrayInputStream(data)), RPBlockState.class);
            blockStateResources.put(blockState, resource);
            return resource;
        }
        return null;
    }

    public static RPModel.View loadModelView(Identifier resourceLocation, Vector3fc blockRotation, boolean uvlock) {
        return new RPModel.View(loadModel(resourceLocation), blockRotation, uvlock);
    }

    public static RPModel loadModel(Identifier resourceLocation) {
        if (modelResources.containsKey(resourceLocation)) {
            return modelResources.get(resourceLocation);
        }

        byte[] data = getBuilder().getAsset("assets/" + resourceLocation.getNamespace() + "/models/" + resourceLocation.getPath() + ".json");
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
                for (Map.Entry<Direction, RPElement.TextureInfo> textureInfoEntry : element.faces.entrySet()) {
                    if (textureInfoEntry.getValue().uv == null) {
                        textureInfoEntry.getValue().uv = new Vector4f(
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

    public static byte[] loadTextureBytes(Identifier path) {
        return getBuilder().getAsset("assets/" + path.getNamespace() + "/textures/" + path.getPath() + ".png");
    }

    private static byte[] loadTextureImage(Identifier path) throws Exception {
        if (textureCache.containsKey(path)) {
            return textureCache.get(path);
        }

        byte[] data = loadTextureBytes(path);
        textureCache.put(path, data);
        return data;
    }

    private static Texture getMapTexture(String m) {
        var id = Integer.parseInt(m);
        var mapId = new MapId(id);
        MinecraftServer minecraftServer = Platforms.get().getMinecraftServer();
        var data = minecraftServer.getDataStorage().get(MapItemSavedData.type(mapId));
        if (data == null) {
            return Texture.DEFAULT_TEXTURE;
        }

        return Texture.fromARGB(ImageUtils.mapColorsToRGB(data.colors), 128, 128);
    }

    private static Texture getPlayerTexture(String uuid) {
        try {
            MinecraftServer nmsServer = Platforms.get().getMinecraftServer();

            var id = nmsServer.services().profileResolver().fetchById(UUID.fromString(uuid));
            if (id.isEmpty())
                throw new UnsupportedOperationException();

            var x = nmsServer.services().sessionService().getTextures(id.orElseThrow());
            assert x.skin() != null;

            var skin = x.skin().getUrl();

            URL textureUrl = new URI(skin).toURL();
            var s = textureUrl.openStream();
            var tex = Texture.fromPng(s);
            s.close();

            return tex;
        } catch (Exception e) {
            Platforms.get().getLogger().error("Error fetching player texture!", e);
        }

        return Texture.DEFAULT_TEXTURE;
    }

    public static List<RPModel.View> loadModel(RPBlockState rpBlockState, BlockState blockState) {
        if (rpBlockState != null && rpBlockState.variants != null) {
            for (Map.Entry<String, Variant> entry : rpBlockState.variants.entrySet()) {
                boolean matches = true;
                if (!entry.getKey().isEmpty()) {
                    try {
                        String str = String.format("%s[%s]", blockState.getBlock().builtInRegistryHolder().key().identifier(), entry.getKey());
                        BlockStateParser.BlockResult blockResult = BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK, str, false);

                        for (Map.Entry<Property<?>, Comparable<?>> propertyComparableEntry : blockResult.properties().entrySet()) {
                            if (!blockState.getValue(propertyComparableEntry.getKey()).equals(propertyComparableEntry.getValue())) {
                                matches = false;
                            }
                        }
                    } catch (Exception e) {
                        //CameraObscura.logger().error("Could not load model(s) for {}", blockState, e);
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
        if (rpBlockState != null) {
            return loadModel(rpBlockState, block);
        }

        return null;
    }

    private static BlockState safePolymerBlockState(BlockState blockState) {
        return blockState;
    }

    public static String loadTextureMeta(Identifier resourceLocation) {
        var data = getBuilder().getAsset("assets/" + resourceLocation.getNamespace() + "/textures/" + resourceLocation.getPath() + ".png.mcmeta");
        return data == null ? null : new String(data, StandardCharsets.UTF_8);
    }

    public static void removeCached(Identifier id) {
        textureCache2.remove(id);
    }

    public static Texture loadTexture(Identifier id) {
        Texture cached = textureCache2.get(id);
        if (cached != null) return cached;

        if (id.getNamespace().equals(Constants.DYNAMIC_PLAYER_TEXTURE)) {
            var img = getPlayerTexture(id.getPath());
            textureCache2.put(id, img);
            return img;
        } else if (id.getNamespace().equals(Constants.DYNAMIC_SIGN_TEXTURE)) {
            var img = getPlayerTexture(id.getPath());
            textureCache2.put(id, img);
            return img;
        } else if (id.getNamespace().equals(Constants.DYNAMIC_MAP_TEXTURE)) {
            var img = getMapTexture(id.getPath());
            textureCache2.put(id, img);
            return img;
        }

        try {
            byte[] image = loadTextureImage(id);
            String metaJson = loadTextureMeta(id);
            Texture.AnimationMeta meta = metaJson != null ? Texture.AnimationMeta.fromJson(metaJson) : null;
            Texture texture = Texture.fromPng(image, meta);
            textureCache2.put(id, texture);
            return texture;
        } catch (Exception ignored) {
            return null;
        }
    }

    public static JsonElement getEquipment(Identifier path) {
        var p = "assets/" + path.getNamespace() + "/equipment/" + path.getPath() + ".json";
        var data = getBuilder().getAsset(p);
        if (data == null) return null;
        return JsonParser.parseString(new String(data, StandardCharsets.UTF_8));
    }
}
