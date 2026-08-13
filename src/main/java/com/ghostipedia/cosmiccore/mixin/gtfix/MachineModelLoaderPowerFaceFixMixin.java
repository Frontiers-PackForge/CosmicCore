package com.ghostipedia.cosmiccore.mixin.gtfix;

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

import java.util.Map;

@Mixin(value = MachineModelLoader.class, remap = false)
public abstract class MachineModelLoaderPowerFaceFixMixin {

    @Inject(
            method = "read(Lcom/google/gson/JsonObject;Lcom/google/gson/JsonDeserializationContext;)Lcom/gregtechceu/gtceu/client/model/machine/UnbakedMachineModel;",
            at = @At("HEAD"),
            require = 1,
            expect = 1,
            allow = 1)
    private void cosmiccore$correctPowerFaces(JsonObject json, JsonDeserializationContext context,
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
            cosmiccore$rewriteTextures(json, MachineModelLoaderPowerFaceFixMixin::cosmiccore$swapDiodeFaces);
            return;
        }
        int bufferSize = cosmiccore$getBatteryBufferSize(path);
        if (bufferSize > 0) {
            cosmiccore$rewriteTextures(json, textures -> cosmiccore$setBatteryBufferFaces(textures, bufferSize));
        }
    }

    private static void cosmiccore$rewriteTextures(JsonElement element,
                                                   java.util.function.Consumer<JsonObject> operation) {
        if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(child -> cosmiccore$rewriteTextures(child, operation));
            return;
        }
        if (!element.isJsonObject()) {
            return;
        }
        JsonObject object = element.getAsJsonObject();
        if (object.has("textures") && object.get("textures").isJsonObject()) {
            operation.accept(object.getAsJsonObject("textures"));
        }
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            if (!entry.getKey().equals("textures")) {
                cosmiccore$rewriteTextures(entry.getValue(), operation);
            }
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
