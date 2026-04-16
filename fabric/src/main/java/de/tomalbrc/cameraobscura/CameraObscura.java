package de.tomalbrc.cameraobscura;

import com.mojang.logging.LogUtils;
import de.tomalbrc.cameraobscura.color.BlockColors;
import de.tomalbrc.cameraobscura.command.CameraCommand;
import de.tomalbrc.cameraobscura.command.PresetManager;
import de.tomalbrc.cameraobscura.item.CameraBlock;
import de.tomalbrc.cameraobscura.item.CameraBlockEntity;
import de.tomalbrc.cameraobscura.item.CameraItem;
import de.tomalbrc.cameraobscura.item.PolymerBlockItem;
import de.tomalbrc.cameraobscura.platform.ItemDataStore;
import de.tomalbrc.cameraobscura.platform.Platforms;
import de.tomalbrc.cameraobscura.renderer.ChunkMeshCache;
import de.tomalbrc.cameraobscura.util.BuiltinEntityModels;
import de.tomalbrc.cameraobscura.util.Constants;
import de.tomalbrc.cameraobscura.util.MojangAssetFetcher;
import de.tomalbrc.cameraobscura.util.image.VideoPlaybackManager;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.slf4j.Logger;

public class CameraObscura implements ModInitializer {
    public static Item ITEM;
    public static BlockEntityType<CameraBlockEntity> CAMERA_BLOCK_ENTITY;
    protected static Logger LOGGER = LogUtils.getLogger();
    FabricPlatform fabricPlatform = new FabricPlatform();

    public static Logger logger() {
        return LOGGER;
    }

    public static <T extends BlockEntityType<?>> T registerBlockEntity(String path, T blockEntityType) {
        var key = ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, Identifier.fromNamespaceAndPath(Constants.MOD_ID, path));
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, key, blockEntityType);
    }

    @Override
    public void onInitialize() {
        Platforms.set(fabricPlatform);

        if (!FabricLoader.getInstance().isModLoaded("polymer-autohost") || !PolymerAssetFetcher.isEnabled()) {
            fabricPlatform.setAssetFetcher(new MojangAssetFetcher(FabricLoader.getInstance().getRawGameVersion(), fabricPlatform.getConfigDir(), CameraObscura.logger()));
            fabricPlatform.getAssetFetcher().initialize().thenRun(() -> {
                BlockColors.init();
                BuiltinEntityModels.initModels();
            });
        }

        CustomContent.register();

        registerItems();
        registerCamcorder();

        Block cameraBlock = registerBlock("camera_block");
        Item camBlockItem = registerBlockItem("camera_block", new Item.Properties().modelId(Identifier.withDefaultNamespace("player_head")), cameraBlock);
        CAMERA_BLOCK_ENTITY = registerBlockEntity("camera_block_entity", FabricBlockEntityTypeBuilder.create(CameraBlockEntity::new, cameraBlock).build());
        PolymerBlockUtils.registerBlockEntity(CAMERA_BLOCK_ENTITY);

        registerEventHandler();
    }

    private void registerCamcorder() {
        ITEM = registerItem("camcorder", new Item.Properties()
                .modelId(Identifier.withDefaultNamespace("player_head"))
                .component(DataComponents.PROFILE, ItemDataStore.createCameraProfile())
                .component(CustomContent.CAMERA_COLOR_MODE, Components.ColorMode.COLOR)
                .component(CustomContent.CAMERA_RESOLUTION, Components.Resolution.DEFAULT)
                .component(CustomContent.CAMERA_VIDEO_PARAMS, new Components.VideoParams(10, 400, true))
                .component(CustomContent.CAMERA_DITHER_MODE, Components.DitherMode.NONE)
        );

        var ITEM2 = registerItem("grayscale_camcorder", new Item.Properties()
                .modelId(Identifier.withDefaultNamespace("player_head"))
                .component(DataComponents.PROFILE, ItemDataStore.createCameraProfile())
                .component(CustomContent.CAMERA_COLOR_MODE, Components.ColorMode.GRAYSCALE)
                .component(CustomContent.CAMERA_RESOLUTION, Components.Resolution.DEFAULT)
                .component(CustomContent.CAMERA_VIDEO_PARAMS, new Components.VideoParams(20, 20 * 20, true))
                .component(CustomContent.CAMERA_DITHER_MODE, Components.DitherMode.NONE)
        );

        var ITEM3 = registerItem("primitive_camcorder", new Item.Properties()
                .modelId(Identifier.withDefaultNamespace("player_head"))
                .component(DataComponents.PROFILE, ItemDataStore.createCameraProfile())
                .component(CustomContent.CAMERA_COLOR_MODE, Components.ColorMode.MONOCHROME)
                .component(CustomContent.CAMERA_RESOLUTION, Components.Resolution.DEFAULT)
                .component(CustomContent.CAMERA_VIDEO_PARAMS, new Components.VideoParams(20, 20 * 20, true))
                .component(CustomContent.CAMERA_DITHER_MODE, Components.DitherMode.NONE)
        );

        var ITEM4 = registerItem("retro_camcorder", new Item.Properties()
                .modelId(Identifier.withDefaultNamespace("player_head"))
                .component(DataComponents.PROFILE, ItemDataStore.createCameraProfile())
                .component(CustomContent.CAMERA_COLOR_MODE, Components.ColorMode.SEPIA)
                .component(CustomContent.CAMERA_RESOLUTION, Components.Resolution.DEFAULT)
                .component(CustomContent.CAMERA_VIDEO_PARAMS, new Components.VideoParams(20, 20 * 20, true))
                .component(CustomContent.CAMERA_DITHER_MODE, Components.DitherMode.NONE)
        );
    }

    private void registerItems() {
        ITEM = registerItem("camera", new Item.Properties()
                .modelId(Identifier.withDefaultNamespace("spyglass"))
                .component(CustomContent.CAMERA_COLOR_MODE, Components.ColorMode.COLOR)
                .component(CustomContent.CAMERA_RESOLUTION, Components.Resolution.DEFAULT)
                .component(CustomContent.CAMERA_VIDEO_PARAMS, new Components.VideoParams(0, 1, false))
                .component(CustomContent.CAMERA_DITHER_MODE, Components.DitherMode.NONE)
        );

        var ITEM2 = registerItem("grayscale_camera", new Item.Properties()
                .modelId(Identifier.withDefaultNamespace("spyglass"))
                .component(CustomContent.CAMERA_COLOR_MODE, Components.ColorMode.GRAYSCALE)
                .component(CustomContent.CAMERA_RESOLUTION, Components.Resolution.DEFAULT)
                .component(CustomContent.CAMERA_VIDEO_PARAMS, new Components.VideoParams(0, 1, false))
                .component(CustomContent.CAMERA_DITHER_MODE, Components.DitherMode.NONE)
        );

        var ITEM3 = registerItem("primitive_camera", new Item.Properties()
                .modelId(Identifier.withDefaultNamespace("spyglass"))
                .component(CustomContent.CAMERA_COLOR_MODE, Components.ColorMode.MONOCHROME)
                .component(CustomContent.CAMERA_RESOLUTION, Components.Resolution.DEFAULT)
                .component(CustomContent.CAMERA_VIDEO_PARAMS, new Components.VideoParams(0, 1, false))
                .component(CustomContent.CAMERA_DITHER_MODE, Components.DitherMode.NONE)
        );

        var ITEM4 = registerItem("retro_camera", new Item.Properties()
                .modelId(Identifier.withDefaultNamespace("spyglass"))
                .component(CustomContent.CAMERA_COLOR_MODE, Components.ColorMode.SEPIA)
                .component(CustomContent.CAMERA_RESOLUTION, Components.Resolution.DEFAULT)
                .component(CustomContent.CAMERA_VIDEO_PARAMS, new Components.VideoParams(0, 1, true))
                .component(CustomContent.CAMERA_DITHER_MODE, Components.DitherMode.NONE)
        );
    }

    private Item registerItem(String name, Item.Properties props) {
        var key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Constants.MOD_ID, name));
        return Registry.register(
                BuiltInRegistries.ITEM,
                key,
                new CameraItem(props
                        .stacksTo(1)
                        .rarity(Rarity.RARE)
                        .setId(key)
                )
        );
    }

    private Item registerBlockItem(String name, Item.Properties props, Block block) {
        var key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Constants.MOD_ID, name));
        return Registry.register(
                BuiltInRegistries.ITEM,
                key,
                new PolymerBlockItem(block, props
                        .stacksTo(1)
                        .rarity(Rarity.RARE)
                        .setId(key)
                )
        );
    }

    private Block registerBlock(String name) {
        var key = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(Constants.MOD_ID, name));
        return Registry.register(
                BuiltInRegistries.BLOCK,
                key,
                new CameraBlock(BlockBehaviour.Properties.of().destroyTime(2).noCollision().setId(key))
        );
    }

    private void registerEventHandler() {
        CameraItem.registerEventHandler();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> CameraCommand.register(dispatcher));
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            fabricPlatform.setMinecraftServer(server);
            VideoPlaybackManager.startTicking();
            PresetManager.loadPresets();
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            Constants.stop();
            VideoPlaybackManager.stopTicking();
        });

        ServerTickEvents.END_LEVEL_TICK.register(x -> {
            if (x.getServer().getTickCount() % (10 * 20) == 0) {
                var playerList = x.players();
                var d = ModConfig.getInstance().renderDistance+64;
                var cache = ChunkMeshCache.CHUNK_CACHE.get(x);
                if (cache != null) cache.values().removeIf(chunk -> {
                    for (ServerPlayer player : playerList) {
                        if (player.distanceToSqr(chunk.origin().getCenter()) < d*d) {
                            return false;
                        }
                    }

                    return true;
                });
            }
        });

        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClientSide()) return InteractionResult.PASS;

            if (entity instanceof ItemFrame frame) {
                ItemStack item = frame.getItem();
                if (Platforms.get().getItemDataStore().hasMediaData(item) && item.has(DataComponents.MAP_ID)) {
                    VideoPlaybackManager.togglePlayback(frame);
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.PASS;
        });

        PolymerResourcePackUtils.RESOURCE_PACK_AFTER_INITIAL_CREATION_EVENT.register(resourcePackBuilder -> {
            fabricPlatform.setAssetFetcher(new PolymerAssetFetcher(resourcePackBuilder));
            BlockColors.init();
            BuiltinEntityModels.initModels();

            //byte[] pixelModel = PixelDisplayGenerator.generatePixelModel();
            //byte[] itemDef = PixelDisplayGenerator.generateItemDefinition();
            //byte[] whiteImage = PixelDisplayGenerator.generateWhiteImage();

            //String PIXEL_MODEL_PATH = "assets/cameraobscura/models/item/pixel.json";
            //String ITEM_DEF_PATH = "assets/cameraobscura/items/display.json";
            //String WHITE_TEXTURE_PATH = "assets/cameraobscura/textures/item/display_white.png";

            //resourcePackBuilder.addData(PIXEL_MODEL_PATH, pixelModel);
            //resourcePackBuilder.addData(ITEM_DEF_PATH, itemDef);
            //resourcePackBuilder.addData(WHITE_TEXTURE_PATH, whiteImage);
        });

        ServerChunkEvents.CHUNK_LOAD.register((level, chunk, generated) -> {
            ChunkMeshCache.onChunkLoaded(level, chunk);
        });

        ServerChunkEvents.CHUNK_UNLOAD.register((level, chunk) -> {
            ChunkMeshCache.onChunkUnloaded(level, chunk);
        });
    }
}
