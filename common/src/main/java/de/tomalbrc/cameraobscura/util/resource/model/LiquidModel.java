package de.tomalbrc.cameraobscura.util.resource.model;

import de.tomalbrc.cameraobscura.json.CachedIdentifierDeserializer;
import de.tomalbrc.cameraobscura.model.resource.RPElement;
import de.tomalbrc.cameraobscura.model.resource.RPModel;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class LiquidModel {
    public static RPModel.View get(FluidState fluidState, FluidState fluidStateAbove) {
        int height = !fluidStateAbove.isEmpty() ? 16 : (fluidState.getAmount() - 1) * 2;

        RPModel rpModel = new RPModel();
        rpModel.parent = CachedIdentifierDeserializer.get("minecraft:block/cube_all");
        rpModel.textures = new Object2ObjectOpenHashMap<>();
        if (fluidState.is(FluidTags.WATER)) {
            rpModel.textures.put("top", RPModel.TextureEntry.of("minecraft:block/water_still"));
            rpModel.textures.put("side", RPModel.TextureEntry.of("minecraft:block/water_flow"));
        } else {
            rpModel.textures.put("top", RPModel.TextureEntry.of("minecraft:block/lava_still"));
            rpModel.textures.put("side", RPModel.TextureEntry.of("minecraft:block/lava_flow"));
        }
        rpModel.elements = new ObjectArrayList<>();
        var element = new RPElement();
        element.from = new Vector3f(0, 0, 0);
        element.to = new Vector3f(16, height, 16);
        element.faces = new Object2ObjectOpenHashMap<>();
        element.shade = false;

        var ti = new RPElement.TextureInfo();
        ti.texture = "#top";
        ti.cullface = Direction.UP;
        ti.tintIndex = 0;
        ti.uv = new Vector4f(0, 0, 16, 16);

        var tiDown = new RPElement.TextureInfo();
        tiDown.texture = "#top";
        tiDown.cullface = Direction.DOWN;
        tiDown.tintIndex = 0;
        tiDown.uv = new Vector4f(0, 0, 16, 16);

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            var tiSide = new RPElement.TextureInfo();
            tiSide.texture = "#side";
            tiSide.uv = new Vector4f(0, 0, 16, height);
            tiSide.tintIndex = 0;
            tiSide.cullface = direction;
            element.faces.put(direction, tiSide);
        }

        element.faces.put(Direction.UP, ti);
        element.faces.put(Direction.DOWN, tiDown);

        rpModel.elements.add(element);

        return new RPModel.View(rpModel, Vec3.ZERO.toVector3f());
    }
}
