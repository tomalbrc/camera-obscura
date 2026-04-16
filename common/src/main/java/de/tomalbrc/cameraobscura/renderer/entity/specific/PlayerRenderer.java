package de.tomalbrc.cameraobscura.renderer.entity.specific;

import de.tomalbrc.cameraobscura.renderer.entity.ModelBakery;
import de.tomalbrc.cameraobscura.util.Constants;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.item.*;
import org.joml.Vector3d;

import java.util.HashMap;
import java.util.Map;

public class PlayerRenderer extends HumanoidRenderer<Avatar> {
    private final Map<String, ModelBakery.BakedPart> PLAYER_CACHE = new HashMap<>();

    @Override
    protected String getTexturePath(Avatar player) {
        var b = player.getProfile().skinPatch().body();
        if (b.isPresent())
            return b.get().id().toString();

        return Constants.DYNAMIC_PLAYER_TEXTURE + ":" + player.getStringUUID();
    }

    @Override
    protected int getTexWidth() {
        return 64;
    }

    @Override
    protected int getTexHeight() {
        return 64;
    }

    @Override
    public ModelBakery.BakedPart buildRoot(Avatar player) {
        String texture = getTexturePath(player);
        boolean slim = isSlim(player);
        String key = texture + (slim ? "_slim" : "_wide");
        return PLAYER_CACHE.computeIfAbsent(key, k -> buildPlayerParts(slim, player));
    }

    private ModelBakery.BakedPart buildPlayerParts(boolean slim, Avatar player) {
        ModelBakery bakery = new ModelBakery(getTexturePath(player), getTexWidth(), getTexHeight());
        ModelBakery.ModelDefinition model = new ModelBakery.ModelDefinition(bakery);
        ModelBakery.PartDefinition root = model.root();

        var head = root.addOrReplaceChild("head",
                ModelBakery.CubeListBuilder.create().texOffs(0, 0).addBox(-4, -8, -4, 8, 8, 8),
                ModelBakery.PartPose.offset(0, 0, 0));
        head.addOrReplaceChild("hat",
                ModelBakery.CubeListBuilder.create().texOffs(32, 0).addBox(-4, -8, -4, 8, 8, 8, new ModelBakery.CubeDeformation(0.5F)),
                ModelBakery.PartPose.ZERO);

        var body = root.addOrReplaceChild("body",
                ModelBakery.CubeListBuilder.create().texOffs(16, 16).addBox(-4, 0, -2, 8, 12, 4),
                ModelBakery.PartPose.offset(0, 0, 0));
        body.addOrReplaceChild("jacket",
                ModelBakery.CubeListBuilder.create().texOffs(16, 32).addBox(-4, 0, -2, 8, 12, 4, new ModelBakery.CubeDeformation(0.25F)),
                ModelBakery.PartPose.ZERO);

        if (slim) {
            ModelBakery.PartDefinition rightArm = root.addOrReplaceChild("right_arm",
                    ModelBakery.CubeListBuilder.create().texOffs(40, 16).addBox(-2, -2, -2, 3, 12, 4),
                    ModelBakery.PartPose.offset(-5, 2, 0));
            rightArm.addOrReplaceChild("right_sleeve",
                    ModelBakery.CubeListBuilder.create().texOffs(40, 32).addBox(-2, -2, -2, 3, 12, 4, new ModelBakery.CubeDeformation(0.25F)),
                    ModelBakery.PartPose.ZERO);

            ModelBakery.PartDefinition leftArm = root.addOrReplaceChild("left_arm",
                    ModelBakery.CubeListBuilder.create().texOffs(32, 48).addBox(-1, -2, -2, 3, 12, 4),
                    ModelBakery.PartPose.offset(5, 2, 0));
            leftArm.addOrReplaceChild("left_sleeve",
                    ModelBakery.CubeListBuilder.create().texOffs(48, 48).addBox(-1, -2, -2, 3, 12, 4, new ModelBakery.CubeDeformation(0.25F)),
                    ModelBakery.PartPose.ZERO);
        } else {
            root.addOrReplaceChild("right_arm",
                            ModelBakery.CubeListBuilder.create().texOffs(40, 16).addBox(-3, -2, -2, 4, 12, 4),
                            ModelBakery.PartPose.offset(-5, 2, 0))
                    .addOrReplaceChild("right_sleeve",
                            ModelBakery.CubeListBuilder.create().texOffs(40, 32).addBox(-3, -2, -2, 4, 12, 4, new ModelBakery.CubeDeformation(0.25F)),
                            ModelBakery.PartPose.ZERO);

            root.addOrReplaceChild("left_arm",
                            ModelBakery.CubeListBuilder.create().texOffs(32, 48).addBox(-1, -2, -2, 4, 12, 4),
                            ModelBakery.PartPose.offset(5, 2, 0))
                    .addOrReplaceChild("left_sleeve",
                            ModelBakery.CubeListBuilder.create().texOffs(48, 48).addBox(-1, -2, -2, 4, 12, 4, new ModelBakery.CubeDeformation(0.25F)),
                            ModelBakery.PartPose.ZERO);
        }

        var rightLeg = root.addOrReplaceChild("right_leg",
                ModelBakery.CubeListBuilder.create().texOffs(0, 16).addBox(-2, 0, -2, 4, 12, 4),
                ModelBakery.PartPose.offset(-1.9f, 12, 0));
        rightLeg.addOrReplaceChild("right_pants",
                ModelBakery.CubeListBuilder.create().texOffs(0, 32).addBox(-2, 0, -2, 4, 12, 4, new ModelBakery.CubeDeformation(0.25F)),
                ModelBakery.PartPose.ZERO);

        var leftLeg = root.addOrReplaceChild("left_leg",
                ModelBakery.CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2, 0, -2, 4, 12, 4).mirror(false),
                ModelBakery.PartPose.offset(1.9f, 12, 0));
        leftLeg.addOrReplaceChild("left_pants",
                ModelBakery.CubeListBuilder.create().texOffs(0, 48).addBox(-2, 0, -2, 4, 12, 4, new ModelBakery.CubeDeformation(0.25F)),
                ModelBakery.PartPose.ZERO);

        return root.bake();
    }

    @Override
    protected void buildModelParts(ModelBakery.PartDefinition root, ModelBakery bakery) {
    }

    @Override
    protected ArmPose getArmPose(Avatar player, HumanoidArm arm) {
        InteractionHand hand = (player.getMainArm() == arm) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        ItemStack itemInHand = player.getItemInHand(hand);
        if (itemInHand.isEmpty()) return ArmPose.EMPTY;

        if (!player.swinging && itemInHand.is(Items.CROSSBOW) && CrossbowItem.isCharged(itemInHand)) {
            return ArmPose.CROSSBOW_HOLD;
        }

        if (player.getUsedItemHand() == hand && player.getUseItemRemainingTicks() > 0) {
            ItemUseAnimation anim = itemInHand.getUseAnimation();
            if (anim == ItemUseAnimation.BLOCK) return ArmPose.BLOCK;
            if (anim == ItemUseAnimation.BOW) return ArmPose.BOW_AND_ARROW;
            if (anim == ItemUseAnimation.CROSSBOW) return ArmPose.CROSSBOW_CHARGE;
            if (anim == ItemUseAnimation.SPYGLASS) return ArmPose.SPYGLASS;
            if (anim == ItemUseAnimation.TOOT_HORN) return ArmPose.TOOT_HORN;
            if (anim == ItemUseAnimation.BRUSH) return ArmPose.BRUSH;
            if (anim == ItemUseAnimation.SPEAR) return ArmPose.SPEAR;
            if (anim == ItemUseAnimation.TRIDENT) return ArmPose.THROW_TRIDENT;
        }
        if (itemInHand.getItem() instanceof TridentItem) return ArmPose.THROW_TRIDENT;
        return ArmPose.ITEM;
    }

    @Override
    protected boolean isOverlayPartVisible(String partName, Avatar player) {
        return switch (partName) {
            case "hat" -> player.isModelPartShown(PlayerModelPart.HAT);
            case "jacket" -> player.isModelPartShown(PlayerModelPart.JACKET);
            case "left_sleeve" -> player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE);
            case "right_sleeve" -> player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE);
            case "left_pants" -> player.isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG);
            case "right_pants" -> player.isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG);
            default -> true;
        };
    }

    @Override
    protected boolean isSlim(Avatar player) {
        var model = player.getProfile().skinPatch().model();
        return model.isPresent() && model.get() == PlayerModelType.SLIM;
    }

    @Override
    protected Vector3d getHandItemOffset(Avatar player, HumanoidArm arm) {
        Vector3d off = super.getHandItemOffset(player, arm);
        if (isSlim(player)) {
            off.x += (arm == HumanoidArm.RIGHT ? 1 : -1) * 0.5f;
        }
        return off;
    }
}
