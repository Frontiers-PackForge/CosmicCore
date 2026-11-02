package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.ghostipedia.cosmiccore.client.compat.gtceu.MachineModelTextures;

import com.gregtechceu.gtceu.client.model.machine.MachineModelLoader;
import com.gregtechceu.gtceu.client.model.machine.UnbakedMachineModel;

import net.minecraft.resources.ResourceLocation;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MachineModelLoader.class, remap = false)
public abstract class MachineModelLoaderPowerFaceFixMixin {

    @Inject(
            method = "read(Lcom/google/gson/JsonObject;Lcom/google/gson/JsonDeserializationContext;)Lcom/gregtechceu/gtceu/client/model/machine/UnbakedMachineModel;",
            at = @At("HEAD"),
            require = 1,
            expect = 1,
            allow = 1)
    private void cosmiccore$correctDiodeFaces(JsonObject json, JsonDeserializationContext context,
                                              CallbackInfoReturnable<UnbakedMachineModel> cir) {
        if (!json.has("machine")) {
            return;
        }
        ResourceLocation machineId = ResourceLocation.parse(json.get("machine").getAsString());
        if (!machineId.getNamespace().equals("gtceu")) {
            return;
        }
        String path = machineId.getPath();
        if (path.endsWith("_diode")) {
            MachineModelTextures.visit(json, MachineModelLoaderPowerFaceFixMixin::cosmiccore$swapDiodeFaces);
        }
    }

    private static void cosmiccore$swapDiodeFaces(JsonObject textures) {
        cosmiccore$swap(textures, "overlay_in_io", "overlay_out_io");
        cosmiccore$swap(textures, "overlay_in_io_emissive", "overlay_out_io_emissive");
        cosmiccore$swap(textures, "overlay_in_tinted", "overlay_out_tinted");
    }

    private static void cosmiccore$swap(JsonObject textures, String first, String second) {
        JsonElement firstValue = textures.get(first);
        JsonElement secondValue = textures.get(second);
        if (firstValue == null || secondValue == null) {
            return;
        }
        textures.add(first, secondValue);
        textures.add(second, firstValue);
    }
}
