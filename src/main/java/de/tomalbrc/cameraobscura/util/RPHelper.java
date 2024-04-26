package de.tomalbrc.cameraobscura.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.tomalbrc.cameraobscura.RPBlockState;
import de.tomalbrc.cameraobscura.RPModel;
import de.tomalbrc.cameraobscura.json.VariantDeserializer;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;
import net.minecraft.resources.ResourceLocation;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;

public class RPHelper {
    private static ResourcePackBuilder resourcePackBuilder = PolymerResourcePackUtils.createBuilder(Path.of("a/b"));
    final private static Gson gson = new GsonBuilder()
            .registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
            .registerTypeAdapter(RPBlockState.Variant.class, new VariantDeserializer())
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
}
