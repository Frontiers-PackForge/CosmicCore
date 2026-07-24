package com.ghostipedia.cosmiccore.integration.emi.favorites;

import dev.emi.emi.input.EmiInput;

public final class CosmicBookmarkUiState {

    private static boolean alertVisible = true;

    private CosmicBookmarkUiState() {}

    public static boolean isAlertVisible() {
        return alertVisible;
    }

    public static void toggleAlert() {
        alertVisible = !alertVisible;
    }

    public static boolean isForceDeleteModifierDown() {
        return EmiInput.isShiftDown() && EmiInput.isControlDown() && EmiInput.isAltDown();
    }
}
