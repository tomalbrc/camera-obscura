package de.tomalbrc.cameraobscura.renderer.entity.specific;

import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;

public class ParchedRenderer extends SkeletonRenderer {
    @Override
    protected String getTexturePath(AbstractSkeleton e) {
        return "entity/skeleton/parched";
    }

    @Override
    protected int getTexHeight() {
        return 64;
    }
}