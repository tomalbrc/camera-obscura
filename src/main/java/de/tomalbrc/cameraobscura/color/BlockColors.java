package de.tomalbrc.cameraobscura.color;

import de.tomalbrc.cameraobscura.util.RPHelper;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.DryFoliageColor;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class BlockColors {
    private static BufferedImage GRASS_TEXTURE;
    private static BufferedImage FOLIAGE_TEXTURE;
    private static BufferedImage DRY_FOLIAGE_TEXTURE;

    private static void loadColorMaps() {
        try {
            GRASS_TEXTURE = ImageIO.read(new ByteArrayInputStream(RPHelper.loadTextureBytes(ResourceLocation.withDefaultNamespace("colormap/grass"))));
            FOLIAGE_TEXTURE = ImageIO.read(new ByteArrayInputStream(RPHelper.loadTextureBytes(ResourceLocation.withDefaultNamespace("colormap/foliage"))));
            DRY_FOLIAGE_TEXTURE = ImageIO.read(new ByteArrayInputStream(RPHelper.loadTextureBytes(ResourceLocation.withDefaultNamespace("colormap/dry_foliage"))));

            GrassColor.init(GRASS_TEXTURE.getRGB(0, 0, GRASS_TEXTURE.getWidth(), GRASS_TEXTURE.getHeight(), null, 0, GRASS_TEXTURE.getWidth()));
            FoliageColor.init(FOLIAGE_TEXTURE.getRGB(0, 0, FOLIAGE_TEXTURE.getWidth(), FOLIAGE_TEXTURE.getHeight(), null, 0, FOLIAGE_TEXTURE.getWidth()));
            DryFoliageColor.init(DRY_FOLIAGE_TEXTURE.getRGB(0, 0, DRY_FOLIAGE_TEXTURE.getWidth(), DRY_FOLIAGE_TEXTURE.getHeight(), null, 0, DRY_FOLIAGE_TEXTURE.getWidth()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    interface BlockColorProvider {
        int get(LevelChunk level, BlockState blockState, BlockPos blockPos);
    }

    private static final Reference2ObjectArrayMap<Block, BlockColorProvider> colors = new Reference2ObjectArrayMap<>();

    public static void init() {
        loadColorMaps();

        BlockColorProvider grassColor = (level, blockState, blockPos) -> getBiome(level, blockPos).value().getGrassColor(blockPos.getX(), blockPos.getZ());
        BlockColorProvider foliageColor = (level, blockState, blockPos) -> getBiome(level, blockPos).value().getFoliageColor();
        BlockColorProvider dryFoliageColor = (level, blockState, blockPos) -> getBiome(level, blockPos).value().getDryFoliageColor();

        colors.put(Blocks.LARGE_FERN, grassColor);

        colors.put(Blocks.SHORT_DRY_GRASS, dryFoliageColor);
        colors.put(Blocks.TALL_DRY_GRASS, dryFoliageColor);
        colors.put(Blocks.LEAF_LITTER, dryFoliageColor);

        colors.put(Blocks.SUGAR_CANE, foliageColor);

        colors.put(Blocks.GRASS_BLOCK, grassColor);
        colors.put(Blocks.FERN, grassColor);
        colors.put(Blocks.SHORT_GRASS, grassColor);
        colors.put(Blocks.TALL_GRASS, grassColor);
        colors.put(Blocks.POTTED_FERN, grassColor);

        colors.put(Blocks.PINK_PETALS, grassColor);

        colors.put(Blocks.SPRUCE_LEAVES, (level, blockState, blockPos) -> FoliageColor.FOLIAGE_EVERGREEN);
        colors.put(Blocks.BIRCH_LEAVES, (level, blockState, blockPos) -> FoliageColor.FOLIAGE_BIRCH);

        colors.put(Blocks.OAK_LEAVES, foliageColor);
        colors.put(Blocks.JUNGLE_LEAVES, foliageColor);
        colors.put(Blocks.ACACIA_LEAVES, foliageColor);
        colors.put(Blocks.DARK_OAK_LEAVES, foliageColor);

        colors.put(Blocks.VINE, foliageColor);
        colors.put(Blocks.MANGROVE_LEAVES, foliageColor);

        //colors.put(Blocks.WATER, (level, blockState, blockPos) -> 0x3F76E4);
        //colors.put(Blocks.BUBBLE_COLUMN, (level, blockState, blockPos) -> -1);
        //colors.put(Blocks.WATER_CAULDRON, (level, blockState, blockPos) -> -1);

        colors.put(Blocks.REDSTONE_WIRE, (level, blockState, blockPos) -> RedStoneWireBlock.getColorForPower(blockState.getValue(RedStoneWireBlock.POWER)));

        //colors.put(Blocks.SUGAR_CANE, (blockState) -> GrassColor.getDefaultColor());

        colors.put(Blocks.ATTACHED_MELON_STEM, (level, blockState, blockPos) -> 0xE0C71C);
        colors.put(Blocks.ATTACHED_PUMPKIN_STEM, (level, blockState, blockPos) -> 0xE0C71C);

        colors.put(Blocks.MELON_STEM, (level, blockState, blockPos) -> {
            int j = blockState.getValue(StemBlock.AGE);
            int k = j * 32;
            int l = 255 - j * 8;
            int m = j * 4;
            return k << 16 | l << 8 | m;
        });
        colors.put(Blocks.PUMPKIN_STEM, (level, blockState, blockPos) -> {
            int j = blockState.getValue(StemBlock.AGE);
            int k = j * 32;
            int l = 255 - j * 8;
            int m = j * 4;
            return k << 16 | l << 8 | m;
        });

        colors.put(Blocks.LILY_PAD, (level, blockState, blockPos) -> 0x208030);
    }

    public static int get(LevelChunk level, BlockState blockState, BlockPos blockPos) {
        if (colors.containsKey(blockState.getBlock()))
            return colors.get(blockState.getBlock()).get(level, blockState, blockPos) | 0xff_00_00_00;

        return -1;
    }

    public static int biomeWaterColor(LevelChunk level, BlockPos blockPos) {
        return 0xFF<<24 | getBiome(level, blockPos).value().getWaterColor();
    }

    private static Holder<Biome> getBiome(LevelChunk chunk, BlockPos blockPos) {
        return chunk.getNoiseBiome(
                QuartPos.fromBlock(blockPos.getX()),
                QuartPos.fromBlock(blockPos.getY()),
                QuartPos.fromBlock(blockPos.getZ())
        );
    }
}
