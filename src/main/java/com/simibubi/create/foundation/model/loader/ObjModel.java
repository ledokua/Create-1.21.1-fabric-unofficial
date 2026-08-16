package com.simibubi.create.foundation.model.loader;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.google.gson.JsonObject;
import com.simibubi.create.Create;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;

/**
 * NeoForge's {@code neoforge:obj} model loader, Fabric-side. Supports what Create's models use: {@code v/vt/vn}, n-gon
 * faces (split into quads), {@code usemtl}/{@code mtllib} with {@code map_Kd #textureRef} materials, {@code Ka}
 * (ambient → full-bright, unshaded quads, as NeoForge's {@code emissive_ambient}), {@code flip_v}, automatic culling of
 * quads lying on a block face, and blockstate rotations through the {@link ModelState}. Options honoured from the JSON:
 * {@code model}, {@code flip_v}, {@code automatic_culling}, {@code shade_quads}, {@code emissive_ambient}, {@code render_type}.
 */
@Environment(EnvType.CLIENT)
public final class ObjModel {

	private ObjModel() {}

	/** Parsed OBJ file (positions/uvs/normals + faces grouped by material). Cached per resource location. */
	static final class Geometry {
		final List<Vector3f> positions = new ArrayList<>();
		final List<float[]> texCoords = new ArrayList<>();
		final List<Vector3f> normals = new ArrayList<>();
		/** material name → faces; each face = vertex index triples [v, vt, vn] (-1 when absent). */
		final Map<String, List<int[][]>> facesByMaterial = new LinkedHashMap<>();
		final Map<String, MtlMaterial> materials = new HashMap<>();
	}

	static final class MtlMaterial {
		@Nullable
		String diffuseColorMap; // map_Kd
		float[] ambientColor = { 0, 0, 0 }; // Ka
		float[] diffuseColor = { 1, 1, 1, 1 }; // Kd
	}

	private static final Map<ResourceLocation, Geometry> CACHE = new HashMap<>();

	public static void clearCache() {
		CACHE.clear();
	}

	static Geometry load(ResourceLocation objLocation) {
		return CACHE.computeIfAbsent(objLocation, ObjModel::parse);
	}

	private static Geometry parse(ResourceLocation objLocation) {
		Geometry geometry = new Geometry();
		ResourceManager manager = Minecraft.getInstance().getResourceManager();
		String currentMaterial = "";
		try (BufferedReader reader = manager.openAsReader(objLocation)) {
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty() || line.charAt(0) == '#')
					continue;
				String[] parts = line.split("\\s+");
				switch (parts[0]) {
					case "v" -> geometry.positions.add(new Vector3f(Float.parseFloat(parts[1]), Float.parseFloat(parts[2]), Float.parseFloat(parts[3])));
					case "vt" -> geometry.texCoords.add(new float[] { Float.parseFloat(parts[1]), parts.length > 2 ? Float.parseFloat(parts[2]) : 0 });
					case "vn" -> geometry.normals.add(new Vector3f(Float.parseFloat(parts[1]), Float.parseFloat(parts[2]), Float.parseFloat(parts[3])).normalize());
					case "usemtl" -> currentMaterial = parts.length > 1 ? parts[1] : "";
					case "mtllib" -> {
						String path = objLocation.getPath();
						String dir = path.substring(0, path.lastIndexOf('/') + 1);
						loadMtl(manager, ResourceLocation.fromNamespaceAndPath(objLocation.getNamespace(), dir + parts[1]), geometry);
					}
					case "f" -> {
						int[][] face = new int[parts.length - 1][];
						for (int i = 1; i < parts.length; i++) {
							String[] idx = parts[i].split("/", -1);
							int[] triple = { -1, -1, -1 };
							for (int j = 0; j < Math.min(3, idx.length); j++) {
								if (idx[j].isEmpty())
									continue;
								int index = Integer.parseInt(idx[j]);
								int size = j == 0 ? geometry.positions.size() : j == 1 ? geometry.texCoords.size() : geometry.normals.size();
								triple[j] = index < 0 ? size + index : index - 1;
							}
							face[i - 1] = triple;
						}
						geometry.facesByMaterial.computeIfAbsent(currentMaterial, $ -> new ArrayList<>()).add(face);
					}
					default -> {} // o, g, s, l, ... ignored
				}
			}
		} catch (IOException e) {
			Create.LOGGER.error("Could not read OBJ model {}", objLocation, e);
		}
		return geometry;
	}

	private static void loadMtl(ResourceManager manager, ResourceLocation mtlLocation, Geometry geometry) {
		MtlMaterial current = null;
		try (BufferedReader reader = manager.openAsReader(mtlLocation)) {
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty() || line.charAt(0) == '#')
					continue;
				String[] parts = line.split("\\s+");
				switch (parts[0]) {
					case "newmtl" -> geometry.materials.put(parts.length > 1 ? parts[1] : "", current = new MtlMaterial());
					case "map_Kd" -> { if (current != null && parts.length > 1) current.diffuseColorMap = parts[parts.length - 1]; }
					case "Ka" -> { if (current != null && parts.length > 3) current.ambientColor = new float[] { Float.parseFloat(parts[1]), Float.parseFloat(parts[2]), Float.parseFloat(parts[3]) }; }
					case "Kd" -> { if (current != null && parts.length > 3) current.diffuseColor = new float[] { Float.parseFloat(parts[1]), Float.parseFloat(parts[2]), Float.parseFloat(parts[3]), 1 }; }
					default -> {}
				}
			}
		} catch (IOException e) {
			Create.LOGGER.warn("Could not read MTL {} (materials default to particle texture)", mtlLocation);
		}
	}

	// ---- unbaked ----

	@Environment(EnvType.CLIENT)
	public static final class Unbaked extends LoaderBlockModel {
		private final ResourceLocation objLocation;
		private final boolean flipV;
		private final boolean automaticCulling;
		private final boolean shadeQuads;
		private final boolean emissiveAmbient;

		public Unbaked(ResourceLocation id, JsonObject json) {
			super(id, json);
			this.objLocation = ResourceLocation.parse(GsonHelper.getAsString(json, "model"));
			this.flipV = GsonHelper.getAsBoolean(json, "flip_v", false);
			this.automaticCulling = GsonHelper.getAsBoolean(json, "automatic_culling", true);
			this.shadeQuads = GsonHelper.getAsBoolean(json, "shade_quads", true);
			this.emissiveAmbient = GsonHelper.getAsBoolean(json, "emissive_ambient", true);
		}

		@Override
		public boolean hasCustomGeometry() {
			return true;
		}

		@Override
		public BakedModel bakeGeometry(BlockModel owner, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState state) {
			Geometry geometry = load(objLocation);
			LoaderBakedModel.Bucket bucket = new LoaderBakedModel.Bucket();
			TextureAtlasSprite particle = particle(owner, spriteGetter);

			Matrix4f positionMatrix = null;
			Matrix3f normalMatrix = null;
			if (!state.getRotation().equals(com.mojang.math.Transformation.identity())) {
				positionMatrix = new Matrix4f().translate(0.5f, 0.5f, 0.5f).mul(state.getRotation().getMatrix()).translate(-0.5f, -0.5f, -0.5f);
				normalMatrix = new Matrix3f(state.getRotation().getMatrix()).invert().transpose();
			}

			for (Map.Entry<String, List<int[][]>> entry : geometry.facesByMaterial.entrySet()) {
				MtlMaterial material = geometry.materials.getOrDefault(entry.getKey(), new MtlMaterial());
				TextureAtlasSprite sprite = material.diffuseColorMap == null ? particle
					: material.diffuseColorMap.startsWith("#") ? sprite(owner, spriteGetter, material.diffuseColorMap)
					: spriteGetter.apply(new Material(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS, ResourceLocation.parse(material.diffuseColorMap)));
				int fakeLight = emissiveAmbient ? (int) ((material.ambientColor[0] + material.ambientColor[1] + material.ambientColor[2]) * 15 / 3.0f) : 0;
				int lightmap = LightTexture.pack(fakeLight, fakeLight);
				boolean shade = fakeLight == 0 && shadeQuads;
				int color = packColor(material.diffuseColor);

				for (int[][] face : entry.getValue()) {
					if (face.length < 3)
						continue;
					if (face.length <= 4) {
						emit(geometry, face, sprite, lightmap, shade, color, positionMatrix, normalMatrix, bucket);
					} else {
						// triangle-fan the n-gon into quads: (0,1,2,3), (0,3,4,5), ...
						for (int i = 1; i + 1 < face.length; i += 2) {
							int[][] quad = i + 2 < face.length
								? new int[][] { face[0], face[i], face[i + 1], face[i + 2] }
								: new int[][] { face[0], face[i], face[i + 1] };
							emit(geometry, quad, sprite, lightmap, shade, color, positionMatrix, normalMatrix, bucket);
						}
					}
				}
			}

			Map<RenderType, LoaderBakedModel.Bucket> buckets = new HashMap<>();
			buckets.put(renderType, bucket); // null key = the block's own layer
			return new LoaderBakedModel(buckets, owner.hasAmbientOcclusion(), true, owner.getGuiLight().lightLikeBlock(), particle, owner.getTransforms(), overrides(owner, baker));
		}

		private void emit(Geometry geometry, int[][] indices, TextureAtlasSprite sprite, int lightmap, boolean shade, int color,
			@Nullable Matrix4f positionMatrix, @Nullable Matrix3f normalMatrix, LoaderBakedModel.Bucket bucket) {
			Vector3f faceNormal = null;
			boolean needsNormal = geometry.normals.isEmpty();
			for (int[] index : indices)
				needsNormal |= index[2] < 0;
			if (needsNormal) {
				Vector3f a = geometry.positions.get(indices[0][0]);
				Vector3f b = geometry.positions.get(indices[1][0]);
				Vector3f c = geometry.positions.get(indices[2][0]);
				faceNormal = new Vector3f(b).sub(a).cross(new Vector3f(c).sub(a)).normalize();
				if (!faceNormal.isFinite())
					faceNormal = new Vector3f(0, 1, 0);
			}

			int[] data = new int[32];
			Vector3f[] positions = new Vector3f[4];
			Vector3f normalSum = new Vector3f();
			for (int i = 0; i < 4; i++) {
				int[] index = indices[Math.min(i, indices.length - 1)];
				Vector3f pos = new Vector3f(geometry.positions.get(index[0]));
				float[] uv = index[1] >= 0 && index[1] < geometry.texCoords.size() ? geometry.texCoords.get(index[1]) : DEFAULT_UVS[i];
				Vector3f normal = new Vector3f(!needsNormal && index[2] >= 0 ? geometry.normals.get(index[2]) : faceNormal);
				if (positionMatrix != null) {
					Vector4f p = new Vector4f(pos, 1).mul(positionMatrix);
					pos.set(p.x, p.y, p.z);
					normal.mul(normalMatrix).normalize();
				}
				positions[i] = pos;
				normalSum.add(normal);
				float u = uv[0];
				float v = flipV ? 1 - uv[1] : uv[1];
				int o = i * 8;
				data[o] = Float.floatToRawIntBits(pos.x);
				data[o + 1] = Float.floatToRawIntBits(pos.y);
				data[o + 2] = Float.floatToRawIntBits(pos.z);
				data[o + 3] = color;
				data[o + 4] = Float.floatToRawIntBits(sprite.getU(u));
				data[o + 5] = Float.floatToRawIntBits(sprite.getV(v));
				data[o + 6] = lightmap;
				data[o + 7] = ((int) (normal.x * 127) & 0xFF) | (((int) (normal.y * 127) & 0xFF) << 8) | (((int) (normal.z * 127) & 0xFF) << 16);
			}
			Direction direction = Direction.getNearest(normalSum.x, normalSum.y, normalSum.z);
			Direction cull = automaticCulling ? cullFace(positions) : null;
			bucket.add(cull, new BakedQuad(data, -1, direction, sprite, shade));
		}

		private static final float[][] DEFAULT_UVS = { { 0, 0 }, { 0, 1 }, { 1, 1 }, { 1, 0 } };

		@Nullable
		private static Direction cullFace(Vector3f[] p) {
			if (all(p, v -> Mth.equal(v.x, 0))) return Direction.WEST;
			if (all(p, v -> Mth.equal(v.x, 1))) return Direction.EAST;
			if (all(p, v -> Mth.equal(v.y, 0))) return Direction.DOWN;
			if (all(p, v -> Mth.equal(v.y, 1))) return Direction.UP;
			if (all(p, v -> Mth.equal(v.z, 0))) return Direction.NORTH;
			if (all(p, v -> Mth.equal(v.z, 1))) return Direction.SOUTH;
			return null;
		}

		private static boolean all(Vector3f[] p, java.util.function.Predicate<Vector3f> test) {
			for (Vector3f v : p)
				if (!test.test(v))
					return false;
			return true;
		}

		private static int packColor(float[] rgba) {
			int r = (int) (rgba[0] * 255) & 0xFF, g = (int) (rgba[1] * 255) & 0xFF, b = (int) (rgba[2] * 255) & 0xFF, a = (int) (rgba[3] * 255) & 0xFF;
			return (a << 24) | (b << 16) | (g << 8) | r; // vanilla BLOCK format: RGBA bytes, little-endian
		}
	}
}
