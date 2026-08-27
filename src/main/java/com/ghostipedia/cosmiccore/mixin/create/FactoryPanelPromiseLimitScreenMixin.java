package com.ghostipedia.cosmiccore.mixin.create;

import com.ghostipedia.cosmiccore.common.compat.create.FactoryGaugePromiseLimit;
import com.ghostipedia.cosmiccore.common.compat.create.FactoryGaugePromiseLimitSupport;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.network.packet.FactoryGaugePromiseLimitPacket;

import net.createmod.catnip.gui.AbstractSimiScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelScreen;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FactoryPanelScreen.class)
public abstract class FactoryPanelPromiseLimitScreenMixin extends AbstractSimiScreen {

    @Shadow
    private FactoryPanelBehaviour behaviour;
    @Shadow
    private boolean restocker;

    @Unique
    private ScrollInput cosmiccore$promiseLimit;

    @Inject(method = "init", at = @At("RETURN"))
    private void cosmiccore$addPromiseLimit(CallbackInfo ci) {
        if (!FactoryGaugePromiseLimitSupport.supports(behaviour) ||
                !(behaviour instanceof FactoryGaugePromiseLimit promiseLimit)) {
            cosmiccore$promiseLimit = null;
            return;
        }
        boolean fluid = FactoryGaugePromiseLimitSupport.isFluid(behaviour);
        int upperBound = fluid ? 16_000_001 : restocker ? 128_001 : 1_001;
        cosmiccore$promiseLimit = new ScrollInput(guiLeft + 27, guiTop + windowHeight - 24, 40, 16)
                .withRange(-1, upperBound)
                .withStepFunction(context -> {
                    if ((context.currentValue < 0 && context.forward) ||
                            (context.currentValue == 0 && !context.forward)) {
                        return 1;
                    }
                    return fluid ? context.shift ? 1000 : context.control ? 100 : 10 : context.shift ? 10 : 1;
                })
                .withShiftStep(fluid ? 1000 : 10);
        cosmiccore$promiseLimit.setState(promiseLimit.cosmiccore$getPromiseLimit());
        cosmiccore$updateTitle();
        addRenderableWidget(cosmiccore$promiseLimit);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void cosmiccore$tickPromiseLimit(CallbackInfo ci) {
        cosmiccore$updateTitle();
    }

    @Inject(method = "renderWindow", at = @At("RETURN"))
    private void cosmiccore$renderPromiseLimit(
                                               GuiGraphics graphics, int mouseX, int mouseY, float partialTicks,
                                               CallbackInfo ci) {
        if (cosmiccore$promiseLimit == null) return;
        int configured = cosmiccore$promiseLimit.getState();
        int displayed = configured;
        if (displayed >= 0 && !restocker) {
            displayed = FactoryGaugePromiseLimitSupport.effectiveRecipeLimit(behaviour, displayed);
        }
        String text = displayed < 0 ? "---" : FactoryGaugePromiseLimitSupport.isFluid(behaviour) ?
                cosmiccore$formatFluid(displayed) : Integer.toString(displayed);
        graphics.drawString(
                font,
                text,
                cosmiccore$promiseLimit.getX() + 3,
                cosmiccore$promiseLimit.getY() + 4,
                0xffeeeeee,
                true);
    }

    @Inject(method = "sendIt", at = @At("RETURN"))
    private void cosmiccore$sendPromiseLimit(CallbackInfo ci) {
        if (cosmiccore$promiseLimit == null) return;
        CCoreNetwork.sendToServer(new FactoryGaugePromiseLimitPacket(
                behaviour.getPanelPosition(), cosmiccore$promiseLimit.getState()));
    }

    @Unique
    private void cosmiccore$updateTitle() {
        if (cosmiccore$promiseLimit == null) return;
        cosmiccore$promiseLimit.titled(Component.translatable(cosmiccore$promiseLimit.getState() < 0 ?
                "cosmiccore.gui.factory_gauge.promise_limit.none" : "cosmiccore.gui.factory_gauge.promise_limit"));
    }

    @Unique
    private static String cosmiccore$formatFluid(int amount) {
        if (amount >= 1_000_000) return amount / 1_000_000 + "kB";
        if (amount >= 100_000) return amount / 1_000 + "B";
        if (amount >= 10_000) return String.format(java.util.Locale.ROOT, "%.1fB", amount / 1000.0);
        if (amount >= 1_000) return String.format(java.util.Locale.ROOT, "%.2fB", amount / 1000.0);
        return amount + "mB";
    }
}
