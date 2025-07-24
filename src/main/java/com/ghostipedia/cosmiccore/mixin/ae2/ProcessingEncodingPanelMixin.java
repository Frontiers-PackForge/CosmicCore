package com.ghostipedia.cosmiccore.mixin.ae2;

// CREDITS ; EasterFG & GTOCore for huge help on this mixin reference

import com.ghostipedia.cosmiccore.client.gui.IPatternEncodingTerminalMenu;
import com.ghostipedia.cosmiccore.client.gui.ModifyIcon;
import com.ghostipedia.cosmiccore.client.gui.ModifyIconButton;

import net.minecraft.network.chat.Component;

import appeng.client.gui.WidgetContainer;
import appeng.client.gui.me.items.EncodingModePanel;
import appeng.client.gui.me.items.PatternEncodingTermScreen;
import appeng.client.gui.me.items.ProcessingEncodingPanel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ProcessingEncodingPanel.class, remap = false)
public abstract class ProcessingEncodingPanelMixin extends EncodingModePanel {

    @Unique
    private ModifyIconButton cosmicCore$multTwo;
    @Unique
    private ModifyIconButton cosmicCore$multThree;
    @Unique
    private ModifyIconButton cosmicCore$multEight;
    @Unique
    private ModifyIconButton cosmicCore$divTwo;
    @Unique
    private ModifyIconButton cosmicCore$divThree;
    @Unique
    private ModifyIconButton cosmicCore$divEight;

    protected ProcessingEncodingPanelMixin(PatternEncodingTermScreen<?> screen, WidgetContainer widgets) {
        super(screen, widgets);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(PatternEncodingTermScreen<?> screen, WidgetContainer widgets, CallbackInfo ci) {
        cosmicCore$multTwo = new ModifyIconButton(
                b -> ((IPatternEncodingTerminalMenu) menu).cosmicCore$modifyPattern(2),
                ModifyIcon.MULTIPLY_2,
                Component.translatable("coscore.pattern.multiply", 2),
                Component.translatable("coscore.pattern.tooltip.multiply", 2));

        cosmicCore$multThree = new ModifyIconButton(
                b -> ((IPatternEncodingTerminalMenu) menu).cosmicCore$modifyPattern(3),
                ModifyIcon.MULTIPLY_3,
                Component.translatable("coscore.pattern.multiply", 3),
                Component.translatable("coscore.pattern.tooltip.multiply", 3));

        cosmicCore$multEight = new ModifyIconButton(
                b -> ((IPatternEncodingTerminalMenu) menu).cosmicCore$modifyPattern(8),
                ModifyIcon.MULTIPLY_8,
                Component.translatable("coscore.pattern.multiply", 8),
                Component.translatable("coscore.pattern.tooltip.multiply", 8));

        cosmicCore$divTwo = new ModifyIconButton(
                b -> ((IPatternEncodingTerminalMenu) menu).cosmicCore$modifyPattern(-2),
                ModifyIcon.DIVISION_2,
                Component.translatable("coscore.pattern.div", 2),
                Component.translatable("coscore.pattern.tooltip.div", 2));

        cosmicCore$divThree = new ModifyIconButton(
                b -> ((IPatternEncodingTerminalMenu) menu).cosmicCore$modifyPattern(-3),
                ModifyIcon.DIVISION_3,
                Component.translatable("coscore.pattern.div", 3),
                Component.translatable("coscore.pattern.tooltip.div", 3));

        cosmicCore$divEight = new ModifyIconButton(
                b -> ((IPatternEncodingTerminalMenu) menu).cosmicCore$modifyPattern(-8),
                ModifyIcon.DIVISION_8,
                Component.translatable("coscore.pattern.div", 8),
                Component.translatable("coscore.pattern.tooltip.div", 8));

        widgets.add("mult2", cosmicCore$multTwo);
        widgets.add("mult3", cosmicCore$multThree);
        widgets.add("mult8", cosmicCore$multEight);
        widgets.add("div2", cosmicCore$divTwo);
        widgets.add("div3", cosmicCore$divThree);
        widgets.add("div8", cosmicCore$divEight);
    }

    @Inject(method = "setVisible", at = @At("TAIL"))
    private void setVisibleHooks(boolean visible, CallbackInfo ci) {
        cosmicCore$multTwo.setVisibility(visible);
        cosmicCore$multThree.setVisibility(visible);
        cosmicCore$multEight.setVisibility(visible);
        cosmicCore$divTwo.setVisibility(visible);
        cosmicCore$divThree.setVisibility(visible);
        cosmicCore$divEight.setVisibility(visible);
    }
}
