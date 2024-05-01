package de.tomalbrc.cameraobscura.json;

import com.google.gson.*;
import org.joml.Vector4i;

import java.lang.reflect.Type;

public class Vector4iDeserializer implements JsonDeserializer<Vector4i> {
    @Override
    public Vector4i deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        int x = jsonArray.get(0).getAsInt();
        int y = jsonArray.get(1).getAsInt();
        int z = jsonArray.get(2).getAsInt();
        int w = jsonArray.get(3).getAsInt();
        return new Vector4i(x, y, z, w);
    }
}