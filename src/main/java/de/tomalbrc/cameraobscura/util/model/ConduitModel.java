package de.tomalbrc.cameraobscura.util.model;

import de.tomalbrc.cameraobscura.render.model.resource.RPElement;
import de.tomalbrc.cameraobscura.render.model.resource.RPModel;
import de.tomalbrc.cameraobscura.util.RPHelper;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public class ConduitModel {
    public static RPModel.View get() {
        RPModel rpModel = RPHelper.loadModelView(ChestModel.class.getResourceAsStream("/builtin/conduit.json"));
        for (RPElement element : rpModel.elements) {
            for (Map.Entry<String, RPElement.TextureInfo> entry : element.faces.entrySet()) {
                if (element.name == null || !element.name.equals("eye")) {
                    entry.getValue().uv.mul(2,1,2,1); // might not be correct
                }
            }
        }

        return new RPModel.View(rpModel, Vec3.ZERO.toVector3f());
    }
}
