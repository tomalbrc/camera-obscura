package de.tomalbrc.cameraobscura.render.renderer;

import net.minecraft.util.Mth;

public interface Renderer<T> {
    float FOV_YAW_DEG = 53;
    float FOV_PITCH_DEG = 40;

    float FOV_YAW_RAD = Mth.DEG_TO_RAD * FOV_YAW_DEG;
    float FOV_PITCH_RAD = Mth.DEG_TO_RAD * FOV_PITCH_DEG;

    T render();
}
