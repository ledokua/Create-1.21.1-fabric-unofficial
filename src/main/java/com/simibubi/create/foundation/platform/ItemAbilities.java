package com.simibubi.create.foundation.platform;

import java.util.Map;

import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;

/**
 * Standard tool actions. On Fabric there is no per-item action registry, so {@link #canPerform} maps each
 * action to the vanilla tool tags (which Fabric convention also uses); {@code ItemStack#canPerformAction} is
 * interface-injected to call this.
 */
public final class ItemAbilities {

	private ItemAbilities() {}

	public static final ItemAbility AXE_DIG = ItemAbility.get("axe_dig");
	public static final ItemAbility PICKAXE_DIG = ItemAbility.get("pickaxe_dig");
	public static final ItemAbility SHOVEL_DIG = ItemAbility.get("shovel_dig");
	public static final ItemAbility HOE_DIG = ItemAbility.get("hoe_dig");
	public static final ItemAbility SWORD_DIG = ItemAbility.get("sword_dig");
	public static final ItemAbility SHEARS_DIG = ItemAbility.get("shears_dig");
	public static final ItemAbility AXE_STRIP = ItemAbility.get("axe_strip");
	public static final ItemAbility AXE_SCRAPE = ItemAbility.get("axe_scrape");
	public static final ItemAbility AXE_WAX_OFF = ItemAbility.get("axe_wax_off");
	public static final ItemAbility SHOVEL_FLATTEN = ItemAbility.get("shovel_flatten");
	public static final ItemAbility SWORD_SWEEP = ItemAbility.get("sword_sweep");
	public static final ItemAbility SHEARS_HARVEST = ItemAbility.get("shears_harvest");
	public static final ItemAbility SHEARS_REMOVE_ARMOR = ItemAbility.get("shears_remove_armor");
	public static final ItemAbility SHEARS_CARVE = ItemAbility.get("shears_carve");
	public static final ItemAbility SHEARS_DISARM = ItemAbility.get("shears_disarm");
	public static final ItemAbility SHEARS_TRIM = ItemAbility.get("shears_trim");
	public static final ItemAbility HOE_TILL = ItemAbility.get("hoe_till");
	public static final ItemAbility SHIELD_BLOCK = ItemAbility.get("shield_block");
	public static final ItemAbility FISHING_ROD_CAST = ItemAbility.get("fishing_rod_cast");
	public static final ItemAbility TRIDENT_THROW = ItemAbility.get("trident_throw");
	public static final ItemAbility BRUSH_BRUSH = ItemAbility.get("brush_brush");
	public static final ItemAbility FIRESTARTER_LIGHT = ItemAbility.get("firestarter_light");

	private static final Map<ItemAbility, TagKey<Item>> BY_TAG = Map.ofEntries(
		Map.entry(AXE_DIG, ItemTags.AXES), Map.entry(AXE_STRIP, ItemTags.AXES), Map.entry(AXE_SCRAPE, ItemTags.AXES), Map.entry(AXE_WAX_OFF, ItemTags.AXES),
		Map.entry(PICKAXE_DIG, ItemTags.PICKAXES),
		Map.entry(SHOVEL_DIG, ItemTags.SHOVELS), Map.entry(SHOVEL_FLATTEN, ItemTags.SHOVELS),
		Map.entry(HOE_DIG, ItemTags.HOES), Map.entry(HOE_TILL, ItemTags.HOES),
		Map.entry(SWORD_DIG, ItemTags.SWORDS), Map.entry(SWORD_SWEEP, ItemTags.SWORDS));

	public static boolean canPerform(ItemStack stack, ItemAbility ability) {
		if (stack.isEmpty())
			return false;
		TagKey<Item> tag = BY_TAG.get(ability);
		if (tag != null)
			return stack.is(tag);
		if (ability.name().startsWith("shears_"))
			return stack.getItem() instanceof ShearsItem;
		return false;
	}
}
