package de.tomalbrc.cameraobscura.util;

import de.tomalbrc.cameraobscura.render.model.resource.RPElement;
import de.tomalbrc.cameraobscura.render.model.resource.RPModel;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.material.FluidState;
import org.joml.Vector3f;
import org.joml.Vector4i;

import java.util.Map;

public class BuiltinModels {
    static Int2ObjectOpenHashMap<RPModel.View> waterModels = new Int2ObjectOpenHashMap<>();
    static Int2ObjectOpenHashMap<RPModel.View> lavaModels = new Int2ObjectOpenHashMap<>();
    public static RPModel.View liquidModel(FluidState fluidState, FluidState fluidStateAbove) {
        int height = fluidState.getAmount() * 2 - (fluidStateAbove != null && fluidStateAbove.is(fluidState.getType()) ? 0:1);

        if (fluidState.is(FluidTags.WATER) && waterModels.containsKey(height))
            return waterModels.get(height);
        if (fluidState.is(FluidTags.LAVA) && lavaModels.containsKey(height))
            return lavaModels.get(height);

        RPModel rpModel = new RPModel();
        rpModel.parent = new ResourceLocation("minecraft:block/cube_all");
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
        tiSide.uv = new Vector4i(0,0,16,height);
        tiSide.tintIndex = 0;

        element.faces.put("up", ti);
        element.faces.put("down", ti);
        element.faces.put("north", tiSide);
        element.faces.put("east", tiSide);
        element.faces.put("south", tiSide);
        element.faces.put("west", tiSide);

        rpModel.elements.add(element);

        var view = new RPModel.View(rpModel, new Vector3f());

        if (fluidState.is(FluidTags.WATER))
            waterModels.put(height, view);

        if (fluidState.is(FluidTags.LAVA))
            lavaModels.put(height, view);

        return view;
    }

    static RPModel.View portalModel = null;
    static RPModel.View portalBlockModel = null;

    public static RPModel.View portalModel(boolean flat) {
        if (flat && portalModel != null)
            return portalModel;
        else if (portalBlockModel != null)
            return portalBlockModel;

        RPModel rpModel = new RPModel();
        rpModel.parent = new ResourceLocation("minecraft:block/cube_all");
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
        tiSide.uv = new Vector4i(0,0,16,16);

        element.faces.put("up", ti);
        element.faces.put("down", ti);
        element.faces.put("north", tiSide);
        element.faces.put("east", tiSide);
        element.faces.put("south", tiSide);
        element.faces.put("west", tiSide);

        rpModel.elements.add(element);

        var view = new RPModel.View(rpModel, new Vector3f());

        if (flat)
            portalModel = view;
        else
            portalBlockModel = view;

        return view;
    }


    static Map<String, RPModel.View> chestModels = new Object2ObjectOpenHashMap<>();

    public static RPModel.View chestModel(String texture) {
        if (chestModels.containsKey(texture))
            return chestModels.get(texture);

        RPModel rpModel = new RPModel();
        rpModel.parent = new ResourceLocation("minecraft:block/cube_all");
        rpModel.textures = new Object2ObjectOpenHashMap<>();
        rpModel.textures.put("all", texture);
        rpModel.elements = new ObjectArrayList<>();
        var element = new RPElement();
        element.from = new Vector3f(1, 0,1);
        element.to = new Vector3f(15, 14, 15);
        element.faces = new Object2ObjectOpenHashMap<>();

        var ti = new RPElement.TextureInfo();
        ti.texture = "#all";
        ti.uv = new Vector4i(14,19,28,33);

        var tiSide = new RPElement.TextureInfo();
        tiSide.texture = "#all";
        tiSide.uv = ti.uv;

        element.faces.put("up", ti);
        element.faces.put("down", ti);
        element.faces.put("north", tiSide);
        element.faces.put("east", tiSide);
        element.faces.put("south", tiSide);
        element.faces.put("west", tiSide);

        rpModel.elements.add(element);

        var view = new RPModel.View(rpModel, new Vector3f());

        chestModels.put(texture, view);

        return view;
    }

    static RPModel.View shulkerModel = null;

    public static RPModel.View shulkerModel() {
        if (shulkerModel != null)
            return shulkerModel;

        RPModel rpModel = new RPModel();
        rpModel.parent = new ResourceLocation("minecraft:block/cube_all");
        rpModel.textures = new Object2ObjectOpenHashMap<>();
        rpModel.textures.put("all", "minecraft:entity/shulker/shulker");
        rpModel.elements = new ObjectArrayList<>();

        rpModel.elements.add(shulkerElement1());
        rpModel.elements.add(shulkerElement2());

        var view = new RPModel.View(rpModel, new Vector3f());

        shulkerModel = view;

        return view;
    }

    private static RPElement shulkerElement1() {
        var element = new RPElement();
        element.from = new Vector3f(0, 0,0);
        element.to = new Vector3f(16, 16, 16);
        element.faces = new Object2ObjectOpenHashMap<>();

        var ti = new RPElement.TextureInfo();
        ti.texture = "#all";
        ti.uv = new Vector4i(16,0,32,16);

        var tiSide = new RPElement.TextureInfo();
        tiSide.texture = "#all";
        tiSide.uv = new Vector4i(0,16,16,32);

        element.faces.put("up", ti);
        element.faces.put("down", ti);
        element.faces.put("north", tiSide);
        element.faces.put("east", tiSide);
        element.faces.put("south", tiSide);
        element.faces.put("west", tiSide);

        return element;
    }

    private static RPElement shulkerElement2() {
        var element = new RPElement();
        element.from = new Vector3f(0, 0,0);
        element.to = new Vector3f(16, 16, 16);
        element.faces = new Object2ObjectOpenHashMap<>();

        var tiSide = new RPElement.TextureInfo();
        tiSide.texture = "#all";
        tiSide.uv = new Vector4i(0,32 + 4,16,48 + 4);

        element.faces.put("north", tiSide);
        element.faces.put("east", tiSide);
        element.faces.put("south", tiSide);
        element.faces.put("west", tiSide);

        return element;
    }



    static RPModel.View skyModel = null;
    public static RPModel.View skyModel() {
        if (skyModel != null)
            return skyModel;

        RPModel rpModel = new RPModel();
        rpModel.parent = new ResourceLocation("minecraft:block/cube_all");
        rpModel.textures = new Object2ObjectOpenHashMap<>();
        rpModel.textures.put("all", "minecraft:environment/clouds");
        rpModel.elements = new ObjectArrayList<>();
        var element = new RPElement();
        element.from = new Vector3f(-128*64, 0,-128*64);
        element.to = new Vector3f(128*64,0, 128*64);
        element.faces = new Object2ObjectOpenHashMap<>();

        var ti = new RPElement.TextureInfo();
        ti.texture = "#all";
        ti.uv = new Vector4i(0,0,32,32);

        //element.faces.put("up", ti);
        element.faces.put("down", ti);

        rpModel.elements.add(element);

        var view = new RPModel.View(rpModel, new Vector3f());

        skyModel = view;

        return view;
    }
}
