package com.simibubi.create.foundation.mixin;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import com.simibubi.create.compat.Mods;

public class CreateMixinPlugin implements IMixinConfigPlugin {
	@Override
	public void onLoad(String mixinPackage) {
	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		// Map-mod integrations (compat/trainmap): their target classes only exist when the mod is installed.
		if (mixinClassName.startsWith("com.simibubi.create.foundation.mixin.compat.xaeros"))
			return Mods.XAEROWORLDMAP.isLoaded();
		if (mixinClassName.startsWith("com.simibubi.create.foundation.mixin.compat.journeymap"))
			return Mods.JOURNEYMAP.isLoaded();
		if (mixinClassName.startsWith("com.simibubi.create.foundation.mixin.compat.ftbchunks"))
			return Mods.FTBCHUNKS.isLoaded();
		return true;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
	}

	@Override
	public List<String> getMixins() {
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}

}
