package de.tomalbrc.cameraobscura.json;

import com.google.gson.*;
import de.tomalbrc.cameraobscura.render.model.resource.state.Variant;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Type;

public class VariantDeserializer implements JsonDeserializer<Variant> {

    @Override
    public Variant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonObject()) {
            Variant variant = new Variant();

            JsonObject jsonObject = json.getAsJsonObject();
            if (jsonObject.has("model")) variant.model = new ResourceLocation(jsonObject.get("model").getAsString());
            if (jsonObject.has("x")) variant.x = jsonObject.get("x").getAsInt();
            if (jsonObject.has("y")) variant.y = jsonObject.get("y").getAsInt();
            if (jsonObject.has("z")) variant.z = jsonObject.get("z").getAsInt();

            return variant;
        } else if (json.isJsonArray()) {
            JsonArray jsonArray = json.getAsJsonArray();
            if (jsonArray.size() > 0) {
                return context.deserialize(jsonArray.get(0), Variant.class);
            } else {
                return null;
            }
        } else {
            throw new JsonParseException("Unexpected JSON element type for Variant: " + json);
        }
    }
}
