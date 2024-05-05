package de.tomalbrc.cameraobscura.render.renderer;

public interface Renderer<T> {
    double FOV_YAW_DEG = 53;
    double FOV_PITCH_DEG = 40;

    double FOV_YAW_RAD = Math.toRadians(FOV_YAW_DEG);
    double FOV_PITCH_RAD = Math.toRadians(FOV_PITCH_DEG);

    T render();
}
