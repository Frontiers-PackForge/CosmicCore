package com.ghostipedia.cosmiccore.mixin.neoforge;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class LegacyAccessoriesPlayerDataScrubberMixin {

    @Inject(method = "load", at = @At("HEAD"))
    private void cosmiccore$scrubLegacyAccessoriesAttachment(CompoundTag root, CallbackInfo ci) {
        if ((Object) this instanceof Player && root.contains("neoforge:attachments", Tag.TAG_COMPOUND)) {
            root.getCompound("neoforge:attachments").remove("accessories:inventory_holder");
        }
    }
}
