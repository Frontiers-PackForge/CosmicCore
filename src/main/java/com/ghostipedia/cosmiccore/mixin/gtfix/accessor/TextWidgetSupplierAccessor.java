package com.ghostipedia.cosmiccore.mixin.gtfix.accessor;

import net.minecraft.network.chat.Component;

import brachy.modularui.widgets.TextWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Supplier;

@Mixin(value = TextWidget.class, remap = false)
public interface TextWidgetSupplierAccessor {

    @Accessor("keySupplier")
    Supplier<Component> cosmiccore$getKeySupplier();
}
