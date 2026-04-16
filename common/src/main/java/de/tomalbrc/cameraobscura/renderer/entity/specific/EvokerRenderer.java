package de.tomalbrc.cameraobscura.renderer.entity.specific;

import net.minecraft.world.entity.monster.illager.Evoker;

public class EvokerRenderer extends IllagerRenderer<Evoker> {
    @Override
    protected String getTexture(Evoker entity) {
        return "entity/illager/evoker";
    }
}
