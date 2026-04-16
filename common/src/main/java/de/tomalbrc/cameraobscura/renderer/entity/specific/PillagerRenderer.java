package de.tomalbrc.cameraobscura.renderer.entity.specific;

import net.minecraft.world.entity.monster.illager.Pillager;

public class PillagerRenderer extends IllagerRenderer<Pillager> {
    @Override
    protected String getTexture(Pillager entity) {
        return "entity/illager/pillager";
    }
}
