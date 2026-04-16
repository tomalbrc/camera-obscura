package de.tomalbrc.cameraobscura.model.triangle;

import de.tomalbrc.cameraobscura.json.CachedIdentifierDeserializer;
import de.tomalbrc.cameraobscura.model.resource.RPElement;
import de.tomalbrc.cameraobscura.model.resource.RPModel;
import de.tomalbrc.cameraobscura.sore.Texture;
import de.tomalbrc.cameraobscura.sore.model.Mesh;
import de.tomalbrc.cameraobscura.util.resource.RPHelper;
import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class ModelTesselator {
    private static final int[][] UV_ROT = {{0, 1, 2, 3}, {1, 2, 3, 0}, {2, 3, 0, 1}, {3, 0, 1, 2}};
    private static final int[] TRI_INDICES = {0, 1, 2, 0, 2, 3};
    private static final Vector3d[] FACE_NORMALS_F = new Vector3d[6];
    private static final Map<Direction, Vector3d> FACE_LOCAL_UP = new EnumMap<>(Direction.class);

    static {
        for (Direction d : Direction.values()) {
            Vec3i v = d.getUnitVec3i();
            FACE_NORMALS_F[d.ordinal()] = new Vector3d(v.getX(), v.getY(), v.getZ());
        }

        FACE_LOCAL_UP.put(Direction.UP, new Vector3d(0, 0, -1));
        FACE_LOCAL_UP.put(Direction.DOWN, new Vector3d(0, 0, 1));
        FACE_LOCAL_UP.put(Direction.NORTH, new Vector3d(0, 1, 0));
        FACE_LOCAL_UP.put(Direction.SOUTH, new Vector3d(0, 1, 0));
        FACE_LOCAL_UP.put(Direction.WEST, new Vector3d(0, 1, 0));
        FACE_LOCAL_UP.put(Direction.EAST, new Vector3d(0, 1, 0));
    }

    private final Map<String, Identifier> textureMap = new Object2ObjectOpenHashMap<>();
    private final boolean ao;
    private final FloatArrayList posList = new FloatArrayList();
    private final FloatArrayList normList = new FloatArrayList();
    private final FloatArrayList uvList = new FloatArrayList();
    private final ObjectArrayList<Texture> texList = new ObjectArrayList<>();
    private final IntArrayList tintList = new IntArrayList();
    private final BooleanArrayList shadeList = new BooleanArrayList();
    private final IntArrayList indexList = new IntArrayList();
    private final float[] defaultUV = new float[4];
    private final Vector3f tmpCullVec = new Vector3f();
    private final Vector3f tmpOrigin = new Vector3f();
    private final Quaternionf tmpElementQuat = new Quaternionf();
    private final float[] scaleRes = new float[3];
    private final Vector3f tmpEuler = new Vector3f();

    private final float[] faceVertices = new float[12];
    private final float[] uvCornersU = new float[4];
    private final float[] uvCornersV = new float[4];

    private final Set<String> textureResolveVisited = new java.util.HashSet<>();

    private boolean translucent = false;
    private int vertexCount;

    private static final EnumSet<Direction> NONE = EnumSet.noneOf(Direction.class);

    public ModelTesselator(RPModel.View view) {
        this(view, NONE);
    }

    public ModelTesselator(RPModel.View view, EnumSet<Direction> culledFaces) {
        textureMap.putAll(view.model().collectTextures());
        ao = view.model().ambientOcclusion();
        buildMesh(view, culledFaces);
    }

    private static void rotateVertex(float[] v, int offset, Quaternionf q,
                                     float pivotX, float pivotY, float pivotZ,
                                     float scaleX, float scaleY, float scaleZ) {
        float rx = v[offset] - pivotX;
        float ry = v[offset + 1] - pivotY;
        float rz = v[offset + 2] - pivotZ;
        float qx = q.x, qy = q.y, qz = q.z, qw = q.w;

        float ix = qw * rx + qy * rz - qz * ry;
        float iy = qw * ry + qz * rx - qx * rz;
        float iz = qw * rz + qx * ry - qy * rx;
        float iw = -qx * rx - qy * ry - qz * rz;

        float rotx = ix * qw + iw * -qx + iy * -qz - iz * -qy;
        float roty = iy * qw + iw * -qy + iz * -qx - ix * -qz;
        float rotz = iz * qw + iw * -qz + ix * -qy - iy * -qx;

        v[offset] = rotx * scaleX + pivotX;
        v[offset + 1] = roty * scaleY + pivotY;
        v[offset + 2] = rotz * scaleZ + pivotZ;
    }

    private static float uvLockRotationAngle(Direction faceDir, Quaternionf blockQuat) {
        Vector3f normal = new Vector3f(FACE_NORMALS_F[faceDir.ordinal()]);
        normal.rotate(blockQuat);

        Vector3f localUp = new Vector3f(FACE_LOCAL_UP.get(faceDir));
        localUp.rotate(blockQuat);

        Vector3f worldUp = new Vector3f(0, 1, 0);
        float dot = worldUp.dot(normal);
        Vector3f projWorldUp = new Vector3f(
                worldUp.x - dot * normal.x,
                worldUp.y - dot * normal.y,
                worldUp.z - dot * normal.z
        );

        if (projWorldUp.lengthSquared() < 0.001) {
            Direction upDown = normal.y > 0 ? Direction.UP : Direction.DOWN;
            projWorldUp.set(FACE_LOCAL_UP.get(upDown));
        } else {
            projWorldUp.normalize();
        }

        dot = localUp.dot(projWorldUp);
        float cross = localUp.cross(projWorldUp).dot(normal);
        return (float) Math.atan2(cross, dot);
    }

    public static Direction getApproximateNearest(final float dx, final float dy, final float dz) {
        Direction result = null;
        float highestDot = Float.MIN_VALUE;
        for (Direction direction : Direction.values()) {
            float dot = dx * direction.getUnitVec3i().getX()
                    + dy * direction.getUnitVec3i().getY()
                    + dz * direction.getUnitVec3i().getZ();
            if (dot > highestDot) {
                highestDot = dot;
                result = direction;
            }
        }
        return result;
    }

    private void buildMesh(RPModel.View view, EnumSet<Direction> culledFaces) {
        Vector3fc blockRotRad = view.blockRotation();
        Quaternionf blockQuat = new Quaternionf().rotationYXZ(
                -(float) Math.toRadians(blockRotRad.y()),
                -(float) Math.toRadians(blockRotRad.x()),
                -(float) Math.toRadians(blockRotRad.z())
        );

        boolean blockRotated = blockRotRad.x() != 0 || blockRotRad.y() != 0;

        for (RPElement element : view.model().collectElements()) {
            processElement(element, view.offset(), blockQuat, blockRotated, view.uvlock(), culledFaces);
        }
    }

    private void processElement(RPElement element, Vector3fc viewOffset, Quaternionf blockQuat,
                                boolean blockRotated, boolean uvlock,
                                EnumSet<Direction> culledFaces) {
        float fx = element.from.x() / 16f + viewOffset.x();
        float fy = element.from.y() / 16f + viewOffset.y();
        float fz = element.from.z() / 16f + viewOffset.z();
        float tx = element.to.x() / 16f + viewOffset.x();
        float ty = element.to.y() / 16f + viewOffset.y();
        float tz = element.to.z() / 16f + viewOffset.z();

        boolean hasRot = element.rotation != null;
        Quaternionf elementQuat = hasRot ? element.rotation.toQuaternionf(tmpElementQuat) : null;
        float pivotX = hasRot ? element.rotation.getOrigin(tmpOrigin).x : 0f;
        float pivotY = hasRot ? tmpOrigin.y : 0f;
        float pivotZ = hasRot ? tmpOrigin.z : 0f;
        boolean rescale = hasRot && element.rotation.rescale;

        float scaleX = 1f, scaleY = 1f, scaleZ = 1f;
        if (rescale) {
            computePerAxisRescale(element, scaleRes);
            scaleX = scaleRes[0];
            scaleY = scaleRes[1];
            scaleZ = scaleRes[2];
        }

        float adjPivotX = pivotX + viewOffset.x();
        float adjPivotY = pivotY + viewOffset.y();
        float adjPivotZ = pivotZ + viewOffset.z();

        for (Map.Entry<Direction, RPElement.TextureInfo> entry : element.faces.entrySet()) {
            RPElement.TextureInfo info = entry.getValue();
            Direction faceDir = entry.getKey();

            if (info.cullface != null) {
                Vec3i cullDir = info.cullface.getUnitVec3i();
                tmpCullVec.set(cullDir.getX(), cullDir.getY(), cullDir.getZ());
                tmpCullVec.rotate(blockQuat);
                Direction fc = getApproximateNearest(tmpCullVec.x, tmpCullVec.y, tmpCullVec.z);
                if (fc != null && culledFaces.contains(fc)) continue;
            }

            Identifier texId = resolveTexture(info.texture);
            if (texId == null) continue;
            Texture texture = RPHelper.loadTexture(texId);
            if (texture == null) continue;

            translucent |= texture.hasTranslucency();

            processFace(
                    fx, fy, fz, tx, ty, tz,
                    faceDir, info,
                    elementQuat, adjPivotX, adjPivotY, adjPivotZ,
                    scaleX, scaleY, scaleZ,
                    blockQuat, blockRotated, uvlock,
                    element.shade, info.tintIndex,
                    texture
            );
        }
    }

    private void computePerAxisRescale(RPElement rot, float[] dest) {
        rot.rotation.toQuaternionf(tmpElementQuat).getEulerAnglesXYZ(tmpEuler);
        float pitch = tmpEuler.x;
        float yaw = tmpEuler.y;
        float roll = tmpEuler.z;

        float factorPitch = (float) (Math.sin(pitch) + Math.cos(pitch));
        float factorYaw = (float) (Math.sin(yaw) + Math.cos(yaw));
        float factorRoll = (float) (Math.sin(roll) + Math.cos(roll));

        dest[0] = factorYaw * factorRoll;
        dest[1] = factorPitch * factorRoll;
        dest[2] = factorPitch * factorYaw;
    }

    private void processFace(
            float fx, float fy, float fz, float tx, float ty, float tz,
            Direction faceDir, RPElement.TextureInfo info,
            Quaternionf elementQuat,
            float adjPivotX, float adjPivotY, float adjPivotZ,
            float scaleX, float scaleY, float scaleZ,
            Quaternionf blockQuat, boolean blockRotated, boolean uvlock,
            boolean shade, int tintIndex, Texture texture
    ) {
        // build vertices (element‑local)
        switch (faceDir) {
            case UP:
                faceVertices[0] = fx;
                faceVertices[1] = ty;
                faceVertices[2] = fz;
                faceVertices[3] = fx;
                faceVertices[4] = ty;
                faceVertices[5] = tz;
                faceVertices[6] = tx;
                faceVertices[7] = ty;
                faceVertices[8] = tz;
                faceVertices[9] = tx;
                faceVertices[10] = ty;
                faceVertices[11] = fz;
                break;
            case DOWN:
                faceVertices[0] = fx;
                faceVertices[1] = fy;
                faceVertices[2] = tz;
                faceVertices[3] = fx;
                faceVertices[4] = fy;
                faceVertices[5] = fz;
                faceVertices[6] = tx;
                faceVertices[7] = fy;
                faceVertices[8] = fz;
                faceVertices[9] = tx;
                faceVertices[10] = fy;
                faceVertices[11] = tz;
                break;
            case NORTH:
                faceVertices[0] = tx;
                faceVertices[1] = ty;
                faceVertices[2] = fz;
                faceVertices[3] = tx;
                faceVertices[4] = fy;
                faceVertices[5] = fz;
                faceVertices[6] = fx;
                faceVertices[7] = fy;
                faceVertices[8] = fz;
                faceVertices[9] = fx;
                faceVertices[10] = ty;
                faceVertices[11] = fz;
                break;
            case SOUTH:
                faceVertices[0] = fx;
                faceVertices[1] = ty;
                faceVertices[2] = tz;
                faceVertices[3] = fx;
                faceVertices[4] = fy;
                faceVertices[5] = tz;
                faceVertices[6] = tx;
                faceVertices[7] = fy;
                faceVertices[8] = tz;
                faceVertices[9] = tx;
                faceVertices[10] = ty;
                faceVertices[11] = tz;
                break;
            case WEST:
                faceVertices[0] = fx;
                faceVertices[1] = ty;
                faceVertices[2] = fz;
                faceVertices[3] = fx;
                faceVertices[4] = fy;
                faceVertices[5] = fz;
                faceVertices[6] = fx;
                faceVertices[7] = fy;
                faceVertices[8] = tz;
                faceVertices[9] = fx;
                faceVertices[10] = ty;
                faceVertices[11] = tz;
                break;
            case EAST:
                faceVertices[0] = tx;
                faceVertices[1] = ty;
                faceVertices[2] = tz;
                faceVertices[3] = tx;
                faceVertices[4] = fy;
                faceVertices[5] = tz;
                faceVertices[6] = tx;
                faceVertices[7] = fy;
                faceVertices[8] = fz;
                faceVertices[9] = tx;
                faceVertices[10] = ty;
                faceVertices[11] = fz;
                break;
            default:
                return;
        }

        // apply element rotation and rescale
        if (elementQuat != null) {
            for (int i = 0; i < 12; i += 3) {
                rotateVertex(faceVertices, i, elementQuat,
                        adjPivotX, adjPivotY, adjPivotZ,
                        scaleX, scaleY, scaleZ);
            }
        }

        // get uv rect unrotated
        float u0, v0, u1, v1;
        if (info.uv != null) {
            u0 = info.uv.x;
            v0 = info.uv.y;
            u1 = info.uv.z;
            v1 = info.uv.w;
        } else {
            getDefaultUV(faceDir, fx, fy, fz, tx, ty, tz, defaultUV);
            u0 = defaultUV[0];
            v0 = defaultUV[1];
            u1 = defaultUV[2];
            v1 = defaultUV[3];
        }

        // apply block rot
        for (int i = 0; i < 12; i += 3) {
            rotateVertex(faceVertices, i, blockQuat,
                    0.5f, 0.5f, 0.5f,
                    1f, 1f, 1f);
        }

        // setup uv corner before rotating
        uvCornersU[0] = u0;
        uvCornersV[0] = v0;
        uvCornersU[1] = u0;
        uvCornersV[1] = v1;
        uvCornersU[2] = u1;
        uvCornersV[2] = v1;
        uvCornersU[3] = u1;
        uvCornersV[3] = v0;

        // rotate around center (0.5,0.5)
        if (uvlock && blockRotated) {
            float angle = uvLockRotationAngle(faceDir, blockQuat);
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);
            for (int i = 0; i < 4; i++) {
                float nu = uvCornersU[i] / 16f;
                float nv = uvCornersV[i] / 16f;

                float du = nu - 0.5f;
                float dv = nv - 0.5f;
                float ru = 0.5f + du * cos - dv * sin;
                float rv = 0.5f + du * sin + dv * cos;

                uvCornersU[i] = ru * 16f;
                uvCornersV[i] = rv * 16f;
            }
        }

        int[] faceRotOrder = UV_ROT[(info.rotation / 90) % 4];
        float[] finalU = new float[4];
        float[] finalV = new float[4];
        for (int i = 0; i < 4; i++) {
            finalU[i] = uvCornersU[faceRotOrder[i]];
            finalV[i] = uvCornersV[faceRotOrder[i]];
        }

        System.arraycopy(finalU, 0, uvCornersU, 0, 4);
        System.arraycopy(finalV, 0, uvCornersV, 0, 4);

        float uScale = 1f / 16f, vScale = 1f / 16f;

        Vector3f faceNormal = new Vector3f(FACE_NORMALS_F[faceDir.ordinal()]);
        if (elementQuat != null) {
            faceNormal.rotate(elementQuat);
        }
        faceNormal.rotate(blockQuat);
        float nx = faceNormal.x, ny = faceNormal.y, nz = faceNormal.z;

        for (int i = 0; i < TRI_INDICES.length; i++) {
            int vi = TRI_INDICES[i];
            int off = vi * 3;
            emitVertex(
                    faceVertices[off], faceVertices[off + 1], faceVertices[off + 2],
                    nx, ny, nz,
                    uvCornersU[vi] * uScale,
                    uvCornersV[vi] * vScale,
                    texture, tintIndex, shade
            );
        }
    }

    private void emitVertex(float px, float py, float pz,
                            float nx, float ny, float nz,
                            float u, float v,
                            Texture texture, int tintIndex, boolean shade) {

        int existingIdx = -1;
        for (int i = 0; i < vertexCount; i++) {
            if (posList.getFloat(i * 3) == px &&
                    posList.getFloat(i * 3 + 1) == py &&
                    posList.getFloat(i * 3 + 2) == pz &&
                    normList.getFloat(i * 3) == nx &&
                    normList.getFloat(i * 3 + 1) == ny &&
                    normList.getFloat(i * 3 + 2) == nz &&
                    uvList.getFloat(i * 2) == u &&
                    uvList.getFloat(i * 2 + 1) == v &&
                    texList.get(i) == texture &&
                    tintList.getInt(i) == tintIndex &&
                    shadeList.getBoolean(i) == shade) {
                existingIdx = i;
                break;
            }
        }

        if (existingIdx == -1) {
            existingIdx = vertexCount++;
            posList.add(px);
            posList.add(py);
            posList.add(pz);
            normList.add(nx);
            normList.add(ny);
            normList.add(nz);
            uvList.add(u);
            uvList.add(v);
            texList.add(texture);
            tintList.add(tintIndex);
            shadeList.add(shade);
        }
        indexList.add(existingIdx);
    }

    private void getDefaultUV(Direction d, float fx, float fy, float fz,
                              float tx, float ty, float tz, float[] out) {
        float nx = fx + 0.5f, ny = fy + 0.5f, nz = fz + 0.5f;
        float mx = tx + 0.5f, my = ty + 0.5f, mz = tz + 0.5f;
        switch (d) {
            case DOWN:
            case UP:
                out[0] = nx;
                out[1] = nz;
                out[2] = mx;
                out[3] = mz;
                break;
            case NORTH:
            case SOUTH:
                out[0] = nx;
                out[1] = 1.0f - my;
                out[2] = mx;
                out[3] = 1.0f - ny;
                break;
            case WEST:
            case EAST:
                out[0] = nz;
                out[1] = 1.0f - my;
                out[2] = mz;
                out[3] = 1.0f - ny;
                break;
        }
    }

    private Identifier resolveTexture(String key) {
        if (key == null || key.isBlank()) return null;

        String current = key.startsWith("#") ? key.substring(1) : key;
        textureResolveVisited.clear();
        while (textureResolveVisited.add(current)) {
            Identifier resolved = textureMap.get(current);
            if (resolved == null) return CachedIdentifierDeserializer.get(current);
            String next = resolved.getPath();
            if (next.isBlank()) return resolved;
            if (!textureMap.containsKey(next)) return resolved;
            current = next;
        }
        return null;
    }

    public Mesh build() {
        if (indexList.isEmpty()) return null;
        return new Mesh(
                posList.toFloatArray(),
                normList.toFloatArray(),
                uvList.toFloatArray(),
                indexList.toIntArray(),
                tintList.toIntArray(),
                shadeList.toBooleanArray(),
                texList.toArray(new Texture[0]),
                ao, translucent
        );
    }
}
