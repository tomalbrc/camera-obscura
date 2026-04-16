package de.tomalbrc.cameraobscura.util.resource.model;

import de.tomalbrc.cameraobscura.model.resource.RPModel;
import de.tomalbrc.cameraobscura.util.resource.RPHelper;
import net.minecraft.world.phys.Vec3;

public class DecoratedPotModel {
    public static RPModel.View get() {
        RPModel rpModel = RPHelper.loadModel(ChestModel.class.getResourceAsStream("/builtin/decorated_pot.json"));
        return new RPModel.View(rpModel, Vec3.ZERO.toVector3f());
    }
}
