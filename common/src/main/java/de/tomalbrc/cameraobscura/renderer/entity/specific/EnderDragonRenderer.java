package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.EntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.enderdragon.DragonFlightHistory;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import org.joml.Matrix4d;

public class EnderDragonRenderer implements EntityRenderer<EnderDragon> {

    private static final String TEXTURE = "entity/enderdragon/dragon";
    private static final int TEX_WIDTH = 256, TEX_HEIGHT = 256;
    private static final int NECK = 5;
    private static final int TAIL = 12;
    private static ModelBakery.BakedPart cachedModel;

    private static ModelBakery.BakedPart buildModel() {
        ModelBakery bakery = new ModelBakery(TEXTURE, TEX_WIDTH, TEX_HEIGHT);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        var head = root.addOrReplaceChild("head", ModelBakery.CubeListBuilder.create()
                        .texOffs(176, 44).addBox(-6, -1, -24, 12, 5, 16)
                        .texOffs(112, 30).addBox(-8, -8, -10, 16, 16, 16)
                        .texOffs(0, 0).addBox(-5, -12, -4, 2, 4, 6).addBox(3, -12, -4, 2, 4, 6)
                        .texOffs(112, 0).addBox(-5, -3, -22, 2, 2, 4).addBox(3, -3, -22, 2, 2, 4),
                ModelBakery.PartPose.offset(0, 20, -62));
        head.addOrReplaceChild("jaw", ModelBakery.CubeListBuilder.create()
                        .texOffs(176, 65).addBox(-6, 0, -16, 12, 4, 16),
                ModelBakery.PartPose.offset(0, 4, -8));

        var spine = ModelBakery.CubeListBuilder.create()
                .texOffs(192, 104).addBox(-5, -5, -5, 10, 10, 10)
                .texOffs(48, 0).addBox(-1, -9, -3, 2, 4, 6);
        for (int i = 0; i < NECK; i++)
            root.addOrReplaceChild("neck" + i, spine, ModelBakery.PartPose.offset(0, 20, -12 - i * 10));
        for (int i = 0; i < TAIL; i++)
            root.addOrReplaceChild("tail" + i, spine, ModelBakery.PartPose.offset(0, 10, 60 + i * 10));

        var body = root.addOrReplaceChild("body", ModelBakery.CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-12, 1, -16, 24, 24, 64)
                        .texOffs(220, 53).addBox(-1, -5, -10, 2, 6, 12).addBox(-1, -5, 10, 2, 6, 12).addBox(-1, -5, 30, 2, 6, 12),
                ModelBakery.PartPose.offset(0, 3, 8));

        var lw = body.addOrReplaceChild("left_wing", ModelBakery.CubeListBuilder.create().mirror()
                        .texOffs(112, 88).addBox(0, -4, -4, 56, 8, 8)
                        .texOffs(-56, 88).addBox(0, 0, 2, 56, 0, 56),
                ModelBakery.PartPose.offset(12, 2, -6));
        lw.addOrReplaceChild("left_wing_tip", ModelBakery.CubeListBuilder.create().mirror()
                        .texOffs(112, 136).addBox(0, -2, -2, 56, 4, 4)
                        .texOffs(-56, 144).addBox(0, 0, 2, 56, 0, 56),
                ModelBakery.PartPose.offset(56, 0, 0));

        var rw = body.addOrReplaceChild("right_wing", ModelBakery.CubeListBuilder.create()
                        .texOffs(112, 88).addBox(-56, -4, -4, 56, 8, 8)
                        .texOffs(-56, 88).addBox(-56, 0, 2, 56, 0, 56),
                ModelBakery.PartPose.offset(-12, 2, -6));
        rw.addOrReplaceChild("right_wing_tip", ModelBakery.CubeListBuilder.create()
                        .texOffs(112, 136).addBox(-56, -2, -2, 56, 4, 4)
                        .texOffs(-56, 144).addBox(-56, 0, 2, 56, 0, 56),
                ModelBakery.PartPose.offset(-56, 0, 0));

        addLeg(body, "left_front", 12, 17, -6, 1.3f, -0.5f, 0.75f);
        addLeg(body, "right_front", -12, 17, -6, 1.3f, -0.5f, 0.75f);

        addLeg(body, "left_hind", 16, 13, 34, 1.0f, 0.5f, 0.75f);
        addLeg(body, "right_hind", -16, 13, 34, 1.0f, 0.5f, 0.75f);

        return root.bake();
    }

    private static void addLeg(ModelBakery.PartDefinition body, String name,
                               float x, float y, float z,
                               float legXRot, float legTipXRot, float footXRot) {
        var leg = body.addOrReplaceChild(name + "_leg",
                ModelBakery.CubeListBuilder.create().texOffs(112, 104).addBox(-4, -4, -4, 8, 24, 8),
                ModelBakery.PartPose.offsetAndRotation(x, y, z, legXRot, 0, 0));
        var tip = leg.addOrReplaceChild(name + "_leg_tip",
                ModelBakery.CubeListBuilder.create().texOffs(226, 138).addBox(-3, -1, -3, 6, 24, 6),
                ModelBakery.PartPose.offsetAndRotation(0, 20, -1, legTipXRot, 0, 0));
        tip.addOrReplaceChild(name + "_foot",
                ModelBakery.CubeListBuilder.create().texOffs(144, 104).addBox(-4, 0, -12, 8, 4, 16),
                ModelBakery.PartPose.offsetAndRotation(0, 23, 0, footXRot, 0, 0));
    }

    private double getHeadPartYOffset(EnderDragon dragon, int part,
                                      DragonFlightHistory.Sample body,
                                      DragonFlightHistory.Sample partPos,
                                      double partialTicks) {
        boolean landing = false, sitting = false;
        if (dragon.getPhaseManager() != null) {
            var phase = dragon.getPhaseManager().getCurrentPhase();
            landing = phase.getPhase() == EnderDragonPhase.LANDING || phase.getPhase() == EnderDragonPhase.TAKEOFF;
            sitting = phase.isSitting();
        }
        double dist = dragon.level()
                .getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                        EndPodiumFeature.getLocation(dragon.getFightOrigin()))
                .distToCenterSqr(dragon.position());

        if (landing) return (double) (part / Math.max(dist / 4.0, 1.0));
        if (sitting) return part;
        if (part == 6) return 0f;
        return (double) (partPos.y() - body.y());
    }

    @Override
    public void render(RenderPipeline pipeline, EnderDragon dragon) {
        if (cachedModel == null) cachedModel = buildModel();

        float pTicks = 1f;
        DragonFlightHistory hist = dragon.flightHistory;
        double flapTime = Mth.lerp(pTicks, dragon.oFlapTime, dragon.flapTime);
        double flapRad = flapTime * (double) (Math.PI * 2);

        double yr = hist.get(7, pTicks).yRot();
        double r2 = (double) (hist.get(5, pTicks).y() - hist.get(10, pTicks).y());
        Matrix4d base = new Matrix4d()
                .translate(dragon.position().toVector3f())
                .translate(0, 3.1f, -3.0f)
                .rotateY(Mth.DEG_TO_RAD * -yr)
                .rotateX(Mth.DEG_TO_RAD * r2 * 10f)
                .translate(0, 0, 1)
                .scale(-1, -1, 1);

        double bounce = Mth.sin(flapRad - 1) + 1;
        bounce = (bounce * bounce + bounce * 2) * 0.05f;

        DragonFlightHistory.Sample startNeck = hist.get(6, pTicks);
        double rot = Mth.wrapDegrees(hist.get(5, pTicks).yRot()
                + Mth.wrapDegrees(hist.get(5, pTicks).yRot() - hist.get(10, pTicks).yRot()) / 2f);
        double xx = 0, yy = 20, zz = -12;

        Anim a = new Anim();
        a.base = base;
        a.bounce = bounce;
        a.flapRad = flapRad;

        for (int i = 0; i < NECK; i++) {
            DragonFlightHistory.Sample pt = hist.get(5 - i, pTicks);
            double yaw = Mth.wrapDegrees(pt.yRot() - startNeck.yRot()) * Mth.DEG_TO_RAD * 1.5f;
            double pitch = Mth.cos(i * 0.45f + flapRad) * 0.15f
                    + getHeadPartYOffset(dragon, i, startNeck, pt, pTicks) * Mth.DEG_TO_RAD * 1.5f * 5f;
            double roll = -Mth.wrapDegrees(pt.yRot() - rot) * Mth.DEG_TO_RAD * 1.5f;

            a.nx[i] = xx;
            a.ny[i] = yy;
            a.nz[i] = zz;
            a.nxRot[i] = pitch;
            a.nyRot[i] = yaw;
            a.nzRot[i] = roll;

            // advance
            xx -= Mth.sin(yaw) * Mth.cos(pitch) * 10;
            yy += Mth.sin(pitch) * 10;
            zz -= Mth.cos(yaw) * Mth.cos(pitch) * 10;
        }

        DragonFlightHistory.Sample cur = hist.get(0, pTicks);
        a.hx = xx;
        a.hy = yy;
        a.hz = zz;
        a.hxRot = getHeadPartYOffset(dragon, 6, startNeck, cur, pTicks) * Mth.DEG_TO_RAD * 1.5f * 5f;
        a.hyRot = Mth.wrapDegrees(cur.yRot() - startNeck.yRot()) * Mth.DEG_TO_RAD;
        a.hzRot = -Mth.wrapDegrees(cur.yRot() - rot) * Mth.DEG_TO_RAD;
        a.jaw = (Mth.sin(flapRad) + 1) * 0.2f;

        DragonFlightHistory.Sample startTail = hist.get(11, pTicks);
        double tx = 0, ty = 10, tz = 60;
        double tp = 0;
        for (int i = 0; i < TAIL; i++) {
            DragonFlightHistory.Sample pt = hist.get(12 + i, pTicks);
            tp += Mth.sin(i * 0.45f + flapRad) * 0.05f;
            double yaw = (Mth.wrapDegrees(pt.yRot() - startTail.yRot()) * 1.5f + 180) * Mth.DEG_TO_RAD;
            double pitch = tp + (double) (pt.y() - startTail.y()) * Mth.DEG_TO_RAD * 1.5f * 5f;
            double roll = Mth.wrapDegrees(pt.yRot() - rot) * Mth.DEG_TO_RAD * 1.5f;

            a.tx[i] = tx;
            a.ty[i] = ty;
            a.tz[i] = tz;
            a.txRot[i] = pitch;
            a.tyRot[i] = yaw;
            a.tzRot[i] = roll;

            tx -= Mth.sin(yaw) * Mth.cos(pitch) * 10;
            ty += Mth.sin(pitch) * 10;
            tz -= Mth.cos(yaw) * Mth.cos(pitch) * 10;
        }

        a.bodyZ = -Mth.wrapDegrees(hist.get(5, pTicks).yRot() - hist.get(10, pTicks).yRot()) * 1.5f * Mth.DEG_TO_RAD;

        a.lwX = 0.125f - Mth.cos(flapRad) * 0.2f;
        a.lwZ = -(Mth.sin(flapRad) + 0.125f) * 0.8f;
        a.lwTipZ = (Mth.sin(flapRad + 2) + 0.5f) * 0.75f;

        renderPart(pipeline, cachedModel, "root", new Matrix4d(), a);
    }

    private void renderPart(RenderPipeline pipeline, ModelBakery.BakedPart part,
                            String name, Matrix4d parent, Anim a) {
        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        ModelBakery.PartPose ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

        switch (name) {
            case "head":
                mat.translate(a.hx / 16f - part.localPivot.x,
                        a.hy / 16f - part.localPivot.y,
                        a.hz / 16f - part.localPivot.z);
                mat.rotateZYX(a.hzRot, a.hyRot, a.hxRot);
                break;
            case "jaw":
                mat.rotateX(a.jaw);
                break;
            case "body":
                mat.rotateZ(a.bodyZ);
                mat.rotateX(a.bounce * 2f * Mth.DEG_TO_RAD);
                break;
            case "left_wing":
                mat.rotateX(a.lwX);
                mat.rotateY(-0.25f);
                mat.rotateZ(a.lwZ);
                break;
            case "left_wing_tip":
                mat.rotateZ(a.lwTipZ);
                break;
            case "right_wing":
                mat.rotateX(a.lwX);
                mat.rotateY(0.25f);
                mat.rotateZ(-a.lwZ);
                break;
            case "right_wing_tip":
                mat.rotateZ(-a.lwTipZ);
                break;

            default:
                if (name.startsWith("neck")) {
                    int i = Integer.parseInt(name.substring(4));
                    if (i >= 0 && i < NECK) {
                        mat.translate(a.nx[i] / 16f - part.localPivot.x,
                                a.ny[i] / 16f - part.localPivot.y,
                                a.nz[i] / 16f - part.localPivot.z);
                        mat.rotateZYX(a.nzRot[i], a.nyRot[i], a.nxRot[i]);
                    }
                } else if (name.startsWith("tail")) {
                    int i = Integer.parseInt(name.substring(4));
                    if (i >= 0 && i < TAIL) {
                        mat.translate(a.tx[i] / 16f - part.localPivot.x,
                                a.ty[i] / 16f - part.localPivot.y,
                                a.tz[i] / 16f - part.localPivot.z);
                        mat.rotateZYX(a.tzRot[i], a.tyRot[i], a.txRot[i]);
                    }
                } else if (name.contains("leg") || name.contains("foot")) {
                    double add = a.bounce * 0.1f;
                    if (name.contains("front_leg_tip"))
                        add = -add;
                    mat.rotateX(add);
                }
                break;
        }

        Matrix4d world = new Matrix4d(a.base).mul(mat);
        if (part.mesh != null)
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh), world, IntList.of(0xFFFFFFFF)));

        for (var child : part.children.entrySet())
            renderPart(pipeline, child.getValue(), child.getKey(), mat, a);
    }

    private static class Anim {
        Matrix4d base;
        double bounce, flapRad;

        double hx, hy, hz, hxRot, hyRot, hzRot, jaw;

        double[] nx = new double[NECK], ny = new double[NECK], nz = new double[NECK];
        double[] nxRot = new double[NECK], nyRot = new double[NECK], nzRot = new double[NECK];

        double[] tx = new double[TAIL], ty = new double[TAIL], tz = new double[TAIL];
        double[] txRot = new double[TAIL], tyRot = new double[TAIL], tzRot = new double[TAIL];

        double lwX, lwY, lwZ, lwTipZ;

        double bodyZ;
    }
}