package de.tomalbrc.cameraobscura.renderer.animation;

import org.joml.Vector3dc;

public record Keyframe(double timestamp, Vector3dc preTarget, Vector3dc postTarget,
                       AnimationChannel.Interpolation interpolation) {
    public Keyframe(final double timestamp, final Vector3dc postTarget, final AnimationChannel.Interpolation interpolation) {
        this(timestamp, postTarget, postTarget, interpolation);
    }
}
