package de.tomalbrc.cameraobscura.renderer.entity.specific;

import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;

public class WanderingTraderRenderer extends AbstractVillagerRenderer<WanderingTrader> {
    private static final String TEXTURE = "entity/wandering_trader/wandering_trader";

    @Override
    protected String getAdultTexture(WanderingTrader entity) {
        return TEXTURE;
    }

    @Override
    protected String getBabyTexture(WanderingTrader entity) {
        return TEXTURE;
    }
}