package com.ghostipedia.cosmiccore.mixin.ae2;


//CREDITS ; EasterFG & GTOCore for huge help on this mixin reference

import appeng.client.gui.WidgetContainer;
import appeng.client.gui.me.items.EncodingModePanel;
import appeng.client.gui.me.items.PatternEncodingTermScreen;
import appeng.client.gui.me.items.ProcessingEncodingPanel;
import com.ghostipedia.cosmiccore.client.gui.IPatternEncodingTerminalMenu;
import com.ghostipedia.cosmiccore.client.gui.ModifyIcon;
import com.ghostipedia.cosmiccore.client.gui.ModifyIconButton;
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
@Mixin(value = ProcessingEncodingPanel.class, remap = false)
public abstract class ProcessingEncodingPanelMixin extends EncodingModePanel {


    @Unique
    private ModifyIconButton cosCore$multTwo;
    @Unique
    private ModifyIconButton cosCore$multThree;
    @Unique
    private ModifyIconButton cosCore$multEight;
    @Unique
    private ModifyIconButton cosCore$divTwo;
    @Unique
    private ModifyIconButton cosCore$divThree;
    @Unique
    private ModifyIconButton cosCore$divEight;


    protected ProcessingEncodingPanelMixin(PatternEncodingTermScreen<?> screen, WidgetContainer widgets) {
        super(screen, widgets);
    }

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void init(PatternEncodingTermScreen<?> screen, WidgetContainer widgets, CallbackInfo ci) {
        System.out.println("HELP ME");
        cosCore$multTwo = new ModifyIconButton(b -> ((IPatternEncodingTerminalMenu) menu).cosCore$ModifyPattern(2), ModifyIcon.MULTIPLY_2,
                Component.translatable("coscore.pattern.multiply", 2),
                Component.translatable("coscore.pattern.tooltip.multiply", 2)
                );
        System.out.println("HELP ME2");
        cosCore$multThree = new ModifyIconButton(b -> ((IPatternEncodingTerminalMenu) menu).cosCore$ModifyPattern(3), ModifyIcon.MULTIPLY_3,
                Component.translatable("coscore.pattern.multiply", 3),
                Component.translatable("coscore.pattern.tooltip.multiply", 3)
        );
        System.out.println("HELP ME3");
        cosCore$multEight = new ModifyIconButton(b -> ((IPatternEncodingTerminalMenu) menu).cosCore$ModifyPattern(8), ModifyIcon.MULTIPLY_8,
                Component.translatable("coscore.pattern.multiply", 8),
                Component.translatable("coscore.pattern.tooltip.multiply", 8)
        );
        System.out.println("HELP ME4");
        cosCore$divTwo = new ModifyIconButton(b -> ((IPatternEncodingTerminalMenu) menu).cosCore$ModifyPattern(-2), ModifyIcon.DIVISION_2,
                Component.translatable("coscore.pattern.div", 2),
                Component.translatable("coscore.pattern.tooltip.div", 2)
        );
        System.out.println("HELP ME5");
        cosCore$divThree = new ModifyIconButton(b -> ((IPatternEncodingTerminalMenu) menu).cosCore$ModifyPattern(-3), ModifyIcon.DIVISION_3,
                Component.translatable("coscore.pattern.div", 3),
                Component.translatable("coscore.pattern.tooltip.div", 3)
        );
        System.out.println("HELP ME6");
        cosCore$divEight = new ModifyIconButton(b -> ((IPatternEncodingTerminalMenu) menu).cosCore$ModifyPattern(-8), ModifyIcon.DIVISION_8,
                Component.translatable("coscore.pattern.div", 8),
                Component.translatable("coscore.pattern.tooltip.div", 8)
        );

        widgets.add("mult2",cosCore$multTwo);
        widgets.add("mult3",cosCore$multThree);
        widgets.add("mult8",cosCore$multEight);
        widgets.add("div2",cosCore$divTwo);
        widgets.add("div3",cosCore$divThree);
        widgets.add("div8",cosCore$divEight);


    }

    @Inject(method = "setVisible", at = @At("TAIL"), remap = false)
    private void setVisibleHooks(boolean visible, CallbackInfo ci) {
        cosCore$multTwo.setVisibility(visible);
        cosCore$multThree.setVisibility(visible);
        cosCore$multEight.setVisibility(visible);
        cosCore$divTwo.setVisibility(visible);
        cosCore$divThree.setVisibility(visible);
        cosCore$divEight.setVisibility(visible);
    }
}
