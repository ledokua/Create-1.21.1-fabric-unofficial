package com.simibubi.create.foundation.event.entity.player;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.foundation.event.bus.ICancellableEvent;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Player interaction events (bridge). Fired from Fabric's {@code UseBlockCallback}/{@code UseItemCallback}/
 * {@code UseEntityCallback}/{@code AttackBlockCallback} etc. — see {@code CreateEventHooks}. Same subclass
 * shape and accessors as upstream's handlers expect.
 */
public class PlayerInteractEvent extends PlayerEvent {

	private final InteractionHand hand;
	private final BlockPos pos;
	@Nullable
	private final Direction face;
	private InteractionResult cancellationResult = InteractionResult.PASS;

	private PlayerInteractEvent(Player player, InteractionHand hand, BlockPos pos, @Nullable Direction face) {
		super(player);
		this.hand = hand;
		this.pos = pos;
		this.face = face;
	}

	public InteractionHand getHand() {
		return hand;
	}

	public ItemStack getItemStack() {
		return getEntity().getItemInHand(hand);
	}

	public BlockPos getPos() {
		return pos;
	}

	@Nullable
	public Direction getFace() {
		return face;
	}

	public Level getLevel() {
		return getEntity().level();
	}

	public EnvType getSide() {
		return getLevel().isClientSide ? EnvType.CLIENT : EnvType.SERVER;
	}

	/** The result the interaction returns to vanilla when the event is cancelled. */
	public InteractionResult getCancellationResult() {
		return cancellationResult;
	}

	public void setCancellationResult(InteractionResult result) {
		this.cancellationResult = result;
	}

	// ---- subclasses ----

	/** Right-click on a block; cancellable. Fired on both sides. */
	public static class RightClickBlock extends PlayerInteractEvent implements ICancellableEvent {
		private TriState useBlock = TriState.DEFAULT;
		private TriState useItem = TriState.DEFAULT;
		private final BlockHitResult hitVec;

		public RightClickBlock(Player player, InteractionHand hand, BlockPos pos, BlockHitResult hitVec) {
			super(player, hand, pos, hitVec.getDirection());
			this.hitVec = hitVec;
		}

		public TriState getUseBlock() {
			return useBlock;
		}

		public TriState getUseItem() {
			return useItem;
		}

		public BlockHitResult getHitVec() {
			return hitVec;
		}

		public void setUseBlock(TriState triggerBlock) {
			this.useBlock = triggerBlock;
		}

		public void setUseItem(TriState triggerItem) {
			this.useItem = triggerItem;
		}

		@Override
		public void setCanceled(boolean canceled) {
			ICancellableEvent.super.setCanceled(canceled);
			if (canceled) {
				useBlock = TriState.FALSE;
				useItem = TriState.FALSE;
			}
		}
	}

	/** Right-click with an item, not targeting a block or entity; cancellable. */
	public static class RightClickItem extends PlayerInteractEvent implements ICancellableEvent {
		public RightClickItem(Player player, InteractionHand hand) {
			super(player, hand, player.blockPosition(), null);
		}
	}

	/** Right-click with an empty hand into the air; not cancellable (client only). */
	public static class RightClickEmpty extends PlayerInteractEvent {
		public RightClickEmpty(Player player, InteractionHand hand) {
			super(player, hand, player.blockPosition(), null);
		}
	}

	/** Left-click on a block; cancellable. Fired on both sides. */
	public static class LeftClickBlock extends PlayerInteractEvent implements ICancellableEvent {
		private TriState useBlock = TriState.DEFAULT;
		private TriState useItem = TriState.DEFAULT;
		private final Action action;

		public LeftClickBlock(Player player, BlockPos pos, Direction face, Action action) {
			super(player, InteractionHand.MAIN_HAND, pos, face);
			this.action = action;
		}

		public TriState getUseBlock() {
			return useBlock;
		}

		public TriState getUseItem() {
			return useItem;
		}

		public void setUseBlock(TriState triggerBlock) {
			this.useBlock = triggerBlock;
		}

		public void setUseItem(TriState triggerItem) {
			this.useItem = triggerItem;
		}

		public Action getAction() {
			return action;
		}

		@Override
		public void setCanceled(boolean canceled) {
			ICancellableEvent.super.setCanceled(canceled);
			if (canceled) {
				useBlock = TriState.FALSE;
				useItem = TriState.FALSE;
			}
		}

		public enum Action {
			START, ABORT, STOP, CLIENT_HOLD;
		}
	}

	/** Left-click into the air; not cancellable (client only). */
	public static class LeftClickEmpty extends PlayerInteractEvent {
		public LeftClickEmpty(Player player) {
			super(player, InteractionHand.MAIN_HAND, player.blockPosition(), null);
		}
	}

	/** Right-click on an entity; cancellable. */
	public static class EntityInteract extends PlayerInteractEvent implements ICancellableEvent {
		private final Entity target;

		public EntityInteract(Player player, InteractionHand hand, Entity target) {
			super(player, hand, target.blockPosition(), null);
			this.target = target;
		}

		public Entity getTarget() {
			return target;
		}
	}

	/** Right-click on an entity with a precise hit location; cancellable. Fires before {@link EntityInteract}. */
	public static class EntityInteractSpecific extends PlayerInteractEvent implements ICancellableEvent {
		private final Vec3 localPos;
		private final Entity target;

		public EntityInteractSpecific(Player player, InteractionHand hand, Entity target, Vec3 localPos) {
			super(player, hand, target.blockPosition(), null);
			this.localPos = localPos;
			this.target = target;
		}

		public Vec3 getLocalPos() {
			return localPos;
		}

		public Entity getTarget() {
			return target;
		}
	}
}
