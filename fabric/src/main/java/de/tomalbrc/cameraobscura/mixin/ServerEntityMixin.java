package de.tomalbrc.cameraobscura.mixin;

import net.minecraft.server.level.ServerEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerEntity.class)
public abstract class ServerEntityMixin {
//
//    @Shadow @Final private Entity entity;
//    @Shadow private int tickCount;
//    @Shadow @Final private ServerLevel level;
//
//    @Unique
//    DynamicWorldRenderer co$renderer;
//
//    @Unique
//    CompletableFuture<Void> co$future;
//
//    @Inject(method = "sendChanges", at = @At("TAIL"))
//    protected void co$syncMap(CallbackInfo ci) {
//        Entity var3 = this.entity;
//        if (var3 instanceof ItemFrame frame) {
//            if (false&&this.tickCount % 10 == 0) {
//
//                var itemStack = frame.getItem();
//
//                if (itemStack.has(Components.ENTITY_REF) && itemStack.has(Components.LIVE_MAP) && itemStack.has(DataComponents.MAP_ID)) {
//                    var ref = itemStack.get(Components.ENTITY_REF);
//                    if (co$renderer == null) {
//                        var entity = ref.getEntity(level, LivingEntity.class);
//                        if (entity == null)
//                            return;
//                        co$renderer = new DynamicWorldRenderer(128, 128, 32);
//                        co$renderer.updateCamera(entity);
//                    }
//
//                    var mapId = itemStack.get(DataComponents.MAP_ID);
//
//                    if (co$future == null || co$future.isDone()) {
//                        co$renderer.updateChunksInRange(level);
//
//                        co$future = CompletableFuture.runAsync(() -> {
//                            var img = co$renderer.render(level);
//                            var image = CanvasImage.from(img);
//
//                            MapItemSavedData mapData = level.getMapData(mapId);
//
//                            byte[] mapColors = new byte[128*128];
//                            for (int x = 0; x < 128; x++) {
//                                for (int y = 0; y < 128; y++) {
//
//                                    if (x < image.getWidth() && y < image.getHeight()) {
//                                        mapColors[x + y * 128] = image.getRaw(x, y);
//                                    }
//                                }
//                            }
//
//                            Packet<?> packet = new ClientboundMapItemDataPacket(mapId, mapData.scale, false, Optional.empty(), Optional.of(new MapItemSavedData.MapPatch(0, 0, 128, 128, mapColors)));
//                            for (ServerPlayer player : this.level.players()) {
//                                player.connection.send(packet);
//                            }
//                        });
//                    }
//                }
//            }
//        }
//    }
}
