package de.tomalbrc.cameraobscura.renderer.entity.specific;

import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;

public class EnderPearlRenderer extends BillboardItemEntityRenderer<ThrownEnderpearl> {
    public EnderPearlRenderer() {
        super(ThrownEnderpearl::getItem, 1f);
    }
}
