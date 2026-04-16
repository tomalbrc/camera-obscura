package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.illager.Illusioner;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4d;

public class IllusionerRenderer extends IllagerRenderer<Illusioner> {

    @Override
    protected String getTexture(Illusioner entity) {
        return "entity/illager/illusioner";
    }

    @Override
    protected boolean showHat() {
        return true;
    }

    @Override
    public void render(RenderPipeline pipeline, Illusioner entity) {
        if (entity.isInvisible()) {
            Vec3[] offsets = entity.getIllusionOffsets(1.0f);
            double ageInTicks = entity.tickCount + 1.0f;
            for (int i = 0; i < offsets.length; i++) {
                Matrix4d base = computeBaseMatrix(entity);

                base.translate(
                        (double) (offsets[i].x + Mth.cos(i + ageInTicks * 0.5f) * 0.025),
                        (double) (offsets[i].y + Mth.cos(i + ageInTicks * 0.75f) * 0.0125),
                        (double) (offsets[i].z + Mth.cos(i + ageInTicks * 0.7f) * 0.025)
                );
                renderSingle(pipeline, entity, base);
            }
        } else {
            super.render(pipeline, entity);
        }
    }
}