package de.tomalbrc.cameraobscura.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.tomalbrc.cameraobscura.ServerRenderer;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CameraCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> blockboy = Commands.literal("camera").requires(Permissions.require("camera-obscura.command", 1));

        blockboy.executes(CameraCommand::createMap);

        LiteralCommandNode<CommandSourceStack> gestureNode = blockboy.build();

        dispatcher.getRoot().addChild(gestureNode);
    }

    private static int createMap(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        ServerPlayer player = source.getPlayer();

        source.sendSuccess(() -> Component.literal("Taking photo..."), false);

        int finalHeight = 128;
        int finalWidth = 128;

//        CompletableFuture.supplyAsync(() -> {
//            try {
//                return new ServerRenderer(player).render();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            return null;
//        }).thenAcceptAsync(mapImage -> {
//            source.sendSuccess(() -> Component.literal("Took a photo!"), false);
//
//            var items = CameraCommand.toVanillaItems(mapImage, source.getLevel());
//            player.addItem(items.get(0));
//            source.sendSuccess(() -> Component.literal("Done!"), false);
//        }, source.getServer());

        CanvasImage mapImage = null;
        try {
            mapImage = new ServerRenderer(player).render();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        source.sendSuccess(() -> Component.literal("Took a photo!"), false);

        var items = CameraCommand.toVanillaItems(mapImage, source.getLevel());
        player.addItem(items.get(0));
        source.sendSuccess(() -> Component.literal("Done!"), false);

        return Command.SINGLE_SUCCESS;
    }


    private static CompletableFuture<BufferedImage> getImage(String input) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return new BufferedImage(128,128, BufferedImage.TYPE_INT_RGB);
            } catch (Throwable e) {
                e.printStackTrace();
                return null;
            }
        });
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

                // getMapName() = makeKey??????? TODO: FUCK
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
