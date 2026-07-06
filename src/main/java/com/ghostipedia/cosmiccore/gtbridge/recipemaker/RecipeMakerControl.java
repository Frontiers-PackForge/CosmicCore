package com.ghostipedia.cosmiccore.gtbridge.recipemaker;

import com.ghostipedia.cosmiccore.client.recipemaker.RecipeMakerClipboard;

import net.minecraft.network.RegistryFriendlyByteBuf;

import brachy.modularui.value.sync.SyncHandler;

import java.util.function.Supplier;

/**
 * Client-to-server channel for the Copy button. The active editor sets an exporter that reads the recipe state on the
 * server (where the synced slots and fields live); the button triggers it and the built KubeJS line is shipped back to
 * drop on the client clipboard. The exporter is reassigned each time the editor rebuilds for a new recipe type.
 */
public class RecipeMakerControl extends SyncHandler<RecipeMakerControl> {

    private static final int EXPORT = 1;
    private static final int RESULT = 2;

    private Supplier<String> exporter = () -> "";

    public void setExporter(Supplier<String> exporter) {
        this.exporter = exporter;
    }

    public void requestExport() {
        syncToServer(EXPORT);
    }

    @Override
    public void readOnServer(int id, RegistryFriendlyByteBuf buf) {
        if (id == EXPORT) {
            String result = exporter.get();
            syncToClient(RESULT, buffer -> buffer.writeUtf(result));
        }
    }

    @Override
    public void readOnClient(int id, RegistryFriendlyByteBuf buf) {
        if (id == RESULT) {
            RecipeMakerClipboard.copy(buf.readUtf());
        }
    }
}
