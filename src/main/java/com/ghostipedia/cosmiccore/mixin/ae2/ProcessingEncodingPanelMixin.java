package com.ghostipedia.cosmiccore.mixin.ae2;


//CREDITS ; EasterFG & GTOCore for huge help on this mixin reference

import appeng.client.gui.WidgetContainer;
import appeng.client.gui.me.items.EncodingModePanel;
import appeng.client.gui.me.items.PatternEncodingTermScreen;
import appeng.client.gui.me.items.ProcessingEncodingPanel;
import com.ghostipedia.cosmiccore.client.gui.IPatternEcodingTerminalMenu;
import com.ghostipedia.cosmiccore.client.gui.ModifyStackButton;
import net.minecraft.network.chat.Component;

import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Debug(
        export = true
)
@Mixin(ProcessingEncodingPanel.class)
public abstract class ProcessingEncodingPanelMixin extends EncodingModePanel {


    @Unique
    private ModifyStackButton cosCore$multTwo;
    @Unique
    private ModifyStackButton cosCore$multThree;
    @Unique
    private ModifyStackButton cosCore$multEight;
    @Unique
    private ModifyStackButton cosCore$divTwo;
    @Unique
    private ModifyStackButton cosCore$divThree;
    @Unique
    private ModifyStackButton cosCore$divEight;


    protected ProcessingEncodingPanelMixin(PatternEncodingTermScreen<?> screen, WidgetContainer widgets) {
        super(screen, widgets);
    }

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void init(PatternEncodingTermScreen<?> screen, WidgetContainer widgets, CallbackInfo ci) {
        cosCore$multTwo = new ModifyStackButton(b -> ((IPatternEcodingTerminalMenu)menu).cosCore$ModifyPattern(2), ModifyStackButton.MultiplierIcons.MULT_2,
                Component.translatable("coscore.pattern.multiply", 2),
                Component.translatable("coscore.pattern.tooltip.multiply", 2)
                );
        cosCore$multThree = new ModifyStackButton(b -> ((IPatternEcodingTerminalMenu)menu).cosCore$ModifyPattern(3), ModifyStackButton.MultiplierIcons.MULT_3,
                Component.translatable("coscore.pattern.multiply", 2),
                Component.translatable("coscore.pattern.tooltip.multiply", 2)
        );
        cosCore$multEight = new ModifyStackButton(b -> ((IPatternEcodingTerminalMenu)menu).cosCore$ModifyPattern(8), ModifyStackButton.MultiplierIcons.MULT_8,
                Component.translatable("coscore.pattern.multiply", 2),
                Component.translatable("coscore.pattern.tooltip.multiply", 2)
        );
        widgets.add("mult2",cosCore$multTwo);
        widgets.add("mult2",cosCore$multThree);
        widgets.add("mult2",cosCore$multEight);

    }

    @Inject(method = "setVisible", at = @At("TAIL"), remap = false)
    private void setVisibleHooks(boolean visible, CallbackInfo ci) {
        cosCore$multTwo.setVisibility(visible);
        cosCore$multThree.setVisibility(visible);
        cosCore$multEight.setVisibility(visible);
    }
}
