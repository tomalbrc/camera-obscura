package de.tomalbrc.cameraobscura.util.resource.model;

import de.tomalbrc.cameraobscura.json.CachedIdentifierDeserializer;
import de.tomalbrc.cameraobscura.model.resource.RPElement;
import de.tomalbrc.cameraobscura.model.resource.RPModel;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class GeneratedItemModel {
    private static RPElement element(String layer, float inflate) {
        var element = new RPElement();
        element.from = new Vector3f(0, 0, 8).sub(inflate, inflate, inflate);
        element.to = new Vector3f(16, 16, 8).add(inflate, inflate, inflate);
        element.faces = new Object2ObjectOpenHashMap<>();

        var tiSide = new RPElement.TextureInfo();
        tiSide.texture = "#" + layer;
        tiSide.uv = new Vector4f(0, 0, 16, 16);

        var tiSide2 = new RPElement.TextureInfo();
        tiSide2.texture = "#" + layer;
        tiSide2.uv = new Vector4f(16, 0, 0, 16);

        element.faces.put(Direction.NORTH, tiSide);
        element.faces.put(Direction.SOUTH, tiSide2);
        element.shade = false;
        element.light = false;

        return element;
    }

    public static RPModel getItem(String layer0, @Nullable String layer1) {
        RPModel rpModel = new RPModel();
        rpModel.parent = CachedIdentifierDeserializer.get("item/generated");
        rpModel.textures = new Object2ObjectOpenHashMap<>();
        rpModel.textures.put("layer0", RPModel.TextureEntry.of(layer0));
        if (layer1 != null) rpModel.textures.put("layer1", RPModel.TextureEntry.of(layer1));

        rpModel.elements = new ObjectArrayList<>();

        rpModel.elements.add(element("layer0", 0));
        if (layer1 != null) rpModel.elements.add(element("layer1", 0.01f));

        return rpModel;
    }
}
