package de.tomalbrc.cameraobscura.item;

import de.tomalbrc.cameraobscura.CameraObscura;
import de.tomalbrc.cameraobscura.Components;
import de.tomalbrc.cameraobscura.renderer.VideoRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class CameraBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer, StackedContentsCompatible {
    NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);

    VideoRenderer renderer;

    float pitch, yaw;

    public CameraBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(CameraObscura.CAMERA_BLOCK_ENTITY, worldPosition, blockState);

    }

    public static void serverTick(ServerLevel level, BlockPos pos, BlockState state, CameraBlockEntity entity) {
        entity.tick(level, pos, state);
    }

    private void tick(ServerLevel level, BlockPos pos, BlockState state) {
        if (renderer != null && state.getValue(CameraBlock.STATE) == CameraBlock.State.OFF) {
            this.renderer.stopAndFinalizeAsync();
            this.renderer = null;
        } else if (renderer == null && state.getValue(CameraBlock.STATE) == CameraBlock.State.WARMUP) {
            this.renderer = new VideoRenderer(
                    VideoRenderer.blockEntitySource(this, worldPosition.getCenter(), -pitch, yaw),
                    Components.Resolution.DEFAULT,
                    Components.ColorMode.COLOR,
                    Components.DitherMode.NONE,
                    Components.VideoParams.DEFAULT
            );

            this.renderer.start();
        } else if (renderer != null && state.getValue(CameraBlock.STATE) == CameraBlock.State.RECORDING) {
            renderer.tick();
        }
    }

    @Override
    protected @NonNull Component getDefaultName() {
        return Component.literal("Camera");
    }

    @Override
    protected @NonNull NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(@NonNull NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected @NonNull AbstractContainerMenu createMenu(int containerId, @NonNull Inventory inventory) {
        return null;
    }

    @Override
    public int @NonNull [] getSlotsForFace(@NonNull Direction direction) {
        return new int[0];
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, @NonNull ItemStack itemStack, @Nullable Direction direction) {
        return true;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, @NonNull ItemStack itemStack, @NonNull Direction direction) {
        return true;
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public void fillStackedContents(@NonNull StackedItemContents contents) {

    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        output.putFloat("Yaw", yaw);
        output.putFloat("Pitch", pitch);
        ContainerHelper.saveAllItems(output.child("Items"), items);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        yaw = input.getFloatOr("Yaw", yaw);
        pitch = input.getFloatOr("Pitch", pitch);
        ContainerHelper.loadAllItems(input.childOrEmpty("Items"), this.items);
    }

    public void setRotation(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;

        if (this.renderer != null)
            renderer.renderer().updateCamera(getBlockPos().getCenter().x(), getBlockPos().getCenter().y(), getBlockPos().getCenter().z(), -pitch, yaw);
    }
}
