package com.simibubi.create.foundation.data.modelgen;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;

/** Shell — see package docs. Fluent model JSON builder; every method is a no-op returning the builder. */
public class ModelBuilder<T extends ModelBuilder<T>> extends ModelFile {

	protected final ExistingFileHelper existingFileHelper;

	protected ModelBuilder(ResourceLocation outputLocation, ExistingFileHelper existingFileHelper) {
		super(outputLocation);
		this.existingFileHelper = existingFileHelper;
	}

	@SuppressWarnings("unchecked")
	protected T self() {
		return (T) this;
	}

	@Override
	protected boolean exists() {
		return true;
	}

	public T parent(ModelFile parent) {
		return self();
	}

	public T texture(String key, String texture) {
		return self();
	}

	public T texture(String key, ResourceLocation texture) {
		return self();
	}

	public T renderType(String renderType) {
		return self();
	}

	public T renderType(ResourceLocation renderType) {
		return self();
	}

	public T ao(boolean ao) {
		return self();
	}

	public T guiLight(GuiLight light) {
		return self();
	}

	public TransformsBuilder transforms() {
		return new TransformsBuilder();
	}

	public ElementBuilder element() {
		return new ElementBuilder();
	}

	public ElementBuilder element(int index) {
		return new ElementBuilder();
	}

	public int getElementCount() {
		return 0;
	}

	public <L> L customLoader(java.util.function.BiFunction<T, ExistingFileHelper, L> customLoaderFactory) {
		return customLoaderFactory.apply(self(), existingFileHelper);
	}

	public enum GuiLight {
		FRONT, SIDE
	}

	public enum FaceRotation {
		ZERO, CLOCKWISE_90, UPSIDE_DOWN, COUNTERCLOCKWISE_90
	}

	public class ElementBuilder {
		public ElementBuilder from(float x, float y, float z) {
			return this;
		}

		public ElementBuilder to(float x, float y, float z) {
			return this;
		}

		public FaceBuilder face(Direction dir) {
			return new FaceBuilder();
		}

		public RotationBuilder rotation() {
			return new RotationBuilder();
		}

		public ElementBuilder shade(boolean shade) {
			return this;
		}

		public ElementBuilder allFaces(java.util.function.BiConsumer<Direction, FaceBuilder> action) {
			for (Direction d : Direction.values())
				action.accept(d, new FaceBuilder());
			return this;
		}

		public ElementBuilder faces(java.util.function.BiConsumer<Direction, FaceBuilder> action) {
			return allFaces(action);
		}

		public ElementBuilder textureAll(String texture) {
			return this;
		}

		public ElementBuilder texture(String texture) {
			return this;
		}

		public ElementBuilder cube(String texture) {
			return this;
		}

		public ElementBuilder emissivity(int blockLight, int skyLight) {
			return this;
		}

		public ElementBuilder color(int color) {
			return this;
		}

		public ElementBuilder ao(boolean ao) {
			return this;
		}

		public T end() {
			return self();
		}

		public class FaceBuilder {
			public FaceBuilder cullface(Direction dir) {
				return this;
			}

			public FaceBuilder tintindex(int index) {
				return this;
			}

			public FaceBuilder texture(String texture) {
				return this;
			}

			public FaceBuilder uvs(float u1, float v1, float u2, float v2) {
				return this;
			}

			public FaceBuilder rotation(FaceRotation rot) {
				return this;
			}

			public FaceBuilder emissivity(int blockLight, int skyLight) {
				return this;
			}

			public FaceBuilder color(int color) {
				return this;
			}

			public FaceBuilder ao(boolean ao) {
				return this;
			}

			public ElementBuilder end() {
				return ElementBuilder.this;
			}
		}

		public class RotationBuilder {
			public RotationBuilder origin(float x, float y, float z) {
				return this;
			}

			public RotationBuilder axis(Direction.Axis axis) {
				return this;
			}

			public RotationBuilder angle(float angle) {
				return this;
			}

			public RotationBuilder rescale(boolean rescale) {
				return this;
			}

			public ElementBuilder end() {
				return ElementBuilder.this;
			}
		}
	}

	public class TransformsBuilder {
		public TransformVecBuilder transform(ItemDisplayContext type) {
			return new TransformVecBuilder();
		}

		public T end() {
			return self();
		}

		public class TransformVecBuilder {
			public TransformVecBuilder rotation(float x, float y, float z) {
				return this;
			}

			public TransformVecBuilder leftRotation(float x, float y, float z) {
				return this;
			}

			public TransformVecBuilder rightRotation(float x, float y, float z) {
				return this;
			}

			public TransformVecBuilder translation(float x, float y, float z) {
				return this;
			}

			public TransformVecBuilder scale(float sc) {
				return this;
			}

			public TransformVecBuilder scale(float x, float y, float z) {
				return this;
			}

			public TransformsBuilder end() {
				return TransformsBuilder.this;
			}
		}
	}
}
