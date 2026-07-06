package com.ghostipedia.cosmiccore.mixin.xaeroworldmap;

import com.ghostipedia.cosmiccore.client.map.RevealedField;
import com.ghostipedia.cosmiccore.client.map.RevealedFieldStorage;
import com.ghostipedia.cosmiccore.client.map.RevealedFields;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.map.gui.GuiMap;
import xaero.map.gui.IRightClickableElement;
import xaero.map.gui.dropdown.rightclick.RightClickOption;

import java.util.ArrayList;

@Mixin(value = GuiMap.class, remap = false)
public abstract class CCoreGuiMapDepleteMenuMixin implements IRightClickableElement {

    @Shadow
    private int rightClickX;

    @Shadow
    private int rightClickZ;

    @Shadow
    private ResourceKey<Level> rightClickDim;

    @Inject(method = "getRightClickOptions", at = @At("RETURN"), remap = false)
    private void cosmiccore$appendDepleteToggle(CallbackInfoReturnable<ArrayList<RightClickOption>> cir) {
        ResourceKey<Level> dimension = this.rightClickDim;
        if (dimension == null) return;
        RevealedField field = RevealedFields.INSTANCE.fieldAt(dimension, this.rightClickX, this.rightClickZ);
        if (field == null) return;
        ArrayList<RightClickOption> options = cir.getReturnValue();
        options.add(new RightClickOption("button.cosmiccore.toggle_depleted.name", options.size(), this) {

            @Override
            public void onAction(Screen screen) {
                RevealedFields.INSTANCE.toggleDepleted(dimension, field.x(), field.z());
                RevealedFieldStorage.save();
            }
        });
    }
}
