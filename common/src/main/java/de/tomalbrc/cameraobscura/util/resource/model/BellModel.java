package de.tomalbrc.cameraobscura.util.resource.model;

import de.tomalbrc.cameraobscura.model.resource.RPModel;
import de.tomalbrc.cameraobscura.util.resource.RPHelper;

public class BellModel {
    public static RPModel.View get() {
        RPModel rpModel = RPHelper.loadModel(BellModel.class.getResourceAsStream("/builtin/bell.json"));
        return new RPModel.View(rpModel);
    }
}
