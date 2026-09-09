package com.ghostipedia.cosmiccore.common.compat.gtceu.ae2;

import brachy.modularui.api.widget.Interactable;
import brachy.modularui.widgets.textfield.TextFieldWidget;
import com.mojang.blaze3d.platform.InputConstants;

public final class MEScrollableAmountField extends TextFieldWidget {

    private int maximum = Integer.MAX_VALUE;

    @Override
    public TextFieldWidget setNumbers(int min, int max) {
        maximum = max;
        return super.setNumbers(min, max);
    }

    @Override
    public boolean onMouseScrolled(double scrollX, double scrollY) {
        if (scrollY == 0 || !Double.isFinite(scrollY)) return false;
        int next = MEConfigAmounts.scroll((long) parse(getText()), scrollY, Interactable.hasControlDown(), maximum);
        String text = Integer.toString(next);
        getStringValue().setStringValue(text);
        setText(text);
        markTooltipDirty();
        return true;
    }

    @Override
    public Interactable.Result onKeyPressed(int keyCode, int scanCode, int modifiers) {
        if (isFocused() && (keyCode == InputConstants.KEY_RETURN || keyCode == InputConstants.KEY_NUMPADENTER)) {
            onRemoveFocus(getContext());
            return Interactable.Result.SUCCESS;
        }
        return super.onKeyPressed(keyCode, scanCode, modifiers);
    }
}
