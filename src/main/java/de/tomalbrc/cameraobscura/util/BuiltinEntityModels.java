package de.tomalbrc.cameraobscura.util;

import de.tomalbrc.cameraobscura.render.model.resource.RPElement;
import de.tomalbrc.cameraobscura.render.model.resource.RPModel;
import de.tomalbrc.cameraobscura.util.model.*;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.io.InputStream;
import java.util.Map;
import java.util.Random;

public class BuiltinEntityModels {
    static Map<EntityType, RPModel> modelMap = new Object2ObjectOpenHashMap<>();
    public static RPModel.View getModel(Entity entity, Vector3f pos, Vector3f rot) {
        if (modelMap.containsKey(entity.getType())) {
            RPModel.View view = switch (entity) {
                case Villager villager ->
                        new RPModel.View(modelMap.get(entity.getType()), new Vector3f(0, rot.y() + 180, 0), pos.add(0, 2.f / 16.f, 0));
                case IronGolem ironGolem ->
                        new RPModel.View(modelMap.get(entity.getType()), new Vector3f(0, rot.y() + 180, 0), pos.add(0, -1, 0));
                default ->
                        new RPModel.View(modelMap.get(entity.getType()), new Vector3f(0, rot.y() + 180, 0), pos);
            };
            return view;
        } else {
            return new RPModel.View(modelMap.get(EntityType.PIG), new Vector3f(0, rot.y() + 180, 0), pos);
        }
    }

    private static RPModel loadModel(String model) {
        int num = Math.abs(new Random().nextInt());

        RPModel rpModel = RPHelper.loadModel(BuiltinEntityModels.class.getResourceAsStream(model));
        for (var entry: rpModel.textures.entrySet()) {
            entry.getKey().replace("0", ""+num);
        }
        rpModel.textures.put(""+num, rpModel.textures.get("0"));
        rpModel.textures.remove("0");

        for (RPElement element : rpModel.elements) {
            element.shade = false;
            for (String key : element.faces.keySet()) {
                var face = element.faces.get(key);
                face.texture = "#" + num;
                face.uv.mul(rpModel.textureSize.get(0)/16.f, rpModel.textureSize.get(1)/16.f, rpModel.textureSize.get(0)/16.f, rpModel.textureSize.get(1)/16.f);
            }
        }
        return rpModel;
    }

    public static void initModels() {
        modelMap.put(EntityType.ARMOR_STAND, loadModel("/builtin/armor_stand.json"));
        modelMap.put(EntityType.AXOLOTL, loadModel("/builtin/axolotl.json"));
        modelMap.put(EntityType.BEE, loadModel("/builtin/zombie.json"));
        modelMap.put(EntityType.COW, loadModel("/builtin/cow.json"));
        modelMap.put(EntityType.IRON_GOLEM, loadModel("/builtin/iron_golem.json"));
        modelMap.put(EntityType.VILLAGER, loadModel("/builtin/villager.json"));
        modelMap.put(EntityType.ZOMBIE, loadModel("/builtin/zombie.json"));
        modelMap.put(EntityType.HUSK, loadModel("/builtin/husk.json"));
        modelMap.put(EntityType.DROWNED, loadModel("/builtin/drowned.json"));
        modelMap.put(EntityType.SKELETON, loadModel("/builtin/skeleton.json"));
        modelMap.put(EntityType.STRAY, loadModel("/builtin/stray.json"));
        modelMap.put(EntityType.BOGGED, loadModel("/builtin/bogged.json"));
        modelMap.put(EntityType.PIG, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.SHEEP, loadModel("/builtin/sheep.json"));
        modelMap.put(EntityType.PIGLIN, loadModel("/builtin/piglin.json"));
        modelMap.put(EntityType.ZOMBIFIED_PIGLIN, loadModel("/builtin/zombified_piglin.json"));
        modelMap.put(EntityType.CHICKEN, loadModel("/builtin/chicken.json"));

        modelMap.put(EntityType.ITEM_FRAME, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.GLOW_ITEM_FRAME, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.SQUID, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.GLOW_SQUID, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.SNOW_GOLEM, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.ARMADILLO, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.RABBIT, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.CAMEL, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.CAT, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.DONKEY, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.WOLF, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.HORSE, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.SKELETON_HORSE, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.ZOMBIE_HORSE, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.TNT, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.TRIDENT, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.TADPOLE, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.FROG, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.FOX, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.FIREBALL, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.FALLING_BLOCK, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.ITEM, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.ITEM_DISPLAY, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.BLOCK_DISPLAY, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.TEXT_DISPLAY, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.INTERACTION, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.AREA_EFFECT_CLOUD, loadModel("/builtin/shulker.json"));
        modelMap.put(EntityType.DOLPHIN, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.ENDER_DRAGON, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.DRAGON_FIREBALL, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.COD, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.ALLAY, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.VEX, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.ILLUSIONER, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.VINDICATOR, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.PILLAGER, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.EVOKER, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.EVOKER_FANGS, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.RAVAGER, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.CAVE_SPIDER, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.SPIDER, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.SILVERFISH, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.SLIME, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.MAGMA_CUBE, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.BLAZE, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.SMALL_FIREBALL, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.WITHER_SKELETON, loadModel("/builtin/skeleton.json"));
        modelMap.put(EntityType.WITHER_SKULL, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.WITHER, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.ENDERMITE, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.ENDERMAN, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.END_CRYSTAL, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.ENDER_PEARL, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.EYE_OF_ENDER, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.SNOWBALL, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.ARROW, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.SPECTRAL_ARROW, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.PARROT, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.ZOGLIN, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.WITCH, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.WIND_CHARGE, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.BREEZE_WIND_CHARGE, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.BREEZE, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.BAT, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.FIREWORK_ROCKET, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.HOGLIN, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.PUFFERFISH, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.TROPICAL_FISH, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.SALMON, loadModel("/builtin/pig.json"));
        modelMap.put(EntityType.SHULKER, loadModel("/builtin/shulker.json"));
        modelMap.put(EntityType.SHULKER_BULLET, loadModel("/builtin/shulker.json"));

        modelMap.put(EntityType.MINECART, loadModel("/builtin/minecart.json"));
        modelMap.put(EntityType.FURNACE_MINECART, loadModel("/builtin/minecart.json"));
        modelMap.put(EntityType.TNT_MINECART, loadModel("/builtin/minecart.json"));
        modelMap.put(EntityType.COMMAND_BLOCK_MINECART, loadModel("/builtin/minecart.json"));
        modelMap.put(EntityType.SPAWNER_MINECART, loadModel("/builtin/minecart.json"));
        modelMap.put(EntityType.HOPPER_MINECART, loadModel("/builtin/minecart.json"));
        modelMap.put(EntityType.CHEST_MINECART, loadModel("/builtin/minecart.json"));
    }
}
