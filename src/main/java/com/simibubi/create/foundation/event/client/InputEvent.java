package com.simibubi.create.foundation.event.client;

import com.simibubi.create.foundation.event.bus.Event;
import com.simibubi.create.foundation.event.bus.ICancellableEvent;

import net.minecraft.client.KeyMapping;
import net.minecraft.world.InteractionHand;

/** Client input hooks; fired from Fabric's ClientPreAttackCallback / mixins on MouseHandler & KeyboardHandler. */
public abstract class InputEvent extends Event {

	/** Raw mouse button (GLFW codes). */
	public abstract static class MouseButton extends InputEvent {
		private final int button;
		private final int action;
		private final int modifiers;

		protected MouseButton(int button, int action, int modifiers) {
			this.button = button;
			this.action = action;
			this.modifiers = modifiers;
		}

		public int getButton() {
			return button;
		}

		public int getAction() {
			return action;
		}

		public int getModifiers() {
			return modifiers;
		}

		/** Before vanilla handles the click; cancellable. */
		public static class Pre extends MouseButton implements ICancellableEvent {
			public Pre(int button, int action, int modifiers) {
				super(button, action, modifiers);
			}
		}

		public static class Post extends MouseButton {
			public Post(int button, int action, int modifiers) {
				super(button, action, modifiers);
			}
		}
	}

	/** Mouse wheel; cancellable. */
	public static class MouseScrollingEvent extends InputEvent implements ICancellableEvent {
		private final double scrollDeltaX;
		private final double scrollDeltaY;
		private final double mouseX;
		private final double mouseY;
		private final boolean leftDown;
		private final boolean middleDown;
		private final boolean rightDown;

		public MouseScrollingEvent(double scrollDeltaX, double scrollDeltaY, boolean leftDown, boolean middleDown, boolean rightDown, double mouseX, double mouseY) {
			this.scrollDeltaX = scrollDeltaX;
			this.scrollDeltaY = scrollDeltaY;
			this.leftDown = leftDown;
			this.middleDown = middleDown;
			this.rightDown = rightDown;
			this.mouseX = mouseX;
			this.mouseY = mouseY;
		}

		public double getScrollDeltaX() {
			return scrollDeltaX;
		}

		public double getScrollDeltaY() {
			return scrollDeltaY;
		}

		public boolean isLeftDown() {
			return leftDown;
		}

		public boolean isRightDown() {
			return rightDown;
		}

		public boolean isMiddleDown() {
			return middleDown;
		}

		public double getMouseX() {
			return mouseX;
		}

		public double getMouseY() {
			return mouseY;
		}
	}

	/** Raw keyboard key (GLFW codes). */
	public static class Key extends InputEvent {
		private final int key;
		private final int scanCode;
		private final int action;
		private final int modifiers;

		public Key(int key, int scanCode, int action, int modifiers) {
			this.key = key;
			this.scanCode = scanCode;
			this.action = action;
			this.modifiers = modifiers;
		}

		public int getKey() {
			return key;
		}

		public int getScanCode() {
			return scanCode;
		}

		public int getAction() {
			return action;
		}

		public int getModifiers() {
			return modifiers;
		}
	}

	/** Attack/use/pick keybinding about to fire; cancellable (cancel = don't perform the interaction). */
	public static class InteractionKeyMappingTriggered extends InputEvent implements ICancellableEvent {
		private final int button;
		private final KeyMapping keyMapping;
		private final InteractionHand hand;
		private boolean handSwing = true;

		public InteractionKeyMappingTriggered(int button, KeyMapping keyMapping, InteractionHand hand) {
			this.button = button;
			this.keyMapping = keyMapping;
			this.hand = hand;
		}

		public void setSwingHand(boolean value) {
			handSwing = value;
		}

		public boolean shouldSwingHand() {
			return handSwing;
		}

		public InteractionHand getHand() {
			return hand;
		}

		public boolean isAttack() {
			return button == 0;
		}

		public boolean isUseItem() {
			return button == 1;
		}

		public boolean isPickBlock() {
			return button == 2;
		}

		public KeyMapping getKeyMapping() {
			return keyMapping;
		}
	}
}
