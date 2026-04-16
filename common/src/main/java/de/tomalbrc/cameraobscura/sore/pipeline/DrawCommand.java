package de.tomalbrc.cameraobscura.sore.pipeline;

import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.util.Constants;
import it.unimi.dsi.fastutil.ints.IntList;
import org.joml.Matrix4dc;
import org.joml.Vector3d;

public record DrawCommand(RenderType renderType, Model model, Matrix4dc transform, IntList tints) {
    public DrawCommand(RenderType renderType, Model model, Matrix4dc transform) {
        this(renderType, model, transform, IntList.of());
    }

    public DrawCommand(RenderType renderType, Model model) {
        this(renderType, model, Constants.ZERO_MATRIX);
    }

    public Vector3d worldPosition() {
        Vector3d pos = new Vector3d();
        transform.getTranslation(pos);
        return pos;
    }
}
