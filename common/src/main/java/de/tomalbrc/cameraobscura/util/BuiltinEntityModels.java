package de.tomalbrc.cameraobscura.util;

import de.tomalbrc.cameraobscura.json.CachedIdentifierDeserializer;
import de.tomalbrc.cameraobscura.model.resource.RPModel;
import de.tomalbrc.cameraobscura.util.resource.RPHelper;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Map;
import java.util.UUID;

public class BuiltinEntityModels {
    public static final Map<EntityType<?>, RPModel> modelMap = new Reference2ObjectArrayMap<>();

    public static @Nullable RPModel.View getModel(EntityType<?> entityType, @Nullable UUID uuid) {
        if (modelMap.containsKey(entityType)) {
            if (entityType == EntityType.ITEM_FRAME || entityType == EntityType.GLOW_ITEM_FRAME) {
                return new RPModel.View(modelMap.get(entityType), new Vector3f(), new Vector3f(0, 0, 0));
            } else {
                return new RPModel.View(modelMap.get(entityType), new Vector3f(), new Vector3f(-0.5f, 0, -0.5f));
            }
        }

        return null;
    }

    public static void initModels() {
        modelMap.put(EntityType.ITEM_FRAME, RPHelper.loadModel(CachedIdentifierDeserializer.get("block/item_frame")));
        modelMap.put(EntityType.GLOW_ITEM_FRAME, RPHelper.loadModel(CachedIdentifierDeserializer.get("block/glow_item_frame")));
    }
}
