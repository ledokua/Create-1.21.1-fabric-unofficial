package com.simibubi.create.foundation.model.loader;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.simibubi.create.Create;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;

/**
 * NeoForge custom model loaders that Create's (vendored, upstream-generated) model JSONs use — {@code neoforge:obj}
 * (Wavefront OBJ geometry: trains, bogeys, water wheels, flywheel, chain conveyor, blaze burner, crushing wheel, valve
 * handle) and {@code neoforge:composite} (several sub-models with their own render types) — reimplemented on Fabric's
 * model-loading API. Model JSONs are scanned once per reload for a top-level {@code "loader"} key; matching ids are
 * resolved to our {@link UnbakedModel}s before vanilla gets to parse them.
 */
@Environment(EnvType.CLIENT)
public final class CreateModelLoaders {

	private CreateModelLoaders() {}

	public static final String OBJ = "neoforge:obj";
	public static final String COMPOSITE = "neoforge:composite";

	public static void register() {
		PreparableModelLoadingPlugin.register(CreateModelLoaders::scan, (loaderModels, pluginContext) -> {
			pluginContext.resolveModel().register(context -> {
				JsonObject json = loaderModels.get(context.id());
				if (json == null)
					return null;
				String loader = GsonHelper.getAsString(json, "loader");
				try {
					return switch (loader) {
						case OBJ -> new ObjModel.Unbaked(context.id(), json);
						case COMPOSITE -> new CompositeModel.Unbaked(context.id(), json);
						default -> null;
					};
				} catch (RuntimeException e) {
					Create.LOGGER.error("Failed to load {} model {}", loader, context.id(), e);
					return null;
				}
			});
		});
	}

	/** Async prep: model id → JSON for every model json in the {@code create} namespace that declares a custom loader. */
	private static CompletableFuture<Map<ResourceLocation, JsonObject>> scan(ResourceManager manager, Executor executor) {
		ObjModel.clearCache();
		return CompletableFuture.supplyAsync(() -> {
			Map<ResourceLocation, JsonObject> result = new HashMap<>();
			Map<ResourceLocation, Resource> resources = manager.listResources("models", path -> path.getNamespace().equals(Create.ID) && path.getPath().endsWith(".json"));
			for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
				try (Reader reader = entry.getValue().openAsReader()) {
					JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
					if (!json.has("loader"))
						continue;
					String loader = json.get("loader").isJsonObject() ? GsonHelper.getAsString(json.getAsJsonObject("loader"), "type") : GsonHelper.getAsString(json, "loader");
					if (!loader.equals(OBJ) && !loader.equals(COMPOSITE)) {
						Create.LOGGER.warn("Model {} uses unsupported loader {}", entry.getKey(), loader);
						continue;
					}
					json.addProperty("loader", loader);
					String path = entry.getKey().getPath(); // models/<id>.json
					ResourceLocation id = ResourceLocation.fromNamespaceAndPath(entry.getKey().getNamespace(), path.substring("models/".length(), path.length() - ".json".length()));
					result.put(id, json);
				} catch (Exception e) {
					Create.LOGGER.error("Failed to pre-read model {}", entry.getKey(), e);
				}
			}
			Create.LOGGER.info("Found {} custom-loader models", result.size());
			return result;
		}, executor);
	}

	/** NeoForge's named render types ("minecraft:cutout" etc.), for {@code render_type} in model JSON. */
	@Nullable
	public static RenderType renderType(@Nullable String name) {
		if (name == null)
			return null;
		String path = name.contains(":") ? name.substring(name.indexOf(':') + 1) : name;
		return switch (path) {
			case "solid" -> RenderType.solid();
			case "cutout" -> RenderType.cutout();
			case "cutout_mipped", "cutout_mipped_all" -> RenderType.cutoutMipped();
			case "translucent" -> RenderType.translucent();
			case "tripwire" -> RenderType.tripwire();
			default -> {
				Create.LOGGER.warn("Unknown model render_type {}", name);
				yield null;
			}
		};
	}
}
