package com.ghostipedia.cosmiccore.mixin.biomesoplenty;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.vehicle.ContainerEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "biomesoplenty.entity.ChestBoatBOP", remap = false)
public class ChestBoatBOPInventoryPersistenceMixin {

    @Inject(
            method = "addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V",
            at = @At("TAIL"),
            remap = false)
    private void cosmiccore$saveChestContents(CompoundTag nbt, CallbackInfo ci) {
        ContainerEntity chestBoat = (ContainerEntity) (Object) this;
        chestBoat.addChestVehicleSaveData(nbt, chestBoat.level().registryAccess());
    }

    @Inject(
            method = "readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V",
            at = @At("TAIL"),
            remap = false)
    private void cosmiccore$loadChestContents(CompoundTag nbt, CallbackInfo ci) {
        ContainerEntity chestBoat = (ContainerEntity) (Object) this;
        chestBoat.readChestVehicleSaveData(nbt, chestBoat.level().registryAccess());
    }
}
