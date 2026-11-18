package com.simibubi.create.content.logistics;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.simibubi.create.foundation.platform.ServerLifecycleHooks;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;

/**
 * [Fabric port] Item matching for stock ticker orders.
 * <p>
 * Orders come back from the client as a copy decoded from the network, and the packager compares that copy with what
 * it finds in the inventory using {@link ItemStack#isSameItemSameComponents}. That relies on every component value having
 * value equality. Some don't (Create's own toolbox inventory, other mods' bundle-like contents), so such items were
 * listed by the stock ticker but never packed. Where the plain check fails for the same item, this compares the
 * differing component values by their saved (codec) form instead.
 */
public final class LogisticsItemMatching {

	private LogisticsItemMatching() {}

	public static boolean matches(ItemStack a, ItemStack b) {
		if (ItemStack.isSameItemSameComponents(a, b))
			return true;
		if (a.isEmpty() || b.isEmpty() || !ItemStack.isSameItem(a, b))
			return false;

		DataComponentPatch patchA = a.getComponentsPatch();
		DataComponentPatch patchB = b.getComponentsPatch();
		if (patchA.size() != patchB.size())
			return false;

		DynamicOps<Tag> ops = ops();
		for (Map.Entry<DataComponentType<?>, Optional<?>> entry : patchA.entrySet()) {
			DataComponentType<?> type = entry.getKey();
			Optional<?> valueB = patchB.get(type);
			if (valueB == null)
				return false;
			Optional<?> valueA = entry.getValue();
			if (Objects.equals(valueA, valueB))
				continue;
			if (valueA.isEmpty() || valueB.isEmpty())
				return false;
			if (!sameEncoding(type, valueA.get(), valueB.get(), ops))
				return false;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	private static <T> boolean sameEncoding(DataComponentType<T> type, Object a, Object b, DynamicOps<Tag> ops) {
		Codec<T> codec = type.codec();
		if (codec == null)
			return false;
		try {
			Optional<Tag> encodedA = codec.encodeStart(ops, (T) a).result();
			Optional<Tag> encodedB = codec.encodeStart(ops, (T) b).result();
			return encodedA.isPresent() && encodedA.equals(encodedB);
		} catch (RuntimeException e) {
			return false;
		}
	}

	private static DynamicOps<Tag> ops() {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		return server == null ? NbtOps.INSTANCE : server.registryAccess().createSerializationContext(NbtOps.INSTANCE);
	}
}
