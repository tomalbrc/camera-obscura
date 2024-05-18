package de.tomalbrc.cameraobscura.json;

import com.google.gson.*;
import org.joml.Vector4f;

import java.lang.reflect.Type;

public class Vector4fDeserializer implements JsonDeserializer<Vector4f> {
    @Override
    public Vector4f deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        var x = jsonArray.get(0).getAsFloat();
        var y = jsonArray.get(1).getAsFloat();
        var z = jsonArray.get(2).getAsFloat();
        var w = jsonArray.get(3).getAsFloat();
        return new Vector4f(x, y, z, w);
    }
}