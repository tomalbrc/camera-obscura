package de.tomalbrc.cameraobscura.item;

import de.tomalbrc.cameraobscura.platform.ItemDataStore;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Items;
import org.joml.Vector3f;

public class CameraHolder extends ElementHolder {
    ItemDisplayElement display;
    ItemDisplayElement displayStatus;

    public CameraHolder() {
        super();

        var item = Items.PLAYER_HEAD.getDefaultInstance();
        item.set(DataComponents.PROFILE, ItemDataStore.createCameraProfile());
        this.display = new ItemDisplayElement(item);

        this.displayStatus = new ItemDisplayElement(Items.GREEN_CONCRETE);
        this.displayStatus.setTranslation(new Vector3f(0, 0.33f, 0));
        this.displayStatus.setScale(new Vector3f(0.2f));

        this.display.setScale(new Vector3f(1.25f));
        this.display.setTranslation(new Vector3f(0, 0.33f, 0));

        this.addElement(displayStatus);
        this.addElement(display);
    }

    @Override
    public void notifyUpdate(HolderAttachment.UpdateType updateType) {
        super.notifyUpdate(updateType);

        var attachment = this.getAttachment();
        if (updateType == BlockBoundAttachment.BLOCK_STATE_UPDATE && attachment != null) {
            var state = ((BlockAwareAttachment) this.getAttachment()).getBlockState();
            if (state.hasProperty(CameraBlock.STATE)) {
                var s = state.getValue(CameraBlock.STATE);
                if (s == CameraBlock.State.OFF) {
                    this.displayStatus.setItem(Items.GREEN_CONCRETE.getDefaultInstance());
                }

                if (s == CameraBlock.State.WARMUP) {
                    this.displayStatus.setItem(Items.ORANGE_CONCRETE.getDefaultInstance());
                }

                if (s == CameraBlock.State.RECORDING) {
                    this.displayStatus.setItem(Items.RED_CONCRETE.getDefaultInstance());
                }
            }

            this.tick();
        }
    }

    public void setRotation(float yaw, float pitch) {
        this.display.setRotation(pitch, yaw);
        this.displayStatus.setRotation(pitch, yaw);
    }
}
