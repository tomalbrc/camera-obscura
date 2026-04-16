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
        RENDERER.put(EntityType.MANNEQUIN, new PlayerRenderer());
        RENDERER.put(EntityType.PLAYER, new PlayerRenderer());
        RENDERER.put(EntityType.COW, new CowRenderer());
        RENDERER.put(EntityType.MOOSHROOM, new MooshroomCowRenderer());
        RENDERER.put(EntityType.PIG, new PigRenderer());
        RENDERER.put(EntityType.SHEEP, new SheepRenderer());
        RENDERER.put(EntityType.CHICKEN, new ChickenRenderer());
        RENDERER.put(EntityType.RABBIT, new RabbitRenderer());
        RENDERER.put(EntityType.BLAZE, new BlazeRenderer());
        RENDERER.put(EntityType.ALLAY, new AllayRenderer());
        RENDERER.put(EntityType.ENDERMAN, new EndermanRenderer());
        RENDERER.put(EntityType.SKELETON, new SkeletonRenderer());
        RENDERER.put(EntityType.BOGGED, new BoggedRenderer());
        RENDERER.put(EntityType.PARCHED, new ParchedRenderer());
        RENDERER.put(EntityType.STRAY, new StrayRenderer());
        RENDERER.put(EntityType.ZOMBIE, new ZombieRenderer<>());
        RENDERER.put(EntityType.ZOMBIE_VILLAGER, new ZombieVillagerRenderer());
        RENDERER.put(EntityType.ZOMBIFIED_PIGLIN, new ZombifiedPiglinRenderer());
        RENDERER.put(EntityType.PIGLIN, new PiglinRenderer<>());
        RENDERER.put(EntityType.PIGLIN_BRUTE, new PiglinRenderer<>());
        RENDERER.put(EntityType.GIANT, new ZombieGiantRenderer());
        RENDERER.put(EntityType.DROWNED, new DrownedRenderer());
        RENDERER.put(EntityType.HUSK, new HuskRenderer());
        RENDERER.put(EntityType.TURTLE, new TurtleRenderer());
        RENDERER.put(EntityType.SILVERFISH, new SilverfishRenderer());
        RENDERER.put(EntityType.ENDERMITE, new EndermiteRenderer());
        RENDERER.put(EntityType.SQUID, new SquidRenderer(false));
        RENDERER.put(EntityType.GLOW_SQUID, new SquidRenderer(true));
        RENDERER.put(EntityType.CREEPER, new CreeperRenderer());
        RENDERER.put(EntityType.TROPICAL_FISH, new TropicalFishRenderer());
        RENDERER.put(EntityType.COD, new CodRenderer());
        RENDERER.put(EntityType.SALMON, new SalmonRenderer());
        RENDERER.put(EntityType.DOLPHIN, new DolphinRenderer());
        RENDERER.put(EntityType.PUFFERFISH, new PufferfishRenderer());
        RENDERER.put(EntityType.VILLAGER, new VillagerRenderer());
        RENDERER.put(EntityType.WANDERING_TRADER, new WanderingTraderRenderer());
        RENDERER.put(EntityType.WITCH, new WitchRenderer());
        RENDERER.put(EntityType.PILLAGER, new PillagerRenderer());
        RENDERER.put(EntityType.EVOKER, new EvokerRenderer());
        RENDERER.put(EntityType.EVOKER_FANGS, new EvokerFangsRenderer());
        RENDERER.put(EntityType.VINDICATOR, new VindicatorRenderer());
        RENDERER.put(EntityType.ILLUSIONER, new IllusionerRenderer());
        RENDERER.put(EntityType.VEX, new VexRenderer());
        RENDERER.put(EntityType.RAVAGER, new RavagerRenderer());
        RENDERER.put(EntityType.ENDER_DRAGON, new EnderDragonRenderer());
        RENDERER.put(EntityType.FALLING_BLOCK, new FallingBlockRenderer());
        RENDERER.put(EntityType.TNT, new TntRenderer());
        RENDERER.put(EntityType.SPIDER, new SpiderRenderer());
        RENDERER.put(EntityType.CAVE_SPIDER, new SpiderRenderer());
        RENDERER.put(EntityType.PARROT, new ParrotRenderer());
        RENDERER.put(EntityType.ARMADILLO, new ArmadilloRenderer());
        RENDERER.put(EntityType.STRIDER, new StriderRenderer());
        RENDERER.put(EntityType.SHULKER_BULLET, new ShulkerBulletRenderer());
        RENDERER.put(EntityType.SHULKER, new ShulkerRenderer());
        RENDERER.put(EntityType.MAGMA_CUBE, new MagmaCubeRenderer());
        RENDERER.put(EntityType.SLIME, new SlimeRenderer());
        RENDERER.put(EntityType.BAT, new BatRenderer());
        RENDERER.put(EntityType.AXOLOTL, new AxolotlRenderer());
        RENDERER.put(EntityType.BEE, new BeeRenderer());
        RENDERER.put(EntityType.POLAR_BEAR, new PolarBearRenderer());
        RENDERER.put(EntityType.GUARDIAN, new GuardianRenderer());
        RENDERER.put(EntityType.CAT, new CatRenderer());
        RENDERER.put(EntityType.OCELOT, new OcelotRenderer());
        RENDERER.put(EntityType.WOLF, new WolfRenderer());
        RENDERER.put(EntityType.GOAT, new GoatRenderer());
        RENDERER.put(EntityType.SNOW_GOLEM, new SnowGolemRenderer());
        RENDERER.put(EntityType.MULE, new DonkeyRenderer(true));
        RENDERER.put(EntityType.DONKEY, new DonkeyRenderer(false));
        RENDERER.put(EntityType.HORSE, new HorseRenderer());
        RENDERER.put(EntityType.ZOMBIE_HORSE, new UndeadHorseRenderer());
        RENDERER.put(EntityType.SKELETON_HORSE, new UndeadHorseRenderer());
        RENDERER.put(EntityType.LLAMA, new LlamaRenderer());
        RENDERER.put(EntityType.CAMEL, new CamelRenderer());
        RENDERER.put(EntityType.CAMEL_HUSK, new CamelHuskRenderer());
        RENDERER.put(EntityType.TRADER_LLAMA, new LlamaRenderer());
        RENDERER.put(EntityType.HOGLIN, new HoglinRenderer<>());
        RENDERER.put(EntityType.ZOGLIN, new HoglinRenderer<>());
        RENDERER.put(EntityType.PANDA, new PandaRenderer());
        RENDERER.put(EntityType.SNIFFER, new SnifferRenderer());
        RENDERER.put(EntityType.FOX, new FoxRenderer());
        RENDERER.put(EntityType.FROG, new FrogRenderer());
        RENDERER.put(EntityType.TADPOLE, new TadpoleRenderer());
        RENDERER.put(EntityType.GHAST, new GhastRenderer<>());
        RENDERER.put(EntityType.HAPPY_GHAST, new GhastRenderer<>());
        RENDERER.put(EntityType.NAUTILUS, new NautilusRenderer<>());
        RENDERER.put(EntityType.ZOMBIE_NAUTILUS, new ZombieNautilusRenderer());
        RENDERER.put(EntityType.IRON_GOLEM, new IronGolemRenderer());
        RENDERER.put(EntityType.COPPER_GOLEM, new CopperGolemRenderer());
        RENDERER.put(EntityType.PHANTOM, new PhantomRenderer());
        RENDERER.put(EntityType.CREAKING, new CreakingRenderer());
        RENDERER.put(EntityType.WARDEN, new WardenRenderer());
        RENDERER.put(EntityType.WITHER, new WitherRenderer());
        RENDERER.put(EntityType.WITHER_SKELETON, new WitherSkeletonRenderer());
        RENDERER.put(EntityType.BREEZE, new BreezeRenderer());

        RENDERER.put(EntityType.LIGHTNING_BOLT, new LightningBoltRenderer());

        RENDERER.put(EntityType.MINECART, new MinecartRenderer<>());
        RENDERER.put(EntityType.CHEST_MINECART, new MinecartRenderer<>());
        RENDERER.put(EntityType.FURNACE_MINECART, new MinecartRenderer<>());
        RENDERER.put(EntityType.HOPPER_MINECART, new MinecartRenderer<>());
        RENDERER.put(EntityType.SPAWNER_MINECART, new MinecartRenderer<>());
        RENDERER.put(EntityType.COMMAND_BLOCK_MINECART, new MinecartRenderer<>());
        RENDERER.put(EntityType.TNT_MINECART, new TntMinecartRenderer());

        RENDERER.put(EntityType.ACACIA_BOAT, new BoatRenderer(BoatRenderer.ACACIA_BOAT, false, false));
        RENDERER.put(EntityType.BIRCH_BOAT, new BoatRenderer(BoatRenderer.BIRCH_BOAT, false, false));
        RENDERER.put(EntityType.CHERRY_BOAT, new BoatRenderer(BoatRenderer.CHERRY_BOAT, false, false));
        RENDERER.put(EntityType.DARK_OAK_BOAT, new BoatRenderer(BoatRenderer.DARK_OAK_BOAT, false, false));
        RENDERER.put(EntityType.JUNGLE_BOAT, new BoatRenderer(BoatRenderer.JUNGLE_BOAT, false, false));
        RENDERER.put(EntityType.MANGROVE_BOAT, new BoatRenderer(BoatRenderer.MANGROVE_BOAT, false, false));
        RENDERER.put(EntityType.OAK_BOAT, new BoatRenderer(BoatRenderer.OAK_BOAT, false, false));
        RENDERER.put(EntityType.PALE_OAK_BOAT, new BoatRenderer(BoatRenderer.PALE_OAK_BOAT, false, false));
        RENDERER.put(EntityType.SPRUCE_BOAT, new BoatRenderer(BoatRenderer.SPRUCE_BOAT, false, false));
        RENDERER.put(EntityType.ACACIA_CHEST_BOAT, new BoatRenderer(BoatRenderer.ACACIA_CHEST_BOAT, false, true));
        RENDERER.put(EntityType.BIRCH_CHEST_BOAT, new BoatRenderer(BoatRenderer.BIRCH_CHEST_BOAT, false, true));
        RENDERER.put(EntityType.CHERRY_CHEST_BOAT, new BoatRenderer(BoatRenderer.CHERRY_CHEST_BOAT, false, true));
        RENDERER.put(EntityType.DARK_OAK_CHEST_BOAT, new BoatRenderer(BoatRenderer.DARK_OAK_CHEST_BOAT, false, true));
        RENDERER.put(EntityType.JUNGLE_CHEST_BOAT, new BoatRenderer(BoatRenderer.JUNGLE_CHEST_BOAT, false, true));
        RENDERER.put(EntityType.MANGROVE_CHEST_BOAT, new BoatRenderer(BoatRenderer.MANGROVE_CHEST_BOAT, false, true));
        RENDERER.put(EntityType.OAK_CHEST_BOAT, new BoatRenderer(BoatRenderer.OAK_CHEST_BOAT, false, true));
        RENDERER.put(EntityType.PALE_OAK_CHEST_BOAT, new BoatRenderer(BoatRenderer.PALE_OAK_CHEST_BOAT, false, true));
        RENDERER.put(EntityType.SPRUCE_CHEST_BOAT, new BoatRenderer(BoatRenderer.SPRUCE_CHEST_BOAT, false, true));
        RENDERER.put(EntityType.BAMBOO_RAFT, new BoatRenderer(BoatRenderer.BAMBOO_RAFT, true, false));
        RENDERER.put(EntityType.BAMBOO_CHEST_RAFT, new BoatRenderer(BoatRenderer.BAMBOO_CHEST_RAFT, true, true));

        RENDERER.put(EntityType.ARROW, new ArrowRenderer<Arrow>());
        RENDERER.put(EntityType.SPECTRAL_ARROW, new ArrowRenderer<SpectralArrow>());
        RENDERER.put(EntityType.LLAMA_SPIT, new LlamaSpitRenderer());
        RENDERER.put(EntityType.TRIDENT, new TridentRenderer());
        RENDERER.put(EntityType.WIND_CHARGE, new WindChargeRenderer());
        RENDERER.put(EntityType.BREEZE_WIND_CHARGE, new WindChargeRenderer());
        RENDERER.put(EntityType.EYE_OF_ENDER, new EyeOfEnderRenderer());
        RENDERER.put(EntityType.ENDER_PEARL, new EnderPearlRenderer());
        RENDERER.put(EntityType.SNOWBALL, new SnowballRenderer());
        RENDERER.put(EntityType.WITHER_SKULL, new WitherSkullRenderer());
        RENDERER.put(EntityType.DRAGON_FIREBALL, new DragonFireballRenderer());
        RENDERER.put(EntityType.FIREBALL, new FireballRenderer(3f));
        RENDERER.put(EntityType.SMALL_FIREBALL, new FireballRenderer(0.75f));
        RENDERER.put(EntityType.EXPERIENCE_ORB, new ExperienceOrbRenderer());
        RENDERER.put(EntityType.SPLASH_POTION, new BillboardItemEntityRenderer<>(ThrownSplashPotion::getItem, 1f));
        RENDERER.put(EntityType.LINGERING_POTION, new BillboardItemEntityRenderer<>(ThrownLingeringPotion::getItem, 1f));
        RENDERER.put(EntityType.EXPERIENCE_BOTTLE, new BillboardItemEntityRenderer<>(ThrownExperienceBottle::getItem, 1f));
        RENDERER.put(EntityType.FIREWORK_ROCKET, new FireworkRocketRenderer());

        RENDERER.put(EntityType.PAINTING, new PaintingRenderer());
        RENDERER.put(EntityType.END_CRYSTAL, new EndCrystalRenderer());
        RENDERER.put(EntityType.ITEM_FRAME, new ItemFrameRenderer());
        RENDERER.put(EntityType.GLOW_ITEM_FRAME, new ItemFrameRenderer());
        RENDERER.put(EntityType.ITEM, new ItemEntityRenderer());
        RENDERER.put(EntityType.ARMOR_STAND, new ArmorStandRenderer());
        RENDERER.put(EntityType.LEASH_KNOT, new LeashKnotRenderer());
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
