package de.tomalbrc.cameraobscura.renderer;

import com.mojang.math.Axis;
import de.tomalbrc.cameraobscura.model.triangle.ModelTesselator;
import de.tomalbrc.cameraobscura.renderer.entity.EntityRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.ItemStackRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.specific.*;
import de.tomalbrc.cameraobscura.renderer.entity.specific.block.FallingBlockRenderer;
import de.tomalbrc.cameraobscura.renderer.entity.specific.block.TntRenderer;
import de.tomalbrc.cameraobscura.sore.model.Model;
import de.tomalbrc.cameraobscura.sore.pipeline.DrawCommand;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderPipeline;
import de.tomalbrc.cameraobscura.sore.pipeline.RenderType;
import de.tomalbrc.cameraobscura.util.BuiltinEntityModels;
import de.tomalbrc.cameraobscura.util.DisplayAccessor;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.arrow.SpectralArrow;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownExperienceBottle;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownLingeringPotion;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4d;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class EntityRenderers {
    public static final Map<EntityType<?>, Model> ENTITY_TYPE_MODELS = new IdentityHashMap<>();
    public static Reference2ObjectArrayMap<EntityType<? extends Entity>, EntityRenderer<? extends Entity>> RENDERER = new Reference2ObjectArrayMap<>();

    static {
        RENDERER.put(EntityTypes.MANNEQUIN, new PlayerRenderer());
        RENDERER.put(EntityTypes.PLAYER, new PlayerRenderer());
        RENDERER.put(EntityTypes.COW, new CowRenderer());
        RENDERER.put(EntityTypes.MOOSHROOM, new MooshroomCowRenderer());
        RENDERER.put(EntityTypes.PIG, new PigRenderer());
        RENDERER.put(EntityTypes.SHEEP, new SheepRenderer());
        RENDERER.put(EntityTypes.CHICKEN, new ChickenRenderer());
        RENDERER.put(EntityTypes.RABBIT, new RabbitRenderer());
        RENDERER.put(EntityTypes.BLAZE, new BlazeRenderer());
        RENDERER.put(EntityTypes.ALLAY, new AllayRenderer());
        RENDERER.put(EntityTypes.ENDERMAN, new EndermanRenderer());
        RENDERER.put(EntityTypes.SKELETON, new SkeletonRenderer());
        RENDERER.put(EntityTypes.BOGGED, new BoggedRenderer());
        RENDERER.put(EntityTypes.PARCHED, new ParchedRenderer());
        RENDERER.put(EntityTypes.STRAY, new StrayRenderer());
        RENDERER.put(EntityTypes.ZOMBIE, new ZombieRenderer<>());
        RENDERER.put(EntityTypes.ZOMBIE_VILLAGER, new ZombieVillagerRenderer());
        RENDERER.put(EntityTypes.ZOMBIFIED_PIGLIN, new ZombifiedPiglinRenderer());
        RENDERER.put(EntityTypes.PIGLIN, new PiglinRenderer<>());
        RENDERER.put(EntityTypes.PIGLIN_BRUTE, new PiglinRenderer<>());
        RENDERER.put(EntityTypes.GIANT, new ZombieGiantRenderer());
        RENDERER.put(EntityTypes.DROWNED, new DrownedRenderer());
        RENDERER.put(EntityTypes.HUSK, new HuskRenderer());
        RENDERER.put(EntityTypes.TURTLE, new TurtleRenderer());
        RENDERER.put(EntityTypes.SILVERFISH, new SilverfishRenderer());
        RENDERER.put(EntityTypes.ENDERMITE, new EndermiteRenderer());
        RENDERER.put(EntityTypes.SQUID, new SquidRenderer(false));
        RENDERER.put(EntityTypes.GLOW_SQUID, new SquidRenderer(true));
        RENDERER.put(EntityTypes.CREEPER, new CreeperRenderer());
        RENDERER.put(EntityTypes.TROPICAL_FISH, new TropicalFishRenderer());
        RENDERER.put(EntityTypes.COD, new CodRenderer());
        RENDERER.put(EntityTypes.SALMON, new SalmonRenderer());
        RENDERER.put(EntityTypes.DOLPHIN, new DolphinRenderer());
        RENDERER.put(EntityTypes.PUFFERFISH, new PufferfishRenderer());
        RENDERER.put(EntityTypes.VILLAGER, new VillagerRenderer());
        RENDERER.put(EntityTypes.WANDERING_TRADER, new WanderingTraderRenderer());
        RENDERER.put(EntityTypes.WITCH, new WitchRenderer());
        RENDERER.put(EntityTypes.PILLAGER, new PillagerRenderer());
        RENDERER.put(EntityTypes.EVOKER, new EvokerRenderer());
        RENDERER.put(EntityTypes.EVOKER_FANGS, new EvokerFangsRenderer());
        RENDERER.put(EntityTypes.VINDICATOR, new VindicatorRenderer());
        RENDERER.put(EntityTypes.ILLUSIONER, new IllusionerRenderer());
        RENDERER.put(EntityTypes.VEX, new VexRenderer());
        RENDERER.put(EntityTypes.RAVAGER, new RavagerRenderer());
        RENDERER.put(EntityTypes.ENDER_DRAGON, new EnderDragonRenderer());
        RENDERER.put(EntityTypes.FALLING_BLOCK, new FallingBlockRenderer());
        RENDERER.put(EntityTypes.TNT, new TntRenderer());
        RENDERER.put(EntityTypes.SPIDER, new SpiderRenderer());
        RENDERER.put(EntityTypes.CAVE_SPIDER, new SpiderRenderer());
        RENDERER.put(EntityTypes.PARROT, new ParrotRenderer());
        RENDERER.put(EntityTypes.ARMADILLO, new ArmadilloRenderer());
        RENDERER.put(EntityTypes.STRIDER, new StriderRenderer());
        RENDERER.put(EntityTypes.SHULKER_BULLET, new ShulkerBulletRenderer());
        RENDERER.put(EntityTypes.SHULKER, new ShulkerRenderer());
        RENDERER.put(EntityTypes.MAGMA_CUBE, new MagmaCubeRenderer());
        RENDERER.put(EntityTypes.SLIME, new SlimeRenderer());
        RENDERER.put(EntityTypes.BAT, new BatRenderer());
        RENDERER.put(EntityTypes.AXOLOTL, new AxolotlRenderer());
        RENDERER.put(EntityTypes.BEE, new BeeRenderer());
        RENDERER.put(EntityTypes.POLAR_BEAR, new PolarBearRenderer());
        RENDERER.put(EntityTypes.GUARDIAN, new GuardianRenderer());
        RENDERER.put(EntityTypes.CAT, new CatRenderer());
        RENDERER.put(EntityTypes.OCELOT, new OcelotRenderer());
        RENDERER.put(EntityTypes.WOLF, new WolfRenderer());
        RENDERER.put(EntityTypes.GOAT, new GoatRenderer());
        RENDERER.put(EntityTypes.SNOW_GOLEM, new SnowGolemRenderer());
        RENDERER.put(EntityTypes.MULE, new DonkeyRenderer(true));
        RENDERER.put(EntityTypes.DONKEY, new DonkeyRenderer(false));
        RENDERER.put(EntityTypes.HORSE, new HorseRenderer());
        RENDERER.put(EntityTypes.ZOMBIE_HORSE, new UndeadHorseRenderer());
        RENDERER.put(EntityTypes.SKELETON_HORSE, new UndeadHorseRenderer());
        RENDERER.put(EntityTypes.LLAMA, new LlamaRenderer());
        RENDERER.put(EntityTypes.CAMEL, new CamelRenderer());
        RENDERER.put(EntityTypes.CAMEL_HUSK, new CamelHuskRenderer());
        RENDERER.put(EntityTypes.TRADER_LLAMA, new LlamaRenderer());
        RENDERER.put(EntityTypes.HOGLIN, new HoglinRenderer<>());
        RENDERER.put(EntityTypes.ZOGLIN, new HoglinRenderer<>());
        RENDERER.put(EntityTypes.PANDA, new PandaRenderer());
        RENDERER.put(EntityTypes.SNIFFER, new SnifferRenderer());
        RENDERER.put(EntityTypes.FOX, new FoxRenderer());
        RENDERER.put(EntityTypes.FROG, new FrogRenderer());
        RENDERER.put(EntityTypes.TADPOLE, new TadpoleRenderer());
        RENDERER.put(EntityTypes.GHAST, new GhastRenderer<>());
        RENDERER.put(EntityTypes.HAPPY_GHAST, new GhastRenderer<>());
        RENDERER.put(EntityTypes.NAUTILUS, new NautilusRenderer<>());
        RENDERER.put(EntityTypes.ZOMBIE_NAUTILUS, new ZombieNautilusRenderer());
        RENDERER.put(EntityTypes.IRON_GOLEM, new IronGolemRenderer());
        RENDERER.put(EntityTypes.COPPER_GOLEM, new CopperGolemRenderer());
        RENDERER.put(EntityTypes.PHANTOM, new PhantomRenderer());
        RENDERER.put(EntityTypes.CREAKING, new CreakingRenderer());
        RENDERER.put(EntityTypes.WARDEN, new WardenRenderer());
        RENDERER.put(EntityTypes.WITHER, new WitherRenderer());
        RENDERER.put(EntityTypes.WITHER_SKELETON, new WitherSkeletonRenderer());
        RENDERER.put(EntityTypes.BREEZE, new BreezeRenderer());

        RENDERER.put(EntityTypes.LIGHTNING_BOLT, new LightningBoltRenderer());

        RENDERER.put(EntityTypes.MINECART, new MinecartRenderer<>());
        RENDERER.put(EntityTypes.CHEST_MINECART, new MinecartRenderer<>());
        RENDERER.put(EntityTypes.FURNACE_MINECART, new MinecartRenderer<>());
        RENDERER.put(EntityTypes.HOPPER_MINECART, new MinecartRenderer<>());
        RENDERER.put(EntityTypes.SPAWNER_MINECART, new MinecartRenderer<>());
        RENDERER.put(EntityTypes.COMMAND_BLOCK_MINECART, new MinecartRenderer<>());
        RENDERER.put(EntityTypes.TNT_MINECART, new TntMinecartRenderer());

        RENDERER.put(EntityTypes.ACACIA_BOAT, new BoatRenderer(BoatRenderer.ACACIA_BOAT, false, false));
        RENDERER.put(EntityTypes.BIRCH_BOAT, new BoatRenderer(BoatRenderer.BIRCH_BOAT, false, false));
        RENDERER.put(EntityTypes.CHERRY_BOAT, new BoatRenderer(BoatRenderer.CHERRY_BOAT, false, false));
        RENDERER.put(EntityTypes.DARK_OAK_BOAT, new BoatRenderer(BoatRenderer.DARK_OAK_BOAT, false, false));
        RENDERER.put(EntityTypes.JUNGLE_BOAT, new BoatRenderer(BoatRenderer.JUNGLE_BOAT, false, false));
        RENDERER.put(EntityTypes.MANGROVE_BOAT, new BoatRenderer(BoatRenderer.MANGROVE_BOAT, false, false));
        RENDERER.put(EntityTypes.OAK_BOAT, new BoatRenderer(BoatRenderer.OAK_BOAT, false, false));
        RENDERER.put(EntityTypes.PALE_OAK_BOAT, new BoatRenderer(BoatRenderer.PALE_OAK_BOAT, false, false));
        RENDERER.put(EntityTypes.SPRUCE_BOAT, new BoatRenderer(BoatRenderer.SPRUCE_BOAT, false, false));
        RENDERER.put(EntityTypes.ACACIA_CHEST_BOAT, new BoatRenderer(BoatRenderer.ACACIA_CHEST_BOAT, false, true));
        RENDERER.put(EntityTypes.BIRCH_CHEST_BOAT, new BoatRenderer(BoatRenderer.BIRCH_CHEST_BOAT, false, true));
        RENDERER.put(EntityTypes.CHERRY_CHEST_BOAT, new BoatRenderer(BoatRenderer.CHERRY_CHEST_BOAT, false, true));
        RENDERER.put(EntityTypes.DARK_OAK_CHEST_BOAT, new BoatRenderer(BoatRenderer.DARK_OAK_CHEST_BOAT, false, true));
        RENDERER.put(EntityTypes.JUNGLE_CHEST_BOAT, new BoatRenderer(BoatRenderer.JUNGLE_CHEST_BOAT, false, true));
        RENDERER.put(EntityTypes.MANGROVE_CHEST_BOAT, new BoatRenderer(BoatRenderer.MANGROVE_CHEST_BOAT, false, true));
        RENDERER.put(EntityTypes.OAK_CHEST_BOAT, new BoatRenderer(BoatRenderer.OAK_CHEST_BOAT, false, true));
        RENDERER.put(EntityTypes.PALE_OAK_CHEST_BOAT, new BoatRenderer(BoatRenderer.PALE_OAK_CHEST_BOAT, false, true));
        RENDERER.put(EntityTypes.SPRUCE_CHEST_BOAT, new BoatRenderer(BoatRenderer.SPRUCE_CHEST_BOAT, false, true));
        RENDERER.put(EntityTypes.BAMBOO_RAFT, new BoatRenderer(BoatRenderer.BAMBOO_RAFT, true, false));
        RENDERER.put(EntityTypes.BAMBOO_CHEST_RAFT, new BoatRenderer(BoatRenderer.BAMBOO_CHEST_RAFT, true, true));

        RENDERER.put(EntityTypes.ARROW, new ArrowRenderer<Arrow>());
        RENDERER.put(EntityTypes.SPECTRAL_ARROW, new ArrowRenderer<SpectralArrow>());
        RENDERER.put(EntityTypes.LLAMA_SPIT, new LlamaSpitRenderer());
        RENDERER.put(EntityTypes.TRIDENT, new TridentRenderer());
        RENDERER.put(EntityTypes.WIND_CHARGE, new WindChargeRenderer());
        RENDERER.put(EntityTypes.BREEZE_WIND_CHARGE, new WindChargeRenderer());
        RENDERER.put(EntityTypes.EYE_OF_ENDER, new EyeOfEnderRenderer());
        RENDERER.put(EntityTypes.ENDER_PEARL, new EnderPearlRenderer());
        RENDERER.put(EntityTypes.SNOWBALL, new SnowballRenderer());
        RENDERER.put(EntityTypes.WITHER_SKULL, new WitherSkullRenderer());
        RENDERER.put(EntityTypes.DRAGON_FIREBALL, new DragonFireballRenderer());
        RENDERER.put(EntityTypes.FIREBALL, new FireballRenderer(3f));
        RENDERER.put(EntityTypes.SMALL_FIREBALL, new FireballRenderer(0.75f));
        RENDERER.put(EntityTypes.EXPERIENCE_ORB, new ExperienceOrbRenderer());
        RENDERER.put(EntityTypes.SPLASH_POTION, new BillboardItemEntityRenderer<>(ThrownSplashPotion::getItem, 1f));
        RENDERER.put(EntityTypes.LINGERING_POTION, new BillboardItemEntityRenderer<>(ThrownLingeringPotion::getItem, 1f));
        RENDERER.put(EntityTypes.EXPERIENCE_BOTTLE, new BillboardItemEntityRenderer<>(ThrownExperienceBottle::getItem, 1f));
        RENDERER.put(EntityTypes.FIREWORK_ROCKET, new FireworkRocketRenderer());

        RENDERER.put(EntityTypes.PAINTING, new PaintingRenderer());
        RENDERER.put(EntityTypes.END_CRYSTAL, new EndCrystalRenderer());
        RENDERER.put(EntityTypes.ITEM_FRAME, new ItemFrameRenderer());
        RENDERER.put(EntityTypes.GLOW_ITEM_FRAME, new ItemFrameRenderer());
        RENDERER.put(EntityTypes.ITEM, new ItemEntityRenderer());
        RENDERER.put(EntityTypes.ARMOR_STAND, new ArmorStandRenderer());
        RENDERER.put(EntityTypes.LEASH_KNOT, new LeashKnotRenderer());
    }

    public static void renderItemDisplay(RenderPipeline pipeline, Entity ent) {
        ItemStack item = DisplayAccessor.getItemStack(ent);
        ItemDisplayContext displayContext = DisplayAccessor.getItemTransform(ent);

        Matrix4d transform = new Matrix4d().translate(ent.position().toVector3f());

        if (!item.isEmpty()) {
            transform
                    .rotateY(Mth.DEG_TO_RAD * (-ent.getPreciseBodyRotation(1.0f) - 360f))
                    .rotateX(Mth.DEG_TO_RAD * ent.getXRot(1.0f));

            transform.translate(ent.getEntityData().get(DisplayAccessor.getDataTranslationId()));
            transform.rotate(ent.getEntityData().get(DisplayAccessor.getDataLeftRotationId()));
            transform.scale(ent.getEntityData().get(DisplayAccessor.getDataScaleId()));
            transform.rotate(ent.getEntityData().get(DisplayAccessor.getDataRightRotationId()));

            transform.rotate(Axis.YP.rotation((float) Math.PI));

            ItemStackRenderer.render(pipeline, item, displayContext, transform);
        }
    }

    public static void renderFromModel(Entity ent, List<DrawCommand> allCommands) {
        var cachedModel = ENTITY_TYPE_MODELS.get(ent.getType());
        if (cachedModel == null) {
            var view = BuiltinEntityModels.getModel(ent.getType(), ent.getUUID());
            if (view != null) {
                ModelTesselator tri = new ModelTesselator(view);
                var mesh = tri.build();
                if (mesh != null) {
                    cachedModel = new Model(mesh);
                    ENTITY_TYPE_MODELS.put(ent.getType(), cachedModel);
                }
            }
        }

        if (cachedModel != null) {
            var transform = new Matrix4d()
                    .translate(ent.position().toVector3f())
                    .rotateY(Mth.DEG_TO_RAD * (180f - ent.getPreciseBodyRotation(1.f)));

            allCommands.add(new DrawCommand(RenderType.ENTITY, cachedModel, transform, IntList.of(0xFFFFFFFF)));
        }
    }
}
