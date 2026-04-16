package de.tomalbrc.cameraobscura.renderer.entity.specific;

import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;

public class SnowballRenderer extends BillboardItemEntityRenderer<Snowball> {
    public SnowballRenderer() {
        super(Snowball::getItem, 1.f);
    }
}
