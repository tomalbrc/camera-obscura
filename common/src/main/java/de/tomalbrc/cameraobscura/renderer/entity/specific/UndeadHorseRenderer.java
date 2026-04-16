package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.equine.AbstractHorse;

import java.util.HashMap;
import java.util.Map;

public class UndeadHorseRenderer extends AbstractEquineRenderer<AbstractHorse> {

    private static final Map<EntityType<?>, String[]> TEXTURE_MAP = Map.of(
            EntityType.SKELETON_HORSE, new String[]{
                    "entity/horse/horse_skeleton",
                    "entity/horse/horse_skeleton_baby"
            },
            EntityType.ZOMBIE_HORSE, new String[]{
                    "entity/horse/horse_zombie",
                    "entity/horse/horse_zombie_baby"
            }
    );

    private final Map<String, ModelBakery.BakedPart> cache = new HashMap<>();

    @Override
    protected String getTexture(AbstractHorse entity) {
        String[] paths = TEXTURE_MAP.get(entity.getType());
        if (paths == null) return "entity/horse/horse_white"; // fallback
        return entity.isBaby() ? paths[1] : paths[0];
    }

    @Override
    public ModelBakery.BakedPart buildRoot(AbstractHorse entity) {
        String tex = getTexture(entity);
        return cache.computeIfAbsent(tex, k ->
                entity.isBaby() ? buildBabyBase(k) : buildAdultBase(k)
        );
    }
}