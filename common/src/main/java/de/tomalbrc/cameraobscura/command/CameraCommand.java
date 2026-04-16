package de.tomalbrc.cameraobscura.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.tomalbrc.cameraobscura.ModConfig;
import de.tomalbrc.cameraobscura.platform.Platforms;
import de.tomalbrc.cameraobscura.renderer.WorldRenderer;
import de.tomalbrc.cameraobscura.util.ImageUtils;
import de.tomalbrc.cameraobscura.util.resource.RPHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.CommonColors;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class CameraCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("camera-obscura")
                .requires(Platforms.get().getPermission("cameraobscura.command", 2));

        //  /camera-obscura <scale> 
        root.then(Commands.argument("scale", IntegerArgumentType.integer(1, 3))
                .requires(Platforms.get().getPermission("cameraobscura.command.scale",
                        ModConfig.getInstance().commandPermissionLevel))
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    int scale = ctx.getArgument("scale", Integer.class);
                    return createMap(ctx, player, player, scale);
                }));

        //  /camera-obscura <source> <player> [scale] 
        root.then(Commands.argument("source", EntityArgument.entity())
                .requires(Platforms.get().getPermission("cameraobscura.command.type", 2))
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(ctx -> {
                            LivingEntity source = getLivingEntity(ctx, "source");
                            ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                            return createMap(ctx, source, player, 1);
                        })
                        .then(Commands.argument("scale", IntegerArgumentType.integer(1, 3))
                                .requires(Platforms.get().getPermission("cameraobscura.command.type.scale",
                                        ModConfig.getInstance().commandPermissionLevel))
                                .executes(ctx -> {
                                    LivingEntity source = getLivingEntity(ctx, "source");
                                    ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                                    int scale = ctx.getArgument("scale", Integer.class);
                                    return createMap(ctx, source, player, scale);
                                }))));

        //  /camera-obscura save 
        LiteralArgumentBuilder<CommandSourceStack> saveNode = Commands.literal("save")
                .requires(Platforms.get().getPermission("cameraobscura.command.save",
                        ModConfig.getInstance().commandPermissionLevel));
        saveNode.executes(ctx -> {
            if (ctx.getSource().getEntity() instanceof LivingEntity entity)
                createImageAsync(ctx, entity, 1);
            return 0;
        });
        saveNode.then(Commands.argument("source", EntityArgument.entity())
                .requires(Platforms.get().getPermission("cameraobscura.command.save.type", 2))
                .executes(ctx -> {
                    LivingEntity entity = getLivingEntity(ctx, "source");
                    if (entity != null) createImageAsync(ctx, entity, 1);
                    return 0;
                })
                .then(Commands.argument("scale", IntegerArgumentType.integer(1, 20))
                        .requires(Platforms.get().getPermission("cameraobscura.command.save.type.scale",
                                ModConfig.getInstance().commandPermissionLevel))
                        .executes(ctx -> {
                            LivingEntity entity = getLivingEntity(ctx, "source");
                            int scale = ctx.getArgument("scale", Integer.class);
                            if (entity != null) createImageAsync(ctx, entity, scale);
                            return 0;
                        })));
        saveNode.then(Commands.argument("scale", IntegerArgumentType.integer(1, 20))
                .requires(Platforms.get().getPermission("cameraobscura.command.save.scale",
                        ModConfig.getInstance().commandPermissionLevel))
                .executes(ctx -> {
                    if (ctx.getSource().getEntity() instanceof LivingEntity entity) {
                        int scale = ctx.getArgument("scale", Integer.class);
                        createImageAsync(ctx, entity, scale);
                    }
                    return 0;
                }));
        root.then(saveNode);

        root.then(Commands.literal("reload")
                .requires(Platforms.get().getPermission("cameraobscura.command.reload", 2))
                .executes(ctx -> {
                    ModConfig.load();
                    PresetManager.loadPresets();
                    ctx.getSource().sendSuccess(() -> Component.literal("Reloaded config!"), false);
                    return 0;
                }));

        //  /camera-obscura clear-cache 
        root.then(Commands.literal("clear-cache")
                .requires(Platforms.get().getPermission("cameraobscura.command.clear-cache", 2))
                .executes(ctx -> {
                    RPHelper.clearCache();
                    ctx.getSource().sendSuccess(() -> Component.literal("Cache cleared!"), false);
                    return 0;
                }));

        //  /camera-obscura give 
        LiteralArgumentBuilder<CommandSourceStack> giveNode = Commands.literal("give")
                .then(Commands.argument("preset", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            for (String name : PresetManager.getPresetNames()) {
                                builder.suggest(name);
                            }
                            return builder.buildFuture();
                        })
                        .executes(ctx -> {
                            ServerPlayer target = ctx.getSource().getPlayerOrException();
                            String presetName = ctx.getArgument("preset", String.class);
                            return givePreset(ctx, target, presetName);
                        })
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> {
                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                    String presetName = ctx.getArgument("preset", String.class);
                                    return givePreset(ctx, target, presetName);
                                })));

        root.then(giveNode);

        dispatcher.getRoot().addChild(root.build());
    }

    private static int givePreset(CommandContext<CommandSourceStack> ctx, ServerPlayer target, String presetName) {
        CameraPreset preset = PresetManager.getPreset(presetName);
        if (preset == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown preset: " + presetName));
            return 0;
        }

        ItemStack camera = preset.createItemStack();
        target.addItem(camera);
        ctx.getSource().sendSuccess(() -> Component.literal("Gave " + presetName + " camera to " + target.getName()), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int createMap(CommandContext<CommandSourceStack> ctx, LivingEntity entity, ServerPlayer player, int scale) {
        CommandSourceStack source = ctx.getSource();
        if (entity == null || player == null) {
            source.sendFailure(Component.literal("Invalid source or target player."));
            return 0;
        }
        if (ModConfig.getInstance().showSystemMessages)
            source.sendSuccess(() -> Component.literal("Taking photo..."), false);

        long startTime = System.nanoTime();
        int size = 128 * scale;
        ServerLevel level = player.level();

        WorldRenderer renderer = new WorldRenderer(level, size, size, ModConfig.getInstance().renderDistance);
        renderer.entity = entity;
        renderer.updateCamera(entity);

        CompletableFuture.supplyAsync(() -> {
                    renderer.updateChunksInRange(player.level(), true);
                    return renderer.render();
                })
                .thenAcceptAsync(image -> {
                    if (image == null) {
                        source.sendFailure(Component.literal("Render failed."));
                        return;
                    }

                    List<ItemStack> mapStacks = ImageUtils.createMapItems(image, level, entity);
                    for (ItemStack stack : mapStacks) {
                        player.addItem(stack);

                    }
                    if (ModConfig.getInstance().showSystemMessages) {
                        long millis = (System.nanoTime() - startTime) / 1_000_000;
                        String time = millis > 1000 ? (millis / 1000) + "s " + (millis % 1000) + "ms" : millis + "ms";
                        source.sendSuccess(() -> Component.literal("Done! (" + time + ")"), false);
                    }
                }, source.getServer());

        return Command.SINGLE_SUCCESS;
    }

    private static void createImageAsync(CommandContext<CommandSourceStack> ctx, LivingEntity entity, int scale) {
        CommandSourceStack source = ctx.getSource();
        if (ModConfig.getInstance().showSystemMessages)
            source.sendSuccess(() -> Component.literal("Taking photo..."), false);

        WorldRenderer renderer = new WorldRenderer(entity.level(), 128 * scale, 128 * scale, ModConfig.getInstance().renderDistance);
        renderer.entity = entity;
        renderer.updateCamera(entity);

        long startTime = System.nanoTime();

        CompletableFuture.supplyAsync(() -> {
                    renderer.updateChunksInRange(entity.level(), true);
                    return renderer.render();
                })
                .thenAcceptAsync(image -> {
                    if (image == null) {
                        source.sendFailure(Component.literal("Render failed."));
                        return;
                    }
                    Path outDir = Platforms.get().getConfigDir().resolve("renders");
                    try {
                        Files.createDirectories(outDir);
                    } catch (IOException e) {
                        source.sendFailure(Component.literal("Cannot create renders folder."));
                        return;
                    }
                    String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH).format(new Date());
                    File file = outDir.resolve(date + ".png").toFile();
                    try {
                        ImageIO.write(image, "PNG", file);
                        if (ModConfig.getInstance().showSystemMessages) {
                            long millis = (System.nanoTime() - startTime) / 1_000_000;
                            String time = millis > 1000 ? (millis / 1000) + "s " + (millis % 1000) + "ms" : millis + "ms";
                            source.sendSuccess(() -> Component.literal("Saved as " + file.getName() + " (" + time + ")"), false);
                        }
                    } catch (IOException e) {
                        source.sendFailure(Component.literal("Failed to write image.")
                                .withStyle(style -> style.withColor(CommonColors.RED)));
                    }
                }, source.getServer());
    }

    private static LivingEntity getLivingEntity(CommandContext<CommandSourceStack> ctx, String name) {
        try {
            Entity entity = EntityArgument.getEntity(ctx, name);
            return entity instanceof LivingEntity le ? le : null;
        } catch (CommandSyntaxException e) {
            return null;
        }
    }
}