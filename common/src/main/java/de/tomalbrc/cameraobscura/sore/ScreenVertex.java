package de.tomalbrc.cameraobscura.sore;

import de.tomalbrc.cameraobscura.sore.shader.VertexOut;

public record ScreenVertex(VertexOut v, double x, double y, double z, double invW) {
}
