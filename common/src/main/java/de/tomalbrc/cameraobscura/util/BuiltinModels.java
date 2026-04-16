package de.tomalbrc.cameraobscura.util;

import de.tomalbrc.cameraobscura.model.resource.RPElement;
import de.tomalbrc.cameraobscura.model.resource.RPModel;
import de.tomalbrc.cameraobscura.util.resource.model.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Map;
import java.util.Optional;

public class BuiltinModels {
    public static Map<BlockState, RPModel.View> modelMap = new Reference2ObjectArrayMap<>();

    static RPModel.View portalModel = null;
    static RPModel.View portalBlockModel = null;
    static RPModel.View decoratedPotModel = null;
    static RPModel.View conduitModel = null;

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

    public static RPModel.View chestModel(BlockState chestBlockState) {
        if (modelMap.containsKey(chestBlockState))
            return modelMap.get(chestBlockState);

        var model = ChestModel.get(chestBlockState);
        modelMap.put(chestBlockState, model);
        return model;
    }

    public static RPModel.View bedModel(BlockState chestBlockState, Optional<DyeColor> color) {
        if (modelMap.containsKey(chestBlockState))
            return modelMap.get(chestBlockState);


        var model = BedModel.get(chestBlockState, color);
        modelMap.put(chestBlockState, model);
        return model;
    }

    public static RPModel.View shulkerModel(BlockState blockState, Optional<DyeColor> color) {
        if (modelMap.containsKey(blockState))
            return modelMap.get(blockState);

        var model = ShulkerModel.get(blockState, color);
        modelMap.put(blockState, model);
        return model;
    }

    public static RPModel.View decoratedPotModel() {
        if (decoratedPotModel != null)
            return decoratedPotModel;

        decoratedPotModel = DecoratedPotModel.get();
        return decoratedPotModel;
    }

    public static RPModel.View conduitModel() {
        if (conduitModel != null)
            return conduitModel;

        conduitModel = ConduitModel.get();
        return conduitModel;
    }

    public static RPModel.View signModel(BlockState blockState) {
        if (modelMap.containsKey(blockState))
            return modelMap.get(blockState);

        var model = SignModel.get(blockState);
        modelMap.put(blockState, model);
        return model;
    }

    public static RPModel.View bellModel(BlockState blockState) {
        if (modelMap.containsKey(blockState))
            return modelMap.get(blockState);

        var model = BellModel.get();
        modelMap.put(blockState, model);
        return model;
    }

    public static RPModel.View cloudModel() {
        RPModel rpModel = new RPModel();
        rpModel.textures = new Object2ObjectOpenHashMap<>();
        rpModel.textures.put("all", RPModel.TextureEntry.of("minecraft:environment/clouds"));
        rpModel.elements = new ObjectArrayList<>();

        var element = new RPElement();
        element.from = new Vector3f(-64, 0, -64);
        element.to = new Vector3f(64, 0, 64);
        element.faces = new Object2ObjectOpenHashMap<>();

        var ti = new RPElement.TextureInfo();
        ti.texture = "#all";
        ti.uv = new Vector4f(0, 0, 16, 16);

        element.faces.put(Direction.UP, ti);

        rpModel.elements.add(element);

        return new RPModel.View(rpModel, new Vector3f(), new Vector3f());
    }


}
