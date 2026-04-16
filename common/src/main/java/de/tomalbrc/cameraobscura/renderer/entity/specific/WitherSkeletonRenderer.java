package de.tomalbrc.cameraobscura.renderer.entity.specific;

import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;

public class WitherSkeletonRenderer extends SkeletonRenderer {
    @Override
    protected String getTexturePath(AbstractSkeleton e) {
        return "entity/skeleton/wither_skeleton";
    }

    @Override
    protected double scale() {
        return 1.2f;
    }
}
