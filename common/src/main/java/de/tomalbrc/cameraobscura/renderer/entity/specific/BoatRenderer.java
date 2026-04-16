package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.EntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.level.LightLayer;
import org.joml.Matrix4d;
import org.joml.Quaterniond;

import java.util.HashMap;
import java.util.Map;


public class BoatRenderer implements EntityRenderer<AbstractBoat> {
    public static final String ACACIA_BOAT = "entity/boat/acacia";
    public static final String ACACIA_CHEST_BOAT = "entity/chest_boat/acacia";
    public static final String BIRCH_BOAT = "entity/boat/birch";
    public static final String BIRCH_CHEST_BOAT = "entity/chest_boat/birch";
    public static final String CHERRY_BOAT = "entity/boat/cherry";
    public static final String CHERRY_CHEST_BOAT = "entity/chest_boat/cherry";
    public static final String DARK_OAK_BOAT = "entity/boat/dark_oak";
    public static final String DARK_OAK_CHEST_BOAT = "entity/chest_boat/dark_oak";
    public static final String JUNGLE_BOAT = "entity/boat/jungle";
    public static final String JUNGLE_CHEST_BOAT = "entity/chest_boat/jungle";
    public static final String MANGROVE_BOAT = "entity/boat/mangrove";
    public static final String MANGROVE_CHEST_BOAT = "entity/chest_boat/mangrove";
    public static final String OAK_BOAT = "entity/boat/oak";
    public static final String OAK_CHEST_BOAT = "entity/chest_boat/oak";
    public static final String PALE_OAK_BOAT = "entity/boat/pale_oak";
    public static final String PALE_OAK_CHEST_BOAT = "entity/chest_boat/pale_oak";
    public static final String SPRUCE_BOAT = "entity/boat/spruce";
    public static final String SPRUCE_CHEST_BOAT = "entity/chest_boat/spruce";
    public static final String BAMBOO_RAFT = "entity/boat/bamboo";
    public static final String BAMBOO_CHEST_RAFT = "entity/chest_boat/bamboo";

    private static final Map<String, ModelBakery.BakedPart> MODEL_CACHE = new HashMap<>();

    private final String texture;
    private final boolean isRaft;
    private final boolean hasChest;

    public BoatRenderer(String texture, boolean isRaft, boolean hasChest) {
        this.texture = texture;
        this.isRaft = isRaft;
        this.hasChest = hasChest;
    }

    @Override
    public void render(RenderPipeline pipeline, AbstractBoat entity) {
        float partialTicks = 1.0f;
        double yRot = entity.getYRot(partialTicks);
        double hurtTime = entity.getHurtTime() - partialTicks;
        double damageTime = Math.max(entity.getDamage() - partialTicks, 0.0f);
        double bubbleAngle = entity.getBubbleAngle(partialTicks);
        boolean isUnderWater = entity.isUnderWater();
        double rowLeft = entity.getRowingTime(0, partialTicks);
        double rowRight = entity.getRowingTime(1, partialTicks);
        double hurtDir = entity.getHurtDir();

        String cacheKey = texture + "_" + isRaft + "_" + hasChest;
        ModelBakery.BakedPart model = MODEL_CACHE.computeIfAbsent(cacheKey, k -> buildModel());

        Matrix4d base = new Matrix4d()
                .translate(entity.position().toVector3f())
                .translate(0.0f, 0.375f, 0.0f)
                .rotateY(Mth.DEG_TO_RAD * (180.0f - yRot));

        if (hurtTime > 0.0f) {
            double hurtRot = Mth.sin(hurtTime) * hurtTime * damageTime / 10.0f * hurtDir;
            base.rotateX(hurtRot * Mth.DEG_TO_RAD);
        }
        if (!isUnderWater && !Mth.equal(bubbleAngle, 0.0f)) {
            base.rotate(new Quaterniond().setAngleAxis(bubbleAngle * Mth.DEG_TO_RAD, 1.0f, 0.0f, 1.0f));
        }
        base.scale(-1.0f, -1.0f, 1.0f);
        base.rotateY(Mth.DEG_TO_RAD * 90.0f);

        var block = entity.level().getBrightness(LightLayer.BLOCK, entity.blockPosition()) / 15f;
        var sky = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) / 15f;

        renderPart(pipeline, model, "root", base, rowLeft, rowRight, block, sky);
    }

    private void renderPart(RenderPipeline pipeline, ModelBakery.BakedPart part, String name,
                            Matrix4d parent, double rowLeft, double rowRight,
                            double block, double sky) {

        Matrix4d mat = new Matrix4d(parent);
        mat.translate(part.localPivot.x, part.localPivot.y, part.localPivot.z);

        ModelBakery.PartPose ip = part.initialPose;
        if (ip.xRot() != 0 || ip.yRot() != 0 || ip.zRot() != 0)
            mat.rotateZYX(ip.zRot(), ip.yRot(), ip.xRot());
        if (ip.xScale() != 1 || ip.yScale() != 1 || ip.zScale() != 1)
            mat.scale(ip.xScale(), ip.yScale(), ip.zScale());

        if (name.equals("left_paddle")) {
            double xRot = Mth.clampedLerp((Mth.sin(-rowLeft) + 1.0f) / 2.0f, -Mth.PI / 3, -Mth.PI / 12);
            double yRot = Mth.clampedLerp((Mth.sin(-rowLeft + 1.0f) + 1.0f) / 2.0f, -Mth.PI / 4, Mth.PI / 4);
            mat.rotateY(yRot);
            mat.rotateX(xRot);
        } else if (name.equals("right_paddle")) {
            double xRot = Mth.clampedLerp((Mth.sin(-rowRight) + 1.0f) / 2.0f, -Mth.PI / 3, -Mth.PI / 12);
            double yRot = Mth.clampedLerp((Mth.sin(-rowRight + 1.0f) + 1.0f) / 2.0f, -Mth.PI / 4, Mth.PI / 4);
            mat.rotateY(-yRot);
            mat.rotateX(xRot);
        }

        if (part.mesh != null) {
            pipeline.draw(new DrawCommand(RenderType.ENTITY, new Model(part.mesh, block, sky), mat, IntList.of(0xFFFFFFFF)));
        }
        for (var child : part.children.entrySet()) {
            renderPart(pipeline, child.getValue(), child.getKey(), mat, rowLeft, rowRight, block, sky);
        }
    }

    private ModelBakery.BakedPart buildModel() {
        ModelBakery bakery = new ModelBakery(texture, hasChest ? 128 : 128, hasChest ? 128 : 64);
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        if (isRaft) {
            buildRaftParts(root);
        } else {
            buildBoatParts(root);
        }
        if (hasChest) {
            buildChestParts(root);
        }
        return root.bake();
    }

    private void buildBoatParts(ModelBakery.PartDefinition root) {
        root.addOrReplaceChild("bottom",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-14, -9, -3, 28, 16, 3),
                ModelBakery.PartPose.offsetAndRotation(0, 3, 1, Mth.PI / 2, 0, 0));
        root.addOrReplaceChild("back",
                ModelBakery.CubeListBuilder.create().texOffs(0, 19).addBox(-13, -7, -1, 18, 6, 2),
                ModelBakery.PartPose.offsetAndRotation(-15, 4, 4, 0, Mth.PI * 3 / 2, 0));
        root.addOrReplaceChild("front",
                ModelBakery.CubeListBuilder.create().texOffs(0, 27).addBox(-8, -7, -1, 16, 6, 2),
                ModelBakery.PartPose.offsetAndRotation(15, 4, 0, 0, Mth.PI / 2, 0));
        root.addOrReplaceChild("right",
                ModelBakery.CubeListBuilder.create().texOffs(0, 35).addBox(-14, -7, -1, 28, 6, 2),
                ModelBakery.PartPose.offsetAndRotation(0, 4, -9, 0, Mth.PI, 0));
        root.addOrReplaceChild("left",
                ModelBakery.CubeListBuilder.create().texOffs(0, 43).addBox(-14, -7, -1, 28, 6, 2),
                ModelBakery.PartPose.offset(0, 4, 9));
        root.addOrReplaceChild("left_paddle",
                ModelBakery.CubeListBuilder.create().texOffs(62, 0).addBox(-1, 0, -5, 2, 2, 18).addBox(-1.001f, -3, 8, 1, 6, 7),
                new ModelBakery.PartPose(3, -5, 9, 0, 0, Mth.PI / 16, 1, 1, 1));
        root.addOrReplaceChild("right_paddle",
                ModelBakery.CubeListBuilder.create().texOffs(62, 20).addBox(-1, 0, -5, 2, 2, 18).addBox(0.001f, -3, 8, 1, 6, 7),
                new ModelBakery.PartPose(3, -5, -9, 0, Mth.PI, Mth.PI / 16, 1, 1, 1));
    }

    private void buildRaftParts(ModelBakery.PartDefinition root) {
        root.addOrReplaceChild("bottom",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-14, -11, -4, 28, 20, 4)
                        .addBox(-14, -9, -8, 28, 16, 4),
                ModelBakery.PartPose.offsetAndRotation(0, -2.1f, 1, 1.5708f, 0, 0));
        root.addOrReplaceChild("left_paddle",
                ModelBakery.CubeListBuilder.create().texOffs(0, 24).addBox(-1, 0, -5, 2, 2, 18).addBox(-1.001f, -3, 8, 1, 6, 7),
                new ModelBakery.PartPose(3, -4, 9, 0, 0, Mth.PI / 16, 1, 1, 1));
        root.addOrReplaceChild("right_paddle",
                ModelBakery.CubeListBuilder.create().texOffs(40, 24).addBox(-1, 0, -5, 2, 2, 18).addBox(0.001f, -3, 8, 1, 6, 7),
                new ModelBakery.PartPose(3, -4, -9, 0, Mth.PI, Mth.PI / 16, 1, 1, 1));
    }

    private void buildChestParts(ModelBakery.PartDefinition root) {
        float yBase = isRaft ? -10.1f : -5.0f;
        root.addOrReplaceChild("chest_bottom",
                ModelBakery.CubeListBuilder.create().texOffs(0, 76).addBox(0, 0, 0, 12, 8, 12),
                ModelBakery.PartPose.offsetAndRotation(-2, yBase, -6, 0, -Mth.PI / 2, 0));
        root.addOrReplaceChild("chest_lid",
                ModelBakery.CubeListBuilder.create().texOffs(0, 59).addBox(0, 0, 0, 12, 4, 12),
                ModelBakery.PartPose.offsetAndRotation(-2, yBase - 4, -6, 0, -Mth.PI / 2, 0));
        root.addOrReplaceChild("chest_lock",
                ModelBakery.CubeListBuilder.create().texOffs(0, 59).addBox(0, 0, 0, 2, 4, 1),
                ModelBakery.PartPose.offsetAndRotation(-1, yBase - 1, -1, 0, -Mth.PI / 2, 0));
    }
}