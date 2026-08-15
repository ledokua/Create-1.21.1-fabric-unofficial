package com.simibubi.create.foundation.mixin.extension;

import java.util.OptionalInt;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.tterrag.registrate.fabric.RegistrateMenuData;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * Injected onto {@code Player}: NeoForge's {@code openMenu(provider, buf -> ...)} that ships extra data to the
 * client menu factory. Fabric's counterpart is an {@code ExtendedScreenHandlerFactory} carrying a typed
 * payload; we wrap the provider and carry the written buffer as {@link RegistrateMenuData}, matching what
 * Registrate's {@code MenuBuilder} registered on the client side.
 */
public interface PlayerExtension {

	default OptionalInt openMenu(@Nullable MenuProvider provider, Consumer<RegistryFriendlyByteBuf> extraDataWriter) {
		Player self = (Player) this;
		if (provider == null)
			return OptionalInt.empty();
		if (!(self instanceof ServerPlayer serverPlayer))
			return OptionalInt.empty();
		RegistrateMenuData data = RegistrateMenuData.write(serverPlayer.registryAccess(), extraDataWriter);
		return serverPlayer.openMenu(new ExtendedScreenHandlerFactory<RegistrateMenuData>() {
			@Override
			public RegistrateMenuData getScreenOpeningData(ServerPlayer player) {
				return data;
			}

			@Override
			public Component getDisplayName() {
				return provider.getDisplayName();
			}

			@Override
			public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
				return provider.createMenu(containerId, playerInventory, player);
			}
		});
	}

	default OptionalInt openMenu(@Nullable MenuProvider provider, BlockPos pos) {
		return openMenu(provider, buf -> buf.writeBlockPos(pos));
	}
}
