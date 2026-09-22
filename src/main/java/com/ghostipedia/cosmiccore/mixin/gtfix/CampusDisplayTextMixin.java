package com.ghostipedia.cosmiccore.mixin.gtfix;

import com.ghostipedia.cosmiccore.mixin.gtfix.accessor.TextWidgetSupplierAccessor;

import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import brachy.modularui.api.widget.IWidget;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.Widget;
import brachy.modularui.widgets.TextWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.Set;

@Mixin(value = WorkableElectricMultiblockMachine.class, remap = false)
public abstract class CampusDisplayTextMixin {

    @Unique
    private static final Set<String> cosmiccore$whiteDisplays = Set.of(
            "mechanical_mineshaft", "essence_reactor", "hemophagic_transfuser", "imbument_pylon");

    @Inject(method = "getMainTextPanel", at = @At("RETURN"))
    private void cosmiccore$whiteDisplay(PanelSyncManager syncManager, CallbackInfoReturnable<Widget<?>> cir) {
        var id = ((WorkableElectricMultiblockMachine) (Object) this).getDefinition().getId();
        if (id.getNamespace().equals("cosmiccore") && cosmiccore$whiteDisplays.contains(id.getPath())) {
            cosmiccore$whiten(cir.getReturnValue());
        }
    }

    @Unique
    private static void cosmiccore$whiten(IWidget widget) {
        if (widget instanceof TextWidget<?> text) {
            var supplier = ((TextWidgetSupplierAccessor) text).cosmiccore$getKeySupplier();
            var original = text.getKey();
            text.color(0xFFFFFFFF).value(() -> {
                var source = supplier == null ? original : supplier.get();
                var result = Component.empty();
                source.visit((style, value) -> {
                    result.append(Component.literal(value).setStyle(style.withColor(0xFFFFFF)));
                    return Optional.empty();
                }, Style.EMPTY);
                return result;
            });
        }
        widget.getChildren().forEach(CampusDisplayTextMixin::cosmiccore$whiten);
    }
}
