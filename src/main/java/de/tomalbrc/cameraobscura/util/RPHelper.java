package de.tomalbrc.cameraobscura.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.tomalbrc.cameraobscura.json.VariantDeserializer;
import de.tomalbrc.cameraobscura.json.Vector3fDeserializer;
import de.tomalbrc.cameraobscura.json.Vector4iDeserializer;
import de.tomalbrc.cameraobscura.render.RPBlockState;
import de.tomalbrc.cameraobscura.render.model.resource.RPModel;
import de.tomalbrc.cameraobscura.render.model.resource.state.Variant;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;
import org.joml.Vector4i;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;

public class RPHelper {
    private static ResourcePackBuilder resourcePackBuilder = PolymerResourcePackUtils.createBuilder(Path.of("a/b"));
    final private static Gson gson = new GsonBuilder()
            .registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
            .registerTypeAdapter(Variant.class, new VariantDeserializer())
            .registerTypeAdapter(Vector3f.class, new Vector3fDeserializer())
            .registerTypeAdapter(Vector4i.class, new Vector4iDeserializer())
            .create();

    public static RPBlockState loadBlockState(String path) {
        byte[] data = resourcePackBuilder.getDataOrSource("assets/minecraft/blockstates/" + path + ".json");
        return gson.fromJson(new InputStreamReader(new ByteArrayInputStream(data)), RPBlockState.class);
    }

    public static RPModel loadModel(String path, Vector3f blockRotation) {
        byte[] data = resourcePackBuilder.getDataOrSource("assets/minecraft/models/" + path + ".json");
        RPModel model = gson.fromJson(new InputStreamReader(new ByteArrayInputStream(data)), RPModel.class);
        model.blockRotation = blockRotation;
        return model;
    }

    public static byte[] loadTexture(String path) {
        byte[] data = resourcePackBuilder.getDataOrSource("assets/minecraft/textures/" + path + ".png");
        return data;
    }

    public static RPModel loadModel(BlockState blockState) {
        String blockName = BuiltInRegistries.BLOCK.getKey(blockState.getBlock()).getPath();
        RPBlockState rpBlockState = RPHelper.loadBlockState(blockName);
        if (rpBlockState == null) return null;

        if (rpBlockState.variants != null) for (var entry: rpBlockState.variants.entrySet()) {
            BlockState state = null;
            if (!entry.getKey().isEmpty()) {
                try {
                    state = BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK.asLookup(), String.format("%s[%s]", blockName, entry.getKey()), false).blockState();
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            if (entry.getKey().isEmpty() || state == blockState) {
                return RPHelper.loadModel(entry.getValue().model.getPath(), new Vector3f(entry.getValue().x, entry.getValue().y, entry.getValue().z));
            }
        }
        return null;
    }
}
