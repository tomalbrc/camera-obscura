package de.tomalbrc.cameraobscura.renderer.entity.specific;

import net.minecraft.world.entity.projectile.hurtingprojectile.Fireball;

public class FireballRenderer extends BillboardItemEntityRenderer<Fireball> {
    public FireballRenderer(double scale) {
        super(Fireball::getItem, scale);
    }
}
