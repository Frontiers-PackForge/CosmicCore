package com.ghostipedia.cosmiccore.gtbridge.recipemaker;

import com.ghostipedia.cosmiccore.client.recipemaker.RecipeMakerClipboard;

import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import brachy.modularui.value.sync.SyncHandler;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Client-to-server channel for the Copy button. The active editor sets an exporter that reads the recipe state on the
 * server (where the synced slots and fields live); the button triggers it and the built KubeJS line is shipped back to
 * drop on the client clipboard. The exporter is reassigned each time the editor rebuilds for a new recipe type.
 */
public class RecipeMakerControl extends SyncHandler<RecipeMakerControl> {

    private static final int EXPORT = 1;
    private static final int RESULT = 2;
    private static final int SELECT_EDITOR = 3;

    private Supplier<RecipeExportResult> exporter = () -> RecipeExportResult.copied("");
    private Consumer<String> editorSelector = typeId -> {};

    public void setExporter(Supplier<RecipeExportResult> exporter) {
        this.exporter = exporter;
    }

    public void requestExport() {
        syncToServer(EXPORT);
    }

    public void setEditorSelector(Consumer<String> editorSelector) {
        this.editorSelector = editorSelector;
    }

    public void requestEditor(String typeId) {
        syncToServer(SELECT_EDITOR, buf -> buf.writeUtf(typeId, 256));
    }

    @Override
    public void readOnServer(int id, RegistryFriendlyByteBuf buf) {
        Player player = getSyncManager().getPlayer();
        if (!player.hasPermissions(4)) {
            player.sendSystemMessage(Component.translatable("cosmiccore.recipe_maker.access_denied")
                    .withStyle(ChatFormatting.RED));
            return;
        }
        if (id == EXPORT) {
            RecipeExportResult result = exporter.get();
            if (result.message() != null) player.sendSystemMessage(result.message());
            if (result.copy()) syncToClient(RESULT, buffer -> buffer.writeUtf(result.script()));
        } else if (id == SELECT_EDITOR) {
            editorSelector.accept(buf.readUtf(256));
        }
    }

    @Override
    public void readOnClient(int id, RegistryFriendlyByteBuf buf) {
        if (id == RESULT) {
            RecipeMakerClipboard.copy(buf.readUtf());
        }
    }
}
