package de.tomalbrc.cameraobscura.util.model;

import de.tomalbrc.cameraobscura.render.model.resource.RPElement;
import de.tomalbrc.cameraobscura.render.model.resource.RPModel;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class GeneratedItemModel {
    private static RPElement element(String layer, float inflate) {
        var element = new RPElement();
        element.from = new Vector3f(0,0,8).sub(inflate, inflate, inflate);
        element.to = new Vector3f(16,16, 8).add(inflate, inflate, inflate);
        element.faces = new Object2ObjectOpenHashMap<>();

        var ti = new RPElement.TextureInfo();
        ti.texture = "#" + layer;

        var tiSide = new RPElement.TextureInfo();
        tiSide.texture = "#" + layer;
        tiSide.uv = new Vector4f(0,0,16,16);

        element.faces.put("north", tiSide);
        element.faces.put("south", tiSide);

        return element;
    }

    public static RPModel getItem(String layer0, String layer1) {
        RPModel rpModel = new RPModel();
        rpModel.parent = null;
        rpModel.textures = new Object2ObjectOpenHashMap<>();
        rpModel.textures.put("layer0", layer0);
        if (layer1 != null) rpModel.textures.put("layer1", layer1);

        rpModel.elements = new ObjectArrayList<>();

        rpModel.elements.add(element("layer0", 0));
        if (layer1 != null) rpModel.elements.add(element("layer1", 0.01f));

        return rpModel;
    }
}
