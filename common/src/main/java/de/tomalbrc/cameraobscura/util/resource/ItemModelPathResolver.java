package de.tomalbrc.cameraobscura.util.resource;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.tomalbrc.cameraobscura.json.CachedIdentifierDeserializer;
import de.tomalbrc.cameraobscura.platform.AssetFetcher;
import de.tomalbrc.cameraobscura.platform.Platforms;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class ItemModelPathResolver {

    public static Set<Identifier> getModelPaths(ItemStack stack) {
        Set<Identifier> paths = new HashSet<>();
        Identifier definitionId = getItemModelDefinitionId(stack);
        if (definitionId == null) return paths;

        collectModelPaths(definitionId, RPHelper.getBuilder(), paths);
        return paths;
    }

    private static Identifier getItemModelDefinitionId(ItemStack stack) {
        return stack.get(DataComponents.ITEM_MODEL);
    }

    private static void collectModelPaths(Identifier definitionId, AssetFetcher resourcePackBuilder, Set<Identifier> paths) {
        Identifier fileLocation = Identifier.fromNamespaceAndPath(
                definitionId.getNamespace(),
                "items/" + definitionId.getPath() + ".json"
        );

        byte[] resourceOpt = resourcePackBuilder.getAsset("assets/" + fileLocation.getNamespace() + "/" + fileLocation.getPath());
        if (resourceOpt == null) {
            return;
        }

        try (InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(resourceOpt))) {
            JsonObject root = RPHelper.gson.fromJson(reader, JsonObject.class);
            if (root.has("model")) {
                processModelObject(root.getAsJsonObject("model"), paths);
            }
        } catch (Exception e) {
            Platforms.get().getLogger().error("Error while processing ItemModelPaths for " + fileLocation, e);
        }
    }

    private static void processModelObject(JsonObject modelObj, Set<Identifier> paths) {
        if (!modelObj.has("type")) return;
        String type = modelObj.get("type").getAsString();

        switch (type) {
            case "minecraft:model" -> {
                if (modelObj.has("model")) {
                    paths.add(CachedIdentifierDeserializer.get(modelObj.get("model").getAsString()));
                }
            }
            case "minecraft:composite" -> {
                JsonArray models = modelObj.getAsJsonArray("models");
                for (JsonElement elem : models) {
                    processModelObject(elem.getAsJsonObject(), paths);
                }
            }
            case "minecraft:condition" -> {
                if (modelObj.has("on_true")) {
                    processModelObject(modelObj.getAsJsonObject("on_true"), paths);
                }
                if (modelObj.has("on_false")) {
                    processModelObject(modelObj.getAsJsonObject("on_false"), paths);
                }
            }
            case "minecraft:select" -> {
                JsonArray cases = modelObj.getAsJsonArray("cases");
                for (JsonElement elem : cases) {
                    JsonObject caseObj = elem.getAsJsonObject();
                    if (caseObj.has("model")) {
                        processModelObject(caseObj.getAsJsonObject("model"), paths);
                    }
                }
                if (modelObj.has("fallback")) {
                    processModelObject(modelObj.getAsJsonObject("fallback"), paths);
                }
            }
            case "minecraft:range_dispatch" -> {
                JsonArray entries = modelObj.getAsJsonArray("entries");
                for (JsonElement elem : entries) {
                    JsonObject entry = elem.getAsJsonObject();
                    if (entry.has("model")) {
                        processModelObject(entry.getAsJsonObject("model"), paths);
                    }
                }
                if (modelObj.has("fallback")) {
                    processModelObject(modelObj.getAsJsonObject("fallback"), paths);
                }
            }
            // TODO: special, bundle/selected_item ?
            // tints too
        }
    }
}