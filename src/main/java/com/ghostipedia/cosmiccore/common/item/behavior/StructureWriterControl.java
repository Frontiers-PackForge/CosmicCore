package com.ghostipedia.cosmiccore.common.item.behavior;

import com.ghostipedia.cosmiccore.client.recipemaker.RecipeMakerClipboard;

import net.minecraft.network.RegistryFriendlyByteBuf;

import brachy.modularui.value.sync.SyncHandler;

import java.util.function.IntConsumer;
import java.util.function.Supplier;

public final class StructureWriterControl extends SyncHandler<StructureWriterControl> {

    public static final int PRINT = 1;
    public static final int ROTATE_X = 2;
    public static final int ROTATE_Y = 3;
    public static final int CLEAR = 4;

    private static final int COPY = 5;
    private static final int COPY_RESULT = 6;
    private static final int MAX_EXPORT_LENGTH = 1_000_000;

    private Supplier<String> exporter = () -> "";
    private IntConsumer actionHandler = action -> {};

    public void setExporter(Supplier<String> exporter) {
        this.exporter = exporter;
    }

    public void setActionHandler(IntConsumer actionHandler) {
        this.actionHandler = actionHandler;
    }

    public void requestCopy() {
        syncToServer(COPY);
    }

    public void requestAction(int action) {
        syncToServer(action);
    }

    @Override
    public void readOnServer(int id, RegistryFriendlyByteBuf buf) {
        if (id == COPY) {
            String result = exporter.get();
            if (!result.isEmpty()) {
                syncToClient(COPY_RESULT, buffer -> buffer.writeUtf(result, MAX_EXPORT_LENGTH));
            }
        } else if (id >= PRINT && id <= CLEAR) {
            actionHandler.accept(id);
        }
    }

    @Override
    public void readOnClient(int id, RegistryFriendlyByteBuf buf) {
        if (id == COPY_RESULT) {
            RecipeMakerClipboard.copy(buf.readUtf(MAX_EXPORT_LENGTH));
        }
    }
}
