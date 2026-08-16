package dev.engine_room.flywheel.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.api.backend.Backend;
import dev.engine_room.flywheel.api.backend.Engine;
import dev.engine_room.flywheel.api.internal.FlwApiLink;
import dev.engine_room.flywheel.api.layout.ElementType;
import dev.engine_room.flywheel.api.layout.FloatRepr;
import dev.engine_room.flywheel.api.layout.Layout;
import dev.engine_room.flywheel.api.layout.LayoutBuilder;
import dev.engine_room.flywheel.api.layout.ValueRepr;
import dev.engine_room.flywheel.api.registry.IdRegistry;
import dev.engine_room.flywheel.api.visualization.BlockEntityVisualizer;
import dev.engine_room.flywheel.api.visualization.EntityVisualizer;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Dedicated-server stand-in for Flywheel's API link: no visualization anywhere, registries are plain maps. */
public class FlwApiLinkImpl implements FlwApiLink {

	private final Map<Object, Object> visualizers = new HashMap<>();
	private final Backend offBackend = new Backend() {
		@Override
		public Engine createEngine(LevelAccessor level) {
			throw new UnsupportedOperationException("Flywheel engines are client-only");
		}

		@Override
		public int priority() {
			return Integer.MIN_VALUE;
		}

		@Override
		public boolean isSupported() {
			return true;
		}
	};

	@Override
	public <T> IdRegistry<T> createIdRegistry() {
		return new SimpleIdRegistry<>();
	}

	@Override
	public Backend getCurrentBackend() {
		return offBackend;
	}

	@Override
	public boolean isBackendOn() {
		return false;
	}

	@Override
	public Backend getOffBackend() {
		return offBackend;
	}

	@Override
	public Backend getDefaultBackend() {
		return offBackend;
	}

	@Override
	public LayoutBuilder createLayoutBuilder() {
		return new SimpleLayoutBuilder();
	}

	@Override
	public boolean supportsVisualization(@Nullable LevelAccessor level) {
		return false;
	}

	@Override
	@Nullable
	public VisualizationManager getVisualizationManager(@Nullable LevelAccessor level) {
		return null;
	}

	@Override
	public VisualizationManager getVisualizationManagerOrThrow(@Nullable LevelAccessor level) {
		throw new IllegalStateException("No visualization manager on the server");
	}

	@SuppressWarnings("unchecked")
	@Override
	@Nullable
	public <T extends BlockEntity> BlockEntityVisualizer<? super T> getVisualizer(BlockEntityType<T> type) {
		return (BlockEntityVisualizer<? super T>) visualizers.get(type);
	}

	@SuppressWarnings("unchecked")
	@Override
	@Nullable
	public <T extends Entity> EntityVisualizer<? super T> getVisualizer(EntityType<T> type) {
		return (EntityVisualizer<? super T>) visualizers.get(type);
	}

	@Override
	public <T extends BlockEntity> void setVisualizer(BlockEntityType<T> type, BlockEntityVisualizer<? super T> visualizer) {
		visualizers.put(type, visualizer);
	}

	@Override
	public <T extends Entity> void setVisualizer(EntityType<T> type, EntityVisualizer<? super T> visualizer) {
		visualizers.put(type, visualizer);
	}

	static final class SimpleIdRegistry<T> implements IdRegistry<T> {
		private final Map<ResourceLocation, T> byId = new LinkedHashMap<>();
		private final Map<T, ResourceLocation> ids = new HashMap<>();

		@Override
		public void register(ResourceLocation id, T object) {
			byId.put(id, object);
			ids.put(object, id);
		}

		@Override
		public <S extends T> S registerAndGet(ResourceLocation id, S object) {
			register(id, object);
			return object;
		}

		@Override
		@Nullable
		public T get(ResourceLocation id) {
			return byId.get(id);
		}

		@Override
		@Nullable
		public ResourceLocation getId(T object) {
			return ids.get(object);
		}

		@Override
		public T getOrThrow(ResourceLocation id) {
			T t = byId.get(id);
			if (t == null)
				throw new IllegalArgumentException("No object registered for id " + id);
			return t;
		}

		@Override
		public ResourceLocation getIdOrThrow(T object) {
			ResourceLocation id = ids.get(object);
			if (id == null)
				throw new IllegalArgumentException("Object not registered");
			return id;
		}

		@Override
		public Set<ResourceLocation> getAllIds() {
			return Collections.unmodifiableSet(byId.keySet());
		}

		@Override
		public Collection<T> getAll() {
			return Collections.unmodifiableCollection(byId.values());
		}

		@Override
		public boolean isFrozen() {
			return false;
		}

		@Override
		public Iterator<T> iterator() {
			return getAll().iterator();
		}
	}

	/** Enough of a layout to satisfy static initialisers of instance types; byte layout is approximate. */
	static final class SimpleLayoutBuilder implements LayoutBuilder {
		private final List<Layout.Element> elements = new ArrayList<>();
		private int offset;

		private LayoutBuilder add(String name, int size, int alignment) {
			int align = Math.max(1, alignment);
			offset = (offset + align - 1) / align * align;
			int start = offset;
			// ElementType is sealed; ScalarElementType is one of its permitted (non-sealed) sub-interfaces
			ElementType type = new dev.engine_room.flywheel.api.layout.ScalarElementType() {
				@Override
				public ValueRepr repr() {
					return FloatRepr.FLOAT;
				}

				@Override
				public int byteSize() {
					return size;
				}

				@Override
				public int byteAlignment() {
					return align;
				}
			};
			elements.add(new Layout.Element() {
				@Override
				public String name() {
					return name;
				}

				@Override
				public ElementType type() {
					return type;
				}

				@Override
				public int byteOffset() {
					return start;
				}

				@Override
				public int paddedByteSize() {
					return size;
				}

				@Override
				public int paddingByteSize() {
					return 0;
				}
			});
			offset += size;
			return this;
		}

		@Override
		public LayoutBuilder scalar(String name, ValueRepr repr) {
			return add(name, repr.byteSize(), repr.byteSize());
		}

		@Override
		public LayoutBuilder vector(String name, ValueRepr repr, int size) {
			return add(name, repr.byteSize() * size, repr.byteSize());
		}

		@Override
		public LayoutBuilder matrix(String name, FloatRepr repr, int rows, int columns) {
			return add(name, repr.byteSize() * rows * columns, repr.byteSize());
		}

		@Override
		public LayoutBuilder matrix(String name, FloatRepr repr, int size) {
			return matrix(name, repr, size, size);
		}

		@Override
		public LayoutBuilder scalarArray(String name, ValueRepr repr, int length) {
			return add(name, repr.byteSize() * length, repr.byteSize());
		}

		@Override
		public LayoutBuilder vectorArray(String name, ValueRepr repr, int size, int length) {
			return add(name, repr.byteSize() * size * length, repr.byteSize());
		}

		@Override
		public LayoutBuilder matrixArray(String name, FloatRepr repr, int rows, int columns, int length) {
			return add(name, repr.byteSize() * rows * columns * length, repr.byteSize());
		}

		@Override
		public LayoutBuilder matrixArray(String name, FloatRepr repr, int size, int length) {
			return matrixArray(name, repr, size, size, length);
		}

		@Override
		public Layout build() {
			List<Layout.Element> list = List.copyOf(elements);
			Map<String, Layout.Element> map = new LinkedHashMap<>();
			list.forEach(e -> map.put(e.name(), e));
			int size = offset;
			return new Layout() {
				@Override
				public List<Layout.Element> elements() {
					return list;
				}

				@Override
				public Map<String, Layout.Element> asMap() {
					return map;
				}

				@Override
				public int byteSize() {
					return size;
				}

				@Override
				public int byteAlignment() {
					return 4;
				}
			};
		}
	}
}
