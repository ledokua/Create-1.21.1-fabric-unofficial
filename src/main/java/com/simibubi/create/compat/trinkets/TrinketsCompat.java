package com.simibubi.create.compat.trinkets;

import java.util.List;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.equipment.armor.BacktankUtil;
import com.simibubi.create.content.equipment.goggles.GogglesItem;
import com.simibubi.create.foundation.platform.EffectiveSide;

import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.item.ItemStack;

/**
 * Trinkets counterpart of upstream's Curios compat: goggles worn in a trinket slot count as worn (and render on the
 * head), backtanks in trinket slots supply air. Slot assignment comes from data
 * ({@code data/trinkets/tags/item/head/hat.json}, {@code chest/back.json}).
 */
public class TrinketsCompat {

	public static void init() {
		GogglesItem.addIsWearingPredicate(player -> TrinketsApi.getTrinketComponent(player)
			.map(component -> component.isEquipped(AllItems.GOGGLES.get()))
			.orElse(false));

		BacktankUtil.addBacktankSupplier(entity -> TrinketsApi.getTrinketComponent(entity)
			.map(component -> component.getEquipped(stack -> AllTags.AllItemTags.PRESSURIZED_AIR_SOURCES.matches(stack))
				.stream().map(net.minecraft.util.Tuple::getB).toList())
			.orElse(List.<ItemStack>of()));

		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT)
			TrinketsClientCompat.init();
	}
}
