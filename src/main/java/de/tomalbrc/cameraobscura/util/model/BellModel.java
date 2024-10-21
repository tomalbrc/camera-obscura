package de.tomalbrc.cameraobscura.util.model;

import de.tomalbrc.cameraobscura.render.model.resource.RPElement;
import de.tomalbrc.cameraobscura.render.model.resource.RPModel;
import de.tomalbrc.cameraobscura.util.RPHelper;
import org.joml.Vector3f;

import java.util.Map;

public class BellModel {
    public static RPModel.View get() {
        RPModel rpModel = RPHelper.loadModel(BellModel.class.getResourceAsStream("/builtin/bell.json"));
        for (RPElement element : rpModel.elements) {
            for (Map.Entry<String, RPElement.TextureInfo> entry : element.faces.entrySet()) {
                entry.getValue().uv.mul(2);
            }
        }

        return new RPModel.View(rpModel, new Vector3f());
    }
}
