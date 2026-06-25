package com.ghostipedia.cosmiccore.gtbridge.recipemaker;

import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A text field that also re-filters a list CLIENT-side as you type. LDLib's normal text responder only fires
 * server-side (through handleClientAction), and widget add/remove is not cross-synced mid-session - each side
 * rebuilds its own widget tree. So a server-only rebuild leaves the client's view stale (it keeps showing the
 * unfiltered list). This widget runs onClientType with the live text on the client so the picker can rebuild its
 * own list locally; the server responder rebuilds the server's copy identically, keeping click indices aligned.
 */
public class SearchFieldWidget extends TextFieldWidget {

    private final Consumer<String> onClientType;

    public SearchFieldWidget(int x, int y, int width, int height, Supplier<String> getter, Consumer<String> setter,
                             Consumer<String> onClientType) {
        super(x, y, width, height, getter, setter);
        this.onClientType = onClientType;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        boolean result = super.charTyped(codePoint, modifiers);
        if (result && onClientType != null) onClientType.accept(getRawCurrentString());
        return result;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean result = super.keyPressed(keyCode, scanCode, modifiers);
        if (result && onClientType != null) onClientType.accept(getRawCurrentString());
        return result;
    }
}
