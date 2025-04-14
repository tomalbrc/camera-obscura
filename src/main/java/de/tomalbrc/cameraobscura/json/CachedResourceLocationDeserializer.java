package de.tomalbrc.cameraobscura.json;

import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;

public class CachedResourceLocationDeserializer implements JsonDeserializer<ResourceLocation>, JsonSerializer<ResourceLocation> {
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

        location = ResourceLocation.parse(string);

        CACHE.put(string, location);
        return location;
    }

    @Override
    public JsonElement serialize(ResourceLocation resourceLocation, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(resourceLocation.toString());
    }
}