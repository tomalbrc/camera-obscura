package de.tomalbrc.cameraobscura.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.tomalbrc.cameraobscura.json.VariantDeserializer;
import de.tomalbrc.cameraobscura.json.Vector3fDeserializer;
import de.tomalbrc.cameraobscura.render.RPBlockState;
import de.tomalbrc.cameraobscura.render.RPModel;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;

public class RPHelper {
    private static ResourcePackBuilder resourcePackBuilder = PolymerResourcePackUtils.createBuilder(Path.of("a/b"));
    final private static Gson gson = new GsonBuilder()
            .registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
            .registerTypeAdapter(RPBlockState.Variant.class, new VariantDeserializer())
            .registerTypeAdapter(Vector3f.class, new Vector3fDeserializer())
            .create();

    public static RPBlockState loadBlockState(String path) {
        byte[] data = resourcePackBuilder.getDataOrSource("assets/minecraft/blockstates/" + path + ".json");
        return gson.fromJson(new InputStreamReader(new ByteArrayInputStream(data)), RPBlockState.class);
    }

    public static RPModel loadModel(String path) {
        byte[] data = resourcePackBuilder.getDataOrSource("assets/minecraft/models/" + path + ".json");
        return gson.fromJson(new InputStreamReader(new ByteArrayInputStream(data)), RPModel.class);
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
                return RPHelper.loadModel(entry.getValue().model.getPath()).prepare().rotate(entry.getValue());
            }
        }
        return null;
    }
}
