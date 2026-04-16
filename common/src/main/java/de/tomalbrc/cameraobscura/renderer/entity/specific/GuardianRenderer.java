package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.LivingEntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4d;

public class GuardianRenderer implements LivingEntityRenderer<Guardian> {
    private static final String TEXTURE = "entity/guardian/guardian";
    private ModelBakery.BakedPart cachedModel;

    @Override
    public ModelBakery.BakedPart buildRoot(Guardian entity) {
        if (cachedModel == null) {
            cachedModel = buildModel();
        }
        return cachedModel;
    }

    private ModelBakery.BakedPart buildModel() {
        ModelBakery bakery = new ModelBakery(TEXTURE, 64, 64);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        ModelBakery.PartDefinition head = root.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-6, 10, -8, 12, 12, 16)
                        .texOffs(0, 28).addBox(-8, 10, -6, 2, 12, 12)
                        .texOffs(0, 28).addBox(6, 10, -6, 2, 12, 12)
                        .texOffs(16, 40).addBox(-6, 8, -6, 12, 2, 12)
                        .texOffs(16, 40).addBox(-6, 22, -6, 12, 2, 12),
                ModelBakery.PartPose.ZERO);

        float[] spikeXRot = {1.75f, 0.25f, 0, 0, 0.5f, 0.5f, 0.5f, 0.5f, 1.25f, 0.75f, 0, 0};
        float[] spikeYRot = {0, 0, 0, 0, 0.25f, 1.75f, 1.25f, 0.75f, 0, 0, 0, 0};
        float[] spikeZRot = {0, 0, 0.25f, 1.75f, 0, 0, 0, 0, 0, 0, 0.75f, 1.25f};
        float[] spikeX = {0, 0, 8, -8, -8, 8, 8, -8, 0, 0, 8, -8};
        float[] spikeY = {-8, -8, -8, -8, 0, 0, 0, 0, 8, 8, 8, 8};
        float[] spikeZ = {8, -8, 0, 0, -8, -8, 8, 8, 8, -8, 0, 0};

        ModelBakery.CubeListBuilder spikeBuilder = ModelBakery.CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1, -4.5f, -1, 2, 9, 2);

        for (int i = 0; i < 12; i++) {
            head.addOrReplaceChild("spike" + i, spikeBuilder,
                    ModelBakery.PartPose.offsetAndRotation(
                            spikeX[i], 16.0f + spikeY[i], spikeZ[i],
                            Mth.PI * spikeXRot[i],
                            Mth.PI * spikeYRot[i],
                            Mth.PI * spikeZRot[i]));
        }

        head.addOrReplaceChild("eye", ModelBakery.CubeListBuilder.create()
                        .texOffs(8, 0).addBox(-1, 15, 0, 2, 2, 1),
                ModelBakery.PartPose.offset(0, 0, -8.25f));

        ModelBakery.PartDefinition tail0 = head.addOrReplaceChild("tail0",
                ModelBakery.CubeListBuilder.create().texOffs(40, 0).addBox(-2, 14, 7, 4, 4, 8),
                ModelBakery.PartPose.ZERO);
        ModelBakery.PartDefinition tail1 = tail0.addOrReplaceChild("tail1",
                ModelBakery.CubeListBuilder.create().texOffs(0, 54).addBox(0, 14, 0, 3, 3, 7),
                ModelBakery.PartPose.offset(-1.5f, 0.5f, 14));
        tail1.addOrReplaceChild("tail2",
                ModelBakery.CubeListBuilder.create()
                        .texOffs(41, 32).addBox(0, 14, 0, 2, 2, 6)
                        .texOffs(25, 19).addBox(1, 10.5f, 3, 1, 9, 9),
                ModelBakery.PartPose.offset(0.5f, 0.5f, 6));

        return root.bake();
    }

    @Override
    public void render(RenderPipeline pipeline, Guardian entity) {
        var pos = entity.position();
        double bodyYaw = entity.getPreciseBodyRotation(1.0f);
        double modelYaw = Mth.DEG_TO_RAD * (180f - bodyYaw);
        double headYaw = Mth.DEG_TO_RAD * (entity.getYHeadRot() - bodyYaw);
        double headPitch = Mth.DEG_TO_RAD * entity.getXRot(1.0f);
        double spikesAnim = entity.getSpikesAnimation(1.0f);
        double tailAnim = entity.getTailAnimation(1.0f);
        double age = entity.tickCount + 1.0f;

        double eyeX, eyeY;
        Vec3 eyePos = entity.getEyePosition(1.0f);
        Vec3 lookDir = entity.getViewVector(1.0f);

        Vec3 targetPos = pipeline.getCamera().position();
        double dy = targetPos.y - eyePos.y;
        eyeY = dy > 0 ? 0 : 1.0f;
        Vec3 dirH = new Vec3(lookDir.x, 0, lookDir.z).normalize();
        Vec3 delta = new Vec3(eyePos.x - targetPos.x, 0, eyePos.z - targetPos.z).normalize().yRot((float) (Math.PI / 2));
        double dot = dirH.dot(delta);
        eyeX = Math.sqrt(Math.abs(dot)) * 2.0f * Math.signum(dot);

        Matrix4d base = new Matrix4d()
                .translate(pos.x, pos.y + 1.5f, pos.z)
                .rotateY(modelYaw)
                .rotateY(Mth.PI)
                .rotateX(Mth.PI);

        double withdrawal = (1.0f - spikesAnim) * 0.55f;

        AnimParams params = new AnimParams(
                headYaw, headPitch, withdrawal, tailAnim, age,
                eyeX, eyeY
        );

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        renderGuardianPart(pipeline, buildRoot(entity), "root", base, params, block, sky);
    }

    private void renderGuardianPart(RenderPipeline pipeline, ModelBakery.BakedPart part, String name,
                                    Matrix4d parent, AnimParams params,
                                    double block, double sky) {

        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        ModelBakery.PartPose ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

        if (name.startsWith("spike")) {
            int idx = Integer.parseInt(name.substring(5));
            double offset = 1.0f + Mth.cos(params.age * 1.5f + idx) * 0.01f - params.spikeWithdrawal;

            double[] spikeX = {0, 0, 8, -8, -8, 8, 8, -8, 0, 0, 8, -8};
            double[] spikeY = {-8, -8, -8, -8, 0, 0, 0, 0, 8, 8, 8, 8};
            double[] spikeZ = {8, -8, 0, 0, -8, -8, 8, 8, 8, -8, 0, 0};

            mat = new Matrix4d(parent);
            mat.translate(spikeX[idx] * offset / 16f, (16f + spikeY[idx] * offset) / 16f, spikeZ[idx] * offset / 16f);

            double[] spikeXRot = {1.75f, 0.25f, 0, 0, 0.5f, 0.5f, 0.5f, 0.5f, 1.25f, 0.75f, 0, 0};
            double[] spikeYRot = {0, 0, 0, 0, 0.25f, 1.75f, 1.25f, 0.75f, 0, 0, 0, 0};
            double[] spikeZRot = {0, 0, 0.25f, 1.75f, 0, 0, 0, 0, 0, 0, 0.75f, 1.25f};
            mat.rotateZYX(Math.PI * spikeZRot[idx], Math.PI * spikeYRot[idx], Math.PI * spikeXRot[idx]);
        } else if (name.equals("eye")) {
            mat.translate(params.eyeX / 16f, params.eyeY / 16f, 0);
        } else if (name.equals("tail0")) {
            mat.rotateY(Mth.sin(params.tailAnim) * Math.PI * 0.05f);
        } else if (name.equals("tail1")) {
            mat.rotateY(Mth.sin(params.tailAnim) * Math.PI * 0.1f);
        } else if (name.equals("tail2")) {
            mat.rotateY(Mth.sin(params.tailAnim) * Math.PI * 0.15f);
        } else if (name.equals("head")) {
            mat.rotateY(params.headYaw);
            mat.rotateX(params.headPitch);
        }

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, IntList.of(0xFFFFFFFF)));
        }

        for (var child : part.children.entrySet()) {
            renderGuardianPart(pipeline, child.getValue(), child.getKey(), mat, params, block, sky);
        }
    }

    private static class AnimParams {
        final double headYaw, headPitch;
        final double spikeWithdrawal, tailAnim, age;
        final double eyeX, eyeY;

        AnimParams(double headYaw, double headPitch, double spikeWithdrawal, double tailAnim, double age,
                   double eyeX, double eyeY) {
            this.headYaw = headYaw;
            this.headPitch = headPitch;
            this.spikeWithdrawal = spikeWithdrawal;
            this.tailAnim = tailAnim;
            this.age = age;
            this.eyeX = eyeX;
            this.eyeY = eyeY;
        }
    }
}