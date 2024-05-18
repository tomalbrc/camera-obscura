package de.tomalbrc.cameraobscura.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import de.tomalbrc.cameraobscura.render.model.resource.state.MultipartDefinition;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class ConditionDeserializer implements JsonDeserializer<MultipartDefinition.Condition> {

    @Override
    public MultipartDefinition.Condition deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonObject()) {
            MultipartDefinition.Condition condition = new MultipartDefinition.Condition();

            var obj = json.getAsJsonObject();
            if (obj.has("OR")) {
                 condition.OR = new MultipartDefinition.OrCondition();
                Type listStringMapType = new TypeToken<List<Map<String, String>>>(){}.getType();

                condition.OR.blockStateValueList = context.deserialize(obj.get("OR").getAsJsonArray(), listStringMapType);
            }
            else if (obj.has("AND")) {
                condition.AND = new MultipartDefinition.AndCondition();
                Type listStringMapType = new TypeToken<List<Map<String, String>>>(){}.getType();

                condition.AND.blockStateValueList = context.deserialize(obj.get("AND").getAsJsonArray(), listStringMapType);
            } else {
                Type t = new TypeToken<Map<String, String>>(){}.getType();
                condition.blockStateValues = context.deserialize(json, t);
            }

            return condition;
        } else {
            throw new JsonParseException("Unexpected JSON element type for Variant: " + json);
        }
    }
}
