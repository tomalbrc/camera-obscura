package de.tomalbrc.cameraobscura.util.model;

import de.tomalbrc.cameraobscura.render.model.resource.RPElement;
import de.tomalbrc.cameraobscura.render.model.resource.RPModel;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class LiquidModel {
    public static RPModel.View get(FluidState fluidState, FluidState fluidStateAbove) {
        int height = !fluidStateAbove.isEmpty() ? 16 : (fluidState.getAmount()-1) * 2;

        RPModel rpModel = new RPModel();
        rpModel.parent = ResourceLocation.withDefaultNamespace("block/cube_all");
        rpModel.textures = new Object2ObjectOpenHashMap<>();
        if (fluidState.is(FluidTags.WATER)) {
            rpModel.textures.put("top", "minecraft:block/water_still");
            rpModel.textures.put("side", "minecraft:block/water_flow");
        } else {
            rpModel.textures.put("top", "minecraft:block/lava_still");
            rpModel.textures.put("side", "minecraft:block/lava_flow");
        }
        rpModel.elements = new ObjectArrayList<>();
        var element = new RPElement();
        element.from = new Vector3f(0,0,0);
        element.to = new Vector3f(16,height,16);
        element.faces = new Object2ObjectOpenHashMap<>();
        element.shade = false;

        var ti = new RPElement.TextureInfo();
        ti.texture = "#top";
        ti.tintIndex = 0;

        var tiSide = new RPElement.TextureInfo();
        tiSide.texture = "#side";
        tiSide.uv = new Vector4f(0,0,16,height);
        tiSide.tintIndex = 0;

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
