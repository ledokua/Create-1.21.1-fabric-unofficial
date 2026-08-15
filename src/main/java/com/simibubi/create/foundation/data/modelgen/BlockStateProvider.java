package com.simibubi.create.foundation.data.modelgen;

import java.util.function.Function;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Shell — see package docs. Blockstate datagen provider: the {@code simpleBlock/horizontalBlock/...} preset
 * helpers upstream has all no-op here; {@link #models()} / {@link #itemModels()} hand out shell providers.
 */
public abstract class BlockStateProvider {

	private final String modid;
	private final ExistingFileHelper existingFileHelper;
	private final BlockModelProvider blockModels;
	private final ItemModelProvider itemModels;

	public BlockStateProvider(String modid, ExistingFileHelper exFileHelper) {
		this.modid = modid;
		this.existingFileHelper = exFileHelper;
		this.blockModels = new BlockModelProvider(modid, exFileHelper) {};
		this.itemModels = new ItemModelProvider(modid, exFileHelper) {};
	}

	protected abstract void registerStatesAndModels();

	public VariantBlockStateBuilder getVariantBuilder(Block b) {
		return new VariantBlockStateBuilder(b);
	}

	public MultiPartBlockStateBuilder getMultipartBuilder(Block b) {
		return new MultiPartBlockStateBuilder(b);
	}

	public BlockModelProvider models() {
		return blockModels;
	}

	public ItemModelProvider itemModels() {
		return itemModels;
	}

	public ResourceLocation modLoc(String name) {
		return ResourceLocation.fromNamespaceAndPath(modid, name);
	}

	public ResourceLocation mcLoc(String name) {
		return ResourceLocation.withDefaultNamespace(name);
	}

	public ResourceLocation blockTexture(Block block) {
		ResourceLocation name = key(block);
		return ResourceLocation.fromNamespaceAndPath(name.getNamespace(), ModelProvider.BLOCK_FOLDER + "/" + name.getPath());
	}

	private ResourceLocation key(Block block) {
		return BuiltInRegistries.BLOCK.getKey(block);
	}

	private String name(Block block) {
		return key(block).getPath();
	}

	public ModelFile cubeAll(Block block) {
		return models().cubeAll(name(block), blockTexture(block));
	}

	public void simpleBlock(Block block) {}

	public void simpleBlock(Block block, Function<ModelFile, ConfiguredModel[]> expander) {}

	public void simpleBlock(Block block, ModelFile model) {}

	public void simpleBlockItem(Block block, ModelFile model) {}

	public void simpleBlockWithItem(Block block, ModelFile model) {}

	public void simpleBlock(Block block, ConfiguredModel... models) {}

	public void axisBlock(RotatedPillarBlock block) {}

	public void logBlock(RotatedPillarBlock block) {}

	public void axisBlock(RotatedPillarBlock block, ResourceLocation baseName) {}

	public void axisBlock(RotatedPillarBlock block, ResourceLocation side, ResourceLocation end) {}

	public void axisBlock(RotatedPillarBlock block, ModelFile vertical, ModelFile horizontal) {}

	public void horizontalBlock(Block block, ResourceLocation side, ResourceLocation front, ResourceLocation top) {}

	public void horizontalBlock(Block block, ModelFile model) {}

	public void horizontalBlock(Block block, ModelFile model, int angleOffset) {}

	public void horizontalBlock(Block block, Function<BlockState, ModelFile> modelFunc) {}

	public void horizontalBlock(Block block, Function<BlockState, ModelFile> modelFunc, int angleOffset) {}

	public void horizontalFaceBlock(Block block, ModelFile model) {}

	public void horizontalFaceBlock(Block block, ModelFile model, int angleOffset) {}

	public void horizontalFaceBlock(Block block, Function<BlockState, ModelFile> modelFunc) {}

	public void horizontalFaceBlock(Block block, Function<BlockState, ModelFile> modelFunc, int angleOffset) {}

	public void directionalBlock(Block block, ModelFile model) {}

	public void directionalBlock(Block block, ModelFile model, int angleOffset) {}

	public void directionalBlock(Block block, Function<BlockState, ModelFile> modelFunc) {}

	public void directionalBlock(Block block, Function<BlockState, ModelFile> modelFunc, int angleOffset) {}

	public void stairsBlock(StairBlock block, ResourceLocation texture) {}

	public void stairsBlock(StairBlock block, String name, ResourceLocation texture) {}

	public void stairsBlock(StairBlock block, ResourceLocation side, ResourceLocation bottom, ResourceLocation top) {}

	public void stairsBlock(StairBlock block, String name, ResourceLocation side, ResourceLocation bottom, ResourceLocation top) {}

	public void stairsBlock(StairBlock block, ModelFile stairs, ModelFile stairsInner, ModelFile stairsOuter) {}

	public void slabBlock(SlabBlock block, ResourceLocation doubleslab, ResourceLocation texture) {}

	public void slabBlock(SlabBlock block, ResourceLocation doubleslab, ResourceLocation side, ResourceLocation bottom, ResourceLocation top) {}

	public void slabBlock(SlabBlock block, ModelFile bottom, ModelFile top, ModelFile doubleslab) {}

	public void buttonBlock(net.minecraft.world.level.block.ButtonBlock block, ResourceLocation texture) {}

	public void pressurePlateBlock(net.minecraft.world.level.block.PressurePlateBlock block, ResourceLocation texture) {}

	public void signBlock(net.minecraft.world.level.block.StandingSignBlock signBlock, net.minecraft.world.level.block.WallSignBlock wallSignBlock, ResourceLocation texture) {}

	public void fourWayBlock(net.minecraft.world.level.block.CrossCollisionBlock block, ModelFile post, ModelFile side) {}

	public void fourWayMultipart(MultiPartBlockStateBuilder builder, ModelFile side) {}

	public void fenceBlock(FenceBlock block, ResourceLocation texture) {}

	public void fenceBlock(FenceBlock block, String name, ResourceLocation texture) {}

	public void fenceBlock(FenceBlock block, ModelFile post, ModelFile side) {}

	public void fenceGateBlock(FenceGateBlock block, ResourceLocation texture) {}

	public void fenceGateBlock(FenceGateBlock block, String name, ResourceLocation texture) {}

	public void fenceGateBlock(FenceGateBlock block, ModelFile gate, ModelFile gateOpen, ModelFile gateWall, ModelFile gateWallOpen) {}

	public void wallBlock(WallBlock block, ResourceLocation texture) {}

	public void wallBlock(WallBlock block, String name, ResourceLocation texture) {}

	public void wallBlock(WallBlock block, ModelFile post, ModelFile side, ModelFile sideTall) {}

	public void paneBlock(IronBarsBlock block, ResourceLocation pane, ResourceLocation edge) {}

	public void paneBlock(IronBarsBlock block, String name, ResourceLocation pane, ResourceLocation edge) {}

	public void paneBlock(IronBarsBlock block, ModelFile post, ModelFile side, ModelFile sideAlt, ModelFile noSide, ModelFile noSideAlt) {}

	public void doorBlock(DoorBlock block, ResourceLocation bottom, ResourceLocation top) {}

	public void doorBlock(DoorBlock block, String name, ResourceLocation bottom, ResourceLocation top) {}

	public void doorBlock(DoorBlock block, ModelFile bottomLeft, ModelFile bottomLeftOpen, ModelFile bottomRight, ModelFile bottomRightOpen, ModelFile topLeft, ModelFile topLeftOpen, ModelFile topRight, ModelFile topRightOpen) {}

	public void trapdoorBlock(TrapDoorBlock block, ResourceLocation texture, boolean orientable) {}

	public void trapdoorBlock(TrapDoorBlock block, String name, ResourceLocation texture, boolean orientable) {}

	public void trapdoorBlock(TrapDoorBlock block, ModelFile bottom, ModelFile top, ModelFile open, boolean orientable) {}

	public ExistingFileHelper getExistingFileHelper() {
		return existingFileHelper;
	}
}
