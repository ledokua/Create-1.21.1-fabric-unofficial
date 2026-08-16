package com.simibubi.create.foundation.mixin;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.schematics.SchematicProcessor;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureEntityInfo;

/**
 * NeoForge's {@code StructureProcessor#processEntity} pass in {@code StructureTemplate#placeEntities}, for Create's
 * {@link SchematicProcessor} (drops entities whose NBT only ops may set). The settings aren't passed to placeEntities,
 * so they're captured around the call in placeInWorld.
 */
@Mixin(StructureTemplate.class)
public class StructureTemplateEntityProcessorMixin {

	@Unique
	private static final ThreadLocal<StructurePlaceSettings> create$SETTINGS = new ThreadLocal<>();

	@WrapOperation(method = "placeInWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate;placeEntities(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Mirror;Lnet/minecraft/world/level/block/Rotation;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/levelgen/structure/BoundingBox;Z)V"))
	private void create$captureSettings(StructureTemplate template, ServerLevelAccessor level, BlockPos pos, net.minecraft.world.level.block.Mirror mirror,
		net.minecraft.world.level.block.Rotation rotation, BlockPos pivot, net.minecraft.world.level.levelgen.structure.BoundingBox box, boolean finalize,
		Operation<Void> original, @Local(argsOnly = true) StructurePlaceSettings settings) {
		StructurePlaceSettings previous = create$SETTINGS.get();
		create$SETTINGS.set(settings);
		try {
			original.call(template, level, pos, mirror, rotation, pivot, box, finalize);
		} finally {
			create$SETTINGS.set(previous);
		}
	}

	@WrapOperation(method = "placeEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate;createEntityIgnoreException(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/nbt/CompoundTag;)Ljava/util/Optional;"))
	private Optional<Entity> create$processEntity(ServerLevelAccessor level, CompoundTag tag, Operation<Optional<Entity>> original,
		@Local(argsOnly = true, ordinal = 0) BlockPos pos, @Local StructureEntityInfo rawInfo) {
		StructurePlaceSettings settings = create$SETTINGS.get();
		if (settings != null) {
			StructureEntityInfo info = new StructureEntityInfo(rawInfo.pos, rawInfo.blockPos, tag);
			for (StructureProcessor processor : settings.getProcessors()) {
				if (!(processor instanceof SchematicProcessor schematicProcessor))
					continue;
				info = schematicProcessor.processEntity(level, pos, rawInfo, info, settings, (StructureTemplate) (Object) this);
				if (info == null)
					return Optional.empty();
			}
			tag = info.nbt;
		}
		return original.call(level, tag);
	}
}
