package de.tomalbrc.cameraobscura.item;

import com.mojang.serialization.MapCodec;
import de.tomalbrc.cameraobscura.CameraObscura;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockAwareAttachment;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class CameraBlock extends BaseEntityBlock implements PolymerBlock, BlockWithElementHolder {
    public static EnumProperty<State> STATE = EnumProperty.create("state", State.class);
    public static final MapCodec<CameraBlock> CODEC = simpleCodec(CameraBlock::new);

    public CameraBlock(Properties properties) {
        super(properties);

        this.registerDefaultState(this.defaultBlockState().setValue(STATE, State.OFF).setValue(BlockStateProperties.WATERLOGGED, false));
    }

    @Override
    protected @NonNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NonNull BlockPos worldPosition, @NonNull BlockState blockState) {
        return new CameraBlockEntity(worldPosition, blockState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(final @NonNull Level level, final @NonNull BlockState blockState, final @NonNull BlockEntityType<T> type) {
        return level instanceof ServerLevel serverLevel
                ? createTickerHelper(type, CameraObscura.CAMERA_BLOCK_ENTITY, (_, pos, state, entity) -> CameraBlockEntity.serverTick(serverLevel, pos, state, entity))
                : null;

    }

    @Override
    protected @NonNull InteractionResult useItemOn(@NonNull ItemStack heldItem, @NonNull BlockState state, Level level,
                                                   @NonNull BlockPos pos, @NonNull Player player, @NonNull InteractionHand hand,
                                                   @NonNull BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof CameraBlockEntity cam) {

            if (cam.getItem(0).isEmpty() && heldItem.is(Items.MAP)) {
                ItemStack inserted = heldItem.split(1);
                cam.setItem(0, inserted);

                player.sendSystemMessage(
                        Component.literal("Map inserted!")
                );
                return InteractionResult.SUCCESS;
            }
        }
        return super.useItemOn(heldItem, state, level, pos, player, hand, hit);
    }

    @Override
    protected @NonNull InteractionResult useWithoutItem(
            @NonNull BlockState state, Level level, @NonNull BlockPos pos,
            @NonNull Player player, @NonNull BlockHitResult hitResult) {

        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof CameraBlockEntity camEnt) {
            float yaw = player.getYHeadRot();
            float pitch = player.getXRot();
            camEnt.setRotation(yaw, pitch);
            player.sendSystemMessage(
                    Component.literal("Updated Camera angles!")
            );

            var at = BlockAwareAttachment.get(level, pos);
            if (at != null && at.holder() instanceof CameraHolder holder) {
                holder.setRotation(yaw, pitch);
                holder.tick();
            }

            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NonNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);

        builder.add(STATE);
        builder.add(BlockStateProperties.WATERLOGGED);
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, @Nullable PacketContext context) {
        return Blocks.BARRIER.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, state.getValue(BlockStateProperties.WATERLOGGED));
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return new CameraHolder();
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState replacedFluidState = context.getLevel().getFluidState(context.getClickedPos());
        return defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, replacedFluidState.is(Fluids.WATER));
    }

    @Override
    protected void neighborChanged(final @NonNull BlockState state, final Level level, final @NonNull BlockPos pos, final @NonNull Block block, @Nullable final Orientation orientation, final boolean movedByPiston) {
        if (!level.isClientSide()) {
            var s = state.getValue(STATE);
            var hasSignal = level.hasNeighborSignal(pos);

            if (s == State.OFF && hasSignal) {
                level.setBlock(pos, state.cycle(STATE), Block.UPDATE_ALL);
                level.scheduleTick(pos, this, 20 * 2);
            } else if (s == State.RECORDING && !hasSignal) {
                level.setBlock(pos, state.cycle(STATE), Block.UPDATE_ALL);
            }
        }
    }

    @Override
    protected void tick(final BlockState state, final @NonNull ServerLevel level, final @NonNull BlockPos pos, final @NonNull RandomSource random) {
        if (state.getValue(STATE) != State.OFF && !level.hasNeighborSignal(pos)) {
            level.setBlock(pos, state.setValue(STATE, State.OFF), Block.UPDATE_ALL);
        } else if (state.getValue(STATE) == State.WARMUP && level.hasNeighborSignal(pos)) {
            level.setBlock(pos, state.setValue(STATE, State.RECORDING), Block.UPDATE_ALL);
        }
    }

    public enum State implements StringRepresentable {
        OFF("off"),
        WARMUP("warmup"),
        RECORDING("recording");

        final String name;

        State(String name) {
            this.name = name;
        }

        @Override
        public @NonNull String getSerializedName() {
            return name;
        }
    }
}
