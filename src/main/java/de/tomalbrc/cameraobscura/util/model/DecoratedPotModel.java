package de.tomalbrc.cameraobscura.util.model;

import de.tomalbrc.cameraobscura.render.model.resource.RPElement;
import de.tomalbrc.cameraobscura.render.model.resource.RPModel;
import de.tomalbrc.cameraobscura.util.RPHelper;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public class DecoratedPotModel {
    public static RPModel.View get() {
        RPModel rpModel = RPHelper.loadModelView(ChestModel.class.getResourceAsStream("/builtin/decorated_pot.json"));
        for (RPElement element : rpModel.elements) {
            for (Map.Entry<String, RPElement.TextureInfo> entry : element.faces.entrySet()) {
                if (element.name == null || !(element.name.equals("left") || element.name.equals("right") || element.name.equals("back") || element.name.equals("front")))
                    entry.getValue().uv.mul(2.f);
            }
        }

        return new RPModel.View(rpModel, Vec3.ZERO.toVector3f());
    }
}
