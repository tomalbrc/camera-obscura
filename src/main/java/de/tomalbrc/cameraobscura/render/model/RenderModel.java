package de.tomalbrc.cameraobscura.render.model;

import net.minecraft.core.Direction;
import org.joml.Vector3f;

public interface RenderModel {
    public record ModelHitResult(int color, Direction direction, boolean shade) {}

    public RenderModel.ModelHitResult intersect(Vector3f origin, Vector3f direction, Vector3f offset, int textureTint);
}
