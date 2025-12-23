package de.tomalbrc.cameraobscura.json;

import com.google.gson.*;
import net.minecraft.resources.Identifier;

import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;

public class CachedIdentifierDeserializer implements JsonDeserializer<Identifier>, JsonSerializer<Identifier> {
    private static final ConcurrentHashMap<String, Identifier> CACHE = new ConcurrentHashMap<>();

    public static Identifier get(String name) {
        Identifier resourceLocation = CACHE.get(name);
        if (resourceLocation == null) {
            resourceLocation = Identifier.parse(name);
            CACHE.put(name, resourceLocation);
            return resourceLocation;
        }
        return CACHE.get(name);
    }

    @Override
    public Identifier deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
        String string = element.getAsString();
        if (!string.contains(":")) {
            string = "minecraft:" + string;
        }

        Identifier location = CACHE.get(string);
        if (location != null) {
            return location;
        }

        location = Identifier.parse(string);

        CACHE.put(string, location);
        return location;
    }

    @Override
    public JsonElement serialize(Identifier resourceLocation, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(resourceLocation.toString());
    }
}