package de.tomalbrc.cameraobscura.util;

import de.tomalbrc.cameraobscura.json.CachedResourceLocationDeserializer;
import de.tomalbrc.cameraobscura.render.model.resource.RPElement;
import de.tomalbrc.cameraobscura.render.model.resource.RPModel;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Map;
import java.util.UUID;

public class BuiltinEntityModels {
    static Map<EntityType<?>, RPModel> modelMap = new Object2ObjectOpenHashMap<>();

    public static RPModel getRaw(EntityType<?> type) {
        return modelMap.get(type);
    }

    public static RPModel.View getModel(EntityType entityType, Vector3f pos, Vector3fc rot, @Nullable UUID uuid, Object data) {
        if (modelMap.containsKey(entityType)) {
            if (entityType == EntityType.VILLAGER) {
                return new RPModel.View(modelMap.get(entityType), new Vector3f(0, rot.y() + 180, 0), pos.add(0, -2.f / 16.f, 0));
            } else if (entityType == EntityType.WITHER_SKELETON || entityType == EntityType.IRON_GOLEM || entityType == EntityType.WITHER || entityType == EntityType.WARDEN || entityType == EntityType.WANDERING_TRADER || entityType == EntityType.PILLAGER || entityType == EntityType.EVOKER || entityType == EntityType.RAVAGER || entityType == EntityType.VINDICATOR) {
                return new RPModel.View(modelMap.get(entityType), new Vector3f(0, rot.y() + 180, 0), pos.add(0, -1, 0));
            } else if (entityType == EntityType.CAMEL) {
                return new RPModel.View(modelMap.get(entityType), new Vector3f(0, rot.y() + 180, 0), pos.add(0, -1, -0.5f));
            } else {
                return new RPModel.View(modelMap.get(entityType), new Vector3f(rot.x(), rot.y() + 180, rot.z()), pos);
            }
        } else if (entityType == EntityType.PLAYER) {
            RPModel model = loadModel("/builtin/player.json"); // todo: cache per player uuid
            model.textures.put(model.textures.keySet().iterator().next(), Constants.DYNAMIC_PLAYER_TEXTURE+":"+uuid.toString().replace("-", ""));
            return new RPModel.View(model, new Vector3f(0, rot.y() + 180, 0), pos.add(0, -1.f / 16.f, 0));
        } else if (entityType == EntityType.ITEM) {
            ItemStack itemStack = (ItemStack) data;
            RPModel model = RPHelper.loadItemModel(itemStack);
            return new RPModel.View(model, new Vector3f(0, rot.y() + 180, 0), pos.add(0, 0, 0));
        } else {
            return new RPModel.View(modelMap.get(EntityType.PIG), new Vector3f(0, rot.y() + 180, 0), pos);
        }
    }

    private static RPModel loadModel(String model) {
        RPModel rpModel = RPHelper.loadModel(BuiltinEntityModels.class.getResourceAsStream(model));
        for (RPElement element : rpModel.elements) {
            element.shade = false;
            for (String key : element.faces.keySet()) {
                var face = element.faces.get(key);
                face.uv.mul(rpModel.textureSize.get(0)/16.f, rpModel.textureSize.get(1)/16.f, rpModel.textureSize.get(0)/16.f, rpModel.textureSize.get(1)/16.f);
            }
        }
        return rpModel;
    }

    public static void initModels() {
        modelMap.put(EntityType.ARMOR_STAND, loadModel("/builtin/armor_stand.json"));
        modelMap.put(EntityType.AXOLOTL, loadModel("/builtin/axolotl.json"));
        modelMap.put(EntityType.BEE, loadModel("/builtin/bee.json"));
        modelMap.put(EntityType.COW, loadModel("/builtin/cow.json"));
        modelMap.put(EntityType.IRON_GOLEM, loadModel("/builtin/iron_golem.json"));
        modelMap.put(EntityType.VILLAGER, loadModel("/builtin/villager.json"));
        modelMap.put(EntityType.WANDERING_TRADER, loadModel("/builtin/wandering_trader.json"));
        modelMap.put(EntityType.ZOMBIE, loadModel("/builtin/zombie.json"));
        modelMap.put(EntityType.CREEPER, loadModel("/builtin/creeper.json"));
        modelMap.put(EntityType.HUSK, loadModel("/builtin/husk.json"));
        modelMap.put(EntityType.DROWNED, loadModel("/builtin/drowned.json"));
        modelMap.put(EntityType.SKELETON, loadModel("/builtin/skeleton.json"));
        modelMap.put(EntityType.STRAY, loadModel("/builtin/stray.json"));
        modelMap.put(EntityType.BOGGED, loadModel("/builtin/bogged.json"));
        modelMap.put(EntityType.PIG, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.SHEEP, loadModel("/builtin/sheep.json"));
        modelMap.put(EntityType.PIGLIN, loadModel("/builtin/piglin.json"));
        modelMap.put(EntityType.ZOMBIFIED_PIGLIN, loadModel("/builtin/zombified_piglin.json"));
        modelMap.put(EntityType.GHAST, loadModel("/builtin/ghast.json"));
        modelMap.put(EntityType.CHICKEN, loadModel("/builtin/chicken.json"));

        modelMap.put(EntityType.ITEM_FRAME, RPHelper.loadModel(CachedResourceLocationDeserializer.get("minecraft:block/item_frame")));
        modelMap.put(EntityType.GLOW_ITEM_FRAME, RPHelper.loadModel(CachedResourceLocationDeserializer.get("minecraft:block/glow_item_frame")));
        modelMap.put(EntityType.SQUID, loadModel("/builtin/squid.json"));
        modelMap.put(EntityType.GLOW_SQUID, loadModel("/builtin/glow_squid.json"));
        modelMap.put(EntityType.SNOW_GOLEM, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.ARMADILLO, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.RABBIT, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.CAMEL, loadModel("/builtin/camel.json"));
        modelMap.put(EntityType.CAT, loadModel("/builtin/cat.json"));
        modelMap.put(EntityType.DONKEY, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.WOLF, loadModel("/builtin/wolf.json"));
        modelMap.put(EntityType.HORSE, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.SKELETON_HORSE, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.ZOMBIE_HORSE, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.TNT, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.TRIDENT, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.TADPOLE, loadModel("/builtin/tadpole.json"));
        modelMap.put(EntityType.FROG, loadModel("/builtin/frog.json"));
        modelMap.put(EntityType.FOX, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.FIREBALL, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.FALLING_BLOCK, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.ITEM_DISPLAY, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.BLOCK_DISPLAY, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.TEXT_DISPLAY, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.INTERACTION, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.AREA_EFFECT_CLOUD, loadModel("/builtin/shulker.json"));
        modelMap.put(EntityType.DOLPHIN, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.ENDER_DRAGON, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.DRAGON_FIREBALL, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.COD, loadModel("/builtin/cod.json"));
        modelMap.put(EntityType.ALLAY, loadModel("/builtin/allay.json"));
        modelMap.put(EntityType.VEX, loadModel("/builtin/allay.json"));
        modelMap.put(EntityType.ILLUSIONER, loadModel("/builtin/illusioner.json"));
        modelMap.put(EntityType.VINDICATOR, loadModel("/builtin/vindicator.json"));
        modelMap.put(EntityType.PILLAGER, loadModel("/builtin/pillager.json"));
        modelMap.put(EntityType.EVOKER, loadModel("/builtin/evoker.json"));
        modelMap.put(EntityType.EVOKER_FANGS, loadModel("/builtin/evoker_fangs.json"));
        modelMap.put(EntityType.RAVAGER, loadModel("/builtin/ravager.json"));
        modelMap.put(EntityType.CAVE_SPIDER, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.SPIDER, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.SILVERFISH, loadModel("/builtin/silverfish.json"));
        modelMap.put(EntityType.SLIME, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.MAGMA_CUBE, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.BLAZE, loadModel("/builtin/blaze.json"));
        modelMap.put(EntityType.SMALL_FIREBALL, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.WITHER_SKELETON, loadModel("/builtin/wither_skeleton.json"));
        modelMap.put(EntityType.WITHER_SKULL, loadModel("/builtin/wither_skull.json"));
        modelMap.put(EntityType.WITHER, loadModel("/builtin/wither.json"));
        modelMap.put(EntityType.WARDEN, loadModel("/builtin/warden.json"));
        modelMap.put(EntityType.ENDERMITE, loadModel("/builtin/endermite.json"));
        modelMap.put(EntityType.ENDERMAN, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.END_CRYSTAL, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.ENDER_PEARL, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.EYE_OF_ENDER, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.SNOWBALL, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.ARROW, loadModel("/builtin/arrow.json"));
        modelMap.put(EntityType.SPECTRAL_ARROW, loadModel("/builtin/arrow.json"));
        modelMap.put(EntityType.PARROT, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.ZOGLIN, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.WITCH, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.WIND_CHARGE, loadModel("/builtin/shulker_bullet.json"));
        modelMap.put(EntityType.BREEZE_WIND_CHARGE, loadModel("/builtin/shulker_bullet.json"));
        modelMap.put(EntityType.BREEZE, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.BAT, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.FIREWORK_ROCKET, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.HOGLIN, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.PUFFERFISH, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.TROPICAL_FISH, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.SALMON, loadModel("/builtin/salmon.json"));
        modelMap.put(EntityType.SHULKER, loadModel("/builtin/shulker.json"));
        modelMap.put(EntityType.SHULKER_BULLET, loadModel("/builtin/shulker_bullet.json"));

        modelMap.put(EntityType.STRIDER, loadModel("/builtin/strider.json"));

        modelMap.put(EntityType.MINECART, loadModel("/builtin/minecart.json"));
        modelMap.put(EntityType.FURNACE_MINECART, loadModel("/builtin/minecart.json"));
        modelMap.put(EntityType.TNT_MINECART, loadModel("/builtin/minecart.json"));
        modelMap.put(EntityType.COMMAND_BLOCK_MINECART, loadModel("/builtin/minecart.json"));
        modelMap.put(EntityType.SPAWNER_MINECART, loadModel("/builtin/minecart.json"));
        modelMap.put(EntityType.HOPPER_MINECART, loadModel("/builtin/minecart.json"));
        modelMap.put(EntityType.CHEST_MINECART, loadModel("/builtin/minecart.json"));

        modelMap.put(EntityType.BOAT, loadModel("/builtin/boat.json"));
        modelMap.put(EntityType.CHEST_BOAT, loadModel("/builtin/boat.json"));
    }
}
