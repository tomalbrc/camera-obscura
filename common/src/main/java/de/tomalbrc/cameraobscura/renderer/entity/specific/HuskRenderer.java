package de.tomalbrc.cameraobscura.renderer.entity.specific;

import net.minecraft.world.entity.monster.zombie.Husk;

public class HuskRenderer extends ZombieRenderer<Husk> {
    @Override
    protected String getTexturePath(Husk z) {
        return z.isBaby() ? "entity/zombie/husk_baby" : "entity/zombie/husk";
    }
}