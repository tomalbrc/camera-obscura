package de.tomalbrc.cameraobscura.util.model;

import de.tomalbrc.cameraobscura.render.model.resource.RPElement;
import de.tomalbrc.cameraobscura.render.model.resource.RPModel;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class GeneratedItemModel {
    public static RPModel getItem(String textureLoc) {
        RPModel rpModel = new RPModel();
        rpModel.parent = null;
        rpModel.textures = new Object2ObjectOpenHashMap<>();
        rpModel.textures.put("layer0", textureLoc);

        rpModel.elements = new ObjectArrayList<>();
        var element = new RPElement();
        element.from = new Vector3f(0,0,0);
        element.to = new Vector3f(16,16, 0);
        element.faces = new Object2ObjectOpenHashMap<>();

        var ti = new RPElement.TextureInfo();
        ti.texture = "#layer0";

        var tiSide = new RPElement.TextureInfo();
        tiSide.texture = "#layer0";
        tiSide.uv = new Vector4f(0,0,16,16);

        element.faces.put("north", tiSide);
        element.faces.put("south", tiSide);

        rpModel.elements.add(element);

        return rpModel;
    }
}
