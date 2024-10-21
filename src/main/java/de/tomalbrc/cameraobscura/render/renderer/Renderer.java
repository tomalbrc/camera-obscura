package de.tomalbrc.cameraobscura.render.renderer;

import de.tomalbrc.cameraobscura.ModConfig;
import net.minecraft.util.Mth;

public interface Renderer<T> {
    float FOV_YAW_RAD = Mth.DEG_TO_RAD * (Mth.clamp(ModConfig.getInstance().fov, 30,110)/2.f);
    float FOV_PITCH_RAD = Mth.DEG_TO_RAD * (Mth.clamp(ModConfig.getInstance().fov, 30,110)/2.f);

    T render();
}
