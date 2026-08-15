package com.tterrag.registrate;

/** Trivial concrete {@link AbstractRegistrate}, matching upstream's shape. */
public class Registrate extends AbstractRegistrate<Registrate> {

	public static Registrate create(String modid) {
		return new Registrate(modid);
	}

	protected Registrate(String modid) {
		super(modid);
	}
}
