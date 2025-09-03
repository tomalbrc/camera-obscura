package de.tomalbrc.cameraobscura.json;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import de.tomalbrc.cameraobscura.render.model.resource.state.MultipartDefinition;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

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
                Type listStringMapType = new TypeToken<List<Map<String, String>>>() {
                }.getType();

                condition.OR.blockStateValueList = convertToList(context.deserialize(obj.get("OR").getAsJsonArray(), listStringMapType));
            } else if (obj.has("AND")) {
                condition.AND = new MultipartDefinition.AndCondition();
                Type listStringMapType = new TypeToken<List<Map<String, String>>>() {
                }.getType();

                condition.AND.blockStateValueList = convertToList(context.deserialize(obj.get("AND").getAsJsonArray(), listStringMapType));
            } else {
                Type t = new TypeToken<Map<String, String>>() {
                }.getType();
                condition.blockStateValues = convertToList(ImmutableList.of(context.deserialize(obj, t))).getFirst();
            }

            return condition;
        } else {
            throw new JsonParseException("Unexpected JSON element type for Variant: " + json);
        }
    }

    private static @NotNull List<Map<String, String[]>> convertToList(List<Map<String, String>> t1) {
        List<Map<String, String[]>> t2 = new ObjectArrayList<>();
        for (int i = 0; i < t1.size(); i++) {
            var map = t1.get(i);
            Map<String, String[]> resMap = new Object2ObjectOpenHashMap<>();
            for (Map.Entry<String, String> entry : map.entrySet()) {
                var list = entry.getValue().split("\\|");
                resMap.put(entry.getKey(), list);
            }
            t2.add(i, resMap);
        }
        return t2;
    }
}
