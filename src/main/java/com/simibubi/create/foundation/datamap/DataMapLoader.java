package com.simibubi.create.foundation.datamap;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.simibubi.create.Create;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

/**
 * Loads every registered {@link DataMapType} from {@code data/<ns>/data_maps/<registry>/<id>.json}, same
 * layout and JSON shape as NeoForge ({@code values} keyed by entry id or {@code #tag}, optional
 * {@code replace}/{@code remove}), so upstream's generated files work unchanged. Registered as a
 * server-data reload listener; synced to clients by {@link DataMapSyncPayload}.
 */
public final class DataMapLoader extends SimplePreparableReloadListener<Map<DataMapType<?, ?>, List<JsonObject>>> implements IdentifiableResourceReloadListener {

	public static final ResourceLocation ID = Create.asResource("data_maps");

	public static void register() {
		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new DataMapLoader());
	}

	@Override
	public ResourceLocation getFabricId() {
		return ID;
	}

	@Override
	protected Map<DataMapType<?, ?>, List<JsonObject>> prepare(ResourceManager manager, ProfilerFiller profiler) {
		Map<DataMapType<?, ?>, List<JsonObject>> result = new IdentityHashMap<>();
		for (DataMapType<?, ?> type : DataMaps.types()) {
			ResourceLocation regKey = type.registryKey().location();
			String folder = "data_maps/" + (regKey.getNamespace().equals("minecraft") ? regKey.getPath() : regKey.getNamespace() + "/" + regKey.getPath());
			ResourceLocation file = ResourceLocation.fromNamespaceAndPath(type.id().getNamespace(), folder + "/" + type.id().getPath() + ".json");
			List<Resource> stack = manager.getResourceStack(file);
			List<JsonObject> jsons = new java.util.ArrayList<>();
			for (Resource resource : stack) {
				try (var reader = resource.openAsReader()) {
					jsons.add(GsonHelper.parse(reader));
				} catch (Exception e) {
					Create.LOGGER.error("Failed to read data map {} from {}", type, resource.sourcePackId(), e);
				}
			}
			result.put(type, jsons);
		}
		return result;
	}

	@Override
	protected void apply(Map<DataMapType<?, ?>, List<JsonObject>> prepared, ResourceManager manager, ProfilerFiller profiler) {
		prepared.forEach((type, jsons) -> applyType(type, jsons));
	}

	@SuppressWarnings("unchecked")
	private static <R, T> void applyType(DataMapType<R, T> type, List<JsonObject> jsons) {
		Registry<R> registry = DataMaps.registryOf(type);
		if (registry == null) {
			Create.LOGGER.warn("Data map {} targets unknown registry {}", type, type.registryKey().location());
			return;
		}
		Map<R, T> values = new IdentityHashMap<>();
		for (JsonObject json : jsons) {
			if (GsonHelper.getAsBoolean(json, "replace", false))
				values.clear();
			if (json.has("remove")) {
				for (JsonElement el : GsonHelper.getAsJsonArray(json, "remove"))
					resolve(registry, el.getAsString()).forEach(values::remove);
			}
			JsonObject entries = GsonHelper.getAsJsonObject(json, "values", new JsonObject());
			for (Map.Entry<String, JsonElement> entry : entries.entrySet()) {
				Optional<T> value = type.codec().parse(JsonOps.INSTANCE, entry.getValue()).resultOrPartial(err ->
					Create.LOGGER.error("Invalid data map value for {} in {}: {}", entry.getKey(), type, err));
				if (value.isEmpty())
					continue;
				resolve(registry, entry.getKey()).forEach(r -> values.put(r, value.get()));
			}
		}
		DataMaps.set(type, values);
	}

	private static <R> List<R> resolve(Registry<R> registry, String key) {
		if (key.startsWith("#")) {
			TagKey<R> tag = TagKey.create(registry.key(), ResourceLocation.parse(key.substring(1)));
			return registry.getTag(tag).map(set -> set.stream().map(Holder::value).toList()).orElse(List.of());
		}
		return registry.getOptional(ResourceLocation.parse(key)).map(List::of).orElse(List.of());
	}
}
