package de.tomalbrc.cameraobscura.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;

public class CachedResourceLocationDeserializer implements JsonDeserializer<ResourceLocation> {
    private static final ConcurrentHashMap<String, ResourceLocation> CACHE = new ConcurrentHashMap<>();

    public static ResourceLocation get(String name) {
        ResourceLocation resourceLocation = CACHE.get(name);
        if (resourceLocation == null) {
            resourceLocation = ResourceLocation.parse(name);
            CACHE.put(name, resourceLocation);
            return resourceLocation;
        }
        return CACHE.get(name);
    }

    @Override
    public ResourceLocation deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
        String string = element.getAsString();
        if (!string.contains(":")) {
            string = "minecraft:" + string;
        }

        ResourceLocation location = CACHE.get(string);
        if (location != null) {
            return location;
        }

        ResourceLocation.Serializer serializer = new ResourceLocation.Serializer();
        location = serializer.deserialize(element, type, context);

        CACHE.put(string, location);
        return location;
    }
}