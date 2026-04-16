package de.tomalbrc.cameraobscura.util.resource.model;

import de.tomalbrc.cameraobscura.json.CachedIdentifierDeserializer;
import de.tomalbrc.cameraobscura.model.resource.RPElement;
import de.tomalbrc.cameraobscura.model.resource.RPModel;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Direction;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class PortalModel {
    public static RPModel.View get(boolean flat) {
        RPModel rpModel = new RPModel();
        rpModel.parent = CachedIdentifierDeserializer.get("minecraft:block/cube_all");
        rpModel.textures = new Object2ObjectOpenHashMap<>();
        // TODO
        if (flat) {
            rpModel.textures.put("all", RPModel.TextureEntry.of("minecraft:block/black_concrete"));
        } else {
            rpModel.textures.put("all", RPModel.TextureEntry.of("minecraft:block/black_concrete"));
        }

        return new RPModel.View(rpModel);
    }

    public static RPModel getItem() {
        RPModel rpModel = new RPModel();
        rpModel.parent = null;
        rpModel.textures = new Object2ObjectOpenHashMap<>();
        rpModel.elements = new ObjectArrayList<>();
        var element = new RPElement();
        element.from = new Vector3f(0, 0, 0);
        element.to = new Vector3f(16, 16, 0);
        element.faces = new Object2ObjectOpenHashMap<>();

        var ti = new RPElement.TextureInfo();
        ti.texture = "#layer0";

        var tiSide = new RPElement.TextureInfo();
        tiSide.texture = "#layer0";
        tiSide.uv = new Vector4f(0, 0, 16, 16);

        element.faces.put(Direction.NORTH, tiSide);
        element.faces.put(Direction.SOUTH, tiSide);

        rpModel.elements.add(element);

        return rpModel;
    }
}
