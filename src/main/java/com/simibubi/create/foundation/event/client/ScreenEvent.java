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
}
