package de.tomalbrc.cameraobscura.sore.pipeline;

import org.joml.Vector3d;

public record FrameContext(long time, Vector3d sunDir, double skyLight) {
}
