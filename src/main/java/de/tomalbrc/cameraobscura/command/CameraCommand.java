package de.tomalbrc.cameraobscura.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import de.tomalbrc.cameraobscura.ModConfig;
import de.tomalbrc.cameraobscura.render.Raytracer;
import de.tomalbrc.cameraobscura.render.renderer.BufferedImageRenderer;
import de.tomalbrc.cameraobscura.render.renderer.CanvasImageRenderer;
import de.tomalbrc.cameraobscura.util.RPHelper;
import eu.pb4.mapcanvas.api.core.CanvasImage;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class CameraCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> camera_obscura = Commands.literal("camera-obscura").requires(Permissions.require("camera-obscura.command", 2));

        var node = camera_obscura
                .executes(CameraCommand::createMapOfSourceForSource)
                .then(Commands.argument("scale", IntegerArgumentType.integer(1,3)).requires(Permissions.require("camera-obscura.command.scale", ModConfig.getInstance().commandPermissionLevel))
                        .executes(CameraCommand::createMapOfSourceScaled))
                .then(Commands.argument("source", EntityArgument.entity()).requires(Permissions.require("camera-obscura.command.entity", 2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(CameraCommand::createMapOfSourceForSource)
                                .then(Commands.argument("scale", IntegerArgumentType.integer(1,3)).requires(Permissions.require("camera-obscura.command.entity.scale", ModConfig.getInstance().commandPermissionLevel))
                                        .executes(CameraCommand::createMapOfSourceForSourceScaled))))
                .then(Commands.literal("save").requires(Permissions.require("camera-obscura.command.save", ModConfig.getInstance().commandPermissionLevel))
                        .executes(x -> {
                            if (x.getSource().getEntity() instanceof LivingEntity livingEntity)
                                CameraCommand.createImageAsync(x, livingEntity, 1);
                            return 0;
                        })
                        .then(Commands.argument("source", EntityArgument.entity()).requires(Permissions.require("camera-obscura.command.save.entity", ModConfig.getInstance().commandPermissionLevel))
                                .executes(x -> createImageFromSource(x, 1))
                                .then(Commands.argument("scale", IntegerArgumentType.integer(1,20)).requires(Permissions.require("camera-obscura.command.save.entity.scale", ModConfig.getInstance().commandPermissionLevel))
                                        .executes(x -> createImageFromSource(x, IntegerArgumentType.getInteger(x, "scale")))
                                ))
                        .then(Commands.argument("scale", IntegerArgumentType.integer(1,20)).requires(Permissions.require("camera-obscura.command.save.scale", ModConfig.getInstance().commandPermissionLevel))
                                .executes(x -> {
                                    if (x.getSource().getEntity() instanceof LivingEntity livingEntity)
                                        CameraCommand.createImageAsync(x, livingEntity, IntegerArgumentType.getInteger(x, "scale"));
                                    return 0;
                                })
                        )
                )
                .then(Commands.literal("clear-cache").requires(Permissions.require("camera-obscura.command.clear-cache", 2))
                        .executes(x -> {
                            Raytracer.clearCache();
                            RPHelper.clearCache();
                            return 0;
                        }))
                .build();

        dispatcher.getRoot().addChild(node);
    }

    private static int createImageFromSource(CommandContext<CommandSourceStack> x, int scale) throws CommandSyntaxException {
        var source = EntityArgument.getEntity(x, "source");
        if (source instanceof LivingEntity livingEntity)
            CameraCommand.createImageAsync(x, livingEntity, scale);
        return 0;
    }

    private static int createMapOfSourceScaled(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getPlayer() == null) {
            context.getSource().sendFailure(Component.literal("Needs to be executed as player!"));
        }

        var scale = IntegerArgumentType.getInteger(context, "scale");

        return createMap(context, context.getSource().getPlayer(), context.getSource().getPlayer(), scale);
    }

    private static int createMapOfSourceForSourceScaled(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getPlayer() == null) {
            context.getSource().sendFailure(Component.literal("Needs to be executed as player!"));
        }

        Player player;
        Entity source;
        var scale = IntegerArgumentType.getInteger(context, "scale");
        try {
            source = EntityArgument.getEntity(context, "source");
            player = EntityArgument.getPlayer(context, "player");
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        if (source instanceof LivingEntity livingEntity) {
            return createMap(context, livingEntity, player, scale);
        }

        return 0;

    }

    private static int createMapOfSourceForSource(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getPlayer() == null) {
            context.getSource().sendFailure(Component.literal("Needs to be executed as player!"));
        }

        return createMap(context, context.getSource().getPlayer(), context.getSource().getPlayer(), 1);
    }

    private static int createMap(CommandContext<CommandSourceStack> context, LivingEntity entity, Player player, int scale) {
        CommandSourceStack source = context.getSource();

        if (ModConfig.getInstance().showSystemMessages)
            source.sendSuccess(() -> Component.literal("Taking photo..."), false);

        long startTime = System.nanoTime();

        int size = 128*scale;

        boolean async = ModConfig.getInstance().renderAsyncMap;
        if (async) {
            var renderer = new CanvasImageRenderer(entity, size, size, ModConfig.getInstance().renderDistance);

            CompletableFuture.supplyAsync(() -> {
                try {
                    return renderer.render();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }).thenAcceptAsync(mapImage -> {
                finalize(player, mapImage, source, startTime);
            }, source.getServer());
        }
        else {
            var mapImage = new CanvasImageRenderer(entity, size, size, ModConfig.getInstance().renderDistance).render();
            finalize(player, mapImage, source, startTime);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static void finalize(Player player, CanvasImage mapImage, CommandSourceStack source, long startTime) {
        source.sendSuccess(() -> Component.literal("Took a photo!"), false);

        var items = CameraCommand.mapItems(mapImage, source.getLevel());

        if (player != null) {
            items.forEach(player::addItem);
        } else if (source.getPlayer() != null) {
            items.forEach(source.getPlayer()::addItem);
        }

        if (ModConfig.getInstance().showSystemMessages) {
            long durationInMillis = (System.nanoTime() - startTime) / 1000000;
            long millis = durationInMillis % 1000;
            long second = (durationInMillis / 1000) % 60;
            String time = String.format("%d.%02d seconds", second, millis);
            source.sendSuccess(() -> Component.literal("Done! ("+time+")"), false);
        }
    }

    public static List<ItemStack> mapItems(CanvasImage image, Level level) {
        var xSections = Mth.ceil(image.getWidth() / 128d);
        var ySections = Mth.ceil(image.getHeight() / 128d);

        var xDelta = (xSections * 128 - image.getWidth()) / 2;
        var yDelta = (ySections * 128 - image.getHeight()) / 2;

        var items = new ArrayList<ItemStack>();

        for (int ys = 0; ys < ySections; ys++) {
            for (int xs = 0; xs < xSections; xs++) {
                var id = level.getFreeMapId();
                var state = MapItemSavedData.createFresh(0, 0, (byte) 0, false, false, ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath("camera-obscura", "generated")));

                for (int xl = 0; xl < 128; xl++) {
                    for (int yl = 0; yl < 128; yl++) {
                        var x = xl + xs * 128 - xDelta;
                        var y = yl + ys * 128 - yDelta;

                        if (x >= 0 && y >= 0 && x < image.getWidth() && y < image.getHeight()) {
                            state.colors[xl + yl * 128] = image.getRaw(x, y);
                        }
                    }
                }

                level.setMapData(id, state);

                var stack = new ItemStack(Items.FILLED_MAP);
                stack.set(DataComponents.MAP_ID, id);
                items.add(stack);
            }
        }

        return items;
    }


    private static int createImageAsync(CommandContext<CommandSourceStack> context, LivingEntity entity, int scale) {
        CommandSourceStack source = context.getSource();

        if (ModConfig.getInstance().showSystemMessages)
            source.sendSuccess(() -> Component.literal("Taking photo..."), false);

        var renderer = new BufferedImageRenderer(entity, 128*scale, 128*scale, ModConfig.getInstance().renderDistance);

        long startTime = System.nanoTime();

        if (ModConfig.getInstance().renderAsyncImage) {
            CompletableFuture.supplyAsync(renderer::render).thenAcceptAsync(mapImage -> {
                finalizeImage(mapImage, startTime, source);
            }, source.getServer());
        } else {
            finalizeImage(renderer.render(), startTime, source);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static void finalizeImage(BufferedImage mapImage, long startTime, CommandSourceStack source) {
        var rendersDir = FabricLoader.getInstance().getGameDir().resolve("renders").toAbsolutePath();
        var f = rendersDir.toFile();
        if (!f.exists()) f.mkdir();

        String date = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss.SSS", Locale.ENGLISH).format(new Date());
        var file = rendersDir.resolve("img"+".png").toFile();

        try {
            ImageIO.write(mapImage, "PNG", file);

            if (ModConfig.getInstance().showSystemMessages) {
                long durationInMillis = (System.nanoTime() - startTime) / 1000000;
                long millis = durationInMillis % 1000;
                long second = (durationInMillis / 1000) % 60;
                String time = String.format("%d.%02d seconds", second, millis);
                source.sendSuccess(() -> Component.literal("Done! ("+time+")"), false);
            }
        } catch (IOException e) {
            LogUtils.getLogger().error("Could not write image to " + file.getPath());
        }
    }
}
