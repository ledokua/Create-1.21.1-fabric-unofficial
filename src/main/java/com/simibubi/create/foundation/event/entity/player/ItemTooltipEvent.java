package com.simibubi.create.foundation.event.entity.player;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** Fired when an item's tooltip is built; handlers may edit the line list. Fired from Fabric's ItemTooltipCallback. */
public class ItemTooltipEvent extends PlayerEvent {

	private final TooltipFlag flags;
	private final ItemStack itemStack;
	private final List<Component> toolTip;
	private final Item.TooltipContext context;

	public ItemTooltipEvent(ItemStack itemStack, @Nullable Player player, List<Component> list, TooltipFlag flags, Item.TooltipContext context) {
		super(player);
		this.itemStack = itemStack;
		this.toolTip = list;
		this.flags = flags;
		this.context = context;
	}

	public TooltipFlag getFlags() {
		return flags;
	}

	public ItemStack getItemStack() {
		return itemStack;
	}

	public List<Component> getToolTip() {
		return toolTip;
	}

	public Item.TooltipContext getContext() {
		return context;
	}

	/** May be null when tooltips are built without a player (e.g. from JEI). */
	@Override
	@Nullable
	public Player getEntity() {
		return super.getEntity();
	}
}
