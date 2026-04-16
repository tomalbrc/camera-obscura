package de.tomalbrc.cameraobscura.mixin;

import de.tomalbrc.cameraobscura.CustomContent;
import de.tomalbrc.cameraobscura.util.image.VideoPlaybackManager;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemFrame.class)
public abstract class ItemFrameMixin {
    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void onInteract(Player player, InteractionHand hand, Vec3 location, CallbackInfoReturnable<InteractionResult> cir) {
        if (player.level().isClientSide()) return;

        ItemFrame self = (ItemFrame) (Object) this;
        ItemStack item = self.getItem();
        if (item.has(CustomContent.DATA) && item.has(DataComponents.MAP_ID)) {
            VideoPlaybackManager.togglePlayback(self);
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }
}