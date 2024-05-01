package de.tomalbrc.cameraobscura.util;

import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BlockColors {
    interface BlockColorProvider {
        int get(BlockState blockState);
    }

    private static Reference2ObjectArrayMap<Block, BlockColorProvider> colors = new Reference2ObjectArrayMap<>();

    public static void init() {
        BlockColorProvider grassColor = (blockState) -> FoliageColor.getDefaultColor();

        colors.put(Blocks.LARGE_FERN, grassColor);
        colors.put(Blocks.TALL_GRASS, grassColor);

        colors.put(Blocks.GRASS_BLOCK, grassColor);
        colors.put(Blocks.FERN, grassColor);
        colors.put(Blocks.SHORT_GRASS, grassColor);
        colors.put(Blocks.POTTED_FERN, grassColor);

        colors.put(Blocks.PINK_PETALS, grassColor);

        colors.put(Blocks.SPRUCE_LEAVES, (blockState) -> FoliageColor.getEvergreenColor());
        colors.put(Blocks.BIRCH_LEAVES, (blockState) -> FoliageColor.getBirchColor());

        colors.put(Blocks.OAK_LEAVES, (blockState) -> FoliageColor.getDefaultColor());
        colors.put(Blocks.JUNGLE_LEAVES, (blockState) -> FoliageColor.getDefaultColor());
        colors.put(Blocks.ACACIA_LEAVES, (blockState) -> FoliageColor.getDefaultColor());
        colors.put(Blocks.DARK_OAK_LEAVES, (blockState) -> FoliageColor.getDefaultColor());
        colors.put(Blocks.VINE, (blockState) -> FoliageColor.getDefaultColor());
        colors.put(Blocks.MANGROVE_LEAVES, (blockState) -> FoliageColor.getDefaultColor());

        //colors.put(Blocks.WATER, (blockState) -> -1);
        colors.put(Blocks.BUBBLE_COLUMN, (blockState) -> -1);
        colors.put(Blocks.WATER_CAULDRON, (blockState) -> -1);

        colors.put(Blocks.REDSTONE_WIRE, (blockState) -> RedStoneWireBlock.getColorForPower(blockState.getValue(RedStoneWireBlock.POWER)));

        //colors.put(Blocks.SUGAR_CANE, (blockState) -> GrassColor.getDefaultColor());

        colors.put(Blocks.ATTACHED_MELON_STEM, (blockState) -> 14731036);
        colors.put(Blocks.ATTACHED_PUMPKIN_STEM, (blockState) -> 14731036);

        colors.put(Blocks.MELON_STEM, (blockState) -> {
            int j = blockState.getValue(StemBlock.AGE);
            int k = j * 32;
            int l = 255 - j * 8;
            int m = j * 4;
            return k << 16 | l << 8 | m;
        });
        colors.put(Blocks.PUMPKIN_STEM, (blockState) -> {
            int j = blockState.getValue(StemBlock.AGE);
            int k = j * 32;
            int l = 255 - j * 8;
            int m = j * 4;
            return k << 16 | l << 8 | m;
        });

        colors.put(Blocks.LILY_PAD, (blockState) -> 2129968);
    }

    public static int get(BlockState blockState) {
        if (colors.containsKey(blockState.getBlock()))
            return colors.get(blockState.getBlock()).get(blockState) | 0xff_00_00_00;

        return -1;
    }
}
