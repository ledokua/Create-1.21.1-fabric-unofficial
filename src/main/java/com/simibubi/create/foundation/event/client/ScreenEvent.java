package com.simibubi.create.foundation.event.client;

import java.util.List;
import java.util.function.Consumer;

import com.simibubi.create.foundation.event.bus.Event;

import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;

/** Screen lifecycle hooks; fired from Fabric's ScreenEvents. */
public abstract class ScreenEvent extends Event {

	private final Screen screen;

	protected ScreenEvent(Screen screen) {
		this.screen = screen;
	}

	public Screen getScreen() {
		return screen;
	}

	public abstract static class Init extends ScreenEvent {
		private final Consumer<GuiEventListener> add;
		private final Consumer<GuiEventListener> remove;
		private final List<GuiEventListener> listenerList;

		protected Init(Screen screen, List<GuiEventListener> listenerList, Consumer<GuiEventListener> add, Consumer<GuiEventListener> remove) {
			super(screen);
			this.listenerList = listenerList;
			this.add = add;
			this.remove = remove;
		}

		public List<GuiEventListener> getListenersList() {
			return listenerList;
		}

		public void addListener(GuiEventListener listener) {
			add.accept(listener);
		}

		public void removeListener(GuiEventListener listener) {
			remove.accept(listener);
		}

		public static class Pre extends Init {
			public Pre(Screen screen, List<GuiEventListener> list, Consumer<GuiEventListener> add, Consumer<GuiEventListener> remove) {
				super(screen, list, add, remove);
			}
		}

		public static class Post extends Init {
			public Post(Screen screen, List<GuiEventListener> list, Consumer<GuiEventListener> add, Consumer<GuiEventListener> remove) {
				super(screen, list, add, remove);
			}
		}
	}

	/** Screen render pass; fired from Fabric's ScreenEvents.BEFORE/AFTER_RENDER. */
	public abstract static class Render extends ScreenEvent {
		private final net.minecraft.client.gui.GuiGraphics guiGraphics;
		private final int mouseX;
		private final int mouseY;
		private final float partialTick;

		protected Render(Screen screen, net.minecraft.client.gui.GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
			super(screen);
			this.guiGraphics = guiGraphics;
			this.mouseX = mouseX;
			this.mouseY = mouseY;
			this.partialTick = partialTick;
		}

		public net.minecraft.client.gui.GuiGraphics getGuiGraphics() {
			return guiGraphics;
		}

		public int getMouseX() {
			return mouseX;
		}

		public int getMouseY() {
			return mouseY;
		}

		public float getPartialTick() {
			return partialTick;
		}

		public static class Pre extends Render implements com.simibubi.create.foundation.event.bus.ICancellableEvent {
			public Pre(Screen screen, net.minecraft.client.gui.GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
				super(screen, guiGraphics, mouseX, mouseY, partialTick);
			}
		}

		public static class Post extends Render {
			public Post(Screen screen, net.minecraft.client.gui.GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
				super(screen, guiGraphics, mouseX, mouseY, partialTick);
			}
		}
	}
}
