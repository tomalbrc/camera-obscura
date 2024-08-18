package de.tomalbrc.cameraobscura.util.model;

import de.tomalbrc.cameraobscura.render.model.resource.RPElement;
import de.tomalbrc.cameraobscura.render.model.resource.RPModel;
import de.tomalbrc.cameraobscura.util.RPHelper;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.Optional;

public class ShulkerModel {

    public static RPModel.View get(Optional<DyeColor> dyeColor) {
        RPModel rpModel = RPHelper.loadModelView(ChestModel.class.getResourceAsStream("/builtin/shulker.json"));

        dyeColor.ifPresentOrElse(
                color -> rpModel.textures.put("0", "minecraft:entity/shulker/shulker_" + color.getName()),
                () -> rpModel.textures.put("0", "minecraft:entity/shulker/shulker"));

        for (RPElement element : rpModel.elements) {
            for (Map.Entry<String, RPElement.TextureInfo> entry : element.faces.entrySet()) {
                entry.getValue().uv.mul(4.f);
            }
        }

        return new RPModel.View(rpModel, Vec3.ZERO.toVector3f());
    }
}
