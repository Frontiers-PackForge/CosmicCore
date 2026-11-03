package com.ghostipedia.cosmiccore.mixin.apothicspawners;

import com.ghostipedia.cosmiccore.common.compat.apothic.SpawnerEntityPolicy;

import net.minecraft.nbt.CompoundTag;

import dev.shadowsoffire.apothic_spawners.block.ApothSpawnerTile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ApothSpawnerTile.class)
public abstract class ApothSpawnerEntityPolicyMixin {

    @ModifyVariable(method = "loadAdditional", at = @At("HEAD"), argsOnly = true)
    private CompoundTag cosmiccore$restrictSpawnerEntities(CompoundTag tag) {
        return SpawnerEntityPolicy.sanitize(tag);
    }
}
