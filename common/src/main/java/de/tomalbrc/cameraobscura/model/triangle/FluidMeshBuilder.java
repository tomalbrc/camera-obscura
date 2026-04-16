package de.tomalbrc.cameraobscura.model.triangle;

import de.tomalbrc.cameraobscura.json.CachedIdentifierDeserializer;
import de.tomalbrc.cameraobscura.model.resource.RPElement;
import de.tomalbrc.cameraobscura.sore.Texture;
import de.tomalbrc.cameraobscura.sore.model.Mesh;
import de.tomalbrc.cameraobscura.util.resource.RPHelper;
import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.List;

public class FluidMeshBuilder {
    private static final float MAX_HEIGHT = 0.8888889f;
    private static final float EPSILON = 0.001f;

    public static Mesh createFluidMesh(BlockAndLightGetter level, BlockPos pos, FluidState fluidState) {
        List<Triangle> triangles = new ObjectArrayList<>();
        BlockState blockState = level.getBlockState(pos);

        BlockState blockStateDown = level.getBlockState(pos.below());
        FluidState fluidStateDown = blockStateDown.getFluidState();
        BlockState blockStateUp = level.getBlockState(pos.above());
        FluidState fluidStateUp = blockStateUp.getFluidState();
        BlockState blockStateNorth = level.getBlockState(pos.north());
        FluidState fluidStateNorth = blockStateNorth.getFluidState();
        BlockState blockStateSouth = level.getBlockState(pos.south());
        FluidState fluidStateSouth = blockStateSouth.getFluidState();
        BlockState blockStateWest = level.getBlockState(pos.west());
        FluidState fluidStateWest = blockStateWest.getFluidState();
        BlockState blockStateEast = level.getBlockState(pos.east());
        FluidState fluidStateEast = blockStateEast.getFluidState();

        boolean renderUp = !isNeighborSameFluid(fluidState, fluidStateUp);
        boolean renderDown = shouldRenderFace(fluidState, blockState, Direction.DOWN, fluidStateDown) &&
                !isFaceOccludedByNeighbor(Direction.DOWN, MAX_HEIGHT, blockStateDown);
        boolean renderNorth = shouldRenderFace(fluidState, blockState, Direction.NORTH, fluidStateNorth);
        boolean renderSouth = shouldRenderFace(fluidState, blockState, Direction.SOUTH, fluidStateSouth);
        boolean renderWest = shouldRenderFace(fluidState, blockState, Direction.WEST, fluidStateWest);
        boolean renderEast = shouldRenderFace(fluidState, blockState, Direction.EAST, fluidStateEast);

        if (!(renderUp || renderDown || renderEast || renderWest || renderNorth || renderSouth)) {
            return null;
        }

        Fluid type = fluidState.getType();
        float heightSelf = getHeight(level, type, pos, blockState, fluidState);
        float heightNorthEast, heightNorthWest, heightSouthEast, heightSouthWest;

        if (heightSelf >= 1.0F) {
            heightNorthEast = heightNorthWest = heightSouthEast = heightSouthWest = 1.0F;
        } else {
            float heightNorth = getHeight(level, type, pos.north(), blockStateNorth, fluidStateNorth);
            float heightSouth = getHeight(level, type, pos.south(), blockStateSouth, fluidStateSouth);
            float heightEast = getHeight(level, type, pos.east(), blockStateEast, fluidStateEast);
            float heightWest = getHeight(level, type, pos.west(), blockStateWest, fluidStateWest);
            heightNorthEast = calculateAverageHeight(level, type, heightSelf, heightNorth, heightEast, pos.north().east());
            heightNorthWest = calculateAverageHeight(level, type, heightSelf, heightNorth, heightWest, pos.north().west());
            heightSouthEast = calculateAverageHeight(level, type, heightSelf, heightSouth, heightEast, pos.south().east());
            heightSouthWest = calculateAverageHeight(level, type, heightSelf, heightSouth, heightWest, pos.south().west());
        }

        float x = 0.0F;
        float y = 0.0F;
        float z = 0.0F;
        float bottomOffs = renderDown ? EPSILON : 0.0F;

        Identifier stillTexture = getStillTexture(fluidState);
        Identifier flowingTexture = getFlowingTexture(fluidState);
        Identifier overlayTexture = getOverlayTexture(fluidState);

        // top
        if (renderUp && !isFaceOccludedByNeighbor(Direction.UP, Math.min(Math.min(heightNorthWest, heightSouthWest), Math.min(heightSouthEast, heightNorthEast)), blockStateUp)) {
            float hNW = heightNorthWest - EPSILON;
            float hSW = heightSouthWest - EPSILON;
            float hSE = heightSouthEast - EPSILON;
            float hNE = heightNorthEast - EPSILON;

            Vec3 flow = fluidState.getFlow(level, pos);
            float u00, v00, u01, v01, u10, v10, u11, v11;
            Identifier topTexture;

            if (flow.x == 0.0 && flow.z == 0.0) {
                u00 = 0.0F;
                v00 = 0.0F;
                u01 = 0.0F;
                v01 = 1.0F;
                u10 = 1.0F;
                v10 = 1.0F;
                u11 = 1.0F;
                v11 = 0.0F;
                topTexture = stillTexture;
            } else {
                // flowing
                float angle = (float) (Mth.atan2(flow.z, flow.x) - (Math.PI / 2));
                float s = Mth.sin(angle) * 0.25F;
                float c = Mth.cos(angle) * 0.25F;
                u00 = 0.5F + (-c - s);
                v00 = 0.5F + -c + s;
                u01 = 0.5F + -c + s;
                v01 = 0.5F + c + s;
                u10 = 0.5F + c + s;
                v10 = 0.5F + (c - s);
                u11 = 0.5F + (c - s);
                v11 = 0.5F + (-c - s);
                topTexture = flowingTexture;
            }

            //front
            triangles.addAll(createQuad(
                    new Vector3f(x, y + hNW, z), new Vector2f(u00, v00),
                    new Vector3f(x, y + hSW, z + 1.0F), new Vector2f(u01, v01),
                    new Vector3f(x + 1.0F, y + hSE, z + 1.0F), new Vector2f(u10, v10),
                    new Vector3f(x + 1.0F, y + hNE, z), new Vector2f(u11, v11),
                    true, 0, topTexture
            ));

            if (fluidState.shouldRenderBackwardUpFace(level, pos.above())) {
                triangles.addAll(createQuad(
                        new Vector3f(x + 1.0F, y + hNE, z), new Vector2f(u11, v11),
                        new Vector3f(x + 1.0F, y + hSE, z + 1.0F), new Vector2f(u10, v10),
                        new Vector3f(x, y + hSW, z + 1.0F), new Vector2f(u01, v01),
                        new Vector3f(x, y + hNW, z), new Vector2f(u00, v00),
                        false, 0, topTexture
                ));
            }
        }

        // bottom
        if (renderDown) {
            float u0 = 0.0F, v0 = 0.0F, u1 = 1.0F, v1 = 1.0F;
            triangles.addAll(createQuad(
                    new Vector3f(x, y + bottomOffs, z), new Vector2f(u0, v0),
                    new Vector3f(x + 1.0F, y + bottomOffs, z), new Vector2f(u1, v0),
                    new Vector3f(x + 1.0F, y + bottomOffs, z + 1.0F), new Vector2f(u1, v1),
                    new Vector3f(x, y + bottomOffs, z + 1.0F), new Vector2f(u0, v1),
                    false, 0, stillTexture
            ));
        }

        // sides
        for (Direction faceDir : Plane.HORIZONTAL) {
            float h0, h1;
            float x0, z0, x1, z1;
            boolean renderCondition;
            BlockState faceState;

            switch (faceDir) {
                case NORTH:
                    h0 = heightNorthWest;
                    h1 = heightNorthEast;
                    x0 = x;
                    x1 = x + 1.0F;
                    z0 = z + EPSILON;
                    z1 = z + EPSILON;
                    renderCondition = renderNorth;
                    faceState = blockStateNorth;
                    break;
                case SOUTH:
                    h0 = heightSouthEast;
                    h1 = heightSouthWest;
                    x0 = x + 1.0F;
                    x1 = x;
                    z0 = z + 1.0F - EPSILON;
                    z1 = z + 1.0F - EPSILON;
                    renderCondition = renderSouth;
                    faceState = blockStateSouth;
                    break;
                case WEST:
                    h0 = heightSouthWest;
                    h1 = heightNorthWest;
                    x0 = x + EPSILON;
                    x1 = x + EPSILON;
                    z0 = z + 1.0F;
                    z1 = z;
                    renderCondition = renderWest;
                    faceState = blockStateWest;
                    break;
                case EAST:
                    h0 = heightNorthEast;
                    h1 = heightSouthEast;
                    x0 = x + 1.0F - EPSILON;
                    x1 = x + 1.0F - EPSILON;
                    z0 = z;
                    z1 = z + 1.0F;
                    renderCondition = renderEast;
                    faceState = blockStateEast;
                    break;
                default:
                    continue;
            }

            if (renderCondition && !isFaceOccludedByNeighbor(faceDir, Math.max(h0, h1), faceState)) {
                boolean useOverlay = false;
                Block relativeBlock = faceState.getBlock();
                if (fluidState.is(FluidTags.WATER) && relativeBlock instanceof HalfTransparentBlock || relativeBlock instanceof LeavesBlock) {
                    useOverlay = true;
                }

                Identifier sideTexture = useOverlay ? overlayTexture : flowingTexture;

                float u0 = 0.0F, u1 = 0.5F;
                float v0 = (1.0F - h0) * 0.5F;
                float v1 = (1.0F - h1) * 0.5F;
                float vBottom = 0.5F;

                triangles.addAll(createQuad(
                        new Vector3f(x0, y + h0, z0), new Vector2f(u0, v0),
                        new Vector3f(x1, y + h1, z1), new Vector2f(u1, v1),
                        new Vector3f(x1, y + bottomOffs, z1), new Vector2f(u1, vBottom),
                        new Vector3f(x0, y + bottomOffs, z0), new Vector2f(u0, vBottom),
                        !useOverlay, 0, sideTexture
                ));
            }
        }

        if (triangles.isEmpty()) {
            return null;
        }

        return buildMeshFromTriangles(triangles);
    }

    private static List<Triangle> createQuad(Vector3f v0, Vector2f uv0,
                                             Vector3f v1, Vector2f uv1,
                                             Vector3f v2, Vector2f uv2,
                                             Vector3f v3, Vector2f uv3,
                                             boolean shade, int tintIndex,
                                             Identifier textureId) {
        List<Triangle> list = new ObjectArrayList<>(2);
        RPElement.TextureInfo texInfo = new RPElement.TextureInfo();
        texInfo.texture = textureId.toString();

        Triangle t1 = new Triangle(v0, v1, v2, uv0, uv1, uv2, shade);
        t1.tintindex = tintIndex;
        t1.textureInfo = texInfo;
        Triangle t2 = new Triangle(v0, v2, v3, uv0, uv2, uv3, shade);
        t2.tintindex = tintIndex;
        t2.textureInfo = texInfo;
        list.add(t1);
        list.add(t2);
        return list;
    }

    private static boolean isNeighborSameFluid(FluidState fluidState, FluidState neighborFluidState) {
        return neighborFluidState.getType().isSame(fluidState.getType());
    }

    private static boolean isFaceOccludedByState(final Direction direction, final double height, final BlockState state) {
        VoxelShape occluder = state.getFaceOcclusionShape(direction.getOpposite());
        if (occluder == Shapes.empty()) {
            return false;
        } else if (occluder == Shapes.block()) {
            boolean fullBlock = height == 1.0F;
            return direction != Direction.UP || fullBlock;
        } else {
            VoxelShape shape = Shapes.box(0.0F, 0.f, 0.0F, 1.0F, height, 1.0F);
            return Shapes.blockOccludes(shape, occluder, direction);
        }
    }

    private static boolean isFaceOccludedByNeighbor(Direction direction, float height, BlockState neighborState) {
        return isFaceOccludedByState(direction, height, neighborState);
    }

    private static boolean isFaceOccludedBySelf(BlockState state, Direction direction) {
        return isFaceOccludedByState(direction.getOpposite(), 1.0F, state);
    }

    private static boolean shouldRenderFace(FluidState fluidState, BlockState blockState, Direction direction, FluidState neighborFluidState) {
        return !isNeighborSameFluid(fluidState, neighborFluidState) && !isFaceOccludedBySelf(blockState, direction);
    }

    private static float getHeight(BlockAndLightGetter level, Fluid fluidType, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return getHeight(level, fluidType, pos, state, state.getFluidState());
    }

    private static float getHeight(BlockAndLightGetter level, Fluid fluidType, BlockPos pos, BlockState state, FluidState fluidState) {
        if (fluidType.isSame(fluidState.getType())) {
            BlockState aboveState = level.getBlockState(pos.above());
            return fluidType.isSame(aboveState.getFluidState().getType()) ? 1.0F : fluidState.getOwnHeight();
        } else {
            return !state.isSolid() ? 0.0F : -1.0F;
        }
    }

    private static float calculateAverageHeight(BlockAndLightGetter level, Fluid type, float heightSelf, float height2, float height1, BlockPos cornerPos) {
        if (height1 >= 1.0F || height2 >= 1.0F) {
            return 1.0F;
        }
        float[] weightedHeight = new float[2];
        if (height1 > 0.0F || height2 > 0.0F) {
            float heightCorner = getHeight(level, type, cornerPos);
            if (heightCorner >= 1.0F) {
                return 1.0F;
            }
            addWeightedHeight(weightedHeight, heightCorner);
        }
        addWeightedHeight(weightedHeight, heightSelf);
        addWeightedHeight(weightedHeight, height1);
        addWeightedHeight(weightedHeight, height2);
        return weightedHeight[0] / weightedHeight[1];
    }

    private static void addWeightedHeight(float[] weightedHeight, float height) {
        if (height >= 0.8F) {
            weightedHeight[0] += height * 10.0F;
            weightedHeight[1] += 10.0F;
        } else if (height >= 0.0F) {
            weightedHeight[0] += height;
            weightedHeight[1] += 1.0F;
        }
    }

    private static Identifier getStillTexture(FluidState state) {
        if (state.is(FluidTags.WATER)) {
            return Identifier.fromNamespaceAndPath("minecraft", "block/water_still");
        } else if (state.is(FluidTags.LAVA)) {
            return Identifier.fromNamespaceAndPath("minecraft", "block/lava_still");
        }

        return Identifier.fromNamespaceAndPath("minecraft", "block/water_still");
    }

    private static Identifier getFlowingTexture(FluidState state) {
        if (state.is(FluidTags.WATER)) {
            return Identifier.fromNamespaceAndPath("minecraft", "block/water_flow");
        } else if (state.is(FluidTags.LAVA)) {
            return Identifier.fromNamespaceAndPath("minecraft", "block/lava_flow");
        }
        return Identifier.fromNamespaceAndPath("minecraft", "block/water_flow");
    }

    private static Identifier getOverlayTexture(FluidState state) {
        //return getFlowingTexture(state);
        return Identifier.fromNamespaceAndPath("minecraft", "block/water_overlay");

    }

    private static Mesh buildMeshFromTriangles(List<Triangle> triangles) {
        FloatArrayList pos = new FloatArrayList();
        FloatArrayList norm = new FloatArrayList();
        FloatArrayList uv = new FloatArrayList();
        IntList indices = new IntArrayList();
        IntList tintIndices = new IntArrayList();
        BooleanList shade = new BooleanArrayList();
        List<Texture> textures = new ObjectArrayList<>();
        int vIdx = 0;

        for (Triangle tri : triangles) {
            String texStr = tri.textureInfo != null ? tri.textureInfo.texture : null;
            if (texStr == null) continue;

            Identifier texId = CachedIdentifierDeserializer.get(texStr);
            if (texId == null) continue;

            Texture texture = RPHelper.loadTexture(texId);
            if (texture == null) {
                continue;
            }

            Vector3fc n = tri.getNormal();
            Vector3f[] verts = {tri.v0, tri.v1, tri.v2};
            Vector2f[] uvs = {tri.uv0, tri.uv1, tri.uv2};

            for (int i = 0; i < 3; i++) {
                pos.add(verts[i].x);
                pos.add(verts[i].y);
                pos.add(verts[i].z);

                norm.add(n.x());
                norm.add(n.y());
                norm.add(n.z());

                uv.add(uvs[i].x);
                uv.add(uvs[i].y);

                textures.add(texture);
                indices.add(vIdx++);
                tintIndices.add(tri.tintindex);
                shade.add(tri.shade);
            }
        }

        return new Mesh(
                pos.toFloatArray(),
                norm.toFloatArray(),
                uv.toFloatArray(),
                indices.toIntArray(),
                tintIndices.toIntArray(),
                shade.toBooleanArray(),
                textures.toArray(new Texture[0]),
                false, true
        );
    }
}