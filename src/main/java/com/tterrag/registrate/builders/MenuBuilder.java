package com.tterrag.registrate.builders;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.fabric.RegistrateClientHooks;
import com.tterrag.registrate.fabric.RegistrateEnv;
import com.tterrag.registrate.fabric.RegistrateMenuData;
import com.tterrag.registrate.util.entry.MenuEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

/**
 * Same public shape as upstream. The extra-data ({@link ForgeMenuFactory}) path — which is what all of
 * Create's menus use — maps onto Fabric's {@link ExtendedScreenHandlerType} carrying a
 * {@link RegistrateMenuData} blob, so the client factory still receives a {@code RegistryFriendlyByteBuf}
 * to read exactly like it does under NeoForge's {@code IMenuTypeExtension}. The plain
 * {@link MenuFactory} path is a vanilla {@code MenuType}.
 */
public class MenuBuilder<T extends AbstractContainerMenu, S extends Screen & MenuAccess<T>, P> extends AbstractBuilder<MenuType<?>, MenuType<T>, P, MenuBuilder<T, S, P>> {

	@FunctionalInterface
	public interface MenuFactory<T extends AbstractContainerMenu> {
		T create(MenuType<T> type, int windowId, Inventory inv);
	}

	/** Upstream's name for the "with extra data buffer" factory shape; kept so Create's imports resolve. */
	@FunctionalInterface
	public interface ForgeMenuFactory<T extends AbstractContainerMenu> {
		T create(MenuType<T> type, int windowId, Inventory inv, @Nullable RegistryFriendlyByteBuf buffer);
	}

	@FunctionalInterface
	public interface ScreenFactory<M extends AbstractContainerMenu, T extends Screen & MenuAccess<M>> {
		T create(M menu, Inventory inv, Component displayName);
	}

	public static <T extends AbstractContainerMenu, S extends Screen & MenuAccess<T>, P> MenuBuilder<T, S, P> create(
		AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, MenuFactory<T> factory,
		NonNullSupplier<ScreenFactory<T, S>> screenFactory) {
		return new MenuBuilder<>(owner, parent, name, callback, factory, screenFactory);
	}

	public static <T extends AbstractContainerMenu, S extends Screen & MenuAccess<T>, P> MenuBuilder<T, S, P> create(
		AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ForgeMenuFactory<T> factory,
		NonNullSupplier<ScreenFactory<T, S>> screenFactory) {
		return new MenuBuilder<>(owner, parent, name, callback, factory, screenFactory);
	}

	@Nullable
	private final MenuFactory<T> simpleFactory;
	@Nullable
	private final ForgeMenuFactory<T> extendedFactory;
	private final NonNullSupplier<ScreenFactory<T, S>> screenFactory;

	public MenuBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, MenuFactory<T> factory,
		NonNullSupplier<ScreenFactory<T, S>> screenFactory) {
		super(owner, parent, name, callback, Registries.MENU);
		this.simpleFactory = factory;
		this.extendedFactory = null;
		this.screenFactory = screenFactory;
	}

	public MenuBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ForgeMenuFactory<T> factory,
		NonNullSupplier<ScreenFactory<T, S>> screenFactory) {
		super(owner, parent, name, callback, Registries.MENU);
		this.simpleFactory = null;
		this.extendedFactory = factory;
		this.screenFactory = screenFactory;
	}

	@Override
	protected MenuType<T> createEntry() {
		NonNullSupplier<MenuType<T>> self = asSupplier();
		MenuType<T> ret;
		if (extendedFactory != null) {
			ForgeMenuFactory<T> factory = extendedFactory;
			ret = new ExtendedScreenHandlerType<>((windowId, inv, data) -> {
				RegistryFriendlyByteBuf buf = data.read(inv.player.registryAccess());
				try {
					return factory.create(self.get(), windowId, inv, buf);
				} finally {
					buf.release();
				}
			}, RegistrateMenuData.CODEC);
		} else {
			MenuFactory<T> factory = simpleFactory;
			ret = new MenuType<>((windowId, inv) -> factory.create(self.get(), windowId, inv), FeatureFlags.VANILLA_SET);
		}
		RegistrateEnv.runOnClient(() -> () -> RegistrateClientHooks.registerScreen(ret, screenFactory));
		return ret;
	}

	@Override
	protected RegistryEntry<MenuType<?>, MenuType<T>> createEntryWrapper(ResourceLocation id) {
		return MenuEntry.unresolved(id);
	}

	@Override
	public MenuEntry<T> register() {
		return (MenuEntry<T>) super.register();
	}
}
