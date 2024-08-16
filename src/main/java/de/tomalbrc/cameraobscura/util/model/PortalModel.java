package de.tomalbrc.cameraobscura.util.model;

import de.tomalbrc.cameraobscura.render.model.resource.RPElement;
import de.tomalbrc.cameraobscura.render.model.resource.RPModel;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class PortalModel {
    public static RPModel.View get(boolean flat) {
        RPModel rpModel = new RPModel();
        rpModel.parent = ResourceLocation.withDefaultNamespace("block/cube_all");
        rpModel.textures = new Object2ObjectOpenHashMap<>();
        if (flat) {
            rpModel.textures.put("all", "minecraft:block/black_concrete");
        } else {
            rpModel.textures.put("all", "minecraft:block/black_concrete");
        }
        rpModel.elements = new ObjectArrayList<>();
        var element = new RPElement();
        element.from = new Vector3f(0,flat ? 12 : 0,0);
        element.to = new Vector3f(16,flat ? 12 : 16, 16);
        element.faces = new Object2ObjectOpenHashMap<>();

        var ti = new RPElement.TextureInfo();
        ti.texture = "#all";

        var tiSide = new RPElement.TextureInfo();
        tiSide.texture = "#all";
        tiSide.uv = new Vector4f(0,0,16,16);

        element.faces.put("up", ti);
        element.faces.put("down", ti);
        element.faces.put("north", tiSide);
        element.faces.put("east", tiSide);
        element.faces.put("south", tiSide);
        element.faces.put("west", tiSide);

        rpModel.elements.add(element);

        var view = new RPModel.View(rpModel, Vec3.ZERO.toVector3f());

        return view;
    }
}
