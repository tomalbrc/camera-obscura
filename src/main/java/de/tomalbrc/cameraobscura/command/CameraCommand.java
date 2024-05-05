package de.tomalbrc.cameraobscura.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.tomalbrc.cameraobscura.render.renderer.CanvasImageRenderer;
import eu.pb4.mapcanvas.api.core.CanvasImage;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CameraCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> camera_obscura = Commands.literal("camera-obscura").requires(Permissions.require("camera-obscura.command", 1));

        camera_obscura.executes(CameraCommand::createMapOfSourceForSource);
        camera_obscura.then(Commands.literal("async").executes(CameraCommand::createMapAsyncOfSource));

        LiteralCommandNode<CommandSourceStack> gestureNode = camera_obscura.build();

        dispatcher.getRoot().addChild(gestureNode);
    }

    private static int createMapOfSourceForSource(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getPlayer() == null) {
            context.getSource().sendFailure(Component.literal("Needs to be executed as player!"));
        }

        return createMap(context, context.getSource().getPlayer(), context.getSource().getPlayer());
    }

    private static int createMap(CommandContext<CommandSourceStack> context, LivingEntity entity, Player player) {
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> Component.literal("Taking photo..."), false);

        long startTime = System.nanoTime();

        CanvasImage mapImage = null;
        try {
            int size = 128;//512;
            mapImage = new CanvasImageRenderer(player, size, size).render();
            finalize(player, mapImage, source, startTime);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return Command.SINGLE_SUCCESS;
    }
    private static int createMapAsync(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getPlayer() == null) {
            context.getSource().sendFailure(Component.literal("Needs to be executed as player!"));
        }

        return createMapAsync(context, context.getSource().getPlayer(), context.getSource().getPlayer());
    }

    private static int createMapAsyncOfSource(CommandContext<CommandSourceStack> context) {
        if (context.getSource().getPlayer() == null) {
            context.getSource().sendFailure(Component.literal("Needs to be executed as player!"));
        }

        return createMapAsync(context, context.getSource().getPlayer(), context.getSource().getPlayer());
    }

    private static int createMapAsync(CommandContext<CommandSourceStack> context, LivingEntity entity, Player player) {
        CommandSourceStack source = context.getSource();

        source.sendSuccess(() -> Component.literal("Taking photo..."), false);
        long startTime = System.nanoTime();

        var renderer = new CanvasImageRenderer(entity, 128, 128);
        CompletableFuture.supplyAsync(() -> {
            try {
                return renderer.render();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }).thenAcceptAsync(mapImage -> {
            finalize(player, mapImage, source, startTime);
        }, source.getServer());

        return Command.SINGLE_SUCCESS;
    }

    private static void finalize(Player player, CanvasImage mapImage, CommandSourceStack source, long startTime) {
        source.sendSuccess(() -> Component.literal("Took a photo!"), false);

        var items = CameraCommand.toVanillaItems(mapImage, source.getLevel());

        if (player != null) {
            items.forEach(player::addItem);
        } else if (source.getPlayer() != null) {
            items.forEach(source.getPlayer()::addItem);
        }

        long durationInMillis = (System.nanoTime() - startTime) / 1000000;
        long millis = durationInMillis % 1000;
        long second = (durationInMillis / 1000) % 60;
        String time = String.format("%d.%02d seconds", second, millis);

        source.sendSuccess(() -> Component.literal("Done! ("+time+")"), false);
    }

    public static List<ItemStack> toVanillaItems(CanvasImage image, ServerLevel level) {
        var xSections = Mth.ceil(image.getWidth() / 128d);
        var ySections = Mth.ceil(image.getHeight() / 128d);

        var xDelta = (xSections * 128 - image.getWidth()) / 2;
        var yDelta = (ySections * 128 - image.getHeight()) / 2;

        var items = new ArrayList<ItemStack>();

        for (int ys = 0; ys < ySections; ys++) {
            for (int xs = 0; xs < xSections; xs++) {
                var id = level.getFreeMapId();
                var state = MapItemSavedData.createFresh(0, 0, (byte) 0, false, false, ResourceKey.create(Registries.DIMENSION, new ResourceLocation("cameraobscura", "generated")));

                for (int xl = 0; xl < 128; xl++) {
                    for (int yl = 0; yl < 128; yl++) {
                        var x = xl + xs * 128 - xDelta;
                        var y = yl + ys * 128 - yDelta;

                        if (x >= 0 && y >= 0 && x < image.getWidth() && y < image.getHeight()) {
                            state.colors[xl + yl * 128] = image.getRaw(x, y);
                        }
                    }
                }

                level.setMapData(MapItem.makeKey(id), state);

                var stack = new ItemStack(Items.FILLED_MAP);
                stack.getOrCreateTag().putInt("map", id);
                var lore = new ListTag();
                lore.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal(xs + " / " + ys).withStyle(ChatFormatting.GRAY))));
                stack.getOrCreateTagElement("display").put("Lore", lore);
                items.add(stack);
            }
        }

        return items;
    }
}
