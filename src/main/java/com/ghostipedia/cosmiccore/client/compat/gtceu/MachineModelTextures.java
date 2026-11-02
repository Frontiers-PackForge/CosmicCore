package com.ghostipedia.cosmiccore.client.compat.gtceu;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Map;
import java.util.function.Consumer;

public final class MachineModelTextures {

    private MachineModelTextures() {}

    public static void visit(JsonElement element, Consumer<JsonObject> operation) {
        if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(child -> visit(child, operation));
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
                visit(entry.getValue(), operation);
            }
        }
    }
}
