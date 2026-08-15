package com.tterrag.registrate.builders;

import java.util.LinkedHashSet;
import java.util.Set;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.fabric.RegistrateClientHooks;
import com.tterrag.registrate.fabric.RegistrateEnv;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

public class BlockEntityBuilder<T extends BlockEntity, P> extends AbstractBuilder<BlockEntityType<?>, BlockEntityType<T>, P, BlockEntityBuilder<T, P>> {

	@FunctionalInterface
	public interface BlockEntityFactory<T extends BlockEntity> {
		T create(BlockEntityType<T> type, BlockPos pos, BlockState state);
	}

	public static <T extends BlockEntity, P> BlockEntityBuilder<T, P> create(AbstractRegistrate<?> owner, P parent,
		String name, BuilderCallback callback, BlockEntityFactory<T> factory) {
		return new BlockEntityBuilder<>(owner, parent, name, callback, factory);
	}

	private final BlockEntityFactory<T> factory;
	private final Set<NonNullSupplier<? extends Block>> validBlocks = new LinkedHashSet<>();
	private NonNullSupplier<NonNullFunction<BlockEntityRendererProvider.Context, BlockEntityRenderer<? super T>>> renderer;

	protected BlockEntityBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, BlockEntityFactory<T> factory) {
		super(owner, parent, name, callback, Registries.BLOCK_ENTITY_TYPE);
		this.factory = factory;
	}

	public BlockEntityBuilder<T, P> validBlock(NonNullSupplier<? extends Block> block) {
		validBlocks.add(block);
		return this;
	}

	@SafeVarargs
	public final BlockEntityBuilder<T, P> validBlocks(NonNullSupplier<? extends Block>... blocks) {
		for (var block : blocks) {
			validBlock(block);
		}
		return this;
	}

	/** Block entity renderer (client). Registered via vanilla BlockEntityRenderers after the type resolves. */
	public BlockEntityBuilder<T, P> renderer(NonNullSupplier<NonNullFunction<BlockEntityRendererProvider.Context, BlockEntityRenderer<? super T>>> renderer) {
		if (this.renderer == null) { // first call only — later calls just swap the supplier
			onRegister(type -> RegistrateEnv.runOnClient(() -> () -> {
				if (this.renderer != null) {
					RegistrateClientHooks.registerBlockEntityRenderer(type, this.renderer);
				}
			}));
		}
		this.renderer = renderer;
		return this;
	}

	@Override
	protected BlockEntityType<T> createEntry() {
		NonNullSupplier<BlockEntityType<T>> self = asSupplier();
		Block[] blocks = validBlocks.stream().map(NonNullSupplier::get).toArray(Block[]::new);
		return BlockEntityType.Builder.of((pos, state) -> factory.create(self.get(), pos, state), blocks).build(null);
	}

	@Override
	protected RegistryEntry<BlockEntityType<?>, BlockEntityType<T>> createEntryWrapper(ResourceLocation id) {
		return BlockEntityEntry.unresolved(id);
	}

	@Override
	public BlockEntityEntry<T> register() {
		return (BlockEntityEntry<T>) super.register();
	}
}
