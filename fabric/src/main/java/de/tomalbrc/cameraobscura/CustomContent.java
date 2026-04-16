package de.tomalbrc.cameraobscura;

import de.tomalbrc.cameraobscura.util.Constants;
import eu.pb4.polymer.core.api.other.PolymerComponent;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.level.entity.UniquelyIdentifyable;

public class CustomContent {
    public static final DataComponentType<Unit> LIVE_MAP = new DataComponentType.Builder<Unit>().persistent(Unit.CODEC).build();
    public static final DataComponentType<EntityReference<UniquelyIdentifyable>> ENTITY_REF = new DataComponentType.Builder<EntityReference<UniquelyIdentifyable>>().persistent(EntityReference.codec()).build();

    public static final DataComponentType<Components.ColorMode> CAMERA_COLOR_MODE = new DataComponentType.Builder<Components.ColorMode>().persistent(Components.ColorMode.CODEC).build();
    public static final DataComponentType<Components.Resolution> CAMERA_RESOLUTION = new DataComponentType.Builder<Components.Resolution>().persistent(Components.Resolution.CODEC).build();
    public static final DataComponentType<Components.VideoParams> CAMERA_VIDEO_PARAMS = new DataComponentType.Builder<Components.VideoParams>().persistent(Components.VideoParams.CODEC).build();
    public static final DataComponentType<Components.DitherMode> CAMERA_DITHER_MODE = new DataComponentType.Builder<Components.DitherMode>().persistent(Components.DitherMode.CODEC).build();

    public static final DataComponentType<Components.MediaData> DATA = new DataComponentType.Builder<Components.MediaData>().persistent(Components.MediaData.CODEC).build();

    public static void register() {
        String modId = Constants.MOD_ID;

        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
                Identifier.fromNamespaceAndPath(modId, "map"), CustomContent.LIVE_MAP);
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
                Identifier.fromNamespaceAndPath(modId, "camera_entity"), CustomContent.ENTITY_REF);

        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
                Identifier.fromNamespaceAndPath(modId, "color"), CustomContent.CAMERA_COLOR_MODE);
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
                Identifier.fromNamespaceAndPath(modId, "resolution"), CustomContent.CAMERA_RESOLUTION);
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
                Identifier.fromNamespaceAndPath(modId, "video"), CustomContent.CAMERA_VIDEO_PARAMS);
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
                Identifier.fromNamespaceAndPath(modId, "dither"), CustomContent.CAMERA_DITHER_MODE);

        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
                Identifier.fromNamespaceAndPath(modId, "data"), CustomContent.DATA);

        PolymerComponent.registerDataComponent(
                CustomContent.LIVE_MAP, CustomContent.ENTITY_REF,
                CustomContent.CAMERA_COLOR_MODE, CustomContent.CAMERA_RESOLUTION,
                CustomContent.CAMERA_VIDEO_PARAMS, CustomContent.CAMERA_DITHER_MODE,
                CustomContent.DATA
        );
    }
}