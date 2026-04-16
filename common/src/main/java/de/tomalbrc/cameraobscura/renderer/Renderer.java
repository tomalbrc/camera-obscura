package de.tomalbrc.cameraobscura.renderer;

import de.tomalbrc.cameraobscura.ModConfig;
import net.minecraft.util.Mth;

public interface Renderer<T> {
    double FOV_RAD = Mth.DEG_TO_RAD * (Mth.clamp(ModConfig.getInstance().fov, 30, 110) / 2.f);

    T render();
}
