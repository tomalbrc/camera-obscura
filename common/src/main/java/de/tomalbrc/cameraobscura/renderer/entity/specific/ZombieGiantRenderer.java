package de.tomalbrc.cameraobscura.renderer.entity.specific;

import net.minecraft.world.entity.monster.Giant;

public class ZombieGiantRenderer extends ZombieRenderer<Giant> {
    @Override
    protected double scale() {
        return 6f;
    }
}
