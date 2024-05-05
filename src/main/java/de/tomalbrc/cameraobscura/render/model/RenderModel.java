package de.tomalbrc.cameraobscura.render.model;

import net.minecraft.core.Direction;
import org.joml.Vector3f;

import java.util.List;

public interface RenderModel {

    record ModelHit(int color, Direction direction, boolean shade) {}

    List<ModelHit> intersect(Vector3f origin, Vector3f direction, Vector3f offset, int textureTint);

    record View(RenderModel renderModel, RenderModel renderModelWithWater) {}
}
