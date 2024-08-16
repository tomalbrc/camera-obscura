package de.tomalbrc.cameraobscura.util;

import de.tomalbrc.cameraobscura.render.model.resource.RPElement;
import de.tomalbrc.cameraobscura.render.model.resource.RPModel;
import de.tomalbrc.cameraobscura.util.model.*;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Map;

public class BuiltinModels {
    static Int2ObjectOpenHashMap<RPModel.View> waterModels = new Int2ObjectOpenHashMap<>();
    static Int2ObjectOpenHashMap<RPModel.View> lavaModels = new Int2ObjectOpenHashMap<>();
    public static RPModel.View liquidModel(FluidState fluidState, FluidState fluidStateAbove) {
        int height = (fluidState.getAmount()-1) * 2;

        if (fluidState.is(FluidTags.WATER) && waterModels.containsKey(height))
            return waterModels.get(height);
        if (fluidState.is(FluidTags.LAVA) && lavaModels.containsKey(height))
            return lavaModels.get(height);

        var view = LiquidModel.get(fluidState, fluidStateAbove);

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

        var view = PortalModel.get(flat);

        if (flat)
            portalModel = view;
        else
            portalBlockModel = view;

        return view;
    }

    static Map<BlockState, RPModel.View> chestModels = new Object2ObjectOpenHashMap<>();
    public static RPModel.View chestModel(BlockState chestBlockState) {
        if (chestModels.containsKey(chestBlockState))
            return chestModels.get(chestBlockState);

        var view = ChestModel.get(chestBlockState);

        chestModels.put(chestBlockState, view);

        return view;
    }

    static Map<BlockState, RPModel.View> bedModels = new Object2ObjectOpenHashMap<>();
    public static RPModel.View bedModel(BlockState chestBlockState) {
        if (bedModels.containsKey(chestBlockState))
            return bedModels.get(chestBlockState);

        var view = BedModel.get(chestBlockState);

        bedModels.put(chestBlockState, view);

        return view;
    }

    static RPModel.View shulkerModel = null;
    public static RPModel.View shulkerModel() {
        if (shulkerModel != null)
            return shulkerModel;

        shulkerModel = ShulkerModel.get();
        return shulkerModel;
    }

    static RPModel.View decoratedPotModel = null;
    public static RPModel.View decoratedPotModel() {
        if (decoratedPotModel != null)
            return decoratedPotModel;

        decoratedPotModel = DecoratedPotModel.get();
        return decoratedPotModel;
    }

    static RPModel.View conduitModel = null;
    public static RPModel.View conduitModel() {
        if (conduitModel != null)
            return conduitModel;

        conduitModel = ConduitModel.get();
        return conduitModel;
    }

    static RPModel.View skyModel = null;
    public static RPModel.View skyModel() {
        if (skyModel != null)
            return skyModel;

        RPModel rpModel = new RPModel();
        rpModel.parent = ResourceLocation.withDefaultNamespace("block/cube_all");
        rpModel.textures = new Object2ObjectOpenHashMap<>();
        rpModel.textures.put("all", "minecraft:environment/clouds");
        rpModel.elements = new ObjectArrayList<>();
        var element = new RPElement();
        element.from = new Vector3f(-128*64, 0,-128*64);
        element.to = new Vector3f(128*64,0, 128*64);
        element.faces = new Object2ObjectOpenHashMap<>();

        var ti = new RPElement.TextureInfo();
        ti.texture = "#all";
        ti.uv = new Vector4f(0,0,32,32);

        //element.faces.put("up", ti);
        element.faces.put("down", ti);

        rpModel.elements.add(element);

        var view = new RPModel.View(rpModel, Vec3.ZERO.toVector3f());

        skyModel = view;

        return view;
    }
}
