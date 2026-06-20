package com.ghostipedia.cosmiccore.gtbridge.recipemaker;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.function.Supplier;

/**
 * Export button for the recipe-maker. The press runs server-side (where the slot/field state lives), builds the
 * KubeJS line, and ships it to the client via LDLib's widget update channel; {@link #readUpdateInfo} then runs
 * client-side and drops it on the clipboard. Avoids needing a bespoke network payload for a client-only action.
 */
public class ExportButtonWidget extends ButtonWidget {

    private static final int CLIPBOARD = 1;

    public ExportButtonWidget(int x, int y, int width, int height, IGuiTexture texture, Supplier<String> exporter) {
        super(x, y, width, height, texture, null);
        setOnPressCallback(data -> writeUpdateInfo(CLIPBOARD, buffer -> buffer.writeUtf(exporter.get())));
    }

    @Override
    public void readUpdateInfo(int id, RegistryFriendlyByteBuf buffer) {
        if (id == CLIPBOARD) {
            Minecraft.getInstance().keyboardHandler.setClipboard(buffer.readUtf());
        } else {
            super.readUpdateInfo(id, buffer);
        }
    }
}
