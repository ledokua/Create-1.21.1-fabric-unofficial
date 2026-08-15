package com.simibubi.create.foundation.platform;

public enum LogicalSide {
	CLIENT, SERVER;

	public boolean isServer() {
		return this == SERVER;
	}

	public boolean isClient() {
		return this == CLIENT;
	}
}
