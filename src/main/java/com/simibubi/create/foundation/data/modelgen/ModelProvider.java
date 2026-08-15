package com.simibubi.create.foundation.data.modelgen;

import java.util.function.BiFunction;
import java.util.function.Function;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

/**
 * Shell — see package docs. Base of the block/item model providers; the many texture-preset factory methods
 * upstream has all just return a fresh builder here.
 */
public abstract class ModelProvider<T extends ModelBuilder<T>> {

	public static final String BLOCK_FOLDER = "block";
	public static final String ITEM_FOLDER = "item";

	protected final String modid;
	protected final String folder;
	protected final Function<ResourceLocation, T> factory;
	protected final ExistingFileHelper existingFileHelper;

	protected ModelProvider(String modid, String folder, Function<ResourceLocation, T> factory, ExistingFileHelper existingFileHelper) {
		this.modid = modid;
		this.folder = folder;
		this.factory = factory;
		this.existingFileHelper = existingFileHelper;
	}

	protected ModelProvider(String modid, String folder, BiFunction<ResourceLocation, ExistingFileHelper, T> builderFromModId, ExistingFileHelper existingFileHelper) {
		this(modid, folder, loc -> builderFromModId.apply(loc, existingFileHelper), existingFileHelper);
	}

	public T getBuilder(String path) {
		ResourceLocation loc = ResourceLocation.parse(path);
		if (loc.getNamespace().equals("minecraft") && !path.contains(":"))
			loc = ResourceLocation.fromNamespaceAndPath(modid, path);
		return factory.apply(extendWithFolder(loc));
	}

	private ResourceLocation extendWithFolder(ResourceLocation rl) {
		if (rl.getPath().contains("/"))
			return rl;
		return ResourceLocation.fromNamespaceAndPath(rl.getNamespace(), folder + "/" + rl.getPath());
	}

	public ResourceLocation modLoc(String name) {
		return ResourceLocation.fromNamespaceAndPath(modid, name);
	}

	public ResourceLocation mcLoc(String name) {
		return ResourceLocation.withDefaultNamespace(name);
	}

	public ModelFile.ExistingModelFile getExistingFile(ResourceLocation path) {
		return new ModelFile.ExistingModelFile(extendWithFolder(path), existingFileHelper);
	}

	public T withExistingParent(String name, String parent) {
		return withExistingParent(name, mcLoc(parent));
	}

	public T withExistingParent(String name, ResourceLocation parent) {
		return getBuilder(name).parent(getExistingFile(parent));
	}

	public T cube(String name, ResourceLocation down, ResourceLocation up, ResourceLocation north, ResourceLocation south, ResourceLocation east, ResourceLocation west) {
		return getBuilder(name);
	}

	public T singleTexture(String name, ResourceLocation parent, ResourceLocation texture) {
		return getBuilder(name);
	}

	public T singleTexture(String name, ResourceLocation parent, String textureKey, ResourceLocation texture) {
		return getBuilder(name);
	}

	public T cubeAll(String name, ResourceLocation texture) {
		return getBuilder(name);
	}

	public T cubeTop(String name, ResourceLocation side, ResourceLocation top) {
		return getBuilder(name);
	}

	public T cubeBottomTop(String name, ResourceLocation side, ResourceLocation bottom, ResourceLocation top) {
		return getBuilder(name);
	}

	public T cubeColumn(String name, ResourceLocation side, ResourceLocation end) {
		return getBuilder(name);
	}

	public T cubeColumnHorizontal(String name, ResourceLocation side, ResourceLocation end) {
		return getBuilder(name);
	}

	public T orientable(String name, ResourceLocation side, ResourceLocation front, ResourceLocation top) {
		return getBuilder(name);
	}

	public T orientableVertical(String name, ResourceLocation side, ResourceLocation front) {
		return getBuilder(name);
	}

	public T orientableWithBottom(String name, ResourceLocation side, ResourceLocation front, ResourceLocation bottom, ResourceLocation top) {
		return getBuilder(name);
	}

	public T crop(String name, ResourceLocation crop) {
		return getBuilder(name);
	}

	public T cross(String name, ResourceLocation cross) {
		return getBuilder(name);
	}

	public T stairs(String name, ResourceLocation side, ResourceLocation bottom, ResourceLocation top) {
		return getBuilder(name);
	}

	public T stairsOuter(String name, ResourceLocation side, ResourceLocation bottom, ResourceLocation top) {
		return getBuilder(name);
	}

	public T stairsInner(String name, ResourceLocation side, ResourceLocation bottom, ResourceLocation top) {
		return getBuilder(name);
	}

	public T slab(String name, ResourceLocation side, ResourceLocation bottom, ResourceLocation top) {
		return getBuilder(name);
	}

	public T slabTop(String name, ResourceLocation side, ResourceLocation bottom, ResourceLocation top) {
		return getBuilder(name);
	}

	public T fencePost(String name, ResourceLocation texture) {
		return getBuilder(name);
	}

	public T fenceSide(String name, ResourceLocation texture) {
		return getBuilder(name);
	}

	public T fenceInventory(String name, ResourceLocation texture) {
		return getBuilder(name);
	}

	public T fenceGate(String name, ResourceLocation texture) {
		return getBuilder(name);
	}

	public T wallPost(String name, ResourceLocation wall) {
		return getBuilder(name);
	}

	public T wallSide(String name, ResourceLocation wall) {
		return getBuilder(name);
	}

	public T wallSideTall(String name, ResourceLocation wall) {
		return getBuilder(name);
	}

	public T wallInventory(String name, ResourceLocation wall) {
		return getBuilder(name);
	}

	public T panePost(String name, ResourceLocation pane, ResourceLocation edge) {
		return getBuilder(name);
	}

	public T paneSide(String name, ResourceLocation pane, ResourceLocation edge) {
		return getBuilder(name);
	}

	public T paneSideAlt(String name, ResourceLocation pane, ResourceLocation edge) {
		return getBuilder(name);
	}

	public T paneNoSide(String name, ResourceLocation pane) {
		return getBuilder(name);
	}

	public T paneNoSideAlt(String name, ResourceLocation pane) {
		return getBuilder(name);
	}

	public T doorBottomLeft(String name, ResourceLocation bottom, ResourceLocation top) {
		return getBuilder(name);
	}

	public T doorBottomRight(String name, ResourceLocation bottom, ResourceLocation top) {
		return getBuilder(name);
	}

	public T doorTopLeft(String name, ResourceLocation bottom, ResourceLocation top) {
		return getBuilder(name);
	}

	public T doorTopRight(String name, ResourceLocation bottom, ResourceLocation top) {
		return getBuilder(name);
	}

	public T trapdoorBottom(String name, ResourceLocation texture) {
		return getBuilder(name);
	}

	public T trapdoorTop(String name, ResourceLocation texture) {
		return getBuilder(name);
	}

	public T trapdoorOpen(String name, ResourceLocation texture) {
		return getBuilder(name);
	}

	public T trapdoorOrientableBottom(String name, ResourceLocation texture) {
		return getBuilder(name);
	}

	public T trapdoorOrientableTop(String name, ResourceLocation texture) {
		return getBuilder(name);
	}

	public T trapdoorOrientableOpen(String name, ResourceLocation texture) {
		return getBuilder(name);
	}

	public T torch(String name, ResourceLocation torch) {
		return getBuilder(name);
	}

	public T torchWall(String name, ResourceLocation torch) {
		return getBuilder(name);
	}

	public T carpet(String name, ResourceLocation wool) {
		return getBuilder(name);
	}

	public T buttonInventory(String name, ResourceLocation texture) {
		return getBuilder(name);
	}

	public T pressurePlate(String name, ResourceLocation texture) {
		return getBuilder(name);
	}

	public T sign(String name, ResourceLocation texture) {
		return getBuilder(name);
	}

	public T leaves(String name, ResourceLocation texture) {
		return getBuilder(name);
	}

	public T logHorizontal(String name, ResourceLocation side, ResourceLocation end) {
		return getBuilder(name);
	}

	public String getModid() {
		return modid;
	}

	protected static String name(Block block) {
		return net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block).getPath();
	}
}
