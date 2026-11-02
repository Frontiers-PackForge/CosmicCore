package com.ghostipedia.cosmiccore.mixin.gttweak.power;

import com.ghostipedia.cosmiccore.client.compat.gtceu.MachineModelTextures;

import com.gregtechceu.gtceu.client.model.machine.MachineModelLoader;
import com.gregtechceu.gtceu.client.model.machine.UnbakedMachineModel;

import net.minecraft.resources.ResourceLocation;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MachineModelLoader.class, remap = false)
public abstract class BatteryBufferModelFacesMixin {

    @Inject(
            method = "read(Lcom/google/gson/JsonObject;Lcom/google/gson/JsonDeserializationContext;)Lcom/gregtechceu/gtceu/client/model/machine/UnbakedMachineModel;",
            at = @At("HEAD"),
            require = 1,
            expect = 1,
            allow = 1)
    private void cosmiccore$setBatteryBufferFaces(JsonObject json, JsonDeserializationContext context,
                                                  CallbackInfoReturnable<UnbakedMachineModel> cir) {
        if (!json.has("machine")) {
            return;
        }
        ResourceLocation machineId = ResourceLocation.parse(json.get("machine").getAsString());
        if (!machineId.getNamespace().equals("gtceu")) {
            return;
        }
        int bufferSize = cosmiccore$getBatteryBufferSize(machineId.getPath());
        if (bufferSize > 0) {
            MachineModelTextures.visit(json, textures -> cosmiccore$setBatteryBufferFaces(textures, bufferSize));
        }
    }

    private static int cosmiccore$getBatteryBufferSize(String path) {
        if (path.endsWith("_battery_buffer_4x")) {
            return 4;
        }
        if (path.endsWith("_battery_buffer_8x")) {
            return 8;
        }
        if (path.endsWith("_battery_buffer_16x")) {
            return 16;
        }
        return 0;
    }

    private static void cosmiccore$setBatteryBufferFaces(JsonObject textures, int bufferSize) {
        String root = "gtceu:block/overlay/machine/overlay_energy_" + bufferSize + "a_";
        String tinted = "gtceu:block/overlay/machine/overlay_energy_" + bufferSize + "a_tinted";
        textures.addProperty("overlay_in_io", root + "out");
        textures.addProperty("overlay_in_io_emissive", root + "out_emissive");
        textures.addProperty("overlay_in_tinted", tinted);
        textures.addProperty("overlay_out_io", root + "in");
        textures.addProperty("overlay_out_io_emissive", root + "in_emissive");
        textures.addProperty("overlay_out_tinted", tinted);
    }
}
