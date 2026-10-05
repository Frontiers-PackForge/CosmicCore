package com.ghostipedia.cosmiccore.mixin.client;

import com.ghostipedia.cosmiccore.common.data.worldgen.field.InitialOreSurvey;

import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.gui.screens.worldselection.CreateWorldScreen$WorldTab")
public abstract class StartingOreSurveyWorldTabMixin extends GridLayoutTab {

    protected StartingOreSurveyWorldTabMixin(Component title) {
        super(title);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void cosmiccore$addStartingSurveyRadius(CreateWorldScreen screen, CallbackInfo ci) {
        var state = screen.getUiState();
        CycleButton<Integer> radiusButton = CycleButton.<Integer>builder(radius -> radius == 0 ?
                CommonComponents.OPTION_OFF : Component.translatable("cosmiccore.survey.initial.radius", radius))
                .withValues(0, 1000, 2000, 3000, 4000, 5000)
                .withInitialValue(
                        InitialOreSurvey.normalizeRadius(state.getGameRules().getInt(InitialOreSurvey.RADIUS)))
                .withTooltip(radius -> Tooltip.create(Component.translatable("cosmiccore.survey.initial.tooltip")))
                .create(0, 0, 310, 20, Component.translatable("gamerule.cosmiccoreStartingOreSurveyRadius"),
                        (button, radius) -> {
                            var rules = state.getGameRules();
                            rules.getRule(InitialOreSurvey.RADIUS).set(radius, null);
                            state.setGameRules(rules);
                        });
        this.layout.addChild(radiusButton, 3, 0, 1, 2);
        state.addListener(updated -> {
            radiusButton
                    .setValue(InitialOreSurvey.normalizeRadius(updated.getGameRules().getInt(InitialOreSurvey.RADIUS)));
            radiusButton.active = !updated.isDebug();
        });
    }
}
